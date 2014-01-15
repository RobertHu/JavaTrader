package tradingConsole.enumDefine;

import framework.lang.Enum;
import tradingConsole.ui.language.Language;

public class CancelReason extends Enum<CancelReason>
{
	public static final CancelReason CustomerCanceled = new CancelReason("CustomerCanceled", 0);
	public static final CancelReason DealerCanceled = new CancelReason("DealerCanceled", 1);
	public static final CancelReason RiskMonitorCanceled = new CancelReason("RiskMonitorCanceled", 2);
	public static final CancelReason MooMocNewPositionNotAllowed = new CancelReason("MooMocNewPositionNotAllowed", 3);
	public static final CancelReason InitialOrderCanNotBeAmended = new CancelReason("InitialOrderCanNotBeAmended", 4);
	public static final CancelReason OrderExpired = new CancelReason("OrderExpired", 5);
	public static final CancelReason InvalidPrice = new CancelReason("InvalidPrice", 6);

	public static final CancelReason RiskMonitorDelete = new CancelReason("RiskMonitorDelete", 7);
	public static final CancelReason AccountResetFailed = new CancelReason("AccountResetFailed", 8);
	//DealerCancele,
	public static final CancelReason NecessaryIsNotWithinThreshold = new CancelReason("NecessaryIsNotWithinThreshold", 9);
	public static final CancelReason MarginIsNotEnough = new CancelReason("MarginIsNotEnough", 10);
	public static final CancelReason AccountIsNotTrading = new CancelReason("AccountIsNotTrading", 11);
	public static final CancelReason InstrumentIsNotAccepting = new CancelReason("InstrumentIsNotAccepting", 12);
	public static final CancelReason TimingIsNotAcceptable = new CancelReason("TimingIsNotAcceptable", 13);
	public static final CancelReason OrderTypeIsNotAcceptable = new CancelReason("OrderTypeIsNotAcceptable", 14);
	public static final CancelReason HasNoAccountsLocked = new CancelReason("HasNoAccountsLocked", 15);
	public static final CancelReason IsLockedByAgent = new CancelReason("IsLockedByAgent", 16);
	//InvalidPrice,
	public static final CancelReason LossExecutedOrderInOco = new CancelReason("LossExecutedOrderInOco", 17);
	public static final CancelReason ExceedOpenLotBalance = new CancelReason("ExceedOpenLotBalance", 18);
	public static final CancelReason OneCancelOther = new CancelReason("OneCancelOther", 19);
	//CustomerCanceled,
	public static final CancelReason AccountIsInAlerting = new CancelReason("AccountIsInAlerting", 20);
	//RiskMonitorCanceled,
	//OrderExpired,
	public static final CancelReason LimitStopAddPositionNotAllowed = new CancelReason("LimitStopAddPositionNotAllowed", 21);
	//MooMocNewPositionNotAllowed
	public static final CancelReason TransactionCannotBeBooked = new CancelReason("TransactionCannotBeBooked", 22);
	public static final CancelReason OnlySptMktIsAllowedForPreCheck = new CancelReason("OnlySptMktIsAllowedForPreCheck", 23);
	public static final CancelReason InvalidTransactionPhase = new CancelReason("InvalidTransactionPhase", 24);
	public static final CancelReason TransactionExpired = new CancelReason("TransactionExpired", 25);
	//InitialOrderCanNotBeAmended
	public static final CancelReason OtherReason = new CancelReason("OtherReason", 26);
	public static final CancelReason PriceChanged = new CancelReason("PriceChanged", 27);
	public static final CancelReason OpenOrderIsClosed = new CancelReason("OpenOrderIsClosed", 28);
	public static final CancelReason ReplacedWithMaxLot = new CancelReason("ReplacedWithMaxLot", 29);
	public static final CancelReason SplittedForHasShortSell = new CancelReason("SplittedForHasShortSell", 30);
	public static final CancelReason AdjustedToFullPaidOrderForHasShortSell = new CancelReason("AdjustedToFullPaidOrderForHasShortSell", 31);

