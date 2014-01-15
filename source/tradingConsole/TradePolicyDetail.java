package tradingConsole;

import java.math.BigDecimal;

import framework.Guid;
import framework.data.DataRow;
import framework.xml.XmlNode;
import tradingConsole.settings.SettingsManager;
import framework.xml.XmlAttributeCollection;
import framework.StringHelper;
import framework.DBNull;
import tradingConsole.enumDefine.physical.PhysicalTradeSide;
import tradingConsole.enumDefine.PaymentForm;

public class TradePolicyDetail
{
	private Guid _tradePolicyId;
	private Guid _instrumentId;
	private Guid _volumeNecessaryId;
	private Guid _instalmentPolicyId;
	private BigDecimal _contractSize;
	private boolean _isTradeActive;
	private BigDecimal _commissionCloseD;
	private BigDecimal _commissionCloseO;
	private BigDecimal _minCommissionOpen;
	private BigDecimal _minCommissionClose;
	private BigDecimal _minOpen;
	private BigDecimal _openMultiplier;
	private BigDecimal _minClose;
	private BigDecimal _closeMultiplier;
	private BigDecimal _defaultLot;
	private BigDecimal _marginD;
	private BigDecimal _marginLockedD;
	private BigDecimal _marginO;
	private BigDecimal _marginLockedO;
	private int _option;
	private int _necessaryRound;

	private boolean _isAcceptNewStop;
	private boolean _isAcceptNewLimit;
	private boolean _isAcceptNewMOOMOC;
	private boolean _allowIfDone;

	private Guid _interestRateId;
	private BigDecimal _interestRateBuy;
	private BigDecimal _interestRateSell;

	private Guid _deliveryChargeId;

	private int _dQMaxMove = 0;
	private BigDecimal _pairRelationFactor;

	private boolean _goodTillDate = false;
	private boolean _goodTillMonthDayOrder = true;
	private boolean _goodTillMonthSession = false;
	private boolean _changePlacedOrderAllowed = false;
	private boolean _goodTillMonthGTM = false;
	private boolean _goodTillMonthGTF = false;
	private boolean _multipleCloseAllowed = false;
	private boolean _canPlaceMatchOrder = false;
	private boolean _allowNewOCO = false;

	private int _allowedPhysicalTradeSides;
	private BigDecimal _discountOfOdd;
	private BigDecimal _valueDiscountAsMargin;
	private BigDecimal _instalmentPledgeDiscount;
	private BigDecimal _physicalMinDeliveryQuantity;
	private BigDecimal _physicalDeliveryIncremental;
	private BigDecimal _shortSellDownPayment;
	private BigDecimal _partPaidPhysicalNecessary;
	private int _paymentForm;

	public boolean get_GoodTillDate()
	{
		return this._goodTillDate;
	}

	public boolean get_GoodTillMonthDayOrder()
	{
		return this._goodTillMonthDayOrder;
	}

	public boolean get_GoodTillMonthSession()
	{
		return this._goodTillMonthSession;
	}

	public boolean get_AllowNewOCO()
	{
		return this._allowNewOCO;
	}

	public boolean  get_ChangePlacedOrderAllowed()
	{
		return this._changePlacedOrderAllowed;
	}

	public boolean get_GoodTillMonthGTM()
	{
		return this._goodTillMonthGTM;
	}

	public boolean get_GoodTillMonthGTF()
	{
		return this._goodTillMonthGTF;
	}

	public boolean get_IsAcceptNewStop()
	{
		return this._isAcceptNewStop;
	}
	public boolean get_IsAcceptNewLimit()
	{
		return this._isAcceptNewLimit;
	}
	public boolean get_IsAcceptNewMOOMOC()
	{
		return this._isAcceptNewMOOMOC;
	}

	public boolean get_MultipleCloseAllowed()
	{
		return this._multipleCloseAllowed;
	}

	public boolean get_AllowIfDone()
	{
		return this._allowIfDone;
	}

	public BigDecimal get_MinOpen()
	{
		return this._minOpen;
	}

	public BigDecimal get_MinCommissionOpen()
	{
		return this._minCommissionOpen;
	}

	public BigDecimal get_MinCommissionClose()
	{
		return this._minCommissionClose;
	}

	public BigDecimal get_OpenMultiplier()
	{
		return this._openMultiplier;
	}

	public BigDecimal get_MinClose()
	{
		return this._minClose;
	}

