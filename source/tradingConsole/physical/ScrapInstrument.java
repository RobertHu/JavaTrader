package tradingConsole.physical;

import framework.data.DataRow;
import framework.Guid;
import framework.DBNull;

public class ScrapInstrument
{
	private Guid _id;
	private String _description;
	private String _unit;
	private int _decimals;

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Description()
	{
		return this._description;
	}

	public String get_Unit()
	{
		return this._unit;
	}

	public int get_Decimals()
	{
		return this._decimals;
	}

	public ScrapInstrument(DataRow dataRow)
	{
		this._id = (Guid)dataRow.get_Item("Id");
		this._description = (String)dataRow.get_Item("Description");
		this._unit = dataRow.get_Item("Unit") == DBNull.value ? "" : (String)dataRow.get_Item("Unit");
		this._decimals = (Integer)dataRow.get_Item("QuantityDecimalDigits");
	}
}
