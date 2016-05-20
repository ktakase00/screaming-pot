package jp.co.uniquevision.screamingpot.receiver.models;

import java.util.Date;

public class Humidity {

	private long sequence;
	private String code;
	private Date time;
	private double degree;
	
	public Humidity(String code, Date time, double degree) {
		this.sequence = 0;
		this.code = code;
		this.time = time;
		this.degree = degree;
	}
	
	public Humidity(long sequence, String code, Date time, double degree) {
		this.sequence = sequence;
		this.code = code;
		this.time = time;
		this.degree = degree;
	}
	
	public static Humidity newInstance(double degree) {
		Humidity humidity = new Humidity("", new Date(), degree);
		return humidity;
	}
	
	public static Humidity cloneWithSequence(Humidity humidity, long sequence) {
		return new Humidity(sequence,
				humidity.getCode(),
				humidity.getTime(),
				humidity.getDegree());
	}
	
	public long getSequence() {
		return this.sequence;
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
