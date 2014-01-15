package tradingConsole.service;

import java.math.*;
import java.util.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.net.*;
import framework.time.*;
import framework.web.services.protocols.*;
import framework.xml.*;
import framework.xml.serialization.*;
import tradingConsole.*;
import tradingConsole.common.*;
import tradingConsole.settings.*;

public class WebService extends SoapHttpClientProtocol
{
	private static String nameSpace = "http://www.omnicare.com/TradingConsole/";

	public static String setProperty(String key, String value)
	{
		String oldValue = null;
		Properties properties = System.getProperties();

		if (properties.containsKey(key))
		{
			oldValue = (String)properties.get(key);
			properties.remove(key);
		}
		properties.put(key, value);

		return oldValue;
	}

	public WebService(String url, CookieContainer cookieContainer)
	{
		super(url);
		this.WebService2(cookieContainer);
	}

	public WebService(String url, CookieContainer cookieContainer, XmlDocument xmlDocument)
	{
		super(url, xmlDocument);
		this.WebService2(cookieContainer);
	}

	private void WebService2(CookieContainer cookieContainer)
	{
		this.set_CookieContainer(cookieContainer);

		XmlTypeMapping typeMapping = new XmlTypeMapping();
		typeMapping.addMap(TransactionError.class, XmlType.getXmlType("TransactionError", WebService.nameSpace));
		typeMapping.addMap(DataSet.class, XmlType.getXmlType("DataSet", WebService.nameSpace));
		typeMapping.addMap(TimeInfo.class, XmlType.getXmlType("TimeInfo", "framework.time.TimeInfo"));
		typeMapping.addMap(TimeSyncSettings.class, XmlType.getXmlType("TimeSyncSettings", "framework.time.TimeSyncSettings"));
		super.service.set_XmlTypeMapping(typeMapping);
	}

	/// <remarks/>
	public Guid authenticateForTickByTick(String loginId, String password)
	{
		Object[] results = this.invoke("AuthenticateForTickByTick", ServiceTimeoutSetting.authenticateForTickByTick, new Object[]
									   {
									   loginId,
									   password});
		return ( (Guid) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginAuthenticateForTickByTick(String loginId, String password, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("AuthenticateForTickByTick", new Object[]
								{
								loginId,
								password}, callback, asyncState);
	}

	/// <remarks/>
	public Guid endAuthenticateForTickByTick(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Guid) (results[0]));
	}

