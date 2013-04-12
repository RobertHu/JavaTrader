package tradingConsole.ui.language;

import java.io.File;

import framework.xml.XmlNode;

import tradingConsole.AppToolkit;
import tradingConsole.settings.PublicParametersManager;
import tradingConsole.enumDefine.RiskDisclosureStatementType;
import Packet.ClassProduceMark;
import Packet.FieldProduceMark;

@ClassProduceMark public class Login
{
	public static String Unlogin = "Unlogin";
	public static String LoginFailed = "LoginFailed";
	public static String Loging = "Loging";
	public static String LoginSucceed = "Ready";
	public static String Logouting = "Logouting";
	public static String Logouted = "Logouted";
	public static String Ready = "Ready";
	public static String Connected = "Connected";
	public static String Disconnected = "Disconnected";
	public static String Kickouted = "Kickouted";
	@FieldProduceMark public static String Connecting = "Connecting";
	public static String loginFormTitle = "Connecting to";
	public static String ApplicationUpdatedMessage = "The online trading system has been upgraded, kindly re-install the components by the link provided below and run the downloaded  file in order to let the system runs properly.";
	public static String lblSystemLogon = "SYSTEM LOGON";
	public static String lblUserName = "User Name";
	public static String lblPasswords = "Passwords";
	public static String lblCaseSensitive = "* passwords are case-sensitive";
	public static String agreeCheckBoxCaption = "I Agree";
	public static String disagreeCheckBoxCaption = "I Disagree";
	public static String chkVisableOK = "I have read and agreed to the terms &amp; conditions of the";
	//public static String chkVisableOK2 = "before using this Trading Platform.  By entering this site, I agree to be bound by the terms and conditions in the said Agreement.";
	public static String btnVisableOKCaption = "Risk Disclosure Statement";
	public static String endUser = "Agreement and the Risk Disclosure Statement";
	public static String btnOk = "Enter";
	public static String lblLoginPrompt1 = "Login name must not be empty!";
	public static String lblLoginPrompt2 = "Authenication failed, please try again!";
	public static String lblLoginPrompt22 = "The User ID is suspended for the time being, please contact our Customer Service!";
	public static String lblLoginPrompt23 = "Login abended for communication problem, please try again!";
	public static String lblLoginPrompt24 = "The user account is locked on reaching failed login limit, please contact our customer service to reactivate the account!";
	public static String lblAttention = "ATTENTION";
	public static String lblDescription = "Please note that transactions over internet may be subject to interruption," +
		"transmission blackout, delayed transmission because of internet traffic," +
		"or incorrect data transmission due to the public nature of internet." +
		"We cannot assume responsibility for malfunctions in communications facilities " +
		"not under our control that may affect the accuracy or timeliness of messages " +
		"you sent.";
	public static String ForgetPasswordLink = "forgotten Password";
	public static String TradingAccountNotify = "Account Active";
	public static String TradingAccountActive = "Active";
	public static String TradingAccountNotSelect = "Please select an account!";
	public static String TradingAccountHasActivated = "The account has been activated!";
	public static String TradingAccountActiveLogin = "Active Login";
	public static String riskDisclosureStatement = "0";
	public static String showRiskStatementInRed = "false";

	public static void initialize()
	{
		//String filePath = AppToolkit.get_LanguageDirectory()
		//	+ PublicParametersManager.version + File.separator + "Login.xml";
		//XmlNode xmlNode = AppToolkit.getXml(filePath);
		XmlNode xmlNode = AppToolkit.getResourceXml("Language/" + PublicParametersManager.version, "Login.xml");
		if (xmlNode!=null)
		{
			Language.setValue(Login.class, xmlNode.get_Item("Login"));
		}
	}

	public static boolean showRiskStatementInRed()
	{
		return Login.showRiskStatementInRed.equalsIgnoreCase("true");
	}

	public static RiskDisclosureStatementType getRiskDisclosureStatementType()
	{
		if (riskDisclosureStatement.equalsIgnoreCase("1"))
		{
			return RiskDisclosureStatementType.Common;
		}
		else if (riskDisclosureStatement.equalsIgnoreCase("2"))
		{
			return RiskDisclosureStatementType.Special;
		}
		return RiskDisclosureStatementType.None;
	}
}
