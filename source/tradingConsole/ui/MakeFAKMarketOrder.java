package tradingConsole.ui;

import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.enumDefine.OrderTypeMask;

public class MakeFAKMarketOrder extends MakeLimitOrder
{
	public MakeFAKMarketOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.FAK_Market);
	}
}
