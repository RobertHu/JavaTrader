package tradingConsole.physical;

import framework.Guid;
import java.math.BigDecimal;
import framework.data.DataRow;
import framework.DateTime;
import tradingConsole.settings.SettingsManager;
import framework.DBNull;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;

public class DeliveryRequestOrderRelation
{
	private Guid deliveryRequestId;
	private Guid openOrderId;
	private BigDecimal deliveryQuantity;
	private BigDecimal deliveryLot;

	public Guid getDeliveryRequestId()
	{
		return this.deliveryRequestId;
	}

	public Guid getOpenOrderId()
	{
		return this.openOrderId;
	}

	public BigDecimal getDeliveryQuantity()
	{
		return this.deliveryQuantity;
	}

	public BigDecimal getDeliveryLot()
	{
		return this.deliveryLot;
	}

	public void initialize(DataRow dataRow)
	{
		this.deliveryRequestId = (Guid)dataRow.get_Item("DeliveryRequestId");
		this.openOrderId = (Guid)dataRow.get_Item("OpenOrderId");
		this.deliveryQuantity = (BigDecimal)dataRow.get_Item("DeliveryQuantity");
		this.deliveryLot = (BigDecimal)dataRow.get_Item("DeliveryLot");
	}

	public void initialize(DeliveryRequest request, XmlNode node)
	{
		XmlAttributeCollection attributes = node.get_Attributes();
		this.deliveryRequestId = request.id;
		this.openOrderId = new Guid(attributes.get_ItemOf("OpenOrderId").get_Value());
		this.deliveryQuantity = new BigDecimal(attributes.get_ItemOf("DeliveryQuantity").get_Value());
		this.deliveryLot = new BigDecimal(attributes.get_ItemOf("DeliveryLot").get_Value());
	}
}
