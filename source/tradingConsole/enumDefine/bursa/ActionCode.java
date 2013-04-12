package tradingConsole.enumDefine.bursa;

import framework.lang.Enum;

public class ActionCode extends Enum<ActionCode>
{
	public static ActionCode DeferrOpen = new ActionCode("DeferrOpen", 0);
	public static ActionCode CancelDeferrOpen = new ActionCode("CancelDeferrOpen", 1);
	public static ActionCode Reserve = new ActionCode("Reserve", 2);
	public static ActionCode Trading = new ActionCode("Trading", 3);
	public static ActionCode Open = new ActionCode("Open", 4);
	public static ActionCode Suspend = new ActionCode("Suspend", 5);
	public static ActionCode AuthorizeOrderEntry = new ActionCode("AuthorizeOrderEntry", 6);
	public static ActionCode ForbidOrderEntry = new ActionCode("ForbidOrderEntry", 7);
	public static ActionCode EliminateAllOrders = new ActionCode("EliminateAllOrders", 8);
	public static ActionCode StateAtInitialization = new ActionCode("StateAtInitialization", 9);
	public static ActionCode StopBroadCastingMarketSheet = new ActionCode("StopBroadCastingMarketSheet", 10);
	public static ActionCode ResumeBroadCastingMarketSheet = new ActionCode("ResumeBroadCastingMarketSheet", 11);
	public static ActionCode Freeze = new ActionCode("Freeze", 12);
	public static ActionCode Thaw = new ActionCode("Thaw", 13);
	public static ActionCode Expiry = new ActionCode("Expiry", 14);
	public static ActionCode Activate = new ActionCode("Activate", 15);

	private ActionCode(String name, int value)
	{
		super(name, value);
	}
}
