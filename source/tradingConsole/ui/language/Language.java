package tradingConsole.ui.language;

import framework.xml.XmlNode;
import framework.xml.XmlNodeList;
import framework.xml.XmlDocument;

import tradingConsole.AppToolkit;
import tradingConsole.settings.PublicParametersManager;
import java.io.IOException;
import java.util.ArrayList;
import tradingConsole.framework.ResourceHelper;
import java.util.Locale;
import Packet.ReformMessage;
//import java.io.File;

public class Language
{
	public static String initializeAutoUpdateServiceFailed = "Failed to initialize during calling autoUpdate service, please try again!";
	public static String initializeAuthenticationWebServiceFailed = "Failed to initialize during calling authentication service, please try again!";
	public static String initializeServiceFailed = "Failed to initialize during calling service, please try again!";
	public static String authenticateForTickByTickFailed = "Failed to initialize during calling tick by tick authenticate service, please try again!";
	public static String authenticateForChartFailed = "Failed to initialize during calling chart authenticate service, please try again!";

	public static String CannotShowTradingFact = "Can't show trading fact";
	public static String tradingFactButtonText = "Trading fact";
	public static String complainButtonText = "Error Submission";
	public static String tradingFactButtonCaption = "Trading fact";
	public static String executePriceCaption = "Exec Price";

	public static String complainSuccessfully="Complaint is sent successfully";

	public static String today = "Today";
	public static String thisWeek = "This week";
	public static String thisMonth = "This month";

	public static String rememberMe = "Remember me";
	public static String rememberPassword = "Remember password";

	public static String floatingFramesButtonText = "Floting/Dock";
	public static String floatingFramesButtonCaption = "Floting/Dock";

	public static String ExceedMaxInstrumentCount = "Instrument count will exceed the max count {0}, please re-select and try again";
	public static String SomeInstrumentCannotDeselectedForHasOrder = "Some instruments will not be deselected for existing related open orders";

	public static String showFrame = "Frame Show:";
	public static String failtedToSetLayout = "Failed to Set Layout!";
	public static String ChangePasswordForm2ChangePasswordPanel = "Change Password";
	public static String ChangePasswordForm2RecoverPasswordPanel = "Recover Password";
	public static String TelephoneIdentificationCodePanel = "Telephone Identification Code";
	public static String ChangePasswordForm2SucceedRecoverPassword = "Succeed to recoverPassword setting!";
	public static String ChangePasswordForm2FailedRecoverPassword = "Failed to recoverPassword setting!";
	public static String ReportError = "The PDF file is not load or failed to view report, please try again!";
	public static String TimeAdjusted = "System time has been changed!";
	public static String ConfirmSendEmail = "Are you sure to send email?";
	public static String DialogOptionYes = "Yes";
	public static String DialogOptionNo = "No";
	public static String alertDialogFormTitle = "Warning";
	public static String assignOrderFormTitle = "Assign Order";
	public static String limitOrderFormTitle = "Pending Order";
	public static String matchingOrderFormTitle = "Matching";
	public static String makeOrderFormTitle = "Trading Instruction";
	public static String instrumentSelectionFormTitle = "Instrument Selection";
	public static String accountSelectionFormTitle = "Account Selection";
	public static String candidateAccounts = "Available accounts";
	public static String faileToGetAccountForSelection = "Failed to get account info, please try again";
	public static String getAccountForSelection = "Get account settings...";
	public static String faileToSaveAccountForSelection = "Failed to save account settings, please try again";
	public static String liquidationOrderFormTitle = "Trading Instruction";
	public static String matchOrderFormTitle = "Matching";
	public static String lockAccountFormTitle = "Lock Account";
	public static String mainFormTitle = "Trading Console";
	public static String marginFormTitle = "Margin";
	public static String plsInputMarginPIN = "Please input Maring PIN";
	public static String changeMarginPIN="Change Margin PIN";
	public static String ChuRuJin = "Margin";
	public static String messageContentFormTitle = "Message";
	public static String messageFormTitle = "Message";
	public static String aboutFormTitle = "About";
	public static String multiDQOrderFormTitle = "Trading Instruction";
	public static String newsFormTitle = "News";
	public static String notifyFormTitle = "Notify";
	public static String optionFormTitle = "Option";
	public static String spotTradeOrderFormTitle = "Instant Order";
	public static String verificationOrderFormTitle = "Verification";
	public static String notify = "Notify";
	public static String notify2 = "Notify";
	public static String optionFormProxyLabel = "Connection";
	public static String serverSetting = "Server Setting";
	public static String host = "Host";
	public static String autoUpdateListenPort = "Updater Port";
	public static String serviceHost1 = "Server 1";
	public static String serviceHost2 = "Server 2";
	public static String serviceHost3 = "Server 3";
	public static String serviceHost4 = "Server 4";
	public static String serviceHost5 = "Server 5";
	public static String serviceHost6 = "Server 6";
	public static String userDefineHost = "User Define";
	public static String userDefineHostFailed = "Failed to input user define host.";
	public static String autoUpdateListenPortFailed = "Failed to input updater port.";
	public static String updateServiceFailed = "Failed to update server info.";
	public static String serverChangeNotify = "The function will be effected for next login!";
	public static String optionFormFontLabel = "Font";
	public static String optionFormLanguageLabel = "Language";
	public static String optionFormUseUnitGridLabel = "Use Trading Panel Grid";
	public static String optionFormuseUnitGridPromptLabel = "The functions will be effected for next login!";
	public static String optionFormGridsPerUnitRowLabel = "Grids/Row";
	public static String emptyAllLogFilesWhenEnterSystem = "";
	public static String optionFormTraceLabel = "Trace";
	public static String optionFormTraceNormal = "Normal";
	public static String optionFormTraceSpecial = "Special";
	public static String optionFormMinTimeToken = "MinTimeTaken";
	public static String optionFormTraceClear = "Clear History";
	public static String optionFormTraceOpenLog = "Open log";
	public static String optionFormProxyNotify = "Please set HTTP proxy server and port using Internet Explorer.";
	public static String optionFormProxyHost = "HTTP";
	public static String optionFormProxyPort = ":";
	public static String optionFormUserId = "Username:";
	public static String optionFormPassword = "Password:";
	public static String optionFormEnableDocking = "Enable docking";

	public static String LoggedInAt = "logged in at";
	public static String LoggedOutAt = "logged out at";
	public static String SystemDisconnected = "System Disconnected";
	public static String InitializeParameterError = "The parameter has been invalided, please contact our Customer Service for details!";

	public static String LMTSTOPSetPriceValidation1 = "The Lmt price should be above the market price,please modify.";
	public static String LMTSTOPSetPriceValidation2 = "The Lmt price should be below the market price,please modify.";
	public static String LMTSTOPSetPriceValidation3 = "The Stop price should be above the market price,please modify.";
	public static String LMTSTOPSetPriceValidation4 = "The Stop price should be below the market price,please modify.";

	public static String MakeOrderCurrent = "Current";
	public static String MakeOrderLast1 = "Last 1";
	public static String MakeOrderLast2 = "Last 2";
	public static String MakeOrderLast3 = "Last 3";
	public static String MakeOrderSelectBuySell = "Please select buy / sell";
	public static String MakeOrderSelectOrderType = "Please select order type";
	public static String MakeOrderSelectAccount = "Please select account";

	public static String setPriceFarFromMaketPrice = "Set price({0}) is far from market pirce({1}), are you sure to continue?";

	public static String DeleteCaption = "Delete";
	public static String ModifyCaption = "Modify";
	public static String AddCaption = "Add";
	public static String ExitCaption = "Exit";
	public static String PreviousCaption = "Previous";
	public static String NextCaption = "Next";

	public static String CannotGetBankAccounts = "Can't get bank accounts for {0}";

