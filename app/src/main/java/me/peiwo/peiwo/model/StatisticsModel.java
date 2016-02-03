package me.peiwo.peiwo.model;

import java.util.List;

public class StatisticsModel {
	public String name;
	public String statistic_version;
	public String uid;
	public List<StatisticInfoModel> statistic_list;
	public String sent;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatistic_version() {
		return statistic_version;
	}

	public void setStatistic_version(String statistic_version) {
		this.statistic_version = statistic_version;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public List<StatisticInfoModel> getStatistic_list() {
		return statistic_list;
	}

	public void setStatistic_list(List<StatisticInfoModel> statistic_list) {
		this.statistic_list = statistic_list;
	}

	public String getSent() {
		return sent;
	}

	public void setSent(String sent) {
		this.sent = sent;
	}

}
