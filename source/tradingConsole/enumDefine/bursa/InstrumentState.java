package tradingConsole.enumDefine.bursa;

import framework.lang.Enum;

public class InstrumentState extends Enum<InstrumentState>
{
	public static InstrumentState None = new InstrumentState("None", 0, true, true, true);
	public static InstrumentState Authorised = new InstrumentState("Authorised", 1, true, true, true);
	public static InstrumentState Forbid = new InstrumentState("Forbid", 2, false, false, false);
	public static InstrumentState Open = new InstrumentState("Open", 3, true, true, true);
	public static InstrumentState Reserved = new InstrumentState("Reserved", 4, true, true, true);
	public static InstrumentState Suspended = new InstrumentState("Suspended", 5, false, false, false);
	public static InstrumentState AuthorizeOrderEntry = new InstrumentState("AuthorizeOrderEntry", 6, true, false, false);
	public static InstrumentState ForbidOrderEntry = new InstrumentState("ForbidOrderEntry", 7, false, false, false);

	private boolean _canPlaceOrder;
	private boolean _canModifyOrder;
	private boolean _canCancelOrder;

	private InstrumentState(String name, int value, boolean canPalceOrder, boolean canModifyOrder, boolean canCancelOrder)
	{
		super(name, value);
		this._canPlaceOrder = canPalceOrder;
		this._canModifyOrder = canModifyOrder;
		this._canCancelOrder = canCancelOrder;
	}

	public boolean get_CanPlaceOrder()
	{
		return this._canPlaceOrder;
	}

	public boolean get_CanModifyOrder()
	{
		return this._canModifyOrder;
	}

	public boolean get_CanCancelOrder()
	{
		return this._canCancelOrder;
	}
}
