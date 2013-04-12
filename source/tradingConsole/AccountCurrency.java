package tradingConsole;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.settings.*;

public class AccountCurrency
{
	private SettingsManager _settingsManager;

	private double _unclearAmount;

	private double _balance;
	private double _necessary;

	private TradingItem _notValuedTradingItem;
	private TradingItem _floatTradingItem;

	private Account _account;
	private Currency _currency;

	public Account get_Account()
	{
		return this._account;
	}

	public Currency get_Currency()
	{
		return this._currency;
	}

	public double get_UnclearAmount()
	{
		return this._unclearAmount;
	}

	public double get_Equity()
	{
		return this._balance + this._notValuedTradingItem.get_Interest()
							+ this._notValuedTradingItem.get_Storage() + this._notValuedTradingItem.get_Trade()
							+ this._floatTradingItem.get_Trade() + this._floatTradingItem.get_Interest()
							+ this._floatTradingItem.get_Storage();

	}

	public TradingItem get_FloatTradingItem()
	{
		return this._floatTradingItem;
	}

	public TradingItem get_NotValuedTradingItem()
	{
		return this._notValuedTradingItem;
	}

	public void set_NotValuedTradingItem(TradingItem value)
	{
		double notValuedTrade = 0;
		if(this._notValuedTradingItem != null) notValuedTrade =  this._notValuedTradingItem.get_Trade();

		this._notValuedTradingItem = value;
		this._notValuedTradingItem.set_Trade(notValuedTrade);
	}

	public void set_FloatTradingItem(TradingItem value)
	{
		this._floatTradingItem = value;
	}

	public double get_Balance()
	{
		return this._balance;
	}

	public double get_Necessary()
	{
		return this._necessary;
	}

	public double get_Usable()
	{
		return this.get_Equity() - this.get_Necessary();
	}

	public void clearFloatTradingItem()
	{
		this._floatTradingItem.clear();
	}

	public AccountCurrency(SettingsManager settingsManager, Guid accountId, Guid currencyId)
	{
		this._settingsManager = settingsManager;
		this._notValuedTradingItem = TradingItem.create(0.00, 0.00, 0.00);
		this._floatTradingItem = TradingItem.create(0.00, 0.00, 0.00);

		this._account = this._settingsManager.getAccount(accountId);
		this._currency = this._settingsManager.getCurrency(currencyId);
	}