	public static String Others = "Others";

	public static String ExitSystemPleaseWait = "Exit system...";
	public static String Instrument = "Item";
	public static String AssignOrderCode = "Assign Order";
	public static String NotAllowOptionItem = "The windows form is not allow setting currently!";
	public static String NotAllowFontOptionItem = "The font has been invalided!";
	public static String ReportViewCaption = "View";
	public static String VerifyInstruction = "Please verify your instructon/s:";
	public static String Lots = "Lots";
	public static String At = "AT";
	public static String TrigerAt = "Triger at";
	public static String LimitWith = "Limit with";
	public static String ClosesOut = "Closes Out";
	public static String WaitingForAcceptance = "Waiting for acceptance";
	public static String ForLiquidation = "For Liquidation:";
	public static String ChangePasswordMessage1 = "Your password has been successfuly changed!";
	public static String ChangePasswordMessage2 = "Failed to change password!";
	public static String ChangePasswordMessage3 = "Please enter your new/comfirm password again!";
	public static String ChangePasswordMessage4 = "Submission failed, please try again!";

	public static String PasswordErrorPlsTryAgain = "The password is wrong, please try again";

	public static String PlaceLimitOrder = "Place limit order";
	public static String PlaceStopOrder = "Place stop order";

	public static String CantGetPayNoFor99Bill = "Can't get a pay No. for 99Bill";

	public static String MakeOrderMessage = "";
	public static String MakeOrderMessageSPT = "Explanatory/Instruction Notes:\n "
		+ "1. You have selected \"SPOT TRADE\" for the type of your order instruction, if you have selected the wrong order type, please point your mouse back to the \"Order Type\" drop down box and re-enter your selection for Order Type.\n "
		+ "2. Next point you mouse to the \"BUY\" or \"SELL\" drop down box to select \"BUY\", or \"SELL\"\n "
		+ "3. Next point your mouse to the drop down box denoted by \"Lot\" (the number of standard contracts denominated in integral numbers) to enter the number of lots you want to enter into transaction.\n "
		+ "4. Next you will observe that the \"PLACE ORDER\" button is activated, meaning that this Trader Console is ready to execute your order instruction in accordance with the instructions you have give in the steps 1, 2 and 3 above. Now read the latest FX currency pair buy/sell rates quoted in the box marked under \"Current Bid/Ask\" located on the top left hand corner of this pop-up window \u2013 this is the latest quoted price on the FX currency pair good for trading on SPOT TRADE. On hitting once the \"PLACE ORDER\" button, you will have \u2013 if it is an order to BUY \u2013 bought the contracts at the rate quoted as the asking price, or you will have \u2013 if it is an order to SELL \u2013 sold the contracts at the rate quoted as the bidding price, as they would appear within the \"Current Bid/Ask\" box.\n "
		+ "5. Hit the PLACE ORDER button when the bid/ask quote provided in the \"Current Bid/Ask\" box is made at a level that suits you to execute the transaction as you have intended.";
	public static String MakeOrderMessageLMT = "Explanatory/Instruction Notes:\n "
		+ "1. You have selected \"LIMIT\" for the type of your order instruction, if you have selected the wrong order type, please point your mouse back to the \"Order Type\" drop down box and re-enter your selection for Order Type\n "
		+ "2. Next point you mouse to the \"BUY\" or \"SELL\" drop down box to select \"BUY\", or \"SELL\"\n "
		+ "3. Next point your mouse to the entry box denoted by \"Lot\" (the number of standard contracts denominated in integral numbers) to enter the number of lots you want to enter into transaction.\n "
		+ "4. Next point your mouse to the entry box denoted by \"Limit/Stop\" to enter the price level at which you intend to BUY or SELL. Please note that there is a price zone at which you can place your limit BUY orders (restricted to the zone starting from 30 s below the Bid price last quoted on the \"Current Bid/Ask\" price) and limit SELL orders (restricted to the zone starting from 30 pips above the Ask price last quoted on the \"Current Bid/Ask\" price).\n "
		+ "5. Next you will observe that the \"PLACE ORDER\" button is activated, meaning that this Trader Console is ready to accept your order instruction in accordance with the instructions you have give in the steps 1, 2, 3 and 4 above.\n "
		+ "6. Hit the PLACE ORDER button and your LIMIT order will be activated at a level for execution that suits you to execute the transaction as you have intended.";
	public static String MakeOrderMessageSTP = "Explanatory/Instruction Notes:\n "
		+ "1. You have selected \"STOP\" for the type of your order instruction, if you have selected the wrong order type, please point your mouse back to the \"Order Type\" drop down box and re-enter your selection for Order Type.\n "
		+ "2. Next point you mouse to the \"BUY\" or \"SELL\" drop down box to select \"BUY\", or \"SELL\"\n "
		+ "3. Next point your mouse to the entry box denoted by \"Lot\" (the number of standard contracts denominated in integral numbers) to enter the number of lots you want to enter into transaction.\n "
		+ "4. Next point your mouse to the entry box denoted by \"Limit/Stop\" to enter the price level at which you intend to BUY or SELL (as a STOP ORDER). Please note that there is a price zone at which you can place your BUY STOP orders (restricted to the zone starting from 30 pips above the Ask price last quoted on the \"Current Bid/Ask\" price) and SELL STOP orders (restricted to the zone starting from 30 pips below the Bid price last quoted on the \"Current Bid/Ask\" price).\n "
		+ "5. Next you will observe that the \"PLACE ORDER\" button is activated, meaning that this Trader Console is ready to accept your order instruction in accordance with the instructions you have give in the steps 1, 2, 3 and 4 above.\n "
		+ "6. Hit the PLACE ORDER button and your STOP order will be activated at a level for execution that suits you to execute the transaction as you have intended. Also note that a STOP order, when triggered for execution, will be executed as a MARKET ORDER by which the final execution price may result at a level determined by the next Bid/Ask price level made available immediately following the STOP trigger event. Subject to the prevalent market condition, such final execution price may be different from the price level at which your have entered in the step 4 above.";
	public static String OrderLMTlblSetPriceA2 = "Stop price";
	public static String OrderLMTlblSetPriceA3 = "Limit price";
	public static String RejectCancelLmtOrder = "Limit price is too close to the market price";
	public static String TradeConsoleCancelLMTOrderAlert1 = "The limit price is too close to the market price, cancellation is not accepted!";
	public static String LoginFailedPrompt = "Another instance is running, initialization failed!";
	public static String CloseWindow = "Close";
	public static String NoNews = "No News";
	public static String AccountExpire = "Account/s";
	public static String AccountWillExpire = "will expire the next trade day.";
	public static String AccountExpired = "expired, no trade will be allowed.";
	public static String btnDocument = "about physical delivery";

	public static String AccountMarginNotify1 = "Margin position of:";
	public static String AccountMarginNotify2 = "need/s your attention!";
	public static String InstrumentSelectOverRange = "Selected instrument/s can not more than";

	public static String EntranceForInput = "The Trading quantity placed for the accounts:";
	public static String EntranceForInput2 = "have not met with the minimum requirement. Please try again!";

	public static String SetLMTStopCaption = "Lmt/Stop";
	public static String GoodTillMonthDayOrder = "Day Order";
	public static String GoodTillMonthSession = "Session";
	public static String GoodTillMonthGTM = "GTM";
	public static String GoodTillMonthGTF = "GTF";
	public static String GoodTillDate = "Date";
	public static String ImmediateOrCancel = "IOC";
	public static String FillOrKill = "FillOrKill";
	public static String FillAndKill = "FillAndKill";
	public static String GoodTillCancel = "GTC";
	public static String PLCaption = "You may specify an amount (+/-) to limit the profit/loss arising from this open order ";
	public static String ExistsMessage = "You have a unread message!";

