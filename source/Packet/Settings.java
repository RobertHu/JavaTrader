package Packet;

import Util.DocumentHelper;
import nu.xom.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Settings
{
	private static final String configPath = "Configuration/config.xml";
	private static final String log4jPath = "Configuration/log4j.properties";
	private static Logger logger= Logger.getLogger(Settings.class);
	private static IpAndPort ipAndPort;
	private static int waitTimeout;
	static{
		try
		{
			Document doc = DocumentHelper.load(configPath);
			Element root = doc.getRootElement();
			Element server = root.getFirstChildElement("server");
			Element ipE = server.getFirstChildElement("ip");
			Element portE = server.getFirstChildElement("port");
			String ip= ipE.getValue();
			int port = Integer.parseInt(portE.getValue());
			ipAndPort = new IpAndPort(ip,port);
			Element waitTimeoutE = root.getFirstChildElement("waitTimeout");
			waitTimeout = Integer.parseInt(waitTimeoutE.getValue());
		}
		catch (Exception e)
		{
			logger.error(e.getStackTrace());
		}

	}

	public static void initialize(){
		PropertyConfigurator.configure(log4jPath);
	}


	public static int getWaitTimeout(){
		return waitTimeout;
	}


	public static IpAndPort getIpAndPort()
	{
		return ipAndPort;
	}

}
