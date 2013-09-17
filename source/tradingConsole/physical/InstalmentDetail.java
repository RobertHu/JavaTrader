package tradingConsole.physical;

import java.math.BigDecimal;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.language.InstalmentLanguage;
import javax.swing.SwingConstants;
import tradingConsole.AppToolkit;
import framework.DateTime;
import tradingConsole.enumDefine.physical.InstalmentDetailStatus;
import framework.data.DataRow;
import framework.DBNull;
import tradingConsole.TradeDay;

public class InstalmentDetail
{
	private int sequence;
	private BigDecimal principal;
	private BigDecimal interest;
	private BigDecimal debitInterest;
	private DateTime paidDateTime;
	private DateTime paymentDateTimeOnPlan;
	private InstalmentDetailStatus status;

	private int decimals = 2;

	public InstalmentDetail(int sequence, BigDecimal principal, BigDecimal interest, int decimals)
	{
		this.sequence = sequence;
		this.principal = principal;
		this.interest = interest;

		this.decimals = decimals;
	}

	public InstalmentDetail(DataRow row, int decimals)
	{
		this.sequence = (Integer)row.get_Item("Sequence");
		this.principal = (BigDecimal)row.get_Item("Principal");
		this.interest = (BigDecimal)row.get_Item("Interest");
		this.debitInterest = (BigDecimal)row.get_Item("DebitInterest");
		this.paymentDateTimeOnPlan = (DateTime)row.get_Item("PaymentDateTimeOnPlan");
		if(row.get_Item("PaidDateTime") == DBNull.value)
		{
			this.paidDateTime = null;
		}
		else
		{
			this.paidDateTime = (DateTime)row.get_Item("PaidDateTime");
		}

		this.decimals = decimals;
	}

	public void initStatus(TradeDay currentTradeDay)
	{
		if(this.isPaid())
		{
			this.status = InstalmentDetailStatus.Paid;
		}
		else if(this.isOverdue(currentTradeDay))
		{
			this.status = InstalmentDetailStatus.Overdue;
		}
		else
		{
			this.status = InstalmentDetailStatus.NotPaid;
		}
	}

	public boolean isOverdue(TradeDay currentTradeDay)
	{
		if(this.paidDateTime == null)
		{
			return this.paymentDateTimeOnPlan.compareTo(currentTradeDay.get_TradeDay()) < 0;
		}
		else
		{
			if(this.paymentDateTimeOnPlan.compareTo(this.paidDateTime) >= 0)
			{
				return false;
			}
			else
			{
				if (this.paymentDateTimeOnPlan.compareTo(currentTradeDay.get_TradeDay()) == 0
					&& currentTradeDay.contains(this.paidDateTime))
				{
					return false;
				}
				else
				{
					return true;
				}
			}
		}
	}

	public boolean isPaid()
	{
		return this.paidDateTime != null;
	}

	public int get_Sequence()
	{
		return this.sequence;
	}

	public String get_PrincipalStr()
	{
		return AppToolkit.format(this.principal, this.decimals);
	}

	public String get_InterestStr()
	{
		return AppToolkit.format(this.interest, this.decimals);
	}

	public String get_InstalmentAmountStr()
	{
		return AppToolkit.format(this.interest.add(this.principal), this.decimals);
	}

	public String get_RepaymentDueDateStr()
	{
		return this.paymentDateTimeOnPlan.toString("yyyyMMdd");
	}

	public String get_PaidDateStr()
	{
		return this.paidDateTime == null ? "" : this.paidDateTime.toString("yyyyMMdd");
	}

	public String get_DebitInterestStr()
	{
		return AppToolkit.format(this.debitInterest, this.decimals);
	}

	public String get_StatusStr()
	{
		return this.status.toLocalString();
	}

	public DateTime get_RepaymentDueDate()
	{
		return this.paymentDateTimeOnPlan;
	}

	public DateTime get_RepaymentDate()
	{
		return this.paidDateTime;
	}

	public BigDecimal get_Principal()
	{
		return this.principal;
	}

	public BigDecimal get_Interest()
	{
		return this.interest;
	}

	public BigDecimal get_PenaltyInterest()
	{
		return this.debitInterest;
	}



	public static PropertyDescriptor[] getPropertyDescriptorsForExecutedOrder()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[8];
		int i = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "Sequence",
			true, null, InstalmentLanguage.Sequence, 40, SwingConstants.LEFT, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "RepaymentDueDateStr", true, null,
			InstalmentLanguage.RepaymentDueDate, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "PrincipalStr", true, null,
			InstalmentLanguage.Principal, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "InterestStr", true, null,
			InstalmentLanguage.Interest, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "InstalmentAmountStr", true, null,
			InstalmentLanguage.InstalmentAmount, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "DebitInterestStr", true, null,
			InstalmentLanguage.DebitInterest, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "PaidDateStr", true, null,
			InstalmentLanguage.PaidDate, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "StatusStr", true, null,
			InstalmentLanguage.Status, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[4];
		int i = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "Sequence",
			true, null, InstalmentLanguage.Sequence, 40, SwingConstants.LEFT, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "PrincipalStr", true, null,
			InstalmentLanguage.Principal, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "InterestStr", true, null,
			InstalmentLanguage.Interest, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentDetail.class, "InstalmentAmountStr", true, null,
			InstalmentLanguage.InstalmentAmount, 60, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		return propertyDescriptors;
	}
}
