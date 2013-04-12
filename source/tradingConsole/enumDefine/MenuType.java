package tradingConsole.enumDefine;

import framework.lang.Enum;

public class MenuType extends Enum<MenuType>
{
	public static final MenuType Connect = new MenuType("Connect", 0);
	public static final MenuType InstrumentSelection = new MenuType("InstrumentSelection", 1);
	public static final MenuType ChangePassword = new MenuType("ChangePassword", 2);
	public static final MenuType Chart = new MenuType("Chart", 3);
	public static final MenuType Message = new MenuType("Message", 4);
	public static final MenuType Report = new MenuType("Report", 5);
	public static final MenuType Margin = new MenuType("Margin", 6);
	public static final MenuType ClearCancelledOrder = new MenuType("ClearCancelledOrder", 7);
	public static final MenuType DownloadDocument = new MenuType("DownloadDocument", 8);
	public static final MenuType Option = new MenuType("Option", 9);
	public static final MenuType ChatRoom = new MenuType("ChatRoom", 10);
	public static final MenuType Assign = new MenuType("Assign", 11);
	public static final MenuType AccountActive = new MenuType("AccountActive", 12);
	public static final MenuType RefreshSystem = new MenuType("RefreshSystem", 13);
	public static final MenuType OnlineHelp = new MenuType("OnlineHelp", 14);
	public static final MenuType News = new MenuType("News", 15);
	public static final MenuType Debug = new MenuType("Debug", 16);
	public static final MenuType ExitSystem = new MenuType("ExitSystem", 17);
	public static final MenuType ChangeLeverage = new MenuType("Margin", 18);
	public static final MenuType AccountSelection = new MenuType("AccountSelection", 19);

	private MenuType(String name, int value)
	{
		super(name, value);
	}
}
