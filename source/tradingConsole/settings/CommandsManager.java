package tradingConsole.settings;

import java.math.*;
import java.util.*;

import java.awt.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.Currency;
import tradingConsole.common.*;
import tradingConsole.enumDefine.*;
import tradingConsole.ui.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.language.*;
import org.apache.log4j.Logger;
public class CommandsManager
{
	private Logger logger = Logger.getLogger(CommandsManager.class);
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private int _refreshUiCount = 0;

	public CommandsManager(TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
	}

	public boolean setQuotation(XmlNode quotation)
	{
		boolean caculatePLFloat = false;
		if (quotation == null)
		{
			return false;
		}
		XmlAttributeCollection xmlAttributeCollection = quotation.get_Attributes();
		if (xmlAttributeCollection.get_Count() > 0)
		{
			XmlAttribute originsXmlAttribute = quotation.get_Attributes().get_ItemOf("Origin");
			if (originsXmlAttribute != null)
			{
				caculatePLFloat |= this.setQuotation(originsXmlAttribute.get_InnerText());
			}
			XmlAttribute overridedQsXmlAttribute = quotation.get_Attributes().get_ItemOf("Overrided");
			if (overridedQsXmlAttribute != null)
			{
				caculatePLFloat |= this.setQuotation(overridedQsXmlAttribute.get_InnerText());
			}
		}
		return caculatePLFloat;
	}

	private static final long _maxTimeTolerance = TimeSpan.fromSeconds(20).get_Ticks();
	private static final TimeSpan intervalToResetAppTime = TimeSpan.fromSeconds(10);
	private Thread _resetAppTimeThread = null;
	private DateTime _lastTimestampToResetAppTime = null;
	private boolean setQuotation(String quotations)
	{
		boolean caculatePLFloat = false;
		if (StringHelper.isNullOrEmpty(quotations))
		{
			return false;
		}
		String[] quotationRows = StringHelper.split(quotations, TradingConsole.delimiterRow);

		/*DateTime maxTimestamp = this.getMaxTimestamp(quotationRows);
		if(Math.abs(maxTimestamp.substract(TradingConsoleServer.appTime()).get_Ticks()) > _maxTimeTolerance)
		{
			if(this._resetAppTimeThread == null && (this._lastTimestampToResetAppTime == null
				|| maxTimestamp.substract(this._lastTimestampToResetAppTime).compareTo(CommandsManager.intervalToResetAppTime) > 0))
			{
				this._lastTimestampToResetAppTime = maxTimestamp;

				this._resetAppTimeThread = new Thread(new Runnable()
				{
					public void run()
					{
						TradingConsole.traceSource.trace(TraceType.Information, "Reset apptime for the time in price is far different from apptime.now");
						try
						{
							TradingConsoleServer.appTime.reset();
						}
						catch (Exception ex)
						{
							TradingConsole.traceSource.trace(TraceType.Error, ex);
						}
						finally
						{
							_resetAppTimeThread = null;
						}
					}
				});
				this._resetAppTimeThread.setPriority(Thread.MAX_PRIORITY);
				this._resetAppTimeThread.setDaemon(true);
				this._resetAppTimeThread.start();
			}
		}*/

		for (int i = 0, count = quotationRows.length; i < count; i++)
		{
			String[] quotationCols = StringHelper.split(quotationRows[i], TradingConsole.delimiterCol);
			if(quotationCols.length==1){
				quotationCols=StringHelper.split(quotationRows[i], ' ');
			}

			Guid instrumentId = new Guid(quotationCols[0]);

			Instrument instrument = this._settingsManager.getInstrument(instrumentId);

			if (instrument == null)
			{
				continue;
			}

			//for refresh image,special process......
			if (this._refreshUiCount == 0)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "instrument.setImage");
				instrument.setIcon(Instrument.tradingPanelKey, InstrumentColKey.Bid, Quotation.get_PicDown());

				TradingConsole.traceSource.trace(TraceType.Information, "InstrumentDoLayout");

				TradingConsole.traceSource.trace(TraceType.Information, "instrument.setImage2");
				instrument.setIcon(Instrument.tradingPanelKey, InstrumentColKey.Bid, Quotation.get_PicUp());

				TradingConsole.traceSource.trace(TraceType.Information, "InstrumentDoLayout2");

				TradingConsole.traceSource.trace(TraceType.Information, "instrument.setImage3");
				instrument.setIcon(Instrument.tradingPanelKey, InstrumentColKey.Bid, Quotation.get_PicNotChange());

				TradingConsole.traceSource.trace(TraceType.Information, "InstrumentDoLayout3");
				this._refreshUiCount = 1;
			}

