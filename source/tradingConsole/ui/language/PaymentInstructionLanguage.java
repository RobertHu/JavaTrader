package tradingConsole.ui.language;

public class PaymentInstructionLanguage
{
	private PaymentInstructionLanguage()
	{
	}

	public static String paymentInstructionCaption = "PAYMENT INSTRUCTION";
	public static String reportDateCaption = "Date:";
	public static String organizationCaption = "To:";
	public static String customerAccountCaption = "From:";
	public static String emailCaption = "E-mail/Tel:";
	public static String clientCaption = "This is Client:";
	public static String clientNo1Caption = "(1)";
	public static String clientNo2Caption = "(2)";
	public static String clientNo3Caption = "(3)";
	public static String clientNo4Caption = "(4)";
	public static String separatorCaption = "----------------------------------------------------------------------------------------------------------";
	public static String amountCaption = "Amount:";
	public static String beneficiaryNameCaption = "Name of beneficiary:";
	public static String bankAccountNoCaption = "Bank Account No.:";
	public static String bankerNameCaption = "Name of Banker:";
	public static String bankerAddressCaption = "Banker's Address:";
	public static String swiftCodeCaption = "Swift Code:";
	public static String remarksCaption = "Remarks:";
	public static String submitButtonCaption = "Submit";
	public static String resetButtonCaption = "Reset";

	public static String amountToBeTransferredCaption = "Amount to be Transferred:";
	public static String beneficiaryAddressCaption = "Address of Beneficiary";
	public static String specialInstructionCaption = "Special Instruction";
	public static String declarationAndWarrantyCaption = "DECLARATIOIN AND WARRANTY";
	public static String beneficiaryAccountCaption = "Beneficiary Account No.:";
	public static String beneficiaryAccountOwnerCaption = "Owner of Receipient/Beneficiary Account:";
	public static String cashWarrant = "Client name and beneficiary are one and the same person."
			+ "Client hereby warrants that the amount being requested to be paid in cash represents withdrawal"
			+ "	form the available amount in his trading account as stated above. Receipt of the payment absolves {0} from"
			+ " any such obligation pertaining to the stated amount withdrawn";
	public static String payableInCash = "Payable In Cash";
	public static String cashInstruction = "Please pay in Cash to the Beneficiary stated above";

	public static String internalTransferWarrant1 = "  The owner of the source account No. {0} hereby declares that the amount requested to be transferred is the available amount in his trading account and hereby absolves {1}. LLP form any liabilities whatsoever on the amount transferred upon execution of the Payment instruction, hereof.";
	public static String internalTransferWarrant2 = "  The owner of the recipient account No. {2} hereby confirms receipt and benefits of the transferred amount upon execution of the herein Payment instruction in his favor.";
	public static String internalTransfer = "Internal Transfer";
	public static String internalTransferInstruction = "Please Transfer Funds From the Above Source Account to the Recipient Account Stated Below";
}
