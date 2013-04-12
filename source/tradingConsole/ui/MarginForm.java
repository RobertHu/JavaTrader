package tradingConsole.ui;


import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.util.Iterator;

import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.Language;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import javax.swing.JDialog;
import javax.swing.JFrame;
import framework.StringHelper;
import com.jidesoft.swing.JideSwingUtilities;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import framework.Guid;
import tradingConsole.settings.PublicParametersManager;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.event.WindowEvent;

public class MarginForm extends JDialog implements VerifyMarginPin.IMarginPinVerifyCallback //Frame
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;

	private JDialog _additionalClientForm = null;
	private JDialog _agentForm = null;
	private JDialog _callMarginExtensionForm = null;
	private JDialog _paymentInstructionForm = null;
	private JDialog _bankAccountForm = null;
	private JDialog _fundTransferForm = null;
	private JDialog _paymentInstructionCashForm = null;
	private JDialog _paymentInstructionInternalForm = null;
	private JDialog _transfer99BillForm = null;

	private JFrame _parent;

	public MarginForm(JFrame parent, TradingConsole owner, SettingsManager settingsManager)
	{
		super(parent, true);
		try
		{
			this._parent = parent;
			this._owner = owner;
			this._settingsManager = settingsManager;

			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());

			this.accountStaticText.setText(Language.MarginPageOnloadAccount);
			this.documentStaticText.setText(Language.MarginPageOnloadDocument);
			this.nextButton.setText(Language.MarginPageOnloadNext);

			this.fillAccount();
			this.fillDocument();
			this.accountChoice.setEditable(false);
			this.documentChoice.setEditable(false);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void fillAccount()
	{
		this.accountChoice.disableItemEvent();
		this.reallyFillAccount();
		this.accountChoice.enableItemEvent();
	}

	private void reallyFillAccount()
	{
		this.accountChoice.removeAllItems();
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			boolean verifiedCustomerIdentity = account.get_VerifiedCustomerIdentity();
			if (verifiedCustomerIdentity && account.get_SupportMarginAction())
			{
				this.accountChoice.addItem(account.get_Code());
			}
		}
		//this.accountChoice.sort(true);
		this.accountChoice.setSelectedIndex(0);
	}

	private void fillDocument()
	{
		this.documentChoice.disableItemEvent();
		this.reallyFillDocument();
		this.documentChoice.enableItemEvent();
	}

	private void reallyFillDocument()
	{
		this.documentChoice.removeAllItems();
		String accountCode = this.accountChoice.getSelectedItem().toString();
		Account account = this._settingsManager.getAccount(accountCode);

		if (account.get_EnableOwnerRegistration())
		{
			this.documentChoice.addItem(Language.AdditionalClientPrompt);
		}

		if (account.get_EnableAgentRegistration())
		{
			this.documentChoice.addItem(Language.AgentPrompt);
		}

		if (account.get_EnableCMExtension())
		{
			this.documentChoice.addItem(Language.CallMarginExtensionPrompt);
		}

		if (account.get_EnablePI())
		{
			if (account.get_CustomerId().compareTo(this._settingsManager.get_Customer().get_UserId()) == 0)
			{
				this.documentChoice.addItem(Language.BankAccount);
			}
			this.documentChoice.addItem(Language.PaymentInstructionPrompt);
		}

		if (account.get_EnablePICash())
		{
			this.documentChoice.addItem(Language.PICashPrompt);
		}

		if (account.get_EnablePIInterACTransfer())
		{
			this.documentChoice.addItem(Language.PIInterACTransferPrompt);
		}

		if (this._settingsManager.existsMerchantIdFor99Bill(account)
			&& !StringHelper.isNullOrEmpty(this._owner.get_ServiceManager().get_serviceUrlFor99Bill()))
		{
			Guid sourceCurrencyId = this._settingsManager.get_SystemParameter().get_RMBCurrencyId();
			Guid targetCurrencyId = account.get_Currency().get_Id();
			if (this._settingsManager.getCurrencyRate(sourceCurrencyId, targetCurrencyId) != null)
			{
				this.documentChoice.addItem(Language.Transfer99Bill);
			}
		}

		//this.documentChoice.addItem(Language.FundTransferPrompt);
		if (this.documentChoice.getItemCount() > 0)
		{
			this.documentChoice.setSelectedIndex(0);
		}
	}

	private void next()
	{
		String accountCode = this.accountChoice.getSelectedItem().toString();
		if (framework.StringHelper.isNullOrEmpty(accountCode))
		{
			//AlertDialogForm.showDialog(this, null, true, Language.MarginPageBtnNextPrompt);
			return;
		}
		Account account = this._settingsManager.getAccount(accountCode);
		if (account == null)
		{
			return;
		}
		String document = this.documentChoice.getSelectedItem().toString();
		if (document.equals(Language.AdditionalClientPrompt))
		{
			if (this._additionalClientForm != null)
			{
				this._additionalClientForm.dispose();
			}
			this._additionalClientForm = new AdditionalClientForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._additionalClientForm);
			int x = (this._owner.get_MainForm().getWidth() - this._additionalClientForm.getWidth())/2;
			this._additionalClientForm.setLocation(x, this._additionalClientForm.getLocation().y);
			this._additionalClientForm.show();
		}
		else if (document.equals(Language.AgentPrompt))
		{
			if (this._agentForm != null)
			{
				this._agentForm.dispose();
			}
			this._agentForm = new AgentForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._agentForm);
			this._agentForm.show();
		}
		else if (document.equals(Language.CallMarginExtensionPrompt))
		{
			if (this._callMarginExtensionForm != null)
			{
				this._callMarginExtensionForm.dispose();
			}
			this._callMarginExtensionForm = new CallMarginExtensionForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._callMarginExtensionForm);
			this._callMarginExtensionForm.show();
		}
		else if (document.equals(Language.PaymentInstructionPrompt))
		{
			if (this._paymentInstructionForm != null)
			{
				this._paymentInstructionForm.dispose();
			}
			this._paymentInstructionForm = new PaymentInstructionForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._paymentInstructionForm);
			this._paymentInstructionForm.show();
		}
		else if (document.equals(Language.BankAccount))
		{
			if (this._bankAccountForm != null)
			{
				this._bankAccountForm.dispose();
			}
			this._bankAccountForm = new BankAccountForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._bankAccountForm);
			this._bankAccountForm.show();
		}
		else if (document.equals(Language.FundTransferPrompt))
		{
			if (this._fundTransferForm != null)
			{
				this._fundTransferForm.dispose();
			}
			this._fundTransferForm = new FundTransferForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._fundTransferForm);
			this._fundTransferForm.show();
		}
		else if (document.equals(Language.PICashPrompt))
		{
			if (this._paymentInstructionCashForm != null)
			{
				this._paymentInstructionCashForm.dispose();
			}
			this._paymentInstructionCashForm = new PaymentInstructionCashForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._paymentInstructionCashForm);
			this._paymentInstructionCashForm.show();
		}
		else if (document.equals(Language.PIInterACTransferPrompt))
		{
			if (this._paymentInstructionInternalForm != null)
			{
				this._paymentInstructionInternalForm.dispose();
			}
			this._paymentInstructionInternalForm = new PaymentInstructionInternalForm(this._owner, this._settingsManager, account);
			JideSwingUtilities.centerWindow(this._paymentInstructionInternalForm);
			this._paymentInstructionInternalForm.show();
		}
		else if (document.equals(Language.Transfer99Bill))
		{
			if (this._settingsManager.get_SystemParameter().get_EnableMarginPin())
			{
				VerifyMarginPin verifyMarginPin = new VerifyMarginPin(this, this._owner.get_TradingConsoleServer(), account, this);
				JideSwingUtilities.centerWindow(verifyMarginPin);
				verifyMarginPin.show();
				verifyMarginPin.toFront();
				verifyMarginPin.requestFocus();
			}
			else
			{
				deposit();
			}
			/*if (this._transfer99BillForm != null)
				{
			 this._transfer99BillForm.dispose();
				}
				this._transfer99BillForm = new Transfer99BillForm(this._owner, this._settingsManager, account);
				JideSwingUtilities.centerWindow(this._transfer99BillForm);
				this._transfer99BillForm.show();*/
		}
	}

	public void OnMarginPinVerified(boolean isValidMarginPin)
	{
		if (isValidMarginPin)
		{
			deposit();
		}
		else
		{
			this.dispose();
		}
	}

	private void deposit()
	{
		String accountCode = this.accountChoice.getSelectedItem().toString();
		Account account = this._settingsManager.getAccount(accountCode);

		String url = this._owner.get_ServiceManager().get_serviceUrlFor99Bill();
		url += "PaymentRequest.aspx?consumerCode=" + this._owner.get_ServiceManager().get_companyCodeForTransfer();
		url += "&culture=" + PublicParametersManager.version;
		url += "&accountId=" + account.get_Id().toString();
		url += "&customer=" + this._settingsManager.get_Customer().get_CustomerCode();
		url += "&isNewAccount=false";
		url += "&emailAddress=";
		if (!StringHelper.isNullOrEmpty(this._settingsManager.get_Customer().get_Email()))
		{
			url += this._settingsManager.get_Customer().get_Email();
		}

		BareBonesBrowserLaunch.openURL(url);
	}

	private void jbInit() throws Exception
	{
		Font font = new Font("SansSerif", Font.BOLD, 12);
		this.addWindowListener(new MarginUi_this_windowAdapter(this));

		this.setSize(360, 180);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.marginFormTitle);
		if (this._owner.get_ServiceManager().get_ShowMarginAsChuRuJin())
		{
			this.setTitle(Language.ChuRuJin);
		}
		this.setBackground(FormBackColor.marginForm);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		nextButton.setBounds(new Rectangle(141, 115, 101, 23));
		nextButton.setFont(font);
		nextButton.setText("Next");
		nextButton.addActionListener(new MarginUi_nextButton_actionAdapter(this));
		accountStaticText.setText("Account:");
		accountStaticText.setFont(font);
		accountStaticText.setBounds(new Rectangle(41, 40, 79, 21));
		documentStaticText.setText("Document");
		documentStaticText.setFont(font);
		documentStaticText.setBounds(new Rectangle(41, 66, 87, 20));
		accountChoice.setBounds(new Rectangle(139, 36, 174, 20));
		accountChoice.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		documentChoice.setBounds(new Rectangle(139, 68, 174, 20));
		documentChoice.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		this.add(documentChoice);
		this.add(accountChoice);
		accountChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				fillDocument();
			}
		});
		this.add(documentStaticText);
		this.add(accountStaticText);
		this.add(nextButton);
	}

	PVButton2 nextButton = new PVButton2();
	PVStaticText2 accountStaticText = new PVStaticText2();
	PVStaticText2 documentStaticText = new PVStaticText2();
	JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	JAdvancedComboBox documentChoice = new JAdvancedComboBox();

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	class MarginUi_this_windowAdapter extends WindowAdapter
	{
		private MarginForm adaptee;
		MarginUi_this_windowAdapter(MarginForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	public void nextButton_actionPerformed(ActionEvent e)
	{
		this.next();
	}
}

class MarginUi_nextButton_actionAdapter implements ActionListener
{
	private MarginForm adaptee;
	MarginUi_nextButton_actionAdapter(MarginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.nextButton_actionPerformed(e);
	}
}
