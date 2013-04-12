package tradingConsole;

import framework.time.ITimeSyncService;
import framework.time.ITimeSource;
import framework.time.TimeAdjustedEventHandler;
import framework.time.TimeAccuracy;
import framework.DateTime;
import framework.time.TimeAdjustedEvent;
import framework.lang.MulticastDelegate;
import framework.FrameworkException;
import framework.time.TimeInfo;
import framework.TimeSpan;
import framework.util.EnvironmentHelper;
import framework.diagnostics.TraceSource;
import framework.diagnostics.TraceType;
import java.util.Timer;
import framework.StringHelper;

public class SimpleAppTime implements ITimeSource
{
	private static final TraceSource _traceSource = new TraceSource("Framework.Time.AppTime");

	private ITimeSyncService _timeProvider;
	private TimeAdjustedEvent _onTimeAdjusted;
	private boolean _started = false;
	private DateTime _serviceTime = null;
	private TimeSpan _ticksForServiceTime = null;

	public SimpleAppTime(ITimeSyncService timeProvider)
	{
		this._timeProvider = timeProvider;
	}

	public boolean start()
	{
		if(!this._started)
		{
			try
			{
				this.sample();
				this._started = true;
				this._traceSource.trace(TraceType.Start, "Started");
			}
			catch (Exception ex)
			{
				this._traceSource.trace(TraceType.Error, ex);
				return false;
			}
		}
		return true;
	}

	public void stop()
	{
		if(this._started)
		{
			this._started = false;
			this._traceSource.trace(TraceType.Start, "Stopped");
		}
	}

	public void reset()
	{
		this.stop();
		this.start();
	}

	public DateTime get_UtcNow()
	{
		if(!this._started)
		{
			throw new FrameworkException("[AppTime.getCurrrentTime]AppTime is stopped, please call Start first");
		}
		TimeSpan tickCount = EnvironmentHelper.get_SystemElapsedTime();
		TimeSpan diff = tickCount.subtract(this._ticksForServiceTime);
		if(diff.compareTo(TimeSpan.zero) < 0)
		{
			this._traceSource.trace(TraceType.Warning, "SystemElapsedTime error, get new sample");
			try
			{
				this.sample();
			}
			catch (Exception ex)
			{
				this._traceSource.trace(TraceType.Error, ex);
			}
		}

		this._traceSource.trace(TraceType.Information, StringHelper.format("ServiceTime={0}, diff={1}", new Object[]{this._serviceTime, diff}));
		return this._serviceTime.add(diff);
	}

	public DateTime getUtcNow(TimeAccuracy accuracy)
	{
		return this.get_UtcNow();
	}

	public void add_TimeAdjusted(TimeAdjustedEventHandler handler)
	{
		if (handler == null)
		{
			return;
		}
		this._onTimeAdjusted = (TimeAdjustedEvent)MulticastDelegate.combine(this._onTimeAdjusted, new TimeAdjustedEvent(handler));
	}

	public void remove_TimeAdjusted(TimeAdjustedEventHandler handler)
	{
		if (handler == null)
		{
			return;
		}
		this._onTimeAdjusted = (TimeAdjustedEvent)MulticastDelegate.remove(this._onTimeAdjusted, new TimeAdjustedEvent(handler));
	}

	private void sample() throws Exception
	{
		TimeSpan sentTickCount = null;
		TimeInfo timeInfo = null;
		for(int index = 1; index <= 3; index++)
		{
			sentTickCount = EnvironmentHelper.get_SystemElapsedTime();
			timeInfo = this._timeProvider.getTimeInfo();
			if(timeInfo == null)
			{
				Thread.sleep(index * 500);
			}
			else
			{
				break;
			}
		}
		TimeSpan receiveTickCount = EnvironmentHelper.get_SystemElapsedTime();
		TimeSpan roundTrip = receiveTickCount.subtract(sentTickCount);
		roundTrip = roundTrip.compareTo(TimeSpan.zero) > 0 ? new TimeSpan(roundTrip.get_Ticks() / 2) : TimeSpan.zero;
		this._serviceTime = timeInfo.get_DateTime().add(roundTrip) ;
		this._ticksForServiceTime = receiveTickCount;
	}
}
