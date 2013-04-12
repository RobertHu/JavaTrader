package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OrderTypeMask extends Enum<OrderTypeMask>
{
	public static final OrderTypeMask SpotTrade = new OrderTypeMask("SpotTrade", 1);
	public static final OrderTypeMask Limit = new OrderTypeMask("Limit", 2);
	public static final OrderTypeMask Market = new OrderTypeMask("Market", 4);
	public static final OrderTypeMask MarketOnOpen = new OrderTypeMask("MarketOnOpen", 8);
	public static final OrderTypeMask MarketOnClose = new OrderTypeMask("MarketOnClose", 16);
	public static final OrderTypeMask OneCancelOther = new OrderTypeMask("OneCancelOther", 32);

	public static final OrderTypeMask LimitStop = new OrderTypeMask("LimitStop", 0);

	public static final OrderTypeMask MarketToLimit = new OrderTypeMask("MarketToLimit", 64);
	public static final OrderTypeMask StopLimit = new OrderTypeMask("StopLimit", 128);
	public static final OrderTypeMask FAK_Market = new OrderTypeMask("FAK_Market", 256);

	private OrderTypeMask(String name, int value)
	{
		super(name, value);
	}
}
