package tradingConsole.settings;

import framework.data.DataRow;
import framework.Guid;
import tradingConsole.enumDefine.physical.InstalmentType;
import tradingConsole.enumDefine.physical.InstalmentCloseOption;
import java.math.BigDecimal;
import tradingConsole.enumDefine.physical.RecalculateRateType;
import framework.lang.Enum;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;
import java.util.HashMap;
import java.util.Set;

public class InstalmentPolicy
{
	private Guid _id;
	private String _code;
	private InstalmentType _allowedInstalmentTypes;
	private InstalmentCloseOption _closeOption;
	private RecalculateRateType _recalculateRateTypes;
	private BigDecimal _valueDiscountAsMargin;

	private HashMap<Integer, InstalmentPolicyDetail> _instalmentPolicyDetails = new HashMap<Integer, InstalmentPolicyDetail>();

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Code()
	{
		return this._code;
	}

	public InstalmentType get_AllowedInstalmentTypes()
	{
		return this._allowedInstalmentTypes;
	}

	public InstalmentCloseOption get_CloseOption()
	{
		return this._closeOption;
	}

	public RecalculateRateType get_AllowedRecalculateRateTypes()
	{
		return this._recalculateRateTypes;
	}

	public BigDecimal get_ValueDiscountAsMargin()
	{
		return this._valueDiscountAsMargin;
	}

	public Set<Integer> getPeriods()
	{
		return this._instalmentPolicyDetails.keySet();
	}

	public InstalmentPolicy(DataRow dataRow)
	{
		this._id = (Guid)dataRow.get_Item("Id");
		this._code = (String)dataRow.get_Item("Code");
		this._allowedInstalmentTypes = Enum.valueOf(InstalmentType.class, (Integer)dataRow.get_Item("AllowedInstalmentTypes"));
		this._closeOption = Enum.valueOf(InstalmentCloseOption.class, (Integer)dataRow.get_Item("AllowClose"));
		this._recalculateRateTypes = Enum.valueOf(RecalculateRateType.class, (Integer)dataRow.get_Item("RecalculateRateTypes"));
		this._valueDiscountAsMargin = (BigDecimal)dataRow.get_Item("ValueDiscountAsMargin");
	}

	public void update(XmlNode node)
	{
		XmlAttributeCollection attributes = node.get_Attributes();
		for (int i = 0; i < attributes.get_Count(); i++)
		{
			String nodeName = attributes.get_ItemOf(i).get_LocalName();
			String nodeValue = attributes.get_ItemOf(i).get_Value();
			if (nodeName.equals("AllowedInstalmentTypes"))
			{
				this._allowedInstalmentTypes = Enum.valueOf(InstalmentType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("AllowClose"))
			{
				this._closeOption = Enum.valueOf(InstalmentCloseOption.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("RecalculateRateTypes"))
			{
				this._recalculateRateTypes = Enum.valueOf(InstalmentCloseOption.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("ValueDiscountAsMargin"))
			{
				this._valueDiscountAsMargin = new BigDecimal(nodeValue);
			}
		}
	}

	public void add(InstalmentPolicyDetail instalmentPolicyDetail)
	{
		this._instalmentPolicyDetails.put(instalmentPolicyDetail.get_Period(), instalmentPolicyDetail);
	}

	public InstalmentPolicyDetail get_InstalmentPolicyDetail(int period)
	{
		if(this._instalmentPolicyDetails.containsKey(period))
		{
			return this._instalmentPolicyDetails.get(period);
		}
		else
		{
			return null;
		}
	}
}
