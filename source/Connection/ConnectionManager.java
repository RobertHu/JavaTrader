package Connection;

import javax.net.ssl.SSLSocket;
import org.apache.log4j.Logger;
import java.io.OutputStream;
import java.io.IOException;
import Packet.ComunicationObject;
import Packet.PacketBuilder;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.io.InputStream;
import Packet.AsyncManager;
import Packet.AliveKeeper;
import Packet.RecoverService;
import tradingConsole.TradingConsole;
import Packet.MsgParser;
import tradingConsole.enumDefine.LoginStatus;
import Util.RequestCommandHelper;
import java.util.concurrent.TimeUnit;
import Packet.Settings;

public class ConnectionManager
{
	private Logger logger= Logger.getLogger(ConnectionManager.class);
	private AsyncManager asyncManager;
	private MsgParser msgParser;
	private AliveKeeper aliveKeeper;
	private SSLConnection  sslConnection ;
	private TradingConsole tradingConsole;
	private final long TCP_INITIALIZE_TIME = 1* 60 * 1000;
	private final long INITIALIZE_SLEEP_PERIAD = 10*1000;

	public  ConnectionManager(TradingConsole tradingConsole)
	{
		this.tradingConsole= tradingConsole;
	}

	public AsyncManager getAsyncManager(){
		return this.asyncManager;
	}

	public void startTcpConnect()
	{
		this.msgParser.start();
		this.asyncManager.start();
		this.aliveKeeper.start();
		this.aliveKeeper.addObserver(this.tradingConsole);
	}


	public void initializeTcpSettings() throws Connection.ConnectionManager.TcpInializeTimeoutException
	{
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime <= TCP_INITIALIZE_TIME){
			if(initializeTcpSettingsHelper()){
				break;
			}
			else{
				try{
					TimeUnit.MILLISECONDS.sleep(INITIALIZE_SLEEP_PERIAD);
				}
				catch(Exception ex){}
			}
		}
		if(System.currentTimeMillis() - startTime > TCP_INITIALIZE_TIME){
			throw new TcpInializeTimeoutException();
		}

	}

	public  void closeTcpConnect(){
		try{
			if (this.msgParser != null)
			{
				this.msgParser.stop();
			}
			if (this.asyncManager != null)
			{
				this.asyncManager.stop();
			}
			if (this.aliveKeeper != null)
			{
				this.aliveKeeper.stop();
			}
			this.logger.info("begin close tcp connection");
			this.sslConnection.getSocket().close();
			this.logger.debug("Close tcp connection");
		}
		catch(Exception e){
			this.logger.error(e.getStackTrace());
		}
	}


	private boolean initializeTcpSettingsHelper()
	{
		try
		{
			this.tradingConsole.get_LoginInformation().set_LoginStatus(LoginStatus.Connecting);
			this.tradingConsole.setConnectStatus();
			this.sslConnection = new SSLConnection(Settings.getHostName(), Settings.getPort());
			RequestCommandHelper.setOutputStream(this.sslConnection.getSocket().getOutputStream());
			this.asyncManager = new AsyncManager();
			this.msgParser = new MsgParser(this.sslConnection.getSocket().getInputStream(), this.asyncManager);
			this.aliveKeeper = new AliveKeeper(this.tradingConsole.get_LoginInformation());
			return true;
		}
		catch (Exception ex)
		{
			this.logger.error("initialize tcp error",ex);
			return false;
		}

	}
	public class TcpInializeTimeoutException extends Exception{}

}
