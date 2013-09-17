package tradingConsole.physical;

import java.util.*;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.enumDefine.physical.*;
import tradingConsole.ui.*;
import tradingConsole.ui.grid.*;

public class PendingInventoryManager
{
	public static PendingInventoryManager instance = new PendingInventoryManager();

	private HashMap<Guid, PendingInventory> pendingInventories = new HashMap<Guid, PendingInventory>();
	private HashMap<Guid, ArrayList<PendingInventory>> account2PendingInventories = new HashMap<Guid, ArrayList<PendingInventory>>();

	private BindingSource bindingSource = new BindingSource();

	private TradingConsole tradingConsole;

	private PendingInventoryManager(){}

	public void initialize(DataSet initData, TradingConsole tradingConsole)
	{
		this.tradingConsole = tradingConsole;

		this.pendingInventories.clear();
		this.account2PendingInventories.clear();
		this.bindingSource.removeAll();

		if (ColorSettings.useBlackAsBackground)
		{
			this.bindingSource.useBlackAsBackground();
		}

		DataTable table = initData.get_Tables().get_Item("DeliveryRequest");
		if(table != null && table.get_Rows().get_Count() > 0)
		{
			for(int index = 0; index < table.get_Rows().get_Count(); index++)
			{
				DataRow dataRow = table.get_Rows().get_Item(index);
				DeliveryRequest deliveryRequest = new DeliveryRequest();
				deliveryRequest.initialize(dataRow, tradingConsole.get_SettingsManager());

				this.add(deliveryRequest, true);
			}
		}

		table = initData.get_Tables().get_Item("DeliveryRequestOrderRelation");
		if(table != null && table.get_Rows().get_Count() > 0)
		{
			for(int index = 0; index < table.get_Rows().get_Count(); index++)
			{
				DataRow dataRow = table.get_Rows().get_Item(index);
				DeliveryRequestOrderRelation deliveryRequestOrderRelation = new DeliveryRequestOrderRelation();
				deliveryRequestOrderRelation.initialize(dataRow);

				DeliveryRequest deliveryRequest = (DeliveryRequest)this.pendingInventories.get(deliveryRequestOrderRelation.getDeliveryRequestId());
				deliveryRequest.add(deliveryRequestOrderRelation);

				Order order = this.tradingConsole.getOrder(deliveryRequestOrderRelation.getOpenOrderId());
				order.addDeliveryLockLot(deliveryRequestOrderRelation.getDeliveryLot());
			}
		}

		table = initData.get_Tables().get_Item("ScrapDeposit");
		if(table != null && table.get_Rows().get_Count() > 0)
		{
			for(int index = 0; index < table.get_Rows().get_Count(); index++)
			{
				DataRow dataRow = table.get_Rows().get_Item(index);
				ScrapDeposit scrapDeposit = new ScrapDeposit();
				scrapDeposit.initialize(dataRow, tradingConsole.get_SettingsManager());

				this.add(scrapDeposit, true);
			}
		}
	}

	public Collection<PendingInventory> getValues()
	{
		return this.pendingInventories.values();
	}

	public void add(PendingInventory pendingInventory)
	{
		this.add(pendingInventory, false);
	}

	private void add(PendingInventory pendingInventory, boolean isFromInitialization)
	{
		if(this.pendingInventories.containsKey(pendingInventory.getId())) return;

		this.pendingInventories.put(pendingInventory.getId(), pendingInventory);
		Guid accountId = pendingInventory.getAccount().get_Id();
		if(!this.account2PendingInventories.containsKey(accountId))
		{
			this.account2PendingInventories.put(accountId, new ArrayList<PendingInventory>());
		}
		this.account2PendingInventories.get(accountId).add(pendingInventory);


		if(!isFromInitialization)
		{
			TradingConsole.bindingManager.add(PendingInventory.bindingKey, pendingInventory);
			if(pendingInventory instanceof DeliveryRequest)
			{
				DeliveryRequest deliveryRequest = (DeliveryRequest)pendingInventory;
				this.addOrderLockedDeliveryLots(deliveryRequest);
			}
		}
	}

