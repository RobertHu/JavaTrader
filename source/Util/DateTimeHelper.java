package Util;

import framework.DateTime;

public final class DateTimeHelper
{
	public static String ToStandardFormat(DateTime dt){
		return dt.toString("yyyy-MM-dd HH:mm:ss");
	}
}
