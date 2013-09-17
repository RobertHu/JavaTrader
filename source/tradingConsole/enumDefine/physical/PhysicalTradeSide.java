package tradingConsole.enumDefine.physical;

import framework.lang.Enum;

public class PhysicalTradeSide extends Enum<PhysicalTradeSide>
{
	public static final PhysicalTradeSide None = new PhysicalTradeSide("None", 0);
	public static final PhysicalTradeSide Buy = new PhysicalTradeSide("Buy", 1);
	public static final PhysicalTradeSide Sell = new PhysicalTradeSide("Sell", 2);
	public static final PhysicalTradeSide ShortSell = new PhysicalTradeSide("ShortSell", 4);
	public static final PhysicalTradeSide Delivery = new PhysicalTradeSide("Delivery", 8);
	public static final PhysicalTradeSide Deposit = new PhysicalTradeSide("Deposit", 16);

	private PhysicalTradeSide(String name, int value)
	{
		super(name, value);
	}
}
