package tradingConsole.enumDefine.bursa;

import framework.lang.Enum;
import tradingConsole.enumDefine.OrderType;

public class InstrumentTimeState extends Enum<InstrumentTimeState>
{
	public static InstrumentTimeState Closed = new InstrumentTimeState("Closed", 0, false, false, false);
	public static InstrumentTimeState PreOpen = new InstrumentTimeState("PreOpen", 1, true, true, true);
	public static InstrumentTimeState Open = new InstrumentTimeState("Open", 2, true, true, true);
	public static InstrumentTimeState PreClose = new InstrumentTimeState("PreClose", 3, true, false, false);
	public static InstrumentTimeState TradingAtLast = new InstrumentTimeState("TradingAtLast", 4, true, false, false);
	public static InstrumentTimeState End = new InstrumentTimeState("End", 5, false, false, false);

	private boolean _canPlaceOrder;
	private boolean _canModifyOrder;
	private boolean _canCancelOrder;

	private InstrumentTimeState(String name, int value, boolean canPalceOrder, boolean canModifyOrder, boolean canCancelOrder)
	{
		super(name, value);

		this._canPlaceOrder = canPalceOrder;
		this._canModifyOrder = canModifyOrder;
		this._canCancelOrder = canCancelOrder;
	}

	public boolean get_CanPlaceOrder(OrderType orderType)
	{
		if((this == InstrumentTimeState.PreClose && (orderType == OrderType.Market || orderType == OrderType.Limit))
		   || (this == InstrumentTimeState.TradingAtLast && orderType == OrderType.Limit))
		{
			return true;
		}
		return this._canPlaceOrder;
	}

	public boolean get_CanModifyOrder()
	{
		return this._canModifyOrder;
	}

	public boolean get_CanCancelOrder()
	{
		return this._canCancelOrder;
	}
}
