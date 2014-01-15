package tradingConsole.ui;

import javax.swing.JFormattedTextField;
import java.awt.event.KeyAdapter;
import framework.StringHelper;
import java.awt.event.KeyEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Summary description for JSpinnerHelper.
 */
public class JDQMoveSpinnerHelper
{
	public static void applyMaxDQMove(JSpinner spinner, int value, int maxDQMove, int stepSize)
	{
		SpinnerNumberModel model = new SpinnerNumberModel(value, 0, maxDQMove, stepSize);
		spinner.setModel(model);
		((JSpinner.NumberEditor)spinner.getEditor()).getTextField().addKeyListener(new DQMoveKeyAdapter(maxDQMove));
	}

	public static void applyMaxDQMove(JSpinner spinner, int maxDQMove, int stepSize)
	{
		JDQMoveSpinnerHelper.applyMaxDQMove(spinner, 0, maxDQMove, stepSize);
	}

	private static class DQMoveKeyAdapter extends KeyAdapter
	{
		private int _maxValue = 10;
		private int _minValue = 0;
		private int _maxLength = 0;
		private int _minlength = 0;

		private String _oldText = "0";

		private DQMoveKeyAdapter(int maxValue)
		{
			this._maxValue = maxValue;
			this._maxLength = ((Integer)maxValue).toString().length();
		}

		public void keyPressed(KeyEvent e)
		{
			JFormattedTextField editor = (JFormattedTextField)e.getSource();
			boolean isValid = this.isValid(editor.getText());
			if(isValid)
			{
				this._oldText = editor.getText();
			}
		}

		public void keyReleased(KeyEvent e)
		{
			JFormattedTextField editor = (JFormattedTextField)e.getSource();
			boolean isValid = this.isValid(editor.getText());
			if(!isValid)
			{
				editor.setText(this._oldText);
			}
			if(editor.getText().length() == 0) editor.setText("0");
		}

		private boolean isValid(String value)
		{
			if(value == null || value.length() > this._maxLength || value.length() < this._minlength) return false;

			boolean isValid = true;
			for(char c : value.toCharArray())
			{
				if(!Character.isDigit(c))
				{
					isValid = false;
					break;
				}
			}

			if(isValid)
			{
				int number = 0;
				if(value.length() > 0) number = Integer.parseInt(value);
				if (number > this._maxValue || number < this._minValue)
				{
					isValid = false;
				}
			}

			return isValid;
		}
	}
}
