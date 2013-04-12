package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class MakeOrderVerificationLanguage
{
  public static String Sequence = "";
  public static String AccountCode = "Account";
  public static String Lot = "Lot";
  public static String IsBuyString = "B/S";
  public static String IsOpenString = "N/C";
  public static String SetPriceString = "Price";
  public static String TradeOptionString = "Option";

  public static void setValue(XmlNode xmlNode)
  {
    XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
    for (int i = 0; i < xmlNodeList.get_Count(); i++)
    {
      XmlNode xmlNode2 = xmlNodeList.item(i);
      String nodeName = xmlNode2.get_LocalName();
      String nodeValue = xmlNode2.get_InnerText();

      if (nodeName.equals("AccountCode"))
      {
	MakeOrderVerificationLanguage.AccountCode = nodeValue;
      }
      else if (nodeName.equals("Lot"))
      {
	MakeOrderVerificationLanguage.Lot = nodeValue;
      }
      else if (nodeName.equals("IsBuy"))
      {
	MakeOrderVerificationLanguage.IsBuyString = nodeValue;
      }
      else if (nodeName.equals("IsOpen"))
      {
	MakeOrderVerificationLanguage.IsOpenString = nodeValue;
      }
      else if (nodeName.equals("SetPrice"))
      {
	MakeOrderVerificationLanguage.SetPriceString = nodeValue;
      }
      else if (nodeName.equals("TradeOption"))
      {
	MakeOrderVerificationLanguage.TradeOptionString = nodeValue;
      }
    }
  }

}
