package Util;

import java.io.IOException;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.ValidityException;
import framework.xml.*;
import  framework.data.DataSet;
import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import java.nio.CharBuffer;
import Packet.PacketContants;
import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayOutputStream;
import com.ms.xml.util.XMLOutputStream;
public class XmlElementHelper
{
	private static Logger logger= Logger.getLogger(XmlElementHelper.class);
	private static final String defaultElementName = "argElment";
	public static Element parse(String node) throws ValidityException, ParsingException, IOException
	{
		Document doc = new Builder(false).build(node, null);
		Element ele = doc.getRootElement();
		return ele;
	}

	public static Element create(String name, String value)
	{
		Element element = new Element(name);
		element.appendChild(new Text(value));
		return element;
	}

	public static void appendChild(Element parent,String key,String value)
	{
		parent.appendChild(create(key,value));
	}

	public static void appendChild(Element parent, String value)
	{
		parent.appendChild(create(defaultElementName, value));
	}



	public static XmlNode ConvertToXmlNode(Element element)
	{
		return ConvertToXmlNodeCommon(element.getValue());
	}

	public static XmlNode ConvertToXmlNode(String content)
	{
		return ConvertToXmlNodeCommon(content);
	}

	private static XmlNode ConvertToXmlNodeCommon(String xml)
	{
		try
		{
			XmlDocument doc = new XmlDocument();
			doc.loadXml(xml);
			return doc.get_DocumentElement();

		}
		catch (Exception ex)
		{
			logger.error("convert xml to xmlnode error  "+xml,ex);
			return null;
		}

	}




	private static XmlNode convertToXmlNodeForDataset(String content)
	{
		//String xml = StringEscapeUtils.unescapeXml(content);
		return ConvertToXmlNodeCommon(content);
	}

	public static DataSet convertToDataset(String content)
	{
		DataSet ds = new DataSet();
		try
		{
			if (StringHelper.IsNullOrEmpty(content))
			{
				return null;
			}
			XmlNode xml = convertToXmlNodeForDataset(content);
			ds.readXml(xml);
		}
		catch (Exception ex)
		{
			logger.error("convert to dataset", ex);
		}
		return ds;
	}


	public static DataSet convertToDataset(Element element)
	{
		return convertToDataset(element.getValue());
	}


	public static String getInnerValue(Element parent,String nodeName){
		return parent.getFirstChildElement(nodeName).getValue();
	}

}
