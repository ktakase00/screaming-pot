package jp.co.uniquevision.screamingpot.receiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String dateToString(Date time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dateFormat.format(time);
	}
	
	public static Date stringToDate(String timeStr) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return dateFormat.parse(timeStr);
	}
}
