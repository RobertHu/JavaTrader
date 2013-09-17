package tradingConsole.ui.account;

import framework.*;
import tradingConsole.*;
import tradingConsole.ui.grid.*;
import com.jidesoft.grid.HierarchicalTableModel;
import tradingConsole.framework.PropertyDescriptor;
import java.util.Vector;
import tradingConsole.ui.language.AccountSingleLanguage;
import javax.swing.SwingConstants;
import java.awt.Color;
import javax.swing.UIManager;
import java.util.Enumeration;
import tradingConsole.enumDefine.AlertLevel;
import javax.swing.JTable;
import tradingConsole.ui.colorHelper.NumericColor;
import tradingConsole.ui.ColorSettings;
import java.util.ArrayList;

public class Account
{
	public final static Color LightGray = (Color)UIManager.getColor("ScrollPane.background");

	private tradingConsole.Account _rawAccount;
	private BindingSource _details;

	public Account(tradingConsole.Account account)
	{
		this._rawAccount = account;
		this._rawAccount.set_Select(!AccountSelectionStorage.Instance.isUnselected(this));
		this.fillDetails(account);
	}

	public Guid get_Id()
	{
		return this._rawAccount.get_Id();
	}

	public String get_Code()
	{
		if(this._rawAccount.get_TradingConsole().get_SettingsManager().get_SystemParameter().get_ShowAccountName())
		{
			return this._rawAccount.get_Code() + " (" + this._rawAccount.get_Name() + ")";
		}
		else
		{
			return this._rawAccount.get_Code();
		}
	}

	public boolean get_IsSelected()
	{
		return this._rawAccount.get_Select();
	}

	public void set_IsSelected(boolean value)
	{
		if(value)
		{
			AccountSelectionStorage.Instance.remove(this);
		}
		else
		{
			int unselectedAccountCount = 0;
			for(int index = 0; index < this._rawAccount.get_TradingConsole().get_AccountBindingManager().get_BindingSource().getRowCount(); index++)
			{
				Account account2 = (Account) this._rawAccount.get_TradingConsole().get_AccountBindingManager().get_BindingSource().getObject(index);
				if(AccountSelectionStorage.Instance.isUnselected(account2))
				{
					unselectedAccountCount++;
				}
			}

			if(unselectedAccountCount >= (this._rawAccount.get_TradingConsole().get_SettingsManager().get_Accounts().size() - 1))
			{
				this._rawAccount.get_TradingConsole().get_AccountBindingManager().update(this._rawAccount);
				return;
			}
			AccountSelectionStorage.Instance.add(this);
		}
		this._rawAccount.set_Select(value);
	}

	public boolean get_IsLocked()
	{
		return this._rawAccount.get_IsLocked();
	}

	public BindingSource get_Details()
	{
		return this._details;
	}

	tradingConsole.Account get_RawAccount()
	{
		return this._rawAccount;
	}

	boolean needShowStatus()
	{
		return this._rawAccount.needShowStatus();
	}