	public static String btnGetValueFromGridCaption = "Get width";
	public static String lblFontGridCaption = "Selection";
	public static String lblFontNameCaption = "Font";
	public static String lblFontSizeCaption = "Size";
	public static String lblFomtColKeyCaption = "Column";
	public static String lblFontColWidthCaption = "Col Width";
	public static String lblFontColHiddenCaption = "Hide";
	public static String lblIsCalculateFloatCaption = "Real Time Floating";
	public static String lblLayoutCaption = "Save Current Layout";
	public static String lblRowHeightCaption = "Height";

	public static String SettingAccountGrid = "Account Status";
	public static String SettingInstrumentGrid = "Trading Panel";
	public static String SettingOpenOrderGrid = "Open Order List";
	public static String SettingOrderGrid = "Working Order List";
	public static String btnOKCaption = "Apply";
	public static String btnCancelCaption = "Exit";
	public static String MakeOrderNotAllowLiqudate = "The order is not allow liqudate.";
	public static String DisallowTradePrompt = "You are not granted with appropriate right to trade, please contact our customer service.";

	public static String ChartMINUTE1 = "1 minute";
	public static String ChartMINUTE5 = "5 minutes";
	public static String ChartHOUR1 = "Hourly";
	public static String ChartDAY1 = "Daily";
	public static String ChartWEEK1 = "Weekly";
	public static String ChartMONTH1 = "Monthly";

	public static String ChartBlackBlue = "BlackBlue";
	public static String ChartGreenRed = "GreenRed";
	public static String ChartBlackWhite = "BlackWhite";
	public static String ChartGreenWhite = "GreenWhite";
	public static String ChartCyanGreen = "CyanGreen";
	public static String ChartOceanBlue = "OceanBlue";
	public static String ChartRedBlack = "RedBlack";
	public static String ChartRedWhite = "RedWhite";
	public static String ChartWhite = "White";

	public static String ChartCandle = "Candle";
	public static String ChartLine = "Line";
	public static String ChartCloseHiLo = "Close HiLo";
	public static String ChartOpenCloseHiLo = "Open Close HiLo";

	public static String ChartGetFormula = "Main";
	public static String ChartOverlay = "Overlay";
	public static String ChartGo = "Go";

	public static String ChartFormulaRemove = "Remove";
	public static String ChartFormulaGo = "OK";
	public static String ChartFormulaRefresh = "Refresh";
	public static String ChartFormulaParameter = "Parameter";
	public static String ChartFormulaParameterName = "Name";
	public static String ChartFormulaParameterValue = "Value";

	public static String OperateSingleDQOrderMethodTitle0 = "If you choose to";
	public static String OperateSingleDQOrderMethodTitle1 = "Order details as follows:";
	public static String OperateSingleDQOrderMethodTitle2 = "at";
	public static String OperateSingleDQOrderMethodTitle3 = "To close out";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert4 = "The total quantity is more than the quantity of the DQ order ";
	public static String OrderMultiDQbtnDeal = "Deal";
	public static String OrderMultiDQbtnClear = "Clear";
	public static String OperateOrderMethod1PageSubmitAlert0 = "Please input lot!";
	public static String OperateOrderMethod1PageSubmitAlert1 = "Please wait for price!";
	public static String OperateOrderMethod1PageSubmitAlert2 = "Please press deal!";
	public static String ChangePasswordlblChangePasswordPromptTitle = "PASSWORDS CHANGE";
	public static String ChangePasswordlblCurrentPassword = "Current";
	public static String ChangePasswordlblNewPassword = "New";
	public static String ChangePasswordlblConfirmPassword = "Confirm";
	public static String ChangePasswordlblPrompt = "The new passwords should contain combination of 8 - 16 digits and/or letters.";
	public static String ChatArrivePrompt = "You have a message, please check!";
	public static String QuoteDelayTimeout = "Quote time out!";
	public static String AdditionalClientPrompt = "Owner Registration";
	public static String AgentPrompt = "Agent Registration";
	public static String CallMarginExtensionPrompt = "Margin Call Extension";
	public static String PaymentInstructionPrompt = "Payment Instruction";
	public static String BankAccount = "Bank account";
	public static String PICashPrompt = "PICash";
	public static String PIInterACTransferPrompt = "PIInterACTransfer";

	public static String ChangeLeverageFailed = "Failed to change leverage";

	public static String FundTransferPrompt = "Fund Transfer";
	public static String MarginPageOnloadDocument = "Document";
	public static String MarginPageOnloadAccount = "Account";
	public static String MarginPageOnloadNext = "Next";
	public static String MarginPageBtnNextPrompt = "Please select a account!";

	public static String WebServiceAdditionalClientMessage0 = "Sent failed, please try again.";
	public static String WebServiceAdditionalClientMessage1 = "Application has been submitted successfully, the reference No. is: ";
	public static String AdditionalClientPageSubmitAlert0 = "Sent failed, please try again.";
	public static String WebServiceAgentMessage0 = "Sent failed, please try again.";
	public static String WebServiceAgentMessage1 = "Application has been submitted successfully.";
	public static String AgentPageSubmitAlert0 = "Sent failed, please try again.";

	public static String QueryOrederFailedPleaseRetry = "Query order failed, please try later";

