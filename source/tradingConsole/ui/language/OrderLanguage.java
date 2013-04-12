package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class OrderLanguage
{
	public static String ID = "";
	public static String PhaseString = "Status";
	public static String SubmitTime = "Time";
	public static String EndTime = "Expire time";
	public static String AccountCode = "Account";
	public static String InstrumentCode = "Item";
	public static String Lot = "Lot";
	public static String IsOpenString = "N / C";
	public static String IsBuyString = "B / S";
	public static String SetPriceString = "Price";
	public static String OrderTypeString = "Type";
	public static String TradePLString = "Trade PL";
	public static String CommissionSumString = "Fee";
	public static String LevySumString = "Levy";

	public static String LotString = "Lot";

	public static void setValue(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();

			if (nodeName.equals("Phase"))
			{
				OrderLanguage.PhaseString = nodeValue;
			}
			else if (nodeName.equals("SubmitTime"))
			{
				OrderLanguage.SubmitTime = nodeValue;
			}
			else if (nodeName.equals("EndTime"))
			{
				OrderLanguage.EndTime = nodeValue;
			}
			else if (nodeName.equals("AccountCode"))
			{
				OrderLanguage.AccountCode = nodeValue;
			}
			else if (nodeName.equals("InstrumentCode"))
			{
				OrderLanguage.InstrumentCode = nodeValue;
			}
			else if (nodeName.equals("Lot"))
			{
				OrderLanguage.Lot = nodeValue;
				OrderLanguage.LotString = nodeValue;
			}
			else if (nodeName.equals("IsOpen"))
			{
				OrderLanguage.IsOpenString = nodeValue;
			}
			else if (nodeName.equals("IsBuy"))
			{
				OrderLanguage.IsBuyString = nodeValue;
			}
			else if (nodeName.equals("SetPrice"))
			{
				OrderLanguage.SetPriceString = nodeValue;
			}
			else if (nodeName.equals("OrderType"))
			{
				OrderLanguage.OrderTypeString = nodeValue;
			}
			else if (nodeName.equals("TradePL"))
			{
				OrderLanguage.TradePLString = nodeValue;
			}
			else if (nodeName.equals("CommissionSum"))
			{
				OrderLanguage.CommissionSumString = nodeValue;
			}
			else if (nodeName.equals("LevySum"))
			{
				OrderLanguage.LevySumString = nodeValue;
			}
		}
	}
}
