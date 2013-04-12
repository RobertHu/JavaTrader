package tradingConsole.enumDefine;

import framework.lang.Enum;

public class RiskDisclosureStatementType extends Enum<RiskDisclosureStatementType>
{
	public static final RiskDisclosureStatementType None = new RiskDisclosureStatementType("None", 0);
	public static final RiskDisclosureStatementType Common = new RiskDisclosureStatementType("Common", 1);
	public static final RiskDisclosureStatementType Special = new RiskDisclosureStatementType("Special", 2);

	private RiskDisclosureStatementType(String name, int value)
	{
		super(name, value);
	}
}
