package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.InstalmentTypeLanguage;
import tradingConsole.ui.language.InstalmentLanguage;

public class PaymentMode extends Enum<PaymentMode>
{
	public static final PaymentMode FullAmount = new PaymentMode("FullAmount", 0);
	public static final PaymentMode Instalment = new PaymentMode("Instalment", 1);
	public static final PaymentMode AdvancePayment = new PaymentMode("AdvancePayment", 2);

	private PaymentMode(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this == PaymentMode.FullAmount)
		{
			return InstalmentTypeLanguage.FullAmount;
		}
		else if(this == PaymentMode.Instalment)
		{
			return InstalmentLanguage.Instalment;
		}
		else if(this == PaymentMode.AdvancePayment)
		{
			return InstalmentLanguage.AdvancePayment;
		}
		else
		{
			return this.toString();
		}
	}
}
