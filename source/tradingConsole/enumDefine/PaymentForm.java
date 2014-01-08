package tradingConsole.enumDefine;

import framework.lang.Enum;

public class PaymentForm extends Enum<PaymentForm>
{
	public static final PaymentForm FullPayment = new PaymentForm("FullPayment", 1);
	public static final PaymentForm Prepay = new PaymentForm("Prepay", 2);
	public static final PaymentForm Instalment = new PaymentForm("Instalment", 4);

	private PaymentForm(String name, int value)
	{
		super(name, value);
	}
}
