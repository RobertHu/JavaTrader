package tradingConsole;

import framework.DateTime;

import framework.data.DataRow;
import framework.Guid;
import framework.FrameworkException;

import tradingConsole.settings.SettingsManager;
import framework.threading.Scheduler;
import framework.threading.Scheduler.SchedulerEntry;

public class TradingTime
{
	private SchedulerEntry _schedulerEntryForBeginTime;
	private SchedulerEntry _schedulerEntryForEndTime;

	private SettingsManager _settingsManager;

	private Instrument _instrument;
	private DateTime _beginTime;
	private DateTime _endTime;

	public DateTime get_BeginTime()
	{
		return this._beginTime;
	}

	public DateTime get_EndTime()
	{
		return this._endTime;
	}

	public TradingTime(SettingsManager settingsManager,DataRow dataRow)
	{
		this._settingsManager = settingsManager;

		Guid instrumentId = (Guid) dataRow.get_Item("InstrumentID");
		this._instrument = this._settingsManager.getInstrument(instrumentId);

		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._beginTime = (DateTime) dataRow.get_Item("BeginTime");
		this._endTime = (DateTime) dataRow.get_Item("EndTime");
	}

	public void addScheduler()
	{
		String queue = this._instrument.get_Id().toString();
		try
		{
			this.removeScheduler();
		}
		catch (Exception exception)
		{
			throw new FrameworkException(exception);
		}
		try
		{
			this._schedulerEntryForBeginTime = TradingConsoleServer.scheduler.add(queue,this._instrument,this,this._beginTime);
			this._schedulerEntryForEndTime = TradingConsoleServer.scheduler.add(queue,this._instrument,this,this._endTime);
		}
		catch (Exception exception2)
		{
			throw new FrameworkException(exception2);
		}
	}

	public synchronized void removeScheduler()
	{
		if (this._schedulerEntryForBeginTime != null)
		{
			TradingConsoleServer.scheduler.remove(this._schedulerEntryForBeginTime);
			this._schedulerEntryForBeginTime = null;
		}
		if (this._schedulerEntryForEndTime != null)
		{
			TradingConsoleServer.scheduler.remove(this._schedulerEntryForEndTime);
			this._schedulerEntryForEndTime = null;
		}
	}

	@Override
	public String toString()
	{
		return "Instrument = " + this._instrument.get_Code()
			+ "; BeginTime = " +  this._beginTime.toString()
			+ "; EndTime = " +  this._endTime.toString();
	}
}