	public BigDecimal get_DefaultLot()
	{
		return this._defaultLot;
	}

	public BigDecimal get_MarginD()
	{
		return this._marginD;
	}

	public BigDecimal get_PartPaidPhysicalNecessary()
	{
		return this._partPaidPhysicalNecessary;
	}

	public int get_NecessaryRound()
	{
		return this._necessaryRound;
	}

	public BigDecimal get_MarginLockedD()
	{
		return this._marginLockedD;
	}

	public BigDecimal get_MarginO()
	{
		return this._marginO;
	}

	public BigDecimal get_MarginLockedO()
	{
		return this._marginLockedO;
	}

	public BigDecimal get_CloseMultiplier()
	{
		return this._closeMultiplier;
	}

	public int get_Option()
	{
		return this._option;
	}

	public BigDecimal get_ContractSize()
	{
		return this._contractSize;
	}

	public Guid get_TradePolicyId()
	{
		return this._tradePolicyId;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public Guid get_VolumeNecessaryId()
	{
		return this._volumeNecessaryId;
	}

	public Guid get_InstalmentPolicyId()
	{
		return this._instalmentPolicyId;
	}

	public BigDecimal get_CommissionCloseD()
	{
		return this._commissionCloseD;
	}

	public BigDecimal get_CommissionCloseO()
	{
		return this._commissionCloseO;
	}

	public boolean get_IsTradeActive()
	{
		return this._isTradeActive;
	}

	public Guid get_InterestRateId()
	{
		return this._interestRateId;
	}

	public Guid get_DeliveryChargeId()
	{
		return this._deliveryChargeId;
	}

	public BigDecimal get_InterestRateBuy()
	{
		return this._interestRateBuy;
	}

	public void set_InterestRateBuy(BigDecimal value)
	{
		this._interestRateBuy = value;
	}

	public BigDecimal get_InterestRateSell()
	{
		return this._interestRateSell;
	}

	public void set_InterestRateSell(BigDecimal value)
	{
		this._interestRateSell = value;
	}

	public int get_DQMaxMove()
	{
		return this._dQMaxMove;
	}

	public BigDecimal get_PairRelationFactor()
	{
		return this._pairRelationFactor;
	}

	public boolean get_CanPlaceMatchOrder()
	{
		return this._canPlaceMatchOrder;
	}

	public boolean isAllowed(PhysicalTradeSide physicalTradeSide)
	{
		return (physicalTradeSide.value() & this._allowedPhysicalTradeSides) == physicalTradeSide.value();
	}

	public boolean isAllowed(PaymentForm paymentForm)
	{
		return (paymentForm.value() & this._paymentForm) == paymentForm.value();
	}

	public BigDecimal get_DiscountOfOdd()
	{
		return this._discountOfOdd;
	}

	public BigDecimal get_ValueDiscountAsMargin()
	{
		return this._valueDiscountAsMargin;
	}

	public BigDecimal get_ShortSellDownPayment()
	{
		return this._shortSellDownPayment;
	}

	public BigDecimal get_InstalmentPledgeDiscount()
	{
		return this._instalmentPledgeDiscount;
	}

	public BigDecimal get_PhysicalMinDeliveryQuantity()
	{
		return this._physicalMinDeliveryQuantity;
	}

	public BigDecimal get_PhysicalDeliveryIncremental()
	{
		return this._physicalDeliveryIncremental;
	}


	public TradePolicyDetail(Guid tradePolicyId, Guid instrumentId)
	{
		this._tradePolicyId = tradePolicyId;
		this._instrumentId = instrumentId;
	}

	public TradePolicyDetail(DataRow dataRow)
	{
		this._tradePolicyId = (Guid) (dataRow.get_Item("TradePolicyID"));
		this._instrumentId = (Guid) (dataRow.get_Item("InstrumentID"));
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._contractSize = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("ContractSize"), 0.0);
		this._volumeNecessaryId = dataRow.get_Item("VolumeNecessaryId").equals(DBNull.value) ? null : (Guid) (dataRow.get_Item("VolumeNecessaryId"));
		this._instalmentPolicyId = dataRow.get_Item("InstalmentPolicyId").equals(DBNull.value) ? null : (Guid) (dataRow.get_Item("InstalmentPolicyId"));
		this._isTradeActive = (Boolean) dataRow.get_Item("IsTradeActive");
		this._commissionCloseD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("CommissionCloseD"),0.0);
		this._commissionCloseO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("CommissionCloseO"),0.0);
		this._minCommissionOpen = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MinCommissionOpen"),0.0);
		this._minCommissionClose = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MinCommissionClose"),0.0);
		this._minOpen = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MinOpen"),0.0);
		this._openMultiplier = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("OpenMultiplier"),0.0);
		this._minClose = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MinClose"),0.0);
		this._defaultLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("DefaultLot"),0.0);
		this._closeMultiplier = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("CloseMultiplier"),0.0);
		this._marginD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginD"),1.0);
		this._marginLockedD = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginLockedD"),1.0);
		this._marginO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginO"),1.0);
		this._marginLockedO = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MarginLockedO"),1.0);
		this._option = (Integer) dataRow.get_Item("Option");
		this._necessaryRound = (Integer) dataRow.get_Item("NecessaryRound");

		this._isAcceptNewStop = (Boolean) dataRow.get_Item("IsAcceptNewStop");
		this._isAcceptNewLimit = (Boolean) dataRow.get_Item("IsAcceptNewLimit");
		this._isAcceptNewMOOMOC = (Boolean) dataRow.get_Item("IsAcceptNewMOOMOC");
		this._multipleCloseAllowed = (Boolean) dataRow.get_Item("MultipleCloseAllowed");
		this._allowIfDone = (Boolean) dataRow.get_Item("AllowIfDone");

		this._deliveryChargeId = AppToolkit.isDBNull(dataRow.get_Item("DeliveryChargeID")) ? null : (Guid) dataRow.get_Item("DeliveryChargeID");

		this._interestRateId = AppToolkit.isDBNull(dataRow.get_Item("InterestRateID")) ? null : (Guid) dataRow.get_Item("InterestRateID");
		this._interestRateBuy = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateBuy")) ? null : (BigDecimal) (dataRow.get_Item("InterestRateBuy")));
		this._interestRateSell = (AppToolkit.isDBNull(dataRow.get_Item("InterestRateSell")) ? null : (BigDecimal) (dataRow.get_Item("InterestRateSell")));

	    this._dQMaxMove = (Integer) dataRow.get_Item("DQMaxMove");
		this._pairRelationFactor = (AppToolkit.isDBNull(dataRow.get_Item("PairRelationFactor")) ? BigDecimal.ONE : (BigDecimal) (dataRow.get_Item("PairRelationFactor")));

		this._goodTillDate = (Boolean) dataRow.get_Item("GoodTillDate");
		this._goodTillMonthDayOrder = (Boolean) dataRow.get_Item("GoodTillMonthDayOrder");
		this._goodTillMonthSession = (Boolean) dataRow.get_Item("GoodTillMonthSession");
		if(dataRow.get_Table().get_Columns().contains("ChangePlacedOrderAllowed"))
		{
			this._changePlacedOrderAllowed = (Boolean)dataRow.get_Item("ChangePlacedOrderAllowed");
		}
		this._goodTillMonthGTM = (Boolean) dataRow.get_Item("GoodTillMonthGTM");
		this._goodTillMonthGTF = (Boolean) dataRow.get_Item("GoodTillMonthGTF");
		this._canPlaceMatchOrder = (Boolean) dataRow.get_Item("CanPlaceMatchOrder");
		this._allowNewOCO = (Boolean) dataRow.get_Item("AllowNewOCO");
		this._paymentForm = (Integer)dataRow.get_Item("PaymentForm");

		this._allowedPhysicalTradeSides = (Integer)dataRow.get_Item("AllowedPhysicalTradeSides");
		this._discountOfOdd = (BigDecimal) dataRow.get_Item("DiscountOfOdd");
		this._valueDiscountAsMargin = (BigDecimal) dataRow.get_Item("ValueDiscountAsMargin");

		this._shortSellDownPayment = dataRow.get_Item("ShortSellDownPayment") != DBNull.value ? (BigDecimal) dataRow.get_Item("ShortSellDownPayment") : BigDecimal.ZERO;
		this._partPaidPhysicalNecessary = dataRow.get_Item("PartPaidPhysicalNecessary") != DBNull.value ? (BigDecimal) dataRow.get_Item("PartPaidPhysicalNecessary") : BigDecimal.ZERO;

		if(dataRow.get_Table().get_Columns().contains("InstalmentPledgeDiscount")
			&& dataRow.get_Item("InstalmentPledgeDiscount") != DBNull.value)
		{
			this._instalmentPledgeDiscount = (BigDecimal)dataRow.get_Item("InstalmentPledgeDiscount");
		}
		else
		{
			this._instalmentPledgeDiscount = BigDecimal.ZERO;
		}
		this._physicalMinDeliveryQuantity = (BigDecimal) dataRow.get_Item("PhysicalMinDeliveryQuantity");
		this._physicalDeliveryIncremental = (BigDecimal) dataRow.get_Item("PhysicalDeliveryIncremental");
	}

	private void setValue(XmlAttributeCollection tradePolicyDetailCollection)
	{
		for (int i = 0; i < tradePolicyDetailCollection.get_Count(); i++)
		{
			String nodeName = tradePolicyDetailCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = tradePolicyDetailCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("TradePolicyID"))
			{
				this._tradePolicyId = new Guid(nodeValue);
			}
			else if (nodeName.equals("InstrumentID"))
			{
				this._instrumentId = new Guid(nodeValue);
			}
			else if (nodeName.equals("VolumeNecessaryId"))
			{
				this._volumeNecessaryId = StringHelper.isNullOrEmpty(nodeValue) ? null : new Guid(nodeValue);
			}
			else if (nodeName.equals("InstalmentPolicyId"))
			{
				this._instalmentPolicyId = StringHelper.isNullOrEmpty(nodeValue) ? null : new Guid(nodeValue);
			}
			else if (nodeName.equals("ContractSize"))
			{
				this._contractSize = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("IsTradeActive"))
			{
				this._isTradeActive = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("CommissionCloseD"))
			{
				this._commissionCloseD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("CommissionCloseO"))
			{
				this._commissionCloseO = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MinCommissionOpen"))
			{
				this._minCommissionOpen = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MinCommissionClose"))
			{
				this._minCommissionClose = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MinOpen"))
			{
				this._minOpen = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("OpenMultiplier"))
			{
				this._openMultiplier = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MinClose"))
			{
				this._minClose = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("DefaultLot"))
			{
				this._defaultLot = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginD"))
			{
				this._marginD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginLockedD"))
			{
				this._marginLockedD = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginO"))
			{
				this._marginO = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MarginLockedO"))
			{
				this._marginLockedO = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("CloseMultiplier"))
			{
				this._closeMultiplier = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("Option"))
			{
				this._option = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("NecessaryRound"))
			{
				this._necessaryRound = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsAcceptNewStop"))
			{
				this._isAcceptNewStop = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsAcceptNewLimit"))
			{
				this._isAcceptNewLimit = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsAcceptNewMOOMOC"))
			{
				this._isAcceptNewMOOMOC = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("MultipleCloseAllowed"))
			{
				this._multipleCloseAllowed = Boolean.valueOf(nodeValue);
			}
			else if(nodeName.equals("ChangePlacedOrderAllowed"))
			{
				this._changePlacedOrderAllowed = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowIfDone"))
			{
				this._allowIfDone = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equalsIgnoreCase("InterestRateID"))
			{
				this._interestRateId = new Guid(nodeValue);
			}
			else if (nodeName.equalsIgnoreCase("DeliveryChargeId"))
			{
				if(StringHelper.isNullOrEmpty(nodeValue))
				{
					this._deliveryChargeId = null;
				}
				else
				{
					this._deliveryChargeId = new Guid(nodeValue);
				}
			}
			else if (nodeName.equalsIgnoreCase("InterestRateBuy"))
			{
				this._interestRateBuy = AppToolkit.isDBNull(nodeValue) ? null : new BigDecimal(nodeValue);
			}
			else if (nodeName.equalsIgnoreCase("InterestRateSell"))
			{
				this._interestRateSell = AppToolkit.isDBNull(nodeValue) ? null : new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("DQMaxMove"))
			{
				this._dQMaxMove = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("PairRelationFactor"))
			{
				this._pairRelationFactor = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("GoodTillDate"))
			{
				this._goodTillDate = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("GoodTillMonthDayOrder"))
			{
				this._goodTillMonthDayOrder = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("GoodTillMonthSession"))
			{
				this._goodTillMonthSession = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("GoodTillMonthGTM"))
			{
				this._goodTillMonthGTM = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("GoodTillMonthGTF"))
			{
				this._goodTillMonthGTF = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowedPhysicalTradeSides"))
			{
				this._allowedPhysicalTradeSides = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DiscountOfOdd"))
			{
				this._discountOfOdd = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("ValueDiscountAsMargin"))
			{
				this._valueDiscountAsMargin = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("ShortSellDownPayment"))
			{
				this._shortSellDownPayment = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("PartPaidPhysicalNecessary"))
			{
				this._partPaidPhysicalNecessary = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("InstalmentPledgeDiscount"))
			{
				this._instalmentPledgeDiscount = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("PhysicalMinDeliveryQuantity"))
			{
				this._physicalMinDeliveryQuantity = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("PhysicalDeliveryIncremental"))
			{
				this._physicalDeliveryIncremental = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("CanPlaceMatchOrder"))
			{
				this._canPlaceMatchOrder = Boolean.valueOf(nodeValue);
			}
			else if(nodeName.equals("AllowNewOCO"))
			{
				this._allowNewOCO = Boolean.valueOf(nodeValue);
			}
			else if(nodeName.equals("PaymentForm"))
			{
				this._paymentForm = Integer.parseInt(nodeValue);
			}
		}
	}

	public static void updateTradePolicyDetail(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode tradePolicyDetailNode, String updateType)
	{
		if (tradePolicyDetailNode == null)
		{
			return;
		}
		XmlAttributeCollection tradePolicyDetailCollection = tradePolicyDetailNode.get_Attributes();
		Guid instrumentId = new Guid(tradePolicyDetailCollection.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}

		Guid oldInstrumentId = null;
		if (updateType.equals("Modify"))
		{
			if (tradePolicyDetailNode.get_ChildNodes().get_Count() > 0)
			{
				oldInstrumentId = new Guid(tradePolicyDetailNode.get_ChildNodes().itemOf(0).get_Attributes().get_ItemOf("InstrumentID").get_Value());
			}
		}

		Guid tradePolicyId = new Guid(tradePolicyDetailCollection.get_ItemOf("TradePolicyID").get_Value());
		TradePolicyDetail tradePolicyDetail = settingsManager.getTradePolicyDetail(tradePolicyId, instrumentId);
		if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			if (tradePolicyDetail != null)
			{
				tradePolicyDetail.setValue(tradePolicyDetailCollection);

				if(tradePolicyDetail.get_DeliveryChargeId() != null
				   && !settingsManager.containsDeliveryCharge(tradePolicyDetail.get_DeliveryChargeId()))
				{
					tradingConsole.get_MainForm().refreshSystem();
				}
				if(tradePolicyDetail.get_InstalmentPolicyId() != null
				   && !settingsManager.containsInstalmentPolicy(tradePolicyDetail.get_InstalmentPolicyId()))
				{
					tradingConsole.get_MainForm().refreshSystem();
				}

			}
			else
			{
				if (tradePolicyId != null && oldInstrumentId != null)
				{
					TradePolicyDetail oldTradePolicyDetail = settingsManager.getTradePolicyDetail(tradePolicyId, oldInstrumentId);
					if (oldTradePolicyDetail != null)
					{
						oldTradePolicyDetail.setValue(tradePolicyDetailCollection);
					}
					else
					{
						//has change it
						oldTradePolicyDetail = new TradePolicyDetail(tradePolicyId, oldInstrumentId);
						oldTradePolicyDetail.setValue(tradePolicyDetailCollection);
						settingsManager.setTradePolicyDetail(oldTradePolicyDetail);
					}
				}
				else
				{
					tradePolicyDetail = new TradePolicyDetail(tradePolicyId, instrumentId);
					tradePolicyDetail.setValue(tradePolicyDetailCollection);
					settingsManager.setTradePolicyDetail(tradePolicyDetail);
				}
			}
			if (tradePolicyDetailCollection.get_ItemOf("InterestRateID") != null)
			{
				Guid interestRateId = new Guid(tradePolicyDetailCollection.get_ItemOf("InterestRateID").get_Value());
				tradingConsole.getInterestRate(interestRateId);
			}
		}
		else if (updateType.equals("Delete"))
		{
			if (tradePolicyDetail != null)
			{
				settingsManager.removeTradePolicyDetail(tradePolicyDetail);
			}
		}

		for(Account account : settingsManager.get_Accounts().values())
		{
			account.clearIsSplitLot();
		}
	}
}
