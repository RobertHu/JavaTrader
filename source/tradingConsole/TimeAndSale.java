package tradingConsole;

import javax.swing.*;

import framework.*;
import framework.data.*;
import tradingConsole.framework.*;
import tradingConsole.ui.language.*;

public class TimeAndSale
{
	private Guid _instrumentId;
	private Guid[] _accountIds;
	private DateTime _timestamp;
	private String _price;
	private double _quantity;

	public TimeAndSale(Guid instrumentId, Guid[] accountIds, DateTime timestamp, String price, double quantity)
	{
		this._instrumentId = instrumentId;
		this._accountIds = accountIds;
		this._timestamp = timestamp;
		this._price = price;
		this._quantity = quantity;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public Guid[] get_AccountIds()
	{
		return this._accountIds;
	}

	public DateTime get_Timestamp()
	{
		return this._timestamp;
	}

	public String get_Time()
	{
		return this._timestamp.toString("HH:mm:ss");
	}

	public String get_Price()
	{
		return this._price;
	}

	public double get_Quantity()
	{
		return this._quantity;
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];

		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(TimeAndSale.class, "Time", true, null, TimeAndSaleLanguage.Time, 120, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = PropertyDescriptor.create(TimeAndSale.class, "Price", true, null, TimeAndSaleLanguage.Price, 150, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = PropertyDescriptor.create(TimeAndSale.class, "Quantity", true, null, TimeAndSaleLanguage.Volume, 60, SwingConstants.CENTER, null, null);

		return propertyDescriptors;
	}
}
