/**
 * 
 */
package com.example.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.Area;

/**
 * 从网站上面读取到的数据是
 * 代号｜城市，代号|城市这样的，所以解析这样的数据
 * @author liuhuabai
 *
 */
public class Utility {
	public synchronized static boolean handleArea(CoolWeatherDB coolWeatherDB,String response , int superId) {
		if(!TextUtils.isEmpty(response)) {
			String [] allAreas = response.split(",");
			if((allAreas != null) && (allAreas.length > 0)) {
				String [] array;
				Area area;
				for(String c : allAreas) {
					array = c.split("\\|");
					area = new Area();
					area.setAreaCode(array[0]);
					area.setAreaName(array[1]);
					area.setSuperId(superId);
					//将解析出来的数据存储到Area表。
					coolWeatherDB.saveArea(area);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析从服务器返回得到JSON数据
	 * @param context
	 * @param response
	 */
	public static void handleWeatherResponse(Context context,String response) {
		
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			//最低温度在json数据中是temp2，这里调转一下
			String temp1 = weatherInfo.getString("temp2");
			//最高温度在json数据中是temp1，这里调转一下
			String temp2 = weatherInfo.getString("temp1");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中去
	 * @param context
	 * @param cityName
	 * @param weatherCode
	 * @param temp1
	 * @param temp2
	 * @param weatherDesp
	 * @param publishTime
	 */
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		//提交
		editor.commit();
		
	}
}
