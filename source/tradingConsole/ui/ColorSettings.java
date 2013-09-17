package tradingConsole.ui;

import java.awt.Color;
import tradingConsole.settings.PublicParametersManager;

public class ColorSettings
{
	public static Color defaultBackground = new Color(-13421773);//new Color(16, 16, 16);

	public static boolean useBlackAsBackground = true;

	public static Color TradingPanelListFrameBackground = defaultBackground;
	public static Color TradingPanelGridFrameBackground = defaultBackground;
	public static Color AccountStatusFrameBackground = defaultBackground;
	public static Color PositionSummaryFrameBackground = defaultBackground;
	public static Color WorkingOrderListFrameBackground = defaultBackground;
	public static Color PhysicalFrameBackground = defaultBackground;
	public static Color OpenOrderListFrameBackground = defaultBackground;
	public static Color LogFrameBackground = defaultBackground;
	public static Color MessageFrameBackground = defaultBackground;
	public static Color NewsFrameBackground = defaultBackground;

	public static Color TradingListGridBackground = defaultBackground;
	public static Color TradingPanelGridBackground = defaultBackground;
	public static Color AccountStatusGridBackground = defaultBackground;
	public static Color PositionSummaryGridBackground = defaultBackground;
	public static Color WorkingOrderListGridBackground = defaultBackground;
	public static Color OpenOrderListGridBackground = defaultBackground;
	public static Color LogGridBackground = defaultBackground;
	public static Color MessageGridBackground = defaultBackground;
	public static Color NewsGridBackground = defaultBackground;

	public static Color[] GridStripeColors = new Color[]{defaultBackground, new Color(65, 65, 65)};
	public static Color GridForeground = Color.WHITE;

	public static Color Buy = PublicParametersManager.useGreenAsRiseColor ? new Color(0,136,8) :  new Color(108, 108, 255) ;
	public static Color Up = Buy;
	public static Color Positive = Buy;
	public static Color Open = Buy;
}
