package tradingConsole.common;

import framework.lang.Enum;

import tradingConsole.ui.language.Language;

public class TransactionError extends Enum<TransactionError>
{
	public static final TransactionError NoLinkedServer = new TransactionError("NoLinkedServer", -1);
	public static final TransactionError OK = new TransactionError("OK", 0);
	public static final TransactionError RuntimeError = new TransactionError("RuntimeError", 1);
	public static final TransactionError DbOperationFailed = new TransactionError("DbOperationFailed", 2);
	public static final TransactionError TransactionAlreadyExists = new TransactionError("TransactionAlreadyExists", 3);
	public static final TransactionError HasNoOrders = new TransactionError("HasNoOrders", 4);
	public static final TransactionError InvalidRelation = new TransactionError("InvalidRelation", 5);
	public static final TransactionError InvalidLotBalance = new TransactionError("InvalidLotBalance", 6);
	public static final TransactionError ExceedOpenLotBalance = new TransactionError("ExceedOpenLotBalance", 7);
	public static final TransactionError InvalidPrice = new TransactionError("InvalidPrice", 8);
	public static final TransactionError AccountIsNotTrading = new TransactionError("AccountIsNotTrading", 9);
	public static final TransactionError AccountResetFailed = new TransactionError("AccountResetFailed", 10);
	public static final TransactionError InstrumentIsNotAccepting = new TransactionError("InstrumentIsNotAccepting", 11);
	public static final TransactionError TimingIsNotAcceptable = new TransactionError("TimingIsNotAcceptable", 12);
	public static final TransactionError OrderTypeIsNotAcceptable = new TransactionError("OrderTypeIsNotAcceptable", 13);
	public static final TransactionError HasUnassignedOvernightOrders = new TransactionError("HasUnassignedOvernightOrders", 14);
	public static final TransactionError HasNoAccountsLocked = new TransactionError("HasNoAccountsLocked", 15);
	public static final TransactionError IsLockedByAgent = new TransactionError("IsLockedByAgent", 16);
	public static final TransactionError IsNotLockedByAgent = new TransactionError("IsNotLockedByAgent", 17);
	public static final TransactionError ExceedAssigningLotBalance = new TransactionError("ExceedAssigningLotBalance", 18);
	public static final TransactionError LossExecutedOrderInOco = new TransactionError("LossExecutedOrderInOco", 19);
	public static final TransactionError OrderLotExceedMaxLot = new TransactionError("OrderLotExceedMaxLot", 20);
	public static final TransactionError OpenOrderNotExists = new TransactionError("OpenOrderNotExists", 21);
	public static final TransactionError AssigningOrderNotExists = new TransactionError("AssigningOrderNotExists", 22);
	public static final TransactionError TransactionNotExists = new TransactionError("TransactionNotExists", 23);
	public static final TransactionError TransactionCannotBeCanceled = new TransactionError("TransactionCannotBeCanceled", 24);
	public static final TransactionError TransactionCannotBeExecuted = new TransactionError("TransactionCannotBeExecuted", 25);
	public static final TransactionError OrderCannotBeDeleted = new TransactionError("OrderCannotBeDeleted", 26);
	public static final TransactionError NecessaryIsNotWithinThreshold = new TransactionError("NecessaryIsNotWithinThreshold", 27);
	public static final TransactionError MarginIsNotEnough = new TransactionError("MarginIsNotEnough", 28);
	public static final TransactionError IsNotAccountOwner = new TransactionError("IsNotAccountOwner", 29);
	public static final TransactionError InvalidOrderRelation = new TransactionError("InvalidOrderRelation", 30);
	public static final TransactionError TradePolicyIsNotActive = new TransactionError("TradePolicyIsNotActive", 31);
	public static final TransactionError SetPriceTooCloseToMarket = new TransactionError("SetPriceTooCloseToMarket", 32);
	public static final TransactionError HasNoQuotationExists = new TransactionError("HasNoQuotationExists", 33);
	public static final TransactionError AccountIsInAlerting = new TransactionError("AccountIsInAlerting", 34);
	public static final TransactionError DailyQuotationIsNotIntegrated = new TransactionError("DailyQuotationIsNotIntegrated", 35);
	public static final TransactionError LimitStopAddPositionNotAllowed = new TransactionError("LimitStopAddPositionNotAllowed", 36);
	public static final TransactionError MooMocNewPositionNotAllowed = new TransactionError("MooMocNewPositionNotAllowed", 37);
	public static final TransactionError TransactionCannotBeBooked = new TransactionError("TransactionCannotBeBooked", 38);
	public static final TransactionError OnlySptMktIsAllowedForPreCheck = new TransactionError("OnlySptMktIsAllowedForPreCheck", 39);
	public static final TransactionError InvalidTransactionPhase = new TransactionError("InvalidTransactionPhase", 40);
	public static final TransactionError ExecuteTimeMustBeInTradingTime = new TransactionError("ExecuteTimeMustBeInTradingTime", 41);