	public void remove(Guid pendingInventoryId)
	{
		if(pendingInventoryId == null) return;

		if(this.pendingInventories.containsKey(pendingInventoryId))
		{
			PendingInventory pendingInventory = this.pendingInventories.get(pendingInventoryId);
			this.pendingInventories.remove(pendingInventoryId);

			Guid accountId = pendingInventory.getAccount().get_Id();
			ArrayList<PendingInventory> pendingInventories = this.account2PendingInventories.get(accountId);
			int index = -1;
			for (int i = 0; i < pendingInventories.size(); i++)
			{
				if(pendingInventories.get(i).getId().equals(pendingInventoryId))
				{
					index = i;
					break;
				}
			}
			if(index != -1) pendingInventories.remove(index);

			if(pendingInventory instanceof DeliveryRequest)
			{
				DeliveryRequest deliveryRequest = (DeliveryRequest)pendingInventory;
				this.removeOrderLockedDeliveryLots(deliveryRequest);
			}

			TradingConsole.bindingManager.remove(PendingInventory.bindingKey, pendingInventory);
		}
	}

	public BindingSource get_BindingSource()
	{
		return this.bindingSource;
	}

	private void addOrderLockedDeliveryLots(DeliveryRequest deliveryRequest)
	{
		for(DeliveryRequestOrderRelation relation : deliveryRequest.getDeliveryRequestOrderRelations())
		{
			Order order = this.tradingConsole.getOrder(relation.getOpenOrderId());
			order.addDeliveryLockLot(relation.getDeliveryLot());
			Inventory inventory = InventoryManager.instance.getInventory(order.get_Account(), order.get_Instrument());
			if(inventory != null)
			{
				inventory.caculate();
				order.recaculateValueAsMargin();
				InventoryManager.instance.update(order);

				this.tradingConsole.calculatePLFloat();
			}
		}
	}

	private void removeOrderLockedDeliveryLots(DeliveryRequest deliveryRequest)
	{
		boolean onlySubtractDeliveryLockLot = deliveryRequest.getStatus().equals(DeliveryStatus.Hedge);
		for(DeliveryRequestOrderRelation relation : deliveryRequest.getDeliveryRequestOrderRelations())
		{
			Order order = this.tradingConsole.getOrder(relation.getOpenOrderId());
			order.subtractDeliveryLockLot(relation.getDeliveryLot(), onlySubtractDeliveryLockLot);
			InventoryManager.instance.update(order);
		}
	}

	public DeliveryRequest getDeliveryRequest(Guid deliveryRequestId)
	{
		return (DeliveryRequest)this.pendingInventories.get(deliveryRequestId);
	}

	public void addOrUpdateScrapDeposit(XmlNode xmlNode)
	{
		Guid id = new Guid(xmlNode.get_Attributes().get_ItemOf("Id").get_Value());
		if(this.pendingInventories.containsKey(id))
		{
			ScrapDeposit scrapDeposit = (ScrapDeposit)this.pendingInventories.get(id);
			scrapDeposit.initialize(xmlNode, tradingConsole.get_SettingsManager());
			tradingConsole.bindingManager.update(PendingInventory.bindingKey, scrapDeposit);
		}
		else
		{
			ScrapDeposit scrapDeposit = new ScrapDeposit();
			scrapDeposit.initialize(xmlNode, tradingConsole.get_SettingsManager());
			this.pendingInventories.put(scrapDeposit.getId(), scrapDeposit);
			Guid accountId = scrapDeposit.getAccount().get_Id();
			if (!this.account2PendingInventories.containsKey(accountId))
			{
				this.account2PendingInventories.put(accountId, new ArrayList<PendingInventory> ());
			}
			this.account2PendingInventories.get(accountId).add(scrapDeposit);

			tradingConsole.bindingManager.add(PendingInventory.bindingKey, scrapDeposit);
		}
	}

	public boolean hasInventoryOf(Instrument instrument)
	{
		for(PendingInventory pendingVentory : this.pendingInventories.values())
		{
			if(pendingVentory.get_InstrumentId().equals(instrument.get_Id())) return true;
		}
		return false;
	}


	public ArrayList<DeliveryRequest> getAvaiableDeliveryRequests(Account account)
	{
		ArrayList<DeliveryRequest> deliveryRequests = null;
		for(PendingInventory inventory : this.pendingInventories.values())
		{
			if(inventory instanceof DeliveryRequest)
			{
				DeliveryRequest deliveryRequest = (DeliveryRequest)inventory;
				if(deliveryRequest.getAccount() == account
				   && (deliveryRequest.isStatus(DeliveryStatus.Approved) || deliveryRequest.isStatus(DeliveryStatus.ReadyForDelivery)))
				{
					if(deliveryRequests == null) deliveryRequests = new ArrayList<DeliveryRequest>();
					deliveryRequests.add(deliveryRequest);
				}
			}
		}
		return deliveryRequests;
	}
}
