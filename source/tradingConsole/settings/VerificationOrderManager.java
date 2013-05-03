package tradingConsole.settings;

import java.math.*;
import java.util.*;

import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.common.*;
import tradingConsole.enumDefine.*;
import tradingConsole.service.*;
import tradingConsole.ui.*;
import tradingConsole.ui.language.*;
import Packet.SignalObject;

public class VerificationOrderManager
{
	private String _dataSourceKey;
	//private BindingSource _bindingSource;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private OperateType _operateType;
	private OrderType _orderType;
	private Order _assigningOrder;
	private Instrument _instrument;
	private boolean _isMakeOcoOrder;
	private VerificationOrderForm _verificationOrderForm;

	private DateTime _goodTillMonth = null;
	private ExpireType _expireType;

	private HashMap<Guid, Transaction> _verificationTransactions;
	//for fill grid
	private HashMap<Guid, Order> _verificationOrders;

	private VerificationOrderManager(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, OrderType orderType, OperateType operateType,
									 Order assigningOrder, DateTime goodTillMonth, ExpireType expireType, boolean isMakeOcoOrder, VerificationOrderForm verificationOrderForm)
	{
		this._dataSourceKey = Guid.newGuid().toString();
		//this._bindingSource = new BindingSource();

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;
		this._orderType = orderType;
		this._operateType = operateType;
		this._assigningOrder = assigningOrder;
		this._goodTillMonth = goodTillMonth;
		this._expireType = expireType;
		this._isMakeOcoOrder = isMakeOcoOrder;
		this._verificationOrderForm = verificationOrderForm;

		this._verificationTransactions = new HashMap<Guid, Transaction> ();
		this._verificationOrders = new HashMap<Guid, Order> ();
	}

	public static VerificationOrderManager create(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, OrderType orderType,
												  OperateType operateType, Order assigningOrder, DateTime goodTillMonth, ExpireType expireType, boolean isMakeOcoOrder, VerificationOrderForm verificationOrderForm)
	{
		return new VerificationOrderManager(tradingConsole, settingsManager, instrument, orderType, operateType, assigningOrder,
											goodTillMonth, expireType, isMakeOcoOrder, verificationOrderForm);
	}

	//Exclude Direct Liq
	private TransactionType getTransactionType()
	{
		TransactionType transactionType = TransactionType.Single;
		if (this._operateType.equals(OperateType.Assign))
		{
			transactionType = TransactionType.Assign;
		}
		else if (this._operateType.equals(OperateType.OneCancelOther))
		{
			transactionType = TransactionType.OneCancelOther;
		}

		return transactionType;
	}

