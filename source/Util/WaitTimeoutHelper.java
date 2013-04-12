package Util;

import Packet.WaitTimeoutException;
import Packet.Settings;
import org.apache.log4j.Logger;
public class WaitTimeoutHelper
{
	private static Logger logger = Logger.getLogger(WaitTimeoutHelper.class);
	public static void wait(Object signal) throws WaitTimeoutException,
			InterruptedException {
		synchronized (signal) {
			long endTimeMillis = System.currentTimeMillis()
					+ Settings.getWaitTimeout() ;
			signal.wait(Settings.getWaitTimeout());
			if (System.currentTimeMillis() >= endTimeMillis) {
				logger.info("wait time out");
				throw new WaitTimeoutException();
			}
		}
	}

}
