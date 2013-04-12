package tradingConsole.ui;

import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.enumDefine.OrderTypeMask;

public class MakeOneCancelOtherOrder extends MakeLimitOrder
{
	public MakeOneCancelOtherOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.OneCancelOther);
	}
}