	private static BigDecimal getContractSize(SettingsManager settingsManager, Account account, Instrument instrument)
	{
		BigDecimal contractSize = BigDecimal.ZERO;
		TradePolicyDetail tradePolicyDetail = settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), instrument.get_Id());
		if (tradePolicyDetail != null)
		{
			contractSize = tradePolicyDetail.get_ContractSize();
		}
		return contractSize;
	}
	private Order createOrder(Transaction transaction, TradeOption tradeOption, boolean isOpen, boolean isBuy, String setPrice, BigDecimal lot, MakeOrderAccount makeOrderAccount)
	{
		return this.createOrder(transaction, tradeOption, isOpen, isBuy, setPrice, null, lot, makeOrderAccount);
	}

	private Order createOrder(Transaction transaction, TradeOption tradeOption, boolean isOpen, boolean isBuy, String setPrice, String setPrice2, BigDecimal lot, MakeOrderAccount makeOrderAccount)
	{
		DataTable orderDataTable = Order.createStructure();

		Guid orderId = Guid.newGuid();
		DataRow orderDataRow = orderDataTable.newRow();
		orderDataRow.set_Item("ID", orderId);
		orderDataRow.set_Item("TradeOption", (short)tradeOption.value());
		orderDataRow.set_Item("IsOpen", isOpen);
		orderDataRow.set_Item("IsBuy", isBuy);
		orderDataRow.set_Item("SetPrice", setPrice);
		orderDataRow.set_Item("SetPrice2", StringHelper.isNullOrEmpty(setPrice2) ? DBNull.value : setPrice2);
		orderDataRow.set_Item("Lot", lot);
		orderDataRow.set_Item("LotBalance", isOpen ? lot : BigDecimal.ZERO);

		orderDataRow.set_Item("Code", "");
		orderDataRow.set_Item("ExecutePrice", "");
		orderDataRow.set_Item("CommissionSum", BigDecimal.ZERO);
		orderDataRow.set_Item("LevySum", BigDecimal.ZERO);
		orderDataRow.set_Item("PeerOrderCodes", "");
		orderDataRow.set_Item("InterestPerLot", BigDecimal.ZERO);
		orderDataRow.set_Item("StoragePerLot", BigDecimal.ZERO);
		orderDataRow.set_Item("TradePLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("InterestPLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("StoragePLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("DQMaxMove", makeOrderAccount.get_DQMaxMove());
		//orderDataRow.set_Item("Sequence",++sequence);

		Order order = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
		order.set_PriceInfo(makeOrderAccount);
		transaction.set_PlaceOrderInfo(makeOrderAccount.get_PlaceOrderInfo());
		transaction.set_IfDoneInfo(makeOrderAccount.get_IfDoneInfo());
		transaction.set_IsPlaceMatchOrder(makeOrderAccount.get_IsPlacMatchOrder());
		return order;
	}

	private Transaction createTransaction(MakeOrderAccount makeOrderAccount, TransactionType transactionType)
	{
		DataTable transactionDataTable = Transaction.createStructure();

		Guid transactionId = Guid.newGuid();

		DataRow transactionDataRow = transactionDataTable.newRow();
		transactionDataRow.set_Item("ID", transactionId);
		transactionDataRow.set_Item("AccountID", makeOrderAccount.get_Account().get_Id());
		transactionDataRow.set_Item("InstrumentID", makeOrderAccount.get_Instrument().get_Id());
		transactionDataRow.set_Item("Code", "");
		transactionDataRow.set_Item("Type", (short) (transactionType.value()));
		transactionDataRow.set_Item("Phase", (short)Phase.Placing.value()); //Phase.Placing?????????
		transactionDataRow.set_Item("BeginTime", DateTime.maxValue);
		transactionDataRow.set_Item("EndTime", DateTime.maxValue);
		transactionDataRow.set_Item("ExpireType", this._expireType.value());
		transactionDataRow.set_Item("SubmitTime", DateTime.maxValue);
		transactionDataRow.set_Item("ExecuteTime", DateTime.maxValue);
		transactionDataRow.set_Item("SubmitorID", this._tradingConsole.get_LoginInformation().get_CustomerId());
		transactionDataRow.set_Item("AssigningOrderID", ( (this._assigningOrder == null) ? Guid.empty : this._assigningOrder.get_Id()));
		transactionDataRow.set_Item("OrderType", this._orderType.value());
		if(this._assigningOrder == null)
		{
			transactionDataRow.set_Item("ContractSize",
										VerificationOrderManager.getContractSize(this._settingsManager, makeOrderAccount.get_Account(),
				makeOrderAccount.get_Instrument()));
		}
		else
		{
			transactionDataRow.set_Item("ContractSize", this._assigningOrder.get_Transaction().get_ContractSize());
		}
		transactionDataRow.set_Item("OperateType",
									this._operateType == OperateType.LimitStop ? OperateType.Limit.value() : this._operateType.value());

		Transaction transaction = new Transaction(this._tradingConsole, this._settingsManager, transactionDataRow);
		transaction.set_MakeOrderAccount(makeOrderAccount);
		return transaction;
	}

	public String checkSpotSetPrice()
	{
		for(Order order : this._verificationOrders.values())
		{
			String warning = Order.checkSpotSetPrice(order);
			if(!StringHelper.isNullOrEmpty(warning))
			{
				return warning;
			}
		}
		return StringHelper.empty;
	}

	public String setVerificationOrderAccounts(HashMap<Guid, MakeOrderAccount> makeOrderAccounts, boolean isCheckEntrance)
	{
		if (this._operateType == OperateType.DirectLiq)
		{
			BigDecimal sumBuyLot = BigDecimal.ZERO;
			BigDecimal sumSellLot = BigDecimal.ZERO;
			for (MakeOrderAccount makeOrderAccount : makeOrderAccounts.values())
			{
				sumBuyLot = sumBuyLot.add(makeOrderAccount.getSumLiqLots(true));
				sumSellLot = sumSellLot.add(makeOrderAccount.getSumLiqLots(false));
			}
			if (sumBuyLot.compareTo(BigDecimal.ZERO) == 0 && sumSellLot.compareTo(BigDecimal.ZERO) == 0)
			{
				return "Non-Liquidate Lot!";
			}
		}

		int sequence = 0;
		for (Iterator<MakeOrderAccount> iterator = makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();

			//Assembly orders.........
			if (this._operateType == OperateType.SingleSpotTrade
				|| this._operateType == OperateType.Method2SpotTrade
				|| this._operateType == OperateType.MultiSpotTrade
				|| this._operateType == OperateType.Assign
				|| this._operateType == OperateType.OneCancelOther
				|| this._operateType == OperateType.Limit
				|| this._operateType == OperateType.LimitStop)
			{
				BuySellType buySellType = makeOrderAccount.get_BuySellType();
				int count = (buySellType == BuySellType.Both) ? 1 : 0;
				for (int i = 0; i <= count; i++)
				{
					boolean isBuy;
					if (buySellType == BuySellType.Buy)
					{
						isBuy = true;
					}
					else if (buySellType == BuySellType.Sell)
					{
						isBuy = false;
					}
					else
					{
						isBuy = (i == 0) ? true : false;
					}
					BigDecimal lot = (isBuy) ? makeOrderAccount.get_BuyLot() : makeOrderAccount.get_SellLot();
					if ( lot.compareTo(BigDecimal.ZERO) > 0)
					{
						BigDecimal liqLots = makeOrderAccount.getSumLiqLots(isBuy);
						BigDecimal liqLots2 = liqLots;

						Price setPrice = makeOrderAccount.getSetPrice(isBuy);
						Price setPrice2 = makeOrderAccount.getSetPrice2(isBuy);

						if (isCheckEntrance)
						{
							Object[] result = MakeOrder.isAcceptEntrance(this._settingsManager, makeOrderAccount.get_Account(), this._instrument,
								this._orderType, isBuy, lot, liqLots2, setPrice, setPrice2, this._operateType == OperateType.Assign);
							if (! ( (Boolean)result[0]))
							{
								return (String)result[1];
							}
						}

						boolean makeOcoOrder = this._isMakeOcoOrder;
						Transaction ocoTransaction = null;
						if(makeOcoOrder)
						{
							ocoTransaction = this.createTransaction(makeOrderAccount, TransactionType.OneCancelOther);
							this._verificationTransactions.put(ocoTransaction.get_Id(), ocoTransaction);

							BigDecimal closeLot = lot.compareTo(liqLots) > 0 ? liqLots : lot;
							boolean isOpen = closeLot.compareTo(BigDecimal.ZERO) == 0;
							BigDecimal placeLot = isOpen ? lot : closeLot;
							if(isOpen) lot = BigDecimal.ZERO;
							Order limitOrder = this.createOrder(ocoTransaction, TradeOption.Better, isOpen, isBuy, makeOrderAccount.getSetPriceString(isBuy), placeLot, makeOrderAccount);
							this._verificationOrders.put(limitOrder.get_Id(), limitOrder);
							ocoTransaction.get_Orders().put(limitOrder.get_Id(), limitOrder);
							if(!isOpen)
							{
								limitOrder.setVerificationOutstandingOrders();
								limitOrder.set_PeerOrderIDs(limitOrder.getPeerOrders()[0]);
								limitOrder.set_PeerOrderCodes(limitOrder.getPeerOrders()[1]);
							}

							Order stopOrder = this.createOrder(ocoTransaction, TradeOption.Stop, isOpen, isBuy, makeOrderAccount.getSetPrice2String(isBuy), placeLot, makeOrderAccount);
							ocoTransaction.get_Orders().put(stopOrder.get_Id(), stopOrder);
							this._verificationOrders.put(stopOrder.get_Id(), stopOrder);
							if(!isOpen)
							{
								stopOrder.setVerificationOutstandingOrders();
								stopOrder.set_PeerOrderIDs(stopOrder.getPeerOrders()[0]);
								stopOrder.set_PeerOrderCodes(stopOrder.getPeerOrders()[1]);
							}
						}

						if(!makeOcoOrder || lot.compareTo(liqLots) > 0)
						{
							if(!makeOcoOrder && liqLots.compareTo(BigDecimal.ZERO) > 0)
							{
								Transaction transaction = this.createTransaction(makeOrderAccount, this.getTransactionType());
								this._verificationTransactions.put(transaction.get_Id(), transaction);

								BigDecimal closeLot = lot.compareTo(liqLots) > 0 ? liqLots : lot;
								TradeOption tradeOption = makeOrderAccount.getTradeOption(isBuy);

								String orderSetPrice = makeOrderAccount.getSetPriceString(isBuy);
								String orderSetPrice2 = makeOrderAccount.getSetPrice2String(isBuy);
								SpotOrderPriceContainer spotOrderPriceContainer = makeOrderAccount.get_SpotOrderPriceContainer();
								if(spotOrderPriceContainer != null)
								{
									orderSetPrice = Price.toString( (isBuy) ? spotOrderPriceContainer.BuyPrice : spotOrderPriceContainer.SellPrice);
									transaction.set_PlacingLogInfo(spotOrderPriceContainer.LogInfo);
								}
								Order order = this.createOrder(transaction, tradeOption, false, isBuy, orderSetPrice, orderSetPrice2, closeLot,	makeOrderAccount);
								this._verificationOrders.put(order.get_Id(), order);
								transaction.get_Orders().put(order.get_Id(), order);
								if (!makeOcoOrder && liqLots.compareTo(BigDecimal.ZERO) > 0)
								{
									order.setVerificationOutstandingOrders();
									order.set_PeerOrderIDs(order.getPeerOrders()[0]);
									order.set_PeerOrderCodes(order.getPeerOrders()[1]);
								}

								if (this._operateType.equals(OperateType.LimitStop))
								{
									transaction = this.createTransaction(makeOrderAccount, this.getTransactionType());
									this._verificationTransactions.put(transaction.get_Id(), transaction);
									tradeOption = makeOrderAccount.getTradeOption2(isBuy);
									order = this.createOrder(transaction, tradeOption, false, isBuy, makeOrderAccount.getSetPrice2String(isBuy), closeLot,
										makeOrderAccount);
									this._verificationOrders.put(order.get_Id(), order);
									transaction.get_Orders().put(order.get_Id(), order);
									if (!makeOcoOrder && liqLots.compareTo(BigDecimal.ZERO) > 0)
									{
										order.setVerificationOutstandingOrders();
										order.set_PeerOrderIDs(order.getPeerOrders()[0]);
										order.set_PeerOrderCodes(order.getPeerOrders()[1]);
									}
								}
							}
						}

						BigDecimal remainLot = lot.subtract(liqLots);
						if(remainLot.compareTo(BigDecimal.ZERO) > 0)
						{
							Transaction transaction = this.createTransaction(makeOrderAccount, this.getTransactionType());
							this._verificationTransactions.put(transaction.get_Id(), transaction);
							TradeOption tradeOption = makeOrderAccount.getTradeOption(isBuy);

							String orderSetPrice = makeOrderAccount.getSetPriceString(isBuy);
							String orderSetPrice2 = makeOrderAccount.getSetPrice2String(isBuy);
							SpotOrderPriceContainer spotOrderPriceContainer = makeOrderAccount.get_SpotOrderPriceContainer();
							if(spotOrderPriceContainer != null)
							{
								orderSetPrice = Price.toString( (isBuy) ? spotOrderPriceContainer.BuyPrice : spotOrderPriceContainer.SellPrice);
								transaction.set_PlacingLogInfo(spotOrderPriceContainer.LogInfo);
							}
							Order order = this.createOrder(transaction, tradeOption, true, isBuy, orderSetPrice, orderSetPrice2, remainLot,	makeOrderAccount);
							this._verificationOrders.put(order.get_Id(), order);
							transaction.get_Orders().put(order.get_Id(), order);

							if (this._operateType.equals(OperateType.LimitStop))
							{
								transaction = this.createTransaction(makeOrderAccount, this.getTransactionType());
								this._verificationTransactions.put(transaction.get_Id(), transaction);
								tradeOption = makeOrderAccount.getTradeOption2(isBuy);
								order = this.createOrder(transaction, tradeOption, true, isBuy, makeOrderAccount.getSetPrice2String(isBuy), remainLot,
									makeOrderAccount);
								this._verificationOrders.put(order.get_Id(), order);
								transaction.get_Orders().put(order.get_Id(), order);
							}
						}
					}
				}
			}
			else if(this._operateType == OperateType.MultipleClose)
			{
				for (Iterator<RelationOrder> relationIterator = makeOrderAccount.getOutstandingOrders().values().iterator(); relationIterator.hasNext(); )
				{
					RelationOrder relationOrder = relationIterator.next();
					if (relationOrder.get_IsSelected())
					{
						Order openOrder = relationOrder.get_OpenOrder();
						this._verificationOrders.put(openOrder.get_Id(), openOrder);
						this._verificationTransactions.put(openOrder.get_Transaction().get_Id(), openOrder.get_Transaction());
					}
				}
			}
			else if (this._operateType == OperateType.DirectLiq)
			{
				BigDecimal sumBuyLot = makeOrderAccount.getSumLiqLots(true);
				BigDecimal sumSellLot = makeOrderAccount.getSumLiqLots(false);
				if (sumBuyLot.compareTo(BigDecimal.ZERO) == 0 && sumSellLot.compareTo(BigDecimal.ZERO) == 0)
				{
					continue;
				}
				//Create 1 tran include 1S orders when sumBuyLot = 0
				//Create 1 tran include 1B orders when sumSellLot = 0
				//pair orders, create 1 tran include 1B & 1S orders when sumBuyLot = sumSellLot

				BigDecimal lotForBuy = sumBuyLot;
				BigDecimal lotForSell = sumSellLot;
				boolean isOpen = false;
				TransactionType transactionType = TransactionType.Single;
				//Price setPriceForBuy = this._instrument.get_LastQuotation().getBuy();
				//Price setPriceForSell = this._instrument.get_LastQuotation().getSell();
				Price setPriceForBuy = makeOrderAccount.get_BuySetPrice();
				Price setPriceForSell = makeOrderAccount.get_SellSetPrice();

				HashMap<Guid, RelationOrder> relationOrdersForBuy = new HashMap<Guid, RelationOrder>();
				HashMap<Guid, RelationOrder> relationOrdersForSell = new HashMap<Guid, RelationOrder>();
				for (Iterator<RelationOrder> relationIterator = makeOrderAccount.getOutstandingOrders().values().iterator(); relationIterator.hasNext(); )
				{
					RelationOrder relationOrder = relationIterator.next();
					if (relationOrder.get_IsSelected())
					{
						if (!relationOrder.get_IsBuy())
						{
							relationOrdersForBuy.put(relationOrder.get_OpenOrderId(), relationOrder);
						}
						else
						{
							relationOrdersForSell.put(relationOrder.get_OpenOrderId(), relationOrder);
						}
					}
				}

				int type2 = 0;
				if (sumBuyLot.compareTo(sumSellLot) == 0)
				{
					type2 = 1;
					transactionType = TransactionType.Pair;
				}
				else if (sumBuyLot.compareTo(BigDecimal.ZERO) != 0 && sumSellLot.compareTo(BigDecimal.ZERO) != 0)
				{
					type2 = 2;
					//Create 2 trans
					//Sample:
					//1S(a)	2S(b)		3S(c)		(total 6S)
					//1B(1)	2B(2)		1B(3)		(total 4B)
					//Create:
					//1 Tran:
					//2B close 2/3(c)

					//1 Tran(pair):
					//4B close 1S(a) + 2S(b) + 1/3(c)  - same tran as below
					//4S close 1B(1) + 2B(2) + 1B(3)   - same tran as below

					BigDecimal diffSumLot = sumBuyLot.subtract(sumSellLot).abs();
					BigDecimal minLot = sumBuyLot.compareTo(sumSellLot) > 0 ? sumSellLot : sumBuyLot;
					Price setPrice = (sumBuyLot.compareTo(sumSellLot) > 0) ? setPriceForBuy : setPriceForSell;

					//var arrayOrders = (sumBuyLot > sumSellLot)?arrayOrdersForBuy:arrayOrdersForSell;
					//var oReturn = this.getPeerOrderIDsAndCodes(arrayOrders,minLot,diffSumLot);
					HashMap<Guid, RelationOrder> relationOrders0 = new HashMap<Guid, RelationOrder>();
					HashMap<Guid, RelationOrder> relationOrders1 = new HashMap<Guid, RelationOrder>();
					BigDecimal alreadyLiqLot = BigDecimal.ZERO;
					boolean isGetDiffPart = false;
					for (Iterator<RelationOrder> relationIterator2 = makeOrderAccount.getOutstandingOrders().values().iterator(); relationIterator2.hasNext(); )
					{
						RelationOrder relationOrder = relationIterator2.next();
						if (relationOrder.get_IsSelected() &&
							((sumBuyLot.compareTo(sumSellLot) > 0 && !relationOrder.get_IsBuy())
							 || (sumBuyLot.compareTo(sumSellLot) < 0 && relationOrder.get_IsBuy())))
						{

							BigDecimal liqLot = relationOrder.get_LiqLot();
							if (!isGetDiffPart && alreadyLiqLot.add(liqLot).compareTo(minLot) > 0)
							{
								RelationOrder relationOrder0 = new RelationOrder(this._tradingConsole, this._settingsManager, relationOrder.get_OpenOrder());
								relationOrder0.set_LiqLot(minLot.subtract(alreadyLiqLot));
								relationOrders0.put(relationOrder0.get_OpenOrderId(), relationOrder0);
								relationOrder0.set_Origin(relationOrder);

								RelationOrder relationOrder1 = new RelationOrder(this._tradingConsole, this._settingsManager, relationOrder.get_OpenOrder());
								relationOrder1.set_LiqLot(liqLot.subtract(minLot).add(alreadyLiqLot));
								relationOrders1.put(relationOrder1.get_OpenOrderId(), relationOrder1);
								relationOrder1.set_Origin(relationOrder);

								isGetDiffPart = true;

								continue;
							}
							if (!isGetDiffPart)
							{
								relationOrders0.put(relationOrder.get_OpenOrderId(), relationOrder);

								//alreadyLiqLot += relationOrder.get_LiqLot().doubleValue();
								alreadyLiqLot = alreadyLiqLot.add(liqLot);
								if (alreadyLiqLot.compareTo(minLot) == 0)
								{
									isGetDiffPart = true;
								}
							}
							else
							{
								relationOrders1.put(relationOrder.get_OpenOrderId(), relationOrder);
							}
						}
					}

					if (isCheckEntrance)
					{
						Price entrancePrice = this._instrument.get_LastQuotation().getBuySell(sumBuyLot.compareTo(sumSellLot) > 0);
						Object[] result = MakeOrder.isAcceptEntrance(this._settingsManager, makeOrderAccount.get_Account(), this._instrument, isOpen, diffSumLot,
							entrancePrice);
						if (! ( (Boolean) result[0]))
						{
							return (String)result[1];
						}
					}

					//Create Transaction
					Guid transactionId = Guid.newGuid();
					DataTable transactionDataTable = Transaction.createStructure();

					DataRow transactionDataRow = transactionDataTable.newRow();
					transactionDataRow.set_Item("ID", transactionId);
					transactionDataRow.set_Item("AccountID", makeOrderAccount.get_Account().get_Id());
					transactionDataRow.set_Item("InstrumentID", makeOrderAccount.get_Instrument().get_Id());
					transactionDataRow.set_Item("Code", "");
					transactionDataRow.set_Item("Type", (short) (transactionType.value()));
					transactionDataRow.set_Item("Phase", (short) Phase.Placing.value());
					transactionDataRow.set_Item("BeginTime", DateTime.maxValue);
					transactionDataRow.set_Item("EndTime", DateTime.maxValue);
					transactionDataRow.set_Item("ExpireType", this._expireType.value());
					transactionDataRow.set_Item("SubmitTime", DateTime.maxValue);
					transactionDataRow.set_Item("ExecuteTime", DateTime.maxValue);
					transactionDataRow.set_Item("SubmitorID", this._tradingConsole.get_LoginInformation().get_CustomerId());
					transactionDataRow.set_Item("AssigningOrderID", ( (this._assigningOrder == null) ? Guid.empty : this._assigningOrder.get_Id()));
					transactionDataRow.set_Item("OrderType", this._orderType.value());
					transactionDataRow.set_Item("ContractSize",
												VerificationOrderManager.getContractSize(this._settingsManager, makeOrderAccount.get_Account(), makeOrderAccount.get_Instrument()));
					transactionDataRow.set_Item("OperateType", this._operateType.value());

					Transaction transaction = new Transaction(this._tradingConsole, this._settingsManager, transactionDataRow);
					transaction.set_MakeOrderAccount(makeOrderAccount);
					this._verificationTransactions.put(transactionId, transaction);

					//Create Order
					DataTable orderDataTable = Order.createStructure();
					Guid orderId = Guid.newGuid();
					DataRow orderDataRow = orderDataTable.newRow();
					orderDataRow.set_Item("ID", orderId);
					orderDataRow.set_Item("TradeOption", (short) TradeOption.None.value());
					orderDataRow.set_Item("IsOpen", isOpen);
					orderDataRow.set_Item("IsBuy", (sumBuyLot.compareTo(sumSellLot) > 0));
					orderDataRow.set_Item("SetPrice", Price.toString(setPrice));
					orderDataRow.set_Item("Lot", diffSumLot);
					orderDataRow.set_Item("LotBalance", BigDecimal.ZERO);

					orderDataRow.set_Item("Code", "");
					orderDataRow.set_Item("ExecutePrice", "");
					orderDataRow.set_Item("CommissionSum", BigDecimal.ZERO);
					orderDataRow.set_Item("LevySum", BigDecimal.ZERO);
					orderDataRow.set_Item("PeerOrderCodes", "");
					orderDataRow.set_Item("InterestPerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("TradePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("InterestPLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("DQMaxMove", makeOrderAccount.get_DQMaxMove());
					//orderDataRow.set_Item("Sequence",++sequence);

					Order order = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
					order.set_Sequence(++sequence);

					//order.setVerificationOutstandingOrders();
					order.set_RelationOrders(relationOrders1);

					order.set_PeerOrderIDs(order.getPeerOrders()[0]);
					order.set_PeerOrderCodes(order.getPeerOrders()[1]);
					order.set_PriceInfo(makeOrderAccount);

					this._verificationOrders.put(orderId, order);
					transaction.get_Orders().put(orderId, order);

					transactionType = TransactionType.Pair;
					if (sumBuyLot.compareTo(sumSellLot) > 0)
					{
						lotForBuy = minLot;
						relationOrdersForBuy = relationOrders0;
						//peerOrderIDsForBuy = oReturn.peerOrderIDs0;
						//peerOrderCodesForBuy = oReturn.peerOrderCodes0;
					}
					else
					{
						lotForSell = minLot;
						relationOrdersForSell = relationOrders0;
						//peerOrderIDsForSell = oReturn.peerOrderIDs0;
						//peerOrderCodesForSell = oReturn.peerOrderCodes0;
					}
				}
				//Create Transaction
				Guid transactionId = Guid.newGuid();
				DataTable transactionDataTable = Transaction.createStructure();

				DataRow transactionDataRow = transactionDataTable.newRow();
				transactionDataRow.set_Item("ID", transactionId);
				transactionDataRow.set_Item("AccountID", makeOrderAccount.get_Account().get_Id());
				transactionDataRow.set_Item("InstrumentID", makeOrderAccount.get_Instrument().get_Id());
				transactionDataRow.set_Item("Code", "");
				transactionDataRow.set_Item("Type", (short) (transactionType.value()));
				transactionDataRow.set_Item("Phase", (short) Phase.Placing.value());
				transactionDataRow.set_Item("BeginTime", DateTime.maxValue);
				transactionDataRow.set_Item("EndTime", DateTime.maxValue);
				transactionDataRow.set_Item("ExpireType", this._expireType.value());
				transactionDataRow.set_Item("SubmitTime", DateTime.maxValue);
				transactionDataRow.set_Item("SubmitorID", this._tradingConsole.get_LoginInformation().get_CustomerId());
				transactionDataRow.set_Item("ExecuteTime", DateTime.maxValue);
				transactionDataRow.set_Item("AssigningOrderID", ( (this._assigningOrder == null) ? Guid.empty : this._assigningOrder.get_Id()));
				transactionDataRow.set_Item("OrderType", this._orderType.value());
				transactionDataRow.set_Item("ContractSize",
											VerificationOrderManager.getContractSize(this._settingsManager, makeOrderAccount.get_Account(), makeOrderAccount.get_Instrument()));
				transactionDataRow.set_Item("OperateType", this._operateType.value());

				Transaction transaction = new Transaction(this._tradingConsole, this._settingsManager, transactionDataRow);
				transaction.set_MakeOrderAccount(makeOrderAccount);
				this._verificationTransactions.put(transactionId, transaction);

				//Create Order
				if (sumSellLot.compareTo(BigDecimal.ZERO) == 0 || (sumBuyLot.compareTo(BigDecimal.ZERO) != 0 && sumBuyLot.compareTo(sumSellLot) <= 0) || (type2 == 2))
				{
					if (isCheckEntrance)
					{
						Price entrancePrice = this._instrument.get_LastQuotation().getBuySell(true);
						Object[] result = MakeOrder.isAcceptEntrance(this._settingsManager, makeOrderAccount.get_Account(), this._instrument, isOpen, lotForBuy,
							entrancePrice);
						if (! ( (Boolean) result[0]))
						{
							return (String)result[1];
						}
					}
					DataTable orderDataTable = Order.createStructure();
					Guid orderId = Guid.newGuid();
					DataRow orderDataRow = orderDataTable.newRow();
					orderDataRow.set_Item("ID", orderId);
					orderDataRow.set_Item("TradeOption", (short) TradeOption.None.value());
					orderDataRow.set_Item("IsOpen", isOpen);
					orderDataRow.set_Item("IsBuy", true);
					orderDataRow.set_Item("SetPrice", Price.toString(setPriceForBuy));
					orderDataRow.set_Item("Lot", lotForBuy);
					orderDataRow.set_Item("LotBalance", BigDecimal.ZERO);

					orderDataRow.set_Item("Code", "");
					orderDataRow.set_Item("ExecutePrice", "");
					orderDataRow.set_Item("CommissionSum", BigDecimal.ZERO);
					orderDataRow.set_Item("LevySum", BigDecimal.ZERO);
					orderDataRow.set_Item("PeerOrderCodes", "");
					orderDataRow.set_Item("InterestPerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("TradePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("InterestPLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("DQMaxMove", makeOrderAccount.get_DQMaxMove());
					//orderDataRow.set_Item("Sequence",++sequence);

					Order order = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
					order.set_Sequence(++sequence);

	                order.set_RelationOrders(relationOrdersForBuy);
					//order.setVerificationOutstandingOrders();

					order.set_PeerOrderIDs(order.getPeerOrders()[0]);
					order.set_PeerOrderCodes(order.getPeerOrders()[1]);
					order.set_PriceInfo(makeOrderAccount);

					this._verificationOrders.put(orderId, order);
					transaction.get_Orders().put(orderId, order);
					transaction.set_PlaceOrderInfo(makeOrderAccount.get_PlaceOrderInfo());
					transaction.set_IfDoneInfo(makeOrderAccount.get_IfDoneInfo());
					transaction.set_IsPlaceMatchOrder(makeOrderAccount.get_IsPlacMatchOrder());
				}
				if (sumBuyLot.compareTo(BigDecimal.ZERO) == 0 || (sumSellLot.compareTo(BigDecimal.ZERO) != 0 && sumBuyLot.compareTo(sumSellLot) >= 0) || (type2 == 2))
				{
					if (isCheckEntrance)
					{
						Price entrancePrice = this._instrument.get_LastQuotation().getBuySell(false);
						Object[] result = MakeOrder.isAcceptEntrance(this._settingsManager, makeOrderAccount.get_Account(), this._instrument, isOpen, lotForSell,
							entrancePrice);
						if (! ( (Boolean) result[0]))
						{
							return (String)result[1];
						}
					}
					DataTable orderDataTable = Order.createStructure();
					Guid orderId = Guid.newGuid();
					DataRow orderDataRow = orderDataTable.newRow();
					orderDataRow.set_Item("ID", orderId);
					orderDataRow.set_Item("TradeOption", (short) TradeOption.None.value());
					orderDataRow.set_Item("IsOpen", isOpen);
					orderDataRow.set_Item("IsBuy", false);
					orderDataRow.set_Item("SetPrice", Price.toString(setPriceForSell));
					orderDataRow.set_Item("Lot", lotForSell);
					orderDataRow.set_Item("LotBalance", BigDecimal.ZERO);

					orderDataRow.set_Item("Code", "");
					orderDataRow.set_Item("ExecutePrice", "");
					orderDataRow.set_Item("CommissionSum", BigDecimal.ZERO);
					orderDataRow.set_Item("LevySum", BigDecimal.ZERO);
					orderDataRow.set_Item("PeerOrderCodes", "");
					orderDataRow.set_Item("InterestPerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePerLot", BigDecimal.ZERO);
					orderDataRow.set_Item("TradePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("InterestPLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("StoragePLFloat", BigDecimal.ZERO);
					orderDataRow.set_Item("DQMaxMove", makeOrderAccount.get_DQMaxMove());
					//orderDataRow.set_Item("Sequence",++sequence);

					Order order = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
					order.set_Sequence(++sequence);

					order.set_RelationOrders(relationOrdersForSell);
					//order.setVerificationOutstandingOrders();

					order.set_PeerOrderIDs(order.getPeerOrders()[0]);
					order.set_PeerOrderCodes(order.getPeerOrders()[1]);
					order.set_PriceInfo(makeOrderAccount);

					this._verificationOrders.put(orderId, order);
					transaction.get_Orders().put(orderId, order);
				}
			}

			//Make other order..........
		}

		//this.recreateTransactionsByContractSize(); deal by transaction server, so comment
		return "";
	}

	private void recreateTransactionsByContractSize()
	{
		ArrayList<Transaction> shouldRemoveTransactions = new ArrayList<Transaction>();
		ArrayList<Transaction> shouldAddTransactions = new ArrayList<Transaction>();
		ArrayList<Transaction> handledTransactions = new ArrayList<Transaction>();

		for(Order order : this._verificationOrders.values())
		{
			if(order.get_RelationOrders().size() > 0)
			{
				Transaction oldTransaction = order.get_Transaction();
				if(!handledTransactions.contains(oldTransaction))
				{
					handledTransactions.add(oldTransaction);

					HashMap<BigDecimal, ArrayList<RelationOrder>> contractSize2RelationOrders = new HashMap<BigDecimal, ArrayList<RelationOrder>> ();
					for (Iterator<RelationOrder> iterator = order.get_RelationOrders().values().iterator(); iterator.hasNext(); )
					{
						RelationOrder relationOrder = iterator.next();
						BigDecimal contractSize = relationOrder.get_OpenOrder().get_Transaction().get_ContractSize();
						if (!contractSize2RelationOrders.containsKey(contractSize))
						{
							contractSize2RelationOrders.put(contractSize, new ArrayList<RelationOrder> ());
						}
						contractSize2RelationOrders.get(contractSize).add(relationOrder);
					}

					if (contractSize2RelationOrders.size() == 1)
					{
						BigDecimal[] contractSizes = new BigDecimal[contractSize2RelationOrders.size()];
						contractSizes = contractSize2RelationOrders.keySet().toArray(contractSizes);
						order.get_Transaction().setContractSize(contractSizes[0]);
					}
					else
					{
						shouldRemoveTransactions.add(oldTransaction);
						ArrayList<Transaction> newTransactions
							= this.splitByOpenOrderContractSize(oldTransaction, contractSize2RelationOrders);
						shouldAddTransactions.addAll(newTransactions);
					}
				}
			}
		}

		for(Transaction transaction : shouldRemoveTransactions)
		{
			this.removeTransaction(transaction);
		}

		for(Transaction transaction : shouldAddTransactions)
		{
			this.addTransaction(transaction);
		}
	}

	private void addTransaction(Transaction transaction)
	{
		this._verificationTransactions.put(transaction.get_Id(), transaction);
		for (Iterator<Order> orders = transaction.get_Orders().values().iterator(); orders.hasNext(); )
		{
			Order order = orders.next();
			this._verificationOrders.put(order.get_Id(), order);
		}
	}

	private void removeTransaction(Transaction transaction)
	{
		this._verificationTransactions.remove(transaction.get_Id());
		for (Iterator<Order> orders = transaction.get_Orders().values().iterator(); orders.hasNext(); )
		{
			Order order = orders.next();
			this._verificationOrders.remove(order.get_Id());
		}
	}

	/*
	A transaction have only one order except OCO, OCO transaction have two orders have same open order collection
	*/
	private ArrayList<Transaction> splitByOpenOrderContractSize(Transaction oldTransaction, HashMap<BigDecimal, ArrayList<RelationOrder>> contractSize2RelationOrders)
	{
		ArrayList<Transaction> newTransactions = new ArrayList<Transaction>();

		BigDecimal[] contractSizes = new BigDecimal[contractSize2RelationOrders.size()];
		contractSizes = contractSize2RelationOrders.keySet().toArray(contractSizes);

		Order[] oldOrders = new Order[oldTransaction.get_Orders().size()];
		oldOrders = oldTransaction.get_Orders().values().toArray(oldOrders);

		for (BigDecimal contractSize : contractSizes)
		{
			BigDecimal closeLot = this.getCloseLot(contractSize2RelationOrders.get(contractSize));

			MakeOrderAccount makeOrderAccount = oldTransaction.get_MakeOrderAccount();
			Transaction transaction = this.createTransaction(makeOrderAccount, oldTransaction.get_Type());
			transaction.setContractSize(contractSize);
			newTransactions.add(transaction);

			if (oldTransaction.get_Type().equals(TransactionType.OneCancelOther))
			{
				Order order = this.createOrderWithRelation(transaction, oldOrders[0], closeLot, contractSize2RelationOrders.get(contractSize));
				transaction.get_Orders().put(order.get_Id(), order);

				Order order2 = this.createOrderWithRelation(transaction, oldOrders[1], closeLot, contractSize2RelationOrders.get(contractSize));
				transaction.get_Orders().put(order2.get_Id(), order2);
			}
			else
			{
				Order order = this.createOrderWithRelation(transaction, oldOrders[0], closeLot, contractSize2RelationOrders.get(contractSize));
				transaction.get_Orders().put(order.get_Id(), order);
			}
		}

		return newTransactions;
	}

	private Order createOrderWithRelation(Transaction transaction, Order oldOrder, BigDecimal closeLot, ArrayList<RelationOrder> relationOrders)
	{
		Order order = this.createOrder(transaction, oldOrder.get_TradeOption(),
									   oldOrder.get_IsOpen(), oldOrder.get_IsBuy(),
									   oldOrder.get_SetPriceString(), closeLot, transaction.get_MakeOrderAccount());

		order.setRelationOrders(relationOrders);
		order.set_PeerOrderIDs(order.getPeerOrders()[0]);
		order.set_PeerOrderCodes(order.getPeerOrders()[1]);
		return order;
	}

	private BigDecimal getCloseLot(ArrayList<RelationOrder> relationOrders)
	{
		BigDecimal closeLot = BigDecimal.ZERO;
		for(RelationOrder relationOrder : relationOrders)
		{
			closeLot = closeLot.add(relationOrder.get_CloseLot());
		}
		return closeLot;
	}

	public BigDecimal getQuoteLot()
	{
		/*BigDecimal quoteLot = BigDecimal.ZERO;
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			BigDecimal lot = order.get_Lot();
			quoteLot = quoteLot.add(lot);
		}
		return quoteLot;*/

		BigDecimal totalBuyLot = BigDecimal.ZERO;
		BigDecimal totalSellLot = BigDecimal.ZERO;
		for(Order order : this._verificationOrders.values())
		{
			if(order.get_IsBuy())
			{
				totalBuyLot = totalBuyLot.add(order.get_Lot());
			}
			else
			{
				totalSellLot = totalSellLot.add(order.get_Lot());
			}
		}
		return totalBuyLot.compareTo(totalSellLot)  > 0 ? totalBuyLot : totalSellLot;
	}

	public BSStatus getBSStatus()
	{
		BSStatus bsStatus = BSStatus.None;
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			if (bsStatus.equals(BSStatus.None))
			{
				bsStatus = (order.get_IsBuy()) ? BSStatus.HasBuyOnly : BSStatus.HasSellOnly;
			}
			else
			{
				if ( (order.get_IsBuy() && bsStatus.equals(BSStatus.HasSellOnly))
					|| (!order.get_IsBuy() && bsStatus.equals(BSStatus.HasBuyOnly)))
				{
					bsStatus = BSStatus.Both;
					break;
				}
			}
		}
		return bsStatus;
	}

	public void freezePriceForPlacing()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.freezePriceForPlacing();
		}
	}

	public void unfreezePriceForPlacing()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.unfreezePriceForPlacing();
		}
	}

	public String getVerificationInfo()
	{
	  String verificationInfo = "";
	  for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
	  {
		  Order order = iterator.next();
		  String verificationInfo2 = order.getVerificationInfo();
		  if (StringHelper.isNullOrEmpty(verificationInfo))
		  {
		    verificationInfo = verificationInfo2;
		  }
		  else
		  {
		    verificationInfo += TradingConsole.enterLine + TradingConsole.enterLine + verificationInfo2;
		  }
	  }
	  return verificationInfo;
	}

	/*public void initialize(Table grid, boolean ascend, boolean caseOn)
	{
		//hidden setPrice & tradeOption
		boolean isHiddenSetPrice = (this._orderType.equals(OrderType.Market)
									|| this._orderType.equals(OrderType.MarketOnOpen)
									|| this._orderType.equals(OrderType.MarketOnClose)
									|| this._orderType.equals(OrderType.OneCancelOther));
		boolean isHiddenTradeOption = (this._orderType.equals(OrderType.SpotTrade)
									   || this._orderType.equals(OrderType.Market)
									   || this._orderType.equals(OrderType.MarketOnOpen)
									   || this._orderType.equals(OrderType.MarketOnClose)
									   || this._orderType.equals(OrderType.OneCancelOther));

		Order.initializeVerification(grid, this._dataSourceKey, this._verificationOrders.values(), this._bindingSource, isHiddenSetPrice, isHiddenTradeOption);
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.updateForVerification(this._dataSourceKey);
		}
		int column = this._bindingSource.getColumn(MakeOrderVerificationColKey.Sequence);
		grid.sort(column, ascend, caseOn);
	}

	public void unbind()
	{
		Order.unbindVerification(this._dataSourceKey, this._bindingSource);
	}*/

	//for before quote
	public void setSetPriceToNull()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.set_SetPrice(null);
		}
	}

	//for after quote
	public void setSetPrice()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.set_SetPrice(order.get_Transaction().get_Instrument().get_LastQuotation().getBuySell(order.get_IsBuy()));
		}
	}

	public void updateSetPrice()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			order.updateForVerification(this._dataSourceKey);
		}
	}
