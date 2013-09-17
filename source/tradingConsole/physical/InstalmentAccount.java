package tradingConsole.physical;

import tradingConsole.Account;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.language.InstalmentLanguage;
import javax.swing.SwingConstants;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import tradingConsole.ui.language.Language;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;

public class InstalmentAccount
{
	private boolean enableInstalment;
	private Account account;
	private String lot;

	public InstalmentAccount(boolean enableInstalment, Account account, String lot)
	{
		this.account = account;
		this.enableInstalment = enableInstalment;
		this.lot = lot;
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];
		int i = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "Enable",
			false, null, InstalmentLanguage.Instalment, 30, SwingConstants.LEFT, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "AccountCode", true, null,
			Language.Account, 100, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstalmentAccount.class, "Lot", true, null,
			Language.Lots, 50, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public boolean get_Enable()
	{
		return this.enableInstalment;
	}

	public void set_Enable(boolean value)
	{
		this.enableInstalment = value;
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
