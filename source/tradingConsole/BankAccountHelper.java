package tradingConsole;

import java.util.*;

import framework.*;
import framework.data.*;
import tradingConsole.enumDefine.AccountType;
import framework.lang.Enum;
import tradingConsole.ui.language.Language;

public class BankAccountHelper
{
	private boolean _initialized = false;
	private Hashtable<Long, Country> _countries = new Hashtable<Long, Country> ();
	private Hashtable<Guid, Bank> _banks = new Hashtable<Guid, Bank> ();
	private Hashtable<Long, Province> _provinces = new Hashtable<Long, Province> ();
	private Hashtable<Long, City> _cities = new Hashtable<Long, City> ();

	private TradingConsole _tradingConsole;

	public BankAccountHelper(TradingConsole tradingConsole)
	{
		this._tradingConsole = tradingConsole;
		this._countries.put(Country.Others.get_Id(), Country.Others);
		this._banks.put(Bank.Others.get_Id(), Bank.Others);
	}

	public void refresh()
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				for(int retryCount = 1; retryCount < 5; retryCount++)
				{
					if(getBankAccountReferenceData(null))
					{
						_initialized = true;
						break;
					}
					else
					{
						try
						{
							Thread.sleep(1000 * retryCount);
						}
						catch (InterruptedException ex)
						{
						}
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public Country getCountry(long countryId)
	{
		return this._countries.containsKey(countryId) ? this._countries.get(countryId) : Country.Others;
	}

	public Bank getBank(Guid id)
	{
		return this._banks.containsKey(id) ? this._banks.get(id) : null;
	}

	public Province getProvince(long id)
	{
		return this._provinces.containsKey(id) ? this._provinces.get(id) : null;
	}

	public City getCity(long id)
	{
		return this._cities.containsKey(id) ? this._cities.get(id) : null;
	}

	public boolean getBankAccountReferenceData(String countryId)
	{
		DataSet dataSet = this._tradingConsole.get_TradingConsoleServer().getAccountBankReferenceData(countryId);

	    if(dataSet == null) return false;

		if (countryId == null)
		{
			if(dataSet.get_Tables().get_Count() > 0)
			{
				DataRowCollection rows = dataSet.get_Tables().get_Item(0).get_Rows();
				for (int index = 0; index < rows.get_Count(); index++)
				{
					DataRow row = rows.get_Item(index);
					long id = (Long)row.get_Item("ID");
					String name = (String)row.get_Item("Name");
					Country country = new Country(id, name);
					this._countries.put(id, country);
				}
			}
		}
		else
		{
			initializeReferenceData(dataSet);
		}
		return true;
	}

	public void initializeReferenceData(DataSet dataSet)
	{
		DataRowCollection rows = dataSet.get_Tables().get_Item(0).get_Rows();
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);
			Guid id = (Guid)row.get_Item("ID");
			if (this._banks.containsKey(id))
			{
				continue;
			}
			String name = (String)row.get_Item("Name");
			Bank bank = new Bank(id, name);
			this._banks.put(id, bank);
			long countryId2 = (Long)row.get_Item("CountryID");
			this._countries.get(countryId2).get_Banks().add(bank);
			this._countries.get(countryId2).set_IsReady(true);
		}

		rows = dataSet.get_Tables().get_Item(1).get_Rows();
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);
			long provinceId = (Long)row.get_Item("ProvinceID");
			if (this._provinces.containsKey(provinceId))
			{
				continue;
			}
			String provinceName = (String)row.get_Item("ProvinceName");
			Province province = new Province(provinceId, provinceName);
			this._provinces.put(provinceId, province);
			long countryId2 = (Long)row.get_Item("CountryID");
			this._countries.get(countryId2).get_Provinces().add(province);
		}

		rows = dataSet.get_Tables().get_Item(2).get_Rows();
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);
			long cityId = (Long)row.get_Item("CityID");
			if (this._cities.containsKey(cityId))
			{
				continue;
			}
			long provinceId = (Long)row.get_Item("ProvinceID");
			String cityName = (String)row.get_Item("CityName");
			City city = new City(cityId, cityName);
			this._cities.put(cityId, city);
			Province province = this._provinces.get(provinceId);
			province.get_Cities().add(city);
		}
	}

	public Collection<Country> get_Countries()
	{
		return this._countries.values();
	}

	public Collection<Bank> get_Banks()
	{
		return this._banks.values();
	}

	public Collection<Province> get_Provinces()
	{
		return this._provinces.values();
	}
}
