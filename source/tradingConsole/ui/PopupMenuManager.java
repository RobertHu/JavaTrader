package tradingConsole.ui;

import javax.swing.MenuElement;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.JPopupMenu;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.Instrument;
import tradingConsole.enumDefine.MenuType;
import java.awt.event.ActionEvent;

public class PopupMenuManager
{
	private PopupMenuManager()
	{
	}

	public static void initializePopupMenus(MainForm mainForm)
	{
		/*MenuItemProcessor menuItemProcessor = new MenuItemProcessor(mainForm);
		MenuItemStautsProvider menuItemStautsProvider = new MenuItemStautsProvider(mainForm);

		JPopupMenu popupMenu = PopupMenuManager.createPopupMenuForInstrumentTable(menuItemProcessor, menuItemStautsProvider);
		mainForm.get_InstrumentTable().setComponentPopupMenu(popupMenu);*/
	}

	private static JPopupMenu createPopupMenuForInstrumentTable(MenuItemProcessor menuItemProcessor, MenuItemStautsProvider menuItemStautsProvider)
	{
		JAdvancedPopupMenu popupMenu = new JAdvancedPopupMenu(PopupMenuNames.InstrumentTable, PopupMenuNames.InstrumentTable, menuItemProcessor, menuItemStautsProvider);

		JAdvancedMenuItem menuItem = new JAdvancedMenuItem(MenuItemNames.PlaceLimitOrder, MenuItemNames.PlaceLimitOrder);
		menuItem.addTo(popupMenu);

		menuItem = new JAdvancedMenuItem(MenuItemNames.PlaceLiquidationOrder, MenuItemNames.PlaceLiquidationOrder);
		menuItem.addTo(popupMenu);

		/*popupMenu.addSeparator();

		menuItem = new JAdvancedMenuItem(MenuItemNames.MoveUp, MenuItemNames.MoveUp);
		menuItem.addTo(popupMenu);

		menuItem = new JAdvancedMenuItem(MenuItemNames.MoveDown, MenuItemNames.MoveDown);
		menuItem.addTo(popupMenu);

		menuItem = new JAdvancedMenuItem(MenuItemNames.MoveToTop, MenuItemNames.MoveToTop);
		menuItem.addTo(popupMenu);

		menuItem = new JAdvancedMenuItem(MenuItemNames.MoveToBottom, MenuItemNames.MoveToBottom);
		menuItem.addTo(popupMenu);*/

		popupMenu.addSeparator();

		menuItem = new JAdvancedMenuItem(MenuItemNames.SelectInstruments, MenuItemNames.SelectInstruments);
		menuItem.addTo(popupMenu);

		return popupMenu;
	}
}

class MenuItemProcessor implements IMenuItemProcessor
{
	private MainForm _mainForm;
	public MenuItemProcessor(MainForm mainForm)
	{
		this._mainForm = mainForm;
	}

	public void process(JAdvancedMenuItem menuItem)
	{
		String parentName = menuItem.get_Parent().getName();
		if(parentName.equalsIgnoreCase(PopupMenuNames.InstrumentTable))
		{
			DataGrid grid = this._mainForm.get_InstrumentTable();
			int selectedRow = grid.getSelectedRow();
			Instrument instrument = (Instrument)(grid.getObject(selectedRow));
			String itemName = menuItem.getName();
			if(itemName.equalsIgnoreCase(MenuItemNames.PlaceLimitOrder))
			{
				this._mainForm.processInstrumentEvent(instrument, false, false);
			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.PlaceLiquidationOrder))
			{
				this._mainForm.processInstrumentEvent(instrument, true, false);
			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.SelectInstruments))
			{
				this._mainForm.menuProcess(MenuType.InstrumentSelection);
			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.MoveDown))
			{

			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.MoveToBottom))
			{

			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.MoveToTop))
			{

			}
			else if(itemName.equalsIgnoreCase(MenuItemNames.MoveUp))
			{

			}
		}
	}
}

