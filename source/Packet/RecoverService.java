package Packet;

import Util.CommandHelper;
import Util.RequestCommandHelper;
import java.io.IOException;
import org.apache.log4j.Logger;
public class RecoverService
{
	private Logger logger = Logger.getLogger(RecoverService.class);
	private String expectedResult = "1";
	public ComunicationObject buildRequest(){
		return CommandHelper.buildRecoverCommand();
	}
	public boolean recover() throws WaitTimeoutException, InterruptedException, IOException
	{
		this.logger.info("send recover request");
		SignalObject signal = RequestCommandHelper.request(this.buildRequest());
		this.logger.info("get recover request");
		if(signal.getIsError()){
			return false;
		}
	    String result =	RequestCommandHelper.getStringFromResponse(signal.getResult());
		return result.equals(expectedResult);
	}
}