	public static String CallMarginExtensionPageSubmitAlert0 = "Sent failed, please try again.";
	public static String WebServiceCallMarginExtensionMessage0 = "Failed to call margin extension.";
	public static String WebServiceCallMarginExtensionMessage1 = "Application has been submitted successfully.";
	public static String WebServiceCallMarginExtensionAlert0 = "An error occurred when calling margin extension.";
	public static String FundTransferPageSubmitAlert0 = "Sent failed, please try again.";
	public static String WebServiceFundTransferMessage0 = "Failed to Fund Transfer.";
	public static String WebServiceFundTransferMessage1 = "Application has been submitted successfully.";
	public static String WebServiceFundTransferAlert0 = "An error occurred when Fund Transfer.";
	public static String PaymentInstructionPageSubmitAlert0 = "Sent failed, please try again.";
	public static String PaymentInstructionPageSubmitAlert1 = "";
	public static String UsableMarginNotSufficient = "Usable Margin is NOT Sufficient";
	public static String WebServicePaymentInstructionMessage0 = "Failed to Payment Instruction.";
	public static String WebServicePaymentInstructionMessage1 = "Application has been submitted successfully.";
	public static String WebServicePaymentInstructionAlert0 = "An error occurred when Payment Instruction.";
	public static String MenupageMarginAlert0 = "Failed to initialize during logging, please try again!";
	public static String RealTimeAsk = "-Ask";
	public static String RealTimeBid = "-Bid";
	public static String InstrumentViewPrompt = "Trading Panel List";
	public static String InstrumentView2Prompt = "Trading Panel Grid";
	public static String AccountStatusPrompt = "Account status";
	public static String SummaryPrompt = "Position Summary";
	public static String LogPrompt = "Log";
	public static String SingleDQBuyPrompt = "BUYING";
	public static String SingleDQSellPrompt = "SELLING";
	public static String ReportTypeStatement = "Statement";
	public static String ReportTypeLedger = "Ledger";
	public static String ReportTypeAccountSummary = "Account Summary";
	public static String Reports = "Reports";
	public static String ReportType = "Report Type";
	public static String ReportAccountCode = "Account";
	public static String OldTelephoneIdentificationCode = "Old Code";
	public static String NewTelephoneIdentificationCode = "New Code";
	public static String ReconfirmTelephoneIdentificationCode= "Reconfirm Code";
	public static String OldTelephoneIdentificationCodeIsEmpty="Old code is empty";
	public static String NewTelephoneIdentificationCodeIsEmpty="New code is empty";
	public static String ReconfirmTelephoneIdentificationCodeIsEmpty="Reconfirm code is empty";
	public static String TelephoneIdentificationCodeIsDifferent="New code is different from reconfirm code";
	public static String ModifyTelephoneIdentificationCodeSussessed="Modify telephone identification code successed";
	public static String ModifyTelephoneIdentificationCodeFailed="Modify telephone identification code failed";
	public static String ReportDate1InnerTextForLedger = "From";
	public static String ReportDate2InnerTextForLedger = "To";
	public static String ReportDate1InnerTextForStatement = "DateTime";
	public static String FailedToLoadReport = "Failed to load report!";
	public static String GettingQuotesPrompt = " Getting quote, please wait...";
	public static String QuotePriceArrivedPrompt = "Please proceed with your instruction!";
	public static String Buy = "B";
	public static String Sell = "S";
	public static String LongBuy = "Buy";
	public static String LongSell = "Sell";
	public static String Open = "N";
	public static String Close = "C";
	public static String TimeRemainPrompt = "Time Remain: ";
	public static String DQQuoteLotPrompt = "Lots:";
	public static String DQPrompt = "DQ";
	public static String LMTPrompt = "LMT";
	public static String PriceForIsNotAcceptable = "'{0}' for '{1}' is not acceptable, please modify it and try again";
	public static String STPPrompt = "STP";
	public static String MKTPrompt = "MKT";
	public static String MOOPrompt = "MOO";
	public static String MOCPrompt = "MOC";
	public static String OCOPrompt = "OCO";
	public static String MatchPrompt = "MTH";
	public static String IfDonePrompt = "If Done";
	public static String SPTPrompt = "SPT";
	public static String MultipleClosePrompt = "MPC";
	public static String MarketToLimit = "MarketToLimit";
	public static String StopLimit = "StopLimit";
	public static String FAK_Market = "FAK_Market";
	public static String SYSPrompt = "SYS";
	public static String TradeOptionPromptStop = "STOP";
	public static String TradeOptionPromptBetter = "BETTER";
	public static String PhasePlacedPlacingPrompt = "Placing";
	public static String PhasePlacedPlacingPromptForDoneOrder = "Wait for activate";
	public static String PhasePlacedUnconfirmedPrompt = "Placed and wait";
	public static String PhasePlacedUnconfirmedPrompt2 = "In process";
	public static String PhaseCancelledPromptForRejectDQByDealer = "Price change cx";
	public static String CancellingForModification = "Cancelling";
	public static String PhaseCancelledPrompt = "Cancelled";
	public static String PhaseExecutedPrompt = "Confirmed";
	public static String PhaseCompletedPrompt = "Completed";
	public static String PhaseDeletedPrompt = "Deleted";
	public static String InstructionPrompt = "Instruction";
	public static String ModifyInstructionPrompt = "Modify instruction";
	public static String ExpireOrderPrompt = "Order time expired";
	public static String AccountNodeFontBold = "true";
	public static String AccountNodeFontName = "SansSerif";
	public static String AccountNodeFontSize = "10";
	public static String NewOrderAcceptedHedging = "The new order is only accepted for hedging only.";
	public static String NewOrderAcceptedHedging2 = "MOO/MOC order is only accepted for liquidation only.";
	public static String AlertLevelPrompt0 = "Memo";
	public static String AlertLevelPrompt1 =
		"Account margin has not been sufficient, you may need to contact our Customer Service for account operation!";
	public static String AlertLevelPrompt2 = "Account is in critical situation, your immediate attention is much needed.";
	public static String AlertLevelPrompt3 = "Please ignore this message if appropriate action has been taken to avoid further loses.";
	public static String RiskMonitorDelete = "The system has deleted the order. If you find query, please contact our Customer Service!";
	public static String AccountResetFailed =
		"The system has failed to prepare the account for coming trade day. Please contact the system administrator";
	public static String DealerCanceled = "The order has been cancelled by the Trading Desk!";
	public static String RejectDQByDealer = "Price time out, please try again!";
	public static String NecessaryIsNotWithinThreshold = "The trading quantity exceeds the limit allowed for the account, order was not accepted!";
	public static String MarginIsNotEnough = "Account's usable margin is not sufficient, order was not accepted!";
	public static String AccountIsNotTrading = "Account allowed for trading is not available, please contact our Customer Service for details!";
	public static String InstrumentIsNotAccepting = "The instrument is not available for trading, please contact our Customer Service for details!";
	public static String TimingIsNotAcceptable = "The order is out of the trading time accepted, order was cancelled!";
	public static String OrderTypeIsNotAcceptable = "The select order type is not available for the instrument!";
	public static String HasNoAccountsLocked =
		"The agent account has not been granted with control over the other accounts, please select appropriate accounts and try later!";
	public static String IsLockedByAgent = "The account has been occupied by its Agent!";
	public static String IsNotLockedByAgent = "The trading account has not been controlled by Agent!";
	public static String InvalidPrice = "Price is not in a valid format, please try later!";

	public static String PriceIsOutOfDate = "Pirce is out of date, please try again!";
	public static String TraderVersionError = "Please install the new version and try again!";

	public static String LastDay = "Last Days";
	public static String Query = "Qurey";
	public static String NotConfirmedPendingList = "";

	public static String LossExecutedOrderInOco = "The other end of the OCO has not been found!";
	public static String ExceedOpenLotBalance = "Close order quantity exceeds the quantity of the open order";
	public static String OneCancelOther = "Order cancelled as part of the OCO order is confirmed";
	public static String OneCancelOtherPrompt = "Order cancelled as part of the OCO order is confirmed.";
	public static String HasUnassignedOvernightOrders = "There are orders in the agent account, please assign the order to appropriate accounts!";
	public static String DbOperationFailed = "Unexpected error has occured when locating the account's information";
	public static String TransactionAlreadyExists = "Unexpected error has occured when locating the order";
	public static String HasNoOrders = "The order does not exist";
	public static String InvalidRelation = "Unexpected error has occured when locating the corresponding open order";
	public static String InvalidLotBalance = "Unexpected error has occured with the open trading quantity";
	public static String ExceedAssigningLotBalance = "The order has exceeded the quantity of the assigning order";
	public static String OrderLotExceedMaxLot = "The order has exceeded the permitted trading quantity of the account";
	public static String OpenOrderNotExists = "The corresponding open order does not exist, order cancelled.";
	public static String AssigningOrderNotExists = "The assigning order does not exist any more, order cancelled";
	public static String TransactionNotExists = "Unexpected error for the transaction has occured";
	public static String TransactionCannotBeCanceled = "The order cannot be cancelled";
	public static String TransactionCannotBeExecuted = "Unexpected error occurred, failed to executed the order";
	public static String OrderCannotBeDeleted = "The order cannot be cancelled";
	public static String IsNotAccountOwner = "User is not authorized to trade for the account,";
	public static String InvalidOrderRelation = "The system fails to execute the order due to the missing of open order";
	public static String TradePolicyIsNotActive =
		"The trading policy for the account has been inactivated, please contact our Customer Service for details!";
	public static String SetPriceTooCloseToMarket = "The order was rejected because being too close to the market!";
	public static String SetPriceTooCloseOrTooFarToMarket = "The order was rejected because the price was either too far or too close  to the market!";
	public static String HasNoQuotationExists = "There is no price available for the execution, please try again";
	public static String AccountIsInAlerting = "The Account is in margin call position, the order was rejected";
	public static String CustomerCanceled = "The order has been cancelled by the Customer";
	public static String StopAddPositionNotAllowed = "The stop order was rejected as it was not placed to minimize the position exposure.";
	public static String LmtMooMocNewPositionNotAllowed = "The instruction is allowed for liquiation only!";
	public static String TransactionCannotBeBooked = "Transaction Can not Be Booked!";
	public static String OnlySptMktIsAllowedForPreCheck = "Only Spt Mkt Is Allowed For PreCheck";
	public static String InvalidTransactionPhase = "Invalid Transaction Phase";
	public static String ExecuteTimeMustBeInTradingTime = "Order's executing time must be in the trading time.";
	public static String DatabaseDataIntegralityViolated = "The data of the datebase is not integral.";
	public static String TransactionExpired = "Order has expired.";
	public static String ExceedMaxOpenLot = "The placed order exceeds permitted trading quantity for open position.";
	public static String ReplacedWithMaxLot = "Order replaced for exceed max lot";
	public static String FillOnMarketCloseNotAllowed = "The order didn't allowed to deal on market closed time.";
	public static String FailedToCancel = "Fail to cancel the selected order, please try again!";
	public static String InstrumentSelectlblSelectInstrument = "Please select items to display:";
	public static String InstrumentSelectbtnUp = "Move Up";
	public static String InstrumentSelectbtnDown = "Move Down";
	public static String InstrumentSelectbtnSynchronize = "Synchronize";
	public static String InstrumentSelectbtnOk = "O K ";
	public static String InstrumentSelectbtnExit = "Exit";
	public static String LockAccountlblLockAccountPrompt = "select the account for agent operation:";
	public static String LockAccountbtnSelectAll = "Select All";
	public static String LockAccountbtnClearAll = "Clear All";
	public static String LockAccountbtnSubmit = "Submit";

