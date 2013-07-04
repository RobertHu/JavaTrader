package tradingConsole.settings;

import java.io.*;
import java.net.*;

import java.awt.*;
import javax.swing.*;

import framework.*;
import framework.diagnostics.*;
import framework.io.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.framework.*;
import tradingConsole.ui.*;
import tradingConsole.ui.language.*;
import Packet.Settings;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import Util.XmlElementHelper;
import nu.xom.ParsingException;
import Util.DocumentHelper;
import nu.xom.Elements;

public class ServiceManager
{
	private String serviceUrl = "";
	private String serviceUrlFor99Bill = "";
	private String companyCodeForTransfer= "";
	private boolean showMarginAsChuRuJin=false;
	//1st service host, not allow change
	private String[] serverHosts;
	//user define service host, allow change
	private String userDefineServiceHost = "";
	//using which service host
	private int selectedServiceHostSequence = 1; //0: userDefineServiceHost,1: serviceHost1,2: serviceHost2,3: serviceHost3,4: serviceHost4,5: serviceHost5,6: serviceHost6
	private String[] mapPorts;
	private String userDefinePasswordLink = "";
	private String[] forgetPasswordLinks;
	private String priceAlignString = "";
	private String placardUrl = "";
	private String useCell = "false";

	private String recoverPasswordAnswerCountString = "";
	private String defaultMainFormColor = "255,255,255";

	private int backupServerNumber = 0;
	private String backupServiceUrl = "";
	private String backupAuthenticationUrl = "";

	private String backupServiceUrl2 = "";
	private String backupAuthenticationUrl2 = "";

	private String backupServiceUrl3 = "";
	private String backupAuthenticationUrl3 = "";

	private int usingBackupSettingsIndex = -1;

	private boolean useBlackAsBackground = false;
	private Integer waitTimeout;
	private String defaultMapPort;

	public int getMapPort()
	{
		String mapPortString = this.getSelectedMapPortString();
		return (StringHelper.isNullOrEmpty(mapPortString)) ? AppToolkit.getPort(this.serviceUrl) : XmlConvert.toInt32(mapPortString);
	}

	public int getMapPort(int selectedServiceHostSequence)
	{
		if(selectedServiceHostSequence < 0 || selectedServiceHostSequence> this.serverHosts.length)
		{
			return Integer.parseInt( this.defaultMapPort);
		}
		return Integer.parseInt(this.mapPorts[selectedServiceHostSequence - 1]);
	}


	private String getSelectedMapPortString()
	{
		if (this.selectedServiceHostSequence < 0 || this.selectedServiceHostSequence >  this.mapPorts.length)
		{
			this.selectedServiceHostSequence = 0;
		}
		if(this.selectedServiceHostSequence ==0)
		{
			return this.defaultMapPort;
		}
		return this.mapPorts[this.selectedServiceHostSequence - 1];
	}

	public String getSelectedForgetPasswordLink()
	{
		return this.getForgetPasswordLink(this.selectedServiceHostSequence);
	}

	public String getSelectedHostName()
	{
		if (this.selectedServiceHostSequence < 0 || this.selectedServiceHostSequence > this.serverHosts.length)
		{
			this.selectedServiceHostSequence = 0;
			return this.userDefineServiceHost;
		}
		String hostName = "";
		if (this.selectedServiceHostSequence == 0)
		{
			hostName = this.userDefineServiceHost;
		}
		else if (this.selectedServiceHostSequence == 1)
		{
			hostName = Language.serviceHost1;
		}
		else if (this.selectedServiceHostSequence == 2)
		{
			hostName = Language.serviceHost2;
		}
		else if (this.selectedServiceHostSequence == 3)
		{
			hostName = Language.serviceHost3;
		}
		else if (this.selectedServiceHostSequence == 4)
		{
			hostName = Language.serviceHost4;
		}
		else if (this.selectedServiceHostSequence == 5)
		{
			hostName = Language.serviceHost5;
		}
		else if (this.selectedServiceHostSequence == 6)
		{
			hostName = Language.serviceHost6;
		}
		else
		{
			hostName = this.userDefineServiceHost;
		}
		return hostName;
	}

	//edit option use
	public String getSelectedHost(int selectedServiceHostSequence)
	{
		if(selectedServiceHostSequence<0 || selectedServiceHostSequence > this.serverHosts.length)
		{
			selectedServiceHostSequence = 0;
		}

		if (selectedServiceHostSequence == 0)
		{
			return this.userDefineServiceHost;
		}
	    return  this.serverHosts[selectedServiceHostSequence - 1];
	}

	public String getForgetPasswordLink(int selectedServiceHostSequence)
	{
		if(selectedServiceHostSequence<0 || selectedServiceHostSequence > this.serverHosts.length)
		{
			selectedServiceHostSequence = 0;
		}
		if (selectedServiceHostSequence == 0)
		{
			return this.userDefinePasswordLink;
		}
		return this.forgetPasswordLinks[selectedServiceHostSequence - 1];
	}

	public String getSelectedHost()
	{
		return this.getSelectedHost(this.selectedServiceHostSequence);
	}

	public int getPriceAlign()
	{
		int align = (StringHelper.isNullOrEmpty(this.priceAlignString)) ? 2 : XmlConvert.toInt32(this.priceAlignString);
		switch(align)
		{
			case 0:
				return SwingConstants.LEFT;
			case 1:
				return SwingConstants.CENTER;
			default:
				return SwingConstants.RIGHT;
		}
	}

	public int getRecoverPasswordAnswerCount()
	{
		int recoverPasswordAnswerCount = (StringHelper.isNullOrEmpty(this.recoverPasswordAnswerCountString)) ? 2 :
			XmlConvert.toInt32(this.recoverPasswordAnswerCountString);
		if (recoverPasswordAnswerCount < 0 || recoverPasswordAnswerCount > 10)
		{
			recoverPasswordAnswerCount = 6;
		}
		return recoverPasswordAnswerCount;
	}

