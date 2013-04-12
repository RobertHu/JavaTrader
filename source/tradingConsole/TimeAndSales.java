package tradingConsole;

import java.util.*;

import framework.*;
import tradingConsole.ui.grid.*;
import javax.swing.SwingConstants;

public class TimeAndSales
{
	private Guid _instrumentId;
	private HashMap<Guid, BindingSource> _timeAndSales = new HashMap<Guid, BindingSource>();

	public TimeAndSales(Guid instrumentId)
	{
		this._instrumentId = instrumentId;
	}

	public BindingSource get_TimeAndSales(Guid accountId)
	{
		BindingSource timeAndSales = this._timeAndSales.get(accountId);
		if (timeAndSales == null)
		{
			timeAndSales = new BindingSource();
			timeAndSales.setHorizontalAlignment(SwingConstants.CENTER);
			String dataSourceKey = this._instrumentId.toString() + accountId.toString() + "TimeAndSales";
			TradingConsole.bindingManager.bind(dataSourceKey, new Vector(), timeAndSales, TimeAndSale.getPropertyDescriptors());

			this._timeAndSales.put(accountId, timeAndSales);
		}
		return timeAndSales;
	}

	public void add(TimeAndSale timeAndSale)
	{
		for(Guid accountId : timeAndSale.get_AccountIds())
		{
			BindingSource timeAndSales = this.get_TimeAndSales(accountId);

			boolean added = false;
			for (int index = 0; index < timeAndSales.getRowCount(); index++)
			{
				TimeAndSale timeAndSale2 = (TimeAndSale)timeAndSales.getObject(index);
				if (!timeAndSale.get_Timestamp().before(timeAndSale2.get_Timestamp()))
				{
					timeAndSales.insert(index, timeAndSale);
					added = true;
					break;
				}
			}
			if(!added) timeAndSales.add(timeAndSale);
		}
	}

	public void clear()
	{
		for(BindingSource timeAndSales : this._timeAndSales.values())
		{
			timeAndSales.removeAll();
		}
	}
}
