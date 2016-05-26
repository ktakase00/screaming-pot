package jp.co.uniquevision.screamingpot.receiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ユーティリティ
 *
 */
public class Util {
	
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * 日時から文字列に変換する
	 * 
	 * @param time 日時
	 * @return 日時を表す文字列
	 */
	public static String dateToString(Date time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		return dateFormat.format(time);
	}
	
	/**
	 * 文字列から日時に変換する
	 * 
	 * @param timeStr 日時を表す文字列
	 * @return 日時
	 * @throws ParseException 書式が不正
	 */
	public static Date stringToDate(String timeStr) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		return dateFormat.parse(timeStr);
	}
}
