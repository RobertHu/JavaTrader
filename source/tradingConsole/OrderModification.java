package tradingConsole;

import framework.Guid;
import framework.data.DataRow;
import framework.DBNull;

public class OrderModification
{
	private Guid id;
	private Guid transactionId;
	private Guid orderId;
	private Guid instrumentId;

	private String newLot;
	private String newSetPrice;
	private String newSetPrice2;

	public OrderModification(Order modifiedOrder, String newLot, String newSetPrice, String newSetPrice2)
	{
		this(Guid.newGuid(), modifiedOrder.get_Transaction().get_Id(), modifiedOrder.get_Id(), modifiedOrder.get_Transaction().get_Instrument().get_Id(),
			 newLot, newSetPrice, newSetPrice2);
	}

	public OrderModification(Guid id, Guid transactionId, Guid orderId, Guid instrumentId, String newLot, String newSetPrice, String newSetPrice2)
	{
		this.id = id;
		this.transactionId = transactionId;
		this.orderId = orderId;
		this.instrumentId = instrumentId;
		this.newLot = newLot;
		this.newSetPrice = newSetPrice;
		this.newSetPrice2 = newSetPrice2;
	}

	public Guid get_Id()
	{
		return this.id;
	}

	public Guid get_TransactionId()
	{
		return this.transactionId;
	}

	public Guid get_OrderId()
	{
		return this.orderId;
	}

	public Guid get_InstrumentId()
	{
		return this.instrumentId;
	}

	public String get_NewLot()
	{
		return this.newLot;
	}

	public String get_NewSetPrice()
	{
		return this.newSetPrice;
	}

	public String get_NewSetPrice2()
	{
		return this.newSetPrice2;
	}

	public static OrderModification from(DataRow dataRow)
	{
		Guid id = (Guid)dataRow.get_Item("Id");
		Guid transactionId = (Guid)dataRow.get_Item("TransactionId");
		Guid orderId = (Guid)dataRow.get_Item("OrderId");
		Guid instrumentId = (Guid)dataRow.get_Item("InstrumentId");
		String newLot = dataRow.get_Item("NewLot") == DBNull.value ? null : dataRow.get_Item("NewLot").toString();
		String newSetPrice = dataRow.get_Item("NewSetPrice") == DBNull.value ? null : (String)dataRow.get_Item("NewSetPrice");
		String newSetPrice2 = dataRow.get_Item("NewSetPrice2") == DBNull.value ? null : (String)dataRow.get_Item("NewSetPrice2");

		return new OrderModification(id, transactionId, orderId, instrumentId, newLot, newSetPrice, newSetPrice2);
	}
}
