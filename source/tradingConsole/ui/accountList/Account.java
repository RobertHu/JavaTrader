package tradingConsole.ui.accountList;

import java.awt.*;
import javax.swing.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import java.util.Vector;

public class Account
{
	public final static Color LightGray = (Color)UIManager.getColor("ScrollPane.background");
	public final static String[] propertyNames = new String[]{"IsSelected", "Code", "Currency", "Balance", "TotalPaidAmount",
		"Necessary", "NecessaryForPartialPaymentPhysicalOrder", "TradePLFloat", "TotalUnrealisedSwap",	"UnrealisedPL",	"Equity", "Usable", "UnclearAmount",
		"CreditAmount", "Ratio", "ValueAsMargin", "FrozenFund", "AlertLevel"};

	private Color[] foregroundColors = new Color[propertyNames.length];

	private tradingConsole.Account _rawAccount;
	private BindingSource _details;
	private short decimals;

	public Account(tradingConsole.Account account)
	{
		this._rawAccount = account;
		this.decimals = account.get_Currency().get_Decimals();
		this._details = new BindingSource();
		this._details.bind(new Vector(0), AccountCurrency.getPropertyDescriptors());
	}

	boolean needShowStatus()
	{
		return this._rawAccount.needShowStatus();
	}

	boolean hasDetail()
	{
		return this._details != null && this._details.getRowCount() > 0;
	}

	BindingSource get_Details()
	{
		return this._details;
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[propertyNames.length];
		int index = 0;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], false, null, "*", 40, SwingConstants.RIGHT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Code, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.CurrencyCode, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Balance, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.TotalPaidAmount, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Necessary, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.NecessaryForPartialPaymentPhysicalOrder, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.TradePLFloat, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.TotalUnrealisedSwap, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.UnrealisedPL, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Equity, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Usable, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.UnclearAmount, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.CreditAmount, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.Ratio, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.ValueAsMargin, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.FrozenFund, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;
		propertyDescriptors[index] = PropertyDescriptor.create(Account.class, propertyNames[index], true, null, AccountSingleLanguage.AlertLevel, 60, SwingConstants.LEFT, null, Account.LightGray);
		index++;

