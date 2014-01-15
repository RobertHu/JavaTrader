package tradingConsole.ui;

import java.beans.*;
import java.io.*;
import java.io.File;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.Action;

import org.aiotrade.core.common.Instrument;
import org.aiotrade.math.timeseries.*;
import org.aiotrade.ui.*;
import com.jidesoft.action.*;
import com.jidesoft.docking.*;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockableFrame.*;
import com.jidesoft.icons.*;
import com.jidesoft.status.*;
import com.jidesoft.swing.*;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import framework.io.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import tradingConsole.service.ServiceTimeoutSetting;
import tradingConsole.ui.account.HierarchicalTableComponentFactory;

public class MainFormBase extends DefaultDockableBarDockableHolder implements MouseListener, MouseMotionListener //DefaultDockableHolder implements MouseListener, MouseMotionListener
{
	protected TradingConsole _owner;
	protected SettingsManager _settingsManager;

	private DockableFrame _tradingPanelListFrame;
	private DockableFrame _tradingPanelGridFrame;
	private DockableFrame _accountStatusFrame;
	private DockableFrame _accountListFrame;
	private DockableFrame _positionSummaryFrame;
	private DockableFrame _newsFrame;
	private DockableFrame _messageFrame;
	private DockableFrame _logFrame;
	private DockableFrame _workingOrderListFrame;
	private DockableFrame _openOrderListFrame;
	private DockableFrame _orderQueryFrame;

	protected DockableFrame _physicalFrame;
	private MainForm_physicalInvetoryTable_AdjustmentListener _physicalInventoryTableActionListener;

	protected boolean _floatingChildFrames = true;
	protected HashMap<String, AioChartPanel> _chartPanels;
	protected int _chartPanelKey = 0;

	static
	{
		//TradingConsole.traceSource.trace(TraceType.Information, "com.jidesoft.utils.Lm.verifyLicense begin " + DateTime.get_Now().toString(DateTime.fullFormat));
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader", "TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
		//TradingConsole.traceSource.trace(TraceType.Information, "com.jidesoft.utils.Lm.verifyLicense end " + DateTime.get_Now().toString(DateTime.fullFormat));
	}

	public DockableFrame get_NewsFrame()
	{
		return this._newsFrame;
	}

	public DockableFrame get_MessageFrame()
	{
		return this._messageFrame;
	}

	public DockableFrame get_LogFrame()
	{
		return this._logFrame;
	}

	public DockableFrame get_PositionSummaryFrame()
	{
		return this._positionSummaryFrame;
	}

	public IActionListener get_PhysicalInventoryTableActionListener()
	{
		return this._physicalInventoryTableActionListener;
	}

	private StatusBar createStatusBar()
	{
		// setup status bar
		StatusBar statusBar = new StatusBar();

		statusBar.add(this.statusStaticText, JideBoxLayout.FLEXIBLE);
		statusBar.add(this.loginInformationStaticText, JideBoxLayout.FLEXIBLE);
		statusBar.add(this.appTimeStaticText, JideBoxLayout.FLEXIBLE);

		final ResizeStatusBarItem resize = new ResizeStatusBarItem();
		statusBar.add(resize, JideBoxLayout.FLEXIBLE);

		return statusBar;
	}

