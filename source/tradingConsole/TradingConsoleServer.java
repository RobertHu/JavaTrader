package tradingConsole;

import java.io.*;
import java.math.*;
import java.util.*;

import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.net.*;
import framework.threading.*;
import framework.threading.Scheduler.*;
import framework.time.*;
import framework.xml.*;
import tradingConsole.common.*;
import tradingConsole.enumDefine.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.language.*;

import Packet.*;
import nu.xom.Element;
import Util.XmlElementHelper;
import Connection.*;
import org.apache.log4j.Logger;
import Util.RequestCommandHelper;
import Util.XmlNodeHelper;
import Util.CommandHelper;
import Util.CommandConstants;
import Util.SignalHelper;
import tradingConsole.Transaction.PlaceCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tradingConsole.ui.BankAccountForm;
import tradingConsole.ui.BankAccountCallback;
import tradingConsole.settings.VerificationOrderManager.MultipleCloseCallback;

public class TradingConsoleServer implements ITimeSyncService, Scheduler.ISchedulerCallback, TimeAdjustedEventHandler, SlidingWindow.ICommandStream, SlidingWindow.ICommandProcessor
{
	public static SimpleAppTime appTime;
	public static Scheduler scheduler;

	//private static String _actionArgsForGetCommandsScheduler = "GetCommandsScheduler";
	private static String _actionArgsForCallChartServiceScheduler = "CallChartServiceScheduler";

	//used on debug
	private static String _customerIdString = "";

	//private SchedulerEntry _workScheduleEntry;
	private SchedulerEntry _callChartServiceScheduleEntry;

	private TradingConsole _tradingConsole;

	private DateTime _firstDisconnectTime;

	private SlidingWindow _slidingWindow;
	private int _lastEndSequence;

	private TradingProxy _tradingProxy = null;

	private Logger logger = Logger.getLogger(TradingConsoleServer.class);
	//SystemTime.ISynchronizeTime.SynchronizeTime
	public TimeInfo getTimeInfo() throws Exception
	{
		return this.getTimeInfo2();
	}

	public TimeSyncSettings getTimeSyncSettings() throws Exception
	{
		return this.getTimeSyncSettings2();
	}

	public TradingConsoleServer(TradingConsole tradingConsole)
	{
		this._tradingConsole = tradingConsole;
	}

