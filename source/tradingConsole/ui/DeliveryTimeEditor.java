package tradingConsole.ui;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.JSpinner.*;

import com.jidesoft.spinner.*;
import framework.DateTime;
import tradingConsole.*;
import tradingConsole.physical.*;

public class DeliveryTimeEditor extends DateSpinner
{
	public DeliveryTimeEditor()
	{
		super("yyyy-MM-dd");
	}

	public void initialize(Instrument instrument)
	{
		ExpireTimeSpinnerModel model = new ExpireTimeSpinnerModel(instrument);
		this.setModel(model);

		JFormattedTextField textField = ((DateEditor)this.getEditor()).getTextField();
		textField.setValue(model._value);
		textField.addFocusListener(new FocusListener()
			{
			public void focusGained(FocusEvent e)
			{
			}

			public void focusLost(FocusEvent e)
			{
				ExpireTimeSpinnerModel model = (ExpireTimeSpinnerModel)getModel();
				JFormattedTextField textField = ( (DateEditor)getEditor()).getTextField();
				try
				{
					DateTime newTime = DateTime.fromDate( (Date)getValue());
					if (newTime.compareTo(model.getEndTime()) > 0)
					{
						textField.setValue(model.getEndTime());
					}
					else if(newTime.compareTo(model.getBeginTime()) < 0)
					{
						textField.setValue(model.getBeginTime());
					}
					else if(DeliveryHoliday.instance.isHoliday(newTime))
					{
						textField.setValue(model.getNextAvaliableTime(newTime));
					}
				}
				catch (IllegalArgumentException exception) //the input maybe out of datetime range
				{
					textField.setValue(model.getEndTime());
				}
			}
		});
	}

	private static class ExpireTimeSpinnerModel extends SpinnerDateModel
	{
		private Date _value;
		private Instrument _instrument;

		ExpireTimeSpinnerModel(Instrument instrument)
		{
			this._instrument = instrument;
		}

		public Object getValue()
		{
			if(this._value == null)
			{
				this._value = this.getBeginTime().toDate();
			}
			return this._value;
		}

		public void setValue(Object value)
		{
			if ((value == null) || !(value instanceof Date))
			{
				throw new IllegalArgumentException("null value");
			}

			if (!value.equals(this._value))
			{
				this._value = ((Date)value);
				fireStateChanged();
			}
		}

		public Object getNextValue()
		{
			DateTime end = this.getEndTime();
			DateTime begin = this.getBeginTime();
			DateTime nextValue = DateTime.fromDate(this._value).addDays(1);
			while(DeliveryHoliday.instance.isHoliday(nextValue))
			{
				nextValue = nextValue.addDays(1);
			}
			if(nextValue.after(end))
			{
				nextValue = end;
			}
			return nextValue.toDate();
		}

		public Object getPreviousValue()
		{
			DateTime being = this.getBeginTime();
			DateTime previousValue = DateTime.fromDate(this._value).addDays(-1);

			while(DeliveryHoliday.instance.isHoliday(previousValue))
			{
				previousValue = previousValue.addDays(-1);
			}

			if(previousValue.before(being))
			{
				previousValue = being;
			}

			return previousValue.toDate();
		}

		private DateTime getBeginTime()
		{
			DateTime today = DateTime.get_Now().get_Date();
			DateTime beginTime = today;
			int totalDay = this._instrument.get_DeliveryTimeBeginDay();
			if(totalDay == 0)
			{
				beginTime = this.getWorkingDate(beginTime);
			}
			else
			{
				while (totalDay > 0)
				{
					beginTime = this.getWorkingDate(beginTime.addDays(1));
					totalDay--;
				}
			}
			return beginTime;
		}

	    private static DateTime dayBeforeMaxDate = DateTime.maxValue.addDays(-1);
		private DateTime getWorkingDate(DateTime time)
		{
			while (DeliveryHoliday.instance.isHoliday(time) && time.compareTo(dayBeforeMaxDate) < 0)
			{
				time = time.addDays(1);
			}
			return time;
		}

		private DateTime getEndTime()
		{
			if( this._instrument.get_DeliveryTimeEndDay() == 0) return DateTime.maxValue.get_Date();

			DateTime endTime = this.getBeginTime();
			int totalDay = this._instrument.get_DeliveryTimeEndDay() - 1;
			while(totalDay > 0 )
			{
				endTime = this.getWorkingDate(endTime.addDays(1));
				totalDay--;
			}
			return endTime;
		}

		private Object getNextAvaliableTime(DateTime newTime)
		{
			while(DeliveryHoliday.instance.isHoliday(newTime))
			{
				newTime = newTime.addDays(1);
			}
			return newTime;
		}
	}
}