	public MainFormBase(TradingConsole owner, SettingsManager settingsManager)
	{
		this._owner = owner;
		this._settingsManager = settingsManager;
		queryPanel = new OrderQueryPanel(this._owner);
		this._physicalInventoryTableActionListener = new MainForm_physicalInvetoryTable_AdjustmentListener(this);

		try
		{
			//TradingConsole.traceSource.trace(TraceType.Information, "MainFormBase constructor begin " + DateTime.get_Now().toString(DateTime.fullFormat));
			accountStatusTable = new DataGrid("AccountStatusGrid");
			if(ColorSettings.useBlackAsBackground)
			{
				accountStatusTable.setBackground(ColorSettings.AccountStatusGridBackground);
				accountStatusTable.setGridColor(ColorSettings.AccountStatusGridBackground);
			}
			accountListTable = new DataGrid("AccountListGrid");
			if(ColorSettings.useBlackAsBackground)
			{
				accountListTable.setBackground(ColorSettings.AccountStatusGridBackground);
				accountListTable.setGridColor(ColorSettings.AccountStatusGridBackground);
			}

			/*{
				@Override
				public void paint(Graphics g)
				{
					super.paint(g);
					if (ColorSettings.Enabled)
					{
						g.setXORMode(new Color(192, 192, 192));
						g.fillRect(0, 0, this.getSize().width, this.getSize().height);
					}
				}
			};*/

			appTimeStaticText = new PVStaticText2();
			loginInformationStaticText = new PVStaticText2();
			statusStaticText = new PVStaticText2();
			logoButton = new PVButton2();

			commandBar = new CommandBar("CommandBar");
			connectButton2 = new JideToggleButton();
			exitButton2 = new JideToggleButton();

			instrumentSelectionButton2 = new JideToggleButton();
			accountSelectionButton2 = new JideToggleButton();
			clearCancelledOrderButton2 = new JideToggleButton();
			assignButton2 = new JideToggleButton();
			accountActiveButton2 = new JideToggleButton();
			changePasswordButton2 = new JideToggleButton();
			refreshButton2 = new JideToggleButton();

			reportButton2 = new JideToggleButton();
			chartButton2 = new JideToggleButton();
			newsButton2 = new JideToggleButton();
			messageButton2 = new JideToggleButton();
			marginButton2 = new JideToggleButton();
			changeLeverageButton2 = new JideToggleButton();
			downloadDocumentButton2 = new JideToggleButton();
			chatRoomButton2 = new JideToggleButton();

			optionButton2 = new JideToggleButton();
			debugButton2 = new JideToggleButton();

			resetLayoutButton2 = new JideToggleButton();
			tradingFactButton = new JideToggleButton();
			dumpButton = new JideToggleButton();
			floatingFramesButton = new JideToggleButton();

			onlineHelpButton2 = new JideToggleButton();

			//Menu Bar
			commandMenuBar = new CommandMenuBar("MenuBar");
			fileMenu = new JideMenu("File");
			connectButton = new JMenuItem();
			exitButton = new JMenuItem();

			operateMenu = new JideMenu("Operate");
			instrumentSelectionButton = new JMenuItem();
			accountSelectionButton = new JMenuItem();
			clearCancelledOrderButton = new JMenuItem();
			assignButton = new JMenuItem();
			accountActiveButton = new JMenuItem();
			changePasswordButton = new JMenuItem();
			refreshButton = new JMenuItem();

			viewMenu = new JideMenu("View");
			reportButton = new JMenuItem();
			chartButton = new JMenuItem();
			newsButton = new JMenuItem();
			messageButton = new JMenuItem();
			marginButton = new JMenuItem();
			changeLeverageButton = new JMenuItem();
			downloadDocumentButton = new JMenuItem();
			chatRoomButton = new JMenuItem();

			toolsMenu = new JideMenu("Tools");
			optionButton = new JMenuItem();
			debugButton = new JMenuItem();

			windowMenu = new JideMenu("Window");
			resetLayoutButton = new JMenuItem();

			helpMenu = new JideMenu("Help");
			onlineHelpButton = new JMenuItem();

			lookAndFeelItem = new JideSplitButton();

			jbInit();

			// add toolbar
			this.setMenuBarCaption();
			this.menuCommandBarInit();
			this.getDockableBarManager().addDockableBar(this.commandMenuBar);

			this.commandMenuBar.setOpaque(true);

			this.commandBarInit();
			this.getDockableBarManager().addDockableBar(this.commandBar);
			this.getContentPane().add(createStatusBar(), BorderLayout.AFTER_LAST_LINE);
			this.setExtendedState(Frame.MAXIMIZED_BOTH);
			this.getDockingManager().setDoubleClickAction(DockingManager.DOUBLE_CLICK_NONE);
			//this.setResizable(false);
			//TradingConsole.traceSource.trace(TraceType.Information, "MainFormBase constructor end " + DateTime.get_Now().toString(DateTime.fullFormat));
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void addChildFrames()
	{
		try
		{
			DockingManager dockingManager = this.getDockingManager();

			this.createInstrumentGridPanel();
			this.createInstrumentSpanGridPanel();
			this.createAccountStatusPanel();
			this.createAccountListPanel();
			this.createPositionSummaryPanel();
			this.createWorkingOrderListPanel();
			this.createOrderQueryPanel();
			this.createOpenOrderListPanel();
			this.createLogPanel();
			this.createMessagePanel();
			this.createNewsPanel();
			this.createPhysicalPanel();

			if(ColorSettings.useBlackAsBackground)
			{
				this.setScrollPaneBackground(this._tradingPanelListFrame.getContentPane(), ColorSettings.TradingPanelListFrameBackground);
				this.setScrollPaneBackground(this._tradingPanelGridFrame.getContentPane(), ColorSettings.TradingPanelGridFrameBackground);
				this.setScrollPaneBackground(this._accountStatusFrame.getContentPane(), ColorSettings.AccountStatusFrameBackground);
				this.setScrollPaneBackground(this._accountListFrame.getContentPane(), ColorSettings.AccountStatusFrameBackground);
				this.setScrollPaneBackground(this._positionSummaryFrame.getContentPane(), ColorSettings.PositionSummaryFrameBackground);
				this.setScrollPaneBackground(this._workingOrderListFrame.getContentPane(), ColorSettings.WorkingOrderListFrameBackground);
				this.setScrollPaneBackground(this._openOrderListFrame.getContentPane(), ColorSettings.OpenOrderListFrameBackground);
				this.setScrollPaneBackground(this._physicalFrame.getContentPane(), ColorSettings.PhysicalFrameBackground);
				this.setScrollPaneBackground(this._logFrame.getContentPane(), ColorSettings.LogFrameBackground);
				this.setScrollPaneBackground(this._messageFrame.getContentPane(), ColorSettings.MessageFrameBackground);
				this.setScrollPaneBackground(this._newsFrame.getContentPane(), ColorSettings.NewsFrameBackground);
			}

			dockingManager.addFrame(this._tradingPanelListFrame);
			dockingManager.addFrame(this._tradingPanelGridFrame);
			dockingManager.addFrame(this._accountStatusFrame);
			if(this._settingsManager.get_Accounts().size() > 1)
			{
				dockingManager.addFrame(this._accountListFrame);

				FloatingSearchHelper.SearchHandler searchHandler = new FloatingSearchHelper.SearchHandler()
				{
					public void doSearch(String searchStr)
					{
						searchInAccountList(searchStr);
					}
				};

				FloatingSearchHelper.register(this.accountListTable, this, searchHandler);
				FloatingSearchHelper.register(this._accountListFrame, this, searchHandler);

				searchHandler = new FloatingSearchHelper.SearchHandler()
				{
					public void doSearch(String searchStr)
					{
						searchInAccountTree(searchStr);
					}
				};
				FloatingSearchHelper.register(this.accountStatusTable, this, searchHandler);
				FloatingSearchHelper.register(this._accountStatusFrame, this, searchHandler);
			}
			if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
			{
				dockingManager.addFrame(this._positionSummaryFrame);
			}
			dockingManager.addFrame(this._workingOrderListFrame);
			if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
			{
				dockingManager.addFrame(this._openOrderListFrame);
			}
			dockingManager.addFrame(this._orderQueryFrame);

			if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical))
			{
				dockingManager.addFrame(this._physicalFrame);
			}
			this._owner.initializeQueryOrderList();
			boolean needShowLog = this._settingsManager.get_Customer().get_ShowLog();
			if (needShowLog)
			{
				dockingManager.addFrame(this._logFrame);
			}

			dockingManager.addFrame(this._messageFrame);
			dockingManager.addFrame(this._newsFrame);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void searchInAccountTree(String searchStr)
	{
		int row = -1;
		searchStr = searchStr.toLowerCase();
		for(int index = 0; index < this.accountStatusTable.getRowCount(); index++)
		{
			Object obj = this.accountStatusTable.getObject(index);
			if(obj instanceof tradingConsole.ui.account.Account)
			{
				tradingConsole.ui.account.Account item = (tradingConsole.ui.account.Account)obj;
				String code = item.get_Code().toLowerCase();
				if(code.startsWith(searchStr))
				{
					row = index;
					break;
				}
			}
		}

		if(row > -1)
		{
			this.accountStatusTable.scrollToVisible(row, 0);

			this.accountStatusTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			this.accountStatusTable.setCellSelectionEnabled(true);

			this.accountStatusTable.changeSelection(row, 0, false, false);
		}
	}

	private void searchInAccountList(String searchStr)
	{
		int row = -1;
		searchStr = searchStr.toLowerCase();
		for (int index = 0; index < this.accountListTable.getRowCount(); index++)
		{
			Object obj = this.accountListTable.getObject(index);
			if (obj instanceof tradingConsole.ui.accountList.Account)
			{
				tradingConsole.ui.accountList.Account item = (tradingConsole.ui.accountList.Account)obj;
				String code = item.get_Code().toLowerCase();
				if (code.startsWith(searchStr))
				{
					row = index;
					break;
				}
			}
		}

		if(row > -1)
		{
			this.accountListTable.scrollToVisible(row, 0);

			this.accountListTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			this.accountListTable.setCellSelectionEnabled(true);

			for(int index = 1; index < this.accountListTable.getColumnCount(); index++)
			{
				this.accountListTable.changeSelection(row, index, false, index > 1);
			}
		}
	}

	protected Rectangle getFloatingRectangle(String key)
	{
		return this.getFloatingRectangle(key, true);
	}

	public Rectangle getFloatingRectangle(String key, boolean showFirstChart)
	{
		Dimension screenSize = this.getSize();
		Point location = this.getLocation();
		screenSize = new Dimension(screenSize.width - 30, screenSize.height - 200);

		if (key.equalsIgnoreCase("TradingPanelListFrame"))
		{
			if(/*this._settingsManager.get_IsForRSZQ() || */!showFirstChart) return new Rectangle(location.x + 8, location.y + 85, (int) (screenSize.width)+14, (int) (screenSize.height * 0.4));
			else return new Rectangle(location.x + 8, location.y + 85, (int) (screenSize.width * 0.6), (int) (screenSize.height * 0.4));
		}
		else if (key.equalsIgnoreCase("ChartFrame1"))
		{
			return new Rectangle(location.x + (int) (screenSize.width * 0.6) + 22, location.y + 85, (int) (screenSize.width * 0.4), (int) (screenSize.height * 0.4) + 32);
		}
		else if (key.equalsIgnoreCase("PositionSummaryFrame") || key.equalsIgnoreCase("AccountStatusFrame"))
		{
			return new Rectangle(location.x + 8, location.y + (int) (screenSize.height * 0.4) + 130, (int) (screenSize.width * 0.3), (int) (screenSize.height * 0.5) + 42);
		}
		else if (key.equalsIgnoreCase("OpenOrderListFrame") || key.equalsIgnoreCase("QueryOrderListFrame")
			|| key.equalsIgnoreCase("PhysicalStokFrame"))
		{
			if(this._settingsManager.get_IsForRSZQ())
			{
				return new Rectangle(location.x + (int) (screenSize.width * 0.3) + 22, location.y + (int) (screenSize.height * 0.6) + 181,
									 (int) (screenSize.width * 0.7), (int) (screenSize.height * 0.285));
			}
			else
			{
				return new Rectangle(location.x + (int) (screenSize.width * 0.3) + 22, location.y + (int) (screenSize.height * 0.4) + 130,
									 (int) (screenSize.width * 0.7), (int) (screenSize.height * 0.6));
			}
		}
		else if(key.equalsIgnoreCase("WorkingOrderListFrame"))
		{
			return new Rectangle(location.x + (int) (screenSize.width * 0.3) + 22, location.y + (int) (screenSize.height * 0.4) + 130,
								 (int) (screenSize.width * 0.7), (int) (screenSize.height * 0.28) + 4);
		}
		return null;
	}

	protected void showFrame(String key)
	{
		DockableFrame dockableFrame = this.getDockingManager().getFrame(key);
		if (dockableFrame != null)
		{
			this.getDockingManager().showFrame(key);
		}
	}

	public void setFrameCaption()
	{
		this.setMenuBarCaption();

		this._tradingPanelListFrame.setTabTitle(Language.InstrumentViewPrompt);
		this._tradingPanelListFrame.setTitle(Language.InstrumentViewPrompt);
		this._tradingPanelListFrame.setSideTitle(Language.InstrumentViewPrompt);

		if (this._tradingPanelGridFrame != null)
		{
			this._tradingPanelGridFrame.setTabTitle(Language.InstrumentView2Prompt);
			this._tradingPanelGridFrame.setTitle(Language.InstrumentView2Prompt);
			this._tradingPanelGridFrame.setSideTitle(Language.InstrumentView2Prompt);
		}

		this._accountStatusFrame.setTabTitle(Language.AccountStatusPrompt);
		this._accountStatusFrame.setTitle(Language.AccountStatusPrompt);
		this._accountStatusFrame.setSideTitle(Language.AccountStatusPrompt);

		this._positionSummaryFrame.setTabTitle(Language.SummaryPrompt);
		this._positionSummaryFrame.setTitle(Language.SummaryPrompt);
		this._positionSummaryFrame.setSideTitle(Language.SummaryPrompt);

		if (this._logFrame != null)
		{
			this._logFrame.setTabTitle(Language.LogPrompt);
			this._logFrame.setTitle(Language.LogPrompt);
			this._logFrame.setSideTitle(Language.LogPrompt);
		}
		if (this._newsFrame != null)
		{
			this._newsFrame.setTabTitle(Language.newsFormTitle);
			this._newsFrame.setTitle(Language.newsFormTitle);
			this._newsFrame.setSideTitle(Language.newsFormTitle);
		}
		if (this._messageFrame != null)
		{
			this._messageFrame.setTabTitle(Language.messageFormTitle);
			this._messageFrame.setTitle(Language.messageFormTitle);
			this._messageFrame.setSideTitle(Language.messageFormTitle);
		}

		this._workingOrderListFrame.setTabTitle(Language.SettingOrderGrid);
		this._workingOrderListFrame.setTitle(Language.SettingOrderGrid);
		this._workingOrderListFrame.setSideTitle(Language.SettingOrderGrid);

		this._openOrderListFrame.setTabTitle(Language.SettingOpenOrderGrid);
		this._openOrderListFrame.setTitle(Language.SettingOpenOrderGrid);
		this._openOrderListFrame.setSideTitle(Language.SettingOpenOrderGrid);

		DockableFrame chartFrame1 = this.getDockingManager().getFrame("ChartFrame1");
		if (chartFrame1 != null)
		{
			chartFrame1.setTabTitle(Language.MenuimgShowAnalyticChart);
			chartFrame1.setTitle(Language.MenuimgShowAnalyticChart);
			chartFrame1.setSideTitle(Language.MenuimgShowAnalyticChart);
		}
	}

	private Icon getAsIcon(String name)
	{
		Icon icon = AppToolkit.getAsIcon(name);
		if (icon == null)
		{
			icon = JideIconsFactory.getImageIcon(JideIconsFactory.DockableFrame.FRAME1);
		}
		return icon;
	}

	protected void removeChartPanel(String key)
	{
		String frameKey = "ChartFrame" + key;
		DockableFrame dockableFrame = this.getDockingManager().getFrame(frameKey);
		if (dockableFrame != null)
		{
			this.getDockingManager().removeFrame(frameKey);
			dockableFrame.removeAll();
		}
	}

	private static class ChartViewTypeListener implements org.aiotrade.core.common.IViewTypeListener
	{
		private DockableFrame _chartFrame;

		public ChartViewTypeListener(DockableFrame chartFrame)
		{
			this._chartFrame = chartFrame;
		}

		public void viewTypeSelected(Instrument instrument, Frequency frequency)
		{
			String title = instrument + "-" + frequency;
			this._chartFrame.setTabTitle(title);
			this._chartFrame.setTitle(title);
			this._chartFrame.setSideTitle(title);
		}
	}

	protected void addChart(String key, AioChartPanel chartPanel)
	{
		if(this.getDockingManager().getAllFrameNames().contains("ChartFrame" + key))
		{
			this.getDockingManager().showFrame("ChartFrame" + key);
			return;
		}

		DockableFrame chartFrame = new DockableFrame(Language.MenuimgShowAnalyticChart + key, this.getAsIcon("Chart.ico"));
		chartFrame.getContentPane().add(chartPanel);

		//ID
		chartFrame.setKey("ChartFrame" + key);

		//Titles
		chartFrame.setTabTitle(Language.MenuimgShowAnalyticChart);
		chartFrame.setTitle(Language.MenuimgShowAnalyticChart);
		chartFrame.setSideTitle(Language.MenuimgShowAnalyticChart);

		ChartViewTypeListener viewTypeListener = new ChartViewTypeListener(chartFrame);
		chartPanel.addViewTypeListener(viewTypeListener);

		//Initial Layout
		if (key.equalsIgnoreCase("1"))
		{
			chartFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
			chartFrame.getContext().setInitSide(DockContext.DOCK_SIDE_NORTH);
			chartFrame.getContext().setInitIndex(6);
			chartFrame.getContext().setFloatable(this._floatingChildFrames);
		}
		else
		{
			chartFrame.getContext().setInitMode(this._floatingChildFrames ? DockContext.STATE_FLOATING : DockContext.STATE_FRAMEDOCKED);
			chartFrame.getContext().setFloatable(this._floatingChildFrames);
			chartFrame.getContext().setInitIndex(6);
			Action action = new ChartFrameCloseAction(chartFrame, key, this);
			action.setEnabled(true);
			chartFrame.setCloseAction(action);
		}

		if(!this.getDockingManager().getAllFrameNames().contains(chartFrame.getKey())) this.getDockingManager().addFrame(chartFrame);
		this.getDockingManager().showFrame(chartFrame.getKey());
		if(this._floatingChildFrames)
		{
			if(this.getDockingManager().getFrame("TradingPanelListFrame").isVisible())
			{
				this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame"), false);
			}

			this.getDockingManager().floatFrame(chartFrame.getKey(), this.getFloatingRectangle("ChartFrame1"), true);
		}
	}

	static class ChartFrameCloseAction implements Action
	{
		private String _key;
		private DockableFrame.CloseAction _originCloseAction;
		private MainFormBase _mainForm;

		public ChartFrameCloseAction(DockableFrame chartFrame, String key, MainFormBase mainForm)
		{
			this._key = key;
			this._originCloseAction = (DockableFrame.CloseAction)chartFrame.getCloseAction();
			this._mainForm = mainForm;
		}

		public void actionPerformed(ActionEvent e)
		{
			this._mainForm.removeChartPanel(this._key);
		}

		public Object getValue(String key)
		{
			return this._originCloseAction.getValue(key);
		}

		public void putValue(String key, Object value)
		{
			this._originCloseAction.putValue(key, value);
		}

		public void setEnabled(boolean b)
		{
			this._originCloseAction.setEnabled(b);
		}

		public boolean isEnabled()
		{
			return this._originCloseAction.isEnabled();
		}

		public void addPropertyChangeListener(PropertyChangeListener listener)
		{
			this._originCloseAction.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener)
		{
			this._originCloseAction.removePropertyChangeListener(listener);
		}
	}

	private void menuCommandBarInit()
	{
		this.commandMenuBar.setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
		this.commandMenuBar.setInitIndex(0);
		this.commandMenuBar.setPaintBackground(false);
		this.commandMenuBar.setStretch(true);
		this.commandMenuBar.setFloatable(true);
		this.commandMenuBar.setHidable(false);

		this.fileMenuInit();
		this.commandMenuBar.add(this.fileMenu);

		this.operateMenuInit();
		this.commandMenuBar.add(this.operateMenu);

		this.viewMenuInit();
		this.commandMenuBar.add(this.viewMenu);

		this.toolsMenuInit();
		this.commandMenuBar.add(this.toolsMenu);

		this.windowMenuInit();
		this.commandMenuBar.add(this.windowMenu);

		this.helpMenuInit();
		this.commandMenuBar.add(this.helpMenu);
	}

	private void commandBarInit()
	{
		this.commandBar.setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
		this.commandBar.setInitIndex(1);
		this.commandBar.setPaintBackground(false);
		this.commandBar.setStretch(true);
		this.commandBar.setFloatable(true);
		this.commandBar.setHidable(false);

		this.commandBar.add(this.connectButton2);
		this.commandBar.add(this.exitButton2);

		//this.commandBar.addSeparator();

		this.commandBar.add(this.instrumentSelectionButton2);
		this.commandBar.add(this.accountSelectionButton2);
		this.commandBar.add(this.clearCancelledOrderButton2);
		this.commandBar.add(this.assignButton2);
		this.commandBar.add(this.accountActiveButton2);
		this.commandBar.add(this.changePasswordButton2);
		this.commandBar.add(this.refreshButton2);

		//this.commandBar.addSeparator();

		this.commandBar.add(this.reportButton2);
		/*if(!this._settingsManager.get_IsForRSZQ())*/ this.commandBar.add(this.chartButton2);
		/*if(!this._settingsManager.get_IsForRSZQ())*/ this.commandBar.add(this.newsButton2);
		this.commandBar.add(this.messageButton2);
		this.commandBar.add(this.marginButton2);
		this.commandBar.add(this.changeLeverageButton2);
		this.commandBar.add(this.downloadDocumentButton2);
		this.commandBar.add(this.chatRoomButton2);

		//this.commandBar.addSeparator();

		this.commandBar.add(this.optionButton2);
		this.commandBar.add(this.optionButton2);

		//this.commandBar.addSeparator();

		this.commandBar.add(this.resetLayoutButton2);
		this.commandBar.add(this.tradingFactButton);

		//ServiceTimeoutSetting.initialize();
		if(ServiceTimeoutSetting.showComplainButton) this.commandBar.add(this.dumpButton);

		//this.commandBar.addSeparator();

		this.setLookAndFeelDockableBar();
		//this.lookAndFeelItem.getPopupMenu().setLightWeightPopupEnabled(false);
		if(!ColorSettings.useBlackAsBackground) this.commandBar.add(this.lookAndFeelItem);
		//this.commandBar.addSeparator();
		this.commandBar.add(this.floatingFramesButton);

		this.commandBar.add(this.onlineHelpButton2);
	}

	private void setLookAndFeelDockableBar()
	{
		if(ColorSettings.useBlackAsBackground) return;

		CommandBar dockableBar = CommandBarFactory.createLookAndFeelCommandBar(this);
		Component[] components = dockableBar.getComponents();
		for (int i = components.length - 1; i >= 0; i--)
		{
			Component component = components[i];
			if (component instanceof JideSplitButton)
			{
				if ( ( (JideSplitButton)component).getText().equalsIgnoreCase("Office2003 Themes"))
				{
					JideSplitButton item = (JideSplitButton)component;
					AppToolkit.toolBarItemInit(item, Language.LookAndFeelMenuItemText, Language.LookAndFeelMenuItemText,
											   Language.LookAndFeelMenuItemToolTipText, "LookAndFeel.ico");
					for (int j = 0; j < item.getItemCount(); j++)
					{
						JMenuItem menuItem = item.getItem(j);
						String menuItemText = menuItem.getText();
						if (menuItemText.equalsIgnoreCase("Default"))
						{
							AppToolkit.menuItemInit(menuItem, Language.LookAndFeelMenuItem1Text, Language.LookAndFeelMenuItem1Text,
								Language.LookAndFeelMenuItem1ToolTipText, "LookAndFeelMenuItem1.ico");
						}
						else if (menuItemText.equalsIgnoreCase("Gray"))
						{
							AppToolkit.menuItemInit(menuItem, Language.LookAndFeelMenuItem2Text, Language.LookAndFeelMenuItem2Text,
								Language.LookAndFeelMenuItem2ToolTipText, "LookAndFeelMenuItem2.ico");
						}
						else if (menuItemText.equalsIgnoreCase("HomeStead"))
						{
							AppToolkit.menuItemInit(menuItem, Language.LookAndFeelMenuItem3Text, Language.LookAndFeelMenuItem3Text,
								Language.LookAndFeelMenuItem3ToolTipText, "LookAndFeelMenuItem3.ico");
						}
						else if (menuItemText.equalsIgnoreCase("Metallic"))
						{
							AppToolkit.menuItemInit(menuItem, Language.LookAndFeelMenuItem4Text, Language.LookAndFeelMenuItem4Text,
								Language.LookAndFeelMenuItem4ToolTipText, "LookAndFeelMenuItem4.ico");
						}
						else if (menuItemText.equalsIgnoreCase("NormalColor"))
						{
							AppToolkit.menuItemInit(menuItem, Language.LookAndFeelMenuItem5Text, Language.LookAndFeelMenuItem5Text,
								Language.LookAndFeelMenuItem5ToolTipText, "LookAndFeelMenuItem5.ico");
						}
					}
					this.lookAndFeelItem = item;
					break;
				}
			}
		}
	}

	protected void setMenuBarCaption()
	{
		this.commandMenuBar.setTitle(Language.ManuBarTitle);
		this.commandMenuBar.setToolTipText(Language.ManuBarToolTipText);

		this.fileMenu.setText(Language.FileMenuText);
		this.fileMenu.setToolTipText(Language.FileMenuToolTipText);

		this.operateMenu.setText(Language.OperateMenuText);
		this.operateMenu.setToolTipText(Language.OperateMenuToolTipText);

		this.viewMenu.setText(Language.ViewMenuText);
		this.viewMenu.setToolTipText(Language.ViewMenuToolTipText);

		this.toolsMenu.setText(Language.ToolsMenuText);
		this.toolsMenu.setToolTipText(Language.ToolsMenuToolTipText);

		this.windowMenu.setText(Language.WindowMenuText);
		this.windowMenu.setToolTipText(Language.WindowMenuToolTipText);

		this.helpMenu.setText(Language.HelpMenuText);
		this.helpMenu.setToolTipText(Language.HelpMenuToolTipText);

		this.setLookAndFeelDockableBar();
	}

	private void fileMenuInit()
	{
		this.fileMenu.setMnemonic('F');

		this.connectButton.setMnemonic('C');
		this.fileMenu.add(this.connectButton);

		this.fileMenu.addSeparator();

		this.exitButton.setMnemonic('E');
		this.fileMenu.add(this.exitButton);
	}

	private void operateMenuInit()
	{
		this.operateMenu.setMnemonic('O');

		this.instrumentSelectionButton.setMnemonic('I');
		this.operateMenu.add(this.instrumentSelectionButton);

		this.accountSelectionButton.setMnemonic('A');
		this.operateMenu.add(this.accountSelectionButton);

		this.clearCancelledOrderButton.setMnemonic('L');
		this.operateMenu.add(this.clearCancelledOrderButton);

		this.assignButton.setMnemonic('A');
		this.operateMenu.add(this.assignButton);

		this.accountActiveButton.setMnemonic('O');
		this.operateMenu.add(this.accountActiveButton);

		this.changePasswordButton.setMnemonic('C');
		this.operateMenu.add(this.changePasswordButton);

		this.operateMenu.addSeparator();

		this.refreshButton.setMnemonic('R');
		this.operateMenu.add(this.refreshButton);
	}

	private void viewMenuInit()
	{
		this.viewMenu.setMnemonic('V');

		this.reportButton.setMnemonic('R');
		this.viewMenu.add(this.reportButton);

		//if(!this._settingsManager.get_IsForRSZQ())
		{
			this.chartButton.setMnemonic('C');
			this.viewMenu.add(this.chartButton);

			this.newsButton.setMnemonic('N');
			this.viewMenu.add(this.newsButton);
		}

		this.messageButton.setMnemonic('M');
		this.viewMenu.add(this.messageButton);

		this.marginButton.setMnemonic('G');
		this.viewMenu.add(this.marginButton);
		this.viewMenu.add(this.changeLeverageButton);

		this.downloadDocumentButton.setMnemonic('D');
		this.viewMenu.add(this.downloadDocumentButton);

		this.chatRoomButton.setMnemonic('A');
		this.viewMenu.add(this.chatRoomButton);
	}

	private void toolsMenuInit()
	{
		this.toolsMenu.setMnemonic('T');

		this.optionButton.setMnemonic('O');
		this.toolsMenu.add(this.optionButton);

		this.debugButton.setMnemonic('D');
		this.toolsMenu.add(this.debugButton);
	}

	protected void windowMenuInit()
	{
		this.windowMenu.removeAll();
		this.windowMenu.setMnemonic('W');
	}

	private void helpMenuInit()
	{
		this.helpMenu.setMnemonic('H');

		this.onlineHelpButton.setMnemonic('O');
		this.helpMenu.add(this.onlineHelpButton);
	}

	private void createInstrumentGridPanel()
	{
		this._tradingPanelListFrame = this.instrumentTable.get_DockableFrame();

		//ID
		this._tradingPanelListFrame.setKey("TradingPanelListFrame");

		//Titles
		this._tradingPanelListFrame.setTabTitle(Language.InstrumentViewPrompt);
		this._tradingPanelListFrame.setTitle(Language.InstrumentViewPrompt);
		this._tradingPanelListFrame.setSideTitle(Language.InstrumentViewPrompt);

		//Initial Layout
		this._tradingPanelListFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._tradingPanelListFrame.getContext().setFloatable(true);
		this._tradingPanelListFrame.getContext().setInitSide(DockContext.DOCK_SIDE_NORTH);
		this._tradingPanelListFrame.getContext().setInitIndex(2);
	}

	private void setScrollPaneBackground(Container container, Color backgroundColor)
	{
		for(Component component : container.getComponents())
		{
			if(component instanceof JScrollPane)
			{
				((JScrollPane)component).getViewport().setBackground(backgroundColor);
			}
			if(component instanceof Container)
			{
				this.setScrollPaneBackground((Container)component, backgroundColor);
			}
		}
	}

	private void createInstrumentSpanGridPanel()
	{
		this._tradingPanelGridFrame = this.instrument2Table.get_DockableFrame();

		//ID
		this._tradingPanelGridFrame.setKey("TradingPanelGridFrame");

		//Titles
		this._tradingPanelGridFrame.setTabTitle(Language.InstrumentView2Prompt);
		this._tradingPanelGridFrame.setTitle(Language.InstrumentView2Prompt);
		this._tradingPanelGridFrame.setSideTitle(Language.InstrumentView2Prompt);

		//Initial Layout
		this._tradingPanelGridFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._tradingPanelGridFrame.getContext().setFloatable(true);
		this._tradingPanelGridFrame.getContext().setInitSide(DockContext.DOCK_SIDE_NORTH);
		this._tradingPanelGridFrame.getContext().setInitIndex(2);
	}

	private void createAccountListPanel()
	{
		this._accountListFrame = new DockableFrame(Language.AccountListPrompt, this.getAsIcon("AccountStatus.ico"));
		this.accountListTable.setBorder(BorderFactory.createEmptyBorder());
		//this.accountListTable.setRowSelectionAllowed(false);
		this.accountListTable.setColumnSelectionAllowed(false);
		JScrollPane scrollPanel = new JScrollPane(this.accountListTable);
		this.accountListTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//this.accountListTable.setRowAutoResizes(true);
		this._accountListFrame.getContentPane().add(scrollPanel);

		//ID
		this._accountListFrame.setKey("AccountListFrame");

		//Titles
		this._accountListFrame.setTabTitle(Language.AccountListPrompt);
		this._accountListFrame.setTitle(Language.AccountListPrompt);
		this._accountListFrame.setSideTitle(Language.AccountListPrompt);

		//Initial Layout
		this._accountListFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._accountListFrame.getContext().setFloatable(true);
		this._accountListFrame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
		this._accountListFrame.getContext().setInitIndex(3);
	}

	private void createAccountStatusPanel()
	{
		this._accountStatusFrame = new DockableFrame(Language.AccountStatusPrompt, this.getAsIcon("AccountStatus.ico"));
		this.accountStatusTable.setBorder(BorderFactory.createEmptyBorder());
		this.accountStatusTable.setRowSelectionAllowed(false);
		this.accountStatusTable.setColumnSelectionAllowed(false);
		JScrollPane scrollPanel = new JScrollPane(this.accountStatusTable);
		this.accountStatusTable.setRowAutoResizes(true);
		this._accountStatusFrame.getContentPane().add(scrollPanel);

		//ID
		this._accountStatusFrame.setKey("AccountStatusFrame");

		//Titles
		this._accountStatusFrame.setTabTitle(Language.AccountStatusPrompt);
		this._accountStatusFrame.setTitle(Language.AccountStatusPrompt);
		this._accountStatusFrame.setSideTitle(Language.AccountStatusPrompt);

		//Initial Layout
		this._accountStatusFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._accountStatusFrame.getContext().setFloatable(true);
		this._accountStatusFrame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
		this._accountStatusFrame.getContext().setInitIndex(3);
	}

	private void createPositionSummaryPanel()
	{
		this._positionSummaryFrame = this.summaryTable.get_DockableFrame();
		if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
		{
			//ID
			this._positionSummaryFrame.setKey("PositionSummaryFrame");

			//Titles
			this._positionSummaryFrame.setTabTitle(Language.SummaryPrompt);
			this._positionSummaryFrame.setTitle(Language.SummaryPrompt);
			this._positionSummaryFrame.setSideTitle(Language.SummaryPrompt);

			//Initial Layout
			this._positionSummaryFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
			this._positionSummaryFrame.getContext().setFloatable(true);
			this._positionSummaryFrame.getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
			this._positionSummaryFrame.getContext().setInitIndex(3);
		}
	}

	private void createLogPanel()
	{
		this._logFrame = this.logTable.get_DockableFrame();

		//ID
		this._logFrame.setKey("LogFrame");

		//Titles
		this._logFrame.setTabTitle(Language.LogPrompt);
		this._logFrame.setTitle(Language.LogPrompt);
		this._logFrame.setSideTitle(Language.LogPrompt);

		//Initial Layout
		this._logFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._logFrame.getContext().setFloatable(true);
		this._logFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._logFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);
	}

	private void createMessagePanel()
	{
		this._messageFrame = this.messageTable.get_DockableFrame();

		//ID
		this._messageFrame.setKey("MessageFrame");

		//Titles
		this._messageFrame.setTabTitle(Language.messageFormTitle);
		this._messageFrame.setTitle(Language.messageFormTitle);
		this._messageFrame.setSideTitle(Language.messageFormTitle);

		//Initial Layout
		this._messageFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._messageFrame.getContext().setFloatable(true);
		this._messageFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._messageFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);
	}

