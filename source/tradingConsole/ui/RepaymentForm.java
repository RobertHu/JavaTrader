package tradingConsole.ui;

import javax.swing.*;
import tradingConsole.Order;
import java.awt.Window;
import java.awt.Font;
import tradingConsole.ui.language.Language;
import tradingConsole.TradePolicyDetail;
import tradingConsole.Account;
import tradingConsole.Instrument;
import tradingConsole.settings.SettingsManager;
import tradingConsole.settings.InstalmentPolicy;
import tradingConsole.settings.InstalmentPolicyDetail;
import tradingConsole.enumDefine.physical.ContractTerminateType;
import tradingConsole.AppToolkit;
import java.math.BigDecimal;
import tradingConsole.CurrencyRate;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import tradingConsole.ui.language.InstalmentLanguage;
import framework.Guid;
import tradingConsole.common.TransactionError;
import java.text.DecimalFormat;
import framework.xml.XmlNode;
import framework.xml.XmlDocument;

public class RepaymentForm extends JDialog
{
	private Order order;
	private InstalmentPolicy instalmentPolicy;
	private InstalmentPolicyDetail instalmentPolicyDetail;

	private PVStaticText2 orderCodeLable = new PVStaticText2();
	private PVStaticText2 orderCodeText = new PVStaticText2();

	private PVStaticText2 lotLable = new PVStaticText2();
	private PVStaticText2 lotText = new PVStaticText2();

	private PVStaticText2 repaymentAmountLable = new PVStaticText2();
	private JFormattedTextFieldEx repaymentAmountText = new JFormattedTextFieldEx(new DecimalFormat(), false);

	private PVStaticText2 repaymentFeeTypeLable = new PVStaticText2();
	private PVStaticText2 repaymentFeeTypeText = new PVStaticText2();

	private PVStaticText2 repaymentFeeBaseLable = new PVStaticText2();
	private PVStaticText2 repaymentFeeBaseText = new PVStaticText2();

	private PVStaticText2 repaymentFeeLable = new PVStaticText2();
	private JFormattedTextFieldEx repaymentFeeText = new JFormattedTextFieldEx(new DecimalFormat(), false);

	private PVStaticText2 totalAmountLable = new PVStaticText2();
	private JFormattedTextFieldEx totalAmountText = new JFormattedTextFieldEx(new DecimalFormat(), false);

	private PVButton2 confirmButton = new PVButton2();
	private PVButton2 cancelButton = new PVButton2();

	private boolean isRepaymented = false;

