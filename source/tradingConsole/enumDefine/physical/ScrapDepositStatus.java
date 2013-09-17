package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.language.ScrapDepositStatusLanguage;

public class ScrapDepositStatus extends Enum<ScrapDepositStatus>
{
	public static final ScrapDepositStatus WaitingForAssay = new ScrapDepositStatus("WaitingForAssay", 0);
	public static final ScrapDepositStatus Assaied = new ScrapDepositStatus("Assaied", 1);
	public static final ScrapDepositStatus OrderCreated = new ScrapDepositStatus("Sell", 2);
	public static final ScrapDepositStatus Canceled = new ScrapDepositStatus("Canceled", 100);

	private ScrapDepositStatus(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.equals(WaitingForAssay))
		{
			return ScrapDepositStatusLanguage.WaitingForAssay;
		}
		else if(this.equals(Assaied))
		{
			return ScrapDepositStatusLanguage.Assayed;
		}
		else if(this.equals(OrderCreated))
		{
			return ScrapDepositStatusLanguage.OrderCreated;
		}
		else if(this.equals(Canceled))
		{
			return Language.PhaseCancelledPrompt;
		}
		else
		{
			return this.toString();
		}
	}
}
