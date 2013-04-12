package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OperateSource extends Enum<OperateSource>
{
	public static final OperateSource Common = new OperateSource("Common", 0);
	public static final OperateSource LiquidationLMTSTP = new OperateSource("LiquidationLMTSTP", 1);
	public static final OperateSource LiquidationSingleSPT = new OperateSource("LiquidationSingleSPT", 2);
	public static final OperateSource LiquidationMultiSPT = new OperateSource("LiquidationMultiSPT", 3);

	private OperateSource(String name, int value)
	{
		super(name, value);
	}

}
