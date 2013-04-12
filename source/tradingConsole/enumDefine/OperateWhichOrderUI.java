package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OperateWhichOrderUI extends Enum<OperateWhichOrderUI>
{
	public static final OperateWhichOrderUI None = new OperateWhichOrderUI("None", 0);
	public static final OperateWhichOrderUI WorkingOrderList = new OperateWhichOrderUI("WorkingOrderList", 1);
	public static final OperateWhichOrderUI OpenOrderList = new OperateWhichOrderUI("OpenOrderList", 2);
	public static final OperateWhichOrderUI Both = new OperateWhichOrderUI("Both", 3);

	private OperateWhichOrderUI(String name, int value)
	{
		super(name, value);
	}
}
