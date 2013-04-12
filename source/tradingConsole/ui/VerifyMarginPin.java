package tradingConsole.ui;

import javax.swing.*;
import java.awt.Font;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import tradingConsole.Account;
import tradingConsole.TradingConsoleServer;
import tradingConsole.ui.language.Login;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

public class VerifyMarginPin extends JDialog
{
	interface IMarginPinVerifyCallback
	{
		void OnMarginPinVerified(boolean isValidMarginPin);
	}

	private PVStaticText2 accountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 accountStaticText = new PVStaticText2();
	private PVStaticText2 passwordStaticText = new PVStaticText2();
	private JPasswordField passwordField = new JPasswordField();
	private PVButton2 okButton = new PVButton2();
	private PVButton2 cancelButton = new PVButton2();

	private Account _account;
	private TradingConsoleServer _tradingConsoleServer;
	private boolean _isValidMarginPin;
	private IMarginPinVerifyCallback _callback;

	public VerifyMarginPin(JDialog parent, TradingConsoleServer tradingConsoleServer, Account account, IMarginPinVerifyCallback callback)
	{
		super(parent);

		this._account = account;
		this._tradingConsoleServer = tradingConsoleServer;
		this._isValidMarginPin = false;
		this._callback = callback;

		this.jbInit();
		this.setModal(true);
		this.okButton.setEnabled(false);
	}

	private void jbInit()
	{
		Font font = new Font("SansSerif", Font.PLAIN, 12);
		Font font2 = new Font("SansSerif", Font.BOLD, 12);

		this.setSize(360, 180);
		this.setResizable(false);
		this.setLayout(new GridBagLayout());
		this.setTitle(Language.plsInputMarginPIN);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		accountCaptionStaticText.setFont(font);
		accountCaptionStaticText.setText(Language.ReportAccountCode);
		this.add(this.accountCaptionStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 30, 0));

		accountStaticText.setFont(font2);
		accountStaticText.setText(this._account.get_Code());
		this.add(this.accountStaticText, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 50, 0));

		passwordStaticText.setFont(font);
		passwordStaticText.setText(Login.lblPasswords);
		this.add(this.passwordStaticText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 50, 0));

		this.add(this.passwordField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 50, 0));

		okButton.setFont(font2);
		okButton.setText(Language.InstrumentSelectbtnOk);
		this.add(this.okButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 0, 0), 50, 0));

		cancelButton.setFont(font2);
		cancelButton.setText(Language.InstrumentSelectbtnExit);
		this.add(this.cancelButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(30, 10, 0, 0), 50, 0));

		passwordField.requestFocus();

		okButton.addActionListener(new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
			{
				verify();
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_isValidMarginPin = false;
				dispose();
			}
		});

		passwordField.addKeyListener(new KeyAdapter()
			{
			public void keyPressed(KeyEvent e)
			{
				okButton.setEnabled(passwordField.getPassword() != null && passwordField.getPassword().length > 0);
				if(e.getKeyCode() == e.VK_ENTER)
				{
					verify();
				}
			}
		});
	}

	private int verifyFailedCount = 0;
	private void verify()
	{
		this._isValidMarginPin =  this._tradingConsoleServer.verifyMarginPin(this._account.get_Id(), new String(passwordField.getPassword()));
		if(this._isValidMarginPin || verifyFailedCount ++ >= 2)
		{
			this.dispose();
			this._callback.OnMarginPinVerified(this._isValidMarginPin);
		}
		else
		{
			AlertDialogForm.showDialog(this, Language.alertDialogFormTitle, true, Language.PasswordErrorPlsTryAgain);
		}
	}
}
