package tradingConsole.ui.language;

import framework.xml.XmlNodeList;
import framework.xml.XmlNode;

public class AccountLockLanguage
{
  public static String IsLocked = "Select";
  public static String Code = "Code";

  public static void setValue(XmlNode xmlNode)
  {
    XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
    for (int i = 0; i < xmlNodeList.get_Count(); i++)
    {
      XmlNode xmlNode2 = xmlNodeList.item(i);
      String nodeName = xmlNode2.get_LocalName();
      String nodeValue = xmlNode2.get_InnerText();
      if (nodeName.equals("Select"))
      {
	AccountLockLanguage.IsLocked = nodeValue;
      }
      else if (nodeName.equals("Code"))
      {
	AccountLockLanguage.Code = nodeValue;
      }
    }
  }
}
