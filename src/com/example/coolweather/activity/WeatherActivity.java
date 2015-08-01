/**
 * 
 */
package com.example.coolweather.activity;

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
	 * 显示城市名称
	 */
	private TextView cityNameText;
	/**
	 * 显示发布日期
	 */
	private TextView publishText;
	/**
	 * 显示天气情况
	 */
	private TextView weatherDespText;
	/**
	 * 温度1
	 */
	private TextView temp1Text;
	/**
	 * 温度2
	 */
	private TextView temp2Text;
	/**
	 * 当前日期
	 */
	private TextView currentDateText;
	/**
	 * 切换城市
	 */
	private Button switchCity;
	/**
	 * 更新天气
	 */
	private Button refresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//取消标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各个控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		
		switchCity = (Button) findViewById(R.id.switch_city);
		switchCity.setOnClickListener(this);
		refresh = (Button) findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
		
		String weatherCode = getIntent().getStringExtra("weather_code");
		if(!TextUtils.isEmpty(weatherCode)) {
			//直接查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherCode);
		} else {
			showWeather();
		}
		
	}
	
	/**
	 * 从SharedPreferences文件中读取存储的天气信息,并显示到界面上。
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		//设置为可见
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
	}


	private void queryWeatherInfo(String weatherCode) {
		LogUtil.v("WeatherActivity", "query Weather by Weather Code");
		String address = "http://www.weather.com.cn/data/cityinfo/" +
				weatherCode + ".html";
		queryFromServer(address);
		
	}


	private void queryFromServer(String address) {
		//开启服务，然后写入到配置文件中去
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(WeatherActivity.this, response);
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("同步成功");
						showWeather();
						
					}
				});
				
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
				
			}
			
		});
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.switch_city:
			//直接切换好changearea那个activity去
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh:
			publishText.setText("同步中...");
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
