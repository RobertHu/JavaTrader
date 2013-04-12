package tradingConsole.ui.colorHelper;

import java.awt.Color;
import tradingConsole.ui.ColorSettings;
import tradingConsole.settings.PublicParametersManager;

public class BuySellColor
{
	public static Color buy = PublicParametersManager.useGreenAsRiseColor ? new Color(0,192,0) : Color.BLUE;
	public static Color sell = Color.red;

	public static Color getColor(boolean isBuy, boolean useColorSettings)
	{
		if(useColorSettings && ColorSettings.useBlackAsBackground)
		{
			return (isBuy) ? ColorSettings.Buy : BuySellColor.sell;
		}
		else
		{
			return (isBuy) ? BuySellColor.buy : BuySellColor.sell;
		}
	}
}
