package jp.co.uniquevision.screamingpot.receiver.models;

import java.util.Date;

public class Humidity {

	private String code;
	private Date time;
	private double degree;
	
	public Humidity(String code, Date time, double degree) {
		this.code = code;
		this.time = time;
		this.degree = degree;
	}
	
	public static Humidity newInstance(double degree) {
		Humidity humidity = new Humidity("", new Date(), degree);
		return humidity;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public Date getTime() {
		return this.time;
	}
	
	public double getDegree() {
		return this.degree;
	}
}
