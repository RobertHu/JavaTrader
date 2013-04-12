package tradingConsole.ui.colorHelper;

import java.awt.Color;
import tradingConsole.settings.PublicParametersManager;

public class TradeOptionColor
{
	public static Color stop = new Color(16711680);
	public static Color better = PublicParametersManager.useGreenAsRiseColor ? new Color(0,192,0) : Color.BLUE;
}
