package tradingConsole.ui;

import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;

public class MakeLimitStopOrder extends MakeLimitOrder
{
	public MakeLimitStopOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
	}
}
