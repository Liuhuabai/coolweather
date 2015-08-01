/**
 * 
 */
package com.example.coolweather.activity;

import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.LogUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author liuhuabai
 *
 */
public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	
	/**
	 * ��ʾ��������
	 */
	private TextView cityNameText;
	/**
	 * ��ʾ��������
	 */
	private TextView publishText;
	/**
	 * ��ʾ�������
	 */
	private TextView weatherDespText;
	/**
	 * �¶�1
	 */
	private TextView temp_current;
	/**
	 * �¶�2
	 */
	private TextView temp_today;
	
	private TextView wind_direction;
	/**
	 * ��ǰ����
	 */
	private TextView currentDateText;
	/**
	 * �л�����
	 */
	private Button switchCity;
	/**
	 * ��������
	 */
	private Button refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//ȡ��������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ�������ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		
		currentDateText = (TextView) findViewById(R.id.current_date);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		
		temp_current = (TextView) findViewById(R.id.temp_current);
		wind_direction = (TextView)findViewById(R.id.wind_direction);
		temp_today = (TextView) findViewById(R.id.temp_today);
		
		
		switchCity = (Button) findViewById(R.id.switch_city);
		switchCity.setOnClickListener(this);
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
		
		String weatherCode = getIntent().getStringExtra("weather_code");
		if(!TextUtils.isEmpty(weatherCode)) {
			//ֱ�Ӳ�ѯ����
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherCode);
		} else {
			showWeather();
		}
		
	}
	
	/**
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ,����ʾ�������ϡ�
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		publishText.setText(prefs.getString("publish_time", "")+"����");
		
		currentDateText.setText("����:"+prefs.getString("current_date", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		temp_current.setText(prefs.getString("temp_current", ""));
		wind_direction.setText(prefs.getString("wind_Direction", ""));
		temp_today.setText(prefs.getString("temp_today", ""));
		
		
		//����Ϊ�ɼ�
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		//��service����
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);
		LogUtil.v("WeatherActivity", "Start Service");
		
	}


	private void queryWeatherInfo(String weatherCode) {
		LogUtil.v("WeatherActivity", "query Weather by Weather Code");
		String address0 = "http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode=" +
				weatherCode + "&weatherType=0";
		String address1 = "http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode=" +
				weatherCode + "&weatherType=1";
		//http://weather.51wnl.com/weatherinfo/GetMoreWeather?cityCode=101040100&weatherType=0
		queryFromServer(address0,address1);
		
	}


	private void queryFromServer(String address0,String address1) {
		
		//��������Ȼ��д�뵽�����ļ���ȥ
		HttpUtil.sendHttpRequest(address0, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				
				Utility.handleWeatherResponse(WeatherActivity.this, response,0);
				
			}

			@Override
			public void onError(Exception e) {
				
				
			}
			
		});
		
		HttpUtil.sendHttpRequest(address1, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(WeatherActivity.this, response,1);
				//Utility.handleWeatherResponse(WeatherActivity.this, response,0);
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("ͬ���ɹ�");
						showWeather();
						
					}
				});
				
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
				
			}
			
		});
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.switch_city:
			//ֱ���л���changearea�Ǹ�activityȥ
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
		
	}

}