	public boolean start()
	{
		try
		{
			TradingConsole.traceSource.trace(TraceType.Information, "new AppTime_Begin");
			TradingConsoleServer.appTime = new SimpleAppTime(this);
			TradingConsole.traceSource.trace(TraceType.Information, "new AppTime_End");
			TradingConsole.traceSource.trace(TraceType.Information, "add_TimeAdjusted_Begin");
			TradingConsoleServer.appTime.add_TimeAdjusted(this);
			TradingConsole.traceSource.trace(TraceType.Information, "add_TimeAdjusted_End");

			//first get appTime
			//TradingConsoleServer.appTime.set_SynchronizeFunction(this);
			this._tradingConsole.traceSource.trace(TraceType.Information, "TradingConsoleServer.appTime.start()_Begin");
			if(!TradingConsoleServer.appTime.start())
			{
				return false;
			}
			this._tradingConsole.traceSource.trace(TraceType.Information, "TradingConsoleServer.appTime.start()_End");

			TradingConsoleServer.scheduler = new Scheduler(TradingConsoleServer.appTime);

			TimeSpan readInterval = TimeSpan.fromMilliseconds(Parameter.getCommandsInterval);

			this._slidingWindow = new SlidingWindow(this, this, TradingConsoleServer.scheduler, readInterval);
		}
		catch (Throwable throwable2)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable2.toString());
			return false;
		}
		return true;
	}

	public SlidingWindow get_SlidingWindow()
	{
		return this._slidingWindow;
	}

	public void finalize() throws Throwable
	{
		this.clearScene();

		super.finalize();
	}

	public void clearScene()
	{
		if (this._slidingWindow != null && this._slidingWindow.isStarted())
		{
			this.logger.debug("clear scene");
			this._slidingWindow.stop();
		}

		if (TradingConsoleServer.scheduler != null)
		{
			try
			{
				TradingConsoleServer.scheduler.dispose();
			}
			catch (Throwable exception)
			{
				this._tradingConsole.traceSource.trace(TraceType.Error, exception);
			}
			TradingConsoleServer.scheduler = null;
			this._tradingConsole.traceSource.trace(TraceType.Information, "TradingConsoleServer.scheduler.dispose()");
		}

		if (TradingConsoleServer.appTime != null)
		{
			TradingConsoleServer.appTime.stop();
			TradingConsoleServer.appTime = null;
			this._tradingConsole.traceSource.trace(TraceType.Information, "TradingConsoleServer.appTime.stop()");
		}
		this.logout();
	}

	public static DateTime appTime()
	{
		try
		{
			return TradingConsoleServer.appTime.get_UtcNow();
		}
		catch(Exception exception)
		{
			return DateTime.get_Now();
		}
	}

	public void schedulerStart()
	{
		try
		{
			this.stopSlidingWindow();
			this._slidingWindow.start(this._lastEndSequence);
			this.logger.debug("sliding window start");
			TradingConsole.traceSource.trace(TraceType.Information, "GetCommandsScheduler.scheduler.add_Begin");
			DateTime beginTime = TradingConsoleServer.appTime();
			TradingConsole.traceSource.trace(TraceType.Information, "GetCommandsScheduler.scheduler.add_End");
		}
		catch(Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Information, exception);
			throw new framework.FrameworkException(exception);
		}
	}

	public XmlNode read()
	{
		this.logger.debug("read command really");
		return this.getCommands();
	}

	public XmlNode read(int begingSequence, int endSequence)
	{
		return this.getCommands2(begingSequence,endSequence);
	}

	public void process(XmlNode commands)
	{
		String commandNodeString = ( (commands == null) ? "NULL" : commands.get_OuterXml());
		TradingConsole.getCommandTraceSource.trace(TraceType.Information, "CustomerId is " + this.get_CustomerIdString() + ": " + commandNodeString);

		if (commands == null)
		{
			return;
		}
		this._tradingConsole.processCommands(commands);
	}

	public void process(Throwable throwable)
	{
		this.throwableProcess("getCommands", throwable);
	}

	public void communicationBroken()
	{
		this._tradingConsole.networkDisconnected();
	}

	public synchronized void schedulerStop()
	{

	   if (this._slidingWindow != null && this._slidingWindow.isStarted())
	   {
		   this.logger.debug("scheduler stop");
		   this._slidingWindow.stop();
	   }

	   if (this._callChartServiceScheduleEntry != null)
	   {
		   this.scheduler.remove(this._callChartServiceScheduleEntry);
		   this._callChartServiceScheduleEntry = null;
	   }
	}

	//Scheduler.ISchedulerAction.Action
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		if (schedulerEntry.get_IsRemoved())
		{
			return;
		}

	    if (schedulerEntry.get_ActionArgs().equals(TradingConsoleServer._actionArgsForCallChartServiceScheduler))
		{
			this.callService();
		}
	}

	public void stopSlidingWindow()
	{
		if (this._slidingWindow != null && this._slidingWindow.isStarted())
		{
			this.logger.debug("stop sliding window");
			this._slidingWindow.stop();
		}
	}

	//used on debug
	private String get_CustomerIdString()
	{
		if (StringHelper.isNullOrEmpty(TradingConsoleServer._customerIdString))
		{
			if (this._tradingConsole.get_LoginInformation().get_CustomerId() == null)
			{
				TradingConsoleServer._customerIdString = "";
			}
			else
			{
				TradingConsoleServer._customerIdString = this._tradingConsole.get_LoginInformation().get_CustomerId().toString();
			}
		}
		return TradingConsoleServer._customerIdString;
	}

	private void throwableProcess(String debugTitle, Throwable throwable)
	{
		if (throwable instanceof Exception)
		{
			this.exceptionProcess(debugTitle, (Exception)throwable);
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Error, debugTitle + ": \n" + throwable.toString());
		}
	}

	private void exceptionProcess(String debugTitle, Throwable exception)
	{
		SwingUtilities.invokeLater(new ExceptionProcessor(this, debugTitle, exception));
	}
	private static class ExceptionProcessor implements Runnable
	{
		private TradingConsoleServer _owner;
		private String _debugTitle;
		private Throwable _exception;

		public ExceptionProcessor(TradingConsoleServer owner, String debugTitle, Throwable exception)
		{
			this._owner = owner;
			this._debugTitle = debugTitle;
			this._exception = exception;
		}

		public void run()
		{
			if (this._exception instanceof FrameworkException)
			{
				Exception innerException = ( (FrameworkException)this._exception).get_InnerException();
				while (innerException != null && innerException instanceof FrameworkException)
				{
					innerException = ( (FrameworkException)innerException).get_InnerException();
				}
				if (innerException != null)
				{
					if (innerException instanceof WebException)
					{
						this._owner.webExceptionProcess(_debugTitle, (WebException)innerException);
					}
					else
					{
						this._owner.frameworkInnerExceptionProcess(_debugTitle, innerException);
					}
				}
				else
				{
					TradingConsole.traceSource.trace(TraceType.Error, _exception);
				}
			}
			else if (this._exception instanceof WebException)
			{
				this._owner.webExceptionProcess(_debugTitle, (WebException)this._exception);
			}
			else
			{
				TradingConsole.traceSource.trace(TraceType.Error, this._exception);
			}
		}
	}

	private void webExceptionProcess(String debugTitle, WebException webException)
	{
		switch (webException.get_Code())
		{
			case HttpStatusCode.Found:
				TradingConsole.traceSource.trace(TraceType.Information, "HttpStatusCode.Found: " + webException.toString());
				this.logger.debug("webException HttpStatusCode.Found  "+ debugTitle);
				this._tradingConsole.kickout();
				break;
			case HttpStatusCode.Unused:
				this.logger.debug("webException HttpStatusCode.Unused  " + debugTitle);
				TradingConsole.traceSource.trace(TraceType.Information, "HttpStatusCode.Unused: " + webException.toString());
				if (webException.get_Description().indexOf("<title>Login</title>") != -1)
				{
					TradingConsole.traceSource.trace(TraceType.Warning, webException.get_Description());
					this._tradingConsole.kickout();
				}
				else
				{
					TradingConsole.traceSource.trace(TraceType.Error, debugTitle + " exceptionProcess.HttpStatusCode.Unused: " + webException.toString());
				}
				break;
			default:
				TradingConsole.traceSource.trace(TraceType.Error, debugTitle + " exceptionProcess.default: " + webException.toString());
		}
	}

	private void frameworkInnerExceptionProcess(String debugTitle, Exception innerException)
	{
		TradingConsole.traceSource.trace(TraceType.Error, debugTitle + "- " + innerException.toString());

		if (innerException instanceof java.net.ConnectException
			|| innerException instanceof java.io.InterruptedIOException
			|| innerException instanceof java.net.SocketException)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "setDisconnectStatusDisplay");

			//this._tradingConsole.get_LoginInformation().set_LoginStatus(LoginStatus.Disconnected);
			//this._tradingConsole.setConnectStatus();
			this._tradingConsole.setDisconnectStatusDisplay();
			DateTime localTime = DateTime.fromDate(new Date());
			if (this._firstDisconnectTime == null)
			{
				this._firstDisconnectTime = localTime;
			}
			else
			{
				if (this._firstDisconnectTime.before(localTime.addMinutes( -2)))
				{
					this._firstDisconnectTime = localTime;
					try
					{
						this._tradingConsole.networkDisconnected();
					}
					catch(Exception exception)
					{
						TradingConsole.traceSource.trace(TraceType.Error, exception);
						this.messageNotify(Language.SystemDisconnected, false);
						Runtime.getRuntime().exit(0);
					}
				}
			}
		}
		else if (innerException instanceof javax.net.ssl.SSLHandshakeException)
		{
			if (innerException.toString().lastIndexOf("timestamp check failed") > 0)
			{
				this._tradingConsole.timestampCheckFailed();
			}
		}
	}

	private void messageNotify(String message, boolean isClear)
	{
		this._tradingConsole.messageNotify(message, isClear);
	}

	//CallService as following:
	public LoginResult loginForJava(String loginID, String password, String version,int appType)
	{
		LoginResult result = null;
		try
		{
			ComunicationObject loginCommand = CommandHelper.BuildLoginCommand(loginID,
				password, version,appType);
			SignalObject signal=RequestCommandHelper.request(loginCommand);
			if(signal.getIsError()){
				return result;
			}
			Element ele= signal.getResult();
			String session =ele.getFirstChildElement("session").getValue();
			LoginInfoManager.Default.setSession(session);
			result = new LoginResult(ele);

		}
		catch (Throwable throwable)
		{
			this.logger.error(throwable.getStackTrace());
			this.throwableProcess("loginForJava", throwable);
		}
		return result;
	}



	public TradingAccountLoginResult activeAccountLogin(Guid customerId, String loginID, String password)
	{
		throw new UnsupportedOperationException();

	}

	public Guid asyncGetTickByTickHistoryDatas(Guid instrumentId)
	{
		throw new UnsupportedOperationException();
	}

	//used in the chart
	public void setInstrumentCodes()
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getChartData(Guid instrumentId, String dataCycleParameter, DateTime from, DateTime to)
	{
		throw new UnsupportedOperationException();
	}

	//used in the chart
	public DataSet getChartData(Guid instrumentId, DateTime lastDate, int count, String dataCycleParameter)
	{
		throw new UnsupportedOperationException();
	}

	public Guid asyncGetChartData(Guid instrumentId, DateTime lastDate, int count, String dataCycleParameter)
	{
		throw new UnsupportedOperationException();
	}

	public Guid asyncGetChartData2(Guid instrumentId, String dataCycleParameter, DateTime from, DateTime to)
	{
		//dataCycleParameter = "MINUTE1"
		//dataCycleParameter = "MINUTE5"
		//dataCycleParameter = "HOUR1"
		//dataCycleParameter = "DAY1"
		//dataCycleParameter = "WEEK1"
		//dataCycleParameter = "MONTH1"
		Guid asyncResultId = Guid.empty;
		try
		{
			ComunicationObject command = CommandHelper.buildAsyncGetChartData2Command(instrumentId, from, to, dataCycleParameter);
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return asyncResultId;
			}
			asyncResultId = RequestCommandHelper.getGuidFromResponse(signal.getResult());
			this.logger.debug("asyncGetChartData2 get result  "+ asyncResultId.toString());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("asyncGetChartData2", throwable);
		}
		return asyncResultId;
	}



	public DataSet GetTickByTickHistoryData(Guid instrumentId, DateTime from, DateTime to)
	{
		DataSet result=null;
		try
		{
			ComunicationObject command = CommandHelper.buildAsyncGetTickByTickHistoryDatas2(instrumentId,from,to);
			SignalObject signal = RequestCommandHelper.request(command);
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("asyncGetTickByTickHistoryDatas2", throwable);
		}
		return result;

	}



	public Guid asyncGetTickByTickHistoryDatas2(Guid instrumentId, DateTime from, DateTime to)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getChartData(Guid asyncResultId)
	{
		DataSet dataSet = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetChartDataCommand(asyncResultId);
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return dataSet;
			}
			dataSet = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getChartData", throwable);
		}
		return dataSet;
	}

	//used in the chart
	public void clearHistoryDataCache(Guid instrumentId)
	{
		throw new UnsupportedOperationException();
	}

	//test
	public XmlNode logout()
	{
		try{
			ComunicationObject command = CommandHelper.buildLogoutCommand();
			SignalObject signal = RequestCommandHelper.request(command);
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("Logout", throwable);
		}
		return null;

	}

	public void saveIsCalculateFloat(boolean isCalculateFloat)
	{
		throw new UnsupportedOperationException();
	}

	public XmlNode getAccounts(Guid[] accountIDs, boolean includeTransactions)
	{
		throw new UnsupportedOperationException();
	}

	public XmlNode getAccountsForCut(Guid[] accountIDs, boolean includeTransactions)
	{
		throw new UnsupportedOperationException();
	}

	public AccountForCutResult getAccountForCut(DateTime lastAlertTime, Guid accountId, boolean includeTransactions)
	{
		throw new UnsupportedOperationException();
	}

	public GetInitDataResult getInitData( /*out*/int commandSequence)
	{
		GetInitDataResult result = null;
		try
		{
			ComunicationObject command = CommandHelper.BuildGetInitDataCommand();
			SignalObject signal=RequestCommandHelper.request(command);
			if(signal.getIsError()){
				this.logger.info("get init data error");
				return result;
			}
			Element content = signal.getResult();
			result = new GetInitDataResult(content);
			this._lastEndSequence = result.get_CommandSequence();
			if(this._slidingWindow != null) this._slidingWindow.reset(this._lastEndSequence);
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getInitData", throwable);
		}
		return result;
	}





	public void refreshInstrumentsState(Guid customerID)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getDealingPolicyDetails()
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getQuotePolicyDetailsAndRefreshInstrumentsState(Guid customerID)
	{
		throw new UnsupportedOperationException();
	}

	public void emailExecuteOrders(Object[] emailExecuteOrders)
	{
		throw new UnsupportedOperationException();
	}

	public XmlNode getCommands()
	{
		throw new UnsupportedOperationException();
	}

	public XmlNode getCommands2(int firstSequence, int lastSequence)
	{
		XmlNode result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetLostCommands(firstSequence, lastSequence);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getNodeFromResponse(signal.getResult());
		}
		catch (Exception ex)
		{
			this.logger.error(ex);
		}
		return result;
	}

	private void LogResultOfGetCommands(XmlNode command, boolean resultOfGetCommands)
	{
		throw new UnsupportedOperationException();
	}

	public void cancelQuote(Guid instrumentId, BigDecimal buyQuoteLot, BigDecimal sellQuoteLot)
	{
		throw new UnsupportedOperationException();
	}

	public void quote2(Guid instrumentId, BigDecimal buyQuoteLot, BigDecimal sellQuoteLot, int tick)
	{
		try{
			ComunicationObject command = CommandHelper.buildQuote2Command(instrumentId,buyQuoteLot,sellQuoteLot,tick);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				this.logger.error("quote2 error");
			}
		}
		catch(Exception e){
			this.logger.error(e.getStackTrace());
		}
	}

	public void quote(Guid instrumentId, BigDecimal quoteLot, int BSStatus)
	{
		try{
			ComunicationObject command = CommandHelper.buildQuoteCommand(instrumentId,quoteLot,BSStatus);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				this.logger.error("quote error");
			}
		}
		catch(Exception e){
			this.logger.error(e.getStackTrace());
		}
	}

	public IAsyncResult beginCancelLMTOrder(Guid transactionId, IAsyncCallback callback, Object asyncState)
	{
		throw new UnsupportedOperationException();
	}

	public TransactionError endCancelLMTOrder(IAsyncResult asyncResult)
	{
		throw new UnsupportedOperationException();
	}

	public boolean updateAccountLock(Guid agentAccountId, String[][] arrayAccountLock)
	{
		throw new UnsupportedOperationException();
	}

	private TimeInfo getTimeInfo2()
	{
		TimeInfo timeInfo = null;
		try
		{
			ComunicationObject command = BuildGetTimeInfoCommand();
			SignalObject signal=RequestCommandHelper.request(command);
			Element result=signal.getResult();
			logger.debug(result.toXML());
			Element timeInfoElement=result.getFirstChildElement(StringConstants.SingleContentNodeName);
			timeInfo=new TimeInfo();
			timeInfo.readXml(XmlNodeHelper.Parse(timeInfoElement.getValue()));
			logger.debug(String.format("%s %s", timeInfo.get_AdjustedTime().toString(),timeInfo.get_DateTime().toString()));
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable.toString());
		}
		return timeInfo;
	}

	private ComunicationObject BuildGetTimeInfoCommand()
	{
		Element root = new Element(PacketContants.CommandRootName);
		return RequestCommandHelper.newCommandWithSession("GetTimeInfo",root);
	}


	private TimeSyncSettings getTimeSyncSettings2()
	{
		throw new UnsupportedOperationException();
	}

	public void beginMultipleClose(final Guid[] orderIds, final MultipleCloseCallback callback, Object asyncState)
	{
		try{
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(new Runnable(){
			public void run()
			{
				try{
					ComunicationObject command = CommandHelper.buildMultipleCloseCommand(orderIds);
					SignalObject signal = RequestCommandHelper.request(command);
					callback.asyncCallback(signal);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});

		}
		catch (Throwable throwable)
		{
			this.throwableProcess("beginMultipleClose", throwable);
		}

	}

	public MultipleCloseResult endMultipleClose(IAsyncResult asyncResult)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getAccountBankReferenceData(String countryId)
	{
		try
		{
			String language= PublicParametersManager.version;
			ComunicationObject command= CommandHelper.buildGetAccountBankReferenceData(countryId,language);
			SignalObject signal= RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return null;
			}
			DataSet result = RequestCommandHelper.getDataFromResponse(signal.getResult());
			return result;
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getAccountBankReferenceData", throwable);
		}
		return null;
	}

	public IAsyncResult beginGetAccountBankReferenceData(String countryId, String language, IAsyncCallback callback, Object state)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet endGetAccountBankReferenceData(IAsyncResult asyncResult)
	{
		throw new UnsupportedOperationException();

	}

	public DataSet getAccountBanksApproved(Guid accountId)
	{
		throw new UnsupportedOperationException();
	}

	public void beginGetAccountBanksApproved(final Guid accountId, final BankAccountCallback callback, Object state)
	{
		try{
			ExecutorService executorService = Executors.newCachedThreadPool();
			executorService.execute(new Runnable(){
			public void run()
			{
				try{
					ComunicationObject command = CommandHelper.buildGetAccountBanksApprovedCommand(accountId,PublicParametersManager.version);
					SignalObject signal = RequestCommandHelper.request(command);
					callback.asyncCallback(signal);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});

		}
		catch (Throwable throwable)
		{
			this.throwableProcess("beginGetAccountBanksApproved", throwable);
		}

	}

	public DataSet endGetAccountBanksApproved(IAsyncResult asyncResult)
	{
		throw new UnsupportedOperationException();
	}

	public boolean[] apply(Guid id, String accountBankApprovedId, String accountId, String countryId, String bankId, String bankName,
						   String accountBankNo, String accountBankType, //#00;银行卡|#01;存折
						   String accountOpener, String accountBankProp, Guid accountBankBCId, String accountBankBCName,
						   String idType, //#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
						   String idNo, String bankProvinceId, String bankCityId, String bankAddress, String swiftCode, int applicationType)
	{
		boolean[] result = new boolean[2];
		try{
			ComunicationObject command = CommandHelper.buildApplyCommand(id, accountBankApprovedId, accountId, countryId, bankId, bankName,
						   accountBankNo, accountBankType, //#00;银行卡|#01;存折
						   accountOpener, accountBankProp, accountBankBCId, accountBankBCName,
						   idType, //#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
						   idNo, bankProvinceId, bankCityId, bankAddress, swiftCode, applicationType);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			boolean isApproved  = RequestCommandHelper.getBoolFromResponse(signal.getResult());
			result[0]=true;
			result[1]=isApproved;
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("apply", throwable);
		}
		return result;

	}

	public void beginPlace(final XmlNode tran, final PlaceCallback callback, Object asyncState) throws IOException, InterruptedException
	{
		Vector<String> vector=new Vector<String>();
		XmlNodeList list =tran.get_ChildNodes();
		for(int i=0;i<list.get_Count();i++){
			System.out.println(list.item(i).get_OuterXml());
			XmlAttributeCollection  attrs = list.item(i).get_Attributes();
			System.out.println(attrs.get_ItemOf("IsOpen").get_Value());
			if(attrs.get_ItemOf("IsOpen").get_Value().equals("true")){
				vector.add(attrs.get_ItemOf("Lot").get_Value());
			}
		}

	    System.out.println("Vector Size: "+vector.size());
		XmlAttributeCollection tranAttrs= tran.get_Attributes();
		String accountID=tranAttrs.get_ItemOf("AccountID").get_Value();
		String instrumentID= tranAttrs.get_ItemOf("InstrumentID").get_Value();
		String typeStr=tranAttrs.get_ItemOf("Type").get_Value();
		System.out.println("Type: "+ typeStr);
		String fileName=String.format("%s.txt",accountID);
		Boolean condtion1= vector.size()==1;
		Boolean condtion2 = vector.size()==2 && typeStr.equals("2");
		if(condtion1 || condtion2){
			PrintWriter outputStream = null;
			BufferedReader inputStream = null;
			String content=String.format("%s:%s",instrumentID,vector.get(0));
			Vector<String> orgin= new Vector<String>();
			try{
				File accountFile = new File(fileName);

				if(accountFile.exists()){
					inputStream = new BufferedReader(new FileReader(fileName));
					String l;
					while((l=inputStream.readLine())!=null){
						orgin.add(l);
					}
					int index =-1;
					for(int i=0;i<orgin.size();i++){
						if(orgin.get(i).indexOf(instrumentID)!=-1){
							index =i;
							break;
						}
					}
					if(index!=-1 && index<orgin.size())orgin.remove(index);
				}
				orgin.add(content);
				if(inputStream!=null){
					inputStream.close();
					inputStream=null;
				}
				outputStream = new PrintWriter(new FileWriter(fileName));
				for(String s : orgin){
					outputStream.println(s);
				}

			}
			finally{
				if(outputStream!=null){
					outputStream.close();
				}
				if(inputStream!=null){
					inputStream.close();
				}
			}
		}
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new Runnable(){
			public void run()
			{
				try{
					ComunicationObject command = CommandHelper.buildPlaceCommand(tran);
					SignalObject signal = RequestCommandHelper.request(command);
					callback.asyncCallback(signal);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});

		//return this._service.beginPlace(tran, callback, asyncState);
	}

	public PlaceResult endPlace(IAsyncResult asyncResult, /*out*/ String tranCode)
	{
		throw new UnsupportedOperationException();
	}

	public PlaceResult place(XmlNode tran)
	{
		PlaceResult result = null;
		try{
			ComunicationObject command = CommandHelper.buildPlaceCommand(tran);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = new PlaceResult(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("place", throwable);
		}
		return result;
	}

	public IAsyncResult beginAssign(XmlNode xmlTransaction, IAsyncCallback callback, Object asyncState)
	{
		throw new UnsupportedOperationException();
	}

	public AssignResult endAssign(IAsyncResult asyncResult, XmlNode xmlTransaction, XmlNode xmlAccount, XmlNode xmlInstrument)
	{
		throw new UnsupportedOperationException();
	}

	public ChangeLeverageResult changeLeverage(Guid accountId, int leverage)
	{
		ChangeLeverageResult result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildChangeLeverageCommand(accountId, leverage);
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return result;
			}
			result = new ChangeLeverageResult(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("changeLeverage", throwable);
		}
		return result;
	}

	public DataSet queryOrder(Guid customerId, String accountId, String instrumentId, int lastDays)
	{
		DataSet result = null;
		try{
			ComunicationObject command = CommandHelper.buildQueryOrderCommand(customerId, accountId, instrumentId, lastDays);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("queryOrder", throwable);
		}
		return result;

	}

	public AssignResult assign( XmlNode xmlTransaction, XmlNode xmlAccount,  XmlNode xmlInstrument)
	{
		AssignResult result = null;
		try
		{
			this.logger.debug("assign");
			//Object[] results = (Object[])this._service.assign(xmlTransaction, xmlAccount, xmlInstrument);
			ComunicationObject command = CommandHelper.buildAssignCommand();
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return result;
			}
			result = new AssignResult(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("assign", throwable);
		}
		return result;
	}

	public DataSet getAccountsForSetting()
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetAccountsForSettingCommand();
			SignalObject signal = RequestCommandHelper.request(command);
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getAccountsForSetting", throwable);
		}
		return result;
	}

	public XmlNode getInstrumentForSetting()
	{
		XmlNode result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetInstrumentForSettingCommand();
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			Element content = signal.getResult();
			result = RequestCommandHelper.getNodeFromResponse(content);
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getInstrumentForSetting", throwable);
		}
		return result;
	}

	public DataSet getCurrencyRateByAccountID(String accountID)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getInstrumentsTest(String instrumentID)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getInstruments(Object[] instrumentIDs)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getMessages()
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetMessagesCommand();
			SignalObject signal= RequestCommandHelper.request(command);
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getMessages", throwable);
		}
		return result;
	}

	public void complain(String loginName, String complaint)
	{
		throw new UnsupportedOperationException();
	}

	public boolean deleteMessage(Guid id)
	{
		boolean result= false;
		try{
			ComunicationObject command = CommandHelper.buildDeleteMessageCommand(id);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("deleteMessage", throwable);
		}
		return result;

	}

	public GetCustomerInfoResult getCustomerInfo( /*out*/String customerName)
	{
		throw new UnsupportedOperationException();
	}

	public boolean updateAccountsSetting(Guid[] accountIds)
	{
		try
		{
			ComunicationObject command =  CommandHelper.buildUpdateAccountsSettingCommand(accountIds);
			SignalObject signal = RequestCommandHelper.request(command);
			return RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("updateAccountsSetting", throwable);
		}
		return false;
	}

	public DataSet updateInstrumentSetting(String[] instrumentIds)
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildUpdateInstrumentSetting(instrumentIds);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			Element content = signal.getResult();
			result = RequestCommandHelper.getDataFromResponse(content);
			this.logger.debug("update instrument settings");
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("updateInstrumentSetting", throwable);
		}
		return result;
	}

	public void updateAccount(Guid accountID, Guid groupID, boolean isDelete, boolean isDeleteGroup)
	{
		throw new UnsupportedOperationException();
	}

	public void updateQuotePolicyDetail(Guid instrumentID, Guid quotePolicyID)
	{
		throw new UnsupportedOperationException();
	}

	public UpdatePasswordResult updatePassword2(String loginId, String oldPassword, String newPassword, String[][] recoverPasswordDatas)
	{
		throw new UnsupportedOperationException();
	}

	public UpdatePasswordResult updatePassword(String loginId, String oldPassword, String newPassword)
	{
		UpdatePasswordResult result = null;
		String message = "";
		try
		{
			ComunicationObject command = CommandHelper.buildUpdatePasswordCommand(loginId,oldPassword,newPassword);
			SignalObject signal = RequestCommandHelper.request(command);
			result= new UpdatePasswordResult(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("updatePassword--Message is " + message, throwable);
		}
		return result;
	}

	public boolean recoverPasswordDatas(String[][] recoverPasswordDatas)
	{
		boolean result = false;
		try{
			ComunicationObject command = CommandHelper.buildRecoverPasswordDatasCommand(recoverPasswordDatas);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("recoverPasswordDatas", throwable);
		}
		return result;


	}

	public void statementForJava(int statementReportType, String tradeDay, String IDs, String reportxml)
	{
		throw new UnsupportedOperationException();
	}

	public Guid statementForJava2(int statementReportType, String dayBegin, String dayTo, String IDs, String reportxml)
	{
		Guid result = Guid.empty;
		try
		{
			ComunicationObject command = CommandHelper.buildStatementForJava2Command(statementReportType,dayBegin,dayTo,IDs,reportxml);
			SignalObject signal = RequestCommandHelper.request(command);
			if(!signal.getIsError()){
			    result = RequestCommandHelper.getGuidFromResponse(signal.getResult());
				this.logger.debug("statementForJava2");
			}
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("statementForJava2", throwable);
		}
		return result;
	}

	public Guid accountSummaryForJava2(String tradeDay, String IDs, String reportxml)
	{
		throw new UnsupportedOperationException();
	}

	public Guid ledgerForJava2(String dateFrom, String dateTo, String IDs, String reportxml)
	{
		Guid result = Guid.empty;
		try{
			ComunicationObject command = CommandHelper.buildLedgerForJava2(dateFrom, dateTo, IDs, reportxml);
			SignalObject signal = RequestCommandHelper.request(command);
			if(!signal.getIsError()){
				result = RequestCommandHelper.getGuidFromResponse(signal.getResult());
			}
		}
		catch(Throwable throwable){
			this.throwableProcess("ledgerForJava2", throwable);
		}
		return result;
	}

	public byte[] getReportContent(Guid asyncResultId)
	{
		try
		{
			ComunicationObject command = CommandHelper.buildGetReportContentCommand(asyncResultId);
			SignalObject signal = RequestCommandHelper.request(command);
			byte[] reportContent = RequestCommandHelper.getBytesFromResponse(signal.getResult());
			return reportContent;
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getReportContent", throwable);
		}
		return null;
	}

	public void ledgerForJava(String dateFrom, String dateTo, String IDs, String reportxml)
	{
		throw new UnsupportedOperationException();
	}

	public String[] get99BillBanks()
	{
		throw new UnsupportedOperationException();
	}

	public String[] getMerchantInfoFor99Bill(Guid[] organizationIds)
	{
		try
		{
			ComunicationObject command = CommandHelper.buildGetMerchantInfoFor99BillCommand(organizationIds);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return null;
			}
			return RequestCommandHelper.getStringArrayFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getMerchantInfoFor99Bill", throwable);
		}
		return null;
	}

	public long getNextPaySequence(String merchantAcctId)
	{
		throw new UnsupportedOperationException();
	}

	public String additionalClient(String email, String receive, String organizationName, String customerName,
									String reportDate, String accountCode, String correspondingAddress, String registratedEmailAddress,
									String tel, String mobile, String fax, String fillName1, String ICNo1, String fillName2, String ICNo2,
									String fillName3, String ICNo3)
	{
		String result = "Error";
		try
		{
			ComunicationObject command = CommandHelper.buildAdditionalClientCommand(email,receive,organizationName,customerName,reportDate,accountCode,correspondingAddress,registratedEmailAddress,
				tel,mobile,fax,fillName1,ICNo1,fillName2,ICNo2,fillName3,ICNo3);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getStringFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("additionalClient", throwable);
		}
		return result;
	}

	private static String fix(String value)
	{
		if (StringHelper.isNullOrEmpty(value)) return null;

		value = StringHelper.replace(value, "<", "&lt;");
		value = StringHelper.replace(value, ">", "&gt;");
		value = StringHelper.replace(value, "&", "&amp;");
		value = StringHelper.replace(value, "'", "&apos;");
		value = StringHelper.replace(value, "\"", "&quot;");

		return value;
	}

	public String agent(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
						 String previousAgentCode,
						 String previousAgentName, String newAgentCode, String newAgentName, String newAgentICNo, String dateReply)
	{
		String result = "Error";

		try
		{
			ComunicationObject command = CommandHelper.buildAgentCommand(fix(email), fix(receive), fix(organizationName), fix(customerName),
										 fix(reportDate), fix(accountCode), fix(previousAgentCode),
										 fix(previousAgentName), fix(newAgentCode), fix(newAgentName),
										 fix(newAgentICNo), fix(dateReply));
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getStringFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("agent", throwable);
		}
		return result;
	}

	public String callMarginExtension(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									   String currency,
									   String currencyValue, String dueDate)
	{
		String result = "Error";

		try
		{
			ComunicationObject command = CommandHelper.buildCallMarginExtensionCommand(fix(email), fix(receive), fix(organizationName),
				fix(customerName), fix(reportDate), fix(accountCode), fix(currency), fix(currencyValue), fix(dueDate));
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result =  RequestCommandHelper.getStringFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("callMarginExtension", throwable);
		}
		return result;
	}

	public boolean fundTransfer(String email, String receive, String organizationName, String customerName, String reportDate, String currency,
								String currencyValue,
								String accountCode, String bankAccount, String beneficiaryName, String replyDate)
	{
		boolean isSucceed = false;
		try
		{
			ComunicationObject command = CommandHelper.buildFundTransferCommand(fix(email), fix(receive), fix(organizationName),
				fix(customerName), fix(reportDate), fix(currency), fix(currencyValue),
				fix(accountCode), fix(bankAccount), fix(beneficiaryName), fix(replyDate));
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return false;
			}
			isSucceed = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("fundTransfer", throwable);
			isSucceed = false;
		}
		return isSucceed;
	}

	public String paymentInstruction(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									  String currency,
									  String currencyValue, String beneficiaryName, String bankAccount, String bankerName, String bankerAddress,
									  String swiftCode,
									  String remarks,
									  String thisisClient)
	{
		String result = "Error";
		try
		{

			ComunicationObject command = CommandHelper.buildPaymentInstructionCommand(fix(email), fix(receive), fix(organizationName),
				fix(customerName), fix(reportDate), fix(accountCode), fix(currency), fix(currencyValue),
				fix(beneficiaryName), fix(bankAccount), fix(bankerName), fix(bankerAddress),
				fix(swiftCode), fix(remarks), fix(thisisClient));
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getStringFromResponse(signal.getResult());

		}
		catch (Throwable throwable)
		{
			this.throwableProcess("paymentInstruction", throwable);
		}
		return result;
	}

	public String paymentInstructionInternal(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currencyCode, String amount, String beneficiaryAccount,
					String beneficiaryAccountOwner, String email2)
	{
		String result = "Error";
		try
		{
			ComunicationObject command =CommandHelper.buildPaymentInstructionInternalCommand(fix(email), fix(organizationName),
				fix(customerName), fix(reportDate),	fix(accountCode), fix(currencyCode), fix(amount),
				fix(beneficiaryAccount), fix(beneficiaryAccountOwner), fix(email2));
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return result;
			}
			result = RequestCommandHelper.getStringFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("paymentInstructionInternal", throwable);
		}
		return result;
	}

	public String paymentInstructionCash(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currency, String amount, String beneficiaryName, String beneficiaryAddress)
	{
		String result = "Error";
		try
		{
			ComunicationObject command = CommandHelper.buildPaymentInstructionCashCommand(fix(email), fix(organizationName), fix(customerName),
				fix(reportDate), fix(accountCode), fix(currency), fix(amount), fix(beneficiaryName),
				fix(beneficiaryAddress));
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return result;
			}
			result = RequestCommandHelper.getStringFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("paymentInstructionCash", throwable);
		}
		return result;

	}

	public void notifyCustomerExecuteOrderForJava(String[][] arrayNotifyCustomerExecuteOrder, String companyCode)
	{
		throw new UnsupportedOperationException();
	}

	public byte[] GetTracePropertiesForJava()
	{
		throw new UnsupportedOperationException();
	}

	public boolean updateSystemParameters(String parameters, String ObjectID)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getNewsList(String newsCategoryID, String language, DateTime date)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getNewsList2(String newsCategoryID, String language, DateTime date)
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.BuildGetNewsList2Command(newsCategoryID,language,date);
			SignalObject signal= RequestCommandHelper.request(command);
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getNewsList2", throwable);
		}
		return result;
	}





	public DataSet getNewsContents(Guid newsID)
	{
		throw new UnsupportedOperationException();
	}

	public BigDecimal refreshAgentAccountOrder(String orderID)
	{
		throw new UnsupportedOperationException();
	}

	public Guid[] verifyTransaction(Guid[] transactionIds)
	{
		Guid[] result = null;
		try
		{
			//result = this._service.verifyTransaction(transactionIds);
			ComunicationObject command = CommandHelper.buildVerifyTransactionCommand(transactionIds);
			SignalObject signal = RequestCommandHelper.request(command);
			if (signal.getIsError())
			{
				return result;
			}
			result = RequestCommandHelper.getGuidArrayFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("verifyTransaction", throwable);
		}
		return result;
	}

	public boolean verifyMarginPin(Guid accountId, String marginPin)
	{
		boolean result = false;
		try{
			ComunicationObject command = CommandHelper.buildVerifyMarginPinCommand(accountId,marginPin);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("verifyMarginPin", throwable);
		}
		return result;

	}

	public boolean changeMarginPin(Guid accountId, String oldMarginPin, String newMarginPin)
	{
		boolean result = false;
		try{
			ComunicationObject command = CommandHelper.buildChangeMarginPinCommand(accountId,oldMarginPin,newMarginPin);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result =result = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("changeMarginPin", throwable);
		}
		return result;

	}

	public XmlNode getOuterNews(String newsUrl)
	{
		throw new UnsupportedOperationException();
	}

	public void handleTimeAdjustedEvent(Object object, TimeAdjustedEventArgs timeAdjustedEventArgs)
	{
		this._tradingConsole.timeAdjusted();
	}

	//temp......
	//used in the chart
	public Guid authenticate(String loginId, String password)
	{
		throw new UnsupportedOperationException();
	}

	//temp......
	//used in the TickByTick chart
	public Guid authenticateForTickByTick(String loginId, String password)
	{
		throw new UnsupportedOperationException();
	}

	public DataSet getInterestRate(Guid[] orderIds)
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetInterestRate(orderIds);
			SignalObject signal = RequestCommandHelper.request(command);
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
			//result = (DataSet)this._service.getInterestRate(orderIds);
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getInterestRate", throwable);
		}
		return result;
	}

	public DataSet getInterestRate(Guid interestRateId)
	{
		DataSet result = null;
		try
		{
			ComunicationObject command = CommandHelper.buildGetInterestRate(interestRateId);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getDataFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("getInterestRate", throwable);
		}
		return result;
	}

	public void saveLog(String logCode,DateTime timestamp,String action, Guid transactionId)
	{
		try
		{
			ComunicationObject command = CommandHelper.buildSaveLogCommand(logCode,timestamp,action,transactionId);
			SignalObject signal = RequestCommandHelper.request(command);
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("saveLog", throwable);
		}
	}

	private void callService()
	{
		throw new UnsupportedOperationException();
	}

	public String[] getProxyUrls()
	{
		throw new UnsupportedOperationException();
	}

	public void switchToProxy(TradingProxy tradingProxy, TimeSpan intervalOfGetCommandsFromProxy) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public boolean modifyOrder(OrderModification orderModification)
	{
		throw new UnsupportedOperationException();
	}

	public boolean modifyTelephoneIdentificationCode(Guid accountId, String oldCode, String newCode)
	{
		boolean result = false;
		try{
			ComunicationObject command = CommandHelper.buildModifyTelephoneIdentificationCodeCommand(accountId,oldCode,newCode);
			SignalObject signal = RequestCommandHelper.request(command);
			if(signal.getIsError()){
				return result;
			}
			result = RequestCommandHelper.getBoolFromResponse(signal.getResult());
		}
		catch (Throwable throwable)
		{
			this.throwableProcess("saveLog", throwable);
		}
		return result;

	}

	public boolean isReady()
	{
		LoginStatus status= this._tradingConsole.get_LoginInformation().get_LoginStatus();
		return status.equals(LoginStatus.Ready);
	}
}
