package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class MakeOrderAccountGridLanguage
{
	public static String Code = "Code";
	public static String IsBuyForCurrent = "B/S";
	public static String IsBuyForCombo = "B/S";
	public static String SetPriceString = "Price";
	public static String LotString = "Lot";
	public static String DQMaxMove = "Slide Pips";

	public static void setValue(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();
			if (nodeName.equals("Code"))
			{
				MakeOrderAccountGridLanguage.Code = nodeValue;
			}
			else if (nodeName.equals("IsBuy"))
			{
				MakeOrderAccountGridLanguage.IsBuyForCurrent = nodeValue;
				MakeOrderAccountGridLanguage.IsBuyForCombo = nodeValue;
			}
			else if (nodeName.equals("SetPrice"))
			{
				MakeOrderAccountGridLanguage.SetPriceString = nodeValue;
			}
			else if (nodeName.equals("Lot"))
			{
				MakeOrderAccountGridLanguage.LotString = nodeValue;
			}
			else if (nodeName.equals("DQMaxMove"))
			{
				MakeOrderAccountGridLanguage.DQMaxMove = nodeValue;
			}
		}
	}

}
