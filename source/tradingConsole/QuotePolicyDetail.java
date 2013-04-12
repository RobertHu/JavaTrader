package tradingConsole;

import framework.Guid;
import framework.data.DataRow;
import framework.xml.XmlAttributeCollection;
import framework.xml.XmlNode;
import tradingConsole.settings.SettingsManager;
import framework.data.DataSet;

public class QuotePolicyDetail
{
	private Guid _quotePolicyId;
	private Guid _instrumentId;
	private boolean _isOriginHiLo;
	private int _hiLoSpread;
	private int _spread;

	public QuotePolicyDetail(DataRow dataRow)
	{
		this._quotePolicyId = (Guid)dataRow.get_Item("QuotePolicyID");
		this._instrumentId = (Guid)dataRow.get_Item("InstrumentID");
		this._isOriginHiLo = ((Boolean)dataRow.get_Item("IsOriginHiLo")).booleanValue();
		this._hiLoSpread = ( (Integer)dataRow.get_Item("HiLoSpread")).intValue();
		this._spread = ( (Integer)dataRow.get_Item("SpreadPoints")).intValue();
	}

	public QuotePolicyDetail(Guid quotePolicyId, Guid instrumentId)
	{
		this._quotePolicyId = quotePolicyId;
		this._instrumentId = instrumentId;
	}

	public void replace(DataRow dataRow)
	{
		this._isOriginHiLo = ((Boolean)dataRow.get_Item("IsOriginHiLo")).booleanValue();
		this._hiLoSpread = ( (Integer)dataRow.get_Item("HiLoSpread")).intValue();
		this._spread = ( (Integer)dataRow.get_Item("SpreadPoints")).intValue();
	}

	public Guid get_QuotePolicyId()
	{
		return this._quotePolicyId;
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public boolean get_IsOriginHiLo()
	{
		return this._isOriginHiLo;
	}

	public int get_HiLoSpread()
	{
		return this._hiLoSpread;
	}

	public int get_Spread()
	{
		return this._spread;
	}

	public static void updateQuotePolicyDetail(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode quotePolicyDetailNode, String updateType)
	{
		if (quotePolicyDetailNode == null)
		{
			return;
		}

		XmlAttributeCollection tradePolicyDetailCollection = quotePolicyDetailNode.get_Attributes();
		Guid instrumentId = new Guid(tradePolicyDetailCollection.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}

		Guid oldInstrumentId = null;
		if (updateType.equals("Modify"))
		{
			if (quotePolicyDetailNode.get_ChildNodes().get_Count() > 0)
			{
				oldInstrumentId = new Guid(quotePolicyDetailNode.get_ChildNodes().itemOf(0).get_Attributes().get_ItemOf("InstrumentID").get_Value());
			}
		}

		Guid quotePolicyId = new Guid(tradePolicyDetailCollection.get_ItemOf("QuotePolicyID").get_Value());
		QuotePolicyDetail quotePolicyDetail = settingsManager.getQuotePolicyDetail(instrumentId);
		if (updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			if (quotePolicyDetail != null)
			{
				quotePolicyDetail.update(tradePolicyDetailCollection);
			}
			else
			{
				if (quotePolicyId != null && oldInstrumentId != null)
				{
					QuotePolicyDetail oldQuotePolicyDetail = settingsManager.getQuotePolicyDetail(oldInstrumentId);
					if (oldQuotePolicyDetail != null)
					{
						oldQuotePolicyDetail.update(tradePolicyDetailCollection);
					}
					else
					{
						//has change it
						oldQuotePolicyDetail = new QuotePolicyDetail(quotePolicyId, oldInstrumentId);
						oldQuotePolicyDetail.update(tradePolicyDetailCollection);
						settingsManager.setQuotePolicyDetail(oldQuotePolicyDetail);
					}
				}
				else
				{
					quotePolicyDetail = new QuotePolicyDetail(quotePolicyId, instrumentId);
					quotePolicyDetail.update(tradePolicyDetailCollection);
					settingsManager.setQuotePolicyDetail(quotePolicyDetail);
				}
			}
		}
		else
		{
			Guid userId = settingsManager.get_Customer().get_UserId();
			DataSet dataSet = tradingConsole.get_TradingConsoleServer().getQuotePolicyDetailsAndRefreshInstrumentsState(userId);
			if(dataSet != null && dataSet.get_Tables().get_Item("QuotePolicyDetail") != null)
			{
				settingsManager.updateQuotePolicyDetails(dataSet.get_Tables().get_Item("QuotePolicyDetail"));
			}
		}
	}

	private void update(XmlAttributeCollection tradePolicyDetailCollection)
	{
		for (int i = 0; i < tradePolicyDetailCollection.get_Count(); i++)
		{
			String nodeName = tradePolicyDetailCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = tradePolicyDetailCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("IsOriginHiLo"))
			{
				this._isOriginHiLo = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("HiLoSpread"))
			{
				this._hiLoSpread = Integer.parseInt(nodeValue);
			}
			else if(nodeName.equals("SpreadPoints"))
			{
				this._spread = Integer.parseInt(nodeValue);
			}
		}
	}
}
