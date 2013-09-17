package tradingConsole.ui;

import java.math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import java.util.ArrayList;
import framework.threading.Scheduler.ISchedulerCallback;
import framework.threading.Scheduler.SchedulerEntry;
import framework.diagnostics.TraceType;
import tradingConsole.framework.PropertyDescriptor;
import framework.diagnostics.TraceSource;

public class LiquidationOrderForm extends JDialog implements ISchedulerCallback
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeLiquidationOrder _makeLiquidationOrder;
	private Instrument _instrument;
	private OpenContractForm _openContractForm;
	private PropertyChangingListener _propertyChangingListener;
	private PropertyChangedListener _propertyChangedListener;
	private OpenCloseRelationBaseSite _openCloseRelationSite;
	private SchedulerEntry _timeoutEntry;

	public LiquidationOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, MakeLiquidationOrder makeLiquidationOrder,
		OpenContractForm openContractForm)
	{
		this(openContractForm != openContractForm ? null : tradingConsole.get_MainForm());

		this._openContractForm = openContractForm;
		if(this._openContractForm != null)
		{
			this._openContractForm.setCanClose(false);
		}

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._makeLiquidationOrder = makeLiquidationOrder;

		Order order = this._makeLiquidationOrder.getFirstOpenOrderHasClosed(this._tradingConsole, null);
		this._instrument = order.get_Transaction().get_Instrument();

		if(!StringHelper.isNullOrEmpty(this._instrument.get_QuoteDescription()))
		{
			this.instrumentQuoteDescription.setText(this._instrument.get_QuoteDescription());
		}
		else
		{
			this.instrumentQuoteDescription.setVisible(false);
		}
		this.setTitle(Language.liquidationOrderFormTitle + "  " + this._instrument.get_DescriptionForTrading());


		this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());
		this.setPriceBidStaticText.setText(this._instrument.get_Bid());
		this.setPriceBidStaticText.setForeground(Color.red);
		this.setPriceAskStaticText.setText(this._instrument.get_Ask());
		this.setPriceAskStaticText.setForeground(PublicParametersManager.useGreenAsRiseColor ? new Color(0,192,0) : Color.BLUE);
		this.separatorStaticText.setText("/");
		this.submitButton.setText(Language.OrderLiquidationbtnSubmit);
		this.closeAllButton.setText(Language.CloseAll);
		this.multipleCloseButton.setText(Language.MultipleClose);
		this.exitButton.setText(Language.OrderLiquidationbtnExit);

		this._propertyChangingListener = new PropertyChangingListener(this);
		this._propertyChangedListener = new PropertyChangedListener(this);
		this._openCloseRelationSite = new OpenCloseRelationBaseSite(this);

		this._makeLiquidationOrder.initialize(this.liquidationOrderTable, true, false, this._propertyChangingListener, this._openCloseRelationSite, this._propertyChangedListener, true);
		this._makeLiquidationOrder.update();
		int column = this.liquidationOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
		this.liquidationOrderTable.sortColumn(column, true, false);
		this.liquidationOrderTable.setAutoResort(false);

		//this.fillAccount(order);
		//this.liquidationOrderTable.setSelectedRow(0);
		//int column = this._makeLiquidationOrder.get_BindingSourceForLiquidation().getColumn(MakeOrderLiquidationGridColKey.LiqLotString);
		//this.liquidationOrderTable.requestFocus();
		//this.liquidationOrderTable.setCurrentCell(0, column);

		this.setMakeOrderWindow();
		this.setNotifyIsAcceptWindow();

		this.updateButtonStatus();

		this.StartTimeout();
	}

	public void StartTimeout()
	{
		int timeout = this._settingsManager.get_Customer().get_DQOrderOutTime();
		if(timeout > 0 && this._timeoutEntry == null)
		{
			try
			{
				this._timeoutEntry = TradingConsoleServer.scheduler.add(this, null, TradingConsoleServer.appTime().add(TimeSpan.fromSeconds(timeout)));
			}
			catch (Exception ex)
			{
				TradingConsole.traceSource.trace(TraceType.Error, ex);
			}
		}
	}

	public void StopTimeout()
	{
		if(this._timeoutEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._timeoutEntry);
			this._timeoutEntry = null;
		}
	}

	public void setMakeOrderWindow()
	{
		MakeOrderWindow makeOrderWindow = new MakeOrderWindow(this._instrument.get_Id(), this, false);
		this._settingsManager.setMakeOrderWindow(this._instrument, makeOrderWindow);
	}

	private void removeMakeOrderWindow()
	{
		this._settingsManager.removeMakeOrderWindow(this._instrument, this);
	}

	private void setNotifyIsAcceptWindow()
	{
		this._settingsManager.setNotifyIsAcceptWindow(this._instrument, this);
	}

	public void dispose2()
	{
		if (this._openContractForm != null)
		{
			this._openContractForm.dispose();
		}

		this.dispose();
	}

	public void dispose()
	{
		this.StopTimeout();
		this._makeLiquidationOrder.unbindLiquidation();
		this.removeMakeOrderWindow();
		if(this._openContractForm != null)
		{
			this._openContractForm.setCanClose(true);
			this._openContractForm.enableLiquidationButton();
			this._openContractForm.toFront();
		}
		super.dispose();
	}

	public void getLastPrice()
	{
		//start price and refresh UI
		boolean prevIsUsing = MakeOrder.getLastQuotationIsUsing(this._instrument);
		MakeOrder.setLastQuotationIsUsing(this._instrument, false);
		this._instrument.update(Instrument.tradingPanelKey);
		this._instrument.updateLastQuotationForUnitGrid();
		//this._instrument.updateSummaryPanel(Instrument.summaryPanelKey);

		this.notifyMakeOrderUiByPrice();
		MakeOrder.setLastQuotationIsUsing(this._instrument, prevIsUsing);
	}

	public void resetData()
	{
		this.getLastPrice();
	}

	public void notifyMakeOrderUiByPrice()
	{
		//????????
		this.setPriceBidStaticText.setText( (!this._instrument.get_IsSinglePrice()) ? this._instrument.get_Bid() : this._instrument.get_Ask());
		this.setPriceAskStaticText.setText(this._instrument.get_Ask());
		this._makeLiquidationOrder.update();
	}

	static class PropertyChangedListener implements IPropertyChangedListener
	{
		private LiquidationOrderForm _owner;

		public PropertyChangedListener(LiquidationOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanged(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column)
		{
			if(propertyDescriptor.get_Name().equals(MakeOrderLiquidationGridColKey.IsSelected))
			{
				this._owner.updateButtonStatus();
			}
		}
	}

	private void updateButtonStatus()
	{
		BindingSource bindingSource = this.liquidationOrderTable.get_BindingSource();
		boolean hasSelectedOrder = false;
		boolean hasOppositeSelectedOrder = false;
		boolean isContractSizeDifferent = false;

		TradePolicyDetail tradePolicyDetail = null;
		Boolean lastBuy = null;
		BigDecimal lastContractSize = null;
		ArrayList<Account> involvedAccounts = new ArrayList<Account>();
		if(bindingSource != null)
		{
			for(int index = 0; index < bindingSource.getRowCount(); index++)
			{
				RelationOrder relationOrder = (RelationOrder)bindingSource.getObject(index);
				if(relationOrder.get_IsSelected())
				{
					if(tradePolicyDetail == null)
					{
						Guid tradePolictyId = relationOrder.get_OpenOrder().get_Account().get_TradePolicyId();
						tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolictyId, this._instrument.get_Id());
					}
					if(!involvedAccounts.contains(relationOrder.get_OpenOrder().get_Account()))
					{
						involvedAccounts.add(relationOrder.get_OpenOrder().get_Account());
					}

					hasSelectedOrder = true;
					if(lastBuy == null)
					{
						lastBuy = relationOrder.get_IsBuy();
					}
					else if(lastBuy.booleanValue() != relationOrder.get_IsBuy())
					{
						hasOppositeSelectedOrder = true;
					}

					if(lastContractSize == null)
					{
						lastContractSize = relationOrder.get_OpenOrder().get_Transaction().get_ContractSize();
					}
					else if(lastContractSize.compareTo(relationOrder.get_OpenOrder().get_Transaction().get_ContractSize()) != 0)
					{
						isContractSizeDifferent = true;
					}
				}
			}
		}
		this.submitButton.setEnabled(hasSelectedOrder);

		this.multipleCloseButton.setVisible(tradePolicyDetail != null && tradePolicyDetail.get_MultipleCloseAllowed());
		this.multipleCloseButton.setEnabled(!isContractSizeDifferent && hasOppositeSelectedOrder && involvedAccounts.size() == 1);
	}

	static class PropertyChangingListener implements IPropertyChangingListener
	{
		private LiquidationOrderForm _owner;

		public PropertyChangingListener(LiquidationOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			RelationOrder relationOrder = (RelationOrder)e.get_Owner();
			Order order = relationOrder.get_OpenOrder();
			this._owner.liquidationOrderTable.setSelectionForeground(BuySellColor.getColor(!order.get_IsBuy(), false));
			if (e.get_PropertyDescriptor().get_Name().equals(MakeOrderLiquidationGridColKey.LiqLotString))
			{
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.get_NewValue().toString());
				if (newValue.compareTo(BigDecimal.ZERO) == 0 && !StringHelper.isNullOrEmpty(e.get_NewValue().toString()))
				{
					String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
						{e.get_NewValue(), e.get_OldValue()});
					AlertDialogForm.showDialog(this._owner, null, true, info);
					e.set_Cancel(true);
					return;
				}
				Account account = order.get_Account();
				TradePolicyDetail tradePolicyDetail =
					account.get_TradingConsole().get_SettingsManager().getTradePolicyDetail(account.get_TradePolicyId(), order.get_Instrument().get_Id());
				newValue = AppToolkit.fixCloseLot(newValue, order.getAvailableLotBanlance(true, null), tradePolicyDetail, account);
				String formatLot = AppToolkit.getFormatLot(newValue, account, order.get_Instrument());
				e.set_NewValue(formatLot);
				relationOrder.set_LiqLotString(AppToolkit.getFormatLot(newValue, order.get_Account(), order.get_Instrument()));
				this._owner._makeLiquidationOrder.update();

				/*BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue, order.get_Account(), order.get_Instrument()));
				if (newValue2.compareTo(BigDecimal.ZERO) == 0 || newValue2.compareTo(newValue) != 0)
				{
					//e.set_Cancel(true);
					//return;
					if(newValue2.compareTo(BigDecimal.ZERO) != 0 || newValue.compareTo(BigDecimal.ZERO) > 0)
					{
						String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
							{newValue, newValue2});
						AlertDialogForm.showDialog(this._owner, null, true, info);
						e.set_NewValue(AppToolkit.getFormatLot(newValue2, order.get_Account(), order.get_Instrument()));
						newValue = newValue2;
					}
					else
					{
						e.set_Cancel(true);
						return;
					}
				}

				//new value can not > order.lotBalance
				/*if (newValue.compareTo(order.get_LotBalance()) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert0 +
											   AppToolkit.getFormatLot(order.get_LotBalance(), order.get_Transaction().get_Account()) + ")!");
					e.set_Cancel(true);
					return;
				}*/

				/*if (newValue.compareTo(order.getAvailableLotBanlance(true, null)) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert0);
					e.set_Cancel(true);
					return;
				}

				//new value can not > maxDQLot
				BigDecimal maxDQLot = this._owner._instrument.get_MaxDQLot();
				if (maxDQLot.compareTo(BigDecimal.ZERO) != 0 && newValue.compareTo(maxDQLot) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 +
											   AppToolkit.getFormatLot(this._owner._instrument.get_MaxDQLot(), order.get_Account(), order.get_Instrument()) + ")");
					e.set_Cancel(true);
					return;
				}

				//refresh Open Order
				//relationOrder.set_LiqLotString(e.getString());
				relationOrder.set_LiqLotString(AppToolkit.getFormatLot(newValue, order.get_Account(), order.get_Instrument()));
				this._owner._makeLiquidationOrder.update();*/
			}
		}
	}

	/*private void processTable(tradingConsole.ui.grid.ActionEvent e)
	{
		Table table = (Table) e.getSource();
		RelationOrder relationOrder = (RelationOrder) table.getRow(row).getObject();
		Order order = relationOrder.get_Order();
		liquidationOrderTable.setSelectionForeground(BuySellColor.getColor(!order.get_IsBuy()));
		//switch (e.getID())
		{
			if(e.get_ColumnName().equals(MakeOrderLiquidationGridColKey.LiqLotString))
			{
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.getString());
				if (newValue.compareTo(new BigDecimal("0")) == 0 && !StringHelper.isNullOrEmpty(e.getString()))
				{
					e.setCancel(true);
					return;
				}
				BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue, order.get_Transaction().get_Account()));
				if (newValue2.compareTo(new BigDecimal("0")) == 0 || newValue2.compareTo(newValue) != 0)
				{
					e.setCancel(true);
					return;
				}

				//new value can not > order.lotBalance
				if (newValue.compareTo(order.get_LotBalance()) > 0)
				{
					AlertDialogForm.showDialog(this, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert0 +
											   AppToolkit.getFormatLot(order.get_LotBalance(), order.get_Transaction().get_Account()) + ")!");
					e.setCancel(true);
					return;
				}

				//new value can not > maxDQLot
				BigDecimal maxDQLot = this._instrument.get_MaxDQLot();
				if (maxDQLot.compareTo(new BigDecimal("0")) != 0 && newValue.compareTo(maxDQLot) > 0)
				{
					AlertDialogForm.showDialog(this, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 +
											   AppToolkit.getFormatLot(this._instrument.get_MaxDQLot(), order.get_Transaction().get_Account()) + ")!");
					e.setCancel(true);
					return;
				}

				//refresh Open Order
				//relationOrder.set_LiqLotString(e.getString());
				relationOrder.set_LiqLotString(AppToolkit.getFormatLot(newValue, order.get_Transaction().get_Account()));
				this._makeLiquidationOrder.update();
			}
		}
	}*/

	private void adjustLiqLot(RelationOrder relationOrder)
	{
		BigDecimal liqLot = relationOrder.getAvailableCloseLot();
		if(liqLot.compareTo(relationOrder.get_LiqLot()) < 0)
		{
			relationOrder.set_LiqLot(liqLot);
		}
	}

	private void rebind()
	{
		if(this._openContractForm != null
		   && this._openContractForm.get_Order().getAvailableLotBanlance(true, null).compareTo(BigDecimal.ZERO) == 0)
		{
			AlertDialogForm.showDialog(this, null, true, Language.DisposedForOpenOrderClosed);
			this.dispose2();
		}
		else
		{
			ArrayList<RelationOrder> willRemoveRelatonOrders = new ArrayList<RelationOrder>();

			for(int row = 0; row < this.liquidationOrderTable.get_BindingSource().getRowCount(); row++)
			{
				RelationOrder relationOrder = (RelationOrder)this.liquidationOrderTable.get_BindingSource().getObject(row);
				this.adjustLiqLot(relationOrder);
				if(relationOrder.get_LiqLot().compareTo(BigDecimal.ZERO) == 0)
				{
					willRemoveRelatonOrders.add(relationOrder);
				}
			}

			for(RelationOrder relationOrder : willRemoveRelatonOrders)
			{
				this.liquidationOrderTable.get_BindingSource().remove(relationOrder);
			}

			this._makeLiquidationOrder.update();

			if(this.liquidationOrderTable.getRowCount() == 0)
			{
				AlertDialogForm.showDialog(this, null, true, Language.DisposedForOpenOrderClosed);
				this.dispose2();
			}
			else if(this._openContractForm != null)
			{
				this._openContractForm.rebind();
			}
		}
	}

	private void closeAll()
	{
		this._makeLiquidationOrder.closeAll();
		this.updateButtonStatus();
	}

	private void submit(OperateType orderType)
	{
		this.liquidationOrderTable.updateEditingValue();

		if(!this._makeLiquidationOrder.get_LastQuotationTimeStamp().equals(this._instrument.get_LastQuotation().get_Timestamp()))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderPlacementbuttonConfirm_OnclickAlert0);
			this.resetData();
			return;
		}

		if(!MakeOrder.isAllowOrderType(this._instrument, OrderType.SpotTrade))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			this.resetData();
		}
		else
		{
			Object[] result = this._makeLiquidationOrder.isValidOrder(true, orderType);
			if ( (Boolean)result[0])
			{
				if(orderType.value() == OperateType.MultipleClose.value())
				{
					this.StopTimeout();
				}

				String bidPrice = this.setPriceBidStaticText.getText();
				String askPrice = this.setPriceAskStaticText.getText();
				String logInfo = StringHelper.format("Last quotation in Instrument is {0}; Ask on UI = {1}, Bid on UI = {2}",
												 new Object[]{this._instrument.get_LastQuotation().toLogString(), bidPrice, askPrice});
				//spotOrderPriceContainer.LogInfo = logInfo;
				PlaceOrderInfo placeOrderInfo = new PlaceOrderInfo(this._instrument.get_LastQuotation(), bidPrice, askPrice);
				for(MakeOrderAccount makeOrderAccount : this._makeLiquidationOrder.get_MakeOrderAccounts().values())
				{
					makeOrderAccount.set_PlaceOrderInfo(placeOrderInfo);
				}

				VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
					this._settingsManager, this._instrument,
					this._makeLiquidationOrder.get_MakeOrderAccounts(), OrderType.SpotTrade, orderType, false, null, null);
				//verificationOrderForm.show();
				if(verificationOrderForm.isCanceled())
				{
					this.StartTimeout();
				}
				else
				{
					this.StopTimeout();
				}
				this.resetData();
			}
			else
			{
				AlertDialogForm.showDialog(this, null, true, result[1].toString());

				this.resetData();
			}
		}
	}

	/*public void paint(Graphics g)
	{
		super.paint(g);
		try
		{
			int x = this.setPriceBidStaticText.getLocation().x - 1;
			int y = this.setPriceBidStaticText.getLocation().y - 1;
			int width = this.setPriceBidStaticText.getSize().width + this.setPriceAskStaticText.getSize().width + this.separatorStaticText.getSize().width;
			int height = this.setPriceBidStaticText.getSize().height + 2;
			g.drawRect(x, y, width, height);
		}
		catch (Throwable exception)
		{}
	}*/

