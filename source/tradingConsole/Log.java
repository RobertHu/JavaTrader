package tradingConsole;

import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.docking.*;
import framework.*;
import framework.DateTime;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.borderStyleHelper.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnFixedHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.language.*;
import framework.threading.WaitHandle;
import tradingConsole.ui.ColorSettings;
import javax.swing.event.TableModelEvent;

public class Log
{
	public static final String logPanelKey = "LogPanelKey";
	public static ArrayList<Log> _cache = new ArrayList<Log>();
	public static Thread _logUploader = Log.createUploader();
	public static WaitHandle _logCachedNotifer = new WaitHandle();

	private TradingConsole _tradingConsole;

	private Guid _id;
	private LogCode _logCode;
	private DateTime _timestamp;
	private String _action;
	private Guid _transactionId;

	static
	{
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader", "TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_LogCodeName()
	{
		return LogCode.getCaption(this._logCode);
	}

	public String get_Action()
	{
		return this._action;
	}

	public String get_TimestampString()
	{
		if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_TimeOptionInTraderLogAndConfirmWindow() == 0)
		{
			return framework.Convert.toString(this._timestamp, "HH:mm:ss");
		}
		else
		{
			return framework.Convert.toString(this._timestamp, "HH:mm");
		}
	}

	public DateTime get_Timestamp()
	{
		return this._timestamp;
	}

	public Log(TradingConsole tradingConsole, LogCode logCode, DateTime timestamp, String action, Guid transactionId)
	{
		this._tradingConsole = tradingConsole;

		this._id = Guid.newGuid();
		this._logCode = logCode;
		this._timestamp = timestamp;
		this._action = action;
		this._transactionId = transactionId;
	}

	private static class LogUploader implements Runnable
	{
		public void run()
		{
			while(true)
			{
				Log._logCachedNotifer.waitOne();

				Log log = Log.fetchLogForUploader();
				while(log != null)
				{
					log._tradingConsole.get_TradingConsoleServer().saveLog(log.get_LogCodeName(), log._timestamp, log._action, log._transactionId);
					log = Log.fetchLogForUploader();
				}
			}
		}
	}

	public static synchronized void saveLog(Log log)
	{
		Log._cache.add(log);
		Log._logCachedNotifer.pulse();
	}

	private static synchronized Log fetchLogForUploader()
	{
		if(Log._cache.size() > 0)
		{
			Log log = Log._cache.get(0);
			Log._cache.remove(0);
			return log;
		}
		else
		{
			return null;
		}
	}

	public static void updateProperties(String dataSourceKey, DockableFrame dockableFrame)
	{
		TradingConsole.bindingManager.updateProperties(dataSourceKey, Log.getPropertyDescriptors(dockableFrame));
	}

	public static PropertyDescriptor[] getPropertyDescriptors(DockableFrame dockableFrame)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int i = -1;

		int width = (dockableFrame == null) ? 820 : dockableFrame.getWidth() - 2;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.TimestampString, true, null, LogLanguage.TimestampString,
			UISettingsManager.getWidth(width, 0.12, 100), SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		//int actionColumnWidth = tableWidth - 60;
		//actionColumnWidth -= 60;//Fix width
		//actionColumnWidth = (actionColumnWidth > 0) ? actionColumnWidth : 760;
		propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.Action, true, null, LogLanguage.Action,
			UISettingsManager.getWidth(width, 0.88, 720), SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;

		/*PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[5];
		int i = -1;

		int width = (dockableFrame == null) ? 820 : dockableFrame.getWidth() - 25 - 45;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.Id, true, null, LogLanguage.Id,
			0, -1, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.LogCodeName, true, null, LogLanguage.LogCodeName,
			0, -1, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.Timestamp, true, null, LogLanguage.Timestamp,
			0, PVColumn.COLUMN_CENTER, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.TimestampString, true, null, LogLanguage.TimestampString,
			UISettingsManager.getWidth(width, 0.12, 100), PVColumn.COLUMN_CENTER, null);
		propertyDescriptors[++i] = propertyDescriptor;

		//int actionColumnWidth = tableWidth - 60;
		//actionColumnWidth -= 60;//Fix width
		//actionColumnWidth = (actionColumnWidth > 0) ? actionColumnWidth : 760;
		propertyDescriptor = PropertyDescriptor.create(Log.class, LogColKey.Action, true, null, LogLanguage.Action,
			UISettingsManager.getWidth(width, 0.88, 720), PVColumn.COLUMN_LEFT, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;*/
	}

	public static void initializeLogPanel(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource,
										  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		grid.reset();
		grid.setVertGridLines(false);
		grid.setHorzGridLines(false);
		grid.setBackColor(GridFixedBackColor.logPanel);
		grid.setTableColor(GridBackColor.logPanel);
		grid.setBorderStyle(BorderStyle.logPanel);
		grid.setRowLabelWidth(RowLabelWidth.logPanel);
		//grid.setSelectionBackground(SelectionBackground.logPanel);
		grid.setCurrentCellColor(CurrentCellColor.logPanel);
		grid.setCurrentCellBorder(CurrentCellBorder.logPanel);
		grid.setRowLabelWidth(35);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, Log.getPropertyDescriptors(dockableFrame));
		grid.setDataModel(bindingSource);

		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.logPanel, Color.white, HeaderFont.logPanel);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true, null, false, true, true);
		int timestampColumn = bindingSource.getColumnByName(LogColKey.TimestampString);
		grid.sortColumn(timestampColumn, true, false);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public static void rebindLogPanel(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource,
									  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		bindingSource.fireTableChanged(new TableModelEvent(bindingSource));
		/*Log.unbind(dataSourceKey, bindingSource);
		Log.initializeLogPanel(dockableFrame, grid, dataSourceKey, dataSource, bindingSource);
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Log log = (Log)bindingSource.getObject(i);
			log.changeLogPanelStyle(dataSourceKey);
		}*/
	}

	private void changeLogPanelStyle(String dataSourceKey)
	{
		Color color = LogCode.getColor(this._logCode);
		this.setForeground(dataSourceKey, LogColKey.TimestampString, color);
		this.setForeground(dataSourceKey, LogColKey.Action, color);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	public void addLogPanel(String dataSourceKey)
	{
		TradingConsole.bindingManager.insert(dataSourceKey, 0, this);
		this.changeLogPanelStyle(dataSourceKey);
	}

	public void updateLogPanel(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.changeLogPanelStyle(dataSourceKey);
	}

	public void remove(String dataSourceKey)
	{
		TradingConsole.bindingManager.remove(dataSourceKey, this);
	}

	public static void removeAll(String dataSourceKey)
	{
		TradingConsole.bindingManager.removeAll(dataSourceKey);
	}

	private static Thread createUploader()
	{
		 Thread uploader = new Thread(new LogUploader());
		 uploader.setDaemon(true);
		 uploader.setPriority(Thread.MIN_PRIORITY);
		 uploader.start();
		 return uploader;
	}
}
