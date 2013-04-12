package tradingConsole.service;

import java.io.File;

import framework.TimeSpan;
import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.xml.XmlNodeList;

//import tradingConsole.AppToolkit;
import tradingConsole.framework.ResourceHelper;
import java.io.InputStream;
//import framework.diagnostics.TraceType;
//import tradingConsole.TradingConsole;
import tradingConsole.ui.AlertForm;

public class ServiceTimeoutSetting
{
	private ServiceTimeoutSetting()
	{}

	public static void setValue(Class settingClass, XmlNode xmlNode)
	{
		if (xmlNode==null) return;
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();
			try
			{
				if(nodeName.equalsIgnoreCase("showComplainButton"))
				{
					boolean showComplainButton = framework.Convert.toBoolean(nodeValue);
					settingClass.getField(nodeName).set(nodeName, showComplainButton);
				}
				else if(nodeName.equalsIgnoreCase("isForRSZQ"))
				{
					boolean isForRSZQ = framework.Convert.toBoolean(nodeValue);
					settingClass.getField(nodeName).set(nodeName, isForRSZQ);
				}
				else if(nodeName.equalsIgnoreCase("acceptOrganizations"))
				{
					settingClass.getField(nodeName).set(nodeName, nodeValue.toUpperCase().trim());
				}
				else
				{
					int secounds = framework.Convert.toInt32(nodeValue);
					TimeSpan timeSpan = TimeSpan.fromSeconds(secounds);
					settingClass.getField(nodeName).set(nodeName, timeSpan);
				}
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

	public synchronized static void initialize()
	{
		try
		{
			//String filePath = AppToolkit.get_ConfigurationDirectory()
			//	+ File.separator + "Settings.xml";
			//xmlDocument.load(filePath);
			InputStream inputStream = ResourceHelper.getAsStream("Configuration","Settings.xml");
			if (inputStream != null)
			{
				XmlDocument xmlDocument = new XmlDocument();
				xmlDocument.load(inputStream);
				XmlNode xmlNode = xmlDocument.get_DocumentElement();
				ServiceTimeoutSetting.setValue(ServiceTimeoutSetting.class, xmlNode.get_Item("ServiceTimeoutSetting"));
			}
			else
			{
				AlertForm alertForm = new AlertForm("ERROR", "Failed to read Settings.xml!");
				alertForm.show();
				//TradingConsole.traceSource.trace(TraceType.Error, "Failed to read Settings.xml!");
			}
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public static TimeSpan login = TimeSpan.fromSeconds(90);
	public static TimeSpan authenticateForTickByTick = TimeSpan.fromMinutes(1);
	public static TimeSpan authenticate = TimeSpan.fromMinutes(1);
	public static TimeSpan additionalClient = TimeSpan.fromMinutes(1);
	public static TimeSpan agent = TimeSpan.fromMinutes(1);
	public static TimeSpan assign = TimeSpan.fromMinutes(5);
	public static TimeSpan callMarginExtension = TimeSpan.fromMinutes(1);
	public static TimeSpan callService = TimeSpan.fromSeconds(10);
	public static TimeSpan cancelLMTOrder = TimeSpan.fromMinutes(2);
	public static TimeSpan clearHistoryDataCache = TimeSpan.fromMinutes(1);
	public static TimeSpan deleteMessage = TimeSpan.fromMinutes(1);
	public static TimeSpan emailExecuteOrders = TimeSpan.fromMinutes(1);
	public static TimeSpan fundTransfer = TimeSpan.fromMinutes(1);
	public static TimeSpan getAccounts = TimeSpan.fromMinutes(3);
	public static TimeSpan getChartData = TimeSpan.fromMinutes(5);
	public static TimeSpan getColorSettingsForJava = TimeSpan.fromMinutes(1);
	public static TimeSpan getCommands = TimeSpan.fromSeconds(5);
	public static TimeSpan getCommands2 = TimeSpan.fromSeconds(5);
	public static TimeSpan getCurrencyRateByAccountID = TimeSpan.fromMinutes(1);
	public static TimeSpan getCustomerInfo = TimeSpan.fromMinutes(1);
	public static TimeSpan getInitData = TimeSpan.fromMinutes(5);
	public static TimeSpan getInstrumentForSetting = TimeSpan.fromMinutes(1);
	public static TimeSpan getInstruments = TimeSpan.fromMinutes(1);
	public static TimeSpan getInstrumentsTest = TimeSpan.fromMinutes(1);
	public static TimeSpan getLogoForJava = TimeSpan.fromMinutes(1);
	public static TimeSpan getMessages = TimeSpan.fromMinutes(2);
	public static TimeSpan getNewsContents = TimeSpan.fromMinutes(1);
	public static TimeSpan getNewsList = TimeSpan.fromMinutes(2);
	public static TimeSpan getOuterNews = TimeSpan.fromMinutes(2);
	public static TimeSpan getParameterForJava = TimeSpan.fromMinutes(1);
	public static TimeSpan getSystemTime = TimeSpan.fromSeconds(10);
	public static TimeSpan getTickByTickHistoryDatas = TimeSpan.fromMinutes(5);
	public static TimeSpan getTimeInfo = TimeSpan.fromSeconds(3);
	public static TimeSpan getTimeSyncSettings = TimeSpan.fromMinutes(2);
	public static TimeSpan getTracePropertiesForJava = TimeSpan.fromMinutes(1);
	public static TimeSpan ledger = TimeSpan.fromMinutes(3);
	public static TimeSpan ledgerForJava = TimeSpan.fromMinutes(3);
	public static TimeSpan logout = TimeSpan.fromMinutes(2);
	public static TimeSpan notifyCustomerExecuteOrder = TimeSpan.fromMinutes(1);
	public static TimeSpan notifyCustomerExecuteOrderForJava = TimeSpan.fromMinutes(1);
	public static TimeSpan paymentInstruction = TimeSpan.fromMinutes(1);
	public static TimeSpan place = TimeSpan.fromMinutes(3);
	public static TimeSpan cancelQuote = TimeSpan.fromSeconds(20);
	public static TimeSpan quote = TimeSpan.fromMinutes(2);
	public static TimeSpan refreshAgentAccountOrder = TimeSpan.fromMinutes(2);
	public static TimeSpan refreshInstrumentsState = TimeSpan.fromMinutes(2);
	public static TimeSpan saveIsCalculateFloat = TimeSpan.fromMinutes(1);
	public static TimeSpan setInstrumentCodes = TimeSpan.fromMinutes(2);
	public static TimeSpan statement = TimeSpan.fromMinutes(3);
	public static TimeSpan statementForJava = TimeSpan.fromMinutes(3);
	public static TimeSpan updateAccount = TimeSpan.fromMinutes(1);
	public static TimeSpan updateAccountLock = TimeSpan.fromMinutes(1);
	public static TimeSpan updateInstrumentSetting = TimeSpan.fromMinutes(1);
	public static TimeSpan updatePassword = TimeSpan.fromMinutes(1);
	public static TimeSpan updateQuotePolicyDetail = TimeSpan.fromMinutes(1);
	public static TimeSpan updateSystemParameters = TimeSpan.fromMinutes(1);
	public static TimeSpan verifyTransaction = TimeSpan.fromMinutes(1);
	public static TimeSpan getInterestRate = TimeSpan.fromMinutes(1);
	public static TimeSpan log = TimeSpan.fromMinutes(1);

	public static TimeSpan ThirtySeconds = TimeSpan.fromSeconds(30);
	public static TimeSpan OneMinute = TimeSpan.fromMinutes(1);
	public static TimeSpan TwoMinutes = TimeSpan.fromMinutes(2);
	public static boolean showComplainButton = false;
	public static boolean isForRSZQ = false;
	public static String acceptOrganizations = "";
}