	private void createNewsPanel()
	{
		this._newsFrame = this.newsTable.get_DockableFrame();

		//ID
		this._newsFrame.setKey("NewsFrame");

		//Titles
		this._newsFrame.setTabTitle(Language.newsFormTitle);
		this._newsFrame.setTitle(Language.newsFormTitle);
		this._newsFrame.setSideTitle(Language.newsFormTitle);

		//Initial Layout
		this._newsFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._newsFrame.getContext().setFloatable(true);
		this._newsFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._newsFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);
	}

	public void showNews(boolean needShow)
	{
		if (!needShow/* || this._settingsManager.get_IsForRSZQ()*/)
		{
			this.getDockingManager().hideFrame("NewsFrame");
		}
		else
		{
			this.getDockingManager().showFrame("NewsFrame");
		}
	}

	public void setVisibleOfInstrumentSpanGrid(boolean showInstrumentSpanGrid)
	{
		if (!showInstrumentSpanGrid)
		{
			this.getDockingManager().removeFrame(this._tradingPanelGridFrame.getKey());
		}
		else
		{
			if (this.getDockingManager().getFrame(this._tradingPanelGridFrame.getKey()) == null)
			{
				this.getDockingManager().addFrame(this._tradingPanelGridFrame);
			}
			this.getDockingManager().showFrame(this._tradingPanelGridFrame.getKey());
		}
	}

	public void showLog(boolean needShow)
	{
		if (this._logFrame != null)
		{
			if (needShow)
			{
				if (this.getDockingManager().getFrame(this._logFrame.getKey()) == null)
				{
					this.getDockingManager().addFrame(this._logFrame);
				}

				this.getDockingManager().showFrame(this._logFrame.getKey());
			}
			else if (this.getDockingManager().getFrame(this._logFrame.getKey()) != null)
			{
				this.getDockingManager().hideFrame(this._logFrame.getKey());
			}
		}
	}

	private void createWorkingOrderListPanel()
	{
		this._workingOrderListFrame = this.orderTable.get_DockableFrame();

		//ID
		this._workingOrderListFrame.setKey("WorkingOrderListFrame");

		//Titles
		this._workingOrderListFrame.setTabTitle(Language.SettingOrderGrid);
		this._workingOrderListFrame.setTitle(Language.SettingOrderGrid);
		this._workingOrderListFrame.setSideTitle(Language.SettingOrderGrid);

		//Initial Layout
		this._workingOrderListFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._workingOrderListFrame.getContext().setFloatable(true);
		this._workingOrderListFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._workingOrderListFrame.getContext().setInitIndex(4);
	}

	protected OrderQueryPanel queryPanel;
	private void createOrderQueryPanel()
	{
		this._orderQueryFrame = new DockableFrame(Language.Query, this.getAsIcon("Query.ico"));
		this._orderQueryFrame.getContentPane().add(queryPanel);
		//this._openOrderListFrame = this.openOrderTable.get_DockableFrame();

		//ID
		this._orderQueryFrame.setKey("QueryOrderListFrame");

		//Titles
		this._orderQueryFrame.setTabTitle(Language.Query);
		this._orderQueryFrame.setTitle(Language.Query);
		this._orderQueryFrame.setSideTitle(Language.Query);

		//Initial Layout
		this._orderQueryFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._orderQueryFrame.getContext().setFloatable(true);
		this._orderQueryFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._orderQueryFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);
	}

	JRadioButton inventoryButton = new JRadioButton(Language.PhysicalInventory);
	JRadioButton pendingInventoryButton = new JRadioButton(Language.PhysicalPendingInventory);
	JRadioButton shortSellButton = new JRadioButton(Language.PhysicalShortSell);
	Font unselectedFont = new Font("SansSerif", Font.PLAIN, 12);
	Font selectedFont = new Font("SansSerif", Font.BOLD, 12);
	JScrollPane inventoryScrollPane = null, pendingInventoryScrollPane = null, shortSellScrollPane = null;
	private void handleRadioButtonStateChangedEventInPhysicalPanel()
	{
		if(inventoryButton.isSelected())
		{
			inventoryButton.setFont(selectedFont);
			this.inventoryScrollPane.setVisible(true);

			pendingInventoryButton.setFont(unselectedFont);
			this.pendingInventoryScrollPane.setVisible(false);

			shortSellButton.setFont(unselectedFont);
			this.shortSellScrollPane.setVisible(false);
		}
		else if (pendingInventoryButton.isSelected())
		{
			inventoryButton.setFont(unselectedFont);
			this.inventoryScrollPane.setVisible(false);

			pendingInventoryButton.setFont(selectedFont);
			this.pendingInventoryScrollPane.setVisible(true);

			shortSellButton.setFont(unselectedFont);
			this.shortSellScrollPane.setVisible(false);
		}
		else if (shortSellButton.isSelected())
		{
			inventoryButton.setFont(unselectedFont);
			this.inventoryScrollPane.setVisible(false);

			pendingInventoryButton.setFont(unselectedFont);
			this.pendingInventoryScrollPane.setVisible(false);

			shortSellButton.setFont(selectedFont);
			this.shortSellScrollPane.setVisible(true);
		}
	}

	private void createPhysicalPanel()
	{
		this._physicalFrame = new DockableFrame(Language.PhysicalStok, this.getAsIcon("PhysicalStok.ico"));
		this._physicalFrame.getContentPane().setLayout(new GridBagLayout());
		//JPanel panel = new JPanel();
		//panel.setLayout(new GridBagLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		ButtonGroup group = new ButtonGroup();
		group.add(inventoryButton);
		group.add(pendingInventoryButton);
		group.add(shortSellButton);
		ItemListener itemListener = new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				handleRadioButtonStateChangedEventInPhysicalPanel();
			}
		};
		inventoryButton.addItemListener(itemListener);
		pendingInventoryButton.addItemListener(itemListener);
		shortSellButton.addItemListener(itemListener);

		buttonPanel.add(inventoryButton, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 60, 0));
		buttonPanel.add(pendingInventoryButton, new GridBagConstraints2(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 60, 0));
		buttonPanel.add(shortSellButton, new GridBagConstraints2(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 60, 0));
		buttonPanel.add(new JPanel(), new GridBagConstraints2(3, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 1, 0, 0), 60, 0));

		this._physicalFrame.getContentPane().add(buttonPanel, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 12));

		this.inventoryScrollPane = new JScrollPane(this.physicalInventoryTable);
		this.pendingInventoryScrollPane = new JScrollPane(this.physicalPendingInventoryTable);
		this.shortSellScrollPane = new JScrollPane(this.physicalShotSellTable);

		this._physicalFrame.getContentPane().add(this.inventoryScrollPane, new GridBagConstraints2(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		this._physicalFrame.getContentPane().add(this.pendingInventoryScrollPane, new GridBagConstraints2(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		this._physicalFrame.getContentPane().add(this.shortSellScrollPane, new GridBagConstraints2(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		//this._physicalFrame.getContentPane().add(panel);

		this._physicalFrame.setKey("PhysicalStokFrame");

		//Titles
		this._physicalFrame.setTabTitle(Language.PhysicalStok);
		this._physicalFrame.setTitle(Language.PhysicalStok);
		this._physicalFrame.setSideTitle(Language.PhysicalStok);

		//Initial Layout
		this._physicalFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._physicalFrame.getContext().setFloatable(true);
		this._physicalFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._physicalFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);

		inventoryButton.setSelected(true);
	}

	private void createOpenOrderListPanel()
	{
		this._openOrderListFrame = new DockableFrame(Language.SettingOpenOrderGrid, this.getAsIcon("OpenOrderList.ico"));
		JScrollPane scrollPane = new JScrollPane(this.openOrderTable);
		scrollPane.addMouseListener(new OpenOrderMouseListener(this.openOrderTable));

		scrollPane.getViewport().putClientProperty("HierarchicalTable.mainViewport", Boolean.TRUE);
		this.openOrderTable.setOptimized(true);
		this._openOrderListFrame.getContentPane().add(scrollPane);
		//this._openOrderListFrame = this.openOrderTable.get_DockableFrame();

		//ID
		this._openOrderListFrame.setKey("OpenOrderListFrame");

		//Titles
		this._openOrderListFrame.setTabTitle(Language.SettingOpenOrderGrid);
		this._openOrderListFrame.setTitle(Language.SettingOpenOrderGrid);
		this._openOrderListFrame.setSideTitle(Language.SettingOpenOrderGrid);

		//Initial Layout
		this._openOrderListFrame.getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
		this._openOrderListFrame.getContext().setFloatable(true);
		this._openOrderListFrame.getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
		this._openOrderListFrame.getContext().setInitIndex(this._settingsManager.get_IsForRSZQ() ? 5 : 4);
	}

	private static class OpenOrderMouseListener implements java.awt.event.MouseListener
	{
		private DataGrid _owner;

		public OpenOrderMouseListener(DataGrid owner)
		{
			this._owner = owner;
		}

		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				if (e.getClickCount() == 1)
				{
					this._owner.clearSelection();
				}
			}
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
	}

	private void jbInit() throws Exception
	{
		this.initializeGrids();

		this.addWindowListener(new MainFormBase_this_windowAdapter(this));
		//this.setResizable(true);
		this.addComponentListener(new MainFormBase_this_componentAdapter(this));
		this.setTitle(Language.mainFormTitle);
		this.setBackground(FormBackColor.mainForm);

		appTimeStaticText.setText("2006-08-01 12:30:00");
		appTimeStaticText.setAlignment(1);

		loginInformationStaticText.setText("Michael login at 2006-08-01 12:00:00");
		loginInformationStaticText.setAlignment(1);

		statusStaticText.setText("Disconnection");
		statusStaticText.setAlignment(0);

		logoButton.setText("");

		this.eventSetting();
	}

	public void initializeGrids() throws Exception
	{
		ColumnUIInfoManager.clear();

		this.instrumentTable = new tradingConsole.ui.grid.DockableTable(GridNames.InstrumentGrid, Language.InstrumentViewPrompt,
			this.getAsIcon("TradingPanelList.ico"));
		if(ColorSettings.useBlackAsBackground)
		{
			this.instrumentTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.instrumentTable.enableRowStripe();
		}
		this.instrumentTable.setEditable(true);
		this.instrumentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.instrumentTable.enableChooseColumn();
		this.instrumentTable.enableColumnUIPersistent();

		this.instrument2Table = new InstrumentSpanGrid(Language.InstrumentView2Prompt, this.getAsIcon("TradingPanelGrid.ico"));

		this.orderTable = new tradingConsole.ui.grid.DockableTable(GridNames.OrderGrid, Language.SettingOrderGrid, this.getAsIcon("WorkingOrderList.ico"));
		if(ColorSettings.useBlackAsBackground)
		{
			this.orderTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.orderTable.enableRowStripe();
		}
		this.orderTable.setEditable(false);
		this.orderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.orderTable.enalbeHierarchicalTableMainViewport();
		this.orderTable.enableColumnUIPersistent();

		this.physicalInventoryTable = new tradingConsole.ui.grid.DataGrid(GridNames.PhysicalInventory);
		this.physicalInventoryTable.setBorder(BorderFactory.createEmptyBorder());
		this.physicalInventoryTable.setRowSelectionAllowed(false);
		this.physicalInventoryTable.setColumnSelectionAllowed(false);
		this.physicalInventoryTable.setEditable(false);
		//this.physicalInventoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.physicalInventoryTable.setRowAutoResizes(true);
		//this.physicalInventoryTable.enableChooseColumn();
		this.physicalInventoryTable.setOptimized(true);

		this.physicalPendingInventoryTable = new tradingConsole.ui.grid.DataGrid(GridNames.PhysicalPendingInventory);
		this.physicalShotSellTable = new tradingConsole.ui.grid.DataGrid(GridNames.PhysicalShotSell);
		if(ColorSettings.useBlackAsBackground)
		{
			this.physicalInventoryTable.setBackground(ColorSettings.PhysicalFrameBackground);
			this.physicalInventoryTable.setGridColor(ColorSettings.PhysicalFrameBackground);
			this.physicalInventoryTable.enableRowStripe(ColorSettings.GridStripeColors);

			this.physicalPendingInventoryTable.setBackground(ColorSettings.PhysicalFrameBackground);
			this.physicalPendingInventoryTable.setGridColor(ColorSettings.PhysicalFrameBackground);
			this.physicalPendingInventoryTable.enableRowStripe(ColorSettings.GridStripeColors);

			this.physicalShotSellTable.setBackground(ColorSettings.PhysicalFrameBackground);
			this.physicalShotSellTable.setGridColor(ColorSettings.PhysicalFrameBackground);
			this.physicalShotSellTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.physicalInventoryTable.enableRowStripe();
			this.physicalPendingInventoryTable.enableRowStripe();
			this.physicalShotSellTable.enableRowStripe();
		}

		this.openOrderTable = new tradingConsole.ui.grid.DataGrid(GridNames.OpenOrderGrid);
		this.openOrderTable.setEditable(true);
		if(ColorSettings.useBlackAsBackground)
		{
			this.openOrderTable.enableRowStripe(ColorSettings.GridStripeColors);
			this.openOrderTable.setGridColor(ColorSettings.OpenOrderListGridBackground);
		}
		else
		{
			this.openOrderTable.enableRowStripe();
		}

		this.openOrderTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//this.openOrderTable.enableChooseColumn();
		this.openOrderTable.enableColumnUIPersistent();

		HierarchicalOpenOrderTableComponentFactory hierarchicalTableComponentFactory = new HierarchicalOpenOrderTableComponentFactory();
		this.openOrderTable.setComponentFactory(hierarchicalTableComponentFactory);

		this.summaryTable = new tradingConsole.ui.grid.DockableTable(GridNames.SummaryGrid, Language.SummaryPrompt, this.getAsIcon("PositionSummary.ico"));

		if(ColorSettings.useBlackAsBackground)
		{
			this.summaryTable.setBackground(ColorSettings.PositionSummaryFrameBackground);
			this.summaryTable.setGridColor(ColorSettings.PositionSummaryFrameBackground);
			this.summaryTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.summaryTable.enableRowStripe();
		}

		this.summaryTable.setEditable(false);
		this.summaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.summaryTable.enableColumnUIPersistent();
		this.summaryTable.setOptimized(true);
		this.summaryTable.enalbeHierarchicalTableMainViewport();
		hierarchicalTableComponentFactory = new HierarchicalOpenOrderTableComponentFactory(new MainForm_summaryTable_actionAdapter(this));
		this.summaryTable.setComponentFactory(hierarchicalTableComponentFactory);

		this.logTable = new tradingConsole.ui.grid.DockableTable(GridNames.LogGrid, Language.LogPrompt, this.getAsIcon("Log.ico"));
		if(ColorSettings.useBlackAsBackground)
		{
			this.logTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.logTable.enableRowStripe();
		}

		this.logTable.setEditable(false);
		this.logTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.logTable.enableColumnUIPersistent();

		this.newsTable = new tradingConsole.ui.grid.DockableTable(GridNames.NewsGrid, Language.newsFormTitle, this.getAsIcon("News.ico"));
		if(ColorSettings.useBlackAsBackground)
		{
			this.newsTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.newsTable.enableRowStripe();
		}

		this.newsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.newsTable.setEditable(false);
		this.newsTable.enableColumnUIPersistent();

		this.messageTable = new tradingConsole.ui.grid.DockableTable(GridNames.MessageGrid, Language.messageFormTitle, this.getAsIcon("Message.ico"));
		if(ColorSettings.useBlackAsBackground)
		{
			this.messageTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.messageTable.enableRowStripe();
		}

		this.messageTable.setEditable(false);
		this.messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.messageTable.enableColumnUIPersistent();

		if(ColorSettings.useBlackAsBackground)
		{
			this.accountListTable.enableRowStripe(ColorSettings.GridStripeColors);
			this.accountStatusTable.enableRowStripe(ColorSettings.GridStripeColors);
		}
		else
		{
			this.accountListTable.enableRowStripe();
			this.accountStatusTable.enableRowStripe();
		}

		instrument2Table.addActionListener(new MainFormBase_instrument2Table_actionAdapter(this));
		summaryTable.get_DockableFrame().addComponentListener(new MainFormBase_summaryTable_componentAdapter(this));
		logTable.get_DockableFrame().addComponentListener(new MainFormBase_logTable_componentAdapter(this));
		newsTable.get_DockableFrame().addComponentListener(new MainFormBase_newsTable_componentAdapter(this));
		newsTable.addActionListener(new MainFormBase_newsTable_actionAdapter(this));
		messageTable.get_DockableFrame().addComponentListener(new MainFormBase_messageTable_componentAdapter(this));
		messageTable.addActionListener(new MainFormBase_messageTable_actionAdapter(this));

		this.instrumentTable.addActionListener(new MainForm_instrumentTable_actionAdapter(this));
		this.summaryTable.addActionListener(new MainForm_summaryTable_actionAdapter(this));
		this.accountStatusTable.addActionListener(new MainForm_accountTree_actionAdapter(this));
		this.orderTable.addActionListener(new MainForm_orderTable_actionAdapter(this));
		this.openOrderTable.addActionListener(new MainForm_openOrderTable_actionAdapter(this));
		this.queryPanel.get_NotConfirmedPendingOrderTable().addActionListener(new MainForm_queryOrderTable_actionAdapter(this));
		this.physicalInventoryTable.addActionListener(this._physicalInventoryTableActionListener);
		this.physicalShotSellTable.addActionListener(new IActionListener()
		{
			public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
			{
				processPhysicalShortSellTable(e);
			}
		});
	}

	private void eventSetting()
	{
		connectButton.addActionListener(new MainFormBase_connectButton_actionAdapter(this));
		instrumentSelectionButton.addActionListener(new MainForm_instrumentSelectionButton_actionAdapter(this));
		accountSelectionButton.addActionListener(new MainForm_accountSelectionButton_actionAdapter(this));
		downloadDocumentButton.addActionListener(new MainForm_downloadDocumentButton_actionAdapter(this));
		clearCancelledOrderButton.addActionListener(new MainForm_clearCancelledOrderButton_actionAdapter(this));
		marginButton.addActionListener(new MainForm_marginButton_actionAdapter(this));
		changeLeverageButton.addActionListener(new MainForm_changeLeverageButton_actionAdapter(this));
		reportButton.addActionListener(new MainForm_reportButton_actionAdapter(this));
		optionButton.addActionListener(new MainForm_optionButton_actionAdapter(this));
		//messageButton.addActionListener(new MainForm_messageButton_actionAdapter(this));
		chatRoomButton.addActionListener(new MainForm_chatRoomButton_actionAdapter(this));
		chartButton.addActionListener(new MainForm_chartButton_actionAdapter(this));
		assignButton.addActionListener(new MainForm_assignButton_actionAdapter(this));
		accountActiveButton.addActionListener(new MainForm_accountActiveButton_actionAdapter(this));
		changePasswordButton.addActionListener(new MainForm_changePasswordButton_actionAdapter(this));
		newsButton.addActionListener(new MainForm_newsButton_actionAdapter(this));
		refreshButton.addActionListener(new MainForm_refreshButton_actionAdapter(this));
		onlineHelpButton.addActionListener(new MainForm_onlineHelpButton_actionAdapter(this));
		debugButton.addActionListener(new MainForm_debugButton_actionAdapter(this));
		exitButton.addActionListener(new MainForm_exitButton_actionAdapter(this));
		this.resetLayoutButton.addActionListener(new MainFormBase_resetLayoutButton_actionAdapter(this));

		connectButton2.addActionListener(new MainFormBase_connectButton_actionAdapter(this));
		instrumentSelectionButton2.addActionListener(new MainForm_instrumentSelectionButton_actionAdapter(this));
		accountSelectionButton2.addActionListener(new MainForm_accountSelectionButton_actionAdapter(this));
		downloadDocumentButton2.addActionListener(new MainForm_downloadDocumentButton_actionAdapter(this));
		clearCancelledOrderButton2.addActionListener(new MainForm_clearCancelledOrderButton_actionAdapter(this));
		marginButton2.addActionListener(new MainForm_marginButton_actionAdapter(this));
		changeLeverageButton2.addActionListener(new MainForm_changeLeverageButton_actionAdapter(this));
		reportButton2.addActionListener(new MainForm_reportButton_actionAdapter(this));
		optionButton2.addActionListener(new MainForm_optionButton_actionAdapter(this));
		//messageButton2.addActionListener(new MainForm_messageButton_actionAdapter(this));
		chatRoomButton2.addActionListener(new MainForm_chatRoomButton_actionAdapter(this));
		chartButton2.addActionListener(new MainForm_chartButton_actionAdapter(this));
		assignButton2.addActionListener(new MainForm_assignButton_actionAdapter(this));
		accountActiveButton2.addActionListener(new MainForm_accountActiveButton_actionAdapter(this));
		changePasswordButton2.addActionListener(new MainForm_changePasswordButton_actionAdapter(this));
		newsButton2.addActionListener(new MainForm_newsButton_actionAdapter(this));
		refreshButton2.addActionListener(new MainForm_refreshButton_actionAdapter(this));
		onlineHelpButton2.addActionListener(new MainForm_onlineHelpButton_actionAdapter(this));
		debugButton2.addActionListener(new MainForm_debugButton_actionAdapter(this));
		exitButton2.addActionListener(new MainForm_exitButton_actionAdapter(this));
		this.resetLayoutButton2.addActionListener(new MainFormBase_resetLayoutButton_actionAdapter(this));

		this.tradingFactButton.addActionListener(new MainFormBase_tradingFactButton_actionAdapter(this));
		this.dumpButton.addActionListener(new MainFormBase_dumpButton_actionAdapter(this));
		this.floatingFramesButton.addActionListener(new MainFormBase_floatFramesButton_actionAdapter(this));
	}

	protected tradingConsole.ui.grid.DockableTable summaryTable;
	protected tradingConsole.ui.grid.DockableTable logTable;
	protected tradingConsole.ui.grid.DockableTable newsTable;
	protected tradingConsole.ui.grid.DockableTable messageTable;
	protected DataGrid accountStatusTable;
	protected DataGrid accountListTable;

	protected tradingConsole.ui.grid.DockableTable instrumentTable;
	protected InstrumentSpanGrid instrument2Table;

	protected tradingConsole.ui.grid.DockableTable orderTable;
	protected tradingConsole.ui.grid.DataGrid openOrderTable;
	protected PVStaticText2 appTimeStaticText;
	protected PVStaticText2 loginInformationStaticText;
	protected PVStaticText2 statusStaticText;
	protected PVButton2 logoButton;

	protected tradingConsole.ui.grid.DataGrid physicalInventoryTable;
	protected tradingConsole.ui.grid.DataGrid physicalPendingInventoryTable;
	protected tradingConsole.ui.grid.DataGrid physicalShotSellTable;

	protected CommandBar commandBar;
	protected JideToggleButton connectButton2;
	protected JideToggleButton exitButton2;

	protected JideToggleButton instrumentSelectionButton2;
	protected JideToggleButton accountSelectionButton2;
	protected JideToggleButton clearCancelledOrderButton2;
	protected JideToggleButton assignButton2;
	protected JideToggleButton accountActiveButton2;
	protected JideToggleButton changePasswordButton2;
	protected JideToggleButton refreshButton2;

	protected JideToggleButton reportButton2;
	protected JideToggleButton chartButton2;
	protected JideToggleButton newsButton2;
	protected JideToggleButton messageButton2;
	protected JideToggleButton marginButton2;
	protected JideToggleButton changeLeverageButton2;
	protected JideToggleButton downloadDocumentButton2;
	protected JideToggleButton chatRoomButton2;

	protected JideToggleButton optionButton2;
	protected JideToggleButton debugButton2;

	protected JideToggleButton resetLayoutButton2;
	protected JideToggleButton tradingFactButton;
	protected JideToggleButton dumpButton;
	protected JideToggleButton floatingFramesButton;

	protected JideToggleButton onlineHelpButton2;

	//Menu Bar
	protected CommandBar commandMenuBar;
	protected JMenu fileMenu;
	protected JMenuItem connectButton;
	protected JMenuItem exitButton;

	protected JMenu operateMenu;
	protected JMenuItem instrumentSelectionButton;
	protected JMenuItem accountSelectionButton;
	protected JMenuItem clearCancelledOrderButton;
	protected JMenuItem assignButton;
	protected JMenuItem accountActiveButton;
	protected JMenuItem changePasswordButton;
	protected JMenuItem refreshButton;

	protected JMenu viewMenu;
	protected JMenuItem reportButton;
	protected JMenuItem chartButton;
	protected JMenuItem newsButton;
	protected JMenuItem messageButton;
	protected JMenuItem marginButton;
	protected JMenuItem changeLeverageButton;
	protected JMenuItem downloadDocumentButton;
	protected JMenuItem chatRoomButton;

	protected JMenu toolsMenu;
	protected JMenuItem optionButton;
	protected JMenuItem debugButton;

	protected JMenu windowMenu;
	protected JMenuItem resetLayoutButton;

	protected JMenu helpMenu;
	protected JMenuItem onlineHelpButton;

	private JideSplitButton lookAndFeelItem;

	public void this_windowClosing(WindowEvent e)
	{
		this.exitSystem();
	}

	protected void exitSystem()
	{
		System.exit(0);
	}

	class MainFormBase_this_windowAdapter extends WindowAdapter
	{
		private MainFormBase adaptee;
		MainFormBase_this_windowAdapter(MainFormBase adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}

		public void windowOpened(WindowEvent e)
		{
			adaptee.this_windowOpened(e);
		}

		public void windowActivated(WindowEvent e)
		{
			adaptee.this_windowActivated(e);
		}

		public void windowDeactivated(WindowEvent e)
		{
			adaptee.this_windowDeactivated(e);
		}
	}

	protected void processSummaryGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void processInstrumentGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void processInstrumentSpanGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void processAccountTree(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void processOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void processPhysicalInventoryTable(tradingConsole.ui.grid.ActionEvent e){}

	protected void processPhysicalShortSellTable(tradingConsole.ui.grid.ActionEvent e){}

	protected void processOpenOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{}

	protected void menuProcess(MenuType menuType)
	{}

	protected void resetLayout(boolean removeCustomerLayout)
	{}

	protected void instrument2TableAdjustmentValueChanged()
	{
	}

	protected JFrame _tradingFactFrame = null;
	private HTMLViewer _tradingFactHTMLViewer = null;
	private void showTradingFact()
	{
		try
		{
			if (this._tradingFactFrame == null)
			{
				this._tradingFactFrame = new JFrame(Language.tradingFactButtonCaption);
				this._tradingFactFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				this._tradingFactHTMLViewer = new HTMLViewer();
				this._tradingFactFrame.add(this._tradingFactHTMLViewer);

				Rectangle rectangle = AppToolkit.getRectangleByDimension(AppToolkit.get_ScreenSize());
				int width = (int) (rectangle.getWidth() * 0.8);
				int height = (int) (rectangle.getHeight() * 0.8);
				this._tradingFactFrame.setSize(width, height);
				this._tradingFactFrame.setIconImage(AppToolkit.getImage("TradingFact.gif"));
				JideSwingUtilities.centerWindow(this._tradingFactFrame);
			}
			this._tradingFactFrame.setVisible(true);
			this._tradingFactFrame.toFront();
			URL url = new URL(Parameter.tradingFactUrl);
			this._tradingFactHTMLViewer.setUrl(url);
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, ex);
			AlertDialogForm.showDialog(this, null, true, Language.CannotShowTradingFact);
		}
	}

	public void instrumentSelectionButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.InstrumentSelection);
	}

	public void accountSelectionButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.AccountSelection);
	}

	public void instrument2Table_adjustmentValueChanged(AdjustmentEvent e)
	{
		this.instrument2TableAdjustmentValueChanged();
	}

	public void changePasswordButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.ChangePassword);
	}

	public void chartButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Chart);
	}

	public void reportButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Report);
	}

	public void marginButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Margin);
	}

	public void changeLeverageButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.ChangeLeverage);
	}

	public void clearCancelledOrderButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.ClearCancelledOrder);
	}

	public void downloadDocumentButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.DownloadDocument);
	}

	public void optionButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Option);
	}

	public void chatRoomButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.ChatRoom);
	}

	public void refreshButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.RefreshSystem);
	}

	public void onlineHelpButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.OnlineHelp);
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.ExitSystem);
	}

	public void resetLayoutButton_actionPerformed(ActionEvent e)
	{
		this.resetLayout(true);
	}

	public void tradingFactButton_actionPerformed(ActionEvent e)
	{
		this.showTradingFact();
	}

	private DateTime _lastDumpTime = null;
	public void dumpButton_actionPerformed(ActionEvent e)
	{
		DateTime now = this._owner.get_TradingConsoleServer().appTime();
		if(this._lastDumpTime == null || now.substract(this._lastDumpTime).get_TotalSeconds() > 5)
		{
			this._lastDumpTime = now;
			this.dump();
			AlertDialogForm.showDialog(this, null, true, Language.complainSuccessfully);
		}
	}

	private void dump()
	{
		DateTime now = DateTime.get_Now();
		String filePath = DirectoryHelper.combine(AppToolkit.get_DumpDirectory(), now.toString("HH-mm-ss") + ".txt");
		File dumpingFile = new File(filePath);
		if (dumpingFile.exists())
		{
			dumpingFile.delete();
		}

		try
		{
			dumpingFile.createNewFile();
		}
		catch (IOException ex1)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Error to create dumping file " + filePath);
			return;
		}

		PrintWriter dumpingWriter = null;
		try
		{
			/*dumpingWriter = new PrintWriter(dumpingFile);
			dumpingWriter.println(DateTime.get_Now().toString(DateTime.fullFormat));
			this.dumpOpenOrders(dumpingWriter);
			this._owner.dumpOrders(dumpingWriter);			*/

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			dumpingWriter = new PrintWriter(outputStream);
			this.dumpOpenOrders(dumpingWriter);
			this._owner.dumpOrders(dumpingWriter);
			outputStream.close();
			dumpingWriter.flush();
			String complaint = new String(outputStream.toByteArray());
			String loginName = this._owner.get_LoginInformation().get_LoginName();
			this._owner.get_TradingConsoleServer().complain(loginName, complaint);
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Error to open dumping file " + filePath);
		}
		finally
		{
			if (dumpingWriter != null)
			{
				try
				{
					dumpingWriter.close();
				}
				catch (Exception ex2)
				{
				}
			}
		}
	}

	private void dumpOpenOrders(PrintWriter dumpingWriter)
	{
		if (this.openOrderTable != null && this.openOrderTable.get_BindingSource() != null)
		{
			dumpingWriter.println();
			dumpingWriter.println("----------------------Order in open order grid-----------------------");
			dumpingWriter.println("Row count = " + this.openOrderTable.getRowCount());
			for (int row = 0; row < this.openOrderTable.getRowCount(); row++)
			{
				Order order = (Order)this.openOrderTable.getObject(row);
				order.dump(dumpingWriter);
			}
			dumpingWriter.println("------------------------------------------------------------------------------------");

			dumpingWriter.println();
			dumpingWriter.println("-------------------Order in biding source of open order grid--------------------------");
			dumpingWriter.println("Row count = " + this.openOrderTable.get_BindingSource().getRowCount());
			for (int row = 0; row < this.openOrderTable.get_BindingSource().getRowCount(); row++)
			{
				Order order = (Order)this.openOrderTable.get_BindingSource().getObject(row);
				order.dump(dumpingWriter);
			}
			dumpingWriter.println("---------------------------------------------------------------------------------");
		}
	}

	public boolean get_FloatingChildFrames()
	{
		return this._floatingChildFrames;
	}

	public void set_FloatingChildFrames(boolean floatingChildFrames)
	{
		this._floatingChildFrames = floatingChildFrames;
		//this.setResizable(true);
		//this.setResizable(!this._floatingChildFrames);
	}

	public void floatFramesButton_actionPerformed(ActionEvent e)
	{
		this.closeAllOpenedCharts();

		this.setResizable(true);
		this.setLocation(0,0);
		Rectangle maxSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		this.setSize(maxSize.width, maxSize.height);

		boolean isFirstChartHidden = /*this._settingsManager.get_IsForRSZQ() || */this.getDockingManager().getFrame("ChartFrame1").isHidden();
		boolean isNewsFrameHidden = /*this._settingsManager.get_IsForRSZQ() || */this.getDockingManager().getFrame("NewsFrame").isHidden();
		this._floatingChildFrames = !this._floatingChildFrames;
		if (!this._floatingChildFrames)
		{
			for (String key : this.getDockingManager().getAllFrames())
			{
				DockableFrame frame = this.getDockingManager().getFrame(key);
				/*if(this._settingsManager.get_IsForRSZQ())
				{
					if(key.startsWith("ChartFrame") || key.equals("NewsFrame")) continue;
				}*/
				this.getDockingManager().dockFrame(key, frame.getInitSide(), frame.getInitIndex());
			}
			this._owner.resetLayout(false);
		}
		else
		{
			this.resetLayout(false);
		}

		/*if(this._settingsManager.get_IsForRSZQ())
		{
			for (String key : this.getDockingManager().getAllFrames())
			{
				if(key.startsWith("ChartFrame") || key.equalsIgnoreCase("NewsFrame"))
				{
					this.getDockingManager().hideFrame(key);
				}
			}
		}*/
		if(isFirstChartHidden)
		{
			this.getDockingManager().hideFrame("ChartFrame1");
			if(this._floatingChildFrames)
			{
				if (this.getDockingManager().getFrame("TradingPanelListFrame").isVisible())
				{
					//this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame", false), false);
				}
			}
		}
		if(isNewsFrameHidden)
		{
			this.getDockingManager().hideFrame("NewsFrame");
		}
		//this.setResizable(!this._floatingChildFrames);
	}

	private void closeAllOpenedCharts()
	{
		int chartCount = this._chartPanelKey;
		for (int index = 2; index <= chartCount; index++)
		{
			this.getDockingManager().removeFrame("ChartFrame" + index);
			String key = Convert.toString(index);
			if(this.getDockingManager().getAllFrames().contains(key))
			{
				this._owner.get_ChartManager().destoryChartPanel(this._chartPanels.get(key));
			}
			if(this._chartPanels.containsKey(key))
			{
				this._chartPanels.remove(key);
			}
		}
		this._chartPanelKey = 1;
	}

	public void assignButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Assign);
	}

	public void accountActiveButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.AccountActive);
	}

	public void instrumentTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processInstrumentGridActionEvent(e);
	}

	public void summaryTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processSummaryGridActionEvent(e);
	}

	public void instrument2Table_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processInstrumentSpanGridActionEvent(e);
	}

	public void accountTree_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processAccountTree(e);
	}

	public void orderTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processOrderTable(e);
	}

	public void openOrderTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processOpenOrderTable(e);
	}

	public void downloadDocumentButton_mouseClicked(MouseEvent e)
	{
	}

	public void debugButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Debug);
	}

	public void newsButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.News);
	}

	public void connectButton_actionPerformed(ActionEvent e)
	{
		this.menuProcess(MenuType.Connect);
	}

	public void this_componentResized(ComponentEvent e)
	{
	}

	public void this_windowOpened(WindowEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	protected String getToolTipText(Component component)
	{
		return "ToolTipText";
	}

	//private static DateTime _mouseRefreshDateTime;
	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void this_windowActivated(WindowEvent e)
	{
	}

	public void this_windowDeactivated(WindowEvent e)
	{
	}

	public void summaryTable_componentResized(ComponentEvent e)
	{
	}

	public void logTable_componentResized(ComponentEvent e)
	{
	}

	public void newsTable_componentResized(ComponentEvent e)
	{
	}

	public void newsTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
	}

	public void messageTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
	}

	public void messageTable_componentResized(ComponentEvent e)
	{
	}

	public void instrument2Table_componentResized(ComponentEvent e)
	{
	}

	public void instrumentViewTab_actionPerformed(ActionEvent e)
	{
	}

	public void instrumentViewTab_mouseEntered(MouseEvent e)
	{

	}

	public void instrumentViewTab_mouseClicked(MouseEvent e)
	{

	}
}

