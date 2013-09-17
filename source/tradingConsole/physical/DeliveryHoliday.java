package tradingConsole.physical;

import java.util.ArrayList;
import framework.DateTime;
import framework.data.DataTable;
import framework.data.DataRow;
import framework.data.DataRowCollection;
import java.util.Calendar;

public class DeliveryHoliday
{
	private static class Holiday
	{
		private DateTime _beginDay;
		private DateTime _endDay;

		Holiday(DataRow row)
		{
			this._beginDay = ((DateTime)row.get_Item("BeginDate")).get_Date();
			this._endDay = ((DateTime)row.get_Item("EndDate")).get_Date();
		}

		boolean contains(DateTime dateTime)
		{
			return !dateTime.before(this._beginDay) && !dateTime.after(this._endDay);
		}
	}

	public static DeliveryHoliday instance = new DeliveryHoliday();
	private ArrayList<Holiday> _holidays;

	private DeliveryHoliday(){}

	public void initailize(DataTable deliveryHolidays)
	{
		DataRowCollection rows = deliveryHolidays.get_Rows();
		this._holidays = new ArrayList<Holiday>(rows.get_Count());
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);
			this._holidays.add(new Holiday(row));
		}
	}

	public boolean isHoliday(DateTime dateTime)
	{
		dateTime = dateTime.get_Date();
		if(this.isWeekend(dateTime))
		{
			return true;
		}
		else
		{
			for(Holiday item : this._holidays)
			{
				if(item.contains(dateTime)) return true;
			}
		}
		return false;
	}

	private boolean isWeekend(DateTime dateTime)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateTime);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY;
	}
}
