package tradingConsole;

import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.docking.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class AccountTradingSummary
{
	public static String AccountTradingSummaryBindingKey = "AccountTradingSummaryBindingKey";;

	private Instrument _instrument;
	private Account _account;
	private BigDecimal _sellLots = BigDecimal.ZERO;
	private double _totalSellPrice = 0;

	private BigDecimal _buyLots = BigDecimal.ZERO;
	private double _totalBuyPrice = 0;

	public AccountTradingSummary(Instrument instrument, Account account)
	{
		this._instrument = instrument;
		this._account = account;
	}

	public Account get_Account()
	{
		return this._account;
	}

	public String get_AccountCode()
	{
		if (this._account.get_TradingConsole().get_SettingsManager().get_SystemParameter().get_ShowAccountName())
		{
			return this._account.get_Code() + " (" + this._account.get_Name() + ")";
		}
		else
		{
			return this._account.get_Code();
		}
	}

	public BigDecimal get_SellLots()
	{
		return this._sellLots;
	}

	public String get_SellString()
	{
		return (this._sellLots.compareTo(BigDecimal.ZERO) != 0) ? AppToolkit.getFormatLot(this._sellLots, true) : "-";
	}

	public BigDecimal get_BuyLots()
	{
		return this._buyLots;
	}

	public String get_BuyString()
	{
		return (this._buyLots.compareTo(BigDecimal.ZERO) != 0) ? AppToolkit.getFormatLot(this._buyLots, true) : "-";
	}

	public String get_AvgBuyPrice()
	{
		if(this._totalBuyPrice <= 0 || this._buyLots.compareTo(BigDecimal.ZERO) == 0)
		{
			return "-";
		}
		else
		{
			double avgPrice = this._totalBuyPrice / this._buyLots.doubleValue();
			avgPrice = ((double)Math.round(avgPrice * this._instrument.get_Denominator())) / this._instrument.get_Denominator();
			return Price.toString(Price.create(avgPrice, this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator()));
		}
	}

	public String get_AvgSellPrice()
	{
		if(this._totalSellPrice <= 0 || this._sellLots.compareTo(BigDecimal.ZERO) == 0)
		{
			return "-";
		}
		else
		{
			double avgPrice = this._totalSellPrice / this._sellLots.doubleValue();
			avgPrice = ((double)Math.round(avgPrice * this._instrument.get_Denominator())) / this._instrument.get_Denominator();
			return Price.toString(Price.create(avgPrice, this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator()));
		}
	}

	public String get_NetString()
	{
		BigDecimal net = this._buyLots.subtract(this._sellLots);
		int compare = net.compareTo(BigDecimal.ZERO);
		if (compare == 0)
		{
			return "-";
		}
		else if (compare < 0)
		{
			return "-" + AppToolkit.getFormatLot(net.abs(), true);
		}
		else
		{
			return AppToolkit.getFormatLot(net, true);
		}
	}

	public static PropertyDescriptor[] getPropertyDescriptorsForSummary(DockableFrame dockableFrame)
	{
		java.util.ArrayList<String> currencies = InstrumentPLFloatHelper.get_Currencies();

		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[6 + currencies.size()];
		int index = 0;

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, OrderColKey.AccountCode, true, null, OrderLanguage.AccountCode,
			120, SwingConstants.LEFT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, SummaryColKey.BuyString, true, null, SummaryLanguage.Buy,
			45, SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, SummaryColKey.SellString, true, null, SummaryLanguage.Sell,
			45, SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, SummaryColKey.AvgBuyPrice, true, null, SummaryLanguage.AvgBuyPrice,
			70, SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, SummaryColKey.AvgSellPrice, true, null, SummaryLanguage.AvgSellPrice,
			70, SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, SummaryColKey.NetString, true, null, SummaryLanguage.Net,
			45, SwingConstants.RIGHT, null, null);

		for(String currency : currencies)
		{
			String caption = Language.OpenContractlblTradePLFloatA + "(" + currency + ")";
			propertyDescriptors[index++] = PropertyDescriptor.create(AccountTradingSummary.class, currency, true, null, caption,
				150, SwingConstants.RIGHT, null, null, true);
		}

		return propertyDescriptors;
	}

	private HashMap<String, Double> _plFloats = new HashMap<String, Double>();
	private HashMap<String, Currency> _plFloatCurrencies = new HashMap<String, Currency>();
	public Object get_DynamicValue(String key)
	{
		if(this._plFloats.containsKey(key))
		{
			Double value = this._plFloats.get(key);
			Currency currency = this._plFloatCurrencies.get(key);
			return AppToolkit.format(value, currency.get_Decimals());
		}
		else
		{
			return "";
		}
	}

	public void set_DynamicValue(String key, Object value)
	{
		this._plFloats.put(key, (Double)value);
	}

	public void addToSummary(Order order)
	{
		if(order.get_Account().get_Id().compareTo(this._account.get_Id()) != 0) return;

		BigDecimal lotBalance = order.get_LotBalance();
		double executePrice = Price.toDouble(order.get_ExecutePrice());

		if(lotBalance.compareTo(BigDecimal.ZERO) > 0)
		{
			if (order.get_IsBuy())
			{
				this._buyLots = this._buyLots.add(lotBalance);
				this._totalBuyPrice += executePrice * lotBalance.doubleValue();
			}
			else
			{
				this._sellLots = this._sellLots.add(lotBalance);
				this._totalSellPrice += executePrice * lotBalance.doubleValue();
			}

			double plFloat = order.get_TradePLFloat();
			Currency currency = order.get_Transaction().get_Account().get_IsMultiCurrency() ?
					order.get_Transaction().get_Instrument().get_Currency() : order.get_Transaction().get_Account().get_Currency();
			this.addPLFloat(currency, plFloat);

			TradingConsole.bindingManager.update(AccountTradingSummary.AccountTradingSummaryBindingKey+this._instrument.get_Code(), this);
		}
	}

	public void clearBuySellLots(boolean clearCurrencies)
	{
		this._buyLots = BigDecimal.ZERO;
		this._sellLots = BigDecimal.ZERO;
		this._totalBuyPrice = 0.0;//BigDecimal.ZERO;
		this._totalSellPrice = 0.0;//BigDecimal.ZERO;
		this._plFloats.clear();
		if(clearCurrencies) this._plFloatCurrencies.clear();
	}

	public void changeSummaryPanelStyle(String dataSourceKey)
	{
		//this.setForeground(dataSourceKey, SummaryColKey.SellString, NumericColor.getColor(this._sellLots, true));
		this.setForeground(dataSourceKey, SummaryColKey.SellString, (this._sellLots != null && this._sellLots.compareTo(BigDecimal.ZERO) > 0) ? Color.red : NumericColor.zero);
		this.setForeground(dataSourceKey, SummaryColKey.BuyString, NumericColor.getColor(this._buyLots, true));
		this.setForeground(dataSourceKey, SummaryColKey.NetString, NumericColor.getColor(this._buyLots.subtract(this._sellLots), true));
		java.util.ArrayList<String> currencies = InstrumentPLFloatHelper.get_Currencies();
		if(currencies != null && currencies.size() > 0)
		{
			for(String currency : currencies)
			{
				if(this._plFloats.containsKey(currency))
				{
					double plFlaot = this._plFloats.get(currency);
					this.setForeground(dataSourceKey, currency, NumericColor.getColor(plFlaot, true));
				}
			}
		}
	}

	private void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	private void addPLFloat(Currency currency, double value)
	{
		String currencyCode = currency.get_Code();
		if(!this._plFloatCurrencies.containsKey(currencyCode)) this._plFloatCurrencies.put(currencyCode, currency);
		Double oldValue = 0d;
		if(this._plFloats.containsKey(currencyCode))
		{
			oldValue = this._plFloats.get(currencyCode);
		}
		this._plFloats.put(currencyCode, oldValue.doubleValue() + value);
	}

	public Instrument get_Instrument()
	{
		return this._instrument;
	}
}
