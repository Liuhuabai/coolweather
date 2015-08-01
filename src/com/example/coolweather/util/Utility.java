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
	public static void handleWeatherResponse(Context context,String response,int type) {
		
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp_today,temp_current,weatherDesp,publishTime,windDirection;
			if(type == 0) {
				//今天的温度
				temp_today = weatherInfo.getString("temp1");
				//明天的温度
				//String temp2 = weatherInfo.getString("temp2");
				weatherDesp = weatherInfo.getString("weather1");
				LogUtil.v("temp1", temp_today);
				LogUtil.v("weatherDesp", weatherDesp);
				saveWeatherInfo(context,cityName,weatherCode,temp_today,weatherDesp);
				LogUtil.d("Utility", "Save Successfully...");
			} else if(type == 1) {
				temp_current = weatherInfo.getString("temp");
				windDirection = weatherInfo.getString("WD");
				publishTime = weatherInfo.getString("time");
				LogUtil.v("temp_current", temp_current);
				LogUtil.v("windDirection", windDirection);
				LogUtil.v("publishTime", publishTime);

				saveWeatherInfo(context,temp_current,windDirection,publishTime);
				LogUtil.d("Utility", "Save Successfully...");
			}
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
	 * @param temp_today
	 * @param weatherDesp
	 */
	private static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp_today,String weatherDesp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp_today", temp_today);
		
		editor.putString("weather_desp", weatherDesp);
		
		editor.putString("current_date", sdf.format(new Date()));
		//提交
		editor.commit();
	}
	private static void saveWeatherInfo(Context context,String temp_current,String windDirection,String publishTime) {
	//	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss",Locale.CHINA);
	//	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年M月d日 HH时mm分ss秒",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putString("temp_current", temp_current);
		editor.putString("wind_Direction", windDirection);
		editor.putString("publish_time", publishTime);
//		try {
//			editor.putString("publish_time", sdf2.format(sdf1.parse(publishTime)));
//		} catch (ParseException e) {
//			// e.printStackTrace();
//		}
		
		//提交
		editor.commit();
	}

}
