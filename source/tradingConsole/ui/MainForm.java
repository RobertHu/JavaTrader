package tradingConsole.ui;

import java.io.*;
import java.math.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.*;

import org.aiotrade.ui.*;
import com.jidesoft.docking.*;
import com.jidesoft.grid.*;
import com.jidesoft.swing.*;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import framework.io.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.Account;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.physical.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.account.HierarchicalTableComponentFactory;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class MainForm extends MainFormBase
{
	public MainForm(TradingConsole owner, SettingsManager settingsManager)
	{
		super(owner, settingsManager);
		//TradingConsole.traceSource.trace(TraceType.Information, "MainForm constructor begin " + DateTime.get_Now().toString(DateTime.fullFormat));
		try
		{
			this.setIconImage(TradingConsole.get_TraderImage());
			jbInit();

			this._chartPanels = new HashMap<String, AioChartPanel> ();
			this.initAccountStatusTable();
			this.initAccountListTable();

			Rectangle rectangle = AppToolkit.getRectangleByDimension2();
			this.setBounds(rectangle);
			//TradingConsole.traceSource.trace(TraceType.Information, "MainForm constructor end " + DateTime.get_Now().toString(DateTime.fullFormat));
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public void addChildFrames()
	{
		super.addChildFrames();
		this.floatChildFrames();
		/*if(this._settingsManager.get_IsForRSZQ())
		{
			this.loadCharts();
		}
		else if(!this.loadCharts())*/
		{
			this.showFirstChart();
		}
		/*this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener()
		{
			public void ancestorMoved(HierarchyEvent e)
			{
				//if(_floatingChildFrames)
				//{
				//	floatChildFrames();
				//}
			}

			public void ancestorResized(HierarchyEvent e)
			{
				if(_floatingChildFrames)
				{
					floatChildFrames();
					//_owner.applyUserLayout();
				}
			}
		});*/
		//this.getDockingManager().restoreFrame();
	}

	private void floatChildFrames()
	{
		this.getDockingManager().setAutohidable(false);
		//this.getDockingManager().setHideFloatingFramesOnSwitchOutOfApplication(true);
		this.getDockingManager().setHideFloatingFramesWhenDeactivate(true);
		this.getDockingManager().setWithinFrameBoundsOnDragging(true);
		//this.getDockingManager().setWithinScreenBoundsOnDragging(true);

		if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
		{
			this.getDockingManager().floatFrame("PositionSummaryFrame", this.getFloatingRectangle("PositionSummaryFrame"), false);
		}
		else
		{
			this.getDockingManager().floatFrame("AccountStatusFrame", this.getFloatingRectangle("AccountStatusFrame"), false);
		}
		this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame"), false);
		if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
		{
			this.getDockingManager().floatFrame("OpenOrderListFrame", this.getFloatingRectangle("OpenOrderListFrame"), false);
		}
		this.getDockingManager().floatFrame("QueryOrderListFrame", this.getFloatingRectangle("QueryOrderListFrame"), false);
		if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical))
		{
			this.getDockingManager().floatFrame("PhysicalStokFrame", this.getFloatingRectangle("PhysicalStokFrame"), false);
		}
		if(this._settingsManager.get_IsForRSZQ()) this.getDockingManager().floatFrame("WorkingOrderListFrame", this.getFloatingRectangle("WorkingOrderListFrame"), false);
		for(String key : this.getDockingManager().getAllFrames())
		{
			if(key.startsWith("ChartFrame")/* && !this._settingsManager.get_IsForRSZQ()*/)
			{
				this.getDockingManager().floatFrame(key, this.getFloatingRectangle("ChartFrame1"), true);
			}
		}
	}

	public void initAccountStatusTable()
	{
		this.accountStatusTable.setModel(this._owner.get_AccountBindingManager().get_BindingSource());
		this.accountStatusTable.setHierarchicalColumn(0);

		this.accountStatusTable.get_HeaderRenderer().setHeight(0);

		TableColumn tableColumn = this.accountStatusTable.getColumn("IsSelected");
		BooleanCheckBoxCellEditor booleanCheckBoxCellEditor = new BooleanCheckBoxCellEditor();
		tableColumn.setCellEditor(booleanCheckBoxCellEditor);
		tableColumn.addPropertyChangeListener(null);

		BooleanCheckBoxCellRenderer booleanCheckBoxCellRenderer = new BooleanCheckBoxCellRenderer();
		tableColumn.setCellRenderer(booleanCheckBoxCellRenderer);
		tableColumn.setWidth(20);

		this.accountStatusTable.setComponentFactory(new HierarchicalTableComponentFactory());
		this.accountStatusTable.setIntercellSpacing(new Dimension(0, 0));
		this.accountStatusTable.setOpaque(false);
	}

	public void initAccountListTable()
	{
		this.accountListTable.setModel(this._owner.get_AccountBindingManager().get_AccountListBindingSource());
		this.accountListTable.setHierarchicalColumn(0);

		TableColumn tableColumn = this.accountListTable.getColumn("IsSelected");
		BooleanCheckBoxCellEditor booleanCheckBoxCellEditor = new BooleanCheckBoxCellEditor();
		tableColumn.setCellEditor(booleanCheckBoxCellEditor);
		tableColumn.addPropertyChangeListener(null);

		BooleanCheckBoxCellRenderer booleanCheckBoxCellRenderer = new BooleanCheckBoxCellRenderer();
		tableColumn.setCellRenderer(booleanCheckBoxCellRenderer);
		tableColumn.setWidth(20);

		this.accountListTable.setComponentFactory(new HierarchicalOpenOrderTableComponentFactory());
		this.accountListTable.setIntercellSpacing(new Dimension(0, 0));
		this.accountListTable.setOpaque(false);

		this.accountListTable.setRowHeight(18);
	}

	private void closeAllChartForms()
	{
		for (String key : this._chartPanels.keySet())
		{
			AioChartPanel chartPanel = this._chartPanels.get(key);
			try
			{
				this._owner.get_ChartManager().destoryChartPanel(chartPanel);
			}
			catch (Throwable exception)
			{
			}
			finally
			{
				super.removeChartPanel(key);
			}
		}
		this._chartPanels.clear();
		this._chartPanelKey = 0;
	}

	public void closeAllPopWindow()
	{
		this.closeAllChartForms();
		this._settingsManager.closeMakeOrderWindows();
		this.closeTradingFactWindow();
	}

	private void closeTradingFactWindow()
	{
		if(this._tradingFactFrame != null)
		{
			this._tradingFactFrame.dispose();
			this._tradingFactFrame = null;
		}
	}

	public void showFrame2(String key)
	{
		if (key.equalsIgnoreCase("ChartFrame"))
		{
			for (String chartKey : this._chartPanels.keySet())
			{
				String frameKey = "ChartFrame" + chartKey;
				super.showFrame(frameKey);
			}
		}
		else
		{
			super.showFrame(key);
		}
	}

	//Michael????
	public void showLog()
	{
		boolean needShowLog = this._settingsManager.get_Customer().get_ShowLog();
		this.showLog(needShowLog);
	}

	public HierarchicalTable get_AccountTable()
	{
		return this.accountStatusTable;
	}

	public DataGrid get_PhysicalPendingInventoryTable()
	{
		return this.physicalPendingInventoryTable;
	}

	public DataGrid get_PhysicalInventoryTable()
	{
		return this.physicalInventoryTable;
	}

	public DataGrid get_PhysicalShotSellTable()
	{
		return this.physicalShotSellTable;
	}

	public tradingConsole.ui.grid.DockableTable get_SummaryTable()
	{
		return this.summaryTable;
	}

	public tradingConsole.ui.grid.DockableTable get_LogTable()
	{
		return this.logTable;
	}

	public tradingConsole.ui.grid.DockableTable get_NewsTable()
	{
		return this.newsTable;
	}

	public tradingConsole.ui.grid.DockableTable get_MessageTable()
	{
		return this.messageTable;
	}

	public tradingConsole.ui.grid.DockableTable get_InstrumentTable()
	{
		return this.instrumentTable;
	}

	public InstrumentSpanGrid get_InstrumentSpanGrid()
	{
		return this.instrument2Table;
	}

	public tradingConsole.ui.grid.DockableTable get_OrderTable()
	{
		return this.orderTable;
	}

	public tradingConsole.ui.grid.DataGrid get_OpenOrderTable()
	{
		return this.openOrderTable;
	}

	public void orderDoLayout()
	{
		//this.orderTable.doLayout();
		//this.openOrderTable.doLayout();
	}

	public void refreshLoginInformation()
	{
		LoginInformation loginInformation = this._owner.get_LoginInformation();
		String hostName = AppToolkit.getHost(this._owner.get_ServiceManager().get_ServiceUrl());
		String message = loginInformation.getLoginInformation(hostName);
		if (loginInformation.get_LoginStatus().equals(LoginStatus.Logouted))
		{
			message += "   [" + this.loginInformationStaticText.getText() + "]";
		}
		this.loginInformationStaticText.setText(message);
	}

	//called by scheduler
	public void refreshAppTimeForm(DateTime appTime)
	{
		String appTimeString = XmlConvert.toString(appTime, "yyyy-MM-dd HH:mm:ss");
		//TradingConsole.traceSource.trace(TraceType.Warning, appTimeString);
		this.appTimeStaticText.setText(appTimeString);
	}

	public void tradingAccountFormShow()
	{
		if (this._owner.get_TradingAccountManager().needActiveAccount())
		{
			TradingAccountForm tradingAccountForm = new TradingAccountForm(this, this._owner, this._settingsManager);
			tradingAccountForm.toFront();
			tradingAccountForm.show();
		}
	}

	protected void resetLayout(boolean removeCustomerLayout)
	{
		this._owner.resetLayout(removeCustomerLayout);
		if (this._floatingChildFrames)	this.floatChildFrames();
		if (!Parameter.isHasNews)
		{
			this.showNews(false);
		}
		if(this._settingsManager.get_IsForRSZQ())
		{
			this.getDockingManager().hideFrame("NewsFrame");
			this.getDockingManager().hideFrame("ChartFrame1");
			if(this.get_FloatingChildFrames())
			{
				this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame", false), false);
			}
		}
	}

	protected void menuProcess(MenuType menuType)
	{
		if (menuType.equals(MenuType.Connect))
		{
			if (this._owner.get_LoginInformation().getIsConnected())
			{
				try
				{
					this._owner.get_LoginInformation().set_LogoutTime(TradingConsoleServer.appTime());
				}
				catch (Throwable exception)
				{
				}
				this._owner.saveLayout();
				this._owner.get_LoginInformation().set_LoginStatus(LoginStatus.Logouted);
				this.refreshLoginInformation();
				this._owner.loggedOut();
			}
			else
			{
				this._owner.reConnect(false);
			}
		}
		else if (menuType.equals(MenuType.InstrumentSelection))
		{
			InstrumentSelectionForm instrumentSelectionForm = new InstrumentSelectionForm(this, this._owner, this._settingsManager);
			instrumentSelectionForm.toFront();
			instrumentSelectionForm.show();
		}
		else if(menuType.equals(MenuType.AccountSelection))
		{
			AccountSelectionForm form = new AccountSelectionForm(this, this._owner, false);
			form.toFront();
			form.show();
		}
		else if (menuType.equals(MenuType.ChangePassword))
		{
			ChangePasswordForm2 changePasswordForm = new ChangePasswordForm2(this, this._owner);
			changePasswordForm.toFront();
			changePasswordForm.show();
		}
		else if (menuType.equals(MenuType.Chart))
		{
			this.showChart();
		}
		else if (menuType.equals(MenuType.Report))
		{
			this.showReport();
		}
		else if (menuType.equals(MenuType.Margin))
		{
			MarginForm marginForm = new MarginForm(this, this._owner, this._settingsManager);
			marginForm.toFront();
			marginForm.show();
		}
		else if(menuType.equals(MenuType.ChangeLeverage))
		{
			ChangeLeverageForm changeLeverageForm = new ChangeLeverageForm(this, this._owner, this._settingsManager);
			changeLeverageForm.toFront();
			changeLeverageForm.show();
		}
		else if (menuType.equals(MenuType.ClearCancelledOrder))
		{
			this.clearCancelledOrder();
		}
		else if (menuType.equals(MenuType.DownloadDocument))
		{
			this.showDownloadDocument();
		}
		else if (menuType.equals(MenuType.Option))
		{
			this.optionFormShow(0);
		}
		else if (menuType.equals(MenuType.ChatRoom))
		{
			this.showChatRoom();
		}
		else if (menuType.equals(MenuType.Assign))
		{
			if (this._settingsManager.get_IsExistsAgentAccount())
			{
				LockAccountForm assignForm = new LockAccountForm(this, this._owner, this._settingsManager);
				assignForm.toFront();
				assignForm.show();
			}
		}
		else if (menuType.equals(MenuType.AccountActive))
		{
			this.tradingAccountFormShow();
		}
		else if (menuType.equals(MenuType.RefreshSystem))
		{
			this.refreshSystem();
		}
		else if (menuType.equals(MenuType.OnlineHelp))
		{
			this.showOnlineHelp();
		}
		else if (menuType.equals(MenuType.News))
		{
			//this.showNews();
		}
		else if (menuType.equals(MenuType.Debug))
		{
			DebugForm debugForm = new DebugForm();
			debugForm.toFront();
			debugForm.show();
		}
		else if (menuType.equals(MenuType.ExitSystem))
		{
			this.exitSystem();
		}
	}



	protected void exitSystem()
	{
		try
		{
			this._owner.get_TradingConsoleServer().stopSlidingWindow();
			this._owner.saveLayout();

			if (this._owner.get_TradingConsoleServer() != null)
			{
				this._owner.get_TradingConsoleServer().clearScene();
			}
			this.getDockingManager().removeAllFrames();
			this.closeAllChartForms();
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		super.exitSystem();
		//System.exit(0);
	}

	public void optionFormShow(int currentIndex)
	{
		OptionForm optionForm = new OptionForm(this, this._owner, this._settingsManager, currentIndex);
		optionForm.toFront();
		optionForm.show();
	}

	private void showChart()
	{
		if (this.showHidenChartFrames() > 0) return;

		this._chartPanelKey++;
		String key = Convert.toString(this._chartPanelKey);

		String path = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "ChartSettings");
		File file = new File(path);
		File chartSettingFile = null;
		if (file.exists())
		{
			File[] files = file.listFiles();
			if (files != null && files.length > 0)
			{
				for (File childFile : files)
				{
					if (!childFile.getName().startsWith("ChartFrame"))	continue;
					String key2 = childFile.getName().substring("ChartFrame".length());
					if (key2.equals(key))
					{
						chartSettingFile = childFile;
						break;
					}
				}
			}
		}

		if(this._settingsManager.getInstruments().size() > 0)
		{
			AioChartPanel chartPanel = this._owner.get_ChartManager().createChartPanel(chartSettingFile);
			if (chartPanel == null)
			{
				this._owner.messageNotify("Failed to show chart!", false);
				this._chartPanelKey--;
				return;
			}
			super.addChart(key, chartPanel);
			this._chartPanels.put(key, chartPanel);
		}
	}

	public boolean loadCharts()
	{
		boolean result = false;
		try
		{
			String path = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "ChartSettings");
			File file = new File(path);
			if (!file.exists())	return result;

			File[] files = file.listFiles();
			if (files != null && files.length > 0)
			{
				for (File childFile : files)
				{
					if(!childFile.getName().startsWith("ChartFrame")) continue;
					String key = childFile.getName().substring("ChartFrame".length());
					AioChartPanel chartPanel = this._owner.get_ChartManager().createChartPanel(childFile);
					if (chartPanel != null)
					{
						super.addChart(key, chartPanel);
						this._chartPanelKey = Integer.parseInt(key);
						this._chartPanels.put(key, chartPanel);
						result = true;
					}
				}
			}
		}
		catch(Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
		return result;
	}

	public void saveChartSettings()
	{
		try
		{
			String path = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "ChartSettings");
			AppToolkit.createDirectory(path);
			File file = new File(path);
			File[] files = file.listFiles();
			if (files != null && files.length > 0)
			{
				for (File childFile : files)
				{
					childFile.delete();
				}
			}

			DockingManager dockingManager = this.getDockingManager();
			for (String key : dockingManager.getAllFrames())
			{
				if (key.startsWith("ChartFrame"))
				{
					DockableFrame frame = dockingManager.getFrame(key);
					//if (frame.isVisible())
					{
						for (Component component : frame.getContentPane().getComponents())
						{
							if (component instanceof AioChartPanel)
							{
								String fullFileName = DirectoryHelper.combine(path, key);
								AioChartPanel aioChartPanel = (AioChartPanel)component;
								File file2 = new File(fullFileName);
								if (file2.exists()) file2.delete();
								file2.createNewFile();
								aioChartPanel.saveSettings(file2);
								break;
							}
						}
					}
				}
			}
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void showFirstChart()
	{
		if (this._chartPanels.containsKey("1"))
		{
			String frameKey = "ChartFrame1";
			DockableFrame dockableFrame = super.getDockingManager().getFrame(frameKey);
			if (dockableFrame != null && dockableFrame.isHidden())
			{
				super.getDockingManager().showFrame(frameKey);
			}
		}
		else
		{
			this.showChart();
		}
	}

	public void changeChartInstrumentList()
	{
		this._owner.get_ChartManager().refreshInstrument();
	}

	private int showHidenChartFrames()
	{
		int hiddenChartFrameCount = 0;
		for (String key : this._chartPanels.keySet())
		{
			String frameKey = "ChartFrame" + key;
			if(frameKey.equals("ChartFrame1") && this._settingsManager.get_IsForRSZQ() && this._floatingChildFrames)
			{
				if(this.getDockingManager().getFrame("TradingPanelListFrame").isVisible())
				{
					this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame", true), false);
				}
			}

			DockableFrame dockableFrame = super.getDockingManager().getFrame(frameKey);
			if (dockableFrame != null && dockableFrame.isHidden())
			{
				/*if(this._floatingChildFrames)
				{
					dockableFrame.dispose();
				}
				else*/
				{
					super.getDockingManager().showFrame(frameKey);
					hiddenChartFrameCount++;
				}
			}
		}
		return hiddenChartFrameCount;
	}

	private void showReport()
	{
		ReportForm reportForm = new ReportForm(this, this._owner, this._settingsManager);
		reportForm.show();
	}

	private void clearCancelledOrder()
	{
		boolean[] expandedRows = new boolean[this.openOrderTable.getRowCount()];
		for(int row = 0; row < this.openOrderTable.getRowCount(); row++)
		{
			expandedRows[row] = this.openOrderTable.isExpanded(row);
		}
		this.openOrderTable.collapseAllRows();
		this._owner.clearCancelledOrder();
		for(int row = 0; row < expandedRows.length; row++)
		{
			if(expandedRows[row])
			{
				this.openOrderTable.expandRow(row);
			}
		}
	}

	private void showChatRoom()
	{
		//not implemented
	}

	public void refreshSystem()
	{
		this.get_SummaryTable().collapseAllRows();

		DockableFrame frame = this.getDockingManager().getFrame("ChartFrame1");
		boolean isFirstChartHidden = frame == null ? false : frame.isHidden();
		frame = this.getDockingManager().getFrame("NewsFrame");
		boolean isNewsHidden = frame == null ? false : frame.isHidden();

		//this.openOrderTable.setModel(null);

		this._owner.resetSystem(true);
		this.openOrderTable.setOptimized(true);//Eliminate flicker in Open order table after refresh

		if(this._owner.get_LoginInformation().get_LoginStatus() == LoginStatus.LoginSucceed
			|| this._owner.get_LoginInformation().get_LoginStatus() == LoginStatus.Ready )
		{
			this.loadCharts();
			//this.resetLayout();
			if (!this._floatingChildFrames)
			{
				for (String key : this.getDockingManager().getAllFrames())
				{
					frame = this.getDockingManager().getFrame(key);
					this.getDockingManager().dockFrame(key, frame.getInitSide(), frame.getInitIndex());
				}
				this._owner.resetLayout(false);
			}
			else
			{
				this.resetLayout(false);
			}
			this._owner.applyUserLayout();

			if(isFirstChartHidden)
			{
				this.getDockingManager().hideFrame("ChartFrame1");
				if(this._floatingChildFrames)
				{
					this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame", false), false);
				}
			}

			if(this._settingsManager.get_IsForRSZQ())
			{
				if(!isNewsHidden) this.getDockingManager().showFrame("NewsFrame");
				if(!isFirstChartHidden)
				{
					this.getDockingManager().showFrame("ChartFrame1");
					if(this._floatingChildFrames)
					{
						this.getDockingManager().floatFrame("TradingPanelListFrame", this.getFloatingRectangle("TradingPanelListFrame", true), false);
					}
				}
			}
		}
	}

	private void showDownloadDocument()
	{
		String companyCode = this._owner.get_LoginInformation().get_CompanyName();
		//String url = StringHelper.replace(this._owner.get_ServiceManager().get_ServiceUrl(), "Service.asmx",
		//								  "") + companyCode + "/" + PublicParametersManager.version + "/Document/Document.htm";
		String url = StringHelper.replace(this._owner.get_ServiceManager().get_ServiceUrl(), "Service.asmx", "");
		url = AppToolkit.changeUrlProtocol(url, "http");
		url += companyCode + "/" + PublicParametersManager.version + "/Document/Document.htm";
		int mapPort = this._owner.get_ServiceManager().getMapPort();
		url = AppToolkit.changeToMapPort(url, mapPort);
		BrowserControl.displayURL(url, true);
	}

	//private JFrame _onlineHelpFrame = null;
	//private HTMLViewer _onlineHelpHTMLViewer = null;
	private void showOnlineHelp()
	{
		String companyCode = this._owner.get_LoginInformation().get_CompanyName();
		//String url = StringHelper.replace(this._owner.get_ServiceManager().get_ServiceUrl(), "Service.asmx",
		//								  "") + companyCode + "/" + PublicParametersManager.version + "/Help/Help.html";
		String url = StringHelper.replace(this._owner.get_ServiceManager().get_ServiceUrl(), "Service.asmx", "");
		url = AppToolkit.changeUrlProtocol(url, "http");
		url += companyCode + "/" + PublicParametersManager.version + "/Java35Help/Help.html";
		int mapPort = this._owner.get_ServiceManager().getMapPort();
		url = AppToolkit.changeToMapPort(url, mapPort);
		BrowserControl.displayURL(url, true);
		/*try
		{
			if(this._onlineHelpFrame == null)
			{
				this._onlineHelpFrame = new JFrame(Language.HelpMenuText);
				this._onlineHelpFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				this._onlineHelpHTMLViewer = new HTMLViewer();
				this._onlineHelpFrame.add(this._onlineHelpHTMLViewer);

				Rectangle rectangle = AppToolkit.getRectangleByDimension(AppToolkit.get_ScreenSize());
				int width = (int) (rectangle.getWidth() * 0.8);
				int height = (int) (rectangle.getHeight() * 0.8);
				this._onlineHelpFrame.setSize(width, height);
				this._onlineHelpFrame.setIconImage(AppToolkit.getImage("Help.gif"));
				JideSwingUtilities.centerWindow(this._onlineHelpFrame);
			}
			this._onlineHelpFrame.setVisible(true);
			this._onlineHelpFrame.toFront();
			this._onlineHelpHTMLViewer.setUrl(new URL(url));
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, ex);
			AlertDialogForm.showDialog(this, null, true, Language.CannotShowTradingFact);
		}*/
	}

	protected void processAccountTree(tradingConsole.ui.grid.ActionEvent e)
	{
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.Clicked)
		{
			tradingConsole.ui.account.Account account = (tradingConsole.ui.account.Account)e.get_Object();
			Account account2 = this._settingsManager.getAccount(account.get_Id());
			if (account2 != null)
			{
				this._owner.refreshInterestRate(account2);
			}
		}
	}

	protected void processSummaryGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			String columnName = e.get_ColumnName();
			if (columnName.equalsIgnoreCase(SummaryColKey.SellString)
				|| columnName.equalsIgnoreCase(SummaryColKey.BuyString))
			{
				Instrument instrument = null;
				Account account = null;
				AccountTradingSummary accountTradingSummary = null;
				if(e.get_Object() instanceof Instrument)
				{
					instrument = (Instrument)e.get_Object();
				}
				else
				{
					accountTradingSummary = (AccountTradingSummary)e.get_Object();
					instrument = accountTradingSummary.get_Instrument();
					account = accountTradingSummary.get_Account();
				}

				boolean closeAllSell = columnName.equalsIgnoreCase(SummaryColKey.SellString) ? true : false;
				BigDecimal lot = closeAllSell ?  instrument.get_SellLots() : instrument.get_BuyLots();
				if(accountTradingSummary != null) lot = closeAllSell ?  accountTradingSummary.get_SellLots() : accountTradingSummary.get_BuyLots();
				if(lot.compareTo(BigDecimal.ZERO) <=0) return;

				boolean isLiquidation = false;
				BigDecimal maxLot = isLiquidation ? instrument.get_MaxDQLot() : instrument.get_MaxOtherLot();
				if (maxLot.compareTo(BigDecimal.ZERO) <= 0)
				{
					AlertDialogForm.showDialog(this, null, true, Language.InstrumentIsNotAccepting);
					return;
				}

				if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
					|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
				{
					//this.makeOrder2Process(instrument);
					//doesn't support OrderType 2 now
				}
				else
				{
					MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(instrument);
					if (makeOrderWindow != null)
					{
						makeOrderWindow.closeAllWindow();
						this._settingsManager.removeMakeOrderWindow(instrument, makeOrderWindow.get_MainWindow());
					}

					Object[] result = MakeOrder.isAllowMakeSpotTradeOrder(this._owner, this._settingsManager, instrument, account);
					Object[] result2 = MakeOrder.isAllowMakeBroadLimitOrder(this._owner, this._settingsManager, instrument, account);
					if ( (Boolean)result[0])
					{
						MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)result[2];
						if(makeSpotTradeOrder._makeOrderAccounts.size() == 1)
						{
							makeSpotTradeOrder.showUi(true, (Boolean)result[0], (Boolean)result2[0], true, closeAllSell);
						}
					}
					else if ( (Boolean)result2[0])
					{
						MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result2[2];
						if(makeLimitOrder._makeOrderAccounts.size() == 1)
						{
							makeLimitOrder.showUi(true, (Boolean)result[0], (Boolean)result2[0], false, closeAllSell);
						}
					}
					else
					{
						AlertDialogForm.showDialog(this, null, true, result[1].toString());
					}
				}
			}
		}
	}

	protected void processInstrumentGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			Instrument instrument = (Instrument)e.get_Object();

			String columnName = e.get_ColumnName();
			if (columnName.equalsIgnoreCase(InstrumentColKey.Description)
				|| columnName.equalsIgnoreCase(InstrumentColKey.Ask)
				|| columnName.equalsIgnoreCase(InstrumentColKey.Bid))
			{
				this.processInstrumentEvent(instrument, !columnName.equalsIgnoreCase(InstrumentColKey.Description),
									   columnName.equalsIgnoreCase(InstrumentColKey.Ask));
			}
		}
	}

	void processInstrumentEvent(Instrument instrument, boolean isLiquidation, boolean isAsk)
	{
		BigDecimal maxLot = isLiquidation ? instrument.get_MaxDQLot() : instrument.get_MaxOtherLot();
		if(maxLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			AlertDialogForm.showDialog(this, null, true, Language.InstrumentIsNotAccepting);
			return;
		}

		if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
			|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
		{
			this.makeOrder2Process(instrument);
		}
		else
		{
			//this.spotTradeOrderProcess(instrument, isAsk);
			if (isLiquidation)
			{
				this.spotTradeOrderProcess(instrument, isAsk);
			}
			else
			{
				this.broadLimitOrderProcess(instrument, true);
			}
		}
	}

	protected void processInstrumentSpanGridActionEvent(tradingConsole.ui.grid.ActionEvent e)
	{
		InstrumentSpanGrid grid = (InstrumentSpanGrid)e.get_Source();
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			Instrument instrument = (Instrument)e.get_Object();

			String columnName = e.get_ColumnName();

			if (columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.Description.name())
				|| columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.AskLeft.name())
				|| columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.AskRight.name())
				|| columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.BidLeft.name())
				|| columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.BidRight.name()))
			{
				boolean isLiquidation = !columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.Description.name());
				boolean isAsk = columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.AskLeft.name())
					|| columnName.equalsIgnoreCase(InstrumentSpanBindingSource.ColumnNames.AskRight.name());
				this.processInstrumentEvent(instrument, isLiquidation, isAsk);
			}
		}
	}

	private void makeOrder2Process(Instrument instrument)
	{
		Object[] result = MakeOrder.isAllowMakeOrder2(this._owner, this._settingsManager, instrument);
		if ( (Boolean)result[0])
		{
			this.makeOrder2Instrance(instrument);
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());
		}
	}

	private void makeOrder2Instrance(Instrument instrument)
	{
		this.makeOrder2Instrance(instrument, null, null, null, OperateSource.Common);
	}

	public void makeOrder2Instrance(Instrument instrument, Order order, MakeLiquidationOrder makeLiquidationOrder, OpenContractForm openContractForm,
									OperateSource operateSource)
	{
		if(this._settingsManager.getMakeOrderWindow(instrument) == null)
		{
			MakeOrderForm makeOrderForm = new MakeOrderForm(this._owner, this._settingsManager, instrument, order, makeLiquidationOrder, openContractForm,
				operateSource);
			makeOrderForm.show();
		}
		else
		{
			this._settingsManager.getMakeOrderWindow(instrument).get_MainWindow().toFront();
			this._settingsManager.getMakeOrderWindow(instrument).get_MainWindow().show();
		}
	}

	//makeLimitOrder
	public void broadLimitOrderProcess(Instrument instrument, boolean isBuy)
	{
		/*Object[] result = MakeOrder.isAllowMakeBroadLimitOrder(this._owner, this._settingsManager, instrument);
		if ( (Boolean)result[0])
		{
			this.limitOrderFormIntrance(instrument, null, null, isBuy);
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());
		}*/
		this.placeOrder(instrument, true, false);
	}

	//MakeSpotTradeOrder
	private void spotTradeOrderProcess(Instrument instrument, boolean isDblClickAsk)
	{
		this.placeOrder(instrument, isDblClickAsk, true);
	}

	private void placeOrder(Instrument instrument, boolean isDblClickAsk, boolean isSpt)
	{
		MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(instrument);
		if(makeOrderWindow != null)
		{
			makeOrderWindow.get_MainWindow().toFront();
		}
		else
		{
			Object[] result = MakeOrder.isAllowMakeSpotTradeOrder(this._owner, this._settingsManager, instrument);
			Object[] result2 = MakeOrder.isAllowMakeBroadLimitOrder(this._owner, this._settingsManager, instrument);
			if ( (Boolean)result[0])
			{
				MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)result[2];
				makeSpotTradeOrder.showUi(isDblClickAsk, (Boolean)result[0], (Boolean)result2[0], isSpt);
			}
			else if((Boolean)result2[0])
			{
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result2[2];
				makeLimitOrder.showUi(isDblClickAsk, (Boolean)result[0], (Boolean)result2[0], isSpt);
			}
			else if(TradingConsole.DeliveryHelper.getDeliveryAccounts(this._owner, instrument).size() > 0)
			{
				DeliveryDialog dialog
					= new DeliveryDialog(this, this._owner, instrument, null, null, false);
				JideSwingUtilities.centerWindow(dialog);
				dialog.show();
			}
			else
			{
				AlertDialogForm.showDialog(this, null, true, result[1].toString());
			}
		}
	}

	public void matchingOrderProcess(Instrument instrument)
	{
		if (MakeOrder.isOperateSameInstrument(this._settingsManager, instrument)
			|| MakeOrder.isOverRangeOperateOrderUI(this._settingsManager))
		{
			String message = Language.OperateDQOrderPrompt + "/" + Language.InstrumentSelectOverRange + " " + Parameter.operateOrderCount;
			AlertDialogForm.showDialog(this, null, true, message);
			return;
		}
		MatchOrderForm matchOrderForm = new MatchOrderForm(this._owner, this._settingsManager, instrument);
		matchOrderForm.show();
	}

	public void limitOrderFormIntrance(JDialog parent, Instrument instrument, Order order, OpenContractForm openContractForm, boolean isBuy)
	{
		LimitOrderFormDialog limitOrderForm = new LimitOrderFormDialog(parent, this._owner, this._settingsManager, instrument, order, openContractForm, isBuy);
		JideSwingUtilities.centerWindow(limitOrderForm);
		limitOrderForm.show();
	}

	protected void processOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{
		DataGrid grid = (DataGrid)e.get_Source();
		Order order = (Order)e.get_Object();
		grid.setSelectionForeground(BuySellColor.getColor(order.get_IsBuy(), true));
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			try
			{
				TradingInstructionForm tradingInstructionForm = new TradingInstructionForm(this._owner, this._settingsManager, order);
				tradingInstructionForm.show();
			}
			catch (Throwable exception)
			{
				exception.printStackTrace();
			}
		}
		/*else if(e.get_GridAction() == tradingConsole.ui.grid.Action.Clicked
			&& order.get_InstalmentPolicyId() != null)
		{
			InstalmentForm instalmentForm = new InstalmentForm(this, order);
			JideSwingUtilities.centerWindow(instalmentForm);
			instalmentForm.show();
		}*/
	}

	protected void processPhysicalShortSellTable(tradingConsole.ui.grid.ActionEvent e)
	{
		Order order = (Order)e.get_Object();
		this.closeOrder(e, order, false);
	}

	protected void processPhysicalInventoryTable(tradingConsole.ui.grid.ActionEvent e)
	{
		Object object = e.get_Object();
		Order order = null;
		Inventory inventory = null;
		if(object instanceof Order)
		{
			order = (Order)object;
		}
		else if(object instanceof Inventory)
		{
			inventory = (Inventory)object;
		}

		if(order != null)
		{
			this.closeOrder(e, order, true);
		}
		else if(inventory != null && e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			Instrument instrument = inventory.get_Instrument2();
			Account account = inventory.get_Account();

			boolean closeAllSell = false;

			if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
				|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
			{
				//this.makeOrder2Process(instrument);
				//doesn't support OrderType 2 now
			}
			else
			{
				if (instrument.get_MaxDQLot().compareTo(BigDecimal.ZERO) <= 0
					&& instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) <= 0
					&& !TradingConsole.DeliveryHelper.hasInventory(this._owner, account, instrument))
				{
					AlertDialogForm.showDialog(this, null, true, Language.InstrumentIsNotAccepting);
					return;
				}

				MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(instrument);
				if (makeOrderWindow != null)
				{
					makeOrderWindow.closeAllWindow();
					this._settingsManager.removeMakeOrderWindow(instrument, makeOrderWindow.get_MainWindow());
				}

				Object[] result = MakeOrder.isAllowMakeSpotTradeOrder(this._owner, this._settingsManager, instrument, account);
				Object[] result2 = MakeOrder.isAllowMakeBroadLimitOrder(this._owner, this._settingsManager, instrument, account);
				if ( (Boolean)result[0])
				{
					MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)result[2];
					if (makeSpotTradeOrder._makeOrderAccounts.size() == 1)
					{
						makeSpotTradeOrder.showUi(true, (Boolean)result[0], (Boolean)result2[0], true, closeAllSell);
					}
				}
				else if ( (Boolean)result2[0])
				{
					MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result2[2];
					if (makeLimitOrder._makeOrderAccounts.size() == 1)
					{
						makeLimitOrder.showUi(true, (Boolean)result[0], (Boolean)result2[0], false, closeAllSell);
					}
				}
				else if(TradingConsole.DeliveryHelper.getDeliveryAccounts(this._owner, instrument).size() > 0)
				{
					DeliveryDialog dialog
						= new DeliveryDialog(this, this._owner, instrument, account, null, true);
					JideSwingUtilities.centerWindow(dialog);
					dialog.show();
				}
				else
				{
					AlertDialogForm.showDialog(this, null, true, result[1].toString());
				}
			}
		}
	}

	protected void processOpenOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{
		DataGrid grid = (DataGrid)e.get_Source();
		Order order = (Order)e.get_Object();
		grid.setSelectionForeground(BuySellColor.getColor(order.get_IsBuy(), true));

		this.closeOrder(e, order, null);
	}

	private void closeOrder(tradingConsole.ui.grid.ActionEvent e, Order order, Boolean isBuyOfOpenOrders)
	{
		if (e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked
			|| (e.get_GridAction() == tradingConsole.ui.grid.Action.Clicked
				&& this._settingsManager.getMakeOrderWindow(order.get_Transaction().get_Instrument()) != null))
		{
			Object[] result = MakeOrder.isAllowMakeLiquidationOrder(this._owner, this._settingsManager);
			if ( (Boolean)result[0])
			{
				MakeLiquidationOrder makeLiquidationOrder = (MakeLiquidationOrder)result[2];
				this.LiquidationOrder(makeLiquidationOrder);
			}
			else
			{
				Guid instrumentId = order.get_Transaction().get_Instrument().get_Id();
				if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
				{
					if (this._settingsManager.containsOpenContractFormOf(instrumentId))
					{
						this._settingsManager.getOpenContractFormOf(instrumentId).toFront();
					}

					MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(order.get_Transaction().get_Instrument());
					if (makeOrderWindow != null)
					{
						makeOrderWindow.get_MainWindow().toFront();
					}
					else
					{
						if (!this._settingsManager.containsOpenContractFormOf(instrumentId))
						{
							OpenContractForm openContractForm = new OpenContractForm(this._owner, this._settingsManager, order);
							this._settingsManager.add(instrumentId, openContractForm);
							openContractForm.show();
						}
					}
				}
			}
		}
	}

	private void LiquidationOrder(MakeLiquidationOrder makeLiquidationOrder)
	{
		if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
			|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
		{
			this._owner.get_MainForm().makeOrder2Instrance(makeLiquidationOrder.get_Instrument(), null, makeLiquidationOrder, null,
				OperateSource.LiquidationMultiSPT);
		}
		else
		{
			makeLiquidationOrder.showUi(null);
		}
	}

	private void logoInit()
	{
		this.logoButton.setText(" ");
		this.logoButton.setEnabled(false);
	}

	private void menuInit()
	{
		AppToolkit.menuItemInit(connectButton, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Connect.gif");
		AppToolkit.menuItemInit(instrumentSelectionButton, Language.MenuimgInstrumentSelectText, Language.MenuimgInstrumentSelectText,
							  Language.MenuimgInstrumentSelect, "ItemSelect.gif");
		AppToolkit.menuItemInit(accountSelectionButton, Language.accountSelectionFormTitle, Language.accountSelectionFormTitle,
			Language.accountSelectionFormTitle, "AccountSelect.gif");
		AppToolkit.menuItemInit(downloadDocumentButton, Language.MenuimgDownloadText, Language.MenuimgDownloadText, Language.MenuimgDownload,
							  "DownloadDocument.gif");
		AppToolkit.menuItemInit(clearCancelledOrderButton, Language.MenuimgClearAllText, Language.MenuimgClearAllText, Language.MenuimgClearAll, "Clear.gif");
		if(this._owner.get_ServiceManager().get_ShowMarginAsChuRuJin())
		{
			AppToolkit.menuItemInit(marginButton, Language.ChuRuJin, Language.ChuRuJin, Language.ChuRuJin, "Margin.gif");
		}
		else
		{
			AppToolkit.menuItemInit(marginButton, Language.MenuimgMarginText, Language.MenuimgMarginText, Language.MenuimgMargin, "Margin.gif");
		}
		AppToolkit.menuItemInit(changeLeverageButton, Language.ChangeLeverageText, Language.ChangeLeverageText, Language.ChangeLeverageText, "ChangeLeverage.gif");

		AppToolkit.menuItemInit(reportButton, Language.MenuimgOpenReportSelectText, Language.MenuimgOpenReportSelectText, Language.MenuimgOpenReportSelect,
							  "Report.gif");
		AppToolkit.menuItemInit(optionButton, Language.MenuimgOptionText, Language.MenuimgOptionText, Language.MenuimgOption, "Option.gif");
		AppToolkit.menuItemInit(messageButton, Language.MenuimgOpenChatText, Language.MenuimgOpenChatText, Language.MenuimgOpenChat, "Message.gif");
		AppToolkit.menuItemInit(chatRoomButton, Language.MenuimgChatRoomText, Language.MenuimgChatRoomText, Language.MenuimgChatRoom, "Chat.gif");
		/*if(!this._settingsManager.get_IsForRSZQ())*/ AppToolkit.menuItemInit(chartButton, Language.MenuimgShowAnalyticChartText, Language.MenuimgShowAnalyticChartText, Language.MenuimgShowAnalyticChart,
							  "Chart.gif");
		AppToolkit.menuItemInit(assignButton, Language.MenuimgLockAccountText, Language.MenuimgLockAccountText, Language.MenuimgLockAccount, "Agent.gif");
		AppToolkit.menuItemInit(accountActiveButton, Language.MenuimgAccountActiveText, Language.MenuimgAccountActiveText, Language.MenuimgAccountActive,
							  "AccountActive.gif");
		AppToolkit.menuItemInit(changePasswordButton, Language.MenuimgOpenChangePasswordText, Language.MenuimgOpenChangePasswordText,
							  Language.MenuimgOpenChangePassword, "ChangePasswords.gif");
		/*if(!this._settingsManager.get_IsForRSZQ())*/ AppToolkit.menuItemInit(newsButton, Language.MenuimgNewsText, Language.MenuimgNewsText, Language.MenuimgNews, "News.gif");
		AppToolkit.menuItemInit(refreshButton, Language.MenuimgResetText, Language.MenuimgResetText, Language.MenuimgReset, "Refresh.gif");
		AppToolkit.menuItemInit(onlineHelpButton, Language.MenuimgHelpText, Language.MenuimgHelpText, Language.MenuimgHelp, "Help.gif");
		AppToolkit.menuItemInit(debugButton, Language.debugFormTitleText, Language.debugFormTitleText, Language.debugFormTitle, "Debug.gif");
		AppToolkit.menuItemInit(exitButton, Language.MenuimgOpenLogOutText, Language.MenuimgOpenLogOutText, Language.MenuimgOpenLogOut, "Exit.gif");
		AppToolkit.menuItemInit(resetLayoutButton, Language.resetLayoutButtonText, Language.resetLayoutButtonText, Language.resetLayoutButtonCaption,
							  "ResetLayout.gif");
	}

	private void toolBarInit()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "toolBarInit");

		AppToolkit.toolBarItemInit(connectButton2, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Connect.gif");
		AppToolkit.toolBarItemInit(instrumentSelectionButton2, Language.MenuimgInstrumentSelectText, Language.MenuimgInstrumentSelectText,
							  Language.MenuimgInstrumentSelect, "ItemSelect.gif");
		AppToolkit.toolBarItemInit(accountSelectionButton2, Language.accountSelectionFormTitle, Language.accountSelectionFormTitle,
							  Language.accountSelectionFormTitle, "AccountSelect.gif");
		AppToolkit.toolBarItemInit(downloadDocumentButton2, Language.MenuimgDownloadText, Language.MenuimgDownloadText, Language.MenuimgDownload,
							  "DownloadDocument.gif");
		AppToolkit.toolBarItemInit(clearCancelledOrderButton2, Language.MenuimgClearAllText, Language.MenuimgClearAllText, Language.MenuimgClearAll, "Clear.gif");

		if(this._owner.get_ServiceManager().get_ShowMarginAsChuRuJin())
		{
			AppToolkit.toolBarItemInit(marginButton2, Language.ChuRuJin, Language.ChuRuJin, Language.ChuRuJin, "Margin.gif");
		}
		else
		{
			AppToolkit.toolBarItemInit(marginButton2, Language.MenuimgMarginText, Language.MenuimgMarginText, Language.MenuimgMargin, "Margin.gif");
		}

		AppToolkit.toolBarItemInit(changeLeverageButton2, Language.ChangeLeverageText, Language.ChangeLeverageText, Language.ChangeLeverageText, "ChangeLeverage.gif");

		AppToolkit.toolBarItemInit(reportButton2, Language.MenuimgOpenReportSelectText, Language.MenuimgOpenReportSelectText, Language.MenuimgOpenReportSelect,
							  "Report.gif");
		AppToolkit.toolBarItemInit(optionButton2, Language.MenuimgOptionText, Language.MenuimgOptionText, Language.MenuimgOption, "Option.gif");
		AppToolkit.toolBarItemInit(messageButton2, Language.MenuimgOpenChatText, Language.MenuimgOpenChatText, Language.MenuimgOpenChat, "Message.gif");
		AppToolkit.toolBarItemInit(chatRoomButton2, Language.MenuimgChatRoomText, Language.MenuimgChatRoomText, Language.MenuimgChatRoom, "Chat.gif");
		/*if(!this._settingsManager.get_IsForRSZQ())*/ AppToolkit.toolBarItemInit(chartButton2, Language.MenuimgShowAnalyticChartText, Language.MenuimgShowAnalyticChartText, Language.MenuimgShowAnalyticChart,
							  "Chart.gif");
		AppToolkit.toolBarItemInit(assignButton2, Language.MenuimgLockAccountText, Language.MenuimgLockAccountText, Language.MenuimgLockAccount, "Agent.gif");
		AppToolkit.toolBarItemInit(accountActiveButton2, Language.MenuimgAccountActiveText, Language.MenuimgAccountActiveText, Language.MenuimgAccountActive,
							  "AccountActive.gif");
		AppToolkit.toolBarItemInit(changePasswordButton2, Language.MenuimgOpenChangePasswordText, Language.MenuimgOpenChangePasswordText,
							  Language.MenuimgOpenChangePassword, "ChangePasswords.gif");
		/*if(!this._settingsManager.get_IsForRSZQ())*/ AppToolkit.toolBarItemInit(newsButton2, Language.MenuimgNewsText, Language.MenuimgNewsText, Language.MenuimgNews, "News.gif");
		AppToolkit.toolBarItemInit(refreshButton2, Language.MenuimgResetText, Language.MenuimgResetText, Language.MenuimgReset, "Refresh.gif");
		AppToolkit.toolBarItemInit(onlineHelpButton2, Language.MenuimgHelpText, Language.MenuimgHelpText, Language.MenuimgHelp, "Help.gif");
		AppToolkit.toolBarItemInit(debugButton2, Language.debugFormTitleText, Language.debugFormTitleText, Language.debugFormTitle, "Debug.gif");
		AppToolkit.toolBarItemInit(exitButton2, Language.MenuimgOpenLogOutText, Language.MenuimgOpenLogOutText, Language.MenuimgOpenLogOut, "Exit.gif");
		AppToolkit.toolBarItemInit(resetLayoutButton2, Language.resetLayoutButtonText, Language.resetLayoutButtonText, Language.resetLayoutButtonCaption,
							  "ResetLayout.gif");

		TradingConsole.traceSource.trace(TraceType.Information, "MainForm.getLogo() ");
		AppToolkit.toolBarItemInit(tradingFactButton, Language.tradingFactButtonText, Language.tradingFactButtonText, Language.tradingFactButtonCaption,
							  "TradingFact.gif");

		AppToolkit.toolBarItemInit(dumpButton, Language.complainButtonText, Language.complainButtonText, Language.complainButtonText,  "Debug.gif");

		AppToolkit.toolBarItemInit(floatingFramesButton, Language.floatingFramesButtonText, Language.floatingFramesButtonText, Language.floatingFramesButtonCaption,
							  "FloatingFrames.gif");
		tradingFactButton.setVisible(false);
	}

	public void setConnectStatus()
	{
		LoginStatus loginStatus = this._owner.get_LoginInformation().get_LoginStatus();
		this.statusStaticText.setText(LoginStatus.getCaption(loginStatus));
		//this.statusStaticText.doLayout();
	}

	//only change display for network disconnect
	public void setDisconnectStatusDisplay()
	{
		this.statusStaticText.setText(LoginStatus.getCaption(LoginStatus.Disconnected));
	}

	public void loggedIn(LoginResult loginResult)
	{
		//this.setLogo(loginResult);

		AppToolkit.menuItemInit(this.connectButton, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Disconnect.gif");
		AppToolkit.toolBarItemInit(this.connectButton2, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Disconnect.gif");

		this.setConnectStatus();
		PopupMenuManager.initializePopupMenus(this);
	}

	private void setLogo(LoginResult loginResult)
	{
		Image image = null;
		try
		{
			byte[] logo = loginResult.get_CompanyLogo();
			if (logo == null)
			{
				TradingConsole.traceSource.trace(TraceType.Error, "CompanyLogo: " + Language.InitializeParameterError);
				return;
			}
			image = java.awt.Toolkit.getDefaultToolkit().createImage(logo);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "MainForm.setLogo()");
		}

		if (image != null)
		{
			//Michael????
			//this.logoButton.setImageAt(image, 0);
			//this.logoButton.prepareImage(image, this.logoButton);
/*
			if (this._aboutPanel != null)
			{
				try
				{
					this._aboutPanel.changeBackgroundImage(image);
				}
				catch (Exception exception)
				{
				}
			}
			*/
		}
	}

	private void processNewsTable(tradingConsole.ui.grid.ActionEvent e)
	{
		if(e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			News news = (News)e.get_Object();
			NewsContentForm newsContentForm = new NewsContentForm(_owner, _settingsManager, news);
			newsContentForm.show();
		}
	}

	private void processMessageTable(tradingConsole.ui.grid.ActionEvent e)
	{
		if (e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
		{
			Message message = (Message)e.get_Object();
			MessageContentForm messageContentForm = new MessageContentForm(this._owner, this._settingsManager, message);
			messageContentForm.show();
		}
	}

	public void disconnect()
	{
		//MainForm.menuButtonInit(this.connectButton, MainForm.getConnectImage());
		AppToolkit.menuItemInit(this.connectButton, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Connect.gif");
		AppToolkit.toolBarItemInit(this.connectButton2, Language.MenuimgConnectText, Language.MenuimgConnectText, Language.MenuimgConnect, "Connect.gif");
		this.setMenuStatus(false);
		//this.menuPanel.doLayout();
		//this.commandMenuBar.doLayout();
		//this.commandBar.doLayout();

		this.setConnectStatus();
	}

	private void setMenuStatus(boolean isValidation)
	{
		this.instrumentSelectionButton.setEnabled(isValidation);
		this.accountSelectionButton.setEnabled(isValidation);
		this.downloadDocumentButton.setVisible(isValidation);
		this.clearCancelledOrderButton.setEnabled(isValidation);
		this.marginButton.setVisible(isValidation);
		this.changeLeverageButton.setVisible(isValidation);
		this.reportButton.setVisible(isValidation);
		//this.messageButton.setEnabled(isValidation);
		this.messageButton.setVisible(false);
		this.chatRoomButton.setVisible(isValidation);
		this.chartButton.setEnabled(isValidation);
		this.assignButton.setVisible(isValidation);
		this.accountActiveButton.setVisible(isValidation);
		this.changePasswordButton.setEnabled(isValidation);
		//this.newsButton.setVisible(isValidation);
		this.newsButton.setVisible(false);
		this.refreshButton.setEnabled(isValidation);
		this.onlineHelpButton.setEnabled(isValidation);
		this.debugButton.setVisible(isValidation);
		this.resetLayoutButton.setVisible(isValidation);

		this.accountSelectionButton2.setEnabled(isValidation);
		this.instrumentSelectionButton2.setEnabled(isValidation);
		this.downloadDocumentButton2.setVisible(isValidation);
		this.clearCancelledOrderButton2.setEnabled(isValidation);
		this.marginButton2.setVisible(isValidation);
		this.changeLeverageButton2.setVisible(isValidation);
		this.reportButton2.setVisible(isValidation);
		//this.messageButton2.setEnabled(isValidation);
		this.messageButton2.setVisible(false);
		this.chatRoomButton2.setVisible(isValidation);
		this.chartButton2.setEnabled(isValidation);
		this.assignButton2.setVisible(isValidation);
		this.accountActiveButton2.setVisible(isValidation);
		this.changePasswordButton2.setEnabled(isValidation);
		//this.newsButton2.setVisible(isValidation);
		this.newsButton2.setVisible(false);
		this.refreshButton2.setEnabled(isValidation);
		this.onlineHelpButton2.setEnabled(isValidation);
		this.debugButton2.setVisible(isValidation);
		this.resetLayoutButton2.setVisible(isValidation);
		this.tradingFactButton.setVisible(isValidation);

		//this.menuPanel.doLayout();
		//this.commandMenuBar.doLayout();
		//this.commandBar.doLayout();
	}

	public void setMenuVisible()
	{
		this.instrumentSelectionButton.setEnabled(true);
		this.accountSelectionButton.setEnabled(true);
		this.downloadDocumentButton.setVisible(Parameter.isUseDownload);
		this.clearCancelledOrderButton.setEnabled(true);
		this.marginButton.setVisible(this._settingsManager.shouldEnableMarginButton());
		this.changeLeverageButton.setVisible(this._settingsManager.get_SystemParameter().get_CanModifyLeverage());
		this.reportButton.setVisible(!this._settingsManager.get_Customer().get_IsNoShowAccountStatus());
		//this.messageButton.setEnabled(true);
		this.messageButton.setVisible(false);
		//this.chatRoomButton.setEnabled(Parameter.isHasChatRoom);
		this.chatRoomButton.setVisible(false);
		this.chartButton.setEnabled(true);
		this.assignButton.setVisible(this._settingsManager.isAllowLockAccount());
		this.accountActiveButton.setVisible(this._owner.get_TradingAccountManager().needActiveAccount());
		this.changePasswordButton.setEnabled(true);
		this.changePasswordButton.setVisible(this._owner.getShowChangePasswordMenuItem());
		//this.newsButton.setVisible(Parameter.isHasNews);
		this.newsButton.setVisible(false);
		this.refreshButton.setEnabled(true);
		this.onlineHelpButton.setEnabled(true);
		//this.debugButton.setEnabled(Parameter.isHasDebug);
		this.debugButton.setVisible(false);
		this.resetLayoutButton.setVisible(true);

		this.instrumentSelectionButton2.setEnabled(true);
		this.accountSelectionButton2.setEnabled(true);
		this.downloadDocumentButton2.setVisible(Parameter.isUseDownload);
		this.clearCancelledOrderButton2.setEnabled(true);
		this.marginButton2.setVisible(this._settingsManager.shouldEnableMarginButton());
		this.changeLeverageButton2.setVisible(this._settingsManager.get_SystemParameter().get_CanModifyLeverage());
		this.reportButton2.setVisible(!this._settingsManager.get_Customer().get_IsNoShowAccountStatus());
		//this.messageButton2.setEnabled(true);
		this.messageButton2.setVisible(false);
		//this.chatRoomButton2.setEnabled(Parameter.isHasChatRoom);
		this.chatRoomButton2.setVisible(false);
		this.chartButton2.setEnabled(true);
		this.assignButton2.setVisible(this._settingsManager.isAllowLockAccount());
		this.accountActiveButton2.setVisible(this._owner.get_TradingAccountManager().needActiveAccount());
		this.changePasswordButton2.setEnabled(true);
		this.changePasswordButton2.setVisible(this._owner.getShowChangePasswordMenuItem());
		//this.newsButton2.setVisible(Parameter.isHasNews);
		this.newsButton2.setVisible(false);
		this.refreshButton2.setEnabled(true);
		this.onlineHelpButton2.setEnabled(true);
		//this.debugButton2.setEnabled(Parameter.isHasDebug);
		this.debugButton2.setVisible(false);
		this.resetLayoutButton2.setVisible(true);

		//this.menuPanel.doLayout();
		//this.commandMenuBar.doLayout();
		//this.commandBar.doLayout();
	}

	public void summaryTable_componentResized(ComponentEvent e)
	{
		try
		{
			//Instrument.updatePropertiesForSummary(Instrument.summaryPanelKey, this.get_PositionSummaryFrame());
			//this.summaryTable.doLayout();
		}
		catch (Exception exception)
		{
		}
		/*
		if (this._owner == null || !this._owner.get_LoginInformation().getIsConnected())
		{
			return;
		}
		this._owner.rebindSummary();
		*/
	}

	public void logTable_componentResized(ComponentEvent e)
	{
		try
		{
			Log.updateProperties(Log.logPanelKey, this.get_LogFrame());
			this.logTable.doLayout();
		}
		catch (Exception exception)
		{
		}
		/*
		if (this._owner == null || !this._owner.get_LoginInformation().getIsConnected())
		{
			return;
		}
		this._owner.rebindLog();
	    */
	}

	public void newsTable_componentResized(ComponentEvent e)
	{
		/*try
		{
			News.updateProperties(News.newsPanelKey, this.get_NewsFrame());
			this.newsTable.doLayout();
		}
		catch (Exception exception)
		{
		}*/

		/*
		if (this._owner == null || !this._owner.get_LoginInformation().getIsConnected())
		{
			return;
		}
		if (Parameter.isHasNews)
		{
			this._owner.rebindNews();
		}
		*/
	}

	public void messageTable_componentResized(ComponentEvent e)
	{
		try
		{
			Message.updateProperties(Message.messagePanelKey, this.get_MessageFrame());
			this.messageTable.doLayout();
		}
		catch (Exception exception)
		{
		}

		/*
		if (this._owner == null || !this._owner.get_LoginInformation().getIsConnected())
		{
			return;
		}
		this._owner.rebindMessage();
		*/
	}

	public void newsTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processNewsTable(e);
	}

	public void messageTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processMessageTable(e);
	}

	/*public void instrument2Table_componentResized(ComponentEvent e)
	{
		if (this._owner == null || !this._owner.get_LoginInformation().getIsConnected())
		{
			return;
		}
		//if (this.instrumentViewTab.getCurrentIndex()==1)
		//if (MainForm.instrumentViewTabCurrentIndex == 1)
		{
			this.instrument2Table.setRedraw(false);
			this.adjustInstrument2TableSplitLocation();
			this.instrument2Table.setRedraw(true);
		}
	}*/

	protected void instrument2TableAdjustmentValueChanged()
	{
	}

	public void adjustInstrument2TableSplitLocation()
	{
		/*
		   int unitGridRowHeight = UnitGridRow.getUnitGridRowHeight(this.instrument2Table);
		   if (unitGridRowHeight > 0)
		   {
		 int unitGridRowsCount = this._owner.get_UnitGridManager().getUnitGridRowsCount();
		 PVScrollBar scrollBar = (PVScrollBar)this.instrument2Table.m_scrollH;
		 int scrollHeight = (scrollBar.isVisible())?scrollBar.getButtonSize():0;
		 scrollHeight += 5;//Fix height
		 int splitLocation = this.clientAreaPanel.getSplitLocation();
		 int tabHeight = this.instrumentViewTab.getTabHeight();

		 int i = splitLocation/unitGridRowHeight;
		 int i2 = (splitLocation > (i * unitGridRowHeight + tabHeight + scrollHeight))?i + 1:i;
		 if (i == 0) i2 = 1;
		 if (i2 > unitGridRowsCount && unitGridRowsCount >= 1) i2 = unitGridRowsCount;
		 this.clientAreaPanel.setSplitLocation(i2 * unitGridRowHeight + tabHeight + scrollHeight);
		 this.clientAreaPanel.doLayout();
		   }
		 */
	}

	//Michael????
	//public SplitterPanel getAccountPanel()
	//{
	//	return this.accountPanel;
	//}

/*
	private void setMenuBackground()
	{
		this.instrumentSelectionButton.setBackground(FormBackColor.mainForm);
		this.downloadDocumentButton.setBackground(FormBackColor.mainForm);
		this.clearCancelledOrderButton.setBackground(FormBackColor.mainForm);
		this.marginButton.setBackground(FormBackColor.mainForm);
		this.reportButton.setBackground(FormBackColor.mainForm);
		this.messageButton.setBackground(FormBackColor.mainForm);
		this.chatRoomButton.setBackground(FormBackColor.mainForm);
		this.chartButton.setBackground(FormBackColor.mainForm);
		this.assignButton.setBackground(FormBackColor.mainForm);
		this.accountActiveButton.setBackground(FormBackColor.mainForm);
		this.changePasswordButton.setBackground(FormBackColor.mainForm);
		this.newsButton.setBackground(FormBackColor.mainForm);
		this.refreshButton.setBackground(FormBackColor.mainForm);
		this.onlineHelpButton.setBackground(FormBackColor.mainForm);
		this.debugButton.setBackground(FormBackColor.mainForm);
		this.connectButton.setBackground(FormBackColor.mainForm);
		this.optionButton.setBackground(FormBackColor.mainForm);
		this.exitButton.setBackground(FormBackColor.mainForm);
	}
*/

	public void setBackground()
	{
		//Michael????
		/*
		this.setBackground(FormBackColor.mainForm);
		this.logoButton.setBackground(FormBackColor.mainForm);
		//this.menuPanel.setBackground(FormBackColor.mainForm);
		this.commandMenuBar.setBackground(FormBackColor.mainForm);

		this.setMenuBackground();
		*/
	}

	public void applyLanguage()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "applyLanguage");
		this.setTitle(Language.mainFormTitle);
		this.setMenuBarCaption();
		this.menuInit();
		this.toolBarInit();
	}

	public void windowMenuInit2()
	{
		super.windowMenuInit();

		boolean isConnected = this._owner.get_LoginInformation().getIsConnected();

		if (isConnected)
		{
			this.windowMenu.add(this.resetLayoutButton);

			super.windowMenu.addSeparator();

			JMenuItem windowMenuItem = new JMenuItem();
			AppToolkit.menuItemInit(windowMenuItem, Language.InstrumentViewPrompt, Language.InstrumentViewPrompt, Language.InstrumentViewPrompt,
									"TradingPanelList.ico");
			windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
			super.windowMenu.add(windowMenuItem);

			//if (PublicParametersManager.showInstrumentSpanGrid)
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.InstrumentView2Prompt, Language.InstrumentView2Prompt, Language.InstrumentView2Prompt,
		"TradingPanelGrid.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			windowMenuItem = new JMenuItem();
			AppToolkit.menuItemInit(windowMenuItem, Language.Query, Language.Query, Language.Query, "Query.ico");
			windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
			super.windowMenu.add(windowMenuItem);

			windowMenuItem = new JMenuItem();
			AppToolkit.menuItemInit(windowMenuItem, Language.AccountStatusPrompt, Language.AccountStatusPrompt, Language.AccountStatusPrompt, "AccountStatus.ico");
			windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
			super.windowMenu.add(windowMenuItem);

			if(this._settingsManager.get_Accounts().size() > 1)
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.AccountListPrompt, Language.AccountListPrompt, Language.AccountListPrompt, null);
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			if (this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.SummaryPrompt, Language.SummaryPrompt, Language.SummaryPrompt, "PositionSummary.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			windowMenuItem = new JMenuItem();
			AppToolkit.menuItemInit(windowMenuItem, Language.messageFormTitle, Language.messageFormTitle, Language.messageFormTitle, "Message.ico");
			windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
			super.windowMenu.add(windowMenuItem);

			if (Parameter.isHasNews /* && !this._settingsManager.get_IsForRSZQ()*/)
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.newsFormTitle, Language.newsFormTitle, Language.newsFormTitle, "News.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			//if (isConnected)
			{
				boolean needShow = this._settingsManager.get_Customer().get_ShowLog();
				if (needShow)
				{
					windowMenuItem = new JMenuItem();
					AppToolkit.menuItemInit(windowMenuItem, Language.LogPrompt, Language.LogPrompt, Language.LogPrompt, "Log.ico");
					windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
					super.windowMenu.add(windowMenuItem);
				}
			}

			windowMenuItem = new JMenuItem();
			AppToolkit.menuItemInit(windowMenuItem, Language.SettingOrderGrid, Language.SettingOrderGrid, Language.SettingOrderGrid, "WorkingOrderList.ico");
			windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
			super.windowMenu.add(windowMenuItem);

			if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin))
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.SettingOpenOrderGrid, Language.SettingOpenOrderGrid, Language.SettingOpenOrderGrid,
										"OpenOrderList.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			if(this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical))
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.PhysicalStok, Language.PhysicalStok, Language.PhysicalStok,
										"PhysicalStok.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}

			//if (isConnected)
			//if (!this._settingsManager.get_IsForRSZQ())
			{
				windowMenuItem = new JMenuItem();
				AppToolkit.menuItemInit(windowMenuItem, Language.MenuimgShowAnalyticChart, Language.MenuimgShowAnalyticChart, Language.MenuimgShowAnalyticChart,
										"Chart.ico");
				windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
				super.windowMenu.add(windowMenuItem);
			}
		}
