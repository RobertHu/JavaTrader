package tradingConsole.ui;

import tradingConsole.Order;
import com.jidesoft.grid.HierarchicalTableModel;
import tradingConsole.ui.grid.BindingSource;
import tradingConsole.physical.Inventory;
import java.util.Collection;

public class HierarchicalInventoryModel implements HierarchicalTableModel
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
		Inventory inventory = (Inventory)this._owner.getObject(row);
		Collection<Order> orders = inventory.get_Orders();
		return orders != null && orders.size() > 0;
	}

	public boolean isHierarchical(int row)
	{
		Inventory inventory = (Inventory)this._owner.getObject(row);
		Collection<Order> orders = inventory.get_Orders();
		return orders != null && orders.size() > 0;
	}

	public Object getChildValueAt(int row)
	{
		Inventory inventory = (Inventory)this._owner.getObject(row);
		return inventory.get_BindingSource();
	}

	public boolean isExpandable(int row)
	{
		Inventory inventory = (Inventory)this._owner.getObject(row);
		Collection<Order> orders = inventory.get_Orders();
		return orders != null && orders.size() > 0;
	}
}
