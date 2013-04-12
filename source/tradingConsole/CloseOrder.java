package tradingConsole;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.ArrayList;
import tradingConsole.settings.UISetting;
import tradingConsole.settings.UISetting2;
import tradingConsole.framework.PropertyDescriptor;
import javax.swing.SwingConstants;
import tradingConsole.ui.columnKey.OrderColKey;
import tradingConsole.ui.language.OrderLanguage;
import tradingConsole.settings.SettingsManager;
import framework.DateTime;
import tradingConsole.ui.language.Language;

public class CloseOrder
{
	private Order _order;
	private BigDecimal _closeLot;

	private CloseOrder(Order order, BigDecimal closeLot)
	{
		this._order = order;
		this._closeLot = closeLot;
	}

	public static PropertyDescriptor[] getPropertyDescriptorsForWorkingOrderList(SettingsManager settingsManager)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[11];

		UISetting uiSetting = settingsManager.getUISetting(UISetting.workingOrderListUiSetting);
		HashMap<String, UISetting2> uiSetting2s = uiSetting.get_UiSetting2s();
		UISetting2 uiSetting2;

		uiSetting2 = uiSetting2s.get(OrderColKey.PhaseString);
		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.PhaseString, true, null, OrderLanguage.PhaseString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[0] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.SubmitTime);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.SubmitTime, true, null, OrderLanguage.SubmitTime, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[1] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.EndTime);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.EndTime, true, null, OrderLanguage.EndTime, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[2] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.AccountCode);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.AccountCode, true, null, OrderLanguage.AccountCode, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[3] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.InstrumentCode);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.InstrumentCode, true, null, OrderLanguage.InstrumentCode,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[4] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.LotString);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, "CloseLotString", true, null, Language.CloseLot, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[5] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.LotString, true, null, OrderLanguage.LotString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[6] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.IsOpenString);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.IsOpenString, true, null, OrderLanguage.IsOpenString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[7] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.IsBuyString);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.IsBuyString, true, null, OrderLanguage.IsBuyString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[8] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.SetPriceString);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.SetPriceString, true, null, OrderLanguage.SetPriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[9] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.OrderTypeString);
		propertyDescriptor = PropertyDescriptor.create(CloseOrder.class, OrderColKey.OrderTypeString, true, null, OrderLanguage.OrderTypeString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[10] = propertyDescriptor;

		return propertyDescriptors;
	}

	public Order get_Order()
	{
		return this._order;
	}

	public DateTime get_EndTime()
	{
		return this._order.get_EndTime();
	}

	public String get_PhaseString()
	{
		return this._order.get_PhaseString();
	}

	public String get_SubmitTime()
	{
		return this._order.get_SubmitTime();
	}

	public String get_AccountCode()
	{
		return this._order.get_AccountCode();
	}

	public String get_InstrumentCode()
	{
		return this._order.get_InstrumentCode();
	}

	public String get_OrderTypeString()
	{
		return this._order.get_OrderTypeString();
	}

	public String get_LotString()
	{
		return this._order.get_LotString();
	}

	public String get_CloseLotString()
	{
		return AppToolkit.getFormatLot(this._closeLot, this._order.get_Account(), this._order.get_Instrument());
	}

	public String get_IsOpenString()
	{
		return this._order.get_IsOpenString();
	}

	public String get_IsBuyString()
	{
		return this._order.get_IsBuyString();
	}

	public String get_SetPriceString()
	{
		return this._order.get_SetPriceString();
	}

	public static class CloseOrderHelper
	{
		private static HashMap<Order, ArrayList<CloseOrder>> _orderCloseOrderMapping = new HashMap<Order, ArrayList<CloseOrder>>();

		public static CloseOrder createCloseOrder(Order order, BigDecimal closeLot)
		{
			CloseOrder closeOrder = new CloseOrder(order, closeLot);
			if(!CloseOrderHelper._orderCloseOrderMapping.containsKey(order))
			{
				CloseOrderHelper._orderCloseOrderMapping.put(order, new ArrayList<CloseOrder>());
			}
			CloseOrderHelper._orderCloseOrderMapping.get(order).add(closeOrder);
			return closeOrder;
		}

		public static ArrayList<CloseOrder> getCreatedCloseOrders(Order order)
		{
			return CloseOrderHelper._orderCloseOrderMapping.get(order);
		}
	}
}
