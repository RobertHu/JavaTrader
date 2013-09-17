package tradingConsole.ui;

import java.math.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.physical.*;
import tradingConsole.ui.language.*;

public class InstalmentInfo
{
	private Guid instalmentPolicyId;
	private boolean enable;
	private InstalmentType instalmentType;
	private int period;
	private BigDecimal downPayment;
	private BigDecimal fee;
	private RecalculateRateType recalculateRateType;

	public InstalmentInfo(Guid instalmentPolicyId, int period, BigDecimal downPayment,
						  InstalmentType instalmentType, RecalculateRateType recalculateRateType, BigDecimal fee, boolean enable)
	{
		this.instalmentPolicyId = instalmentPolicyId;
		this.instalmentType = instalmentType;
		this.period = period;
		this.downPayment = downPayment;
		this.recalculateRateType = recalculateRateType;
		this.fee = fee;
		this.enable = enable;
	}

	public InstalmentInfo clone()
	{
		return new InstalmentInfo(this.instalmentPolicyId, this.period, this.downPayment,
								  this.instalmentType, this.recalculateRateType, this.fee, this.enable);
	}

	public boolean isEnabled()
	{
		return this.enable;
	}

	public void setEnabled(boolean value)
	{
		this.enable = value;
	}

	public Guid get_InstalmentPolicyId()
	{
		return this.instalmentPolicyId;
	}

	public InstalmentType get_InstalmentType()
	{
		return this.instalmentType;
	}

	public int get_Period()
	{
		return this.period;
	}

	public BigDecimal get_DownPayment()
	{
		return this.downPayment;
	}

	public String get_DownPaymentInFormat()
	{
		return AppToolkit.format(this.downPayment, 2);
	}

	public RecalculateRateType get_RecalculateRateType()
	{
		return this.recalculateRateType;
	}

	public BigDecimal get_Fee()
	{
		return this.fee;
	}

	public String getVerificationInfo()
	{
		return InstalmentLanguage.InstalmentType + ":" + this.instalmentType.toLocalString() + "  "
			+ InstalmentLanguage.Period + ":" + Integer.toString(this.period)  + "  "
			+ InstalmentLanguage.DownPayment + ":" + this.get_DownPaymentInFormat()  + "  "
			+ InstalmentLanguage.RecalculateRateType + ":" + this.recalculateRateType.toLocalString();
	}

	public void update(Guid instalmentPolicyId, int period, BigDecimal downPayment,
					   InstalmentType instalmentType, RecalculateRateType recalculateRateType, BigDecimal fee, boolean enable)
	{
		this.instalmentPolicyId = instalmentPolicyId;
		this.period = period;
		if(downPayment != null) this.downPayment = downPayment;
		if(instalmentType != null) this.instalmentType = instalmentType;
		if(recalculateRateType != null) this.recalculateRateType = recalculateRateType;
		this.fee = fee;
		this.enable = enable;
	}
}
