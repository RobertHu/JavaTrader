package tradingConsole.ui;

import javax.swing.*;
import tradingConsole.Instrument;
import tradingConsole.Price;
import java.awt.Component;
import java.awt.Font;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.Dimension;

interface IPriceSpinnerSite
{
	Instrument getInstrument();
}

public class PriceSpinner extends JSpinner
{
	private JFormattedTextField _innerEditor;
	private SpinnerPriceModel _spinnerPriceModel;

	public PriceSpinner(IPriceSpinnerSite priceSpinnerSite)
	{
		this._spinnerPriceModel = new SpinnerPriceModel(priceSpinnerSite);
		super.setModel(this._spinnerPriceModel);
		this._innerEditor = ((DefaultEditor)this.getEditor()).getTextField();
		this._innerEditor.setPreferredSize(new Dimension(40, 18));
	}

	@Override
	public void setEnabled(boolean value)
	{
		super.setEnabled(value);
		if(this._innerEditor != null)
		{
			this._innerEditor.setEnabled(value);
			this._innerEditor.setEditable(value);
		}
	}

	public void setEditable(boolean value)
	{
		if(this._innerEditor != null)
		{
			this._innerEditor.setEditable(value);
		}
	}

	@Override
	public void setFont(Font value)
	{
		super.setFont(value);
		if(this._innerEditor != null) this._innerEditor.setFont(value);
	}

	@Override
	public void setForeground(Color value)
	{
		super.setForeground(value);
		if(this._innerEditor != null) this._innerEditor.setForeground(value);
	}

	public Document getDocument()
	{
		return this._innerEditor != null ? this._innerEditor.getDocument() : null;
	}

	public void setText(String value)
	{
		this._spinnerPriceModel.setValue(value);
	}

	public void select(int index, int length)
	{
		if(this._innerEditor != null) this._innerEditor.select(index, length);
	}

	public String getText()
	{
		return (String)this._spinnerPriceModel.getValue();
	}

	public void addActionListener(ActionListener listener)
	{
		this._innerEditor.addActionListener(listener);
	}

	/*@Override
	protected JComponent createEditor(SpinnerModel model)
	{
		if (model instanceof SpinnerPriceModel)
		{
			return this._innerEditor;
		}
		else
		{
			return super.createEditor(model);
		}
	}*/

	private static class SpinnerPriceModel extends AbstractSpinnerModel
	{
		private String _value;
		private IPriceSpinnerSite _priceSpinnerSite;

		public SpinnerPriceModel(IPriceSpinnerSite priceSpinnerSite)
		{
			this._priceSpinnerSite = priceSpinnerSite;
		}

		public Object getValue()
		{
			return this._value;
		}

		public void setValue(Object value)
		{
			this._value = (String)value;
			fireStateChanged();
		}

		public Object getNextValue()
		{
			Price price = Price.parse(this._value, this._priceSpinnerSite.getInstrument().get_NumeratorUnit(), this._priceSpinnerSite.getInstrument().get_Denominator());
			Price nextPrice = Price.add(price, this._priceSpinnerSite.getInstrument().get_NumeratorUnit());
			if (this.isValidPirce(nextPrice))
			{
				return Price.toString(nextPrice);
			}
			else
			{
				return this._value;
			}
		}

		public Object getPreviousValue()
		{
			Price price = Price.parse(this._value, this._priceSpinnerSite.getInstrument().get_NumeratorUnit(), this._priceSpinnerSite.getInstrument().get_Denominator());
			Price previousPrice = Price.subStract(price, this._priceSpinnerSite.getInstrument().get_NumeratorUnit());
			if (this.isValidPirce(previousPrice))
			{
				return Price.toString(previousPrice);
			}
			else
			{
				return this._value;
			}
		}

		private boolean isValidPirce(Price setPrice)
		{
			return true;
		}
	}
}
