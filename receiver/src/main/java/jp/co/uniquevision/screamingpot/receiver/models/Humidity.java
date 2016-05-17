package jp.co.uniquevision.screamingpot.receiver.models;

public class Humidity {

	private String code;
	private String time;
	private Double degree;
	
	public Humidity(String code, String time, Double degree) {
		this.code = code;
		this.time = time;
		this.degree = degree;
	}
	
	public String getCode() {
		return this.code;
	}
	
	public String getTime() {
		return this.time;
	}
	
	public Double getDegree() {
		return this.degree;
	}
}
