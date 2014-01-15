package tradingConsole;

import java.applet.*;
import java.io.*;
import java.math.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

import org.aiotrade.ui.*;
import com.install4j.api.launcher.*;
import com.jidesoft.docking.*;
import com.jidesoft.plaf.*;
import com.jidesoft.plaf.office2003.*;
import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.io.*;
import framework.net.*;
import framework.threading.*;
import framework.threading.Scheduler.*;
import framework.xml.*;
import tradingConsole.bursa.*;
import tradingConsole.bursa.TimeTable.*;
import tradingConsole.diagnostics.*;
import tradingConsole.diagnostics.UncaughtExceptionHandler;
import tradingConsole.enumDefine.*;
import tradingConsole.enumDefine.bursa.*;
import tradingConsole.framework.*;
import tradingConsole.install4j.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;
import tradingConsole.ui.chart.*;
import tradingConsole.ui.chart.SettingsProvider;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import tradingConsole.physical.*;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.enumDefine.physical.PhysicalTradeSide;

import Packet.*;

import org.apache.log4j.Logger;

import Connection.ConnectionManager;
import org.apache.log4j.PropertyConfigurator;
import java.util.concurrent.TimeUnit;
import Connection.SSLConnection;
import Util.RequestCommandHelper;
import Connection.ConnectionManager.TcpInializeTimeoutException;
import nu.xom.Element;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class TradingConsole extends Applet implements Scheduler.ISchedulerCallback, IInstrumentStateListener,ConnectionObserver //, IAutoUpdatable
{
	public static final char delimiterRow = '\n';
	public static final char delimiterCol = '\t';
	public static final String delimiterCol2 = "x";
	public static final String emptyRateString = "-";
	public static final String enterLine = System.getProperty("line.separator");
	private static Image _traderImage;

	private static String _actionArgsRefreshAppTime = "RefreshAppTime";
	private static String _actionArgsVerifyTransactionTime = "VerifyTransactionTime";
	private static String _actionArgsResetSystemTime = "resetSystemTime";

	private static final short DockingLayoutVersion = 1;

	public static TraceSource traceSource;
	public static TraceSource getCommandTraceSource;
	public static TraceSource performaceTraceSource;

	//public static Scheduler scheduler;
	private SchedulerEntry _workScheduleEntry;
	private SchedulerEntry _verifyTransactionScheduleEntry;
	private SchedulerEntry _resetSystemScheduleEntry;

	public static BindingManager bindingManager;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForTradingPanel;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForSummaryPanel;
	private HierarchicalTradingSummaryTableModel _hierarchicalTradingSummaryTableModel;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForLogPanel;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForNewsPanel;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForMessagePanel;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForWorkingOrderList;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForOpenOrderList;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForNotConfirmedPendingList;
	private HierarchicalOpenOrderTableModel _hierarchicalOpenOrderTableModel;
	private InstrumentSpanBindingSource _instrumentSpanBindingSource;

	private TradingAccountManager _tradingAccountManager;
	private RecoverPasswordManager _recoverPasswordManager;
	private SettingsManager _settingsManager;
	private InstrumentStateManager _instrumentStateManager;
	private HashMap<Guid, Transaction> _transactions;
	private HashMap<Guid, Order> _orders;
	private HashMap<Guid, Order> _workingOrders;
	private HashMap<Guid, Order> _openOrders;
	private HashMap<Guid, Quotation> _quotations;
	private HashMap<Guid, Log> _logs;
	//private HashMap<Guid, News> _newses;
	//private HashMap<Guid, Message> _messages;

	private boolean _isInitialized;

	private LoginInformation _loginInformation;
	private MainForm _mainForm;

	private CommandsManager _commandsManager;
	private TradingConsoleServer _tradingConsoleServer;

	//WebService
	private int _commandSequence;
	private CookieContainer _cookieContainer;
	private ProxyManager _proxy;
	private ServiceManager _serviceManager;
	private HashMap<Guid, Guid> _interestRateOrderIds;

	private QuotationProvider _quotationProvider;
	private SettingsProvider _settingsProvider;
	private AioChartManager _chartManager;
	private tradingConsole.ui.account.BindingManager _bindingManager; // = new tradingConsole.ui.account.BindingManager();
	private boolean _hasMessage = false;
	private BankAccountHelper _bankAccountHelper;
	private Logger logger = Logger.getLogger(TradingConsole.class);
	private RecoverService recoverService = new RecoverService();
	private ConnectionManager connectionManager;
	static
	{
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader", "TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}


	public ConnectionManager getConnectionManager(){
		return this.connectionManager;
	}


	public TradingAccountManager get_TradingAccountManager()
	{
		return this._tradingAccountManager;
	}


	public tradingConsole.ui.account.BindingManager get_AccountBindingManager()
	{
		if (this._bindingManager == null)
		{
			this._bindingManager = new tradingConsole.ui.account.BindingManager(this._settingsManager.get_Customer());
		}
		return this._bindingManager;
	}

	public RecoverPasswordManager get_RecoverPasswordManager()
	{
		return this._recoverPasswordManager;
	}

	public ServiceManager get_ServiceManager()
	{
		return this._serviceManager;
	}

	public AioChartManager get_ChartManager()
	{
		return this._chartManager;
	}

	public void setServiceManager()
	{
		this._serviceManager = ServiceManager.Create();
		ColorSettings.useBlackAsBackground = this._serviceManager.get_UseBlackAsBackground();
		if (ColorSettings.useBlackAsBackground)
		{
			( (Office2003Painter)Office2003Painter.getInstance()).setColorName("Metallic");
		}
		else
		{
			( (Office2003Painter)Office2003Painter.getInstance()).setColorName("NormalColor");
		}
	}

	private void proxySet()
	{
		try
		{
			this._proxy = ProxyManager.create();
			boolean proxySet = this._proxy.getProxySet();
			if (proxySet)
			{
				this._proxy.setProperty();
			}
		}
		catch (Throwable exception)
		{

			//TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}



	public void start(String[] stringArray)
	{
		SwingUtilities.invokeLater(new SwingThreadSafeStartup(this));
	}

	private static class SwingThreadSafeStartup implements Runnable
	{
		private TradingConsole _owner;

		public SwingThreadSafeStartup(TradingConsole owner)
		{
			this._owner = owner;
		}

		public void run()
		{
			this._owner.init();
			this._owner.start();
		}
	}



	public static Image get_TraderImage()
	{
		if (TradingConsole._traderImage == null)
		{
			TradingConsole._traderImage = AppToolkit.getImage("Trader.gif");
		}
		return TradingConsole._traderImage;
	}

	public boolean get_IsInitialized()
	{
		return this._isInitialized;
	}

	public TradingConsoleServer get_TradingConsoleServer()
	{
		return this._tradingConsoleServer;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForTradingPanel()
	{
		return this._bindingSourceForTradingPanel;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForSummaryPanel()
	{
		return this._bindingSourceForSummaryPanel;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForLogPanel()
	{
		return this._bindingSourceForLogPanel;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForMessagePanel()
	{
		return this._bindingSourceForMessagePanel;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForWorkingOrderList()
	{
		return this._bindingSourceForWorkingOrderList;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForOpenOrderList()
	{
		return this._bindingSourceForOpenOrderList;
	}

	public HierarchicalOpenOrderTableModel get_HierarchicalOpenOrderTableModel()
	{
		return this._hierarchicalOpenOrderTableModel;
	}

	public InstrumentSpanBindingSource get_InstrumentSpanBindingSource()
	{
		return this._instrumentSpanBindingSource;
	}


	public Log getLog(Guid logId)
	{
		return (this._logs.containsKey(logId)) ? this._logs.get(logId) : null;
	}

	public void setLog(Log log)
	{
		this._logs.put(log.get_Id(), log);
	}

	public void removeLog(Log log)
	{
		this.removeLog(log.get_Id());
	}

	public void removeLog(Guid logId)
	{
		if (this.getLog(logId) != null)
		{
			this._logs.remove(logId);
		}
	}

	public Transaction getTransaction(Guid transactionId)
	{
		return (this._transactions.containsKey(transactionId)) ? this._transactions.get(transactionId) : null;
	}

	public void setTransaction(Transaction transaction)
	{
		this._transactions.put(transaction.get_Id(), transaction);
		InventoryManager.instance.add(transaction);
	}

	public Order getOrder(Guid orderId)
	{
		return (this._orders.containsKey(orderId)) ? this._orders.get(orderId) : null;
	}

	public void setOrder(Order order)
	{
		this._orders.put(order.get_Id(), order);
		InventoryManager.instance.add(order.get_Transaction());
	}

	public Order getWorkingOrder(Guid orderId)
	{
		return (this._workingOrders.containsKey(orderId)) ? this._workingOrders.get(orderId) : null;
	}

	public void setWorkingOrder(Order order)
	{
		if (order.get_Transaction().get_Phase() == Phase.Executed)
		{
			//don't add to working list, but maybe should add to some other list in future
		}
		else
		{
			this._workingOrders.put(order.get_Id(), order);
		}
	}

	public Order getOpenOrder(Guid orderId)
	{
		return (this._openOrders.containsKey(orderId)) ? this._openOrders.get(orderId) : null;
	}

	public void setOpenOrder(Order order)
	{
		if (!this._openOrders.containsKey(order.get_Id()))
		{
			this._openOrders.put(order.get_Id(), order);
			Instrument instrument = order.get_Transaction().get_Instrument();
			/*if (order.get_Transaction().needCalculateSummary())
			 {
			 if (order.get_IsBuy())
			 {
			  instrument.addBuyLots(order.get_LotBalance());
			 }
			 else
			 {
			  instrument.addSellLots(order.get_LotBalance());
			 }
			 this.rebindSummary();
			 }*/
		}
	}

	public HashMap<Guid, Order> get_OpenOrders()
	{
		return this._openOrders;
	}

	public void refreshAllOrderUi()
	{
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.update();
		}
		this._mainForm.orderDoLayout();
	}

	public Quotation getQuotation(Guid instrumentId)
	{
		return this._quotations.get(instrumentId);
	}

	public LoginInformation get_LoginInformation()
	{
		return this._loginInformation;
	}

	public MainForm get_MainForm()
	{
		return this._mainForm;
	}

	public BankAccountHelper get_BankAccountHelper()
	{
		return this._bankAccountHelper;
	}



	static XmlDocument getServiceWsdl(ServiceManager serviceManager, InputStream inputStream, String replacedUrl)
	{
		XmlDocument xmlDocument = new XmlDocument();
		//xmlDocument.load(serviceWsdlXmlFilePath);
		xmlDocument.load(inputStream);
		XmlNode documentElement = xmlDocument.get_DocumentElement();
		XmlElement wsdlserviceElement = documentElement.get_Item("service");
		XmlNodeList wsdlPortElements = wsdlserviceElement.get_ChildNodes();
		int count = wsdlPortElements.get_Count();
		for (int i = 0; i < count; i++)
		{
			XmlNodeList addressNodes = wsdlPortElements.item(i).get_ChildNodes();
			for (int j = 0; j < addressNodes.get_Count(); j++)
			{
				if (addressNodes.item(j).get_LocalName().equalsIgnoreCase("address"))
				{
					XmlAttribute locationAttribute = addressNodes.item(j).get_Attributes().get_ItemOf("location");
					locationAttribute.set_Value(serviceManager.get_ServiceUrl());
				}
			}
		}
		return xmlDocument;
	}



	public static void main(String[] args)
	{
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		String company = args.length > 0 ? args[0] : "Omnicare";
		AppToolkit.setDirectory(company);

		TradingConsole tradingConsole = new TradingConsole();
		Trace.setLogProperties(tradingConsole);

		TradingConsole.traceSource = new TraceSource("TradingCosole.TradingCosole");
		TradingConsole.getCommandTraceSource = new TraceSource("TradingCosole.GetCommands");
		TradingConsole.performaceTraceSource = new TraceSource("TradingCosole.Performace");

		String enableThreadMonitor = System.getProperty("EnableThreadMonitor");
		if (!StringHelper.isNullOrEmpty(enableThreadMonitor) && enableThreadMonitor.compareToIgnoreCase("true") == 0)
		{
			ThreadMonitor.start();
		}

		PublicParametersManager.initialize();

		String enableTraceForcelyStr = System.getProperty("EnableTraceForcely");
		boolean enableTraceForcely = true;
		if (StringHelper.isNullOrEmpty(enableTraceForcelyStr) || enableTraceForcelyStr.compareToIgnoreCase("true") != 0)
		{
			enableTraceForcely = false;
		}
		if (enableTraceForcely || PublicParametersManager.isTrace)
		{
			TraceManager.enable();
		}
		if (PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem)
		{
			TraceManager.emptyAllLogFiles(false);
		}
		TradingConsole.traceSource.trace(TraceType.Information, AppToolkit.getSystemInfo());

		TradingConsole.traceSource.trace(TraceType.Information, "To launch auto updater");
		TradingConsole.launchAutoUpdater(tradingConsole);

		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader", "TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
		LookAndFeelFactory.setDefaultStyle(LookAndFeelFactory.OFFICE2003_STYLE);
		LookAndFeelFactory.installDefaultLookAndFeel();

		tradingConsole.start(args);
	}

	private static String getUpdateDescriptorUrl() throws FileNotFoundException, IOException
	{
		String configFileName = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "i4jparams.conf");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName)));
		String line = reader.readLine();
		while (line != null)
		{
			if (line.indexOf("updateDescriptorUrl") > 0)
			{
				line = reader.readLine();
				int begin = line.indexOf("http");
				int end = line.indexOf("</string>");
				return line.substring(begin, end);
			}
			line = reader.readLine();
		}
		return null;
	}

	private static void setUpdateDescriptorUrl(String urlString)
	{
		String newConfigFileName = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "newi4jparams.conf");
		TradingConsole.traceSource.trace(TraceType.Information, "newConfigFileName = " + newConfigFileName);

		String configFileName = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "i4jparams.conf");
		try
		{
			File newConfigFile = new File(newConfigFileName);
			if (newConfigFile.exists())
			{
				newConfigFile.delete();
			}
			newConfigFile.createNewFile();

			PrintWriter writer = new PrintWriter(newConfigFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName)));
			String line = reader.readLine();
			while (line != null)
			{
				writer.println(line);

				if (line.indexOf("updateDescriptorUrl") > 0)
				{
					line = reader.readLine();
					String newLine = "<string>" + urlString + "</string>";
					writer.println(newLine);
				}

				line = reader.readLine();
			}
			reader.close();
			writer.close();

			File configFile = new File(configFileName);
			configFile.delete();

			new File(newConfigFileName);
			newConfigFile.renameTo(configFile);
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "changeDomainOfUpdateUrl failed: ");
			TradingConsole.traceSource.trace(TraceType.Error, ex);
		}
	}

	private static boolean changeDomainOfUpdateUrl(String domain)
	{
		String enableSyncUpdateUrl = System.getProperty("EnableSyncUpdateUrl");
		if (StringHelper.isNullOrEmpty(enableSyncUpdateUrl) || enableSyncUpdateUrl.compareToIgnoreCase("true") != 0)
		{
			return false;
		}

		TradingConsole.traceSource.trace(TraceType.Information, "changeDomainOfUpdateUrl to: " + domain);

		String newConfigFileName = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "newi4jparams.conf");
		TradingConsole.traceSource.trace(TraceType.Information, "newConfigFileName = " + newConfigFileName);

		String configFileName = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "i4jparams.conf");
		try
		{
			File newConfigFile = new File(newConfigFileName);
			if (newConfigFile.exists())
			{
				newConfigFile.delete();
			}
			newConfigFile.createNewFile();

			PrintWriter writer = new PrintWriter(newConfigFileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFileName)));
			String line = reader.readLine();
			while (line != null)
			{
				writer.println(line);

				if (line.indexOf("updateDescriptorUrl") > 0)
				{
					line = reader.readLine();
					line = line.toLowerCase();

					int start = line.indexOf("http://") + "http://".length();
					int end = line.indexOf("/", start);

					String oldHost = line.substring(start, end);
					TradingConsole.traceSource.trace(TraceType.Information, "[changeDomainOfUpdateUrl] oldHost = " + oldHost);
					if (oldHost.compareToIgnoreCase(domain) == 0)
					{
						reader.close();
						writer.close();
						return false;
					}

					String newLine = line.substring(0, start);
					newLine += domain;
					newLine += line.substring(end);
					writer.println(newLine);
				}

				line = reader.readLine();
			}
			reader.close();
			writer.close();

			File configFile = new File(configFileName);
			configFile.delete();

			new File(newConfigFileName);
			newConfigFile.renameTo(configFile);
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "changeDomainOfUpdateUrl failed: ");
			TradingConsole.traceSource.trace(TraceType.Error, ex);
			return false;
		}
		return true;
	}

	public static class DeliveryHelper
	{
		public static boolean isDeliveryAccount(Account account, Instrument instrument)
		{
			TradingConsole tradingConsole = account.get_TradingConsole();

			for(Order order : tradingConsole._orders.values())
			{
				if(order.get_Account() == account && order.get_Instrument() == instrument && order.get_Account().get_Select()
				   && order.get_Transaction().get_Instrument().get_Category().equals(InstrumentCategory.Physical)
					&& order.get_IsOpen() && order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0
					&& (order.get_PhysicalTradeSide() == PhysicalTradeSide.Buy || order.get_PhysicalTradeSide() == PhysicalTradeSide.Deposit ))
				{
					TradePolicyDetail tradePolicyDetail =
						tradingConsole._settingsManager.getTradePolicyDetail(order.get_Account().get_TradePolicyId(), order.get_Instrument().get_Id());
					if(tradePolicyDetail.isAllowed(PhysicalTradeSide.Delivery))
					{
						Guid deliveryChargeId = tradePolicyDetail.get_DeliveryChargeId();
						if(deliveryChargeId == null)
						{
							return true;
						}
						else
						{
							DeliveryCharge deliveryCharge = tradingConsole.get_SettingsManager().getDeliveryCharge(deliveryChargeId);
							if (deliveryCharge.get_PriceType().equals(MarketValuePriceType.UnitFixAmount))
							{
								return true;
							}
							else if (deliveryCharge.get_PriceType().equals(MarketValuePriceType.DayOpenPrice))
							{
								if (instrument.get_LastQuotation() != null && instrument.get_LastQuotation().get_Open() != null)
									return true;
							}
							else
							{
								if (instrument.get_Quotation() != null && !instrument.get_Quotation().get_Timestamp().before(instrument.get_OpenTime()))
									return true;
							}
						}
					}
				}
			}

			return false;
		}

		public static boolean hasCanDeliveryInventory(TradingConsole tradingConsole, Account account, Instrument instrument)
		{
			for(Order order : tradingConsole._orders.values())
			{
				if(order.get_Instrument() == instrument && order.get_Account() == account
				   && order.get_Transaction().get_Instrument().get_Category().equals(InstrumentCategory.Physical)
					&& order.get_IsOpen() && order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0
					&& order.canDelivery())
				{
					return true;
				}
			}
			return false;
		}


		public static boolean hasInventory(TradingConsole tradingConsole, Account account, Instrument instrument)
		{
			for(Order order : tradingConsole._orders.values())
			{
				if(order.get_Instrument() == instrument && order.get_Account() == account
				   && order.get_Transaction().get_Instrument().get_Category().equals(InstrumentCategory.Physical)
					&& order.get_IsOpen() && order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0
					&& (order.get_PhysicalTradeSide() == PhysicalTradeSide.Buy || order.get_PhysicalTradeSide() == PhysicalTradeSide.Deposit ))
				{
					return true;
				}
			}
			return false;
		}

		public static Collection<MakeOrderAccount> getDeliveryAccounts(TradingConsole tradingConsole, Instrument instrument)
		{
			HashMap<Guid, MakeOrderAccount> deliveryAccounts = new HashMap<Guid, MakeOrderAccount>();

			for(Order order : tradingConsole._orders.values())
			{
				if(order.get_Instrument() == instrument && order.get_Account().get_Select()
				   && order.get_Transaction().get_Instrument().get_Category().equals(InstrumentCategory.Physical)
					&& order.get_IsOpen() && order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0
					&& (order.get_PhysicalTradeSide() == PhysicalTradeSide.Buy || order.get_PhysicalTradeSide() == PhysicalTradeSide.Deposit ))
				{
					Account account = order.get_Account();
					TradePolicyDetail tradePolicyDetail =
						tradingConsole._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), instrument.get_Id());
					if(!tradePolicyDetail.isAllowed(PhysicalTradeSide.Delivery)) continue;
					Guid deliveryChargeId = tradePolicyDetail.get_DeliveryChargeId();
					if (deliveryChargeId != null)
					{
						DeliveryCharge deliveryCharge = tradingConsole.get_SettingsManager().getDeliveryCharge(deliveryChargeId);
						if (deliveryCharge.get_PriceType().equals(MarketValuePriceType.DayOpenPrice))
						{
							if (instrument.get_LastQuotation() == null || instrument.get_LastQuotation().get_Open() == null) continue;
						}
						else
						{
							if (instrument.get_Quotation() == null || instrument.get_Quotation().get_Timestamp().before(instrument.get_OpenTime())) continue;
						}
					}

					if(!deliveryAccounts.containsKey(account.get_Id()))
					{
						MakeOrderAccount makeOrderAccount =
							MakeOrderAccount.create(tradingConsole, tradingConsole.get_SettingsManager(), account, instrument, true);
						deliveryAccounts.put(account.get_Id(), makeOrderAccount);
					}
				}
			}

			return deliveryAccounts.values();
		}
	}

	private static class Updater implements Runnable
	{
		private TradingConsole tradingConsole;

		Updater(TradingConsole tradingConsole)
		{
			this.tradingConsole = tradingConsole;
		}

		private static boolean canConnect(String urlString)
		{
			URL url = null;
			try
			{
				url = new URL(urlString);
				URLConnection connection = url.openConnection();
				connection.connect();
				return connection.getInputStream() != null;
			}
			catch (Exception ex)
			{
				return false;
			}
		}

		public void run()
		{
			AutoUpdater autoUpdater = new AutoUpdater();
			try
			{
				String[] urlStrings = this.tradingConsole.getUpdateUrlString();
				if(urlStrings != null && urlStrings.length > 0)
				{
					for (String urlString : urlStrings)
					{
						if (canConnect(urlString))
						{
							TradingConsole.setUpdateDescriptorUrl(urlString);
							break;
						}
					}
				}
				/*String updatesXmlUrl = TradingConsole.getUpdateDescriptorUrl();
					 TradingConsole.traceSource.trace(TraceType.Information, "updatesXmlUrl = " + updatesXmlUrl == null ? "null" : updatesXmlUrl);
					 String installFileFullPath = DirectoryHelper.combine(AppToolkit.get_Install4JDirectory(), "install.exe");

					 autoUpdater.checkAndUpdate(updatesXmlUrl, installFileFullPath);
					 if (autoUpdater.get_HasNewVersion())
					 {
				 try
				 {
				  TradingConsole.traceSource.trace(TraceType.Information, "New version available, begin to upgrade");

				  //tradingConsole.messageNotify("New version found, program will be upgraded", true);
				 }
				 finally
				 {
				  Runtime.getRuntime().exit(0);
				 }
					 }*/

				ApplicationLauncher.launchApplication("17", null, false, new ApplicationLauncher.Callback()
				{
					public void exited(int code)
					{
						TradingConsole.traceSource.trace(TraceType.Information, "Auot updater exit with code = " + code);
					}

					public void prepareShutdown()
					{
					}
				});
			}
			catch (Throwable ex)
			{
				try
				{
					TradingConsole.traceSource.trace(TraceType.Error, ex);
					//tradingConsole.messageNotify("Failed to check new version, program will exit", true);
				}
				finally
				{
					//Runtime.getRuntime().exit(0);
				}
			}
		}
	}

	private static void launchAutoUpdater(TradingConsole tradingConsole)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Begin ApplicationLauncher.launchApplication");
		Thread thread = new Thread(new Updater(tradingConsole));
		thread.setDaemon(true);
		thread.start();
		TradingConsole.traceSource.trace(TraceType.Information, "End ApplicationLauncher.launchApplication");
	}


	public void init()
	{

		if (!AppToolkit.checkConfigurateFile())
		{
			return;
		}
		Settings.initialize();
		ServiceTimeoutSetting.initialize();

		PublicParametersManager.initialize();

		System.getProperties().put("sun.net.client.defaultConnectTimeout", "30000");
		System.getProperties().put("sun.net.client.defaultReadTimeout", "30000");
		System.getProperties().put("Omnicare.Mutex.ClassName", "tradingConsole.Mutex2");

		TradingConsole.traceSource.trace(TraceType.Information, "Begin TradingConsole.init");

		this.proxySet();

		String keyStoreFileFullPath = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(), "TradingConsole.keystore");
		try
		{
			ResourceHelper.fetchToFile("Configuration", "TradingConsole.keystore", keyStoreFileFullPath, true);
		}
		catch (IOException iOException)
		{
			TradingConsole.traceSource.trace(TraceType.Error, iOException);
		}
		if (new File(keyStoreFileFullPath).exists())
		{
			System.getProperties().put("javax.net.ssl.trustStore", keyStoreFileFullPath);
			System.getProperties().put("javax.net.ssl.trustStorePassword", "12345678");
		}

		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.ServiceManager.initialize()");
		this.setServiceManager();
		this._isInitialized = false;
		this._tradingConsoleServer = new TradingConsoleServer(this);
		this.connectionManager = new ConnectionManager(this);


		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.Language.initialize()");
		Language.initialize();

		TradingConsole.traceSource.trace(TraceType.Information, "UISettingsManager.setSettingsDefaultValue()");
		UISettingsManager.setSettingsDefaultValue();

		TradingConsole.traceSource.trace(TraceType.Information, "End TradingConsole.init");
	}





	public void start()
	{
		if (TradingConsole.traceSource == null)
		{
			return;
		}

		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.start");
		this._loginInformation = LoginInformation.create();

		this._recoverPasswordManager = RecoverPasswordManager.Create();
		this._settingsManager = new SettingsManager();
		this._transactions = new HashMap<Guid, Transaction> ();
		this._orders = new HashMap<Guid, Order> ();
		this._workingOrders = new HashMap<Guid, Order> ();
		this._openOrders = new HashMap<Guid, Order> ();
		this._quotations = new HashMap<Guid, Quotation> ();
		this._interestRateOrderIds = new HashMap<Guid, Guid> ();
		this._logs = new HashMap<Guid, Log> ();
		this._settingsProvider = new SettingsProvider(this, this._settingsManager);
		this._quotationProvider = new QuotationProvider(this);
		this._chartManager = new AioChartManager(this._settingsProvider, this._quotationProvider);

		FormBackColor.mainForm = this._serviceManager.get_DefaultMainFormColor();

		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.connect()");
		try{
			this.connect(false);
		}
		catch(Exception ex){
			this.logger.error("start failed",ex);
			this.disconnect(false);
		}
	}

	public SettingsManager get_SettingsManager()
	{
		return this._settingsManager;
	}

	public InstrumentStateManager get_InstrumentStateManager()
	{
		return this._instrumentStateManager;
	}

	public void stop()
	{
	}

	public void destroy()
	{
	}

	public void dumpOrders(PrintWriter writer)
	{
		writer.println("-------------------All open orders--------------------------");
		this.dumpOrders(writer, this._openOrders);
		writer.println("---------------------------------------------");

		writer.println();
		writer.println("-------------------All orders--------------------------");
		this.dumpOrders(writer, this._orders);
		writer.println("---------------------------------------------");

		writer.println();
		writer.println("-------------------All working orders--------------------------");
		this.dumpOrders(writer, this._workingOrders);
		writer.println("---------------------------------------------");
	}

	private void dumpOrders(PrintWriter writer, HashMap<Guid, Order> orders)
	{
		writer.println("Count = " + orders.size());
		for (Order order : orders.values())
		{
			order.dump(writer);
		}
	}

	public void loggedIn(final LoginResult loginResult)
	{
		try
		{
			this.loginInHelper(loginResult);
			boolean needActiveAccount = loginResult.get_NeedActiveAccount();
			if (!needActiveAccount)
			{
				enterMainForm(loginResult);
			}
			else
			{
				ActivateAccountForm activateAccountForm = new ActivateAccountForm(this._mainForm, this);
				activateAccountForm.show();
			}
			ExecutorService executorService =Executors.newCachedThreadPool();
			executorService.execute(new Runnable(){
				public void run(){
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							_chartManager.refreshInstrument();
							_chartManager.refreshProperties();
							TradingConsole.traceSource.trace(TraceType.Information, "Begin chartManager.clearStorage");
							_chartManager.clearStorage();
							for (Instrument instrument : _settingsManager.getInstruments().values())
							{
								instrument.saveRealTimeChartData();
							}
							TradingConsole.traceSource.trace(TraceType.Information, "End chartManager.clearStorage");
						}
					});

				}
			}
			);
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			String stackTrace = FrameworkException.getStackTrace(throwable);
			TradingConsole.traceSource.trace(TraceType.Error, stackTrace);
			AlertDialogForm.showDialog(this._mainForm, null, true, Login.lblLoginPrompt23);
			this._loginInformation.set_LoginStatus(LoginStatus.Logouted);
			this._mainForm.refreshLoginInformation();
			this.loggedOut();
			return;
		}
	}

	private void loginInHelper(LoginResult loginResult) throws MalformedURLException
	{
		String serviceUrl = this._serviceManager.get_ServiceUrl();
		final String host = new URL(serviceUrl).getHost();
		final TradingConsole self = this;
		ExecutorService executorService =Executors.newCachedThreadPool();
		executorService.execute(new Runnable(){
			public void run(){

				if (TradingConsole.changeDomainOfUpdateUrl(host))
				{
					TradingConsole.launchAutoUpdater(self);
				}

			}
		});

		this.showMainForm();
		Trace.setLogProperties(this);
		if (PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem)
		{
			TraceManager.emptyAllLogFiles(false);
		}

		TradingConsole.traceSource.trace(TraceType.Information, AppToolkit.getSystemInfo());
		TradingConsole.traceSource.trace(TraceType.Information, "------------------------------------------------------------");
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.loggedIn");

		this._loginInformation.set_LoginStatus(LoginStatus.LoginSucceed);
		this._mainForm.loggedIn(loginResult);

		XmlNode parameterXmlNode = loginResult.get_Parameter();
		if (parameterXmlNode == null)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "Parameter: " + Language.InitializeParameterError);
		}
		else
		{
			Parameter.initialize(parameterXmlNode);
			//TradingConsole.traceSource.trace(TraceType.Information, parameterXmlNode.get_Value());
			TradingConsole.traceSource.trace(TraceType.Information,
											 "Trading fact url = " +
											 (StringHelper.isNullOrEmpty(Parameter.tradingFactUrl) ? "null" : Parameter.tradingFactUrl));
			if (!StringHelper.isNullOrEmpty(Parameter.tradingFactUrl))
			{
				this._mainForm.showTradingFactButton();
			}
		}

		this.tradingAccountInstance(loginResult);

		this._recoverPasswordManager.initialize(loginResult.get_RecoverPasswordData(), this._serviceManager.getRecoverPasswordAnswerCount());
		ActivateAccount.initialize();
	}


	public boolean needRecoverPasswordQuotationSetting()
	{
		return !StringHelper.isNullOrEmpty(this._serviceManager.getSelectedForgetPasswordLink())
			&& this._serviceManager.getRecoverPasswordAnswerCount() > 0;
	}

	private void tradingAccountInstance(LoginResult loginResult)
	{
		//if (this.needRecoverPasswordQuotationSetting()) return;

		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.processTradingAccount()");

		this._tradingAccountManager = TradingAccountManager.Create();
		DataSet tradingAccountData = loginResult.get_TradingAccountData();
		if (tradingAccountData != null && tradingAccountData.get_Tables().get_Count() > 0)
		{
			this._tradingAccountManager.initialize(loginResult.get_TradingAccountData());
		}
	}

	public void enterMainForm(final LoginResult loginResult)
	{
		try
		{
			ColumnUIInfoManager.load(AppToolkit.getColumnVisibilityPersistentFileName());
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "[TradingConsole.enterMainForm] " + FrameworkException.getStackTrace(ex));
		}

		TradingConsoleServer.appTime.start();
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.enterMainForm()");

		if (Login.getRiskDisclosureStatementType().equals(RiskDisclosureStatementType.Common)
			&& !StringHelper.isNullOrEmpty(Login.endUser))
		{
			AgreementForm.showDialog(this._mainForm, Login.btnVisableOKCaption, true, Login.endUser);
		}

		//this.unbind();
		this.initializeForm();
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new Runnable()
		{
			public void run()
			{
				try
				{
					PalceLotNnemonic.load();
				}
				catch (Exception ex)
				{}
			}
		});
		final Semaphore semaphore = new Semaphore(1, true);
		executorService.execute(new Runnable()
		{
			public void run()
			{
				try
				{
					semaphore.acquire();
					initData(semaphore, loginResult);
				}
				catch (Exception ex)
				{
					TradingConsole.traceSource.trace(TraceType.Error, "[TradingConsole.enterMainForm] " + FrameworkException.getStackTrace(ex));
				}
			}
		});

		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException e)
		{
			this.logger.error(e);
		}

		executorService.execute(new Runnable()
		{
			public void run()
			{
				try
				{
					semaphore.acquire();
				}
				catch (Exception ex)
				{}
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						enterMainFormHelper();
						semaphore.release();
					}
				});
			}
		});
	}

	private void enterMainFormHelper(){
		if (this.get_TradingAccountManager().needActiveAccount())
		{
			this.disconnect(false);
			return;
		}

		//this._mainForm.setTitle("Data loaded");
		this._mainForm.addChildFrames();

		this._mainForm.refreshLoginInformation();
		this.workSchedulerStart();

		this._mainForm.showNews(Parameter.isHasNews);
		if (Parameter.isHasNews)
		{
			this.rebindNews();
		}

		this.rebindMessage();
		this._mainForm.windowMenuInit2();

		if (!StringHelper.isNullOrEmpty(this._serviceManager.get_PlacardUrl()))
		{
			BrowserControl.displayURL(this._serviceManager.get_PlacardUrl(), true);
		}

		if(!this._settingsManager.get_SystemParameter().get_ShowChartAsDefultInTrader())
		{
			this._mainForm.getDockingManager().hideFrame("ChartFrame1");
		}

		if(this._settingsManager.get_IsForRSZQ())
		{
			this._mainForm.getDockingManager().hideFrame("NewsFrame");
			this._mainForm.getDockingManager().hideFrame("ChartFrame1");
			if(this._mainForm.get_FloatingChildFrames())
			{
				this._mainForm.getDockingManager().floatFrame("TradingPanelListFrame", this._mainForm.getFloatingRectangle("TradingPanelListFrame", false), false);
			}
		}
		this.applyUserLayout();
		if (this._hasMessage && this._mainForm.get_MessageTable().getRowCount() > 0)
		{
			Message message = (Message)this._mainForm.get_MessageTable().getObject(0);
			MessageContentForm messageContentForm = new MessageContentForm(this, this._settingsManager, message);
			messageContentForm.setAlwaysOnTop(true);
			messageContentForm.show();
		}

	}




	public void initData(Semaphore semaphore,LoginResult loginResult)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initData()");
		if (this._instrumentStateManager != null)
		{
			this._instrumentStateManager.stop();
		}

		try
		{
			DataSet ds ;
			if(loginResult == null){
				GetInitDataResult getInitDataResult = this._tradingConsoleServer.getInitData(this._commandSequence);
				ds= getInitDataResult.get_DataSet();
				this._commandSequence = getInitDataResult.get_CommandSequence();
			}
			else{
				ds = loginResult.getInitData();
				this._commandSequence = loginResult.getCommandSequence();
			}
			this.initialize(ds, false,semaphore);
			if(firstShowInstrumentSelection) return;
			this._bankAccountHelper = new BankAccountHelper(this);
			this._bankAccountHelper.refresh();
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
			this.logger.info(throwable.getStackTrace());
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
			String stackTrace = FrameworkException.getStackTrace(throwable);
			TradingConsole.traceSource.trace(TraceType.Error, stackTrace);
			AlertDialogForm.showDialog(this._mainForm, null, true, Login.lblLoginPrompt23);
			this._loginInformation.set_LoginStatus(LoginStatus.Logouted);
			this._mainForm.refreshLoginInformation();
			this.loggedOut();
			return;
		}

		TradingConsole.traceSource.trace(TraceType.Information, "set_LoginStatus(LoginStatus.Ready)");

		this._loginInformation.set_LoginStatus(LoginStatus.Ready);

		TradingConsole.traceSource.trace(TraceType.Information, "setConnectStatus()");
		this.setConnectStatus();

		TradingConsole.traceSource.trace(TraceType.Information, "setMenuVisible()");
		this._mainForm.setMenuVisible();
		this._mainForm.setBackground();
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.Ready!");

		this._mainForm.tradingAccountFormShow();

		Runnable getOtherData = new Runnable()
		{
			public void run()
			{
				//_mainForm.showFirstChart();
				getNewsList();
				getMessageList();
			}
		};
		Thread thread = new Thread(getOtherData);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.run();

		MaxMoveAutoFillHelper.initialize();
	}



	private void closeAllPopWindow()
	{
		if (this._mainForm != null)
		{
			this._mainForm.closeAllPopWindow();
		}
	}

	private void clearScene()
	{
		this.clearScene(false);
	}

	private void clearScene(boolean isResetSystem)
	{
		this.closeAllPopWindow();

		this._isInitialized = false;

		this.workSchedulerStop();
		this.resetSystemSchedulerStop();
		this.verifyTransactionSchedulerStop();
		this._settingsManager.getAccountsForCutSchedulerStop();
		this.unbind();

		try
		{
			this._settingsManager.clear();

			this._transactions.clear();
			this._orders.clear();
			this._workingOrders.clear();
			this._openOrders.clear();
			this._quotations.clear();
			this._interestRateOrderIds.clear();
			if (!isResetSystem)
			{
				this._logs.clear();
			}

			/*if(this._bindingManager == null)
			 {
			 this._bindingManager.clear();
			 }*/
			//will cause exception after called more than twice, use followed two lines
			this._bindingManager = null;
			this._mainForm.initAccountStatusTable();
			this._mainForm.initAccountListTable();
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void resetSystem(boolean isRefresh)
	{
		this.resetSystem(isRefresh, true);
	}

	public void resetSystem(boolean isRefresh, boolean saveLayout)
	{
		this.logger.debug("reset system");
		if(saveLayout) this.saveLayout();

		this.clearScene(true);

		try
		{
			if(isRefresh)
			{
			}
			ColumnUIInfoManager.load(AppToolkit.getColumnVisibilityPersistentFileName());
		}
		catch (Exception ex1)
		{
		}

		this.initializeForm();

		this._mainForm.refreshLoginInformation();

		this.workSchedulerStart();

		this.initData(null,null);
		if (this._loginInformation.get_LoginStatus() == LoginStatus.LoginSucceed
			|| this._loginInformation.get_LoginStatus() == LoginStatus.Ready)
		{
			this._mainForm.showLog();
			if (this._settingsManager.get_Customer().get_ShowLog())
			{
				this.rebindLog();
			}

			this._mainForm.showNews(Parameter.isHasNews);
			if (Parameter.isHasNews/* && !this._settingsManager.get_IsForRSZQ()*/)
			{
				if (isRefresh)
				{
					this.getNewsList();
				}
				this.rebindNews();
			}

			this.rebindMessage();

			this._mainForm.windowMenuInit2();
			this._mainForm.loadCharts();

			for (Instrument instrument : this._settingsManager.getInstruments().values())
			{
				instrument.saveRealTimeChartData();
			}

			this.calculatePLFloat();
		}
	}

	public TradingConsole()
	{
	}

	private void initializeForm()
	{
		this.initializeForm(false);
	}

	private void initializeForm(boolean isRefresh)
	{
		this._settingsManager.setUISettings();
		this.bindingManager = new BindingManager();

		if(isRefresh)
		{
			this._bindingSourceForTradingPanel.removeAll();
			this._bindingSourceForSummaryPanel.removeAll();
			this._bindingSourceForLogPanel.removeAll();
			this._bindingSourceForNewsPanel.removeAll();
			this._bindingSourceForMessagePanel.removeAll();
			this._bindingSourceForWorkingOrderList.removeAll();
			this._bindingSourceForTradingPanel.removeAll();
			this._bindingSourceForOpenOrderList.removeAll();
		}
		else
		{
			this._bindingSourceForTradingPanel = new tradingConsole.ui.grid.BindingSource();
			/*{
			 private CellStyle defaultCellStyle = null;
@Override
			 public CellStyle getCellStyleAt(int row, int column)
			 {
			  if(column == 2)
			  {
			   CellStyle cellStyle = super.getCellStyleAt(row, column);
			   if (cellStyle != null && cellStyle.getIcon() == null)
			   {
			 Image image = Quotation.get_PicNotChange();
			 cellStyle.setIcon(new ImageIcon(image));
			   }
			   if(this.defaultCellStyle == null)
			   {
			 this.defaultCellStyle = new CellStyle();
			 Image image = Quotation.get_PicNotChange();
			 this.defaultCellStyle.setIcon(new ImageIcon(image));
			   }
			   return cellStyle == null ? this.defaultCellStyle : cellStyle;
			  }
			  else
			  {
			   return super.getCellStyleAt(row, column);
			  }
			 }
			   };*/

			this._hierarchicalTradingSummaryTableModel = new HierarchicalTradingSummaryTableModel();
			this._bindingSourceForSummaryPanel = new tradingConsole.ui.grid.BindingSource(this._hierarchicalTradingSummaryTableModel);
			this._hierarchicalTradingSummaryTableModel.set_Owner(this._bindingSourceForSummaryPanel);


			this._bindingSourceForLogPanel = new tradingConsole.ui.grid.BindingSource();
			this._bindingSourceForNewsPanel = new tradingConsole.ui.grid.BindingSource();
			this._bindingSourceForMessagePanel = new tradingConsole.ui.grid.BindingSource();
			this._bindingSourceForWorkingOrderList = new tradingConsole.ui.grid.BindingSource();

			this._bindingSourceForNotConfirmedPendingList = new BindingSource();

			this._hierarchicalOpenOrderTableModel = new HierarchicalOpenOrderTableModel();
			this._bindingSourceForOpenOrderList = new tradingConsole.ui.grid.BindingSource(_hierarchicalOpenOrderTableModel);
			this._hierarchicalOpenOrderTableModel.set_Owner(this._bindingSourceForOpenOrderList);
			if (ColorSettings.useBlackAsBackground)
			{
				this._bindingSourceForNotConfirmedPendingList.useBlackAsBackground();
				this._bindingSourceForTradingPanel.useBlackAsBackground();
				this._bindingSourceForSummaryPanel.useBlackAsBackground();
				this._bindingSourceForLogPanel.useBlackAsBackground();
				this._bindingSourceForNewsPanel.useBlackAsBackground();
				this._bindingSourceForMessagePanel.useBlackAsBackground();
				this._bindingSourceForWorkingOrderList.useBlackAsBackground();
				this._bindingSourceForOpenOrderList.useBlackAsBackground();
			}
		}

		this.fillInstrumentSpanGrid();
		Instrument.initializeTradingPanel(this._settingsManager, this._mainForm.get_InstrumentTable(), Instrument.tradingPanelKey,
										  this._settingsManager.getInstruments().values(),
										  this._bindingSourceForTradingPanel, this._serviceManager.getPriceAlign());

		Instrument.initializeSummaryPanel(this._mainForm.get_PositionSummaryFrame(), this._mainForm.get_SummaryTable(), Instrument.summaryPanelKey,
										  this._settingsManager.getInstrumentsForSummary(),
										  this._bindingSourceForSummaryPanel);
		InstrumentPLFloatHelper.initialize(this);
		Log.initializeLogPanel(this._mainForm.get_LogFrame(), this._mainForm.get_LogTable(), Log.logPanelKey,
							   this._logs.values(),
							   this._bindingSourceForLogPanel);
		News.initialize(this._mainForm.get_NewsFrame(), this._mainForm.get_NewsTable(), News.newsPanelKey,
						this._settingsManager.get_News().values(),
						this._bindingSourceForNewsPanel);
		Message.initialize(this._mainForm.get_MessageFrame(), this._mainForm.get_MessageTable(), Message.messagePanelKey,
						   this._settingsManager.get_Messages().values(),
						   this._bindingSourceForMessagePanel);
		Order.initializeWorkingOrderList(this._settingsManager, this._mainForm.get_OrderTable(), Order.workingOrdersKey, this._workingOrders.values(),
										 this._bindingSourceForWorkingOrderList);
		adjustWorkingListColumn(true);
		if(this._bindingSourceForWorkingOrderList.getRowCount() > 0)
		{
			String key = this._mainForm.get_OrderTable().get_DockableFrame().getKey();
			this._mainForm.getDockingManager().showFrame(key);
		}
		TableModelListener workingOrderTableModelListener = new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getType() == TableModelEvent.INSERT)
				{
					String key = _mainForm.get_OrderTable().get_DockableFrame().getKey();
					_mainForm.getDockingManager().showFrame(key);
				}

				if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE)
				{
					adjustWorkingListColumn(false);
				}
			}
		};
		this._bindingSourceForWorkingOrderList.addTableModelListener(workingOrderTableModelListener);


		Order.initializeOpenOrderList(this._settingsManager, this._mainForm.get_OpenOrderTable(), Order.openOrdersKey, this.getMarginOpenOrders(),
									  this._bindingSourceForOpenOrderList);
		adjustOpenListColumn(true);
		TableModelListener openOrderTableModelListener = new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE)
				{
					adjustOpenListColumn(false);
				}
			}
		};
		this._bindingSourceForOpenOrderList.addTableModelListener(openOrderTableModelListener);


		Order.initializeWorkingOrderList(this._settingsManager, this._mainForm.get_NotConfirmedPendingOrderTable(), Order.notConfirmedPendingOrdersKey, this._workingOrders.values(),
										 this._bindingSourceForNotConfirmedPendingList, true);
		this._mainForm.get_QueryPanel().initailize();
	}

	private void adjustOpenListColumn(boolean isInitialize)
	{
		boolean shouldShowRebateColumn = false;

		for(int index = 0; index < this._bindingSourceForOpenOrderList.getRowCount(); index++)
		{
			Order order = (Order)(this._bindingSourceForOpenOrderList.getObject(index));
			if(order.hasRebate())
			{
				shouldShowRebateColumn = true;
				break;
			}
		}

		if(isInitialize)
		{
			if(!shouldShowRebateColumn)
			{
				int autoLimitColumn = this._bindingSourceForOpenOrderList.getColumnByName(OrderColKey.RebateString);
				com.jidesoft.grid.TableColumnChooser.hideColumn(this.get_MainForm().get_OpenOrderTable(), autoLimitColumn);
			}
		}
		else if(shouldShowRebateColumn)
		{
			int autoLimitColumn = this._bindingSourceForOpenOrderList.getColumnByName(OrderColKey.RebateString);
			if(!com.jidesoft.grid.TableColumnChooser.isVisibleColumn(this.get_MainForm().get_OpenOrderTable().getColumnModel(), autoLimitColumn))
			{
				com.jidesoft.grid.TableColumnChooser.showColumn(this.get_MainForm().get_OpenOrderTable(), autoLimitColumn, -1);
			}
		}
	}

	private void adjustWorkingListColumn(boolean isInitialize)
	{
		boolean shouldShowRebateColumn = false;
		for(int index = 0; index < this._bindingSourceForWorkingOrderList.getRowCount(); index++)
		{
			Order order = (Order)(this._bindingSourceForWorkingOrderList.getObject(index));
			if(order.hasRebate())
			{
				shouldShowRebateColumn = true;
				break;
			}
		}

		if(isInitialize)
		{
			if(!shouldShowRebateColumn)
			{
				int autoLimitColumn = this._bindingSourceForWorkingOrderList.getColumnByName(OrderColKey.RebateString);
				com.jidesoft.grid.TableColumnChooser.hideColumn(this.get_MainForm().get_OrderTable(), autoLimitColumn);
			}
		}
		else if(shouldShowRebateColumn)
		{
			int autoLimitColumn = this._bindingSourceForWorkingOrderList.getColumnByName(OrderColKey.RebateString);
			if(!com.jidesoft.grid.TableColumnChooser.isVisibleColumn(this.get_MainForm().get_OrderTable().getColumnModel(), autoLimitColumn))
			{
				com.jidesoft.grid.TableColumnChooser.showColumn(this.get_MainForm().get_OrderTable(), autoLimitColumn, -1);
			}
		}
	}

	private Collection<Order> getMarginOpenOrders()
	{
		Collection<Order> marginOpenOrders = new ArrayList<Order>();
		for(Order order : this._openOrders.values())
		{
			if(order.get_Instrument().get_Category().equals(InstrumentCategory.Margin))
			{
				marginOpenOrders.add(order);
			}
		}
		return marginOpenOrders;
	}

	public void initializeQueryOrderList()
	{
		try
		{
			Order.initializeWorkingOrderList(this._settingsManager, this._mainForm.get_NotConfirmedPendingOrderTable(), Order.notConfirmedPendingOrdersKey,
											 this._workingOrders.values(),
											 this._bindingSourceForNotConfirmedPendingList, true);
		}
		catch(Exception e)
		{

		}
	}

	public void getNewsList()
	{
		if (Parameter.isHasNews && !this._settingsManager.get_IsGotNews())
		{
			DateTime appTime = TradingConsoleServer.appTime();
			DataSet dataSet = this.get_TradingConsoleServer().getNewsList2("", Parameter.newsLanguage, appTime);
			this._settingsManager.setNews(dataSet);
			this.rebindNews();
		}
	}

	public void getMessageList()
	{
		if (!this._settingsManager.get_IsGotMessage())
		{
			DataSet dataSet = this.get_TradingConsoleServer().getMessages();
			this._settingsManager.setMessages(dataSet);
		}
	}

	private void unbind()
	{
		try
		{
			Instrument.unbind(Instrument.tradingPanelKey, this._bindingSourceForTradingPanel);
			Instrument.unbind(Instrument.summaryPanelKey, this._bindingSourceForSummaryPanel);
			Log.unbind(Log.logPanelKey, this._bindingSourceForLogPanel);
			News.unbind(News.newsPanelKey, this._bindingSourceForNewsPanel);
			Message.unbind(Message.messagePanelKey, this._bindingSourceForMessagePanel);
			Order.unbind(Order.workingOrdersKey, this._bindingSourceForWorkingOrderList);
			Order.unbind(Order.openOrdersKey, this._bindingSourceForOpenOrderList);
			Order.unbind(Order.notConfirmedPendingOrdersKey, this._bindingSourceForNotConfirmedPendingList);
		}
		catch (Throwable exception)
		{
		}
	}

	public void rebindSummary()
	{
		this.rebindSummary(false);
	}

	public void rebindSummary(boolean unbindFirst)
	{
		if(unbindFirst) Instrument.unbind(Instrument.summaryPanelKey, this._bindingSourceForSummaryPanel);
		if (this._mainForm != null)
		{
			Instrument.initializeSummaryPanel(this._mainForm.get_PositionSummaryFrame(), this._mainForm.get_SummaryTable(), Instrument.summaryPanelKey,
										  this._settingsManager.getInstrumentsForSummary(),
										  this._bindingSourceForSummaryPanel);
			this.sortForSummaryPanel();
		}
	}

	public void rebindLog()
	{
		//Michael????
		int tableWidth = 850; //this._mainForm.getAccountPanel().getSplitLocation();
		Log.rebindLogPanel(this._mainForm.get_LogFrame(), this._mainForm.get_LogTable(), Log.logPanelKey,
						   this._logs.values(),
						   this._bindingSourceForLogPanel);
//		this.sortForLogPanel();
		this._mainForm.get_LogTable().doLayout();
	}

	public void rebindNews()
	{
		News.rebind(this._mainForm.get_NewsFrame(), this._mainForm.get_NewsTable(), News.newsPanelKey,
					this._settingsManager.get_News().values(),
					this._bindingSourceForNewsPanel);
		this.sortForNewsPanel();
	}

	public void addNews(News news)
	{
		this._bindingSourceForNewsPanel.add(news);
	}

	public void rebindMessage()
	{
		Message.rebind(this._mainForm.get_MessageFrame(), this._mainForm.get_MessageTable(), Message.messagePanelKey,
					   this._settingsManager.get_Messages().values(),
					   this._bindingSourceForMessagePanel);
		this.sortForMessagePanel();
		this._mainForm.get_MessageTable().doLayout();
	}

	public void finalize() throws Throwable
	{
		this.workSchedulerStop();
		this.resetSystemSchedulerStop();
		this.verifyTransactionSchedulerStop();
		this._settingsManager.getAccountsForCutSchedulerStop();

		if (this._tradingConsoleServer != null)
		{
			this._tradingConsoleServer.finalize();
		}

		super.finalize();
	}

	public void timeAdjusted()
	{
		TradingConsole.traceSource.trace(TraceType.Information, Language.TimeAdjusted);
	}

	public void timestampCheckFailed()
	{
		this.logger.debug("timestampCheckFailed");
		this.disconnect(false);

		String message = Language.TimestampCheckFailed;

		this.messageNotify(message, true);
	}

	public void kickout()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "kickout");
		this.logger.debug("kickout");
		this.disconnect(false);

		TradingConsole.traceSource.trace(TraceType.Information, "kickout---messageNotify");
		String message = Language.WebServiceGetCommandsAlert0
			+ TradingConsole.enterLine
			+ Language.WebServiceGetCommandsAlert1
			+ TradingConsole.enterLine
			+ Language.WebServiceGetCommandsAlert2;

		this.messageNotify(message, true);
	}

	public void loggedOut()
	{
		this.logger.debug("logged out");
		TradingConsole.traceSource.trace(TraceType.Information, "loggedOut");
		this.disconnect(false);
	}

	public void reConnect(boolean isRecover)
	{
		try
		{
			this.disconnect(false);
			Language.initialize();
			this.destoryMainForm();
			this.initializeForm();
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}

		this.connect(isRecover);
	}

	public void connect(boolean isRecover)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.setConfiguration()");
		this.proxySet();
		Login.initialize();
		final ServiceManager editingServiceManager = ServiceManager.Create();
		LoginForm loginForm = new LoginForm(this, isRecover,editingServiceManager);
		loginForm.toFront();
		loginForm.show();
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new Runnable(){
			public void run()
			{
				try
				{
					Settings.setHostName(editingServiceManager.getSelectedHost());
					Settings.setPort(editingServiceManager.getMapPort());
					Settings.setIsDestinationChanged(false);
					connectHelper();
				}
				catch (Exception ex)
				{}
			}
		});

	}


	public void connectHelper() throws TcpInializeTimeoutException
	{
		this.connectionManager.initializeTcpSettings();
		this.connectionManager.startTcpConnect();
	}




	private void destoryMainForm()
	{
		if (this._mainForm != null)
		{
			try
			{
				this._mainForm.setVisible(false);
				this._mainForm.setEnabled(false);
				this._mainForm.dispose();
			}
			catch (Throwable exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exception);
			}
		}
	}

	private void showMainForm()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "begin showMainForm");

		TradingConsole.traceSource.trace(TraceType.Information, "begin new MainForm");
		this._mainForm = new MainForm(this, this._settingsManager);
		TableColumnChooser.MainFrame = this._mainForm;

		TradingConsole.traceSource.trace(TraceType.Information, "end new MainForm");

		DockingManager dockingManager = this._mainForm.getDockingManager();
		dockingManager.setShowGripper(true);
		dockingManager.setOutlineMode(0);
		dockingManager.setAllowedDockSides(DockContext.DOCK_SIDE_VERTICAL);

		this._mainForm.getLayoutPersistence().setProfileKey("MenuBar");
		TradingConsole.traceSource.trace(TraceType.Information, "begin loadLayoutData");
		this._mainForm.getLayoutPersistence().loadLayoutData();
		TradingConsole.traceSource.trace(TraceType.Information, "end loadLayoutData");

		// disallow drop dockable frame to workspace area
		dockingManager.getWorkspace().setAcceptDockableFrame(true);
		dockingManager.getWorkspace().setVisible(false);

		// load layout information from previous session. This indicates the end of beginLoadLayoutData() method above.
		TradingConsole.traceSource.trace(TraceType.Information, "begin setLayout");
		this.setLayout();
		TradingConsole.traceSource.trace(TraceType.Information, "end setLayout");

		this._mainForm.setState(Frame.MAXIMIZED_BOTH);

		Dimension screenSize = AppToolkit.get_ScreenSize();
		Dimension trimStatusScreenSize = new Dimension(screenSize.width, screenSize.height - 30);
		this._mainForm.setSize(trimStatusScreenSize);
		this._mainForm.toFront();
		TradingConsole.traceSource.trace(TraceType.Information, "end showMainForm");

		this._mainForm.applyLanguage();
	}

	private void setLayoutFailed()
	{
		String message = Language.failtedToSetLayout;

		this.messageNotify(message, true);
		this._mainForm.getDockingManager().loadLayoutData();
	}

	private String getTradingConsoleLayoutFilePath()
	{
		String directory;
		if (this.get_LoginInformation().getIsConnected())
		{
			directory = AppToolkit.get_SettingDirectory();
		}
		else
		{
			directory = AppToolkit.get_CompanySettingDirectory();
		}
		String fileName = this._mainForm.get_FloatingChildFrames() ? "floating.layout" : "docking.layout";
		return DirectoryHelper.combine(directory, fileName);
	}

	public void resetLayout(boolean removeCustomerLayout)
	{
		if (removeCustomerLayout)
		{
			String filePath = this.getTradingConsoleLayoutFilePath();
			AppToolkit.deleteFile(filePath);
		}
		this.setLayout();
	}

	public void saveLayout()
	{
		if (this._mainForm == null)
		{
			return;
		}
		if (this._isInitialized)
		{
			try
			{
				ColumnUIInfoManager.save(AppToolkit.getColumnVisibilityPersistentFileName());
				PalceLotNnemonic.save();
			}
			catch (Exception exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exception);
			}
			this._mainForm.saveChartSettings();
			TradingConsole.saveLayout(this.getTradingConsoleLayoutFilePath(), this._mainForm.getDockingManager(), this._mainForm);
		}
	}

	synchronized private static void saveLayout(String fileName, DockingManager dockingManager, MainForm mainForm)
	{
		if (dockingManager != null)
		{
			try
			{
				AppToolkit.deleteFile(fileName);
				FileOutputStream outputStream = new FileOutputStream(fileName);
				dockingManager.saveLayoutTo( (OutputStream)outputStream);
				outputStream.close();

				TradingConsole.saveLayoutDataVersion(dockingManager, mainForm);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
	}

	synchronized public void setLayout()
	{
		this._mainForm.getDockingManager().setVersion(TradingConsole.DockingLayoutVersion);
		this._mainForm.getDockingManager().loadLayoutData();
		this._mainForm.getDockingManager().resetToDefault();
	}

	public void applyUserLayout()
	{
		if (this.get_SettingsManager().get_SystemParameter().get_TradinPanelGridFirst())
		{
			this._mainForm.showFrame2("TradingPanelGridFrame");
		}

		if (this.isLayoutDataVersionValid())
		{
			String destinationFilePath = this.getTradingConsoleLayoutFilePath();
			if (AppToolkit.isExistsFilePath(destinationFilePath))
			{
				this._mainForm.getDockingManager().beginLoadLayoutData();
				this._mainForm.getDockingManager().loadLayoutDataFromFile(destinationFilePath);
			}
		}
	}

	private static void saveLayoutDataVersion(DockingManager dockingManager, MainForm mainFrame)
	{
		String versionFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "layoutversion");
		try
		{
			AppToolkit.deleteFile(versionFilePath);
			OutputStream stream = new FileOutputStream(versionFilePath);
			Properties properties = new Properties();
			properties.setProperty("LayoutVersion", Integer.toString(dockingManager.getVersion()));
			if (mainFrame != null)
			{
				properties.setProperty("FloatingChildFrames", Boolean.toString(mainFrame.get_FloatingChildFrames()));
			}
			properties.store(stream, "");
			stream.close();
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private boolean isLayoutDataVersionValid()
	{
		String versionFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "layoutversion");
		if (AppToolkit.isExistsFilePath(versionFilePath))
		{
			try
			{
				InputStream stream = new FileInputStream(versionFilePath);
				Properties properties = new Properties();
				properties.load(stream);
				int version = Integer.parseInt(properties.getProperty("LayoutVersion"));
				if (!StringHelper.isNullOrEmpty(properties.getProperty("FloatingChildFrames")))
				{
					boolean floatingChildFrames = Boolean.parseBoolean(properties.getProperty("FloatingChildFrames"));
					this._mainForm.set_FloatingChildFrames(floatingChildFrames);
				}
				boolean result = version == TradingConsole.DockingLayoutVersion;
				stream.close();
				return result;
			}
			catch (Exception exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exception);
			}
		}
		return false;
	}

	public void disconnect(boolean isRecover)
	{
		try{
			this.logger.info("disconnect");
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect");
			if (this._tradingConsoleServer != null)
			{
				this._tradingConsoleServer.schedulerStop();
			}
			this._bindingManager = null;

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---saveLayout");
			this.saveLayout();
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---saveLayout end");

			if (this._mainForm != null)
			{
				if (this._mainForm.getDockingManager() != null)
				{
					this._mainForm.getDockingManager().removeAllFrames();
				}

				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---set_LoginStatus");
				this._loginInformation.set_LoginStatus(LoginStatus.Disconnected);
				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---set_LoginStatus end");

				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---this._mainForm.disconnect");
				this._mainForm.disconnect();
				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---this._mainForm.disconnect end");
			}

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---clearScene");
			this.clearScene();
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---clearScene end");

			if (this._tradingConsoleServer != null)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---this._tradingConsoleServer.clearScene");
				this._tradingConsoleServer.clearScene();
				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---this._tradingConsoleServer.clearScene end");
			}

			if (this._quotationProvider != null)
				this._quotationProvider.reset();

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---initializeForm");

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---initializeForm end");

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---windowMenuInit2");
			if (this._mainForm != null)
			{
				this._mainForm.windowMenuInit2();
			}
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect---windowMenuInit2 end");

			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.disconnect end");
		}
		catch(Exception ex){
			this.logger.error(ex.getStackTrace());
		}
		finally{
			if(!isRecover){
				GuidMapping.Default.clear();
				this.connectionManager.closeTcpConnect();
			}
		}
	}






	public void networkDisconnected()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "netWorkDisconnected: "
			+ new FrameworkException("").getStackTrace());
		this.logger.debug("networkDisconnected");
		this.disconnect(false);
		this.messageNotify(Language.SystemDisconnected, false);
	}

	public void setConnectStatus()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if (_mainForm != null)
				{
					_mainForm.setConnectStatus();
				}
			}
		});
	}

	public void setDisconnectStatusDisplay()
	{
		if (this._mainForm != null)
		{
			this._mainForm.setDisconnectStatusDisplay();
		}
	}


	//Scheduler.SchedulerCallback.Action
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		//Clock
		if (schedulerEntry.get_IsRemoved())
		{
			return;
		}
		SwingUtilities.invokeLater(new EDTSafeAction(this, schedulerEntry));
	}

	private static class EDTSafeAction implements Runnable
	{
		private TradingConsole _owner;
		private Scheduler.SchedulerEntry schedulerEntry;

		EDTSafeAction(TradingConsole owner, Scheduler.SchedulerEntry schedulerEntry)
		{
			this._owner = owner;
			this.schedulerEntry = schedulerEntry;
		}

		public void run()
		{
			if (this.schedulerEntry.get_ActionArgs().equals(TradingConsole._actionArgsRefreshAppTime))
			{
				this._owner._mainForm.refreshAppTimeForm(TradingConsoleServer.appTime());
			}
			else if (this.schedulerEntry.get_ActionArgs().equals(TradingConsole._actionArgsVerifyTransactionTime))
			{
				this._owner.verifyTransaction();
			}
			else if (this.schedulerEntry.get_ActionArgs().equals(TradingConsole._actionArgsResetSystemTime))
			{
				TradingConsole.traceSource.trace(TraceType.Information, "!!!!!!To resetSystem by scheduler");
				try
				{
					DateTime endTime = this._owner._settingsManager.get_TradeDay().get_EndTime().addSeconds(AppToolkit.getRandom());
					TradingConsoleServer.appTime.reset();
					DateTime now = TradingConsoleServer.appTime();
					if (now.before(endTime))
					{
						TradingConsole.traceSource.trace(TraceType.Warning, "Scheduler have error!!!");

						now = now.addSeconds(5);
						this._owner._resetSystemScheduleEntry = TradingConsoleServer.scheduler.add(TradingConsole.class.getName(), this._owner,
							TradingConsole._actionArgsResetSystemTime, now);
					}
					else
					{
						TradingConsole.traceSource.trace(TraceType.Information, StringHelper.format("To resetSystem by scheduler, RestTime={0}, now={1}", new Object[]{endTime, now}));
						this._owner.resetSystem(false);
					}
				}
				catch (Throwable exception)
				{
					TradingConsole.traceSource.trace(TraceType.Error, exception);
				}
			}
		}
	}

	public void workSchedulerStart()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "RefreshAppTime.scheduler.add_Begin");
		try
		{
			DateTime beginTime = TradingConsoleServer.appTime();
			TradingConsole.traceSource.trace(TraceType.Information, beginTime.toString());

			TimeSpan timeSpan = TimeSpan.fromSeconds(1);
			this._workScheduleEntry = TradingConsoleServer.scheduler.add(this, TradingConsole._actionArgsRefreshAppTime, beginTime, DateTime.maxValue, timeSpan, true);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
		TradingConsole.traceSource.trace(TraceType.Information, "RefreshAppTime.scheduler.add_End");
	}

	public synchronized void workSchedulerStop()
	{
		if (this._workScheduleEntry != null)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "RefreshAppTime.scheduler.remove_Begin");

			TradingConsoleServer.scheduler.remove(this._workScheduleEntry);
			this._workScheduleEntry = null;

			TradingConsole.traceSource.trace(TraceType.Information, "RefreshAppTime.scheduler.remove_End");
		}
	}

	public void updateData(DataSet dataSet)
	{
		this.initialize(dataSet, false,null);
	}

	public void updateData2(DataSet dataSet)
	{
		this.initialize(dataSet, true,null);
	}

	private void initialize(DataSet dataSet, boolean supressAccountNotify,Semaphore semaphore)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize()");
		this._settingsManager.initialize(this, dataSet,semaphore);
		this._settingsManager.resetCheckArrivedAlertLevel3();
		if (!supressAccountNotify)
		{
			this._settingsManager.accountAlert();
		}
		this.initialize2(dataSet);
		if(firstShowInstrumentSelection) return;

		this.initializePhysical(dataSet);
		//Remarked by Michael on 2008-04-09
		//this._settingsManager.checkAccountsForCut();
		this.calculatePLFloat();
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize() end");
		//this._mainForm.setTitle(this.getOrganizationNameForYauSen());//remember to delete this line
		if (this._settingsManager.get_TraderName() != null)
		{
			this._mainForm.setTitle(this._settingsManager.get_TraderName());
		}
		else if (dataSet != null && dataSet.get_Tables().contains("OrganizationName"))
		{
			this._mainForm.setTitle(this.getOrganizationName(dataSet));
		}
		boolean visibleOfAccountDetail = !this._settingsManager.get_Customer().get_IsNoShowAccountStatus();
		this._mainForm.setDetailVisibleInAccountList(visibleOfAccountDetail, true);
	}

	private String getOrganizationName(DataSet dataSet)
	{
		String organizationName = null;

		DataRowCollection rows = dataSet.get_Tables().get_Item("OrganizationName").get_Rows();
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);

			String name = row.get_Item("Name") == DBNull.value ? null : row.get_Item("Name").toString().trim();
			if (name != null)
			{
				String language = row.get_Item("Language").toString().trim();
				if (language.equalsIgnoreCase(PublicParametersManager.version))
				{
					organizationName = name;
				}
				else if (language.equalsIgnoreCase("ENG") && organizationName == null)
				{
					organizationName = name;
				}
			}
		}
		if (organizationName == null)
		{
			organizationName = this._settingsManager.get_OrganizationName();
		}
		return organizationName;
	}

	private void initializePhysical(DataSet initData)
	{
		PendingInventoryManager.instance.initialize(initData, this);
		InventoryManager.instance.initialize(this._transactions.values());

		TradingConsole.bindingManager.bind(Inventory.bindingKey, InventoryManager.instance.get_Inventories(),
										   InventoryManager.instance.get_BindingSource(false), Inventory.getPropertyDescriptors());

		DataGrid grid = this._mainForm.get_PhysicalInventoryTable();
		grid.setModel(InventoryManager.instance.get_BindingSource(false));
		grid.setHierarchicalColumn(0);
		grid.setComponentFactory(new HierarchicalOpenOrderTableComponentFactory(this._mainForm.get_PhysicalInventoryTableActionListener(), InventoryManager.instance));
		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), false));
		grid.filter();

		SettingsManager settingsManager = this._settingsManager;
		UISetting uiSetting = settingsManager.getUISetting(UISetting.workingOrderListUiSetting);
		TradingConsole.bindingManager.setGrid(Inventory.bindingKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(Inventory.bindingKey, SwingConstants.CENTER, 25, GridFixedForeColor.workingOrderList, Color.white,
												HeaderFont.workingOrderList);

		TradingConsole.bindingManager.bind(Inventory.shortSellBindingKey, InventoryManager.instance.get_ShortSellOrders(),
										   InventoryManager.instance.get_BindingSource(true), Order.getPropertyDescriptorsForShortSell());
		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), false));
		grid.filter();

		grid = this._mainForm.get_PhysicalShotSellTable();
		grid.setModel(InventoryManager.instance.get_BindingSource(true));
		TradingConsole.bindingManager.setGrid(Inventory.shortSellBindingKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(Inventory.shortSellBindingKey, SwingConstants.CENTER, 25, GridFixedForeColor.workingOrderList, Color.white,
												HeaderFont.workingOrderList);
		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), false));
		grid.filter();


		TradingConsole.bindingManager.bind(PendingInventory.bindingKey, PendingInventoryManager.instance.getValues(),
										   PendingInventoryManager.instance.get_BindingSource(), PendingInventory.getPropertyDescriptors());

		grid = this._mainForm.get_PhysicalPendingInventoryTable();
		grid.setModel(PendingInventoryManager.instance.get_BindingSource());
		int column = PendingInventoryManager.instance.get_BindingSource().getColumnByName(PhysicalInventoryColKey.SubmitTime);
		grid.sortColumn(column, true);

		TradingConsole.bindingManager.setGrid(PendingInventory.bindingKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(PendingInventory.bindingKey, SwingConstants.CENTER, 25, GridFixedForeColor.workingOrderList, Color.white,
												HeaderFont.workingOrderList);
		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), false));
		grid.filter();
	}

	private void initialize2(DataSet dataSet)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize2 begin");
		if (dataSet == null)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize2 end");
			return;
		}

		DataTableCollection tables = dataSet.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;

		dataTable = tables.get_Item("Quotation");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("InstrumentID");
				if (this._quotations.containsKey(id))
				{
					this.getQuotation(id).replace(dataRow);
				}
				else
				{
					Instrument instrument = this._settingsManager.getInstrument(id);
					Quotation quotation = new Quotation(instrument, this._settingsManager, dataRow);
					this._quotations.put(id, quotation);
					instrument.set_Quotation(quotation);

					//save first chart data
					//instrument.saveRealTimeChartData();

					instrument.refreshLastQuotation();
				}
			}
		}

		dataTable = tables.get_Item("Transaction");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("ID");
				if (this._transactions.containsKey(id))
				{
					this.getTransaction(id).replace(dataRow);
				}
				else
				{
					Transaction transaction = new Transaction(this, this._settingsManager, dataRow);
					this._transactions.put(id, transaction);

					Guid accountId = transaction.get_Account().get_Id();
					Account account = this._settingsManager.getAccount(accountId);
					account.get_Transactions().put(id, transaction);

					Guid instrumentId = transaction.get_Instrument().get_Id();
					Instrument instrument = this._settingsManager.getInstrument(instrumentId);
					instrument.get_Transactions().put(id, transaction);
				}
			}
		}
		for(Transaction transaction : this._transactions.values())
		{
			if(transaction.get_Instrument() == null) continue;
			if(transaction.get_Instrument().isFromBursa() && transaction.get_SubType() == TransactionSubType.Amend)
			{
				Order assigningOrder = transaction.get_AssigningOrder();
				if(assigningOrder != null)
				{
					assigningOrder.get_Transaction().setModification(transaction);
				}
			}
		}

		dataTable = tables.get_Item("Order");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Order order = null;
				Guid id = (Guid)dataRow.get_Item("ID");
				if (this._orders.containsKey(id))
				{
					order = this.getOrder(id);
					order.replace(dataRow);
					//Refresh Order UI
					order.update();
				}
				else
				{
					Guid transactionId = (Guid)dataRow.get_Item("TransactionID");
					Transaction transaction = this.getTransaction(transactionId);
					order = new Order(this, this._settingsManager, dataRow, transaction, true);
					this._orders.put(id, order);
					transaction.get_Orders().put(id, order);

					this._settingsManager.verifyAccountCurrency(order);

					OperateWhichOrderUI operateWhichOrderUI = order.getOperateWhichOrderUI();
					if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
						|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
					{
						if (order.get_Transaction().get_Phase() == Phase.Executed)
						{
							//don't add to working list, but maybe should add to some other list in future
						}
						else
						{
							this._workingOrders.put(id, order);
						}
					}
					if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
						|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
					{
						this._openOrders.put(id, order);
					}
				}
			}
		}

		dataTable = tables.get_Item("OrderRelation");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				Guid openOrderId = (Guid)dataRow.get_Item("OpenOrderId");
				Guid closeOrderId = (Guid)dataRow.get_Item("CloseOrderId");
				String lot = dataRow.get_Item("ClosedLot").toString();
				String price = dataRow.get_Item("Price").toString();
				Order closeOrder = this.getOrder(closeOrderId);
				closeOrder.setPeerOrderIDs(openOrderId.toString() + TradingConsole.delimiterCol
										   + lot + TradingConsole.delimiterCol + price);
			}
		}

		dataTable = tables.get_Item("DayPLNotValued");
		if (dataTable != null)
		{
			DataRowCollection rows = dataTable.get_Rows();
			for (int index = 0; index < rows.get_Count(); index++)
			{
				DataRow dataRow2 = rows.get_Item(index);
				Guid orderID = (Guid)dataRow2.get_Item("OrderID");
				double dayInterestPLNotValued = ( (BigDecimal)dataRow2.get_Item("DayInterestPLNotValued")).doubleValue();
				double dayStoragePLNotValued = ( (BigDecimal)dataRow2.get_Item("DayStoragePLNotValued")).doubleValue();
				Order.DayPLNotValuedItem dayNotValuedItem = new Order.DayPLNotValuedItem(dayInterestPLNotValued, dayStoragePLNotValued);
				this._orders.get(orderID).get_PLNotValued().addDayPLNotValuedItem(dayNotValuedItem);
			}
		}
		for (Order order : this._orders.values())
		{
			order.calculatePLNotValued();
			order.add(false, true);
		}

		dataTable = tables.get_Item("OrderModification");
		if (dataTable != null)
		{
			DataRowCollection rows = dataTable.get_Rows();
			for (int index = 0; index < rows.get_Count(); index++)
			{
				DataRow dataRow2 = rows.get_Item(index);
				OrderModification orderModification = OrderModification.from(dataRow2);
				this._orders.get(orderModification.get_OrderId()).set_OrderModification(orderModification);
			}
		}

		dataTable = tables.get_Item("TradingTime");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize2 row count of TradingTime = " + dataRowCollection.get_Count());

			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				Guid instrumentId = (Guid)dataRow.get_Item("InstrumentID");
				Instrument instrument = this._settingsManager.getInstrument(instrumentId);
				if (instrument == null)
				{
					continue;
				}
				CompositeKey2<Guid, DateTime> compositeKey2 = new CompositeKey2<Guid, DateTime> (instrumentId, (DateTime)dataRow.get_Item("BeginTime"));
				TradingTime tradingTime = instrument.getTradingTime(compositeKey2);
				if (tradingTime != null)
				{
					tradingTime.removeScheduler();
					tradingTime.replace(dataRow);
					tradingTime.addScheduler();
				}
				else
				{
					tradingTime = new TradingTime(this._settingsManager, dataRow);
					instrument.setTradingTime(tradingTime);
					tradingTime.addScheduler();
				}
				TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize2 tradingTime = " + tradingTime);
			}
		}

		for (Iterator<Account> iterator = this._settingsManager.getAccounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.updateAccountStatus();
		}

		Instrument.removeAll(Instrument.tradingPanelKey);
		Instrument.removeAll(Instrument.summaryPanelKey);
		for (Iterator<Instrument> iterator = this._settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();

			instrument.setFirstOpenCloseTime();
			instrument.calculateSummary();
			instrument.add(Instrument.tradingPanelKey);
			instrument.addSummaryPanel(Instrument.summaryPanelKey);
			instrument.get_BestPendings().clear();
			instrument.get_TimeAndSales().clear();
		}
		Instrument.SubtotalInstrument.addSummaryPanel(Instrument.summaryPanelKey);
		Instrument.updateSubtotalSummary();
		this.sortForTradingPanel();
		this.sortForSummaryPanel();

		this.fillInstrumentSpanGrid();
		this._mainForm.get_QueryPanel().initailize();

		//Message
		dataTable = tables.get_Item("Message");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				if ( (Integer)dataRow.get_Item("MessageCount") > 0)
				{
					this._hasMessage = true;
				}
			}
		}
		dataTable = tables.get_Item("BestLimits");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);
				Guid instrumentId = (Guid)dataRow.get_Item("InstrumentId");
				Instrument instrument = this._settingsManager.getInstrument(instrumentId);
				BestLimit bestLimit = BestLimit.create(dataRow);
				instrument.setBestLimit(bestLimit);
			}
		}

		dataTable = tables.get_Item("BursaDefaultTimeTable");
		if (dataTable != null)
		{
			Session forenoonSession = new Session(dataTable.get_Rows().get_Item(0));
			Session afternoonSession = new Session(dataTable.get_Rows().get_Item(1));
			TimeTable defaultTimeTable = new TimeTable(forenoonSession, afternoonSession);
			this._instrumentStateManager = new InstrumentStateManager(this, this._settingsManager, defaultTimeTable, TradingConsoleServer.appTime);
			this._instrumentStateManager.start();
		}

		dataTable = tables.get_Item("BestPending");
		if (dataTable != null)
		{
			DataRowCollection rows = dataTable.get_Rows();
			TradingConsole.traceSource.trace(TraceType.Information, "Row count of BestPending = " + rows.get_Count());
			for(int index = 0; index < rows.get_Count(); index++)
			{
				dataRow = rows.get_Item(index);
				BestPending bestPending = BestPending.create(dataRow);

				Guid organizationId= (Guid)dataRow.get_Item("OrganizationId");
				Guid instrumentId= (Guid)dataRow.get_Item("InstrumentId");
				boolean isBuy= ((Boolean)dataRow.get_Item("IsBuy")).booleanValue();

				Guid[] accounts = this._settingsManager.get_AccountsByOrganization(organizationId);
				Instrument instrument = this._settingsManager.getInstrument(instrumentId);
				instrument.get_BestPendings().add(isBuy, accounts, bestPending);
			}
		}

		dataTable = tables.get_Item("TimeAndSale");
		if (dataTable != null)
		{
			DataRowCollection rows = dataTable.get_Rows();
			TradingConsole.traceSource.trace(TraceType.Information, "Row count of TimeAndSale = " + rows.get_Count());
			for(int index = 0; index < rows.get_Count(); index++)
			{
				dataRow = rows.get_Item(index);
				Guid organizationId= (Guid)dataRow.get_Item("OrganizationID");
				Guid instrumentId= (Guid)dataRow.get_Item("InstrumentID");

				Guid[] accounts = this._settingsManager.get_AccountsByOrganization(organizationId);
				double price= Double.parseDouble((String)dataRow.get_Item("Price"));
				double quantity= ((BigDecimal)dataRow.get_Item("Quantity")).doubleValue();
				DateTime timestamp= (DateTime)dataRow.get_Item("Timestamp");
				Instrument instrument = this._settingsManager.getInstrument(instrumentId);
				String price2 =  AppToolkit.format(price, instrument.get_Decimal());
				TimeAndSale timeAndSale = new TimeAndSale(instrumentId, accounts, timestamp, price2, quantity);
				instrument.get_TimeAndSales().add(timeAndSale);
			}
		}

		//verify structure.....
		//......
		if (!this._isInitialized)
		{
			this.ready();
			if (!firstShowInstrumentSelection && this._settingsManager.get_Accounts().size() <= 0)
			{
				this.messageNotify(Language.AccountIsNotTrading, false);
			}
		}
		TradingConsole.traceSource.trace(TraceType.Information, "TradingConsole.initialize2 end");
	}

	public boolean getShowChangePasswordMenuItem()
	{
		return this.get_SettingsManager().allowChangePasswordInTrader()
			|| this.needRecoverPasswordQuotationSetting()
			|| this.get_SettingsManager().get_SystemParameter().get_EnableModifyTelephoneIdentification()
			|| this.get_SettingsManager().get_SystemParameter().get_EnableMarginPin();
	}

	public void fillInstrumentSpanGrid()
	{
		Instrument[] sortedInstruments = this.getSortedInstruments();
		this._instrumentSpanBindingSource = new InstrumentSpanBindingSource(sortedInstruments, PublicParametersManager.gridsPerUnitRow);

		InstrumentSpanGrid instrumentSpanGrid = this._mainForm.get_InstrumentSpanGrid();
		instrumentSpanGrid.setModel(this._instrumentSpanBindingSource);
	}

	//...
	private Instrument[] getSortedInstruments()
	{
		tradingConsole.ui.grid.DataGrid instrumentTable = this.get_MainForm().get_InstrumentTable();
		int size = instrumentTable.getRowCount();
		Instrument[] sortedInstruments = new Instrument[size];
		for (int row = 0; row < size; row++)
		{
			Instrument instrument = (Instrument)instrumentTable.get_BindingSource().getObject(row);
			sortedInstruments[row] = instrument;
		}
		return sortedInstruments;
	}

	private void ready()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "ready()_Begin");

		this._isInitialized = true;

		this.resetSystemScheduleStart();
		this.verifyTransactionScheduleStart();
		this._settingsManager.getAccountsForCutSchedulerStart();
		if(this._settingsManager.get_SystemParameter().get_NeedSelectAccount())
		{
			this.showAccountSelection();
		}
		if (this._settingsManager.getInstruments().values().size() == 0)
		{
			firstShowInstrumentSelection = true;
			this.firstShowInstrumentSelection();
			return;
		}
		else
		{
			firstShowInstrumentSelection = false;
		}

		//Start GetCommands of the TradingConsoleServer
		this._commandsManager = new CommandsManager(this, this._settingsManager);
		this._tradingConsoleServer.schedulerStart();

		TradingConsole.traceSource.trace(TraceType.Warning, "ready()_End");
	}

	public void sortForTradingPanel()
	{

	}

	public void sortForSummaryPanel()
	{
	}

	private void sortForNewsPanel()
	{
		int column = this._bindingSourceForNewsPanel.getColumnByName(NewsColKey.PublishTimeString);
		this._mainForm.get_NewsTable().sort(column, false, false);
	}

	private void sortForMessagePanel()
	{
		int column = this._bindingSourceForMessagePanel.getColumnByName(MessageColKey.PublishTimeString);
		this._mainForm.get_MessageTable().sort(column, false, false);
	}

	//Remove
	public void removeTransaction(Transaction transaction)
	{
		Guid transactionId = transaction.get_Id();
		Guid instrumentId = transaction.get_Instrument().get_Id();
		Guid accountId = transaction.get_Account().get_Id();
		Collection orders = transaction.get_Orders().values();

		for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			this.removeOrder2(order);
		}

		transaction.get_Orders().clear();

		this._settingsManager.getInstrument(instrumentId).get_Transactions().remove(transactionId);
		this._settingsManager.getAccount(accountId).get_Transactions().remove(transactionId);
		this._transactions.remove(transactionId);
		InventoryManager.RemoveTransactionResult result = InventoryManager.instance.remove(transaction);
		if(result.hasInventoryRemoved)
		{
			this._mainForm.get_PhysicalInventoryTable().collapseAllRows();
		}
	}

	public void removeOrder(Order order)
	{
		Transaction transaction = this.getTransaction(order.get_Transaction().get_Id());
		if (transaction.get_Orders().size() == 1)
		{
			this.removeTransaction(transaction);
		}
		else
		{
			this.removeOrder2(order);
			transaction.get_Orders().remove(order.get_Id());
		}
	}

	public void removeWorkingOrder(Order order)
	{
		if (this.getWorkingOrder(order.get_Id()) != null)
		{
			this._workingOrders.remove(order.get_Id());
		}
	}

	public void removeOpenOrder(Order order)
	{
		if (this.getOpenOrder(order.get_Id()) != null)
		{
			this._openOrders.remove(order.get_Id());
		}
	}

	private void removeOrder2(Order order)
	{
		order.remove();

		order.calculateForDeleteOrder();

		Guid orderId = order.get_Id();
		this._orders.remove(orderId);

		if (this.getWorkingOrder(orderId) != null)
		{
			this._workingOrders.remove(orderId);
		}
		if (this.getOpenOrder(orderId) != null)
		{
			this._openOrders.remove(orderId);
		}

		InventoryManager.RemoveTransactionResult result = InventoryManager.instance.remove(order);
		if(result.hasInventoryRemoved)
		{
			this._mainForm.get_PhysicalInventoryTable().collapseAllRows();
		}
	}

	public void removeQuotation(Guid instrumentId)
	{
		if (this._quotations.containsKey(instrumentId))
		{
			this._quotations.remove(instrumentId);
		}
	}

	public void clearCancelledOrder()
	{
		Object[] orders = this._orders.values().toArray();
		for (int i = this._orders.values().size() - 1; i >= 0; i--)
		{
			Order order = (Order)orders[i];
			if (order.get_Phase() == Phase.Cancelled
				|| order.get_Phase() == Phase.Deleted)
			{
				this.removeOrder(order);
			}
		}
	}

	private void resetSystemScheduleStart()
	{
		try
		{
			DateTime endTime = this._settingsManager.get_TradeDay().get_EndTime().addSeconds(AppToolkit.getRandom());

			TradingConsole.traceSource.trace(TraceType.Information, "###### resttime = " + endTime.toString(DateTime.fullFormat));

			this._resetSystemScheduleEntry = TradingConsoleServer.scheduler.add(TradingConsole.class.getName(), this, TradingConsole._actionArgsResetSystemTime,
				endTime);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public synchronized void resetSystemSchedulerStop()
	{
		if (this._resetSystemScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._resetSystemScheduleEntry);
			this._resetSystemScheduleEntry = null;
		}
	}

	private void showAccountSelection()
	{
		AccountSelectionForm accountSelectionForm = new AccountSelectionForm(this._mainForm, this, true);
		accountSelectionForm.show();
		accountSelectionForm.toFront();
	}

	private boolean firstShowInstrumentSelection = false;
	private void firstShowInstrumentSelection()
	{
		firstShowInstrumentSelection = true;
		InstrumentSelectionForm instrumentSelectionUi = new InstrumentSelectionForm(this._mainForm, this, this._settingsManager);
		instrumentSelectionUi.exitOnConfirm();
		instrumentSelectionUi.show();
	}

	public void accountMarginNotify(String alertAccountCodes)
	{
		this.messageNotify(Language.AccountMarginNotify1 + "\n" + alertAccountCodes + "\n" + Language.AccountMarginNotify2, false);
	}

	public void accountWillExpireNotify(String willExpireAccountCodes)
	{
		this.messageNotify(Language.AccountExpire + "\n" + willExpireAccountCodes + "\n" + Language.AccountWillExpire, false);
	}

	public void accountExpiredNotify(String expiredAccountCodes)
	{
		this.messageNotify(Language.AccountExpire + "\n" + expiredAccountCodes + "\n" + Language.AccountExpired, false);
	}

	public void messageNotify(String message, boolean isClear)
	{
		this.logger.debug("------------------------------------------"+message+"------------------------------------------");
		message += "\n";
		NotifyForm notifyForm = new NotifyForm(this, this._settingsManager, Language.notify, 450, 200, true);
		notifyForm.setMessage(message, isClear);
		notifyForm.show();
		notifyForm.toFront();
	}

	//webservice
	public void processCommands(XmlNode commandNode)
	{
		try
		{
			this._commandsManager.processCommands(commandNode);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void processCommands(Element commandNode)
	{
		try
		{
			this._commandsManager.processCommands(commandNode);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}

	}


	public void setInterestRateOrderId(Transaction transaction)
	{
		for (Iterator<Order> iterator = transaction.get_Orders().values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			if (order.get_Phase() == Phase.Executed && order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0 && order.get_InterestRate() == null)
			{
				this.setInterestRateOrderId(order.get_Id());
			}
		}
	}

	private void setInterestRateOrderId(Guid orderId)
	{
		if (!this._interestRateOrderIds.containsKey(orderId))
		{
			this._interestRateOrderIds.put(orderId, orderId);
		}
	}

	public void getInterestRate()
	{
		if (this._interestRateOrderIds.size() > 0)
		{
			Guid[] orderIds = new Guid[this._interestRateOrderIds.size()];
			int i = 0;
			for (Iterator<Guid> iterator = this._interestRateOrderIds.values().iterator(); iterator.hasNext(); )
			{
				Guid orderId = iterator.next();
				orderIds[i] = orderId;
				i++;
			}
			DataSet dataSet = this._tradingConsoleServer.getInterestRate(orderIds);
			this.getInterestRateResult(dataSet);
		}
	}

	public void getInterestRate(Guid interestRateId)
	{
		DataSet dataSet = this._tradingConsoleServer.getInterestRate(interestRateId);
		this.getInterestRateResult(dataSet);
	}

	private void getInterestRateResult(DataSet dataSet)
	{
		if (dataSet == null)
		{
			return;
		}
		DataTableCollection tables = dataSet.get_Tables();
		if (tables.get_Count() <= 0)
		{
			return;
		}
		DataTable dataTable = tables.get_Item(0);
		if (dataTable != null)
		{
			DataRowCollection dataRowCollection = dataTable.get_Rows();
			for (int i = 0, count = dataRowCollection.get_Count(); i < count; i++)
			{
				DataRow dataRow = dataRowCollection.get_Item(i);
				Guid orderId = (Guid)dataRow.get_Item("Id");

				Guid interestRateId = (Guid)dataRow.get_Item("InterestRateID");
				BigDecimal interestRateBuy = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateBuy")) ? null :
											  (BigDecimal) (dataRow.get_Item("InterestRateBuy")));
				BigDecimal interestRateSell = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateSell")) ? null :
											   (BigDecimal) (dataRow.get_Item("InterestRateSell")));
				if (tables.get_Count() == 1)
				{
					this._settingsManager.setInterestRate(interestRateId, interestRateBuy, interestRateSell);
				}

				Order order = this.getOpenOrder(orderId);
				if (order == null)
				{
					continue;
				}
				BigDecimal interestRate = order.get_IsBuy() ? interestRateBuy : interestRateSell;
				order.set_InterestRate(interestRate);
				order.update(OpenOrderColKey.InterestRateString, order.get_InterestRateString());

				if (this._interestRateOrderIds.containsKey(orderId) && interestRate != null)
				{
					this._interestRateOrderIds.remove(orderId);
				}
			}

			//this._mainForm.get_OpenOrderTable().doLayout();
		}
		if (tables.get_Count() > 1)
		{
			dataTable = tables.get_Item(1);

			DataRowCollection dataRowCollection = dataTable.get_Rows();
			for (int i = 0, count = dataRowCollection.get_Count(); i < count; i++)
			{
				DataRow dataRow = dataRowCollection.get_Item(i);

				Guid interestRateId = (Guid)dataRow.get_Item("InterestRateID");
				BigDecimal interestRateBuy = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateBuy")) ? null :
											  (BigDecimal) (dataRow.get_Item("InterestRateBuy")));
				BigDecimal interestRateSell = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateSell")) ? null :
											   (BigDecimal) (dataRow.get_Item("InterestRateSell")));
				this._settingsManager.setInterestRate(interestRateId, interestRateBuy, interestRateSell);
			}
		}

		if (this._currentAccount != null)
		{
			this.refreshInterestRate(this._currentAccount);
		}
	}

	public void saveLog(LogCode logCode, String action, Guid transactionId)
	{
		this.saveLog(logCode, action, transactionId, null);
	}

	public void saveLog(LogCode logCode, String action, Guid transactionId, Guid accountId)
	{
		this.saveLog(logCode, action, null, transactionId, accountId);
	}

	public void saveLog(LogCode logCode, String action, String debugInfo, Guid transactionId, Guid accountId)
	{
		try
		{
			DateTime appTime = TradingConsoleServer.appTime();
			Log log = new Log(this, logCode, appTime, action, transactionId);
			this.setLog(log);
			log.addLogPanel(Log.logPanelKey);
			TradingConsole.bindingManager.update(Log.logPanelKey, log);

			boolean needCreateNewLog = false;
			if(accountId != null)
			{
				action = action + "[AccountId]=" + accountId.toString();
				needCreateNewLog = true;
			}

			if(!StringHelper.isNullOrEmpty(debugInfo))
			{
				action = action + "[DebugInfo]=" + debugInfo;
				needCreateNewLog = true;
			}

			if(needCreateNewLog)
			{
				log = new Log(this, logCode, appTime, action, transactionId);
			}
			Log.saveLog(log);
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, FrameworkException.getStackTrace(exception));
		}
	}

	public void calculatePLFloat()
	{
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			for (Iterator<AccountCurrency> iterator2 = account.get_AccountCurrencies().values().iterator(); iterator2.hasNext(); )
			{
				AccountCurrency accountCurrency = iterator2.next();
				accountCurrency.clearFloatTradingItem();
			}
			account.clearFloatTradingItem();
		}

		this.caculateTradingItem(true);
		//this.caculateTradingItem(false);
		this.caculateMargin();

		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			account.calculateEquity();
			account.updateNode();
		}
	}

	private void caculateMargin()
	{
		for (Account account : this._settingsManager.get_Accounts().values())
		{
			account.set_Necessary(0);
			account.set_NecessaryForPartialPaymentPhysicalOrder(0);
			for(AccountCurrency item : account.get_AccountCurrencies().values())
			{
				item.set_Necessary(0);
				item.set_NecessaryForPartialPaymentPhysicalOrder(0);
			}
		}

		for (Iterator<Instrument> iterator = this._settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();
			instrument.reCalculateTradePLFloat();
			HashMap<Guid, Double> margins = instrument.get_Margins();
			for (Iterator<Guid> iterator2 = margins.keySet().iterator(); iterator2.hasNext(); )
			{
				Guid accountId = iterator2.next();
				Account account = this._settingsManager.getAccount(accountId);
				double margin = margins.get(accountId);
				if(account.get_IsMultiCurrency())
				{
					Guid currencyId = instrument.get_Currency().get_Id();
					CompositeKey2<Guid, Guid> key = new CompositeKey2<Guid,Guid>(accountId, currencyId);
					if(account.get_AccountCurrencies().containsKey(key))
					{
						AccountCurrency accountCurrency = account.get_AccountCurrencies().get(key);
						margin += accountCurrency.get_Necessary();
						accountCurrency.set_Necessary(margin);
					}
				}
				else
				{
					margin += account.get_Necessary();
					account.set_Necessary(margin);
				}
			}
			margins = instrument.get_PartialPaymentPhysicalMargins();
			for (Iterator<Guid> iterator2 = margins.keySet().iterator(); iterator2.hasNext(); )
			{
				Guid accountId = iterator2.next();
				Account account = this._settingsManager.getAccount(accountId);
				double margin = margins.get(accountId);
				if(account.get_IsMultiCurrency())
				{
					Guid currencyId = instrument.get_Currency().get_Id();
					CompositeKey2<Guid, Guid> key = new CompositeKey2<Guid,Guid>(accountId, currencyId);
					if(account.get_AccountCurrencies().containsKey(key))
					{
						AccountCurrency accountCurrency = account.get_AccountCurrencies().get(key);
						margin += accountCurrency.get_NecessaryForPartialPaymentPhysicalOrder();
						accountCurrency.set_NecessaryForPartialPaymentPhysicalOrder(margin);
					}
				}
				else
				{
					margin += account.get_NecessaryForPartialPaymentPhysicalOrder();
					account.set_NecessaryForPartialPaymentPhysicalOrder(margin);
				}
			}
		}

		for (Account account : this._settingsManager.get_Accounts().values())
		{
			for(AccountCurrency accountCurrency : account.get_AccountCurrencies().values())
			{
				if(account.get_IsMultiCurrency())
				{
					Guid sourceCurrencyId = accountCurrency.get_Currency().get_Id();
					Guid targetCurrencyId = account.get_Currency().get_Id();
					CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(sourceCurrencyId, targetCurrencyId);
					double margin = AppToolkit.round(accountCurrency.get_Necessary(), accountCurrency.get_Currency().get_Decimals());
					margin = currencyRate.exchange(margin);
					margin = AppToolkit.round(margin, account.get_Currency().get_Decimals());
					margin += account.get_Necessary();
					account.set_Necessary(margin);

					margin = AppToolkit.round(accountCurrency.get_NecessaryForPartialPaymentPhysicalOrder(), accountCurrency.get_Currency().get_Decimals());
					margin = currencyRate.exchange(margin);
					margin = AppToolkit.round(margin, account.get_Currency().get_Decimals());
					margin += account.get_NecessaryForPartialPaymentPhysicalOrder();
					account.set_NecessaryForPartialPaymentPhysicalOrder(margin);
				}
				accountCurrency.updateNode();
			}
		}
	}

	private void caculateTradingItem(boolean isFloating)
	{
		HashMap<CompositeKey2<Guid, Guid>, TradingItem> accountCurrencyTradingItems = new HashMap<CompositeKey2<Guid, Guid>, TradingItem> ();
		for (Iterator<Instrument> iterator = this._settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();
			instrument.calculateTradePLFloat();
			HashMap<Guid, TradingItem> tradingItems = isFloating ? instrument.get_FloatingTradingItems() : instrument.get_NotValuedTradingItems();

			for (Iterator<Guid> iterator2 = tradingItems.keySet().iterator(); iterator2.hasNext(); )
			{
				Guid accountId = iterator2.next();
				Account account = this._settingsManager.getAccount(accountId);
				Currency currency = (account.get_IsMultiCurrency()) ? instrument.get_Currency() : account.get_Currency();

				CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (accountId, currency.get_Id());
				if (!accountCurrencyTradingItems.containsKey(compositeKey))
				{
					accountCurrencyTradingItems.put(compositeKey, tradingItems.get(accountId).clone());
				}
				else
				{
					TradingItem tradingItem = TradingItem.add(accountCurrencyTradingItems.get(compositeKey), tradingItems.get(accountId));
					accountCurrencyTradingItems.put(compositeKey, tradingItem);
				}
			}
		}

		for (Account account : this._settingsManager.get_Accounts().values())
		{
			if (isFloating)
			{
				account.get_FloatTradingItem().clear();
			}
			else
			{
				account.get_NotValuedTradingItem().clear();
			}
		}

		for (Iterator<CompositeKey2<Guid, Guid>> iterator = accountCurrencyTradingItems.keySet().iterator(); iterator.hasNext(); )
		{
			CompositeKey2<Guid, Guid> compositeKey = iterator.next();
			AccountCurrency accountCurrency = this._settingsManager.getAccountCurrency(compositeKey.get_Member1(), compositeKey.get_Member2());
			if (isFloating)
			{
				accountCurrency.set_FloatTradingItem(accountCurrencyTradingItems.get(compositeKey).clone());
			}
			else
			{
				accountCurrency.set_NotValuedTradingItem(accountCurrencyTradingItems.get(compositeKey).clone());
			}
			//Update Account Currency
			accountCurrency.updateNode();

			TradingItem tradingItem = null;
			Account account = accountCurrency.get_Account();
			if (account.get_IsMultiCurrency())
			{
				CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(accountCurrency.get_Currency().get_Id(), account.get_Currency().get_Id());
				TradingItem rawTradingItem = isFloating ? accountCurrency.get_FloatTradingItem() : accountCurrency.get_NotValuedTradingItem();
				tradingItem = TradingItem.exchange(rawTradingItem, currencyRate);

				short decimals = account.get_Currency().get_Decimals();
				tradingItem.set_Interest(AppToolkit.round(tradingItem.get_Interest(), decimals));
				tradingItem.set_Storage(AppToolkit.round(tradingItem.get_Storage(), decimals));
				tradingItem.set_Trade(AppToolkit.round(tradingItem.get_Trade(), decimals));
				tradingItem.set_ValueAsMargin(AppToolkit.round(tradingItem.get_ValueAsMargin(), decimals));
			}
			else
			{
				tradingItem = isFloating ? accountCurrency.get_FloatTradingItem().clone() : accountCurrency.get_NotValuedTradingItem().clone();
			}

			if (isFloating)
			{
				account.set_FloatTradingItem(TradingItem.add(account.get_FloatTradingItem(), tradingItem));
			}
			else
			{
				account.set_NotValuedTradingItem(TradingItem.add(account.get_NotValuedTradingItem(), tradingItem));
			}

			//Remarked by Michael on 2008-04-09
			//account.calculatePLFloat();
		}
	}

	private Account _currentAccount;
	public Account getCurrentAccount()
	{
		return this._currentAccount;
	}

	public void setCurrentAccount(Account account)
	{
		this._currentAccount = account;
	}

	public void refreshInterestRate(Account account)
	{
		this._currentAccount = account;
		tradingConsole.ui.grid.DockableTable instrumentTable = this.get_MainForm().get_InstrumentTable();
		for (Iterator<Instrument> iterator = this._settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();
			instrument.update(Instrument.tradingPanelKey);
		}
	}

	private ArrayList<IAsyncCommandListener> _asyncCommandListeners = new ArrayList<IAsyncCommandListener> ();

	public void asyncCommandCompleted(Guid asyncResultId, String methodName, boolean failed, String errorMessage)
	{
		if (failed)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "Call " + methodName + " failed, error message is: " + errorMessage);
		}

		for (IAsyncCommandListener asyncCommandListener : this._asyncCommandListeners)
		{
			if(asyncCommandListener.getAsyncResultId()==null){
				continue;
			}
			if (!asyncCommandListener.getAsyncResultId().equals(Guid.empty)
				&& asyncResultId.equals(asyncCommandListener.getAsyncResultId()))
			{
				asyncCommandListener.asyncCommandCompleted(asyncResultId, methodName, failed, errorMessage);
			}
		}
	}

	public void addAsyncCommandListener(IAsyncCommandListener asyncCommandListener)
	{
		this._asyncCommandListeners.add(asyncCommandListener);
	}

	public void removeAsyncCommandListener(IAsyncCommandListener asyncCommandListener)
	{
		this._asyncCommandListeners.remove(asyncCommandListener);
	}

	private boolean getAccountsResultProcess2(XmlNode accountXmlNodes)
	{
		boolean isExistsTransactions = false;

		if (accountXmlNodes == null)
		{
			return isExistsTransactions;
		}

		XmlNodeList accountXmlNodeList = accountXmlNodes.get_ChildNodes();
		for (int i = 0; i < accountXmlNodeList.get_Count(); i++)
		{
			XmlElement accountXmlElement = (XmlElement)accountXmlNodeList.item(i);
			Account.assign(this, this._settingsManager, accountXmlElement);
			XmlNodeList xmlNodeList = accountXmlElement.get_ChildNodes();
			for (int j = 0; j < xmlNodeList.get_Count(); j++)
			{
				XmlNode xmlNode = xmlNodeList.item(j);
				if (xmlNode.get_LocalName().equals("Transactions"))
				{
					XmlNodeList xmlNodeList2 = xmlNode.get_ChildNodes();
					for (int k = 0; k < xmlNodeList2.get_Count(); k++)
					{
						isExistsTransactions = true;

						XmlElement transactionXmlElement = (XmlElement)xmlNodeList2.item(k);
						Transaction.assign2(this, this._settingsManager, transactionXmlElement);
					}
				}
			}
		}
		return isExistsTransactions;
	}

	//region-----------------------------------------------------------------------------------------
	//VerifyTransaction
	private void verifyTransactionScheduleStart()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Start verify transaction scheduler");
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			TimeSpan timeSpan = TimeSpan.fromSeconds(20);
			this._verifyTransactionScheduleEntry = TradingConsoleServer.scheduler.add(this, TradingConsole._actionArgsVerifyTransactionTime, beginTime,
				DateTime.maxValue, timeSpan, true);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public synchronized void verifyTransactionSchedulerStop()
	{
		if (this._verifyTransactionScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._verifyTransactionScheduleEntry);
			this._verifyTransactionScheduleEntry = null;
		}
	}

	private void verifyTransaction()
	{
		HashMap<Guid, Transaction> transactions = new HashMap<Guid, Transaction> ();
		DateTime appTime = TradingConsoleServer.appTime();
		for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.isSuspicious(appTime))
			{
				transactions.put(transaction.get_Id(), transaction);
			}
		}
		int size = transactions.size();
		if (size > 0)
		{
			int i = 0;
			Guid[] transactionIds = new Guid[size];
			for (Iterator<Guid> iterator = transactions.keySet().iterator(); iterator.hasNext(); )
			{
				Guid transactionId = iterator.next();
				transactionIds[i] = transactionId;
				i++;
			}

			Guid[] cancelledTransactionIds = this.get_TradingConsoleServer().verifyTransaction(transactionIds);
			this.verifyTransactionResult(cancelledTransactionIds);
		}
	}

	private void verifyTransactionResult(Guid[] cancelledTransactionIds)
	{
		if (cancelledTransactionIds == null)
		{
			return;
		}
		for (int i = 0, length = cancelledTransactionIds.length; i < length; i++)
		{
			Guid transactionId = cancelledTransactionIds[i];
			Transaction transaction = this.getTransaction(transactionId);
			if (transaction != null)
			{
				if (transaction.get_Phase() == Phase.Executed || transaction.get_Phase() == Phase.Completed)
				{
					continue;
				}

				transaction.cancel(Language.TradeConsoleIsValidOperateOrderTimePrompt1, false);
				transaction.saveLog(LogCode.Cancelled);
			}
		}
	}

	//endregion-----------------------------------------------------------------------------------------

	//region-----------------------------------------------------------------------------------------
	//1. fixDataForAllowFreeAgent
	//2. Get Cut Orders when Necessary * TradePolicy.AlertLevel3 >= Equity & All Instruments's Executed Order: SumLotBalanceForBuy != SumLotBalanceForSell
	public void fixData(XmlNode accountXmlNodes)
	{
		CommandsManager.fixData(this, this._settingsManager, accountXmlNodes);
	}

	private boolean _suspendRefreshSummary = false;
	public void suspendRefreshSummary()
	{
		this._suspendRefreshSummary = true;
	}

	public void resumeRefreshSummary()
	{
		this._suspendRefreshSummary = false;
	}

	public void refreshSummary()
	{
		if (this._suspendRefreshSummary || this._settingsManager.get_Customer().get_IsNoShowAccountStatus())
		{
			return;
		}

		if (this._mainForm != null
			&& this._mainForm.get_InstrumentTable() != null
			&& this._mainForm.get_OpenOrderTable() != null)
		{
			for (int row = 0; row < this._mainForm.get_InstrumentTable().getRowCount(); row++)
			{
				Instrument instrument = (Instrument)this._mainForm.get_InstrumentTable().getObject(row);
				instrument.clearBuySellLots();
			}
			Instrument.SubtotalInstrument.clearBuySellLots();

			BindingSource orders = this._mainForm.get_OpenOrderTable().get_BindingSource();
			for (int row = 0; row < orders.getRowCount(); row++)
			{
				Order order = (Order)orders.getObject(row);
				if (order.get_Transaction().get_Instrument().get_Select() &&
					order.get_Account().get_CanDisplay())
				{
					if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
					{
						order.get_Transaction().get_Instrument().addToSummary(order);
					}
				}
			}
			Instrument.SubtotalInstrument.updateSubtotalSummary();
			this.rebindSummary();
		}
	}

	public void applyOrderModification(Guid orderModificationId, OrderModifyResult result)
	{
		for (Order order : this._orders.values())
		{
			if (order.get_OrderModification() != null && order.get_OrderModification().get_Id().equals(orderModificationId))
			{
				order.applyModification(result);
				return;
			}
		}
	}

	public void instrumentTimeStateChanged(Instrument instrument, InstrumentTimeState newState)
	{
		instrument.setValidateColor();
	}

	public void connectionBroken(){
		this.logger.info("connection broken");
		this.reCover();
	}

	private void reCover(){
		boolean isRecoved = false;
		try{
			this.connectionManager.closeTcpConnect();
			this.logger.info("closed connection");
			this.connectHelper();
			this.logger.info("connected");
			if(this.recoverService.recover()){
				this.logger.info("recoved");
				isRecoved = true;
				this.connectionManager.getAsyncManager().setSlidingWindow(this.get_TradingConsoleServer().get_SlidingWindow(),this);
				this._loginInformation.set_LoginStatus(LoginStatus.Ready);
				this.setConnectStatus();
			}
			else{
				this.logger.info("not recoved");
			}
		}
		catch(TcpInializeTimeoutException e){
			this.messageNotify(ReformMessage.Disconnect, true);
			this.disconnect(true);
			return;
		}
		catch(Exception ex){
			this.logger.error(ex.getStackTrace());
		}
		if(!isRecoved){
			this.messageNotify(ReformMessage.Relogin, true);
			this.disconnect(true);
			this.reConnect(true);
		}

	}


	private String[] getUpdateUrlString()
	{
		XmlNode xmlNode = AppToolkit.getResourceXml("Configuration", "AutoUpdateUrls.xml");
		if(xmlNode != null)
		{
			XmlNodeList children = xmlNode.get_ChildNodes();

			String[] updateUrlStrings = new String[children.get_Count()];
			for (int index = 0; index < children.get_Count(); index++)
			{
				XmlNode child = children.itemOf(index);
				updateUrlStrings[index] = child.get_InnerText();
			}
			return updateUrlStrings;
		}
		else
		{
			return null;
		}
	}
}
