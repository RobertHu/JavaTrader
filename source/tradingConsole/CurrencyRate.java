package tradingConsole;

import java.util.HashMap;
import java.util.Iterator;

import framework.Guid;
import framework.data.DataRow;

import tradingConsole.settings.SettingsManager;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;

public class CurrencyRate
{
	private SettingsManager _settingsManager;

	private Guid _sourceCurrencyId;
	private Guid _targetCurrencyId;
	private double _rateIn;
	private double _rateOut;

	public CurrencyRate(SettingsManager settingsManager, Guid sourceCurrencyId, Guid targetCurrencyId)
	{
		this._settingsManager = settingsManager;

		this._sourceCurrencyId = sourceCurrencyId;
		this._targetCurrencyId = targetCurrencyId;

		this._rateIn = 1.00;
		this._rateOut = 1.00;
	}

	public CurrencyRate(SettingsManager settingsManager, DataRow dataRow)
	{
		this._settingsManager = settingsManager;

		this._sourceCurrencyId = (Guid) dataRow.get_Item("SourceCurrencyID");
		this._targetCurrencyId = (Guid) dataRow.get_Item("TargetCurrencyID");
		this.setValue(dataRow);
	}

	public Guid get_SourceCurrencyId()
	{
		return this._sourceCurrencyId;
	}

	public Guid get_TargetCurrencyId()
	{
		return this._targetCurrencyId;
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	public double get_RateIn()
	{
		return this._rateIn;
	}

	private void setValue(DataRow dataRow)
	{
		this._rateIn = ( (Double) dataRow.get_Item("RateIn")).doubleValue();
		this._rateOut = ( (Double) dataRow.get_Item("RateOut")).doubleValue();
	}

	public void setValue(XmlAttributeCollection currencyRateCollection)
	{
		for (int i = 0; i < currencyRateCollection.get_Count(); i++)
		{
			String nodeName = currencyRateCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = currencyRateCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("TargetCurrencyID"))
			{
				this._targetCurrencyId = new Guid(nodeValue);
			}
			else if (nodeName.equals("RateIn"))
			{
				this._rateIn = Double.valueOf(nodeValue).doubleValue();
			}
			else if (nodeName.equals("RateOut"))
			{
				this._rateOut = Double.valueOf(nodeValue).doubleValue();
			}
		}
	}

	public double getRateFor(double value)
	{
		return value > 0 ? this._rateIn : this._rateOut;
	}

	public double exchange(double value)
	{
		//Currency currency = this._settingsManager.getCurrency(this._sourceCurrencyId);
		//value = AppToolkit.round(value, currency.get_Decimals());

		double exchange = 0.00;
		if (value > 0)
		{
			exchange = value * this._rateIn;
		}
		else
		{
			exchange = value * this._rateOut;
		}
		return exchange;
	}

	public static CurrencyRate updateCurrencyRate(SettingsManager settingsManager, XmlNode currencyRateNode, String updateType)
	{
		if (currencyRateNode == null)
		{
			return null;
		}
		XmlAttributeCollection currencyRateCollection = currencyRateNode.get_Attributes();
		Guid sourceCurrencyId = new Guid(currencyRateCollection.get_ItemOf("SourceCurrencyID").get_Value());
		Guid targetCurrencyId = new Guid(currencyRateCollection.get_ItemOf("TargetCurrencyID").get_Value());
		Currency currency = settingsManager.getCurrency(sourceCurrencyId);
		if (currency == null)
		{
			return null;
		}
		currency = settingsManager.getCurrency(targetCurrencyId);
		if (currency == null)
		{
			return null;
		}
		CurrencyRate currencyRate = settingsManager.getCurrencyRate(sourceCurrencyId, targetCurrencyId);
		if (updateType.equals("Delete"))
		{
			if (currencyRate != null)
			{
				settingsManager.removeCurrencyRate(sourceCurrencyId, targetCurrencyId);
			}
		}
		else if (updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			if (currencyRate == null)
			{
				currencyRate = new CurrencyRate(settingsManager, sourceCurrencyId, targetCurrencyId);
				currencyRate.setValue(currencyRateCollection);
				settingsManager.setCurrencyRate(sourceCurrencyId, targetCurrencyId, currencyRate);
			}
			else
			{
				currencyRate.setValue(currencyRateCollection);
				for (Iterator<Instrument> iterator = settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
				{
					Instrument instrument = iterator.next();
					instrument.reCalculateTradePLFloat();
				}
			}
		}
		return currencyRate;
	}
}
