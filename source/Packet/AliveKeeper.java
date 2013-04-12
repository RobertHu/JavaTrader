package Packet;

import java.util.List;
import java.util.ArrayList;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import nu.xom.Element;
import Util.RequestCommandHelper;
import Util.CommandHelper;
import org.apache.log4j.Logger;
import tradingConsole.enumDefine.LoginStatus;
import tradingConsole.LoginInformation;
public class AliveKeeper implements Runnable {
	private Logger logger= Logger.getLogger(AliveKeeper.class);
	private List<ConnectionObserver> observers= new ArrayList<ConnectionObserver>();
	private final int SLEEP_TIME = 30000;
	private final int MAX_EXCEPTION_COUNT = 3;
	private int sleepTime = SLEEP_TIME;
	private int exceptionCount = 0;
	private final String expectedResult = "1";
	private volatile boolean isStop = false;
	private volatile boolean isStart = false;
	private LoginInformation loginInfo;
	public AliveKeeper(LoginInformation loginInfo){
		this.loginInfo = loginInfo;
	}

	public ComunicationObject buildRequest(){
	     return CommandHelper.buildKeepAliveCommand();
	}

	public void stop(){
		this.isStop=true;
	}

	public void start(){
		try {
			if(this.isStart){
				return;
			}
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
			this.isStart=true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void run(){
		while(true){
			try {
				if(this.isStop){
					break;
				}
				TimeUnit.MILLISECONDS.sleep(this.sleepTime);
				SignalObject signalObject = RequestCommandHelper.request(this.buildRequest());
				if(this.isSessionLost(signalObject)){
					this.logger.info("session lost");
					this.fireConnectionBroken();
					break;
				}
				else{
					sleepTime = SLEEP_TIME;
					this.exceptionCount = 0;
				}

			} catch (Exception e) {
				logger.error("has exception");
				this.exceptionCount++;
				if(this.exceptionCount >= MAX_EXCEPTION_COUNT){
					this.fireConnectionBroken();
					break;
				}
				sleepTime = SLEEP_TIME / (2 * this.exceptionCount);
			}
		}
	}


	private boolean isSessionLost(SignalObject signalObject)
	{
		boolean isLost=false;
		if (signalObject.getIsError())
		{
			if (this.loginInfo.get_LoginStatus().equals(LoginStatus.LoginSucceed)
				|| this.loginInfo.get_LoginStatus().equals(LoginStatus.Ready))
			{
				isLost= true;
			}
		}
		else{
			String content = RequestCommandHelper.getStringFromResponse(signalObject.getResult());
			if (!content.equals(expectedResult)){
				isLost = true;
			}
		}
		return isLost;
	}



	private synchronized  void fireConnectionBroken(){
		try{
			if(this.isStop){
				return;
			}
			for (ConnectionObserver observer : this.observers)
			{
				observer.connectionBroken();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public synchronized void addObserver(ConnectionObserver observer) {
		this.observers.add(observer);
	}

	public synchronized void removeObserver(ConnectionObserver observer){
		this.observers.remove(observer);
	}



}

