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
	private static boolean isIpOrPortChanged=false;
	public static final String ConfigFileName = "config.xml";
	public static final String LocalConfigFileName = "local_config.xml";

	public static boolean isDestinationChanged(){
		return isIpOrPortChanged;
	}

	public static void setIsDestinationChanged(boolean value){
		isIpOrPortChanged = value;
	}

	public static String getHostName(){
		return hostName;
	}
	public static int getPort(){
		return port;
	}

	public static void setHostName(String hostName){
		if(hostName!=null && !hostName.equals(Settings.hostName)){
			Settings.hostName = hostName;
			isIpOrPortChanged=true;
		}
	}
	public static void setPort(int port){
		if(port!=Settings.port){
			Settings.port = port;
			isIpOrPortChanged=true;
		}
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
