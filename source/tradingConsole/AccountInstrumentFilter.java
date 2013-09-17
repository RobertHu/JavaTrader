package tradingConsole;

import com.jidesoft.grid.AbstractTableFilter;
import tradingConsole.ui.grid.BindingSource;
import tradingConsole.enumDefine.Phase;
import tradingConsole.enumDefine.OrderType;
import tradingConsole.physical.PendingInventory;
import tradingConsole.physical.DeliveryRequest;
import tradingConsole.physical.Inventory;
import tradingConsole.physical.ScrapDeposit;

public class AccountInstrumentFilter extends AbstractTableFilter
{
	private BindingSource _bindingSource;
	private boolean _onlyShowNotConfirmedPendingOrder = false;

	public AccountInstrumentFilter(BindingSource bindingSource, boolean onlyShowNotConfirmedPendingOrder)
	{
		this._bindingSource = bindingSource;
		this._onlyShowNotConfirmedPendingOrder = onlyShowNotConfirmedPendingOrder;
	}

	public boolean isValueFiltered(Object object)
	{
		Object object2 = this._bindingSource.getObject(this.getRowIndex());
		if(object2 instanceof Order)
		{
			Order order = (Order)object2;
			if (this._onlyShowNotConfirmedPendingOrder)
			{
				OrderType orderType = order.get_Transaction().get_OrderType();
				if ( (orderType.equals(OrderType.Limit) || orderType.equals(OrderType.Limit)
					  || orderType.equals(OrderType.OneCancelOther)
					  || orderType.equals(OrderType.MarketOnOpen)
					  || orderType.equals(OrderType.MarketOnClose))
					&& (order.get_Phase().equals(Phase.Placed) || order.get_Phase().equals(Phase.Placing)))
				{
				}
				else
				{
					return true;
				}
			}

			return !order.get_Account().get_Select() || !order.get_Transaction().get_Instrument().get_Select();
		}
		else if(object2 instanceof DeliveryRequest)
		{
			DeliveryRequest deliveryRequest = (DeliveryRequest)object2;
			return !deliveryRequest.getAccount().get_Select() || !deliveryRequest.getInstrument().get_Select();
		}
		else if(object2 instanceof Inventory)
		{
			Inventory inventory = (Inventory)object2;
			return !inventory.get_Account().get_Select() || !inventory.get_Instrument2().get_Select();
		}
		else if(object2 instanceof ScrapDeposit)
		{
			ScrapDeposit scrapDeposit = (ScrapDeposit)object2;
			return !scrapDeposit.getAccount().get_Select() || !scrapDeposit.getInstrument().get_Select();
		}
		else
		{
			return true;
		}
	}
}
