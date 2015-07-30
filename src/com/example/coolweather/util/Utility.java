/**
 * 
 */
package com.example.coolweather.util;

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
}