class MainFormBase_instrumentViewTab_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_instrumentViewTab_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.instrumentViewTab_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainFormBase_summaryTable_componentAdapter extends ComponentAdapter
{
	private MainFormBase adaptee;
	MainFormBase_summaryTable_componentAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void componentResized(ComponentEvent e)
	{
		adaptee.summaryTable_componentResized(e);
	}
}

class MainFormBase_logTable_componentAdapter extends ComponentAdapter
{
	private MainFormBase adaptee;
	MainFormBase_logTable_componentAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void componentResized(ComponentEvent e)
	{
		adaptee.logTable_componentResized(e);
	}
}

class MainFormBase_newsTable_componentAdapter extends ComponentAdapter
{
	private MainFormBase adaptee;
	MainFormBase_newsTable_componentAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void componentResized(ComponentEvent e)
	{
		adaptee.newsTable_componentResized(e);
	}
}

class MainFormBase_newsTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainFormBase_newsTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.newsTable_actionPerformed(e);
	}
}

class MainFormBase_messageTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainFormBase_messageTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.messageTable_actionPerformed(e);
	}
}

class MainFormBase_messageTable_componentAdapter extends ComponentAdapter
{
	private MainFormBase adaptee;
	MainFormBase_messageTable_componentAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void componentResized(ComponentEvent e)
	{
		adaptee.messageTable_componentResized(e);
	}
}

