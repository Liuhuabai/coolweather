package com.example.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 
 * @author liuhuabai
 *
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

	public CoolWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	/**
	 * 建表语句
	 */
	public static final String CREATE_AREA="CREATE TABLE Area ("
			+ "id integer primary key autoincrement,"
			+ "area_name text,"
			+ "area_code text,"
			+ "super_id integer "
			+ ")";
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_AREA);// 创建Area表
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