			TradingConsole.traceSource.trace(TraceType.Information, "Begin instrument.setQuotation");
			caculatePLFloat |= instrument.setQuotation(quotationCols, false);
			TradingConsole.traceSource.trace(TraceType.Information, "End instrument.setQuotation");
		}
		return caculatePLFloat;
	}

	private DateTime getMaxTimestamp(String[] quotationRows)
	{
		DateTime maxTimestamp = DateTime.minValue;
		for(String quotationString : quotationRows)
		{
			String[] quotationCols = StringHelper.split(quotationString, TradingConsole.delimiterCol);
			DateTime timestamp = AppToolkit.getDateTime(quotationCols[1]);
			if(timestamp.after(maxTimestamp)) maxTimestamp = timestamp;
		}
		return maxTimestamp;
	}

	private HashMap<Guid, Object> canceledTransactions = new HashMap<Guid, Object>();
	private void cancel(XmlNode xmlNode)
	{
		try
		{
			if (xmlNode == null)
			{
				return;
			}
			//check....
			//XmlElement cancelXmlElement = xmlNode.get_Item("Transaction");

			XmlAttributeCollection cancelCollection = xmlNode.get_Attributes();
			Guid transactionId = new Guid(cancelCollection.get_ItemOf("TransactionID").get_Value());
			String errorCode = cancelCollection.get_ItemOf("ErrorCode").get_Value();
			int cancelType = Integer.parseInt(cancelCollection.get_ItemOf("CancelType").get_Value());
			CancelReason cancelReason = Enum.valueOf(CancelReason.class, cancelType);
			Transaction transaction = this._tradingConsole.getTransaction(transactionId);
			if(transaction == null)
			{
				if(!canceledTransactions.containsKey(transactionId))
				{
					canceledTransactions.put(transactionId, null); //When matching order, Cancel command may arrive before Place command
				}
				return; //Cancel repeated while refresh trader
			}
			if (!errorCode.equals("0"))
			{
				TransactionError error = Enum.valueOf(TransactionError.class, Integer.parseInt(errorCode));
				transaction.setMessage(TransactionError.getCaption(error));
			}
			else
			{
				String message = cancelReason.toMessage();
				boolean isRejectDQByDealer = false;
				transaction.cancel(message, isRejectDQByDealer);
				transaction.saveLog(LogCode.Cancelled, message);
			}
			//this._tradingConsole.get_MainForm().get_OrderTable().doLayout();
		}
		catch (java.lang.Throwable exception)
		{
			this._tradingConsole.messageNotify(Language.TradeConsoleCancelAlert0, false);
		}
	}

	private void getRejectCancelLmtOrder(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}

		//XmlElement rejectCancelLmtOrderXmlElement = xmlNode.get_Item("Transaction");
		XmlAttributeCollection rejectCancelLmtOrderCollection = xmlNode.get_Attributes();
		Guid transactionId = new Guid(rejectCancelLmtOrderCollection.get_ItemOf("TransactionID").get_Value());
		Transaction transaction = this._tradingConsole.getTransaction(transactionId);
		transaction.rejectCancelLmtOrder(this._tradingConsole, TransactionError.getCaption(rejectCancelLmtOrderCollection.get_ItemOf("ErrorCode").get_Value()));

		//this._tradingConsole.get_MainForm().get_OrderTable().doLayout();
	}

	private Guid execute(XmlNode xmlNode)
	{
		Sound.play(Sound.Execute);

		XmlElement transactionXmlElement = xmlNode.get_Item("Transaction");
		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0)
		{
			return Guid.empty;
		}
		Guid accountId = Guid.empty;
		XmlElement accountXmlElement = xmlNode.get_Item("Account");
		if(accountXmlElement != null) accountId = new Guid(accountXmlElement.get_Attributes().get_ItemOf("ID").get_Value());

		Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
		Transaction transaction = this._tradingConsole.getTransaction(transactionId);
		if (transaction == null)
		{
			this.execute2(xmlNode);//must be auto executed or multiple-close transaction
			return accountId;
		}

		Phase oldPhase = transaction.get_Phase();
		if (oldPhase.equals(Phase.Executed)
			|| oldPhase.equals(Phase.Completed)
			|| oldPhase.equals(Phase.Cancelled))
		{
			this._tradingConsole.traceSource.trace(TraceType.Warning,
				"Execute Command-- TransactionId: " + transactionId.toString() + " Old Phase: " + Phase.getCaption(oldPhase));
			if (oldPhase.equals(Phase.Cancelled))
			{
				this._tradingConsole.messageNotify("TransactionServer error!!!", false);
			}
			return accountId;
		}
		if (transactionCollection.get_ItemOf("ErrorCode") != null)
		{
			String errorCode = transactionCollection.get_ItemOf("ErrorCode").get_Value();
			TransactionError error = TransactionError.OK;
			try
			{
				error = Enum.valueOf(TransactionError.class, errorCode);
			}
			catch(Exception e)
			{
				error = Enum.valueOf(TransactionError.class, Integer.parseInt(errorCode));
			}

			if(error.value() != TransactionError.OK.value())
			{
				this._tradingConsole.traceSource.trace(TraceType.Error,
					"Execute Command-- TransactionId: " + transactionId.toString() + " Old Phase: " + Phase.getCaption(oldPhase) + " ErrorCode: " + errorCode);

				transaction.cancel(TransactionError.getCaption(errorCode), false);

				transaction.saveLog(LogCode.Cancelled);
				return accountId;
			}
		}
		Transaction.execute(this._tradingConsole, this._settingsManager, transactionXmlElement);
		Account.execute(this._tradingConsole, this._settingsManager, accountXmlElement);

		transaction.saveLog(LogCode.Confirmed);

		//this._tradingConsole.get_MainForm().orderDoLayout();
		//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();

		transaction.notifyCustomerExecuteOrder();
		return accountId;
	}

	private Guid execute2(XmlNode xmlNode)
	{
		Sound.play(Sound.Execute2);

		XmlElement transactionXmlElement = xmlNode.get_Item("Transaction");
		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0)
		{
			this._tradingConsole.traceSource.trace(TraceType.Warning, "Execute2 Command with no transaction information: " + xmlNode.get_OuterXml());
			return Guid.empty;
		}
		if (transactionCollection.get_ItemOf("ErrorCode") != null)
		{
			this._tradingConsole.traceSource.trace(TraceType.Error, "Execute2 Command with ErrorCode: " + xmlNode.get_OuterXml());
			return Guid.empty;
		}

		Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
		Transaction transaction = this._tradingConsole.getTransaction(transactionId);

		//should be not exists in local
		if (transaction != null)
		{
			Phase oldPhase = transaction.get_Phase();
			if (oldPhase.equals(Phase.Executed)
				|| oldPhase.equals(Phase.Completed)
				|| oldPhase.equals(Phase.Cancelled))
			{
				this._tradingConsole.traceSource.trace(TraceType.Warning,
					"Execute2 Command-- TransactionId: " + transactionId.toString() + " Old Phase: " + Phase.getCaption(oldPhase));
				if (oldPhase.equals(Phase.Cancelled))
				{
					this._tradingConsole.messageNotify("TransactionServer error!!!", false);
				}
				return Guid.empty;
			}
		}

		XmlElement accountXmlElement = xmlNode.get_Item("Account");
		Guid accountId = Guid.empty;
		if(accountXmlElement != null) accountId = new Guid(accountXmlElement.get_Attributes().get_ItemOf("ID").get_Value());

		Transaction.execute2(this._tradingConsole, this._settingsManager, transactionXmlElement);
		Account.execute2(this._tradingConsole, this._settingsManager, accountXmlElement);

		if (transaction == null)
		{
			transaction = this._tradingConsole.getTransaction(transactionId);
		}
		transaction.saveLog(LogCode.Confirmed);

		//this._tradingConsole.get_MainForm().orderDoLayout();
		//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();

		transaction.notifyCustomerExecuteOrder();
		return accountId;
	}

	private void answer(XmlNode xmlNode)
	{
		Sound.play(Sound.Answer);

		if (xmlNode == null)
		{
			return;
		}
		XmlNode answerXmlNode = xmlNode.get_FirstChild();
		Guid instrumentId = new Guid(answerXmlNode.get_Attributes().get_ItemOf("ID").get_Value());
		if (instrumentId == null)
		{
			this._tradingConsole.messageNotify(Language.TradeConsoleAnswerAlert0, false);
			return;
		}
		Instrument instrument = this._settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}
		String[] quotationCols = new String[5];
		quotationCols[0] = instrumentId.toString();
		quotationCols[1] = answerXmlNode.get_Attributes().get_ItemOf("Timestamp").get_Value();
		quotationCols[2] = answerXmlNode.get_Attributes().get_ItemOf("Ask").get_Value();
		quotationCols[3] = answerXmlNode.get_Attributes().get_ItemOf("Bid").get_Value();

		if(answerXmlNode.get_Attributes().get_ItemOf("AnswerLot") != null)
		{
			BigDecimal answerLot = new BigDecimal(answerXmlNode.get_Attributes().get_ItemOf("AnswerLot").get_Value());
			instrument.answer(quotationCols, answerLot);
		}
		else
		{
			instrument.answer(quotationCols, null);
		}
		//this._tradingConsole.get_MainForm().doLayout();
		//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
		//this._tradingConsole.get_MainForm().Instrument2DoLayout(); //get_Instrument2Table().doLayout();
		//this._tradingConsole.get_MainForm().get_OpenOrderTable().doLayout();
		//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();

		//Notify make order UI
		MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(instrument);
		if (makeOrderWindow != null)
		{
			Window quoteWindow = makeOrderWindow.get_QuoteWindow();
			if (quoteWindow instanceof SpotTradeOrderForm)
			{
				( (SpotTradeOrderForm)quoteWindow).quoteArrived();
			}
			else if (quoteWindow instanceof VerificationOrderForm)
			{
				( (VerificationOrderForm)quoteWindow).quoteArrived();
			}
			else if (quoteWindow instanceof MultiDQOrderForm)
			{
				( (MultiDQOrderForm)quoteWindow).quoteArrived();
			}
			else if (quoteWindow instanceof MakeOrderForm)
			{
				( (MakeOrderForm)quoteWindow).quoteArrived2();
			}
			//.................
		}
	}

	private void cancelQuote(XmlNode xmlNode)
	{
		//Sound.play(Sound.Answer);

		if (xmlNode == null)
		{
			return;
		}
		XmlNode answerXmlNode = xmlNode.get_FirstChild();
		Guid instrumentId = new Guid(answerXmlNode.get_Attributes().get_ItemOf("ID").get_Value());
		if (instrumentId == null)
		{
			this._tradingConsole.messageNotify(Language.TradeConsoleAnswerAlert0, false);
			return;
		}
		Instrument instrument = this._settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}
		//Notify make order UI
		MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(instrument);
		if (makeOrderWindow != null)
		{
			Window quoteWindow = makeOrderWindow.get_QuoteWindow();
			if (quoteWindow instanceof MakeOrderForm)
			{
				( (MakeOrderForm)quoteWindow).cancelQuoteArrived();
			}
		}
	}

	private boolean update(XmlNode xmlNode)
	{
		boolean isNeedCalculatePLFloat = false;
		XmlElement modifyXmlElement = xmlNode.get_Item("Modify");
		isNeedCalculatePLFloat |= this.update(modifyXmlElement, "Modify");
		XmlElement deleteXmlElement = xmlNode.get_Item("Delete");
		isNeedCalculatePLFloat |= this.update(deleteXmlElement, "Delete");
		XmlElement addXmlElement = xmlNode.get_Item("Add");
		isNeedCalculatePLFloat |= this.update(addXmlElement, "Add");

		return isNeedCalculatePLFloat;
	}

	private boolean update(XmlElement xmlElement, String updateType)
	{
		if (xmlElement == null)
		{
			return false;
		}

		boolean isNeedCalculatePLFloat = false;

		for (int i = 0; i < xmlElement.get_ChildNodes().get_Count(); i++)
		{
			XmlNode xmlNode = xmlElement.get_ChildNodes().itemOf(i);
			String nodeName = xmlNode.get_LocalName();
			if (nodeName.equals("Instruments"))
			{
				Instrument.updateInstruments(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
				//this._tradingConsole.get_MainForm().orderDoLayout();
				if(updateType.equalsIgnoreCase("Delete") || updateType.equalsIgnoreCase("Add"))
				{
					this._tradingConsole.get_MainForm().changeChartInstrumentList();
				}
				isNeedCalculatePLFloat = updateType.equalsIgnoreCase("Modify");
			}
			else if (nodeName.equals("Instrument"))
			{
				Instrument.updateInstrument(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
				//this._tradingConsole.get_MainForm().orderDoLayout();
				if(updateType.equalsIgnoreCase("Delete") || updateType.equalsIgnoreCase("Add"))
				{
					this._tradingConsole.get_MainForm().changeChartInstrumentList();
				}
				isNeedCalculatePLFloat = updateType.equalsIgnoreCase("Modify");
			}
			else if (nodeName.equals("PrivateDailyQuotation"))
			{
				isNeedCalculatePLFloat |= Instrument.updatePrivateDailyQuotation(this._settingsManager, xmlNode, updateType);
				//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
			}
			else if (nodeName.equals("PublicDailyQuotation"))
			{
				isNeedCalculatePLFloat |= Instrument.updatePublicDailyQuotation(this._settingsManager, xmlNode, updateType);
				//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
			}
			else if (nodeName.equals("Account"))
			{
				Account.updateAccount(this._tradingConsole, this._settingsManager, xmlNode, updateType);
			}
			else if (nodeName.equals("AccountBalance"))
			{
				AccountCurrency.updateAccountBalance(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				this._tradingConsole.calculatePLFloat();
				//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
			}
			else if (nodeName.equals("SystemParameter"))
			{
				boolean oldValue = this._settingsManager.get_SystemParameter().get_UseNightNecessaryWhenBreak();
				SystemParameter.updateSystemParameter(this._settingsManager, xmlNode, updateType);
				boolean newValue = this._settingsManager.get_SystemParameter().get_UseNightNecessaryWhenBreak();
				if(newValue != oldValue)
				{
					Guid[] accounts = new Guid[this._tradingConsole.get_SettingsManager().get_Accounts().size()];
					accounts = this._tradingConsole.get_SettingsManager().get_Accounts().keySet().toArray(accounts);
					XmlNode accountXmlNode = this._tradingConsole.get_TradingConsoleServer().getAccounts(accounts, true);
					CommandsManager.fixData(this._tradingConsole, this._settingsManager, accountXmlNode);
				}
			}
			else if (nodeName.equals("Customer") || nodeName.equals("Employee"))
			{
				Customer.updateCustomer(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				this._tradingConsole.get_MainForm().refreshLoginInformation();
			}
			else if (nodeName.equals("Customers") || nodeName.equals("Employees"))
			{
				Customer.updateCustomers(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				this._tradingConsole.get_MainForm().refreshLoginInformation();
			}
			else if (nodeName.equals("TradePolicyDetail"))
			{
				TradePolicyDetail.updateTradePolicyDetail(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				isNeedCalculatePLFloat = updateType.equalsIgnoreCase("Modify");
			}
			else if(nodeName.equals("DealingPolicyDetail"))
			{
				DealingPolicyDetail.updateDealingPolicyDetail(this._tradingConsole, this._settingsManager, xmlNode, updateType);
			}
			else if(nodeName.equals("DealingPolicyDetails"))
			{
				DealingPolicyDetail.updateDealingPolicyDetails(this._tradingConsole, this._settingsManager, xmlNode, updateType);
			}
			else if (nodeName.equals("QuotePolicyDetail"))
			{
				Instrument.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				QuotePolicyDetail.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, xmlNode, updateType);
				//this._tradingConsole.get_MainForm().InstrumentDoLayout(); //get_InstrumentTable().doLayout();
			}
			else if (nodeName.equals("QuotePolicyDetails"))
			{
				XmlNodeList children = xmlNode.get_ChildNodes();
				for(int index = 0; index < children.get_Count(); index++)
				{
					XmlNode quotePolicyDetailNode = children.item(index);
					XmlAttributeCollection tradePolicyDetailCollection = quotePolicyDetailNode.get_Attributes();
					Guid quotePolicyId = new Guid(tradePolicyDetailCollection.get_ItemOf("QuotePolicyID").get_Value());
					if(quotePolicyId.compareTo(this._settingsManager.get_Customer().get_PublicQuotePolicyId()) == 0)
					{
						Instrument.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, quotePolicyDetailNode, updateType);
						QuotePolicyDetail.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, quotePolicyDetailNode, updateType);
					}
				}
				for(int index = 0; index < children.get_Count(); index++)
				{
					XmlNode quotePolicyDetailNode = children.item(index);
					XmlAttributeCollection tradePolicyDetailCollection = quotePolicyDetailNode.get_Attributes();
					Guid quotePolicyId = new Guid(tradePolicyDetailCollection.get_ItemOf("QuotePolicyID").get_Value());
					if(quotePolicyId.compareTo(this._settingsManager.get_Customer().get_PrivateQuotePolicyId()) == 0)
					{
						Instrument.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, quotePolicyDetailNode, updateType);
						QuotePolicyDetail.updateQuotePolicyDetail(this._tradingConsole, this._settingsManager, quotePolicyDetailNode, updateType);
					}
				}

			}
			else if (nodeName.equals("Currency"))
			{
				Currency.updateCurrency(this._tradingConsole, this._settingsManager, xmlNode, updateType);
			}
			else if (nodeName.equals("CurrencyRate"))
			{
				this.processCurrencyRate(xmlNode, updateType);
			}
			else if (nodeName.equals("InterestRateDetail") || nodeName.equals("InterestRate"))
			{
				if (xmlNode == null)
				{
					continue;
				}
				XmlAttributeCollection interestRateDetailCollection = xmlNode.get_Attributes();
				Guid interestRateId = Guid.empty;
				if (interestRateDetailCollection.get_ItemOf("InterestRateID") != null)
				{
					interestRateId = new Guid(interestRateDetailCollection.get_ItemOf("InterestRateID").get_Value());
				}
				if (interestRateId.equals(Guid.empty))
				{
					if (interestRateDetailCollection.get_ItemOf("ID") != null)
					{
						interestRateId = new Guid(interestRateDetailCollection.get_ItemOf("ID").get_Value());
					}
				}
				if (!interestRateId.equals(Guid.empty))
				{
					this._tradingConsole.getInterestRate(interestRateId);
				}
			}
			else if (nodeName.equals("TradingTime"))
			{
				/*
				  //no use
				 break;
				 if (tradingTimeNode)
				 {
				  var instrumentID = tradingTimeNode.getAttribute("InstrumentID");
				  var beginTimeTemp = tradingTimeNode.getAttribute("BeginTime");
				  beginTimeTemp = convertDateTimeFormat(beginTimeTemp);
				  var beginTimeString = this.getDateTimeString(new DateTime(beginTimeTemp));
				  var id = instrumentID + beginTimeString;
				  if (id)
				  {
				   if (updateType == "Delete")
				   {
				 if (this.TradingTimes.exists(id) == true)
				 {
				  this.TradingTimes.remove(id);
				 }
				   }
				   else if (updateType == "Modify")// || updateType == "Add")
				   {
				 if (!this.TradingTimes.exists(id))
				 {
				  var tradingTime = new TradingTime(id);
				  tradingTime.initializeXml(tradingTimeNode);
				  this.TradingTimes.add(id,tradingTime);

				  this.instruments(instrumentID).TradingTimes.add(id,tradingTime);
				 }
				 else
				 {
				  this.tradingTimes(id).initializeXml(tradingTimeNode);
				 }
				   }
				  }
				 }
				 break;
				 */
			}
		}
		return isNeedCalculatePLFloat;
	}

	private void processCurrencyRate(XmlNode xmlNode, String updateType)
	{
		CurrencyRate currencyRate = CurrencyRate.updateCurrencyRate(this._settingsManager, xmlNode, updateType);
		if(currencyRate != null)
		{
			for (Order order : this._tradingConsole.get_OpenOrders().values())
			{
				order.calculatePLNotValued();
				order.update();
			}

			ArrayList<Guid> affecetedAccounts = new ArrayList<Guid>();
			Guid sourceCurrencyId = currencyRate.get_SourceCurrencyId();
			Guid targetCurrencyId = currencyRate.get_TargetCurrencyId();
			for(Account account : this._settingsManager.get_Accounts().values())
			{
				Guid accountCurrencyId = account.get_Currency().get_Id();
				if(accountCurrencyId.equals(sourceCurrencyId) || accountCurrencyId.equals(targetCurrencyId))
				{
					for (AccountCurrency accountCurrency : account.get_AccountCurrencies().values())
					{
						Guid currencyId = accountCurrency.get_Currency().get_Id();
						if( (!currencyId.equals(accountCurrencyId) || !account.get_IsMultiCurrency())
							&& (currencyId.equals(sourceCurrencyId) || currencyId.equals(targetCurrencyId)))
						{
							affecetedAccounts.add(account.get_Id());
							break;
						}
					}
				}
			}

			if(affecetedAccounts.size() > 0)
			{
				this._tradingConsole.calculatePLFloat();

				/*Guid[] affecetedAccountArray = new Guid[affecetedAccounts.size()];
				affecetedAccountArray = affecetedAccounts.toArray(affecetedAccountArray);
				XmlNode accountXmlNode = this._tradingConsole.get_TradingConsoleServer().getAccounts(affecetedAccountArray, false);
				XmlNodeList accounts = accountXmlNode.get_ChildNodes();
				for(int index = 0; index < accounts.get_Count(); index++)
				{
					XmlNode account = accounts.item(index);
					Account.updateAccount(this._tradingConsole, this._settingsManager, account, "Modify");
				}*/
			}
		}
	}

	private void resetAlertLevel(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}
		XmlNodeList resetAlertLevelNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < resetAlertLevelNodeList.get_Count(); i++)
		{
			XmlNode accountNode = resetAlertLevelNodeList.itemOf(i);
			Guid accountId = new Guid(accountNode.get_Attributes().get_ItemOf("ID").get_Value());
			Account account = this._settingsManager.getAccount(accountId);
			//account.resetAlertLevel();
			AlertLevel alertLevel = Enum.valueOf(AlertLevel.class, Integer.parseInt(accountNode.get_Attributes().get_ItemOf("AlertLevel").get_Value()));
			account.setAlertLevel(alertLevel);
			account.resetCheckArrivedAlertLevel3();
		}
		//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
	}

	private Guid cut(XmlNode xmlNode)
	{
		Sound.play(Sound.Cut);

		if (xmlNode == null)
		{
			return Guid.empty;
		}
		Guid accountId = Guid.empty;
		XmlElement accountXmlElement = xmlNode.get_Item("Account");
		if (accountXmlElement != null)
		{
			XmlAttributeCollection accountCollection = accountXmlElement.get_Attributes();
			accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
			Account account = this._settingsManager.getAccount(accountId);

			//Remarked by Michael on 2008-04-09
			//if (account.get_IsGotCutOrder()) return;
			//account.set_IsGotCutOrder(true);
			account.resetCheckArrivedAlertLevel3();
		}
		Account.cut(this._tradingConsole, this._settingsManager, accountXmlElement);

		XmlNodeList accountCurrencyNodeList = accountXmlElement.get_ChildNodes();
		for (int j = 0, count = accountCurrencyNodeList.get_Count(); j < count; j++)
		{
			XmlElement transactionXmlElement = (XmlElement)accountCurrencyNodeList.item(j);
			if (transactionXmlElement.get_LocalName().equals("Transactions"))
			{
				XmlNodeList transactionXmlNodeList = transactionXmlElement.get_ChildNodes();
				if (transactionXmlNodeList != null)
				{
					for (int k = 0; k < transactionXmlNodeList.get_Count(); k++)
					{
						XmlElement transactionNode = (XmlElement)transactionXmlNodeList.itemOf(k);
						Transaction.cut(this._tradingConsole, this._settingsManager, transactionNode);

						XmlAttributeCollection transactionCollection = transactionNode.get_Attributes();
						if (transactionCollection.get_Count() <= 0)
						{
							break;
						}
						Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
						Transaction transaction = this._tradingConsole.getTransaction(transactionId);
						if (transaction != null)
						{
							transaction.saveLog(LogCode.Confirmed);
						}
					}
				}
			}
		}

		return accountId;

		//this._tradingConsole.get_MainForm().orderDoLayout();
	}

	private Guid delete(XmlNode xmlNode)
	{
		Sound.play(Sound.Delete);

		XmlElement accountXmlElement = xmlNode.get_Item("Account");
		Account.delete(this._tradingConsole, this._settingsManager, accountXmlElement);

		String logAction = "";

		XmlNode affectedOrdersNode = xmlNode.get_Item("AffectedOrders");
		if (affectedOrdersNode != null)
		{
			for (int i = 0; i < affectedOrdersNode.get_ChildNodes().get_Count(); i++)
			{
				XmlElement transactionNode = (XmlElement)affectedOrdersNode.get_ChildNodes().itemOf(i);
				Transaction.delete(this._tradingConsole, this._settingsManager, transactionNode);

				XmlAttributeCollection transactionCollection = transactionNode.get_Attributes();
				if (transactionCollection.get_Count() <= 0)
				{
					break;
				}
				Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
				Transaction transaction = this._tradingConsole.getTransaction(transactionId);
				if (transaction != null)
				{
					logAction = transaction.getLogAction();
					this._tradingConsole.saveLog(LogCode.Affected, logAction, transactionId, transaction.get_Account().get_Id());
				}
			}
		}

		XmlNode deletedOrderNode = xmlNode.get_Item("DeletedOrder");
		if (deletedOrderNode != null)
		{
			Guid orderId = new Guid(deletedOrderNode.get_Attributes().get_ItemOf("ID").get_Value());
			Order deletedOrder = this._tradingConsole.getOrder(orderId);
			if (deletedOrder != null)
			{
				/*Instrument instrument = deletedOrder.get_Transaction().get_Instrument();
				if (deletedOrder.get_Transaction().needCalculateSummary())
				{
					if (deletedOrder.get_IsBuy())
					{
						instrument.subtractBuyLots(deletedOrder.get_LotBalance());
					}
					else
					{
						instrument.subtractSellLots(deletedOrder.get_LotBalance());
					}
					this._tradingConsole.rebindSummary();
				}*/

				//logAction = (!StringHelper.isNullOrEmpty(logAction)) ? logAction + TradingConsole.delimiterRow : "";
				logAction += deletedOrder.getLogActionForRemoveByRisk();
				this._tradingConsole.saveLog(LogCode.Deleted, logAction, deletedOrder.get_Transaction().get_Id(), deletedOrder.get_Transaction().get_Account().get_Id());

				this._tradingConsole.removeOrder(deletedOrder);
			}
		}
		//if require refresh UI ?????????????????????
		Guid accountId = Guid.empty;
		if (accountXmlElement != null)
		{
			accountId = new Guid(accountXmlElement.get_Attributes().get_ItemOf("ID").get_Value());
			Account account = this._settingsManager.getAccount(accountId);
			if (account != null)
			{
				account.updateNode();
			}
		}

		return accountId;

		//this._tradingConsole.get_MainForm().orderDoLayout();
		//this._tradingConsole.saveLog(LogCode.Deleted,logAction);
	}

	private void chat(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}
		Sound.play(Sound.Message);

		XmlAttributeCollection collection = xmlNode.get_Attributes();
		Guid id = new Guid(collection.get_ItemOf("ID").get_Value());
		Message message = this._settingsManager.getMessage(id);
		if (message == null)
		{
			message = new Message();
		}
		message.setValue(collection);

		this._settingsManager.setMessage(message);
		//this._tradingConsole.get_MainForm().refreshMessage();
		this._tradingConsole.rebindMessage();
		MessageContentForm.updateButtonStatusForAll();

		MessageContentForm messageContentForm = new MessageContentForm(this._tradingConsole, this._settingsManager, message);
		messageContentForm.setAlwaysOnTop(true);
		messageContentForm.show();
	}

	private void news(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}
		XmlNodeList nodeList = xmlNode.get_ChildNodes();
		if (nodeList == null || nodeList.get_Count() == 0)
		{
			return;
		}
		for (int i = 0, count = nodeList.get_Count(); i < count; i++)
		{
			XmlNode newsNode = nodeList.item(i);
			XmlAttributeCollection collection = newsNode.get_Attributes();
			Guid id = new Guid(collection.get_ItemOf("Id").get_Value());
			News news = this._settingsManager.getNews(id);
			if (news == null)
			{
				news = new News();
			}
			news.setValue(collection);

			this._settingsManager.setNews(news);
			this._tradingConsole.addNews(news);
			NewsContentForm.updateButtonStatusForAll();
		}
		//this._tradingConsole.rebindNews();
	}

	private void place(XmlNode xmlNode)
	{
		Sound.play(Sound.Message);
		XmlElement transactionXmlElement = xmlNode.get_Item("Transaction");
		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0) return;

		Transaction.place(this._tradingConsole, this._settingsManager, transactionXmlElement);

		XmlElement accountXmlElement = xmlNode.get_Item("Account");
		if(accountXmlElement != null) Account.place(this._tradingConsole, this._settingsManager, accountXmlElement);

		Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
		if(canceledTransactions.containsKey(transactionId))
		{
			canceledTransactions.remove(transactionId); //When matching order, Cancel command may arrive before Place command
			return;
		}

		Transaction transaction = this._tradingConsole.getTransaction(transactionId);
		if (transaction != null)
		{
			transaction.saveLog( (transaction.get_Phase() == Phase.Placing) ? LogCode.Placing : LogCode.Placed);
		}
	}

	private void parameters(XmlNode xmlNode)
	{
		Sound.play(Sound.Message);

		if (xmlNode.get_Attributes().get_Count() > 0)
		{
			DataSet dataSet = new DataSet();
			dataSet.readXml(xmlNode);
			this.getInstrumentsResult(dataSet);
		}
	}

	private void getInstrumentsResult(DataSet dataSet)
	{
		this._tradingConsole.updateData(dataSet);
	}

	private void email(XmlNode xmlNode)
	{
		Sound.play(Sound.Message);
		//????????????????????
	}

	private void redial(XmlNode xmlNode)
	{
		this._tradingConsole.resetSystem(false);
	}

	private void acceptPlace(XmlNode xmlNode)
	{
		try
		{
			if (xmlNode == null)
			{
				return;
			}
			XmlAttributeCollection cancelCollection = xmlNode.get_Attributes();
			Guid transactionId = new Guid(cancelCollection.get_ItemOf("TransactionID").get_Value());
			String errorCode = cancelCollection.get_ItemOf("ErrorCode").get_Value();
			Transaction transaction = this._tradingConsole.getTransaction(transactionId);
			String message = "";
			boolean isChangePhase = false;
			if (!errorCode.equals("0"))
			{
				message = TransactionError.getCaption(errorCode);
			}
			else
			{
				isChangePhase = true;
			}
			transaction.acceptPlace(message, isChangePhase);

			transaction.saveLog( (isChangePhase) ? LogCode.Placed : LogCode.Placing);
		}
		catch (java.lang.Throwable exception)
		{
			this._tradingConsole.messageNotify("acceptPlace--RuntimeError", false);
		}
	}

	/*
	  private void reset(XmlNode xmlNode)
	  {
	 for (int i = 0; i < xmlNode.get_ChildNodes().get_Count(); i++)
	 {
	  XmlNode accountNode = xmlNode.get_ChildNodes().itemOf(i);
	  XmlElement accountXmlElement = accountNode.get_Item("Account");
	  Account.reset(this._tradingConsole, this._settingsManager, accountXmlElement);
	  XmlElement transactionXmlElement = accountNode.get_Item("Transaction");
	  XmlNodeList transactionXmlNodeList = transactionXmlElement.get_ChildNodes();
	  if (transactionXmlNodeList != null)
	  {
	   for (int k = 0; k < transactionXmlNodeList.get_Count(); k++)
	   {
		XmlNode transactionNode = transactionXmlNodeList.itemOf(k);
		Transaction.reset(this._tradingConsole, this._settingsManager, transactionNode.get_Item("Transaction"));
	   }
	  }
	 }
	 this._tradingConsole.get_MainForm().orderDoLayout();
	 this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
	  }
	 */

	//Need Test......
	private void reset(XmlNode xmlNode)
	{
		CommandsManager.fixData(this._tradingConsole, this._settingsManager, xmlNode);
	}

	public static void fixData(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode accountXmlNodes)
	{
		if (accountXmlNodes == null)
		{
			return;
		}

		XmlNodeList accountXmlNodeList = accountXmlNodes.get_ChildNodes();
		for (int i = 0; i < accountXmlNodeList.get_Count(); i++)
		{
			XmlElement accountXmlElement = (XmlElement)accountXmlNodeList.item(i);

			XmlAttributeCollection accountCollection = accountXmlElement.get_Attributes();
			Guid accountId = new Guid(accountCollection.get_ItemOf("ID").get_Value());
			Account account = settingsManager.getAccount(accountId);
			if (account != null)
			{
				//Remove exists Transactions
				XmlNodeList xmlNodeList = accountXmlElement.get_ChildNodes();
				for (int j = 0; j < xmlNodeList.get_Count(); j++)
				{
					XmlNode xmlNode = xmlNodeList.item(j);
					if (xmlNode.get_LocalName().equals("Transactions"))
					{
						XmlNodeList xmlNodeList2 = xmlNode.get_ChildNodes();
						for (int k = 0; k < xmlNodeList2.get_Count(); k++)
						{
							XmlElement transactionXmlElement = (XmlElement)xmlNodeList2.item(k);
							XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();

							Guid transactionId = new Guid(transactionCollection.get_ItemOf("ID").get_Value());
							Transaction transaction = tradingConsole.getTransaction(transactionId);
							if (transaction != null)
							{
								tradingConsole.removeTransaction(transaction);
							}
						}
					}
				}
			}

			Account.merge(tradingConsole, settingsManager, accountXmlElement);
			XmlNodeList xmlNodeList = accountXmlElement.get_ChildNodes();
			for (int j = 0; j < xmlNodeList.get_Count(); j++)
			{
				XmlNode xmlNode = xmlNodeList.item(j);
				if (xmlNode.get_LocalName().equals("Transactions"))
				{
					XmlNodeList xmlNodeList2 = xmlNode.get_ChildNodes();
					for (int k = 0; k < xmlNodeList2.get_Count(); k++)
					{
						XmlElement transactionXmlElement = (XmlElement)xmlNodeList2.item(k);
						Transaction.merge(tradingConsole, settingsManager, transactionXmlElement);
					}
				}
			}
		}
		tradingConsole.getInterestRate();
		tradingConsole.calculatePLFloat();
	}

	//Test
	/*
	  private void test()
	  {
	 this._settingsManager.getAccountsForCut();
	  }
	 */

	private void updateAccounts(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}
		try
		{
			//check....
			//XmlElement alertXmlElement = xmlNode.get_Item("Transaction");
			//for (int i = 0; i < xmlNode.get_ChildNodes().get_Count(); i++)
			{
				XmlNode accountNode = xmlNode;
				XmlAttributeCollection alertCollection = accountNode.get_Attributes();
				Guid accountId = new Guid(alertCollection.get_ItemOf("ID").get_Value());
				Account account = this._settingsManager.getAccount(accountId);
				AlertLevel alertLevel = Enum.valueOf(AlertLevel.class, Integer.parseInt(alertCollection.get_ItemOf("AlertLevel").get_Value()));
				account.setAlertLevel(alertLevel);

				if (alertCollection.get_ItemOf("Balance") != null)
				{
					double balance = Double.parseDouble(alertCollection.get_ItemOf("Balance").get_Value());
					account.set_Balance(balance);
				}
				if (alertCollection.get_ItemOf("Necessary") != null)
				{
					double necessary = Double.parseDouble(alertCollection.get_ItemOf("Necessary").get_Value());
					account.set_Necessary(necessary);
				}
				account.calculateEquity();

				if (alertCollection.get_ItemOf("InterestPLNotValued") != null)
				{
					double interestPLNotValued = Double.parseDouble(alertCollection.get_ItemOf("InterestPLNotValued").get_Value());
					account.get_NotValuedTradingItem().set_Interest(interestPLNotValued);
				}
				if (alertCollection.get_ItemOf("StoragePLNotValued") != null)
				{
					double storagePLNotValued = Double.parseDouble(alertCollection.get_ItemOf("StoragePLNotValued").get_Value());
					account.get_NotValuedTradingItem().set_Storage(storagePLNotValued);
				}
				if (alertCollection.get_ItemOf("TradePLNotValued") != null)
				{
					double tradePLNotValued = Double.parseDouble(alertCollection.get_ItemOf("TradePLNotValued").get_Value());
					account.get_NotValuedTradingItem().set_Trade(tradePLNotValued);
				}

				/*if (alertCollection.get_ItemOf("InterestPLValued") != null)
				{
					double interestPLValued = Double.parseDouble(alertCollection.get_ItemOf("InterestPLValued").get_Value());
					account.get_FloatTradingItem().set_Interest(interestPLValued);
				}
				if (alertCollection.get_ItemOf("StoragePLValued") != null)
				{
					double storagePLValued = Double.parseDouble(alertCollection.get_ItemOf("StoragePLValued").get_Value());
					account.get_FloatTradingItem().set_Storage(storagePLValued);
				}
				if (alertCollection.get_ItemOf("TradePLValued") != null)
				{
					double tradePLValued = Double.parseDouble(alertCollection.get_ItemOf("TradePLValued").get_Value());
					account.get_FloatTradingItem().set_Trade(tradePLValued);
				}
				if (alertCollection.get_ItemOf("Equity") != null)
				{
					double equity = Double.parseDouble(alertCollection.get_ItemOf("Equity").get_Value());
					account.set_Equity(equity);
				}*/

				for (int index = 0; index < accountNode.get_ChildNodes().get_Count(); index++)
				{
					XmlNode currencyNode = accountNode.get_ChildNodes().itemOf(index);
					XmlAttributeCollection currencyCollection = currencyNode.get_Attributes();
					Guid currencyId = new Guid(currencyCollection.get_ItemOf("ID").get_Value());
					CompositeKey2<Guid, Guid> compositeKey = new CompositeKey2<Guid, Guid> (accountId, currencyId);
					AccountCurrency accountCurrency = account.get_AccountCurrencies().get(compositeKey);
					accountCurrency.setValue(currencyCollection);
				}
				account.updateNode();

				//DisplayAlert: 0: none; 1:1; 2:1,2; 3:1,3; 4:1,2,3; 5:2; 6:2,3; 7:3
				int displayAlert = this._settingsManager.get_Customer().get_DisplayAlert();
				if (displayAlert != 0)
				{
					if ( (alertLevel.equals(AlertLevel.AlertLevel1) && (displayAlert == 1 || displayAlert == 2 || displayAlert == 3 || displayAlert == 4))
						|| (alertLevel.equals(AlertLevel.AlertLevel2) && (displayAlert == 2 || displayAlert == 4 || displayAlert == 5 || displayAlert == 6))
						|| (alertLevel.equals(AlertLevel.AlertLevel3) && (displayAlert == 3 || displayAlert == 4 || displayAlert == 6 || displayAlert == 7))
						)
					{
						if(account.get_AlertLevelChanged())
						{
							account.resetAlertLevelChanged();
							this._tradingConsole.accountMarginNotify(account.get_Code());
						}
					}
				}
			}
			//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
		}
		catch (java.lang.Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private void alertAccounts(XmlNode xmlNode)
	{
		if (xmlNode == null)
		{
			return;
		}
		try
		{
			//check....
			//XmlElement alertXmlElement = xmlNode.get_Item("Transaction");
			for (int i = 0; i < xmlNode.get_ChildNodes().get_Count(); i++)
			{
				XmlNode accountNode = xmlNode.get_ChildNodes().itemOf(i);
				XmlAttributeCollection alertCollection = accountNode.get_Attributes();
				Guid accountId = new Guid(alertCollection.get_ItemOf("ID").get_Value());
				Account account = this._settingsManager.getAccount(accountId);
				AlertLevel alertLevel = Enum.valueOf(AlertLevel.class, Integer.parseInt(alertCollection.get_ItemOf("AlertLevel").get_Value()));
				account.setAlertLevel(alertLevel);

				if (alertCollection.get_ItemOf("Balance") != null)
				{
					double balance = Double.parseDouble(alertCollection.get_ItemOf("Balance").get_Value());
					account.set_Balance(balance);
				}
				if (alertCollection.get_ItemOf("Necessary") != null)
				{
					double necessary = Double.parseDouble(alertCollection.get_ItemOf("Necessary").get_Value());
					account.set_Necessary(necessary);
				}

				//since the execution order of alertAccounts is adjusted, the nesessary must be got from the server to make sure it is correct
				XmlNode accountXmlNode = this._tradingConsole.get_TradingConsoleServer().getAccounts(new Guid[]{accountId}, false);
				CommandsManager.fixData(this._tradingConsole, this._settingsManager, accountXmlNode);

				account.calculateEquity();
				/*if (alertCollection.get_ItemOf("Equity") != null)
				{
					double equity = Double.parseDouble(alertCollection.get_ItemOf("Equity").get_Value());
					account.set_Equity(equity);
				}*/
				account.updateNode();

				//DisplayAlert: 0: none; 1:1; 2:1,2; 3:1,3; 4:1,2,3; 5:2; 6:2,3; 7:3
				int displayAlert = this._settingsManager.get_Customer().get_DisplayAlert();
				if (displayAlert != 0)
				{
					if ( (alertLevel.equals(AlertLevel.AlertLevel1) && (displayAlert == 1 || displayAlert == 2 || displayAlert == 3 || displayAlert == 4))
						|| (alertLevel.equals(AlertLevel.AlertLevel2) && (displayAlert == 2 || displayAlert == 4 || displayAlert == 5 || displayAlert == 6))
						|| (alertLevel.equals(AlertLevel.AlertLevel3) && (displayAlert == 3 || displayAlert == 4 || displayAlert == 6 || displayAlert == 7))
						)
					{
						if(account.get_AlertLevelChanged())
						{
							account.resetAlertLevelChanged();
							this._tradingConsole.accountMarginNotify(account.get_Code());
						}
					}
				}

				for (int index = 0; index < accountNode.get_ChildNodes().get_Count(); index++)
				{
					XmlNode orderNode = accountNode.get_ChildNodes().itemOf(i);
					XmlAttributeCollection orderCollection = orderNode.get_Attributes();
					Guid orderId = new Guid(orderCollection.get_ItemOf("ID").get_Value());
					Order order = this._tradingConsole.getOrder(orderId);
					order.setValue(orderCollection, order.get_Transaction());
				}
			}
			//this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
		}
		catch (java.lang.Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	public void processCommands(XmlNode commandNode)
	{
		boolean isNeedGetInterestRateFromServer = false;
		boolean isNeedCalculatePLFloat = false;
		boolean needRefreshSummary = false;

		XmlNode alertAccountsXmlNode = null;
		ArrayList<Guid> accountIds = new ArrayList<Guid>();
		this._tradingConsole.suspendRefreshSummary();
		for (int i = 0, count = commandNode.get_ChildNodes().get_Count(); i < count; i++)
		{
			XmlNode xmlNode = commandNode.get_ChildNodes().item(i);
			String localName = xmlNode.get_LocalName();
			if (localName.equalsIgnoreCase("Quotation"))
			{
				//this.test();
				//this.logger.debug("process quotaion begin");
				isNeedCalculatePLFloat = this.setQuotation(xmlNode);
				//this.logger.debug("process quotaion end");
			}
			else if(localName.equalsIgnoreCase("ChartDatas"))
			{
				this.fixChartData(xmlNode);
			}
			else if(localName.equals("Account"))
			{
				this.updateAccounts(xmlNode);
			}
			else if (localName.equals("Answer"))
			{
				this.answer(xmlNode);
				//isNeedCalculatePLFloat = true;
			}
			else if (localName.equalsIgnoreCase("CancelQuote2"))
			{
				this.cancelQuote(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Execute"))
			{
				Guid accountId = this.execute(xmlNode);
				if(!accountId.equals(Guid.empty) && !accountIds.contains(accountId)) accountIds.add(accountId);
				isNeedGetInterestRateFromServer = true;
				isNeedCalculatePLFloat = true;
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("Execute2") || localName.equalsIgnoreCase("Assign"))
			{
				Guid accountId = this.execute2(xmlNode);
				if(!accountId.equals(Guid.empty) && !accountIds.contains(accountId)) accountIds.add(accountId);
				isNeedGetInterestRateFromServer = true;
				isNeedCalculatePLFloat = true;
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("AlertAccounts"))
			{
				alertAccountsXmlNode = xmlNode;
			}
			else if (localName.equalsIgnoreCase("Cancel"))
			{
				this.cancel(xmlNode);
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("RejectCancelLmtOrder"))
			{
				this.getRejectCancelLmtOrder(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Place"))
			{
				this.place(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Cut"))
			{
				if(alertAccountsXmlNode != null)
				{
					this.alertAccounts(alertAccountsXmlNode);
					alertAccountsXmlNode = null;
				}

				Guid accountId = this.cut(xmlNode);
				if(!accountId.equals(Guid.empty) && !accountIds.contains(accountId)) accountIds.add(accountId);
				isNeedGetInterestRateFromServer = true;
				isNeedCalculatePLFloat = true;
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("Delete"))
			{
				Guid accountId = this.delete(xmlNode);
				if(!accountId.equals(Guid.empty) && !accountIds.contains(accountId)) accountIds.add(accountId);
				isNeedGetInterestRateFromServer = true;
				isNeedCalculatePLFloat = true;
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("Update"))
			{
				isNeedCalculatePLFloat = this.update(xmlNode);
			}
			else if (localName.equalsIgnoreCase("ResetAlertLevel"))
			{
				this.resetAlertLevel(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Reset"))
			{
				this.reset(xmlNode);
				isNeedGetInterestRateFromServer = true;
				isNeedCalculatePLFloat = true;
				needRefreshSummary = true;
			}
			else if (localName.equalsIgnoreCase("Chat"))
			{
				this.chat(xmlNode);
			}
			else if (localName.equalsIgnoreCase("News"))
			{
				this.news(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Parameters"))
			{
				this.parameters(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Email"))
			{
				this.email(xmlNode);
			}
			else if (localName.equalsIgnoreCase("AcceptPlace"))
			{
				this.acceptPlace(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Redial"))
			{
				this.redial(xmlNode);
			}
			else if (localName.equalsIgnoreCase("AsyncCommand"))
			{
				this.handleAsyncCommand(xmlNode);
			}
			else if (localName.equalsIgnoreCase("Orders"))
			{
				this.handleAccountUpdateCommand(xmlNode);
			}
			else if(localName.equalsIgnoreCase("BestLimits"))
			{
				this.handleBestLimitsCommand(xmlNode);
			}
			else if(localName.equalsIgnoreCase("MatchInfo"))
			{
				this.handleMatchInfo(xmlNode);
			}
			else if(localName.equalsIgnoreCase("ApplyOrderModification"))
			{
				this.applyOrderModification(xmlNode);
			}
			else if(localName.equalsIgnoreCase("UpdateTransaction"))
			{
				this.updateTransaction(xmlNode);
			}
			else if(localName.equalsIgnoreCase("InstrumentGroupStateChange"))
			{
				this._tradingConsole.get_InstrumentStateManager().addInstrumentGroupStateChangeCommand(xmlNode);
			}
			else if(localName.equalsIgnoreCase("InstrumentStateChange"))
			{
				this._tradingConsole.get_InstrumentStateManager().addInstrumentStateChangeCommand(xmlNode);
			}
		}

		if(alertAccountsXmlNode != null)
		{
			this.alertAccounts(alertAccountsXmlNode);
		}
		this._tradingConsole.resumeRefreshSummary();
		if(needRefreshSummary) this._tradingConsole.refreshSummary();

		if (isNeedGetInterestRateFromServer)
		{
			this._tradingConsole.getInterestRate();
		}
		if (isNeedCalculatePLFloat)
		{
			this._tradingConsole.calculatePLFloat();
		}

		for(Guid accountId : accountIds)
		{
			Account.refresh(this._tradingConsole, this._settingsManager, accountId);
		}
	}

	private void handleMatchInfo(XmlNode xmlNode)
	{
		Guid instrumentId = new Guid(xmlNode.get_Attributes().get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = this._settingsManager.getInstrument(instrumentId);

		if(instrument.isFromBursa())
		{
			XmlNodeList childNodeList = xmlNode.get_ChildNodes();
			for(int index = 0; index < childNodeList.get_Count(); index++)
			{
				XmlNode childNode = childNodeList.item(index);
				if(childNode.get_Name().equalsIgnoreCase("BestBuys"))
				{
					XmlNodeList bestPendingList = childNode.get_ChildNodes();
					for (int index2 = 0; index2 < bestPendingList.get_Count(); index2++)
					{
						XmlNode bestPendingNode = bestPendingList.item(index2);
						XmlAttributeCollection attributes = bestPendingNode.get_Attributes();
						String price = attributes.get_ItemOf("Price").get_Value();
						String quantity = attributes.get_ItemOf("Quantity").get_Value();

						BestLimit bestLimit = new BestLimit(instrument.get_Id(), (byte)(index2+1), true, Double.parseDouble(price), Double.parseDouble(quantity));
						instrument.setBestLimit(bestLimit);
					}
				}
				else if (childNode.get_Name().equalsIgnoreCase("BestSells"))
				{
					XmlNodeList bestPendingList = childNode.get_ChildNodes();
					for (int index2 = 0; index2 < bestPendingList.get_Count(); index2++)
					{
						XmlNode bestPendingNode = bestPendingList.item(index2);
						XmlAttributeCollection attributes = bestPendingNode.get_Attributes();
						String price = attributes.get_ItemOf("Price").get_Value();
						String quantity = attributes.get_ItemOf("Quantity").get_Value();

						BestLimit bestLimit = new BestLimit(instrument.get_Id(), (byte)(index2+1), false, Double.parseDouble(price), Double.parseDouble(quantity));
						instrument.setBestLimit(bestLimit);
					}
				}
			}
			return;
		}

		Guid[] accountIds = null;
		TimeAndSale[] timeAndSales = null;
		BestPending[] bestBuys = null;
		BestPending[] bestSells = null;

		XmlNode accountsNode = xmlNode.get_Item("Accounts");
		XmlNodeList accountList = accountsNode.get_ChildNodes();
		accountIds = new Guid[accountList.get_Count()];
		for(int index = 0; index < accountList.get_Count(); index++)
		{
			XmlNode accountNode = accountList.item(index);
			Guid accountId = new Guid(accountNode.get_Attributes().get_ItemOf("ID").get_Value());
			accountIds[index] = accountId;
		}

		XmlNodeList childNodeList = xmlNode.get_ChildNodes();
		for(int index = 0; index < childNodeList.get_Count(); index++)
		{
			XmlNode childNode = childNodeList.item(index);
			if(childNode.get_Name().equalsIgnoreCase("TimeAndSales"))
			{
				XmlNodeList timeAndSaleList = childNode.get_ChildNodes();
				timeAndSales = new TimeAndSale[timeAndSaleList.get_Count()];
				for(int index2 = 0; index2 < timeAndSaleList.get_Count(); index2++)
				{
					XmlNode timeAndSaleNode = timeAndSaleList.item(index2);
					XmlAttributeCollection attributes = timeAndSaleNode.get_Attributes();
					DateTime timestamp = DateTime.valueOf(attributes.get_ItemOf("Timestamp").get_Value());
					double price = Double.parseDouble(attributes.get_ItemOf("Price").get_Value());
					double quantity = Double.parseDouble(attributes.get_ItemOf("Quantity").get_Value());

					String price2 =  AppToolkit.format(price, instrument.get_Decimal());
					TimeAndSale timeAndSale = new TimeAndSale(instrumentId, accountIds, timestamp, price2, quantity);
					timeAndSales[index2] = timeAndSale;
				}
			}
			else if(childNode.get_Name().equalsIgnoreCase("BestBuys"))
			{
				XmlNodeList bestPendingList = childNode.get_ChildNodes();
				bestBuys = new BestPending[bestPendingList.get_Count()];
				for(int index2 = 0; index2 < bestPendingList.get_Count(); index2++)
				{
					XmlNode bestPendingNode = bestPendingList.item(index2);
					XmlAttributeCollection attributes = bestPendingNode.get_Attributes();
					String price = attributes.get_ItemOf("Price").get_Value();
					String quantity = attributes.get_ItemOf("Quantity").get_Value();

					BestPending bestPending = new BestPending(instrumentId, price, quantity, index2);
					bestBuys[index2] = bestPending;
				}
			}
			else if(childNode.get_Name().equalsIgnoreCase("BestSells"))
			{
				XmlNodeList bestPendingList = childNode.get_ChildNodes();
				bestSells = new BestPending[bestPendingList.get_Count()];
				for(int index2 = 0; index2 < bestPendingList.get_Count(); index2++)
				{
					XmlNode bestPendingNode = bestPendingList.item(index2);
					XmlAttributeCollection attributes = bestPendingNode.get_Attributes();
					String price = attributes.get_ItemOf("Price").get_Value();
					String quantity = attributes.get_ItemOf("Quantity").get_Value();

					BestPending bestPending = new BestPending(instrumentId, price, quantity, index2);
					bestSells[index2] = bestPending;
				}
			}
		}

		if(timeAndSales != null)
		{
			for(TimeAndSale timeAndSale : timeAndSales)
			{
				instrument.get_TimeAndSales().add(timeAndSale);
			}
		}
		if(bestBuys != null && bestBuys.length > 0)
		{
			instrument.get_BestPendings().set(true, accountIds, bestBuys);
		}
		else
		{
			for(Guid accountId : accountIds)
			{
				instrument.get_BestPendings().getBestBuys(accountId).removeAll();
			}
		}
		if(bestSells != null && bestSells.length > 0)
		{
			instrument.get_BestPendings().set(false, accountIds, bestSells);
		}
		else
		{
			for(Guid accountId : accountIds)
			{
				instrument.get_BestPendings().getBestSells(accountId).removeAll();
			}
		}
	}

	private void processAffectedOrders(XmlNode affectedOrdersNode)
	{
		XmlNodeList transactionNodeList = affectedOrdersNode.get_ChildNodes();
		for(int index = 0; index < transactionNodeList.get_Count(); index++)
		{
			XmlNode transactionNode = transactionNodeList.item(index);
			XmlNodeList orderNodeList = transactionNode.get_ChildNodes();
			for (int index2 = 0; index2 < orderNodeList.get_Count(); index2++)
			{
				XmlNode orderNode = orderNodeList.item(index);
				XmlAttributeCollection orderCollection = orderNode.get_Attributes();
				Guid orderId = new Guid(orderCollection.get_ItemOf("ID").get_Value());
				if(this._tradingConsole.get_OpenOrders().containsKey(orderId))
				{
					Order order = this._tradingConsole.get_OpenOrders().get(orderId);
					String dayInterestNotValued = orderCollection.get_ItemOf("DayInterestNotValued").get_Value();
					String dayStorageNotValued = orderCollection.get_ItemOf("DayStorageNotValued").get_Value();
					order.get_PLNotValued().set(dayInterestNotValued, dayStorageNotValued);

					order.calculatePLNotValued();
					order.update();
				}
			}
		}
	}

	private void updateTransaction(XmlNode xmlNode)
	{
		XmlElement transactionXmlElement = xmlNode.get_Item("Transaction");
		XmlAttributeCollection transactionCollection = transactionXmlElement.get_Attributes();
		if (transactionCollection.get_Count() <= 0) return;

		Transaction.place(this._tradingConsole, this._settingsManager, transactionXmlElement, true);
	}

	private void applyOrderModification(XmlNode xmlNode)
	{
		XmlAttributeCollection attributes = xmlNode.get_Attributes();
		Guid modificationId = new Guid(attributes.get_ItemOf("ModificationId").get_Value());
		OrderModifyResult result = Enum.valueOf(OrderModifyResult.class, attributes.get_ItemOf("Result").get_Value());
		this._tradingConsole.applyOrderModification(modificationId, result);
	}

	private void handleBestLimitsCommand(XmlNode xmlNode)
	{
		XmlAttributeCollection attributes = xmlNode.get_Attributes();
		Guid instrumentId = new Guid(attributes.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = this._settingsManager.getInstrument(instrumentId);

		XmlNodeList children = xmlNode.get_ChildNodes();
		for (int index = 0; index < children.get_Count(); index++)
		{
			XmlNode bestLimitXmlNode = children.itemOf(index);
			BestLimit bestLimit = BestLimit.create(instrumentId, bestLimitXmlNode);
			instrument.setBestLimit(bestLimit);
		}
	}

	private void fixChartData(XmlNode xmlNode)
	{
		XmlNodeList children = xmlNode.get_ChildNodes();
		for (int index = 0; index < children.get_Count(); index++)
		{
			XmlNode fixChartDatas = children.itemOf(index);
			XmlAttributeCollection attributes = fixChartDatas.get_Attributes();
			Guid quotePolicyID = new Guid(attributes.get_ItemOf("QuotePolicyID").get_Value());
			if(quotePolicyID.equals(this._settingsManager.get_Customer().get_PrivateQuotePolicyId()))
			{
				Guid instrumentId = new Guid(attributes.get_ItemOf("InstrumentID").get_Value());
				String instrumentCode = this._settingsManager.getInstrument(instrumentId).get_Code();
				XmlNodeList children2 = fixChartDatas.get_ChildNodes();
				for (int index2 = 0; index2 < children2.get_Count(); index2++)
				{
					XmlAttributeCollection attributes2 = children2.item(index2).get_Attributes();

					org.aiotrade.core.common.Quotation quotation = new org.aiotrade.core.common.Quotation();
					quotation.setTime(DateTime.valueOf(attributes2.get_ItemOf("Date").get_Value()).getTime());
					quotation.setOpen(Float.parseFloat(attributes2.get_ItemOf("Open").get_Value()));
					quotation.setClose(Float.parseFloat(attributes2.get_ItemOf("Close").get_Value()));
					quotation.setHigh(Float.parseFloat(attributes2.get_ItemOf("High").get_Value()));
					quotation.setLow(Float.parseFloat(attributes2.get_ItemOf("Low").get_Value()));
					quotation.setVolume(Float.parseFloat(attributes2.get_ItemOf("Volume").get_Value()));
					int type = Integer.parseInt(attributes2.get_ItemOf("Minutes").get_Value());
					String status = attributes2.get_ItemOf("Status").get_Value();
					this._tradingConsole.get_ChartManager().setMinutesData(instrumentId.toString(), quotation, type, status);
				}
			}
		}
	}

	private void handleAccountUpdateCommand(XmlNode xmlNode)
	{
		XmlNodeList children = xmlNode.get_ChildNodes();
		for (int index = 0; index < children.get_Count(); index++)
		{
			XmlNode orderNode = children.itemOf(index);
			XmlAttributeCollection attributes = orderNode.get_Attributes();
			Guid orderId = Guid.empty;
			String autoLimitPrice = null;
			String autoStopPrice = null;

			for (int index2 = 0; index2 < attributes.get_Count(); index2++)
			{
				XmlAttribute attribute = attributes.get_ItemOf(index2);
				String name = attribute.get_LocalName();
				String value = attribute.get_Value();
				if (name.equalsIgnoreCase("id"))
				{
					orderId = new Guid(value);
				}
				else if (name.equalsIgnoreCase("AutoLimitPrice"))
				{
					autoLimitPrice = value;
				}
				else if (name.equalsIgnoreCase("AutoStopPrice"))
				{
					autoStopPrice = value;
				}
			}
			Order order = this._tradingConsole.getOrder(orderId);
			if (order != null)
			{
				order.set_AutoLimitPriceString(autoLimitPrice);
				order.set_AutoStopPriceString(autoStopPrice);
				order.update("AutoLimitPriceString", autoLimitPrice);
				order.update("AutoStopPriceString", autoStopPrice);
			}
		}
	}

	private void handleAsyncCommand(XmlNode xmlNode)
	{
		XmlAttributeCollection attributes = xmlNode.get_Attributes();
		boolean failed = Boolean.valueOf(attributes.get_ItemOf("Failed").get_Value());
		XmlAttribute exceptionAttribute = attributes.get_ItemOf("InnerException");
		String errorMessage = exceptionAttribute == null ? "" : exceptionAttribute.get_Value();
		attributes = xmlNode.get_FirstChild().get_Attributes();
		System.out.println(attributes.get_ItemOf("Id").get_Value());
		Guid asyncResultId = new Guid(attributes.get_ItemOf("Id").get_Value());
		String methodName = attributes.get_ItemOf("MethodName").get_Value();

		this._tradingConsole.asyncCommandCompleted(asyncResultId, methodName, failed, errorMessage);
	}
}
