package tradingConsole.ui.language;

import java.io.File;

import framework.xml.XmlNode;

import tradingConsole.AppToolkit;
import tradingConsole.settings.PublicParametersManager;

public class ActivateAccount
{
	public static String Title = "Activate Account";
	public static String Header1 = "Dear Customer,";
	public static String Header2 = "This is your first attempt to use our system. For safety reason,";
	public static String Header3 = "the system requires you to activate the account by changing the";
	public static String Header4 = "password.";
	public static String OldPassword = "Old password:";
	public static String NewPassword = "New password:";
	public static String ConfirmNewPassword = "Confirm password:";
	public static String Ok = "Ok";
	public static String Next = "Next";
	public static String RecoverPasswordQuotationSettingNotify = "Recover Password";
	public static String RecoverPasswordDataNotAvailable = "The data is not available for the recover password, please try again!";
	public static String Passed = "Your password has been successfuly changed!";
	public static String Failed = "Failed to change password!";
	public static String ReEnter = "Please enter your new/comfirm password again!";
	public static String SubmissionFailed = "Submission failed, please try again!";
	public static String Notify1 = "The new passwords should contain combination of 8 - 16 digits";
	public static String Notify2 = "and/or letters.";

	public static void initialize()
	{
		//String filePath = AppToolkit.get_LanguageDirectory()
		//	+ PublicParametersManager.version + File.separator + "ActivateAccount.xml";
		//XmlNode xmlNode = AppToolkit.getXml(filePath);
		XmlNode xmlNode = AppToolkit.getResourceXml("Language/" + PublicParametersManager.version, "ActivateAccount.xml");
		if (xmlNode!=null)
		{
			Language.setValue(ActivateAccount.class, xmlNode.get_Item("ActivateAccount"));
		}
	}
}
