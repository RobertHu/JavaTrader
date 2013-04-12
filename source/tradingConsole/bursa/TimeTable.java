package tradingConsole.bursa;

import framework.DateTime;
import tradingConsole.enumDefine.bursa.InstrumentTimeState;
import framework.data.DataRow;

public class TimeTable
{
	private Session _forenoonSession;
	private Session _afternoonSession;

	public TimeTable(Session forenoonSession, Session afternoonSession)
	{
		this._forenoonSession = forenoonSession;
		this._afternoonSession = afternoonSession;
	}

	public Session get_ForenoonSession()
	{
		return this._forenoonSession;
	}

	public Session get_AfternoonSession()
	{
		return this._afternoonSession;
	}

	public static class Session
	{
		private DateTime _preOpen;
		private DateTime _open;
		private DateTime _preClose;
		private DateTime _tradeAtLast;
		private DateTime _end;

		public Session(DataRow dataRow)
		{
			DateTime preOpen = (DateTime)dataRow.get_Item("PreOpen");
			DateTime open = (DateTime)dataRow.get_Item("Open");
			DateTime preClose = (DateTime)dataRow.get_Item("PreClose");
			DateTime tradeAtLast = (DateTime)dataRow.get_Item("TradeAtLast");
			DateTime end = (DateTime)dataRow.get_Item("End");

			this._preOpen = preOpen;
			this._open = open;
			this._preClose = preClose;
			this._tradeAtLast = tradeAtLast;
			this._end = end;
		}

		public Session(DateTime preOpen, DateTime open, DateTime preClose, DateTime tradeAtLast, DateTime end)
		{
			this._preOpen = preOpen;
			this._open = open;
			this._preClose = preClose;
			this._tradeAtLast = tradeAtLast;
			this._end = end;
		}

		public InstrumentTimeState getTimeState(DateTime time)
		{
			if(time.before(this._preOpen))
			{
				return InstrumentTimeState.Closed;
			}
			else if(time.before(this._open))
			{
				return InstrumentTimeState.PreOpen;
			}
			else if(time.before(this._preClose))
			{
				return InstrumentTimeState.Open;
			}
			else if(time.before(this._tradeAtLast))
			{
				return InstrumentTimeState.PreClose;
			}
			else if(time.before(this._end))
			{
				return InstrumentTimeState.TradingAtLast;
			}
			else
			{
				return InstrumentTimeState.End;
			}
		}
	}
}
