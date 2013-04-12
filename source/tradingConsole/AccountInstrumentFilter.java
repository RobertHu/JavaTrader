package tradingConsole;

import com.jidesoft.grid.AbstractTableFilter;
import tradingConsole.ui.grid.BindingSource;
import tradingConsole.enumDefine.Phase;
import tradingConsole.enumDefine.OrderType;

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
		Order order = (Order)this._bindingSource.getObject(this.getRowIndex());
		if(this._onlyShowNotConfirmedPendingOrder)
		{
			OrderType orderType = order.get_Transaction().get_OrderType();
			if((orderType.equals(OrderType.Limit) || orderType.equals(OrderType.Limit)
				|| orderType.equals(OrderType.OneCancelOther)
			   || orderType.equals(OrderType.MarketOnOpen)
			  || orderType.equals(OrderType.MarketOnClose))
			   && (order.get_Phase().equals(Phase.Placed) ||order.get_Phase().equals(Phase.Placing)))
			{
			}
			else
			{
				return true;
			}
		}

		return !order.get_Account().get_Select() || !order.get_Transaction().get_Instrument().get_Select();
	}
}