		return propertyDescriptors;
	}

	public void applyColor(BindingSource bindingSource)
	{
		int index = 0;
		for(String name : Account.propertyNames)
		{
			bindingSource.setForeground(this, name, this.foregroundColors[index++]);
		}
	}

	public boolean get_IsSelected()
	{
		this.foregroundColors[0] = null;
		return this._rawAccount.get_Select();
	}

	public void set_IsSelected(boolean value)
	{
		this._rawAccount.set_Select(value);
	}

	public String get_Code()
	{
		this.foregroundColors[1] = null;
		return this._rawAccount.get_Code();
	}

	public String get_Currency()
	{
		this.foregroundColors[2] = null;
		return this._rawAccount.get_Currency().get_Code();
	}

	public String get_Balance()
	{
		this.foregroundColors[3] = NumericColor.getColor(this._rawAccount.get_Balance(), true);
		return AppToolkit.format(this._rawAccount.get_Balance(), decimals);
	}

	public String get_TotalPaidAmount()
	{
		this.foregroundColors[4] = NumericColor.getColor(this._rawAccount.get_TotalPaidAmount(), true);
		return AppToolkit.format(this._rawAccount.get_TotalPaidAmount(), decimals);
	}

	public String get_Necessary()
	{
		this.foregroundColors[5] = NumericColor.getColor(this._rawAccount.get_Necessary(), true);
		return AppToolkit.format(this._rawAccount.get_Necessary(), decimals);
	}

	public String get_NecessaryForPartialPaymentPhysicalOrder()
	{
		this.foregroundColors[6] = NumericColor.getColor(this._rawAccount.get_NecessaryForPartialPaymentPhysicalOrder(), true);
		return AppToolkit.format(this._rawAccount.get_NecessaryForPartialPaymentPhysicalOrder(), decimals);
	}

	public String get_TradePLFloat()
	{
		double value
			= TradingItem.sum(this._rawAccount.get_FloatTradingItem()) - this._rawAccount.get_FloatTradingItem().get_ValueAsMargin();
		this.foregroundColors[7] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_TotalUnrealisedSwap()
	{
		double value = this._rawAccount.get_NotValuedTradingItem().get_Interest() + this._rawAccount.get_NotValuedTradingItem().get_Storage();
		this.foregroundColors[8] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_UnrealisedPL()
	{
		this.foregroundColors[9] = NumericColor.getColor(this._rawAccount.get_NotValuedTradingItem().get_Trade(), true);
		return AppToolkit.format(this._rawAccount.get_NotValuedTradingItem().get_Trade(), decimals);
	}

	public String get_Equity()
	{
		this.foregroundColors[10] = NumericColor.getColor(this._rawAccount.get_Equity(), true);
		return AppToolkit.format(this._rawAccount.get_Equity(), decimals);
	}

	public String get_Usable()
	{
		double value = this._rawAccount.get_Equity() - this._rawAccount.get_Necessary();
		this.foregroundColors[11] = NumericColor.getColor(value, true);
		return AppToolkit.format(value, decimals);
	}

	public String get_UnclearAmount()
	{
		this.foregroundColors[12] = NumericColor.getColor(this._rawAccount.get_UnclearAmount(), true);
		return AppToolkit.format(this._rawAccount.get_UnclearAmount(), decimals);
	}

	public String get_CreditAmount()
	{
		this.foregroundColors[13] = NumericColor.getColor(this._rawAccount.get_CreditAmount(), true);
		return AppToolkit.format(this._rawAccount.get_CreditAmount(), decimals);
	}

	public String get_Ratio()
	{
		if(this._rawAccount.get_Necessary() == 0)
		{
			this.foregroundColors[14] = null;
		}
		else
		{
			double ratio = (this._rawAccount.get_Equity() / this._rawAccount.get_Necessary());
			this.foregroundColors[14] = NumericColor.getColor(ratio, true);
		}
		return this._rawAccount.getRatioString();
	}

	public String get_AlertLevel()
	{
		this.foregroundColors[15] = AlertLevel.getColor(this._rawAccount.get_AlertLevel());
		return tradingConsole.enumDefine.AlertLevel.getCaption(this._rawAccount.get_AlertLevel());
	}

	public String get_ValueAsMargin()
	{
		this.foregroundColors[15] = NumericColor.getColor(this._rawAccount.get_FloatTradingItem().get_ValueAsMargin(), true);
		return AppToolkit.format(this._rawAccount.get_FloatTradingItem().get_ValueAsMargin(), decimals);
	}

	public String get_FrozenFund()
	{
		this.foregroundColors[16] = NumericColor.getColor(this._rawAccount.get_FrozenFund(), true);
		return AppToolkit.format(this._rawAccount.get_FrozenFund(), decimals);
	}

	public Guid get_Id()
	{
		return this._rawAccount.get_Id();
	}

	public void updateCurrencies()
	{
		if(this._details == null || this._details.getRowCount() == 0) return;

		for(int index = 0; index < this._details.getRowCount();  index++)
		{
			AccountCurrency accountCurrency = (AccountCurrency)this._details.getObject(index);
			this._details.update(accountCurrency);
			accountCurrency.applyColor(this._details);
		}
	}

	public void add(tradingConsole.AccountCurrency accountCurrency)
	{
		AccountCurrency accountCurrency2 = new AccountCurrency(accountCurrency);
		this._details.add(accountCurrency2);
		accountCurrency2.applyColor(this._details);
	}

	public void update(tradingConsole.AccountCurrency accountCurrency)
	{
		for(int index = 0; index < this._details.getRowCount();  index++)
		{
			AccountCurrency accountCurrency2 = (AccountCurrency)this._details.getObject(index);
			if(accountCurrency2.get_CurrencyId().equals(accountCurrency.get_Currency().get_Id()))
			{
				this._details.update(accountCurrency2);
				accountCurrency2.applyColor(this._details);
				break;
			}
		}
	}
}
