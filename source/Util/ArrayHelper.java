package Util;

public class ArrayHelper
{
	public static <T> String join(T[] source,String separator)
	{
		if(source.length==0) return "";
		StringBuilder sbBuilder=new StringBuilder();
		for(T item:source ){
			sbBuilder.append(item.toString());
			sbBuilder.append(separator);
		}
		return sbBuilder.substring(0, sbBuilder.length() - 1);
	}

}
