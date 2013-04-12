package tradingConsole.ui;

import tradingConsole.enumDefine.OrderType;

public interface IPlaceOrderTypeChangedListener
{
	void OrderTypeChanged(OrderType newOrderType, OrderType oldOrderType);
}
