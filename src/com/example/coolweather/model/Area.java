package com.example.coolweather.model;

/**
 * 它由主键 区域名称 区域代码 以及它的上级区域id构成的
 * 省 市 县 都是一样的
 * @author liuhuabai
 *
 */
public class Area {
	private int id;
	private String areaName;
	private String areaCode;
	private int superId;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAreaName() {
		return areaName;
	}
	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public int getSuperId() {
		return superId;
	}
	public void setSuperId(int superId) {
		this.superId = superId;
	}
	
	

	
}
