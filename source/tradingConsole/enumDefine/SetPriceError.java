package tradingConsole.enumDefine;

import framework.lang.Enum;

public class SetPriceError extends Enum<SetPriceError>
{
	public static final SetPriceError Ok = new SetPriceError("Ok", 0);
	public static final SetPriceError SetPriceTooCloseMarket = new SetPriceError("SetPriceTooCloseMarket", -1);
	public static final SetPriceError SetPriceTooFarAwayMarket = new SetPriceError("SetPriceTooFarAwayMarket", -2);
	public static final SetPriceError InvalidSetPrice = new SetPriceError("InvalidPrice", -3);
	public static final SetPriceError SetPriceBetweenAskAndBid = new SetPriceError("SetPriceBetweenAskAndBid", -4);

	private SetPriceError(String name, int value)
	{
		super(name, value);
	}
}