class MenuItemStautsProvider implements IMenuItemStautsProvider
{
	private MainForm _mainForm;
	public MenuItemStautsProvider(MainForm mainForm)
	{
		this._mainForm = mainForm;
	}

	public boolean get_isEnable(JAdvancedMenuItem menuItem)
	{
		JAdvancedPopupMenu popupMenu = menuItem.get_Parent();
		String popupMenuName = menuItem.get_Parent().getName();
		String menuItemName = menuItem.getName();

		if(popupMenuName.equals(PopupMenuNames.InstrumentTable))
		{
			DataGrid grid = this._mainForm.get_InstrumentTable();
			int selectedRow = grid.getSelectedRow();

			if(selectedRow == -1)
			{
				return menuItemName.equals(MenuItemNames.SelectInstruments);
			}
			else
			{
				Instrument instrument = (Instrument)grid.getObject(selectedRow);
				if(menuItemName.equals(MenuItemNames.PlaceLimitOrder) || menuItemName.equals(MenuItemNames.PlaceLiquidationOrder))
				{
					return instrument.getIsValidate();
				}
			}
		}
		return true;
	}

	public boolean get_isVisible(JAdvancedMenuItem menuItem)
	{
		return true;
	}
}

class PopupMenuNames
{
	public static String InstrumentTable = "InstrumentTable";
}

class MenuItemNames
{
	public static String MoveUp = "MoveUp";
	public static String MoveDown = "MoveDown";
	public static String MoveToTop = "MoveToTop";
	public static String MoveToBottom = "MoveToBottom";
	public static String PlaceLimitOrder = "PlaceLimitOrder";
	public static String PlaceLiquidationOrder = "PlaceLiquidationOrder";
	public static String SelectInstruments = "SelectInstruments";
}

class JAdvancedMenuItem extends JMenuItem
{
	private JAdvancedPopupMenu _parent;

	public JAdvancedMenuItem(String caption, String name)
	{
		super(caption);
		this.setName(name);
	}

	public JAdvancedPopupMenu get_Parent()
	{
		return this._parent;
	}

	public void addTo(JAdvancedPopupMenu parent)
	{
		this._parent = parent;
		parent.add(this);
		this.addActionListener(new ActionListener(this));
	}

	private static class ActionListener implements java.awt.event.ActionListener
	{
		private JAdvancedMenuItem _owner;
		public ActionListener(JAdvancedMenuItem menuItem)
		{
			this._owner = menuItem;
		}

		public void actionPerformed(ActionEvent e)
		{
			this._owner._parent.Process(this._owner);
		}
	}
}

class JAdvancedPopupMenu extends JPopupMenu
{
	private IMenuItemProcessor _menuItemProcessor;
	private IMenuItemStautsProvider _menuItemStautsProvider;

	public JAdvancedPopupMenu(String caption, String name, IMenuItemProcessor menuItemProcessor, IMenuItemStautsProvider menuItemStautsProvider)
	{
		super(caption);
		this.setName(name);

		this._menuItemProcessor = menuItemProcessor;
		this._menuItemStautsProvider = menuItemStautsProvider;

		super.addPopupMenuListener(new PopupMenuListener(this));
	}

	public void Process(JAdvancedMenuItem menuItem)
	{
		this._menuItemProcessor.process(menuItem);
	}

	private static class PopupMenuListener implements javax.swing.event.PopupMenuListener
	{
		private JAdvancedPopupMenu _owner;
		public PopupMenuListener(JAdvancedPopupMenu owner)
		{
			this._owner = owner;
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e)
		{
			MenuElement[] children = this._owner.getSubElements();
			for(MenuElement menuElement : children)
			{
				if(menuElement instanceof JAdvancedMenuItem)
				{
					JAdvancedMenuItem menuItem = ((JAdvancedMenuItem)menuElement);
					menuItem.setVisible(this._owner._menuItemStautsProvider.get_isVisible(menuItem));
					menuItem.setEnabled(this._owner._menuItemStautsProvider.get_isEnable(menuItem));
				}
			}
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
		{
		}

		public void popupMenuCanceled(PopupMenuEvent e)
		{
		}
	}
}
