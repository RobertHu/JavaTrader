package tradingConsole.ui.account;

import java.util.*;

import javax.swing.*;

import com.jidesoft.grid.*;
import framework.*;
import tradingConsole.ui.grid.*;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;
import tradingConsole.Customer;
import tradingConsole.ui.ColorSettings;

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
				return account.needShowStatus();
			}

			public boolean isHierarchical(int row)
			{
				if(_customer.get_IsNoShowAccountStatus()) return false;
				if(row >= _bindingSource.getRowCount()) return false;
				Account account = (Account)_bindingSource.getObject(row);
				return account.needShowStatus();
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
				return account.needShowStatus();
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
		//if(ColorSettings.Enabled) this._bindingSource.useBlackAsBackground();
	}

	public BindingSource get_BindingSource()
	{
		return this._bindingSource;
	}

	public void add(tradingConsole.Account account)
	{
		Account account2 = this.getAccount(account.get_Id());
		if(account2 == null)
		{
			account2 = new Account(account);
			this._bindingSource.add(account2);
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Account is already in set");
		}
	}

	public boolean remove(tradingConsole.Account account)
	{
		Account account2 = this.getAccount(account.get_Id());

		if(account2 == null)
		{
			return false;
		}
		else
		{
			this._bindingSource.remove(account2);
			return true;
		}
	}

	public void update(tradingConsole.Account account)
	{
		this.update(account, false);
	}

	public void update(tradingConsole.Account account, boolean includeCurrencies)
	{
		Account account2 = this.getAccount(account.get_Id());
		account2.updateDetails();
		this._bindingSource.update(account2);
		if(includeCurrencies) account2.updateCurrencies();
	}

	public void add(tradingConsole.AccountCurrency accountCurrency)
	{
		Account account2 = this.getAccount(accountCurrency.get_Account().get_Id());
		account2.add(accountCurrency);
	}

	public void update(tradingConsole.AccountCurrency accountCurrency)
	{
		Account account2 = this.getAccount(accountCurrency.get_Account().get_Id());
		this._bindingSource.update(account2);
		account2.update(accountCurrency);
	}

	private Account getAccount(Guid accountId)
	{
		for(int index = 0; index < this._bindingSource.getRowCount(); index++)
		{
			Account account2 = (Account) this._bindingSource.getObject(index);
			if(account2.get_Id().equals(accountId))
			{
				return account2;
			}
		}
		return null;
	}

	public void updateCurrencyCode()
	{
		for(int index = 0; index < this._bindingSource.getRowCount(); index++)
		{
			Account account2 = (Account)this._bindingSource.getObject(index);
			account2.updateDetails();
			this._bindingSource.update(account2);
		}
	}

	public void setAccountIcon(tradingConsole.Account account, Icon icon)
	{
		Account account2 = this.getAccount(account.get_Id());
		this._bindingSource.setIcon(account2, "Code", icon);
	}

	public int getRow(tradingConsole.Account account)
	{
		Account account2 = this.getAccount(account.get_Id());
		return this._bindingSource.getRow(account2);
	}

	public void clear()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Account.BindingManager clear");
		this._bindingSource.removeAll();
	}
}
