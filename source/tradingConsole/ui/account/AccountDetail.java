package tradingConsole.ui.account;

import framework.*;
import tradingConsole.framework.*;
import tradingConsole.ui.grid.*;
import java.util.Vector;
import tradingConsole.AppToolkit;
import tradingConsole.TradingItem;
import tradingConsole.ui.language.AccountSingleLanguage;
import javax.swing.SwingConstants;
import java.awt.Color;
import tradingConsole.ui.ColorSettings;
import tradingConsole.settings.PublicParametersManager;

public class AccountDetail
{
	private Account _owner;
	private AccountDetailCategory _type;
	private BindingSource _details;

	public AccountDetail(Account account, AccountDetailCategory type)
	{
		this._owner = account;
		this._type = type;
	}

	public String get_Caption()
	{
		return this.getCaption();
	}

	public String get_Value()
	{
		return this.getValue();
	}

	private String getValue()
	{
		tradingConsole.Account account = this._owner.get_RawAccount();
		short decimals = account.get_Currency().get_Decimals();
		if (this._type == AccountDetailCategory.AccountCurrency)
		{
			return account.get_Currency().get_Code();
		}
		else if (this._type == AccountDetailCategory.Balance)
		{
			return AppToolkit.format(account.get_Balance(), decimals);
		}
		else if (this._type == AccountDetailCategory.Necessary)
		{
			return AppToolkit.format(account.get_Necessary() - account.get_NecessaryForPartialPaymentPhysicalOrder(), decimals);
		}
		else if (this._type == AccountDetailCategory.NecessaryForPartialPaymentPhysicalOrder)
		{
			return AppToolkit.format(account.get_NecessaryForPartialPaymentPhysicalOrder(), decimals);
		}
		else if (this._type == AccountDetailCategory.TradePLFloat)
		{
			return AppToolkit.format(TradingItem.sum(account.get_FloatTradingItem()) - account.get_FloatTradingItem().get_ValueAsMargin(), decimals);
		}
		else if (this._type == AccountDetailCategory.ValueAsMargin)
		{
			return AppToolkit.format(account.get_FloatTradingItem().get_ValueAsMargin(), decimals);
		}
		else if (this._type == AccountDetailCategory.FrozenFund)
		{
				return AppToolkit.format(account.get_FrozenFund(), decimals);
		}
		else if (this._type == AccountDetailCategory.TotalPaidAmount)
		{
				return AppToolkit.format(account.get_TotalPaidAmount(), decimals);
		}
		else if (this._type == AccountDetailCategory.TotalUnrealisedSwap)
		{
			return AppToolkit.format(account.get_NotValuedTradingItem().get_Interest() + account.get_NotValuedTradingItem().get_Storage(), decimals);
		}
		else if (this._type == AccountDetailCategory.UnrealisedPL)
		{
			return AppToolkit.format(account.get_NotValuedTradingItem().get_Trade(), decimals);
		}
		else if (this._type == AccountDetailCategory.Equity)
		{
			return AppToolkit.format(account.get_Equity(), decimals);
		}
		else if (this._type == AccountDetailCategory.Usable)
		{
			return AppToolkit.format(account.get_Equity() - account.get_Necessary(), decimals);
		}
		else if (this._type == AccountDetailCategory.Unclear)
		{
			return AppToolkit.format(account.get_UnclearAmount(), decimals);
		}
		else if (this._type == AccountDetailCategory.CreditAmount)
		{
			return AppToolkit.format(account.get_CreditAmount(), decimals);
		}
		else if (this._type == AccountDetailCategory.Ratio)
		{
			return account.getRatioString();
		}
		else if (this._type == AccountDetailCategory.AlertLevel)
		{
			return tradingConsole.enumDefine.AlertLevel.getCaption(account.get_AlertLevel());
		}
		throw new IllegalStateException(this._type.name() + " is not supportted");
	}

	public AccountDetailCategory get_Type()
	{
		return this._type;
	}

	public BindingSource get_Details()
	{
		return this._details;
	}

