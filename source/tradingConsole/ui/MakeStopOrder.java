package tradingConsole.ui;

import java.util.HashMap;

import framework.Guid;

import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.settings.MakeOrderAccount;
import tradingConsole.enumDefine.OrderTypeMask;

public class MakeStopOrder extends MakeLimitOrder
{
	public MakeStopOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.Limit);
	}
}
