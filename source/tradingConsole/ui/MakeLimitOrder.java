package tradingConsole.ui;

import java.util.HashMap;
import java.util.Iterator;

import framework.Guid;

import tradingConsole.Instrument;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.enumDefine.AccountType;
import tradingConsole.ui.language.Language;
import tradingConsole.enumDefine.OrderTypeMask;
import tradingConsole.TradingConsole;
import tradingConsole.settings.MakeOrderAccount;
import tradingConsole.Price;
import java.math.BigDecimal;

public class MakeLimitOrder extends MakeSpotTradeOrder
{
	public MakeLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
	}

	public void setMakeOrderAccounts()
	{
		Price buySetPrice = this._instrument.get_LastQuotation().getBuy();
		Price sellSetPrice = this._instrument.get_LastQuotation().getSell();
		Object[] results = MakeOrder.isAllowTime(this.get_Instrument());
		boolean isAllowTime = (Boolean)(results[0]) && !(Boolean)results[2];

		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if(!isAllowTime && !this.get_Instrument().get_CanPlacePendingOrderAtAnyTime())
			{
				continue;
			}

			if (!account.isSelectAccount())
			{
				continue;
			}
			if (!account.getIsAllowTrade())
			{
				continue;
			}
			if (!account.isValidAccountForTradePolicy(this._instrument, true))
			{
				continue;
			}

			if ( (!account.get_Type().equals(AccountType.Agent) && !account.get_IsLocked()))
				//|| (account.get_Type().equals(AccountType.Agent) && !account.getIsMustAssignOrder()))
			{
				MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
				makeOrderAccount.set_BuySetPrice(buySetPrice);
				makeOrderAccount.set_SellSetPrice(sellSetPrice);
				this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
			}
		}
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.Limit);
	}

	public Object[] isAccept(OrderTypeMask orderTypeMask)
	{
		Object[] result = new Object[2];
		result[0] = false;
		result[1] = "";

		if (MakeOrder.isDisallowTradeSetting(this._settingsManager) || this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) <= 0)
		{
			result[1] = Language.DisallowTradePrompt;
			return result;
		}
		if (!MakeLimitOrder.isAllowOrderTypeMask(this._instrument, orderTypeMask))
		{
			result[1] = Language.TradeConsoleGetOperateHtmlPageAlert3;
			return result;
		}

		Object[] result2;
		if(orderTypeMask == OrderTypeMask.Limit
			|| orderTypeMask == OrderTypeMask.MarketOnOpen
			 || orderTypeMask == OrderTypeMask.MarketOnClose)
		{
			result2 = this.isAllowTime(this._settingsManager, this._instrument);
		}
		else
		{
			result2 = this.isAcceptTime();
		}
		if (! (Boolean)result2[0])
		{
			result[1] = result2[1].toString();
			return result;
		}

		Price buySetPrice = this._instrument.get_LastQuotation().getBuy();
		Price sellSetPrice = this._instrument.get_LastQuotation().getSell();
		if (buySetPrice==null || sellSetPrice==null)
		{
			result[1] = Language.OrderLMTPageorderValidAlert1;
			return result;
		}

		this.setMakeOrderAccounts();
		if (this._makeOrderAccounts.values().size() <= 0)
		{
			result[1] = Language.TradeConsoleGetOperateHtmlPageAlert2;
		}
		result[0] = (this._makeOrderAccounts.size() > 0);

		return result;
	}

}
