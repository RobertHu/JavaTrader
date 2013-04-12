package tradingConsole.settings;

import java.math.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.jidesoft.grid.*;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.ui.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class RelationOrder
{
	public static RelationOrder.ComparatorForAdjustingLot comparatorForAdjustingLot = new ComparatorForAdjustingLot();
	private static RelationBindingManager _relationBindingManager = new RelationBindingManager();
	private static OrderStateReceiver _orderStateReceiver = new OrderStateReceiver(RelationOrder._relationBindingManager);

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _openOrderId;
	private BigDecimal _liqLot = BigDecimal.ZERO;
	private BigDecimal _closeLot = null;
	private boolean _isSelected;
	private boolean _isPlacingSpotTrade;
	private Boolean _isMakeLimitOrder;

	private Order _owner;
	private Order _openOrder;
	private RelationOrder _origin;

	public RelationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Order openOrder, BigDecimal closeLot)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._openOrderId = openOrder.get_Id();
		this._openOrder = openOrder;
		this._liqLot = closeLot;
		this._closeLot = closeLot;
	}

	public RelationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Order openOrder)
	{
		this(tradingConsole, settingsManager, openOrder, false, null);
	}

	public RelationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Order openOrder, boolean isPlacingSpotTrade, Boolean isMakeLimitOrder)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._openOrderId = openOrder.get_Id();
		this._openOrder = openOrder;
		this._isPlacingSpotTrade = isPlacingSpotTrade;
		this._isMakeLimitOrder = isMakeLimitOrder;
		this._liqLot = this._openOrder.getAvailableLotBanlance(this._isPlacingSpotTrade, this._isMakeLimitOrder);
	}

	public void resetLiqLot()
	{
		this._liqLot = this._closeLot != null ? this._closeLot : this._openOrder.getAvailableLotBanlance(this._isPlacingSpotTrade, this._isMakeLimitOrder);
	}

	public static OrderStateReceiver get_OrderStateReceiver()
	{
		return RelationOrder._orderStateReceiver;
	}

	public BigDecimal getAvailableCloseLot()
	{
		return this._openOrder.getAvailableLotBanlance(this._isPlacingSpotTrade, this._isMakeLimitOrder);
	}

	public void set_IsPlacingSpotTrade(boolean isPlacingSpotTrade)
	{
		if(isPlacingSpotTrade != this._isPlacingSpotTrade)
		{
			this._isPlacingSpotTrade = isPlacingSpotTrade;
			this._liqLot = this._openOrder.getAvailableLotBanlance(this._isPlacingSpotTrade, this._isMakeLimitOrder);
		}
	}

	public void set_IsMakeLimitOrder(Boolean isMakeLimitOrder)
	{
		if(isMakeLimitOrder != this._isMakeLimitOrder)
		{
			this._isMakeLimitOrder = isMakeLimitOrder;
			this._liqLot = this._openOrder.getAvailableLotBanlance(this._isPlacingSpotTrade, this._isMakeLimitOrder);
		}
	}

	public BigDecimal get_CloseLot()
	{
		return this._liqLot;
	}

	public boolean get_IsPlacingSpotTrade()
	{
		return this._isPlacingSpotTrade;
	}

	public Boolean get_IsMakeLimitOrder()
	{
		return this._isMakeLimitOrder;
	}

	public String getMakeOrderConfirmXml()
	{
		return "<OrderRelation " +
			"OpenOrderID=\'" + this._openOrderId.toString() + "\' " +
			"ClosedLot=\'" + this.get_LiqLotString() + "\'>" +
			"</OrderRelation>";
	}

	//temp???????????
	public String[] getPeerOrders()
	{
		String[] peerOrders = new String[]{"", ""};
		peerOrders[0] = this._openOrderId.toString() + TradingConsole.delimiterCol + this.get_LiqLotString() + TradingConsole.delimiterCol +
			this._openOrder.get_ExecutePriceString();
		peerOrders[1] = this.toString();
		return peerOrders;
	}

	public Guid get_OpenOrderId()
	{
		return this._openOrderId;
	}

	public BigDecimal get_LiqLot()
	{
		return this._liqLot;
	}

	public void set_LiqLot(BigDecimal value)
	{
		if(this._origin != null)
		{
			if(this._origin._liqLot == null)
			{
				this._origin.set_LiqLot(value);
			}
			else if(this._liqLot != null)
			{
				BigDecimal diff = value.subtract(this._liqLot);
				this._origin.set_LiqLot(this._origin._liqLot.add(diff));
			}
		}
		this._liqLot = value;
		if(this._liqLot.compareTo(BigDecimal.ZERO) == 0)
		{
			this._isSelected = false;
		}
	}

	public String get_LiqLotString()
	{
		return AppToolkit.getFormatLot(this._liqLot, this._openOrder.get_Account(), this._openOrder.get_Instrument());
	}

	public void set_LiqLotString(String value)
	{
		if (StringHelper.isNullOrEmpty(value))
		{
			this._liqLot = BigDecimal.ZERO;
		}
		else
		{
			//double newValue = AppToolkit.convertStringToDouble(value);
			//newValue = AppToolkit.convertStringToDouble(AppToolkit.getFormatLot(newValue, this._order.get_Transaction().get_Account()));
			//this._liqLot = new BigDecimal(newValue);
			this._liqLot = AppToolkit.convertStringToBigDecimal(value);
		}
	}

	public Order get_OpenOrder()
	{
		return this._openOrder;
	}

	public boolean get_IsBuy()
	{
		return this._openOrder.get_IsBuy();
	}

	public boolean get_IsSelected()
	{
		return this._isSelected;
	}

	public void set_IsSelected(boolean value)
	{
		this._isSelected = value;
	}

	public String get_OpenOrderSummary()
	{
		return this._openOrder.get_Summary();
	}

	public static PropertyDescriptor[] getPropertyDescriptorsForOutstanding(boolean isMakeOrder2, BuySellType buySellType)
	{
		return RelationOrder.getPropertyDescriptorsForOutstanding(isMakeOrder2, buySellType, false);
	}

	public static PropertyDescriptor[] getPropertyDescriptorsForOutstanding(boolean isMakeOrder2, BuySellType buySellType, boolean allowChangeLiqLot)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[buySellType == BuySellType.Both ? 4 : 3];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, OutstandingOrderColKey.IsSelected, false, null,
			OutstandingOrderLanguage.IsSelected, 50, SwingConstants.LEFT, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[++i] = propertyDescriptor;

		if(buySellType == BuySellType.Both)
		{
			propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, OutstandingOrderColKey.IsBuy, true, null, OutstandingOrderLanguage.IsBuy,
				0, SwingConstants.LEFT, null, null, null, new BooleanCheckBoxCellRenderer());
			propertyDescriptors[++i] = propertyDescriptor;
		}

		propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, OutstandingOrderColKey.OpenOrderSummary, true, null, OutstandingOrderLanguage.OpenOrder,
			(isMakeOrder2) ? 190 : 158,	SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, OutstandingOrderColKey.LiqLotString, allowChangeLiqLot ? false : true, null,
			OutstandingOrderLanguage.LiqLotString, 60,	SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(boolean isMakeOrder2, DataGrid grid, String dataSourceKey,
								  Collection dataSource,  BindingSource bindingSource, IOpenCloseRelationSite openCloseRelationSite, BuySellType buySellType)
	{
		grid.setShowVerticalLines(false);
		grid.setBackground(GridFixedBackColor.relationOrder);
		grid.setForeground(GridBackColor.relationOrder);
		//grid.setSelectionBackground(SelectionBackground.relationOrder);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, RelationOrder.getPropertyDescriptorsForOutstanding(isMakeOrder2, buySellType, openCloseRelationSite.allowChangeCloseLot()));
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.relationOrder, Color.white,
												HeaderFont.relationOrder);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, true, true, GridFont.relationOrder,
											  false, true, true);

		RelationOrder._relationBindingManager.bind(dataSourceKey, grid, bindingSource, openCloseRelationSite, true);

		grid.addFilter(new RelationOrderFilter(bindingSource));
		grid.filter();
		Iterator iterator = dataSource.iterator();
		while(iterator.hasNext())
		{
			((RelationOrder)iterator.next()).setStyle(dataSourceKey);
			//((RelationOrder)iterator.next()).setBackground(dataSourceKey, Color.WHITE);
		}
	}

	private static class RelationOrderFilter extends AbstractTableFilter
	{
		private BindingSource _bindingSource;

		public RelationOrderFilter(BindingSource bindingSource)
		{
			this._bindingSource = bindingSource;
		}

		public boolean isValueFiltered(Object object)
		{
			RelationOrder relationOrder = (RelationOrder)this._bindingSource.getObject(super.getRowIndex());
			BigDecimal balance = relationOrder.get_LiqLot();
			return balance.compareTo(BigDecimal.ZERO) < 0;
		}
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void add(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setStyle(dataSourceKey);
		//this.setBackground(dataSourceKey, GridBackgroundColor.relationOrder);
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setStyle(dataSourceKey);
		//this.setBackground(dataSourceKey, GridBackgroundColor.relationOrder);
	}

	private void setStyle(String dataSourceKey)
	{
		this.setForeground(dataSourceKey, OutstandingOrderColKey.OpenOrderSummary, BuySellColor.getColor(this.get_IsBuy(), false));
		this.setBackground(dataSourceKey, Color.WHITE);
		//this.setForeground(dataSourceKey, Color.BLACK);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		//should change to array......
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	public String getVerificationInfo()
	{
		return this.toString();
	}

	public String toString()
	{
		if(this._openOrder.get_Phase() == Phase.Executed)
		{
			return this._openOrder.get_ExecuteTradeDay() + TradingConsole.delimiterCol2 + this.get_LiqLotString() + TradingConsole.delimiterCol2 +
				this._openOrder.get_ExecutePriceString();
		}
		else
		{
			return this._openOrder.get_SubmitDate() + TradingConsole.delimiterCol2 + this.get_LiqLotString() + TradingConsole.delimiterCol2 +
				this._openOrder.get_SetPriceString();
		}
	}

	public String getInfoForEmail()
	{ //do not change string of "Lot"
		return "[" + this._openOrder.get_ExecutePriceString() + " * " + this.get_LiqLotString() + " Lot]";
	}

	//will process later???????????????
	public void process(Order newOrder)
	{
		/*
		 String newPeerOrderIDs = newOrder.get_PeerOrderIDs();
		 if (StringHelper.isNullOrEmpty(newPeerOrderIDs))
		 {
		  return;
		 }
		 String newOrderCode = newOrder.get_Code();
		 Price newOrderExecutePrice = newOrder.get_ExecutePrice();

		 String[] str1 = StringHelper.split(newPeerOrderIDs, TradingConsole.delimiterRow);
		 for (int i = 0; i < str1.length; i++)
		 {
		  String[] str2 = StringHelper.split(str1[i], TradingConsole.delimiterCol);
		  Guid relationId = new Guid(str2[0]);
		  BigDecimal relationLiqLot = new BigDecimal(Double.valueOf(str2[1]).doubleValue());

		  Order order = tradingConsole.getOrder(relationId);
		  if (order == null)
		  {
		   continue;
		  }

		  String previousRelationPeerOrderIDs = order.get_PeerOrderIDs();
		  String previousRelationPeerOrderCodes = order.get_PeerOrderCodes();
		  BigDecimal relationLotBalance = order.get_LotBalance();

		  String currenctRelationPeerOrderIDs = "";

		 currenctRelationPeerOrderIDs = (!StringHelper.isNullOrEmpty(previousRelationPeerOrderIDs)) ? previousRelationPeerOrderIDs + TradingConsole.delimiterRow : "";
		  currenctRelationPeerOrderIDs += newOrderID.toString() + TradingConsole.delimiterCol + relationLiqLot.toString() + TradingConsole.delimiterCol +
		   Price.toString(newOrderExecutePrice);
		  order.set_PeerOrderIDs(currenctRelationPeerOrderIDs);

		  String currenctRelationPeerOrderCodes = "";
		  currenctRelationPeerOrderCodes = (!StringHelper.isNullOrEmpty(previousRelationPeerOrderCodes)) ? previousRelationPeerOrderCodes + TradingConsole.delimiterRow : "";
		  currenctRelationPeerOrderCodes += Order.getExecuteTradeDay(newOrderCode) + TradingConsole.delimiterCol2 + relationLiqLot.toString() + TradingConsole.delimiterCol2 +
		   Price.toString(newOrderExecutePrice);
		  order.set_PeerOrderCodes(currenctRelationPeerOrderCodes);

		  BigDecimal lotBalance = relationLotBalance.subtract(relationLiqLot);
		  if (lotBalance.longValue() <= 0)
		  {
//				//this.removeOrder(order);
//				order.calculateForDeleteOrder();
//				order.removeGrid2(this.OpenOrderGrid);
		   tradingConsole.removeOrder(order);
		  }
		  else
		  {
		   order.set_lotBalance(lotBalance);
		   order.get_Transaction().get_Instrument().reCalculateTradePLFloat();
		   order.update();
		  }
		 }
		 */
	}

	//Direct Liq
	//From Open Order
	public String get_AccountCode()
	{
		return this._openOrder.get_Transaction().get_Account().get_Code();
	}

	public String get_OpenOrderDirectLiq()
	{
		String space = " ";
		return this.get_AccountCode() + space
			+ this.get_IsBuyStringDirectLiq() + space
			+ this._openOrder.get_LotBalanceString() + space
			//+ this.get_LiqLotString() + space
			+ Language.Lots + space
			+ this._openOrder.get_Transaction().get_Instrument().get_Description() + space
			+ Language.ClosesOut + space
			+ this._openOrder.get_ExecutePriceString();

		//return Order.getExecuteTradeDay(this._order.get_Code()) + TradingConsole.delimiterCol2
		//	+ this.get_LiqLotString() + TradingConsole.delimiterCol2 + this._order.get_ExecutePriceString();
	}

	//From Open Order
	public String get_IsBuyStringDirectLiq()
	{
		return (!this.get_IsBuy()) ? Language.Buy : Language.Sell;
	}

	//From Open Order
	public String get_SetPriceStringDirectLiq()
	{
		return Price.toString(this._openOrder.get_Transaction().get_Instrument().get_LastQuotation().getBuySell(!this.get_IsBuy()));
	}

	//Direct Liq
	public static PropertyDescriptor[] getPropertyDescriptorsForLiquidation(boolean enableEditLot)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];

		//PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.AccountCode, true, null,
		//	MakeOrderLiquidationLanguage.AccountCode,
		//	80, PVColumn.COLUMN_LEFT, null);
		//propertyDescriptors[++i] = propertyDescriptor;
		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.IsSelected, false, null,
			OutstandingOrderLanguage.IsSelected, 20, SwingConstants.LEFT, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[0] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.OpenOrderDirectLiq, true, null,
			MakeOrderLiquidationLanguage.OpenOrderDirectLiq, 240, SwingConstants.LEFT, null, null);
		propertyDescriptors[1] = propertyDescriptor;

		//propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.IsBuyStringDirectLiq, true, null,
		//	MakeOrderLiquidationLanguage.IsBuyStringDirectLiq, 60,
		//	PVColumn.COLUMN_LEFT, null);
		//propertyDescriptors[++i] = propertyDescriptor;

		//propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.SetPriceStringDirectLiq, true, null,
		//	MakeOrderLiquidationLanguage.SetPriceStringDirectLiq, 60,
		//	PVColumn.COLUMN_LEFT, null);
		//propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(RelationOrder.class, MakeOrderLiquidationGridColKey.LiqLotString, !enableEditLot, null,
			MakeOrderLiquidationLanguage.LiqLotString, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[2] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initializeLiquidation(DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource, IOpenCloseRelationBaseSite openCloseRelationBaseSite, boolean enableEditLot)
	{
		grid.setShowVerticalLines(false);
		grid.setBackground(GridFixedBackColor.relationOrder);
		grid.setForeground(GridBackColor.relationOrder);
		//grid.setSelectionBackground(SelectionBackground.relationOrder);


		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, RelationOrder.getPropertyDescriptorsForLiquidation(enableEditLot));
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 20, GridFixedForeColor.relationOrder, Color.white,
												HeaderFont.relationOrder);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, true, true, GridFont.relationOrder, false, true, true);

		RelationOrder._relationBindingManager.bind(dataSourceKey, grid, bindingSource, openCloseRelationBaseSite, false);
	}

	public void addLiquidation(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setStyleLiquidation(dataSourceKey);
		//this.setBackground(dataSourceKey, GridBackgroundColor.relationOrder);
	}

	public void updateLiquidation(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setStyleLiquidation(dataSourceKey);
		//this.setBackground(dataSourceKey, GridBackgroundColor.relationOrder);
	}

	private void setStyleLiquidation(String dataSourceKey)
	{
		Color color = BuySellColor.getColor(!this.get_IsBuy(), false);
		this.setForeground(dataSourceKey, MakeOrderLiquidationGridColKey.AccountCode, color);
		this.setForeground(dataSourceKey, MakeOrderLiquidationGridColKey.OpenOrderDirectLiq, color);
		this.setForeground(dataSourceKey, MakeOrderLiquidationGridColKey.IsBuyStringDirectLiq, color);
		this.setForeground(dataSourceKey, MakeOrderLiquidationGridColKey.SetPriceStringDirectLiq, color);
		this.setForeground(dataSourceKey, MakeOrderLiquidationGridColKey.LiqLotString, color);
		this.setBackground(dataSourceKey, Color.WHITE);
	}

	public void set_Owner(Order owner)
	{
		this._owner = owner;
	}

	static void updateCloseLotSafely(IOpenCloseRelationBaseSite site, String text)
	{
		if(site.getTotalLotEditor() != site.getCloseLotEditor())
		{
			CloseLotDocumentListener closeLotDocumentListener
				= RelationOrder._relationBindingManager.getRelationBinding(site)._closeLotDocumentListener;
			site.getCloseLotEditor().getDocument().removeDocumentListener(closeLotDocumentListener);
			try
			{
				site.getCloseLotEditor().setText(text);
			}
			catch (IllegalStateException exception)
			{
				exception.printStackTrace();
				//TradingConsole.traceSource.trace(TraceType.Information, exception);
			}
			finally
			{
				site.getCloseLotEditor().getDocument().addDocumentListener(closeLotDocumentListener);
			}
		}
	}

	public void set_Origin(RelationOrder origin)
	{
		this._origin = origin;
	}

	public static class ComparatorForAdjustingLot implements Comparator<RelationOrder>
	{
		private ComparatorForAdjustingLot(){}

		public int compare(RelationOrder left, RelationOrder right)
		{
			DateTime leftExecuteTime = left.get_OpenOrder().get_Transaction().get_ExecuteTime();
			DateTime rightExecuteTime = right.get_OpenOrder().get_Transaction().get_ExecuteTime();
			int result = leftExecuteTime.compareTo(rightExecuteTime);

			/*if(result == 0)
			{
				int leftIsPair = left._owner.get_Transaction().get_Type() == TransactionType.Pair ? 0 : 1;
				int rightIsPair = right._owner.get_Transaction().get_Type() == TransactionType.Pair ? 0 : 1;
				result = leftIsPair - rightIsPair;
			}*/
			return result;
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}

	private static class PropertySensitiveRelationBinding extends RelationBinding
	{
		private RelationOrderPropertyChangingListener _relationOrderPropertyChangingListener = new RelationOrderPropertyChangingListener();
		private RelationOrderPropertyChangedListener _relationOrderPropertyChangedListener = new RelationOrderPropertyChangedListener();

		private PropertySensitiveRelationBinding(String dataSourceKey, DataGrid grid, BindingSource bindingSource, IOpenCloseRelationBaseSite site)
		{
			super(dataSourceKey, grid, bindingSource, site);
		}

		@Override
		protected void bind()
		{
			super.bind();

			this._bindingSource.removePropertyChangingListener(this._relationOrderPropertyChangingListener);
			this._relationOrderPropertyChangingListener.set_RelationOrderSite((IOpenCloseRelationSite)this._site);
			this._bindingSource.addPropertyChangingListener(this._relationOrderPropertyChangingListener);

			this._bindingSource.removePropertyChangedListener(this._relationOrderPropertyChangedListener);
			this._relationOrderPropertyChangedListener.set_RelationOrderSite((IOpenCloseRelationSite)this._site);
			this._bindingSource.addPropertyChangedListener(this._relationOrderPropertyChangedListener);
		}

		@Override
		protected void unbind()
		{
			this._bindingSource.removePropertyChangingListener(this._relationOrderPropertyChangingListener);
			this._bindingSource.removePropertyChangedListener(this._relationOrderPropertyChangedListener);
			super.unbind();
		}
	}

	private static class RelationBinding
	{
		protected String _dataSourceKey;
		protected DataGrid _grid;
		protected BindingSource _bindingSource;
		protected IOpenCloseRelationBaseSite _site;

		private OrderTypeChangedListener _orderTypeChangedListener = new OrderTypeChangedListener();
		private RelationGridSelectedRowChangedListener _relationGridSelectedRowChangedListener = new RelationGridSelectedRowChangedListener();
		private CloseLotDocumentListener _closeLotDocumentListener = new CloseLotDocumentListener();

		protected RelationBinding(String dataSourceKey, DataGrid grid, BindingSource bindingSource, IOpenCloseRelationBaseSite site)
		{
			this._dataSourceKey = dataSourceKey;
			this._grid = grid;
			this._bindingSource = bindingSource;
			this._site = site;
		}

		private String get_DataSourceKey()
		{
			return this._dataSourceKey;
		}

		private DataGrid get_DataGrid()
		{
			return this._grid;
		}

		private BindingSource get_BindingSource()
		{
			return this._bindingSource;
		}

		private IOpenCloseRelationBaseSite get_OpenCloseRelationSite()
		{
			return this._site;
		}

		protected void bind()
		{
			for(int index = 0; index < this._bindingSource.getRowCount(); index++)
			{
				RelationOrder relationOrder = (RelationOrder) (this._bindingSource.getObject(index));
				relationOrder.set_IsPlacingSpotTrade(this._site.getOrderType().isSpot());
				TradingConsole.bindingManager.update(this._dataSourceKey, relationOrder);
			}

			this._orderTypeChangedListener.initialize(this._dataSourceKey, this._grid, this._bindingSource, this._site);
			this._site.addPlaceOrderTypeChangedListener(this._orderTypeChangedListener);

			if (this._site.getCloseLotEditor() != null)
			{
				this._site.getCloseLotEditor().setEnabled(false);
				this._relationGridSelectedRowChangedListener.initialize(this._site);
				this._grid.addSelectedRowChangedListener(this._relationGridSelectedRowChangedListener);

				this._closeLotDocumentListener.initialize(this._grid, this._site.getCloseLotEditor());
				this._site.getCloseLotEditor().getDocument().addDocumentListener(this._closeLotDocumentListener);
			}
		}

		protected void unbind()
		{
			if(this._site != null)
			{
				this._site.removePlaceOrderTypeChangedListener(this._orderTypeChangedListener);

				if (this._site.getCloseLotEditor() != null)
				{
					this._site.getCloseLotEditor().getDocument().removeDocumentListener(this._closeLotDocumentListener);
				}
			}

			if (this._grid != null)
			{
				this._grid.clearFilters();
				this._grid.removeSelectedRowChangedListener(this._relationGridSelectedRowChangedListener);
			}
		}

		protected void rebind(String dataSourceKey, DataGrid grid, BindingSource bindingSource, IOpenCloseRelationBaseSite site)
		{
			this.unbind();

			this._dataSourceKey = dataSourceKey;
			this._grid = grid;
			this._bindingSource = bindingSource;
			this._site = site;

			this.bind();
		}
	}

	private static class RelationBindingManager
	{
		private HashMap<Window, ArrayList<RelationBinding>> _relationBindings = new HashMap<Window, ArrayList<RelationBinding>>();

		public void bind(String dataSourceKey, DataGrid grid, BindingSource bindingSource, IOpenCloseRelationBaseSite site, boolean PropertySensitive)
		{
			Window residedFrame = site.getFrame();
			RelationBinding relationBinding = this.getRelationBinding(residedFrame, site);
			if(relationBinding != null)
			{
				relationBinding.rebind(dataSourceKey, grid, bindingSource, site);
			}
			else
			{
				if (!this._relationBindings.containsKey(residedFrame))
				{
					this._relationBindings.put(residedFrame, new ArrayList<RelationBinding>());

					WindowAdapter windowListener = new WindowAdapter()
					{
						@Override
						public void windowClosed(WindowEvent e)
						{
							if (_relationBindings.containsKey(e.getWindow()))
							{
								for (RelationBinding placingSite : _relationBindings.get(e.getWindow()))
								{
									placingSite.unbind();
								}
								_relationBindings.remove(e.getWindow());
							}
						}
					};
					residedFrame.addWindowListener(windowListener);
				}
				relationBinding = PropertySensitive ? new PropertySensitiveRelationBinding(dataSourceKey, grid, bindingSource, site) : new RelationBinding(dataSourceKey, grid, bindingSource, site);
				relationBinding.bind();
				this._relationBindings.get(residedFrame).add(relationBinding);
			}
		}

		public RelationBinding getRelationBinding(IOpenCloseRelationBaseSite site)
		{
			for(ArrayList<RelationBinding> relationBindings : this._relationBindings.values())
			{
				for(RelationBinding relationBinding : relationBindings)
				{
					if(relationBinding.get_OpenCloseRelationSite() == site)
					{
						return relationBinding;
					}
				}
			}
			return null;
		}

		private RelationBinding getRelationBinding(Window residedFrame, IOpenCloseRelationBaseSite site)
		{
			if(this._relationBindings.containsKey(residedFrame))
			{
				for(RelationBinding relationBinding : this._relationBindings.get(residedFrame))
				{
					if(relationBinding.get_OpenCloseRelationSite() == site)
					{
						return relationBinding;
					}
				}
			}
			return null;
		}
	}

	public static class OrderStateReceiver
	{
		private RelationSnapshot _relationSnapshot = new RelationSnapshot();
		private RelationBindingManager _relationBindingManager;

		private OrderStateReceiver(RelationBindingManager relationBindingManager)
		{
			this._relationBindingManager = relationBindingManager;
		}

		public void add(Order order)
		{
			this.stateChange(order);
		}

		public void remove(Order order)
		{
			this.stateChange(order);
		}

		public void lotBanlanceChanged(Order order)
		{
			this.stateChange(order);
		}

		private void stateChange(Order order)
		{
			for(ArrayList<RelationBinding> relationBindings : this._relationBindingManager._relationBindings.values())
			{
				for(RelationBinding relationBinding : relationBindings)
				{
					if(order.get_Transaction().get_Instrument() == relationBinding.get_OpenCloseRelationSite().getInstrument())
					{
						this.rebind(relationBinding.get_OpenCloseRelationSite());
					}
				}
			}
		}

		private void rebind(IOpenCloseRelationBaseSite site)
		{
			this._relationSnapshot.takeSnapshot(site);
			site.rebind();
			this._relationSnapshot.applySnapshot(site);
		}

		public void clearSnapshort(MakeOrderAccount makeOrderAccount)
		{
			this._relationSnapshot.clearSnapshot(makeOrderAccount);
		}
	}

	public static class RelationSnapshot
	{
		private HashMap<MakeOrderAccount, ArrayList<RelationOrder>> _relationSnapshot
			= new HashMap<MakeOrderAccount, ArrayList<RelationOrder>>();

		public void applySnapshot(IOpenCloseRelationBaseSite site)
		{
			MakeOrderAccount makeOrderAccount = site.getMakeOrderAccount();
			if(makeOrderAccount == null) return;

			DataGrid relationDataGrid = site.getRelationDataGrid();

			BigDecimal lastLot = makeOrderAccount.get_LotCurrent();

			if(this._relationSnapshot.containsKey(makeOrderAccount))
			{
				if(relationDataGrid.get_BindingSource() == null) return;

				int selectedColumn = relationDataGrid.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
				int liqLotColumn = relationDataGrid.get_BindingSource().getColumnByName(OutstandingOrderColKey.LiqLotString);

				ArrayList<RelationOrder> relationOrders = this._relationSnapshot.get(makeOrderAccount);
				for(int row = 0; row < relationDataGrid.getRowCount(); row++)
				{
					RelationOrder relationOrder = (RelationOrder)relationDataGrid.get_BindingSource().getObject(row);
					for(RelationOrder relationOrder2 : relationOrders)
					{
						if(relationOrder2.get_OpenOrderId().equals(relationOrder.get_OpenOrderId()))
						{
							BigDecimal availableCloseLot = relationOrder.getAvailableCloseLot();
							BigDecimal closeLot = relationOrder2.get_LiqLot();
							closeLot = availableCloseLot.compareTo(closeLot) > 0 ? closeLot : availableCloseLot;
							String value = AppToolkit.getFormatLot(closeLot, makeOrderAccount.get_Account(), makeOrderAccount.get_Instrument());
							relationDataGrid.get_BindingSource().setValueAt(value, row, liqLotColumn);
							relationDataGrid.get_BindingSource().setValueAt(Boolean.TRUE, row, selectedColumn);
						}
					}
				}
			}

			if(site.getAccountDataGrid() != null && site.getAccountDataGrid().get_BindingSource() != null
			   && lastLot.compareTo(makeOrderAccount.get_LotCurrent()) > 0)
			{
				int row = site.getAccountDataGrid().get_BindingSource().getRow(makeOrderAccount);
				int column = site.getAccountDataGrid().get_BindingSource().getColumnByName(MakeOrderAccountGridColKey.LotString);

				String value = AppToolkit.getFormatLot(lastLot, makeOrderAccount.get_Account(), makeOrderAccount.get_Instrument());
				site.getAccountDataGrid().get_BindingSource().setValueAt(value, row, column);
			}

			if(site.getRelationDataGrid() != null && site.getRelationDataGrid().getRowCount() > 0)
			{
				site.getRelationDataGrid().changeSelection(0,0,false,false);
			}
		}

		public void takeSnapshot(IOpenCloseRelationBaseSite site)
		{
			MakeOrderAccount makeOrderAccount = site.getMakeOrderAccount();
			if(makeOrderAccount == null) return;

			DataGrid dataGrid = site.getRelationDataGrid();

			if(this._relationSnapshot.containsKey(makeOrderAccount))
			{
				this._relationSnapshot.get(makeOrderAccount).clear();
			}
			else
			{
				this._relationSnapshot.put(makeOrderAccount, new ArrayList<RelationOrder>());
			}

			for(int row = 0; row < dataGrid.getRowCount(); row++)
			{
				RelationOrder relationOrder = (RelationOrder)dataGrid.getObject(row);
				if(relationOrder.get_IsSelected())
				{
					this._relationSnapshot.get(makeOrderAccount).add(relationOrder);
				}
			}
		}

		public void clearSnapshot(MakeOrderAccount makeOrderAccount)
		{
			this._relationSnapshot.remove(makeOrderAccount);
		}

		public void updateSnapshot(MakeOrderAccount makeOrderAccount)
		{
			if(this._relationSnapshot.containsKey(makeOrderAccount))
			{
				this._relationSnapshot.get(makeOrderAccount).clear();
			}
			else
			{
				this._relationSnapshot.put(makeOrderAccount, new ArrayList<RelationOrder>());
			}

			for(RelationOrder relationOrder : makeOrderAccount.getOutstandingOrders().values())
			{
				if(relationOrder.get_IsSelected())
				{
					this._relationSnapshot.get(makeOrderAccount).add(relationOrder);
				}
			}
		}
	}

	private static class CloseLotDocumentListener implements DocumentListener
	{
		private DataGrid _grid;
		private JTextField _owner;
		private String _oldText;

		public void initialize(DataGrid grid, JTextField owner)
		{
			this._grid = grid;
			this._owner = owner;
			this._oldText = null;
		}

		public void insertUpdate(DocumentEvent e)
		{
			this.handle(e);
		}

		public void removeUpdate(DocumentEvent e)
		{
			//this.handle(e);
		}

		public void changedUpdate(DocumentEvent e)
		{
			this.handle(e);
		}

		private void handle(DocumentEvent e)
		{
			//if(e.getType() == DocumentEvent.EventType.CHANGE)
			{
				String text = this._owner.getText();
				if(text.startsWith(".")) return;
				if(text.endsWith(".")) text = text + "0";

				int row = this._grid.getSelectedRow();
				if(row == -1) return;
				/*if(row == -1 && this._grid.getRowCount() == 1)
				{
					//this._grid.changeSelection(0, 0, true, true);
					row = 0;
				}*/

				if (!StringHelper.isNullOrEmpty(text) && row != -1 /* && this._grid.getRowCount() > 1*/)
				{
					RelationOrder relationOrder = (RelationOrder)this._grid.getObject(row);
					boolean selected = relationOrder._isSelected;
					Object value = this._grid.getValueAt(row, this.getLotStringColumn());
					BigDecimal oldCloseLot = new BigDecimal(value == null || value.toString().length() == 0 ? "0" : value.toString());
					BigDecimal newCloseLot = BigDecimal.ZERO;
					try
					{
						newCloseLot = new BigDecimal(text);
					}
					catch(NumberFormatException ex)
					{
						return;
					}

					if(selected && oldCloseLot.compareTo(newCloseLot) != 0)
					{
						SwingUtilities.invokeLater(new LiqueLotUpdater(this._grid, text, row, this.getLotStringColumn()));
					}
				}
			}
		}

		private static class LiqueLotUpdater implements Runnable
		{
			private DataGrid _grid;
			private String _text;
			private int _row;
			private int _column;

			public LiqueLotUpdater(DataGrid grid, String text, int row, int column)
			{
				this._grid = grid;
				this._text = text;
				this._row = row;
				this._column = column;
			}

			public void run()
			{
				this._grid.setValueAt(this._text, this._row, this._column);
			}
		}

		private int getLotStringColumn()
		{
			TableColumnModel columnModel = this._grid.getColumnModel();
			for(int column = 0; column < columnModel.getColumnCount(); column++)
			{
				Object identifier = columnModel.getColumn(column).getIdentifier();
				if(identifier.equals(OutstandingOrderColKey.LiqLotString) || identifier.equals(MakeOrderLiquidationGridColKey.LiqLotString))
				{
					return column;
				}
			}
			return -1;
		}
	}

	private static class RelationGridSelectedRowChangedListener implements ISelectedRowChangedListener
	{
		private IOpenCloseRelationBaseSite _site;

		public void initialize(IOpenCloseRelationBaseSite site)
		{
			this._site = site;
		}

		public void selectedRowChanged(DataGrid source)
		{
			if(this._site.getCloseLotEditor() != null)
			{
				int selectedRow = source.getSelectedRow();
				if (selectedRow != -1)
				{
					RelationOrder realtionOrder = (RelationOrder)source.getObject(selectedRow);

					if (this._site.getTotalLotEditor() != this._site.getCloseLotEditor())
					{
						this._site.getCloseLotEditor().setEnabled(realtionOrder.get_IsSelected());
						this._site.getCloseLotEditor().setBackground(realtionOrder.get_IsSelected() ? Color.WHITE : Color.LIGHT_GRAY);
					}

					if (realtionOrder.get_IsSelected())
					{
						RelationOrder.updateCloseLotSafely(this._site, realtionOrder.get_LiqLotString());
						return;
					}

					RelationOrder.updateCloseLotSafely(this._site, "");
				}
			}
		}
	}
}