	public String get_PlacardUrl()
	{
		return this.placardUrl;
	}

	public boolean get_UseBlackAsBackground()
	{
		return this.useBlackAsBackground;
	}

	public boolean get_UseCell()
	{
		return this.useCell.equalsIgnoreCase("true");
	}

	public Color get_DefaultMainFormColor()
	{
		try
		{
			String[] rgbString = StringHelper.split(this.defaultMainFormColor, ",");
			int r = Integer.parseInt(rgbString[0]);
			int g = Integer.parseInt(rgbString[1]);
			int b = Integer.parseInt(rgbString[2]);
			return new Color(r, g, b);
		}
		catch (java.lang.Throwable throwable)
		{
			return Color.lightGray;
		}
	}

	public String get_serviceUrlFor99Bill()
	{
		String serviceUrl = this.get_ServiceUrl();
		int index = serviceUrl.indexOf("://");
		int index2 = serviceUrl.indexOf("/", index + 3);
		String domainName = serviceUrl.substring(index + 3, index2);
		if(this.serviceUrlFor99Bill.indexOf(domainName) < 0)
		{
			index = this.serviceUrlFor99Bill.indexOf("://");
			index2 = this.serviceUrlFor99Bill.indexOf("/", index + 3);
			this.serviceUrlFor99Bill = this.serviceUrlFor99Bill.substring(0, index + 3)
				+ domainName + this.serviceUrlFor99Bill.substring(index2);
		}
		return this.serviceUrlFor99Bill;
	}

	public String get_companyCodeForTransfer()
	{
		return this.companyCodeForTransfer;
	}

	public boolean get_ShowMarginAsChuRuJin()
	{
		return this.showMarginAsChuRuJin;
	}

	public String get_ServiceUrl()
	{
		switch(this.usingBackupSettingsIndex)
		{
			case 0:
				return this.backupServiceUrl;
			case 1:
				return this.backupServiceUrl2;
			case 2:
				return this.backupServiceUrl3;
			default:
				return this.serviceUrl;
		}
	}



	public int get_SelectedServiceHostSequence()
	{
		if (this.selectedServiceHostSequence < 0 || this.selectedServiceHostSequence > this.serverHosts.length)
		{
			this.selectedServiceHostSequence = 0;
		}
		return this.selectedServiceHostSequence;
	}

	public int get_TotalService()
	{
		return this.serverHosts.length;
	}

	public int getBackupServerNumber()
	{
		return this.backupServerNumber;
	}

	public int getWaitTimeout(){
		return this.waitTimeout;
	}


	public boolean tryToUsingBackupSettings(int index)
	{
		this.usingBackupSettingsIndex = index;

		if(this.usingBackupSettingsIndex > -1 && this.usingBackupSettingsIndex < this.backupServerNumber)
		{
			String backupAuthenticationUrl = this.backupAuthenticationUrl;
			String backupServiceUrl = this.backupServiceUrl;
			if(this.usingBackupSettingsIndex == 1)
			{
				backupAuthenticationUrl = this.backupAuthenticationUrl2;
				backupServiceUrl = this.backupServiceUrl2;
			}
			else if(this.usingBackupSettingsIndex == 2)
			{
				backupAuthenticationUrl = this.backupAuthenticationUrl3;
				backupServiceUrl = this.backupServiceUrl3;
			}

			if (StringHelper.isNullOrEmpty(backupAuthenticationUrl) || StringHelper.isNullOrEmpty(backupServiceUrl))
			{
				this.usingBackupSettingsIndex = -1;
			}
		}
		return this.usingBackupSettingsIndex > -1 && this.usingBackupSettingsIndex < this.backupServerNumber;
	}

	private ServiceManager()
	{
	}


	private void setConfigSettings() throws ParsingException, FileNotFoundException, IOException
	{
		Document doc = DocumentHelper.load(Settings.ConfigFileName);
		if(doc==null)
		{
			throw new FileNotFoundException();
		}
		Element root = doc.getRootElement();
		Element settingElement = root.getFirstChildElement("appSettings");
		Element hostElement = settingElement.getFirstChildElement("serviceHosts");
		Elements hostChildren = hostElement.getChildElements("serviceHost");
		serviceUrl = XmlElementHelper.getInnerValue(settingElement,"serviceUrl");
		if (settingElement.getFirstChildElement("useBlackAsBackground") != null)
		{
			useBlackAsBackground = XmlConvert.toBoolean(XmlElementHelper.getInnerValue(settingElement, "useBlackAsBackground"));
		}

		if (settingElement.getFirstChildElement("useGreenAsRiseColor") != null)
		{
			PublicParametersManager.useGreenAsRiseColor = XmlConvert.toBoolean(XmlElementHelper.getInnerValue(settingElement, "useGreenAsRiseColor"));
		}

		int hostCount = hostChildren.size();
		serverHosts = new String[hostCount];
		mapPorts = new String[hostCount];
		for (int i = 0; i < hostCount; i++)
		{
			Element item =hostChildren.get(i);
			serverHosts[i] = item.getValue();
			mapPorts[i] = item.getAttributeValue("port");
		}
		Element forgetPwdElement = settingElement.getFirstChildElement("forgetPasswordLinks");
		Elements forgetPwdChildren = forgetPwdElement.getChildElements("forgetPasswordLink");
		int forgetPwdCount = forgetPwdChildren.size();
		forgetPasswordLinks = new String[forgetPwdCount];
		for (int i = 0; i < forgetPwdCount; i++)
		{
			forgetPasswordLinks[i] = forgetPwdChildren.get(i).getValue();
		}

		if (settingElement.getFirstChildElement("serviceUrlFor99Bill") != null)
		{
			serviceUrlFor99Bill = XmlElementHelper.getInnerValue(settingElement, "serviceUrlFor99Bill");
		}
		if (settingElement.getFirstChildElement("companyCodeForTransfer") != null)
		{
			companyCodeForTransfer = XmlElementHelper.getInnerValue(settingElement, "companyCodeForTransfer");
		}

		if (settingElement.getFirstChildElement("priceAlign") != null)
		{
			priceAlignString = XmlElementHelper.getInnerValue(settingElement, "priceAlign");
		}
		if (settingElement.getFirstChildElement("recoverPasswordAnswerCount") != null)
		{
			recoverPasswordAnswerCountString = XmlElementHelper.getInnerValue(settingElement, "recoverPasswordAnswerCount");
		}
		if (settingElement.getFirstChildElement("useCell") != null)
		{
			useCell = XmlElementHelper.getInnerValue(settingElement, "useCell");
		}
		if (settingElement.getFirstChildElement("defaultMainFormColor") != null)
		{
			defaultMainFormColor = XmlElementHelper.getInnerValue(settingElement, "defaultMainFormColor");
		}
		if (settingElement.getFirstChildElement("waitTimeout") != null)
		{
			waitTimeout = Integer.parseInt(XmlElementHelper.getInnerValue(settingElement, "waitTimeout"));
		}
	}

