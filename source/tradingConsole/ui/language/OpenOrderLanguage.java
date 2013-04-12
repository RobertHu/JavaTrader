package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class OpenOrderLanguage
{
	public static String ID = "";
	public static String Close = "Close";
	public static String ShortCode = "Code";
	public static String LimitCloseOrderSummary = "Limit orders";
	public static String StopCloseOrderSummary = "Stop orders";
	public static String AccountCode = "Account";
	public static String InstrumentCode = "Item";
	public static String ExecuteTradeDay = "Time";
	public static String LotBalance = "Lot";
	public static String IsBuyString = "B/S";
	public static String ExecutePriceString = "Price";
	public static String LivePriceString = "Ref Price";
	public static String AutoLimitPriceString = "Auto limit price";
	public static String AutoStopPriceString = "Auto stop price";
	public static String InterestRateString = "Interest Rate";
	public static String CommissionString = "Comm";
	public static String CurrencyRate = "CurrencyRate";

	public static String TradePLFloatString = "Floating P/L";
	public static String UnrealisedSwapString = "Unrealised Swap";
	public static String InterestPLFloatString = "Interest";
	public static String StoragePLFloatString = "Storage";

	public static String LotBalanceString = "Lot";

	public static void setValue(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();

			if (nodeName.equals("IsOpen"))
			{
				OpenOrderLanguage.Close = nodeValue;
			}
			else if (nodeName.equals("ShortCode"))
			{
				OpenOrderLanguage.ShortCode = nodeValue;
			}
			else if (nodeName.equals("AccountCode"))
			{
				OpenOrderLanguage.AccountCode = nodeValue;
			}
			else if (nodeName.equals("InstrumentCode"))
			{
				OpenOrderLanguage.InstrumentCode = nodeValue;
			}
			else if (nodeName.equals("ExecuteTime"))
			{
				OpenOrderLanguage.ExecuteTradeDay = nodeValue;
			}
			else if (nodeName.equals("LotBalance"))
			{
				OpenOrderLanguage.LotBalance = nodeValue;
				OpenOrderLanguage.LotBalanceString = nodeValue;
			}
			else if (nodeName.equals("IsBuy"))
			{
				OpenOrderLanguage.IsBuyString = nodeValue;
			}
			else if (nodeName.equals("ExecutePrice"))
			{
				OpenOrderLanguage.ExecutePriceString = nodeValue;
			}
			else if (nodeName.equals("LivePrice"))
			{
				OpenOrderLanguage.LivePriceString = nodeValue;
			}
			else if (nodeName.equals("AutoLimitPriceString"))
			{
				OpenOrderLanguage.AutoLimitPriceString = nodeValue;
			}
			else if (nodeName.equals("AutoStopPriceString"))
			{
				OpenOrderLanguage.AutoStopPriceString = nodeValue;
			}
			else if (nodeName.equals("InterestRate"))
			{
				OpenOrderLanguage.InterestRateString = nodeValue;
			}
			else if (nodeName.equals("Commission"))
			{
				OpenOrderLanguage.CommissionString = nodeValue;
			}
			else if (nodeName.equals("TradePLFloat"))
			{
				OpenOrderLanguage.TradePLFloatString = nodeValue;
			}
			else if (nodeName.equals("InterestPLFloat"))
			{
				OpenOrderLanguage.InterestPLFloatString = nodeValue;
			}
			else if (nodeName.equals("StoragePLFloat"))
			{
				OpenOrderLanguage.StoragePLFloatString = nodeValue;
			}
			else if (nodeName.equals("LimitCloseOrderSummary"))
			{
				OpenOrderLanguage.LimitCloseOrderSummary = nodeValue;
			}
			else if (nodeName.equals("StopCloseOrderSummary"))
			{
				OpenOrderLanguage.StopCloseOrderSummary = nodeValue;
			}
			else if (nodeName.equals("UnrealisedSwapString"))
			{
				OpenOrderLanguage.UnrealisedSwapString = nodeValue;
			}
			else if (nodeName.equals("CurrencyRate"))
			{
				OpenOrderLanguage.CurrencyRate = nodeValue;
			}
		}
	}
}