class OrderTypeChangedListener implements IPlaceOrderTypeChangedListener
{
	private String _dataSourceKey;
	private DataGrid _grid;
	private BindingSource _bindingSource;
	private IOpenCloseRelationBaseSite _site;

	public void initialize(String dataSourceKey, DataGrid grid, tradingConsole.ui.grid.BindingSource bindingSource, IOpenCloseRelationBaseSite site)
	{
		this._dataSourceKey = dataSourceKey;
		this._grid = grid;
		this._bindingSource = bindingSource;
		this._site = site;
	}

	public void OrderTypeChanged(OrderType newOrderType, OrderType oldOrderType)
	{
		boolean isSpot = newOrderType == null ? false : newOrderType.isSpot();
		boolean isSpotTradeChanged = (oldOrderType == null || isSpot != oldOrderType.isSpot());

		if(isSpotTradeChanged)
		{
			for (int row = 0; row < this._bindingSource.getRowCount(); row++)
			{
				RelationOrder relationOrder = (RelationOrder) this._bindingSource.getObject(row);
				//if (isSpotTradeChanged)
				{
					relationOrder.set_IsPlacingSpotTrade(isSpot);
					TradingConsole.bindingManager.update(this._dataSourceKey, relationOrder,
						MakeOrderLiquidationGridColKey.LiqLotString, relationOrder.get_LiqLotString());
				}
			}
			this._grid.filter();

			BigDecimal totalCloseLot = BigDecimal.ZERO;
			for(int row = 0; row < this._grid.getRowCount(); row++)
			{
				RelationOrder relationOrder = (RelationOrder)this._grid.getObject(row);
				if(relationOrder.get_IsSelected())
				{
					totalCloseLot = totalCloseLot.add(relationOrder.get_LiqLot());
				}
			}
			this._site.getTotalLotEditor().setText(totalCloseLot.toString());

			int selectedRow = this._grid.getSelectedRow();
			if(selectedRow != -1)
			{
				RelationOrder relationOrder = (RelationOrder)this._grid.getObject(selectedRow);
				if(relationOrder.get_IsSelected())
				{
					RelationOrder.updateCloseLotSafely(this._site, relationOrder.get_LiqLotString());
				}
			}
		}
	}
}

