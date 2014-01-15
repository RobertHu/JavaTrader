package tradingConsole.enumDefine;

import framework.lang.Enum;

public class MarketValuePriceType extends Enum<MarketValuePriceType>
{
	public static final MarketValuePriceType DayOpenPrice = new MarketValuePriceType("DayOpenPrice", 0);
	public static final MarketValuePriceType MarketPrice = new MarketValuePriceType("MarketPrice", 1);
	public static final MarketValuePriceType UnitFixAmount = new MarketValuePriceType("UnitFixAmount", 2);

	private MarketValuePriceType(String name, int value)
	{
		super(name, value);
	}
}