	public void add(tradingConsole.AccountCurrency accountCurrency)
	{
		AccountCurrency accountCurrency2 = new AccountCurrency(this, accountCurrency);
		if (this._details == null)
		{
			this._details = new BindingSource();
			this._details.bind(new Vector(0), AccountCurrency.getPropertyDescriptors());
		}
		this._details.add(accountCurrency2);
		this.applyColor(accountCurrency2);
	}

	public void update(tradingConsole.AccountCurrency accountCurrency)
	{
		AccountCurrency accountCurrency2 = this.getAccountCurrency(accountCurrency.get_Currency().get_Id());
		this._details.update(accountCurrency2);
		this.applyColor(accountCurrency2);
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];

		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(AccountDetail.class, "Caption", true, null, "Caption", 200, SwingConstants.LEFT, null,
			Account.LightGray);
		propertyDescriptors[index++] = PropertyDescriptor.create(AccountDetail.class, "Value", true, null, "Value", 300, SwingConstants.RIGHT, null,
			Account.LightGray);

		return propertyDescriptors;
	}

	private void applyColor(AccountCurrency accountCurrency)
	{
		String value = accountCurrency.get_Value().replace(",", "");
		value = value.replaceAll("%", "");
		Color positiveColor = PublicParametersManager.useGreenAsRiseColor ? new Color(0,136,8) :  new Color(108, 108, 255);
		Color color = Double.parseDouble(value) > 0 ? (ColorSettings.useBlackAsBackground ? ColorSettings.Positive : positiveColor) : Color.RED;
		this._details.setForeground(accountCurrency, "Value", color);
	}

	private AccountCurrency getAccountCurrency(Guid currencyId)
	{
		for (int index = 0; index < this._details.getRowCount(); index++)
		{
			AccountCurrency accountCurrency = (AccountCurrency)this._details.getObject(index);
			if (accountCurrency.get_CurrencyId().equals(currencyId))
			{
				return accountCurrency;
			}
		}
		return null;
	}

	private String getCaption()
	{
		if (this._type == AccountDetailCategory.AccountCurrency)
		{
			return AccountSingleLanguage.CurrencyCode;
		}
		else if (this._type == AccountDetailCategory.Balance)
		{
			return AccountSingleLanguage.Balance;
		}
		else if (this._type == AccountDetailCategory.Necessary)
		{
			return AccountSingleLanguage.Necessary;
		}
		else if (this._type == AccountDetailCategory.NecessaryForPartialPaymentPhysicalOrder)
		{
			return AccountSingleLanguage.NecessaryForPartialPaymentPhysicalOrder;
		}
		else if (this._type == AccountDetailCategory.TradePLFloat)
		{
			return AccountSingleLanguage.TradePLFloat;
		}
		else if (this._type == AccountDetailCategory.TotalUnrealisedSwap)
		{
			return AccountSingleLanguage.TotalUnrealisedSwap;
		}
		else if (this._type == AccountDetailCategory.UnrealisedPL)
		{
			return AccountSingleLanguage.UnrealisedPL;
		}
		else if (this._type == AccountDetailCategory.Equity)
		{
			return AccountSingleLanguage.Equity;
		}
		else if (this._type == AccountDetailCategory.Usable)
		{
			return AccountSingleLanguage.Usable;
		}
		else if (this._type == AccountDetailCategory.Unclear)
		{
			return AccountSingleLanguage.UnclearAmount;
		}
		else if (this._type == AccountDetailCategory.CreditAmount)
		{
			return AccountSingleLanguage.CreditAmount;
		}
		else if (this._type == AccountDetailCategory.Ratio)
		{
			return AccountSingleLanguage.Ratio;
		}
		else if (this._type == AccountDetailCategory.AlertLevel)
		{
			return AccountSingleLanguage.AlertLevel;
		}
		else if (this._type == AccountDetailCategory.ValueAsMargin)
		{
			return AccountSingleLanguage.ValueAsMargin;
		}
		else if (this._type == AccountDetailCategory.TotalPaidAmount)
		{
			return AccountSingleLanguage.TotalPledge;
		}
		else if (this._type == AccountDetailCategory.FrozenFund)
		{
			return AccountSingleLanguage.FrozenFund;
		}

		throw new IllegalStateException(this._type.name() + " is not supportted");
	}
}
