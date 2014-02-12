package tradingConsole.settings;

import java.io.File;

import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.StringHelper;
import framework.xml.XmlElement;

import tradingConsole.AppToolkit;
import framework.io.DirectoryHelper;

public class UISettingsManager
{
	private static String _filePath = "";

	public static String tradingPanelUiSetting = "";
	public static String accountStatusUiSetting = "";
	public static String workingOrderListUiSetting = "";
	public static String openOrderListUiSetting = "";

	private UISettingsManager()
	{
	}

	public static void setSetting(UISetting uiSetting)
	{
		String nodeName = uiSetting.get_ObjectId();
		String value = uiSetting.get_Parameter();
		if (nodeName.equals(UISetting.tradingPanelUiSetting))
		{
			UISettingsManager.tradingPanelUiSetting = value;
		}
		else if (nodeName.equals(UISetting.accountStatusUiSetting))
		{
			UISettingsManager.accountStatusUiSetting = value;
		}
		else if (nodeName.equals(UISetting.workingOrderListUiSetting))
		{
			UISettingsManager.workingOrderListUiSetting = value;
		}
		else if (nodeName.equals(UISetting.openOrderListUiSetting))
		{
			UISettingsManager.openOrderListUiSetting = value;
		}
	}

	//public static void reset(String filePath)
	public static void reset(String name)
	{
		//AppToolkit.deleteFile(filePath);
		//String filePath = AppToolkit.get_SettingDirectory()
		//	+ this._tradingConsole.get_LoginInformation().get_CustomerId().toString() + ".xml";
		String filePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),name);
		AppToolkit.deleteFile(filePath);

		UISettingsManager.tradingPanelUiSetting = "";
		UISettingsManager.accountStatusUiSetting = "";
		UISettingsManager.workingOrderListUiSetting = "";
		UISettingsManager.openOrderListUiSetting = "";

		//UISettingsManager.setSettings(filePath);
		UISettingsManager.setSettings(name);
	}

	private static void saveSettings2(String filePath)
	{
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
		xml += "<UISettings>";
		xml += "<UISetting>";
		xml += "<" + UISetting.tradingPanelUiSetting + ">";
		xml += "</" + UISetting.tradingPanelUiSetting + ">";
		xml += "<" + UISetting.accountStatusUiSetting + ">";
		xml += "</" + UISetting.accountStatusUiSetting + ">";
		xml += "<" + UISetting.workingOrderListUiSetting + ">";
		xml += "</" + UISetting.workingOrderListUiSetting + ">";
		xml += "<" + UISetting.openOrderListUiSetting + ">";
		xml += "</" + UISetting.openOrderListUiSetting + ">";
		xml += "</UISetting>";
		xml += "</UISettings>";

		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);

		XmlNode xmlNode = xmlDocument.get_DocumentElement().get_Item("UISetting");

		XmlNode xmlNode2 = xmlNode.get_Item(UISetting.tradingPanelUiSetting);
		xmlNode2.set_InnerXml(UISettingsManager.tradingPanelUiSetting);

		xmlNode2 = xmlNode.get_Item(UISetting.accountStatusUiSetting);
		xmlNode2.set_InnerXml(UISettingsManager.accountStatusUiSetting);

		xmlNode2 = xmlNode.get_Item(UISetting.workingOrderListUiSetting);
		xmlNode2.set_InnerXml(UISettingsManager.workingOrderListUiSetting);

		xmlNode2 = xmlNode.get_Item(UISetting.openOrderListUiSetting);
		xmlNode2.set_InnerXml(UISettingsManager.openOrderListUiSetting);

