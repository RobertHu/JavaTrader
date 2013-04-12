package tradingConsole;

import java.util.Iterator;

import framework.Guid;
import framework.data.DataRow;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;
import framework.StringHelper;

import tradingConsole.settings.SettingsManager;
import java.math.BigDecimal;
import framework.DBNull;
import tradingConsole.settings.PublicParametersManager;

public class Currency
{
	private Guid _id;
	private String _code;
	private String _alias;
	private short _decimals;
	private BigDecimal _minDeposit = null;

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_RealCode()
	{
		return this._code;
	}

	public String get_Code()
	{
		boolean isChinese = PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT");
		if(isChinese && !StringHelper.isNullOrEmpty(this._alias))
		{
			return this._alias;
		}
		else
		{
			return this._code;
		}
	}

	public short get_Decimals()
	{
		return this._decimals;
	}

	public Currency(Guid id)
	{
		this._id = id;
	}

	public BigDecimal get_MinDeposit()
	{
		return this._minDeposit;
	}

	public Currency(DataRow dataRow)
	{
		this._id = (Guid) dataRow.get_Item("ID");
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._code = (String) dataRow.get_Item("Name");
		this._alias = (String) dataRow.get_Item("Code");
		this._decimals = (Short) dataRow.get_Item("Decimals");
		if (dataRow.get_Item("MinDeposit") != DBNull.value)
		{
			this._minDeposit = (BigDecimal)dataRow.get_Item("MinDeposit");
		}
	}

	public void setValue(XmlAttributeCollection currencyCollection)
	{
		for (int i = 0; i < currencyCollection.get_Count(); i++)
		{
			String nodeName = currencyCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = currencyCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Name"))
			{
				this._code = nodeValue;
			}
			if (nodeName.equals("Alias"))
			{
				this._alias = nodeValue;
			}
			else if (nodeName.equals("Decimals"))
			{
				this._decimals = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("MinDeposit"))
			{
				if(!StringHelper.isNullOrEmpty(nodeValue))
				{
					this._minDeposit = new BigDecimal(nodeValue);
				}
			}
		}
	}

	public static void updateCurrency(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode currencyNode, String updateType)
	{
		if (currencyNode == null)
		{
			return;
		}
		XmlAttributeCollection currencyCollection = currencyNode.get_Attributes();
		Guid currencyId = new Guid(currencyCollection.get_ItemOf("ID").get_Value());
		Currency currency = settingsManager.getCurrency(currencyId);
		boolean needRefreshAccounts = false;
		if (updateType.equals("Delete"))
		{
			if (currency != null)
			{
				settingsManager.removeCurrency(currencyId);
			}
		}
		else if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			if (currency == null)
			{
				currency = new Currency(currencyId);
				currency.setValue(currencyCollection);
				settingsManager.setCurrency(currency);
			}
			else
			{
				needRefreshAccounts = true;
				currency.setValue(currencyCollection);
			}
		}
		//Refresh all orders UI
		tradingConsole.refreshAllOrderUi();
		tradingConsole.get_SettingsManager().calculateSummary();

		//Refresh Account UI
		if (needRefreshAccounts)
		{
			for (Iterator<Account> iterator = settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
			{
				Account account = iterator.next();
				account.updateCurrencyCode();
				account.updateNode(true);
			}
		}
	}

}
