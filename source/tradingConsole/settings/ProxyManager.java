package tradingConsole.settings;

import java.io.File;

import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.StringHelper;

import tradingConsole.AppToolkit;
//import framework.xml.XmlElement;
import framework.diagnostics.TraceType;
import tradingConsole.TradingConsole;
import framework.io.DirectoryHelper;

public class ProxyManager
{
	private String _proxyHost = "";
	private String _proxyPort = "";
	private String _userId = ""; //encoded
	private String _password = ""; //encoded

	public String get_ProxyHost()
	{
		return this._proxyHost;
	}

	public void set_ProxyHost(String value)
	{
		this._proxyHost = value;
	}

	public String get_ProxyPort()
	{
		return this._proxyPort;
	}

	public void set_ProxyPort(String value)
	{
		this._proxyPort = value;
	}

	public String get_UserId()
	{
		return this._userId;
	}

	public void set_UserId(String value)
	{
		this._userId = value;
	}

	public String get_Password()
	{
		return this._password;
	}

	public void set_Password(String value)
	{
		this._password = value;
	}

	private ProxyManager()
	{}

	public static ProxyManager create()
	{
		ProxyManager proxy = new ProxyManager();
		proxy.setValue(false);

		return proxy;
	}

	public boolean getProxySet()
	{
		return !(StringHelper.isNullOrEmpty(this._proxyHost));
	}

	public void setProperty()
	{
		System.getProperties().put("proxySet", this.getProxySet());
		System.getProperties().put("http.proxyHost", this._proxyHost);
		System.getProperties().put("http.proxyPort", this._proxyPort);

		System.getProperties().put("https.proxyHost", this._proxyHost);
		System.getProperties().put("https.proxyPort", this._proxyPort);

		TradingConsole.traceSource.trace(TraceType.Information,"setProperty BEGIN");
		TradingConsole.traceSource.trace(TraceType.Information,this.getProxySet());
		TradingConsole.traceSource.trace(TraceType.Information,this._proxyHost);
		TradingConsole.traceSource.trace(TraceType.Information,this._proxyPort);
		TradingConsole.traceSource.trace(TraceType.Warning,"setProperty END");
	}

	public void saveProxy()
	{
		this.setValue(true);
	}

	private void setValue(boolean isNeedUpdate)
	{
		boolean isExistsFile = false;
		String companySettingDirectory = AppToolkit.get_CompanySettingDirectory();
		//String parameterFilePath = settingDirectory + "Proxy.xml";
		String parameterFilePath = DirectoryHelper.combine(companySettingDirectory,"Proxy.xml");
		File directory = new File(companySettingDirectory);
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
					if (!isNeedUpdate)
					{
						//XmlNode xmlNode = AppToolkit.getXml(parameterFilePath);
						XmlNode xmlNode = AppToolkit.getCompanySettingXml("Proxy.xml");
						XmlNode xmlNode2 = xmlNode.get_Item("Proxy");
						this._proxyHost = xmlNode2.get_Item("ProxyHost").get_InnerText();
						this._proxyPort = xmlNode2.get_Item("ProxyPort").get_InnerText();
						this._userId = xmlNode2.get_Item("UserId").get_InnerText();
						this._password = xmlNode2.get_Item("Password").get_InnerText();
					}
					isExistsFile = true;
				}
			}
		}
		if (!isExistsFile)
		{
			//sava data
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
			xml += "<Proxies>";
			xml += "<Proxy>";
			xml += "<ProxyHost></ProxyHost>";
			xml += "<ProxyPort></ProxyPort>";
			xml += "<UserId></UserId>";
			xml += "<Password></Password>";
			xml += "</Proxy>";
			xml += "</Proxies>";
			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.loadXml(xml);
			try
			{
				xmlDocument.save(parameterFilePath);
			}
			catch (Throwable exception)
			{
			}
		}
		if (isNeedUpdate)
		{
			//XmlNode xmlNode = AppToolkit.getXml(parameterFilePath);
			XmlNode xmlNode = AppToolkit.getCompanySettingXml("Proxy.xml");
			XmlNode xmlNode2 = xmlNode.get_Item("Proxy");
			xmlNode2.get_Item("ProxyHost").set_InnerText(this._proxyHost);
			xmlNode2.get_Item("ProxyPort").set_InnerText(this._proxyPort);
			xmlNode2.get_Item("UserId").set_InnerText(this._userId);
			xmlNode2.get_Item("Password").set_InnerText(this._password);

			try
			{
				xmlNode.get_OwnerDocument().save(parameterFilePath);
			}
			catch (java.lang.Throwable  exception2)
			{
			}
		}
	}

}
