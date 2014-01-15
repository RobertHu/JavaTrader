package tradingConsole.physical;

import java.math.*;
import java.util.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.enumDefine.physical.*;
import tradingConsole.ui.*;
import tradingConsole.ui.IColumnVisibilityProvider.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;

public class InventoryManager implements IColumnVisibilityProvider
{
	public static InventoryManager instance = new InventoryManager();

	private static final int OpenInventoryType = 0;
	private static final int ShortSellInventoryType = 1;

	private BindingSource bindingSource;
	private HierarchicalInventoryModel hierarchicalInventoryModel;
	private ArrayList<Inventory> inventories = new ArrayList<Inventory>();

	private BindingSource shortSellBindingSource = new BindingSource();
	private ArrayList<Order> shortSellOrders = new ArrayList<Order>();

	private InventoryManager()
	{
		this.hierarchicalInventoryModel = new HierarchicalInventoryModel();
		this.bindingSource = new BindingSource(this.hierarchicalInventoryModel);
		this.hierarchicalInventoryModel.set_Owner(this.bindingSource);

		if (ColorSettings.useBlackAsBackground)
		{
			this.bindingSource.useBlackAsBackground();
			this.shortSellBindingSource.useBlackAsBackground();
		}

	}

	public BindingSource get_BindingSource(boolean forShortSell)
	{
		return forShortSell ? this.shortSellBindingSource : this.bindingSource;
	}

	public Collection<Inventory> get_Inventories()
	{
		return this.inventories;
	}

	public Collection<Order> get_ShortSellOrders()
	{
		return this.shortSellOrders;
	}

	public void initialize(Collection<Transaction> transactions)
	{
		this.inventories.clear();
		this.bindingSource.removeAll();

		this.shortSellOrders.clear();
		this.shortSellBindingSource.removeAll();

		for(Transaction transaction : transactions)
		{
			this.add(transaction, true);
		}
	}

	public void add(Transaction transaction)
	{
		this.add(transaction, false);
	}

	public RemoveTransactionResult remove(Order order)
	{
		int inventoryType = this.getInventoryType(order);
		if (inventoryType == InventoryManager.OpenInventoryType)
		{
			Inventory inventory = this.getInventory(order.get_Account(), order.get_Instrument());
			if(inventory != null)
			{
				inventory.remove(order);
				if(inventory.get_Orders().size() == 0)
				{
					this.inventories.remove(inventory);
					TradingConsole.bindingManager.remove(Inventory.bindingKey, inventory);
					return RemoveTransactionResult.yes;
				}
				else
				{
					TradingConsole.bindingManager.update(Inventory.bindingKey, inventory);
				}
			}
		}
		else if (inventoryType == InventoryManager.ShortSellInventoryType)
		{
			this.shortSellOrders.remove(order);
			TradingConsole.bindingManager.remove(Inventory.shortSellBindingKey, order);
		}
		return RemoveTransactionResult.no;
	}

	public RemoveTransactionResult remove(Transaction transaction)
	{
		Inventory inventory = null;
		if (transaction.get_Instrument().get_Category().equals(InstrumentCategory.Physical))
		{
			for (Order order : transaction.get_Orders().values())
			{
				int inventoryType = this.getInventoryType(order);
				if (inventoryType == InventoryManager.OpenInventoryType)
				{
					if(inventory == null) inventory = this.getInventory(transaction.get_Account(), transaction.get_Instrument());
					if(inventory != null)
					{
						inventory.remove(order);
						TradingConsole.bindingManager.update(Inventory.bindingKey, inventory);
					}
				}
				else if (inventoryType == InventoryManager.ShortSellInventoryType)
				{
					this.shortSellOrders.remove(order);
					TradingConsole.bindingManager.remove(Inventory.shortSellBindingKey, order);
				}
			}
		}

		if(inventory != null && inventory.get_Orders().size() == 0)
		{
			this.inventories.remove(inventory);
			TradingConsole.bindingManager.remove(Inventory.bindingKey, inventory);
			return RemoveTransactionResult.yes;
		}
		return RemoveTransactionResult.no;
	}

