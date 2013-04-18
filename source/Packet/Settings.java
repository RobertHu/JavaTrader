package Packet;

import Util.DocumentHelper;
import nu.xom.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Settings
{
	private static final String log4jPath = "log4j.properties";
	private static Logger logger= Logger.getLogger(Settings.class);
	private static int  waitTimeout=20000;
	private static String hostName;
	private static int port;
	public static final String ConfigFileName = "config.xml";

	public static String getHostName(){
		return hostName;
	}
	public static int getPort(){
		return port;
	}

	public static void setHostName(String hostName){
		Settings.hostName = hostName;
	}
	public static void setPort(int port){
		Settings.port = port;
	}

	public static int getWaitTimeout(){
		return waitTimeout;
	}

	public static void setWaitTimeout(int value){
		if(value <=0){
			return;
		}
		waitTimeout = value;
	}

	public static void initialize(){
		PropertyConfigurator.configure(log4jPath);
	}
}
