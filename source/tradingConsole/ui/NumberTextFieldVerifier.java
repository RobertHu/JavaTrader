package tradingConsole.ui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import java.text.ParseException;

/**
 * Summary description for NumberTextFieldVerifier.
 */
public class NumberTextFieldVerifier extends InputVerifier
{
	private int _minValue;
	private int _maxValue;

	public NumberTextFieldVerifier(int minValue, int maxValue)
	{
		this._minValue = minValue;
		this._maxValue = maxValue;
	}

	public int get_MaxValue()
	{
		return this._maxValue;
	}

	public int get_MinValue()
	{
		return this._minValue;
	}

	public void set_MaxValue(int maxValue)
	{
		this._maxValue = maxValue;
	}

	public void set_MinValue(int minValue)
	{
		this._minValue = minValue;
	}

	@Override
	public boolean verify(JComponent input)
	{
		if (input instanceof JFormattedTextField)
		{
			JFormattedTextField formattedTextField = (JFormattedTextField)input;
			AbstractFormatter formatter = formattedTextField.getFormatter();
			if (formatter != null)
			{
				String text = formattedTextField.getText();
				try
				{
					int value = ((Long)formatter.stringToValue(text)).intValue();
					return value >= this._minValue && value <= this._maxValue;
				}
				catch (ParseException pe)
				{
					return false;
				}
			}
		}
		return true;
	}
}
