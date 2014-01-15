package tradingConsole;

import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.threading.*;
import framework.xml.*;
import tradingConsole.enumDefine.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.language.*;

public class Account implements Scheduler.ISchedulerCallback
{
	public static ComparatorByCode comparatorByCode = new ComparatorByCode();
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private String _code;
	private String _name;
	private String _organizationCode;
	private String _organizationName;
	private Guid _organizationId;
	private long _bankAccountDefaultCountryId;
	private int _supportMarginAction;
	private boolean _enablePICash;
	private boolean _enablePIInterACTransfer;
	private boolean _enablePI;
	private boolean _enableCMExtension;
	private boolean _enableAgentRegistration;
	private boolean _enableOwnerRegistration;

	private String _organizationEmail;
	private String _organizationPhone;
	private String _agentEmail;
	private String _customerEmail;
	private BigDecimal _rateLotMin;
	private BigDecimal _rateLotMultiplier;
	private BigDecimal _rateDefaultLot;
	private AccountType _type;
	private boolean _isAutoClose;
	private boolean _isMultiCurrency;
	private Guid _customerId;
	private double _rateCommission;
	private Guid _agentId;
	private String _agentName;
	private String _agentCode;
	private boolean _isTradingAllowed;
	private DateTime _beginTime;
	private DateTime _endTime;
	private Guid _tradePolicyId;
	private boolean _isLocked;
	private Guid _groupId;
	private AlertLevel _alertLevel;
	private UserRelation _userRelation;
	private boolean _allowManagerTrading;
	private boolean _allowSalesTrading;
	private boolean _isTradingAllowedOnAccount;
	private boolean _allowChangePasswordInTrader;
	private boolean _needBeneficiaryInPaymentInstruction;

	private double _balance;
	private double _necessary;
	private double _equity;
	private double _frozenFund;
	private double _totalPaidAmount;
	private double _necessaryForPartialPaymentPhysicalOrder;

	private double _creditAmount;
	private double _unclearAmount;

	private BigDecimal _rateMarginD;
	private BigDecimal _rateMarginO;
	private BigDecimal _rateMarginLockD;
	private BigDecimal _rateMarginLockO;
	private Integer _leverage;

	private TradingItem _notValuedTradingItem;
	private TradingItem _floatTradingItem;

	private boolean _select;
	private boolean _isActive;

	private boolean _allowAddNewPosition;

	private HashMap<Guid, Transaction> _transactions;
	private Currency _currency;
	private HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> _accountCurrencies;

	private Scheduler.SchedulerEntry _schedulerEntryForCheckIsMustAssignOrder;
	public static final String checkIsMustAssignOrder = "CheckIsMustAssignOrder";