	public void updateDetails()
	{
		ArrayList<AccountDetail> shouldRemoveDetails = new ArrayList<AccountDetail>();
		ArrayList<AccountDetail> shouldAddDetails = new ArrayList<AccountDetail>();

		for(AccountDetailCategory accountDetailType : AccountDetailCategory.values())
		{
			AccountDetail accountDetail = this.getAccountDetail(accountDetailType);
			if(accountDetail != null)
			{
				this._details.update(accountDetail);

				if((accountDetailType == AccountDetailCategory.TotalUnrealisedSwap && (this._rawAccount.get_NotValuedTradingItem().get_Interest() + this._rawAccount.get_NotValuedTradingItem().get_Storage()) == 0)
				   || (accountDetailType == AccountDetailCategory.UnrealisedPL && this._rawAccount.get_NotValuedTradingItem().get_Trade() == 0)
				   ||(accountDetailType == AccountDetailCategory.CreditAmount && this._rawAccount.get_CreditAmount() == 0)
				   ||(accountDetailType == AccountDetailCategory.FrozenFund && this._rawAccount.get_FrozenFund() == 0)
				   ||(accountDetailType== AccountDetailCategory.Usable && this._rawAccount.get_Necessary() == 0)
				   ||(accountDetailType == AccountDetailCategory.ValueAsMargin && this._rawAccount.get_FloatTradingItem().get_ValueAsMargin() == 0))
				{
					shouldRemoveDetails.add(accountDetail);
				}
				this.applyColor(accountDetail);
			}
			else
			{
				if (accountDetailType == AccountDetailCategory.TotalUnrealisedSwap)
				{
					if ( (this._rawAccount.get_NotValuedTradingItem().get_Interest() + this._rawAccount.get_NotValuedTradingItem().get_Storage()) != 0)
					{
						shouldAddDetails.add(this.createDetail(AccountDetailCategory.TotalUnrealisedSwap));
					}
				}
				else if (accountDetailType == AccountDetailCategory.UnrealisedPL)
				{
					if (this._rawAccount.get_NotValuedTradingItem().get_Trade() != 0)
					{
						shouldAddDetails.add(this.createDetail(AccountDetailCategory.UnrealisedPL));
					}
				}
				else if (accountDetailType == AccountDetailCategory.CreditAmount)
				{
					if (this._rawAccount.get_CreditAmount() != 0)
					{
						shouldAddDetails.add(this.createDetail(accountDetailType));
					}
				}
				else if (accountDetailType == AccountDetailCategory.ValueAsMargin)
				{
					if (this._rawAccount.get_FloatTradingItem().get_ValueAsMargin() != 0)
					{
						shouldAddDetails.add(this.createDetail(accountDetailType));
					}
				}
				else if (accountDetailType == AccountDetailCategory.FrozenFund)
				{
					if (this._rawAccount.get_FrozenFund() != 0)
					{
						shouldAddDetails.add(this.createDetail(accountDetailType));
					}
				}
				else if (accountDetailType== AccountDetailCategory.Usable)
				{
					if (this._rawAccount.get_Necessary()!= 0)
					{
						shouldAddDetails.add(this.createDetail(accountDetailType));
					}
				}
			}
		}

		for(AccountDetail accountDetail : shouldRemoveDetails)
		{
			this._details.remove(accountDetail);
		}

		for(AccountDetail accountDetail : shouldAddDetails)
		{
			this._details.insert(this._details.getRowCount() - 1, accountDetail);
			this.applyColor(accountDetail);
		}
	}

	private AccountDetail createDetail(AccountDetailCategory accountDetailCategory)
	{
		AccountDetail accountDetail = new AccountDetail(this, accountDetailCategory);

		for (tradingConsole.AccountCurrency accountCurrency : accountCurrencies)
		{
			accountDetail.add(accountCurrency);
		}
		return accountDetail;
	}

	private ArrayList<tradingConsole.AccountCurrency> accountCurrencies = new ArrayList<tradingConsole.AccountCurrency>();
	public void add(tradingConsole.AccountCurrency accountCurrency)
	{
		accountCurrencies.add(accountCurrency);
		for(AccountDetailCategory accountDetailType : AccountDetailCategory.values())
		{
			if(this.hasDetails(accountDetailType))
			{
				AccountDetail accountDetail = this.getAccountDetail(accountDetailType);
				if(accountDetail != null)
				{
					accountDetail.add(accountCurrency);
				}
			}
		}
	}

	public void updateCurrencies()
	{
		for(tradingConsole.AccountCurrency accountCurrency : this.accountCurrencies)
		{
			this.update(accountCurrency);
		}
	}

	public void update(tradingConsole.AccountCurrency accountCurrency)
	{
		for(AccountDetailCategory accountDetailType : AccountDetailCategory.values())
		{
			if(this.hasDetails(accountDetailType))
			{
				AccountDetail accountDetail = this.getAccountDetail(accountDetailType);
				if(accountDetail != null)
				{
					this._details.update(accountDetail);
					accountDetail.update(accountCurrency);
				}
			}
		}
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(Account.class, "Code", true, null, AccountSingleLanguage.Code, 300, SwingConstants.LEFT, null, Account.LightGray);
		propertyDescriptors[index++] = PropertyDescriptor.create(Account.class, "IsSelected", false, null, "*", 20, SwingConstants.RIGHT, null, Account.LightGray);
		return propertyDescriptors;
	}

