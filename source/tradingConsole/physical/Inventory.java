package tradingConsole.physical;

import java.math.*;
import java.util.*;

import javax.swing.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.framework.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import tradingConsole.ui.ColorSettings;

public class Inventory
{
	public static final String DateTimeFormat = "yyyy-MM-dd";

	public static final String bindingKey = "InventoryBindingKey";
	public static final String shortSellBindingKey = "ShortSellInventoryBindingKey";

	private Instrument instrument;
	private Account account;
	private double weight;
	private String averagePrice;
	private String marketValue;

	private ArrayList<Order> orders = new ArrayList<Order>();
	private BindingSource bindingSource = new BindingSource();

	public Collection<Order> get_Orders()
	{
		return this.orders;
	}

	public BindingSource get_BindingSource()
	{
		return this.bindingSource;
	}

	public String get_Instrument()
	{
		return this.instrument.get_Description();
	}

	public Instrument get_Instrument2()
	{
		return this.instrument;
	}

	public String get_AccountCode()
	{
		return this.account.getCode();
	}

	public Account get_Account()
	{
		return this.account;
	}

	public String get_Weight()
	{
		return this.weight == 0 ? "" : AppToolkit.format(this.weight, this.instrument.get_PhysicalLotDecimal());
	}

	public String get_Unit()
	{
		return this.instrument.get_Unit();
	}

	public String get_Currency()
	{
		return this.account.get_IsMultiCurrency() ? this.instrument.get_Currency().get_Code() : this.account.get_Currency().get_Code();
	}

	public String get_AveragePrice()
	{
		return this.averagePrice;
	}

	public String get_MarketValue()
	{
		return this.marketValue;
	}

	public Inventory(Account account, Instrument instrument)
	{
		this.account = account;
		this.instrument = instrument;
		if (ColorSettings.useBlackAsBackground)
		{
			this.bindingSource.useBlackAsBackground();
		}

		String bindingKey = account.get_Code() + instrument.get_Code();
		TradingConsole.bindingManager.bind(bindingKey, new Vector(0), this.bindingSource, Order.getPropertyDescriptorsForPhysical());
	}

	public void add(Order order)
	{
		this.orders.add(order);
		this.caculate();
		this.bindingSource.add(order);
	}

	public void remove(Order order)
	{
		this.orders.remove(order);
		this.caculate();
		this.bindingSource.remove(order);
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[7];
		int index = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.Instrument, true, null, PhysicalInventoryLanguage.Instrument,
			100, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Inventory.class, OpenOrderColKey.AccountCode, true, null, OpenOrderLanguage.AccountCode,
			120, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.Weight, true, null, PhysicalInventoryLanguage.Weight,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.Unit, true, null, PhysicalInventoryLanguage.Unit,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.Currency, true, null, AccountSingleLanguage.CurrencyCode,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;


		propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.AveragePrice, true, null, PhysicalInventoryLanguage.AveragePrice,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Inventory.class, PhysicalInventoryColKey.MarketValue, true, null, PhysicalInventoryLanguage.MarketValue,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		return propertyDescriptors;
	}

	void recaculateMarketValue()
	{
		double marketValue = 0;
		for(Order order : this.orders)
		{
			marketValue += order.get_MarketValue();
		}

		if(marketValue > 0)
		{
			/*if(!account.get_IsMultiCurrency())
			{
				Guid targetCurrencyId = this.account.get_Currency().get_Id();
				Guid sourceCurrencyId = this.instrument.get_Currency().get_Id();
				CurrencyRate currencyRate = this.account.get_TradingConsole().get_SettingsManager().getCurrencyRate(sourceCurrencyId, targetCurrencyId);
				short decimals = this.account.get_Currency().get_Decimals();
				this.marketValue = AppToolkit.format(currencyRate.exchange(marketValue), decimals);
			}
			else*/
			{
				short decimals = this.instrument.get_Currency().get_Decimals();
				this.marketValue = AppToolkit.format(marketValue, decimals);
			}
		}
		else
		{
			this.marketValue = "";
		}

		if(TradingConsole.bindingManager.contains(Inventory.bindingKey)) TradingConsole.bindingManager.update(Inventory.bindingKey, this);
	}

	void caculate()
	{
		this.averagePrice = "";
		this.marketValue = "";
		this.weight = 0;

		if(this.orders.size() == 0) return;

		BigDecimal totalLot = BigDecimal.ZERO;
		BigDecimal totalValue = BigDecimal.ZERO;
		double marketValue = 0;
		BigDecimal weight = BigDecimal.ZERO;

		for(Order order : this.orders)
		{
			BigDecimal lot = order.get_LotBalance();
			totalLot = totalLot.add(lot);
			totalValue = totalValue.add(lot.multiply(Price.toBigDecimal(order.get_ExecutePrice())));

			order.recaculateValueAsMargin();
			marketValue += order.get_MarketValue();
			weight = weight.add(lot.multiply(order.get_Transaction().get_ContractSize()));
		}
		this.weight = weight.doubleValue();

		if(totalLot.compareTo(BigDecimal.ZERO) > 0)
		{
			Price averagePrice = Price.create(totalValue.doubleValue() / totalLot.doubleValue(), this.instrument);
			this.averagePrice = Price.toString(averagePrice);
		}
		if(marketValue > 0)
		{
			/*if(!account.get_IsMultiCurrency())
			{
				Guid targetCurrencyId = this.account.get_Currency().get_Id();
				Guid sourceCurrencyId = this.instrument.get_Currency().get_Id();
				short decimals = this.instrument.get_Currency().get_Decimals();
				marketValue = AppToolkit.round(marketValue, decimals);

				CurrencyRate currencyRate = this.account.get_TradingConsole().get_SettingsManager().getCurrencyRate(sourceCurrencyId, targetCurrencyId);
				decimals = this.account.get_Currency().get_Decimals();
				this.marketValue = AppToolkit.format(currencyRate.exchange(marketValue), decimals);
			}
			else*/
			{
				short decimals = this.instrument.get_Currency().get_Decimals();
				this.marketValue = AppToolkit.format(marketValue, decimals);
			}
		}
	}

	public boolean isFor(Account account, Instrument instrument)
	{
		return this.account.get_Id().equals(account.get_Id()) && this.instrument.get_Id().equals(instrument.get_Id());
	}

	public void update(Order order, String propertyName, Object value)
	{
		if(this.orders.contains(order))
		{
			if(order.get_LotBalance().compareTo(BigDecimal.ZERO) == 0)
			{
				this.remove(order);
			}
			else
			{
				this.caculate();
				TradingConsole.bindingManager.update(this.bindingSource.get_DataSourceKey(), order, propertyName, value);
			}
		}
		else
		{
			this.add(order);
		}
	}

	public void update(Order order)
	{
		if(this.orders.contains(order))
		{
			if(order.get_LotBalance().compareTo(BigDecimal.ZERO) == 0)
			{
				this.remove(order);
			}
			else
			{
				this.caculate();
				TradingConsole.bindingManager.update(this.bindingSource.get_DataSourceKey(), order);
			}
		}
		else
		{
			this.add(order);
		}
	}
}
