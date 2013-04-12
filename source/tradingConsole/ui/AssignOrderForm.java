package tradingConsole.ui;

import java.math.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;

import com.jidesoft.grid.*;
import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import framework.diagnostics.TraceType;

public class AssignOrderForm extends FrameBase
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeAssignOrder _makeAssignOrder;
	private MakeOrderAccount _makeOrderAccount; //record current makeOrderAccount
	private OpenContractForm _openContractForm;

	private Order _assigningOrder;
	private Instrument _instrument;
	private OpenCloseRelationSite _relationOrderSite;
	private RelationOrder.RelationSnapshot _relationSnapshot = new RelationOrder.RelationSnapshot();

	public AssignOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, MakeAssignOrder makeAssignOrder,OpenContractForm openContractForm)
	{
		this(tradingConsole.get_MainForm());

		this._openContractForm = openContractForm;

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._makeAssignOrder = makeAssignOrder;

		Transaction agentTransaction = this._makeAssignOrder.get_AgentTransaction();
		this._instrument = this._makeAssignOrder.get_Instrument();
		this._assigningOrder = null;
		for (Iterator<Order> iterator = agentTransaction.get_Orders().values().iterator(); iterator.hasNext(); )
		{
			this._assigningOrder = iterator.next();
			break;
		}
		if (this._assigningOrder == null)
		{
			return;
		}

		this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());
		this.orderCodeStaticText.setText(Language.OrderAssignlblOrderCodeA);
		this.orderCodeValueStaticText.setText(this._assigningOrder.get_Code());
		this.executeTimeStaticText.setText(Language.OrderAssignlblExecuteTimeA);
		this.executeTimeValueStaticText.setText(Convert.toString(agentTransaction.get_ExecuteTime().addMinutes(Parameter.timeZoneMinuteSpan),
			"yyyy-MM-dd HH:mm:ss"));
		this.isBuyStaticText.setText(Language.OrderAssignlblIsBuyA);
		this.isBuyValueStaticText.setText(this._assigningOrder.get_IsBuyString());
		this.executePriceStaticText.setText(Language.OrderAssignlblPriceA);
		this.executePriceValueStaticText.setText(this._assigningOrder.get_ExecutePriceString());
		this.lotBalanceStaticText.setText(Language.OrderAssignlblLotBalanceA);
		this.lotBalanceValueStaticText.setText(this._assigningOrder.get_LotBalanceString());
		this.totalLotStaticText.setText(Language.OrderAssignlblACTotalLotsA);
		this.totalLotValueStaticText.setText("");
		this.liqLotStaticText.setText(Language.OrderAssignlblLiqLotsA);
		this.liqLotValueStaticText.setText("");
		this.resetButton.setText(Language.OrderAssignbtnReset);
		this.submitButton.setText(Language.OrderAssignbtnSubmit);
		this.exitButton.setText(Language.OrderAssignbtnExit);

		//fill accountTable
		this._makeAssignOrder.initialize(this.accountTable, true, false, new PropertyChangingListener(this));
		this.accountTable.getTableHeader().setReorderingAllowed(false);
		this.accountTable.addSelectedRowChangedListener(new ISelectedRowChangedListener()
		{
			public void selectedRowChanged(DataGrid source)
			{
				if(accountTable.getSelectedRow() > -1)
				{
					MakeOrderAccount makeOrderAccount = (MakeOrderAccount)accountTable.getObject(accountTable.getSelectedRow());
					boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();
					accountTable.setSelectionForeground(BuySellColor.getColor(isBuy, false));
					if (! (_makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())
						   && _makeOrderAccount.get_IsBuyForCurrent() == isBuy))
					{
						_relationSnapshot.takeSnapshot(_relationOrderSite);
						_makeOrderAccount = makeOrderAccount;
						fillAccount(_makeOrderAccount.get_Account());
						_makeOrderAccount.initializeOutstanding(outstandingOrderTable, isBuy, false, BuySellType.Both,
							( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), _relationOrderSite);
						BigDecimal sumLiqLots = _makeOrderAccount.getSumLiqLots(isBuy);
						liqLotValueStaticText.setText(AppToolkit.getFormatLot(sumLiqLots, _makeOrderAccount.get_Account(), _makeOrderAccount.get_Instrument()));
						_relationSnapshot.applySnapshot(_relationOrderSite);
					}
				}
			}
		});

		//set default row = first row
		this._makeOrderAccount = (MakeOrderAccount)this.accountTable.get_BindingSource().getObject(0);

		//init outstanding Order Grid
		boolean isBuy = this._assigningOrder.get_IsBuy();
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both, ( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._relationOrderSite);
		//Fill first Account Infomation
		this.fillAccount(this._makeOrderAccount.get_Account());

		//this.refreshPrice();
		this.setBuySellEnabled(false);

		this.setMakeOrderWindow();
		submitButton.setEnabled(true);
		this.resetIfLotNotValid();
	}

	private void resetIfLotNotValid()
	{
		BigDecimal sumLots = this._makeAssignOrder.getSumLotsByCurrentBuySell();
		BigDecimal maxDQLot = this._instrument.get_MaxDQLot();
		if ((maxDQLot.compareTo(BigDecimal.ZERO) != 0 && sumLots.compareTo(maxDQLot) > 0)
			|| sumLots.compareTo(this._assigningOrder.get_LotBalance()) > 0)
		{
			this.reset();
		}
	}

	private void setBuySellEnabled(boolean isVisible)
	{
		String info = new FrameworkException("").getStackTrace();
		TradingConsole.traceSource.trace(TraceType.Information, info);
		this.submitButton.setEnabled(isVisible);
	}

	private void setMakeOrderWindow()
	{
		MakeOrderWindow makeOrderWindow = new MakeOrderWindow(this._instrument.get_Id(), this, false);
		this._settingsManager.setMakeOrderWindow(this._instrument, makeOrderWindow);
	}

	protected void removeMakeOrderWindow()
	{
		this._settingsManager.removeMakeOrderWindow(this._instrument, this);
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
		this._makeOrderAccount.unbindOutstanding();
		this.resetData();
		this.removeMakeOrderWindow();
		if(this._openContractForm != null)
		{
			this._openContractForm.enableAssignButton();
			this._openContractForm.setCanClose(true);
		}
		super.dispose();
	}

	public void resetData()
	{
	}

	private boolean isValidOrder(boolean isPrompt)
	{
		BigDecimal sumLots = this._makeAssignOrder.getSumLotsByCurrentBuySell();
		if (sumLots.compareTo(BigDecimal.ZERO)<=0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderValidAlert1);
			}
			return false;
		}

		int validAccountNumber = this.accountTable.getRowCount();
		for (int row = 0; row < this.accountTable.getRowCount(); row++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountTable.getObject(row);
			boolean isBuy = this._assigningOrder.get_IsBuy();
			BigDecimal liqLots = makeOrderAccount.getSumLiqLots(isBuy);
			BigDecimal lot = (this._assigningOrder.get_IsBuy()) ? makeOrderAccount.get_BuyLot() : makeOrderAccount.get_SellLot();
			if(lot.compareTo(BigDecimal.ZERO) <= 0)
			{
				validAccountNumber--;
				continue;
			}
			if(liqLots.compareTo(BigDecimal.ZERO) <= 0 && (!makeOrderAccount.get_Account().get_AllowAddNewPosition()
			   || !this._instrument.getAllowAddNewPosition(isBuy)))
			{
				makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
				makeOrderAccount.set_SellLot(BigDecimal.ZERO);
				makeOrderAccount.update(this.accountTable.get_DataSourceKey());
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, makeOrderAccount.get_Account().get_Code() + ": " +  Language.NewPositionIsNotAllowed);
				}
				validAccountNumber--;
			}
		}

		if (validAccountNumber <= 0 && isPrompt)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderValidAlert1);
		}

		return validAccountNumber > 0;
	}

	private void submit()
	{
		this._stopSubmit = false;
		this.outstandingOrderTable.updateEditingValue();
		this.accountTable.updateEditingValue();
		if(!this._stopSubmit)
		{
			if (this.isValidOrder(true))
			{
				VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
					this._settingsManager,
					this._instrument,
					this._makeAssignOrder.get_MakeOrderAccounts(), OrderType.SpotTrade, OperateType.Assign, false, this._assigningOrder, null);
			}
			else
			{
				this.resetData();
				return;
			}
		}
	}

	private void fillAccount(Account account)
	{/*
		boolean isNoShowAccountStatus = this._settingsManager.get_Customer().get_IsNoShowAccountStatus();
		if (isNoShowAccountStatus)
		{
			null.setText("-");
			null.setText("-");
		}
		else
		{
			short accountCurrencyDecimals = account.get_Currency().get_Decimals();
			null.setForeground(NumericColor.getColor(account.get_Balance()));
			null.setText(Functions.format(account.get_Balance(), accountCurrencyDecimals));
			null.setForeground(NumericColor.getColor(account.get_Equity()));
			null.setText(Functions.format(account.get_Equity(), accountCurrencyDecimals));
		}
	*/
	}

   private boolean _supressLotChecking = false;
	private void reset()
	{
		submitButton.setEnabled(false);
		this._supressLotChecking = true;
		try
		{
			this.accountTable.updateEditingValue();
			this.outstandingOrderTable.updateEditingValue();
			for (int row = 0; row < this.accountTable.getRowCount(); row++)
			{
				MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountTable.getObject(row);
				makeOrderAccount.reset(true, false);
				this.accountTable.setValueAt("0", row, 2);
				makeOrderAccount.update(this.accountTable.get_DataSourceKey());

				this._relationSnapshot.clearSnapshot(makeOrderAccount);
			}
		}
		finally
		{
			this._supressLotChecking = false;
		}
	}

	private boolean _stopSubmit = false;
	static class PropertyChangingListener implements IPropertyChangingListener
	{
		private AssignOrderForm _owner;
		public PropertyChangingListener(AssignOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			this._owner._stopSubmit = false;

			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)e.get_Owner();
			PropertyDescriptor property = e.get_PropertyDescriptor();
			if (property.get_Name().equals(MakeOrderAccountGridColKey.LotString))
			{
				if(this._owner._supressLotChecking) return;

				BigDecimal oldValue = AppToolkit.convertStringToBigDecimal(e.get_OldValue().toString());
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.get_NewValue().toString());
				if (newValue.compareTo(BigDecimal.ZERO) < 0 && !StringHelper.isNullOrEmpty(e.get_NewValue().toString()))
				{
					e.set_Cancel(true);
					this._owner._stopSubmit = true;
					return;
				}

				//???
				BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue, makeOrderAccount.get_Account(), makeOrderAccount.get_Instrument()));
				if (newValue2.compareTo(BigDecimal.ZERO) < 0 || newValue2.compareTo(newValue) != 0)
				{
					e.set_Cancel(true);
					this._owner._stopSubmit = true;
					return;
				}

				BigDecimal sumLots = this._owner._makeAssignOrder.getSumLotsByCurrentBuySell();
				sumLots = sumLots.subtract(oldValue).add(newValue);
				BigDecimal maxDQLot = this._owner._instrument.get_MaxDQLot();
				if (maxDQLot.compareTo(BigDecimal.ZERO) != 0 && newValue.compareTo(maxDQLot) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert0
											   + "(" + AppToolkit.getFormatLot(maxDQLot, this._owner._makeOrderAccount.get_Account(), this._owner._instrument) + ")!");
					e.set_Cancel(true);
					this._owner._stopSubmit = true;
					return;
				}
				if (sumLots.compareTo(this._owner._assigningOrder.get_LotBalance()) > 0)
				{
					String info = "(" + AppToolkit.getFormatLot(this._owner._assigningOrder.get_LotBalance(), this._owner._makeOrderAccount.get_Account(), this._owner._makeOrderAccount.get_Instrument()) + ")!";
					AlertDialogForm.showDialog(this._owner, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert2  + info);
					e.set_Cancel(true);
					this._owner._stopSubmit = true;
					return;
				}

				//input lot < sumLiqLots, clear liqLots....
				boolean isBuy = this._owner._makeOrderAccount.get_IsBuyForCurrent();
				BigDecimal sumLiqLots = this._owner._makeOrderAccount.getSumLiqLots(isBuy);
				if (newValue.compareTo(sumLiqLots) < 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert3);
					this._owner._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, true, false);
					this._owner.liqLotValueStaticText.setText("");
					//e.set_Cancel(true);
					//this.doLayout();
					this._owner._stopSubmit = true;
					return;
				}

				//for Instrument code with "#"// & isOpen=true
				//if (newValue > sumLiqLots)
				{
					//BigDecimal openLot = new BigDecimal( ( (Double) (newValue - sumLiqLots)).doubleValue());
					boolean isHasMakeNewOrder = newValue.compareTo(sumLiqLots) > 0;
					boolean isAcceptLot = this._owner._makeOrderAccount.isAcceptLot(isBuy, newValue, isHasMakeNewOrder); //openLot);
					if (!isAcceptLot)
					{
						AlertDialogForm.showDialog(this._owner, null, true, Language.NewOrderAcceptedHedging);
						e.set_Cancel(true);
						this._owner._stopSubmit = true;
						return;
					}
				}

				this._owner.totalLotValueStaticText.setText(AppToolkit.getFormatLot(sumLots, true));
