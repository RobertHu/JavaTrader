package tradingConsole;

import tradingConsole.ui.language.BestLimitLanguage;
import javax.swing.SwingConstants;
import tradingConsole.framework.PropertyDescriptor;
import framework.data.DataRow;
import framework.DateTime;
import framework.Guid;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;
import framework.xml.XmlConvert;

public class BestPending
{
	private Guid _instrumentId;
	private String _price;
	private String _quantity;
	private int _sequence;

	private BestPending()
	{
	}

	public BestPending(Guid instrumentId, String price, String quantity, int sequence)
	{
		this._instrumentId = instrumentId;
		this._price = price;
		this._quantity = quantity;
		this._sequence = sequence;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public String get_Price()
	{
		return this._price;
	}

	public String get_Quantity()
	{
		return this._quantity;
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];

		int index = 0;
		propertyDescriptors[index++] = PropertyDescriptor.create(BestPending.class, "Price", true, null, BestLimitLanguage.Price, 140, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = PropertyDescriptor.create(BestPending.class, "Quantity", true, null, BestLimitLanguage.Volume, 80, SwingConstants.CENTER, null, null);

		return propertyDescriptors;
	}

	public static BestPending create(DataRow dataRow)
	{
		BestPending bestPending = new BestPending();
		bestPending._instrumentId = (Guid)dataRow.get_Item("InstrumentId");
		bestPending._price = (String)dataRow.get_Item("Price");
		bestPending._quantity = dataRow.get_Item("Quantity").toString();
		bestPending._sequence = ((Long)dataRow.get_Item("Sequence")).intValue();
		return bestPending;
	}
}
