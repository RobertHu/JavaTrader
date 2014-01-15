package tradingConsole;

import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;

public class SystemParameter
{
	private double _orderValidDuration;
	private double _marketOrderValidDuration = 5 * 60;
	private double _mooMocAcceptDuration;
	private double _mooMocCancelDuration;
	private boolean _displayLmtStopPoints = true;
	private int _enquiryOutTime = 10;
	private int _exceptionEnquiryOutTime = 8;
	private boolean _highBid = false;
	private boolean _lowBid = true;
	private boolean _caculateChangeWithDenominator;
	private boolean _tradinPanelGridFirst;
	private boolean _showAccountName;
	private String _traderNameInEnglish;
	private String _traderNameInSimplifiedChinese;
	private String _traderNameInTraditionalChinese;
	private boolean _useNightNecessaryWhenBreak;
	private boolean _enableModifyTelephoneIdentification = false;
	private boolean _enableModifyLeverage = false;
	private int _placeConfirmMinTime = 0;
	private boolean _DQMaxLotApplyAccount = false;
	private boolean _needSelectAccount = false;
	private boolean _useFlashReportInJava = false;
	private int _minLeverage;
	private int _maxLeverage;
	private int _leverageStep;
	private boolean _bankAccountNameMustSameWithAccountName;
	private boolean _enableMarginPin;
	private boolean _bankAccountOnly;
	private int _maxCustomerBankNo = 0;
	private boolean _allowEditBankAccountInTrader = false;
	private int _timeOptionInTraderLogAndConfirmWindow = 0;
	private boolean _showPriceChangedBeforeCloseConfirmWindow = false;
	private DateTime _tradeDayBeginTime = null;
	private boolean _closingUseCustomerQuotePolicy = false;
	private boolean _showChartAsDefultInTrader = true;

	private Guid _RMBCurrencyId = null;

	public double get_OrderValidDuration()
	{
		return this._orderValidDuration;
	}

	public double get_MarketOrderValidDuration()
	{
		return this._marketOrderValidDuration;
	}

	public Guid get_RMBCurrencyId()
	{
		return this._RMBCurrencyId;
	}

	public double get_MooMocAcceptDuration()
	{
		return this._mooMocAcceptDuration;
	}

	public double get_MooMocCancelDuration()
	{
		return this._mooMocCancelDuration;
	}

	public int get_TimeOptionInTraderLogAndConfirmWindow()
	{
			return this._timeOptionInTraderLogAndConfirmWindow;
	}

	public boolean get_ShowPriceChangedBeforeCloseConfirmWindow()
	{
		return this._showPriceChangedBeforeCloseConfirmWindow;
	}

	public boolean get_ClosingUseCustomerQuotePolicy()
	{
		return this._closingUseCustomerQuotePolicy;
	}

	public boolean get_ShowChartAsDefultInTrader()
	{
		return this._showChartAsDefultInTrader;
	}

	public boolean get_DisplayLmtStopPoints()
	{
		return this._displayLmtStopPoints;
	}

	public boolean get_CaculateChangeWithDenominator()
	{
		return this._caculateChangeWithDenominator;
	}

	public int get_EnquiryOutTime()
	{
		if (this._enquiryOutTime <= 0)
			this._enquiryOutTime = 10;
		return this._enquiryOutTime;
	}

	public int get_ExceptionEnquiryOutTime()
	{
		if (this._exceptionEnquiryOutTime <= 0)
			this._exceptionEnquiryOutTime = 8;
		return this._exceptionEnquiryOutTime;
	}

	public int get_MaxCustomerBankNo()
	{
		return this._maxCustomerBankNo;
	}

	public boolean get_HighBid()
	{
		return this._highBid;
	}

	public boolean get_LowBid()
	{
		return this._lowBid;
	}

	public boolean get_TradinPanelGridFirst()
	{
		return this._tradinPanelGridFirst;
	}

	public boolean get_ShowAccountName()
	{
		return this._showAccountName;
	}

