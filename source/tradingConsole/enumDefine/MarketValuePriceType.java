package tradingConsole.enumDefine;

import framework.lang.Enum;

public class MarketValuePriceType extends Enum<MarketValuePriceType>
{
	public static final MarketValuePriceType DayOpenPrice = new MarketValuePriceType("None", 0);
	public static final MarketValuePriceType MarketPrice = new MarketValuePriceType("HasSellOnly", 1);

	private MarketValuePriceType(String name, int value)
	{
		super(name, value);
	}
}
