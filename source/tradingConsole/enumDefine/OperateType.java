package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OperateType extends Enum<OperateType>
{
	public static final OperateType MultiSpotTrade = new OperateType("MultiSpotTrade", 0);
	public static final OperateType SingleSpotTrade = new OperateType("SingleSpotTrade", 1);
	public static final OperateType Method2SpotTrade = new OperateType("Method2SpotTrade", 2);
	public static final OperateType Limit = new OperateType("Limit", 3);
	public static final OperateType DirectLiq = new OperateType("DirectLiq", 4);
	public static final OperateType Assign = new OperateType("Assign", 5);
	public static final OperateType OneCancelOther = new OperateType("OneCancelOther", 6);
	public static final OperateType LimitStop = new OperateType("LimitStop", 7);
	public static final OperateType MultipleClose = new OperateType("MultipleClose", 8);

	private OperateType(String name, int value)
	{
		super(name, value);
	}
}
