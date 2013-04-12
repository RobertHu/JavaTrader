package tradingConsole.ui;

import java.util.Iterator;
import java.util.HashMap;

import framework.Guid;
import framework.DateTime;

import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.settings.MakeOrderAccount;
import tradingConsole.ui.language.Language;
import tradingConsole.Transaction;
import tradingConsole.enumDefine.AccountType;
import tradingConsole.Account;
import tradingConsole.Order;
import tradingConsole.settings.Parameter;
import tradingConsole.Price;
import java.math.BigDecimal;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.grid.IPropertyChangingListener;

public class MakeAssignOrder extends MakeSpotTradeOrder
{
	private Transaction _agentTransaction;

	public Transaction get_AgentTransaction()
	{
		return this._agentTransaction;
	}

	public HashMap<Guid, MakeOrderAccount> get_MakeOrderAccounts()
	{
		return this._makeOrderAccounts;
	}

	public MakeOrderAccount get_MakeOrderAccount(Guid accountId)
	{
		return this._makeOrderAccounts.get(accountId);
	}

	public MakeAssignOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
		this._instrument = instrument;
	}

	public static Transaction getFirstAgentTransaction(SettingsManager settingsManager)
	{
		DateTime minExecuteTime = DateTime.maxValue;
		Transaction transaction = null;
		for (Iterator<Account> iterator = settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Type() != AccountType.Agent)
			{
				continue;
			}

			for (Iterator<Transaction> iterator2 = account.get_Transactions().values().iterator(); iterator2.hasNext();)
			{
				Transaction transaction2 = iterator2.next();
				DateTime executeTime = transaction2.get_ExecuteTime();
				if ((executeTime != null && executeTime != DateTime.maxValue)	&& !executeTime.after(minExecuteTime))
				{
					minExecuteTime = executeTime;
					for (Iterator<Order> iterator3 = transaction2.get_Orders().values().iterator(); iterator3.hasNext(); )
					{
						Order order = iterator3.next();
						if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
						{
							transaction = transaction2;
						}
					}
				}
			}
		}

		return transaction;
	}

	public Object[] isAccept()
	{
		Object[] result = new Object[]
			{false, ""}; //isAccept, message

		if (MakeOrder.isDisallowTradeSetting(this._settingsManager))
		{
			result[1] = Language.DisallowTradePrompt;
			return result;
		}
		if (!this._settingsManager.get_IsExistsAgentAccount())
		{
			return result;
		}
		Transaction transaction = MakeAssignOrder.getFirstAgentTransaction(this._settingsManager);
		if (transaction == null)
		{
			return result;
		}
		this._agentTransaction = transaction;

		this.setMakeOrderAccounts();
		result[0] = (this._makeOrderAccounts.size() > 0);

		return result;
	}

	private void setMakeOrderAccounts()
	{
		if (this._makeOrderAccounts.size() > 0)
		{
			return;
		}
		boolean isBuy = false;
		Price assigningOrderExecutePrice = null;
		for (Iterator<Order> iterator = this._agentTransaction.get_Orders().values().iterator(); iterator.hasNext(); )
		{
			Order assigningOrder = iterator.next();
			isBuy = assigningOrder.get_IsBuy();
			assigningOrderExecutePrice = assigningOrder.get_ExecutePrice();
			break;
		}
		int allowFreeAgent = this._settingsManager.get_Customer().get_AllowFreeAgent();
		Guid userId = this._tradingConsole.get_LoginInformation().get_CustomerId();
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();

			boolean isAllowTrade = account.getIsAllowTrade();
			boolean isValidAccountForTradePolicy = account.isValidAccountForTradePolicy(this._instrument, false);
			if (account.get_Type() == AccountType.Agent
				|| !isAllowTrade
				|| !isValidAccountForTradePolicy)
			{
				continue;
			}
			if (! (!Parameter.colHiddenLiqLotForAutoClose
				   || (Parameter.colHiddenLiqLotForAutoClose && !account.get_IsAutoClose())))
			{
				continue;
			}
			boolean isPut = false;
			if (allowFreeAgent == 0)
			{
				if (account.get_IsLocked()
					&& (account.get_AgentId() != null && account.get_AgentId().equals(userId))
					&& account.isAssigned(this._agentTransaction.get_ExecuteTime()))
				{
					isPut = true;
				}
			}
			else
			{
				isPut = true;
			}
			if (isPut)
			{
				MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
				makeOrderAccount.set_IsBuyForCurrent(isBuy);
				makeOrderAccount.set_BuySetPrice(assigningOrderExecutePrice);
				makeOrderAccount.set_SellSetPrice(assigningOrderExecutePrice);
				this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
			}
		}

		//init structure
		super.set_makeOrderAccounts(this._makeOrderAccounts);
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn, IPropertyChangingListener propertyChangingListener)
	{
		this.initialize2(grid, ascend, caseOn, false,false,0,0,propertyChangingListener);
	}

	public void showUi(OpenContractForm openContractForm)
	{
		AssignOrderForm assignOrderUi = new AssignOrderForm(this._tradingConsole, this._settingsManager, this,openContractForm);
		assignOrderUi.show();
	}
}
