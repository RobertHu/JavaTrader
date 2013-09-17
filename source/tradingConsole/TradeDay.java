package tradingConsole;

import framework.DateTime;
import framework.data.DataRow;
import framework.TimeSpan;

public class TradeDay
{
	private DateTime _tradeDay;
	private DateTime _beginTime;
	private DateTime _endTime;
	private DateTime _lastTradeDay;

	private DateTime _beginDateTime;
	private DateTime _endDateTime;

	public DateTime get_TradeDay()
	{
		return this._tradeDay;
	}
	public DateTime get_BeginTime()
	{
		return this._beginTime;
	}
	public DateTime get_EndTime()
	{
		return this._endTime;
	}

	public DateTime get_LastTradeDay()
	{
		return this._lastTradeDay;
	}

	public boolean contains(DateTime time)
	{
		return this._beginTime.compareTo(time) <= 0 && this._endTime.compareTo(time) >= 0;
	}

	public TradeDay()
	{
	}

	public void setValue(DataRow dataRow)
	{
		this._tradeDay = (DateTime)dataRow.get_Item("TradeDay");
		this._beginTime = (DateTime)dataRow.get_Item("BeginTime");
		this._endTime = (DateTime)dataRow.get_Item("EndTime");
		this._lastTradeDay = (DateTime)dataRow.get_Item("LastTradeDay");
	}

	public TradeDay getTradeDayOf(DateTime dateTime)//left close, right open
	{
		if(!dateTime.before(this._beginTime) && dateTime.before(this._endTime))
		{
			return this;
		}
		else
		{
			int day = 0;
			if(dateTime.before(this._beginTime))
			{
				TimeSpan timeSpan = dateTime.substract(this._beginTime);
				day = timeSpan.get_Days() - 1;
			}
			else
			{
				TimeSpan timeSpan = dateTime.substract(this._endTime);
				day = timeSpan.get_Days() + 1;
			}
			return this.add(day);
		}
	}

	@Override
	public String toString()
	{
		return this._tradeDay.toString("yyyy-MM-dd");
	}

	private TradeDay add(int day)
	{
		TradeDay tradeDay = new TradeDay();
		tradeDay._beginTime = this._beginTime;
		tradeDay._endTime = this._endTime;
		tradeDay._lastTradeDay = this._lastTradeDay.addDays(day);
		tradeDay._tradeDay = this._tradeDay.addDays(day);
		return tradeDay;
	}
}