	//Scheduler.SchedulerCallback.ActionsetDealStatus
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		SwingUtilities.invokeLater(new SwingSafelyAction(this, schedulerEntry));
	}

	private static class SwingSafelyAction implements Runnable
	{
		private Account _owner;
		private Scheduler.SchedulerEntry _schedulerEntry;

		public SwingSafelyAction(Account owner, Scheduler.SchedulerEntry schedulerEntry)
		{
			this._owner = owner;
			this._schedulerEntry = schedulerEntry;
		}

		public void run()
		{
			if (this._schedulerEntry.get_IsRemoved())
			{
				return;
			}

			if (this._schedulerEntry.get_ActionArgs().equals(Account.checkIsMustAssignOrder))
			{
				this._owner.checkIsMustAssignOrder();
			}
			else if (this._schedulerEntry.get_ActionArgs().equals(Account._actionArgsGetAccountsForCut))
			{
				this._owner.getAccountsForCut();
			}
		}
	}

	public boolean get_VerifiedCustomerIdentity()
	{
		return this._tradingConsole.get_TradingAccountManager().isActivatedAccount(this._id);
	}

	public boolean get_CanDisplay()
	{
		boolean verifiedCustomerIdentity = this.get_VerifiedCustomerIdentity();
		return verifiedCustomerIdentity && this._select;
	}

	public boolean get_Select()
	{
		return this._select;
	}

	public void set_Select(boolean value)
	{
		this._select = value;
		//??????
		this._tradingConsole.get_MainForm().get_OpenOrderTable().filter();
		this._tradingConsole.get_MainForm().get_OrderTable().filter();
		this._tradingConsole.get_MainForm().get_NotConfirmedPendingOrderTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalInventoryTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalPendingInventoryTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalShotSellTable().filter();
		this.updateNode();
		this._settingsManager.calculateSummary();
	}

	public String get_Name()
	{
		return this._name;
	}

	public String get_OrganizationCode()
	{
		return this._organizationCode;
	}

	public boolean get_SupportMarginAction()
	{
		return this._enablePICash || this._enablePIInterACTransfer || this._enablePI
			|| this._enableCMExtension || this._enableAgentRegistration || this._enableOwnerRegistration;
	}

	public boolean get_EnablePICash()
	{
		return this._enablePICash;
	}

	public boolean get_EnablePIInterACTransfer()
	{
		return this._enablePIInterACTransfer;
	}

	public boolean get_EnablePI()
	{
		return this._enablePI;
	}

	public boolean get_EnableCMExtension()
	{
		return this._enableCMExtension;
	}

	public boolean get_EnableAgentRegistration()
	{
		return this._enableAgentRegistration;
	}

	public boolean get_EnableOwnerRegistration()
	{
		return this._enableOwnerRegistration;
	}

	public String get_OrganizationName()
	{
		return this._organizationName;
	}

	public Guid get_OrganizationId()
	{
		return this._organizationId;
	}

	public long get_BankAccountDefaultCountryId()
	{
		return this._bankAccountDefaultCountryId;
	}

	public String get_OrganizationEmail()
	{
		return this._organizationEmail;
	}

	public String get_OrganizationPhone()
	{
		return this._organizationPhone;
	}

	public String get_AgentEmail()
	{
		return this._agentEmail;
	}

	public String get_CustomerEmail()
	{
		return this._customerEmail;
	}

	public boolean get_IsAutoClose()
	{
		return this._isAutoClose;
	}

	public BigDecimal get_RateLotMin()
	{
		return this._rateLotMin;
	}

	public BigDecimal get_RateMarginD()
	{
		return this._rateMarginD;
	}

	public BigDecimal get_RateMarginO()
	{
		return this._rateMarginO;
	}

	public BigDecimal get_RateMarginLockD()
	{
		return this._rateMarginLockD;
	}

	public BigDecimal get_RateMarginLockO()
	{
		return this._rateMarginLockO;
	}

	public Integer get_Leverage()
	{
		return this._leverage;
	}


	public BigDecimal get_RateLotMultiplier()
	{
		return this._rateLotMultiplier;
	}

	public BigDecimal get_RateDefaultLot()
	{
		return this._rateDefaultLot;
	}

	public boolean get_IsLocked()
	{
		return this._isLocked;
	}

	public void set_IsLocked(boolean value)
	{
		this._isLocked = value;
	}

	public Guid get_CustomerId()
	{
		return this._customerId;
	}

	public Guid get_AgentId()
	{
		return this._agentId;
	}

	public String get_AgentName()
	{
		return this._agentName;
	}

	public UserRelation get_UserRelation()
	{
		return this._userRelation;
	}

	public boolean get_AllowManagerTrading()
	{
		return this._allowManagerTrading;
	}

	public boolean get_IsTradingAllowedOnAccount()
	{
		return this._isTradingAllowedOnAccount;
	}

	public boolean get_AllowSalesTrading()
	{
		return this._allowSalesTrading;
	}


	public String get_AgentCode()
	{
		return this._agentCode;
	}

	public void set_CustomerEmail(String value)
	{
		this._customerEmail = value;
	}

	public boolean get_AllowChangePasswordInTrader()
	{
		return this._allowChangePasswordInTrader;
	}

	public boolean get_NeedBeneficiaryInPaymentInstruction()
	{
		return this._needBeneficiaryInPaymentInstruction;
	}

	public void set_AgentEmail(String value)
	{
		this._agentEmail = value;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Code()
	{
		return this._code;
	}

	public String getCode()
	{
		if(this.get_TradingConsole().get_SettingsManager().get_SystemParameter().get_ShowAccountName())
		{
			return this.get_Code() + " (" + this.get_Name() + ")";
		}
		else
		{
			return this.get_Code();
		}

	}

	public Guid get_GroupId()
	{
		return this._groupId;
	}

	public AccountType get_Type()
	{
		return this._type;
	}

	public boolean get_IsMultiCurrency()
	{
		return this._isMultiCurrency;
	}

	public DateTime get_EndTime()
	{
		return this._endTime;
	}

	public double get_Balance()
	{
		return this._balance;
	}

	public void set_Balance(double value)
	{
		this._balance = value;
	}

	public double get_Necessary()
	{
		return this._necessary;
	}

	public void set_Necessary(double value)
	{
		this._necessary = value;
	}

	public double get_NecessaryForPartialPaymentPhysicalOrder()
	{
		return this._necessaryForPartialPaymentPhysicalOrder;
	}

	public void set_NecessaryForPartialPaymentPhysicalOrder(double value)
	{
		this._necessaryForPartialPaymentPhysicalOrder = value;
	}

	public double get_FrozenFund()
	{
		return this._frozenFund;
	}

	public void set_FrozenFund(double value)
	{
		this._frozenFund = value;
	}

	public double get_TotalPaidAmount()
	{
		return this._totalPaidAmount;
	}

	public void set_TotalPaidAmount(double value)
	{
		this._totalPaidAmount = value;
	}

	public double get_Equity()
	{
		return this._equity;
	}

	public void set_Equity(double equity)
	{
		this._equity = equity;
	}

	public double get_UnclearAmount()
	{
		return this._unclearAmount;
	}

	public void set_UnclearAmount(double value)
	{
		this._unclearAmount = value;
	}

	public double get_CreditAmount()
	{
		return this._creditAmount;
	}

	public AlertLevel get_AlertLevel()
	{
		return this._alertLevel;
	}

	public double get_RateCommission()
	{
		return this._rateCommission;
	}

	public Guid get_TradePolicyId()
	{
		return this._tradePolicyId;
	}

	public TradingItem get_NotValuedTradingItem()
	{
		return this._notValuedTradingItem;
	}

	public void set_NotValuedTradingItem(TradingItem value)
	{
		this._notValuedTradingItem = value;
	}

	public TradingConsole get_TradingConsole()
	{
		return this._tradingConsole;
	}

	public TradingItem get_FloatTradingItem()
	{
		return this._floatTradingItem;
	}

	public void set_FloatTradingItem(TradingItem value)
	{
		this._floatTradingItem = value;
	}

	public void clearFloatTradingItem()
	{
		this._floatTradingItem.clear();
	}

	public HashMap<Guid, Transaction> get_Transactions()
	{
		//outer fill them
		return this._transactions;
	}

	public Currency get_Currency()
	{
		return this._currency;
	}

	public boolean get_AllowAddNewPosition()
	{
		return this._allowAddNewPosition;
	}

	public HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> get_AccountCurrencies()
	{
		return this._accountCurrencies;
	}

	public void set_AccountCurrencies(CompositeKey2<Guid, Guid> compositeKey, AccountCurrency accountCurrency)
	{
		//outer fill them
		this._accountCurrencies.put(compositeKey, accountCurrency);
	}

	public void set_AccountCurrencies(Guid accountId, Guid currencyId, AccountCurrency accountCurrency)
	{
		//outer fill them
		CompositeKey2<Guid, Guid> compositeKey =
			new CompositeKey2<Guid, Guid> (accountId, currencyId);
		this._accountCurrencies.put(compositeKey, accountCurrency);
	}

	public Color getAlertLevelColor()
	{
		return AlertLevel.getColor(this._alertLevel);
	}

	public String getAlertLevelCaption()
	{
		return AlertLevel.getCaption(this._alertLevel);
	}

	private Account()
	{
		this._notValuedTradingItem = TradingItem.create();
		this._floatTradingItem = TradingItem.create();

		this._transactions = new HashMap<Guid, Transaction> ();
		this._accountCurrencies = new HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> ();
		this._currency = null;
	}

	public Account(TradingConsole tradingConsole,SettingsManager settingsManager, DataRow dataRow)
	{
		this();

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._id = (Guid) dataRow.get_Item("ID");
		this.setValue(dataRow);
		this._equity = 0.00;

		this._select = true;
		this._isActive = true;

		Guid currencyId = (Guid) dataRow.get_Item("CurrencyID");
		this._currency = this._settingsManager.getCurrency(currencyId);

		//Remarked by Michael on 2008-04-09
		//this.calculateEquity();
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._customerEmail = AppToolkit.isDBNull(dataRow.get_Item("CustomerEmail")) ? null : (String) dataRow.get_Item("CustomerEmail");
		this._code = (String) dataRow.get_Item("Code");
		this._name = (String) dataRow.get_Item("Name");
		this._organizationCode = (String) dataRow.get_Item("OrganizationCode");
		this._organizationName = (String) dataRow.get_Item("OrganizationName");
		this._organizationId = (Guid)dataRow.get_Item("OrganizationID");

		this._enableAgentRegistration = ((Boolean)dataRow.get_Item("EnableAgentRegistration")).booleanValue();
		this._enableCMExtension = ((Boolean)dataRow.get_Item("EnableCMExtension")).booleanValue();
		this._enableOwnerRegistration = ((Boolean)dataRow.get_Item("EnableOwnerRegistration")).booleanValue();
		this._enablePI = ((Boolean)dataRow.get_Item("EnablePI")).booleanValue();
		this._enablePICash = ((Boolean)dataRow.get_Item("EnablePICash")).booleanValue();
		this._enablePIInterACTransfer = ((Boolean)dataRow.get_Item("EnablePIInterACTransfer")).booleanValue();
		if (dataRow.get_Table().get_Columns().contains("AllowChangePasswordInTrader"))
		{
			this._allowChangePasswordInTrader = ( (Boolean)dataRow.get_Item("AllowChangePasswordInTrader")).booleanValue();
		}
		if (dataRow.get_Table().get_Columns().contains("NeedBeneficiaryInPaymentInstruction"))
		{
			this._needBeneficiaryInPaymentInstruction = ( (Boolean)dataRow.get_Item("NeedBeneficiaryInPaymentInstruction")).booleanValue();
		}
		this._rateLotMin = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateLotMin"), 1);
		this._rateLotMultiplier = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateLotMultiplier"), 1);
		this._rateDefaultLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateDefaultLot"), 1);

		if (dataRow.get_Table().get_Columns().contains("RateMarginD"))
		{
			this._rateMarginD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateMarginD"), 1);
		}
		if (dataRow.get_Table().get_Columns().contains("RateMarginO"))
		{
			this._rateMarginO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateMarginO"), 1);
		}
		if (dataRow.get_Table().get_Columns().contains("RateMarginLockD"))
		{
			this._rateMarginLockD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateMarginLockD"), 1);
		}
		if (dataRow.get_Table().get_Columns().contains("RateMarginLockO"))
		{
			this._rateMarginLockO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("RateMarginLockO"), 1);
		}



		if (dataRow.get_Table().get_Columns().contains("Leverage"))
		{
			this._leverage = dataRow.get_Item("Leverage") == DBNull.value ? null : (Integer)dataRow.get_Item("Leverage");
		}

		if (dataRow.get_Table().get_Columns().contains("BankAccountDefaultCountryId"))
		{
			this._bankAccountDefaultCountryId = -1;
			Object value = dataRow.get_Item("BankAccountDefaultCountryId");
			if(value != DBNull.value) this._bankAccountDefaultCountryId = (Long)value;
		}

		this._type = Enum.valueOf(AccountType.class, (Integer) dataRow.get_Item("Type"));
		this._isAutoClose = (Boolean) dataRow.get_Item("IsAutoClose");
		this._isMultiCurrency = (Boolean) dataRow.get_Item("IsMultiCurrency");
		this._allowAddNewPosition = (Boolean) dataRow.get_Item("AllowAddNewPosition");
		this._customerId = (Guid) dataRow.get_Item("CustomerID");
		this._rateCommission = AppToolkit.convertDBValueToDouble(dataRow.get_Item("RateCommission"),1.0);
		this._agentId = AppToolkit.isDBNull(dataRow.get_Item("AgentID")) ? null : (Guid) dataRow.get_Item("AgentID");
		this._agentName = AppToolkit.isDBNull(dataRow.get_Item("AgentName")) ? "" : (String) dataRow.get_Item("AgentName");
		this._agentCode = AppToolkit.isDBNull(dataRow.get_Item("AgentCode")) ? "" : (String) dataRow.get_Item("AgentCode");

		this._agentEmail = AppToolkit.isDBNull(dataRow.get_Item("AgentEmail")) ? null : (String) dataRow.get_Item("AgentEmail");
		this._isTradingAllowed = (Boolean) dataRow.get_Item("IsTradingAllowed");
		this._beginTime = (DateTime) dataRow.get_Item("BeginTime");
		this._endTime = (DateTime) dataRow.get_Item("EndTime");
		this._isLocked = (Boolean) dataRow.get_Item("IsLocked");
		this._groupId = (Guid) dataRow.get_Item("GroupID");
		this._tradePolicyId = (Guid) dataRow.get_Item("TradePolicyID");
		this._creditAmount = AppToolkit.convertDBValueToDouble(dataRow.get_Item("CreditAmount"),0.0);

		this._balance = AppToolkit.convertDBValueToDouble(dataRow.get_Item("Balance"),0.0);
		this._necessary = AppToolkit.convertDBValueToDouble(dataRow.get_Item("Necessary"),0.0);

		this._frozenFund = AppToolkit.convertDBValueToDouble(dataRow.get_Item("FrozenFund"), 0.0);
		this._totalPaidAmount = AppToolkit.convertDBValueToDouble(dataRow.get_Item("TotalPaidAmount"), 0.0);

		this._notValuedTradingItem.set_Interest(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLNotValued"),0.0));
		this._notValuedTradingItem.set_Storage(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLNotValued"),0.0));
		this._notValuedTradingItem.set_Trade(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePLNotValued"),0.0));

		this._floatTradingItem.set_Interest(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLFloat"),0.0));
		this._floatTradingItem.set_Storage(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLFloat"),0.0));
		this._floatTradingItem.set_Trade(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePLFloat"),0.0));
		this._floatTradingItem.set_ValueAsMargin(AppToolkit.convertDBValueToDouble(dataRow.get_Item("ValueAsMargin"),0.0));

		if (AppToolkit.isDBNull(dataRow.get_Item("AlertLevel")))
		{
			this._alertLevel = AlertLevel.AlertLevel0;
		}
		else
		{
			this._alertLevel = Enum.valueOf(AlertLevel.class, (Integer)dataRow.get_Item("AlertLevel"));
		}

		this._userRelation = Enum.valueOf(UserRelation.class, (Integer)dataRow.get_Item("UserRelation"));
		//this._allowManagerTrading = ((Boolean)dataRow.get_Item("AllowManagerTrading")).booleanValue();
		//this._allowSalesTrading = ((Boolean)dataRow.get_Item("AllowSalesTrading")).booleanValue();
		//this._isTradingAllowedOnAccount = ((Boolean)dataRow.get_Item("IsTradingAllowedOnAccount")).booleanValue();
	}

	public void setValue(XmlAttributeCollection accountCollection)
	{
		this.setValue(accountCollection, false);
	}

	private void merge(XmlAttributeCollection accountCollection)
	{
		this.setValue(accountCollection, false);
	}

	private void setValue(XmlAttributeCollection accountCollection, boolean isReset)
	{
		for (int i = 0; i < accountCollection.get_Count(); i++)
		{
			String nodeName = accountCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Code"))
			{
				String oldCode = this._code;
				this._code = nodeValue;
				this.updateCode(oldCode);
			}
			else if (nodeName.equals("Name"))
			{
				this._name = nodeValue;
			}
			else if (nodeName.equals("CustomerEmail"))
			{
				this._customerEmail = nodeValue;
			}
			else if (nodeName.equals("OrganizationCode"))
			{
				this._organizationCode = nodeValue;
			}
			else if (nodeName.equals("OrganizationName"))
			{
				this._organizationName = nodeValue;
			}
			else if (nodeName.equals("OrganizationID"))
			{
				this._organizationId = new Guid(nodeValue);
			}
			else if (nodeName.equals("OrganizationEmail"))
			{
				this._organizationEmail = nodeValue;
			}
			else if (nodeName.equals("OrganizationPhone"))
			{
				this._organizationPhone = nodeValue;
			}
			else if (nodeName.equals("RateLotMin"))
			{
				this._rateLotMin = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateMarginD"))
			{
				this._rateMarginD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateMarginO"))
			{
				this._rateMarginO = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateMarginLockO"))
			{
				this._rateMarginLockO = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateMarginLockD"))
			{
				this._rateMarginLockD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateLotMultiplier"))
			{
				this._rateLotMultiplier = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("RateDefaultLot"))
			{
				this._rateDefaultLot = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("Type"))
			{
				this._type = Enum.valueOf(AccountType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("IsAutoClose"))
			{
				this._isAutoClose = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowManagerTrading"))
			{
				this._allowManagerTrading = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowSalesTrading"))
			{
				this._allowSalesTrading = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsMultiCurrency"))
			{
				this._isMultiCurrency = Boolean.valueOf(nodeValue);
			}
			else if(nodeName.equals("Leverage") && !StringHelper.isNullOrEmpty(nodeValue))
			{
				this._leverage = Integer.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowAddNewPosition"))
			{
				this._allowAddNewPosition = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("CustomerID"))
			{
				this._customerId = new Guid(nodeValue);
			}
			else if (nodeName.equals("CurrencyID"))
			{
				this._currency = this._settingsManager.getCurrency(new Guid(nodeValue));
			}
			else if (nodeName.equals("RateCommission"))
			{
				this._rateCommission = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("AgentID"))
			{
				//this._agentId = AppToolkit.isDBNull(nodeValue) ? null : new Guid(nodeValue);
				this._agentId = StringHelper.isNullOrEmpty(nodeValue) ? null : new Guid(nodeValue);
			}
			else if (nodeName.equals("AgentName"))
			{
				this._agentName = (nodeValue == null) ? "" : nodeValue;
			}
			else if (nodeName.equals("AgentCode"))
			{
				this._agentCode = (nodeValue == null) ? "" : nodeValue;
			}
			else if (nodeName.equals("AgentEmail"))
			{
				this._agentEmail = nodeValue;
			}
			else if (nodeName.equals("IsTradingAllowed"))
			{
				this._isTradingAllowedOnAccount = Boolean.valueOf(nodeValue);
				if(!this._isTradingAllowedOnAccount) this._isTradingAllowed = this._isTradingAllowedOnAccount;
			}
			else if (nodeName.equals("BeginTime"))
			{
				this._beginTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("EndTime"))
			{
				this._endTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("IsLocked"))
			{
				this._isLocked = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("GroupID"))
			{
				this._groupId = new Guid(nodeValue);
			}
			else if (nodeName.equals("TradePolicyID"))
			{
				this._tradePolicyId = new Guid(nodeValue);
			}
			else if (nodeName.equals("AlertLevel"))
			{
				AlertLevel alertLevel = Enum.valueOf(AlertLevel.class, Integer.parseInt(nodeValue));
				this.setAlertLevel(alertLevel);
			}
			else if (nodeName.equals("Balance"))
			{
				this._balance = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("Necessary"))
			{
				this._necessary = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("FrozenFund"))
			{
				this._frozenFund = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("TotalPaidAmount"))
			{
				this._totalPaidAmount = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("ValueAsMargin"))
			{
				this._floatTradingItem.set_ValueAsMargin(Double.valueOf(nodeValue).doubleValue());
			}
			//else if (nodeName.equals("Equity"))
			//{??????????????????????????
			//	this._equity = Double.valueOf(nodeValue).doubleValue();
			//}
			else if (nodeName.equals("InterestPLNotValued"))
			{
				this._notValuedTradingItem.set_Interest(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("StoragePLNotValued"))
			{
				this._notValuedTradingItem.set_Storage(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("TradePLNotValued"))
			{
				this._notValuedTradingItem.set_Trade(Double.valueOf(nodeValue).doubleValue());
			}
			/*
			else if (nodeName.equals("InterestPLFloat"))
			{
				this._floatTradingItem.set_Interest(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("StoragePLFloat"))
			{
				this._floatTradingItem.set_Storage(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("TradePLFloat"))
			{
				this._floatTradingItem.set_Trade(Double.valueOf(nodeValue).doubleValue());
			}
		    */
			else if (nodeName.equals("UnclearAmount"))
			{
				this._unclearAmount = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("CreditAmount"))
			{
				this._creditAmount = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("AllowManagerTrading"))
			{
				this._allowManagerTrading = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowSalesTrading"))
			{
				this._allowSalesTrading = Boolean.valueOf(nodeValue);
			}
		}
		if (isReset)
		{
			this.resetAlertLevel();
		}
		this.clearIsSplitLot();
		//Remarked by Michael on 2008-04-09
		//this.calculateEquity();
	}

	public boolean isAlert()
	{
		return (this._alertLevel.value() > AlertLevel.AlertLevel0.value());
	}

	public boolean isWillExpire()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		TimeSpan timeSpan = TimeSpan.fromDays(1);
		return ( this._endTime.after(appTime)
				 && this._endTime.before(appTime.add(timeSpan)));
	}

	public boolean isExpired()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		return (this._endTime.before(appTime));
	}

	public boolean getIsAllowTrade()
	{
		boolean isAllowTrade = false;

		DateTime appTime = TradingConsoleServer.appTime();
		if (this._isTradingAllowed
			&& !appTime.before(this._beginTime)
			&& !appTime.after(this._endTime)
			&& this._tradingConsole.get_TradingAccountManager().isActivatedAccount(this._id))
		{
			if (this._isActive)
			{
				isAllowTrade = true;
			}
		}
		return isAllowTrade;
	}

	public void dispose()
	{
		String queue = this._id.toString();
		TradingConsoleServer.scheduler.remove(queue);

		this._transactions.clear();
		this._currency = null;
		this._accountCurrencies.clear();
	}

	public void calculatePLFloat()
	{
		this.calculateEquity();

		//Refresh AccountGrid
		this.updateNode();
	}

	/*
	public void calculate(TradingItem deltaTradingItem)
	{
		this._floatTradingItem = TradingItem.add(this._floatTradingItem, deltaTradingItem);
		this.calculateEquity();

		//Refresh AccountGrid
		this.updateNode();
	}
    */

	public void deltaUnclearAmount(double deltaUnclearAmount)
	{
		this._unclearAmount += deltaUnclearAmount;
	}

	public void addNode()
	{
		//boolean verifiedCustomerIdentity = this.get_VerifiedCustomerIdentity();
		//if (verifiedCustomerIdentity)
		{
			this._tradingConsole.get_AccountBindingManager().add(this);
			this._tradingConsole.get_AccountBindingManager().setAccountIcon(this, this.getStatusIcon());
			if(this._settingsManager.getAccountsSize() == 1)
			{
				this._tradingConsole.get_MainForm().get_AccountTable().expandAllRows();
			}
		}
	}

	public boolean needShowStatus()
	{
		return !this._settingsManager.get_Customer().get_IsNoShowAccountStatus();
	}

	public String getRatioString()
	{
		if (this._necessary == 0)
		{
			return TradingConsole.emptyRateString;
		}
		else
		{
			double ratio = (this._equity / this._necessary) * 100;
			if (ratio == 0)
			{
				return TradingConsole.emptyRateString;
			}
			return AppToolkit.format2(ratio, 2) + "%";
		}
	}

	//when code has change, will call this function
	public void updateCode(String oldCode)
	{
		this._tradingConsole.get_AccountBindingManager().update(this);
	}

	//when currency code has change, will call this function
	public void updateCurrencyCode()
	{
		this._tradingConsole.get_AccountBindingManager().updateCurrencyCode();
	}

	public void updateAlertLevel(AlertLevel oldAlertLevel)
	{
		this._tradingConsole.get_AccountBindingManager().update(this);
	}

	public void updateAccountStatus()
	{
		this._tradingConsole.get_AccountBindingManager().setAccountIcon(this, this.getStatusIcon());
	}

	//if code has change, accountNode will equals null,
	//so must update code for UI then call this function,
	public void updateNode()
	{
		this.updateNode(false);
	}

	public void updateNode(boolean includeAccountCurrencies)
	{
		this._tradingConsole.get_AccountBindingManager().update(this, includeAccountCurrencies);
	}

	public void removeNode()
	{
		this._tradingConsole.get_AccountBindingManager().remove(this);
	}

	public boolean isLockedBySelfAtCurrent()
	{
		return AccountAgentHistory.isLockedBySelfAtCurrent(this._settingsManager, this);
	}

	public boolean isAssigned(DateTime agentTime)
	{
		return AccountAgentHistory.isAssigned(this._settingsManager, this, agentTime);
	}

	private static void process(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement, false);
	}

	public static void place(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void execute(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void execute2(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.execute(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void cut(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void reset(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement, true);
	}

	public static void delete(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void assign(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Account.process(tradingConsole, settingsManager, accountXmlElement, false);
	}

	private static void process(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement, boolean isReset)
	{
		if (accountXmlElement==null) return;
		XmlAttributeCollection accountCollection = accountXmlElement.get_Attributes();
		Guid accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
		Account account = settingsManager.getAccount(accountId);
		if (account == null)
		{
			return;
		}
		AccountCurrency.process(tradingConsole, settingsManager, accountXmlElement);
		account.setValue(accountCollection, isReset);
		account.updateNode();
	}

	public static void merge(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		if (accountXmlElement == null)
		{
			return;
		}
		XmlAttributeCollection accountCollection = accountXmlElement.get_Attributes();
		Guid accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
		Account account = settingsManager.getAccount(accountId);
		if (account == null)
		{
			return;
		}
		AccountCurrency.merge(tradingConsole, settingsManager, accountXmlElement);
		account.merge(accountCollection);
	}

	public static void refresh(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		if (accountXmlElement == null)
		{
			return;
		}
		XmlAttributeCollection accountCollection = accountXmlElement.get_Attributes();
		Guid accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
		Account.refresh(tradingConsole, settingsManager, accountId);
	}

	public static void refresh(TradingConsole tradingConsole, SettingsManager settingsManager, Guid accountId)
	{
		Account account = settingsManager.getAccount(accountId);
		if (account == null)
		{
			tradingConsole.traceSource.trace(TraceType.Warning, "Not Exists Account:" + accountId.toString());
			return;
		}
		account.calculateEquity();
		account.updateNode();
	}

	private boolean _alertLevelChanged = false;
	public boolean get_AlertLevelChanged()
	{
		return this._alertLevelChanged;
	}
	public void resetAlertLevelChanged()
	{
		this._alertLevelChanged = false;
	}
	public void setAlertLevel(AlertLevel alertLevel)
	{
		AlertLevel oldAlertLevel = this._alertLevel;
		if (!oldAlertLevel.equals(alertLevel))
		{
			this._alertLevel = alertLevel;
			this._alertLevelChanged = true;
			//Update Account UI
			this.updateAlertLevel(oldAlertLevel);
		}
	}

	public void resetAlertLevel()
	{
		this.setAlertLevel(AlertLevel.AlertLevel0);

		//Modified by Michael on 2008-04-09
		//this._isGotCutOrder = false;
		//this._sentCount = 0;
		this.resetCheckArrivedAlertLevel3();
	}

	public static void updateAccount(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode accountNode, String updateType)
	{
		if (accountNode == null)
		{
			return;
		}
		XmlAttributeCollection accountCollection = accountNode.get_Attributes();
		int count = accountCollection.get_Count();
		if (count <= 0)
		{
			return;
		}
		Guid accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
		Account account = settingsManager.getAccount(accountId);
		if (account==null) return;

		XmlAttribute tradePolicyAttribute = accountCollection.get_ItemOf("TradePolicyID");
		if(tradePolicyAttribute != null)
		{
			Guid tradePolicyID = new Guid(tradePolicyAttribute.get_Value());
			if(tradePolicyID.compareTo(account._tradePolicyId) != 0)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "Refresh system for trade policy changed");
				account._tradingConsole.get_MainForm().refreshSystem();
				return;
			}
		}

		Guid groupId = null;
		Guid customerId = account.get_CustomerId();
		Guid agentId = account.get_AgentId();
		for (int i = 0; i < count; i++)
		{
			String nodeName = accountCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("CustomerID"))
			{
				customerId = new Guid(nodeValue);
			}
			else if (nodeName.equals("AgentID"))
			{
				agentId = StringHelper.isNullOrEmpty(nodeValue) ? null : new Guid(nodeValue);
			}
		}

		boolean isDeleteAccount = false;
		boolean isDeleteGroup = false;
		Guid userId = tradingConsole.get_LoginInformation().get_CustomerId();
		/*if ( (customerId == null && agentId == null)
			|| (customerId != null && customerId.equals(userId))
			|| (agentId != null && agentId.equals(userId)))*/
		if(true)//since agent mapping is added, it's not avaliable to check in trader, let server validate
		{
			if (updateType.equals("Modify")) // || updateType.equals("Add"))
			{
				if (account == null)
				{
					/*
					 if ((customerID == this.UserID) ||
					  (agentID == this.UserID))
					 {
					  account = new Account(accountID,this);
					  account.initializeXml(accountNode,true);
					  this.Accounts.add(accountID,account);

					  account.refreshIsMustAssignOrderAddSchedule();

					  var accountIDs = new Array();
					  accountIDs[0] = accountID;
					  webService.getAccounts(this,accountIDs,true,false);

					  //GetCurrencyRateByAccountID
					  webService.getCurrencyRateByAccountID(this,accountID);

					  account.fillGrid(this.AccountGrid);
					 }
					 groupId = account.get_GroupId();
					 */
				}
				else
				{
					Boolean oldValue = null;
					boolean oldIsTradingAllowedOnAccount = account.get_IsTradingAllowedOnAccount();
					if(account.get_UserRelation().value() == UserRelation.Manager.value())
					{
						oldValue = account.get_AllowManagerTrading();
					}
					else if(account.get_UserRelation().value() == UserRelation.Sales.value())
					{
						oldValue = account.get_AllowSalesTrading();
					}

					account.setValue(accountCollection);
					if(accountNode.get_HasChildNodes()
						&& accountNode.get_FirstChild().get_Name().equalsIgnoreCase("AccountCurrencies"))
					{
						XmlNodeList children = accountNode.get_FirstChild().get_ChildNodes();
						for(int index = 0; index < children.get_Count(); index++)
						{
							XmlNode accountCurrencyNode = children.item(index);
							Guid currencyId = new Guid(accountCurrencyNode.get_Attributes().get_ItemOf("CurrencyId").get_Value());
							CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid,Guid>(account.get_Id(), currencyId);
							if(account.get_AccountCurrencies().containsKey(compositeKey))
							{
								double necessary = Double.valueOf(accountCurrencyNode.get_Attributes().get_ItemOf("Necessary").get_Value()).doubleValue();
								account.get_AccountCurrencies().get(compositeKey).set_Necessary(necessary);
								account.get_AccountCurrencies().get(compositeKey).updateNode();
							}
						}
					}

					Boolean newValue = null;
					if(account.get_UserRelation().value() == UserRelation.Manager.value())
					{
						newValue = account.get_AllowManagerTrading();
					}
					else if(account.get_UserRelation().value() == UserRelation.Sales.value())
					{
						newValue = account.get_AllowSalesTrading();
					}

					if((account.get_IsTradingAllowedOnAccount() && !oldIsTradingAllowedOnAccount)
					   || (newValue != null && newValue.booleanValue() != oldValue.booleanValue()))
					{
						tradingConsole.get_MainForm().refreshSystem();
						return;
					}

					account.updateNode();
					groupId = account.get_GroupId();
				}
			}
			else if (updateType.equals("Delete"))
			{
				if (account != null)
				{
					groupId = account.get_GroupId();
					settingsManager.removeAccount(account);
				}
				isDeleteAccount = true;
				isDeleteGroup = true;
				if (groupId != null)
				{
					for (Iterator<Account> iterator = settingsManager.getAccounts().values().iterator(); iterator.hasNext(); )
					{
						Account account2 = iterator.next();
						if (account2.get_GroupId().equals(groupId)
							&& !account2.get_Id().equals(accountId))
						{
							isDeleteGroup = false;
							break;
						}
					}
				}
			}
		}
		else
		{
			if (account != null)
			{
				groupId = account.get_GroupId();
				settingsManager.removeAccount(account);
			}
			isDeleteAccount = true;
			isDeleteGroup = true;
			if (groupId != null)
			{
				for (Iterator<Account> iterator = settingsManager.getAccounts().values().iterator(); iterator.hasNext(); )
				{
					Account account2 = iterator.next();
					if (account2.get_GroupId().equals(groupId)
						&& !account2.get_Id().equals(accountId))
					{
						isDeleteGroup = false;
						break;
					}
				}
			}
		}
		if (account != null)
		{
			HashMap<Guid, Instrument> instruments = new HashMap<Guid, Instrument> ();
			for (Iterator<Transaction> iterator = account.get_Transactions().values().iterator(); iterator.hasNext(); )
			{
				Transaction transaction = iterator.next();
				Guid instrumentId = transaction.get_Instrument().get_Id();
				if (!instruments.containsKey(instrumentId))
				{
					instruments.put(instrumentId, settingsManager.getInstrument(instrumentId));
				}
			}
			boolean isRequireAccount = false;
			for (Iterator<Instrument> iterator = instruments.values().iterator(); iterator.hasNext(); )
			{
				Instrument instrument = iterator.next();
				instrument.reCalculateTradePLFloat();
				isRequireAccount = true;
			}
			if (isRequireAccount)
			{
				account.updateNode();
			}
		}

		//????????????????????
		//Update OrderGrid/OpenOrderGrid for colhidden
		//this.OrderGrid.colHidden(this.OrderGrid.colIndex(this.OrderColKey.AccountCode)) = (this.isSingleAccount() == true);
		//this.OpenOrderGrid.colHidden(this.OpenOrderGrid.colIndex(this.OpenOrderColKey.AccountCode)) = (this.isSingleAccount() == true);

		//Update service State.Accounts
		//if (updateType != "Add")
		if (isDeleteAccount || (!isDeleteAccount && groupId != null))
		{
			//call webservice.......................
			tradingConsole.get_TradingConsoleServer().updateAccount(accountId, groupId, isDeleteAccount, isDeleteGroup);
		}
	}

	public boolean isValidAccountForTradePolicy(Instrument instrument, boolean isCheckTradePolicyIsTradeActive)
	{
		Guid tradePolicyId = this.get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolicyId, instrument.get_Id());
		if (tradePolicyDetail != null)
		{
			return (!isCheckTradePolicyIsTradeActive
					|| (isCheckTradePolicyIsTradeActive && tradePolicyDetail.get_IsTradeActive()));
		}
		return false;
	}

	public static boolean isExistsLockedAccount(SettingsManager settingsManager)
	{
		//Confimed exists agent account
		for (Iterator<Account> iterator = settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (!account.get_Type().equals(AccountType.Agent))
			{
				LockedStatus lockedStatus = account.getAccountLockedStatus();
				if (lockedStatus.equals(LockedStatus.BySelf))
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isSelectAccount()
	{
		//the old version will always return true????
		return this._select;
	}

	public LockedStatus getAccountLockedStatus()
	{
		LockedStatus lockedStatus = LockedStatus.NotLocked;
		if (!this._isLocked)
		{
			lockedStatus = LockedStatus.NotLocked;
		}
		else
		{
			if (this._agentId != null && this._agentId.equals(this._settingsManager.get_Customer().get_UserId()))
			{
				lockedStatus = (this.isLockedBySelfAtCurrent()) ? LockedStatus.BySelf : LockedStatus.NotLocked;
			}
			else
			{
				lockedStatus = (this._agentId != null) ? LockedStatus.ByOther : LockedStatus.NotLocked;
			}
		}

		return lockedStatus;
	}

	public boolean getIsMustAssignOrder()
	{
		if (!this._type.equals(AccountType.Agent))
		{
			return false;
		}
		for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.getIsMustAssignOrder())
			{
				return true;
			}
		}
		return false;
	}

	public void checkIsMustAssignOrderSchedulerStart()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		TimeSpan timeSpan = TimeSpan.fromSeconds(10);
		String queue = this._id.toString();
		try
		{
			this._schedulerEntryForCheckIsMustAssignOrder = TradingConsoleServer.scheduler.add(queue, this, Account.checkIsMustAssignOrder, appTime,timeSpan);
			//TradingConsoleServer.scheduler.add(queue, this, null, appTime,timeSpan);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void checkIsMustAssignOrder()
	{
		if (this._settingsManager.get_IsHasNotifiedAssignOrder())
		{
			return;
		}
		if (this.getIsMustAssignOrder())
		{
			this._settingsManager.set_IsHasNotifiedAssignOrder(true);
			this._tradingConsole.messageNotify(Language.TradeConsolePromptIsMustAssignOrderAlert0, false);
		}
	}

	public Icon getStatusIcon()
	{
		if (this._type.equals(AccountType.Agent))
		{
			return AppToolkit.getAsIcon("Agent.gif");
		}

		LockedStatus lockedStatus = this.getAccountLockedStatus();
		if (lockedStatus == LockedStatus.BySelf)
		{
			return AppToolkit.getAsIcon("Locked.gif");
		}
		else if (lockedStatus == LockedStatus.ByOther)
		{
			return AppToolkit.getAsIcon("LockedByOther.gif");
		}
		return null;
	}

	public void refreshAccountInfo()
	{
		this.setTransactionVisible();
		this.addNode();
		for (Iterator<AccountCurrency> iterator = this._accountCurrencies.values().iterator(); iterator.hasNext(); )
		{
			AccountCurrency accountCurrency = iterator.next();
			accountCurrency.addNode();
		}
	}

	private void setTransactionVisible()
	{
		for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			//transaction.setOrdersVisible();
		}
	}

	public void resum()
	{
		double balance = 0.0;
		double unclearAmount = 0.0;
		double necessary = 0.0;
		double frozenFund = 0.0;
		double necessaryForPartialPaymentPhysicalOrder = 0.0;
		double totalPaidAmount = 0.0;

		SettingsManager settingsManager = this._settingsManager;

		for(AccountCurrency accountCurrency : this._accountCurrencies.values())
		{
			if (this.get_IsMultiCurrency())
			{
				CurrencyRate currencyRate = settingsManager.getCurrencyRate(accountCurrency.get_Currency().get_Id(), this.get_Currency().get_Id());
				balance += currencyRate.exchange(accountCurrency.get_Balance());
				necessary += currencyRate.exchange(accountCurrency.get_Necessary());
				necessaryForPartialPaymentPhysicalOrder += currencyRate.exchange(accountCurrency.get_NecessaryForPartialPaymentPhysicalOrder());
				frozenFund += currencyRate.exchange(accountCurrency.get_FrozenFund());
				unclearAmount += currencyRate.exchange(accountCurrency.get_UnclearAmount());
				totalPaidAmount += currencyRate.exchange(accountCurrency.get_TotalPaidAmount());
			}
			else
			{
				balance += accountCurrency.get_Balance();
				necessary += accountCurrency.get_Necessary();
				necessaryForPartialPaymentPhysicalOrder += accountCurrency.get_NecessaryForPartialPaymentPhysicalOrder();
				frozenFund += accountCurrency.get_FrozenFund();
				unclearAmount += accountCurrency.get_UnclearAmount();
				totalPaidAmount += accountCurrency.get_TotalPaidAmount();
			}
			this.set_Balance(balance);
			this.set_Necessary(necessary);
			this.set_NecessaryForPartialPaymentPhysicalOrder(necessaryForPartialPaymentPhysicalOrder);
			this.set_FrozenFund(frozenFund);
			this.set_UnclearAmount(unclearAmount);
			this.set_TotalPaidAmount(totalPaidAmount);
		}
		this.calculateEquity();
	}

	public void calculateEquity()
	{
		this._equity = this._balance
			+ TradingItem.sum(this._notValuedTradingItem) + TradingItem.sum(this._floatTradingItem)
			+ this._totalPaidAmount;

		//Modified by Michael on 2008-04-09
		/*
		   if (this._canCheckAccountsForCut && this._isGotCutOrder == false && this._sentCount == 0) //< Account.sendTimes)
		   {
		 if (this.isArrivedAlertLevel3())
		 {
		  this.fixDataForCut();
		 }
		   }
		 */
		this.checkArrivedAlertLevel3();
	}

	//region----------------------------------------------------------------------------------------------------------
	//Get Cut Orders when Necessary * TradePolicy.AlertLevel3 >= Equity & All Instruments's Executed Order: SumLotBalanceForBuy != SumLotBalanceForSell
	private static String _actionArgsGetAccountsForCut = "GetAccountsForCut";
	private Scheduler.SchedulerEntry _getAccountsForCutScheduleEntry;

	private DateTime _lastAlertTime = DateTime.maxValue;
	private DateTime _arrivedAlertLevel3Time = DateTime.maxValue;

	private DateTime get_LastAlertTime()
	{
		if (this._lastAlertTime.equals(DateTime.maxValue))
		{
			this._lastAlertTime = this._tradingConsole.get_LoginInformation().get_LoginTime();
		}
		return this._lastAlertTime;
	}

	//Added by Michael on 2008-04-09
	public void resetCheckArrivedAlertLevel3()
	{
		this._arrivedAlertLevel3Time = DateTime.maxValue;
	}

	//Added by Michael on 2008-04-09
	public boolean needGetAccountsForCutOrder()
	{
		if (this._arrivedAlertLevel3Time.equals(DateTime.maxValue))
		{
			return false;
		}

		DateTime appTime = TradingConsoleServer.appTime();
		return DateTime.substract(appTime, TimeSpan.fromSeconds(30)).before(this._arrivedAlertLevel3Time);
	}

	//Added by Michael on 2008-04-09
	private void checkArrivedAlertLevel3()
	{
		if (this._arrivedAlertLevel3Time.equals(DateTime.maxValue) && this.isArrivedAlertLevel3())
		{
			this._arrivedAlertLevel3Time = TradingConsoleServer.appTime();
		}
	}

	private boolean isArrivedAlertLevel3()
	{
		TradePolicy tradePolicy = this._settingsManager.getTradePolicy(this._tradePolicyId);
		if (tradePolicy == null)
		{
			this._tradingConsole.traceSource.trace(TraceType.Warning, "Not Exists TradePolicy: " + this._tradePolicyId.toString());
			return false;
		}
		if (this._necessary * tradePolicy.get_AlertLevel3() >= this._equity)
		{
			return !this.isSameAsInstrumentExecutedLotBalance();
		}
		return false;
	}

	//Added by Michael on 2008-04-9
	private boolean isSameAsInstrumentExecutedLotBalance()
	{
		if (this._transactions.values().size() <= 0)
		{
			return true;
		}

		HashMap<Guid, ExecutedLotBalance> instrumentExecutedLotBalances = new HashMap<Guid, ExecutedLotBalance> ();
		for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.get_Phase().equals(Phase.Executed))
			{
				Guid instrumentId = transaction.get_Instrument().get_Id();
				if (!instrumentExecutedLotBalances.containsKey(instrumentId))
				{
					instrumentExecutedLotBalances.put(instrumentId, new ExecutedLotBalance());
				}
				ExecutedLotBalance executedLotBalance = instrumentExecutedLotBalances.get(instrumentId);
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if (order.get_IsBuy())
					{
						executedLotBalance.addBuyLotBalance(order.get_LotBalance());
					}
					else
					{
						executedLotBalance.addSellLotBalance(order.get_LotBalance());
					}
				}
			}
		}
		for (Iterator<ExecutedLotBalance> iterator = instrumentExecutedLotBalances.values().iterator(); iterator.hasNext(); )
		{
			ExecutedLotBalance executedLotBalance = iterator.next();
			if (!executedLotBalance.isSame())
			{
				return false;
			}
		}
		return true;
	}

	//Added by Michael on 2008-04-09
	public void getAccountsForCut()
	{
		if (this.needGetAccountsForCutOrder())
		{
			AccountForCutResult accountForCutResult = this._tradingConsole.get_TradingConsoleServer().getAccountForCut(this.get_LastAlertTime(), this._id, true);
			this._lastAlertTime = accountForCutResult.get_LastAlertTime();
			CommandsManager.fixData(this._tradingConsole, this._settingsManager, accountForCutResult.get_AccountXmlNode());
			this.resetCheckArrivedAlertLevel3();
		}
	}

	//Added by Michael on 2008-04-9
	public void getAccountsForCutSchedulerStart()
	{
		if (this._getAccountsForCutScheduleEntry != null)
		{
			return;
		}

		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			TimeSpan timeSpan = TimeSpan.fromMinutes(5);
			this._getAccountsForCutScheduleEntry = TradingConsoleServer.scheduler.add(this, Account._actionArgsGetAccountsForCut, beginTime,
				DateTime.maxValue, timeSpan, true);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	//Added by Michael on 2008-04-9
	public synchronized void getAccountsForCutSchedulerStop()
	{
		if (this._getAccountsForCutScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._getAccountsForCutScheduleEntry);
			this._getAccountsForCutScheduleEntry = null;
		}
	}

	//endregion----------------------------------------------------------------------------------------------------------

	//region----------------------------------------------------------------------------------------------------------
	//fixDataForAllowFreeAgent
	public void fixDataForAllowFreeAgent()
	{
		XmlNode accountXmlNode = this._tradingConsole.get_TradingConsoleServer().getAccounts(new Guid[]
			{this._id}, true);
		CommandsManager.fixData(this._tradingConsole, this._settingsManager, accountXmlNode);
	}

	public boolean hasSufficientUsableMargin(String currencyCode, String amount)
	{
		amount = StringHelper.replace(amount, ",", "");
		double amount2 = Double.valueOf(amount);
		for(AccountCurrency accountCurrency : this._accountCurrencies.values())
		{
			if(accountCurrency.get_Currency().get_RealCode().equalsIgnoreCase(currencyCode))
			{
				if(amount2 <= accountCurrency.get_Usable())
				{
					return true;
				}
			}
		}
		return false;
	}

	//endregion----------------------------------------------------------------------------------------------------------

	private class ExecutedLotBalance
	{
		private BigDecimal _buyLotBalances = BigDecimal.ZERO;
		private BigDecimal _sellLotBalances = BigDecimal.ZERO;

		public ExecutedLotBalance()
		{
		}

		public boolean isSame()
		{
			return this._buyLotBalances.compareTo(this._sellLotBalances) == 0;
		}

		public void addBuyLotBalance(BigDecimal value)
		{
			this._buyLotBalances = this._buyLotBalances.add(value);
		}

		public void addSellLotBalance(BigDecimal value)
		{
			this._sellLotBalances = this._sellLotBalances.add(value);
		}
	}

	private static class ComparatorByCode implements Comparator<Account>
	{
		public int compare(Account left, Account right)
		{
			return left.get_Code().compareTo(right.get_Code());
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}

	public void clearIsSplitLot()
	{
		this._instrumentToIsSplitLot.clear();
	}

	private Hashtable<Instrument, Boolean> _instrumentToIsSplitLot = new  Hashtable<Instrument,Boolean>();
	public boolean getIsSplitLot(Instrument instrument)
	{
		if(!this._instrumentToIsSplitLot.containsKey(instrument))
		{
			Boolean isSplitLot = this.caculateIsSplitLot(instrument);
			this._instrumentToIsSplitLot.put(instrument, isSplitLot);
		}
		return this._instrumentToIsSplitLot.get(instrument).booleanValue();
	}

	private boolean caculateIsSplitLot(Instrument instrument)
	{
		if(instrument.get_Category().equals(InstrumentCategory.Physical))
		{
			if(instrument.get_PhysicalLotDecimal() > 0) return true;
		}

		TradePolicyDetail tradePolicyDetail
				= this._settingsManager.getTradePolicyDetail(this.get_TradePolicyId(), instrument.get_Id());

		BigDecimal step = this._rateLotMin.multiply(tradePolicyDetail.get_MinOpen());
		if (step.compareTo(new BigDecimal(step.intValue())) != 0)  return true;

		step = this._rateLotMin.multiply(tradePolicyDetail.get_MinClose());
		if (step.compareTo(new BigDecimal(step.intValue())) != 0)  return true;

		step = this._rateLotMultiplier.multiply(tradePolicyDetail.get_OpenMultiplier());
		if (step.compareTo(new BigDecimal(step.intValue())) != 0)  return true;

		step = this._rateLotMultiplier.multiply(tradePolicyDetail.get_CloseMultiplier());
		if (step.compareTo(new BigDecimal(step.intValue())) != 0)  return true;

		step = this._rateDefaultLot.multiply(tradePolicyDetail.get_DefaultLot());
		if (step.compareTo(new BigDecimal(step.intValue())) != 0)  return true;

		return false;
	}

	public void setLeverage(int leverage)
	{
		this._leverage = leverage;
	}
}
