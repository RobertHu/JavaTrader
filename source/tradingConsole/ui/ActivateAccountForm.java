package tradingConsole.ui;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;

import tradingConsole.TradingConsole;
import tradingConsole.ui.language.ActivateAccount;
import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.service.UpdatePasswordResult;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import java.awt.Font;

public class ActivateAccountForm extends JDialog
{
	private static boolean _isSubmit = false;
	private TradingConsole _tradingConsole;

	public ActivateAccountForm(JFrame parent)
	{
		super(parent, true);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);

			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public ActivateAccountForm(JFrame parent, TradingConsole tradingConsole)
	{
		this(parent);

		this._tradingConsole = tradingConsole;

		this.setTitle(ActivateAccount.Title);
//		this.headerStaticText.setText(ActivateAccount.header);
		this.okButton.setText((this._tradingConsole.needRecoverPasswordQuotationSetting())?ActivateAccount.Next:ActivateAccount.Ok);

		/*this.currentPassword.setCase(0);
		this.newPassword.setCase(0);
		this.confirmPassword.setCase(0);*/

		this.currentStaticText.setHorizontalAlignment(SwingConstants.LEFT);
		this.newStaticText.setHorizontalAlignment(SwingConstants.LEFT);
		this.confirmStaticText.setHorizontalAlignment(SwingConstants.LEFT);

		this.currentStaticText.setText(ActivateAccount.OldPassword);
		this.newStaticText.setText(ActivateAccount.NewPassword);
		this.confirmStaticText.setText(ActivateAccount.ConfirmNewPassword);


		/*
		   0 - text string is not modified.
		   1 - text string is converted to the upper case.
		   2 - text string is converted to the lower case.
		   Default value is 0.

		 BUT DEFAULT is 2!!!
		*/

	}

	private boolean isValidPasswordInput(String inputString)
	{
		return (inputString.length() >= 8 && inputString.length() <= 16);
	}

	private void submit()
	{
		this.messageStaticText.setText("");
		if (this.isValidPasswordInput(new String(this.currentPassword.getPassword())) &&
			this.isValidPasswordInput(new String(this.newPassword.getPassword())) &&
			this.isValidPasswordInput(new String(this.confirmPassword.getPassword())))
		{
			if (new String(this.newPassword.getPassword()).equals(new String(this.confirmPassword.getPassword())))
			{
				if (this._tradingConsole.needRecoverPasswordQuotationSetting())
				{
					RecoverPasswordQuotationSettingForm recoverPasswordQuotationSettingForm = new RecoverPasswordQuotationSettingForm(this._tradingConsole.
						get_MainForm(), this, this._tradingConsole,
						new String(this.currentPassword.getPassword()), new String(this.newPassword.getPassword()));
					recoverPasswordQuotationSettingForm.show();
				}
				else
				{
					UpdatePasswordResult updatePasswordResult = this._tradingConsole.get_TradingConsoleServer().updatePassword(this._tradingConsole.
						get_LoginInformation().get_LoginName(),
						new String(this.currentPassword.getPassword()), new String(this.newPassword.getPassword()));
					boolean isSucceed = updatePasswordResult.get_IsSucceed();
					if (isSucceed)
					{
						ActivateAccountForm.Set_IsSubmit(true);
						this.dispose();

						this._tradingConsole.enterMainForm(null);
					}
					else
					{
						this.messageStaticText.setText(ActivateAccount.Failed);
					}
				}
			}
			else
			{
				this.messageStaticText.setText(ActivateAccount.ReEnter);
			}
		}
		else
		{
			this.messageStaticText.setText(ActivateAccount.SubmissionFailed);
		}
	}

	public static void Set_IsSubmit(boolean value)
	{
		ActivateAccountForm._isSubmit = value;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
		try
		{
			g.drawString(ActivateAccount.Header1, 20, 40);
			g.drawString(ActivateAccount.Header2, 20, 70);
			g.drawString(ActivateAccount.Header3, 20, 90);
			g.drawString(ActivateAccount.Header4, 20, 110);
			g.drawString(ActivateAccount.Notify1, 20, 340);
			g.drawString(ActivateAccount.Notify2, 20, 360);
		}
		catch (Throwable exception)
		{}
	}

//SourceCode End//////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new ActivateAccountForm_this_windowAdapter(this));

		this.setSize(420, 400);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setBackground(FormBackColor.activateAccountForm);

		//headerStaticText.setText("Activate Account");
		Font headerFont = new Font("SansSerif", Font.BOLD, 12);

		okButton.setText("OK");
		okButton.setFont(headerFont);
		okButton.addActionListener(new ActivateAccountForm_okButton_actionAdapter(this));

		currentStaticText.setText("Current");
		newStaticText.setText("New");
		confirmStaticText.setText("Confirm");

		currentStaticText.setFont(headerFont);
		newStaticText.setFont(headerFont);
		confirmStaticText.setFont(headerFont);

		currentPassword.setEchoChar('*');
		newPassword.setEchoChar('*');
		confirmPassword.setEchoChar('*');

		this.getContentPane().add(confirmPassword, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 4, 0, 10), 120, 0));
		this.getContentPane().add(messageStaticText, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 50, 10, 10), 0, 0));
		this.getContentPane().add(newPassword, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 4, 2, 10), 120, 0));

		this.getContentPane().add(currentPassword, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(19, 4, 3, 11), 120, 0));
		this.getContentPane().add(okButton, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
			, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(9, 14, 11, 0), 0, 0));
		this.getContentPane().add(newStaticText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 46, 0, 0), 0, 0));
		this.getContentPane().add(currentStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(47, 48, 3, 2), 0, 0));
		this.getContentPane().add(confirmStaticText, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 48, 0, 0), 0, 0));
		currentStaticText.setHorizontalAlignment(SwingConstants.LEFT);
		newStaticText.setHorizontalAlignment(SwingConstants.LEFT);
		confirmStaticText.setHorizontalAlignment(SwingConstants.LEFT);
	}

	//PVStaticText2 headerStaticText = new PVStaticText2();
	PVButton2 okButton = new PVButton2();
	JPasswordField currentPassword = new JPasswordField();
	JPasswordField newPassword = new JPasswordField();
	JPasswordField confirmPassword = new JPasswordField();
	PVStaticText2 currentStaticText = new PVStaticText2();
	PVStaticText2 newStaticText = new PVStaticText2();
	PVStaticText2 confirmStaticText = new PVStaticText2();
	PVStaticText2 messageStaticText = new PVStaticText2();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	public void this_windowClosing(WindowEvent e)
	{
		if (!ActivateAccountForm._isSubmit)
		{
			this._tradingConsole.disconnect(false);
		}
		this.dispose();
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	class ActivateAccountForm_this_windowAdapter extends WindowAdapter
	{
		private ActivateAccountForm adaptee;
		ActivateAccountForm_this_windowAdapter(ActivateAccountForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class ActivateAccountForm_okButton_actionAdapter implements ActionListener
{
	private ActivateAccountForm adaptee;
	ActivateAccountForm_okButton_actionAdapter(ActivateAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
