package tradingConsole.ui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import tradingConsole.ui.language.Language;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.TradingConsole;
import tradingConsole.settings.MerchantInfoFor99Bill;
import framework.StringHelper;
import java.io.*;
import java.text.DecimalFormat;
import tradingConsole.ui.language.Login;
import javax.swing.JOptionPane;
import java.util.Arrays;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import framework.Guid;
import tradingConsole.CurrencyRate;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import javax.swing.text.MaskFormatter;
import tradingConsole.AppToolkit;
import tradingConsole.settings.BankFor99Bill;

public class Transfer99BillForm extends JDialog
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;
	private MerchantInfoFor99Bill _merchantInfoFor99Bill;
	private long _payNo;
	private CurrencyRate _CurrencyRate;
	private LotDocumentListener _lotDocumentListener;

	public Transfer99BillForm(TradingConsole owner, SettingsManager settingsManager, Account account)
	{
		super(owner.get_MainForm(), true);
		try
		{
			this._owner = owner;
			this._settingsManager = settingsManager;
			this._account = account;
			Guid sourceCurrencyId = this._settingsManager.get_SystemParameter().get_RMBCurrencyId();
			Guid targetCurrencyId = account.get_Currency().get_Id();
			this._CurrencyRate = this._settingsManager.getCurrencyRate(sourceCurrencyId, targetCurrencyId);
			jbInit();
			this._exchangeRateLabel.setText(Double.toString(this._CurrencyRate.get_RateIn()));
			this._submitButton.setEnabled(false);
			this._exchangeCurrencyLabel.setText(account.get_Currency().get_Code());

			if(account.get_Currency().get_MinDeposit() != null)
			{
				this._minAmountLabel.setText(Language.MinDeposit + AppToolkit.format(account.get_Currency().get_MinDeposit().doubleValue(), 2));
			}
			this._lotDocumentListener = new LotDocumentListener(this);
			_amountFormattedTextField.getDocument().addDocumentListener(this._lotDocumentListener);

			for(BankFor99Bill bank : this._settingsManager.getBanksFor99Bill())
			{
				this._bank.addItem(bank);
			}
			if(this._bank.getItemCount() > 0) this._bank.setSelectedIndex(0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private static long _maxAmount = 99999999999999l;
	private static long _minAmount = 1;
	private long _oldAmount = 0;
	private String _validAmountInString = "";
	void update()
	{
		if(!StringHelper.isNullOrEmpty(this._amountFormattedTextField.getText())
			&& !this._amountFormattedTextField.getText().startsWith("."))
		{
			boolean needRefreshAmount = false;
			long amount = 0;

			String value = this._amountFormattedTextField.getText();
			if(value.endsWith(".")) value = value.substring(0, value.length() - 1);
			if(value.indexOf(".") > 0 && value.indexOf(".") < (value.length() - 3))
			{
				value = value.substring(0, value.indexOf(".") + 3);
				needRefreshAmount = true;
			}

			boolean hasInvalidChar = false;
			StringBuilder sb = new StringBuilder();
			for(int index = 0; index < value.length(); index++)
			{
				if(Character.isDigit(value.charAt(index)) || value.charAt(index) == '.')
				{
					sb.append(value.charAt(index));
				}
				else
				{
					hasInvalidChar = true;
				}
			}

			if(hasInvalidChar)
			{
				_validAmountInString = sb.toString();
				_amountFormattedTextField.getDocument().removeDocumentListener(this._lotDocumentListener);
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						_amountFormattedTextField.setText(_validAmountInString);
						_amountFormattedTextField.getDocument().addDocumentListener(_lotDocumentListener);
					}
				});
				return;
			}

			value = sb.toString();
			int index = value.indexOf(".");
			if (index > 0)
			{
				amount = Long.parseLong(value.substring(0, value.indexOf("."))) * 100 +
					Integer.parseInt(value.substring(value.indexOf(".") + 1));
			}
			else
			{
				amount = Long.parseLong(value) * 100;
			}

			BigDecimal minDeposit = this._account.get_Currency().get_MinDeposit();
			if(minDeposit != null)
			{
				minDeposit = minDeposit.multiply(BigDecimal.valueOf(100));
				this._submitButton.setEnabled(amount >= minDeposit.longValue());
			}
			else
			{
				this._submitButton.setEnabled(amount > 0);
			}

			if(amount != 0)
			{
				if (amount >= 99999999999999l)
				{
					amount = 0;
					needRefreshAmount = true;
				}
			}
			this._oldAmount = amount;

			if (needRefreshAmount)
			{
				_amountFormattedTextField.getDocument().removeDocumentListener(this._lotDocumentListener);

				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						String value = Long.toString( (long) (_oldAmount / 100));
						long remain = _oldAmount % 100;
						if (remain > 0)
						{
							value += "." + Long.toString(remain);
						}
						_amountFormattedTextField.setText(value);
						_amountFormattedTextField.getDocument().addDocumentListener(_lotDocumentListener);
					}
				});
			}
			double exchangedAmount = amount / this._CurrencyRate.get_RateIn();
			exchangedAmount = ( (double) ( (long) (exchangedAmount * 10000))) / 1000000;
			int decimals = this._account.get_Currency().get_Decimals();
			this._exchangeAmountLabel.setText(AppToolkit.format(exchangedAmount, decimals));
			return;
		}

		this._submitButton.setEnabled(false);
	}

	private void jbInit() throws Exception
	{
		Font font = new Font("SansSerif", Font.BOLD, 12);
		//MaskFormatter format = new MaskFormatter("############.##");
		//_amountFormattedTextField = new JFormattedTextField(format);
		_amountFormattedTextField = new JFormattedTextField();

		this.setSize(380, 280);
		this.setTitle(Language.Transfer99Bill);
		this.getContentPane().setLayout(gridBagLayout1);
		_orderNoCatpionLabel.setText(Language.PayId);
		_orderNoCatpionLabel.setFont(font);
		_orderNoLabel.setToolTipText("");
		_accountLabel.setToolTipText("");
		_accountCaptionLabel.setToolTipText("");
		_accountCaptionLabel.setFont(font);
		_accountCaptionLabel.setText(Language.Account);
		_amountCaptionLabel.setToolTipText("");
		_amountCaptionLabel.setFont(font);

		_currencyCaptionLabel.setText(Language.Currency);
		_currencyCaptionLabel.setFont(font);
		_currencyLabel.setText(Language.RMB);

		_exchangeRateCaptionLabel.setText(Language.ExchangeRate);
		_exchangeRateCaptionLabel.setFont(font);
		_exchangeCurrencyCaptionLabel.setText(Language.ExchangeCurrency);
		_exchangeCurrencyCaptionLabel.setFont(font);
		_exchangeAmountCaptionLabel.setText(Language.ExchangeAmount);
		_exchangeAmountCaptionLabel.setFont(font);

		_bankCaptionLabel.setText(Language.Bank);
		_bankCaptionLabel.setFont(font);

		_amountCaptionLabel.setText(Language.Amount);
		_amountFormattedTextField.setText("");
		_submitButton.setSelectedIcon(null);
		_submitButton.setText(Language.Submit);
		_submitButton.addActionListener(new Transfer99BillForm_submitButton_actionAdapter(this));
		_submitButton.setFont(font);
		_amountFormattedTextField.setInputVerifier(null);

		this.addWindowListener(new Transfer99BillForm_this_windowAdapter(this));

		this.getContentPane().add(_orderNoCatpionLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 20, 0, 0), 40, 5));
		this.getContentPane().add(_orderNoLabel, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 0, 0),0, 0));

		this.getContentPane().add(_accountCaptionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_accountLabel, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0),0, 5));

		this.getContentPane().add(_exchangeCurrencyCaptionLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_exchangeCurrencyLabel, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 0), 0, 0));

		this.getContentPane().add(_exchangeAmountCaptionLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_amountFormattedTextField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 0), 80, 0));
		this._minAmountLabel.setHorizontalAlignment(SwingConstants.LEFT);
		this.getContentPane().add(this._minAmountLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 80, 0));
		_amountFormattedTextField.setHorizontalAlignment(SwingConstants.RIGHT);

		this.getContentPane().add(_currencyCaptionLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 0, 0), 40, 5));
		this.getContentPane().add(_currencyLabel, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 0), 0, 5));

		/*this.getContentPane().add(_exchangeRateCaptionLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_exchangeRateLabel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 0), 100, 5));
		_exchangeRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		_exchangeRateLabel.setBorder(null);
		_exchangeRateLabel.setBackground(null);*/

		this.getContentPane().add(_amountCaptionLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_exchangeAmountLabel, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 0), 80, 5));
		_exchangeAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		_exchangeAmountLabel.setBorder(null);
		_exchangeAmountLabel.setBackground(null);

		this.getContentPane().add(_bankCaptionLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 40, 5));
		this.getContentPane().add(_bank, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 1, 0, 5), 0, 0));

		this.getContentPane().add(_submitButton, new GridBagConstraints(0, 7,3, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(25, 0, 0, 0), 60, 0));
	}

	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel _orderNoLabel = new JLabel();
	private JLabel _currencyLabel = new JLabel();
	private JLabel _currencyCaptionLabel = new JLabel();
	private JLabel _orderNoCatpionLabel = new JLabel();
	private JLabel _accountLabel = new JLabel();
	private JLabel _accountCaptionLabel = new JLabel();
	private JLabel _amountCaptionLabel = new JLabel();
	private JFormattedTextField _amountFormattedTextField;
	private JButton _submitButton = new JButton();

	private JLabel _exchangeRateCaptionLabel = new JLabel();
	private JTextField _exchangeRateLabel = new JTextField();

	private JLabel _exchangeCurrencyCaptionLabel = new JLabel();
	private JLabel _exchangeCurrencyLabel = new JLabel();
	private JLabel _minAmountLabel = new JLabel();

	private JLabel _exchangeAmountCaptionLabel = new JLabel();
	private JTextField _exchangeAmountLabel = new JTextField();

	private JLabel _bankCaptionLabel = new JLabel();
	private JComboBox _bank = new JComboBox();

	public void this_windowOpened(WindowEvent e)
	{
		this._merchantInfoFor99Bill = this._settingsManager.getMerchantIdFor99Bill(this._account);
		long payNo = this._owner.get_TradingConsoleServer().getNextPaySequence(this._merchantInfoFor99Bill.get_Id());
		if(payNo == -1)
		{
			AlertDialogForm.showDialog(this._owner.get_MainForm(), null, true, Language.CantGetPayNoFor99Bill);
			this.dispose();
		}
		this._orderNoLabel.setText(Long.toString(payNo));
		this._payNo = payNo;
		this._accountLabel.setText(this._account.get_Code());
	}

	public void _submitButton_actionPerformed(ActionEvent e)
	{
		if(StringHelper.isNullOrEmpty(this._amountFormattedTextField.getText())) return;

		BankFor99Bill bank = (BankFor99Bill)this._bank.getSelectedItem();
		String url = StringHelper.format("{0}&culture={1}&accountId={2}&customer={3}&isNewAccount={4}&emailAddress={5}", new Object[]{
                    this._owner.get_ServiceManager().get_serviceUrlFor99Bill(), this._payNo, this._account.get_Id(), this._settingsManager.get_Customer().get_CustomerName(), false, this._exchangeAmountLabel.getText()});

		BareBonesBrowserLaunch.openURL(url);
		this.dispose();
	}
}