	private CancelReason(String name, int value)
	{
		super(name, value);
	}

	public String toMessage()
	{
		if (this.value() == CustomerCanceled.value())
		{
			return Language.CustomerCanceled;
		}
		if (this.value() == DealerCanceled.value())
		{
			return Language.DealerCanceled;
		}
		if (this.value() == RiskMonitorCanceled.value())
		{
			return this.name();
		}
		if (this.value() == MooMocNewPositionNotAllowed.value())
		{
			return Language.LmtMooMocNewPositionNotAllowed;
		}
		if (this.value() == InitialOrderCanNotBeAmended.value())
		{
			return this.name();
		}
		if (this.value() == OrderExpired.value())
		{
			return Language.TransactionExpired;
		}
		if (this.value() == InvalidPrice.value())
		{
			return Language.InvalidPrice;
		}
		if (this.value() == RiskMonitorDelete.value())
		{
			return Language.RiskMonitorDelete;
		}
		if (this.value() == AccountResetFailed.value())
		{
			return Language.AccountResetFailed;
		}
		if (this.value() == NecessaryIsNotWithinThreshold.value())
		{
			return Language.NecessaryIsNotWithinThreshold;
		}
		if (this.value() == MarginIsNotEnough.value())
		{
			return Language.MarginIsNotEnough;
		}
		if (this.value() == AccountIsNotTrading.value())
		{
			return Language.AccountIsNotTrading;
		}
		if (this.value() == InstrumentIsNotAccepting.value())
		{
			return Language.InstrumentIsNotAccepting;
		}
		if (this.value() == TimingIsNotAcceptable.value())
		{
			return Language.TimingIsNotAcceptable;
		}
		if (this.value() == OrderTypeIsNotAcceptable.value())
		{
			return Language.OrderTypeIsNotAcceptable;
		}
		if (this.value() == HasNoAccountsLocked.value())
		{
			return Language.HasNoAccountsLocked;
		}
		if (this.value() == IsLockedByAgent.value())
		{
			return Language.IsLockedByAgent;
		}
		if (this.value() == LossExecutedOrderInOco.value())
		{
			return Language.LossExecutedOrderInOco;
		}
		if (this.value() == ExceedOpenLotBalance.value())
		{
			return Language.ExceedOpenLotBalance;
		}
		if (this.value() == OneCancelOther.value())
		{
			return Language.OneCancelOther;
		}
		if (this.value() == AccountIsInAlerting.value())
		{
			return Language.AccountIsInAlerting;
		}
		if (this.value() == LimitStopAddPositionNotAllowed.value())
		{
			return Language.StopAddPositionNotAllowed;
		}
		if (this.value() == TransactionCannotBeBooked.value())
		{
			return Language.TransactionCannotBeBooked;
		}
		if (this.value() == OnlySptMktIsAllowedForPreCheck.value())
		{
			return Language.OnlySptMktIsAllowedForPreCheck;
		}
		if (this.value() == InvalidTransactionPhase.value())
		{
			return Language.InvalidTransactionPhase;
		}
		if (this.value() == TransactionExpired.value())
		{
			return Language.TransactionExpired;
		}
		if (this.value() == OtherReason.value())
		{
			return this.name();
		}
		if (this.value() == PriceChanged.value())
		{
			return Language.PriceChangedSincePlace;
		}
		if (this.value() == OpenOrderIsClosed.value())
		{
			return Language.OpenOrderIsClosed;
		}
		if (this.value() == ReplacedWithMaxLot.value())
		{
			return Language.ReplacedWithMaxLot;
		}
		if (this.value() == SplittedForHasShortSell.value())
		{
			return Language.SplittedForHasShortSell;
		}
		if (this.value() == SplittedForHasShortSell.value())
		{
			return Language.SplittedForHasShortSell;
		}
		if (this.value() == AdjustedToFullPaidOrderForHasShortSell.value())
		{
			return Language.AdjustedToFullPaidOrderForHasShortSell;
		}

		return this.name();
	}
}
