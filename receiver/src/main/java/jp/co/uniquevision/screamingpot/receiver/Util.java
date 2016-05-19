package jp.co.uniquevision.screamingpot.receiver;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String dateToString(Date time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		return dateFormat.format(time);
	}
}
