package tradingConsole.settings;

import framework.xml.XmlNode;
import framework.xml.XmlNodeList;
import tradingConsole.ui.language.Login;
import framework.StringHelper;
import framework.xml.XmlDocument;

public class Parameter
{
	public static String newsLanguage = "ENG";
	//public static boolean isVisableOkForLoginForm = true;

	public static String restrictFonts = "";
	public static int specialFontSizeIncreaseForRestrict = 6;
	public static int specialFontSizeIncrease = 3;
	public static boolean colHiddenLiqLotForAutoClose = false;
	public static int operateOrderCount = 3;
	public static int getCommandsInterval = 800;
	public static int dQQuoteDefaultLot = 1;
	public static int orderBeginTimeDiff = 1;
	public static int mooMocOrderValidDuration = 30;
	public static boolean isNewOrderAcceptedHedging = true;
	private static boolean isUseMargin = true;
	public static String supportMarginAction = null;
	public static String receiveAddress = "try@try.com.hk";
	public static boolean isUseDownload = true;
	public static boolean isGoodTillMonth = true;
	public static double timeZoneMinuteSpan = 0;
	public static int statementReportType = 0;
	public static String accountSummaryRDLC = "ENG/Report/AccountSummary.rdlc";
	public static String statementXML = "ENG/Report/RptStatement.xml";
	public static String statement2XML = "ENG/Report/RptStatement2.xml";
	public static String statement3XML = "ENG/Report/RptStatement03.xml";
	public static String statement5XML = "ENG/Report/RptStatement5.xml";
	public static String ledgerXML = "ENG/Report/RptLedger.xml";
	public static String statementMCXML = "ENG/Report/Statement_Mc.xml";
	public static int extendDay = 3;
	public static int quoteDelay = 15;
	public static int nonQuoteVerificationUiDelay = 0;
	public static boolean isCheckAcceptDQVariation = true;
	public static boolean isDisplayOperateOrderTimePrompt = false;
	public static boolean isAllowMixAgent = true;
	public static boolean isHasChatRoom = true;
	public static boolean isHasDebug = true;
	public static boolean isMultiShowAnalyticChart = true;
	public static boolean isHasNews = false;
	public static boolean isUseOuterNews = false;
	public static String newsUrl = "http://rss.news.yahoo.com/rss/economy";
	public static String tradingFactUrl = null;
	public static boolean isCalcCommission = true;
	public static int goodTillMonthType = 0;
	public static boolean isHasGoodTillMonthDayOrder = true;
	public static boolean isHasGoodTillMonthSession = true;
	public static boolean isHasGoodTillMonthGTM = true;
	public static boolean isHasGoodTillMonthGTF = true;
	public static int selectInstrumentsRange = 30;
	public static boolean isAllowLimitInSpread = false;
	public static XmlNode columnSettings = null;
	public static XmlNode integralitySettings = null;

	private Parameter()
	{
	}

	public static boolean isShouldShowColumn(String tableName, String columnName)
	{
		if(Parameter.columnSettings == null || Parameter.columnSettings.selectSingleNode(tableName) == null)
		{
			return true;
		}
		else
		{
			XmlNode settings = Parameter.columnSettings.selectSingleNode(tableName);
			settings = settings.selectSingleNode(columnName);
			if(settings == null) return true;
			return Boolean.parseBoolean(settings.get_Attributes().get_ItemOf("Show").get_Value());
		}
	}

	public static boolean isAllowNull(String key, String fieldName)
	{
		if(Parameter.integralitySettings == null || Parameter.integralitySettings.selectSingleNode(key) == null)
		{
			return true;
		}
		else
		{
			XmlNode settings = Parameter.integralitySettings.selectSingleNode(key);
			settings = settings.selectSingleNode(fieldName);
			if(settings == null) return true;
			return Boolean.parseBoolean(settings.get_Attributes().get_ItemOf("AllowNull").get_Value());
		}
	}

