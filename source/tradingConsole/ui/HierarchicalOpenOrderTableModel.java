package tradingConsole.ui;

import com.jidesoft.grid.HierarchicalTableModel;
import tradingConsole.Order;
import tradingConsole.ui.grid.BindingSource;

public class HierarchicalOpenOrderTableModel implements HierarchicalTableModel
{
	private BindingSource _owner;

	public void set_Owner(BindingSource owner)
	{
		this._owner = owner;
	}

	public BindingSource get_Owner()
	{
		return this._owner;
	}

	public boolean hasChild(int row)
	{
		Order order = (Order)this._owner.getObject(row);
		BindingSource closeOrders = order.get_CloseOrders();
		return closeOrders != null && closeOrders.getRowCount() > 0;
	}

	public boolean isHierarchical(int row)
	{
		Order order = (Order)this._owner.getObject(row);
		BindingSource closeOrders = order.get_CloseOrders();
		return closeOrders != null && closeOrders.getRowCount() > 0;
	}

	public Object getChildValueAt(int row)
	{
		Order order = (Order)this._owner.getObject(row);
		return order.get_CloseOrders();
	}

	public boolean isExpandable(int row)
	{
		Order order = (Order)this._owner.getObject(row);
		BindingSource closeOrders = order.get_CloseOrders();
		return closeOrders != null && closeOrders.getRowCount() > 0;
	}
}