//SourceCode End//////////////////////////////////////////////////////////////////////////////////////////

	public LiquidationOrderForm(JFrame parent)
	{
		super(parent, false);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new LiquidationOrderUi_this_windowAdapter(this));

		this.setSize(450, 300);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.liquidationOrderFormTitle);
		this.setBackground(FormBackColor.liquidationOrderForm);

		Font font = new Font("SansSerif",Font.BOLD,14);
		setPriceBidStaticText.setAlignment(2);
		setPriceBidStaticText.setFont(font);
		setPriceBidStaticText.setText("1.2005");
		setPriceBidStaticText.setForeground(Color.red);
		setPriceAskStaticText.setText("1.2006");
		setPriceAskStaticText.setFont(font);
		setPriceAskStaticText.setForeground(Color.blue);
		setPriceAskStaticText.setAlignment(0);
		separatorStaticText.setText("/");
		separatorStaticText.setFont(font);
		instrumentQuoteDescription.setFont(font);
		submitButton.setText("Submit");
		submitButton.setTriangle(4);
		exitButton.setText("Exit");
		submitButton.addActionListener(new LiquidationOrderForm_submitButton_actionAdapter(this));
		closeAllButton.addActionListener(new LiquidationOrderForm_closeAllButton_actionAdapter(this));
		multipleCloseButton.addActionListener(new LiquidationOrderForm_multipulCloseButton_actionAdapter(this));
		exitButton.addActionListener(new LiquidationOrderForm_exitButton_actionAdapter(this));

		instrumentDescriptionStaticText.setFont(font);
		instrumentDescriptionStaticText.setText("GBP");
		liquidationOrderTable.addKeyListener(new LiquidationOrderForm_liquidationOrderTable_keyAdapter(this));
		//		this.add(instrumentDescriptionStaticText, new GridBagConstraints2(0, 0, 1, 1, 1.0, 0.0
