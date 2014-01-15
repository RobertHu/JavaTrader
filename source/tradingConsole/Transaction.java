package tradingConsole;

import java.math.*;
import java.util.*;

import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.threading.Scheduler.*;
import framework.xml.*;
import tradingConsole.common.*;
import tradingConsole.enumDefine.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;
import tradingConsole.ui.language.*;
import tradingConsole.ui.columnKey.OpenOrderColKey;
import framework.threading.Scheduler;
import java.io.IOException;
import Packet.SignalObject;

public class Transaction implements ISchedulerCallback
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private String _code;
	private TransactionType _type;
	private TransactionSubType _subType = TransactionSubType.None;
	private Phase _phase;
	private DateTime _beginTime;
	private DateTime _endTime;
	private ExpireType _expireType = ExpireType.GTD;
	private DateTime _submitTime;
	private DateTime _executeTime;
	private OrderType _orderType;
	private BigDecimal _contractSize;
	private String _message;
	private Guid _submitorID = Guid.empty;

	private Instrument _instrument;
	private Guid _instrumentId;
	private Account _account;
	private HashMap<Guid, Order> _orders;

	//for Make order
	private Order _assigningOrder;
	private Guid _assigningOrderId;
	private MakeOrderAccount _makeOrderAccount;

	//for Make order
	public MakeOrderAccount get_MakeOrderAccount()
	{
		return this._makeOrderAccount;
	}

	//for Make order
	public void set_MakeOrderAccount(MakeOrderAccount value)
	{
		this._makeOrderAccount = value;
	}

	private String getChangeToOcoOrderConfirmXml()
	{
		Instrument instrument = this.get_Instrument();
		String xml = "<Transaction ";
		this._orderType = OrderType.Limit;
		xml += "ID=\'" + this._id.toString() + "\' " +
			"AccountID=\'" + this._account.get_Id().toString() + "\' " +
			"InstrumentID=\'" + this._instrument.get_Id().toString() + "\' " +
			"InstrumentCategory=\'" + XmlConvert.toString(instrument.get_Category().value()) + "\' " +
			"Type=\'" + XmlConvert.toString(this._type.value()) + "\' " +
			"SubType=\'" + XmlConvert.toString(this._subType.value()) + "\' " +
			"OrderType=\'" + XmlConvert.toString(this._orderType.value()) + "\' " +
			"BeginTime=\'" + XmlConvert.toString(this._beginTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
			"EndTime=\'" + XmlConvert.toString(this._endTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
			"ExpireType=\'" + XmlConvert.toString(this._expireType.value()) + "\' " +
			"SubmitTime=\'" + XmlConvert.toString(this._submitTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
			"SubmitorID=\'" + this._submitorID.toString() + "\' " +
			"AssigningOrderID=\'" + this._assigningOrder.get_Id().toString() + "\' >";

		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			xml += order.getMakeOrderConfirmXml(OperateType.OneCancelOther);
		}
		xml += "</Transaction>";

		return xml;
	}

	//<Transaction ID="" AccountID="" InstrumentID="" Type="" OrderType="" BeginTime="" EndTime="" SubmitTime="" SubmitorID="" >
	//	<Order ID="" TradeOption="" IsOpen="" IsBuy="" SetPrice="" Lot="" >
	//		<OrderRelation OpenOrderID="" ClosedLot="" />
	//		<OrderRelation OpenOrderID="" ClosedLot="" />
	//	</Order>
	//</Transaction>
	private String getMakeOrderConfirmXml(OperateType operateType)
	{
		if(this._subType == TransactionSubType.IfDone)
		{
			Order order = (Order)this._orders.values().iterator().next();
			if (order.get_TradeOption() == TradeOption.Better
				&& this._ifDoneInfo.get_LimitPriceForIfLimitDone() == null
				&& this._ifDoneInfo.get_StopPirceForIfLimitDone() == null)
			{
				this._subType = TransactionSubType.None;
			}

			if (order.get_TradeOption() == TradeOption.Stop
				&& this._ifDoneInfo.get_LimitPriceForIfStopDone() == null
				&& this._ifDoneInfo.get_StopPirceForIfStopDone() == null)
			{
				this._subType = TransactionSubType.None;
			}
		}

		Instrument instrument = this.get_Instrument();

		String xml = "<Transaction ";
		if (operateType.equals(OperateType.Assign))
		{
			xml += "ID=\'" + this._id.toString() + "\' " +
				"AccountID=\'" + this._account.get_Id().toString() + "\' " +
				"Type=\'" + XmlConvert.toString(this._type.value()) + "\' " +
				"SubType=\'" + XmlConvert.toString(this._subType.value()) + "\' " +
				"InstrumentCategory=\'" + XmlConvert.toString(instrument.get_Category().value()) + "\' " +
				"BeginTime=\'" + XmlConvert.toString(this._beginTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				"EndTime=\'" + XmlConvert.toString(this._endTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				"ExpireType=\'" + XmlConvert.toString(this._expireType.value()) + "\' " +
				"SubmitTime=\'" + XmlConvert.toString(this._submitTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				//"ContractSize=\'" + XmlConvert.toString(this._contractSize) + "\' " +
				"SubmitorID=\'" + this._submitorID.toString() + "\' " +
				"AssigningOrderID=\'" + this._assigningOrder.get_Id().toString() + "\' >";
		}
		else if (operateType.equals(OperateType.MultiSpotTrade) ||
				 operateType.equals(OperateType.Method2SpotTrade) ||
				 operateType.equals(OperateType.SingleSpotTrade) ||
				 operateType.equals(OperateType.Limit) ||
				 operateType.equals(OperateType.DirectLiq) ||
				 operateType.equals(OperateType.OneCancelOther))
		{
			//??????????????????
			if (this._orderType == OrderType.OneCancelOther)
			{
				this._orderType = OrderType.Limit;
			}
			xml += "ID=\'" + this._id.toString() + "\' " +
				"AccountID=\'" + this._account.get_Id().toString() + "\' " +
				"InstrumentID=\'" + this._instrument.get_Id().toString() + "\' " +
				"Type=\'" + XmlConvert.toString(this._type.value()) + "\' " +
				"SubType=\'" + XmlConvert.toString(this._subType.value()) + "\' " +
				"InstrumentCategory=\'" + XmlConvert.toString(instrument.get_Category().value()) + "\' " +
				"OrderType=\'" + XmlConvert.toString(this._orderType.value()) + "\' " +
				"BeginTime=\'" + XmlConvert.toString(this._beginTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				"EndTime=\'" + XmlConvert.toString(this._endTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				"ExpireType=\'" + XmlConvert.toString(this._expireType.value()) + "\' " +
				"SubmitTime=\'" + XmlConvert.toString(this._submitTime, "yyyy-MM-dd HH:mm:ss") + "\' " +
				//"ContractSize=\'" + XmlConvert.toString(this._contractSize) + "\' "  +
				"SubmitorID=\'" + this._submitorID.toString() + "\'>";
		}
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			xml += order.getMakeOrderConfirmXml(operateType);
		}
		xml += "</Transaction>";

		return xml;
	}

	public void makeOrderConfirm(OperateType operateType, DateTime beginTime, DateTime endTime, DateTime submitTime)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: CreateDoOrderXml_Begin");

		this._beginTime = beginTime;
		this._endTime = endTime;
		this._submitTime = submitTime;

		this._tradingConsole.setTransaction(this);
		this._account.get_Transactions().put(this._id, this);
		this._instrument.get_Transactions().put(this._id, this);
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			//if(this._settingsManager.get_Accounts().size() == 1)
			{
				PalceLotNnemonic.set(this._instrument.get_Id(), order.get_Account().get_Id(), order.get_Lot());
			}
			order.makeOrderConfirm();
		}

		String xml = this.getMakeOrderConfirmXml(operateType);
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		XmlNode xmlTransaction = xmlDocument.get_DocumentElement();

		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: CreateDoOrderXml_End");
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: saveLog(Placing)_Begin");
		this.saveLog(LogCode.Placing);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: saveLog(Placing)_End");
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: Place_Begin");
		if (operateType.equals(OperateType.Assign))
		{
			this.asyncAssign(xmlTransaction);
		}
		else
		{
			this.asyncPlace(xmlTransaction);
		}
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: Place_End");
	}

	public String getLogAction()
	{
		String logAction = "";
		boolean isFirstOrder = true;
		boolean isLastOrder = false;
		int index = 0;
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			isLastOrder = index == this._orders.size() - 1;
			Order order = iterator.next();
			logAction = (!StringHelper.isNullOrEmpty(logAction)) ? logAction + TradingConsole.delimiterRow : "";
			logAction += order.getLogAction(isFirstOrder, isLastOrder);
			isFirstOrder = false;
			index++;
		}
		if(this.get_OrderType().value() == OrderType.SpotTrade.value()
		   && this._phase.value() == Phase.Placing.value() && this._placingLogInfo != null)
		{
			logAction += this._placingLogInfo;
		}
		return logAction;
	}

	public void saveLog(LogCode logCode)
	{
		this.saveLog(logCode, null);
	}

	public void saveLog(LogCode logCode, String debugInformation)
	{
		String logAction = this.getLogAction();
		if(logCode == LogCode.Placing)
		{
			/*XmlDocument document = new XmlDocument();
			XmlNode debugInfo = document.createElement("DebugInfo");
			document.appendChild(debugInfo);

			if(this._placeOrderInfo != null)
			{
				XmlElement quotationFromInstrument = document.createElement("QuotationFromInstrument");

				quotationFromInstrument.setAttribute("TimeStamp", this._placeOrderInfo.get_TimestampOfLastQuotation().toString());
				quotationFromInstrument.setAttribute("TimeStamp", this._placeOrderInfo.get_TimestampOfLastQuotation().toString());
				quotationFromInstrument.setAttribute("Ask", this._placeOrderInfo.get_AskOfLastQuotation());
				quotationFromInstrument.setAttribute("Bid", this._placeOrderInfo.get_BidOfLastQuotation());
				debugInfo.appendChild(quotationFromInstrument);

				XmlElement quotationOnUI = document.createElement("QuotationOnUI");
				quotationOnUI.setAttribute("Ask", this._placeOrderInfo.get_AskOnUI());
				quotationOnUI.setAttribute("Bid", this._placeOrderInfo.get_BidOnUI());
				debugInfo.appendChild(quotationOnUI);
			}
			debugInfo.appendChild(this._tradingConsole.get_TradingConsoleServer().get_SlidingWindow().getHealthMessage(document));
			this._tradingConsole.saveLog(logCode, logAction, debugInfo.get_OuterXml(), this._id);*/

			StringBuilder debugInfo = new StringBuilder();

			if(this._placeOrderInfo != null)
			{
				debugInfo.append("QuotationFromInstrument: ");
				debugInfo.append("TimeStamp=");
				debugInfo.append(this._placeOrderInfo.get_TimestampOfLastQuotation().toString());
				debugInfo.append(";Ask=");
				debugInfo.append(this._placeOrderInfo.get_AskOfLastQuotation());
				debugInfo.append(";Bid=");
				debugInfo.append(this._placeOrderInfo.get_BidOfLastQuotation());
				debugInfo.append("\t");

				debugInfo.append("QuotationOnUI: ");
				debugInfo.append("Ask=");
				debugInfo.append(this._placeOrderInfo.get_AskOnUI());
				debugInfo.append(";Bid=");
				debugInfo.append(this._placeOrderInfo.get_BidOnUI());
				debugInfo.append("\t");
			}
			this._tradingConsole.get_TradingConsoleServer().get_SlidingWindow().getHealthMessage(debugInfo);
			this._tradingConsole.saveLog(logCode, logAction, debugInfo.toString(), this._id, this.get_Account().get_Id());
		}
		else
		{
			if(StringHelper.isNullOrEmpty(debugInformation))
			{
				this._tradingConsole.saveLog(logCode, logAction, this._id, this.get_Account().get_Id());
			}
			else
			{
				this._tradingConsole.saveLog(logCode, logAction, debugInformation, this._id, this.get_Account().get_Id());
			}
		}
	}

	private void asyncPlace(XmlNode xmlTransaction)
	{
		PlaceCallback placeCallback = new PlaceCallback(this);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: placeToServer()_Begin");
		try
		{
			this._tradingConsole.get_TradingConsoleServer().beginPlace(xmlTransaction, placeCallback, null);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	static class PlaceCallback implements IAsyncCallback
	{
		private Transaction _owner;

		public PlaceCallback(Transaction owner)
		{
			this._owner = owner;
		}
		public void asyncCallback(IAsyncResult iAsyncResult){

		}

		public void asyncCallback(SignalObject signal)
		{
			AwtSafelyPlaceProcessor placeProcessor = new AwtSafelyPlaceProcessor(this._owner, signal);
			SwingUtilities.invokeLater(placeProcessor);
		}
	}

	static class AwtSafelyPlaceProcessor implements Runnable
	{
		private Transaction _owner;
		private SignalObject signal;

		public AwtSafelyPlaceProcessor(Transaction owner, SignalObject signal)
		{
			this._owner = owner;
			this.signal=signal;
		}

		public void run()
		{
			PlaceResult result = new PlaceResult(this.signal.getResult());
				//this._owner._tradingConsole.get_TradingConsoleServer().endPlace(this._asyncResult, "");

			TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: placeToServer()_End");

			if (  !this.signal.getIsError()
				 &&(result.get_TransactionError().equals(TransactionError.OK)
				|| result.get_TransactionError().equals(TransactionError.Action_ShouldAutoFill)))
			{
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: updateTranCode_Begin");
				this._owner.handlePlaceResult(result.get_TranCode());
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: updateTranCode_End");

				/*TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: saveLog(Placed)_Begin");
				this._owner.saveLog(LogCode.Placing);
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: saveLog(Placed)_End");*/
			}
			else
			{
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: cancel_Begin");

				String livePrice = "";
				if(result.get_TransactionError().equals(TransactionError.InvalidPrice)
					&& !StringHelper.isNullOrEmpty(result.get_TranCode()))
				{
					livePrice = OpenOrderColKey.LivePriceString + ": " + result.get_TranCode();
				}
				this._owner.cancel(TransactionError.getCaption(result.get_TransactionError()) + livePrice, false, true);
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: cancel_End");
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: saveLog(Cancelled)_Begin");
				this._owner.saveLog(LogCode.Cancelled, TransactionError.getCaption(result.get_TransactionError()) + livePrice);
				TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: saveLog(Cancelled)_End");
			}
		}
	}

	private void asyncAssign(XmlNode xmlTransaction)
	{
		Transaction.AssignCallback placeCallback = new Transaction.AssignCallback(this);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm.place: assignToServer()_Begin");
		this._tradingConsole.get_TradingConsoleServer().beginAssign(xmlTransaction, placeCallback, xmlTransaction);
	}

	static class AwtSafelyAssignProcessor implements Runnable
	{
		private Transaction _owner;
		private IAsyncResult _asyncResult;

		public AwtSafelyAssignProcessor(Transaction owner, IAsyncResult asyncResult)
		{
			this._owner = owner;
			this._asyncResult = asyncResult;
		}

		public void run()
		{
			this._owner.assign(this._asyncResult);

			this._owner._tradingConsole.getInterestRate();
			this._owner._tradingConsole.calculatePLFloat();
		}
	}

	private static class AssignCallback implements IAsyncCallback
	{
		private Transaction _owner;

		public AssignCallback(Transaction owner)
		{
			this._owner = owner;
		}

		public void asyncCallback(IAsyncResult asyncResult)
		{
			AwtSafelyAssignProcessor assignProcessor = new AwtSafelyAssignProcessor(this._owner, asyncResult);
			SwingUtilities.invokeLater(assignProcessor);
		}
	}

	private void setSubType(TransactionSubType subType)
	{
		this._subType = subType;
	}

	private void setPhase(Phase phase)
	{
		if(this._phase == null || this._phase.value() != phase.value())
		{
			this._phase = phase;
			/*if (this._phase.value() == Phase.Executed.value())
			{
				if(this._instrument != null) this._instrument.calculateSummary();
				if(this._tradingConsole != null) this._tradingConsole.rebindSummary();
			}*/
		}
	}

	private void assign(IAsyncResult asyncResult)
	{
		XmlNode xmlTransaction = (XmlNode)asyncResult.get_AsyncState();
		XmlNode xmlAccount = new XmlDocument();
		XmlNode xmlInstrument = new XmlDocument();

		AssignResult result = _tradingConsole.get_TradingConsoleServer().endAssign(asyncResult, xmlTransaction, xmlAccount, xmlInstrument);

		if (!result.get_TransactionError().equals(TransactionError.OK))
		{
			this.cancel(TransactionError.getCaption(result.get_TransactionError()), false);
			this.saveLog(LogCode.Cancelled);
			return;
		}

		xmlTransaction = result.get_XmlTransaction();
		xmlAccount = result.get_XmlAccount();
		xmlInstrument = result.get_XmlInstrument();
		TradingConsole.traceSource.trace(TraceType.Information, "Assign: Transaction = " + (xmlTransaction == null ? "null" : xmlTransaction.get_OuterXml())
										 + ";" + (xmlAccount == null ? "null" : xmlAccount.get_OuterXml())
										 + ";" + (xmlInstrument == null ? "null" : xmlInstrument.get_OuterXml()));

		this.setPhase(Phase.Executed);

		if (xmlTransaction != null)
		{
			XmlElement transactionXmlElement = (XmlElement)xmlTransaction;
			Transaction.assign(this._tradingConsole, this._settingsManager, transactionXmlElement);
		}

		if (xmlInstrument != null)
		{
			/*
			 XmlNodeList instrumentXmlNodeList = xmlInstrument.get_ChildNodes();
			 for (int i = 0; i < instrumentXmlNodeList.get_Count(); i++)
			 {
			 XmlNode instrumentXmlNode = instrumentXmlNodeList.item(i);
			 for (int j = 0; j < instrumentXmlNode.get_ChildNodes().get_Count(); j++)
			 {
			  XmlElement transactionXmlElement = instrumentXmlNode.get_ChildNodes().item(j).get_Item("Transaction");
			  Transaction.assign2(this._tradingConsole, this._settingsManager, transactionXmlElement);
			 }
			 Guid instrumentId = new Guid(instrumentXmlNode.get_Attributes().get_ItemOf("ID").get_Value());
			 this._settingsManager.getInstrument(instrumentId).reCalculateTradePLFloat();
			 }
			 */

			for (int j = 0; j < xmlInstrument.get_ChildNodes().get_Count(); j++)
			{
				XmlElement transactionXmlElement = xmlInstrument.get_ChildNodes().item(j).get_Item("Transaction");
				Transaction.assign2(this._tradingConsole, this._settingsManager, transactionXmlElement);
			}
			Guid instrumentId = new Guid(xmlInstrument.get_Attributes().get_ItemOf("ID").get_Value());
			this._settingsManager.getInstrument(instrumentId).reCalculateTradePLFloat();
		}

		if (xmlAccount != null)
		{
			XmlElement accountXmlElement = (XmlElement)xmlAccount;
			Account.assign(this._tradingConsole, this._settingsManager, accountXmlElement);
			Account.refresh(this._tradingConsole, this._settingsManager, accountXmlElement);
		}

		this.saveLog(LogCode.Confirmed);

		//multi-agent use
		if (this._settingsManager.get_Customer().get_AllowFreeAgent() == 1)
		{
			this._assigningOrder.get_Transaction().get_Account().fixDataForAllowFreeAgent();
		}

		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.refrshCloseOrders();
		}
		this.notifyCustomerExecuteOrder();
	}

	//for Make order
	public Order get_AssigningOrder()
	{
		return this._assigningOrder;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public TransactionType get_Type()
	{
		return this._type;
	}

	public TransactionSubType get_SubType()
	{
		return this._subType;
	}

	public DateTime get_ExecuteTime()
	{
		return this._executeTime;
	}

	public DateTime get_BeginTime()
	{
		return this._beginTime;
	}

	public DateTime get_EndTime()
	{
		return this._endTime;
	}

	public ExpireType get_ExpireType()
	{
		return this._expireType;
	}

	public DateTime get_SubmitTime()
	{
		return this._submitTime;
	}

	public Guid get_SubmitorID()
	{
		return this._submitorID;
	}

	public String get_Submitor()
	{
		if (this._submitorID.equals(this._settingsManager.get_Customer().get_UserId()))
		{
			return this._settingsManager.get_Customer().get_CustomerName();
		}
		else if (this._account.get_AgentId() != null && this._submitorID.equals(this._account.get_AgentId()))
		{
			return this._account.get_AgentName();
		}
		return "";
	}

	public OrderType get_OrderType()
	{
		return this._orderType;
	}

	public Phase get_Phase()
	{
		return this._phase;
	}

	public String get_Code()
	{
		return this._code;
	}

	public BigDecimal get_ContractSize()
	{
		return this._contractSize;
	}

	public String get_Message()
	{
		return StringHelper.isNullOrEmpty(this._message) ? "" : this._message;
	}

	public Instrument get_Instrument()
	{
		//if(this._instrument == null) this._instrument = this._settingsManager.getInstrument(this._instrumentId);
		return this._instrument;
	}

	public Account get_Account()
	{
		return this._account;
	}

	public HashMap<Guid, Order> get_Orders()
	{
		//outer fill them
		return this._orders;
	}

	public Transaction(TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this(tradingConsole, settingsManager, Phase.Placing);
	}

	public Transaction(TradingConsole tradingConsole, SettingsManager settingsManager, Phase phase)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._orders = new HashMap<Guid, Order> ();
		this.setPhase(phase);
		this._message = "";

		//for Make Order
		this._makeOrderAccount = null;
	}

	//for changing Limit/Stop order to OCO order only
	public Transaction(TradingConsole tradingConsole, SettingsManager settingsManager, Order originalOrder, boolean changeToOCO)
	{
		this(tradingConsole, settingsManager);

		this._id = Guid.newGuid();
		//this._submitorID = this._assigningOrder.get_Transaction().get_SubmitorID();
		this._account = originalOrder.get_Account();
		this._submitorID = originalOrder.get_Transaction().get_SubmitorID();
		this._instrument = originalOrder.get_Transaction().get_Instrument();
		this._assigningOrder = originalOrder;
		this._assigningOrderId = originalOrder.get_Id();
		this._orderType = changeToOCO ? OrderType.OneCancelOther : originalOrder.get_Transaction().get_OrderType();
		this._type = changeToOCO ? TransactionType.OneCancelOther : originalOrder.get_Transaction().get_Type();
	}

	public Transaction(TradingConsole tradingConsole, SettingsManager settingsManager, DataRow dataRow)
	{
		this(tradingConsole, settingsManager);

		this._id = (Guid)dataRow.get_Item("ID");
		this.setValue(dataRow);

		this._account = this._settingsManager.getAccount( (Guid)dataRow.get_Item("AccountID"));
		this._instrumentId = (Guid)dataRow.get_Item("InstrumentID");
		this._instrument = this._settingsManager.getInstrument(this._instrumentId);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._code = (AppToolkit.isDBNull(dataRow.get_Item("Code"))) ? null : (String)dataRow.get_Item("Code");
		this._type = Enum.valueOf(TransactionType.class, ( (Short)dataRow.get_Item("Type")).intValue());
		if(dataRow.get_Table().get_Columns().contains("TransactionSubType"))
		{
			this._subType = Enum.valueOf(TransactionSubType.class, ( (Short)dataRow.get_Item("TransactionSubType")).intValue());
		}
		Phase phase = Enum.valueOf(Phase.class, ( (Short)dataRow.get_Item("Phase")).intValue());
		this.setPhase(phase);
		this._beginTime = (DateTime)dataRow.get_Item("BeginTime");
		this._endTime = (DateTime)dataRow.get_Item("EndTime");
		if(dataRow.get_Table().get_Columns().contains("ExpireType"))
		{
			this._expireType = Enum.valueOf(ExpireType.class, ( (Integer)dataRow.get_Item("ExpireType")).intValue());
		}
		this._submitTime = (DateTime)dataRow.get_Item("SubmitTime");
		this._submitorID = (dataRow.get_Item("SubmitorID") == null) ? Guid.empty : (Guid)dataRow.get_Item("SubmitorID");
		this._executeTime = (AppToolkit.isDBNull(dataRow.get_Item("ExecuteTime"))) ? null : (DateTime)dataRow.get_Item("ExecuteTime");
		this._assigningOrderId = (AppToolkit.isDBNull(dataRow.get_Item("AssigningOrderID"))) ? null :
			(Guid)dataRow.get_Item("AssigningOrderID");
		this._assigningOrder = (AppToolkit.isDBNull(dataRow.get_Item("AssigningOrderID"))) ? null :
			this._tradingConsole.getOrder( (Guid)dataRow.get_Item("AssigningOrderID"));
		this._orderType = Enum.valueOf(OrderType.class, ( (Integer)dataRow.get_Item("OrderType")).intValue());
		Object contractSize = dataRow.get_Item("ContractSize");
		if(contractSize instanceof BigDecimal)
		{
			this._contractSize = (BigDecimal)contractSize;
		}
		else
		{
			this._contractSize = new BigDecimal(contractSize.toString());
		}
	}

	public void setValue(XmlAttributeCollection transactionCollection)
	{
		for (int i = 0; i < transactionCollection.get_Count(); i++)
		{
			String nodeName = transactionCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = transactionCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Code"))
			{
				this._code = nodeValue;
			}
			else if (nodeName.equals("Type"))
			{
				this._type = Enum.valueOf(TransactionType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("SubType"))
			{
				this._subType = Enum.valueOf(TransactionSubType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("Phase"))
			{
				Phase phase = Enum.valueOf(Phase.class, Integer.parseInt(nodeValue));
				this.setPhase(phase);
			}
			else if (nodeName.equals("BeginTime"))
			{
				this._beginTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("EndTime"))
			{
				this._endTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("ExpireType"))
			{
				this._expireType = Enum.valueOf(ExpireType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("SubmitTime"))
			{
				this._submitTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("SubmitorID"))
			{
				this._submitorID = new Guid(nodeValue);
			}
			else if (nodeName.equals("ExecuteTime"))
			{
				this._executeTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("AssigningOrderID"))
			{
				this._assigningOrderId = (AppToolkit.isDBNull(nodeValue) || StringHelper.isNullOrEmpty(nodeValue) ) ? null : new Guid(nodeValue);
				this._assigningOrder = (AppToolkit.isDBNull(nodeValue) || StringHelper.isNullOrEmpty(nodeValue) ) ? null : this._tradingConsole.getOrder(new Guid(nodeValue));
			}
			else if (nodeName.equals("OrderType"))
			{
				this._orderType = Enum.valueOf(OrderType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("ContractSize"))
			{
				this._contractSize = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("AccountID"))
			{
				this._account = this._settingsManager.getAccount(new Guid(nodeValue));
			}
			else if (nodeName.equals("InstrumentID"))
			{
				this._instrumentId = new Guid(nodeValue);
				this._instrument = this._settingsManager.getInstrument(this._instrumentId);
			}
		}
	}

	//Notify customer by email when execute order(s)
	public void notifyCustomerExecuteOrder()
	{
		try
		{
			this._tradingConsole.get_TradingConsoleServer().scheduler.add(this, null, DateTime.get_Now());
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "notifyCustomerExecuteOrder error: " + FrameworkException.getStackTrace(exception));
		}
	}

	public void action(SchedulerEntry schedulerEntry)
	{
		if (!this._settingsManager.get_Customer().get_IsSendOrderMail())
		{
			return;
		}

		String organizationEmail = this._account.get_OrganizationEmail();
		String customerEmail = this._account.get_CustomerEmail();
		String agentEmail = this._account.get_AgentEmail();
		if (!StringHelper.isNullOrEmpty(organizationEmail)
			&& (!StringHelper.isNullOrEmpty(customerEmail) || !StringHelper.isNullOrEmpty(agentEmail)))
		{
			String accountCode = this._account.get_Code();
			String instrumentCode = this._instrument.get_OriginCode();
			String organizationName = this._account.get_OrganizationName();
			int size = this._orders.values().size();
			String[][] arrayNotifyCustomerExecuteOrder = new String[size][15];
			int index = 0;
			for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
			{
				Order order = iterator.next();
				String orderCode = order.get_Code();
				String lot = order.get_LotString();
				String buySell = (order.get_IsBuy()) ? "B" : "S"; //do not change string of "B" and "S"
				String executePriceString = order.get_ExecutePriceString();
				String executeTime = Convert.toString(this._executeTime, "yyyy-MM-dd HH:mm:ss");
				String executeTradeDay = order.get_ExecuteTradeDay();
				String newClose = (order.get_IsOpen()) ? "N" : "C"; //do not change string of "N" and "C"
				//String orderType = OrderType.getCaption(this._orderType);
				String openPrice = order.getRelationsInfoForEmail();

				arrayNotifyCustomerExecuteOrder[index] = new String[]
					{this._id.toString(), order.get_Id().toString(), orderCode, accountCode, instrumentCode,
					lot, buySell, executePriceString, executeTime,executeTradeDay,
					newClose, openPrice,
					organizationName, organizationEmail, customerEmail, agentEmail};
				index++;
			}
			if (size > 0)
			{
				//XmlNode extendXml = this._settingsManager.get_ExtendForEmail();
				String companyCode = this._tradingConsole.get_LoginInformation().get_CompanyName();
				this._tradingConsole.get_TradingConsoleServer().notifyCustomerExecuteOrderForJava(arrayNotifyCustomerExecuteOrder, companyCode);
			}
		}
	}

	public static void place(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement, boolean realtionChanged)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false, false, true, true);
	}

	public static void place(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false, false, true);
	}

	public static void execute(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false, false, false);
	}

	private static boolean isFromExecute2;
	public static void execute2(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		try
		{
			isFromExecute2 = true;
			Transaction.execute(tradingConsole, settingsManager, transactionXmlElement);
		}
		finally
		{
			isFromExecute2 = false;
		}
	}

	public static void cut(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.execute(tradingConsole, settingsManager, transactionXmlElement);
	}

	public static void reset(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false);
	}

	public static void delete(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false);
	}

	public static void assign(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, true);
	}

	//instrument.transactions.process
	public static void assign2(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false);
	}

	public static void merge(TradingConsole tradingConsole, SettingsManager settingsManager, XmlElement transactionXmlElement)
	{
		//Transaction.process(tradingConsole, settingsManager, transactionXmlElement, false);

		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0)
		{
			return;
		}

		//Transaction Process-----------------------------------------------------------------------------------
		Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
		Transaction transaction = tradingConsole.getTransaction(transactionId);
		if (transaction == null)
		{
			transaction = new Transaction(tradingConsole, settingsManager);
			transaction.setValue(transactionCollection);
			tradingConsole.setTransaction(transaction);

			Guid accountId = transaction.get_Account().get_Id();
			Account account = settingsManager.getAccount(accountId);
			account.get_Transactions().put(transactionId, transaction);

			Guid instrumentId = transaction.get_Instrument().get_Id();
			Instrument instrument = settingsManager.getInstrument(instrumentId);
			instrument.get_Transactions().put(transactionId, transaction);
		}
		else
		{
			//transaction.setValue(transactionCollection);
			return;
		}

		XmlNodeList orerNodeList = transactionXmlElement.get_ChildNodes();
		boolean isUnusedTransaction = false;
		//Orders Process-----------------------------------------------------------------------------------
		for (int i = 0, count = orerNodeList.get_Count(); i < count; i++)
		{
			XmlNode orderNode = orerNodeList.item(i);

			XmlAttributeCollection orderCollection = orderNode.get_Attributes();
			Guid orderId = new Guid(orderCollection.get_ItemOf("ID").get_Value());

			//boolean newArriveOrder = false;
			Order order = tradingConsole.getOrder(orderId);
			if (order == null)
			{
				//newArriveOrder = true;

				order = new Order(tradingConsole, settingsManager);
				order.setValue(orderCollection, transaction);
				order.set_IsAssignOrder(false);
				transaction.get_Orders().put(orderId, order);
				tradingConsole.setOrder(order);

				settingsManager.verifyAccountCurrency(order);

				OperateWhichOrderUI operateWhichOrderUI = order.getOperateWhichOrderUI();
				if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
					|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
				{
					tradingConsole.setWorkingOrder(order);
				}
				if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
					|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
				{
					tradingConsole.setOpenOrder(order);
				}

				if (!isUnusedTransaction && operateWhichOrderUI.equals(OperateWhichOrderUI.None))
				{
					isUnusedTransaction = true;
				}

				//Refresh Order UI
				order.add();
			}
			else
			{
				/*
					 order.setValue(orderCollection, transaction);
					 order.set_IsAssignOrder(false);

					 OperateWhichOrderUI operateWhichOrderUI = order.getOperateWhichOrderUI();
					 if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
				 || operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
					 {
				 if (tradingConsole.getOpenOrder(order.get_Id()) == null)
				 {
				  tradingConsole.setOpenOrder(order);
				  //Refresh Order UI
				  order.addOpenOrderList(operateWhichOrderUI);
				 }
					 }

					 //Refresh Order UI
					 order.update();
				 */
				continue;
			}

			//Relation Orders Process-----------------------------------------------------------------------------------
			TradingItem pLTradingItem = TradingItem.create(0.00, 0.00, 0.00, 0.00, 0.00);
			boolean needUpdatePLTradingItem = false;
			//special process for another user(as agent user) make order
			boolean needUpdateRelationOrder = order.get_RelationOrders().size() <= 0;
			BigDecimal closedLotSum = BigDecimal.ZERO;
			XmlNodeList relationOrderNodeList = orderNode.get_ChildNodes();
			for (int j = 0; j < relationOrderNodeList.get_Count(); j++)
			{
				XmlNode relationOrderNode = relationOrderNodeList.item(j);

				BigDecimal closedLot = BigDecimal.ZERO;
				Guid openOrderId = Guid.empty;
				XmlAttributeCollection relationOrderCollection = relationOrderNode.get_Attributes();
				int relationOrderCollectionCount = relationOrderCollection.get_Count();
				String payBackPledge = "", closedPhysicalValue = "";
				if (relationOrderCollectionCount > 0)
				{
					openOrderId = new Guid(relationOrderCollection.get_ItemOf("OpenOrderID").get_Value());
					//if (order.get_PeerOrderIDs().indexOf(openOrderId.toString()) >= 0)
					//{
					//	tradingConsole.traceSource.trace(TraceType.Warning, "Relation Order Disassemble 2 times is not allow!");
					//	continue;
					//}
					//else
					{
						for (int k = 0; k < relationOrderCollectionCount; k++)
						{ //Commission,Levy
							String nodeName = relationOrderCollection.get_ItemOf(k).get_LocalName();
							String nodeValue = relationOrderCollection.get_ItemOf(k).get_Value();
							if (nodeName.equals("ClosedLot"))
							{
								closedLot = AppToolkit.convertStringToBigDecimal(nodeValue);
								closedLotSum = closedLotSum.add(closedLot);
							}
							else if (nodeName.equals("PayBackPledge"))
							{
								payBackPledge = nodeValue;
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("ClosedPhysicalValue"))
							{
								closedPhysicalValue = nodeValue;
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("TradePL"))
							{
								//pLTradingItem.set_Trade(AppToolkit.round(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Trade(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("PhysicalTradePL"))
							{
								//pLTradingItem.set_Trade(AppToolkit.round(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_PhysicalTrade(pLTradingItem.get_PhysicalTrade() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("InterestPL"))
							{
								//pLTradingItem.set_Interest(AppToolkit.round(pLTradingItem.get_Interest() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Interest(pLTradingItem.get_Interest() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("StoragePL"))
							{
								//pLTradingItem.set_Storage(AppToolkit.round(pLTradingItem.get_Storage() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Storage(pLTradingItem.get_Storage() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
						}
						Order openOrder = tradingConsole.getOrder(openOrderId);
						if (needUpdateRelationOrder && openOrder != null && openOrder.get_ExecutePrice() != null)
						{
							RelationOrder relationOrder = new RelationOrder(tradingConsole, settingsManager, openOrder);
							relationOrder.set_LiqLot(closedLot);
							relationOrder.set_PaybackPledge(payBackPledge);
							relationOrder.set_ClosedPhysicalValue(closedPhysicalValue);
							order.get_RelationOrders().put(relationOrder.get_OpenOrderId(), relationOrder);
						}
					}
				}
			}

			//Modified by Michael on 2008-03-18
			if (needUpdatePLTradingItem)
			{
				//maybe has error, if error, please modify V3 together ???????????????????????
				if (!transaction.get_Account().get_IsMultiCurrency())
				{
					CurrencyRate currencyRate = settingsManager.getCurrencyRate(transaction.get_Instrument().get_Currency().get_Id(),
						transaction.get_Account().get_Currency().get_Id());
					pLTradingItem = TradingItem.exchange(pLTradingItem, currencyRate);
				}

				//order.set_PLTradingItem(TradingItem.add(order.get_PLTradingItem(), pLTradingItem));
				order.set_PLTradingItem(pLTradingItem);
			}

			//if (needUpdateRelationOrder)
			{
				order.set_PeerOrderIDs(order.getPeerOrders()[0]);
				order.set_PeerOrderCodes(order.getPeerOrders()[1]);
			}
		}

		if (isUnusedTransaction)
		{
			tradingConsole.removeTransaction(transaction);
		}
		else
		{
			tradingConsole.setInterestRateOrderId(transaction);
		}
	}

	private static void process(TradingConsole tradingConsole, SettingsManager settingsManager,
								XmlElement transactionXmlElement, boolean isAssign)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, isAssign, true, false);
	}

	private static void process(TradingConsole tradingConsole, SettingsManager settingsManager,
								XmlElement transactionXmlElement, boolean isAssign, boolean isRequireCalculateFloat, boolean fromPlace)
	{
		Transaction.process(tradingConsole, settingsManager, transactionXmlElement, isAssign, isRequireCalculateFloat, fromPlace, false);
	}

	private static class SwingSafelyAction implements Runnable
	{
		private TradingConsole _tradingConsole;

		public SwingSafelyAction(TradingConsole tradingConsole)
		{
			this._tradingConsole = tradingConsole;
		}

		public void run()
		{
			this._tradingConsole.get_MainForm().refreshSystem();
		}
	}

	private static HashMap<Guid, BigDecimal> orderIdToMatchedLot = new HashMap<Guid,BigDecimal>();
	//require rewrite?????????????????????????????
	private static void process(TradingConsole tradingConsole, SettingsManager settingsManager,
								XmlElement transactionXmlElement, boolean isAssign, boolean isRequireCalculateFloat, boolean fromPlace, boolean isUpdateTran)
	{
		ArrayList<String> extensions = null;

		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0)
		{
			return;
		}

		Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
		Transaction transaction = tradingConsole.getTransaction(transactionId);
		boolean mathOrderExecutedPartly = false;
		boolean ignoreUpdateOrders = false;
		if (transaction == null)
		{
			if(fromPlace)
			{
				transaction = new Transaction(tradingConsole, settingsManager, Phase.Placed);
			}
			else
			{
				transaction = new Transaction(tradingConsole, settingsManager);
			}
			transaction.setValue(transactionCollection);

			if(!fromPlace
			   && transaction.get_Phase().value() == Phase.Executed.value()
			   && transaction.get_Type().value() == TransactionType.Single.value()
			   && transaction.get_OrderType().value() == OrderType.Limit.value()
			   && transaction.get_SubType().value() == TransactionSubType.Match.value())
			{
				mathOrderExecutedPartly = true;
			}

			if(transaction.get_Instrument() == null)//maybe executed by Riskmonitor, but the instrument is not selected
			{
				SwingUtilities.invokeLater(new SwingSafelyAction(tradingConsole));
				return;
			}

			tradingConsole.setTransaction(transaction);

			Guid accountId = transaction.get_Account().get_Id();
			Account account = settingsManager.getAccount(accountId);
			account.get_Transactions().put(transactionId, transaction);

			Guid instrumentId = transaction.get_Instrument().get_Id();
			Instrument instrument = settingsManager.getInstrument(instrumentId);
			instrument.get_Transactions().put(transactionId, transaction);
		}
		else
		{
			if(fromPlace)
			{
				if(transaction._subType == TransactionSubType.Match
				   && (transaction._phase == Phase.Cancelled || transaction._phase == Phase.Executed))
				{
					//when placing match orde, Place command may arrive after Cancel/Cut Command
					//if the transaction is canceled/executed, ignore Place command
					return;
				}
				ignoreUpdateOrders = true;
				if(transactionCollection.get_ItemOf("Phase") != null)
				{
					String nodeValue = transactionCollection.get_ItemOf("Phase").get_Value();
					Phase phase = Enum.valueOf(Phase.class, Integer.parseInt(nodeValue));
					transaction.setPhase(phase);
				}
				if(transactionCollection.get_ItemOf("SubType") != null)
				{
					String nodeValue = transactionCollection.get_ItemOf("SubType").get_Value();
					TransactionSubType subType = Enum.valueOf(TransactionSubType.class, Integer.parseInt(nodeValue));
					transaction.setSubType(subType);
				}
				else if(transaction.get_Type() == TransactionType.Mapping && !transaction._instrument.isFromBursa())
				{
					transaction.setPhase(Phase.Placed);
				}//when placing match order, PlaceCommand returned may contains the last result after matching
				//the lot maybe reduced, so we only change Phase to Placed here
			}
			else
			{
				transaction.setValue(transactionCollection);
			}
		}

		XmlNodeList orderNodeList = transactionXmlElement.get_ChildNodes();

		//Delete another OCO order
		if(transaction.get_Phase() != Phase.Placing && transaction.get_Phase() != Phase.Placed)
		{
			transaction.cancelOtherOCOOrder(orderNodeList);
		}
		//Order
		BigDecimal assignedLot = BigDecimal.ZERO;
		BigDecimal matchedLot = BigDecimal.ZERO;
		for (int i = 0, count = orderNodeList.get_Count(); i < count; i++)
		{
			XmlNode orderNode = orderNodeList.item(i);

			XmlAttributeCollection orderCollection = orderNode.get_Attributes();
			if(orderCollection.get_ItemOf("Extension") != null)
			{
				if(extensions == null) extensions = new ArrayList<String>();
				extensions.add(orderCollection.get_ItemOf("Extension").get_Value());
			}

			Guid orderId = new Guid(orderCollection.get_ItemOf("ID").get_Value());

			boolean isAddNewOrder = false;
			Order order = tradingConsole.getOrder(orderId);
			if (order == null)
			{
				isAddNewOrder = true;

				//multi-agent use
				int allowFreeAgent = settingsManager.get_Customer().get_AllowFreeAgent();
				if (allowFreeAgent == 1)
				{
					for (int j = 0; j < orderCollection.get_Count(); j++)
					{
						String nodeName = orderCollection.get_ItemOf(j).get_LocalName();
						String nodeValue = orderCollection.get_ItemOf(j).get_Value();
						if (nodeName.equals("LotBalance"))
						{
							if (AppToolkit.convertStringToBigDecimal(nodeValue).compareTo(BigDecimal.ZERO) <= 0)
							{
								continue;
							}
						}
					}
				}

				order = new Order(tradingConsole, settingsManager);
				order.setValue(orderCollection, transaction);
				if(orderIdToMatchedLot.containsKey(order.get_Id()))
				{
					order.changeLotForMatching(order.get_Lot().subtract(orderIdToMatchedLot.get(order.get_Id())));
					orderIdToMatchedLot.remove(order.get_Id());
				}
				order.set_IsAssignOrder(isAssign);

				transaction.get_Orders().put(orderId, order);
				tradingConsole.setOrder(order);

				settingsManager.verifyAccountCurrency(order);

				OperateWhichOrderUI operateWhichOrderUI = order.getOperateWhichOrderUI();
				if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
					|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
				{
					tradingConsole.setWorkingOrder(order);
				}
				if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
					|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
				{
					tradingConsole.setOpenOrder(order);
				}

				XmlNodeList relationOrderNodeList = orderNode.get_ChildNodes();

				///fix bug of Xml///////////
				if(relationOrderNodeList.get_Count() == 0 && !StringHelper.isNullOrEmpty(orderNode.get_OuterXml())
				   && orderNode.get_OuterXml().indexOf("OrderRelation") > 0)
				{
					XmlDocument document = new XmlDocument();
					document.loadXml(orderNode.get_OuterXml());
					orderNode = document.get_DocumentElement();
					relationOrderNodeList = orderNode.get_ChildNodes();
				}
				///fix bug of Xml//////////

				for (int j = 0; j < relationOrderNodeList.get_Count(); j++)
				{
					XmlNode relationOrderNode = relationOrderNodeList.item(j);

					BigDecimal closedLot = BigDecimal.ZERO;
					Guid openOrderId = Guid.empty;
					XmlAttributeCollection relationOrderCollection = relationOrderNode.get_Attributes();
					int relationOrderCollectionCount = relationOrderCollection.get_Count();
					if (relationOrderCollectionCount > 0)
					{
						openOrderId = new Guid(relationOrderCollection.get_ItemOf("OpenOrderID").get_Value());
						closedLot = AppToolkit.convertStringToBigDecimal(relationOrderCollection.get_ItemOf("ClosedLot").get_Value());
						String payBackPledge = relationOrderCollection.get_ItemOf("PayBackPledge") == null ? "" : relationOrderCollection.get_ItemOf("PayBackPledge").get_Value();
						String closedPhysicalValue = relationOrderCollection.get_ItemOf("ClosedPhysicalValue") == null ? "" : relationOrderCollection.get_ItemOf("ClosedPhysicalValue").get_Value();
						Order openOrder = tradingConsole.getOrder(openOrderId);
						if (openOrder != null)
						{
							RelationOrder relationOrder = new RelationOrder(tradingConsole, settingsManager, openOrder);
							relationOrder.set_LiqLot(closedLot);
							relationOrder.set_PaybackPledge(payBackPledge);
							relationOrder.set_ClosedPhysicalValue(closedPhysicalValue);
							order.get_RelationOrders().put(relationOrder.get_OpenOrderId(), relationOrder);
							openOrder.addCloseOrder(order);
						}
					}
				}
				order.set_PeerOrderIDs(order.getPeerOrders()[0]);
				order.set_PeerOrderCodes(order.getPeerOrders()[1]);
				//Refresh Order UI
				order.add(isFromExecute2, false);
			}
			else
			{
				if(ignoreUpdateOrders)
				{
					order.setPhase(transaction.get_Phase());
					String code = orderCollection.get_ItemOf("Code").get_Value();
					order.set_Code(code);
				}
				else
				{
					order.setValue(orderCollection, transaction);
				}
				order.set_IsAssignOrder(isAssign);

				OperateWhichOrderUI operateWhichOrderUI = order.getOperateWhichOrderUI();
				if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
					|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
				{
					if (tradingConsole.getOpenOrder(order.get_Id()) == null)
					{
						tradingConsole.setOpenOrder(order);
						//Refresh Order UI
						order.addOpenOrderList(operateWhichOrderUI);
					}
				}

				//Refresh Order UI
				order.update();
			}

			if(mathOrderExecutedPartly)
			{
				BigDecimal lot = new BigDecimal(orderCollection.get_ItemOf("Lot").get_Value());
				matchedLot = matchedLot.add(lot);
			}

			if(!isUpdateTran && (transaction.get_Phase() == Phase.Placing || transaction.get_Phase() == Phase.Placed)) continue;

			//this.verifyAccountCurrency(order);

			if (isAssign)
			{
				BigDecimal lot = new BigDecimal(orderCollection.get_ItemOf("Lot").get_Value());
				assignedLot = assignedLot.add(lot);
			}

			TradingItem pLTradingItem = TradingItem.create(0.00, 0.00, 0.00, 0.00, 0.00);
			boolean needUpdatePLTradingItem = false;
			/*
			 String peerOrderIDs2 = "";
			 String peerOrderCodes2 = "";
			 */
			//special process for another user(as agent user) make order
			order.get_RelationOrders().clear();

			boolean needUpdateRelationOrder = order.get_RelationOrders().size() <= 0;
			BigDecimal closedLotSum = BigDecimal.ZERO;
			XmlNodeList relationOrderNodeList = orderNode.get_ChildNodes();
			for (int j = 0; j < relationOrderNodeList.get_Count(); j++)
			{
				XmlNode relationOrderNode = relationOrderNodeList.item(j);

				BigDecimal closedLot = BigDecimal.ZERO;
				String payBackPledge = "";
				String closedPhysicalValue = "";
				Guid openOrderId = Guid.empty;
				XmlAttributeCollection relationOrderCollection = relationOrderNode.get_Attributes();
				int relationOrderCollectionCount = relationOrderCollection.get_Count();
				if (relationOrderCollectionCount > 0)
				{
					openOrderId = new Guid(relationOrderCollection.get_ItemOf("OpenOrderID").get_Value());
					//if (order.get_PeerOrderIDs().indexOf(openOrderId.toString()) >= 0)
					//{
					//	tradingConsole.traceSource.trace(TraceType.Warning,"Relation Order Disassemble 2 times is not allow!");
					//	continue;
					//}
					//else
					{
						for (int k = 0; k < relationOrderCollectionCount; k++)
						{ //Commission,Levy
							String nodeName = relationOrderCollection.get_ItemOf(k).get_LocalName();
							String nodeValue = relationOrderCollection.get_ItemOf(k).get_Value();
							//if (nodeName.equals("OpenOrderID"))
							//{
							//	openOrderId = new Guid(nodeValue);
							//}
							//else if (nodeName.equals("ClosedLot"))
							if (nodeName.equals("ClosedLot"))
							{
								closedLot = AppToolkit.convertStringToBigDecimal(nodeValue);
								closedLotSum = closedLotSum.add(closedLot);
							}
							else if(nodeName.equals("PayBackPledge"))
							{
								payBackPledge = nodeValue;
							}
							else if(nodeName.equals("ClosedPhysicalValue"))
							{
								closedPhysicalValue = nodeValue;
							}
							else if (nodeName.equals("TradePL"))
							{
								//pLTradingItem.set_Trade(AppToolkit.round(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Trade(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("PhysicalTradePL"))
							{
								//pLTradingItem.set_Trade(AppToolkit.round(pLTradingItem.get_Trade() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_PhysicalTrade(pLTradingItem.get_PhysicalTrade() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("InterestPL"))
							{
								//pLTradingItem.set_Interest(AppToolkit.round(pLTradingItem.get_Interest() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Interest(pLTradingItem.get_Interest() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
							else if (nodeName.equals("StoragePL"))
							{
								//pLTradingItem.set_Storage(AppToolkit.round(pLTradingItem.get_Storage() + Double.valueOf(nodeValue).doubleValue(),order.getDisplayDecimals()));
								pLTradingItem.set_Storage(pLTradingItem.get_Storage() + Double.valueOf(nodeValue).doubleValue());
								needUpdatePLTradingItem = true;
							}
						}
						Order openOrder = tradingConsole.getOrder(openOrderId);
						if (needUpdateRelationOrder && openOrder != null && (openOrder.get_ExecutePrice() != null || isUpdateTran))
						{
							RelationOrder relationOrder = new RelationOrder(tradingConsole, settingsManager, openOrder);
							relationOrder.set_LiqLot(closedLot);
							relationOrder.set_ClosedPhysicalValue(closedPhysicalValue);
							relationOrder.set_PaybackPledge(payBackPledge);
							order.get_RelationOrders().put(relationOrder.get_OpenOrderId(), relationOrder);
							openOrder.addCloseOrder(order);
						}
					}
				}
			}
			if(isUpdateTran) continue;

			//Modified by Michael on 2008-03-18
			if (needUpdatePLTradingItem)
			{
				//maybe has error, if error, please modify V3 together ???????????????????????
				if (!transaction.get_Account().get_IsMultiCurrency())
				{
					CurrencyRate currencyRate = settingsManager.getCurrencyRate(transaction.get_Instrument().get_Currency().get_Id(),
						transaction.get_Account().get_Currency().get_Id());
					pLTradingItem = TradingItem.exchange(pLTradingItem, currencyRate);
				}

				//order.set_PLTradingItem(TradingItem.add(order.get_PLTradingItem(), pLTradingItem));
				order.set_PLTradingItem(pLTradingItem);
			}

			if (needUpdateRelationOrder)
			{
				order.set_PeerOrderIDs(order.getPeerOrders()[0]);
				order.set_PeerOrderCodes(order.getPeerOrders()[1]);
			}

			//String executePriceString = Price.toString(order.get_ExecutePrice());
			if (!isAddNewOrder)
			{
				Phase phase;
				if (orderCollection.get_ItemOf("Phase") == null)
				{
					phase = transaction.get_Phase();
				}
				else
				{
					phase = Enum.valueOf(Phase.class, Integer.parseInt(orderCollection.get_ItemOf("Phase").get_Value()));
				}
				if (phase.equals(Phase.Placed))
				{
					if (closedLotSum.compareTo(BigDecimal.ZERO) > 0)
					{
						BigDecimal lotBalance = order.get_Lot().subtract(closedLotSum);
						if (lotBalance.compareTo(BigDecimal.ZERO) <= 0)
						{
							//Michael......
							//tradingConsole.removeOrder(order);
							tradingConsole.removeOpenOrder(order);
							order.calculateForDeleteOrder();
							Order.removeFromOpenOrderList(order);

							order.set_LotBalance(lotBalance);
							order.update();
						}
						else
						{
							order.set_LotBalance(lotBalance);
							/*Instrument instrument = order.get_Transaction().get_Instrument();
							if (order.get_Transaction().needCalculateSummary())
							{
								if (order.get_IsBuy())
								{
									instrument.subtractBuyLots(closedLotSum);
								}
								else
								{
									instrument.subtractSellLots(closedLotSum);
								}
								tradingConsole.rebindSummary();
							}*/
							//Refresh order UI
							order.update();
						}
					}
				}
				//if (phase.equals(Phase.Executed))
				{
					Order.disassemblePeerOrderIDs(tradingConsole, order);
					//Refresh order UI
					order.update();
				}
			}
			else
			{
				//if (order.get_Phase().equals(Phase.Executed))
				{
					Order.disassemblePeerOrderIDs(tradingConsole, order);
				}

				//will process.............
				if (order.get_Phase().equals(Phase.Placed)) //From RiskMonitor
				{
					order.set_LotBalance(order.get_Lot().subtract(Order.getLotWithPeerOrderIDs(order.get_PeerOrderIDs())));
				}

				//Refresh order UI
				order.update();
			}
		}

		if(transaction.get_Instrument().isFromBursa() && transaction.get_SubType() == TransactionSubType.Amend)
		{
			Order assigningOrder = transaction.get_AssigningOrder();
			if(assigningOrder != null)
			{
				assigningOrder.get_Transaction().setModification(transaction);
			}
		}

		if ((isAssign && assignedLot.compareTo(BigDecimal.ZERO) > 0)
			|| (mathOrderExecutedPartly && matchedLot.compareTo(BigDecimal.ZERO) > 0))
		{
			//Refresh order for assigned
			Order assigningOrder = transaction.get_AssigningOrder();
			if (assigningOrder != null)
			{
				if(!isAssign)//Matching order
				{
					BigDecimal lot = assigningOrder.get_Lot().subtract(matchedLot);
					if (lot.compareTo(BigDecimal.ZERO) <= 0)
					{
						tradingConsole.removeOrder(assigningOrder);
					}
					else
					{
						assigningOrder.changeLotForMatching(lot);
						assigningOrder.update();
					}
				}
				else//Assigning order
				{
					BigDecimal lotBalance = assigningOrder.get_LotBalance().subtract(assignedLot);
					if (lotBalance.compareTo(BigDecimal.ZERO) <= 0)
					{
						tradingConsole.removeOrder(assigningOrder);
					}
					else
					{
						assigningOrder.set_LotBalance(lotBalance);
						assigningOrder.get_Transaction().get_Instrument().reCalculateTradePLFloat();
						assigningOrder.update();
					}
				}
				/*if (assigningOrder.get_IsBuy())
				{
					assigningOrder.get_Transaction().get_Instrument().subtractBuyLots(assignedLot);
				}
				else
				{
					assigningOrder.get_Transaction().get_Instrument().subtractSellLots(assignedLot);
				}
				tradingConsole.rebindSummary();*/
			}
			else if(!isAssign)//Matching order and the order is not in memory, cache matched lot
			{
				orderIdToMatchedLot.put(transaction.get_AssigningOrderId(), matchedLot);
			}
		}
		if(fromPlace && transaction._subType == TransactionSubType.IfDone
		   && transaction._assigningOrder != null && transaction._assigningOrder.get_IsOpen())
		{//must be done-order changed to OCO, in this case, transaction._assigningOrder is the if-order
			if(transaction._assigningOrder.get_CloseOrders().getRowCount() > 0)
			{
				Order closeOrder = ( (CloseOrder)transaction._assigningOrder.get_CloseOrders().getObject(0)).get_Order();
				if (closeOrder.get_Transaction() != transaction && closeOrder.get_Transaction().get_Type() != TransactionType.OneCancelOther)
				{
					tradingConsole.removeOrder(closeOrder);
				}
			}
		}

		if (isRequireCalculateFloat)
		{
			Guid instrumentId = new Guid(transactionCollection.get_ItemOf("InstrumentID").get_Value());
			settingsManager.getInstrument(instrumentId).reCalculateTradePLFloat();
		}

		tradingConsole.setInterestRateOrderId(transaction);
		if(extensions != null)
		{
			for(String extension : extensions)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "Transaction.process: extension = " + extension);

				XmlElement extentTransactionXmlElement = Transaction.fixExtensionToXmlElement(extension);
				Transaction.process(tradingConsole, settingsManager, extentTransactionXmlElement,
									isAssign, isRequireCalculateFloat, fromPlace, true);
			}
		}
	}

	private Transaction _modificationTransaction = null;
	public void setModification(Transaction transaction)
	{
		this._modificationTransaction = transaction;
		for(Order order : this.get_Orders().values())
		{
			order.update();
		}
	}

	public boolean isInModification()
	{
		return this._modificationTransaction != null &&
			(this.get_Phase() == Phase.Placing || this.get_Phase() == Phase.Placed) &&
			(this._modificationTransaction.get_Phase() == Phase.Placing || this._modificationTransaction.get_Phase() == Phase.Placed);
	}

	public Guid get_AssigningOrderId()
	{
		return this._assigningOrderId;
	}

	private static String transactionId = "Transaction ID";
	private static String transactionCode = "Code";
	private static String instrumentID = "InstrumentID";
	private static String accountID = "AccountID";
	private static String type = "Type";
	private static String subType = "SubType";
	private static String phase = "Phase";
	private static String orderType = "OrderType";
	private static String beginTime = "BeginTime";
	private static String endTime = "EndTime";
	private static String expireType = "ExpireType";
	private static String submitTime = "SubmitTime";
	private static String submitorID = "SubmitorID";
	private static String assigningOrderID = "AssigningOrderID";

	private static String orderId = "Order ID";
	private static String tradeOption = "TradeOption";
	private static String isOpen = "IsOpen";
	private static String isBuy = "IsBuy";
	private static String setPrice = "SetPrice";
	private static String lot = "Lot";
	private static String originalLot = "OriginalLot";
	private static String lotBalance = "LotBalance";

	private static String openOrderID = "OpenOrderID";
	private static String closedLot = "ClosedLot";
	private static String physicalValue = "PhysicalValue";
	private static String payBackPledge = "PayBackPledge";

	private static XmlElement fixExtensionToXmlElement(String extension)
	{
		XmlDocument document = new XmlDocument();
		XmlElement transaction = document.createElement("Transaction");

		int index = extension.indexOf(Transaction.transactionId) + Transaction.transactionId.length() + 1;
		int index2 = extension.indexOf(" ", index);
		String transactionId = extension.substring(index, index2);
		transaction.setAttribute("ID", transactionId);

		index = extension.indexOf(Transaction.transactionCode) + Transaction.transactionCode.length() + 1;
		index2 = extension.indexOf(" ", index);
		String transactionCode = extension.substring(index, index2);
		transaction.setAttribute(Transaction.transactionCode, transactionCode);

		index = extension.indexOf(Transaction.instrumentID) + Transaction.instrumentID.length() + 1;
		index2 = extension.indexOf(" ", index);
		String instrumentID = extension.substring(index, index2);
		transaction.setAttribute(Transaction.instrumentID, instrumentID);

		index = extension.indexOf(Transaction.accountID) + Transaction.accountID.length() + 1;
		index2 = extension.indexOf(" ", index);
		String accountID = extension.substring(index, index2);
		transaction.setAttribute(Transaction.accountID, accountID);

		index = extension.indexOf(Transaction.type) + Transaction.type.length() + 1;
		index2 = extension.indexOf(" ", index);
		String type = extension.substring(index, index2);
		transaction.setAttribute(Transaction.type, type);

		index = extension.indexOf(Transaction.subType) + Transaction.subType.length() + 1;
		index2 = extension.indexOf(" ", index);
		String subType = extension.substring(index, index2);
		transaction.setAttribute(Transaction.subType, subType);

		index = extension.indexOf(Transaction.assigningOrderID) + Transaction.assigningOrderID.length() + 1;
		index2 = extension.indexOf(" ", index);
		String assigningOrderId = extension.substring(index, index2);
		transaction.setAttribute(Transaction.assigningOrderID, assigningOrderId);

		index = extension.indexOf(Transaction.phase) + Transaction.phase.length() + 1;
		index2 = extension.indexOf(" ", index);
		String phase = extension.substring(index, index2);
		transaction.setAttribute(Transaction.phase, phase);

		index = extension.indexOf(Transaction.orderType) + Transaction.orderType.length() + 1;
		index2 = extension.indexOf(" ", index);
		String orderType = extension.substring(index, index2);
		transaction.setAttribute(Transaction.orderType, orderType);

		index = extension.indexOf(Transaction.beginTime) + Transaction.beginTime.length() + 1;
		index2 = index + 19;
		String beginTime = extension.substring(index, index2);
		transaction.setAttribute(Transaction.beginTime, beginTime);

		index = extension.indexOf(Transaction.endTime) + Transaction.endTime.length() + 1;
		index2 = index + 19;
		String endTime = extension.substring(index, index2);
		transaction.setAttribute(Transaction.endTime, endTime);

		index = extension.indexOf(Transaction.submitTime) + Transaction.submitTime.length() + 1;
		index2 = index + 19;
		String submitTime = extension.substring(index, index2);
		transaction.setAttribute(Transaction.submitTime, submitTime);

		index = extension.indexOf(Transaction.expireType) + Transaction.expireType.length() + 1;
		index2 = extension.indexOf(" ", index);
		String expireType = extension.substring(index, index2);
		transaction.setAttribute(Transaction.expireType, expireType);

		index = extension.indexOf(Transaction.submitorID) + Transaction.submitorID.length() + 1;
		index2 = index + 36;
		String submitorID = extension.substring(index, index2);
		transaction.setAttribute(Transaction.submitorID, submitorID);

		XmlElement order = document.createElement("Order");
		transaction.appendChild(order);

		index = extension.indexOf(Transaction.orderId) + Transaction.orderId.length() + 1;
		index2 = extension.indexOf(" ", index);
		int orderStartIndex = index;
		String orderId = extension.substring(index, index2);
		order.setAttribute("ID", orderId);

		index = extension.indexOf(Transaction.tradeOption) + Transaction.tradeOption.length() + 1;
		index2 = extension.indexOf(" ", index);
		String tradeOption = extension.substring(index, index2);
		order.setAttribute(Transaction.tradeOption, tradeOption);

		index = extension.indexOf(Transaction.isOpen) + Transaction.isOpen.length() + 1;
		index2 = extension.indexOf(" ", index);
		String isOpen = extension.substring(index, index2);
		order.setAttribute(Transaction.isOpen, isOpen);

		index = extension.indexOf(Transaction.isBuy) + Transaction.isBuy.length() + 1;
		index2 = extension.indexOf(" ", index);
		String isBuy = extension.substring(index, index2);
		order.setAttribute(Transaction.isBuy, isBuy);

		index = extension.indexOf(Transaction.setPrice) + Transaction.setPrice.length() + 1;
		index2 = extension.indexOf(" ", index);
		String setPrice = extension.substring(index, index2);
		order.setAttribute(Transaction.setPrice, setPrice);

		index = extension.indexOf(Transaction.lot) + Transaction.lot.length() + 1;
		index2 = extension.indexOf(" ", index);
		String lot = extension.substring(index, index2);
		order.setAttribute(Transaction.lot, lot);

		index = extension.indexOf(Transaction.originalLot) + Transaction.originalLot.length() + 1;
		index2 = extension.indexOf(" ", index);
		String originalLot = extension.substring(index, index2);
		order.setAttribute(Transaction.originalLot, originalLot);

		index = extension.indexOf(Transaction.transactionCode, orderStartIndex) + Transaction.transactionCode.length() + 1;
		index2 = extension.indexOf(" ", index);
		String orderCode = extension.substring(index, index2);
		order.setAttribute(Transaction.transactionCode, orderCode);

		index = extension.indexOf(Transaction.lotBalance) + Transaction.lotBalance.length() + 1;
		index2 = extension.indexOf("OrderRelation", index);
		String lotBalance = extension.substring(index, index2);
		order.setAttribute(Transaction.lotBalance, lotBalance);

		XmlElement orderRelation = document.createElement("OrderRelation");
		order.appendChild(orderRelation);

		index = extension.indexOf(Transaction.openOrderID) + Transaction.openOrderID.length() + 1;
		index2 = extension.indexOf(" ", index);
		String openOrderID = extension.substring(index, index2);
		orderRelation.setAttribute(Transaction.openOrderID, openOrderID);

		index = extension.indexOf(Transaction.closedLot) + Transaction.closedLot.length() + 1;
		index2 = extension.indexOf(" ", index);
		String closedLot = extension.substring(index, index2);
		orderRelation.setAttribute(Transaction.closedLot, closedLot);

		index = extension.indexOf(Transaction.physicalValue) + Transaction.physicalValue.length() + 1;
		index2 = extension.indexOf(" ", index);
		String physicalValue = extension.substring(index, index2);
		orderRelation.setAttribute(Transaction.physicalValue, physicalValue);

		index = extension.indexOf(Transaction.payBackPledge) + Transaction.payBackPledge.length() + 1;
		index2 = extension.indexOf("/OrderRelation", index);
		String payBackPledge = extension.substring(index, index2);
		orderRelation.setAttribute(Transaction.payBackPledge, payBackPledge);


		if(extension.indexOf(Transaction.orderId, index2) > 0)
		{
			extension = extension.substring(index2);
			order = document.createElement("Order");
			transaction.appendChild(order);

			index = extension.indexOf(Transaction.orderId) + Transaction.orderId.length() + 1;
			orderStartIndex = index;
			index2 = extension.indexOf(" ", index);
			orderId = extension.substring(index, index2);
			order.setAttribute("ID", orderId);

			index = extension.indexOf(Transaction.transactionCode, orderStartIndex) + Transaction.transactionCode.length() + 1;
			index2 = extension.indexOf(" ", index);
			String orderCode2 = extension.substring(index, index2);
			order.setAttribute(Transaction.transactionCode, orderCode2);

			index = extension.indexOf(Transaction.tradeOption) + Transaction.tradeOption.length() + 1;
			index2 = extension.indexOf(" ", index);
			tradeOption = extension.substring(index, index2);
			order.setAttribute(Transaction.tradeOption, tradeOption);

			index = extension.indexOf(Transaction.isOpen) + Transaction.isOpen.length() + 1;
			index2 = extension.indexOf(" ", index);
			isOpen = extension.substring(index, index2);
			order.setAttribute(Transaction.isOpen, isOpen);

			index = extension.indexOf(Transaction.isBuy) + Transaction.isBuy.length() + 1;
			index2 = extension.indexOf(" ", index);
			isBuy = extension.substring(index, index2);
			order.setAttribute(Transaction.isBuy, isBuy);

			index = extension.indexOf(Transaction.setPrice) + Transaction.setPrice.length() + 1;
			index2 = extension.indexOf(" ", index);
			setPrice = extension.substring(index, index2);
			order.setAttribute(Transaction.setPrice, setPrice);

			index = extension.indexOf(Transaction.lot) + Transaction.lot.length() + 1;
			index2 = extension.indexOf(" ", index);
			lot = extension.substring(index, index2);
			order.setAttribute(Transaction.lot, lot);

			index = extension.indexOf(Transaction.originalLot) + Transaction.originalLot.length() + 1;
			index2 = extension.indexOf(" ", index);
			originalLot = extension.substring(index, index2);
			order.setAttribute(Transaction.originalLot, originalLot);

			index = extension.indexOf(Transaction.lotBalance) + Transaction.lotBalance.length() + 1;
			index2 = extension.indexOf("OrderRelation", index);
			lotBalance = extension.substring(index, index2);
			order.setAttribute(Transaction.lotBalance, lotBalance);

			orderRelation = document.createElement("OrderRelation");
			order.appendChild(orderRelation);

			index = extension.indexOf(Transaction.openOrderID) + Transaction.openOrderID.length() + 1;
			index2 = extension.indexOf(" ", index);
			openOrderID = extension.substring(index, index2);
			orderRelation.setAttribute(Transaction.openOrderID, openOrderID);

			index = extension.indexOf(Transaction.closedLot) + Transaction.closedLot.length() + 1;
			index2 = extension.indexOf(" ", index);
			closedLot = extension.substring(index, index2);
			orderRelation.setAttribute(Transaction.closedLot, closedLot);

			index = extension.indexOf(Transaction.physicalValue) + Transaction.physicalValue.length() + 1;
			index2 = extension.indexOf(" ", index);
			physicalValue = extension.substring(index, index2);
			orderRelation.setAttribute(Transaction.physicalValue, physicalValue);

			index = extension.indexOf(Transaction.payBackPledge) + Transaction.payBackPledge.length() + 1;
			index2 = extension.indexOf("/OrderRelation", index);
			payBackPledge = extension.substring(index, index2);
			orderRelation.setAttribute(Transaction.payBackPledge, payBackPledge);
		}

		return transaction;
	}

	public void rejectCancelLmtOrder(TradingConsole tradingConsole, String message)
	{
		String orderCodes = "";
		this._message = message;
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.rejectCancelLmtOrder("");

			String code = (StringHelper.isNullOrEmpty(order.get_Code())) ? this._code : order.get_Code();
			orderCodes = ( (!StringHelper.isNullOrEmpty(orderCodes)) ? orderCodes + "," : "") + code;
		}

		//notify user
		tradingConsole.messageNotify(orderCodes + ": " + message, false);

		this._tradingConsole.saveLog(LogCode.RejectCancelLmtOrder, orderCodes + ": " + message, this._id, this.get_Account().get_Id());
	}

	public void setMessage(String message)
	{
		this._message = message;
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.setMessage(message);
		}
	}

	private void handlePlaceResult(String tranCode)
	{
		this._code = tranCode;

		//this._phase = Phase.Placed;
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.updateTranCode();
		}

		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.refrshCloseOrders();
		}

		if (this._orderType == OrderType.Limit && this._assigningOrder != null) //must be changing Limit/Stop order to OCO order
		{
			if(this._phase.equals(Phase.Placed))
			{
				Order.removeFromWorkingOrderList(this._assigningOrder);
			}
			//this._tradingConsole.removeOrder(this._assigningOrder);
		}
	}

	public void acceptPlace(String message, boolean isChangePhase)
	{
		this._message = message;
		if (isChangePhase)
		{
			this.setPhase(Phase.Placed);
		}
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.acceptPlace(message);
		}
	}

	public void cancel(String message, boolean isRejectDQByDealer)
	{
		this.cancel(message, isRejectDQByDealer, false);
	}

	private void cancel(String message, boolean isRejectDQByDealer, boolean removeAssignOrder)
	{
		this._message = message;
		this.setPhase(Phase.Cancelled);
		ArrayList<Order> shouldRemovedOrders = new ArrayList<Order>();
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.cancel(message, isRejectDQByDealer);
			if(message.equalsIgnoreCase(Language.SplittedForHasShortSell)
				|| message.equalsIgnoreCase(Language.AdjustedToFullPaidOrderForHasShortSell))
			{
				shouldRemovedOrders.add(order);
			}
			if (removeAssignOrder && order.get_IsAssignOrder())
			{
				shouldRemovedOrders.add(order);
			}
		}

		for (Order order : shouldRemovedOrders)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "to remove " + order.toString());
			this._tradingConsole.removeOrder(order);
		}

		if (this._orderType == OrderType.Limit && this._assigningOrder != null) //must be changing Limit/Stop order to OCO order
		{
			this._assigningOrder.addWorkingOrder(OperateWhichOrderUI.WorkingOrderList);
		}
	}

	public void cancelOtherOCOOrder(XmlNodeList orderNodeList)
	{
		if (this._type == TransactionType.OneCancelOther)
		{
			for (int i = 0, count = orderNodeList.get_Count(); i < count; i++)
			{
				XmlNode orderNode = orderNodeList.item(i);
				if (orderNode.get_Attributes().get_Count() > 0)
				{
					Guid executedOCOOrderId = new Guid(orderNode.get_Attributes().get_ItemOf("ID").get_Value());
					boolean isFirstOrder = true;
					boolean isLastOrder = false;
					int index = 0;
					for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
					{
						isLastOrder = index == this._orders.size() - 1;
						Order order = iterator.next();
						index++;
						if (!order.get_Id().equals(executedOCOOrderId))
						{ //flag order's phase = cancelled, but transaction's phase = executed
							boolean isNeedSaveLog = (order.get_Phase()!=Phase.Cancelled);
							order.cancelOtherOCOOrder();

							if (isNeedSaveLog)
							{
								this._tradingConsole.saveLog(LogCode.Cancelled,order.getLogAction(isFirstOrder, isLastOrder), order.get_Transaction().get_Id(), this.get_Account().get_Id());
								isFirstOrder = false;
							}
							return;
						}
					}
				}
			}
		}
	}

	public boolean getIsMustAssignOrder()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		DateTime executeTime = this._executeTime;
		if (executeTime == null)
		{
			return false;
		}
		DateTime beginTime = this._settingsManager.get_TradeDay().get_BeginTime();
		DateTime endTime = this._settingsManager.get_TradeDay().get_EndTime();

		if ( (appTime.before(beginTime) && !endTime.before(executeTime))
			|| (!appTime.before(beginTime) && !beginTime.before(executeTime)))
		{
			for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
			{
				Order order = iterator.next();
				if (order.getIsMustAssignOrder())
				{
					return true;
				}
			}
		}
		return false;
	}

	//for cancellation
	public String getOrderCodes()
	{
		String orderCodes = "";
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			String code = (StringHelper.isNullOrEmpty(order.get_Code())) ? this._code : order.get_Code();
			orderCodes = ( (!StringHelper.isNullOrEmpty(orderCodes)) ? orderCodes + "," : "") + code;
		}
		return orderCodes;
	}

	public static Object[] isAllowCancelLMTOrder(SettingsManager settingsManager, Order order)
	{
		Object[] result = new Object[]
			{true, ""}; //isAllowCancel,message

		Transaction transaction = order.get_Transaction();
		OrderType orderType = transaction.get_OrderType();
		Instrument instrument = transaction.get_Instrument();

		/*if(orderType.value() == OrderType.Limit.value()
			&& (instrument.get_OrderTypeMask() & OrderTypeMask.Limit.value()) != OrderTypeMask.Limit.value())
		{
			result[0] = false;
			result[1] = Language.DisallowTradePrompt;
			return result;
		}*/

		if(!order.get_IsOpen())
		{
			Order openOrder = order.getFirstOpenOrder();
			if(openOrder != null && openOrder.get_Phase() != Phase.Executed)
			{//Done-order and the if-order not executed
				return result;
			}
		}

		if (settingsManager.get_Customer().get_DisallowTrade()
			|| !order.get_Account().getIsAllowTrade())
		{
			result[0] = false;
			result[1] = Language.DisallowTradePrompt;
			return result;
		}

		if ( (! (transaction.get_Phase() == Phase.Placing || transaction.get_Phase() == Phase.Placed))
			|| StringHelper.isNullOrEmpty(transaction.get_Code()))
		//|| transaction.get_Message().equals(Language.WebServiceCancelLMTOrderMessage1))
		{
			result[0] = false;
			return result;
		}
		if(orderType == OrderType.StopLimit)
		{
			Object[] result2 = MakeLimitOrder.isAllowTime(settingsManager, instrument, transaction.get_Account());
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}

		}
		else if (orderType == OrderType.Limit)
		{
			Object[] result2 = MakeLimitOrder.isAllowTime(settingsManager, instrument, transaction.get_Account());
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}

			if (transaction.get_SubType().value() != TransactionSubType.Match.value()
				&& Order.checkLMTOrderSetPrice(order) == SetPriceError.SetPriceTooCloseMarket)
			{
				result[0] = false;
				result[1] = Language.TradeConsoleCancelLMTOrderAlert1;
				return result;
			}
		}
		else if (orderType == OrderType.MarketOnOpen)
		{
			Object[] result2 = MakeMarketOnOpenOrder.isAllowTime(settingsManager, instrument, true);
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}
		}
		else if (orderType == OrderType.MarketOnClose)
		{
			Object[] result2 = MakeMarketOnCloseOrder.isAllowTime(settingsManager, instrument, true);
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}
		}
		else if (orderType == OrderType.OneCancelOther)
		{
			Object[] result2 = MakeOneCancelOtherOrder.isAllowTime(settingsManager, instrument, transaction.get_Account());
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}
		}
		else if(instrument.isFromBursa() && orderType == OrderType.Market)
		{
			Object[] result2 = MakeMarketOrder.isAllowTime(instrument);
			if (! (Boolean)result2[0])
			{
				result[0] = false;
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.TradeConsoleCancelLMTOrderAlert0;
				return result;
			}
		}
		else
		{
			result[0] = false;
		}
		return result;
	}

	private static DataTable dataTable = null;
	public static DataTable createStructure()
	{
		if(Transaction.dataTable == null)
		{
			Transaction.dataTable = new DataTable("Transaction");
			Transaction.dataTable.get_Columns().add("ID", Guid.class);
			Transaction.dataTable.get_Columns().add("AccountID", Guid.class);
			Transaction.dataTable.get_Columns().add("InstrumentID", Guid.class);
			Transaction.dataTable.get_Columns().add("Code", String.class);
			Transaction.dataTable.get_Columns().add("Type", Short.class);
			Transaction.dataTable.get_Columns().add("Phase", Short.class);
			Transaction.dataTable.get_Columns().add("BeginTime", DateTime.class);
			Transaction.dataTable.get_Columns().add("EndTime", DateTime.class);
			Transaction.dataTable.get_Columns().add("ExpireType", Integer.class);
			Transaction.dataTable.get_Columns().add("SubmitTime", DateTime.class);
			Transaction.dataTable.get_Columns().add("ExecuteTime", DateTime.class);
			Transaction.dataTable.get_Columns().add("SubmitorID", Guid.class);
			Transaction.dataTable.get_Columns().add("AssigningOrderID", Guid.class);
			Transaction.dataTable.get_Columns().add("OrderType", Integer.class);
			Transaction.dataTable.get_Columns().add("ContractSize", BigDecimal.class);
			Transaction.dataTable.get_Columns().add("OperateType", Integer.class);
		}
		return Transaction.dataTable;
	}

	public boolean isAgentTransaction()
	{
		if (this._phase == Phase.Executed)
		{
			for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
			{
				Order order = iterator.next();
				if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
				{
					return true;
				}
			}
		}
		else if (this._phase == Phase.Placing || this._phase == Phase.Placed)
		{
			return true;
		}

		return false;
	}

	private static TimeSpan SuspiciousTimeSpan = TimeSpan.fromSeconds(60);
	public boolean isSuspicious(DateTime appTime)
	{
		return ( (this._phase == Phase.Placing || this._phase == Phase.Placed) && this._endTime.before(appTime))
			|| (this._phase == Phase.Placing && appTime.substract(this._beginTime).compareTo(SuspiciousTimeSpan) > 0 );
	}

	public boolean needCalculateSummary()
	{
		return!this._settingsManager.get_Customer().get_IsNoShowAccountStatus()
			&& this._account.get_CanDisplay()
			&& this.get_Instrument().get_Select();

	}

	public void changeToOcoOrder(DateTime beginTime, DateTime endTime, DateTime submitTime)
	{
		DateTime dt = DateTime.get_Now();
		TimeSpan timeSpan;
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: CreateDoOrderXml_Begin: " + dt.toString("HH:mm:ss.fff"));

		this._beginTime = beginTime;
		this._endTime = endTime;
		this._submitTime = submitTime;

		this._tradingConsole.setTransaction(this);
		this._account.get_Transactions().put(this._id, this);
		this._instrument.get_Transactions().put(this._id, this);
		for (Iterator<Order> iterator = this._orders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.initInstalmentInfo();
			order.makeOrderConfirm();
		}

		this._subType = TransactionSubType.Amend;
		String xml = this.getChangeToOcoOrderConfirmXml();
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		XmlNode xmlTransaction = xmlDocument.get_DocumentElement();

		timeSpan = DateTime.substract(DateTime.get_Now(), dt);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: CreateDoOrderXml_End: " + timeSpan.toString());
		dt = DateTime.get_Now();
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: saveLog(Placing)_Begin: " + dt.toString("HH:mm:ss.fff"));
		this.saveLog(LogCode.Placing);
		timeSpan = DateTime.substract(DateTime.get_Now(), dt);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: saveLog(Placing)_End: " + timeSpan.toString());
		dt = DateTime.get_Now();
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: Place_Begin: " + dt.toString("HH:mm:ss.fff"));
		this.asyncPlace(xmlTransaction);
		timeSpan = DateTime.substract(DateTime.get_Now(), dt);
		TradingConsole.traceSource.trace(TraceType.Information, "confirm.makeOrderConfirm: Place_End: " + timeSpan.toString());
	}

	public void setContractSize(BigDecimal contractSize)
	{
		this._contractSize = contractSize;
	}

	private String _placingLogInfo;
	public void set_PlacingLogInfo(String logInfo)
	{
		//this._placingLogInfo = logInfo;
	}

	public String get_PlacingLogInfo()
	{
		return this._placingLogInfo;
	}

	private PlaceOrderInfo _placeOrderInfo;
	public void set_PlaceOrderInfo(PlaceOrderInfo placeOrderInfo)
	{
		this._placeOrderInfo = placeOrderInfo;
	}

	public void set_IsPlaceMatchOrder(boolean isPlaceMatchOrder)
	{
		this._subType = isPlaceMatchOrder ? TransactionSubType.Match : this._subType;
	}

	private IfDoneInfo _ifDoneInfo;
	public void set_IfDoneInfo(IfDoneInfo ifDoneInfo)
	{
		this._ifDoneInfo = ifDoneInfo;
		this._subType = ifDoneInfo == null ? TransactionSubType.None : TransactionSubType.IfDone;
	}

	public IfDoneInfo get_IfDoneInfo()
	{
		return this._ifDoneInfo;
	}

	private InstalmentInfo _instalmentInfo;
	public void set_InstalmentInfo(InstalmentInfo instalmentInfo)
	{
		this._instalmentInfo = instalmentInfo;
	}

	public InstalmentInfo get_InstalmentInfo()
	{
		return this._instalmentInfo;
	}
}
