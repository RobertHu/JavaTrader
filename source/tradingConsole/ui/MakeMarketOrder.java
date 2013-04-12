package tradingConsole.ui;

import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import tradingConsole.enumDefine.OrderTypeMask;
import tradingConsole.TradingConsole;

public class MakeMarketOrder extends MakeLimitOrder
{
	public MakeMarketOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.Market);
	}
}
