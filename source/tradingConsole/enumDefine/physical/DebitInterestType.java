package tradingConsole.enumDefine.physical;

import framework.lang.Enum;

public class DebitInterestType  extends Enum<DebitInterestType>
{
	public static final DebitInterestType SimpleInterest = new DebitInterestType("SimpleInterest", 0);
	public static final DebitInterestType Compounding = new DebitInterestType("Compounding", 1);
	public static final DebitInterestType SimplePrincipalCompoundingInterest = new DebitInterestType("SimplePrincipalCompoundingInterest", 3);

	private DebitInterestType(String name, int value)
	{
		super(name, value);
	}
}