//			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(25, 25, 2, 2), 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23)); 0, 23));

		this.getContentPane().add(instrumentDescriptionStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 12, 5, 5), 10, 0));

		this.getContentPane().add(setPriceBidStaticText, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 2, 2, 0), 30, 0));
		this.getContentPane().add(separatorStaticText, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 1, 2, 1), 0, 0));
		this.getContentPane().add(setPriceAskStaticText, new GridBagConstraints(4, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 2, 150), 30, 0));

		this.getContentPane().add(this.instrumentQuoteDescription, new GridBagConstraints(0, 4, 6, 1, 0.0, 0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 20));

		JScrollPane scrollPane = new JScrollPane(liquidationOrderTable);
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 5, 6, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 5, 10), 0, 0));

		this.getContentPane().add(submitButton, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 10, 10, 0), 20, 0));
		this.getContentPane().add(multipleCloseButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 1, 10, 0), 0, 0));
		this.getContentPane().add(closeAllButton, new GridBagConstraints(3, 7, 3, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 1, 10, 0), 20, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(5, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(2, 5, 10, 10), 20, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

//	PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	PVStaticText2 setPriceBidStaticText = new PVStaticText2();
	PVStaticText2 setPriceAskStaticText = new PVStaticText2();
	PVStaticText2 separatorStaticText = new PVStaticText2();
	//MultiTextArea instrumentNarrative = new MultiTextArea();
	NoneResizeableTextField instrumentQuoteDescription = new NoneResizeableTextField();
	DataGrid liquidationOrderTable = new DataGrid("LiquidationOrderTable");
	PVButton2 submitButton = new PVButton2();
	PVButton2 closeAllButton = new PVButton2();
	PVButton2 multipleCloseButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	private PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit(OperateType.DirectLiq);
	}

	public void closeAllButton_actionPerformed(ActionEvent e)
	{
		this.closeAll();
	}

	public void multipleCloseButton_actionPerformed(ActionEvent e)
	{
		this.submit(OperateType.MultipleClose);
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void liquidationOrderTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode()==e.VK_ENTER || e.getID()==e.VK_TAB)
		{
			this.liquidationOrderTable.requestFocus();
		}
	}

	public void action(SchedulerEntry schedulerEntry)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				dispose2();
			}
		});
	}

	class LiquidationOrderUi_this_windowAdapter extends WindowAdapter
	{
		private LiquidationOrderForm adaptee;
		LiquidationOrderUi_this_windowAdapter(LiquidationOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	private static class OpenCloseRelationBaseSite implements IOpenCloseRelationBaseSite
	{
		private LiquidationOrderForm _owner;
		private OpenCloseRelationBaseSite(LiquidationOrderForm owner)
		{
			this._owner = owner;
		}

		public OrderType getOrderType()
		{
			return OrderType.SpotTrade;
		}

		public Boolean isMakeLimitOrder()
		{
			return null;
		}

		public boolean isDelivery()
		{
			return false;
		}


		public void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
		}

		public void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
		}

		public JTextField getCloseLotEditor()
		{
			return null;
		}

		public JTextField getTotalLotEditor()
		{
			return null;
		}

		public boolean allowChangeCloseLot()
		{
			return false;
		}

		public JDialog getFrame()
		{
			return this._owner;
		}

		public Instrument getInstrument()
		{
			return this._owner._instrument;
		}

		public MakeOrderAccount getMakeOrderAccount()
		{
			return null;
		}

		public DataGrid getRelationDataGrid()
		{
			return this._owner.liquidationOrderTable;
		}

		public DataGrid getAccountDataGrid()
		{
			return null;
		}

		public void rebind()
		{
			this._owner.rebind();
		}
	}
}

class LiquidationOrderForm_exitButton_actionAdapter implements ActionListener
{
	private LiquidationOrderForm adaptee;
	LiquidationOrderForm_exitButton_actionAdapter(LiquidationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class LiquidationOrderForm_submitButton_actionAdapter implements ActionListener
{
	private LiquidationOrderForm adaptee;
	LiquidationOrderForm_submitButton_actionAdapter(LiquidationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class LiquidationOrderForm_closeAllButton_actionAdapter implements ActionListener
{
	private LiquidationOrderForm adaptee;
	LiquidationOrderForm_closeAllButton_actionAdapter(LiquidationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.closeAllButton_actionPerformed(e);
	}
}

class LiquidationOrderForm_multipulCloseButton_actionAdapter implements ActionListener
{
	private LiquidationOrderForm adaptee;
	LiquidationOrderForm_multipulCloseButton_actionAdapter(LiquidationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.multipleCloseButton_actionPerformed(e);
	}
}

class LiquidationOrderForm_liquidationOrderTable_keyAdapter extends KeyAdapter
{
	private LiquidationOrderForm adaptee;
	LiquidationOrderForm_liquidationOrderTable_keyAdapter(LiquidationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.liquidationOrderTable_keyPressed(e);
	}
}
