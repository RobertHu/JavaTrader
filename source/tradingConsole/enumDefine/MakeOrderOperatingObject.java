package tradingConsole.enumDefine;

import framework.lang.Enum;

public class MakeOrderOperatingObject extends Enum<MakeOrderOperatingObject>
{
	public static final MakeOrderOperatingObject None = new MakeOrderOperatingObject("None", 0);
	public static final MakeOrderOperatingObject OrderType = new MakeOrderOperatingObject("OrderType", 1);
	public static final MakeOrderOperatingObject Account = new MakeOrderOperatingObject("Account", 2);
	public static final MakeOrderOperatingObject IsBuy = new MakeOrderOperatingObject("IsBuy", 3);
	public static final MakeOrderOperatingObject Lot = new MakeOrderOperatingObject("Lot", 4);
	public static final MakeOrderOperatingObject Other = new MakeOrderOperatingObject("Other", 5);

	private MakeOrderOperatingObject(String name, int value)
	{
		super(name, value);
	}
}
