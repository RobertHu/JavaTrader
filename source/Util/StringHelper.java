package Util;

import nu.xom.Element;

public class StringHelper
{
	public static boolean IsNullOrEmpty(String input)
	{
		return input == null || (input.length() == 0);
	}

	public static<T>  String join(T[] source,char separator){
		if(source==null || source.length==0) return "";
		StringBuilder sb=new StringBuilder();
		for (T item : source) {
			sb.append(item);
			sb.append(separator);
		}
		return sb.toString().substring(0, sb.length() - 1);
	}

	public static<T> String join2(T[][] source, char parentSeparator, char childSeparator){
		if(source==null || source.length == 0) {
			return "";
		}
		Element root = new Element("Data");
		for(T[] item : source){
			Element parent = new Element("Parent");
			for(T subItem: item){
				XmlElementHelper.appendChild(parent,"Child",subItem.toString());
			}
			root.appendChild(parent);
		}
		return root.toXML();
	}

}