/*
		windowMenuItem = new JMenuItem();
		AppToolkit.menuItemInit(windowMenuItem, Language.aboutFormTitle, Language.aboutFormTitle, Language.aboutFormTitle, "About.ico");
		windowMenuItem.addActionListener(new MainForm_windowMenuItem_actionAdapter(this, windowMenuItem));
		super.windowMenu.add(windowMenuItem);
	   */
	}

	public void windowMenuItem_actionPerformed(JMenuItem menuItem)
	{
		String name = menuItem.getName();

		String key = "";
		if (name.equalsIgnoreCase(Language.InstrumentViewPrompt))
		{
			key = "TradingPanelListFrame";
		}
		if (name.equalsIgnoreCase(Language.InstrumentView2Prompt))
		{
			key = "TradingPanelGridFrame";
		}
		if (name.equalsIgnoreCase(Language.AccountStatusPrompt))
		{
			key = "AccountStatusFrame";
		}
		if (name.equalsIgnoreCase(Language.AccountListPrompt))
		{
			key = "AccountListFrame";
		}
		if (name.equalsIgnoreCase(Language.SummaryPrompt))
		{
			key = "PositionSummaryFrame";
		}
		if (name.equalsIgnoreCase(Language.messageFormTitle))
		{
			key = "MessageFrame";
		}
		if (name.equalsIgnoreCase(Language.newsFormTitle))
		{
			key = "NewsFrame";
		}
		if (name.equalsIgnoreCase(Language.LogPrompt))
		{
			key = "LogFrame";
		}
		if (name.equalsIgnoreCase(Language.SettingOrderGrid))
		{
			key = "WorkingOrderListFrame";
		}
		if (name.equalsIgnoreCase(Language.SettingOpenOrderGrid))
		{
			key = "OpenOrderListFrame";
		}
		if (name.equalsIgnoreCase(Language.PhysicalStok))
		{
			key = "PhysicalStokFrame";
		}
		if (name.equalsIgnoreCase(Language.MenuimgShowAnalyticChart))
		{
			key = "ChartFrame";
		}
		if (name.equalsIgnoreCase(Language.Query))
		{
			key = "QueryOrderListFrame";
		}

		if (!key.equalsIgnoreCase(""))
		{
			this.showFrame2(key);
		}
	}

	//SourceCode End////////////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		//this.setSize(800, 600);
		this.setTitle(Language.mainFormTitle);

		this.setBackground();

		this.windowMenuInit2();
		this.logoInit();
		//this.menuInit();
		//this.toolBarInit();
		this.disconnect();
		this.loginInformationStaticText.setText("");
		this.appTimeStaticText.setText("");
		this.setDisconnectStatusDisplay();

		this.setMenuStatus(false);

		this.setResizable(true);
	}

	public void showTradingFactButton()
	{
		this.tradingFactButton.setVisible(true);
	}

	public tradingConsole.ui.grid.DataGrid get_NotConfirmedPendingOrderTable()
	{
		return this.queryPanel.get_NotConfirmedPendingOrderTable();
	}

	public OrderQueryPanel get_QueryPanel()
	{
		return this.queryPanel;
	}

	public void setDetailVisibleInAccountList(boolean visible, boolean isForInit)
	{
		this.accountListTable.setAutoResizeMode(visible ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_LAST_COLUMN);
		if (!visible)
		{
			this.accountListTable.getColumnModel().getColumn(0).setMaxWidth(40);
		}
		else
		{
			this.accountListTable.getColumnModel().getColumn(0).setMaxWidth(4000);
		}

		if(isForInit && visible) return;

		for(int index = 2; index < tradingConsole.ui.accountList.Account.propertyNames.length; index++)
		{
			String name = tradingConsole.ui.accountList.Account.propertyNames[index];
			int column = this.accountListTable.get_BindingSource().getColumnByName(name);
			if(!visible)
			{
				if(column > -1)
				{
					tradingConsole.ui.grid.TableColumnChooser.hideColumn(this.accountListTable, column);
				}
			}
			else
			{
				if(column > -1)
				{
					tradingConsole.ui.grid.TableColumnChooser.showColumn(this.accountListTable, column, -1);
				}
			}
		}
	}
}

class MainForm_windowMenuItem_actionAdapter implements ActionListener
{
	private MainForm adaptee;
	private JMenuItem menuItem;
	MainForm_windowMenuItem_actionAdapter(MainForm adaptee, JMenuItem menuItem)
	{
		this.adaptee = adaptee;
		this.menuItem = menuItem;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.windowMenuItem_actionPerformed(menuItem);
	}
}