	public static void initialize(XmlNode xmlNode)
	{
		XmlNodeList xmlNodeList = xmlNode.get_FirstChild().get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();
			if (nodeName.equals("RestrictFonts"))
			{
				Parameter.restrictFonts = nodeValue;
			}
			else if (nodeName.equals("RestrictFonts"))
			{
				Parameter.restrictFonts = nodeValue;
			}
			else if (nodeName.equals("SpecialFontSizeIncreaseForRestrict"))
			{
				Parameter.specialFontSizeIncreaseForRestrict = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("SpecialFontSizeIncrease"))
			{
				Parameter.specialFontSizeIncrease = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("ColHiddenLiqLotForAutoClose"))
			{
				Parameter.colHiddenLiqLotForAutoClose = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("OperateOrderCount"))
			{
				Parameter.operateOrderCount = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("GetCommandsInterval"))
			{
				Parameter.getCommandsInterval = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DQQuoteDefaultLot"))
			{
				Parameter.dQQuoteDefaultLot = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("OrderBeginTimeDiff"))
			{
				Parameter.orderBeginTimeDiff = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("MooMocOrderValidDuration"))
			{
				Parameter.mooMocOrderValidDuration = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsNewOrderAcceptedHedging"))
			{
				Parameter.isNewOrderAcceptedHedging = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsUseMargin"))
			{
				Parameter.isUseMargin = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("SupportMarginAction"))
			{
				Parameter.supportMarginAction = nodeValue.toLowerCase();
			}
			else if (nodeName.equals("ReceiveAddress"))
			{
				Parameter.receiveAddress = nodeValue;
			}
			else if (nodeName.equals("IsUseDownload"))
			{
				Parameter.isUseDownload = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsGoodTillMonth"))
			{
				Parameter.isGoodTillMonth = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("TimeZoneMinuteSpan"))
			{
				Parameter.timeZoneMinuteSpan = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("StatementReportType"))
			{
				Parameter.statementReportType = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("StatementXML"))
			{
				Parameter.statementXML = nodeValue;
			}
			else if(nodeName.equals("AccountSummaryRDLC"))
			{
				Parameter.accountSummaryRDLC = nodeValue;
			}
			else if (nodeName.equals("Statement2XML"))
			{
				Parameter.statement2XML = nodeValue;
			}
			else if (nodeName.equals("Statement3XML"))
			{
				Parameter.statement3XML = nodeValue;
			}
			else if (nodeName.equals("Statement5XML"))
			{
				Parameter.statement5XML = nodeValue;
			}
			else if(nodeName.equals("StatementMCXML"))
			{
				Parameter.statementMCXML = nodeValue;
			}
			else if (nodeName.equals("LedgerXML"))
			{
				Parameter.ledgerXML = nodeValue;
			}
			else if (nodeName.equals("ExtendDay"))
			{
				Parameter.extendDay = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("QuoteDelay"))
			{
				Parameter.quoteDelay = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("NonQuoteVerificationUiDelay"))
			{
				Parameter.nonQuoteVerificationUiDelay = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsCheckAcceptDQVariation"))
			{
				Parameter.isCheckAcceptDQVariation = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsDisplayOperateOrderTimePrompt"))
			{
				Parameter.isDisplayOperateOrderTimePrompt = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsAllowMixAgent"))
			{
				Parameter.isAllowMixAgent = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasChatRoom"))
			{
				Parameter.isHasChatRoom = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasDebug"))
			{
				Parameter.isHasDebug = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsMultiShowAnalyticChart"))
			{
				Parameter.isMultiShowAnalyticChart = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasNews"))
			{
				Parameter.isHasNews = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsUseOuterNews"))
			{
				Parameter.isUseOuterNews = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("NewsUrl"))
			{
				Parameter.newsUrl = nodeValue;
			}
			else if (nodeName.equals("TradingFactUrl"))
			{
				Parameter.tradingFactUrl = nodeValue;
			}
			else if (nodeName.equals("IsCalcCommission"))
			{
				Parameter.isCalcCommission = Boolean.valueOf(nodeValue).booleanValue();
			}
			/*
			else if (nodeName.equals("GoodTillMonthType"))
			{
				Parameter.goodTillMonthType = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsHasGoodTillMonthDayOrder"))
			{
				Parameter.isHasGoodTillMonthDayOrder = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasGoodTillMonthSession"))
			{
				Parameter.isHasGoodTillMonthSession = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasGoodTillMonthGTM"))
			{
				Parameter.isHasGoodTillMonthGTM = Boolean.valueOf(nodeValue).booleanValue();
			}
			else if (nodeName.equals("IsHasGoodTillMonthGTF"))
			{
				Parameter.isHasGoodTillMonthGTF = Boolean.valueOf(nodeValue).booleanValue();
			}
		    */
			else if (nodeName.equals("SelectInstrumentsRange"))
			{
				Parameter.selectInstrumentsRange = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsAllowLimitInSpread"))
			{
				Parameter.isAllowLimitInSpread = Boolean.valueOf(nodeValue).booleanValue();
			}
			//get login.xml from server
			else if (nodeName.equals("NewsLanguage"))
			{
				Parameter.newsLanguage = nodeValue;
			}
			else if (nodeName.equals("Agreement"))
			{
				Login.endUser = nodeValue;
			}
			else if(nodeName.equals("ColumnSettings"))
			{
				Parameter.columnSettings = xmlNode2;
			}
			else if(nodeName.equals("IntegralitySettings"))
			{
				Parameter.integralitySettings = xmlNode2;
			}
			//Error!
			//else if (nodeName.equals("IsVisableOkForLoginForm"))
			//{
			//	Parameter.isVisableOkForLoginForm = Boolean.valueOf(nodeValue).booleanValue();
			//}
		}
	}
}