	private void setLocalConfigSettings() throws ParsingException, FileNotFoundException, IOException
	{
		XmlDocument doc = new XmlDocument();
		doc.load(Settings.LocalConfigFileName);
		XmlNode root = doc.get_DocumentElement();
		userDefineServiceHost = root.get_Item("userDefineServiceHost").get_InnerText();
		userDefinePasswordLink =root.get_Item("userDefinePasswordLink").get_InnerText();
		selectedServiceHostSequence = XmlConvert.toInt32(root.get_Item("selectedServiceHostSequence").get_InnerText());
		defaultMapPort = root.get_Item("defaultMapPort").get_InnerText();
	}


	public static ServiceManager Create()
	{
		ServiceManager serviceManager = new ServiceManager();
		try
		{
			serviceManager.setConfigSettings();
			serviceManager.setLocalConfigSettings();
		}
		catch (Throwable exception)
		{
			AlertForm alertForm = new AlertForm("Notify", "Incorrect configuration.");
			alertForm.show();
		}

		return serviceManager;
	}





	//private void autoUpdaterCopy()
	//{
	//	ServiceManager.autoUpdaterCopy(autoUpdateUrl, XmlConvert.toString(this.autoUpdateListenPort));
	//}

	/*
	private static void autoUpdaterCopy(String autoUpdateUrl, String autoUpdateListenPortString)
	{
		String appBaseDirectory = AppToolkit.get_AppBaseDirectory();
		String filePath = appBaseDirectory + "DeploymentServerUrl.properties";
		String data = "DeploymentServerUrl=" + autoUpdateUrl + TradingConsole.enterLine;
		data += "AutoUpdateListenPort=" + autoUpdateListenPortString;
		AppToolkit.writeFile(filePath, data, false);
	}
    */

	//public static boolean saveServerSetting(int selectedServiceHostSequence, String host, String autoUpdateListenPort)
	public static boolean saveServerSetting(int selectedServiceHostSequence, String host, String forgetPasswordHost)
	{
		try
		{
			XmlDocument doc  = new XmlDocument();
			doc.load(Settings.LocalConfigFileName);
			XmlNode root = doc.get_DocumentElement();
			if (selectedServiceHostSequence == 0)
			{
				root.get_Item("userDefineServiceHost").set_InnerText(host);
				String forgetPasswordLink = root.get_Item("userDefinePasswordLink").get_InnerText();
				if (!StringHelper.isNullOrEmpty(forgetPasswordLink))
				{
					root.get_Item("userDefinePasswordLink").set_InnerText(forgetPasswordHost);
				}
			}
			root.get_Item("selectedServiceHostSequence").set_InnerText(XmlConvert.toString(selectedServiceHostSequence));
			doc.save(Settings.LocalConfigFileName);
			return true;
		}
		catch (Throwable exception)
		{
			return false;
		}
	}

	private static void copy(Integer userSelectedServiceHostSequence, String userSelectedServiceHostUrl)
	{
		//String sourceFilePath = AppToolkit.get_ConfigurationDirectory() + "TradingConsole.config";
		//XmlNode xmlNode = AppToolkit.getXml(sourceFilePath);
		XmlNode xmlNode = AppToolkit.getResourceXml("Configuration", "TradingConsole.config");
		if (xmlNode == null)
		{
			return;
		}

		XmlNode xmlNode2 = xmlNode.get_Item("appSettings");
		//String autoUpdateUrl = xmlNode2.get_Item("autoUpdateUrl").get_InnerText();
		//String autoUpdateListenPort = xmlNode2.get_Item("autoUpdateListenPort").get_InnerText();
		String authenticationUrl = xmlNode2.get_Item("authenticationUrl").get_InnerText();
		String serviceUrl = xmlNode2.get_Item("serviceUrl").get_InnerText();
		String serviceUrlFor99Bill="";
		String companyCodeForTransfer = "";
		String showMarginAsChuRuJin="";

		String backupServerNumber = "";
		if (xmlNode2.get_Item("backupServerNumber") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupServerNumber").get_InnerText()))
		{
			backupServerNumber = xmlNode2.get_Item("backupServerNumber").get_InnerText();
		}


		if (xmlNode2.get_Item("serviceUrlFor99Bill") != null)
		{
			serviceUrlFor99Bill = xmlNode2.get_Item("serviceUrlFor99Bill").get_InnerText();
		}
		if (xmlNode2.get_Item("companyCodeForTransfer") != null)
		{
			companyCodeForTransfer = xmlNode2.get_Item("companyCodeForTransfer").get_InnerText();
		}

		if (xmlNode2.get_Item("showMarginAsChuRuJin") != null)
		{
			showMarginAsChuRuJin = xmlNode2.get_Item("showMarginAsChuRuJin").get_InnerText();
		}


		String useGreenAsRiseColor = "false";
		if (xmlNode2.get_Item("useGreenAsRiseColor") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("useGreenAsRiseColor").get_InnerText()))
		{
			useGreenAsRiseColor = xmlNode2.get_Item("useGreenAsRiseColor").get_InnerText();
		}

		String useBlackAsBackground = "false";
		if (xmlNode2.get_Item("useBlackAsBackground") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("useBlackAsBackground").get_InnerText()))
		{
			useBlackAsBackground = xmlNode2.get_Item("useBlackAsBackground").get_InnerText();
		}