	public static final TransactionError DatabaseDataIntegralityViolated = new TransactionError("DatabaseDataIntegralityViolated", 50);

	public static final TransactionError PriceIsOutOfDate = new TransactionError("PriceIsOutOfDate", 60);

	public static final TransactionError TransactionExpired = new TransactionError("TransactionExpired", 100);
	public static final TransactionError FillOnMarketCloseNotAllowed = new TransactionError("FillOnMarketCloseNotAllowed", 101);
	public static final TransactionError InstrumentNotInTradePolicy = new TransactionError("InstrumentNotInTradePolicy", 102);
	public static final TransactionError AmendedOrderNotFound = new TransactionError("AmendedOrderNotFound", 103);
	public static final TransactionError InitialOrderCanNotBeAmended = new TransactionError("InitialOrderCanNotBeAmended", 104);
	public static final TransactionError InvalidResetStatusWhenAssign = new TransactionError("InvalidResetStatusWhenAssign", 105);

	public static final TransactionError AlreadyValued = new TransactionError("AlreadyValued", 106);
	public static final TransactionError OutOfAcceptDQVariation = new TransactionError("OutOfAcceptDQVariation", 107);
	public static final TransactionError PriceIsDisabled = new TransactionError("PriceIsDisabled", 108);
	public static final TransactionError PriceChangedSincePlace = new TransactionError("PriceChangedSincePlace", 110);
	public static final TransactionError ExceedMaxOpenLot = new TransactionError("ExceedMaxOpenLot", 111);

	//define, tradingconsole only
	public static final TransactionError RiskMonitorDelete = new TransactionError("RiskMonitorDelete", 20000);
	public static final TransactionError DealerCanceled = new TransactionError("DealerCanceled", 20001);
	public static final TransactionError RejectDQByDealer = new TransactionError("RejectDQByDealer", 20002);
	public static final TransactionError OneCancelOther = new TransactionError("OneCancelOther", 20003);
	public static final TransactionError CustomerCanceled = new TransactionError("CustomerCanceled", 20004);

	public static final TransactionError MultipleCloseOrderNotFound = new TransactionError("MultipleCloseOrderNotFound", 200);
	public static final TransactionError MultipleCloseOnlyExecutedOrderAllowed = new TransactionError("MultipleCloseOnlyExecutedOrderAllowed", 201);
	public static final TransactionError MultipleCloseOnlyOpenOrderAllowed = new TransactionError("MultipleCloseOnlyOpenOrderAllowed", 202);
	public static final TransactionError MultipleCloseHasNoLotBalance = new TransactionError("MultipleCloseHasNoLotBalance", 203);
	public static final TransactionError MultipleCloseOnlySameContractSizeAllowed = new TransactionError("MultipleCloseOnlySameContractSizeAllowed", 204);
	public static final TransactionError MultipleCloseOnlySameAccountAllowed = new TransactionError("MultipleCloseOnlySameAccountAllowed", 205);
	public static final TransactionError MultipleCloseOnlySameInstrumentAllowed = new TransactionError("MultipleCloseOnlySameInstrumentAllowed", 206);
	public static final TransactionError MultipleCloseOppositeNotFound = new TransactionError("MultipleCloseOppositeNotFound", 207);
	public static final TransactionError MultipleCloseNotSortByCode = new TransactionError("MultipleCloseNotSortByCode", 208);
	public static final TransactionError MultipleCloseNotAllowed = new TransactionError("MultipleCloseNotAllowed", 209);

