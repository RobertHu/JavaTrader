package tradingConsole.ui.colorHelper;

import java.awt.Color;
import tradingConsole.ui.ColorSettings;
import tradingConsole.settings.PublicParametersManager;

public class QuotationStatusColor
{
	public static Color notChange;
	public static Color up;
	public static Color down = Color.RED;

	static
	{
	if (ColorSettings.useBlackAsBackground)
	{
		QuotationStatusColor.notChange = ColorSettings.GridForeground;
		QuotationStatusColor.up = ColorSettings.Up;
	}
	else
	{
		QuotationStatusColor.notChange = Color.BLACK;
		QuotationStatusColor.up = PublicParametersManager.useGreenAsRiseColor ? new Color(0,136,8) :  new Color(108, 108, 255) ;
	}
	}
}
