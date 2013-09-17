package tradingConsole.ui.accountList;

import java.util.*;

import com.jidesoft.grid.*;
import framework.*;
import tradingConsole.*;
import tradingConsole.ui.*;
import tradingConsole.ui.grid.*;

public class BindingManager
{
	private BindingSource _bindingSource;
	private Customer _customer;

	public BindingManager(Customer customer)
	{
		this._customer = customer;

		HierarchicalTableModel hierarchicalTableModel = new HierarchicalTableModel()
		{
			public boolean hasChild(int row)
			{
				if(_customer.get_IsNoShowAccountStatus()) return false;
				if(row >= _bindingSource.getRowCount()) return false;
				Account account = (Account)_bindingSource.getObject(row);
				return account.needShowStatus() && account.hasDetail();
			}

			public boolean isHierarchical(int row)
			{
				if(_customer.get_IsNoShowAccountStatus()) return false;
				if(row >= _bindingSource.getRowCount()) return false;
				Account account = (Account)_bindingSource.getObject(row);
				return account.needShowStatus() && account.hasDetail();
			}

			public Object getChildValueAt(int row)
			{
				if(_customer.get_IsNoShowAccountStatus()) return false;
				if(row >= _bindingSource.getRowCount()) return null;
				Account account = (Account)_bindingSource.getObject(row);
				return account.get_Details();
			}

			public boolean isExpandable(int row)
			{
				if(_customer.get_IsNoShowAccountStatus()) return false;
				if(row >= _bindingSource.getRowCount()) return false;
				Account account = (Account)_bindingSource.getObject(row);
				return account.needShowStatus()  && account.hasDetail();
			}
		};

		this._bindingSource = new BindingSource(hierarchicalTableModel);
		if(ColorSettings.useBlackAsBackground)
		{
			this._bindingSource.useBlackAsBackground();
		}
		else
		{
			this._bindingSource.setBackground(Account.LightGray);
		}
		this._bindingSource.bind(new Vector(0), Account.getPropertyDescriptors());
	}

	public BindingSource get_BindingSource()
	{
		return this._bindingSource;
	}

	public void remove(tradingConsole.Account account)
	{
		int row = -1;
		for(int index = 0; index < this._bindingSource.getRowCount(); index++)
		{
			Account account2 = (Account) this._bindingSource.getObject(index);
			if(account2.get_Id().equals(account.get_Id()))
			{
				row = index;
				break;
			}
		}
		if(row > 0) this._bindingSource.removeRow(row);
	}

	public void update(tradingConsole.Account account, boolean includeCurrencies)
	{
		Account account2 = this.getAccount(account.get_Id());
		if(account2 != null)
		{
			this._bindingSource.update(account2);
			account2.applyColor(this._bindingSource);

			if (includeCurrencies)
				account2.updateCurrencies();
		}
	}

	private Account getAccount(Guid accountId)
	{
		for(int index = 0; index < this._bindingSource.getRowCount(); index++)
		{
			Account account = (Account) this._bindingSource.getObject(index);
			if(account.get_Id().equals(accountId))
			{
				return account;
			}
		}
		return null;
	}

	public void clear()
	{
		this._bindingSource.removeAll();
	}

	public void add(tradingConsole.AccountCurrency accountCurrency)
	{
		Account account = this.getAccount(accountCurrency.get_Account().get_Id());
		account.add(accountCurrency);
	}

	public void update(tradingConsole.AccountCurrency accountCurrency)
	{
		Account account = this.getAccount(accountCurrency.get_Account().get_Id());
		account.update(accountCurrency);
	}

	public void updateCurrencyCode()
	{
		for(int index = 0; index < this._bindingSource.getRowCount(); index++)
		{
			Account account = (Account)this._bindingSource.getObject(index);
			account.updateCurrencies();
			this._bindingSource.update(account);
		}
	}
}