	public RepaymentForm(Window parent, Order order)
	{
		super(parent);

		this.order = order;
		Account account = this.order.get_Account();
		Instrument instrument = this.order.get_Instrument();
		SettingsManager settingsManager = account.get_TradingConsole().get_SettingsManager();
		TradePolicyDetail tadePolicyDetail =
			settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), instrument.get_Id());
		this.instalmentPolicy = settingsManager.getInstalmentPolicy(tadePolicyDetail.get_InstalmentPolicyId());
		this.instalmentPolicyDetail = this.instalmentPolicy.get_TillPayoffDetail();
		this.jbInit();

		this.confirmButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				repayment();
			}
		});

		this.cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				isRepaymented = false;
				dispose();
			}
		});
	}

	private void jbInit()
	{
		this.setTitle(InstalmentLanguage.BalanceOfCost);
		this.setSize(350, 225);

		this.repaymentAmountText.setEditable(false);
		this.repaymentFeeText.setEditable(false);

		Font font = new Font("SansSerif", Font.BOLD, 12);

		orderCodeLable.setFont(font);
		orderCodeLable.setText(Language.orderCode);
		orderCodeText.setFont(font);
		orderCodeText.setText(this.order.get_Code());

		this.lotLable.setFont(font);
		this.lotLable.setText(InstalmentLanguage.Lot);
		this.lotText.setFont(font);

		repaymentAmountLable.setFont(font);
		repaymentAmountLable.setText(InstalmentLanguage.BalanceAmount);
		repaymentAmountText.setFont(font);
		repaymentAmountText.setText(this.order.get_UnpaidValueString());

		totalAmountLable.setFont(font);
		totalAmountLable.setText(InstalmentLanguage.TotalPayable);
		totalAmountText.setFont(font);

		repaymentFeeTypeLable.setFont(font);
		repaymentFeeTypeLable.setText(InstalmentLanguage.RepaymentFeeType);
		repaymentFeeTypeText.setFont(font);
		repaymentFeeTypeText.setText(this.instalmentPolicyDetail.get_ContractTerminateType().toLocalString());

		repaymentFeeBaseLable.setFont(font);
		repaymentFeeBaseLable.setText(InstalmentLanguage.RepaymentFeeRate);
		repaymentFeeBaseText.setFont(font);

		if(this.instalmentPolicyDetail.get_ContractTerminateType().equals(ContractTerminateType.RepaymentRatio))
		{
			repaymentFeeBaseText.setText(AppToolkit.format(this.instalmentPolicyDetail.get_ContractTerminateFee().doubleValue() * 100, 2) + "%");
		}
		else
		{
			int decimals = this.order.get_Instrument().get_Currency().get_Decimals();
			repaymentFeeBaseText.setText(AppToolkit.format(this.instalmentPolicyDetail.get_ContractTerminateFee(), decimals));
		}

		repaymentFeeLable.setFont(font);
		repaymentFeeLable.setText(InstalmentLanguage.DeliveryCharge);
		repaymentFeeText.setFont(font);

		BigDecimal fee = BigDecimal.ZERO;
		if(this.instalmentPolicyDetail.get_ContractTerminateType().equals(ContractTerminateType.RepaymentRatio))
		{
			fee = this.order.get_UnpaidValue().multiply(this.instalmentPolicyDetail.get_ContractTerminateFee());
		}
		else if(this.instalmentPolicyDetail.get_ContractTerminateType().equals(ContractTerminateType.PerLot))
		{
			fee = this.instalmentPolicyDetail.get_ContractTerminateFee().multiply(this.order.get_LotBalance());
		}
		else if(this.instalmentPolicyDetail.get_ContractTerminateType().equals(ContractTerminateType.LumpSum))
		{
			fee = this.instalmentPolicyDetail.get_ContractTerminateFee();
		}

		Account account = this.order.get_Account();
		int decimals = account.get_Currency().get_Decimals();
		repaymentFeeText.setText(AppToolkit.format(fee, decimals));

		totalAmountText.setText(AppToolkit.format(fee.add(this.order.get_UnpaidValue()), decimals));

		this.confirmButton.setText(Language.OrderPlacementbtnConfirm);
		this.cancelButton.setText(Language.OrderPlacementbtnCancel);

		this.setLayout(new GridBagLayout());

		int y = 0;
		this.add(this.orderCodeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 60, 0));
		this.add(this.orderCodeText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 60, 0));
		y++;

		/*this.add(this.lotLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
		this.add(this.lotText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
			,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 0, 0));
		this.lotText.setText(this.order.get_LotBalanceString());
		y++;*/

		this.add(this.repaymentAmountLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 60, 0));
		this.add(this.repaymentAmountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 60, 0));
		y++;

		this.add(this.repaymentFeeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 60, 0));
		this.add(this.repaymentFeeText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 60, 0));
		y++;

		this.add(this.totalAmountLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 60, 0));
		this.add(this.totalAmountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 60, 0));
		y++;

		/*this.add(this.repaymentFeeTypeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 60, 0));
		this.add(this.repaymentFeeTypeText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 60, 0));
		y++;

		this.add(this.repaymentFeeBaseLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 60, 0));
		this.add(this.repaymentFeeBaseText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 60, 0));
		y++;*/

		JPanel panel = new JPanel();
		this.add(panel, new GridBagConstraints(0, y, 2, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(40, 5, 2, 5), 0, 0));
		panel.setLayout(new GridBagLayout());
		panel.add(this.confirmButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 40, 0));
		panel.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 40, 0));
	}

	private void repayment()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<Orders>");
		sb.append("<Order");

		sb.append(" ID='");
		sb.append(this.order.get_Id().toString());
		sb.append("'");

		sb.append(" IsPayOff='true'");

		sb.append(" TerminateFee='");
		sb.append(this.repaymentFeeText.getPlainText());
		sb.append("'");

		sb.append(" Amount='");
		sb.append(this.repaymentAmountText.getPlainText());
		sb.append("'");

		sb.append(" SourceTerminateFee='");
		sb.append(this.repaymentFeeText.getPlainText());
		sb.append("'");

		sb.append(" SourceAmount='");
		sb.append(this.repaymentAmountText.getPlainText());
		sb.append("'");

		sb.append(" CurrencyRate='1.0'");

		sb.append(" />");
		sb.append("</Orders>");

		Account account = this.order.get_Account();
		Guid currencyId = account.get_Currency().get_Id();
		Guid accountId = account.get_Id();
		double sumSourcePaymentAmount, sumSourceTerminateFee;
		sumSourcePaymentAmount = new BigDecimal(this.repaymentAmountText.getPlainText()).doubleValue();
		sumSourceTerminateFee = new BigDecimal(this.repaymentFeeText.getPlainText()).doubleValue();

		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(sb.toString());

		TransactionError error
			= account.get_TradingConsole().get_TradingConsoleServer().instalmentPayoff(accountId, currencyId, sumSourcePaymentAmount, sumSourceTerminateFee, null, xmlDocument.get_DocumentElement());
		if(error.equals(TransactionError.OK))
		{
			this.isRepaymented = true;
			this.dispose();
			this.getOwner().dispose();
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, Language.FailedToRepayment + ": " + TransactionError.getCaption(error));
		}
	}

	public boolean isRepaymented()
	{
		return this.isRepaymented;
	}
}