		String backupServiceUrl = "";
		if (xmlNode2.get_Item("backupServiceUrl") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupServiceUrl").get_InnerText()))
		{
			backupServiceUrl = xmlNode2.get_Item("backupServiceUrl").get_InnerText();
		}

		String backupAuthenticationUrl = "";
		if (xmlNode2.get_Item("backupAuthenticationUrl") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupAuthenticationUrl").get_InnerText()))
		{
			backupAuthenticationUrl = xmlNode2.get_Item("backupAuthenticationUrl").get_InnerText();
		}

		String backupServiceUrl2 = "";
		if (xmlNode2.get_Item("backupServiceUrl2") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupServiceUrl2").get_InnerText()))
		{
			backupServiceUrl2 = xmlNode2.get_Item("backupServiceUrl2").get_InnerText();
		}

		String backupAuthenticationUrl2 = "";
		if (xmlNode2.get_Item("backupAuthenticationUrl2") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupAuthenticationUrl2").get_InnerText()))
		{
			backupAuthenticationUrl2 = xmlNode2.get_Item("backupAuthenticationUrl2").get_InnerText();
		}

		String backupServiceUrl3 = "";
		if (xmlNode2.get_Item("backupServiceUrl3") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupServiceUrl3").get_InnerText()))
		{
			backupServiceUrl3 = xmlNode2.get_Item("backupServiceUrl3").get_InnerText();
		}

		String backupAuthenticationUrl3 = "";
		if (xmlNode2.get_Item("backupAuthenticationUrl3") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("backupAuthenticationUrl3").get_InnerText()))
		{
			backupAuthenticationUrl3 = xmlNode2.get_Item("backupAuthenticationUrl3").get_InnerText();
		}

		String version = "";
		if (xmlNode2.get_Item("configurationversion") != null && !StringHelper.isNullOrEmpty(xmlNode2.get_Item("configurationversion").get_InnerText()))
		{
			version = xmlNode2.get_Item("configurationversion").get_InnerText();
		}

		int selectedServiceHostSequence = 0;
		if (xmlNode2.get_Item("selectedServiceHostSequence") != null)
		{
			selectedServiceHostSequence = XmlConvert.toInt32(xmlNode2.get_Item("selectedServiceHostSequence").get_InnerText());
		}

		String serviceHost1 = "";
		if (xmlNode2.get_Item("serviceHost1") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost1").get_InnerText()))
		{
			serviceHost1 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost1 = xmlNode2.get_Item("serviceHost1").get_InnerText();
		}
		userSelectedServiceHostUrl = userSelectedServiceHostUrl == null ? "" : userSelectedServiceHostUrl;
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 1
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost1) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}

		String serviceHost2 = "";
		if (xmlNode2.get_Item("serviceHost2") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost2").get_InnerText()))
		{
			serviceHost2 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost2 = xmlNode2.get_Item("serviceHost2").get_InnerText();
		}
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 2
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost2) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}

		String serviceHost3 = "";
		if (xmlNode2.get_Item("serviceHost3") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost3").get_InnerText()))
		{
			serviceHost3 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost3 = xmlNode2.get_Item("serviceHost3").get_InnerText();
		}
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 3
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost3) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}

		String serviceHost4 = "";
		if (xmlNode2.get_Item("serviceHost4") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost4").get_InnerText()))
		{
			serviceHost4 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost4 = xmlNode2.get_Item("serviceHost4").get_InnerText();
		}
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 4
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost4) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}