	public void add(Transaction transaction, boolean isInit)
	{
		Inventory inventory = null;
		if (transaction.get_Instrument().get_Category().equals(InstrumentCategory.Physical))
		{
			for (Order order : transaction.get_Orders().values())
			{
				if(order.get_Phase().equals(Phase.Executed) && order.get_PhysicalTradeSide().equals(PhysicalTradeSide.Delivery))
				{
					DeliveryRequest request = PendingInventoryManager.instance.getDeliveryRequest(order.get_PhysicalRequestId());
					if(request != null)
					{
						TradingConsole.bindingManager.remove(PendingInventory.bindingKey, request);
						/*request.setStatus(DeliveryStatus.OrderCreated);
						request.update();*/
					}
				}

				if(!order.get_Phase().equals(Phase.Executed) || order.get_LotBalance().compareTo(BigDecimal.ZERO) <= 0)
				{
					continue;
				}

				int inventoryType = this.getInventoryType(order);
				if (inventoryType == InventoryManager.OpenInventoryType)
				{
					if(inventory == null) inventory = this.getOrCreateInventory(transaction.get_Account(), transaction.get_Instrument(), isInit);
					inventory.add(order);
					PendingInventoryManager.instance.remove(order.get_PhysicalRequestId());
					this.tryToFireColumnVisibilityChangedEvent(inventory, order);
				}
				else if (inventoryType == InventoryManager.ShortSellInventoryType)
				{
					this.shortSellOrders.add(order);
					if(!isInit)
					{
						this.shortSellBindingSource.add(order);
						PendingInventoryManager.instance.remove(order.get_PhysicalRequestId());
					}
					//if(!isInit) TradingConsole.bindingManager.update(Inventory.shortSellBindingKey, inventory);
				}
			}
		}

		if(!isInit && inventory != null)
		{
			TradingConsole.bindingManager.update(Inventory.bindingKey, inventory);
		}
	}

	private int getInventoryType(Order order)
	{
		if(order.get_PhysicalTradeSide().equals(PhysicalTradeSide.Buy)
			|| order.get_PhysicalTradeSide().equals(PhysicalTradeSide.Deposit))
		{
			return InventoryManager.OpenInventoryType;
		}
		else if(order.get_PhysicalTradeSide().equals(PhysicalTradeSide.ShortSell))
		{
			return InventoryManager.ShortSellInventoryType;
		}
		else
		{
			return -1;
		}
	}

	public void recaculateMarketValue(Order order)
	{
		Inventory inventory = InventoryManager.instance.getInventory(order.get_Account(), order.get_Instrument());
		if(inventory != null) inventory.recaculateMarketValue();
	}

	Inventory getInventory(Account account, Instrument instrument)
	{
		for(Inventory item : this.inventories)
		{
			if(item.isFor(account, instrument)) return item;
		}
		return null;
	}

	private Inventory getOrCreateInventory(Account account, Instrument instrument, boolean isInit)
	{
		Inventory inventory = this.getInventory(account, instrument);
		if(inventory == null)
		{
			inventory = new Inventory(account, instrument);
			this.inventories.add(inventory);
			if(!isInit) TradingConsole.bindingManager.add(Inventory.bindingKey, inventory);
		}
		return inventory;
	}

	public void update(Order order, String propertyName, Object value)
	{
		if(order.get_PhysicalTradeSide().equals(PhysicalTradeSide.ShortSell))
		{
			if(this.shortSellOrders.contains(order))
			{
				TradingConsole.bindingManager.update(Inventory.shortSellBindingKey, order, propertyName, value);
			}
			else
			{
				this.shortSellOrders.add(order);
				this.shortSellBindingSource.add(order);
				PendingInventoryManager.instance.remove(order.get_PhysicalRequestId());
			}
		}
		else
		{
			Inventory inventory = this.getInventory(order.get_Account(), order.get_Instrument());
			if (inventory != null)
			{
				inventory.update(order, propertyName, value);
				this.tryToFireColumnVisibilityChangedEvent(inventory, order);
			}
			else
			{
				this.add(order.get_Transaction());
			}
		}
	}

