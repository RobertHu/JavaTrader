package tradingConsole.enumDefine;

import framework.lang.Enum;

public class OrderLevelRiskBase extends Enum<OrderLevelRiskBase>
{
	public static final OrderLevelRiskBase None = new OrderLevelRiskBase("None", 0);
	public static final OrderLevelRiskBase Necessary = new OrderLevelRiskBase("Necessary", 1);
	public static final OrderLevelRiskBase OpenPrice = new OrderLevelRiskBase("OpenPrice", 2);
	public static final OrderLevelRiskBase SettlementPrice = new OrderLevelRiskBase("SettlementPrice", 3);

	private OrderLevelRiskBase(String name, int value)
	{
		super(name, value);
	}
}