	public static String CurrentLeverage = "Current Leverage";
	public static String NewLeverage = "New Leverage";

	public static String MenuimgConnect = "Connect/Disconnect";
	public static String MenuimgInstrumentSelect = "Selection of trading instrument";
	public static String MenuimgOpenChangePassword = "Change user passwords";
	public static String MenuimgShowAnalyticChart = "Real time/historical chart";
	public static String MenuimgOpenChat = "Message";
	public static String MenuimgOpenReportSelect = "Report";
	public static String MenuimgLockAccount = "Lock account";
	public static String MenuimgAccountActive = "Active Account";
	public static String MenuimgMargin = "Margin";
	public static String MenuimgClearAll = "Clear all of cancelled order";
	public static String MenuimgDownload = "Document to download";
	public static String MenuimgHelp = "online help";
	public static String MenuimgReset = "Refresh";
	public static String MenuimgOpenLogOut = "Exit the trading system";
	public static String resetLayoutButtonCaption = "Reset Layout";
	public static String debugFormTitle = "Debug";
	public static String MenuimgOption = "Option";
	public static String MenuimgNews = "News";
	public static String MenuimgChatRoom = "Chat Room";

	public static String MenuimgConnectText = "Connect/Disconnect";
	public static String MenuimgInstrumentSelectText = "Selection of trading instrument";
	public static String MenuimgOpenChangePasswordText = "Change user passwords";
	public static String MenuimgShowAnalyticChartText = "Real time/historical chart";
	public static String MenuimgOpenChatText = "Message";
	public static String MenuimgOpenReportSelectText = "Report";
	public static String MenuimgLockAccountText = "Lock account";
	public static String MenuimgAccountActiveText = "Active Account";
	public static String MenuimgMarginText = "Margin";
	public static String ChangeLeverageText = "Change leverage";
	public static String MenuimgClearAllText = "Clear all of cancelled order";
	public static String MenuimgDownloadText = "Document to download";
	public static String MenuimgHelpText = "online help";
	public static String MenuimgResetText = "Refresh";
	public static String debugFormTitleText = "Debug";
	public static String MenuimgOptionText = "Option";
	public static String MenuimgNewsText = "News";
	public static String MenuimgChatRoomText = "Chat Room";
	public static String MenuimgOpenLogOutText = "Exit the trading system";
	public static String resetLayoutButtonText = "Reset Layout";

	public static String ManuBarTitle = "Menu Bar";
	public static String ManuBarToolTipText = "Menu Bar";
	public static String FileMenuText = "File";
	public static String FileMenuToolTipText = "File";
	public static String OperateMenuText = "Operate";
	public static String OperateMenuToolTipText = "Operate";
	public static String ViewMenuText = "View";
	public static String ViewMenuToolTipText = "View";
	public static String ToolsMenuText = "Tools";
	public static String ToolsMenuToolTipText = "Tools";
	public static String WindowMenuText = "Window";
	public static String WindowMenuToolTipText = "Window";
	public static String HelpMenuText = "Help";
	public static String HelpMenuToolTipText = "Help";
	public static String LookAndFeelMenuItemText = "Color Scheme";
	public static String LookAndFeelMenuItemToolTipText = "Color Scheme";
	public static String LookAndFeelMenuItem1Text = "Color Scheme 1";
	public static String LookAndFeelMenuItem1ToolTipText = "Color Scheme 1";
	public static String LookAndFeelMenuItem2Text = "Color Scheme 2";
	public static String LookAndFeelMenuItem2ToolTipText = "Color Scheme 2";
	public static String LookAndFeelMenuItem3Text = "Color Scheme 3";
	public static String LookAndFeelMenuItem3ToolTipText = "Color Scheme 3";
	public static String LookAndFeelMenuItem4Text = "Color Scheme 4";
	public static String LookAndFeelMenuItem4ToolTipText = "Color Scheme 4";
	public static String LookAndFeelMenuItem5Text = "Color Scheme 5";
	public static String LookAndFeelMenuItem5ToolTipText = "Color Scheme 5";

	public static String PayId = "PayId";
	public static String Amount = "Amount";
	public static String Account = "Account";
	public static String Submit = "Submit";
	public static String RMB = "RMB";
	public static String Transfer99Bill="Deposit";
	public static String MinDeposit = "Min:";

	public static String Currency = "Currency";
	public static String ExchangeRate = "Exchange Rate";
	public static String ExchangeCurrency = "Target Currency";
	public static String ExchangeAmount = "Exchange Amount";
	public static String Bank = "Bank";
	public static String Country = "Country";

