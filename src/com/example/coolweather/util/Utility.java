/**
 * 
 */
package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.Area;

/**
 * ����վ�����ȡ����������
 * ���ţ����У�����|���������ģ����Խ�������������
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
					//���������������ݴ洢��Area��
					coolWeatherDB.saveArea(area);
				}
				return true;
			}
		}
		return false;
	}
}
