package tradingConsole.settings;

import java.util.HashMap;
import java.util.Iterator;

import tradingConsole.TradingConsole;
import tradingConsole.ui.columnKey.TradingAccountColKey;

import framework.Guid;
import framework.data.DataTable;
import framework.data.DataRow;
import framework.data.DataRowCollection;
import framework.diagnostics.TraceType;
import framework.data.DataTableCollection;
import framework.data.DataSet;
import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.ColorSettings;

public class TradingAccountManager
{
	private String _dataSourceKey;
	private tradingConsole.ui.grid.BindingSource _bindingSource;

	private HashMap<Guid, TradingAccount> _tradingAccounts;

	private TradingAccountManager()
	{
		this._tradingAccounts = new HashMap<Guid, TradingAccount> ();
	}

	public static TradingAccountManager Create()
	{
		return new TradingAccountManager();
	}

	public void initialize(DataSet data)
	{
		if (data == null)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "TradingAccountManager.initialize" + " init data is not available!");
			return;
		}

		DataTableCollection tables = data.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;

		dataTable = tables.get_Item(0);
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("Id");
				if (this._tradingAccounts.containsKey(id))
				{
					this.getTradingAccount(id).replace(dataRow);
				}
				else
				{
					TradingAccount tradingAccount = new TradingAccount(id, dataRow);
					this._tradingAccounts.put(id, tradingAccount);
				}
			}
		}
	}

	public TradingAccount getTradingAccount(Guid id)
	{
		return (this._tradingAccounts.containsKey(id)) ? this._tradingAccounts.get(id) : null;
	}

	public boolean isActivatedAccount(Guid id)
	{
		TradingAccount tradingAccount = this.getTradingAccount(id);
		if (tradingAccount==null)
		{
			return true;
		}
		return tradingAccount.get_IsActivated();
	}

	public boolean needActiveAccount()
	{
		for (Iterator<TradingAccount> iterator = this._tradingAccounts.values().iterator(); iterator.hasNext(); )
		{
			TradingAccount tradingAccount = iterator.next();
			if (!tradingAccount.get_IsActivated())
			{
				return true;
			}
		}
		return false;
	}

	public void activeAccount(Guid customerId)
	{
		for (Iterator<TradingAccount> iterator = this._tradingAccounts.values().iterator(); iterator.hasNext(); )
		{
			TradingAccount tradingAccount = iterator.next();
			if (tradingAccount.get_CustomerId().compareTo(customerId)==0)
			{
				tradingAccount.set_IsActivated(true);
			}
		}
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn)
	{
		this.unbind();

		this._dataSourceKey = Guid.newGuid().toString();
		this._bindingSource = new tradingConsole.ui.grid.BindingSource();
		if(ColorSettings.useBlackAsBackground)
		{
			this._bindingSource.useBlackAsBackground();
		}

		TradingAccount.initialize(grid, this._dataSourceKey, this._tradingAccounts.values(), this._bindingSource);

		int column = this._bindingSource.getColumnByName(TradingAccountColKey.Code);
		grid.sortColumn(column, ascend);
	}

	public void unbind()
	{
		if (this._dataSourceKey == null || this._bindingSource == null) return;

		for (Iterator<TradingAccount> iterator = this._tradingAccounts.values().iterator(); iterator.hasNext(); )
		{
			TradingAccount tradingAccount = iterator.next();
			tradingAccount.unbind(this._dataSourceKey, this._bindingSource);
		}
	}

	public void update()
	{
		for (Iterator<TradingAccount> iterator = this._tradingAccounts.values().iterator(); iterator.hasNext(); )
		{
			TradingAccount tradingAccount = iterator.next();
			tradingAccount.update(this._dataSourceKey);
		}
	}

	public void finalize() throws Throwable
	{
		this.unbind();

		super.finalize();
	}
}