class RelationOrderPropertyChangedListener implements IPropertyChangedListener
{
	private IOpenCloseRelationSite _openCloseRelationSite;

	public void set_RelationOrderSite(IOpenCloseRelationSite openCloseRelationSite)
	{
		this._openCloseRelationSite = openCloseRelationSite;
	}

	private BigDecimal getTotalCloseLot(DataGrid table)
	{
		int selectedRow = table.getSelectedRow();
		Boolean isBuy = null;
		if(selectedRow >= 0)
		{
			RelationOrder relationOrder = (RelationOrder)table.getObject(selectedRow);
			isBuy = relationOrder.get_IsBuy() ? Boolean.TRUE : Boolean.FALSE;
		}
		BigDecimal totalCloseLot = BigDecimal.ZERO;
		for(int rowIndex = 0; rowIndex < table.get_BindingSource().getRowCount(); rowIndex++)
		{
			RelationOrder relationOrder = (RelationOrder)table.get_BindingSource().getObject(rowIndex);
			if(relationOrder.get_IsSelected() && (isBuy == null || relationOrder.get_IsBuy() == isBuy))
			{
				BigDecimal closeLot = relationOrder.get_LiqLot();
				totalCloseLot = totalCloseLot.add(closeLot);
			}
		}
		return totalCloseLot;
	}

