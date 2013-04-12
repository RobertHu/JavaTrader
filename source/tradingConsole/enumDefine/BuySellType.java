package tradingConsole.enumDefine;

import framework.lang.Enum;

public class BuySellType extends Enum<BuySellType>
{
	public static final BuySellType Both = new BuySellType("Both", 0);
	public static final BuySellType Buy = new BuySellType("Buy", 1);
	public static final BuySellType Sell = new BuySellType("Sell", 2);

	private BuySellType(String name, int value)
	{
		super(name, value);
	}
}
