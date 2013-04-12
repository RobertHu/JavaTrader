package tradingConsole.ui;

import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.JSpinner.*;

import com.jidesoft.spinner.*;
import framework.*;
import framework.DateTime;
import tradingConsole.*;
import tradingConsole.settings.*;

public class ExpireTimeEditor extends DateSpinner
{
	public static int ShowTradeDayMode = 0;
	public static int ShowDateMode = 1;

	private int _mode;

	public ExpireTimeEditor()
	{
		this(ExpireTimeEditor.ShowTradeDayMode);
	}

	public ExpireTimeEditor(int mode)
	{
		super("yyyy-MM-dd");
		this._mode = mode;
	}

	public void initialize(SettingsManager settingsManager, Instrument instrument)
	{
		ExpireTimeSpinnerModel model = new ExpireTimeSpinnerModel(settingsManager, instrument, this._mode);
		this.setModel(model);
		JFormattedTextField textField = ((DateEditor)this.getEditor()).getTextField();
		textField.setValue(model._value);
		textField.addPropertyChangeListener(new ExpireTimePropertyChangeListener(this));
	}

	public DateTime getExpireTime()
	{
		SpinnerModel model = this.getModel();
		if(this._mode == ExpireTimeEditor.ShowTradeDayMode && model instanceof ExpireTimeSpinnerModel)
		{
			ExpireTimeSpinnerModel model2 = (ExpireTimeSpinnerModel)model;
			return model2.getExpireTime();
		}
		else
		{
			return DateTime.fromDate((Date)model.getValue());
		}
	}

	private static class ExpireTimePropertyChangeListener implements PropertyChangeListener
	{
		private ExpireTimeEditor _owner;

		public ExpireTimePropertyChangeListener(ExpireTimeEditor expireTimeEditor)
		{
			this._owner = expireTimeEditor;
		}

		public void propertyChange(PropertyChangeEvent evt)
		{
			if(evt.getPropertyName().equals("value"))
			{
				ExpireTimeSpinnerModel model = (ExpireTimeSpinnerModel)this._owner.getModel();
				JFormattedTextField textField = ((DateEditor)this._owner.getEditor()).getTextField();
				try
				{
					DateTime newTime = DateTime.fromDate( (Date)evt.getNewValue());
					if (newTime.compareTo(model.getEndTime()) > 0
						|| newTime.compareTo(model.getBeginTime()) < 0)
					{
						textField.setValue(evt.getOldValue());
					}
				}
				catch(IllegalArgumentException exception)//the input maybe out of datetime range
				{
					textField.setValue(evt.getOldValue());
				}
			}
		}
	}

	private static class ExpireTimeSpinnerModel extends SpinnerDateModel
	{
		private SettingsManager _settingsManager;
		private Instrument _instrument;
		private Date _value;
		private int _mode;

		public ExpireTimeSpinnerModel(SettingsManager settingsManager, Instrument instrument, int mode)
		{
			this._settingsManager = settingsManager;
			this._instrument = instrument;
			this._mode = mode;
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
			if(this._mode == ExpireTimeEditor.ShowDateMode && this._value.compareTo(begin) == 0)
			{
				nextValue = nextValue.get_Date().addDays(1).add(end.get_TimeOfDay());
			}
			if(nextValue.compareTo(end) > 0)
			{
				nextValue = end;
			}
			return nextValue.toDate();
		}

		public Object getPreviousValue()
		{
			DateTime being = this.getBeginTime();
			DateTime previousValue = DateTime.fromDate(this._value).addDays(-1);

			if(previousValue.compareTo(being) < 0
			   || (this._mode == ExpireTimeEditor.ShowDateMode && previousValue.get_Date().substract(being.get_Date()).compareTo(TimeSpan.fromDays(1)) <= 0))
			{
				previousValue = being;
			}
			return previousValue.toDate();
		}

		private DateTime getBeginTime()
		{
			DateTime instrumentBegin = this._instrument.get_DayCloseTime();
			DateTime dayBegin = this._settingsManager.get_TradeDay().get_EndTime();
			DateTime begin = instrumentBegin == null ? dayBegin : instrumentBegin;
			return begin;
		}

		private DateTime getEndTime()
		{
			DateTime endTime = this._settingsManager.get_TradeDay().get_EndTime();
			if(this._mode == ExpireTimeEditor.ShowTradeDayMode)
			{
				return endTime.addMonths(1);
			}
			else
			{
				return endTime.addMonths(1).addDays(1);
			}
		}

		private DateTime getExpireTime()
		{
			DateTime being = this.getBeginTime();
			DateTime value = DateTime.fromDate((Date)this.getValue());
			TimeSpan span = value.get_Date().substract(being.get_Date());
			if(span.compareTo(TimeSpan.fromDays(1)) < 0)
			{
				return this.getBeginTime();
			}
			else
			{
				DateTime end = this.getEndTime();
				return value.get_Date().add(end.get_TimeOfDay());
			}
		}
	}
}