/*class MainFormBase_instrument2Table_componentAdapter extends ComponentAdapter
 {
 private MainFormBase adaptee;
 MainFormBase_instrument2Table_componentAdapter(MainFormBase adaptee)
 {
  this.adaptee = adaptee;
 }

 public void componentResized(ComponentEvent e)
 {
  adaptee.instrument2Table_componentResized(e);
 }
 }*/

class MainFormBase_instrument2Table_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainFormBase_instrument2Table_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.instrument2Table_actionPerformed(e);
	}
}

class MainFormBase_connectButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_connectButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.connectButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainFormBase_this_componentAdapter extends ComponentAdapter
{
	private MainFormBase adaptee;
	MainFormBase_this_componentAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void componentResized(ComponentEvent e)
	{
		adaptee.this_componentResized(e);
	}

	public void componentMoved(ComponentEvent e)
	{
	}

	public void componentShown(ComponentEvent e)
	{
	}

	public void componentHidden(ComponentEvent e)
	{
	}
}

class MainForm_newsButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_newsButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.newsButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainForm_debugButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_debugButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.debugButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainForm_openOrderTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainForm_openOrderTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.openOrderTable_actionPerformed(e);
	}
}

class MainForm_queryOrderTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainForm_queryOrderTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.orderTable_actionPerformed(e);
	}
}

