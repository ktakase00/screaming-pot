package jp.co.uniquevision.screamingpot.receiver.models;

import java.util.Date;

public class Humidity {

	private long sequence;
	private String device;
	private Date time;
	private double degree;
	
	public Humidity(String device, Date time, double degree) {
		this.sequence = 0;
		this.device = device;
		this.time = time;
		this.degree = degree;
	}
	
	public Humidity(long sequence, String device, Date time, double degree) {
		this.sequence = sequence;
		this.device = device;
		this.time = time;
		this.degree = degree;
	}
	
	public static Humidity newInstance(String device, double degree) {
		Humidity humidity = new Humidity(device, new Date(), degree);
		return humidity;
	}
	
	public static Humidity cloneWithSequence(Humidity humidity, long sequence) {
		return new Humidity(sequence,
				humidity.getDevice(),
				humidity.getTime(),
				humidity.getDegree());
	}
	
	public long getSequence() {
		return this.sequence;
	}
	
	public String getDevice() {
		return this.device;
	}
	
	public Date getTime() {
		return this.time;
	}
	
	public double getDegree() {
		return this.degree;
	}
}
