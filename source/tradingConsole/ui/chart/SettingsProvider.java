package tradingConsole.ui.chart;

import org.aiotrade.core.common.ISettingsProvider;
import org.aiotrade.math.timeseries.Frequency;
import org.aiotrade.core.common.Instrument;
import java.util.List;
import org.aiotrade.core.common.ZoomRange;
import tradingConsole.settings.SettingsManager;
import java.util.ArrayList;
import tradingConsole.AppToolkit;
import tradingConsole.settings.PublicParametersManager;
import java.util.Locale;
import framework.threading.Scheduler;
import framework.time.ITimeSource;
import tradingConsole.TradingConsoleServer;
import framework.diagnostics.TraceType;
import tradingConsole.TradingConsole;
import java.awt.Frame;
import org.aiotrade.ui.AioChartPanel;
import com.jidesoft.docking.DockableFrame;
import java.util.Collection;
import java.awt.Component;
import org.aiotrade.core.common.TradeDay;

public class SettingsProvider implements ISettingsProvider
{
	private SettingsManager _settingsManager;
	private TradingConsole _tradingConsole;

	public SettingsProvider(TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
	}

	public String getConfigPath()
	{
		return AppToolkit.get_SettingDirectory();
	}

	public Locale getLocale()
	{
		return PublicParametersManager.getLocal();
	}

	public TradeDay getTradeDay()
	{
		tradingConsole.TradeDay tradeDay = this._settingsManager.get_TradeDay();
		return new TradeDay(tradeDay.get_TradeDay().toDate(), tradeDay.get_BeginTime().toDate(), tradeDay.get_EndTime().toDate());
	}

	public boolean useRealTimeQuotationAsFinalQuotation(String instrumentCode)
	{
		return true;
	}

	public boolean saveQuotationToLocal(String instrumentCode, Frequency frequency)
	{
		return true;
	}

	public String getQuotationLocalPath()
	{
		return AppToolkit.get_ChartDataDirectory();
	}

	public List<AioChartPanel> getVisibleChartPanels()
	{
		List<AioChartPanel> visibleChartPanels = new ArrayList<AioChartPanel>();
		for(String key : this._tradingConsole.get_MainForm().getDockingManager().getAllVisibleFrameKeys())
		{
			DockableFrame frame = this._tradingConsole.get_MainForm().getDockingManager().getFrame(key);
			if(key.startsWith("ChartFrame"))
			{
				for(Component o : frame.getContentPane().getComponents())
				{
					if(o instanceof AioChartPanel)
					{
						visibleChartPanels.add((AioChartPanel)o);
						break;
					}
				}
			}
		}
		return visibleChartPanels;
	}

	public List<Instrument> getInstruments()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getInstruments]");

		if(this._settingsManager.get_Customer().get_UserId() == null) return new ArrayList<Instrument>();

		String userId = this._settingsManager.get_Customer().get_UserId().toString();
		ArrayList<Instrument> instruments = new ArrayList<Instrument>(this._settingsManager.getInstruments().size());
		for(tradingConsole.Instrument instrument : this._settingsManager.getInstruments().values())
		{
			instruments.add(new Instrument(instrument.get_Id().toString(), instrument.get_Code() + userId, instrument.get_Description(), instrument.get_Decimal()));
		}
		return instruments;
	}

	public List<Frequency> getFrequencies()
	{
		ArrayList<Frequency> frequencies = new ArrayList<Frequency>();
		frequencies.add(Frequency.REALTIME);
		frequencies.add(Frequency.ONE_MIN);
		frequencies.add(Frequency.FIVE_MINS);
		frequencies.add(Frequency.FIFTEEN_MINS);
		frequencies.add(Frequency.THIRTY_MINS);
		frequencies.add(Frequency.ONE_HOUR);
		frequencies.add(Frequency.DAILY);
		frequencies.add(Frequency.WEEKLY);
		frequencies.add(Frequency.MONTHLY);

		return frequencies;
	}

	public ZoomRange getZooomRange()
	{
		return ZoomRange.getDefault();
	}

	public Scheduler getScheduler()
	{
		return TradingConsoleServer.scheduler;
	}

	public ITimeSource getTimeSource()
	{
		return TradingConsoleServer.appTime;
	}

	public Frame getTopFrame()
	{
		return this._settingsManager.get_MainFrame();
	}

	public boolean isHighBid()
	{
		return this._settingsManager.get_SystemParameter().get_HighBid();
	}

	public boolean isLowBid()
	{
		return this._settingsManager.get_SystemParameter().get_LowBid();
	}
}
