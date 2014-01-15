package tradingConsole.physical;

import tradingConsole.Account;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.language.InstalmentLanguage;
import javax.swing.SwingConstants;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import tradingConsole.ui.language.Language;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;
import tradingConsole.enumDefine.physical.PaymentMode;

public class InstalmentAccount
{
	private PaymentMode paymentMode;
	private Account account;
	private String lot;

	public InstalmentAccount(PaymentMode paymentMode, Account account, String lot)
	{
		this.account = account;
		this.paymentMode = paymentMode;
		this.lot = lot;
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];
		int i = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "PaymentModeString",
			true, null, InstalmentLanguage.PaymentMode, 50, SwingConstants.LEFT, null, null, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "AccountCode", true, null,
			Language.Account, 100, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "Lot", true, null,
			InstalmentLanguage.Lot, 50, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public String get_PaymentModeString()
	{
		return this.paymentMode.toLocalString();
	}

	public PaymentMode get_PaymentMode()
	{
		return this.paymentMode;
	}

	public void set_PaymentMode(PaymentMode paymentMode)
	{
		this.paymentMode = paymentMode;
	}

	public String get_AccountCode()
	{
		return this.account.get_Code();
	}

	public String get_Lot()
	{
		return this.lot;
	}

	public Account get_Account()
	{
		return this.account;
	}
}
