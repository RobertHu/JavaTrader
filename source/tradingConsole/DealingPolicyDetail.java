package tradingConsole;

import java.math.*;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.settings.*;
import tradingConsole.enumDefine.AllowedOrderSides;
import framework.lang.Enum;

public class DealingPolicyDetail
{
	private Guid _dealingPolicyId;
	private Guid _instrumentId;

	private BigDecimal _maxDQLot;
	private BigDecimal _maxOtherLot;
	private BigDecimal _dqQuoteMinLot;

	private int _acceptDQVariation;
	private int _acceptLmtVariation;
	private int _acceptCloseLmtVariation;
	private int _cancelLmtVariation;
	private AllowedOrderSides _allowAddNewPosition = AllowedOrderSides.AllowAll;

	public DealingPolicyDetail(DataRow dataRow)
	{
		this._dealingPolicyId = (Guid) (dataRow.get_Item("DealingPolicyID"));
		this._instrumentId = (Guid) (dataRow.get_Item("InstrumentID"));
		this.setValue(dataRow);
	}

	public DealingPolicyDetail(Guid dealingPolicyId, Guid instrumentId)
	{
		this._dealingPolicyId = dealingPolicyId;
		this._instrumentId = instrumentId;
	}

	public Guid get_DealingPolicyId()
	{
		return this._dealingPolicyId;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public BigDecimal get_MaxDQLot()
	{
		return this._maxDQLot;
	}

	public BigDecimal get_MaxOtherLot()
	{
		return this._maxOtherLot;
	}

	public BigDecimal get_DQQuoteMinLot()
	{
		return this._dqQuoteMinLot;
	}

	public int get_AcceptDQVariation()
	{
		return this._acceptDQVariation;
	}

	public int get_AcceptLmtVariation()
	{
		return this._acceptLmtVariation;
	}

	public int get_AcceptCloseLmtVariation()
	{
		return this._acceptCloseLmtVariation;
	}

	public int get_CancelLmtVariation()
	{
		return this._cancelLmtVariation;
	}

	public boolean getAllowAddNewPosition(boolean isBuy)
	{
		if(this._allowAddNewPosition.value() == AllowedOrderSides.AllowNoe.value())
		{
			return false;
		}
		else
		{
			return true;//Let Transaction server to check allow side
		}
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._maxDQLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MaxDQLot"),0.0);
		this._maxOtherLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MaxOtherLot"),0.0);
		this._dqQuoteMinLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("DQQuoteMinLot"),0.0);

		this._acceptDQVariation = AppToolkit.convertStringToInteger(dataRow.get_Item("AcceptDQVariation"), 0);
		this._acceptLmtVariation = AppToolkit.convertStringToInteger(dataRow.get_Item("AcceptLmtVariation"), 0);
		this._acceptCloseLmtVariation = AppToolkit.convertStringToInteger(dataRow.get_Item("AcceptCloseLmtVariation"), 0);
		this._cancelLmtVariation = AppToolkit.convertStringToInteger(dataRow.get_Item("CancelLmtVariation"), 0);
		this._allowAddNewPosition = Enum.valueOf(AllowedOrderSides.class, (Short)dataRow.get_Item("AllowedNewTradeSides"));
	}

	private void setValue(XmlAttributeCollection dealingPolicyDetailNode)
	{
		for (int i = 0; i < dealingPolicyDetailNode.get_Count(); i++)
		{
			String nodeName = dealingPolicyDetailNode.get_ItemOf(i).get_LocalName();
			String nodeValue = dealingPolicyDetailNode.get_ItemOf(i).get_Value();
			if (nodeName.equals("MaxDQLot"))
			{
				this._maxDQLot = new BigDecimal(nodeValue);
			}
			if (nodeName.equals("MaxOtherLot"))
			{
				this._maxOtherLot = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("DQQuoteMinLot"))
			{
				this._dqQuoteMinLot = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("AcceptDQVariation"))
			{
				this._acceptDQVariation = Integer.valueOf(nodeValue).intValue();
			}
			else if (nodeName.equals("AcceptLmtVariation"))
			{
				this._acceptLmtVariation = Integer.valueOf(nodeValue).intValue();
			}
			else if (nodeName.equals("AcceptCloseLmtVariation"))
			{
				this._acceptCloseLmtVariation = Integer.valueOf(nodeValue).intValue();
			}
			else if (nodeName.equals("CancelLmtVariation"))
			{
				this._cancelLmtVariation = Integer.valueOf(nodeValue).intValue();
			}
			else if (nodeName.equals("AllowedNewTradeSides"))
			{
				this._allowAddNewPosition = Enum.valueOf(AllowedOrderSides.class, Integer.valueOf(nodeValue).intValue());
			}
		}
	}

	public static void updateDealingPolicyDetail(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode dealingPolicyDetailNode, String updateType)
	{
		if (dealingPolicyDetailNode == null)
		{
			return;
		}

		XmlAttributeCollection dealingPolicyDetailCollection = dealingPolicyDetailNode.get_Attributes();
		Guid instrumentId = new Guid(dealingPolicyDetailCollection.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}

		Guid oldInstrumentId = null;
		if (updateType.equals("Modify"))
		{
			if (dealingPolicyDetailNode.get_ChildNodes().get_Count() > 0)
			{
				oldInstrumentId = new Guid(dealingPolicyDetailNode.get_ChildNodes().itemOf(0).get_Attributes().get_ItemOf("InstrumentID").get_Value());
			}
		}

		Guid dealingPolicyID = new Guid(dealingPolicyDetailCollection.get_ItemOf("DealingPolicyID").get_Value());
		DealingPolicyDetail dealingPolicyDetail = settingsManager.getDealingPolicyDetail(dealingPolicyID, instrumentId);
		if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			if (dealingPolicyDetail != null)
			{
				dealingPolicyDetail.setValue(dealingPolicyDetailCollection);
			}
			else
			{
				if (dealingPolicyID != null && oldInstrumentId != null)
				{
					DealingPolicyDetail oldDealingPolicyDetail = settingsManager.getDealingPolicyDetail(dealingPolicyID, oldInstrumentId);
					if (oldDealingPolicyDetail != null)
					{
						oldDealingPolicyDetail.setValue(dealingPolicyDetailCollection);
					}
					else
					{
						//has change it
						oldDealingPolicyDetail = new DealingPolicyDetail(dealingPolicyID, oldInstrumentId);
						oldDealingPolicyDetail.setValue(dealingPolicyDetailCollection);
						settingsManager.setDealingPolicyDetail(oldDealingPolicyDetail);
					}
				}
				else
				{
					dealingPolicyDetail = new DealingPolicyDetail(dealingPolicyID, instrumentId);
					dealingPolicyDetail.setValue(dealingPolicyDetailCollection);
					settingsManager.setDealingPolicyDetail(dealingPolicyDetail);
				}
			}
		}
		else if (updateType.equals("Delete"))
		{
			if (dealingPolicyDetail != null)
			{
				settingsManager.removeDealingPolicyDetail(dealingPolicyDetail);
			}
		}
	}

	public static void updateDealingPolicyDetails(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode dealingPolicyDetailNodes, String updateType)
	{
		for (int i = 0; i < dealingPolicyDetailNodes.get_ChildNodes().get_Count(); i++)
		{
			XmlNode dealingPolicyDetailNode = dealingPolicyDetailNodes.get_ChildNodes().itemOf(i);
			DealingPolicyDetail.updateDealingPolicyDetail(tradingConsole, settingsManager, dealingPolicyDetailNode, updateType);
		}
	}
}
