package tradingConsole.physical;

import tradingConsole.enumDefine.physical.ScrapDepositStatus;
import framework.DateTime;
import framework.xml.XmlAttributeCollection;
import java.math.BigDecimal;
import framework.data.DataRow;
import framework.Guid;
import framework.xml.XmlNode;
import tradingConsole.settings.SettingsManager;
import framework.DBNull;
import framework.lang.Enum;
import tradingConsole.Instrument;

public class ScrapDeposit extends PendingInventory
{
	private ScrapInstrument scrapInstrument;
	private String scrapInstrumentCode;
	private ScrapDepositStatus scrapDepositStatus;
	private DateTime settlementTime;
	private Instrument instrument;
	private int decimals;

	public String get_Instrument()
	{
		return this.scrapInstrument == null ? scrapInstrumentCode : this.scrapInstrument.get_Description();
	}

	public String get_Status()
	{
		return this.scrapDepositStatus.toLocalString();
	}

	public String get_SettlementTime()
	{
		return this.settlementTime == null ? "" : this.settlementTime.toString(Inventory.DateTimeFormat);
	}

	public int get_Decimals()
	{
		return this.decimals;
	}

	public ScrapDepositStatus get_ScrapDepositStatus()
	{
		return this.scrapDepositStatus;
	}

	@Override
	public void initialize(DataRow dataRow, SettingsManager settings)
	{
		super.initialize(dataRow, settings);

		this.scrapInstrument = settings.getScrapInstrument((Guid)(dataRow.get_Item("ScrapInstrumentId")));
		this.instrumentId = (Guid)(dataRow.get_Item("TradeInstrumentId"));
		this.weight = (BigDecimal)dataRow.get_Item("RawQuantity");
		if(dataRow.get_Item("FinalQuantity") != DBNull.value)
		{
			this.weight = (BigDecimal)dataRow.get_Item("FinalQuantity");
		}
		this.settlementTime = dataRow.get_Item("AcceptTime") == DBNull.value ? null : (DateTime)dataRow.get_Item("AcceptTime");
		this.decimals = this.scrapInstrument.get_Decimals();
		this.instrument = settings.getInstrument(this.instrumentId);
	}

	@Override
	public void initialize(XmlNode node, SettingsManager settings)
	{
		super.initialize(node, settings);

		XmlAttributeCollection attributes = node.get_Attributes();
		this.scrapInstrument = settings.getScrapInstrument( new Guid(attributes.get_ItemOf("ScrapInstrumentId").get_Value()));
		this.scrapInstrumentCode = attributes.get_ItemOf("ScrapInstrumentCode").get_Value();

		this.instrumentId = new Guid(attributes.get_ItemOf("TradeInstrumentId").get_Value());
		this.decimals = Integer.parseInt(attributes.get_ItemOf("QuantityDecimalDigits").get_Value());
		this.weight = new BigDecimal(attributes.get_ItemOf("RawQuantity").get_Value());
		if(attributes.get_ItemOf("FinalQuantity") != null)
		{
			this.weight = new BigDecimal(attributes.get_ItemOf("FinalQuantity").get_Value());
		}
		this.settlementTime = attributes.get_ItemOf("AcceptTime") == null ? null : DateTime.valueOf(attributes.get_ItemOf("AcceptTime").get_Value());
		this.unit = this.scrapInstrument == null ? attributes.get_ItemOf("UnitCode").get_Value() : this.scrapInstrument.get_Unit();
		this.instrument = settings.getInstrument(this.instrumentId);
	}

	public Instrument getInstrument()
	{
		return this.instrument;
	}

	protected void initStatus()
	{
		this.scrapDepositStatus = Enum.valueOf(ScrapDepositStatus.class, this.status.intValue());
	}
}
