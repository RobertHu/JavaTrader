package tradingConsole.ui;

import java.awt.*;
import javax.swing.*;

public class PVStaticText2 extends JLabel
{
	private Object _value;

	public PVStaticText2()
	{
		super();
	}

	public void setAlignment(int alignment)
	{
		switch(alignment)
		{
			case 0:
				this.setHorizontalAlignment(SwingConstants.LEFT);
				break;
			case 1:
				this.setHorizontalAlignment(SwingConstants.CENTER);
				break;
			case 2:
				this.setHorizontalAlignment(SwingConstants.RIGHT);
				break;
			default:
				throw new IllegalArgumentException("alignment = " + alignment);
		}
	}

	public void setValue(Object value)
	{
		this._value = value;
	}

	public Object getValue()
	{
		return this._value;
	}

	public void setBorderColor(Color color)
	{
		this.setBorder(BorderFactory.createLineBorder(color));
	}

	public void setOdometer(int i)
	{
	}

	public void setAutoResize(boolean b)
	{
	}

	public static PVStaticText2 createNotNullFlagFor(PVStaticText2 notNullStaticText)
	{
		Rectangle bounds = notNullStaticText.getBounds();
		notNullStaticText.setBounds(new Rectangle(bounds.getLocation().x + 8, bounds.getLocation().y, bounds.width - 8, bounds.height));
		PVStaticText2 flagStaticText = new PVStaticText2();
		flagStaticText.setForeground(Color.RED);
		flagStaticText.setText("*");
		flagStaticText.setVerticalAlignment(SwingConstants.TOP);
		flagStaticText.setBounds(new Rectangle(bounds.getLocation().x, bounds.getLocation().y, 8, bounds.height));
		return flagStaticText;
	}
}
