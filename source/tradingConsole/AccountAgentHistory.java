package tradingConsole;

import java.util.Iterator;
import java.util.Collection;

import framework.Guid;
import framework.data.*;
import framework.DateTime;
import tradingConsole.settings.SettingsManager;

public class AccountAgentHistory
{
	private Guid _accountId;
	private Guid _agentAccountId;
	private DateTime _agentBeginTime;
	private DateTime _agentEndTime;

	public Guid get_AccountId()
	{
		return this._accountId;
	}

	public Guid get_AgentAccountId()
	{
		return this._agentAccountId;
	}

	public DateTime get_AgentBeginTime()
	{
		return this._agentBeginTime;
	}

	public DateTime get_AgentEndTime()
	{
		return this._agentEndTime;
	}

	public AccountAgentHistory(DataRow dataRow)
	{
		this._accountId = (Guid) dataRow.get_Item("AccountID");
		this._agentAccountId = (Guid) dataRow.get_Item("AgentAccountID");
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._agentBeginTime = (DateTime) dataRow.get_Item("AgentBeginTime");
		if (AppToolkit.isDBNull(dataRow.get_Item("AgentEndTime")))
		{
			this._agentEndTime = this._agentBeginTime;
		}
		else
		{
			this._agentEndTime = (DateTime) dataRow.get_Item("AgentEndTime");
		}
	}

	public static boolean isLockedBySelfAtCurrent(SettingsManager settingsManager,Account account)
	{
		DateTime appTime = TradingConsoleServer.appTime();

		Collection<AccountAgentHistory> accountAgenrHistories = settingsManager.getAccountAgentHistories();
		for (Iterator<AccountAgentHistory> iterator = accountAgenrHistories.iterator(); iterator.hasNext(); )
		{
			AccountAgentHistory accountAgenrHistory = (AccountAgentHistory) iterator.next();
			if (accountAgenrHistory.get_AccountId().equals(account.get_Id()))
			{
				DateTime agentBeginTime = accountAgenrHistory.get_AgentBeginTime();
				DateTime agentEndTime = accountAgenrHistory.get_AgentEndTime();
				if ( (!appTime.before(agentBeginTime) && !appTime.after(agentEndTime)) ||
					(agentBeginTime.equals(agentEndTime)))
				{
					return true;
				}
			}
		}
		return false;
	}

	//for make assign order
	public static boolean isAssigned(SettingsManager settingsManager,Account account,DateTime agentTime)
	{
		Collection<AccountAgentHistory> accountAgenrHistories = settingsManager.getAccountAgentHistories();
		for (Iterator<AccountAgentHistory> iterator = accountAgenrHistories.iterator(); iterator.hasNext(); )
		{
			AccountAgentHistory accountAgenrHistory = (AccountAgentHistory) iterator.next();
			if (accountAgenrHistory.get_AccountId().equals(account.get_Id()))
			{
				DateTime agentBeginTime = accountAgenrHistory.get_AgentBeginTime();
				DateTime agentEndTime = accountAgenrHistory.get_AgentEndTime();
				if ( (!agentTime.before(agentBeginTime) && agentBeginTime.equals(agentEndTime))
					|| (!agentTime.after(agentBeginTime) && (!agentTime.after(agentEndTime))))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static DataTable createStructure()
	{
		DataTable dataTable = new DataTable("AccountAgentHistory");
		dataTable.get_Columns().add("AccountID", Guid.class);
		dataTable.get_Columns().add("AgentAccountID", Guid.class);
		dataTable.get_Columns().add("AgentBeginTime", DateTime.class);
		dataTable.get_Columns().add("AgentEndTime", DateTime.class);

		return dataTable;
	}
}