	public static String CloseLot = "Close lot";
	public static String OpenContractlblQuantityA = "Unit";
	public static String OpenContractbtnLiquidation = "Liquidation";
	public static String OpenContractlblExecutePriceA = "Price";
	public static String OpenContractlblAccountCodeA = "Account No.";
	public static String OpenContractlblContractSizeA = "Size";
	public static String OpenContractlblExecutedTimeA = "Executed Time";
	public static String OpenContractlblCurrencyA = "Currency";
	public static String OpenContractlblReferencePriceA = "Ref Price";
	public static String OpenContractlblTradePLFloatA = "Floating P/L";
	public static String OpenContractlblInterestPLFloatA = "Float Interest";
	public static String OpenContractlblStoragePLFloatA = "Float Storage";
	public static String OpenContractbtnAssignOrder = "Assign";
	public static String OpenContractlblIsBuyA = "Buy/Sell";
	public static String OpenContractbtnExit = "eXit";
	public static String OpenContractlblOrderContract = "OPEN CONTRACT";
	public static String OrderAssignlblOrderCodeA = "Order Code";
	public static String OrderAssignlblExecuteTimeA = "Execute Time";
	public static String OrderAssignlblIsBuyA = "B/S";
	public static String OrderAssignlblPriceA = "Price";
	public static String OrderAssignlblLotBalanceA = "Lot Bal.";
	public static String OrderAssignlblBalanceA = "Balance";
	public static String OrderAssignlblEquityA = "Equity";
	public static String OrderAssignlblACTotalLotsA = "Total Lot";
	public static String OrderAssignlblLiqLotsA = "Close";
	public static String OrderAssignbtnReset = "Reset";
	public static String OrderAssignbtnSubmit = "Submit (F8)";
	public static String OrderAssignbtnExit = "eXit";
	public static String OrderLiquidationlblPriceA = "Price";
	public static String OrderLiquidationlblAccountCodeA = "Account No.";
	public static String OrderLiquidationlblBalanceA = "Balance";
	public static String OrderLiquidationlblEquityA = "Equity";
	public static String OrderLiquidationlblACTotalLotsA = "Quoted Lot";
	public static String OrderLiquidationbtnSubmit = "Submit (F8)";
	public static String CloseAll = "Close All";
	public static String MultipleClose = "Multiple Close";
	public static String OrderLiquidationbtnExit = "eXit";
	public static String OrderLMTlblOrderTypeA = "Type";
	public static String OrderLMTlblAccountCodeA = "Account";
	public static String OrderLMTlblIsBuyA = "B/S";
	public static String OrderLMTlblSetPriceA = "Price";
	public static String OrderLMTlblLot = "Lot";
	public static String OrderLMTlblBalanceA = "Balance";
	public static String OrderLMTlblEquityA = "Equity";
	public static String OrderLMTbtnSetOCO = "O C O";
	public static String OrderLMTbtnReset = " Reset ";
	public static String OrderLMTbtnSubmit = "Submit (F8)";
	public static String OrderLMTbtnExit = "eXit";
	public static String OrderMultiDQlblPriceA = "Price";
	public static String OrderMultiDQlblAccountCodeA = "Account No.";
	public static String OrderMultiDQlblBalanceA = "Balance";
	public static String OrderMultiDQlblEquityA = "Equity";
	public static String OrderMultiDQlblACTotalLotsA = "Total Lot";
	public static String OrderMultiDQlblDQQuoteLotA = "Quoted Lot";
	public static String OrderMultiDQbtnReset = " Reset ";
	public static String OrderMultiDQbtnSubmit = "Submit (F8)";
	public static String OrderMultiDQbtnExit = "eXit";
	public static String OrderPlacementbtnConfirm = "Confirm (F8)";
	public static String OrderPlacementbtnCancel = "Cancel";
	public static String OrderPlacementlblOpenOrderA = "Open Order";
	public static String OrderSingleDQlblAccountCodeA = "Account";
	public static String OrderSingleDQlblLot = "Lot";
	public static String OrderSingleDQlblDQMaxMove = "Slide Pips";
	public static String SaveDQlblDQMaxMoveAsDefault = "Save";
	public static String OrderSingleDQbtnBuy = "BUY";
	public static String OrderSingleDQbtnSell = "SELL";
	public static String OrderSingleDQbtnReset = " Reset ";
	public static String OrderSingleDQbtnExit = "eXit";
	public static String UnconfirmedInstructionlblUnconfirmedInstruction = "UNCONFIRMED INSTRUCTION";
	public static String UnconfirmedInstructionlblExecutePriceA = "Price";
	public static String UnconfirmedInstructionlblIsBuyA = "Buy/Sell";
	public static String UnconfirmedInstructionlblQuantityA = "Quantity";
	public static String UnconfirmedInstructionlblAccountCodeA = "Account No.";
	public static String UnconfirmedInstructionlblOrderTypeA = "Type";
	public static String UnconfirmedInstructionlblTradeOptionA = "Option";
	public static String UnconfirmedInstructionlblSubmitTimeA = "Sent Time";
	public static String UnconfirmedInstructionlblEndTimeA = "Expire Time";
	public static String UnconfirmedInstructionlblEndTimeA2 = "Execute Time";
	public static String UnconfirmedInstructionlblCommissionSumA = "Fee";
	public static String UnconfirmedInstructionlblLevySumA = "Levy";
	public static String UnconfirmedInstructionlblTradePLA = "P/L";
	public static String UnconfirmedInstructionlblInterestPLA = "Interest";
	public static String UnconfirmedInstructionlblStoragePLA = "Storage";
	public static String UnconfirmedInstructionlblPeerOrderCodesA = "Open Positions";
	public static String UnconfirmedInstructionlblMessageA = "Remarks";
	public static String UnconfirmedInstructionbtnCancellation = "Cancel";
	public static String UnconfirmedInstructionbtnClearFromList = "Clear";
	public static String UnconfirmedInstructionbtnModify = "Modify";
	public static String UnconfirmedInstructionbtnExit = "Exit";
	public static String UnconfirmedInstructionModifyOrder = "Modify order price";
	public static String UnconfirmedInstructionNewPrice = "New price";
	public static String UnconfirmedInstructionModifyPriceWarning = "All related orders, if any, will be canceled";

	public static String BankName = "Bank name";
	public static String Province = "Province";
	public static String City = "City";
	public static String SwiftCode = "SwiftCode";
	public static String BankAddress = "Bank address";
	public static String BankAccountProp = "Prop";
	public static String BankAccountOpener = "Opener";
	public static String BankAccountType = "Type";
	public static String BankAccountNo = "Account No";
	public static String BankAccountCurrency = "Currency";
	public static String BankAccountIdType = "Id type";
	public static String BankAccountId = "Id";

