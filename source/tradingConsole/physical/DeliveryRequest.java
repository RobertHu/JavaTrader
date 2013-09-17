package tradingConsole.physical;

import java.math.*;
import java.util.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.lang.Enum;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.enumDefine.physical.*;
import tradingConsole.settings.*;

public class DeliveryRequest extends PendingInventory
{
	private Instrument instrument;
	private DateTime availableTime;
	private DateTime deliveryTime;
	private DeliveryStatus deliveryStatus;

	private ArrayList<DeliveryRequestOrderRelation> deliveryRequestOrderRelations = new ArrayList<DeliveryRequestOrderRelation>();

	public Collection<DeliveryRequestOrderRelation> getDeliveryRequestOrderRelations()
	{
		return this.deliveryRequestOrderRelations;
	}

	@Override
	public String get_Instrument()
	{
		return this.instrument.get_Description();
	}

	@Override
	public String get_Status()
	{
		return this.deliveryStatus.toLocalString();
	}

	@Override
	public String get_SettlementTime()
	{
		DateTime settlementTime = this.availableTime == null ? this.deliveryTime : this.availableTime;
		return settlementTime == null ? "-" : settlementTime.toString(Inventory.DateTimeFormat);
	}

	public int get_Decimals()
	{
		return this.instrument.get_PhysicalLotDecimal();
	}

	@Override
	public void initialize(DataRow dataRow, SettingsManager settings)
	{
		super.initialize(dataRow, settings);
		this.instrumentId = (Guid)dataRow.get_Item("InstrumentId");
		this.weight = (BigDecimal)dataRow.get_Item("RequireQuantity");
		this.availableTime = dataRow.get_Item("AvailableTime") == DBNull.value ? null : (DateTime)dataRow.get_Item("AvailableTime");
		this.deliveryTime = dataRow.get_Item("DeliveryTime") == DBNull.value ? null : (DateTime)dataRow.get_Item("DeliveryTime");

		this.instrument = settings.getInstrument(this.instrumentId);
	}

	@Override
	public void initialize(XmlNode node, SettingsManager settings)
	{
		super.initialize(node, settings);

		XmlAttributeCollection attributes = node.get_Attributes();
		this.instrumentId = new Guid(attributes.get_ItemOf("InstrumentId").get_Value());
		this.weight = new BigDecimal(attributes.get_ItemOf("RequireQuantity").get_Value());
		if(attributes.get_ItemOf("AvailableTime") != null)
		{
			this.availableTime = DateTime.valueOf(attributes.get_ItemOf("AvailableTime").get_Value());
		}
		if(attributes.get_ItemOf("DeliveryTime") != null)
		{
			this.deliveryTime = DateTime.valueOf(attributes.get_ItemOf("DeliveryTime").get_Value());
		}

		for(int index = 0; index < node.get_ChildNodes().get_Count(); index++)
		{
			XmlNode childNode = node.get_ChildNodes().item(index);
			DeliveryRequestOrderRelation relation = new DeliveryRequestOrderRelation();
			relation.initialize(this, childNode);

			this.add(relation);
		}

		this.instrument = settings.getInstrument(this.instrumentId);
		this.unit = this.instrument.get_Unit();
	}

	public Instrument getInstrument()
	{
		return this.instrument;
	}

	public void add(DeliveryRequestOrderRelation deliveryRequestOrderRelation)
	{
		this.deliveryRequestOrderRelations.add(deliveryRequestOrderRelation);
	}

	public void setAvalibleDeliveryTime(DateTime avalibleDeliveryTime)
	{
		this.availableTime = avalibleDeliveryTime;
		TradingConsole.bindingManager.update(PendingInventory.bindingKey, this);
	}

	public void setDeliveryTime(DateTime deliveryTime)
	{
		this.deliveryTime = deliveryTime;
		TradingConsole.bindingManager.update(PendingInventory.bindingKey, this);
	}

	protected void initStatus()
	{
		this.deliveryStatus = Enum.valueOf(DeliveryStatus.class, this.status.intValue());
	}

	public void setStatus(DeliveryStatus deliveryStatus)
	{
		this.deliveryStatus = deliveryStatus;
	}

	public DeliveryStatus getStatus()
	{
		return this.deliveryStatus;
	}

	public void update()
	{
		TradingConsole.bindingManager.update(PendingInventory.bindingKey, this);
	}

	public boolean isStatus(DeliveryStatus deliveryStatus)
	{
		return this.deliveryStatus.equals(deliveryStatus);
	}
}
