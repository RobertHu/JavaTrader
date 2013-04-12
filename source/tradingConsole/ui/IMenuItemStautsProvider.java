package tradingConsole.ui;

import javax.swing.JMenuItem;

public interface IMenuItemStautsProvider
{
	boolean get_isEnable(JAdvancedMenuItem menuItem);
	boolean get_isVisible(JAdvancedMenuItem menuItem);
}
