package Util;

import nu.xom.Element;
import java.util.UUID;
import Packet.LoginInfoManager;
import Packet.ComunicationObject;
import Packet.PacketContants;
import framework.DateTime;
import framework.Guid;
import Packet.PacketContants;
import framework.xml.XmlNode;
import Packet.StringConstants;
import java.math.BigDecimal;

public final class CommandHelper
{
	public static ComunicationObject BuildGetInitDataCommand()
	{
		return buildNoArgumentsCommandCommon("GetInitData");
	}

	public static ComunicationObject BuildLoginCommand(String loginID,
												 String password, String version,Integer appType)
	{
		RequestWithRootAndArgumentNode target= newRootElementWithArgument();
	   buildRequestArgumentsHelper(target.args,loginID,password,version,appType.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithNoSession("Login",target.root);
		return request;
	}



	public static ComunicationObject BuildGetNewsList2Command(String newsCategoryID, String language, DateTime date)
	{
		RequestWithRootAndArgumentNode target= newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args,"newsCategoryID",newsCategoryID);
		XmlElementHelper.appendChild(target.args,"language",language);
		XmlElementHelper.appendChild(target.args,"date",DateTimeHelper.ToStandardFormat(date));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetNewsList2",target.root);
		return request;
	}

	private static RequestWithRootAndArgumentNode newRootElementWithArgument()
	{
		Element root = new Element(PacketContants.CommandRootName);
		Element args = new Element(PacketContants.CommandArgumentName);
		root.appendChild(args);
		RequestWithRootAndArgumentNode result = new RequestWithRootAndArgumentNode(root,args);
		return result;
	}

	public static ComunicationObject buildGetMessagesCommand()
	{
		return buildNoArgumentsCommandCommon("GetMessages");
	}