//				this._owner.setBuySellEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
			}
		}
	}

	public  void updateAccount(BigDecimal accountLot,boolean openOrderIsBuy)
	{
		//boolean isBuy = this._assigningOrder.get_IsBuy();
		//this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both, ( (!isBuy) ? BuySellType.Buy : BuySellType.Sell));
	}

	//Input liqLot validation
	/*private void processOutstandingOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{
		super.processOutstandingOrderTable(e,OperateType.Assign, this._instrument, this._makeOrderAccount,
										   this._makeOrderAccount.get_LotCurrent(),
										   this.liqLotValueStaticText,BigDecimal.ZERO);
	}*/

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private AssignOrderForm _owner;
		public OpenCloseRelationSite(AssignOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return OperateType.Assign;
		}

		public Instrument getInstrument()
		{
			return this._owner._instrument;
		}

		public MakeOrderAccount getMakeOrderAccount()
		{
			return this._owner._makeOrderAccount;
		}

		public BigDecimal getLot()
		{
			this._owner.accountTable.updateEditingValue();
			return this._owner._makeOrderAccount.get_LotCurrent();
		}

		public PVStaticText2 getLiqLotValueStaticText()
		{
			return this._owner.liqLotValueStaticText;
		}

		public BigDecimal getTotalQuantity()
		{
			return BigDecimal.ZERO;
		}

		public DataGrid getRelationDataGrid()
		{
			return this._owner.outstandingOrderTable;
		}

		public JDialog getFrame()
		{
			return this._owner;
		}

		public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
		{
			this._owner.updateAccount(accountLot, openOrderIsBuy);
		}

		public void updateTotalColseLot(BigDecimal totalCloseLot)
		{
		}

		public OrderType getOrderType()
		{
			return OrderType.SpotTrade;
		}

		public Boolean isMakeLimitOrder()
		{
			return null;
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
			return true;
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

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	public AssignOrderForm(JFrame parent)
	{
		super(parent);
		try
		{
			jbInit();
			this._relationOrderSite = new OpenCloseRelationSite(this);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	/*public void paint(Graphics g)
	{
		super.paint(g);
		try
		{
			int x = this.orderCodeStaticText.getLocation().x - 1;
			int y = this.orderCodeStaticText.getLocation().y - 1;
			int width = this.orderCodeStaticText.getSize().width
				+ this.executeTimeStaticText.getSize().width
				+ this.isBuyStaticText.getSize().width
				+ this.executePriceStaticText.getSize().width
				+ this.lotBalanceStaticText.getSize().width;
			int height = this.orderCodeStaticText.getSize().height
				+ this.orderCodeValueStaticText.getSize().height
				+ this.totalLotStaticText.getSize().height
				+ 4;
			g.drawRect(x, y, width, height);
		}
		catch (Throwable exception)
		{}
	}*/

	private void jbInit() throws Exception
	{
		this.addWindowListener(new AssignOrderUi_this_windowAdapter(this));

		this.setSize(600, 350);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.assignOrderFormTitle);
		this.setBackground(FormBackColor.assignOrderForm);

		Font font = new Font("SansSerif", Font.BOLD, 14);
		instrumentDescriptionStaticText.setAlignment(0);
		instrumentDescriptionStaticText.setForeground(Color.blue);
		instrumentDescriptionStaticText.setText("GBP");
		instrumentDescriptionStaticText.setFont(font);
		orderCodeStaticText.setText("Order Code");
		orderCodeValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		orderCodeValueStaticText.setText("OR22005120800001");
		executeTimeStaticText.setText("Execute Time");
		executeTimeValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		executeTimeValueStaticText.setText("2005/12/08 15:10:10");
		isBuyStaticText.setText("B/S");
		isBuyValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		isBuyValueStaticText.setText("S");
		executePriceStaticText.setText("Price");
		executePriceValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		executePriceValueStaticText.setText("1.2500");
		lotBalanceStaticText.setText("Lot Bal.");
		lotBalanceValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		lotBalanceValueStaticText.setText("2");
		totalLotStaticText.setText("Total Lot");
		totalLotValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		totalLotValueStaticText.setText("0");
		liqLotStaticText.setText("Close");
		liqLotStaticText.setVisible(false);
		liqLotValueStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		liqLotValueStaticText.setText("0");
		liqLotValueStaticText.setVisible(false);
		resetButton.setText("Reset");
		resetButton.addActionListener(new AssignOrderForm_resetButton_actionAdapter(this));
		submitButton.setText("Submit");
		submitButton.addActionListener(new AssignOrderForm_submitButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new AssignOrderUi_exitButton_actionAdapter(this));
		this.accountTable.addCellEditorListener(new JideCellEditorAdapter()
		{
			public void editingStarted(ChangeEvent changeEvent)
			{
				String info = new FrameworkException("").getStackTrace();
				TradingConsole.traceSource.trace(TraceType.Information, info);
				submitButton.setEnabled(true);
				super.editingStarted(changeEvent);
			}
		});

		//accountTable.addActionListener(new AssignOrderForm_accountTable_actionAdapter(this));
		//outstandingOrderTable.addActionListener(new AssignOrderForm_outstandingOrderTable_actionAdapter(this));
		//accountTable.addKeyListener(new AssignOrderForm_accountTable_keyAdapter(this));
		//outstandingOrderTable.addKeyListener(new AssignOrderForm_outstandingOrderTable_keyAdapter(this));
		this.add(instrumentDescriptionStaticText, new GridBagConstraints2(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.HORIZONTAL, new Insets(10, 10, 2, 10), 0, 0));
		this.add(executePriceStaticText, new GridBagConstraints2(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(executePriceValueStaticText,new GridBagConstraints2(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		this.add(liqLotValueStaticText,	new GridBagConstraints2(3, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(liqLotStaticText, new GridBagConstraints2(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(isBuyValueStaticText, new GridBagConstraints2(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(isBuyStaticText, new GridBagConstraints2(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(executeTimeStaticText, new GridBagConstraints2(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(executeTimeValueStaticText, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(totalLotValueStaticText, new GridBagConstraints2(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		this.add(orderCodeValueStaticText,	new GridBagConstraints2(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 10, 2, 2), 0, 0));
		this.add(orderCodeStaticText, new GridBagConstraints2(0, 1,	1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 10, 2, 2), 0, 0));
		this.add(totalLotStaticText, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 10, 2, 2), 0, 0));
		this.add(lotBalanceValueStaticText, new GridBagConstraints2(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 10), 0, 0));
		this.add(lotBalanceStaticText, new GridBagConstraints2(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 10), 0, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 2, 10, 5), 20, 0));
		this.getContentPane().add(resetButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 2, 10), 20, 0));
		this.getContentPane().add(submitButton, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 2, 10), 20, 0));

		JScrollPane scrollPane = new JScrollPane(outstandingOrderTable);
		this.getContentPane().add(scrollPane, new GridBagConstraints(2, 4, 3, 1, 0.5, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 1, 5, 5), 0, 0));

		JScrollPane scrollPane2 = new JScrollPane(accountTable);
		this.getContentPane().add(scrollPane2, new GridBagConstraints(0, 4, 2, 1, 0.5, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 1), 0, 0));
		this.accountTable.getTableHeader().setReorderingAllowed(false);
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	PVStaticText2 orderCodeStaticText = new PVStaticText2();
	PVStaticText2 orderCodeValueStaticText = new PVStaticText2();
	PVStaticText2 executeTimeStaticText = new PVStaticText2();
	PVStaticText2 executeTimeValueStaticText = new PVStaticText2();
	PVStaticText2 isBuyStaticText = new PVStaticText2();
	PVStaticText2 isBuyValueStaticText = new PVStaticText2();
	PVStaticText2 executePriceStaticText = new PVStaticText2();
	PVStaticText2 executePriceValueStaticText = new PVStaticText2();
	PVStaticText2 lotBalanceStaticText = new PVStaticText2();
	PVStaticText2 lotBalanceValueStaticText = new PVStaticText2();
	PVStaticText2 totalLotStaticText = new PVStaticText2();
	PVStaticText2 totalLotValueStaticText = new PVStaticText2();
	PVStaticText2 liqLotStaticText = new PVStaticText2();
	PVStaticText2 liqLotValueStaticText = new PVStaticText2();
	DataGrid accountTable = new DataGrid("AccountTable");
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderTable");
	PVButton2 resetButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	private void rebind()
	{
		if(this._makeOrderAccount != null)
		{
			boolean isBuy = this._makeOrderAccount.get_IsBuyForCurrent();
			this._makeOrderAccount.initializeOutstanding(outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), _relationOrderSite);
		}
	}

	/*public void accountTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processAccountTable(e);
	}*/

	/*public void outstandingOrderTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processOutstandingOrderTable(e);
	}*/

	/*public void accountTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode()==e.VK_ENTER || e.getID()==e.VK_TAB)
		{
			this.accountTable.requestFocus();
		}
	}

	public void outstandingOrderTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode()==e.VK_ENTER || e.getID()==e.VK_TAB)
		{
			this.outstandingOrderTable.requestFocus();
		}
	}*/

	class AssignOrderUi_this_windowAdapter extends WindowAdapter
	{
		private AssignOrderForm adaptee;
		AssignOrderUi_this_windowAdapter(AssignOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

/*class AssignOrderForm_outstandingOrderTable_actionAdapter implements IActionListener
{
	private AssignOrderForm adaptee;
	AssignOrderForm_outstandingOrderTable_actionAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.outstandingOrderTable_actionPerformed(e);
	}
}*/

/*class AssignOrderForm_accountTable_actionAdapter implements IActionListener
{
	private AssignOrderForm adaptee;
	AssignOrderForm_accountTable_actionAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.accountTable_actionPerformed(e);
	}
}*/

/*class AssignOrderForm_outstandingOrderTable_keyAdapter extends KeyAdapter
{
	private AssignOrderForm adaptee;
	AssignOrderForm_outstandingOrderTable_keyAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.outstandingOrderTable_keyPressed(e);
	}
}

class AssignOrderForm_accountTable_keyAdapter extends KeyAdapter
{
	private AssignOrderForm adaptee;
	AssignOrderForm_accountTable_keyAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.accountTable_keyPressed(e);
	}
}*/

class AssignOrderForm_resetButton_actionAdapter implements ActionListener
{
	private AssignOrderForm adaptee;
	AssignOrderForm_resetButton_actionAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class AssignOrderForm_submitButton_actionAdapter implements ActionListener
{
	private AssignOrderForm adaptee;
	AssignOrderForm_submitButton_actionAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class AssignOrderUi_exitButton_actionAdapter implements ActionListener
{
	private AssignOrderForm adaptee;
	AssignOrderUi_exitButton_actionAdapter(AssignOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
