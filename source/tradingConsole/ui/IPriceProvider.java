package tradingConsole.ui;

public interface IPriceProvider
{
	/*String getBidLeft();
	String getBidRight();
	String getAskLeft();
	String getAskRight();*/
	String get_Bid();
	String get_Ask();
	boolean isNormal();
}
