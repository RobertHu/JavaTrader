package tradingConsole.ui;

import java.math.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.language.*;
import tradingConsole.service.ChangeLeverageResult;

public class ChangeLeverageForm extends JDialog
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private PVStaticText2 accountCaption = new PVStaticText2();
	private JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	private JAdvancedComboBox leverageChoice = new JAdvancedComboBox();
	private PVStaticText2 currentLevarageCaption = new PVStaticText2();
	private PVStaticText2 currentLevarage = new PVStaticText2();
	private PVStaticText2 newLevarageCaption = new PVStaticText2();
	private PVButton2 okButton = new PVButton2();
	private PVButton2 cancelButton = new PVButton2();

	public ChangeLeverageForm(JFrame parent,TradingConsole owner, SettingsManager settingsManager)
	{
		super(parent, true);
		try
		{
			this._owner = owner;
			this._settingsManager = settingsManager;

			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);

			this.fillAccount();
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void jbInit()
	{
		Font font = new Font("SansSerif", Font.PLAIN, 12);

		this.setSize(360, 180);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.ChangeLeverageText);
		this.setBackground(FormBackColor.marginForm);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		accountCaption.setBounds(new Rectangle(20, 30, 110, 20));
		accountCaption.setFont(font);
		accountCaption.setText(Language.Account);
		this.add(accountCaption);

		accountChoice.setBounds(new Rectangle(135, 30, 205, 20));
		accountChoice.setFont(font);
		this.add(accountChoice);

		currentLevarageCaption.setBounds(new Rectangle(20, 55, 110, 20));
		currentLevarageCaption.setFont(font);
		currentLevarageCaption.setText(Language.CurrentLeverage);
		this.add(currentLevarageCaption);

		currentLevarage.setBounds(new Rectangle(135, 55, 205, 20));
		currentLevarage.setFont(font);
		this.add(currentLevarage);

		newLevarageCaption.setBounds(new Rectangle(20, 80, 110, 20));
		newLevarageCaption.setFont(font);
		newLevarageCaption.setText(Language.NewLeverage);
		this.add(newLevarageCaption);

		leverageChoice.setBounds(new Rectangle(135, 80, 205, 20));
		leverageChoice.setFont(font);
		this.add(leverageChoice);

		okButton.setBounds(new Rectangle(20, 120, 80, 20));
		okButton.setFont(font);
		okButton.setText(Language.InstrumentSelectbtnOk);
		this.add(okButton);

		cancelButton.setBounds(new Rectangle(260, 120, 80, 20));
		cancelButton.setFont(font);
		cancelButton.setText(Language.InstrumentSelectbtnExit);
		this.add(cancelButton);

		leverageChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				okButton.setEnabled(true);
			}
		});

		accountChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateCurrentLeverage();
			}
		});

		okButton.addActionListener(new ChangeLeverageoFrm_okButton_actionAdapter(this));
		cancelButton.addActionListener(new ChangeLeverageForm_cancelButton_actionAdapter(this));
		this.fillLeverages();
	}

	private void fillLeverages()
	{
		SystemParameter sysParameter = this._settingsManager.get_SystemParameter();
		this.leverageChoice.addItem(Integer.toString(sysParameter.get_MinLeverage()));
		int index = 1;
		while(true)
		{
			int leverage = index * sysParameter.get_LeverageStep();
			if(leverage < sysParameter.get_MaxLeverage())
			{
				this.leverageChoice.addItem(Integer.toString(leverage));
				index++;
			}
			else
			{
				break;
			}
		}
		this.leverageChoice.addItem(Integer.toString(sysParameter.get_MaxLeverage()));
	}

	private void fillAccount()
	{
		this.reallyFillAccount();
	}

	private void updateCurrentLeverage()
	{
		this.okButton.setEnabled(false);
		Account account = this.getCurrentAccount();
		if (account == null) return;
		int leverage = 1;
		if(!account.get_RateMarginD().equals(BigDecimal.ZERO))
		{
			leverage = account.get_Leverage() == null ? Math.round(1/ account.get_RateMarginD().floatValue()) : account.get_Leverage();
		}
		this.currentLevarage.setText(Integer.toString(leverage));

		for(int index = 0; index < this.leverageChoice.getItemCount(); index++)
		{
			if(Integer.parseInt(this.leverageChoice.getItemAt(index).toString()) == leverage)
			{
				this.leverageChoice.setSelectedIndex(index);
				break;
			}
		}
	}

	private void reallyFillAccount()
	{
		this.accountChoice.removeAllItems();
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			boolean verifiedCustomerIdentity = account.get_VerifiedCustomerIdentity();
			if (verifiedCustomerIdentity)
			{
				this.accountChoice.addItem(account.get_Code());
			}
		}
		this.accountChoice.setSelectedIndex(0);
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		String accountCode = this.accountChoice.getSelectedItem().toString();
		Account account = this.getCurrentAccount();
		if (account == null) return;

		int leverage = Integer.parseInt(this.leverageChoice.getSelectedItem().toString());
		ChangeLeverageResult result = this._owner.get_TradingConsoleServer().changeLeverage(account.get_Id(), leverage);
		if(result == null || !result.get_IsSuceessed())
		{
			AlertDialogForm.showDialog(this, null, true, Language.ChangeLeverageFailed);
		}
		else
		{
			account.setLeverage(leverage);
			account.set_Necessary(result.get_Necessary().doubleValue());
			account.updateNode();
			this.dispose();
		}
	}

	private Account getCurrentAccount()
	{
		String accountCode = this.accountChoice.getSelectedItem().toString();
		if (framework.StringHelper.isNullOrEmpty(accountCode))
		{
			//AlertDialogForm.showDialog(this, null, true, Language.MarginPageBtnNextPrompt);
			return null;
		}
		return this._settingsManager.getAccount(accountCode);

	}

	public void cancelButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}
}

class ChangeLeverageoFrm_okButton_actionAdapter implements ActionListener
{
	private ChangeLeverageForm adaptee;
	ChangeLeverageoFrm_okButton_actionAdapter(ChangeLeverageForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
class ChangeLeverageForm_cancelButton_actionAdapter implements ActionListener
{
	private ChangeLeverageForm adaptee;
	ChangeLeverageForm_cancelButton_actionAdapter(ChangeLeverageForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.cancelButton_actionPerformed(e);
	}
}
