package tradingConsole;

import javax.swing.*;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.framework.*;
import tradingConsole.ui.language.*;
import java.math.BigDecimal;

public class BestLimit
{
	private Guid _instrumentId;
	private byte _sequence;
	private boolean _isBuy;
	private double _price;
	private double _quantity;
	private int _numberOfOrder;
	private DateTime _timestamp;

	private BestLimit()
	{
	}

	public BestLimit(Guid instrumentId, byte sequence, boolean isBuy, double price, double quantity)
	{
		this._instrumentId = instrumentId;
		this._sequence = sequence;
		this._isBuy = isBuy;
		this._price = price;
		this._quantity = quantity;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public byte get_Sequence()
	{
		return this._sequence;
	}

	public boolean get_IsBuy()
	{
		return this._isBuy;
	}

	public double get_Price()
	{
		return this._price;
	}

	public double get_Quantity()
	{
		return this._quantity;
	}

	public int get_NumberOfOrder()
	{
		return this._numberOfOrder;
	}

	public DateTime get_Timestamp()
	{
		return this._timestamp;
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
	}

	public void updateWith(BestLimit bestLimit)
	{
		this._price = bestLimit._price;
		this._quantity = bestLimit._quantity;
		this._numberOfOrder = bestLimit._numberOfOrder;
		this._timestamp = bestLimit._timestamp;
	}

	public BestLimit Clone()
	{
		BestLimit bestLimit = new BestLimit();

		bestLimit._instrumentId = this._instrumentId;
		bestLimit._sequence = this._sequence;
		bestLimit._isBuy = this._isBuy;
		bestLimit._price = this._price;
		bestLimit._quantity = this._quantity;
		bestLimit._numberOfOrder = this._numberOfOrder;
		bestLimit._timestamp = this._timestamp;

		return bestLimit;
	}

	public static BestLimit create(Guid instrumentId, XmlNode bestLimitXmlNode)
	{
		XmlAttributeCollection attributes = bestLimitXmlNode.get_Attributes();

		BestLimit bestLimit = new BestLimit();
		bestLimit._instrumentId = instrumentId;
		bestLimit._sequence = XmlConvert.toByte(attributes.get_ItemOf("Sequence").get_Value());
		bestLimit._isBuy = XmlConvert.toBoolean(attributes.get_ItemOf("IsBuy").get_Value());
		bestLimit._price = XmlConvert.toDouble(attributes.get_ItemOf("Price").get_Value());
		bestLimit._quantity = XmlConvert.toDouble(attributes.get_ItemOf("Quantity").get_Value());
		bestLimit._numberOfOrder = XmlConvert.toInt32(attributes.get_ItemOf("NumberOfOrder").get_Value());
		bestLimit._timestamp = XmlConvert.toDateTime(attributes.get_ItemOf("Timestamp").get_Value());

		return bestLimit;
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];

		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(BestLimit.class, "Sequence", true, null, BestLimitLanguage.Sequence, 50, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = PropertyDescriptor.create(BestLimit.class, "Price", true, null, BestLimitLanguage.Price, 100, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = PropertyDescriptor.create(BestLimit.class, "Quantity", true, null, BestLimitLanguage.Volume, 100, SwingConstants.CENTER, null, null);

		return propertyDescriptors;
	}

	public static BestLimit create(DataRow dataRow)
	{
		BestLimit bestLimit = new BestLimit();
		bestLimit._instrumentId = (Guid)dataRow.get_Item("InstrumentId");
		bestLimit._sequence = ((Short)dataRow.get_Item("Sequence")).byteValue();
		bestLimit._isBuy = (Boolean)dataRow.get_Item("IsBuy");
		bestLimit._price = (Double)dataRow.get_Item("Price");
		bestLimit._quantity = (Double)dataRow.get_Item("Quantity");
		bestLimit._timestamp = (DateTime)dataRow.get_Item("Timestamp");
		return bestLimit;
	}
}