	public void propertyChanged(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column)
	{
		DataGrid table = this._openCloseRelationSite.getRelationDataGrid();
		BigDecimal totalCloseLot = this.getTotalCloseLot(table);
		this._openCloseRelationSite.updateTotalColseLot(totalCloseLot);
		if (propertyDescriptor.get_Name().equals(OutstandingOrderColKey.IsSelected))
		{
			RelationOrder relationOrder = (RelationOrder)table.get_BindingSource().getObject(row);
			if(relationOrder.get_IsSelected()/* && relationOrder.get_LiqLot().compareTo(BigDecimal.ZERO) > 0*/)
			{
				relationOrder.resetLiqLot();
				if(this._openCloseRelationSite.getCloseLotEditor() != null &&
				   this._openCloseRelationSite.getTotalLotEditor() != this._openCloseRelationSite.getCloseLotEditor())
				{
					this._openCloseRelationSite.getCloseLotEditor().setEnabled(true);
					this._openCloseRelationSite.getCloseLotEditor().setBackground(Color.WHITE);
					this._openCloseRelationSite.getCloseLotEditor().setText(relationOrder.get_LiqLotString());
				}
			}
			else
			{
				if(this._openCloseRelationSite.getCloseLotEditor() != null &&
				   this._openCloseRelationSite.getTotalLotEditor() != this._openCloseRelationSite.getCloseLotEditor())
				{
					this._openCloseRelationSite.getCloseLotEditor().setEnabled(false);
					this._openCloseRelationSite.getCloseLotEditor().setBackground(Color.LIGHT_GRAY);
					this._openCloseRelationSite.getCloseLotEditor().setText("");
				}
			}
		}
		else if(propertyDescriptor.get_Name().equals(OutstandingOrderColKey.LiqLotString))
		{
			if(this._openCloseRelationSite.getCloseLotEditor() != null)
			{
				if(!this._openCloseRelationSite.getCloseLotEditor().getText().equals(newValue.toString()))
				{
					this._openCloseRelationSite.getCloseLotEditor().setText(newValue.toString());
				}
			}
		}
	}
}

