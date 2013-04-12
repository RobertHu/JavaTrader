package tradingConsole;

import tradingConsole.ui.grid.BindingSource;
import tradingConsole.ui.GridNames;
import framework.lang.Enum;
import tradingConsole.ui.language.Language;
import java.awt.Font;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import java.awt.Color;
import tradingConsole.settings.UISetting2;
import tradingConsole.enumDefine.TradeOption;
import javax.swing.SwingConstants;
import framework.data.DataRow;
import tradingConsole.ui.columnKey.OrderColKey;
import tradingConsole.ui.ColorSettings;
import tradingConsole.settings.UISetting;
import java.math.BigDecimal;
import java.util.HashMap;
import tradingConsole.enumDefine.OrderType;
import tradingConsole.ui.language.OrderLanguage;
import tradingConsole.settings.SettingsManager;
import framework.DBNull;
import tradingConsole.settings.Parameter;
import tradingConsole.framework.PropertyDescriptor;
import java.util.Collection;
import java.util.ArrayList;
import tradingConsole.enumDefine.Phase;
import framework.DateTime;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.grid.ColumnUIInfoManager;
import tradingConsole.ui.colorHelper.NumericColor;
import tradingConsole.ui.colorHelper.OpenCloseColor;
import tradingConsole.ui.colorHelper.BuySellColor;
import tradingConsole.enumDefine.TransactionType;

public class HistoryOrder
{
	private String _code;
	private String _account;
	private String _instrument;
	private String _beginTime;
	private String _endTime;
	private String _executeTime;
	private String _orderType;
	private String _lot;
	private String _price;
	private String _buySell;
	private String _openClose;
	private String _phase;
	private Phase _phaseValue;
	private String _remark;

	private boolean isOpen = false;
	private boolean isBuy = false;

	public String get_Code()
	{
		return this._code;
	}

	public String get_AccountCode()
	{
		return this._account;
	}

	public String get_InstrumentCode()
	{
		return this._instrument;
	}

	public String get_SubmitTime()
	{
		return this._beginTime;
	}

	public String get_EndTime()
	{
		return this._endTime;
	}

	public String get_ExecuteTime()
	{
		return this._executeTime;
	}

	public String get_OrderTypeString()
	{
		return this._orderType;
	}

	public String get_LotString()
	{
		return this._lot;
	}

	public String get_Price()
	{
		return this._price;
	}

	public String get_IsBuyString()
	{
		return this._buySell;
	}

	public String get_IsOpenString()
	{
		return this._openClose;
	}

	public String get_PhaseString()
	{
		return this._phase;
	}

	public String get_Remark()
	{
		return this._remark;
	}

	private static BindingSource bindingSource = new BindingSource();

	public static HistoryOrder from(DataRow row, SettingsManager manager)
	{
		HistoryOrder historyOrder = new HistoryOrder();
		historyOrder._code = (String)row.get_Item("Code");
		historyOrder._account = (String)row.get_Item("AccountCode");
		historyOrder._beginTime = ( (DateTime)row.get_Item("BeginTime")).toString(DateTime.defaultFormat);
		historyOrder._endTime = ( (DateTime)row.get_Item("EndTime")).toString(DateTime.defaultFormat);
		if (row.get_Item("ExecuteTime") != DBNull.value)
		{
			historyOrder._executeTime = ( (DateTime)row.get_Item("ExecuteTime")).toString(DateTime.defaultFormat);
		}
		historyOrder._instrument = (String)row.get_Item("InstrumentCode");
		TradeOption tradeOption = Enum.valueOf(TradeOption.class, ( (Short)row.get_Item("TradeOption")).intValue());
		OrderType orderType = Enum.valueOf(OrderType.class, ( (Integer)row.get_Item("OrderTypeID")).intValue());
		TransactionType transactionType = Enum.valueOf(TransactionType.class, ( (Short)row.get_Item("TransactionType")).intValue());
		if(transactionType.equals(TransactionType.OneCancelOther))
		{
			historyOrder._orderType = orderType.equals(OrderType.Limit) ?
				Language.OCOPrompt+ "( " + Language.LMTPrompt + ")" : Language.OCOPrompt+ "( " + Language.STPPrompt + ")";
		}
		else if (orderType.equals(OrderType.Limit) && tradeOption.equals(TradeOption.Stop))
		{
			historyOrder._orderType = Language.STPPrompt;
		}
		else
		{
			historyOrder._orderType = OrderType.getCaption(orderType);
		}

		BigDecimal lot = (BigDecimal)row.get_Item("Lot");
		if(new BigDecimal(lot.intValue()).compareTo(lot) == 0)
		{
			historyOrder._lot = Integer.toString(lot.intValue());
		}
		else
		{
			historyOrder._lot = AppToolkit.format(lot.doubleValue(), 2);
		}

		if (row.get_Item("Price") != DBNull.value)
		{
			historyOrder._price = (String)row.get_Item("Price");
		}
		historyOrder.isBuy = (Boolean)row.get_Item("IsBuy");
		historyOrder._buySell = historyOrder.isBuy ? Language.Buy : Language.Sell;
		historyOrder.isOpen = (Boolean)row.get_Item("IsOpen");
		historyOrder._openClose = historyOrder.isOpen ? Language.Open : Language.Close;
		Phase phase = Enum.valueOf(Phase.class, ( (Short)row.get_Item("Phase")).intValue());
		historyOrder._phaseValue = phase;
		historyOrder._phase = Phase.getCaption(phase);
		historyOrder._remark = (String)row.get_Item("Remarks");

		return historyOrder;
	}

