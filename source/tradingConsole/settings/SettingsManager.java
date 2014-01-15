package tradingConsole.settings;

//import tradingConsole.ui.Tree;
import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.Currency;
import tradingConsole.enumDefine.*;
import tradingConsole.physical.*;
import tradingConsole.service.*;
import tradingConsole.ui.*;
import framework.lang.Enum;
import tradingConsole.enumDefine.physical.InstalmentFrequence;
import Packet.GuidMapping;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

public class SettingsManager
{
	private TradingConsole _tradingConsole;

	private Customer _customer;
	private TradeDay _tradeDay;
	private SystemParameter _systemParameter;
	private tradingConsole.bursa.SystemParameter _bursaSystemParameter;
	private String _organizationName;

	private HashMap<Guid, Currency> _currencies;
	private HashMap<CompositeKey2<Guid, Guid>, CurrencyRate> _currencyRates;
	private HashMap<CompositeKey3<Guid, Guid, DateTime>, AccountAgentHistory> _accountAgentHistories;
	private HashMap<Guid, Account> _accounts;
	private HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> _accountCurrencies;
	private HashMap<Guid, Instrument> _instruments;
	private HashMap<Guid, TradePolicy> _tradePolicies;
	private HashMap<CompositeKey2<Guid, Guid>, TradePolicyDetail> _tradePolicyDetails;
	private HashMap<CompositeKey2<Guid, Guid>, DealingPolicyDetail> _dealingPolicyDetails;
	private HashMap<CompositeKey2<Guid, Guid>, QuotePolicyDetail> _quotePolicyDetails;
	private HashMap<String, UISetting> _uiSettings;
	private HashMap<Guid, MakeOrderWindow> _makeOrderWindows;
	private HashMap<Guid, Message> _messages;
	private HashMap<Guid, News> _newses;
	private HashMap<Guid, OpenContractForm> _openContractForms = new HashMap<Guid, OpenContractForm>();
	private HashMap<Guid, VolumeNecessary> _volumeNecessaries;
	private HashMap<Guid, ScrapInstrument> _scrapInstruments;
	private HashMap<Guid, DeliveryCharge> _deliveryCharges;
	private HashMap<Guid, InstalmentPolicy> _instalmentPolicys;

	private boolean _isGotMessage = false;
	private boolean _isGotNews = false;
	private boolean _isExistsAgentAccount = false;
	private boolean _isHasNotifiedAssignOrder = false;
	private Logger logger = Logger.getLogger(SettingsManager.class);

	public void setTradingConsole(TradingConsole tradingConsole){
		this._tradingConsole = tradingConsole;
	}


	public void activeAgentedAccount(Guid customerId)
	{
		this.refreshAccountInfo(customerId);
	}

	public void showAccountStatus()
	{
		this.refreshAccountInfo(Guid.empty);
	}

	public String get_OrganizationName()
	{
		return this._organizationName;
	}

	public boolean get_IsForRSZQ()
	{
		return ServiceTimeoutSetting.isForRSZQ;
	}

	public boolean containsOpenContractFormOf(Guid instrumentId)
	{
		return this._openContractForms.containsKey(instrumentId);
	}

	public OpenContractForm getOpenContractFormOf(Guid instrumentId)
	{
		return this._openContractForms.get(instrumentId);
	}

	public void add(Guid instrumentId, OpenContractForm openContractForm)
	{
		this._openContractForms.put(instrumentId, openContractForm);
	}

	public void remove(Guid instrumentId)
	{
		this._openContractForms.remove(instrumentId);
	}