class RelationOrderPropertyChangingListener implements IPropertyChangingListener
{
	private IOpenCloseRelationSite _openCloseRelationSite;

	public void set_RelationOrderSite(IOpenCloseRelationSite openCloseRelationSite)
	{
		this._openCloseRelationSite = openCloseRelationSite;
	}

	public void propertyChanging(PropertyChangingEvent e)
	{
		DataGrid table = this._openCloseRelationSite.getRelationDataGrid();
		RelationOrder relationOrder = (RelationOrder)e.get_Owner();
		Order order = relationOrder.get_OpenOrder();
		table.setSelectionForeground(BuySellColor.getColor(order.get_IsBuy(), false));
		BigDecimal sumLiqLots;
		//switch (e.getID())
		{
			if (e.get_PropertyDescriptor().get_Name().equals(OutstandingOrderColKey.IsSelected))
			{
				//old value
				boolean isSelected = relationOrder.get_IsSelected();

				boolean isBuy = !order.get_IsBuy();

				sumLiqLots = this._openCloseRelationSite.getMakeOrderAccount().getSumLiqLots(isBuy);
				BigDecimal accountLot = BigDecimal.ZERO;
				BigDecimal currentLiqLot = relationOrder.get_LiqLot();

				//if the value change to selected will:
				if (!isSelected && ( (Boolean)e.get_NewValue()))
				{
					TradingConsole.traceSource.trace(TraceType.Information, "RelationOrder.propertyChanging: " + new FrameworkException("").getStackTrace());

					int selectedColumn = table.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
					for (int row = 0; row < table.getRowCount(); row++)
					{
						RelationOrder relationOrder2 = (RelationOrder)table.get_BindingSource().getObject(row);
						if (relationOrder2.get_IsBuy() != relationOrder.get_IsBuy())
						{
							table.get_BindingSource().setValueAt(false, row, selectedColumn);
						}
					}

					if (this._openCloseRelationSite.getOperateType() == OperateType.Assign)
					{
						//sumLiqLots can not > lot
						if (this._openCloseRelationSite.getLot().compareTo( (sumLiqLots.add(currentLiqLot))) < 0)
						{
							AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
								Language.OrderOperateOutstandingOrderGridValidateEditPrompt1);
							e.set_Cancel(true);
							return;
						}
					}
					else
					{
						BigDecimal maxLot = (this._openCloseRelationSite.getOperateType() == OperateType.Limit) ?
							this._openCloseRelationSite.getInstrument().get_MaxOtherLot() : this._openCloseRelationSite.getInstrument().get_MaxDQLot();

						if (maxLot.compareTo(BigDecimal.ZERO) != 0
							&& (sumLiqLots.add(currentLiqLot).compareTo(maxLot) > 0))
						{
							//BigDecimal leftLot = maxLot.subtract(sumLiqLots);
							//if(leftLot.compareTo(BigDecimal.ZERO) <= 0)
							{
								TradingConsole.traceSource.trace(TraceType.Information,
									"sumLiqLots= " + sumLiqLots.toString() + "; maxLot = " + maxLot.toString()
									+ new FrameworkException("").getStackTrace());

								AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
									( (this._openCloseRelationSite.getOperateType() == OperateType.Limit) ? Language.OrderLMTPagetextLot_OnblurAlert0 :
									 Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert0)
									+ "("
									+ AppToolkit.getFormatLot(maxLot, this._openCloseRelationSite.getMakeOrderAccount().get_Account(),this._openCloseRelationSite.getMakeOrderAccount().get_Instrument())
									+ ")!");
								//e.set_Cancel(true);
								return;
							}
							/*else
							{
								currentLiqLot = leftLot;
								relationOrder.set_LiqLot(leftLot);
							}*/
						}

						if (this._openCloseRelationSite.getTotalQuantity().compareTo(BigDecimal.ZERO) != 0) //for multiDQOrderForm
						{
							if (sumLiqLots.add(currentLiqLot).compareTo(this._openCloseRelationSite.getTotalQuantity()) != 0)
							{
								this._openCloseRelationSite.updateTotalColseLot(sumLiqLots.add(currentLiqLot));
							}

							/*if (sumLiqLots.add(currentLiqLot).compareTo(this._openCloseRelationSite.getTotalQuantity()) > 0)
							{
								AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
									Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert4);
								//+ "("
								//+ framework.Convert.toString(totalQuantity)
								//+ ")!");
								e.set_Cancel(true);
								return;
							}*/
						}
						accountLot = sumLiqLots.add(currentLiqLot);
					}
				}
				else if(isSelected && ! ( (Boolean)e.get_NewValue()))//if the value change to non-selected will:
				{
					if (! (this._openCloseRelationSite.getOperateType() == OperateType.Assign))
					{
						accountLot = sumLiqLots.subtract(currentLiqLot);
					}
				}

				isSelected = !isSelected;
				relationOrder.set_IsSelected(isSelected);

				this._openCloseRelationSite.updateAccount(accountLot, order.get_IsBuy());

				//because has save IsSelected, so setcancel = true
				//e.setCancel(true);
				table.doLayout();
				table.requestFocus();
			}
			else if (e.get_PropertyDescriptor().get_Name().equals(OutstandingOrderColKey.LiqLotString))
			{
				BigDecimal oldValue = AppToolkit.convertStringToBigDecimal(e.get_OldValue().toString());
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.get_NewValue().toString());
				if (/*newValue.compareTo(BigDecimal.ZERO) == 0 && !StringHelper.isNullOrEmpty(e.get_NewValue().toString())
					|| */newValue.compareTo(BigDecimal.ZERO) < 0)
				{
					if(this._openCloseRelationSite.getCloseLotEditor() != null)
					{
						if(!this._openCloseRelationSite.getCloseLotEditor().getText().equals(oldValue.toString()))
						{
							this._openCloseRelationSite.getCloseLotEditor().setText(oldValue.toString());
						}
					}
					e.set_Cancel(true);
					return;
				}
				//???
				BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue,
					this._openCloseRelationSite.getMakeOrderAccount().get_Account(),
					this._openCloseRelationSite.getMakeOrderAccount().get_Instrument()));
				if (/*newValue2.compareTo(BigDecimal.ZERO) == 0 ||*/ newValue2.compareTo(newValue) != 0)
				{
					if(this._openCloseRelationSite.getCloseLotEditor() != null)
					{
						if(!this._openCloseRelationSite.getCloseLotEditor().getText().equals(oldValue.toString()))
						{
							this._openCloseRelationSite.getCloseLotEditor().setText(oldValue.toString());
						}
					}
					e.set_Cancel(true);
					return;
				}

				//new value can not > order.lotBalance
				/*if (newValue.compareTo(order.get_LotBalance()) > 0)
				{
					AlertDialogForm.showDialog(this._relationOrderSite.getFrame(), null, true,
											   Language.OrderOperateOutstandingOrderGridValidateEditPrompt0 + order.get_LotBalanceString() + ")!");
					e.set_Cancel(true);
					return;
				}*/
		        BigDecimal avaiableCloseLot = order.getAvailableLotBanlance(relationOrder.get_IsPlacingSpotTrade(), relationOrder.get_IsMakeLimitOrder());
				if (newValue.compareTo(avaiableCloseLot) > 0)
				{
					e.set_NewValue(AppToolkit.getFormatLot(avaiableCloseLot,
						this._openCloseRelationSite.getMakeOrderAccount().get_Account(),
						this._openCloseRelationSite.getMakeOrderAccount().get_Instrument()));
					newValue = avaiableCloseLot;
					/*AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
											   Language.OrderOperateOutstandingOrderGridValidateEditPrompt0);
					e.set_Cancel(true);
					return;*/
				}

				sumLiqLots = this._openCloseRelationSite.getMakeOrderAccount().getSumLiqLots(!order.get_IsBuy());
				if (relationOrder.get_IsSelected())
				{
					sumLiqLots = sumLiqLots.subtract(oldValue).add(newValue);
					if (this._openCloseRelationSite.getOperateType() != OperateType.Assign)
					{
						if (this._openCloseRelationSite.getTotalQuantity().compareTo(BigDecimal.ZERO) != 0) //for multiDQOrderForm
						{
							if (sumLiqLots.compareTo(this._openCloseRelationSite.getTotalQuantity()) != 0)
							{
								this._openCloseRelationSite.updateTotalColseLot(sumLiqLots);
							}

							/*if (sumLiqLots.compareTo(this._openCloseRelationSite.getTotalQuantity()) > 0)
							{
								AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
									Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert4);
								//+ "("
								//+ framework.Convert.toString(totalQuantity)
								//+ ")!");
								e.set_Cancel(true);
								return;
							}*/
						}
					}
					else
					{
						//sumLiqLots can not > lot
						if (this._openCloseRelationSite.getLot().compareTo(sumLiqLots) < 0)
						{
							AlertDialogForm.showDialog(this._openCloseRelationSite.getFrame(), null, true,
								Language.OrderOperateOutstandingOrderGridValidateEditPrompt1);
							if(this._openCloseRelationSite.getCloseLotEditor() != null)
							{
								if(!this._openCloseRelationSite.getCloseLotEditor().getText().equals(oldValue.toString()))
								{
									this._openCloseRelationSite.getCloseLotEditor().setText(oldValue.toString());
								}
							}
							e.set_Cancel(true);
							return;
						}
					}
				}

				//table.setCellText(row, col,AppToolkit.getFormatLot(newValue, makeOrderAccount.get_Account()));
				//e.setString("1");
				//table.doLayout();

				if (this._openCloseRelationSite.getLiqLotValueStaticText() != null)
				{
					this._openCloseRelationSite.getLiqLotValueStaticText().setText(AppToolkit.getFormatLot(sumLiqLots,
						this._openCloseRelationSite.getMakeOrderAccount().get_Account(),
						this._openCloseRelationSite.getMakeOrderAccount().get_Instrument()));
				}

				this._openCloseRelationSite.updateAccount(sumLiqLots, order.get_IsBuy());
			}
		}
	}
}
