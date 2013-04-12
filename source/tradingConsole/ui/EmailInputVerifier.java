package tradingConsole.ui;

import javax.swing.*;

import framework.*;

public class EmailInputVerifier extends InputVerifier
{
	private boolean _allowEmpty;
	private boolean _allowPhoneNumber;

	public EmailInputVerifier(boolean allowEmpty)
	{
		this(allowEmpty, false);
	}

	public EmailInputVerifier(boolean isAllowEmpty, boolean allowPhoneNumber)
	{
		this._allowEmpty = isAllowEmpty;
		this._allowPhoneNumber = allowPhoneNumber;
	}

	public boolean verify(JComponent input)
	{
		if (input instanceof JTextField)
		{
			JTextField formattedTextField = (JTextField)input;
			String text = formattedTextField.getText();
			return EmailInputVerifier.isValidEmail(this._allowEmpty, this._allowPhoneNumber, text);
		}
		return true;
	}

	public static boolean isValidEmail(boolean allowEmpty, String email)
	{
		return EmailInputVerifier.isValidEmail(allowEmpty, false, email);
	}

	public static boolean isValidEmail(boolean allowEmpty, boolean allowPhoneNumber, String email)
	{
		if (allowEmpty && StringHelper.isNullOrEmpty(email))
		{
			return true;
		}
		if(allowPhoneNumber && EmailInputVerifier.isPhoneNumber(email))
		{
			return true;
		}
		return !(StringHelper.isNullOrEmpty(email)
				 || email.substring(0, 1).equals(".")
				 || email.substring(0, 1).equals("@")
				 || email.indexOf('@', 0) == -1
				 || email.indexOf('.', 0) == -1
				 || email.lastIndexOf("@") == email.length() - 1
				 || email.lastIndexOf(".") == email.length() - 1);
	}

	private static boolean isPhoneNumber(String email)
	{
		if(StringHelper.isNullOrEmpty(email) || email.length() < 3) return false;
		char[] number = email.toCharArray();
		if(!Character.isDigit(number[0]) && number[0] != '+') return false;
		if(number[0] == '+' && !Character.isDigit(number[1])) return false;

		for(int index = 1; index < number.length; index++)
		{
			if(!Character.isDigit(number[index]) && number[index] != '-') return false;
		}
		return true;
	}
}
