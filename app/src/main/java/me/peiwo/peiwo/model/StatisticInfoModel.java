package me.peiwo.peiwo.model;

import java.util.List;

public class StatisticInfoModel {

	public List<StatisticDataModel> data;
	public String date;

	public List<StatisticDataModel> getData() {
		return data;
	}

	public void setData(List<StatisticDataModel> data) {
		this.data = data;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
