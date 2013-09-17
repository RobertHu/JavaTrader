package tradingConsole.settings;

import java.math.*;

import framework.*;
import framework.data.*;
import framework.lang.Enum;
import framework.xml.*;
import tradingConsole.enumDefine.*;

public class DeliveryCharge
{
	private Guid _id;
	private String _code;
	private MarketValuePriceType _priceType;
	private BigDecimal _chargeRate;
	private BigDecimal _minCharge;

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Code()
	{
		return this._code;
	}

	public MarketValuePriceType get_PriceType()
	{
		return this._priceType;
	}

	public BigDecimal get_ChargeRate()
	{
		return this._chargeRate;
	}

	public BigDecimal get_MinCharge()
	{
		return this._minCharge;
	}

	private DeliveryCharge()
	{
	}

	static public DeliveryCharge create(DataRow dataRow)
	{
		DeliveryCharge deliveryCharge = new DeliveryCharge();
		deliveryCharge.update(dataRow);
		return deliveryCharge;
	}

	static public DeliveryCharge create(XmlNode node)
	{
		DeliveryCharge deliveryCharge = new DeliveryCharge();
		deliveryCharge.update(node);
		return deliveryCharge;
	}

	public void update(DataRow dataRow)
	{
		this._id = (Guid)dataRow.get_Item("ID");
		this._code = (String)dataRow.get_Item("Code");
		this._chargeRate = (BigDecimal)dataRow.get_Item("ChargeRate");
		this._minCharge = (BigDecimal)dataRow.get_Item("MinCharge");
		this._priceType = Enum.valueOf(MarketValuePriceType.class, (Short)(dataRow.get_Item("PriceType")));
	}

	public void update(XmlNode node)
	{
		XmlAttributeCollection attributes = node.get_Attributes();
		for(int index = 0; index < attributes.get_Count(); index++)
		{
			String nodeName = attributes.get_ItemOf(index).get_LocalName();
			String nodeValue = attributes.get_ItemOf(index).get_Value();

			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Code"))
			{
				this._code = nodeValue;
			}
			else if (nodeName.equals("ChargeRate"))
			{
				this._chargeRate = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MinCharge"))
			{
				this._minCharge = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("PriceType"))
			{
				this._priceType = Enum.valueOf(MarketValuePriceType.class, Integer.parseInt(nodeValue));
			}
		}
	}

	public static void update(SettingsManager settingsManager, XmlNode xmlNode, String updateType)
	{
		if (xmlNode == null) return;

		if (updateType.equals("Modify") || updateType.equals("Add"))
		{
			XmlAttributeCollection attributeCollection = xmlNode.get_Attributes();
			Guid id = new Guid(attributeCollection.get_ItemOf("ID").get_Value());
			DeliveryCharge deliveryCharge = settingsManager.getDeliveryCharge(id);
			if(deliveryCharge == null)
			{
				deliveryCharge = DeliveryCharge.create(xmlNode);
				settingsManager.addDeliveryCharge(deliveryCharge);
			}
			else
			{
				deliveryCharge.update(xmlNode);
			}
		}
	}
}
