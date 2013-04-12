package tradingConsole.ui.colorHelper;

import java.awt.Color;
import java.math.BigDecimal;
import tradingConsole.ui.ColorSettings;
import tradingConsole.settings.PublicParametersManager;

public class NumericColor
{
	public static Color positive = PublicParametersManager.useGreenAsRiseColor ? new Color(0,136,8) :  new Color(108, 108, 255) ;
	public static Color negative = Color.red;//-
	public static Color zero = Color.BLACK;

	public NumericColor()
	{
	}

	public static Color getColor(double number, boolean useColorSettings)
	{
		if(useColorSettings && ColorSettings.useBlackAsBackground)
		{
			if (number > 0)
			{
				return ColorSettings.Positive;
			}
			else if (number < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return ColorSettings.GridForeground;
			}
		}
		else
		{
			if (number > 0)
			{
				return NumericColor.positive;
			}
			else if (number < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return NumericColor.zero;
			}
		}
	}

	public static Color getColor(BigDecimal number, boolean useColorSettings)
	{
		if(useColorSettings && ColorSettings.useBlackAsBackground)
		{
			if (number == null)
				return ColorSettings.GridForeground;
			if (number.compareTo(BigDecimal.ZERO) > 0)
			{
				return ColorSettings.Positive;
			}
			else if (number.compareTo(BigDecimal.ZERO) < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return ColorSettings.GridForeground;
			}
		}
		else
		{
			if (number == null)
				return NumericColor.zero;
			if (number.compareTo(BigDecimal.ZERO) > 0)
			{
				return NumericColor.positive;
			}
			else if (number.compareTo(BigDecimal.ZERO) < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return NumericColor.zero;
			}
		}
	}

	public static Color getColor(int number, boolean useColorSettings)
	{
		if(useColorSettings && ColorSettings.useBlackAsBackground)
		{
			if (number > 0)
			{
				return NumericColor.positive;
			}
			else if (number < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return ColorSettings.GridForeground;
			}
		}
		else
		{
			if (number > 0)
			{
				return ColorSettings.Positive;
			}
			else if (number < 0)
			{
				return NumericColor.negative;
			}
			else
			{
				return NumericColor.zero;
			}
		}
	}
}