	public static final TransactionError Action_ShouldAutoFill = new TransactionError("Action_ShouldAutoFill", 10000);
    public static final TransactionError Action_NeedDealerConfirmCanceling = new TransactionError("Action_NeedDealerConfirmCanceling", 10001);


	private TransactionError(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(TransactionError transactionError)
	{
		String message;

		if (transactionError.equals(TransactionError.DbOperationFailed))
		{
			message = Language.DbOperationFailed;
		}
		else if (transactionError.equals(TransactionError.TransactionAlreadyExists))
		{
			message = Language.TransactionAlreadyExists;
		}
		else if (transactionError.equals(TransactionError.HasNoOrders))
		{
			message = Language.HasNoOrders;
		}
		else if (transactionError.equals(TransactionError.InvalidRelation))
		{
			message = Language.InvalidRelation;
		}
		else if (transactionError.equals(TransactionError.InvalidLotBalance))
		{
			message = Language.InvalidLotBalance;
		}
		else if (transactionError.equals(TransactionError.ExceedOpenLotBalance))
		{
			message = Language.ExceedOpenLotBalance;
		}
		else if (transactionError.equals(TransactionError.InvalidPrice))
		{
			message = Language.InvalidPrice;
		}
		else if (transactionError.equals(TransactionError.AccountIsNotTrading))
		{
			message = Language.AccountIsNotTrading;
		}
		else if (transactionError.equals(TransactionError.AccountResetFailed))
		{
			message = Language.AccountResetFailed;
		}
		else if (transactionError.equals(TransactionError.InstrumentIsNotAccepting))
		{
			message = Language.InstrumentIsNotAccepting;
		}
		else if (transactionError.equals(TransactionError.TimingIsNotAcceptable))
		{
			message = Language.TimingIsNotAcceptable;
		}
		else if (transactionError.equals(TransactionError.OrderTypeIsNotAcceptable))
		{
			message = Language.OrderTypeIsNotAcceptable;
		}
		else if (transactionError.equals(TransactionError.HasUnassignedOvernightOrders))
		{
			message = Language.HasUnassignedOvernightOrders;
		}
		else if (transactionError.equals(TransactionError.HasNoAccountsLocked))
		{
			message = Language.HasNoAccountsLocked;
		}
		else if (transactionError.equals(TransactionError.IsLockedByAgent))
		{
			message = Language.IsLockedByAgent;
		}
		else if (transactionError.equals(TransactionError.IsNotLockedByAgent))
		{
			message = Language.IsNotLockedByAgent;
		}
		else if (transactionError.equals(TransactionError.ExceedAssigningLotBalance))
		{
			message = Language.ExceedAssigningLotBalance;
		}
		else if (transactionError.equals(TransactionError.LossExecutedOrderInOco))
		{
			message = Language.LossExecutedOrderInOco;
		}
		else if (transactionError.equals(TransactionError.OrderLotExceedMaxLot))
		{
			message = Language.OrderLotExceedMaxLot;
		}
		else if (transactionError.equals(TransactionError.OpenOrderNotExists))
		{
			message = Language.OpenOrderNotExists;
		}
		else if (transactionError.equals(TransactionError.AssigningOrderNotExists))
		{
			message = Language.AssigningOrderNotExists;
		}
		else if (transactionError.equals(TransactionError.TransactionNotExists))
		{
			message = Language.TransactionNotExists;
		}
		else if (transactionError.equals(TransactionError.TransactionCannotBeCanceled))
		{
			message = Language.TransactionCannotBeCanceled;
		}
		else if (transactionError.equals(TransactionError.TransactionCannotBeExecuted))
		{
			message = Language.TransactionCannotBeExecuted;
		}
		else if (transactionError.equals(TransactionError.OrderCannotBeDeleted))
		{
			message = Language.OrderCannotBeDeleted;
		}
		else if (transactionError.equals(TransactionError.NecessaryIsNotWithinThreshold))
		{
			message = Language.NecessaryIsNotWithinThreshold;
		}
		else if (transactionError.equals(TransactionError.MarginIsNotEnough))
		{
			message = Language.MarginIsNotEnough;
		}
		else if (transactionError.equals(TransactionError.IsNotAccountOwner))
		{
			message = Language.IsNotAccountOwner;
		}
		else if (transactionError.equals(TransactionError.InvalidOrderRelation))
		{
			message = Language.InvalidOrderRelation;
		}
		else if (transactionError.equals(TransactionError.TradePolicyIsNotActive))
		{
			message = Language.TradePolicyIsNotActive;
		}
		else if (transactionError.equals(TransactionError.SetPriceTooCloseToMarket))
		{
			message = Language.SetPriceTooCloseToMarket;
		}
		else if (transactionError.equals(TransactionError.HasNoQuotationExists))
		{
			message = Language.HasNoQuotationExists;
		}
		else if (transactionError.equals(TransactionError.AccountIsInAlerting))
		{
			message = Language.AccountIsInAlerting;
		}
		else if (transactionError.equals(TransactionError.LimitStopAddPositionNotAllowed))
		{
			message = Language.StopAddPositionNotAllowed;
		}
		else if (transactionError.equals(TransactionError.MooMocNewPositionNotAllowed))
		{
			message = Language.LmtMooMocNewPositionNotAllowed;
		}
		else if (transactionError.equals(TransactionError.TransactionCannotBeBooked))
		{
			message = Language.TransactionCannotBeBooked;
		}
		else if (transactionError.equals(TransactionError.OnlySptMktIsAllowedForPreCheck))
		{
			message = Language.OnlySptMktIsAllowedForPreCheck;
		}
		else if (transactionError.equals(TransactionError.InvalidTransactionPhase))
		{
			message = Language.InvalidTransactionPhase;
		}
		else if (transactionError.equals(TransactionError.ExecuteTimeMustBeInTradingTime))
		{
			message = Language.ExecuteTimeMustBeInTradingTime;
		}
		else if (transactionError.equals(TransactionError.DatabaseDataIntegralityViolated))
		{
			message = Language.DatabaseDataIntegralityViolated;
		}
		else if (transactionError.equals(TransactionError.TransactionExpired))
		{
			message = Language.TransactionExpired;
		}
		else if (transactionError.equals(TransactionError.ExceedMaxOpenLot))
		{
			message = Language.ExceedMaxOpenLot;
		}
		else if (transactionError.equals(TransactionError.FillOnMarketCloseNotAllowed))
		{
			message = Language.FillOnMarketCloseNotAllowed;
		}
		else if (transactionError.equals(TransactionError.RiskMonitorDelete))
		{
			message = Language.RiskMonitorDelete;
		}
		else if (transactionError.equals(TransactionError.DealerCanceled))
		{
			message = Language.DealerCanceled;
		}
		else if (transactionError.equals(TransactionError.RejectDQByDealer))
		{
			message = Language.RejectDQByDealer;
		}
		else if (transactionError.equals(TransactionError.OneCancelOther))
		{
			message = Language.OneCancelOtherPrompt;
		}
		else if (transactionError.equals(TransactionError.CustomerCanceled))
		{
			message = Language.CustomerCanceled;
		}
		else if(transactionError.equals(TransactionError.OutOfAcceptDQVariation))
		{
			message = Language.OutOfAcceptDQVariation;
		}
		else if(transactionError.equals(TransactionError.PriceIsDisabled))
		{
			message = Language.PriceIsDisabled;
		}
		else if(transactionError.equals(TransactionError.PriceChangedSincePlace))
		{
			message = Language.PriceChangedSincePlace;
		}
		else if(transactionError.equals(TransactionError.PriceIsOutOfDate))
		{
			message = Language.PriceIsOutOfDate;
		}
		else
		{
			return "(" + transactionError.value() + ")";
		}

		return ("(" + transactionError.value() + "):" + message);
	}

	public static String getCaption(String transactionErrorCode)
	{
		TransactionError transactionError = TransactionError.RuntimeError;
		try
		{
			transactionError = Enum.valueOf(TransactionError.class, Integer.parseInt(transactionErrorCode));
		}
		catch(NumberFormatException exception)
		{
			transactionError = Enum.valueOf(TransactionError.class, transactionErrorCode);
		}

		return getCaption(transactionError);
	}
}
