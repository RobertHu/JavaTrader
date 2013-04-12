package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OrderModifyResult extends Enum<OrderModifyResult>
{
	public static final OrderModifyResult Confirmed = new OrderModifyResult("Confirmed", 0);
	public static final OrderModifyResult Canceled = new OrderModifyResult("Canceled", 1);

	private OrderModifyResult(String name, int value)
	{
		super(name, value);
	}
}
