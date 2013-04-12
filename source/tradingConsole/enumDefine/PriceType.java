package tradingConsole.enumDefine;

import framework.lang.Enum;

public class PriceType extends Enum<PriceType>
{
	public static final PriceType WatchOnly = new PriceType("WatchOnly", 0);
	public static final PriceType RefrenceOnly = new PriceType("RefrenceOnly", 1);
	public static final PriceType DealingEnable = new PriceType("DealingEnable", 2);
	public static final PriceType OriginEnable = new PriceType("OriginEnable", 3);

	private PriceType(String name, int value)
	{
		super(name, value);
	}
}
