package tradingConsole.enumDefine;

import framework.lang.Enum;
import tradingConsole.ui.language.*;

public class OrderType extends Enum<OrderType>
{
	public static final OrderType SpotTrade = new OrderType("SpotTrade", 0);
	public static final OrderType Limit = new OrderType("Limit", 1);
	public static final OrderType Market = new OrderType("Market", 2);
	public static final OrderType MarketOnOpen = new OrderType("MarketOnOpen", 3);
	public static final OrderType MarketOnClose = new OrderType("MarketOnClose", 4);
	public static final OrderType OneCancelOther = new OrderType("OneCancelOther", 5);
	public static final OrderType Risk = new OrderType("Risk", 6);
	public static final OrderType Stop = new OrderType("Stop", 7);
	public static final OrderType MultipleClose = new OrderType("MultipleClose", 8);

	public static final OrderType MarketToLimit = new OrderType("MarketToLimit", 9);
	public static final OrderType StopLimit = new OrderType("StopLimit", 10);
	public static final OrderType FAK_Market = new OrderType("FAK_Market", 11);

	private OrderType(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(OrderType orderType)
	{
		if (orderType.equals(OrderType.SpotTrade))
		{
			return Language.DQPrompt;
		}
		else if (orderType.equals(OrderType.Limit))
		{
			return Language.LMTPrompt;
		}
		else if (orderType.equals(OrderType.Market))
		{
			return Language.MKTPrompt;
		}
		else if (orderType.equals(OrderType.MarketOnOpen))
		{
			return Language.MOOPrompt;
		}
		else if (orderType.equals(OrderType.MarketOnClose))
		{
			return Language.MOCPrompt;
		}
		else if (orderType.equals(OrderType.OneCancelOther))
		{
			return Language.OCOPrompt;
		}
		else if (orderType.equals(OrderType.Risk))
		{
			return Language.SYSPrompt;
		}
		else if(orderType.equals(OrderType.MultipleClose))
		{
			return Language.MultipleClosePrompt;
		}

		else if(orderType.equals(OrderType.MarketToLimit))
		{
			return Language.MarketToLimit;
		}
		else if(orderType.equals(OrderType.StopLimit))
		{
			return Language.StopLimit;
		}
		else if(orderType.equals(OrderType.FAK_Market))
		{
			return Language.FAK_Market;
		}
		return "";
	}

	public boolean isSpot()
	{
		return this == OrderType.Market || this == OrderType.SpotTrade;
	}
}
