package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

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
		//在WeatherActivity中，允许切换城市，因此这里首先判断一下
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//首先根据配置文件中的内容，查询城市city_selected这一项是不是为true，如果为true
		//那么就直接跳转到WeatherActivity上面去了。
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// 默认取值为false
		//如果已经选择了城市，并且不是从WeatherActivity跳转过来的。
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		//如果还没有选中城市，那么就从这里开始
		//取消标题栏
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
					//将这个选中的城市写入到一个本地文件中，然后启动一个intent
					String areaCode = currentAreas.get(index).getAreaCode();
					queryWeatherCode(areaCode);
					
				}
					
				
			}
			
		});
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		queryArea(0);// 加载省级数据
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
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						
					}
					
				});
				
			}
			
		});
	}
	private ProgressDialog progressDialog;
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭进度对话框
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
