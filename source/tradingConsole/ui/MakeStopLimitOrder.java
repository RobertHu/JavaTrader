package tradingConsole.ui;

import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.enumDefine.OrderTypeMask;

public class MakeStopLimitOrder extends MakeLimitOrder
{
	public MakeStopLimitOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.StopLimit);
	}
}
