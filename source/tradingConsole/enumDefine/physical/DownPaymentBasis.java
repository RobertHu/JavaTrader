package tradingConsole.enumDefine.physical;

import framework.lang.Enum;

public class DownPaymentBasis extends Enum<DownPaymentBasis>
{
	public static final DownPaymentBasis PercentageOfAmount = new DownPaymentBasis("PercentageOfAmount", 0);
	public static final DownPaymentBasis FixedAmountPerLot = new DownPaymentBasis("FixedAmountPerLot", 1);

	private DownPaymentBasis(String name, int value)
	{
		super(name, value);
	}
}
