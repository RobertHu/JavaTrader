package tradingConsole.ui.colorHelper;

import java.awt.Color;

import framework.StringHelper;
import framework.xml.XmlNodeList;
import framework.xml.XmlNode;
import tradingConsole.enumDefine.LogColor;

public class ColorSet
{
	public static Color agentAccountBackColor = Color.green;

	public static void setValue(Class colorClass, XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();
			try
			{
				String[] rgbString = StringHelper.split(nodeValue, ",");
				int r = Integer.parseInt(rgbString[0]);
				int g = Integer.parseInt(rgbString[1]);
				int b = Integer.parseInt(rgbString[2]);
				Color color = new Color(r,g,b);
				colorClass.getField(nodeName).set(nodeName, color);
			}
			catch (SecurityException ex)
			{
				continue;
			}
			catch (NoSuchFieldException ex)
			{
				continue;
			}
			catch (IllegalAccessException ex)
			{
				continue;
			}
			catch (IllegalArgumentException exception)
			{
				continue;
			}
		}
	}

	public synchronized static void initialize(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			if (nodeName.equalsIgnoreCase("AlertLevelColor"))
			{
				ColorSet.setValue(AlertLevelColor.class,xmlNode.get_Item("AlertLevelColor"));
			}
			else if (nodeName.equalsIgnoreCase("BuySellColor"))
			{
				ColorSet.setValue(BuySellColor.class,xmlNode.get_Item("BuySellColor"));
			}
			else if (nodeName.equalsIgnoreCase("ColorSet"))
			{
				ColorSet.setValue(ColorSet.class,xmlNode.get_Item("ColorSet"));
			}
			else if (nodeName.equalsIgnoreCase("CurrentCellColor"))
			{
				ColorSet.setValue(CurrentCellColor.class,xmlNode.get_Item("CurrentCellColor"));
			}
			else if (nodeName.equalsIgnoreCase("FormBackColor"))
			{
				ColorSet.setValue(FormBackColor.class,xmlNode.get_Item("FormBackColor"));
			}
			else if (nodeName.equalsIgnoreCase("GridBackColor"))
			{
				ColorSet.setValue(GridBackColor.class,xmlNode.get_Item("GridBackColor"));
			}
			else if (nodeName.equalsIgnoreCase("GridBackgroundColor"))
			{
				ColorSet.setValue(GridBackgroundColor.class,xmlNode.get_Item("GridBackgroundColor"));
			}
			else if (nodeName.equalsIgnoreCase("GridFixedBackColor"))
			{
				ColorSet.setValue(GridFixedBackColor.class,xmlNode.get_Item("GridFixedBackColor"));
			}
			else if (nodeName.equalsIgnoreCase("GridFixedForeColor"))
			{
				ColorSet.setValue(GridFixedForeColor.class,xmlNode.get_Item("GridFixedForeColor"));
			}
			else if (nodeName.equalsIgnoreCase("InstrumentValidationBackColor"))
			{
				ColorSet.setValue(InstrumentValidationBackColor.class,xmlNode.get_Item("InstrumentValidationBackColor"));
			}
			else if (nodeName.equalsIgnoreCase("LockedStatusColor"))
			{
				ColorSet.setValue(LockedStatusColor.class,xmlNode.get_Item("LockedStatusColor"));
			}
			else if (nodeName.equalsIgnoreCase("NumericColor"))
			{
				ColorSet.setValue(NumericColor.class,xmlNode.get_Item("NumericColor"));
			}
			else if (nodeName.equalsIgnoreCase("OpenCloseColor"))
			{
				ColorSet.setValue(OpenCloseColor.class,xmlNode.get_Item("OpenCloseColor"));
			}
			else if (nodeName.equalsIgnoreCase("PhaseColor"))
			{
				ColorSet.setValue(PhaseColor.class,xmlNode.get_Item("PhaseColor"));
			}
			else if (nodeName.equalsIgnoreCase("LogColor"))
			{
				ColorSet.setValue(LogColor.class,xmlNode.get_Item("LogColor"));
			}
			else if (nodeName.equalsIgnoreCase("QuotationStatusColor"))
			{
				ColorSet.setValue(QuotationStatusColor.class,xmlNode.get_Item("QuotationStatusColor"));
			}
			else if (nodeName.equalsIgnoreCase("SelectionBackground"))
			{
				ColorSet.setValue(SelectionBackground.class,xmlNode.get_Item("SelectionBackground"));
			}
			else if (nodeName.equalsIgnoreCase("TradeOptionColor"))
			{
				ColorSet.setValue(TradeOptionColor.class,xmlNode.get_Item("TradeOptionColor"));
			}
		}
	}
}
