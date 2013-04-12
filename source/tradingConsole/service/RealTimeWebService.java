package tradingConsole.service;

import framework.net.CookieContainer;
import framework.xml.XmlDocument;
import framework.IAsyncCallback;
import framework.xml.XmlNode;
import framework.IAsyncResult;

import framework.web.services.protocols.HttpGetClientProtocol;
import framework.TimeSpan;
import java.util.Date;
import tradingConsole.settings.PublicParametersManager;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;
import framework.StringHelper;

public class RealTimeWebService extends HttpGetClientProtocol
{
	private static String nameSpace = "http://www.omnicare.com/TradingConsole/";

	public RealTimeWebService(String url, CookieContainer cookieContainer)
	{
		super(url);
		this.set_CookieContainer(cookieContainer);
	}

	public RealTimeWebService(String url, CookieContainer cookieContainer, XmlDocument xmlDocument)
	{
		super(url, xmlDocument);
		this.set_CookieContainer(cookieContainer);
	}

	private static TimeSpan getCommandTimeout = TimeSpan.fromSeconds(20);
	/// <remarks/>
	public XmlNode getCommands()
	{
		//Object[] results = this.invoke("GetCommands", new Object[0]);
		Object[] results = this.invoke("GetCommands", getCommandTimeout/*ServiceTimeoutSetting.getCommands*/, new Object[0]);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetCommands(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetCommands", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetCommands(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public XmlNode getCommands2(int firstSequence, int lastSequence)
	{
		Object[] results = this.invoke("GetCommands2", getCommandTimeout/*ServiceTimeoutSetting.getCommands2*/, new Object[]
									   {
									   firstSequence,
									   lastSequence});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetCommands2(int firstSequence, int lastSequence, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetCommands2", new Object[]
								{
								firstSequence,
								lastSequence}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetCommands2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	@Override
	protected Object[] invoke(String messageName, TimeSpan timeout, Object[] parameters)
	{
		Date begin = new Date();
		TradingConsole.performaceTraceSource.trace(TraceType.Information, "Begin call " + messageName);
		Object[] result = super.invoke(messageName, timeout, parameters);
		long takeTime = new Date().getTime() - begin.getTime();
		if(takeTime > PublicParametersManager.minTimeTaken)
		{
			String info =  "End call " + messageName + "\tConsume time = " +  takeTime;
			if(parameters.length > 0)
			{
				info = info + "; Paramters: ";
				String format = "";
				for (int index = 0; index < parameters.length; index++)
				{
					if (format.length() > 0)
						format = format + ";\t";
					format = format + "{" + index + "}";
				}
				info = info + StringHelper.format(format, parameters);
			}
			TradingConsole.performaceTraceSource.trace(TraceType.Information, info);
		}
		return result;
	}
}
