package tradingConsole.enumDefine;

import framework.lang.Enum;

public class AllowedOrderSides extends Enum<AllowedOrderSides>
{
	public static final AllowedOrderSides AllowNoe = new AllowedOrderSides("None", 0);
	public static final AllowedOrderSides AllowBuy = new AllowedOrderSides("Buy", 1);
	public static final AllowedOrderSides AllowSell = new AllowedOrderSides("Sell", 2);
	public static final AllowedOrderSides AllowAll = new AllowedOrderSides("All", 3);

	private AllowedOrderSides(String name, int value)
	{
		super(name, value);
	}
}
