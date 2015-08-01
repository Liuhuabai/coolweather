/**
 * 
 */
package com.example.coolweather.service;

import com.example.coolweather.receiver.AutoUpdateReceiver;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.LogUtil;
import com.example.coolweather.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

/**
 * @author liuhuabai
 *
 */
public class AutoUpdateService extends Service {

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable(){

			@Override
			public void run() {
				updateWeatehr();
				
			}
		}).start();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 8*60*60*1000;  //8小时更新一次
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void updateWeatehr() {
		//更新天气
		LogUtil.v("AutoUpdateService", "query Weather by Weather Code");
		//首先获取weather code
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		
		String address = "http://www.weather.com.cn/data/cityinfo/" +
				weatherCode + ".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
				
			}

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				
			}
			
		});
		
	}

}