	public DataSet getTickByTickHistoryDatas(Guid instrumentId, DateTime from, DateTime to)
	{
		Object[] results = this.invoke("GetTickByTickData2", ServiceTimeoutSetting.getTickByTickHistoryDatas,
									   new Object[]{instrumentId, from, to});
		/*Object[] results = this.invoke("GetTickByTickHistoryDatas", ServiceTimeoutSetting.getTickByTickHistoryDatas,
									   new Object[]{instrumentId});*/
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getTickByTickHistoryDatas(Guid instrumentId)
	{
		Object[] results = this.invoke("GetTickByTickHistoryDatas", ServiceTimeoutSetting.getTickByTickHistoryDatas, new Object[]
									   {instrumentId});
		return ( (DataSet) (results[0]));
	}

	public Guid asyncGetTickByTickHistoryDatas(Guid instrumentId)
	{
		Object[] results = this.invoke("AsyncGetTickByTickHistoryData", ServiceTimeoutSetting.getCommands, new Object[]
									   {instrumentId});
		return ( (Guid) (results[0]));
	}

	public Guid asyncGetTickByTickHistoryDatas2(Guid instrumentId, DateTime from, DateTime to)
	{
		Object[] results = this.invoke("AsyncGetTickByTickHistoryData2", ServiceTimeoutSetting.getCommands, new Object[]
									   {instrumentId, from, to});
		return ( (Guid) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetTickByTickHistoryDatas(Guid instrumentId, IAsyncCallback callback,
		Object asyncState)
	{
		return this.beginInvoke("GetTickByTickHistoryDatas", new Object[]
								{
								instrumentId}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetTickByTickHistoryDatas(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public Guid authenticate(String loginId, String password)
	{
		Object[] results = this.invoke("Authenticate", ServiceTimeoutSetting.authenticate, new Object[]
									   {
									   loginId,
									   password});
		return ( (Guid) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginAuthenticate(String loginId, String password, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Authenticate", new Object[]
								{
								loginId,
								password}, callback, asyncState);
	}

	/// <remarks/>
	public Guid endAuthenticate(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Guid) (results[0]));
	}

	public DataSet getChartData(Guid instrumentId, String dataCycleParameter, DateTime from, DateTime to)
	{
		Object[] results = this.invoke("GetChartData2", ServiceTimeoutSetting.getChartData, new Object[]
									   {instrumentId, dataCycleParameter, from, to});
		/*Object[] results = this.invoke("GetChartData", ServiceTimeoutSetting.getChartData, new Object[]
									   {instrumentId, from, 500, dataCycleParameter});*/
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getChartData(Guid instrumentId, DateTime lastDate, int count, String dataCycleParameter)
	{
		Object[] results = this.invoke("GetChartData", ServiceTimeoutSetting.getChartData, new Object[]
									   {instrumentId,
									   lastDate,
									   count,
									   dataCycleParameter});
		return ( (DataSet) (results[0]));
	}

	public DataSet getChartData(Guid asyncResultId)
	{
		Object[] results = this.invoke("GetChartDataForJava2", ServiceTimeoutSetting.getChartData,
									   new Object[]{asyncResultId});
		return ( (DataSet) (results[0]));
	}

	public Guid asyncGetChartData(Guid instrumentId, DateTime lastDate, int count, String dataCycleParameter)
	{
		Object[] results = this.invoke("AsyncGetChartData", ServiceTimeoutSetting.getCommands, new Object[]
									   {instrumentId,
									   lastDate,
									   count,
									   dataCycleParameter});
		return ( (Guid) (results[0]));
	}

	public Guid asyncGetChartData2(Guid instrumentId, DateTime from, DateTime to, String dataCycleParameter)
	{
		Object[] results = this.invoke("AsyncGetChartData2", ServiceTimeoutSetting.getCommands, new Object[]
									   {instrumentId, from, to, dataCycleParameter});
		return ( (Guid) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetChartData(Guid instrumentId, DateTime lastDate, int count, String dataCycleParameter, IAsyncCallback callback,
										  Object asyncState)
	{
		return this.beginInvoke("GetChartData", new Object[]
								{instrumentId,
								lastDate,
								count,
								dataCycleParameter}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetChartData(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public void setInstrumentCodes()
	{
		this.invoke("SetInstrumentCodes", ServiceTimeoutSetting.setInstrumentCodes, new Object[0]);
	}

	/// <remarks/>
	public IAsyncResult beginSetInstrumentCodes(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("SetInstrumentCodes", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public void endSetInstrumentCodes(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void clearHistoryDataCache(Guid instrumentId)
	{
		this.invoke("ClearHistoryDataCache", ServiceTimeoutSetting.clearHistoryDataCache, new Object[]
					{
					instrumentId});
	}

	/// <remarks/>
	public IAsyncResult beginClearHistoryDataCache(Guid instrumentId, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("ClearHistoryDataCache", new Object[]
								{
								instrumentId}, callback, asyncState);
	}

	/// <remarks/>
	public void endClearHistoryDataCache(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void callService()
	{
		this.invoke("CallService", ServiceTimeoutSetting.callService, new Object[0]);
	}

	/// <remarks/>
	public IAsyncResult beginCallService(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("CallService", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public void endCallService(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public XmlNode logout()
	{
		Object[] results = this.invoke("Logout", ServiceTimeoutSetting.logout, new Object[0]);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginLogout(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Logout", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endLogout(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public void saveIsCalculateFloat(boolean isCalculateFloat)
	{
		this.invoke("SaveIsCalculateFloat", ServiceTimeoutSetting.saveIsCalculateFloat, new Object[]
					{
					isCalculateFloat});
	}

	/// <remarks/>
	public IAsyncResult beginSaveIsCalculateFloat(boolean isCalculateFloat, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("SaveIsCalculateFloat", new Object[]
								{
								isCalculateFloat}, callback, asyncState);
	}

	/// <remarks/>
	public void endSaveIsCalculateFloat(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public XmlNode getAccounts(Guid[] accountIDs, boolean includeTransactions)
	{
		Object[] results = this.invoke("GetAccounts", ServiceTimeoutSetting.getAccounts, new Object[]
									   {
									   accountIDs,
									   includeTransactions});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetAccounts(Guid[] accountIDs, boolean includeTransactions, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetAccounts", new Object[]
								{
								accountIDs,
								includeTransactions}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetAccounts(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public XmlNode getAccountsForCut(Guid[] accountIDs, boolean includeTransactions)
	{
		Object[] results = this.invoke("GetAccountsForCut", ServiceTimeoutSetting.getAccounts, new Object[]
									   {
									   accountIDs,
									   includeTransactions});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetAccountsForCut(Guid[] accountIDs, boolean includeTransactions, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetAccountsForCut", new Object[]
								{
								accountIDs,
								includeTransactions}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetAccountsForCut(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	public Object[] getOrderInstalment(Guid orderId)
	{
		Object[] results = this.invoke("GetOrderInstalment",ServiceTimeoutSetting.getAccounts, new Object[]{orderId});
		return (results);
	}

	/// <remarks/>
	public Object[] getAccountForCut(/*ref*/DateTime lastAlertTime, Guid accountId, boolean includeTransactions)
	{
	  Object[] results = this.invoke("GetAccountForCut",ServiceTimeoutSetting.getAccounts, new Object[]
	  {
	  lastAlertTime,
	  accountId,
	  includeTransactions});
	  return (results);
	}

	/// <remarks/>
	public IAsyncResult beginGetAccountForCut(DateTime lastAlertTime, Guid accountId, boolean includeTransactions, IAsyncCallback callback, Object asyncState)
	{
	  return this.beginInvoke("GetAccountForCut", new Object[]
	  {
	  lastAlertTime,
	  accountId,
	  includeTransactions}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endGetAccountForCut(IAsyncResult asyncResult,DateTime lastAlertTime)
	{
		Object[] results = this.endInvoke(asyncResult);
		return (results);
	}

	/// <remarks/>
	public Object[] getInitData( /*out*/int commandSequence)
	{
		Object[] results = this.invoke("GetInitData", ServiceTimeoutSetting.getInitData, new Object[0]);
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginGetInitData(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInitData", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public Object[] endGetInitData(IAsyncResult asyncResult, /*out*/ int commandSequence)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	/// <remarks/>
	public void refreshInstrumentsState(Guid customerID)
	{
		this.invoke("RefreshInstrumentsState", ServiceTimeoutSetting.refreshInstrumentsState,
					new Object[]{customerID});
	}

	public DataSet getQuotePolicyDetailsAndRefreshInstrumentsState(Guid customerID)
	{
		Object[] results = this.invoke("GetQuotePolicyDetailsAndRefreshInstrumentsState", ServiceTimeoutSetting.refreshInstrumentsState,
					new Object[]{customerID});
		return (DataSet)results[0];
	}

	public DataSet getDealingPolicyDetails()
	{
		Object[] results = this.invoke("GetDealingPolicyDetails", ServiceTimeoutSetting.refreshInstrumentsState,
					new Object[]{});
		return (DataSet)results[0];
	}

	/// <remarks/>
	public IAsyncResult beginRefreshInstrumentsState(Guid customerID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("RefreshInstrumentsState",
								new Object[]{customerID}, callback, asyncState);
	}

	/// <remarks/>
	public void endRefreshInstrumentsState(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void emailExecuteOrders(Object[] emailExecuteOrders)
	{
		this.invoke("EmailExecuteOrders", ServiceTimeoutSetting.emailExecuteOrders, new Object[]
					{emailExecuteOrders});
	}

	/// <remarks/>
	public IAsyncResult beginEmailExecuteOrders(Object[] emailExecuteOrders, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("EmailExecuteOrders", new Object[]
								{emailExecuteOrders}, callback, asyncState);
	}

	/// <remarks/>
	public void endEmailExecuteOrders(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	public void LogResultOfGetCommands(Guid userId, int firstSequence, int lastSequence)
	{
		this.invoke("LogResultOfGetCommands", ServiceTimeoutSetting.getCommands,
					new Object[]{userId, true, firstSequence, lastSequence});
	}

	public void LogResultOfGetCommands2(Guid userId, int firstSequence, int lastSequence)
	{
		this.invoke("LogResultOfGetCommands", ServiceTimeoutSetting.getCommands,
					new Object[]{userId, false, firstSequence, lastSequence});
	}

	/// <remarks/>
	public XmlNode getCommands()
	{
		//Object[] results = this.invoke("GetCommands", new Object[0]);
		Object[] results = this.invoke("GetCommands", ServiceTimeoutSetting.getCommands, new Object[0]);
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
		Object[] results = this.invoke("GetCommands2", ServiceTimeoutSetting.getCommands2, new Object[]
									   {firstSequence, lastSequence});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetCommands2(int firstSequence, int lastSequence, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetCommands2", new Object[]
								{firstSequence, lastSequence}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetCommands2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public void cancelQuote(String instrumentID, Double buyQuoteLot, Double sellQuoteLot)
	{
		this.invoke("CancelQuote", ServiceTimeoutSetting.cancelQuote, new Object[]
					{instrumentID, buyQuoteLot, sellQuoteLot});
	}

	/// <remarks/>
	public IAsyncResult beginCancelQuote(String instrumentID, Double buyQuoteLot, Double sellQuoteLot, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("CancelQuote", new Object[]
								{instrumentID, buyQuoteLot, sellQuoteLot}, callback, asyncState);
	}

	/// <remarks/>
	public void endCancelQuote(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void quote2(String instrumentID, Double buyQuoteLot, Double sellQuoteLot, int tick)
	{
		this.invoke("Quote2", ServiceTimeoutSetting.quote, new Object[]
					{instrumentID,
					buyQuoteLot,
					sellQuoteLot,
					tick});
	}

	/// <remarks/>
	public IAsyncResult beginQuote2(String instrumentID, Double buyQuoteLot, Double sellQuoteLot, int tick, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Quote2", new Object[]
								{instrumentID,
								buyQuoteLot,
								sellQuoteLot,
								tick}, callback, asyncState);
	}

	/// <remarks/>
	public void endQuote2(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void quote(String instrumentID, Double quoteLot, int BSStatus)
	{
		this.invoke("Quote", ServiceTimeoutSetting.quote, new Object[]
					{instrumentID,
					quoteLot,
					BSStatus});
	}

	/// <remarks/>
	public IAsyncResult beginQuote(String instrumentID, Double quoteLot, int BSStatus, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Quote", new Object[]
								{instrumentID,
								quoteLot,
								BSStatus}, callback, asyncState);
	}

	/// <remarks/>
	public void endQuote(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public TransactionError cancelLMTOrder(String transactionID)
	{
		Object[] results = this.invoke("CancelLMTOrder", ServiceTimeoutSetting.cancelLMTOrder, new Object[]
									   {transactionID});
		return ( (TransactionError) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginCancelLMTOrder(String transactionID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("CancelLMTOrder", new Object[]
								{
								transactionID}, callback, asyncState);
	}

	/// <remarks/>
	public TransactionError endCancelLMTOrder(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (TransactionError) (results[0]));
	}

	/// <remarks/>
	public boolean updateAccountLock(String agentAccountID, String[][] arrayAccountLock)
	{
		Object[] results = this.invoke("UpdateAccountLock", ServiceTimeoutSetting.updateAccountLock, new Object[]
									   {
									   agentAccountID,
									   arrayAccountLock});
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public IAsyncResult beginUpdateAccountLock(String agentAccountID, String[][] arrayAccountLock, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("UpdateAccountLock", new Object[]
								{agentAccountID,
								arrayAccountLock}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endUpdateAccountLock(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	public TimeInfo getTimeInfo()
	{
		Object[] results = this.invoke("GetTimeInfo", ServiceTimeoutSetting.getTimeInfo, new Object[0]);
		return ( (TimeInfo) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetTimeInfo(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetTimeInfo", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public TimeInfo endGetTimeInfo(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (TimeInfo) (results[0]));
	}

	public TimeSyncSettings getTimeSyncSettings()
	{
		Object[] results = this.invoke("GetTimeSyncSettings", ServiceTimeoutSetting.getTimeSyncSettings, new Object[0]);
		return ( (TimeSyncSettings) (results[0]));
	}

	public Object[] applyDelivery(XmlNode deliveryRequire)
	{
		Object[] results = this.invoke("ApplyDelivery", ServiceTimeoutSetting.place, new Object[]{deliveryRequire});
		return results;
	}

	public Object[] instalmentPayoff(Guid accountId, Guid currencyId,
											 Double sumSourcePaymentAmount, Double sumSourceTerminateFee,
											 XmlNode instalmentXml, XmlNode terminateXml)
	{
		Object[] results = this.invoke("InstalmentPayoff", ServiceTimeoutSetting.place,
									   new Object[]{accountId, currencyId,
									   sumSourcePaymentAmount, sumSourceTerminateFee,
									   instalmentXml, terminateXml});
		return results;
	}


	/// <remarks/>
	public Object[] place(XmlNode tran)
	{
		Object[] results = this.invoke("Place", ServiceTimeoutSetting.place, new Object[]{tran});
		return results;
	}

	public Object[] multipleClose(Guid[] orderIds)
	{
		Object[] results = this.invoke("MultipleClose", ServiceTimeoutSetting.place, new Object[]{orderIds});
		return results;
	}

	public IAsyncResult beginMultipleClose(Guid[] orderIds, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("MultipleClose", new Object[]{orderIds}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endMultipleClose(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	public DataSet getAccountBankReferenceData(String countryId, String language)
	{
		Object[] result = this.invoke("GetAccountBankReferenceData", ServiceTimeoutSetting.login, new Object[]{countryId, language});
		return (DataSet)result[0];
	}

	public IAsyncResult beginGetAccountBankReferenceData(String countryId, String language, IAsyncCallback callback, Object state)
	{
		return this.beginInvoke("GetAccountBankReferenceData", new Object[]{countryId, language}, callback, state);
	}

	public DataSet endGetAccountBankReferenceData(IAsyncResult asyncResult)
	{
		Object[] result = this.endInvoke(asyncResult);
		return (DataSet)result[0];
	}

	public DataSet getAccountBanksApproved(Guid accountId, String language)
	{
		Object[] result = this.invoke("GetAccountBanksApproved", ServiceTimeoutSetting.login, new Object[]{accountId, language});
		return (DataSet)result[0];
	}

	public IAsyncResult beginGetAccountBanksApproved(Guid accountId, String language, IAsyncCallback callback, Object state)
	{
		return this.beginInvoke("GetAccountBanksApproved", new Object[]
									  {accountId, language}, callback, state);
	}

	public DataSet endGetAccountBanksApproved(IAsyncResult asyncResult)
	{
		Object[] result = this.endInvoke(asyncResult);
		return (DataSet)result[0];
	}

	public boolean[] apply(Guid id, String accountBankApprovedId, String accountId, String countryId, String bankId, String bankName,
		   String accountBankNo, String accountBankType,//#00;银行卡|#01;存折
		   String accountOpener, String accountBankProp, Guid accountBankBCId, String accountBankBCName,
		   String idType,//#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
		   String idNo, String bankProvinceId, String bankCityId, String bankAddress, String swiftCode, int applicationType)
	{
		boolean successed = false;
		boolean approved = false;

		Object[] result = this.invoke("Apply", ServiceTimeoutSetting.place, new Object[]{id, accountBankApprovedId,
					accountId, countryId, bankId, bankName, accountBankNo, accountBankType,//#00;银行卡|#01;存折
					accountOpener, accountBankProp, accountBankBCId, accountBankBCName,
					idType,//#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
		   			idNo, bankProvinceId, bankCityId, bankAddress, swiftCode, applicationType});

		successed = (Boolean)result[0];
		approved = (Boolean)result[1];
		boolean[] result2 = new boolean[2];
		result2[0] = successed;
		result2[1] = approved;

		return result2;
	}



	/// <remarks/>
	public IAsyncResult beginPlace(XmlNode tran, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Place", new Object[]{tran}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endPlace(IAsyncResult asyncResult, /*out*/ String tranCode)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	public Object[] changeLeverage(Guid accountId, int leverage)
	{
		Object[] results = this.invoke("ChangeLeverage", ServiceTimeoutSetting.assign, new Object[]{accountId, leverage});
		return results;
	}


	/// <remarks/>
	public Object[] assign( /*ref*/XmlNode xmlTransaction, /*out*/ XmlNode xmlAccount, /*out*/ XmlNode xmlInstrument)
	{
		Object[] results = this.invoke("Assign", ServiceTimeoutSetting.assign, new Object[]{xmlTransaction});
		return results;
	}

	public Object[] queryOrder(Guid customerId, String accountId, String instrumentId, int lastDays)
	{
		Object[] results = this.invoke("OrderQuery", ServiceTimeoutSetting.assign, new Object[]{customerId, accountId, instrumentId, lastDays});
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginAssign(XmlNode xmlTransaction, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Assign", new Object[]
								{xmlTransaction}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endAssign(IAsyncResult asyncResult, /*out*/ XmlNode xmlTransaction, /*out*/ XmlNode xmlAccount,
							  /*out*/XmlNode xmlInstrument)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	/// <remarks/>
	public DataSet getAccountsForSetting()
	{
		Object[] results = this.invoke("GetAccountsForTradingConsole", ServiceTimeoutSetting.getInstrumentForSetting, new Object[0]);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public XmlNode getInstrumentForSetting()
	{
		Object[] results = this.invoke("GetInstrumentForSetting", ServiceTimeoutSetting.getInstrumentForSetting, new Object[0]);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetInstrumentForSetting(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInstrumentForSetting", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetInstrumentForSetting(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public DataSet getCurrencyRateByAccountID(String accountID)
	{
		Object[] results = this.invoke("GetCurrencyRateByAccountID", ServiceTimeoutSetting.getCurrencyRateByAccountID, new Object[]
									   {accountID});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetCurrencyRateByAccountID(String accountID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetCurrencyRateByAccountID", new Object[]
								{accountID}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetCurrencyRateByAccountID(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getInstrumentsTest(String instrumentID)
	{
		Object[] results = this.invoke("GetInstrumentsTest", ServiceTimeoutSetting.getInstrumentsTest, new Object[]
									   {instrumentID});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetInstrumentsTest(String instrumentID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInstrumentsTest", new Object[]
								{instrumentID}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetInstrumentsTest(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getInstruments(Object[] instrumentIDs)
	{
		Object[] results = this.invoke("GetInstruments", ServiceTimeoutSetting.getInstruments, new Object[]
									   {instrumentIDs});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetInstruments(Object[] instrumentIDs, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInstruments", new Object[]
								{instrumentIDs}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetInstruments(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getMessages()
	{
		Object[] results = this.invoke("GetMessages", ServiceTimeoutSetting.getMessages, new Object[0]);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetMessages(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetMessages", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetMessages(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public boolean deleteMessage(Guid id)
	{
		Object[] results = this.invoke("DeleteMessage", ServiceTimeoutSetting.deleteMessage, new Object[]
									   {id});
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public IAsyncResult beginDeleteMessage(Guid id, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("DeleteMessage", new Object[]{id}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endDeleteMessage(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public Object[] getCustomerInfo( /*out*/String customerName)
	{
		Object[] results = this.invoke("GetCustomerInfo", ServiceTimeoutSetting.getCustomerInfo, new Object[0]);
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginGetCustomerInfo(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetCustomerInfo", new Object[0], callback, asyncState);
	}

	/// <remarks/>
	public Object[] endGetCustomerInfo(IAsyncResult asyncResult, /*out*/ String customerName)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	public boolean updateAccountSetting(Guid[] accountIDs)
	{
		Object[] results = this.invoke("UpdateAccountSetting", ServiceTimeoutSetting.updateInstrumentSetting, new Object[]
									   {accountIDs});
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public DataSet updateInstrumentSetting(String[] instrumentIDs)
	{
		Object[] results = this.invoke("UpdateInstrumentSetting", ServiceTimeoutSetting.updateInstrumentSetting, new Object[]
									   {instrumentIDs});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginUpdateInstrumentSetting(String[] instrumentIDs, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("UpdateInstrumentSetting", new Object[]{instrumentIDs}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endUpdateInstrumentSetting(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public void updateAccount(Guid accountID, Guid groupID, boolean isDelete, boolean isDeleteGroup)
	{
		this.invoke("UpdateAccount", ServiceTimeoutSetting.updateAccount, new Object[]
					{accountID,
					groupID,
					isDelete,
					isDeleteGroup});
	}

	/// <remarks/>
	public IAsyncResult beginUpdateAccount(Guid accountID, Guid groupID, boolean isDelete, boolean isDeleteGroup, IAsyncCallback callback,
										   Object asyncState)
	{
		return this.beginInvoke("UpdateAccount", new Object[]
								{accountID,
								groupID,
								isDelete,
								isDeleteGroup}, callback, asyncState);
	}

	/// <remarks/>
	public void endUpdateAccount(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void updateQuotePolicyDetail(Guid instrumentID, Guid quotePolicyID)
	{
		this.invoke("UpdateQuotePolicyDetail", ServiceTimeoutSetting.updateQuotePolicyDetail, new Object[]
					{instrumentID, quotePolicyID});
	}

	/// <remarks/>
	public IAsyncResult beginUpdateQuotePolicyDetail(Guid instrumentID, Guid quotePolicyID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("UpdateQuotePolicyDetail", new Object[]
								{instrumentID, quotePolicyID}, callback, asyncState);
	}

	/// <remarks/>
	public void endUpdateQuotePolicyDetail(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public Object[] recoverPasswordDatas(String[][] recoverPasswordDatas)
	{
		Object[] results = this.invoke("RecoverPasswordDatas", ServiceTimeoutSetting.updatePassword, new Object[]
									   {recoverPasswordDatas});
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginRecoverPasswordDatas(String[][] recoverPasswordDatas, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("RecoverPasswordDatas", new Object[]
								{recoverPasswordDatas}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endRecoverPasswordDatas(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	/// <remarks/>
	public Object[] UpdatePassword2(String loginID, String oldPassword, String newPassword, String[][] recoverPasswordDatas, /*out*/ String message)
	{
		Object[] results = this.invoke("UpdatePassword2", ServiceTimeoutSetting.updatePassword, new Object[]
									   {loginID,
									   oldPassword,
									   newPassword,
									   recoverPasswordDatas});
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginUpdatePassword2(String loginID, String oldPassword, String newPassword, String[][] recoverPasswordDatas, IAsyncCallback callback,
											 Object asyncState)
	{
		return this.beginInvoke("UpdatePassword2", new Object[]
								{loginID,
								oldPassword,
								newPassword,
								recoverPasswordDatas}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endUpdatePassword2(IAsyncResult asyncResult, /*out*/ String message)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	/// <remarks/>
	public Object[] updatePassword(String loginID, String oldPassword, String newPassword, /*out*/ String message)
	{
		Object[] results = this.invoke("UpdatePassword", ServiceTimeoutSetting.updatePassword, new Object[]
									   {loginID,
									   oldPassword,
									   newPassword});
		return results;
	}

	/// <remarks/>
	public IAsyncResult beginUpdatePassword(String loginID, String oldPassword, String newPassword, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("UpdatePassword", new Object[]
								{loginID,
								oldPassword,
								newPassword}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endUpdatePassword(IAsyncResult asyncResult, /*out*/ String message)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	/// <remarks/>
	public void statementForJava(int statementReportType, String tradeDay, String IDs, String reportxml)
	{
		this.invoke("StatementForJava", ServiceTimeoutSetting.statementForJava, new Object[]
					{statementReportType,
					tradeDay,
					IDs,
					reportxml});
	}

/// <remarks/>
	public IAsyncResult beginStatementForJava(int statementReportType, String tradeDay, String IDs, String reportxml, IAsyncCallback callback,
											  Object asyncState)
	{
		return this.beginInvoke("StatementForJava", new Object[]
								{statementReportType,
								tradeDay,
								IDs,
								reportxml}, callback, asyncState);
	}

/// <remarks/>
	public void endStatementForJava(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	public byte[] getReportContent(Guid id)
	{
		Object[] results = this.invoke("GetReportContent", ServiceTimeoutSetting.statementForJava, new Object[]
					{id});
		return ( (byte[]) (results[0]));
	}

/// <remarks/>
	public Guid statementForJava2(int statementReportType, String dayBegin, String dayTo, String IDs, String reportxml)
	{
		Object[] results = this.invoke("StatementForJava2", ServiceTimeoutSetting.getCommands, new Object[]
					{statementReportType,
					dayBegin,
					dayTo,
					IDs,
					reportxml});
		return ( (Guid) (results[0]));
	}

/// <remarks/>
	public IAsyncResult beginStatementForJava2(int statementReportType, String tradeDay, String IDs, String reportxml, IAsyncCallback callback,
											   Object asyncState)
	{
		return this.beginInvoke("StatementForJava2", new Object[]
								{statementReportType,
								tradeDay,
								IDs,
								reportxml}, callback, asyncState);
	}

/// <remarks/>
	public Guid endStatementForJava2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Guid) (results[0]));
	}

	/// <remarks/>
	public void statement(int statementReportType, String tradeDay, String IDs, String reportxml)
	{
		this.invoke("Statement", ServiceTimeoutSetting.statement, new Object[]
					{statementReportType,
					tradeDay,
					IDs,
					reportxml});
	}

	/// <remarks/>
	public IAsyncResult beginStatement(int statementReportType, String tradeDay, String IDs, String reportxml, IAsyncCallback callback,
									   Object asyncState)
	{
		return this.beginInvoke("Statement", new Object[]
								{statementReportType,
								tradeDay,
								IDs,
								reportxml}, callback, asyncState);
	}

	/// <remarks/>
	public void endStatement(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void ledgerForJava(String dateFrom, String dateTo, String IDs, String reportxml)
	{
		this.invoke("LedgerForJava", ServiceTimeoutSetting.ledgerForJava, new Object[]
					{dateFrom,
					dateTo,
					IDs,
					reportxml});
	}

	/// <remarks/>
	public IAsyncResult beginLedgerForJava(String dateFrom, String dateTo, String IDs, String reportxml, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("LedgerForJava", new Object[]
								{dateFrom,
								dateTo,
								IDs,
								reportxml}, callback, asyncState);
	}

	/// <remarks/>
	public void endLedgerForJava(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public Guid accountSummaryForJava2(String fromDay, String toDay, String IDs, String reportxml)
	{
		Object[] results = this.invoke("AccountSummaryForJava2", ServiceTimeoutSetting.getCommands, new Object[]
					{fromDay,
					toDay,
					IDs,
					reportxml});
		return ( (Guid) (results[0]));
	}

	public Guid ledgerForJava2(String dateFrom, String dateTo, String IDs, String reportxml)
	{
		Object[] results = this.invoke("LedgerForJava2", ServiceTimeoutSetting.getCommands, new Object[]
					{dateFrom,
					dateTo,
					IDs,
					reportxml});
		return ( (Guid) (results[0]));
	}


	/// <remarks/>
	public IAsyncResult beginLedgerForJava2(String dateFrom, String dateTo, String IDs, String reportxml, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("LedgerForJava2", new Object[]
								{dateFrom,
								dateTo,
								IDs,
								reportxml}, callback, asyncState);
	}

	/// <remarks/>
	public Guid endLedgerForJava2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Guid) (results[0]));
	}

	public void complain(String loginName, String complaint)
	{
		this.invoke("Complain", ServiceTimeoutSetting.log, new Object[]{loginName, complaint});
	}

	/// <remarks/>
	public void ledger(String dateFrom, String dateTo, String IDs, String reportxml)
	{
		this.invoke("Ledger", ServiceTimeoutSetting.ledger, new Object[]
					{dateFrom,
					dateTo,
					IDs,
					reportxml});
	}

	/// <remarks/>
	public IAsyncResult beginLedger(String dateFrom, String dateTo, String IDs, String reportxml, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Ledger", new Object[]
								{dateFrom,
								dateTo,
								IDs,
								reportxml}, callback, asyncState);
	}

	/// <remarks/>
	public void endLedger(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	public String[] get99BillBanks()
	{
		Object[] results = this.invoke("Get99BillBanks", ServiceTimeoutSetting.additionalClient, new Object[]{});
		return (String[])results[0];
	}

	public String[] getMerchantInfoFor99Bill(Guid[] organizationIds)
	{
		Object[] results = this.invoke("GetMerchantInfoFor99Bill", ServiceTimeoutSetting.additionalClient, new Object[]{organizationIds});
		return (String[])results[0];
	}

	public long getNextPaySequence(String merchantAcctId)
	{
		Object[] results = this.invoke("GetNextOrderNoFor99Bill", ServiceTimeoutSetting.additionalClient, new Object[]{merchantAcctId});
		return ((Long)results[0]).longValue();
	}

	/// <remarks/>
	public String additionalClient(
		String email,
		String receive,
		String organizationName,
		String customerName,
		String reportDate,
		String accountCode,
		String correspondingAddress,
		String registratedEmailAddress,
		String tel,
		String mobile,
		String fax,
		String fillName1,
		String ICNo1,
		String fillName2,
		String ICNo2,
		String fillName3,
		String ICNo3)
	{
		Object[] results = this.invoke("AdditionalClient", ServiceTimeoutSetting.additionalClient, new Object[]
									   {email,
									   receive,
									   organizationName,
									   customerName,
									   reportDate,
									   accountCode,
									   correspondingAddress,
									   registratedEmailAddress,
									   tel,
									   mobile,
									   fax,
									   fillName1,
									   ICNo1,
									   fillName2,
									   ICNo2,
									   fillName3,
									   ICNo3});
		if(( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error";
		}
	}

	/// <remarks/>
	public IAsyncResult beginAdditionalClient(
		String email,
		String receive,
		String organizationName,
		String customerName,
		String reportDate,
		String accountCode,
		String correspondingAddress,
		String registratedEmailAddress,
		String tel,
		String mobile,
		String fax,
		String fillName1,
		String ICNo1,
		String fillName2,
		String ICNo2,
		String fillName3,
		String ICNo3,
		IAsyncCallback callback,
		Object asyncState)
	{
		return this.beginInvoke("AdditionalClient", new Object[]
								{
								email,
								receive,
								organizationName,
								customerName,
								reportDate,
								accountCode,
								correspondingAddress,
								registratedEmailAddress,
								tel,
								mobile,
								fax,
								fillName1,
								ICNo1,
								fillName2,
								ICNo2,
								fillName3,
								ICNo3}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endAdditionalClient(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public String agent(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
						 String previousAgentCode,
						 String previousAgentName, String newAgentCode, String newAgentName, String newAgentICNo, String dateReply)
	{
		Object[] results = this.invoke("Agent", ServiceTimeoutSetting.agent, new Object[]
									   {
									   email,
									   receive,
									   organizationName,
									   customerName,
									   reportDate,
									   accountCode,
									   previousAgentCode,
									   previousAgentName,
									   newAgentCode,
									   newAgentName,
									   newAgentICNo,
									   dateReply});
		if(( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error:" + (String)results[2];
		}
	}

	/// <remarks/>
	public IAsyncResult beginAgent(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
								   String previousAgentCode, String previousAgentName, String newAgentCode, String newAgentName, String newAgentICNo,
								   String dateReply,
								   IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("Agent", new Object[]
								{
								email,
								receive,
								organizationName,
								customerName,
								reportDate,
								accountCode,
								previousAgentCode,
								previousAgentName,
								newAgentCode,
								newAgentName,
								newAgentICNo,
								dateReply}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endAgent(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public String callMarginExtension(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									   String currency,
									   String currencyValue, String dueDate)
	{
		Object[] results = this.invoke("CallMarginExtension", ServiceTimeoutSetting.callMarginExtension, new Object[]
									   {
									   email,
									   receive,
									   organizationName,
									   customerName,
									   reportDate,
									   accountCode,
									   currency,
									   currencyValue,
									   dueDate});
		if(( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error";
		}
	}

	/// <remarks/>
	public IAsyncResult beginCallMarginExtension(String email, String receive, String organizationName, String customerName, String reportDate,
												 String accountCode,
												 String currency, String currencyValue, String dueDate, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("CallMarginExtension", new Object[]
								{
								email,
								receive,
								organizationName,
								customerName,
								reportDate,
								accountCode,
								currency,
								currencyValue,
								dueDate}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endCallMarginExtension(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public boolean fundTransfer(String email, String receive, String organizationName, String customerName, String reportDate, String currency,
								String currencyValue,
								String accountCode, String bankAccount, String beneficiaryName, String replyDate)
	{
		Object[] results = this.invoke("FundTransfer", ServiceTimeoutSetting.fundTransfer, new Object[]
									   {
									   email,
									   receive,
									   organizationName,
									   customerName,
									   reportDate,
									   currency,
									   currencyValue,
									   accountCode,
									   bankAccount,
									   beneficiaryName,
									   replyDate});
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public IAsyncResult beginFundTransfer(String email, String receive, String organizationName, String customerName, String reportDate,
										  String currency,
										  String currencyValue, String accountCode, String bankAccount, String beneficiaryName, String replyDate,
										  IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("FundTransfer", new Object[]
								{
								email,
								receive,
								organizationName,
								customerName,
								reportDate,
								currency,
								currencyValue,
								accountCode,
								bankAccount,
								beneficiaryName,
								replyDate}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endFundTransfer(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public String paymentInstruction(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									  String currency,
									  String currencyValue, String beneficiaryName, String bankAccount, String bankerName, String bankerAddress,
									  String swiftCode, String remarks,
									  String thisisClient)
	{
		Object[] results = this.invoke("PaymentInstruction", ServiceTimeoutSetting.paymentInstruction, new Object[]
									   {
									   email,
									   receive,
									   organizationName,
									   customerName,
									   reportDate,
									   accountCode,
									   currency,
									   currencyValue,
									   beneficiaryName,
									   bankAccount,
									   bankerName,
									   bankerAddress,
									   swiftCode,
									   remarks,
									   thisisClient});
		if(( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error";
		}
	}

	public String paymentInstructionInternal(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currencyCode, String amount, String beneficiaryAccount,
					String beneficiaryAccountOwner, String email2)
	{
		Object[] results = this.invoke("PaymentInstructionInternal", ServiceTimeoutSetting.paymentInstruction, new Object[]
									   {email, organizationName, customerName, reportDate,
									   accountCode, currencyCode, amount, beneficiaryAccount,
									   beneficiaryAccountOwner, email2});
		if ( ( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error";
		}
	}

	public String paymentInstructionCash(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currency, String amount, String beneficiaryName, String beneficiaryAddress)
	{
		Object[] results = this.invoke("PaymentInstructionCash", ServiceTimeoutSetting.paymentInstruction, new Object[]
									   {email, organizationName, customerName, reportDate,
									   accountCode, currency, amount, beneficiaryName, beneficiaryAddress});
		if ( ( (Boolean) (results[0])).booleanValue())
		{
			return (String)results[1];
		}
		else
		{
			return "Error";
		}
	}

	/// <remarks/>
	public IAsyncResult beginPaymentInstruction(
		String email,
		String receive,
		String organizationName,
		String customerName,
		String reportDate,
		String accountCode,
		String currency,
		String currencyValue,
		String beneficiaryName,
		String bankAccount,
		String bankerName,
		String bankerAddress,
		String swiftCode,
		String remarks,
		String thisisClient,
		IAsyncCallback callback,
		Object asyncState)
	{
		return this.beginInvoke("PaymentInstruction", new Object[]
								{
								email,
								receive,
								organizationName,
								customerName,
								reportDate,
								accountCode,
								currency,
								currencyValue,
								beneficiaryName,
								bankAccount,
								bankerName,
								bankerAddress,
								swiftCode,
								remarks,
								thisisClient}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endPaymentInstruction(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public void notifyCustomerExecuteOrder(String[][] arrayNotifyCustomerExecuteOrder)
	{
		this.invoke("NotifyCustomerExecuteOrder", ServiceTimeoutSetting.notifyCustomerExecuteOrder, new Object[]
					{
					arrayNotifyCustomerExecuteOrder});
	}

	/// <remarks/>
	public IAsyncResult beginNotifyCustomerExecuteOrder(String[][] arrayNotifyCustomerExecuteOrder, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("NotifyCustomerExecuteOrder", new Object[]
								{
								arrayNotifyCustomerExecuteOrder}, callback, asyncState);
	}

	/// <remarks/>
	public void endNotifyCustomerExecuteOrder(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public void notifyCustomerExecuteOrderForJava(String[][] arrayNotifyCustomerExecuteOrder, String companyCode, String version)
	{
		this.invoke("NotifyCustomerExecuteOrderForJava", ServiceTimeoutSetting.notifyCustomerExecuteOrderForJava, new Object[]
					{
					arrayNotifyCustomerExecuteOrder,
					companyCode,
					version});
	}

	/// <remarks/>
	public IAsyncResult beginNotifyCustomerExecuteOrderForJava(String[][] arrayNotifyCustomerExecuteOrder, String companyCode, String version,
		IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("NotifyCustomerExecuteOrderForJava", new Object[]
								{
								arrayNotifyCustomerExecuteOrder,
								companyCode, version}, callback, asyncState);
	}

	/// <remarks/>
	public void endNotifyCustomerExecuteOrderForJava(IAsyncResult asyncResult)
	{
		this.endInvoke(asyncResult);
	}

	/// <remarks/>
	public byte[] GetLogoForJava(String companyCode)
	{
		Object[] results = this.invoke("GetLogoForJava", ServiceTimeoutSetting.getLogoForJava, new Object[]
									   {
									   companyCode});
		return ( (byte[]) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetLogoForJava(String companyCode, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetLogoForJava", new Object[]
								{
								companyCode}, callback, asyncState);
	}

	/// <remarks/>
	public byte[] endGetLogoForJava(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (byte[]) (results[0]));
	}

	/// <remarks/>
	public XmlNode getColorSettingsForJava(String companyCode)
	{
		Object[] results = this.invoke("GetColorSettingsForJava", ServiceTimeoutSetting.getColorSettingsForJava, new Object[]
									   {
									   companyCode});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetColorSettingsForJava(String companyCode, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetColorSettingsForJava", new Object[]
								{
								companyCode}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetColorSettingsForJava(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public byte[] GetTracePropertiesForJava()
	{
		Object[] results = this.invoke("GetTracePropertiesForJava", ServiceTimeoutSetting.getTracePropertiesForJava, new Object[]
									   {});
		return ( (byte[]) (results[0]));
	}

/// <remarks/>
	public IAsyncResult beginGetTracePropertiesForJava(IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetTracePropertiesForJava", new Object[]
								{}, callback, asyncState);
	}

/// <remarks/>
	public byte[] endGetTracePropertiesForJava(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (byte[]) (results[0]));
	}

	/// <remarks/>
	public XmlNode getParameterForJava(String companyCode, String version)
	{
		Object[] results = this.invoke("GetParameterForJava", ServiceTimeoutSetting.getParameterForJava, new Object[]
									   {
									   companyCode,
									   version});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetParameterForJava(String companyCode, String version, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetParameterForJava", new Object[]
								{
								companyCode,
								version}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetParameterForJava(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public boolean updateSystemParameters(String parameters, String ObjectID)
	{
		Object[] results = this.invoke("UpdateSystemParameters", ServiceTimeoutSetting.updateSystemParameters, new Object[]
									   {
									   parameters,
									   ObjectID});
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public IAsyncResult beginUpdateSystemParameters(String parameters, String ObjectID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("UpdateSystemParameters", new Object[]
								{
								parameters,
								ObjectID}, callback, asyncState);
	}

	/// <remarks/>
	public boolean endUpdateSystemParameters(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Boolean) (results[0])).booleanValue();
	}

	/// <remarks/>
	public DataSet getNewsList(String newsCategoryID, String language, DateTime date)
	{
		Object[] results = this.invoke("GetNewsList", ServiceTimeoutSetting.getNewsList, new Object[]
									   {
									   newsCategoryID,
									   language,
									   date});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetNewsList(String newsCategoryID, String language, DateTime date, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetNewsList", new Object[]
								{
								newsCategoryID,
								language,
								date}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetNewsList(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getNewsList2(String newsCategoryID, String language, DateTime date)
	{
		Object[] results = this.invoke("GetNewsList2", ServiceTimeoutSetting.getNewsList, new Object[]
									   {
									   newsCategoryID,
									   language,
									   date});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetNewsList2(String newsCategoryID, String language, DateTime date, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetNewsList2", new Object[]
								{
								newsCategoryID,
								language,
								date}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetNewsList2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getNewsContents(String newsID)
	{
		Object[] results = this.invoke("GetNewsContents", ServiceTimeoutSetting.getNewsContents, new Object[]
									   {
									   newsID});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetNewsContents(String newsID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetNewsContents", new Object[]
								{
								newsID}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetNewsContents(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getInterestRate(Guid[] orderIds)
	{
		Object[] results = this.invoke("GetInterestRate", ServiceTimeoutSetting.getInterestRate, new Object[]
									   {
									   orderIds});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetInterestRate(Guid[] orderIds, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInterestRate", new Object[]
								{
								orderIds}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetInterestRate(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public DataSet getInterestRate2(Guid interestRateId)
	{
		Object[] results = this.invoke("GetInterestRate2", ServiceTimeoutSetting.getInterestRate, new Object[]
									   {
									   interestRateId});
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetInterestRate2(Guid interestRateId, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetInterestRate2", new Object[]
								{
								interestRateId}, callback, asyncState);
	}

	/// <remarks/>
	public DataSet endGetInterestRate2(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (DataSet) (results[0]));
	}

	/// <remarks/>
	public BigDecimal refreshAgentAccountOrder(String orderID)
	{
		Object[] results = this.invoke("RefreshAgentAccountOrder", ServiceTimeoutSetting.refreshAgentAccountOrder, new Object[]
									   {
									   orderID});
		return ( (BigDecimal) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginRefreshAgentAccountOrder(String orderID, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("RefreshAgentAccountOrder", new Object[]
								{
								orderID}, callback, asyncState);
	}

	/// <remarks/>
	public BigDecimal endRefreshAgentAccountOrder(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (BigDecimal) (results[0]));
	}

	/// <remarks/>
	public Guid[] verifyTransaction(Guid[] transactionIDs)
	{
		StringBuilder transactionIDs2 = new StringBuilder();
		for(Guid transactionID : transactionIDs)
		{
			transactionIDs2.append(transactionID.toString() + "; ");
		}
		TradingConsole.traceSource.trace(TraceType.Information, "verifyTransaction: " + transactionIDs2);

		Object[] results = this.invoke("VerifyTransaction", ServiceTimeoutSetting.verifyTransaction, new Object[]
									   {transactionIDs});
		return (Guid[])(results[0]);
	}

	/// <remarks/>
	public IAsyncResult beginVerifyTransaction(Guid[] transactionIDs, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("VerifyTransaction", new Object[]
								{
								transactionIDs}, callback, asyncState);
	}

	/// <remarks/>
	public Guid[] endVerifyTransaction(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (Guid[]) (results[0]));
	}

	/// <remarks/>
	public XmlNode getOuterNews(String newsUrl)
	{
		Object[] results = this.invoke("GetOuterNews", ServiceTimeoutSetting.getOuterNews, new Object[]
									   {
									   newsUrl});
		return ( (XmlNode) (results[0]));
	}

	/// <remarks/>
	public IAsyncResult beginGetOuterNews(String newsUrl, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("GetOuterNews", new Object[]
								{
								newsUrl}, callback, asyncState);
	}

	/// <remarks/>
	public XmlNode endGetOuterNews(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return ( (XmlNode) (results[0]));
	}

	public void saveLog(String logCode, DateTime timestamp, String action, Guid transactionId)
	{
		this.invoke("SaveLog2", ServiceTimeoutSetting.log, new Object[]
					{
					logCode,
					timestamp,
					action,
					transactionId});
	}

	public String[] getProxyUrls()
	{
		Object[] result = this.invoke("GetProxyUrls", ServiceTimeoutSetting.ThirtySeconds, new Object[]{});
		return (String[])result[0];
	}

	public boolean registerProxy(Guid userId)
	{
		try
		{
			TradingConsole.traceSource.trace(TraceType.Information, "register proxy with userId = " + userId);
			Object[] result = this.invoke("RegisterProxy", ServiceTimeoutSetting.TwoMinutes, new Object[]{userId});
			return ((Boolean)result[0]).booleanValue();
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
	}

	public boolean switchToProxy()
	{
		try
		{
			this.invoke("SwitchToProxy", ServiceTimeoutSetting.ThirtySeconds, new Object[]{});
			return true;
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
	}

	public boolean modifyOrder(Guid id, Guid transactionId, Guid orderId, Guid instrumentId, String newLot, String newSetPrice, String newSetPrice2)
	{
		try
		{
			Object[] result = this.invoke("ModifyOrder", ServiceTimeoutSetting.ThirtySeconds,
						new Object[]{id, transactionId, orderId, instrumentId, newLot, newSetPrice, newSetPrice2});
			return ((Boolean)result[0]).booleanValue();
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
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

	public String[] getDeliveryAddress(Guid deliveryPointAddressGroupId)
	{
		try
		{
			Object[] result = this.invoke("GetDeliveryAddress", ServiceTimeoutSetting.ThirtySeconds,
						new Object[]{deliveryPointAddressGroupId});
			return ((String[])result[0]);
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return null;
		}
	}

	public boolean modifyTelephoneIdentificationCode(Guid accountId, String oldCode, String newCode)
	{
		try
		{
			Object[] result = this.invoke("ModifyTelephoneIdentificationCode", ServiceTimeoutSetting.ThirtySeconds,
						new Object[]{accountId, oldCode, newCode});
			return ((Boolean)result[0]).booleanValue();
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
	}

	public boolean verifyMarginPin(Guid accountId, String marginPin)
	{
		try
		{
			Object[] result = this.invoke("VerifyMarginPin", ServiceTimeoutSetting.ThirtySeconds,
						new Object[]{accountId, marginPin});
			return ((Boolean)result[0]).booleanValue();
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
	}

	public boolean changeMarginPin(Guid accountId, String oldMarginPin, String newMarginPin)
	{
		try
		{
			Object[] result = this.invoke("ChangeMarginPin", ServiceTimeoutSetting.ThirtySeconds,
						new Object[]{accountId, oldMarginPin, newMarginPin});
			return ((Boolean)result[0]).booleanValue();
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			return false;
		}
	}
}