	public String get_TraderNameInEnglish()
	{
		return this._traderNameInEnglish;
	}

	public String get_TraderNameInSimplifiedChinese()
	{
		return this._traderNameInSimplifiedChinese;
	}

	public String get_TraderNameInTraditionalChinese()
	{
		return this._traderNameInTraditionalChinese;
	}

	public boolean get_UseNightNecessaryWhenBreak()
	{
		return this._useNightNecessaryWhenBreak;
	}

	public boolean get_EnableModifyTelephoneIdentification()
	{
		return this._enableModifyTelephoneIdentification;
	}

	public boolean get_EnableMarginPin()
	{
		return this._enableMarginPin;
	}

	public boolean get_BankAccountNameMustSameWithAccountName()
	{
		return this._bankAccountNameMustSameWithAccountName;
	}

	public boolean get_BankAccountOnly()
	{
		return this._bankAccountOnly;
	}

	public boolean get_AllowEditBankAccountInTrader()
	{
		return this._allowEditBankAccountInTrader;
	}

	public DateTime get_TradeDayBeginTime()
	{
		return this._tradeDayBeginTime;
	}

	public SystemParameter()
	{
	}

	public void setValue(DataRow dataRow)
	{
		this._orderValidDuration = (Integer)dataRow.get_Item("OrderValidDuration");
		this._mooMocAcceptDuration = (Integer)dataRow.get_Item("MooMocAcceptDuration");
		this._mooMocCancelDuration = (Integer)dataRow.get_Item("MooMocCancelDuration");
		this._displayLmtStopPoints = AppToolkit.isDBNull(dataRow.get_Item("DisplayLmtStopPoints")) ? true : (Boolean)dataRow.get_Item("DisplayLmtStopPoints");
		this._enquiryOutTime = (Integer)dataRow.get_Item("EnquiryOutTime");
		this._exceptionEnquiryOutTime = (Integer)dataRow.get_Item("ExceptionEnquiryOutTime");
		this._maxCustomerBankNo = (Integer)dataRow.get_Item("MaxCustomerBankNo");

		if (dataRow.get_Table().get_Columns().contains("PlaceConfirmMinTime"))
		{
			this._placeConfirmMinTime = (Integer)dataRow.get_Item("PlaceConfirmMinTime");
		}

		if (dataRow.get_Table().get_Columns().contains("DQMaxLotApplyAccount"))
		{
			this._DQMaxLotApplyAccount = (Boolean)dataRow.get_Item("DQMaxLotApplyAccount");
		}

		if (dataRow.get_Table().get_Columns().contains("NeedSelectAccount"))
		{
			this._needSelectAccount = (Boolean)dataRow.get_Item("NeedSelectAccount");
		}
		if (dataRow.get_Table().get_Columns().contains("UseFlashReportInJava"))
		{
			this._useFlashReportInJava = (Boolean)dataRow.get_Item("UseFlashReportInJava");
		}

		this._caculateChangeWithDenominator = (Boolean)dataRow.get_Item("CaculateChangeWithDenominator");

		if (dataRow.get_Table().get_Columns().contains("HighBid"))
		{
			this._highBid = (Boolean)dataRow.get_Item("HighBid");
		}
		if (dataRow.get_Table().get_Columns().contains("LowBid"))
		{
			this._lowBid = (Boolean)dataRow.get_Item("LowBid");
		}
		if (dataRow.get_Table().get_Columns().contains("TradinPanelGridFirst"))
		{
			this._tradinPanelGridFirst = (Boolean)dataRow.get_Item("TradinPanelGridFirst");
		}
		if (dataRow.get_Table().get_Columns().contains("ShowAccountName"))
		{
			this._showAccountName = (Boolean)dataRow.get_Item("ShowAccountName");
		}
		if (dataRow.get_Table().get_Columns().contains("CnyCurrencyId")
			&& dataRow.get_Item("CnyCurrencyId") != DBNull.value)
		{
			this._RMBCurrencyId = (Guid)dataRow.get_Item("CnyCurrencyId");
		}
		if (dataRow.get_Table().get_Columns().contains("TraderNameInEnglish")
			&& dataRow.get_Item("TraderNameInEnglish") != DBNull.value)
		{
			this._traderNameInEnglish = (String)dataRow.get_Item("TraderNameInEnglish");
		}
		if (dataRow.get_Table().get_Columns().contains("TraderNameInSimplifiedChinese")
			&& dataRow.get_Item("TraderNameInSimplifiedChinese") != DBNull.value)
		{
			this._traderNameInSimplifiedChinese = (String)dataRow.get_Item("TraderNameInSimplifiedChinese");
		}
		if (dataRow.get_Table().get_Columns().contains("TraderNameInTraditionalChinese")
			&& dataRow.get_Item("TraderNameInTraditionalChinese") != DBNull.value)
		{
			this._traderNameInTraditionalChinese = (String)dataRow.get_Item("TraderNameInTraditionalChinese");
		}
		if (dataRow.get_Table().get_Columns().contains("EnablePalceLotNnemonic")
			&& dataRow.get_Item("EnablePalceLotNnemonic") != DBNull.value)
		{
			PalceLotNnemonic.set_Enable( (Boolean)dataRow.get_Item("EnablePalceLotNnemonic"));
		}
		if (dataRow.get_Table().get_Columns().contains("UseNightNecessaryWhenBreak")
			&& dataRow.get_Item("UseNightNecessaryWhenBreak") != DBNull.value)
		{
			this._useNightNecessaryWhenBreak = (Boolean)dataRow.get_Item("UseNightNecessaryWhenBreak");
		}

		if (dataRow.get_Table().get_Columns().contains("EnableModifyTelephoneIdentificationCode")
			&& dataRow.get_Item("EnableModifyTelephoneIdentificationCode") != DBNull.value)
		{
			this._enableModifyTelephoneIdentification = (Boolean)dataRow.get_Item("EnableModifyTelephoneIdentificationCode");
		}

		if (dataRow.get_Table().get_Columns().contains("EnableModifyLeverage")
			&& dataRow.get_Item("EnableModifyLeverage") != DBNull.value)
		{
			this._enableModifyLeverage = (Boolean)dataRow.get_Item("EnableModifyLeverage");
		}

		if (dataRow.get_Table().get_Columns().contains("MinLeverage")
			&& dataRow.get_Item("MinLeverage") != DBNull.value)
		{
			this._minLeverage = (Integer)dataRow.get_Item("MinLeverage");
		}

		if (dataRow.get_Table().get_Columns().contains("MaxLeverage")
			&& dataRow.get_Item("MaxLeverage") != DBNull.value)
		{
			this._maxLeverage = (Integer)dataRow.get_Item("MaxLeverage");
		}

		if (dataRow.get_Table().get_Columns().contains("LeverageStep")
			&& dataRow.get_Item("LeverageStep") != DBNull.value)
		{
			this._leverageStep = (Integer)dataRow.get_Item("LeverageStep");
		}

		if (dataRow.get_Table().get_Columns().contains("EnableMarginPin")
			&& dataRow.get_Item("EnableMarginPin") != DBNull.value)
		{
			this._enableMarginPin = (Boolean)dataRow.get_Item("EnableMarginPin");
		}

		if (dataRow.get_Table().get_Columns().contains("BankAccountNameMustSameWithAccountName")
			&& dataRow.get_Item("BankAccountNameMustSameWithAccountName") != DBNull.value)
		{
			this._bankAccountNameMustSameWithAccountName = (Boolean)dataRow.get_Item("BankAccountNameMustSameWithAccountName");
		}

		if (dataRow.get_Table().get_Columns().contains("BankAccountOnly")
			&& dataRow.get_Item("BankAccountOnly") != DBNull.value)
		{
			this._bankAccountOnly = (Boolean)dataRow.get_Item("BankAccountOnly");
		}

		if (dataRow.get_Table().get_Columns().contains("AllowEditBankAccountInTrader")
			&& dataRow.get_Item("AllowEditBankAccountInTrader") != DBNull.value)
		{
			this._allowEditBankAccountInTrader = (Boolean)dataRow.get_Item("AllowEditBankAccountInTrader");
		}

		if (dataRow.get_Table().get_Columns().contains("SPCBCCW")
			&& dataRow.get_Item("SPCBCCW") != DBNull.value)
		{
			this._showPriceChangedBeforeCloseConfirmWindow = (Boolean)dataRow.get_Item("SPCBCCW");
		}

		if (dataRow.get_Table().get_Columns().contains("ClosingUseCustomerQuotePolicy")
			&& dataRow.get_Item("ClosingUseCustomerQuotePolicy") != DBNull.value)
		{
			this._closingUseCustomerQuotePolicy = (Boolean)dataRow.get_Item("ClosingUseCustomerQuotePolicy");
		}

		if (dataRow.get_Table().get_Columns().contains("ShowChartAsDefultInTrader")
			&& dataRow.get_Item("ShowChartAsDefultInTrader") != DBNull.value)
		{
			this._showChartAsDefultInTrader = (Boolean)dataRow.get_Item("ShowChartAsDefultInTrader");
		}

		if (dataRow.get_Table().get_Columns().contains("TOITLACW")
			&& dataRow.get_Item("TOITLACW") != DBNull.value)
		{
			this._timeOptionInTraderLogAndConfirmWindow = (Short)dataRow.get_Item("TOITLACW");
		}

		if (dataRow.get_Table().get_Columns().contains("TradeDayBeginTime")
			&& dataRow.get_Item("TradeDayBeginTime") != DBNull.value)
		{
			this._tradeDayBeginTime = (DateTime)dataRow.get_Item("TradeDayBeginTime");
		}
	}