	private void fillDetails(tradingConsole.Account account)
	{
		HierarchicalTableModel hierarchicalTableModel = new HierarchicalTableModel()
		{
			public boolean hasChild(int row)
			{
				AccountDetail accountDetail = (AccountDetail)_details.getObject(row);
				return hasDetails(accountDetail.get_Type()) && accountDetail.get_Details() != null && accountDetail.get_Details().getRowCount() > 1;
			}

			public boolean isHierarchical(int row)
			{
				AccountDetail accountDetail = (AccountDetail)_details.getObject(row);
				return hasDetails(accountDetail.get_Type()) && accountDetail.get_Details() != null && accountDetail.get_Details().getRowCount() > 1;
			}

			public Object getChildValueAt(int row)
			{
				AccountDetail accountDetail = (AccountDetail)_details.getObject(row);
				return accountDetail.get_Details();
			}

			public boolean isExpandable(int row)
			{
				AccountDetail accountDetail = (AccountDetail)_details.getObject(row);
				return hasDetails(accountDetail.get_Type()) && accountDetail.get_Details() != null && accountDetail.get_Details().getRowCount() > 1;
			}
		};
		this._details = new BindingSource(hierarchicalTableModel);
		if(ColorSettings.useBlackAsBackground)
		{
			this._details.useBlackAsBackground();
		}
		else
		{
			this._details.setBackground(Account.LightGray);
		}
		PropertyDescriptor[] propertyDescriptors = AccountDetail.getPropertyDescriptors();
		this._details.bind(new Vector(0), propertyDescriptors);

		for(AccountDetailCategory accountDetailType : AccountDetailCategory.values())
		{
			if ((accountDetailType == AccountDetailCategory.Unclear && account.get_UnclearAmount() == 0)
				|| (accountDetailType == AccountDetailCategory.CreditAmount && account.get_CreditAmount() == 0)
				|| (accountDetailType == AccountDetailCategory.TotalUnrealisedSwap && (account.get_NotValuedTradingItem().get_Interest() + account.get_NotValuedTradingItem().get_Storage()) == 0)
				|| (accountDetailType == AccountDetailCategory.UnrealisedPL && account.get_NotValuedTradingItem().get_Trade() == 0)
				|| (accountDetailType == AccountDetailCategory.ValueAsMargin && account.get_FloatTradingItem().get_ValueAsMargin() == 0))
			{
				continue;
			}
			AccountDetail accountDetail = new AccountDetail(this, accountDetailType);
			this._details.add(accountDetail);
			this.applyColor(accountDetail);
		}
	}

	private void applyColor(AccountDetail accountDetail)
	{
		if (accountDetail.get_Type() == AccountDetailCategory.AlertLevel)
		{
			Color color = AlertLevel.getColor(AlertLevel.parse(accountDetail.get_Value()));
			this._details.setForeground(accountDetail, "Value", color);
		}
		else
		{
			try
			{
				String value = accountDetail.get_Value().replace(",", "");
				value = value.replaceAll("%", "");
				Color color = NumericColor.getColor(Double.parseDouble(value), true);
				this._details.setForeground(accountDetail, "Value", color);
			}
			catch (NumberFormatException ex)
			{
			}
		}
	}

	private AccountDetail getAccountDetail(AccountDetailCategory accountDetailType)
	{
		for(int index = 0; index < this._details.getRowCount(); index++)
		{
			AccountDetail accountDetail = (AccountDetail)this._details.getObject(index);
			if(accountDetail.get_Type().equals(accountDetailType))
			{
				return accountDetail;
			}
		}
		return null;
	}

	private boolean hasDetails(AccountDetailCategory accountDetailCategory)
	{
		return accountDetailCategory == AccountDetailCategory.Balance
			|| accountDetailCategory == AccountDetailCategory.Necessary
			|| accountDetailCategory == AccountDetailCategory.TradePLFloat
			|| accountDetailCategory == AccountDetailCategory.FrozenFund
			|| accountDetailCategory == AccountDetailCategory.ValueAsMargin
			|| (accountDetailCategory == AccountDetailCategory.TotalUnrealisedSwap)
			|| (accountDetailCategory == AccountDetailCategory.UnrealisedPL)
			|| accountDetailCategory == AccountDetailCategory.Unclear
			|| accountDetailCategory == AccountDetailCategory.Equity
			|| accountDetailCategory == AccountDetailCategory.Usable;
	}

	public static class ToolTipProvider implements IToolTipProvider
	{
		private JTable _owner;

		public ToolTipProvider(JTable owner)
		{
			this._owner = owner;
		}

		public boolean showToolTip(int row, int column)
		{
			return this._owner.getValueAt(row, 0).toString().equalsIgnoreCase(AccountSingleLanguage.AlertLevel)
				&& this._owner.getValueAt(row, 1) != null
				&& this._owner.getValueAt(row, 1).toString().length() != 0;
		}
	}
}
