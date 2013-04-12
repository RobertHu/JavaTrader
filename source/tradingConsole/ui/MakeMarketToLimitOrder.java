package tradingConsole.ui;

import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.enumDefine.OrderTypeMask;

public class MakeMarketToLimitOrder extends MakeLimitOrder
{
	public MakeMarketToLimitOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.MarketToLimit);
	}
}
