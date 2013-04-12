package tradingConsole.ui;

import java.awt.Window;
import java.math.BigDecimal;

import framework.TimeSpan;
import framework.threading.Scheduler;
import framework.threading.Scheduler.SchedulerEntry;
import framework.DateTime;
import framework.threading.ThreadPriority;

import tradingConsole.TradingConsoleServer;
import tradingConsole.ui.language.Language;
import tradingConsole.settings.Parameter;
import tradingConsole.settings.MakeOrderWindow;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import tradingConsole.enumDefine.OperateType;
import tradingConsole.enumDefine.BSStatus;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;
import tradingConsole.Account;
import javax.swing.SwingUtilities;

public class FrameBase2 extends FrameBase implements Scheduler.ISchedulerCallback
{
	//private static Scheduler _scheduler;
	private SchedulerEntry _priceValidTimeScheduleEntry;
	private SchedulerEntry _outTimeScheduleEntry;
	private SchedulerEntry _echoScheduleEntry;
	private SchedulerEntry _fillVerificationInfoScheduleEntry;
	private SchedulerEntry _quoteDelayScheduleEntry;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Instrument _instrument;

	public FrameBase2(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole.get_MainForm());

		//this._scheduler = new Scheduler(TradingConsoleServer.appTime);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;
	}

	public void dispose()
	{
		this.echoSchedulerStop();
		this.outTimeSchedulerStop();
		this.priceValidTimeSchedulerStop();
		this.quoteDelayTimeSchedulerStop();
		this.resetData(true);
		this.removeMakeOrderWindow();
		MakeOrder.setLastQuotationIsUsing(this._instrument, false);

		super.dispose();
	}

	public void notifyIsAcceptMakeSpotTradeOrderByPrice()
	{
		BSStatus bsStatus = BSStatus.None;
		boolean isBuy = (bsStatus == BSStatus.HasBuyOnly) ? true : false;
		boolean isAcceptMakeSpotTradeOrderByPrice = this._instrument.isAcceptMakeSpotTradeOrderByPrice(isBuy);
		if (!isAcceptMakeSpotTradeOrderByPrice && !this._inFillVerificationInfo	&& this._priceValidTimeScheduleEntry != null)
		{
			this.resetData();
		}
	}

	public void notifyMakeOrderUiByPrice()
	{
		this.refreshPrice2();
	}

	protected void dealDelayProcess()
	{}

	public void getLastPrice()
	{
		this.setNotifyIsAcceptWindow();

		//start price and refresh UI
		boolean prevIsUsing = MakeOrder.getLastQuotationIsUsing(this._instrument);
		MakeOrder.setLastQuotationIsUsing(this._instrument, false);
		this._instrument.update(Instrument.tradingPanelKey);
		this._instrument.updateLastQuotationForUnitGrid();

		this.refreshPrice2();

		MakeOrder.setLastQuotationIsUsing(this._instrument, prevIsUsing);
	}

	public void resetData()
	{
		this.resetData(false);
	}

	public void resetData(boolean inDisposing)
	{
		this.quoteDelayTimeSchedulerStop();
		this.priceValidTimeSchedulerStop();
		this.fillVerificationInfoScheculerStop();

		this.getLastPrice();

		this.setDealStatus(false);

		this.resetBuySell();
	}

	public void resetBuySell()
	{}

	public void refreshPrice()
	{
	}

	private void refreshPrice2()
	{
		if(this._inFillVerificationInfo)
		{
			return;
		}
		this.refreshPrice();
	}

	public void setDealStatus(boolean isDeal)
	{
	}

	//call service
	//when has deal function this time will has quote
	protected void quote(BigDecimal quoteLot, BSStatus bsStatus)
	{
		this.setQuoteWindow(this);
		MakeOrder.setLastQuotationIsUsing(this._instrument, true);
		this.setIsQuoting(true);
		this._tradingConsole.get_TradingConsoleServer().quote(this._instrument.get_Id(), quoteLot, bsStatus.value());
		this._instrument.set_InQuoting(true);
		this.quoteDelaySchedulerStart();
	}

	protected void cancelQuote(BigDecimal buyQuoteLot, BigDecimal sellQuoteLot)
	{
		this.setQuoteWindow(this);
		this.setIsQuoting(true);
		this._tradingConsole.get_TradingConsoleServer().cancelQuote(this._instrument.get_Id(),buyQuoteLot,sellQuoteLot);
	}

	protected void cancelQuoteForDispose(BigDecimal buyQuoteLot, BigDecimal sellQuoteLot)
	{
		this.setQuoteWindow(null);
		this.setIsQuoting(false);
		this._tradingConsole.get_TradingConsoleServer().cancelQuote(this._instrument.get_Id(),buyQuoteLot,sellQuoteLot);
	}

	protected void cancelQuoteArrived()
	{
		this.setQuoteWindow(null);

		if (!this._settingsManager.getMakeOrderWindow(this._instrument).get_IsQuoting())
		{
			return;
		}
		this.setIsQuoting(false);
	}

	protected void quoteArrived()
	{
		this.setQuoteWindow(null);

		if (!this._settingsManager.getMakeOrderWindow(this._instrument).get_IsQuoting())
		{
			return;
		}
		this.quoteDelayTimeSchedulerStop();
		this.setIsQuoting(false);

		this.refreshPrice2();

		//this.setBuySellEnabled(true);
		//this.priceValidTimeSchedulerStart(OperateType.SingleSpotTrade);
	}

	protected void quote2(BigDecimal buyQuoteLot, BigDecimal sellQuoteLot, int tick)
	{
		this.setQuoteWindow(this);
		this.setIsQuoting(true);
		this._tradingConsole.get_TradingConsoleServer().quote2(this._instrument.get_Id(), buyQuoteLot, sellQuoteLot, tick);
		this._instrument.set_InQuoting(true);
	}

	protected void quoteArrived2()
	{
		this.setQuoteWindow(null);

		if (!this._settingsManager.getMakeOrderWindow(this._instrument).get_IsQuoting())
		{
			return;
		}
		//this.quoteDelayTimeSchedulerStop();
		this.setIsQuoting(false);

		this.refreshPrice2();

		//this.setBuySellEnabled(true);
		//this.priceValidTimeSchedulerStart(OperateType.SingleSpotTrade);
	}

	protected void setIsQuoting(boolean isQuoting)
	{
		if (this._settingsManager.getMakeOrderWindow(this._instrument) == null) return;
		this._settingsManager.getMakeOrderWindow(this._instrument).set_IsQuoting(isQuoting);
	}

	protected void setMakeOrderWindow()
	{
		MakeOrderWindow makeOrderWindow = new MakeOrderWindow(this._instrument.get_Id(), this, false);
		this._settingsManager.setMakeOrderWindow(this._instrument, makeOrderWindow);
	}

	protected void removeMakeOrderWindow()
	{
		this._settingsManager.removeMakeOrderWindow(this._instrument, this);
	}

	protected void setQuoteWindow(Window window)
	{
		this._settingsManager.setQuoteWindow(this._instrument, window);
	}

	protected void setNotifyIsAcceptWindow()
	{
		this._settingsManager.setNotifyIsAcceptWindow(this._instrument, this);
	}

	//Scheduler.SchedulerCallback.ActionsetDealStatus
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		SwingUtilities.invokeLater(new SwingSafelyAction(this, schedulerEntry));
	}

	private static class SwingSafelyAction implements Runnable
	{
		private FrameBase2 _owner;
		private Scheduler.SchedulerEntry _schedulerEntry;

		public SwingSafelyAction(FrameBase2 owner, Scheduler.SchedulerEntry schedulerEntry)
		{
			this._owner = owner;
			this._schedulerEntry = schedulerEntry;
		}

		public void run()
		{
			try
			{
				if (this._schedulerEntry.get_IsRemoved())
				{
					return;
				}

				if (this._schedulerEntry.get_ActionArgs().equals("OutTime"))
				{
					//this.dispose();
					this._owner.startCloseTimer();
				}
				else if (this._schedulerEntry.get_ActionArgs().equals("PriceValidTime"))
				{
					this._owner.resetData();
					MakeOrder.setLastQuotationIsUsing(this._owner._instrument, false);
				}
				else if (this._schedulerEntry.get_ActionArgs().equals("QuoteDelay"))
				{
					this._owner.setQuoteWindow(null);
					MakeOrder.setLastQuotationIsUsing(this._owner._instrument, false);

					if (this._owner._settingsManager.getMakeOrderWindow(this._owner._instrument) != null
						&& !this._owner._settingsManager.getMakeOrderWindow(this._owner._instrument).get_IsQuoting())
					{
						return;
					}

					this._owner.setIsQuoting(false);
					AlertDialogForm.showDialog(this._owner, null, true, Language.QuoteDelayTimeout);
					this._owner.resetData();
					this._owner.outTimeSchedulerStart();
				}
				else if (this._schedulerEntry.get_ActionArgs().equals("FillVerificationInfo"))
				{
					this._owner.fillVerificationInfoScheculerStop();
					this._owner.getLastPrice();
					this._owner.dealDelayProcess();
					this._owner._inFillVerificationInfo = false;
				}
				else if (this._schedulerEntry.get_ActionArgs().equals("Echo"))
				{
					this._owner.echoSchedulerStop();
					this._owner.echo();
				}
			}
			catch(Exception exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exception);
			}
		}
	}

	protected void echo()
	{}

	protected void startCloseTimer()
	{}

	protected void echoSchedulerStart()
	{
		this.echoSchedulerStop();

		TimeSpan timeSpan = TimeSpan.fromMinutes(1);
		Object actionArgs = "Echo";
		this._echoScheduleEntry = this.schedulerStart(timeSpan, actionArgs);
	}

	protected void outTimeSchedulerStart()
	{
		if (this._outTimeScheduleEntry == null
			&& this._settingsManager.get_Customer().get_DQOrderOutTime() > 0)
		{
			TimeSpan timeSpan = TimeSpan.fromSeconds(this._settingsManager.get_Customer().get_DQOrderOutTime());
			Object actionArgs = "OutTime";
			this._outTimeScheduleEntry = this.schedulerStart(timeSpan, actionArgs);
		}
	}

	protected void priceValidTimeSchedulerStart(OperateType operateType)
	{
		TimeSpan timeSpan;
		if (operateType != OperateType.MultiSpotTrade)
		{
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				timeSpan = TimeSpan.fromSeconds(this._settingsManager.get_SystemParameter().get_ExceptionEnquiryOutTime());
			}
			else
			{
				timeSpan = TimeSpan.fromSeconds(this._instrument.get_PriceValidTime());
			}
		}
		else
		{ //only for Make Multi_SpotTrade Order: PriceValidTime * 2
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				timeSpan = TimeSpan.fromSeconds(this._settingsManager.get_SystemParameter().get_ExceptionEnquiryOutTime());
			}
			else
			{
				timeSpan = TimeSpan.fromSeconds(this._instrument.get_PriceValidTime() * 2);
			}
		}
		Object actionArgs = "PriceValidTime";
		this._priceValidTimeScheduleEntry = this.schedulerStart(timeSpan, actionArgs);
	}

	private boolean _inFillVerificationInfo = false;
	protected void fillVerificationInfoScheculerStart()
	{
		TimeSpan timeSpan = TimeSpan.fromSeconds(Parameter.nonQuoteVerificationUiDelay);
		Object actionArgs = "FillVerificationInfo";
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			this._fillVerificationInfoScheduleEntry = TradingConsoleServer.scheduler.add(this, actionArgs, beginTime, DateTime.maxValue, timeSpan, true);
			this._inFillVerificationInfo = true;
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	protected synchronized void fillVerificationInfoScheculerStop()
	{
		this._inFillVerificationInfo = false;
		if (this._fillVerificationInfoScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._fillVerificationInfoScheduleEntry);
			this._fillVerificationInfoScheduleEntry = null;
		}
	}

	protected void quoteDelaySchedulerStart()
	{
		//TimeSpan timeSpan = TimeSpan.fromSeconds(Parameter.quoteDelay);
		TimeSpan timeSpan = TimeSpan.fromSeconds(this._settingsManager.get_SystemParameter().get_EnquiryOutTime());
		Object actionArgs = "QuoteDelay";
		this._quoteDelayScheduleEntry = this.schedulerStart(timeSpan, actionArgs);
	}

	protected void echoSchedulerStop()
	{
		this.schedulerStop(this._echoScheduleEntry);
		this._echoScheduleEntry = null;
	}

	protected void outTimeSchedulerStop()
	{
		this.schedulerStop(this._outTimeScheduleEntry);
		this._outTimeScheduleEntry = null;
	}

	protected void priceValidTimeSchedulerStop()
	{
		this.schedulerStop(this._priceValidTimeScheduleEntry);
		this._priceValidTimeScheduleEntry = null;
	}

	protected void quoteDelayTimeSchedulerStop()
	{
		this.schedulerStop(this._quoteDelayScheduleEntry);
		this._quoteDelayScheduleEntry = null;
	}

	protected SchedulerEntry schedulerStart(TimeSpan timeSpan, Object actionArgs)
	{
		SchedulerEntry schedulerEntry = null;
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			schedulerEntry = TradingConsoleServer.scheduler.add(this, actionArgs, beginTime, timeSpan, ThreadPriority.normal);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
		return schedulerEntry;
	}

	protected synchronized void schedulerStop(SchedulerEntry schedulerEntry)
	{
		if (schedulerEntry != null)
		{
			try
			{
				TradingConsoleServer.scheduler.remove(schedulerEntry);
			}
			catch(NullPointerException exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error,
												 "schedulerEntry is " + schedulerEntry == null ? " null;" : " not null;"
												 +" TradingConsoleServer.scheduler is " + TradingConsoleServer.scheduler == null ? " null;" : " not null;");
			}
		}
	}
}
