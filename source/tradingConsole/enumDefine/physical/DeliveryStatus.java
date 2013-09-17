package tradingConsole.enumDefine.physical;

import tradingConsole.ui.language.ScrapDepositStatusLanguage;
import tradingConsole.ui.language.Language;
import framework.lang.Enum;
import tradingConsole.ui.language.DeliveryStatusLanguage;

public class DeliveryStatus extends Enum<DeliveryStatus>
{
	public static final DeliveryStatus Accepted = new DeliveryStatus("Accepted", 0);
	public static final DeliveryStatus Approved = new DeliveryStatus("Approved", 1);
	public static final DeliveryStatus ReadyForDelivery = new DeliveryStatus("ReadyForDelivery", 2);
	public static final DeliveryStatus Deliveried = new DeliveryStatus("Deliveried", 3);
	public static final DeliveryStatus OrderCreated = new DeliveryStatus("OrderCreated", 4);
	public static final DeliveryStatus Hedge = new DeliveryStatus("Hedge", 5);
	public static final DeliveryStatus Canceled = new DeliveryStatus("Canceled", 100);

	private DeliveryStatus(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.equals(Accepted))
		{
			return DeliveryStatusLanguage.Accepted;
		}
		else if(this.equals(Approved))
		{
			return DeliveryStatusLanguage.Approved;
		}
		else if(this.equals(ReadyForDelivery))
		{
			return DeliveryStatusLanguage.ReadyForDelivery;
		}
		else if(this.equals(Deliveried))
		{
			return DeliveryStatusLanguage.Deliveried;
		}
		else if(this.equals(OrderCreated))
		{
			return DeliveryStatusLanguage.OrderCreated;
		}
		else if(this.equals(Hedge))
		{
			return DeliveryStatusLanguage.Hedge;
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
