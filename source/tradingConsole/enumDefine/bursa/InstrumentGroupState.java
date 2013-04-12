package tradingConsole.enumDefine.bursa;

import framework.lang.Enum;
import tradingConsole.enumDefine.OrderType;

public class InstrumentGroupState extends Enum<InstrumentGroupState>
{
	public static final InstrumentGroupState StartConsultation = new InstrumentGroupState("StartConsultation", 0, true, true, true);
	public static final InstrumentGroupState Preopening = new InstrumentGroupState("Preopening", 1, true, true, true);
	public static final InstrumentGroupState OpeingOrClosing = new InstrumentGroupState("OpeingOrClosing", 2, false, false, false);
	public static final InstrumentGroupState Intervention = new InstrumentGroupState("Intervention", 3, true, true, true);
	public static final InstrumentGroupState ContinuousTrading = new InstrumentGroupState("ContinuousTrading", 4, true, true, true);
	public static final InstrumentGroupState TradingAtLast = new InstrumentGroupState("TradingAtLast", 5, true, false, false);
	public static final InstrumentGroupState SuveillanceIntervention = new InstrumentGroupState("SuveillanceIntervention", 6, true, true, true);
	public static final InstrumentGroupState EndConsultation = new InstrumentGroupState("EndConsultation", 7, true, true, true);
	public static final InstrumentGroupState PostSession = new InstrumentGroupState("PostSession", 8, false, false, false);
	public static final InstrumentGroupState Forbidden = new InstrumentGroupState("Forbidden", 9, false, false, false);
	public static final InstrumentGroupState Interrupted = new InstrumentGroupState("Interrupted", 10, false, false, false);

	private boolean _canPlaceOrder;
	private boolean _canModifyOrder;
	private boolean _canCancelOrder;

	private InstrumentGroupState(String name, int value, boolean canPalceOrder, boolean canModifyOrder, boolean canCancelOrder)
	{
		super(name, value);

		this._canPlaceOrder = canPalceOrder;
		this._canModifyOrder = canModifyOrder;
		this._canCancelOrder = canCancelOrder;
	}

	public boolean get_CanPlaceOrder(OrderType orderType)
	{
		if(this == InstrumentGroupState.TradingAtLast && orderType == OrderType.Limit)
		{
			return true;
		}
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
