package tradingConsole.ui.colorHelper;

import java.awt.Color;
import tradingConsole.ui.ColorSettings;

public class OpenCloseColor
{
	public static Color open = Color.blue;
	public static Color close = Color.red;

	static
	{
	if(ColorSettings.useBlackAsBackground)
	{
		open = ColorSettings.Open;
	}
	else
	{
		open = Color.blue;
	}

	}

	public static Color getColor(boolean isOpen)
	{
		return (isOpen)?OpenCloseColor.open:OpenCloseColor.close;
	}
}
