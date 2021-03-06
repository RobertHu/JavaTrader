package tradingConsole.settings;

import framework.data.DataRow;
import framework.Guid;
import java.math.BigDecimal;
import tradingConsole.enumDefine.physical.AdministrationFeeBase;
import tradingConsole.enumDefine.physical.ContractTerminateType;
import framework.lang.Enum;
import framework.xml.XmlAttributeCollection;
import tradingConsole.enumDefine.physical.InstalmentType;
import framework.xml.XmlNode;
import tradingConsole.enumDefine.physical.DownPaymentBasis;
import tradingConsole.enumDefine.physical.InstalmentFrequence;

public class InstalmentPolicyDetail
{
	private Guid _instalmentPolicyId;
	private int _period;
	private DownPaymentBasis _downPaymentBasis;
	private BigDecimal _minDownPayment;
	private BigDecimal _maxDownPayment;
	private BigDecimal _interestRate;
	private AdministrationFeeBase _administrationFeeBase;
	private BigDecimal _administrationFee;
	private ContractTerminateType _contractTerminateType;
	private BigDecimal _contractTerminateFee;
	private InstalmentFrequence _instalmentFrequence = InstalmentFrequence.Month;
	private boolean _isActive = true;

	public Guid get_InstalmentPolicyId()
	{
		return this._instalmentPolicyId;
	}

	public int get_Period()
	{
		return this._period;
	}

	public DownPaymentBasis get_DownPaymentBasis()
	{
		return this._downPaymentBasis;
	}

	public InstalmentFrequence get_InstalmentFrequence()
	{
		return this._instalmentFrequence;
	}

	public BigDecimal get_MinDownPayment()
	{
		return this._minDownPayment;
	}

	public BigDecimal get_MaxDownPayment()
	{
		return this._maxDownPayment;
	}

	public BigDecimal get_InterestRate()
	{
		return this._interestRate;
	}

	public AdministrationFeeBase get_AdministrationFeeBase()
	{
		return this._administrationFeeBase;
	}

	public BigDecimal get_AdministrationFee()
	{
		return this._administrationFee;
	}

	public ContractTerminateType get_ContractTerminateType()
	{
		return this._contractTerminateType;
	}

	public BigDecimal get_ContractTerminateFee()
	{
		return this._contractTerminateFee;
	}

	public boolean get_IsActive()
	{
		return this._isActive;
	}

	public InstalmentPolicyDetail(DataRow dataRow)
	{
		this._instalmentPolicyId = (Guid)dataRow.get_Item("InstalmentPolicyId");
		this._period = (Integer)dataRow.get_Item("Period");
		if(dataRow.get_Table().get_Columns().contains("DownPaymentBasis"))
		{
			this._downPaymentBasis = Enum.valueOf(DownPaymentBasis.class, (Integer)dataRow.get_Item("DownPaymentBasis"));
		}
		if(dataRow.get_Table().get_Columns().contains("Frequence"))
		{
			this._instalmentFrequence = Enum.valueOf(InstalmentFrequence.class, (Integer)dataRow.get_Item("Frequence"));
		}
		if(dataRow.get_Table().get_Columns().contains("IsActive"))
		{
			this._isActive = (Boolean)dataRow.get_Item("IsActive");
		}

		this._minDownPayment = (BigDecimal)dataRow.get_Item("MinDownPayment");
		this._maxDownPayment = (BigDecimal)dataRow.get_Item("MaxDownPayment");
		this._interestRate = (BigDecimal)dataRow.get_Item("InterestRate");
		this._administrationFeeBase = Enum.valueOf(AdministrationFeeBase.class, (Integer)dataRow.get_Item("AdministrationFeeBase"));
		this._administrationFee = (BigDecimal)dataRow.get_Item("AdministrationFee");
		this._contractTerminateType = Enum.valueOf(ContractTerminateType.class, (Integer)dataRow.get_Item("ContractTerminateType"));
		this._contractTerminateFee = (BigDecimal)dataRow.get_Item("ContractTerminateFee");
	}

	public InstalmentPolicyDetail(XmlNode node)
	{
		this.update(node);
	}

	public void update(XmlNode node)
	{
		XmlAttributeCollection attributes = node.get_Attributes();
		for (int i = 0; i < attributes.get_Count(); i++)
		{
			String nodeName = attributes.get_ItemOf(i).get_LocalName();
			String nodeValue = attributes.get_ItemOf(i).get_Value();
			if (nodeName.equals("InstalmentPolicyId"))
			{
				this._instalmentPolicyId = new Guid(nodeValue);
			}
			else if (nodeName.equals("IsActive"))
			{
				this._isActive = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("Period"))
			{
				this._period = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("MinDownPayment"))
			{
				this._minDownPayment = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("MaxDownPayment"))
			{
				this._maxDownPayment = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("InterestRate"))
			{
				this._interestRate = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("AdministrationFeeBase"))
			{
				this._administrationFeeBase = Enum.valueOf(AdministrationFeeBase.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("DownPaymentBasis"))
			{
				this._downPaymentBasis = Enum.valueOf(DownPaymentBasis.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("InstalmentFrequence"))
			{
				this._instalmentFrequence = Enum.valueOf(InstalmentFrequence.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("AdministrationFee"))
			{
				this._administrationFee = new BigDecimal(nodeValue);
			}
			else if (nodeName.equals("ContractTerminateType"))
			{
				this._contractTerminateType = Enum.valueOf(ContractTerminateType.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("ContractTerminateFee"))
			{
				this._contractTerminateFee = new BigDecimal(nodeValue);
			}
		}
	}
}