//System.out.println(UISettingsManager.tradingPanelUiSetting);
//System.out.println(xmlDocument.get_DocumentElement().get_OuterXml());

		//Functions.saveData(filePath, xmlDocument.get_DocumentElement().get_OuterXml());
		try
		{
			xmlDocument.save(filePath);
		}
		catch (Throwable exception)
		{
		}
	}

	public static void setSettingsDefaultValue()
	{
		if (StringHelper.isNullOrEmpty(UISettingsManager.tradingPanelUiSetting))
		{
			UISettingsManager.tradingPanelUiSetting = UISettingsManager.getDefaultTradingPanelUiSetting();
		}
		if (StringHelper.isNullOrEmpty(UISettingsManager.accountStatusUiSetting))
		{
			UISettingsManager.accountStatusUiSetting = UISettingsManager.getDefaultAccountStatusUiSetting();
		}
		if (StringHelper.isNullOrEmpty(UISettingsManager.workingOrderListUiSetting))
		{
			UISettingsManager.workingOrderListUiSetting = UISettingsManager.getDefaultWorkingOrderListUiSetting();
		}
		if (StringHelper.isNullOrEmpty(UISettingsManager.openOrderListUiSetting))
		{
			UISettingsManager.openOrderListUiSetting = UISettingsManager.getDefaultOpenOrderListUiSetting();
		}
	}

	public static void saveSettings(String filePath)
	{
		AppToolkit.createDirectory(AppToolkit.get_SettingDirectory());

		File file = new File(filePath);
		if (!file.exists())
		{
			UISettingsManager.setSettingsDefaultValue();
		}
		UISettingsManager.saveSettings2(filePath);
	}

	//public static void setSettings(String filePath)
	public static void setSettings(String name)
	{
		AppToolkit.createDirectory(AppToolkit.get_SettingDirectory());

		String filePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),name);

		File file = new File(filePath);
		if (!file.exists())
		{
			UISettingsManager.setSettingsDefaultValue();
			UISettingsManager.saveSettings2(filePath);
		}
		else
		{
			//XmlNode xmlNode = AppToolkit.getXml(filePath).get_Item("UISetting");
			XmlNode xmlNode = AppToolkit.getUserSettingXml(name).get_Item("UISetting");
			if (xmlNode != null)
			{
				XmlDocument xmlDocument = new XmlDocument();
				String xml = UISettingsManager.getDefaultTradingPanelUiSetting();
				xmlDocument.loadXml(xml);
				XmlElement defaultSettingXmlElement = xmlDocument.get_DocumentElement();
				XmlElement defineSettingXmlElement2 = xmlNode.get_Item(UISetting.tradingPanelUiSetting);
				XmlElement defineSettingXmlElement = (XmlElement)defineSettingXmlElement2.get_ChildNodes().item(0);
				UISettingsManager.tradingPanelUiSetting = UISettingsManager.getCorrectXml(defaultSettingXmlElement, defineSettingXmlElement,xml,defineSettingXmlElement2.get_InnerXml());

				xml = UISettingsManager.getDefaultAccountStatusUiSetting();
				xmlDocument.loadXml(xml);
				defaultSettingXmlElement = xmlDocument.get_DocumentElement();
				defineSettingXmlElement2 = xmlNode.get_Item(UISetting.accountStatusUiSetting);
				defineSettingXmlElement = (XmlElement)defineSettingXmlElement2.get_ChildNodes().item(0);
				UISettingsManager.accountStatusUiSetting = UISettingsManager.getCorrectXml(defaultSettingXmlElement, defineSettingXmlElement,xml,defineSettingXmlElement2.get_InnerXml());

				xml = UISettingsManager.getDefaultWorkingOrderListUiSetting();
				xmlDocument.loadXml(xml);
				defaultSettingXmlElement = xmlDocument.get_DocumentElement();
				defineSettingXmlElement2 = xmlNode.get_Item(UISetting.workingOrderListUiSetting);
				defineSettingXmlElement = (XmlElement)defineSettingXmlElement2.get_ChildNodes().item(0);
				UISettingsManager.workingOrderListUiSetting = UISettingsManager.getCorrectXml(defaultSettingXmlElement, defineSettingXmlElement,xml,defineSettingXmlElement2.get_InnerXml());

				xml = UISettingsManager.getDefaultOpenOrderListUiSetting();
				xmlDocument.loadXml(xml);
				defaultSettingXmlElement = xmlDocument.get_DocumentElement();
				defineSettingXmlElement2 = xmlNode.get_Item(UISetting.openOrderListUiSetting);
				defineSettingXmlElement = (XmlElement)defineSettingXmlElement2.get_ChildNodes().item(0);
				UISettingsManager.openOrderListUiSetting = UISettingsManager.getCorrectXml(defaultSettingXmlElement, defineSettingXmlElement,xml,defineSettingXmlElement2.get_InnerXml());
			}
			else
			{
				//if get default value?????????????????
				UISettingsManager.setSettingsDefaultValue();
				UISettingsManager.saveSettings2(filePath);
			}
		}
	}

	private static String getCorrectXml(XmlElement defaultSettingXmlElement,XmlElement defineSettingXmlElement,String defaultSettingXml,String defineSettingXml)
	{
		if (UISettingsManager.isSameColsItem(defaultSettingXmlElement, defineSettingXmlElement))
		{
			return defineSettingXml;
		}
		else
		{
			return defaultSettingXml;
		}
	}

	private static boolean isSameColsItem(XmlElement defaultSettingXmlElement,XmlElement defineSettingXmlElement)
	{
		XmlNode defaultSettingXmlNode = defaultSettingXmlElement.get_Item("Cols");
		for (int i = 0; i < defaultSettingXmlNode.get_ChildNodes().get_Count(); i++)
		{
			XmlNode defaultSettingXmlNode2 = defaultSettingXmlNode.get_ChildNodes().itemOf(i);
			String defaultSettingColKeyValue = defaultSettingXmlNode2.get_Attributes().get_ItemOf("ColKey").get_Value();

			boolean isFound = false;
			XmlNode defineSettingXmlNode = defineSettingXmlElement.get_Item("Cols");
			for (int j = 0; j < defineSettingXmlNode.get_ChildNodes().get_Count(); j++)
			{
				XmlNode defineSettingXmlNode2 = defineSettingXmlNode.get_ChildNodes().itemOf(j);
				String defineSettingColKeyValue = defineSettingXmlNode2.get_Attributes().get_ItemOf("ColKey").get_Value();
				if (defaultSettingColKeyValue.equals(defineSettingColKeyValue))
				{
					isFound = true;
					break;
				}
			}
			if (!isFound)
			{
				return false;
			}
		}
		return true;
	}

	private static String getWidth(double scale)
	{
		int screenSizeWidth = AppToolkit.get_ScreenSize().width - 45;
		String valueString = String.valueOf(screenSizeWidth * scale);
		return valueString.substring(0, valueString.indexOf("."));
	}

	public static int getWidth(int width, double scale, int defaultValue)
	{
		int w = defaultValue;
		try
		{
			w = (int) (width * scale);
		}
		catch (Exception exception)
		{
			w = defaultValue;
		}
		if (w < 0) w = defaultValue;
		return w;
	}

	private static String getDefaultTradingPanelUiSetting()
	{
		return "<Grid FontName=\"SansSerif\" FontSize=\"12\" RowHeight=\"0\">" +
			"<Cols>" +
			"<Col ColKey=\"Select\" ColWidth=\"" + UISettingsManager.getWidth(0.025) + "\" Sequence=\"0\"/>" +
			"<Col ColKey=\"Ask\" ColWidth=\"" + UISettingsManager.getWidth(0.06) + "\" Sequence=\"4\"/>" +
			"<Col ColKey=\"High\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"7\"/>" +
			"<Col ColKey=\"Bid\" ColWidth=\"" + UISettingsManager.getWidth(0.06) + "\" Sequence=\"2\"/>" +
			"<Col ColKey=\"Change\" ColWidth=\"" + UISettingsManager.getWidth(0.07) + "\" Sequence=\"11\"/>" +
			"<Col ColKey=\"Low\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"8\"/>" +
			"<Col ColKey=\"Timestamp\" ColWidth=\"" + UISettingsManager.getWidth(0.08) + "\" Sequence=\"5\"/>" +
			"<Col ColKey=\"Last\" ColWidth=\"" + UISettingsManager.getWidth(0.12) + "\" Sequence=\"6\"/>" +
			"<Col ColKey=\"Description\" ColWidth=\"" + UISettingsManager.getWidth(0.08) + "\" Sequence=\"1\"/>" +
			"<Col ColKey=\"Sequence\" ColWidth=\"0\" Sequence=\"3\"/>" +
			"<Col ColKey=\"Open\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"9\"/>" +
			"<Col ColKey=\"PrevClose\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"10\"/>" +
			"<Col ColKey=\"Change\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"11\"/>" +
			"<Col ColKey=\"InterestRateBuy\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"12\"/>" +
			"<Col ColKey=\"InterestRateSell\" ColWidth=\"" + UISettingsManager.getWidth(0.1) + "\" Sequence=\"13\"/>" +
			"</Cols>" +
			"</Grid>";
	}

	private static String getDefaultAccountStatusUiSetting()
	{
		return "<Grid FontName=\"SansSerif\" FontSize=\"12\"  RowHeight=\"10\">" +
			"<Cols>" +
			"</Cols>" +
			"</Grid>";
	}

	private static String getDefaultWorkingOrderListUiSetting()
	{
		return "<Grid FontName=\"SansSerif\" FontSize=\"12\" RowHeight=\"10\">" +
			"<Cols>" +
			"<Col ColKey=\"PhaseString\" ColWidth=\"100\" Sequence=\"0\"/>" +
			"<Col ColKey=\"SubmitTime\" ColWidth=\"120\" Sequence=\"1\"/>" +
			"<Col ColKey=\"EndTime\" ColWidth=\"120\" Sequence=\"2\"/>" +
			"<Col ColKey=\"AccountCode\" ColWidth=\"120\" Sequence=\"3\"/>" +
			"<Col ColKey=\"InstrumentCode\" ColWidth=\"120\" Sequence=\"4\"/>" +
			"<Col ColKey=\"LotString\" ColWidth=\"50\" Sequence=\"5\"/>" +
			"<Col ColKey=\"IsOpenString\" ColWidth=\"50\" Sequence=\"6\"/>" +
			"<Col ColKey=\"IsBuyString\" ColWidth=\"50\" Sequence=\"7\"/>" +
			"<Col ColKey=\"SetPriceString\" ColWidth=\"80\" Sequence=\"8\"/>" +
			"<Col ColKey=\"DQMaxMove\" ColWidth=\"30\" Sequence=\"9\"/>" +
			"<Col ColKey=\"ExecutePriceString\" ColWidth=\"80\" Sequence=\"10\"/>" +
			"<Col ColKey=\"OrderTypeString\" ColWidth=\"60\" Sequence=\"11\"/>" +
			"<Col ColKey=\"TradePLString\" ColWidth=\"80\" Sequence=\"12\"/>" +
			"<Col ColKey=\"CommissionSumString\" ColWidth=\"80\" Sequence=\"13\"/>" +
			"<Col ColKey=\"LevySumString\" ColWidth=\"80\" Sequence=\"14\"/>" +
			"</Cols>" +
			"</Grid>";
	}

	private static String getDefaultOpenOrderListUiSetting()
	{
		return "<Grid FontName=\"SansSerif\" FontSize=\"12\" RowHeight=\"10\">" +
			"<Cols>" +
			"<Col ColKey=\"Close\" ColWidth=\"70\" Sequence=\"0\"/>" +
			"<Col ColKey=\"ShortCode\" ColWidth=\"120\" Sequence=\"1\"/>" +
			"<Col ColKey=\"AccountCode\" ColWidth=\"80\" Sequence=\"2\"/>" +
			"<Col ColKey=\"InstrumentCode\" ColWidth=\"132\" Sequence=\"3\"/>" +
			"<Col ColKey=\"ExecuteTradeDay\" ColWidth=\"120\" Sequence=\"4\"/>" +
			"<Col ColKey=\"LotBalanceString\" ColWidth=\"50\" Sequence=\"5\"/>" +
			"<Col ColKey=\"LimitCloseOrderSummary\" ColWidth=\"80\" Sequence=\"6\"/>" +
			"<Col ColKey=\"StopCloseOrderSummary\" ColWidth=\"80\" Sequence=\"7\"/>" +
			"<Col ColKey=\"IsBuyString\" ColWidth=\"40\" Sequence=\"8\"/>" +
			"<Col ColKey=\"ExecutePriceString\" ColWidth=\"80\" Sequence=\"9\"/>" +
			"<Col ColKey=\"LivePriceString\" ColWidth=\"80\" Sequence=\"10\"/>" +
			"<Col ColKey=\"AutoLimitPriceString\" ColWidth=\"80\" Sequence=\"11\"/>" +
			"<Col ColKey=\"AutoStopPriceString\" ColWidth=\"80\" Sequence=\"12\"/>" +
			"<Col ColKey=\"TradePLFloatString\" ColWidth=\"80\" Sequence=\"13\"/>" +
			"<Col ColKey=\"UnrealisedSwapString\" ColWidth=\"80\" Sequence=\"14\"/>" +
			"<Col ColKey=\"InterestPLFloatString\" ColWidth=\"80\" Sequence=\"15\"/>" +
			"<Col ColKey=\"StoragePLFloatString\" ColWidth=\"80\" Sequence=\"16\"/>" +
			"<Col ColKey=\"CommissionString\" ColWidth=\"80\" Sequence=\"17\"/>" +
			"<Col ColKey=\"InterestRateString\" ColWidth=\"80\" Sequence=\"18\"/>" +
			"<Col ColKey=\"CurrencyRate\" ColWidth=\"80\" Sequence=\"19\"/>" +
			"</Cols>" +
			"</Grid>";
	}
}