class LotDocumentListener implements DocumentListener
{
	private Transfer99BillForm _owner;

	LotDocumentListener(Transfer99BillForm owner)
	{
		this._owner = owner;
	}

	public void insertUpdate(DocumentEvent e)
	{
		this._owner.update();
	}

	public void removeUpdate(DocumentEvent e)
	{
		this._owner.update();
	}

	public void changedUpdate(DocumentEvent e)
	{
		this._owner.update();
	}
}

class BareBonesBrowserLaunch {

   static final String[] browsers = { "google-chrome", "firefox", "opera",
	  "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
   static final String errMsg = "Error attempting to launch web browser";

   public static void openURL(String url) {
	  try {  //attempt to use Desktop library from JDK 1.6+
		 Class<?> d = Class.forName("java.awt.Desktop");
		 d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
			d.getDeclaredMethod("getDesktop").invoke(null),
			new Object[] {java.net.URI.create(url)});
		 //above code mimicks:  java.awt.Desktop.getDesktop().browse()
		 }
	  catch (Exception ignore) {  //library not available or failed
		 String osName = System.getProperty("os.name");
		 try {
			if (osName.startsWith("Mac OS")) {
			   Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
				  "openURL", new Class[] {String.class}).invoke(null,
				  new Object[] {url});
			   }
			else if (osName.startsWith("Windows"))
			{
				try
				{
					Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
				}
				catch(Exception exception)
				{
					Runtime.getRuntime().exec("start " + url);
				}
			}
			else { //assume Unix or Linux
			   String browser = null;
			   for (String b : browsers)
				  if (browser == null && Runtime.getRuntime().exec(new String[]
						{"which", b}).getInputStream().read() != -1)
					 Runtime.getRuntime().exec(new String[] {browser = b, url});
			   if (browser == null)
				  throw new Exception();
			   }
			}
		 catch (Exception e) {
			JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
			}
		 }
	  }

   }


class Transfer99BillForm_this_windowAdapter extends WindowAdapter
{
	private Transfer99BillForm adaptee;
	Transfer99BillForm_this_windowAdapter(Transfer99BillForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void windowOpened(WindowEvent e)
	{
		adaptee.this_windowOpened(e);
	}
}

class Transfer99BillForm_submitButton_actionAdapter implements ActionListener
{
	private Transfer99BillForm adaptee;
	Transfer99BillForm_submitButton_actionAdapter(Transfer99BillForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee._submitButton_actionPerformed(e);
	}
}
