package tradingConsole;

import tradingConsole.service.WebService;
import tradingConsole.framework.ResourceHelper;
import java.io.InputStream;
import framework.xml.XmlDocument;
import tradingConsole.settings.ServiceManager;
import framework.net.CookieContainer;
import framework.xml.XmlNode;

public class TradingProxy implements SlidingWindow.ICommandStream
{
	private WebService _webService;

	public TradingProxy(String url, ServiceManager serviceManager)
	{
		InputStream inputStream = ResourceHelper.getAsStream("Configuration", "ServiceWsdl.xml");
		XmlDocument xmlDocument = TradingConsole.getServiceWsdl(serviceManager, inputStream, url);
		this._webService = new WebService(url, new CookieContainer(), xmlDocument);
	}

	public WebService get_Service()
	{
		return this._webService;
	}

	public XmlNode read()
	{
		return this._webService.getCommands();
	}

	public XmlNode read(int begingSequence, int endSequence)
	{
		return this._webService.getCommands2(begingSequence,endSequence);
	}

	@Override
	public String toString()
	{
		return "Proxy with url: " + this._webService.get_Url();
	}
}