class MainForm_orderTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainForm_orderTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.orderTable_actionPerformed(e);
	}
}

class MainForm_accountTree_actionAdapter implements tradingConsole.ui.grid.IActionListener
{
	private MainFormBase adaptee;
	MainForm_accountTree_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.accountTree_actionPerformed(e);
	}
}

class MainForm_summaryTable_actionAdapter implements IActionListener
{
		private MainFormBase adaptee;
		MainForm_summaryTable_actionAdapter(MainFormBase adaptee)
		{
			this.adaptee = adaptee;
		}

		public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
		{
			adaptee.summaryTable_actionPerformed(e);
		}
}

class MainForm_instrumentTable_actionAdapter implements IActionListener
{
	private MainFormBase adaptee;
	MainForm_instrumentTable_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.instrumentTable_actionPerformed(e);
	}
}

class MainForm_assignButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_assignButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.assignButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainForm_accountActiveButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_accountActiveButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.accountActiveButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainForm_exitButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_exitButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
		if (e.getSource() instanceof JideToggleButton)
		{
			( (JideToggleButton)e.getSource()).setSelected(false);
		}
	}
}

class MainFormBase_resetLayoutButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_resetLayoutButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetLayoutButton_actionPerformed(e);
	}
}

