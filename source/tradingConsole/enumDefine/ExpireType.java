package tradingConsole.enumDefine;

import framework.lang.Enum;

public class ExpireType extends Enum<ExpireType>
{
	public static final ExpireType Day = new ExpireType("Day", 0);
	public static final ExpireType GTC = new ExpireType("GTC", 1);
	public static final ExpireType IOC = new ExpireType("IOC", 2);
	public static final ExpireType GTD = new ExpireType("GTD", 3);
	public static final ExpireType Session = new ExpireType("Session", 4);
	public static final ExpireType FillOrKill = new ExpireType("FillOrKill", 5);
    public static final ExpireType FillAndKill = new ExpireType("FillAndKill", 6);

	private ExpireType(String name, int value)
	{
		super(name, value);
	}
}
