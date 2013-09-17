package tradingConsole.ui.accountList;

import java.awt.*;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.framework.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import framework.Guid;

public class AccountCurrency
{
	public final static String[] propertyNames = new String[]{"Currency", "Balance",
		"Necessary", "TradePLFloat", "TotalUnrealisedSwap",	"UnrealisedPL",	"Equity", "Usable", "UnclearAmount",
		"ValueAsMargin", "FrozenFund"};

	private Color[] foregroundColors = new Color[propertyNames.length];
	private tradingConsole.AccountCurrency _rawAccountCurrency;
	private short decimals;

	public AccountCurrency(tradingConsole.AccountCurrency rawAccountCurrency)
	{
		this._rawAccountCurrency = rawAccountCurrency;
		this.decimals = this._rawAccountCurrency.get_Currency().get_Decimals();
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[propertyNames.length];
		int index = 0;

		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.CurrencyCode, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.Balance, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.Necessary, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.TradePLFloat, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.TotalUnrealisedSwap, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.UnrealisedPL, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.Equity, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.Usable, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.UnclearAmount, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.ValueAsMargin, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(AccountCurrency.class, propertyNames[index], true, null, AccountSingleLanguage.FrozenFund, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;

		return propertyDescriptors;
	}

	public String get_Currency()
	{
		this.foregroundColors[0] = null;
		return this._rawAccountCurrency.get_Currency().get_Code();
	}

	public String get_Balance()
	{
		this.foregroundColors[1] = NumericColor.getColor(this._rawAccountCurrency.get_Balance(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_Balance(), decimals);
	}

	public String get_Necessary()
	{
		this.foregroundColors[2] = NumericColor.getColor(this._rawAccountCurrency.get_Necessary(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_Necessary(), decimals);
	}

	public String get_TradePLFloat()
	{
		double value
			= TradingItem.sum(this._rawAccountCurrency.get_FloatTradingItem()) - this._rawAccountCurrency.get_FloatTradingItem().get_ValueAsMargin();
		this.foregroundColors[3] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_TotalUnrealisedSwap()
	{
		double value = this._rawAccountCurrency.get_NotValuedTradingItem().get_Interest() + this._rawAccountCurrency.get_NotValuedTradingItem().get_Storage();
		this.foregroundColors[4] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_UnrealisedPL()
	{
		this.foregroundColors[5] = NumericColor.getColor(this._rawAccountCurrency.get_NotValuedTradingItem().get_Trade(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_NotValuedTradingItem().get_Trade(), decimals);
	}

	public String get_Equity()
	{
		this.foregroundColors[6] = NumericColor.getColor(this._rawAccountCurrency.get_Equity(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_Equity(), decimals);
	}

	public String get_Usable()
	{
		double value = this._rawAccountCurrency.get_Equity() - this._rawAccountCurrency.get_Necessary();
		this.foregroundColors[7] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_UnclearAmount()
	{
		this.foregroundColors[8] = NumericColor.getColor(this._rawAccountCurrency.get_UnclearAmount(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_UnclearAmount(), decimals);
	}

	public String get_ValueAsMargin()
	{
		this.foregroundColors[9] = NumericColor.getColor(this._rawAccountCurrency.get_FloatTradingItem().get_ValueAsMargin(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_FloatTradingItem().get_ValueAsMargin(), decimals);
	}

	public String get_FrozenFund()
	{
		this.foregroundColors[10] = NumericColor.getColor(this._rawAccountCurrency.get_FrozenFund(), true);
		return AppToolkit.format(this._rawAccountCurrency.get_FrozenFund(), decimals);
	}

	public void applyColor(BindingSource bindingSource)
	{
		int index = 0;
		for(String name : AccountCurrency.propertyNames)
		{
			bindingSource.setForeground(this, name, this.foregroundColors[index++]);
		}
	}

	public Guid get_CurrencyId()
	{
		return this._rawAccountCurrency.get_Currency().get_Id();
	}
}