	public static String Confirm = "Confirm";
	public static String IsConfirmed = " is confirmed";
	public static String IsRejected = " is rejected";
	public static String MenuPageLockAccountAlert0 = "Agent authority is not granted!";
	public static String MenuPageLockAccountAlert1 = "Failed to operate the selected account!";
	public static String MenupageOpenReportSelectAlert0 = "Failed to initialize during logging, please try again!";
	public static String MenupageInstrumentSelectAlert0 = "Failed to operate selection of trading instruments!";
	public static String OrderAssignPageFillAccount0 = "The account is not available at this moment!";
	public static String OrderLiquidationPageFillAccountAlert0 = "The account is not available at this moment!";
	public static String OrderLMTPagetextLot_OnblurAlert0 = "Exceed permitted trading quantity! ";
	public static String OrderLMTPagetextLot_OnblurAlert1 = "The filled quantity for liquidation will be reseted!";
	public static String OrderLMTPageorderValidAlert0 = "Please select a type or account!";
	public static String DisposedForOpenOrderClosed = "The open order concerned has been liquidated.  The liquidation dialog therefore will be closed out.";
	public static String OrderLMTPageorderValidAlert01 = "Order type is not allowed at this time";
	public static String OrderLMTPageorderValidAlert1 = "The price is not allowed to be blank!";
	public static String OrderLMTPageorderValidAlert2 = "The set price is too close to the market, please make it";
	public static String OrderLMTPageorderValidAlert22 = "points away from the market!";
	public static String OrderLMTPageorderValidAlert3 = "The set price is too far away from the market, please modify the set price and submit again";
	public static String OrderLMTPageorderValidAlert10 = "Order type is not allowed for last price is not available at this time";
	public static String OrderLMTPageorderValidAlert11 = "Limit price must greater than stop price and stop price must greater than market price";
	public static String OrderLMTPageorderValidAlert12 = "Limit price must less than stop price and stop price must less than market price";
	public static String OutOfAcceptDQVariation = "The set price is out of accept variation, please modify the set price and submit again";
	public static String PriceIsDisabled = "The insturment is suspended for trading for the time being, please try later";
	public static String PriceChangedSincePlace = "Price changed, order canceled!";
	public static String FailedToModifyOrder = "Failed to modify order";
	public static String TheOrderNotModified = "The order is not modified";
	public static String OpenOrderIsClosed = "Open order changed, close quantity is not valid, close order is canceled by the system";
	public static String InvalidSetPrice = "Set price is invalid, please modify the price";
	public static String OrderLMTPageorderValidAlert4 = "Stop order is accepted only for the purpose of reducing trading exposure.";
	public static String OrderLMTPageorderValidAlert5 = "Stop order is accepted only for the purpose of reducing trading exposure.";
	public static String OrderLMTPageorderValidAlert6 = "Trading quantity should be greater than 0!";
	public static String OrderLMTPageorderValidAlert7 = "Trading quantity and liquidation quantity are not the same, please re-enter!";
	public static String OrderLMTPageorderValidAlert8 = "Set price is between the current market bid/ask prices, please modify the price!";
	public static String OrderLMTPageorderValidAlertBetter = "Limit Order for new position is accepted for hedging only.";
	public static String OrderLMTPagebuttonSubmit_OnclickAlert0 = "The account is not available at this moment!";
	public static String OrderMultiDQPageFillAccountAlert0 = "The account is not available at the moment!";
	public static String OrderOCOPageorderValidAlert3 = "Trading option is not accepted!";
	public static String NewPositionIsNotAllowed = "New position is not allowed";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert0 =
		"The trading quantity exceeds the limit that is permitted by the account (";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert2 = "The total quantity is more than the quantity of the agent order (";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert3 = "The filled quantity for liquidation will be reseted!";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert5 = "The range should not greater than";
	public static String OrderOperateOrderOperateAccountGrid_ValidateEditAlert6 = "pips!";
	public static String OrderOperateOrderValidAlert0 = "The total quantity is more than the quantity permitted for the quote (";
	public static String OrderOperateOrderValidAlert1 = "The information is incomplete, please check!";
	public static String OrderOperateOutstandingOrderGridValidateEditPrompt0 = "The trade quantity is not acceptable (";
	public static String OrderOperateOutstandingOrderGridValidateEditPrompt1 =
		"Total quantity for liquidation is more that than the available quantity!";
	public static String OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert0 = "The trade quantity is not acceptable (";
	public static String OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 =
		"The total quantity is more than the quantity permitted for trading";
	public static String TradeConsoleCancelLMTOrderAlert0 = "The trading hours are not valid!";
	public static String TradeConsoleInitializeGridAlert0 = "Error: Column property is not valid, please contact the Customer Service!";
	public static String TradeConsolePromptIsMustAssignOrderAlert0 = "Orders in the agent account needed to be assigned!";
	public static String TradeConsoleAnswerAlert0 =
		"Unexpected error occurred during quotation process. If you repeatedly experience the problem, please contact our Customer Service!";
	public static String TradeConsoleGetUpdateAccountLockResultAlert0 = "Agent/Clients status have been updated";
	public static String TradeConsoleGetUpdateAccountLockResultAlert1 = "Failed to update Agent/Clients status!";
	public static String TradeConsoleCancelAlert0 =
		"An error occurred in cancelling the order. If the problem persists, please contact our Customer Service for assistance!";
	public static String TradeConsoleOrderLiquidationOperateAlert0 = "Invalid time format, please try again!";
	public static String TradeConsoleUpdateAlert0 = "Unexpected update error occurred, please try again!";
	public static String TradeConsoleIsValidOperateOrderTimePrompt0 = "Invalid time setting for the order, operation failed.";
	public static String TradeConsoleIsValidOperateOrderTimePrompt1 = "The order was not accepted because of last order acceptance time.";
	public static String TradeConsoleIsValidOperateOrderTimePrompt2 = "The order was not accepted because of order acceptance time.";
	public static String TradeConsoleIsValidOperateOrderTimePrompt3 = "The order was not accepted because of order acceptance time.";
	public static String TradeConsoleIsValidOperateOrderTimePrompt4 = "The order was not accepted because of trading hours!";
	public static String TradeConsoleIsValidOperateOrderTimePrompt5 = "The instrument is not available for trading!";
	public static String TradeConsoleGetOperateHtmlPageAlert2 = "Accounts to place spot order is not available!";
	public static String TradeConsoleGetOperateHtmlPageAlert3 = "Order Type or Open / Close time is not accepted.";
	public static String OperatingDQOrderPrompt = "You are attempting to operate an order for spot!";
	public static String OperateDQOrderPrompt = "You may not operate multiple order dialogs at the same time ";
	public static String AccountLockButtonSubmit_OnclickAlert0 = "Please select a trading account!";
	public static String AccountLockButtonSubmit_OnclickAlert1 = "All account will be unlocked!";
	public static String OrderPlacementbuttonConfirm_OnclickAlert0 = "Price time out.";
	public static String OrderPlacementbuttonConfirm_OnclickAlert2 = "The instrument is not available for trading or it is not trading at this time!";
	public static String OrderSingleDQPageFillAccountAlert0 = "The account does not exist!";
	public static String OrderSingleDQPageorderValidAlert0 = "The price is not allowed to be blank!";
	public static String OrderSingleDQPageorderValidAlert1 = "Trading quantity should be greater than 0, please re-enter!";
	public static String OrderSingleDQPagetextLot_OnblurAlert0 = "The trading quantity exceeds the limit for the instrument (";
	public static String OrderSingleDQPagetextLot_OnblurAlert2 = "The total quantity is more than the limit permitted for the quote (";
	public static String OrderSingleDQPagetextLot_OnblurAlert3 = "The filled quantity for liquidation will be reseted!";
	public static String LotIsNotValidAndWillChangeTo = "Lot {0} is not in valid format and will be changed to {1}";

	public static String ReportContainerpageOnlloadAlert0 = "An error occurred when loading the requested report, please try again!";
	public static String ReportPagebtnPrint_OnclickAlert0 = "The account is not available at the moment!";
	public static String ReportPagebtnPrint_OnclickAlert1 = "Please select an account or a date!";
	public static String ReportPagebtnPrint_OnclickAlert3 = "The account is not available at this moment!";
	public static String ReportPagebtnPrint_OnclickAlert4 = "Please select an account or a date!";
	public static String UnconfirmedInstructionPageBtnCancellation_OnclickAlert0 = "Instruction to cancel the selected order has been submitted!";
	public static String UnconfirmedInstructionPageBtnCancellation_OnclickAlert1 = "Fail to cancel the selected order, please try again!";
	public static String WebServiceGetInitDataAlert0 = "Disconnection";
	public static String WebServiceQuoteAlert0 = "There is unexpected error in getting quotation, please try again!";
	public static String WebServiceCancelLMTOrderAlert0 =
		"An error occurred in cancelling the order. If the problem persists, please contact our Customer Service for assistance!";
	public static String WebServiceUpdateAccountLockAlert0 = "An error occurred on attempt to update account status, please try again!";
	public static String WebServiceGetInstrumentForSettingAlert0 = "An error occurred on loading trading data, please try again!";
	public static String WebServiceGetInstrumentForSettingAlert1 = "No trading instrument is available,please contact our Customer Service!";
	public static String WebServiceUpdateInstrumentSettingAlert0 = "An error occurred on updating the selected instruments, please try again!";
	public static String WebServiceGetAccountsAlert0 = "An error occurred when loading account information, please try again!";
	public static String WebServiceGetInstrumentAlert0 = "An error occurred when loading data for instrument, please try again!";
	public static String WebServiceDeleteMessageAlert0 = "Failed to delete the selected message, please try again!";
	public static String WebServiceGetCurrencyRateByAccountIDAlert0 = "An error occurred when loading exchange rate information, please try again!";
	public static String WebServicePlaceMessage0 = "An error occurred when submitting trading instruction to the servers, please try again!";
	public static String WebServiceAssignMessage0 = "An error occured when assigning order to appropriate account, please try again!";
	public static String WebServiceCancelLMTOrderMessage0 =	"Cancellation request was rejected as the order was too close to the market";
	public static String WebServiceUpdateAccountLockMessage0 = "Failed to update Agent/Clients status!";
	public static String WebServiceCancelLMTOrderMessage1 = "Awaiting response from the system.";
	public static String WebServiceGetMessagesAlert0 = "An error occurred on attempt to access messages from the system, please try later!";
	public static String WebServiceGetCommandsAlert0 = "You have been signed out of the system, because:";
	public static String WebServiceGetCommandsAlert1 = "- you may sign in at other location; or";
	public static String WebServiceGetCommandsAlert2 = "- the server is refreshing its connection with you!";
	public static String DefaultPagePrompt0 = "Initializing system...";
	public static String DefaultPagePrompt1 = "Loading data...";
	public static String DefaultPagePrompt2 = "WELCOME!";
	public static String DefaultPagePrompt3 = "System Initialization...";
	public static String TimestampCheckFailed = "The system time of your PC is not properly configured,please adjust and try again!";
	public static String FailToSaveSettings = "Failed to save settings";