/*
	public DateTime getLmtEndTimeForGoodTillMonth()
	{
		DateTime endTimeForGoodTillMonth = this._goodTillMonth;
		if (Parameter.goodTillMonthType == 0)
		{
			String datePart = Convert.toString(this._goodTillMonth, "yyyy-MM-dd");
			String datePart2 = Convert.toString(this._settingsManager.get_TradeDay().get_TradeDay(), "yyyy-MM-dd");
			if (!datePart.equals(datePart2))
			{
				String timePart = Convert.toString(this._settingsManager.get_TradeDay().get_BeginTime(), "HH:mm:ss");
				endTimeForGoodTillMonth = AppToolkit.getDateTime(datePart + " " + timePart);
				endTimeForGoodTillMonth = endTimeForGoodTillMonth.addDays(1);
			}
			else
			{
				DateTime dayCloseTime = this._instrument.get_DayCloseTime();
				if (dayCloseTime == null)
				{
					dayCloseTime = this._settingsManager.get_TradeDay().get_EndTime();
				}
				endTimeForGoodTillMonth = dayCloseTime;
			}
		}
		return endTimeForGoodTillMonth;
	}
*/
    private Object[] getMakeOrderTime(Transaction transaction)
	{
		DateTime appTime = TradingConsoleServer.appTime();
		DateTime beginTime = DateTime.maxValue;
		DateTime endTime = DateTime.maxValue;

		Object[] result = new Object[]
			{false, "", beginTime, endTime, appTime}; //isValid,message,beginTime,endTime,submitTime

		boolean canPlacePendingOrderAtAnyTime = transaction.get_Instrument().get_CanPlacePendingOrderAtAnyTime();

		if (!this._orderType.equals(OrderType.MarketOnOpen)
			&& !this._orderType.equals(OrderType.MarketOnClose))
		{
			if (!this._instrument.get_CanPlacePendingOrderAtAnyTime()
				&& appTime.after(this._instrument.get_CloseTime().addMinutes(0 - this._instrument.get_LastAcceptTimeSpan())))
			{
				result[1] = Language.OrderPlacementbuttonConfirm_OnclickAlert0;
				return result;
			}
		}
		if (!this._operateType.equals(OperateType.Assign))
		{
			if (!this._instrument.get_CanPlacePendingOrderAtAnyTime()
				&& appTime.after(this._instrument.get_CloseTime().addMinutes(0 - this._instrument.get_LastAcceptTimeSpan())))
			{
				result[1] = Language.OrderPlacementbuttonConfirm_OnclickAlert0;
				return result;
			}
		}
		if (this._orderType.equals(OrderType.SpotTrade))
		{
			if (!this._operateType.equals(OperateType.Assign))
			{
				Object[] result2 = MakeSpotTradeOrder.isAllowTime(this._instrument);
				if (! (Boolean) result2[0])
				{
					result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
					return result;
				}
			}
			beginTime = appTime.addMinutes(0 - Parameter.orderBeginTimeDiff);
			DateTime endTime2 = appTime.addSeconds(this._settingsManager.get_SystemParameter().get_OrderValidDuration());
			if (this._operateType.equals(OperateType.Assign))
			{
				endTime = endTime2;
			}
			else
			{
				DateTime closeTime = this._instrument.get_CloseTime();
				endTime = (endTime2.after(closeTime)) ? closeTime : endTime2;
			}
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.Limit) || this._orderType.equals(OrderType.OneCancelOther)
			|| this._orderType.equals(OrderType.MarketToLimit) || this._orderType.equals(OrderType.FAK_Market)
			|| this._orderType.equals(OrderType.StopLimit))
		{
			Object[] result2 = MakeLimitOrder.isAllowTime(this._instrument);
			if (!canPlacePendingOrderAtAnyTime && ! (Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}

			if((Boolean) result2[0])
			{
				beginTime = appTime.addMinutes(0 - Parameter.orderBeginTimeDiff);
				if (this._goodTillMonth != null)
				{
					endTime = this._goodTillMonth;
				}
				else
				{
					endTime = this._instrument.get_CloseTime();
				}
			}
			else
			{
				DateTime[] nextTradingTime = transaction.get_Instrument().getNextTradingTime();
				beginTime = nextTradingTime[0].addMinutes(0 - Parameter.orderBeginTimeDiff);
				DateTime[] nextTradeDay = transaction.get_Instrument().getNextTradeDay();
				endTime = nextTradeDay[1];

				if(transaction.get_ExpireType() == ExpireType.Day)
				{
					beginTime = nextTradeDay[0].addMinutes(0 - Parameter.orderBeginTimeDiff);
				}
				else if(transaction.get_ExpireType() == ExpireType.Session)
				{
					endTime = nextTradingTime[1];
				}
				else if (this._goodTillMonth != null)
				{
					endTime = this._goodTillMonth;
				}
			}
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.Market))
		{
			Object[] result2 = MakeMarketOrder.isAllowTime(this._instrument);
			if (! (Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			beginTime = appTime.addMinutes(0 - Parameter.orderBeginTimeDiff);
			endTime = appTime.addSeconds(this._settingsManager.get_SystemParameter().get_MarketOrderValidDuration());
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.MarketOnOpen))
		{
			Object[] result2 = MakeMarketOnOpenOrder.isAllowTime(this._settingsManager, this._instrument, false);
			if (!canPlacePendingOrderAtAnyTime && !(Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}

			if(canPlacePendingOrderAtAnyTime && appTime.before(this._instrument.get_DayOpenTime()))
			{
				beginTime = this._instrument.get_DayOpenTime();
				endTime = beginTime.addMinutes(Parameter.mooMocOrderValidDuration);
				result[0] = true;
			}
			else
			{
				DateTime nextDayOpenTime = this._instrument.get_NextDayOpenTime();
				if (nextDayOpenTime != null)
				{
					beginTime =  nextDayOpenTime;
					if(this._instrument.get_DayCloseTime().compareTo(nextDayOpenTime) == 0)
					{//24 hours trading instrument
						beginTime =  nextDayOpenTime.addSeconds(1);
					}
					endTime = nextDayOpenTime.addMinutes(Parameter.mooMocOrderValidDuration);
					result[0] = true;
				}
			}
		}
		else if (this._orderType.equals(OrderType.MarketOnClose))
		{
			Object[] result2 = MakeMarketOnCloseOrder.isAllowTime(this._settingsManager, this._instrument, false);
			if (!canPlacePendingOrderAtAnyTime && !(Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			DateTime mocTime = this._instrument.get_MOCTime();
			if (mocTime == null)
			{
				result[0] = false;
			}
			else
			{
				if(appTime.after(mocTime)) mocTime = this._instrument.getNextMocTime();
				beginTime = mocTime;
				endTime = beginTime.addMinutes(Parameter.mooMocOrderValidDuration);
				result[0] = true;
			}
		}

		result[2] = beginTime;
		result[3] = endTime;
		result[4] = appTime;

		return result;

	}

	private Object[] inMakeOrderTime()
	{
		Object[] result = new Object[]{false, ""}; //isValid,message

		boolean canPlacePendingOrderAtAnyTime = false;
		for (Iterator<Transaction> transactions = this._verificationTransactions.values().iterator(); transactions.hasNext(); )
		{
			Transaction transaction = transactions.next();
			canPlacePendingOrderAtAnyTime = transaction.get_Instrument().get_CanPlacePendingOrderAtAnyTime();
			if(canPlacePendingOrderAtAnyTime) break;
		}

		DateTime appTime = TradingConsoleServer.appTime();
		if (!this._orderType.equals(OrderType.MarketOnOpen)
			&& !this._orderType.equals(OrderType.MarketOnClose))
		{
			if (!this._instrument.get_CanPlacePendingOrderAtAnyTime()
				&&	appTime.after(this._instrument.get_CloseTime().addMinutes(0 - this._instrument.get_LastAcceptTimeSpan())))
			{
				result[1] = Language.OrderPlacementbuttonConfirm_OnclickAlert0;
				return result;
			}
		}
		if (!this._operateType.equals(OperateType.Assign))
		{
			if (!this._instrument.get_CanPlacePendingOrderAtAnyTime()
				&& appTime.after(this._instrument.get_CloseTime().addMinutes(0 - this._instrument.get_LastAcceptTimeSpan())))
			{
				result[1] = Language.OrderPlacementbuttonConfirm_OnclickAlert0;
				return result;
			}
		}
		if (this._orderType.equals(OrderType.SpotTrade))
		{
			if (!this._operateType.equals(OperateType.Assign))
			{
				Object[] result2 = MakeSpotTradeOrder.isAllowTime(this._instrument);
				if (! (Boolean) result2[0])
				{
					result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
					return result;
				}
			}
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.Limit) || this._orderType.equals(OrderType.OneCancelOther)
			|| this._orderType.equals(OrderType.MarketToLimit) || this._orderType.equals(OrderType.FAK_Market)
			|| this._orderType.equals(OrderType.StopLimit))
		{
			Object[] result2 = MakeLimitOrder.isAllowTime(this._instrument);
			if (!canPlacePendingOrderAtAnyTime && ! (Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.Market))
		{
			Object[] result2 = MakeMarketOrder.isAllowTime(this._instrument);
			if (! (Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			result[0] = true;
		}
		else if (this._orderType.equals(OrderType.MarketOnOpen))
		{
			Object[] result2 = MakeMarketOnOpenOrder.isAllowTime(this._settingsManager, this._instrument, false);
			if (!canPlacePendingOrderAtAnyTime && !(Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			DateTime nextDayOpenTime = this._instrument.get_NextDayOpenTime();
			if (nextDayOpenTime != null)
			{
				result[0] = true;
			}
		}
		else if (this._orderType.equals(OrderType.MarketOnClose))
		{
			Object[] result2 = MakeMarketOnCloseOrder.isAllowTime(this._settingsManager, this._instrument, false);
			if (!canPlacePendingOrderAtAnyTime && !(Boolean) result2[0])
			{
				result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				return result;
			}
			DateTime mocTime = this._instrument.get_MOCTime();
			if (mocTime == null)
			{
				result[0] = false;
			}
			else
			{
				result[0] = true;
			}
		}
		return result;
	}

	public Object[] confirm()
	{
		Object[] result = new Object[]
			{false, ""}; //isValid,message

		Object[] result2 = this.inMakeOrderTime();
		if (! (Boolean) result2[0])
		{
			result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
			return result;
		}
		else
		{
			if(this._operateType == OperateType.MultipleClose)
			{
				ArrayList<Order> orders = new ArrayList<Order>(this._verificationOrders.values());
				Order[] orders2 = new Order[orders.size()];
				orders2 = orders.toArray(orders2);

				java.util.Arrays.sort(orders2, new Comparator<Order>()
				{
					public int compare(Order order1, Order order2)
					{
						return order1.get_Code().compareTo(order2.get_Code());
					}

					public boolean equals(Object obj)
					{
						return false;
					}
				});

				ArrayList<Guid> orderIds = new ArrayList<Guid>(orders2.length);
				for(Order order : orders2)
				{
					orderIds.add(order.get_Id());
				}
				this.asyncMultipleClose(orderIds);
			}
			else
			{
				for (Iterator<Transaction> transactions = this._verificationTransactions.values().iterator(); transactions.hasNext(); )
				{
					Transaction transaction = transactions.next();
					if(transaction.get_Orders().size() > 0)
					{
						result2 = this.getMakeOrderTime(transaction);
						if (! (Boolean) result2[0])
						{
							result[1] = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
							continue;
						}

						transaction.makeOrderConfirm(this._operateType == OperateType.LimitStop ? OperateType.Limit : this._operateType, (DateTime)result2[2],
							(DateTime)result2[3], (DateTime)result2[4]);
						result[0] = true;
					}
				}
			}
		}
		return result;
	}

	private void asyncMultipleClose(ArrayList<Guid> orderIds)
	{
		MultipleCloseCallback placeCallback = new MultipleCloseCallback(this);
		TradingConsole.traceSource.trace(TraceType.Information, "begin MultipleClose");
		Guid[] orders = orderIds.toArray(new Guid[orderIds.size()]);
		this._tradingConsole.get_TradingConsoleServer().beginMultipleClose(orders, placeCallback, null);
	}

	public void answer(BigDecimal answerLot)
	{
		Order[] orders = new Order[this._verificationOrders.values().size()];
		orders = this._verificationOrders.values().toArray(orders);
		Arrays.sort(orders, Order.orderComparatorForAdjustingLot);

		ArrayList<RelationOrder> orderRelationList = new ArrayList<RelationOrder>();
		for (Order order : orders)
		{
			if(!order.get_IsOpen() && order.get_RelationOrders() != null)
			{
				orderRelationList.addAll(order.get_RelationOrders().values());
			}
		}
		RelationOrder[] orderRelations = new RelationOrder[orderRelationList.size()];
		orderRelations = orderRelationList.toArray(orderRelations);
		Arrays.sort(orderRelations, RelationOrder.comparatorForAdjustingLot);

		BigDecimal leftBuyLot = answerLot;
		BigDecimal leftSellLot = answerLot;

		for(RelationOrder relationOrder : orderRelations)
		{
			boolean isBuy = !relationOrder.get_IsBuy();
			BigDecimal leftLot = isBuy ? leftBuyLot : leftSellLot;
			if (leftLot.compareTo(BigDecimal.ZERO) > 0)
			{
				BigDecimal closeLot = relationOrder.get_CloseLot();
				closeLot = closeLot.compareTo(leftLot) > 0 ? leftLot : closeLot;
				relationOrder.set_LiqLot(closeLot);

				if(isBuy)
				{
					leftBuyLot = leftBuyLot.subtract(closeLot);
				}
				else
				{
					leftSellLot = leftSellLot.subtract(closeLot);
				}
			}
			else
			{
				relationOrder.set_LiqLot(BigDecimal.ZERO);
				for (Order order : orders)
				{
					if (!order.get_IsOpen() && order.get_IsBuy() == isBuy)
					{
						if(order.get_RelationOrders().get(relationOrder.get_OpenOrderId()) == relationOrder)
						{
							order.get_RelationOrders().remove(relationOrder.get_OpenOrderId());
						}
					}
				}
			}
		}

		for (Order order : orders)
		{
			if(!order.get_IsOpen())
			{
				BigDecimal totalClosLot = BigDecimal.ZERO;
				for(RelationOrder relationOrder : order.get_RelationOrders().values())
				{
					totalClosLot = totalClosLot.add(relationOrder.get_CloseLot());
				}
				if(totalClosLot.compareTo(BigDecimal.ZERO) > 0)
				{
					order.adjustLot(totalClosLot);
				}
				else
				{
					this.removeOrder(order);
				}
			}
			else
			{
				boolean isBuy = order.get_IsBuy();
				BigDecimal leftLot = isBuy ? leftBuyLot : leftSellLot;
				if (leftLot.compareTo(BigDecimal.ZERO) > 0)
				{
					BigDecimal usedLot = order.adjustLot(leftLot);
					if(order.get_IsBuy())
					{
						leftBuyLot = leftBuyLot.subtract(usedLot);
					}
					else
					{
						leftSellLot = leftSellLot.subtract(usedLot);
					}
				}
				else if(order.get_IsBuy() == isBuy)
				{
					this.removeOrder(order);
				}
			}
		}
	}

	private void removeOrder(Order order)
	{
		order.adjustLot(BigDecimal.ZERO);
		this._verificationOrders.remove(order.get_Id());
		for (Transaction transaction : this._verificationTransactions.values())
		{
			transaction.get_Orders().remove(order.get_Id());
		}
	}

	public void clear()
	{
		this._verificationTransactions.clear();
		this._verificationOrders.clear();
	}

	public boolean isSpot()
	{
		for (Iterator<Order> iterator = this._verificationOrders.values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			if(order.get_Transaction().get_OrderType() == OrderType.SpotTrade) return true;
		}
		return false;
	}

	public static class MultipleCloseCallback implements IAsyncCallback
	{
		private VerificationOrderManager _owner;

		public MultipleCloseCallback(VerificationOrderManager owner)
		{
			this._owner = owner;
		}

	     public void asyncCallback(SignalObject signal){
			 TradingConsole.traceSource.trace(TraceType.Information, "end MultipleClose");
			AwtSafelyPlaceProcessor placeProcessor = new AwtSafelyPlaceProcessor(this._owner, signal);
			SwingUtilities.invokeLater(placeProcessor);

		 }


		public void asyncCallback(IAsyncResult asyncResult)
		{

		}
	}

	public static class AwtSafelyPlaceProcessor implements Runnable
	{
		private VerificationOrderManager _owner;
		private SignalObject signal;

		public AwtSafelyPlaceProcessor(VerificationOrderManager owner, SignalObject signal)
		{
			this._owner = owner;
			this.signal = signal;
		}

		public void run()
		{
			if(this.signal.getIsError()){
				TradingConsole.traceSource.trace(TraceType.Error, "MultipleClose failed");
				return;
			}
			MultipleCloseResult result =new MultipleCloseResult(this.signal.getResult());

			if(result.get_TransactionError() == TransactionError.OK)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "MultipleClose successfully");

				Transaction.execute2(this._owner._tradingConsole, this._owner._settingsManager, (XmlElement)result.get_XmlTran());
				//Account.refresh(this._owner._tradingConsole, this._owner._settingsManager, (XmlElement)result.get_XmlAccount());
				Account.execute2(this._owner._tradingConsole, this._owner._settingsManager, (XmlElement)result.get_XmlAccount());
			}
			else
			{
				if(!result.get_TransactionError().equals(TransactionError.MultipleCloseOrderNotFound)
				   && !result.get_TransactionError().equals(TransactionError.MultipleCloseHasNoLotBalance))
				{
					TradingConsole.traceSource.trace(TraceType.Warning, "MultipleClose is not accepted, TransactionError = " + result.get_TransactionError());
					AlertDialogForm.showDialog(this._owner._verificationOrderForm, null, true, TransactionError.getCaption(TransactionError.RuntimeError));
				}
				else
				{
					TradingConsole.traceSource.trace(TraceType.Error, "MultipleClose failed, TransactionError = " + result.get_TransactionError());
				}
			}
		}
	}
}
