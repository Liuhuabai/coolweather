/**
 * 
 */
package com.example.coolweather.db;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.model.Area;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author liuhuabai
 *
 */
public class CoolWeatherDB {
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "cool_weather";
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	
	private static CoolWeatherDB coolWeatherDB;
	
	private SQLiteDatabase db;
	/**
	 * 将构造方法私有化
	 * @param context
	 */
	private CoolWeatherDB(Context context) {
		//这个语句，如果不存在该数据库，就会创建该数据库。否则，就只是拿到这个数据库的东西。
		CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	public synchronized static CoolWeatherDB getInstance(Context context) {
		if(coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	
	public void saveArea(Area area) {
		if(area != null) {
			ContentValues values = new ContentValues();
			values.put("area_name", area.getAreaName());
			values.put("area_code", area.getAreaCode());
			values.put("super_id", area.getSuperId());
			db.insert("Area", null, values);
		}
	}
	public Area getArea(int id) {
		Area area = new Area();
		area.setId(id);
		if(id == 0) {
			area.setAreaName("中国");
			area.setAreaCode("");
			area.setSuperId(0);
		} else {
			Cursor cursor = db.query("Area", null, "id = ? ", new String[]{String.valueOf(id)},
					null,null,null);
			if(cursor.moveToFirst()) {
				area.setAreaName(cursor.getString(cursor.getColumnIndex("area_name")));
				area.setAreaCode(cursor.getString(cursor.getColumnIndex("area_code")));
				area.setSuperId(cursor.getInt(cursor.getColumnIndex("super_id")));
			}
		}
		return area;
		
	}
	public List<Area> loadAreas(int super_id) {
		List<Area> list = new ArrayList<Area>();
		Cursor cursor = db.query("Area", null, "super_id = ? ", new String[]{String.valueOf(super_id)},
				null,null,null);
		if(cursor.moveToFirst()) {
			do {
				Area area = new Area();
				area.setId(cursor.getInt(cursor.getColumnIndex("id")));
				area.setAreaName(cursor.getString(cursor.getColumnIndex("area_name")));
				area.setAreaCode(cursor.getString(cursor.getColumnIndex("area_code")));
				area.setSuperId(super_id);
				list.add(area);
			} while(cursor.moveToNext());
		}
		return list;
	}
}
