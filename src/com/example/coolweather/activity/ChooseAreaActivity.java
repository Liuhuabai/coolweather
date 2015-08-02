package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import net.youmi.android.AdManager;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.Area;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.LogUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {

	
	private TextView titletext;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private List<String> dataList = new ArrayList<String>();
	private List<Area> currentAreas;
	
	private CoolWeatherDB coolWeatherDB;
	
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ������׹��ĳ�ʼ������ ����id Ӧ����Կ �ǲ��ǲ���ģʽ
		AdManager.getInstance(this).init("d55174db1308b0ef", "4c9ae4fbcc6b1a6a", false);
		//��WeatherActivity�У������л����У�������������ж�һ��
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//���ȸ��������ļ��е����ݣ���ѯ����city_selected��һ���ǲ���Ϊtrue�����Ϊtrue
		//��ô��ֱ����ת��WeatherActivity����ȥ�ˡ�
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// Ĭ��ȡֵΪfalse
		//����Ѿ�ѡ���˳��У����Ҳ��Ǵ�WeatherActivity��ת�����ġ�
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		//�����û��ѡ�г��У���ô�ʹ����￪ʼ
		//ȡ��������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titletext = (TextView) findViewById(R.id.title_text);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				int selectId = currentAreas.get(index).getId();
				
				if(currentLevel < 2) {
					currentLevel++;
					queryArea(selectId);
				} else {
					//�����ѡ�еĳ���д�뵽һ�������ļ��У�Ȼ������һ��intent
					String areaCode = currentAreas.get(index).getAreaCode();
					queryWeatherCode(areaCode);
					
				}
					
				
			}
			
		});
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		queryArea(0);// ����ʡ������
	}
	
	@Override
	public void onBackPressed() {
		currentLevel-- ;
		if(currentLevel >= 0 ) {
			Area superarea = coolWeatherDB.getArea(currentAreas.get(0).getSuperId());
			queryArea(superarea.getSuperId());
		} else {
			finish();
		}
	}
	
	private int currentLevel = 0;
	
	private void queryArea(int superId) {
		
		Area superarea = coolWeatherDB.getArea(superId);
		titletext.setText(superarea.getAreaName());
		String  code = superarea.getAreaCode();
		
		currentAreas = coolWeatherDB.loadAreas(superId);
		LogUtil.v("ChooseAreaActivity", "Try to Get Data From Database");
		if(currentAreas.size() > 0) {
			LogUtil.v("ChooseAreaActivity", "Get Data From Database Successfully...");
			dataList.clear();
			for(Area area : currentAreas) {
				dataList.add(area.getAreaName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
		} else {
			LogUtil.d("ChooseAreaActivity", "Get Data From Database Fail...");
			queryFromServer(code,superId);
		}
		
	}
	private String weatherCode;
	private void queryWeatherCode(final String code) {
		LogUtil.v("ChooseAreaActivity", "Try to get Weather Code  From Web");
		String address = "http://www.weather.com.cn/data/list3/city" + code +
				".xml";
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				String [] array = response.split("\\|");
				if(array != null && array.length == 2) {
					weatherCode = array[1];	
				}
				LogUtil.v("ChooseAreaActivity", "query Weather Code From Web Successfully...");
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
						LogUtil.v("ChooseAreaActivity", weatherCode);
						intent.putExtra("weather_code", weatherCode);
						startActivity(intent);
						finish();
					}
					
				});
				
			}

			@Override
			public void onError(Exception e) {
				weatherCode = null;
			}
			
		});
	}
	private void queryFromServer(final String code,final int superId) {
		LogUtil.v("ChooseAreaActivity", "Try to DownLoad Data From Web To Database");
		String address = "http://www.weather.com.cn/data/list3/city" + code +
				".xml";
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				LogUtil.v("ChooseAreaActivity", "DownLoad Data From Web To Database Successfully...");
				boolean result =
						Utility.handleArea(coolWeatherDB, response, superId);
				if(result) {
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							closeProgressDialog();
							queryArea(superId);
						}
						
					});
				}
				
			}

			@Override
			public void onError(Exception e) {
				LogUtil.d("ChooseAreaActivity", "DownLoad Data From Web To Database Fail...");
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
			}
			
		});
	}
	private ProgressDialog progressDialog;
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
