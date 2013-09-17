package tradingConsole.ui.account;

import framework.*;
import tradingConsole.*;
import tradingConsole.framework.*;
import javax.swing.SwingConstants;
import java.awt.Color;

public class AccountCurrency
{
	private AccountDetail _owner;
	private tradingConsole.AccountCurrency _rawAccountCurrency;

	public AccountCurrency(AccountDetail accountDetail, tradingConsole.AccountCurrency rawAccountCurrency)
	{
		this._owner = accountDetail;
		this._rawAccountCurrency = rawAccountCurrency;
	}

	public String get_Currency()
	{
		return this._rawAccountCurrency.get_Currency().get_Code();
	}

	public String get_Value()
	{
		return this.getValue();
	}

	public Guid get_CurrencyId()
	{
		return this._rawAccountCurrency.get_Currency().get_Id();
	}

	private String getValue()
	{
		tradingConsole.AccountCurrency accountCurrency = this._rawAccountCurrency;
		short decimals = accountCurrency.get_Currency().get_Decimals();
		switch(this._owner.get_Type())
		{
			case Balance:
				return AppToolkit.format(accountCurrency.get_Balance(), decimals);
			case Necessary:
				return AppToolkit.format(accountCurrency.get_Necessary(), decimals);
			case TradePLFloat:
				return AppToolkit.format(TradingItem.sum(accountCurrency.get_FloatTradingItem()) - accountCurrency.get_FloatTradingItem().get_ValueAsMargin(), decimals);
			case ValueAsMargin:
				return AppToolkit.format(accountCurrency.get_FloatTradingItem().get_ValueAsMargin(), decimals);
			case FrozenFund:
				return AppToolkit.format(accountCurrency.get_FrozenFund(), decimals);
			case TotalUnrealisedSwap:
				return AppToolkit.format(accountCurrency.get_NotValuedTradingItem().get_Interest() + accountCurrency.get_NotValuedTradingItem().get_Storage(), decimals);
			case UnrealisedPL:
				return AppToolkit.format(accountCurrency.get_NotValuedTradingItem().get_Trade(), decimals);
			case Unclear:
				return AppToolkit.format(accountCurrency.get_UnclearAmount(), decimals);
			case Equity:
				return AppToolkit.format(accountCurrency.get_Equity(), decimals);
			case Usable:
				return AppToolkit.format(accountCurrency.get_Usable(), decimals);
		}
		throw new IllegalStateException(this._owner.get_Type().name() + " is not supportted");
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(AccountCurrency.class, "Currency", true, null, "Currency", 200, SwingConstants.LEFT, null, Account.LightGray);
		propertyDescriptors[index++] = PropertyDescriptor.create(AccountCurrency.class, "Value", true, null, "Value", 300, SwingConstants.RIGHT, null, Account.LightGray);
		return propertyDescriptors;
	}
}
