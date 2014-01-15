package tradingConsole.ui;

import java.math.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.physical.*;
import tradingConsole.settings.*;
import tradingConsole.ui.language.*;

public class InstalmentInfo
{
	private Guid instalmentPolicyId;
	private PaymentMode paymentMode;
	private InstalmentType instalmentType;
	private InstalmentPeriod period;
	private BigDecimal downPayment;
	private BigDecimal fee;
	private RecalculateRateType recalculateRateType;

	public InstalmentInfo(Guid instalmentPolicyId, InstalmentPeriod period, BigDecimal downPayment,
						  InstalmentType instalmentType, RecalculateRateType recalculateRateType, BigDecimal fee, PaymentMode paymentMode)
	{
		this.instalmentPolicyId = instalmentPolicyId;
		this.instalmentType = instalmentType;
		this.period = period;
		this.downPayment = downPayment;
		this.recalculateRateType = recalculateRateType;
		this.fee = fee;
		this.paymentMode = paymentMode;
	}

	public InstalmentInfo clone()
	{
		return new InstalmentInfo(this.instalmentPolicyId, this.period, this.downPayment,
								  this.instalmentType, this.recalculateRateType, this.fee, this.paymentMode);
	}

	public PaymentMode get_PaymentMode()
	{
		return this.paymentMode;
	}

	public void set_PaymentMode(PaymentMode value)
	{
		this.paymentMode = value;
	}

	public boolean isFullPayment()
	{
		return this.paymentMode.equals(PaymentMode.FullAmount);
	}

	public Guid get_InstalmentPolicyId()
	{
		return this.instalmentPolicyId;
	}

	public InstalmentType get_InstalmentType()
	{
		return this.instalmentType;
	}

	public InstalmentPeriod get_Period()
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

	public boolean isAdvancePayment()
	{
		return this.period.get_Frequence().equals(InstalmentFrequence.TillPayoff);
	}

	public String getVerificationInfo(BigDecimal lot, int decimals)
	{
		if(this.isAdvancePayment())
		{
			String advanceAmount = AppToolkit.format(lot.multiply(this.downPayment), decimals);
			return InstalmentLanguage.PaymentMode + ":" + InstalmentLanguage.AdvancePayment +
				"  " + InstalmentLanguage.AdvanceAmount + ":" + advanceAmount;
		}
		else
		{
			return InstalmentLanguage.InstalmentType + ":" + this.instalmentType.toLocalString() + "  "
				+ InstalmentLanguage.Period + ":" + this.period.toString() + "  "
				+ InstalmentLanguage.DownPayment + ":" + this.get_DownPaymentInFormat() + "  "
				+ InstalmentLanguage.RecalculateRateType + ":" + this.recalculateRateType.toLocalString();
		}
	}

	public void update(Guid instalmentPolicyId, InstalmentPeriod period, BigDecimal downPayment,
					   InstalmentType instalmentType, RecalculateRateType recalculateRateType, BigDecimal fee, PaymentMode paymentMode)
	{
		this.instalmentPolicyId = instalmentPolicyId;
		this.period = period;
		if(downPayment != null) this.downPayment = downPayment;
		if(instalmentType != null) this.instalmentType = instalmentType;
		if(recalculateRateType != null) this.recalculateRateType = recalculateRateType;
		this.fee = fee;
		this.paymentMode = paymentMode;
	}

	public static InstalmentInfo createDefaultAdvancePaymentInfo(MakeOrderAccount makeOrderAccount)
	{
		InstalmentPolicy instalmentPolicy = makeOrderAccount.get_InstalmentPolicy();
		return new InstalmentInfo(instalmentPolicy.get_Id(), InstalmentPeriod.TillPayoffInstalmentPeriod,
			instalmentPolicy.get_TillPayoffDetail().get_MinDownPayment(), InstalmentType.EqualInstallment, RecalculateRateType.NextMonth, BigDecimal.ZERO, PaymentMode.AdvancePayment);
	}

	public static InstalmentInfo createDefaultInstalmentInfo(MakeOrderAccount makeOrderAccount)
	{
		InstalmentPolicy instalmentPolicy = makeOrderAccount.get_InstalmentPolicy();
		InstalmentPeriod maxPeriod = null;
		for(InstalmentPeriod period : instalmentPolicy.getActivePeriods())
		{
			if(maxPeriod == null || maxPeriod.get_Period() < period.get_Period()) maxPeriod = period;
		}
		if(maxPeriod == null) return null;
		InstalmentPolicyDetail instalmentPolicyDetail = instalmentPolicy.get_InstalmentPolicyDetail(maxPeriod);
		InstalmentType instalmentType = InstalmentType.EqualInstallment;
		if((instalmentPolicy.get_AllowedInstalmentTypes().value() & InstalmentType.EqualInstallment.value()) != InstalmentType.EqualInstallment.value())
		{
			instalmentType = InstalmentType.EqualPrincipal;
		}
		RecalculateRateType recalculateRateType = RecalculateRateType.NextMonth;
		if((instalmentPolicy.get_AllowedRecalculateRateTypes().value() & RecalculateRateType.NextMonth.value()) != RecalculateRateType.NextMonth.value())
		{
			recalculateRateType = RecalculateRateType.NextYear;
		}
		BigDecimal fee = BigDecimal.ZERO;
		return new InstalmentInfo(instalmentPolicy.get_Id(), maxPeriod,
			instalmentPolicyDetail.get_MinDownPayment(), instalmentType, recalculateRateType, fee, PaymentMode.Instalment);
	}
}