	public void update(Order order)
	{
		if(order.get_PhysicalTradeSide().equals(PhysicalTradeSide.ShortSell))
		{
			if(this.shortSellOrders.contains(order))
			{
				TradingConsole.bindingManager.update(Inventory.shortSellBindingKey, order);
			}
			else
			{
				this.shortSellOrders.add(order);
				this.shortSellBindingSource.add(order);
				PendingInventoryManager.instance.remove(order.get_PhysicalRequestId());
			}
			TradingConsole.bindingManager.setForeground(Inventory.shortSellBindingKey, order, OpenOrderColKey.TradePLFloatString, NumericColor.getColor(order.get_FloatTradingItem().get_Trade(), true));
		}
		else
		{
			Inventory inventory = this.getInventory(order.get_Account(), order.get_Instrument());
			if (inventory != null)
			{
				inventory.update(order);
				if(inventory.get_Orders().size() == 0)
				{
					this.inventories.remove(inventory);
					TradingConsole.bindingManager.remove(Inventory.bindingKey, inventory);
				}
				else
				{
					TradingConsole.bindingManager.update(Inventory.bindingKey, inventory);
					this.tryToFireColumnVisibilityChangedEvent(inventory, order);
				}
			}
			else
			{
				this.add(order.get_Transaction());
			}
		}
	}

	public boolean hasInventoryOf(Instrument instrument)
	{
		for(Inventory inventory : this.inventories)
		{
			if(inventory.get_Instrument2() == instrument) return true;
		}
		for(Order order : this.shortSellOrders)
		{
			if(order.get_Instrument() == instrument) return true;
		}
		return false;
	}

	public boolean isVisible(BindingSource bindingSource, String columnName)
	{
		for(Inventory inventory : this.inventories)
		{
			if(inventory.get_BindingSource() == bindingSource)
			{
				if(columnName.equalsIgnoreCase(PhysicalInventoryColKey.UnpaidValueString))
				{
					for(Order order : inventory.get_Orders())
					{
						if(order.get_UnpaidValue() != null && order.get_UnpaidValue().compareTo(BigDecimal.ZERO) > 0)
						{
							return true;
						}
					}
					return false;
				}
				if(columnName.equalsIgnoreCase(PhysicalInventoryColKey.PaidValueString))
				{
					for(Order order : inventory.get_Orders())
					{
						if(order.get_PaidValue() != null && order.get_PaidValue().compareTo(BigDecimal.ZERO) != 0)
						{
							return true;
						}
					}
					return false;
				}
				else if(columnName.equalsIgnoreCase(PhysicalInventoryColKey.PedgeValueString))
				{
					for(Order order : inventory.get_Orders())
					{
						if(order.get_ValueAsMargin() > 0)
						{
							return true;
						}
					}
					return false;

				}
				else
				{
					return true;
				}
			}
		}
		return true;
	}

	private ArrayList<IColumnVisibilityChangedHandler> _columnVisibilityChangedHandlers = new ArrayList<IColumnVisibilityChangedHandler>();
	public void addColumnVisibilityChangedHandler(IColumnVisibilityChangedHandler handler)
	{
		this._columnVisibilityChangedHandlers.add(handler);
	}

	private void fireColumnVisibilityChangedEvent(BindingSource model, String columnName, boolean isVisible)
	{
		for(IColumnVisibilityChangedHandler handler : this._columnVisibilityChangedHandlers)
		{
			handler.handleColumnVisibilityChanged(model, columnName, isVisible);
		}
	}

	private void tryToFireColumnVisibilityChangedEvent(Inventory inventory, Order order)
	{
		if(this._columnVisibilityChangedHandlers != null
		   && this._columnVisibilityChangedHandlers.size() > 0)
		{
			if(order.get_UnpaidValue() != null && order.get_UnpaidValue().compareTo(BigDecimal.ZERO) > 0)
			{
				this.fireColumnVisibilityChangedEvent(inventory.get_BindingSource(), PhysicalInventoryColKey.UnpaidValueString, true);
			}
			if(order.get_ValueAsMargin() > 0)
			{
				this.fireColumnVisibilityChangedEvent(inventory.get_BindingSource(), PhysicalInventoryColKey.PedgeValueString, true);
			}
		}
	}

	public static class RemoveTransactionResult
	{
		public static final RemoveTransactionResult yes = new RemoveTransactionResult(true);
		public static final RemoveTransactionResult no = new RemoveTransactionResult(false);

		public boolean hasInventoryRemoved;

		private RemoveTransactionResult(boolean hasInventoryRemoved)
		{
			this.hasInventoryRemoved = hasInventoryRemoved;
		}
	}
}