	public static String TimeAndSales = "Time&Sales";
	public static String BestBuy = "Best Buy";
	public static String BestSell = "Best Sell";

	//#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
	public static String QiTaZhengJian = "其他证件";
	public static String ShenFenZheng = "身份证";
	public static String HuKouBo = "户口簿";
	public static String HuZhao = "护照";
	public static String JunGuanZheng = "军官证";
	public static String ShiBingZheng = "士兵证";
	public static String GangAoTongXingZheng = "港澳居民来往内地通行证";
	public static String TaiBaoZheng = "台湾同胞来往内地通行证";
	public static String LinShiShenFenZheng = "临时身份证";
	public static String JunLiuZheng = "外国人居留证";
	public static String JingGuanZheng = "警官证";

	public static String BankCard = "Bank card";
	public static String BankBook = "Bank book";
	public static String Private = "Private";
	public static String Company = "Company";
	public static String NotSet = "Not set";

	public static String SaveBankAccountSuccessedAndApproved = "The bank account is saved";
	public static String SaveBankAccountSuccessed = "The application is submitted, waiting for approve";
	public static String FailedToSaveBankAccount = "Failed to save the bank account";
	public static String DeleteBankAccountSuccessedAndApproved = "The bank account is deleted";
	public static String DeleteBankAccountSuccessed = "The delete application is submitted, waiting for approve";
	public static String FailedToDeleteBankAccount = "Failed to delete the bank account";


	public static void setValue(Class languageClass, XmlNode xmlNode)
	{
		if (xmlNode==null) return;
		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlNode xmlNode2 = xmlNodeList.item(i);
			String nodeName = xmlNode2.get_LocalName();
			String nodeValue = xmlNode2.get_InnerText();
			try
			{
				languageClass.getField(nodeName).set(nodeName, nodeValue);
			}
			catch (SecurityException ex)
			{
				continue;
			}
			catch (NoSuchFieldException ex)
			{
				continue;
			}
			catch (IllegalAccessException ex)
			{
				continue;
			}
			catch (IllegalArgumentException exception)
			{
				continue;
			}
		}
	}

	public synchronized static void initialize()
	{
		try
		{
			//String filePath = AppToolkit.get_LanguageDirectory()
			//	+ PublicParametersManager.version + File.separator + "Default.xml";
			//XmlDocument xmlDocument = new XmlDocument();
			//xmlDocument.load(filePath);
			//XmlNode xmlNode = xmlDocument.get_DocumentElement();
			XmlNode xmlNode = AppToolkit.getResourceXml("Language/" + PublicParametersManager.version,"Default.xml");
			Language.setValue(Language.class, xmlNode.get_Item("Language"));
			AccountLockLanguage.setValue(xmlNode.get_Item("AccountLockLanguage"));
			Language.setValue(AccountSingleLanguage.class, xmlNode.get_Item("AccountSingleLanguage"));
			Language.setValue(InstrumentLanguage.class, xmlNode.get_Item("InstrumentLanguage"));
			Language.setValue(SummaryLanguage.class, xmlNode.get_Item("SummaryLanguage"));
			Language.setValue(MessageLanguage.class, xmlNode.get_Item("MessageLanguage"));
			Language.setValue(NewsLanguage.class, xmlNode.get_Item("NewsLanguage"));
			Language.setValue(MatchOrderLanguage.class, xmlNode.get_Item("MatchOrderLanguage"));
			Language.setValue(LogLanguage.class, xmlNode.get_Item("LogLanguage"));
			Language.setValue(RecoverPasswordQuotationSettingGridLanguage.class, xmlNode.get_Item("RecoverPasswordQuotationSettingGridLanguage"));
			Language.setValue(TradingAccountLanguage.class, xmlNode.get_Item("TradingAccountLanguage"));
			InstrumentSelectLanguage.setValue(xmlNode.get_Item("InstrumentSelectLanguage"));
			MakeOrderAccountGridLanguage.setValue(xmlNode.get_Item("OrderOperateAccountGridForMultiDQLanguage"));
			MakeOrderVerificationLanguage.setValue(xmlNode.get_Item("OrderPlacementForLMTLanguage"));
			OrderLanguage.setValue(xmlNode.get_Item("OrderLanguage"));
			OpenOrderLanguage.setValue(xmlNode.get_Item("OpenOrderLanguage"));
			OutstandingOrderLanguage.setValue(xmlNode.get_Item("OutstandingOrderLanguage"));
			MakeOrderLiquidationLanguage.setValue(xmlNode.get_Item("OrderOperateLiquidationLanguage"));
			//Margin
			Language.setValue(AdditionalClientLanguage.class, xmlNode.get_Item("AdditionalClientLanguage"));
			Language.setValue(AgentLanguage.class, xmlNode.get_Item("AgentLanguage"));
			Language.setValue(CallMarginExtensionLanguage.class, xmlNode.get_Item("CallMarginExtensionLanguage"));
			Language.setValue(PaymentInstructionLanguage.class, xmlNode.get_Item("PaymentInstructionLanguage"));
			Language.setValue(FundTransferLanguage.class, xmlNode.get_Item("FundTransferLanguage"));

			Language.setValue(ChartLanguage.class, xmlNode.get_Item("ChartLanguage"));
			Language.setValue(BestLimitLanguage.class, xmlNode.get_Item("BestLimitLanguage"));
			Language.setValue(TimeAndSaleLanguage.class, xmlNode.get_Item("TimeAndSaleLanguage"));
			Language.setValue(ReformMessage.class,xmlNode.get_Item("ReformMessage"));

			Locale.setDefault(PublicParametersManager.getLocal());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private static String[] _allLanguages = new String[]{"ENG", "繁體中文", "简体中文", "日本語", "KOR", "FRE", "GER", "ITA"};

	//private static String[] _allLanguages = new String[]{"ENG", "CHT", "CHS", "JPN", "KOR", "FRE", "GER", "ITA"};

	public static String[] getAvailableLanguages() throws IOException
	{
		return Language.getAvailableLanguages(null);
	}

	public static String[] getAvailableLanguages(String packageName) throws IOException
	{
		ArrayList<String> availableLanguages = new ArrayList<String>();
		for(int index = 0; index < Language._allLanguages.length; index++)
		{
			String language = Language._allLanguages[index];
			if(index == 1) language = "CHT";
			if(index == 2) language = "CHS";
			if(index == 3) language = "JPN";
			if (Language.isLanguagePackage(packageName,language))
			{
				availableLanguages.add(Language._allLanguages[index]);
			}
		}
		return availableLanguages.toArray(new String[0]);
	}

	public static boolean isLanguagePackage(String packageName, String languageVersion)
	{
		boolean isPackage = false;
		try
		{
			if (packageName == null || packageName.length() == 0)
			{
				isPackage = ResourceHelper.exists(languageVersion + "/" + "Default.xml");
			}
			else
			{
				isPackage = ResourceHelper.exists(packageName + "/" + languageVersion + "/" + "Default.xml");
			}
		}
		catch (IOException iOException)
		{
			iOException.printStackTrace();
			isPackage = false;
		}
		return isPackage;
	}
}