	public void setValue(XmlAttributeCollection systemParameterCollection)
	{
		for (int i = 0; i < systemParameterCollection.get_Count(); i++)
		{
			String nodeName = systemParameterCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = systemParameterCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("OrderValidDuration"))
			{
				this._orderValidDuration = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("MooMocAcceptDuration"))
			{
				this._mooMocAcceptDuration = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("MooMocCancelDuration"))
			{
				this._mooMocCancelDuration = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DisplayLmtStopPoints"))
			{
				this._displayLmtStopPoints = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("EnquiryOutTime"))
			{
				this._enquiryOutTime = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("ExceptionEnquiryOutTime"))
			{
				this._exceptionEnquiryOutTime = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("HighBid"))
			{
				this._highBid = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("LowBid"))
			{
				this._lowBid = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("UseNightNecessaryWhenBreak"))
			{
				this._useNightNecessaryWhenBreak = Boolean.valueOf(nodeValue);
			}
		}
	}

	public static void updateSystemParameter(SettingsManager settingsManager, XmlNode systemParameterNode, String updateType)
	{
		if (systemParameterNode == null)
		{
			return;
		}
		XmlAttributeCollection systemParameterCollection = systemParameterNode.get_Attributes();
		if (updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			settingsManager.get_SystemParameter().setValue(systemParameterCollection);
		}
	}

	public boolean get_CanModifyLeverage()
	{
		return this._enableModifyLeverage;
	}

	public int get_MinLeverage()
	{
		return this._minLeverage;
	}

	public int get_MaxLeverage()
	{
		return this._maxLeverage;
	}

	public int get_LeverageStep()
	{
		return this._leverageStep;
	}

	public int get_PlaceConfirmMinTime()
	{
		return this._placeConfirmMinTime;
	}

	public boolean get_DQMaxLotApplyAccount()
	{
		return this._DQMaxLotApplyAccount;
	}

	public boolean get_NeedSelectAccount()
	{
		return this._needSelectAccount;
	}

	public boolean get_useFlashReportInJava()
	{
		return this._useFlashReportInJava;
	}
}