	public AccountCurrency(SettingsManager settingsManager, DataRow dataRow)
	{
		this(settingsManager, (Guid)dataRow.get_Item("AccountID"), (Guid)dataRow.get_Item("CurrencyID"));
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._unclearAmount = AppToolkit.convertDBValueToDouble(dataRow.get_Item("UnclearAmount"), 0.0);

		this._balance = AppToolkit.convertDBValueToDouble(dataRow.get_Item("Balance"), 0.0);
		this._necessary = AppToolkit.convertDBValueToDouble(dataRow.get_Item("Necessary"), 0.0);

		this._notValuedTradingItem.set_Interest(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLNotValued"), 0.0));
		this._notValuedTradingItem.set_Storage(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLNotValued"), 0.0));
		this._notValuedTradingItem.set_Trade(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePLNotValued"), 0.0));

		//this._floatTradingItem.set_Interest(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLFloat"), 0.0));
		//this._floatTradingItem.set_Storage(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLFloat"), 0.0));
		//this._floatTradingItem.set_Trade(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePLFloat"), 0.0));
	}

	public void setValue(XmlAttributeCollection accountCurrencyCollection)
	{
		for (int i = 0; i < accountCurrencyCollection.get_Count(); i++)
		{
			String nodeName = accountCurrencyCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCurrencyCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Balance"))
			{
				this._balance = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("Necessary"))
			{
				this._necessary = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("UnclearAmount"))
			{
				this._unclearAmount = Double.valueOf(nodeValue).doubleValue();
			}
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
		}
	}

	public void addNode()
	{
	    this._account.get_TradingConsole().get_AccountBindingManager().add(this);
	}

	//when code has change, will call this function
	//by account call
	public void updateCode(String oldCode)
	{
		this._account.get_TradingConsole().get_AccountBindingManager().update(this);
	}

	//if code has change, searchNode will equals null,
	//so must update code for UI then call this function,
	public void updateNode()
	{
		this._account.get_TradingConsole().get_AccountBindingManager().update(this);
	}

	public void removeNode()
	{
		this._account.get_TradingConsole().get_AccountBindingManager().update(this);
	}

	private void decreaseValue()
	{
		if (this._account.get_IsMultiCurrency())
		{
			CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(this._currency.get_Id(), this._account.get_Currency().get_Id());
			this._account.set_Balance(this._account.get_Balance() - currencyRate.exchange(this._balance));
			this._account.set_Necessary(this._account.get_Necessary() - currencyRate.exchange(this._necessary));
			this._account.set_UnclearAmount(this._account.get_UnclearAmount() - currencyRate.exchange(this._unclearAmount));
		}
		else
		{
			this._account.set_Balance(this._account.get_Balance() - this._balance);
			this._account.set_Necessary(this._account.get_Necessary() - this._necessary);
			this._account.set_UnclearAmount(this._account.get_UnclearAmount() - this._unclearAmount);
		}
		//TradingItem deltaTradingItem = TradingItem.create(0.0 - this._floatTradingItem.get_Interest(), 0.0 - this._floatTradingItem.get_Storage(),
		//	0.0 - this._floatTradingItem.get_Trade());
		//this.calculate(deltaTradingItem);
	}

	/*
	private void calculate(TradingItem deltaTradingItem)
	{
		this._floatTradingItem = TradingItem.add(this._floatTradingItem, deltaTradingItem);
		if (this._account.get_IsMultiCurrency())
		{
			CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(this._currency.get_Id(), this._account.get_Id());
			deltaTradingItem = TradingItem.exchange(deltaTradingItem, currencyRate);
		}
		this._account.calculate(deltaTradingItem);
	}
	*/

    public static void merge(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
    {
	   AccountCurrency.process(tradingConsole, settingsManager, accountXmlElement);
	}

	public static void process(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement accountXmlElement)
	{
		Guid accountId = new Guid(accountXmlElement.get_Attributes().get_ItemOf("ID").get_Value());
		XmlNodeList accountCurrencyNodeList = accountXmlElement.get_ChildNodes();
		for (int i = 0, count = accountCurrencyNodeList.get_Count(); i < count; i++)
		{
			XmlNode accountCurrencyNode = accountCurrencyNodeList.item(i);
			if (accountCurrencyNode.get_LocalName().equals("Currency"))
			{
				Guid currencyId = new Guid(accountCurrencyNode.get_Attributes().get_ItemOf("ID").get_Value());
				AccountCurrency accountCurrency = settingsManager.getAccountCurrency(accountId, currencyId);
				if (accountCurrency == null)
				{
					accountCurrency = new AccountCurrency(settingsManager, accountId, currencyId);
					settingsManager.setAccountCurrency(accountId, currencyId, accountCurrency);
					settingsManager.getAccount(accountId).set_AccountCurrencies(accountId, currencyId, accountCurrency);
					accountCurrency.deltaUnclearAmount();
					accountCurrency.addNode();
				}
				else
				{
					accountCurrency.setValue(accountCurrencyNode.get_Attributes());
					accountCurrency.updateNode();
				}
			}
		}
	}

	public static void updateAccountBalance(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode accountCurrencyNode,
											String updateType)
	{
		if (accountCurrencyNode == null)
		{
			return;
		}
		XmlAttributeCollection accountCurrencyCollection = accountCurrencyNode.get_Attributes();
		int count = accountCurrencyCollection.get_Count();
		Guid accountId = new Guid(accountCurrencyCollection.get_ItemOf("AccountID").get_Value());
		Account account = settingsManager.getAccount(accountId);
		if (account == null)
		{
			return;
		}

		double balance = 0.0;
		double unclearAmount = 0.0;
		double necessary = 0.0;
		double interestPLNotValued = 0.0;
		double storagePLNotValued = 0.0;
		double tradePLNotValued = 0.0;
		double interestPLFloat = 0.0;
		double storagePLFloat = 0.0;
		double tradePLFloat = 0.0;
		for (int i = 0; i < count; i++)
		{
			String nodeName = accountCurrencyCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCurrencyCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Balance"))
			{
				balance = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("UnclearAmount"))
			{
				unclearAmount = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("Necessary"))
			{
				necessary = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("InterestPLNotValued"))
			{
				interestPLNotValued = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("StoragePLNotValued"))
			{
				storagePLNotValued = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("TradePLNotValued"))
			{
				tradePLNotValued = Double.valueOf(nodeValue).doubleValue();
			}
			/*
			else if (nodeName.equals("InterestPLFloat"))
			{
				interestPLFloat = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("StoragePLFloat"))
			{
				storagePLFloat = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("TradePLFloat"))
			{
				tradePLFloat = Double.valueOf(nodeValue).doubleValue();
			}
			*/
		}

		Guid currencyId = new Guid(accountCurrencyCollection.get_ItemOf("CurrencyID").get_Value());
		AccountCurrency accountCurrency = settingsManager.getAccountCurrency(accountId, currencyId);
		if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			if (accountCurrency != null)
			{
				//first - then +
				//accountCurrency.decreaseValue();
				if (updateType.equals("Add"))
				{
					accountCurrency._balance += balance;
					accountCurrency._unclearAmount += unclearAmount;
					accountCurrency._necessary += necessary;
					accountCurrency._notValuedTradingItem = TradingItem.add(accountCurrency._notValuedTradingItem,
						TradingItem.create(interestPLNotValued, storagePLNotValued, tradePLNotValued));
					//TradingItem deltaTradingItem = TradingItem.create(interestPLFloat, storagePLFloat, tradePLFloat);
					//accountCurrency.calculate(deltaTradingItem);
				}
				else
				{
					accountCurrency.setValue(accountCurrencyCollection);
				}
				accountCurrency.updateNode();
			}
			else
			{
				accountCurrency = new AccountCurrency(settingsManager, accountId, currencyId);
				accountCurrency.setValue(accountCurrencyCollection);
				settingsManager.setAccountCurrency(accountId, currencyId, accountCurrency);
				settingsManager.getAccount(accountId).set_AccountCurrencies(accountId, currencyId, accountCurrency);
				accountCurrency.deltaUnclearAmount();
				accountCurrency.addNode();

				//Remarked by Michael on 2008-04-09
				//account.calculateEquity();
				//account.updateNode();
			}
		}
		else if (updateType.equals("Delete"))
		{
			if (accountCurrency != null)
			{
				accountCurrency.decreaseValue();

				accountCurrency._balance -= balance;
				accountCurrency._unclearAmount -= unclearAmount;
				accountCurrency._necessary -= necessary;
				accountCurrency._notValuedTradingItem = TradingItem.add(accountCurrency._notValuedTradingItem,
					TradingItem.create(0.0 - interestPLNotValued, 0.0 - storagePLNotValued, 0.0 - tradePLNotValued));
				//TradingItem deltaTradingItem = TradingItem.create(0.0 - interestPLFloat, 0.0 - storagePLFloat, 0.0 - tradePLFloat);
				//accountCurrency.calculate(deltaTradingItem);
				accountCurrency.updateNode();
			}
		}
		if (account.get_IsMultiCurrency())
		{
			CurrencyRate currencyRate = settingsManager.getCurrencyRate(accountCurrency.get_Currency().get_Id(), account.get_Currency().get_Id());
			account.set_Balance(account.get_Balance() + currencyRate.exchange(balance));
			account.set_Necessary(account.get_Necessary() + currencyRate.exchange(necessary));
			account.set_UnclearAmount(account.get_UnclearAmount() + currencyRate.exchange(unclearAmount));
		}
		else
		{
			account.set_Balance(account.get_Balance() + balance);
			account.set_Necessary(account.get_Necessary() + necessary);
			account.set_UnclearAmount(account.get_UnclearAmount() + unclearAmount);
		}
		account.calculateEquity();
		account.updateNode();
	}

	public void deltaUnclearAmount()
	{
		this.deltaUnclearAmount(this.get_UnclearAmount());
	}

	public void deltaUnclearAmount(double unclearAmount)
	{
		double deltaUnclearAmount = 0.00;
		if (this._account.get_IsMultiCurrency())
		{
			CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(this.get_Currency().get_Id(), this._account.get_Currency().get_Id());
			deltaUnclearAmount = currencyRate.exchange(unclearAmount);
		}
		else
		{
			deltaUnclearAmount = unclearAmount;
		}
		this._account.deltaUnclearAmount(deltaUnclearAmount);
	}

	public void set_Necessary(double necessary)
	{
		this._necessary = necessary;
	}
}