	public static ComunicationObject buildGetAccountBankReferenceData(String countryId,String language)
	{
		RequestWithRootAndArgumentNode target= newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args,"countryId",countryId);
		XmlElementHelper.appendChild(target.args,"language",language);
		ComunicationObject request=RequestCommandHelper.newCommandWithSession("GetAccountBankReferenceData",target.root);
		return request;
	}

	public static ComunicationObject buildAsyncGetTickByTickHistoryDatas2(Guid instrumentId, DateTime from, DateTime to)
	{
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args, "instrumentId", instrumentId.toString());
		XmlElementHelper.appendChild(target.args, "from", DateTimeHelper.ToStandardFormat(from));
		XmlElementHelper.appendChild(target.args, "to", DateTimeHelper.ToStandardFormat(to));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetTickByTickHistoryData", target.root);
		return request;
	}


	public static ComunicationObject buildGetLostCommands(Integer firstSequence, Integer lastSequence)
	{
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args, "firstSequence", firstSequence.toString());
		XmlElementHelper.appendChild(target.args, "lastSequence", lastSequence.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetLostCommands", target.root);
		return request;
	}

	public static ComunicationObject buildGetInterestRate(Guid[] orderIds)
	{
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args, "orderIds", ArrayHelper.join(orderIds,PacketContants.CollectionItemSeperator));
		ComunicationObject request = RequestCommandHelper.newCommandWithNoSession("GetInterestRateByOrderId",target.root);
		return request;
	}


	public static ComunicationObject buildGetInterestRate(Guid interestRateId)
	{
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args, "interestRateId", interestRateId.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetInterestRateByInterestRateId",target.root);
		return request;
	}

	public static ComunicationObject buildGetInstrumentForSettingCommand(){
		return buildNoArgumentsCommandCommon("GetInstrumentForSetting");
	}

	public static ComunicationObject buildUpdateInstrumentSetting(String[] instrumentIds){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		XmlElementHelper.appendChild(target.args, "instrumentIds", StringHelper.join(instrumentIds,","));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("UpdateInstrumentSetting",target.root);
		return request;
	}


	public static ComunicationObject buildStatementForJava2Command(Integer statementReportType, String dayBegin, String dayTo, String IDs, String reportxml){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,statementReportType.toString(),dayBegin,dayTo,IDs,reportxml);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("StatementForJava2",target.root);
		return request;
	}

	public static ComunicationObject buildGetReportContentCommand(Guid asycResultId){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,asycResultId.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetReportContent",target.root);
		return request;
	}

	public static ComunicationObject buildLedgerForJava2(String dateFrom, String dateTo, String IDs, String reportxml){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,dateFrom,dateTo,IDs,reportxml);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("LedgerForJava2",target.root);
		return request;

	}


	public static ComunicationObject buildUpdatePasswordCommand(String loginId, String oldPassword, String newPassword){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,loginId,oldPassword,newPassword);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("UpdatePassword",target.root);
		return request;
	}


	public static ComunicationObject buildSaveLogCommand(String logCode,DateTime timestamp,String action, Guid transactionId){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,logCode,DateTimeHelper.ToStandardFormat(timestamp),action,transactionId.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("saveLog",target.root);
		return request;
	}

	public static ComunicationObject buildUpdateAccountsSettingCommand(Guid[] accountIds){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,StringHelper.join(accountIds,","));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("UpdateAccountsSetting",target.root);
		return request;
	}

	public static ComunicationObject buildGetMerchantInfoFor99BillCommand(Guid[] organizationIds){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,StringHelper.join(organizationIds,","));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetMerchantInfoFor99Bill",target.root);
		return request;
	}

	public static ComunicationObject buildAdditionalClientCommand(String email, String receive, String organizationName, String customerName,
									String reportDate, String accountCode, String correspondingAddress, String registratedEmailAddress,
									String tel, String mobile, String fax, String fillName1, String ICNo1, String fillName2, String ICNo2,
									String fillName3, String ICNo3){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,email,receive,organizationName,customerName,reportDate,accountCode,correspondingAddress,registratedEmailAddress
			,tel,mobile,fax,fillName1,ICNo1,fillName2,ICNo2,fillName3,ICNo3);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("AdditionalClient",target.root);
		return request;

	}

	public static ComunicationObject buildAgentCommand(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
						 String previousAgentCode,
						 String previousAgentName, String newAgentCode, String newAgentName, String newAgentICNo, String dateReply){
			RequestWithRootAndArgumentNode target = newRootElementWithArgument();
			buildRequestArgumentsHelper(target.args,email,receive,organizationName,customerName,reportDate,accountCode,previousAgentCode,previousAgentName,
				                        newAgentCode,newAgentName,newAgentICNo,dateReply);
			ComunicationObject request = RequestCommandHelper.newCommandWithSession("Agent",target.root);
			return request;
	}


	public static ComunicationObject buildCallMarginExtensionCommand(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									   String currency,
									   String currencyValue, String dueDate){
			RequestWithRootAndArgumentNode target = newRootElementWithArgument();
			buildRequestArgumentsHelper(target.args,email,receive,organizationName,customerName,reportDate,accountCode,
				                                    currency,currencyValue,dueDate);
			ComunicationObject request = RequestCommandHelper.newCommandWithSession("CallMarginExtension",target.root);
			return request;
	}


	public static ComunicationObject buildPaymentInstructionCommand(String email, String receive, String organizationName, String customerName, String reportDate, String accountCode,
									  String currency,
									  String currencyValue, String beneficiaryName, String bankAccount, String bankerName, String bankerAddress,
									  String swiftCode,
									  String remarks,
									  String thisisClient){
			RequestWithRootAndArgumentNode target = newRootElementWithArgument();
			buildRequestArgumentsHelper(target.args,email,receive,organizationName,customerName,reportDate,accountCode,currency,currencyValue,beneficiaryName,bankAccount,bankerName,
										bankerAddress,swiftCode,remarks,thisisClient);
			ComunicationObject request = RequestCommandHelper.newCommandWithSession("PaymentInstruction",target.root);
			return request;
	}


	public static ComunicationObject buildPaymentInstructionInternalCommand(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currencyCode, String amount, String beneficiaryAccount,
					String beneficiaryAccountOwner, String email2){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,email,organizationName,customerName,reportDate,
									accountCode,currencyCode,amount,beneficiaryAccount,beneficiaryAccountOwner,email2);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("PaymentInstructionInternal",target.root);
		return request;
	}


	public static ComunicationObject buildAssignCommand(){
		return buildNoArgumentsCommandCommon("Assign");
	}

	public static ComunicationObject buildChangeLeverageCommand(Guid accountId, Integer leverage){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,accountId.toString(),leverage.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("ChangeLeverage",target.root);
		return request;
	}


	public static ComunicationObject buildAsyncGetChartData2Command(Guid instrumentId, DateTime from, DateTime to,String dataCycleParameter){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,instrumentId.toString(),DateTimeHelper.ToStandardFormat(from),DateTimeHelper.ToStandardFormat(to),dataCycleParameter);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("AsyncGetChartData2",target.root);
		return request;
	}

	public static ComunicationObject buildGetChartDataCommand(Guid asyncResultId){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,asyncResultId.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("GetChartData",target.root);
		return request;

	}


	public static ComunicationObject buildPaymentInstructionCashCommand(String email, String organizationName, String customerName, String reportDate,
					String accountCode, String currency, String amount, String beneficiaryName, String beneficiaryAddress){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,email,organizationName,customerName,reportDate,
									accountCode,currency,amount,beneficiaryName,beneficiaryAddress);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("PaymentInstructionCash",target.root);
		return request;
	}

	public static ComunicationObject buildFundTransferCommand(String email, String receive, String organizationName, String customerName, String reportDate, String currency,
								String currencyValue,
								String accountCode, String bankAccount, String beneficiaryName, String replyDate){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,email,receive,organizationName,customerName,reportDate,currency,
									currencyValue,accountCode,bankAccount,beneficiaryName,replyDate);
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("FundTransfer",target.root);
		return request;
	}


	public static ComunicationObject buildVerifyTransactionCommand(Guid[] transactionIds){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,StringHelper.join(transactionIds,StringConstants.ArrayItemSeparator));
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("VerifyTransaction",target.root);
		return request;
    }


	public static ComunicationObject buildPlaceCommand(XmlNode tran)
	{
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,tran.get_OuterXml());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("Place",target.root);
		return request;
	}

	public static ComunicationObject buildQuoteCommand(Guid instrumentId, BigDecimal quoteLot, Integer BSStatus){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,instrumentId.toString(),quoteLot.toString(),BSStatus.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("Quote",target.root);
		return request;
	}

	public static ComunicationObject buildQuote2Command(Guid instrumentId, BigDecimal buyQuoteLot, BigDecimal sellQuoteLot, Integer tick){
		RequestWithRootAndArgumentNode target = newRootElementWithArgument();
		buildRequestArgumentsHelper(target.args,instrumentId.toString(),buyQuoteLot.toString(),sellQuoteLot.toString(),tick.toString());
		ComunicationObject request = RequestCommandHelper.newCommandWithSession("Quote2",target.root);
		return request;
	}

	public static ComunicationObject buildGetAccountsForSettingCommand(){
		return buildNoArgumentsCommandCommon("GetAccsountsForSetting");
	}


	public static ComunicationObject buildKeepAliveCommand(){
		return buildNoArgumentsCommandCommon("KeepAlive");
	}

	public static ComunicationObject buildRecoverCommand(){
		ComunicationObject request = buildNoArgumentsCommandCommon("Recover");
		return request;
	}

	public static ComunicationObject buildLogoutCommand(){
		return buildNoArgumentsCommandCommon("Logout");
	}



	private static void buildRequestArgumentsHelper(Element argElement,String... args){
		for (String arg : args)
		{
			XmlElementHelper.appendChild(argElement, arg);
		}
	}


	private static ComunicationObject buildNoArgumentsCommandCommon(String commandName)
	{
		Element root = new Element(PacketContants.CommandRootName);
		ComunicationObject target = RequestCommandHelper.newCommandWithSession(commandName, root);
		return target;

	}


	private final static class RequestWithRootAndArgumentNode
	{
		private final Element root;
		private final Element args;
		public RequestWithRootAndArgumentNode(Element root, Element args)
		{
			this.root = root;
			this.args = args;
		}
	}

}
