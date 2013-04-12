package tradingConsole.ui;

import javax.swing.AbstractSpinnerModel;
import tradingConsole.Instrument;
import tradingConsole.Price;
import tradingConsole.ui.language.Language;
import javax.swing.JSpinner;
import java.awt.event.KeyAdapter;
import javax.swing.JFormattedTextField;
import java.awt.event.KeyEvent;

interface IBuySellProvider
{
	boolean isBuy();
}

public class PriceSpinnerModelForMatching extends AbstractSpinnerModel
{
	private String _value;
	private Instrument _instrument;
	private IBuySellProvider _buySellProvider;

	public PriceSpinnerModelForMatching(Instrument instrument, IBuySellProvider buySellProvider)
	{
		this._instrument = instrument;
		this._buySellProvider = buySellProvider;
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
		Price price = Price.parse(this._value,  this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price nextPrice = Price.add(price, this._instrument.get_NumeratorUnit());
		if(this.isValidPirce(nextPrice))
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
		Price price = Price.parse(this._value,  this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price previousPrice = Price.subStract(price, this._instrument.get_NumeratorUnit());
		if(this.isValidPirce(previousPrice))
		{
			return Price.toString(previousPrice);
		}
		else
		{
			return this._value;
		}
	}

	private double _previousDiff = Double.NaN;
	private boolean isValidPirce(Price setPrice)
	{
		Price marketPrice = this._instrument.get_LastQuotation().getBuySell(this._buySellProvider.isBuy());
		double dblMarketPrice = Price.toDouble(marketPrice);
		double diff = Math.abs(Price.toDouble(setPrice) - dblMarketPrice);
		if ((!Double.isNaN(this._previousDiff) &&  diff < this._previousDiff) || diff <= dblMarketPrice * 0.2)
		{
			this._previousDiff = diff;
			return true;
		}
		else
		{
			this._previousDiff = diff;
			return false;
		}
	}
}
