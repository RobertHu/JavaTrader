package Util;

import java.util.*;
import framework.xml.XmlNode;
import framework.xml.XmlDocument;
import org.apache.commons.lang3.StringEscapeUtils;

public class XmlNodeHelper
{
	public static XmlNode Parse(String xml)
	{
		XmlDocument document = new XmlDocument();
		document.loadXml(StringEscapeUtils.unescapeXml(xml));
		return document.get_DocumentElement();
	}
}
