package tradingConsole;

import framework.Guid;
import framework.data.DataRow;

public class TradePolicy
{
	private Guid _id;
	private double _alertLevel1;
	private double _alertLevel2;
	private double _alertLevel3;

	public TradePolicy(DataRow dataRow)
	{
		this._id = (Guid)(dataRow.get_Item("ID"));
		this.setValue(dataRow);
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public double get_AlertLevel3()
	{
		return this._alertLevel3;
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		//this._alertLevel1 = AppToolkit.convertDBValueToDouble(dataRow.get_Item("AlertLevel1"),0.0);
		//this._alertLevel2 = AppToolkit.convertDBValueToDouble(dataRow.get_Item("AlertLevel2"),0.0);
		this._alertLevel3 = AppToolkit.convertDBValueToDouble(dataRow.get_Item("AlertLevel3"),0.0);
	}

}
