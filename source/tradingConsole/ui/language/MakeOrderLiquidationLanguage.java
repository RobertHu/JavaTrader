package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class MakeOrderLiquidationLanguage
{
  public static String AccountCode = "Code";
  public static String OpenOrderDirectLiq = "Open Order";
  public static String IsBuyStringDirectLiq = "B/S";
  public static String SetPriceStringDirectLiq = "Price";
  public static String LiqLotString = "Lot";

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
	MakeOrderLiquidationLanguage.AccountCode = nodeValue;
      }
      else if (nodeName.equals("PeerOrderCodes"))
      {
	MakeOrderLiquidationLanguage.OpenOrderDirectLiq = nodeValue;
      }
      else if (nodeName.equals("IsBuy"))
      {
	MakeOrderLiquidationLanguage.IsBuyStringDirectLiq = nodeValue;
      }
      else if (nodeName.equals("SetPrice"))
      {
	MakeOrderLiquidationLanguage.SetPriceStringDirectLiq = nodeValue;
      }
      else if (nodeName.equals("LiqLot"))
      {
	MakeOrderLiquidationLanguage.LiqLotString = nodeValue;
      }
    }
  }
}
