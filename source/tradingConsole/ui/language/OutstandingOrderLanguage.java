package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class OutstandingOrderLanguage
{
	public static String IsBuy = ""; //For sort by
	public static String OpenOrder = "Open Order";
	public static String LiqLotString = "Lot";
	public static String IsSelected = "Close";
	public static String Delivery = "Delivery";

	public static void setValue(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();

			if (nodeName.equals("OpenOrder"))
			{
				OutstandingOrderLanguage.OpenOrder = nodeValue;
			}
			else if (nodeName.equals("LiqLot"))
			{
				OutstandingOrderLanguage.LiqLotString = nodeValue;
			}
			else if (nodeName.equals("IsSelected"))
			{
				OutstandingOrderLanguage.IsSelected = nodeValue;
			}
			else if (nodeName.equals("Delivery"))
			{
				OutstandingOrderLanguage.Delivery = nodeValue;
			}
		}
	}
}