class MainFormBase_tradingFactButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_tradingFactButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.tradingFactButton_actionPerformed(e);
	}
}

class MainFormBase_dumpButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_dumpButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.dumpButton_actionPerformed(e);
	}
}

class MainFormBase_floatFramesButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainFormBase_floatFramesButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.floatFramesButton_actionPerformed(e);
	}
}

class MainForm_onlineHelpButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_onlineHelpButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.onlineHelpButton_actionPerformed(e);
	}
}

class MainForm_refreshButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_refreshButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.refreshButton_actionPerformed(e);
	}
}

class MainForm_chatRoomButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_chatRoomButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.chatRoomButton_actionPerformed(e);
	}
}

class MainForm_optionButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_optionButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.optionButton_actionPerformed(e);
	}
}

class MainForm_downloadDocumentButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_downloadDocumentButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.downloadDocumentButton_actionPerformed(e);
	}
}

class MainForm_clearCancelledOrderButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_clearCancelledOrderButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.clearCancelledOrderButton_actionPerformed(e);
	}
}

class MainForm_changeLeverageButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_changeLeverageButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.changeLeverageButton_actionPerformed(e);
	}
}

class MainForm_marginButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_marginButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.marginButton_actionPerformed(e);
	}
}

class MainForm_reportButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_reportButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.reportButton_actionPerformed(e);
	}
}

class MainForm_chartButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_chartButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.chartButton_actionPerformed(e);
	}
}

class MainForm_changePasswordButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_changePasswordButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.changePasswordButton_actionPerformed(e);
	}
}

class MainForm_instrumentSelectionButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_instrumentSelectionButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.instrumentSelectionButton_actionPerformed(e);
	}
}

class MainForm_accountSelectionButton_actionAdapter implements ActionListener
{
	private MainFormBase adaptee;
	MainForm_accountSelectionButton_actionAdapter(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.accountSelectionButton_actionPerformed(e);
	}
}

class MainForm_instrument2Table_AdjustmentListener implements AdjustmentListener
{
	private MainFormBase adaptee;
	MainForm_instrument2Table_AdjustmentListener(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		adaptee.instrument2Table_adjustmentValueChanged(e);
	}
}

class MainForm_physicalInvetoryTable_AdjustmentListener implements  IActionListener
{
	private MainFormBase adaptee;

	MainForm_physicalInvetoryTable_AdjustmentListener(MainFormBase adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.adaptee.processPhysicalInventoryTable(e);
	}
}