		String serviceHost5 = "";
		if (xmlNode2.get_Item("serviceHost5") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost5").get_InnerText()))
		{
			serviceHost5 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost5 = xmlNode2.get_Item("serviceHost5").get_InnerText();
		}
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 5
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost5) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}


		String serviceHost6 = "";
		if (xmlNode2.get_Item("serviceHost6") == null || StringHelper.isNullOrEmpty(xmlNode2.get_Item("serviceHost6").get_InnerText()))
		{
			serviceHost6 = AppToolkit.getHost(serviceUrl);
		}
		else
		{
			serviceHost6 = xmlNode2.get_Item("serviceHost6").get_InnerText();
		}
		if(userSelectedServiceHostSequence != null && userSelectedServiceHostSequence.intValue() == 6
		   && userSelectedServiceHostUrl.compareToIgnoreCase(serviceHost6) == 0)
		{
			selectedServiceHostSequence = userSelectedServiceHostSequence.intValue();
		}


		if (StringHelper.isNullOrEmpty(serviceHost1) && StringHelper.isNullOrEmpty(serviceHost2) && StringHelper.isNullOrEmpty(serviceHost3) &&
			StringHelper.isNullOrEmpty(serviceHost4) && StringHelper.isNullOrEmpty(serviceHost5) && StringHelper.isNullOrEmpty(serviceHost6))
		{
			AlertForm alertForm = new AlertForm("Notify", "Incorrect Service.xml");
			alertForm.show();
			TradingConsole.traceSource.trace(TraceType.Error,
											 "Incorrect Service.xml,but copy serviceUrl's host to serviceHost1, serviceHost2 and serviceHost3 and serviceHost4 and serviceHost5 and serviceHost6!");
		}

		String userDefineServiceHost = "";
		if (xmlNode2.get_Item("userDefineServiceHost") != null)
		{
			userDefineServiceHost = xmlNode2.get_Item("userDefineServiceHost").get_InnerText();
		}

		int totalService = 1;
		if (xmlNode2.get_Item("totalService") != null)
		{
			totalService = XmlConvert.toInt32(xmlNode2.get_Item("totalService").get_InnerText());
		}

		String mapPortString = "";
		if (xmlNode2.get_Item("mapPort") != null)
		{
			mapPortString = xmlNode2.get_Item("mapPort").get_InnerText();
		}
		String mapPortString1 = "";
		if (xmlNode2.get_Item("mapPort1") != null)
		{
			mapPortString1 = xmlNode2.get_Item("mapPort1").get_InnerText();
		}
		String mapPortString2 = "";
		if (xmlNode2.get_Item("mapPort2") != null)
		{
			mapPortString2 = xmlNode2.get_Item("mapPort2").get_InnerText();
		}
		String mapPortString3 = "";
		if (xmlNode2.get_Item("mapPort3") != null)
		{
			mapPortString3 = xmlNode2.get_Item("mapPort3").get_InnerText();
		}
		String mapPortString4 = "";
		if (xmlNode2.get_Item("mapPort4") != null)
		{
			mapPortString4 = xmlNode2.get_Item("mapPort4").get_InnerText();
		}
		String mapPortString5 = "";
		if (xmlNode2.get_Item("mapPort5") != null)
		{
			mapPortString5 = xmlNode2.get_Item("mapPort5").get_InnerText();
		}
		String mapPortString6 = "";
		if (xmlNode2.get_Item("mapPort6") != null)
		{
			mapPortString6 = xmlNode2.get_Item("mapPort6").get_InnerText();
		}
		String priceAlignString = "";
		if (xmlNode2.get_Item("priceAlign") != null)
		{
			priceAlignString = xmlNode2.get_Item("priceAlign").get_InnerText();
		}
		String recoverPasswordAnswerCountString = "";
		if (xmlNode2.get_Item("recoverPasswordAnswerCount") != null)
		{
			recoverPasswordAnswerCountString = xmlNode2.get_Item("recoverPasswordAnswerCount").get_InnerText();
		}
		String placardUrl = "";
		if (xmlNode2.get_Item("placardUrl") != null)
		{
			placardUrl = xmlNode2.get_Item("placardUrl").get_InnerText();
		}
		String useCell = "false";
		if (xmlNode2.get_Item("useCell") != null)
		{
			useCell = xmlNode2.get_Item("useCell").get_InnerText();
		}
		String forgetPasswordLink = "";
		if (xmlNode2.get_Item("forgetPasswordLink") != null)
		{
			forgetPasswordLink = xmlNode2.get_Item("forgetPasswordLink").get_InnerText();
		}

		String forgetPasswordLink1 = "";
		if (xmlNode2.get_Item("forgetPasswordLink1") != null)
		{
			forgetPasswordLink1 = xmlNode2.get_Item("forgetPasswordLink1").get_InnerText().trim();
		}
		String forgetPasswordLink2 = "";
		if (xmlNode2.get_Item("forgetPasswordLink2") != null)
		{
			forgetPasswordLink2 = xmlNode2.get_Item("forgetPasswordLink2").get_InnerText().trim();
		}
		String forgetPasswordLink3 = "";
		if (xmlNode2.get_Item("forgetPasswordLink3") != null)
		{
			forgetPasswordLink3 = xmlNode2.get_Item("forgetPasswordLink3").get_InnerText().trim();
		}
		String forgetPasswordLink4 = "";
		if (xmlNode2.get_Item("forgetPasswordLink4") != null)
		{
			forgetPasswordLink4 = xmlNode2.get_Item("forgetPasswordLink4").get_InnerText().trim();
		}
		String forgetPasswordLink5 = "";
		if (xmlNode2.get_Item("forgetPasswordLink5") != null)
		{
			forgetPasswordLink5 = xmlNode2.get_Item("forgetPasswordLink5").get_InnerText().trim();
		}
		String forgetPasswordLink6 = "";
		if (xmlNode2.get_Item("forgetPasswordLink6") != null)
		{
			forgetPasswordLink6 = xmlNode2.get_Item("forgetPasswordLink6").get_InnerText().trim();
		}
		String userDefinePasswordLink = "";
		if (xmlNode2.get_Item("userDefinePasswordLink") != null)
		{
			userDefinePasswordLink = xmlNode2.get_Item("userDefinePasswordLink").get_InnerText();
		}

		String defaultMainFormColor = "";
		if (xmlNode2.get_Item("defaultMainFormColor") != null)
		{
			defaultMainFormColor = xmlNode2.get_Item("defaultMainFormColor").get_InnerText();
		}
		String waitTimeout="";
		if(xmlNode2.get_Item("waitTimeout")!=null)
		{
			waitTimeout= xmlNode2.get_Item("waitTimeout").get_InnerText();
		}

		//String settingDirectory = AppToolkit.get_SettingDirectory();
		String settingDirectory = AppToolkit.get_CompanySettingDirectory();
		//String parameterFilePath = settingDirectory + "TradingConsole.config";
		String parameterFilePath = DirectoryHelper.combine(settingDirectory,"TradingConsole.config");
		File parameterFile = new File(parameterFilePath);

		parameterFile.delete();

		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
		xml += "<configuration>";
		xml += "<appSettings>";
		if(!StringHelper.isNullOrEmpty(version))
		{
			xml += "<configurationversion>" + version + "</configurationversion>";
		}
		//xml += "<autoUpdateUrl>" + autoUpdateUrl + "</autoUpdateUrl>";
		//xml += "<autoUpdateListenPort>" + autoUpdateListenPort + "</autoUpdateListenPort>";
		xml += "<authenticationUrl>" + authenticationUrl + "</authenticationUrl>";
		xml += "<serviceUrl>" + serviceUrl + "</serviceUrl>";
		xml += "<serviceUrlFor99Bill>" + serviceUrlFor99Bill + "</serviceUrlFor99Bill>";
		xml += "<companyCodeForTransfer>" + companyCodeForTransfer + "</companyCodeForTransfer>";
		if(!StringHelper.isNullOrEmpty(showMarginAsChuRuJin)) xml += "<showMarginAsChuRuJin>" + showMarginAsChuRuJin + "</showMarginAsChuRuJin>";
		xml += "<useBlackAsBackground>" + useBlackAsBackground + "</useBlackAsBackground>";
		xml += "<useGreenAsRiseColor>" + useGreenAsRiseColor + "</useGreenAsRiseColor>";

		xml += "<backupServerNumber>" + backupServerNumber + "</backupServerNumber>";
		xml += "<backupAuthenticationUrl>" + backupAuthenticationUrl + "</backupAuthenticationUrl>";
		xml += "<backupServiceUrl>" + backupServiceUrl + "</backupServiceUrl>";
		xml += "<backupAuthenticationUrl2>" + backupAuthenticationUrl2 + "</backupAuthenticationUrl2>";
		xml += "<backupServiceUrl2>" + backupServiceUrl2 + "</backupServiceUrl2>";
		xml += "<backupAuthenticationUrl3>" + backupAuthenticationUrl3 + "</backupAuthenticationUrl3>";
		xml += "<backupServiceUrl3>" + backupServiceUrl3 + "</backupServiceUrl3>";

		xml += "<serviceHost1>" + serviceHost1 + "</serviceHost1>";
		xml += "<serviceHost2>" + serviceHost2 + "</serviceHost2>";
		xml += "<serviceHost3>" + serviceHost3 + "</serviceHost3>";
		xml += "<serviceHost4>" + serviceHost4 + "</serviceHost4>";
		xml += "<serviceHost5>" + serviceHost5 + "</serviceHost5>";
		xml += "<serviceHost6>" + serviceHost6 + "</serviceHost6>";
		xml += "<userDefineServiceHost>" + userDefineServiceHost + "</userDefineServiceHost>";
		xml += "<selectedServiceHostSequence>" + XmlConvert.toString(selectedServiceHostSequence) + "</selectedServiceHostSequence>";
		xml += "<totalService>" + XmlConvert.toString(totalService) + "</totalService>";
		xml += "<mapPort>" + mapPortString + "</mapPort>";
		xml += "<mapPort1>" + mapPortString1 + "</mapPort1>";
		xml += "<mapPort2>" + mapPortString2 + "</mapPort2>";
		xml += "<mapPort3>" + mapPortString3 + "</mapPort3>";
		xml += "<mapPort4>" + mapPortString4 + "</mapPort4>";
		xml += "<mapPort5>" + mapPortString5 + "</mapPort5>";
		xml += "<mapPort6>" + mapPortString6 + "</mapPort6>";
		xml += "<priceAlign>" + priceAlignString + "</priceAlign>";
		xml += "<placardUrl>" + placardUrl + "</placardUrl>";
		xml += "<useCell>" + useCell + "</useCell>";
		xml += "<forgetPasswordLink>" + forgetPasswordLink + "</forgetPasswordLink>";
		xml += "<forgetPasswordLink1>" + forgetPasswordLink1 + "</forgetPasswordLink1>";
		xml += "<forgetPasswordLink2>" + forgetPasswordLink2 + "</forgetPasswordLink2>";
		xml += "<forgetPasswordLink3>" + forgetPasswordLink3 + "</forgetPasswordLink3>";
		xml += "<forgetPasswordLink4>" + forgetPasswordLink4 + "</forgetPasswordLink4>";
		xml += "<forgetPasswordLink5>" + forgetPasswordLink5 + "</forgetPasswordLink5>";
		xml += "<forgetPasswordLink6>" + forgetPasswordLink6 + "</forgetPasswordLink6>";
		xml += "<userDefinePasswordLink>" + userDefinePasswordLink + "</userDefinePasswordLink>";
		xml += "<recoverPasswordAnswerCount>" + recoverPasswordAnswerCountString + "</recoverPasswordAnswerCount>";
		xml += "<defaultMainFormColor>" + defaultMainFormColor + "</defaultMainFormColor>";
		xml += "<waitTimeout>" + waitTimeout +"</waitTimeout>";
		xml += "</appSettings>";
		xml += "</configuration>";
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		try
		{
			xmlDocument.save(parameterFilePath);
			//ServiceManager.autoUpdaterCopy(autoUpdateUrl, autoUpdateListenPort);
		}
		catch (Throwable exception)
		{
		}
	}

	private static void checkParameterFilePath()
	{
		Integer userSelectedHost = null;
		String userSelectedHostUrl = null;

		boolean isNeedCopy = true;
		//String settingDirectory = AppToolkit.get_SettingDirectory();
		//String parameterFilePath = settingDirectory + "TradingConsole.config";
		//String parameterFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),"TradingConsole.config");
		String parameterFilePath = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(),"TradingConsole.config");
		//File directory = new File(AppToolkit.get_SettingDirectory());
		File directory = new File(AppToolkit.get_CompanySettingDirectory());
		if (!directory.exists())
		{
			directory.mkdirs();
		}
		else
		{
			File file = new File(parameterFilePath);
			if (file.exists())
			{
				if (file.isFile())
				{
					//XmlElement destinationXmlElement = (XmlElement)AppToolkit.getUserSettingXml("TradingConsole.config");
					XmlElement destinationXmlElement = (XmlElement)AppToolkit.getCompanySettingXml("TradingConsole.config");

					//String distinationFile = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),"DefaultTradingConsole.config");
					String distinationFile = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(),"ResourceTradingConsole.config");
					try
					{
						ResourceHelper.fetchToFile("Configuration", "TradingConsole.config", distinationFile, true);
					}
					catch (IOException iOException)
					{
						TradingConsole.traceSource.trace(TraceType.Error, iOException);
					}

					//String sourceFilePath = AppToolkit.get_ConfigurationDirectory() + "TradingConsole.config";
					//XmlElement sourceXmlElement = (XmlElement)AppToolkit.getXml(sourceFilePath);
					//XmlElement sourceXmlElement = (XmlElement)AppToolkit.getUserSettingXml("DefaultTradingConsole.config");
					XmlElement sourceXmlElement = (XmlElement)AppToolkit.getCompanySettingXml("ResourceTradingConsole.config", false);
					boolean isSameItem = true;
					if(sourceXmlElement != null)
					{
						isSameItem = ServiceManager.isSameItem(sourceXmlElement, destinationXmlElement);
					}

					XmlNode destinationXmlNode = destinationXmlElement.get_Item("appSettings");
					//String autoUpdateUrl = destinationXmlNode.get_Item("autoUpdateUrl").get_InnerText();
					//String autoUpdateListenPort = destinationXmlNode.get_Item("autoUpdateListenPort").get_InnerText();
					String authenticationUrl = destinationXmlNode.get_Item("authenticationUrl").get_InnerText();
					userSelectedHost = Integer.valueOf(destinationXmlNode.get_Item("selectedServiceHostSequence").get_InnerText().trim());

					String serviceUrl = destinationXmlNode.get_Item("serviceUrl").get_InnerText();
					String serviceHost1 = destinationXmlNode.get_Item("serviceHost1").get_InnerText();
					if(userSelectedHost == 1) userSelectedHostUrl = serviceHost1;
					String serviceHost2 = destinationXmlNode.get_Item("serviceHost2").get_InnerText();
					if(userSelectedHost == 2) userSelectedHostUrl = serviceHost2;

					String serviceHost3 = "";
					if (destinationXmlNode.get_Item("serviceHost3") != null)
					{
						serviceHost3 = destinationXmlNode.get_Item("serviceHost3").get_InnerText();
						if(userSelectedHost == 3) userSelectedHostUrl = serviceHost3;
					}
					String serviceHost4 = "";
					if (destinationXmlNode.get_Item("serviceHost4") != null)
					{
						serviceHost4 = destinationXmlNode.get_Item("serviceHost4").get_InnerText();
						if(userSelectedHost == 4) userSelectedHostUrl = serviceHost3;
					}
					String serviceHost5 = "";
					if (destinationXmlNode.get_Item("serviceHost5") != null)
					{
						serviceHost5 = destinationXmlNode.get_Item("serviceHost5").get_InnerText();
						if(userSelectedHost == 5) userSelectedHostUrl = serviceHost3;
					}
					String serviceHost6 = "";
					if (destinationXmlNode.get_Item("serviceHost6") != null)
					{
						serviceHost6 = destinationXmlNode.get_Item("serviceHost6").get_InnerText();
						if(userSelectedHost == 6) userSelectedHostUrl = serviceHost3;
					}

					int totalService = 1;
					if (destinationXmlNode.get_Item("totalService") != null)
					{
						totalService = XmlConvert.toInt32(destinationXmlNode.get_Item("totalService").get_InnerText());
					}

					if (!isSameItem)
					{
						isNeedCopy = true;
					}
					else
					{
						//isNeedCopy = !ServiceManager.isValidate(autoUpdateUrl, autoUpdateListenPort, authenticationUrl, serviceUrl, totalService,
						//	serviceHost1, serviceHost2, serviceHost3, serviceHost4, serviceHost5, serviceHost6);
						isNeedCopy = !ServiceManager.isValidate(authenticationUrl, serviceUrl, totalService,
							serviceHost1, serviceHost2, serviceHost3, serviceHost4, serviceHost5, serviceHost6);
					}
				}
			}
		}
		if (isNeedCopy)
		{
			ServiceManager.copy(userSelectedHost, userSelectedHostUrl);
		}
	}

	//private static boolean isValidate(String autoUpdateUrl, String autoUpdateListenPortString, String authenticationUrl, String serviceUrl, int totalService,
	//								  String serviceHost1, String serviceHost2, String serviceHost3, String serviceHost4, String serviceHost5,
	//								  String serviceHost6)
	private static boolean isValidate(String authenticationUrl, String serviceUrl, int totalService,
									  String serviceHost1, String serviceHost2, String serviceHost3, String serviceHost4, String serviceHost5,
									  String serviceHost6)

	{
		/*
		if (StringHelper.isNullOrEmpty(autoUpdateListenPortString))
		{
			return false;
		}
		else
		{
			int autoUpdateListenPort = 0;
			try
			{
				autoUpdateListenPort = XmlConvert.toInt32(autoUpdateListenPortString);
			}
			catch (Throwable exception)
			{
				return false;
			}
			if (autoUpdateListenPort < 1025 || autoUpdateListenPort > 65534)
			{
				return false;
			}
		}
		*/

		boolean isValidateService = false;
		if (totalService >= 6)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost6);
		}
		if (totalService >= 5)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost5);
		}
		if (totalService >= 4)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost4);
		}
		if (totalService >= 3)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost3);
		}
		if (totalService >= 2)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost2);
		}
		if (totalService >= 1)
		{
			isValidateService = !StringHelper.isNullOrEmpty(serviceHost1);
		}

		return isValidateService
			//&& AppToolkit.isUrl(autoUpdateUrl)
			&& AppToolkit.isUrl(authenticationUrl)
			&& AppToolkit.isUrl(serviceUrl);
	}

	private static boolean isSameItem(XmlElement sourceXmlElement, XmlElement destinationXmlElement)
	{
		XmlNode sourceXmlNode = sourceXmlElement.get_Item("appSettings");
		XmlNode destinationXmlNode = destinationXmlElement.get_Item("appSettings");
		if(destinationXmlNode.get_Item("configurationversion") == null
			   ||!sourceXmlNode.get_Item("configurationversion").get_InnerText().equalsIgnoreCase(destinationXmlNode.get_Item("configurationversion").get_InnerText()))
		{
			return false;
		}

		for (int i = 0; i < sourceXmlNode.get_ChildNodes().get_Count(); i++)
		{
			XmlNode sourceXmlNode2 = sourceXmlNode.get_ChildNodes().itemOf(i);
			String sourceLocalName = sourceXmlNode2.get_LocalName();

			boolean isFound = false;

			for (int j = 0; j < destinationXmlNode.get_ChildNodes().get_Count(); j++)
			{
				XmlNode destinationXmlNode2 = destinationXmlNode.get_ChildNodes().itemOf(j);
				if(destinationXmlNode2 == null) return false;

				String destinationLocalName = destinationXmlNode2.get_LocalName();
				if (sourceLocalName.equals(destinationLocalName))
				{
					if (sourceLocalName.equalsIgnoreCase("serviceHost1")
						|| sourceLocalName.equalsIgnoreCase("useBlackAsBackground")
						|| sourceLocalName.equalsIgnoreCase("useGreenAsRiseColor")
						|| sourceLocalName.equalsIgnoreCase("serviceHost2")
						|| sourceLocalName.equalsIgnoreCase("serviceHost3")
						|| sourceLocalName.equalsIgnoreCase("serviceHost4")
						|| sourceLocalName.equalsIgnoreCase("serviceHost5")
						|| sourceLocalName.equalsIgnoreCase("serviceHost6")
						|| sourceLocalName.equalsIgnoreCase("totalService")
						|| sourceLocalName.equalsIgnoreCase("mapPort")
						|| sourceLocalName.equalsIgnoreCase("mapPort1")
						|| sourceLocalName.equalsIgnoreCase("mapPort2")
						|| sourceLocalName.equalsIgnoreCase("mapPort3")
						|| sourceLocalName.equalsIgnoreCase("mapPort4")
						|| sourceLocalName.equalsIgnoreCase("mapPort5")
						|| sourceLocalName.equalsIgnoreCase("mapPort6")
						|| sourceLocalName.equalsIgnoreCase("priceAlign")
						|| sourceLocalName.equalsIgnoreCase("placardUrl")
						|| sourceLocalName.equalsIgnoreCase("useCell")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink1")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink2")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink3")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink4")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink5")
						|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink6")
						|| sourceLocalName.equalsIgnoreCase("recoverPasswordAnswerCount")
						|| sourceLocalName.equalsIgnoreCase("defaultMainFormColor")
						|| sourceLocalName.equalsIgnoreCase("backupServerNumber")
						|| sourceLocalName.equalsIgnoreCase("serviceUrlFor99Bill")
						|| sourceLocalName.equalsIgnoreCase("companyCodeForTransfer")
						|| sourceLocalName.equalsIgnoreCase("showMarginAsChuRuJin")
						|| sourceLocalName.equalsIgnoreCase("waitTimeout"))
					{
						if (!sourceXmlNode2.get_InnerText().equalsIgnoreCase(destinationXmlNode2.get_InnerText()))
						{
							return false;
						}
					}
					else if(sourceLocalName.equalsIgnoreCase("authenticationUrl")
							|| sourceLocalName.equalsIgnoreCase("serviceUrl")
							|| sourceLocalName.equalsIgnoreCase("forgetPasswordLink"))
					{
						try
						{
							if(sourceXmlNode2.get_InnerText().compareToIgnoreCase(destinationXmlNode2.get_InnerText()) != 0)
							{
								URL url = new URL(sourceXmlNode2.get_InnerText());
								URL url2 = new URL(destinationXmlNode2.get_InnerText());
								if (url.getPath().compareToIgnoreCase(url2.getPath()) != 0)
								{
									return false;
								}
							}
						}
						catch(Exception exception)
						{
							return false;
						}
					}
					else if(sourceLocalName.equalsIgnoreCase("backupAuthenticationUrl")
							|| sourceLocalName.equalsIgnoreCase("backupServiceUrl")
							|| sourceLocalName.equalsIgnoreCase("backupAuthenticationUrl2")
							|| sourceLocalName.equalsIgnoreCase("backupServiceUrl2")
							|| sourceLocalName.equalsIgnoreCase("backupAuthenticationUrl3")
							|| sourceLocalName.equalsIgnoreCase("backupServiceUrl3"))
					{
						String source = sourceXmlNode2.get_InnerText();
						String destination = destinationXmlNode2.get_InnerText();
						if(source.compareToIgnoreCase(destination) != 0) return false;
					}

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

	public static boolean isUseCell()
	{
		ServiceManager serviceManager = ServiceManager.Create();
		return serviceManager.get_UseCell();
	}

	public String get_ForgetPasswordLink()
	{
		return this.getForgetPasswordLink(this.selectedServiceHostSequence);
	}
	public String get_BackupHostName(int index)
	{
		String backupAuthenticationUrl = this.backupAuthenticationUrl;
		if(index == 1)
		{
			backupAuthenticationUrl = this.backupAuthenticationUrl2;
		}
		else if(index == 2)
		{
			backupAuthenticationUrl = this.backupAuthenticationUrl3;
		}

		if(StringHelper.isNullOrEmpty(backupAuthenticationUrl))
		{
			return "";
		}
		else
		{
			return AppToolkit.getHost(backupAuthenticationUrl);
		}
	}
}
