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
import java.io.IOException;

public class AliveKeeper implements Runnable {
	private Logger logger= Logger.getLogger(AliveKeeper.class);
	private List<ConnectionObserver> observers= new ArrayList<ConnectionObserver>();
	private final int SLEEP_TIME = 20000;
	private final int MAX_EXCEPTION_COUNT = 3;
	private int sleepTime = SLEEP_TIME;
	private int exceptionCount = 0;
	private volatile boolean isStop = false;
	private volatile boolean isStart = false;
	private LoginInformation loginInfo;
	public AliveKeeper(LoginInformation loginInfo){
		this.loginInfo = loginInfo;
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
			logger.error("start error",e);
		}
	}


	public void run()
	{
		while (true)
		{
			try
			{
				if (this.isStop)
				{
					break;
				}
				pollIsSessionExist();
			}
			catch (Exception e)
			{
				logger.error("keep alive error",e);
				if (IsExceedMaxExceptionCount())
				{
					this.fireConnectionBroken();
					break;
				}
			}
		}
		this.logger.info("closed");
	}

	private void pollIsSessionExist() throws InterruptedException, WaitTimeoutException, IOException
	{
		TimeUnit.MILLISECONDS.sleep(this.sleepTime);
		if (!isLogined())
		{
			System.out.println("not equals login succeed or ready");
			return;
		}
		SignalObject signalObject = RequestCommandHelper.request(this.buildRequest());
		if (this.sessionNotExist(signalObject))
		{
			this.logger.info("session lost");
			this.fireConnectionBroken();
			return;
		}
		sleepTime = SLEEP_TIME;
		this.exceptionCount = 0;
	}

	private ComunicationObject buildRequest(){
		 ComunicationObject target = CommandHelper.buildKeepAliveCommand();
		 target.setIsKeepAlive(true);
		 return target;
	}



	private boolean IsExceedMaxExceptionCount()
	{
		boolean result = false;
		this.exceptionCount++;
		if (this.exceptionCount >= MAX_EXCEPTION_COUNT)
		{
			result = true;
		}
		sleepTime = SLEEP_TIME / (2 * this.exceptionCount);
		return result;
	}


	private boolean sessionNotExist(SignalObject signalObject)
	{
		if (signalObject.getIsError())
		{
			return isLogined();
		}
		return !signalObject.isKeepAliveSucess();
	}

	private boolean isLogined()
	{
		LoginStatus status = loginInfo.get_LoginStatus();
		return status.equals(LoginStatus.LoginSucceed) ||
		       status.equals(LoginStatus.Ready);
	}



	private synchronized  void fireConnectionBroken(){
		try{
			if(this.isStop)	return;
			for (ConnectionObserver observer : this.observers)
			{
				observer.connectionBroken();
			}
		}
		catch(Exception e){
			logger.error("fire connection broken",e);
		}
	}

	public synchronized void addObserver(ConnectionObserver observer) {
		this.observers.add(observer);
	}

	public synchronized void removeObserver(ConnectionObserver observer){
		this.observers.remove(observer);
	}

}

