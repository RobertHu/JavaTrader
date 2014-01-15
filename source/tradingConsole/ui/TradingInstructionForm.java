package tradingConsole.ui;

import java.math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.*;

import com.jidesoft.grid.*;
import framework.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.common.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import com.jidesoft.swing.JideSwingUtilities;
import framework.diagnostics.TraceType;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Locale;
import tradingConsole.service.PlaceResult;
import java.util.HashMap;
import tradingConsole.enumDefine.physical.InstalmentFrequence;
import Packet.SignalObject;
import Util.RequestCommandHelper;

public class TradingInstructionForm extends JDialog implements IPriceSpinnerSite
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Order _order;
	private Order _ifOrder;
	private JPanel _modifySetPricePanel;
	private Instrument _instrument;

	public TradingInstructionForm(TradingConsole tradingConsole, SettingsManager settingsManager, Order order)
	{
		super(tradingConsole.get_MainForm(), true);
		try
		{
			this.newPriceEdit = new PriceSpinner(this);
			this.newPriceEdit2 = new PriceSpinner(this);
			this._order = order;
			this._instrument = order.get_Instrument();
			this._tradingConsole = tradingConsole;
			this._settingsManager = settingsManager;

			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		if(order.get_Transaction().get_SubType() == TransactionSubType.IfDone)
		{
			Guid ifOrderId = this._order.get_Transaction().get_AssigningOrderId();
			this._ifOrder = null;
			if (ifOrderId != null)
				this._ifOrder = this._tradingConsole.getOrder(ifOrderId);
		}

		this.setTitle(order.get_PhaseString() + " " + Language.InstructionPrompt);
		this.orderCodeStaticText.setText(StringHelper.isNullOrEmpty(order.get_Code()) ? order.get_Transaction().get_Code() : order.get_Code());
		//this.orderCodeStaticText.setForeground(Phase.getColor(order.get_Phase()));

		this.remarksStaticText.setText(Language.UnconfirmedInstructionlblMessageA);
		this.remarksEdit.setMessage(order.get_Message());

		this.cancelButton.setText(Language.UnconfirmedInstructionbtnCancellation);
		this.clearButton.setText(Language.UnconfirmedInstructionbtnClearFromList);
		this.modifyButton.setText(Language.UnconfirmedInstructionbtnModify);
		this.submitButton.setText(Language.Submit);
		this.exitButton.setText(Language.UnconfirmedInstructionbtnExit);

		Phase phase = order.get_Phase();

		this.clearButton.setEnabled(this._tradingConsole.get_LoginInformation().getIsConnected()
									&& (phase == Phase.Cancelled
										|| phase == Phase.Executed
										|| phase == Phase.Completed
										|| phase == Phase.Deleted));

		OrderType orderType = order.get_Transaction().get_OrderType();
		this.modifyButton.setVisible(this._tradingConsole.get_LoginInformation().getIsConnected()
									 && (order.get_Transaction().get_Instrument().get_OrderTypeMask() & OrderTypeMask.Limit.value()) == OrderTypeMask.Limit.value()
									 && order.get_Transaction().get_Instrument().isFromBursa()
									 && order.get_OrderModification() == null
									 && (order.get_RelationOrders() == null || order.get_RelationOrders().size() == 0
										 || orderType == OrderType.Limit || orderType == OrderType.Stop || orderType == OrderType.StopLimit)
									 && (phase == Phase.Placed || phase == Phase.Placing));
		if(this.modifyButton.isVisible()) this.modifyButton.setEnabled(!order.get_Transaction().isInModification());
		this.initializeOcoButton();
		this.updateModifySetPriceStatus();

		Object[] result = Transaction.isAllowCancelLMTOrder(this._settingsManager, order);
		this.cancelButton.setEnabled( (Boolean)result[0]);
		if(StringHelper.isNullOrEmpty(order.get_Message()) && !(Boolean)result[0]) this.remarksEdit.setMessage(result[1] == null ? "" : result[1].toString());
		this.fillTradingInstructionTable(this.tradingInstructionTable);
	}

	private void updateModifySetPriceStatus()
	{
		Guid tradePolicyId = this._order.get_Account().get_TradePolicyId();
		Guid instrumentId = this._order.get_Instrument().get_Id();
		TradePolicyDetail tradePolicyDetail
			= this._settingsManager.getTradePolicyDetail(tradePolicyId, instrumentId);
		boolean canModifySetPrice = false;
		if(tradePolicyDetail != null)
		{
			canModifySetPrice = (this._order.get_Phase() == Phase.Placed /*|| this._order.get_Phase() == Phase.Placing*/)
				&& !this._tradingConsole.get_SettingsManager().get_Customer().get_DisallowTrade()
				&& this._order.get_Account().getIsAllowTrade()
				&& MakeOrder.isAllowOrderType(this._order.get_Transaction().get_Instrument(), OrderType.Limit)
				&& !this._order.get_Instrument().isFromBursa()
				&& tradePolicyDetail.get_ChangePlacedOrderAllowed()
				&&(this._order.get_Transaction().get_OrderType() == OrderType.Limit
				|| this._order.get_Transaction().get_OrderType() == OrderType.Stop);
		}

		if(canModifySetPrice)
		{
			this.remarksStaticText.setVisible(false);
			this.remarksEdit.setVisible(false);
		}
		else
		{
			this.submitButton.setVisible(false);
			this._modifySetPricePanel.setVisible(false);
		}
	}

	private void initializeOcoButton()
	{
		OrderType orderType = this._order.get_Transaction().get_OrderType();
		TransactionType transactionType = this._order.get_Transaction().get_Type();
		Phase phase = this._order.get_Phase();
		this.ocoButton.setVisible(false);

		Guid tradePolicyId = this._order.get_Account().get_TradePolicyId();
		Guid instrumentId = this._instrument.get_Id();
		TradePolicyDetail tradePolicyDetail
			= this._tradingConsole.get_SettingsManager().getTradePolicyDetail(tradePolicyId, instrumentId);
		boolean canChangeToOCO = this._order.get_IsOpen() ? tradePolicyDetail.get_AllowNewOCO() : MakeOrder.isAllowOrderType(this._instrument, OrderType.OneCancelOther);
		boolean canChangeToIfDone = this._order.get_IsOpen() && tradePolicyDetail.get_AllowIfDone();
		if((!canChangeToOCO && transactionType == TransactionType.OneCancelOther)
		   || (!tradePolicyDetail.get_AllowIfDone() && this._order.get_Transaction().get_SubType() == TransactionSubType.IfDone))
		{
			this.ocoButton.setVisible(false);
		}
		else
		{
			boolean allowLimit = MakeOrder.isAllowOrderType(this._order.get_Transaction().get_Instrument(), OrderType.Limit);

			Instrument instrument = this._order.get_Transaction().get_Instrument();
			Price bid = instrument.get_LastQuotation().get_Bid();
			Price ask = instrument.get_LastQuotation().get_Ask();
			boolean isPriceAvaiable = bid != null && ask != null;

			if (isPriceAvaiable && (phase == Phase.Placed)
				&& !this._tradingConsole.get_SettingsManager().get_Customer().get_DisallowTrade()
				&& orderType == OrderType.Limit && (canChangeToOCO || canChangeToIfDone || allowLimit)
				&& this._order.get_Account().getIsAllowTrade() && allowLimit
				&& tradePolicyDetail.get_ChangePlacedOrderAllowed())
			{
				this.ocoButton.setVisible(true);
				this.ocoButton.setText(Language.ModifyCaption);
			}

			if (this.ocoButton.isVisible())
			{
				OCOButtonActionListener actionListener = new OCOButtonActionListener(this);
				this.ocoButton.addActionListener(actionListener);
			}
		}
	}

	private static class OCOButtonActionListener implements ActionListener
	{
		private TradingInstructionForm _owner;

		public OCOButtonActionListener(TradingInstructionForm owner)
		{
			this._owner = owner;
		}

		public void actionPerformed(ActionEvent e)
		{
			ChangeToOcoOrderForm changeToOcoOrderForm = new ChangeToOcoOrderForm(this._owner, this._owner._tradingConsole, this._owner._settingsManager, this._owner._order);
			JideSwingUtilities.centerWindow(changeToOcoOrderForm);
			changeToOcoOrderForm.toFront();
			changeToOcoOrderForm.show();
			if(!changeToOcoOrderForm.get_IsCanceled())
			{
				this._owner.dispose();
			}
		}
	}

	private void fillTradingInstructionTable(DataGrid grid)
	{
		grid.get_HeaderRenderer().setHeight(0);
		grid.setModel(this.getTableModel());
		grid.enableRowStripe();
		grid.setShowVerticalLines(false);
		grid.setShowHorizontalLines(false);
		grid.setRowSelectionAllowed(false);
		grid.setColumnSelectionAllowed(false);
		grid.setFont(DataGrid.DefaultFont);
	}

	private TableModel getTableModel()
	{
		String[] columns = new String[]{"Name", "Value"};
		int rowCount = this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther ? 18 : 17;
		if(this._order.get_InstalmentPolicyId() != null)
		{
			if(this._order.get_Period().get_Frequence().equals(InstalmentFrequence.TillPayoff))
			{
				rowCount += 1;
			}
			else
			{
				rowCount += 4;
			}
		}
		String[][] data = new String[rowCount][2];

		int row = 0;
		data[row][0] = Language.UnconfirmedInstructionlblAccountCodeA;
		data[row][1] = this._order.get_AccountCode();
		row++;

		data[row][0] = Language.Instrument;
		data[row][1] = this._order.get_InstrumentCode();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblIsBuyA;
		data[row][1] = this._order.get_IsBuyString();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblQuantityA;
		data[row][1] = this._order.get_LotString();
		row++;

		if(this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther
			&& this._order.get_Transaction().get_Orders().size() == 2)
		{
			Order[] orders = new Order[2];
			this._order.get_Transaction().get_Orders().values().toArray(orders);

			data[row][0] = Language.UnconfirmedInstructionlblExecutePriceA + "(" + orders[0].get_TradeOptionString() + ")";
			data[row][1] = orders[0].get_SetPriceString();
			row++;

			data[row][0] = Language.UnconfirmedInstructionlblExecutePriceA + "(" + orders[1].get_TradeOptionString() + ")";
			data[row][1] = orders[1].get_SetPriceString();
			row++;
		}
		else
		{
			data[row][0] = Language.UnconfirmedInstructionlblExecutePriceA;
			if (this._order.get_Transaction().get_OrderType() == OrderType.StopLimit)
			{
				data[row][1] = this._order.get_SetPriceString() + "/" + this._order.get_SetPrice2String();
			}
			else
			{
				data[row][1] = this._order.get_SetPriceString();
			}
			row++;
		}

		data[row][0] = Language.executePriceCaption;
		data[row][1] = this._order.get_ExecutePriceString();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblOrderTypeA;
		data[row][1] = this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther ? Language.OCOPrompt : this._order.get_OrderTypeString();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblTradeOptionA;
		data[row][1] = /*this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther ? "" :*/ this._order.get_TradeOptionString();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblSubmitTimeA;
		if(this._order.get_Transaction().get_OrderType() == OrderType.Risk)
		{
			data[row][1] = this._settingsManager.get_TradeDay().getTradeDayOf(this._order.get_Transaction().get_SubmitTime()).toString();
		}
		else
		{
			data[row][1] = XmlConvert.toString(this._order.get_Transaction().get_SubmitTime().addMinutes(Parameter.timeZoneMinuteSpan),
												 "yyyy-MM-dd HH:mm:ss");
		}
		row++;

		if (this._order.get_Phase() == Phase.Executed || this._order.get_Phase() == Phase.Completed)
		{
			data[row][0] = Language.UnconfirmedInstructionlblEndTimeA2;
			if(this._order.get_Transaction().get_OrderType() == OrderType.Risk)
			{
				data[row][1] = this._order.get_ExecuteTradeDay();
			}
			else
			{
				data[row][1] = XmlConvert.toString(this._order.get_Transaction().get_ExecuteTime().addMinutes(Parameter.timeZoneMinuteSpan),
												 "yyyy-MM-dd HH:mm:ss");
			}
		}
		else
		{
			data[row][0] = Language.UnconfirmedInstructionlblEndTimeA;
			if(this._order.get_Transaction().get_OrderType() == OrderType.Risk)
			{
				data[row][1] = this._settingsManager.get_TradeDay().getTradeDayOf(this._order.get_Transaction().get_EndTime()).toString();
			}
			else
			{
				data[row][1] = XmlConvert.toString(this._order.get_Transaction().get_EndTime().addMinutes(Parameter.timeZoneMinuteSpan),
												 "yyyy-MM-dd HH:mm:ss");
			}
		}
		row++;

		String currencyCode;
		short decimals;
		if (this._order.get_Transaction().get_Account().get_IsMultiCurrency())
		{
			currencyCode = this._order.get_Transaction().get_Instrument().get_Currency().get_Code();
			decimals = this._order.get_Transaction().get_Instrument().get_Currency().get_Decimals();
		}
		else
		{
			currencyCode = this._order.get_Transaction().get_Account().get_Currency().get_Code();
			decimals = this._order.get_Transaction().get_Account().get_Currency().get_Decimals();
		}

		data[row][0] = Language.OpenContractlblCurrencyA;
		data[row][1] = currencyCode;
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblCommissionSumA;
		data[row][1] = this._order.get_CommissionSumString();
		row++;

		data[row][0] = Language.UnconfirmedInstructionlblLevySumA;
		data[row][1] = this._order.get_LevySumString();
		row++;

		double trade = this._order.get_PLTradingItem().get_Trade();
		data[row][0] = Language.UnconfirmedInstructionlblTradePLA;
		data[row][1] = AppToolkit.format(trade, decimals);
		row++;

		double interest = this._order.get_PLTradingItem().get_Interest();
		data[row][0] = Language.UnconfirmedInstructionlblInterestPLA;
		data[row][1] = AppToolkit.format(interest, decimals);
		row++;

		double storage = this._order.get_PLTradingItem().get_Storage();
		data[row][0] = Language.UnconfirmedInstructionlblStoragePLA;
		data[row][1] = AppToolkit.format(storage, decimals);
		row++;

		/*String assignOrderCode = (this._order.get_Transaction().get_AssigningOrder() == null) ? "" :
			this._order.get_Transaction().get_AssigningOrder().get_Code();
		if (!StringHelper.isNullOrEmpty(assignOrderCode))
		{
			data[row][0] = Language.AssignOrderCode;
			data[row][1] = assignOrderCode;
			row++;
		}*/

		data[row][0] = Language.UnconfirmedInstructionlblPeerOrderCodesA;
		data[row][1] = this._order.getRelationString();
		row++;

		if(this._order.get_InstalmentPolicyId() != null)
		{
			if(this._order.get_Period().get_Frequence().equals(InstalmentFrequence.TillPayoff))
			{
				data[row][0] = InstalmentLanguage.PaymentMode;
				data[row][1] = InstalmentLanguage.AdvancePayment;
				row++;
			}
			else
			{
				data[row][0] = InstalmentLanguage.InstalmentType;
				data[row][1] = this._order.get_PhysicalInstalmentType().toLocalString();
				row++;

				data[row][0] = InstalmentLanguage.Period;
				data[row][1] = this._order.get_Period().toString();
				row++;

				data[row][0] = InstalmentLanguage.DownPayment;
				data[row][1] = AppToolkit.format(this._order.get_DownPayment(), 2);
				row++;

				data[row][0] = InstalmentLanguage.RecalculateRateType;
				data[row][1] = this._order.get_RecalculateRateType().toLocalString();
				row++;
			}
		}

		DefaultStyleTableModel tableModel = new DefaultStyleTableModel(data, columns);
		tableModel.setCellStyleOn(true);

		CellStyle cellStyle = new CellStyle();
		cellStyle.setForeground(Color.BLACK);
		for(int row2 = 0; row2 < rowCount; row2++)
		{
			tableModel.setCellStyle(row2, 1, cellStyle);
		}

		cellStyle = new CellStyle();
		cellStyle.setForeground(BuySellColor.getColor(this._order.get_IsBuy(), false));
		tableModel.setCellStyle(2, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(TradeOption.getColor(this._order.get_TradeOption()));
		tableModel.setCellStyle(6, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(NumericColor.getColor(this._order.get_CommissionSum(), false));
		tableModel.setCellStyle(10, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(NumericColor.getColor(this._order.get_LevySum(), false));
		tableModel.setCellStyle(11, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(NumericColor.getColor(trade, false));
		tableModel.setCellStyle(12, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(NumericColor.getColor(interest, false));
		tableModel.setCellStyle(13, 1, cellStyle);

		cellStyle = new CellStyle();
		cellStyle.setForeground(NumericColor.getColor(storage, false));
		tableModel.setCellStyle(14, 1, cellStyle);

		return tableModel;
	}

	private void cancellationResult(TransactionError transactionError, String message)
	{
		Transaction transaction = this._order.get_Transaction();
		String info = StringHelper.format("cancellationResult: transactionError = {0}; message = {1}; transaction= {2}",
			new Object[]{transactionError, message, transaction == null ? "NULL" : transaction.get_Code()});
		TradingConsole.traceSource.trace(TraceType.Information, info);

		if (transaction==null) return;

		if (transactionError == TransactionError.OK)
		{
			transaction.cancel(Language.CustomerCanceled, false);
		}
		else if(transactionError == TransactionError.Action_NeedDealerConfirmCanceling)
		{
			transaction.setMessage(message);
		}
		else
		{
			String orderCodes = transaction.getOrderCodes();
			AlertDialogForm.showDialog(this, "Cancellation", true, orderCodes + ": " + message);
		}
	}

	private static class AwtSaflyCancelProcessor implements Runnable
	{
		private TradingInstructionForm _owner;
		private SignalObject signal;

		public AwtSaflyCancelProcessor(TradingInstructionForm owner, SignalObject signal)
		{
			this._owner = owner;
			this.signal= signal;
		}

		public void run()
		{
			if(this.signal.getIsError()){
				return;
			}

			String error= RequestCommandHelper.getStringFromResponse(this.signal.getResult());
			TransactionError transactionError=TransactionError.valueOf(TransactionError.class,error);
			String logAction ="";
			if (transactionError == TransactionError.OK || transactionError == TransactionError.Action_NeedDealerConfirmCanceling)
			{
				logAction += "," + Language.WebServiceCancelLMTOrderMessage1;
				if(transactionError == TransactionError.OK)
				{
					this._owner._order.get_Transaction().saveLog(LogCode.Cancelled);
				}
				else
				{
					Transaction transaction = this._owner._order.get_Transaction();
					this._owner._tradingConsole.saveLog(LogCode.RequestCancelLmtOrder, logAction, transaction.get_Id(), transaction.get_Account().get_Id());
				}
				this._owner.cancellationResult(transactionError, Language.WebServiceCancelLMTOrderMessage1);
			}
			else
			{
				logAction += "," + Language.WebServiceCancelLMTOrderMessage0;
				Transaction transaction = this._owner._order.get_Transaction();
				this._owner._tradingConsole.saveLog(LogCode.RequestCancelLmtOrder, logAction, transaction.get_Id(),transaction.get_Account().get_Id());
				this._owner.cancellationResult(transactionError, Language.WebServiceCancelLMTOrderMessage0);
			}
		}
	}

	public static class CancelCallback implements IAsyncCallback
	{
		private TradingInstructionForm _owner;

		public CancelCallback(TradingInstructionForm owner)
		{
			this._owner = owner;
		}
		public void asyncCallback(SignalObject signal){
			AwtSaflyCancelProcessor cancelProcessor = new AwtSaflyCancelProcessor(this._owner, signal);
			SwingUtilities.invokeLater(cancelProcessor);
		}

		public void asyncCallback(IAsyncResult iAsyncResult)
		{

		}
	}

	private void cancellation()
	{
		Object[] result = Transaction.isAllowCancelLMTOrder(this._settingsManager, this._order);
		if ( (Boolean)result[0])
		{
			String logAction = this._order.getLogActionForRequestCancelLMTOrder();
			this._tradingConsole.saveLog(LogCode.RequestCancelLmtOrder,logAction, this._order.get_Transaction().get_Id(),this._order.get_Transaction().get_Account().get_Id());

			CancelCallback callback = new CancelCallback(this);
			this._tradingConsole.get_TradingConsoleServer().beginCancelLMTOrder(this._order.get_Transaction().get_Id(), callback, logAction);
			this.dispose();
		}
		else
		{
			String message = (!StringHelper.isNullOrEmpty(result[1].toString())) ? result[1].toString()
				: Language.UnconfirmedInstructionPageBtnCancellation_OnclickAlert1;
			AlertDialogForm.showDialog(this, "Cancellation", true, message);
		}
	}

	private void clearOrder()
	{
		///fix bug of jide/////////////////////////////////////////////////////////////
		if(!StringHelper.isNullOrEmpty(this._order.get_PeerOrderCodes()))
		{
			this._tradingConsole.get_MainForm().get_OpenOrderTable().collapseAllRows();
		}
		//////////////////////////////////////////////////////////////////////////////////

		if (this._order.get_Phase() == Phase.Cancelled)
		{
			this._tradingConsole.removeOrder(this._order);
		}
		else
		{
			if (this._order.get_LotBalance().compareTo(BigDecimal.ZERO) <= 0)
			{
				this._tradingConsole.removeOrder(this._order);
			}
			else
			{
				this._tradingConsole.removeWorkingOrder(this._order);
				Order.removeFromWorkingOrderList(this._order);
			}
		}

		this.dispose();
	}

	public TradingInstructionForm(JFrame parent)
	{
		super(parent, true);
		try
		{
			this.newPriceEdit = new PriceSpinner(this);

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
		this.addWindowListener(new TradingInstructionUi_this_windowAdapter(this));

		this.setSize(300, this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther ? 420 : 400);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle("Trading Instruction");
		this.setBackground(FormBackColor.tradingInstructionForm);
		orderCodeStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));

		orderCodeStaticText.setHorizontalAlignment(SwingConstants.CENTER);
		orderCodeStaticText.setText("Unconfirmed Instruction");
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new TradingInstructionForm_cancelButton_actionAdapter(this));
		clearButton.setText("Clear");
		modifyButton.setText("Modify");
		exitButton.setText("Exit");
		exitButton.addActionListener(new TradingInstructionUi_exitButton_actionAdapter(this));
		remarksStaticText.setText("Remarks");
		remarksEdit.setText("Remarks");
		clearButton.addActionListener(new TradingInstructionForm_clearButton_actionAdapter(this));
		submitButton.addActionListener(new TradingInstructionForm_submitButton_actionAdapter(this));
		modifyButton.addActionListener(new TradingInstructionForm_modifyButton_actionAdapter(this));
		tradingInstructionTable.setEditable(false);

		this.getContentPane().add(cancelButton, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 3, 10, 1), 0, 0));
		this.getContentPane().add(clearButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 1), 0, 0));
		this.getContentPane().add(modifyButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 1), 0, 0));
		/*this.getContentPane().add(submitButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 1), 0, 0));*/
		this.getContentPane().add(ocoButton, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 3), 0, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 1, 10, 3), 0, 0));

		JScrollPane scrollPane = new JScrollPane(tradingInstructionTable);
		this.getContentPane().add(orderCodeStaticText, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(15, 1, 0, 1), 0, 0));
		this.getContentPane().add(remarksStaticText, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 5, 2, 3), 0, 0));
		this.getContentPane().add(remarksEdit, new GridBagConstraints(0, 3, 5, 1, 1.0, 0.15
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.BOTH, new Insets(1, 5, 2, 5), 0, 0));
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.85
			, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 0, 0));

		this._modifySetPricePanel = new JPanel();
		this._modifySetPricePanel.setLayout(new GridBagLayout());
		this.modifyOrderSetPriceCheckBox.setText(Language.UnconfirmedInstructionModifyOrder);
		this.newPriceStaticText.setText(Language.UnconfirmedInstructionNewPrice);
		this.modifyOrderSetPriceWarningStaticText.setText(Language.UnconfirmedInstructionModifyPriceWarning);
		this._modifySetPricePanel.add(this.modifyOrderSetPriceCheckBox, new GridBagConstraints(0, 0, 3, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(15, 0, 2, 5), 0, 0));
		this._modifySetPricePanel.add(this.newPriceStaticText, new GridBagConstraints(0, 1, 1, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 3, 2, 5), 0, 0));
		this._modifySetPricePanel.add(this.newPriceEdit, new GridBagConstraints(1, 1, 1, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 60, 0));
		this._modifySetPricePanel.add(this.priceRangeStaticText, new GridBagConstraints(2, 1, 1, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 30, 0));
		if(this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther
			&& this._order.get_Transaction().get_Orders().size() == 2)
		{
			Order[] orders = new Order[2];
			this._order.get_Transaction().get_Orders().values().toArray(orders);

			this.newPriceStaticText.setText(Language.UnconfirmedInstructionNewPrice + "(" + orders[0].get_TradeOptionString() + ")");
			this.newPriceStaticText2.setText(Language.UnconfirmedInstructionNewPrice + "(" + orders[1].get_TradeOptionString() + ")");

			this._modifySetPricePanel.add(this.newPriceStaticText2, new GridBagConstraints(0, 2, 1, 1, 0, 0
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 3, 2, 5), 0, 0));
			this._modifySetPricePanel.add(this.newPriceEdit2, new GridBagConstraints(1, 2, 1, 1, 0, 0
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 60, 0));
			this._modifySetPricePanel.add(this.priceRangeStaticText2, new GridBagConstraints(2, 2, 1, 1, 0, 0
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 30, 0));

			this._modifySetPricePanel.add(this.modifyOrderSetPriceWarningStaticText, new GridBagConstraints(0, 3, 3, 1, 0, 85
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 3, 2, 5), 0, 0));
		}
		else
		{
			this._modifySetPricePanel.add(this.modifyOrderSetPriceWarningStaticText, new GridBagConstraints(0, 2, 3, 1, 0, 85
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 3, 2, 5), 0, 0));
		}
		this.priceRangeStaticText.setText("                     ");

		Font font = new Font("SansSerif", Font.PLAIN, 12);
		this.modifyOrderSetPriceCheckBox.setFont(font);
		this.newPriceStaticText.setFont(font);
		this.newPriceEdit.setFont(font);
		this.priceRangeStaticText.setFont(font);
		this.newPriceStaticText2.setFont(font);
		this.newPriceEdit2.setFont(font);
		this.priceRangeStaticText2.setFont(font);

		/*this.getContentPane().add(this._modifySetPricePanel, new GridBagConstraints(0, 2, 5, 2, 1.0, 0.15
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(1, 0, 2, 5), 0, 0));*/

		this.newPriceEdit.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.newPriceEdit2.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.newPriceStaticText.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.newPriceStaticText2.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.priceRangeStaticText.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.modifyOrderSetPriceWarningStaticText.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.priceRangeStaticText2.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());
		this.submitButton.setEnabled(this.modifyOrderSetPriceCheckBox.isSelected());

		font = new Font("SansSerif", Font.BOLD, 12);
		this.modifyOrderSetPriceWarningStaticText.setFont(font);
		this.modifyOrderSetPriceWarningStaticText.setForeground(Color.RED);
		this.modifyOrderSetPriceCheckBox.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				newPriceEdit.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				newPriceStaticText.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				priceRangeStaticText.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				modifyOrderSetPriceWarningStaticText.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				newPriceEdit2.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				newPriceStaticText2.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				priceRangeStaticText2.setEnabled(modifyOrderSetPriceCheckBox.isSelected());

				submitButton.setEnabled(modifyOrderSetPriceCheckBox.isSelected());
				if(modifyOrderSetPriceCheckBox.isSelected())
				{
					updateNewPrice();
				}
			}
		});
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	DataGrid tradingInstructionTable = new DataGrid("TradingInstructionTable");
	PVStaticText2 orderCodeStaticText = new PVStaticText2();
	PVStaticText2 modifyOrderSetPriceStaticText = new PVStaticText2();
	JCheckBox modifyOrderSetPriceCheckBox = new JCheckBox();
	PVStaticText2 newPriceStaticText = new PVStaticText2();
	PriceSpinner newPriceEdit;
	PVStaticText2 priceRangeStaticText = new PVStaticText2();
	PVStaticText2 newPriceStaticText2 = new PVStaticText2();
	PriceSpinner newPriceEdit2;
	PVStaticText2 priceRangeStaticText2 = new PVStaticText2();

	PVStaticText2 modifyOrderSetPriceWarningStaticText = new PVStaticText2();

	PVButton2 cancelButton = new PVButton2();
	PVButton2 clearButton = new PVButton2();
	PVButton2 modifyButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 ocoButton = new PVButton2();
	PVStaticText2 remarksStaticText = new PVStaticText2();
	MultiTextArea remarksEdit = new MultiTextArea();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void cancelButton_actionPerformed(ActionEvent e)
	{
		this.cancellation();
	}

	public void clearButton_actionPerformed(ActionEvent e)
	{
		this.clearOrder();
	}

	public void modifyButton_actionPerformed(ActionEvent e)
	{
		ModifyOrderForm modfiyOrderForm = new ModifyOrderForm(this, this._tradingConsole, this._order);
		JideSwingUtilities.centerWindow(modfiyOrderForm);
		modfiyOrderForm.show();
		if(this._order.get_OrderModification() != null)
		{
			this.modifyButton.setEnabled(false);
		}
	}

	private void updateNewPrice()
	{
		boolean isBuy = this._order.get_IsBuy();
		boolean isOpen = this._order.get_IsOpen();
		boolean isLimit = this._order.get_TradeOption() == TradeOption.Better;
		if(this._ifOrder != null)
		{
			if(this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther)
			{
				Order[] orders = new Order[2];
				this._order.get_Transaction().get_Orders().values().toArray(orders);

				isLimit = orders[0].get_TradeOption() == TradeOption.Better;
				Price newPrice = getNewPrice(isBuy, isLimit);
				this.newPriceEdit.setText(Price.toString(newPrice));

				isLimit = orders[1].get_TradeOption() == TradeOption.Better;
				newPrice = getNewPrice(isBuy, isLimit);
				this.newPriceEdit2.setText(Price.toString(newPrice));
			}
			else
			{
				Price newPrice = getNewPrice(isBuy, isLimit);
				this.newPriceEdit.setText(Price.toString(newPrice));
			}
		}
		else
		{
			Price bid = this._instrument.get_LastQuotation().get_Bid();
			Price ask = this._instrument.get_LastQuotation().get_Ask();
			if (bid == null || ask == null)
			{
				return;
			}
			if(this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther)
			{
				Order[] orders = new Order[2];
				this._order.get_Transaction().get_Orders().values().toArray(orders);

				isLimit = orders[0].get_TradeOption() == TradeOption.Better;
				Price newPrice = getNewPrice(isBuy, isLimit, bid, ask, isOpen);
				this.newPriceEdit.setText(Price.toString(newPrice));

				isLimit = orders[1].get_TradeOption() == TradeOption.Better;
				newPrice = getNewPrice(isBuy, isLimit, bid, ask, isOpen);
				this.newPriceEdit2.setText(Price.toString(newPrice));
			}
			else
			{
				Price newPrice = getNewPrice(isBuy, isLimit, bid, ask, isOpen);
				this.newPriceEdit.setText(Price.toString(newPrice));
			}
		}
	}

	private Price getNewPrice(boolean isBuy, boolean isLimit, Price bid, Price ask, boolean isOpen)
	{
		Price price = null;
		int acceptLmtVariation = this._instrument.get_AcceptLmtVariation(this._order.get_Account(), isBuy, this._order.get_LotBalance(), this._order, this._order.get_RelationOrders(), false);

		 if (isLimit)
		 {
			 price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
			 //relationChar = (this._instrument.get_IsNormal() == isBuy) ? " <= " : " >= ";
		 }
		 else
		 {
			 price = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
			 //relationChar = (this._instrument.get_IsNormal() == isBuy) ? " >= " : " <= ";
		 }

		 if (this.isBetweenBidToAsk(price))
		 {
			 if (isLimit)
			 {
				 price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, this._instrument.get_NumeratorUnit()) :
					 Price.add(ask, this._instrument.get_NumeratorUnit());
			 }
			 else
			 {
				 price = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, this._instrument.get_NumeratorUnit()) :
					 Price.subStract(bid, this._instrument.get_NumeratorUnit());
			 }
		 }
		 return price;
	}

	private Price getNewPrice(boolean isBuy, boolean isLimit)
	{
		Price price = null;
		Order[] orders = new Order[2];
					this._order.get_Transaction().get_Orders().values().toArray(orders);

					Price basePrice = this._ifOrder.get_SetPrice();
					int spread = this._settingsManager.getQuotePolicyDetail(this._instrument.get_Id()).get_Spread() + this._instrument.get_NumeratorUnit();
					int acceptIfDoneVariation = this._instrument.get_AcceptIfDoneVariation();
					spread = Math.max(spread, acceptIfDoneVariation);
					if (isBuy == this._instrument.get_IsNormal())
					{
						price = isLimit ? Price.subStract(basePrice, acceptIfDoneVariation) : Price.add(basePrice, spread);
					}
					else
					{
						price = isLimit ? Price.add(basePrice, acceptIfDoneVariation) : Price.subStract(basePrice, spread);
					}
		return price;
	}

	private boolean isBetweenBidToAsk(Price setPrice)
	{
		if (Parameter.isAllowLimitInSpread)
		{
			return false;
		}
		else
		{
			return (!Price.less(setPrice, this._instrument.get_LastQuotation().get_Bid())
					&& !Price.more(setPrice, this._instrument.get_LastQuotation().get_Ask()));
		}
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		boolean isOCO = this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther;
		Order[] ocoOrders = null;
		if(isOCO)
		{
			ocoOrders = new Order[2];
			this._order.get_Transaction().get_Orders().values().toArray(ocoOrders);
		}

		Price setPrice = Price.parse(this.newPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		if (setPrice == null)
		{
			AlertDialogForm.showDialog(this, null, true,
									   "[" + Language.UnconfirmedInstructionNewPrice + "]" + Language.OrderLMTPageorderValidAlert1);
			return;
		}

		Price setPrice2 = null;
		if(isOCO)
		{
			setPrice2 = Price.parse(this.newPriceEdit2.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			if (setPrice2 == null)
			{
				AlertDialogForm.showDialog(this, null, true,
										   "[" + Language.UnconfirmedInstructionNewPrice + "]" + Language.OrderLMTPageorderValidAlert1);
				return;
			}
		}

		if(this._ifOrder == null)
		{
			if(isOCO)
			{
				TradeOption previousTradeOption = ocoOrders[0].get_TradeOption();
				SetPriceError error = this.checkPrice(setPrice, previousTradeOption);
				previousTradeOption = ocoOrders[1].get_TradeOption();
				SetPriceError error2 = this.checkPrice(setPrice2, previousTradeOption);
				if (error != SetPriceError.Ok || error2 != SetPriceError.Ok)
				{
					this.handleSetPrieceError(error, Language.UnconfirmedInstructionNewPrice);
					this.updateNewPrice();
					return;
				}
			}
			else
			{
				TradeOption previousTradeOption = this._order.get_TradeOption();
				SetPriceError error = this.checkPrice(setPrice, previousTradeOption);
				if (error != SetPriceError.Ok)
				{
					this.handleSetPrieceError(error, Language.UnconfirmedInstructionNewPrice);
					this.updateNewPrice();
					return;
				}
			}
		}
		else
		{
			boolean isBuy = this._order.get_IsBuy();
			Price basePrice = this._ifOrder.get_SetPrice();
			int acceptIfDoneVariation = this._instrument.get_AcceptIfDoneVariation();
			boolean priceIsValid = true;
			if(isOCO)
			{
				priceIsValid = checkNewPrice(setPrice, isBuy, basePrice, acceptIfDoneVariation, ocoOrders[0].get_TradeOption());
				priceIsValid &= checkNewPrice(setPrice2, isBuy, basePrice, acceptIfDoneVariation, ocoOrders[1].get_TradeOption());
			}
			else
			{
				priceIsValid = checkNewPrice(setPrice, isBuy, basePrice, acceptIfDoneVariation, this._order.get_TradeOption());
			}
			if(!priceIsValid)
			{
				AlertDialogForm.showDialog(this, null, true, "[" + Language.UnconfirmedInstructionNewPrice + "] " + Language.InvalidSetPrice);
				this.updateNewPrice();
				return;
			}
		}

		String xml = this.getModifyXml(Price.toString(setPrice), Price.toString(setPrice2));
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		XmlNode xmlTransaction = xmlDocument.get_DocumentElement();

		PlaceResult result = this._tradingConsole.get_TradingConsoleServer().place(xmlTransaction);
		if(result.get_TransactionError() == TransactionError.OK)
		{
			this.dispose();
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, Language.FailedToModifyOrder + ": " + TransactionError.getCaption(result.get_TransactionError()));
		}
	}

	private boolean checkNewPrice(Price setPrice, boolean isBuy, Price basePrice, int acceptIfDoneVariation, TradeOption tradeOption)
	{
		boolean priceIsValid = true;
		if(tradeOption == TradeOption.Better)
		 {
			 if(this._instrument.get_IsNormal() != isBuy ? setPrice.compareTo(Price.add(basePrice, acceptIfDoneVariation)) < 0 :
				setPrice.compareTo(Price.subStract(basePrice, acceptIfDoneVariation)) > 0)
			 {
				  priceIsValid = false;
			 }
		 }
		 else
		 {
			 int spread = this._settingsManager.getQuotePolicyDetail(this._instrument.get_Id()).get_Spread() + this._instrument.get_NumeratorUnit();
			 spread = Math.max(spread, acceptIfDoneVariation);
			 Price comparePrice = isBuy ? Price.add(basePrice, spread) : Price.subStract(basePrice, spread);
			 if(this._instrument.get_IsNormal() != isBuy ? setPrice.compareTo(comparePrice) > 0 :
				setPrice.compareTo(comparePrice) < 0)
			 {
				 priceIsValid = false;
			 }
		 }
		return priceIsValid;
	}

	private String getModifyXml(String setPrice, String setPrice2)
	{
		boolean isOCO = this._order.get_Transaction().get_Type() == TransactionType.OneCancelOther;
		Order[] ocoOrders = null;
		if(isOCO)
		{
			ocoOrders = new Order[2];
			this._order.get_Transaction().get_Orders().values().toArray(ocoOrders);
		}

		Transaction assigningTransaction = this._order.get_Transaction();
		Guid transactionId = Guid.newGuid();

		String xml = "<Transaction ";
		xml += "ID=\'" + transactionId.toString() + "\' " +
			"AccountID=\'" + assigningTransaction.get_Account().get_Id().toString() + "\' " +
			"InstrumentID=\'" + assigningTransaction.get_Instrument().get_Id().toString() + "\' " +
			"Type=\'" + XmlConvert.toString(assigningTransaction.get_Type().value()) + "\' " +
			"SubType=\'" + XmlConvert.toString(TransactionSubType.Amend.value()) + "\' " +
			"OrderType=\'" + XmlConvert.toString(assigningTransaction.get_OrderType().value()) + "\' " +
			"BeginTime=\'" + XmlConvert.toString(assigningTransaction.get_BeginTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"EndTime=\'" + XmlConvert.toString(assigningTransaction.get_EndTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"ExpireType=\'" + XmlConvert.toString(assigningTransaction.get_ExpireType().value()) + "\' " +
			"SubmitTime=\'" + XmlConvert.toString(assigningTransaction.get_SubmitTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"SubmitorID=\'" + assigningTransaction.get_SubmitorID().toString() + "\' " +
			"AssigningOrderID=\'" + this._order.get_Id().toString() + "\' >";

		String lot = this._order.get_LotString();
		if(isOCO)
		{
			xml += getModifyXml(setPrice, lot, ocoOrders[0]);
			xml += getModifyXml(setPrice2, lot, ocoOrders[1]);
		}
		else
		{
			xml += getModifyXml(setPrice, lot, this._order);
		}
		xml += "</Transaction>";
		return xml;
	}

	private String getModifyXml(String setPrice, String lot, Order order)
	{
		Guid orderId = Guid.newGuid();
		String xml = "";
		xml += "<Order ";
				  xml += "ID=\'" + orderId.toString() + "\'";
				  xml += "TradeOption=\'" + XmlConvert.toString(order.get_TradeOption().value()) + "\' ";
				  xml += "IsOpen=\'" + XmlConvert.toString(order.get_IsOpen()) + "\' ";
				  xml += "IsBuy=\'" + XmlConvert.toString(order.get_IsBuy()) + "\' ";
				  xml += "SetPrice=\'" + (StringHelper.isNullOrEmpty(setPrice) ? Price.toString(order.get_SetPrice()) : setPrice) + "\' ";
				  xml += "SetPrice2=\'" + Price.toString(order.get_SetPrice2()) + "\' ";
				  xml += "DQMaxMove=\'" + XmlConvert.toString(order.get_DQMaxMove()) + "\' ";
				  xml += "Lot=\'" + lot + "\' ";
				  xml += "OriginalLot=\'" + lot + "\' ";
				  xml += ">";
				  if(!order.get_IsOpen())
				  {
					  HashMap<Guid, RelationOrder> relations = order.get_RelationOrders();
					  for(Guid openOrderId : relations.keySet())
					  {
						  RelationOrder relation = relations.get(openOrderId);
						  xml +=
							  "<OrderRelation " +
							  "OpenOrderID=\'" + openOrderId.toString() + "\' " +
							  "ClosedLot=\'" + relation.get_LiqLotString() + "\'>" +
							  "</OrderRelation>";
					  }
				  }
				  xml += "</Order>";
		return xml;
	}

	private void handleSetPrieceError(SetPriceError setPriceError, String priceName)
	{
		if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
		{
			if (this._settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
			{
				boolean isBuy = this._order.get_IsBuy();
				AlertDialogForm.showDialog(this, null, true,
										   "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert2 + " " + this._instrument.get_AcceptLmtVariation(this._order.get_Account(), isBuy, this._order.get_LotBalance(), this._order, this._order.get_RelationOrders(), false) +
										   " " +
										   Language.OrderLMTPageorderValidAlert22);
			}
			else
			{
				AlertDialogForm.showDialog(this, null, true, priceName + " " + Language.SetPriceTooCloseToMarket);
			}
	}
		else if (setPriceError == SetPriceError.SetPriceTooFarAwayMarket)
		{
			AlertDialogForm.showDialog(this, null, true, "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert3);
		}
		else if (setPriceError == SetPriceError.InvalidSetPrice)
		{
			AlertDialogForm.showDialog(this, null, true, "[" + priceName + "] " + Language.InvalidSetPrice);
		}
	}


	private SetPriceError checkPrice(Price setPrice, TradeOption previousTradeOption)
	{
		Price marketPrice = this._instrument.get_LastQuotation().getBuySell(this._order.get_IsBuy());

		SetPriceError setPriceError = SetPriceError.Ok;
		boolean isBuy = this._order.get_IsBuy();
		Account account = this._order.get_Account();
		BigDecimal lot = this._order.get_LotBalance();

		setPriceError = Order.checkLMTOrderSetPrice(account, true, this._instrument, isBuy, previousTradeOption, setPrice, marketPrice, lot, this._order, this._order.get_RelationOrders(), false);
		double dblMarketPrice = Price.toDouble(marketPrice);
		if (Math.abs(Price.toDouble(setPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
		{
			setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
		}
		return setPriceError;
	}

	public Instrument getInstrument()
	{
		return this._order.get_Transaction().get_Instrument();
	}

	class TradingInstructionUi_this_windowAdapter extends WindowAdapter
	{
		private TradingInstructionForm adaptee;
		TradingInstructionUi_this_windowAdapter(TradingInstructionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class TradingInstructionForm_clearButton_actionAdapter implements ActionListener
{
	private TradingInstructionForm adaptee;
	TradingInstructionForm_clearButton_actionAdapter(TradingInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.clearButton_actionPerformed(e);
	}
}

class TradingInstructionForm_submitButton_actionAdapter implements ActionListener
	{
		private TradingInstructionForm adaptee;
		TradingInstructionForm_submitButton_actionAdapter(TradingInstructionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e)
		{
			adaptee.submitButton_actionPerformed(e);
		}
}

class TradingInstructionForm_modifyButton_actionAdapter implements ActionListener
{
	private TradingInstructionForm adaptee;
	TradingInstructionForm_modifyButton_actionAdapter(TradingInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.modifyButton_actionPerformed(e);
	}
}

class TradingInstructionForm_cancelButton_actionAdapter implements ActionListener
{
	private TradingInstructionForm adaptee;
	TradingInstructionForm_cancelButton_actionAdapter(TradingInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.cancelButton_actionPerformed(e);
	}
}

class TradingInstructionUi_exitButton_actionAdapter implements ActionListener
{
	private TradingInstructionForm adaptee;
	TradingInstructionUi_exitButton_actionAdapter(TradingInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