	private void refreshAccountInfo(Guid customerId)
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (customerId.compareTo(Guid.empty) == 0 || account.get_CustomerId().compareTo(customerId) == 0)
			{
				account.refreshAccountInfo();
			}
		}

		//this._tradingConsole.get_MainForm().get_AccountTable().doLayout();
		//this._tradingConsole.get_MainForm().orderDoLayout();

		this.calculateSummary();
	}

	public void calculateSummary()
	{
		for (Iterator<Instrument> iterator = this._instruments.values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();
			if(instrument.get_Category().equals(InstrumentCategory.Margin))
			{
				instrument.calculateSummary();
			}
		}
		Instrument.updateSubtotalSummary();
		this._tradingConsole.rebindSummary();
	}

	public boolean get_IsExistsAgentAccount()
	{
		return this._isExistsAgentAccount;
	}

	public Frame get_MainFrame()
	{
		return this._tradingConsole.get_MainForm();
	}

	public boolean isAllowLockAccount()
	{
		boolean isAllowLockAccount = false;
		if (!this._isExistsAgentAccount)
		{
			return false;
		}

		int canLockAccountCount = 0;
		boolean isHasAgentOrder = false;

		canLockAccountCount = 0;
		isHasAgentOrder = this.isHasAgentOrder();
		Guid useId = this._customer.get_UserId();
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			Guid agentId = account.get_AgentId();
			if (account.get_Type() != AccountType.Agent
				&& agentId != null && agentId.equals(useId))
			{
				canLockAccountCount++;
				if (canLockAccountCount >= 2 && !isHasAgentOrder)
				{
					isAllowLockAccount = true;
					break;
				}
			}
		}

		if (isHasAgentOrder && canLockAccountCount >= 2)
		{
			isAllowLockAccount = true;
		}

		return isAllowLockAccount;
	}

	public SettingsManager()
	{
		this._customer = new Customer(this);
		this._tradeDay = new TradeDay();
		this._systemParameter = new SystemParameter();
		this._bursaSystemParameter = new tradingConsole.bursa.SystemParameter();

		this._currencies = new HashMap<Guid, Currency> ();
		this._currencyRates = new HashMap<CompositeKey2<Guid, Guid>, CurrencyRate> ();
		this._accountAgentHistories = new HashMap<CompositeKey3<Guid, Guid, DateTime>, AccountAgentHistory> ();
		this._accounts = new HashMap<Guid, Account> ();
		this._accountCurrencies = new HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> ();
		this._instruments = new HashMap<Guid, Instrument> ();
		this._tradePolicies = new HashMap<Guid, TradePolicy> ();
		this._tradePolicyDetails = new HashMap<CompositeKey2<Guid, Guid>, TradePolicyDetail> ();
		this._dealingPolicyDetails = new HashMap<CompositeKey2<Guid,Guid>,DealingPolicyDetail>();
		this._volumeNecessaries = new HashMap<Guid,VolumeNecessary>();
		this._scrapInstruments = new HashMap<Guid,ScrapInstrument>();
		this._deliveryCharges = new HashMap<Guid,DeliveryCharge>();
		this._instalmentPolicys = new HashMap<Guid,InstalmentPolicy>();
		this._quotePolicyDetails = new HashMap<CompositeKey2<Guid,Guid>,QuotePolicyDetail>();
		this._uiSettings = new HashMap<String, UISetting> ();
		this._makeOrderWindows = new HashMap<Guid, MakeOrderWindow> ();
		this._messages = new HashMap<Guid, Message> ();
		this._newses = new HashMap<Guid, News> ();

		this._isHasNotifiedAssignOrder = false;
		this._isGotMessage = false;
		this._isGotNews = false;
	}

	public void clear()
	{
		this._customer = new Customer(this);
		this._tradeDay = new TradeDay();
		this._systemParameter = new SystemParameter();
		this._bursaSystemParameter = new tradingConsole.bursa.SystemParameter();

		this._currencies.clear();
		this._currencyRates.clear();
		this._accountAgentHistories.clear();
		this._accounts.clear();
		this._accountCurrencies.clear();
		this._instruments.clear();
		this._tradePolicies.clear();
		this._scrapInstruments.clear();
		this._deliveryCharges.clear();
		this._instalmentPolicys.clear();
		this._volumeNecessaries.clear();
		this._tradePolicyDetails.clear();
		this._uiSettings.clear();
		this._makeOrderWindows.clear();
		this._organizationToAccounts.clear();

		this._newses.clear();
		this._isGotNews = false;
		this._messages.clear();
		this._isGotMessage = false;

		this._isHasNotifiedAssignOrder = false;
	}

	public int getPriceAlign()
	{
		if (this._tradingConsole == null || this._tradingConsole.get_ServiceManager() == null)
		{
			return SwingConstants.RIGHT;
		}
		else
		{
			return this._tradingConsole.get_ServiceManager().getPriceAlign();
		}
	}

	public boolean get_IsGotNews()
	{
		return this._isGotNews;
	}

	public HashMap<Guid, News> get_News()
	{
		return this._newses;
	}

	public News getNews(Guid id)
	{
		return (this._newses.containsKey(id)) ? this._newses.get(id) : null;
	}

	public void setNews(News news)
	{
		if (!this._newses.containsKey(news.get_Id()))
		{
			this._newses.put(news.get_Id(), news);
		}
	}
	public void removeNews(News news)
	{
		if (this._newses.containsKey(news.get_Id()))
		{
			this._newses.remove(news.get_Id());
		}
	}

	public boolean get_IsGotMessage()
	{
		return this._isGotMessage;
	}

	public HashMap<Guid, Message> get_Messages()
	{
		return this._messages;
	}

	public Message getMessage(Guid id)
	{
		return (this._messages.containsKey(id)) ? this._messages.get(id) : null;
	}

	public void setMessage(Message message)
	{
		if (!this._messages.containsKey(message.get_Id()))
		{
			this._messages.put(message.get_Id(), message);
		}
	}

	public void removeMessage(Message message)
	{
		if (this._messages.containsKey(message.get_Id()))
		{
			this._messages.remove(message.get_Id());
		}
	}

	public int getMakeOrderWindowSize()
	{
		return this._makeOrderWindows.size();
	}

	public boolean isExistsMakeOrderWindow(Instrument instrument)
	{
		return (this.getMakeOrderWindow(instrument) != null);
	}

	public void setQuoteWindow(Instrument instrument, Window quoteWindow)
	{
		MakeOrderWindow makeOrderWindow = this.getMakeOrderWindow(instrument);
		if (makeOrderWindow != null)
		{
			if(quoteWindow == null) this._tradingConsole.traceSource.trace(TraceType.Information, "Set QuoteWindow to null");
			makeOrderWindow.set_QuoteWindow(quoteWindow);
		}
	}

	public void setNotifyIsAcceptWindow(Instrument instrument, Window notifyIsAcceptWindow)
	{
		MakeOrderWindow makeOrderWindow = this.getMakeOrderWindow(instrument);
		if (makeOrderWindow != null)
		{
			makeOrderWindow.set_NotifyIsAcceptWindow(notifyIsAcceptWindow);
		}
	}

	public MakeOrderWindow getMakeOrderWindow(Instrument instrument)
	{
		return (!this._makeOrderWindows.containsKey(instrument.get_Id())) ? null : this._makeOrderWindows.get(instrument.get_Id());
	}

	public void setMakeOrderWindow(Instrument instrument, MakeOrderWindow makeOrderWindow)
	{
		if (!this._makeOrderWindows.containsKey(instrument.get_Id()))
		{
			this._makeOrderWindows.put(instrument.get_Id(), makeOrderWindow);
		}
	}

	public void removeMakeOrderWindow(Instrument instrument, Window mainWindow)
	{
		if (this._makeOrderWindows.containsKey(instrument.get_Id()))
		{
			MakeOrderWindow makeOrderWindow = (MakeOrderWindow)this._makeOrderWindows.get(instrument.get_Id());
			if(makeOrderWindow.get_MainWindow() == mainWindow)
			{
				this._makeOrderWindows.remove(instrument.get_Id());
			}
		}
	}

	public void closeMakeOrderWindows()
	{
		Object[] makeOrderWindows = this._makeOrderWindows.values().toArray();
		for (int i = this._makeOrderWindows.values().size()-1;i >= 0; i--)
		{
			MakeOrderWindow makeOrderWindow = (MakeOrderWindow)makeOrderWindows[i];
			makeOrderWindow.closeAllWindow();
		}
		for(OpenContractForm form : this._openContractForms.values())
		{
			form.dispose();
		}
	}

	public void notifyIsAcceptMakeSpotTradeOrderByPrice(Instrument instrument)
	{
		MakeOrderWindow makeOrderWindow = this.getMakeOrderWindow(instrument);
		if (makeOrderWindow == null)
		{
			return;
		}
		Window window = makeOrderWindow.get_NotifyIsAcceptWindow();
		if (window == null)
		{
			return;
		}
		if (window instanceof SpotTradeOrderForm)
		{
			( (SpotTradeOrderForm) window).notifyIsAcceptMakeSpotTradeOrderByPrice();
		}
		else if (window instanceof MultiDQOrderForm)
		{
			( (MultiDQOrderForm) window).notifyIsAcceptMakeSpotTradeOrderByPrice();
		}
		else if (window instanceof VerificationOrderForm)
		{
			((VerificationOrderForm)window).notifyIsAcceptMakeSpotTradeOrderByPrice();
		}
		//other..........
	}

	public void notifyMakeOrderUiByPrice(Instrument instrument)
	{
		MakeOrderWindow makeOrderWindow = this.getMakeOrderWindow(instrument);
		if (makeOrderWindow == null)
		{
			return;
		}
		Window window = makeOrderWindow.get_MainWindow();
		if (window == null)
		{
			return;
		}
		if (window instanceof SpotTradeOrderForm)
		{
			( (SpotTradeOrderForm) window).notifyMakeOrderUiByPrice();
		}
		else if (window instanceof LiquidationOrderForm)
		{
			( (LiquidationOrderForm) window).notifyMakeOrderUiByPrice();
		}
		else if (window instanceof MultiDQOrderForm)
		{
			( (MultiDQOrderForm) window).notifyMakeOrderUiByPrice();
		}
		else if (window instanceof MatchOrderForm)
		{
			( (MatchOrderForm) window).notifyMakeOrderUiByPrice();
		}
		else if (window instanceof MakeOrderForm)
		{
			( (MakeOrderForm) window).notifyMakeOrderUiByPrice();
		}
		else if(window instanceof LimitOrderFormDialog)
		{
			( (LimitOrderFormDialog) window).notifyMakeOrderUiByPrice();
		}
		//other..........
	}

	public boolean get_IsHasNotifiedAssignOrder()
	{
		return this._isHasNotifiedAssignOrder;
	}

	public void set_IsHasNotifiedAssignOrder(boolean value)
	{
		this._isHasNotifiedAssignOrder = value;
	}

	public Customer get_Customer()
	{
		return this._customer;
	}

	public TradeDay get_TradeDay()
	{
		return this._tradeDay;
	}

	public SystemParameter get_SystemParameter()
	{
		return this._systemParameter;
	}

	public tradingConsole.bursa.SystemParameter get_BursaSystemParameter()
	{
		return this._bursaSystemParameter;
	}

	//???????
	public Currency getCurrency(String currencyCode)
	{
		for (Iterator<Currency> iterator = this._currencies.values().iterator(); iterator.hasNext(); )
		{
			Currency currency = iterator.next();
			if (currency.get_Code().equals(currencyCode))
			{
				return currency;
			}
		}
		return null;
	}

	public Currency getCurrency(Guid currencyId)
	{
		return (this._currencies.containsKey(currencyId)) ? this._currencies.get(currencyId) : null;
	}

	public void setCurrency(Currency currency)
	{
		this._currencies.put(currency.get_Id(), currency);
	}

	public void removeCurrency(Guid currencyId)
	{
		if (this._currencies.containsKey(currencyId))
		{
			this._currencies.remove(currencyId);
		}
	}

	public CurrencyRate getCurrencyRate(Guid sourceCurrencyId, Guid targetCurrencyId)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (sourceCurrencyId, targetCurrencyId);
		if (this._currencyRates.containsKey(compositeKey))
		{
			return this._currencyRates.get(compositeKey);
		}
		else
		{
			CurrencyRate currencyRate = new CurrencyRate(this, sourceCurrencyId, targetCurrencyId);
			this._currencyRates.put(compositeKey, currencyRate);
			return currencyRate;
		}
	}

	public void setCurrencyRate(Guid sourceCurrencyId, Guid targetCurrencyId, CurrencyRate currencyRate)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (sourceCurrencyId, targetCurrencyId);
		if (!this._currencyRates.containsKey(compositeKey))
		{
			this._currencyRates.put(compositeKey, currencyRate);
		}
	}

	public void removeCurrencyRate(Guid sourceCurrencyId, Guid targetCurrencyId)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (sourceCurrencyId, targetCurrencyId);
		if (this._currencyRates.containsKey(compositeKey))
		{
			this._currencyRates.remove(compositeKey);
		}
	}

	public Collection<AccountAgentHistory> getAccountAgentHistories()
	{
		return this._accountAgentHistories.values();
	}

	public HashMap<Guid, Account> get_Accounts()
	{
		return this._accounts;
	}

	public boolean allowChangePasswordInTrader()
	{
		for(Account account : this._accounts.values())
		{
			if(!account.get_AllowChangePasswordInTrader())
			{
				return false;
			}
		}
		return true;
	}


	public AccountAgentHistory getAccountAgentHistory(Guid accountId, Guid agentAccountId, DateTime agentBeginTime)
	{
		CompositeKey3<Guid, Guid, DateTime> compositeKey = new CompositeKey3<Guid, Guid, DateTime> (accountId, agentAccountId, agentBeginTime);
		return this._accountAgentHistories.get(compositeKey);
	}

	public int getAccountsSize()
	{
		return this._accounts.size();
	}

	public Account getAccount(Guid accountId)
	{
		return (this._accounts.containsKey(accountId)) ? this._accounts.get(accountId) : null;
	}

	public HashMap<Guid, Account> getAccounts()
	{
		return this._accounts;
	}

	//?????
	public Account getAccount(String accountCode)
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Code().compareToIgnoreCase(accountCode) == 0)
			{
				return account;
			}
		}
		return null;
	}

	public AccountCurrency getAccountCurrency(Guid accountId, Guid currencyId)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (accountId, currencyId);
		return (this._accountCurrencies.containsKey(compositeKey)) ? this._accountCurrencies.get(compositeKey) : null;
	}

	public void setAccountCurrency(Guid accountId, Guid currencyId, AccountCurrency accountCurrency)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (accountId, currencyId);
		this._accountCurrencies.put(compositeKey, accountCurrency);
	}

	public void setAccountCurrency(CompositeKey2<Guid, Guid> compositeKey, AccountCurrency accountCurrency)
	{
		this._accountCurrencies.put(compositeKey, accountCurrency);
	}

	public HashMap<Guid, Instrument> getInstruments()
	{
		return this._instruments;
	}

	public ArrayList<Instrument> getInstrumentsForSummary()
	{
		ArrayList list = new ArrayList();
		for(Instrument instrument : this._instruments.values())
		{
			if(instrument.get_Category().equals(InstrumentCategory.Margin))
			{
				list.add(instrument);
			}
		}
		if(list.size() > 0)	list.add(Instrument.SubtotalInstrument);
		return list;
	}

	public Instrument getInstrument(Guid instrumentId)
	{
		return (this._instruments.containsKey(instrumentId)) ? this._instruments.get(instrumentId) : null;
	}

	public TradePolicy getTradePolicy(Guid tradePolicyId)
	{
		return (this._tradePolicies.containsKey(tradePolicyId)) ? this._tradePolicies.get(tradePolicyId) : null;
	}

	public void removeTradePolicy(Guid tradePolicyId)
	{
		if (this._tradePolicies.containsKey(tradePolicyId))
		{
			this._tradePolicies.remove(tradePolicyId);
		}
	}

	public void setTradePolicy(TradePolicy tradePolicy)
	{
		this._tradePolicies.put(tradePolicy.get_Id(), tradePolicy);
	}

	public TradePolicyDetail getTradePolicyDetail(Guid tradePolicyId, Guid instrumentId)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (tradePolicyId, instrumentId);
		return (this._tradePolicyDetails.containsKey(compositeKey)) ? this._tradePolicyDetails.get(compositeKey) : null;
	}

	public QuotePolicyDetail getQuotePolicyDetail(Guid instrumentId)
	{
		QuotePolicyDetail quotePolicyDetail = null;
		Guid privateQuotePolicyId = this._customer.get_PrivateQuotePolicyId();
		if(privateQuotePolicyId != null)
		{
			CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (privateQuotePolicyId, instrumentId);
			quotePolicyDetail
				= (this._quotePolicyDetails.containsKey(compositeKey)) ? this._quotePolicyDetails.get(compositeKey) : null;
		}
		Guid publicQuotePolicyId = this._customer.get_PublicQuotePolicyId();
		if(quotePolicyDetail == null && publicQuotePolicyId != null)
		{
			CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (publicQuotePolicyId, instrumentId);
			quotePolicyDetail
				= (this._quotePolicyDetails.containsKey(compositeKey)) ? this._quotePolicyDetails.get(compositeKey) : null;
		}
		return quotePolicyDetail;
	}

	public void removeTradePolicyDetail(TradePolicyDetail tradePolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (tradePolicyDetail.get_TradePolicyId(), tradePolicyDetail.get_InstrumentId());
		if (this._tradePolicyDetails.containsKey(compositeKey))
		{
			this._tradePolicyDetails.remove(compositeKey);
		}
	}

	public void setTradePolicyDetail(TradePolicyDetail tradePolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (tradePolicyDetail.get_TradePolicyId(), tradePolicyDetail.get_InstrumentId());
		this._tradePolicyDetails.put(compositeKey, tradePolicyDetail);
	}

	public UISetting getUISetting(String objectId)
	{
		return (this._uiSettings.containsKey(objectId)) ? this._uiSettings.get(objectId) : null;
	}

	public void setUISettings()
	{
		//this._uiSettings.put(UISetting.accountStatusUiSetting,new UISetting(UISetting.accountStatusUiSetting,UISettingsManager.accountStatusUiSetting));
		this._uiSettings.put(UISetting.tradingPanelUiSetting, new UISetting(UISetting.tradingPanelUiSetting, UISettingsManager.tradingPanelUiSetting));
		this._uiSettings.put(UISetting.workingOrderListUiSetting, new UISetting(UISetting.workingOrderListUiSetting, UISettingsManager.workingOrderListUiSetting));
		this._uiSettings.put(UISetting.openOrderListUiSetting, new UISetting(UISetting.openOrderListUiSetting, UISettingsManager.openOrderListUiSetting));
	}

	public void initialize(TradingConsole tradingConsole, DataSet dataSet,Semaphore semaphore)
	{

		this._paymentInstructionRemarks.clear();
		Instrument.clearSummary();
		this._tradingConsole = tradingConsole;


		TradingConsole.traceSource.trace(TraceType.Information, "SettingsManager.initialize()");
		if (dataSet == null)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "SettingsManager.initialize() end");
			return;
		}
		this._merchantInfo = null;

		DataTableCollection tables = dataSet.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;

		dataTable = tables.get_Item("DeliveryHolidays");
		if (dataTable != null)
		{
			DeliveryHoliday.instance.initailize(dataTable);
		}

		dataTable = tables.get_Item("Instrument");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = new Guid(dataRow.get_Item("ID").toString());
				if (this._instruments.containsKey(id))
				{
					Instrument instrument = (Instrument)this._instruments.get(id);
					instrument.replace(dataRow);
				}
				else
				{
					Instrument instrument = new Instrument(this._tradingConsole, this, dataRow);
					this._instruments.put(id, instrument);
				}
			}
			fillGuidMapping(dataTable);
		}
		Instrument.fillInstrumentsToSubtotal(this._instruments.values());

		if (semaphore != null)
		{
			semaphore.release();
		}


		dataTable = tables.get_Item("Customer");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			dataRow = dataRowCollection.get_Item(0);
			this._customer.setValue(dataRow);
			this._tradingConsole.get_LoginInformation().set_Customer(this._customer);
			this._tradingConsole.get_MainForm().refreshLoginInformation();

			this._tradingConsole.get_MainForm().showLog();
		}

		dataTable = tables.get_Item("TradeDay");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			dataRow = dataRowCollection.get_Item(0);
			this._tradeDay.setValue(dataRow);
		}

		dataTable = tables.get_Item("SystemParameter");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			dataRow = dataRowCollection.get_Item(0);
			this._systemParameter.setValue(dataRow);
		}

		dataTable = tables.get_Item("BursaSystemParameter");
		if (dataTable != null)
		{
			dataRow = dataTable.get_Rows().get_Item(0);
			if (dataRow != null)
				this._bursaSystemParameter.setValue(dataRow);
		}

		/*//use local setting
		dataTable = tables.get_Item("Settings");
		if (dataTable != null)
		{
		 dataRowCollection = dataTable.get_Rows();
		 for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
		 {
		  dataRow = dataRowCollection.get_Item(rowIndex);

		  String objectId = dataRow.get_Item("ObjectID").toString();
		  if (this._uiSettings.containsKey(objectId))
		  {
		this.getUISetting(objectId).replace(dataRow);
		  }
		  else
		  {
		this._uiSettings.put(objectId, new UISetting(dataRow));
		  }
		 }
		}
		*/

	   dataTable = tables.get_Item("Currency");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("ID");
				if (this._currencies.containsKey(id))
				{
					this.getCurrency(id).replace(dataRow);
				}
				else
				{
					this._currencies.put(id, new Currency(dataRow));
				}
			}
		}

		dataTable = tables.get_Item("CurrencyRate");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				CompositeKey2<Guid, Guid> compositeKey =
					new CompositeKey2<Guid, Guid> ( (Guid)dataRow.get_Item("SourceCurrencyID"), (Guid)dataRow.get_Item("TargetCurrencyID"));
				if (this._currencyRates.containsKey(compositeKey))
				{
					( (CurrencyRate)this._currencyRates.get(compositeKey)).replace(dataRow);
				}
				else
				{
					this._currencyRates.put(compositeKey, new CurrencyRate(this, dataRow));
				}
			}
		}

		dataTable = tables.get_Item("TradePolicy");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = new Guid(dataRow.get_Item("ID").toString());
				if (this._tradePolicies.containsKey(id))
				{
					( (TradePolicy)this._tradePolicies.get(id)).replace(dataRow);
				}
				else
				{
					this._tradePolicies.put(id, new TradePolicy(dataRow));
				}
			}
		}

		dataTable = tables.get_Item("Account");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("ID");
				Account account;
				if (this._accounts.containsKey(id))
				{
					account = (Account)this._accounts.get(id);
					String oldCode = account.get_Code();
					account.replace(dataRow);
					this._organizationName = account.get_OrganizationName();
					if (this._tradingConsole.get_IsInitialized())
					{
						//account.updateCode(oldCode);
						TradingConsole.traceSource.trace(TraceType.Information, "account.updateNode");
						account.updateNode();
						TradingConsole.traceSource.trace(TraceType.Information, "account.updateNode end");
					}
				}
				else
				{
					account = new Account(this._tradingConsole, this, dataRow);
					this._organizationName = account.get_OrganizationName();
					this._accounts.put(id, account);
					//account.addNode();
					account.checkIsMustAssignOrderSchedulerStart();

					if (!this._isExistsAgentAccount && account.get_Type().equals(AccountType.Agent))
					{
						this._isExistsAgentAccount = true;
					}
				}
			}
		}





		dataTable = tables.get_Item("AccountAgentHistory");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				CompositeKey3<Guid, Guid, DateTime> compositeKey =
					new CompositeKey3<Guid, Guid,
									  DateTime> ( (Guid)dataRow.get_Item("AccountID"), (Guid)dataRow.get_Item("AgentAccountID"),
												 (DateTime)dataRow.get_Item("AgentBeginTime"));
				if (this._accountAgentHistories.containsKey(compositeKey))
				{
					( (AccountAgentHistory)this._accountAgentHistories.get(compositeKey)).replace(dataRow);
				}
				else
				{
					this._accountAgentHistories.put(compositeKey, new AccountAgentHistory(dataRow));
				}
			}
		}

		dataTable = tables.get_Item("AccountCurrency");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				AccountCurrency accountCurrency = null;
				CompositeKey2<Guid, Guid> compositeKey =
					new CompositeKey2<Guid, Guid> ( (Guid)dataRow.get_Item("AccountID"), (Guid)dataRow.get_Item("CurrencyID"));
				if (this._accountCurrencies.containsKey(compositeKey))
				{
					accountCurrency = (AccountCurrency)this._accountCurrencies.get(compositeKey);
					double oldUnclearAmount = accountCurrency.get_UnclearAmount();
					accountCurrency.replace(dataRow);
					if (!AppToolkit.isDBNull(dataRow.get_Item("UnclearAmount")))
					{
						double unclearAmount = AppToolkit.convertDBValueToDouble(dataRow.get_Item("UnclearAmount"), 0.0);
						unclearAmount = unclearAmount - oldUnclearAmount;
						accountCurrency.deltaUnclearAmount(unclearAmount);
					}
					if (this._tradingConsole.get_IsInitialized())
					{
						accountCurrency.updateNode();
					}
				}
				else
				{
					accountCurrency = new AccountCurrency(this, dataRow);
					this._accountCurrencies.put(compositeKey, accountCurrency);
					Account account = accountCurrency.get_Account();
					this._accounts.get(account.get_Id()).set_AccountCurrencies(compositeKey, accountCurrency);
					accountCurrency.deltaUnclearAmount();
					//accountCurrency.addNode();
				}
				//Remarked by Michael on 2008-04-09
				//accountCurrency.get_Account().calculateEquity();
			}
		}

		if (!this._tradingConsole.get_IsInitialized())
		{
			dataTable = tables.get_Item("Account");
			if (dataTable != null)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "account.addNode");
				dataRowCollection = dataTable.get_Rows();
				for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
				{
					dataRow = dataRowCollection.get_Item(rowIndex);
					Guid id = (Guid)dataRow.get_Item("ID");
					Account account = (Account)this._accounts.get(id);
					account.addNode();
					if (rowIndex == 0)
					{
						this._tradingConsole.setCurrentAccount(account);
					}
				}
				TradingConsole.traceSource.trace(TraceType.Information, "account.addNode end");
			}

			/*
				for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
				{
			 Account account = iterator.next();
			 account.addNode();
				}
			 */
			TradingConsole.traceSource.trace(TraceType.Information, "accountCurrency.addNode");
			for (Iterator<AccountCurrency> iterator = this._accountCurrencies.values().iterator(); iterator.hasNext(); )
			{
				AccountCurrency accountCurrency = iterator.next();
				accountCurrency.addNode();
			}
			TradingConsole.traceSource.trace(TraceType.Information, "accountCurrency.addNode end");
		}


		dataTable = tables.get_Item("TradePolicyDetail");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				CompositeKey2<Guid, Guid> compositeKey =
					new CompositeKey2<Guid, Guid> ( (Guid)dataRow.get_Item("TradePolicyID"), (Guid)dataRow.get_Item("InstrumentID"));
				if (this._tradePolicyDetails.containsKey(compositeKey))
				{
					( (TradePolicyDetail)this._tradePolicyDetails.get(compositeKey)).replace(dataRow);
				}
				else
				{
					this._tradePolicyDetails.put(compositeKey, new TradePolicyDetail(dataRow));
				}
			}
		}

		dataTable = tables.get_Item("ScrapInstrument");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				ScrapInstrument scrapInstrument = new ScrapInstrument(dataRow);
				this._scrapInstruments.put(scrapInstrument.get_Id(), scrapInstrument);
			}
		}

		dataTable = tables.get_Item("DeliveryCharge");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				DeliveryCharge deliveryCharge = DeliveryCharge.create(dataRow);
				this._deliveryCharges.put(deliveryCharge.get_Id(), deliveryCharge);
			}
		}

		dataTable = tables.get_Item("InstalmentPolicy");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				InstalmentPolicy instalmentPolicy = new InstalmentPolicy(dataRow);
				this._instalmentPolicys.put(instalmentPolicy.get_Id(), instalmentPolicy);
			}
		}

		dataTable = tables.get_Item("InstalmentPolicyDetail");
		if(dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				InstalmentPolicyDetail detail = new InstalmentPolicyDetail(dataRow);
				InstalmentPolicy instalmentPolicy = this._instalmentPolicys.get(detail.get_InstalmentPolicyId());
				instalmentPolicy.add(detail);
			}
		}

		dataTable = tables.get_Item("VolumeNecessary");
		if(dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				VolumeNecessary volumeNecessary = new VolumeNecessary(dataRow);
				this._volumeNecessaries.put(volumeNecessary.get_Id(), volumeNecessary);
			}
		}

		dataTable = tables.get_Item("VolumeNecessaryDetail");
		if(dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				VolumeNecessaryDetail volumeNecessaryDetail = new VolumeNecessaryDetail(dataRow);
				VolumeNecessary volumeNecessary
					= this._volumeNecessaries.get(volumeNecessaryDetail.get_VolumeNecessaryId());
				volumeNecessary.add(volumeNecessaryDetail);
			}
		}

		dataTable = tables.get_Item("DealingPolicyDetail");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				CompositeKey2<Guid, Guid> compositeKey =
					new CompositeKey2<Guid, Guid> ( (Guid) dataRow.get_Item("DealingPolicyID"), (Guid) dataRow.get_Item("InstrumentID"));
				if (this._dealingPolicyDetails.containsKey(compositeKey))
				{
					( (DealingPolicyDetail)this._dealingPolicyDetails.get(compositeKey)).replace(dataRow);
				}
				else
				{
					this._dealingPolicyDetails.put(compositeKey, new DealingPolicyDetail(dataRow));
				}
			}
		}

		dataTable = tables.get_Item("QuotePolicyDetail");
		if (dataTable != null)
		{
			this.updateQuotePolicyDetails(dataTable);
		}

		dataTable = tables.get_Item("PaymentInstructionRemark");
		if (dataTable != null)
		{
			this.updatePaymentInstructionRemark(dataTable);
		}


		TradingConsole.traceSource.trace(TraceType.Information, "SettingsManager.initialize() end");
	}


	private void fillGuidMapping(DataTable table)
	{
		DataRowCollection dataRowCollection = table.get_Rows();
		for (int row_index = 0; row_index < dataRowCollection.get_Count(); row_index++)
		{
			DataRow row = dataRowCollection.get_Item(row_index);
			Guid id = (Guid)row.get_Item("ID");
			String code = row.get_Item("Code").toString();
			int mappingId = (Integer)row.get_Item("SequenceForQuotatoin");
			String rowdataString = String.format("id: %s, code: %s, mappingId: %d", id.toString(), code, mappingId);
			System.out.println(rowdataString);
			GuidMapping.Default.add(mappingId, id);
		}
	}


	private HashMap<Guid, String> _paymentInstructionRemarks = new HashMap<Guid, String>();
	private void updatePaymentInstructionRemark(DataTable dataTable)
	{
		DataRowCollection dataRowCollection = dataTable.get_Rows();
		for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
		{
			DataRow dataRow = dataRowCollection.get_Item(rowIndex);
			Guid organizationId = (Guid)dataRow.get_Item("OrganizationId");
			String remark = (String)dataRow.get_Item("Remark");
			this._paymentInstructionRemarks.put(organizationId, remark);
		}
	}

	public String getPaymentInstructionRemark(Guid organizationId)
	{
		if(this._paymentInstructionRemarks.containsKey(organizationId))
		{
			return this._paymentInstructionRemarks.get(organizationId);
		}
		else
		{
			return null;
		}
	}

	public void setNews(DataSet dataSet)
	{
		if (dataSet == null)
		{
			return;
		}
		DataTableCollection tables = dataSet.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;
		dataTable = tables.get_Item("News");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				Guid id = (Guid) dataRow.get_Item("ID");
				if (this._newses.containsKey(id))
				{
					this.getNews(id).replace(dataRow);
				}
				else
				{
					this._newses.put(id, new News(dataRow));
				}
			}
		}
		this._isGotNews = true;
	}

	public void setMessages(DataSet dataSet)
	{
		if (dataSet == null)
		{
			return;
		}
		DataTableCollection tables = dataSet.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;
		dataTable = tables.get_Item("Messages");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid) dataRow.get_Item("ID");
				if (this._messages.containsKey(id))
				{
					this.getMessage(id).replace(dataRow);
				}
				else
				{
					this._messages.put(id, new Message(dataRow));
				}
			}
		}
		this._isGotMessage = true;
	}

	public boolean hasInstrumentOf(InstrumentCategory category)
	{
		for(Instrument instrument : this._instruments.values())
		{
			if(instrument.get_Category().equals(category)) return true;
		}
		return false;
	}

	public void accountAlert()
	{
		String alertAccountCodes = null;
		String willExpireAccountCodes = null;
		String expiredAccountCodes = null;

		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();

			if (account.isAlert())
			{
				alertAccountCodes = (alertAccountCodes == null) ? (account.get_Code()) : (alertAccountCodes + TradingConsole.enterLine + account.get_Code());
			}
			if (account.isWillExpire())
			{
				willExpireAccountCodes = (willExpireAccountCodes == null) ? (account.get_Code()) : (willExpireAccountCodes + TradingConsole.enterLine + account.get_Code());
			}
			if (account.isExpired())
			{
				expiredAccountCodes = (expiredAccountCodes == null) ? (account.get_Code()) : (expiredAccountCodes + TradingConsole.enterLine + account.get_Code());
			}
		}
		if (alertAccountCodes != null)
		{
			this._tradingConsole.accountMarginNotify(alertAccountCodes);
		}
		if (willExpireAccountCodes != null)
		{
			this._tradingConsole.accountWillExpireNotify(willExpireAccountCodes);
		}
		if (expiredAccountCodes != null)
		{
			this._tradingConsole.accountExpiredNotify(expiredAccountCodes);
		}
	}

	public void verifyAccountCurrency(Order order)
	{
		//Tree accountTree = this._tradingConsole.get_MainForm().get_AccountTable();

		Account account = order.get_Transaction().get_Account();
		Instrument instrument = order.get_Transaction().get_Instrument();

		Currency currency = (account.get_IsMultiCurrency()) ? instrument.get_Currency() : account.get_Currency();
		CompositeKey2<Guid, Guid> compositeKey =
			new CompositeKey2<Guid, Guid> (account.get_Id(), currency.get_Id());
		if (!this._accountCurrencies.containsKey(compositeKey))
		{
			AccountCurrency accountCurrency = new AccountCurrency(this, account.get_Id(), currency.get_Id());
			this.setAccountCurrency(compositeKey, accountCurrency);
			this._accounts.get(account.get_Id()).set_AccountCurrencies(compositeKey, accountCurrency);
			//accountCurrency.deltaUnclearAmount();
			accountCurrency.addNode();
			//Remarked by Michael on 2008-04-09
			//account.calculateEquity();
			account.updateNode();
		}
	}

	public void removeAccount(Account account)
	{
		account.removeNode();
		this._accounts.remove(account.get_Id());
		account.dispose();
	}

	public void removeInstrument(Instrument instrument)
	{
		this._instruments.remove(instrument.get_Id());
		this._tradingConsole.removeQuotation(instrument.get_Id());
		instrument.dispose();
	}

	public Guid getAgentAccountIDForLockAccount()
	{
		Guid userId = this._tradingConsole.get_LoginInformation().get_CustomerId();
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Type() == AccountType.Agent
				&& account.get_CustomerId().equals(userId))
			{
				return account.get_Id();
			}
		}
		return null;
	}

	public boolean isHasAgentOrder()
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Type() == AccountType.Agent)
			{
				for (Iterator<Transaction> iterator2 = account.get_Transactions().values().iterator(); iterator2.hasNext(); )
				{
					Transaction transaction = iterator2.next();
					if (transaction.isAgentTransaction())
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public void getUpdateAccountLockResult(Guid agentAccountId, HashMap<Guid, Boolean> accountLocks)
	{
		DataTable accountAgentHistoryDataTable = AccountAgentHistory.createStructure();

		DateTime appTime = TradingConsoleServer.appTime();
		for (Iterator<Guid> iterator = accountLocks.keySet().iterator(); iterator.hasNext(); )
		{
			Guid accountId = iterator.next();
			boolean isLocked = accountLocks.get(accountId).booleanValue();

			if (isLocked)
			{
				DataRow accountAgentHistoryDataRow = accountAgentHistoryDataTable.newRow();
				accountAgentHistoryDataRow.set_Item("AccountID", accountId);
				accountAgentHistoryDataRow.set_Item("AgentAccountID", agentAccountId);
				accountAgentHistoryDataRow.set_Item("AgentBeginTime", appTime);
				accountAgentHistoryDataRow.set_Item("AgentEndTime", appTime);

				CompositeKey3<Guid, Guid, DateTime> compositeKey =
					new CompositeKey3<Guid, Guid, DateTime> (accountId, agentAccountId, appTime);
				this._accountAgentHistories.put(compositeKey, new AccountAgentHistory(accountAgentHistoryDataRow));
			}
			else
			{
				for (Iterator<AccountAgentHistory> iterator2 = this._accountAgentHistories.values().iterator(); iterator2.hasNext(); )
				{
					AccountAgentHistory accountAgentHistory = iterator2.next();
					if (accountAgentHistory.get_AccountId().equals(accountId)
						&& accountAgentHistory.get_AgentAccountId().equals(agentAccountId))
					{
						DataRow accountAgentHistoryDataRow = accountAgentHistoryDataTable.newRow();
						accountAgentHistoryDataRow.set_Item("AccountID", accountId);
						accountAgentHistoryDataRow.set_Item("AgentAccountID", agentAccountId);
						accountAgentHistoryDataRow.set_Item("AgentBeginTime", accountAgentHistory.get_AgentEndTime());
						accountAgentHistoryDataRow.set_Item("AgentEndTime", appTime);

						accountAgentHistory.replace(accountAgentHistoryDataRow);
					}
				}
			}
			Account account = this.getAccount(accountId);
			if (account == null)
			{
				continue;
			}
			account.set_IsLocked(isLocked);
			account.updateAccountStatus();
			this._tradingConsole.get_MainForm().get_AccountTable().doLayout();
		}
	}

	public void setInterestRate(Guid interestRateId, BigDecimal interestRateBuy, BigDecimal interestRateSell)
	{
		for (Iterator<TradePolicyDetail> iterator2 = this._tradePolicyDetails.values().iterator(); iterator2.hasNext(); )
		{
			TradePolicyDetail tradePolicyDetail = iterator2.next();
			if(tradePolicyDetail==null || tradePolicyDetail.get_InterestRateId() == null){
				continue;
			}
			if (tradePolicyDetail.get_InterestRateId().equals(interestRateId))
			{
				tradePolicyDetail.set_InterestRateBuy(interestRateBuy);
				tradePolicyDetail.set_InterestRateSell(interestRateSell);
			}
		}
		//refresh Intrument Ui........
	}

	//region-----------------------------------------------------------------------------------------
	////Get Cut Orders when Necessary * TradePolicy.AlertLevel3 >= Equity & All Instruments's Executed Order: SumLotBalanceForBuy != SumLotBalanceForSell

	//Test
	public void getAccountsForCut()
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.getAccountsForCut();
		}
	}

	public void getAccountsForCutSchedulerStart()
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.getAccountsForCutSchedulerStart();
		}
	}

	public void getAccountsForCutSchedulerStop()
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.getAccountsForCutSchedulerStop();
		}
	}

	//Added by Michael on 2008-04-09
	public void resetCheckArrivedAlertLevel3()
	{
		for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.resetCheckArrivedAlertLevel3();
		}
	}

	public void setQuotePolicyDetail(QuotePolicyDetail quotePolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (quotePolicyDetail.get_QuotePolicyId(), quotePolicyDetail.get_InstrumentId());
		this._quotePolicyDetails.put(compositeKey, quotePolicyDetail);
	}

	public void removeTradePolicyDetail(QuotePolicyDetail quotePolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (quotePolicyDetail.get_QuotePolicyId(), quotePolicyDetail.get_InstrumentId());
		this._quotePolicyDetails.remove(compositeKey);
	}

	public void updateQuotePolicyDetails(DataTable quotePolicyDetails)
	{
		DataRowCollection dataRowCollection = quotePolicyDetails.get_Rows();
		for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
		{
			DataRow dataRow = dataRowCollection.get_Item(rowIndex);

			CompositeKey2<Guid, Guid> compositeKey =
				new CompositeKey2<Guid, Guid> ( (Guid) dataRow.get_Item("QuotePolicyID"), (Guid) dataRow.get_Item("InstrumentID"));
			if (this._quotePolicyDetails.containsKey(compositeKey))
			{
				( (QuotePolicyDetail)this._quotePolicyDetails.get(compositeKey)).replace(dataRow);
			}
			else
			{
				this._quotePolicyDetails.put(compositeKey, new QuotePolicyDetail(dataRow));
			}
		}
	}

	public DealingPolicyDetail getDealingPolicyDetail(Guid dealingPolicyId, Guid instrumentId)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (dealingPolicyId, instrumentId);
		return (this._dealingPolicyDetails.containsKey(compositeKey)) ? this._dealingPolicyDetails.get(compositeKey) : null;
	}

	public void setDealingPolicyDetail(DealingPolicyDetail dealingPolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (dealingPolicyDetail.get_DealingPolicyId(), dealingPolicyDetail.get_InstrumentId());
		this._dealingPolicyDetails.put(compositeKey, dealingPolicyDetail);
	}

	public void removeDealingPolicyDetail(DealingPolicyDetail dealingPolicyDetail)
	{
		CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (dealingPolicyDetail.get_DealingPolicyId(), dealingPolicyDetail.get_InstrumentId());
		if(this._dealingPolicyDetails.containsKey(compositeKey))
		{
			this._dealingPolicyDetails.remove(compositeKey);
		}
	}

	public boolean shouldEnableMarginButton()
	{
		for(Account account : this._accounts.values())
		{
			if(account.get_SupportMarginAction()) return true;
		}
		return false;
	}

	public String get_TraderName()
	{
		String language = PublicParametersManager.version;
		if(language.equalsIgnoreCase("CHT"))
		{
			return this.get_SystemParameter().get_TraderNameInTraditionalChinese();
		}
		else if(language.equalsIgnoreCase("CHS"))
		{
			return this.get_SystemParameter().get_TraderNameInSimplifiedChinese();
		}
		else
		{
			return this.get_SystemParameter().get_TraderNameInEnglish();
		}
	}

	HashMap<Guid, Guid[]> _organizationToAccounts = new HashMap<Guid,Guid[]>();
	public Guid[] get_AccountsByOrganization(Guid organizationId)
	{
		if(!this._organizationToAccounts.containsKey(organizationId))
		{
			ArrayList<Guid> accountList = new ArrayList<Guid>();
			for(Account account : this._accounts.values())
			{
				if(account.get_OrganizationId().equals(organizationId))
				{
					accountList.add(account.get_Id());
				}
			}
			Guid[] accounts = new Guid[accountList.size()];
			accounts = accountList.toArray(accounts);
			this._organizationToAccounts.put(organizationId, accounts);
		}
		return this._organizationToAccounts.get(organizationId);
	}

	private BankFor99Bill[] _banksFor99Bill=null;
	public BankFor99Bill[] getBanksFor99Bill()
	{
		if(this._banksFor99Bill == null)
		{
			String[] banks = this._tradingConsole.get_TradingConsoleServer().get99BillBanks();
			this._banksFor99Bill = new BankFor99Bill[banks.length];

			int index = 0;
			for(String bank : banks)
			{
				String[] items = StringHelper.split(bank, '|');
				this._banksFor99Bill[index++] = new BankFor99Bill(items[0], items[1]);
			}
		}
		return this._banksFor99Bill;
	}

	private HashMap<Guid, MerchantInfoFor99Bill> _merchantInfo = null;
	public boolean existsMerchantIdFor99Bill(Account account)
	{
		TradingConsole.traceSource.trace(TraceType.Error, "existsMerchantIdFor99Bill");

		if(_merchantInfo == null)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "getMerchantInfoFor99Bill");

			Guid[] organizationIds = new Guid[this._accounts.size()];
			int index = 0;
		 	for(Account item : this._accounts.values())
			 {
				 organizationIds[index++] = item.get_OrganizationId();
			 }
			String[] merchantInfos = this._tradingConsole.get_TradingConsoleServer().getMerchantInfoFor99Bill(organizationIds);

			this._merchantInfo = new HashMap<Guid, MerchantInfoFor99Bill>();
			if(merchantInfos != null)
			{
				for (String merchantInfo : merchantInfos)
				{
					TradingConsole.traceSource.trace(TraceType.Error, "getMerchantInfoFor99Bill: " + merchantInfo);

					String[] items = StringHelper.split(merchantInfo, "|");
					MerchantInfoFor99Bill merchantInfoFor99Bill = new MerchantInfoFor99Bill(items[0], items[1], new Guid(items[2]));
					this._merchantInfo.put(merchantInfoFor99Bill.get_OrganizationId(), merchantInfoFor99Bill);
				}
			}
		}
		Guid organizationId = account.get_OrganizationId();

		TradingConsole.traceSource.trace(TraceType.Error, "getMerchantInfoFor99Bill: " + (this._merchantInfo.containsKey(organizationId) ? " contains " :" doesn't contains ") + organizationId);

		return this._merchantInfo.containsKey(organizationId);
	}

	public MerchantInfoFor99Bill getMerchantIdFor99Bill(Account account)
	{
		Guid organizationId = account.get_OrganizationId();
		if(this._merchantInfo.containsKey(organizationId))
		{
			return this._merchantInfo.get(organizationId);
		}
		else
		{
			return null;
		}
	}

	public Collection<Currency> get_Currencies()
	{
		return this._currencies.values();
	}

	public ScrapInstrument getScrapInstrument(Guid scrapInstrumentId)
	{
		return this._scrapInstruments.get(scrapInstrumentId);
	}

	public InstalmentPolicy getInstalmentPolicy(Guid instalmentPolicyId)
	{
		return this._instalmentPolicys.containsKey(instalmentPolicyId) ? this._instalmentPolicys.get(instalmentPolicyId) : null;
	}

	public boolean containsDeliveryCharge(Guid deliveryChargeId)
	{
		return this._deliveryCharges.containsKey(deliveryChargeId);
	}

	public boolean containsInstalmentPolicy(Guid instalmentPolicyId)
	{
		return this._instalmentPolicys.containsKey(instalmentPolicyId);
	}

	public DeliveryCharge getDeliveryCharge(Guid deliveryChargeId)
	{
		return this._deliveryCharges.get(deliveryChargeId);
	}

	public void replaceDealingPolicyDetails(DataTable dataTable)
	{
		this._dealingPolicyDetails.clear();
		for(int index = 0; index < dataTable.get_Rows().get_Count(); index++)
		{
			DataRow dataRow = dataTable.get_Rows().get_Item(index);
			CompositeKey2<Guid, Guid> compositeKey =
				new CompositeKey2<Guid, Guid> ( (Guid)dataRow.get_Item("DealingPolicyID"), (Guid)dataRow.get_Item("InstrumentID"));
			this._dealingPolicyDetails.put(compositeKey, new DealingPolicyDetail(dataRow));
		}
	}

	public void getClearDealingPolicyDetails()
	{
		this._dealingPolicyDetails.clear();
	}

	public VolumeNecessary getVolumeNecessary(Guid id)
	{
		return this._volumeNecessaries.get(id);
	}

	public void addVolumeNecessary(VolumeNecessary volumeNecessary)
	{
		this._volumeNecessaries.put(volumeNecessary.get_Id(), volumeNecessary);
	}

	public void addDeliveryCharge(DeliveryCharge deliveryCharge)
	{
		this._deliveryCharges.put(deliveryCharge.get_Id(), deliveryCharge);
	}

	public void updateInstalmentPolicy(XmlNode xmlNode, String updateType)
	{
		if (updateType.equals("Modify"))
		{
			XmlAttributeCollection attributes = xmlNode.get_Attributes();
			Guid instalmentPolicyId = new Guid(attributes.get_ItemOf("ID").get_Value());
			if(this._instalmentPolicys.containsKey(instalmentPolicyId))
			{
				InstalmentPolicy instalmentPolicy = this._instalmentPolicys.get(instalmentPolicyId);
				instalmentPolicy.update(xmlNode);
			}
		}
		else if (updateType.equals("Delete"))
		{
			XmlAttributeCollection attributes = xmlNode.get_Attributes();
			Guid instalmentPolicyId = new Guid(attributes.get_ItemOf("Id").get_Value());
			this._instalmentPolicys.remove(instalmentPolicyId);
		}
	}

	public void updateInstalmentPolicyDetail(XmlNode xmlNode, String updateType)
	{
		if (updateType.equals("Add"))
		{
			InstalmentPolicyDetail instalmentPolicyDetail = new InstalmentPolicyDetail(xmlNode);
			if(this._instalmentPolicys.containsKey(instalmentPolicyDetail.get_InstalmentPolicyId()))
			{
				InstalmentPolicy instalmentPolicy
					= this._instalmentPolicys.get(instalmentPolicyDetail.get_InstalmentPolicyId());
				instalmentPolicy.add(instalmentPolicyDetail);
			}
		}
		else if (updateType.equals("Modify"))
		{
			XmlAttributeCollection attributes = xmlNode.get_Attributes();
			Guid instalmentPolicyId = new Guid(attributes.get_ItemOf("InstalmentPolicyId").get_Value());
			int period = Integer.parseInt(attributes.get_ItemOf("Period").get_Value());
			InstalmentFrequence frequence = Enum.valueOf(InstalmentFrequence.class, Integer.parseInt(attributes.get_ItemOf("Frequence").get_Value()));
			if(this._instalmentPolicys.containsKey(instalmentPolicyId))
			{
				InstalmentPolicy instalmentPolicy = this._instalmentPolicys.get(instalmentPolicyId);
				instalmentPolicy.get_InstalmentPolicyDetail(InstalmentPeriod.create(period, frequence)).update(xmlNode);
			}
		}
		else if (updateType.equals("Delete"))
		{
			XmlAttributeCollection attributes = xmlNode.get_Attributes();
			Guid instalmentPolicyId = new Guid(attributes.get_ItemOf("InstalmentPolicyId").get_Value());
			int period = Integer.parseInt(attributes.get_ItemOf("Period").get_Value());
			InstalmentFrequence frequence = Enum.valueOf(InstalmentFrequence.class, Integer.parseInt(attributes.get_ItemOf("InstalmentFrequence").get_Value()));
			if(this._instalmentPolicys.containsKey(instalmentPolicyId))
			{
				InstalmentPolicy instalmentPolicy = this._instalmentPolicys.get(instalmentPolicyId);
				instalmentPolicy.remove(InstalmentPeriod.create(period, frequence));
			}
		}
	}

	//Remarked by Michael on 2008-04-09
	/*
	 public void checkAccountsForCut()
	 {
	 for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
	 {
	  Account account = iterator.next();
	  account.set_CanCheckAccountsForCut(true);
	 }
	 }
	 */

	/*
	 //Added by Michael on 2008-04-09
	 public void getAccountsForCut()
	 {
	   ArrayList<Guid> accountIds = new ArrayList<Guid>();
	   for (Iterator<Account> iterator = this._accounts.values().iterator(); iterator.hasNext(); )
	   {
		Account account = iterator.next();

		if (account.needGetAccountsForCutOrder())
		{
		 accountIds.add(account.get_Id());
		}
	   }
	   if (accountIds.size() > 0)
	   {
		accountIds.toArray();
		Guid[] accountIds2 = new Guid[accountIds.size()];
		accountIds.toArray(accountIds2);
		XmlNode accountXmlNode = this._tradingConsole.get_TradingConsoleServer().getAccountsForCut(accountIds2, true);
		this._tradingConsole.fixData(accountXmlNode);
	   }
	 }
	 //endregion----------------------------------------------------------------------------------------------------------
	 */
}
