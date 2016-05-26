package jp.co.uniquevision.screamingpot.receiver.models;

import java.util.Date;

import jp.co.uniquevision.screamingpot.receiver.Util;

/**
 * ElasticeSearch Bulk APIのCREATEレコードのJSONパラメータ定義
 *
 */
public class EsSourceCreate {
	private String device;
	private String time;
	private double degree;
	
	public EsSourceCreate(String device, Date time, double degree) {
		this.device = device;
		this.time = Util.dateToString(time);
		this.degree = degree;
	}
}