	public void setForeground()
	{
		String dataSourceKey = "HistoryOrderList";
		if(ColorSettings.useBlackAsBackground) TradingConsole.bindingManager.setForeground(dataSourceKey, this, Color.WHITE);
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, OrderColKey.IsOpenString, OpenCloseColor.getColor(this.isOpen));
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, OrderColKey.IsBuyString, BuySellColor.getColor(this.isBuy, true));
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, OrderColKey.PhaseString, Phase.getColor(this._phaseValue));
	}

	private static PropertyDescriptor[] getPropertyDescriptors(SettingsManager settingsManager)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[12];
		int index = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.PhaseString, true, null, OrderLanguage.PhaseString,
			30, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.SubmitTime, true, null, OrderLanguage.SubmitTime,
			75, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.EndTime, true, null, OrderLanguage.EndTime,
			75,	SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.ExecuteTime, true, null, Language.OpenContractlblExecutedTimeA,
			75,	SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.AccountCode, true, null, OrderLanguage.AccountCode,
			30, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.InstrumentCode, true, null, OrderLanguage.InstrumentCode,
			30, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.LotString, true, null, OrderLanguage.LotString,
			20, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.IsOpenString, true, null, OrderLanguage.IsOpenString,
			20, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.IsBuyString, true, null, OrderLanguage.IsBuyString,
			20, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, "Price", true, null, Language.UnconfirmedInstructionlblExecutePriceA,
			30, SwingConstants.RIGHT, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, OrderColKey.OrderTypeString, true, null, OrderLanguage.OrderTypeString,
			40, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(HistoryOrder.class, "Remark", true, null, Language.UnconfirmedInstructionlblMessageA,
			80, SwingConstants.CENTER, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initializeList(SettingsManager settingsManager, tradingConsole.ui.grid.DataGrid grid, Collection dataSource)
	{
		String dataSourceKey = "HistoryOrderList";

		grid.setShowVerticalLines(false);
		grid.setShowHorizontalLines(false);
		grid.setShowGrid(false);
		grid.setBackground(GridFixedBackColor.openOrderList);
		if(ColorSettings.useBlackAsBackground)
		{
			grid.setForeground(Color.WHITE);
		}
		else
		{
			grid.setForeground(GridBackColor.openOrderList);
		}
		UISetting uiSetting = settingsManager.getUISetting(UISetting.workingOrderListUiSetting);
		PropertyDescriptor[] propertyDescriptors = HistoryOrder.getPropertyDescriptors(settingsManager);
		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, propertyDescriptors);
		grid.setModel(bindingSource, 0);

		TradingConsole.bindingManager.setGrid(dataSourceKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.white,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.workingOrderList, Color.white,
												HeaderFont.workingOrderList);
		grid.setRowHeight(25);
	}
}
