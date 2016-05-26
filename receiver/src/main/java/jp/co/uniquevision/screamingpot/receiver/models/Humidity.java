package jp.co.uniquevision.screamingpot.receiver.models;

import java.util.Date;

/**
 * 湿度データ
 *
 */
public class Humidity {

	private long sequence;
	private String device;
	private Date time;
	private double degree;
	
	/**
	 * コンストラクタ
	 * 
	 * @param device 端末名
	 * @param time 日時
	 * @param degree 湿度値
	 */
	public Humidity(String device, Date time, double degree) {
		this.sequence = 0;
		this.device = device;
		this.time = time;
		this.degree = degree;
	}
	
	/**
	 * コンストラクタ
	 * 
	 * @param sequence シーケンス番号
	 * @param device 端末名
	 * @param time 日時
	 * @param degree 湿度値
	 */
	public Humidity(long sequence, String device, Date time, double degree) {
		this.sequence = sequence;
		this.device = device;
		this.time = time;
		this.degree = degree;
	}
	
	/**
	 * 新しい湿度データのインスタンスを生成する
	 * 
	 * @param device 端末名
	 * @param degree 湿度値
	 * @return 湿度データ
	 */
	public static Humidity newInstance(String device, double degree) {
		Humidity humidity = new Humidity(device, new Date(), degree);
		return humidity;
	}
	
	/**
	 * 元の湿度データにシーケンス番号をセットしたインスタンスを生成する
	 * 
	 * @param humidity 元の湿度データ
	 * @param sequence 付与するシーケンス番号
	 * @return シーケンス番号付きの湿度データ
	 */
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
