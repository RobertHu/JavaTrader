package tradingConsole.ui;

import javax.swing.*;
import tradingConsole.Account;
import java.util.ArrayList;
import tradingConsole.Instrument;
import tradingConsole.settings.SettingsManager;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.Price;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Color;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.language.InstalmentLanguage;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import tradingConsole.ui.language.TimeAndSaleLanguage;
import java.util.HashMap;
import framework.Guid;
import tradingConsole.ui.grid.BindingSource;
import java.util.Vector;
import tradingConsole.TradingConsole;
import tradingConsole.framework.PropertyDescriptor;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import tradingConsole.ui.grid.ISelectedRowChangedListener;
import com.jidesoft.grid.TableModelWrapperUtils;
import tradingConsole.TradePolicyDetail;
import tradingConsole.settings.InstalmentPolicy;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import tradingConsole.settings.InstalmentPolicyDetail;
import tradingConsole.AppToolkit;
import tradingConsole.enumDefine.physical.InstalmentType;
import tradingConsole.enumDefine.physical.RecalculateRateType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import tradingConsole.Order;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import tradingConsole.enumDefine.physical.AdministrationFeeBase;
import java.util.Collection;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import tradingConsole.enumDefine.Phase;
import java.awt.Window;
import framework.data.DataSet;
import framework.data.DataTable;
import framework.data.DataRowCollection;
import framework.data.DataRow;
import com.jidesoft.swing.JideSwingUtilities;
import framework.DBNull;
import framework.DateTime;
import tradingConsole.enumDefine.AccountType;
import framework.lang.Enum;
import tradingConsole.TradingConsoleServer;
import tradingConsole.physical.InstalmentDetail;
import tradingConsole.physical.InstalmentAccount;
import tradingConsole.enumDefine.OrderType;
import tradingConsole.ui.grid.IPropertyChangedListener;
import tradingConsole.TradeDay;
import tradingConsole.settings.InstalmentPeriod;
import tradingConsole.enumDefine.physical.PaymentMode;
import tradingConsole.enumDefine.physical.InstalmentFrequence;
import tradingConsole.settings.MakeOrderAccount;
import tradingConsole.enumDefine.physical.DownPaymentBasis;

public class InstalmentForm extends JDialog
{
	private Account[] accounts;
	private HashMap<Guid, MakeOrderAccount> makeOrderAccounts = null;
	private HashMap<Guid, BigDecimal> lots;
	private HashMap<Guid, InstalmentInfo> instalmentInfoList;
	private PaymentMode paymentMode = null;
	private Instrument instrument;
	private SettingsManager settingsManager;
	private Price limitPrice = null, stopPrice = null;
	private ArrayList<InstalmentDetail> instalmentDetails;
	private boolean isForExecutedOrder = false;
	private Order order;

	private PVStaticText2 accountCodeLable = new PVStaticText2();
	private PVStaticText2 accountText = new PVStaticText2();

	private PVStaticText2 priceLable = new PVStaticText2();
	private PVStaticText2 priceText = new PVStaticText2();
	private JCheckBox limitPriceCheckbox = new JCheckBox();
	private JCheckBox stopPriceCheckbox = new JCheckBox();

	private PVStaticText2 instalmentTypeLable = new PVStaticText2();
	private JAdvancedComboBox instalmentTypeChoice = new JAdvancedComboBox();

	private PVStaticText2 periodLable = new PVStaticText2();
	private JTextField periodText = new JTextField();
	private JAdvancedComboBox periodChoice = new JAdvancedComboBox();

	private PVStaticText2 lotLable = new PVStaticText2();
	private PVStaticText2 lotText = new PVStaticText2();

	private PVStaticText2 downPaymentLable = new PVStaticText2();
	private PVStaticText2 downPaymentPercentLable = new PVStaticText2();
	private JSpinner downPaymentField = new JSpinner();

	private PVStaticText2 recalculateRateTypeLable = new PVStaticText2();
	private JAdvancedComboBox  recalculateRateTypeChoice = new JAdvancedComboBox();

	private PVStaticText2 interestRateLable = new PVStaticText2();
	private JTextField interestRateText = new JTextField();

	private PVStaticText2 totalAmountLable = new PVStaticText2();
	private JTextField totalAmountText = new JTextField();

	private PVStaticText2 downPaymentAmountLable = new PVStaticText2();
	private JTextField downPaymentAmountText = new JTextField();

	private PVStaticText2 loanAmountLable = new PVStaticText2();
	private JTextField loanAmountText = new JTextField();

	private PVStaticText2 instalmentFeeLable = new PVStaticText2();
	private JTextField instalmentFeeText = new JTextField();

	private PVStaticText2 instalmentWarningLable = new PVStaticText2();

	private PVStaticText2 paidPeriodLable = new PVStaticText2();
	private JTextField paidPeriodText = new JTextField();

	private PVStaticText2 remainPeriodLable = new PVStaticText2();
	private JTextField remainPeriodText = new JTextField();

	private PVStaticText2 paidOverduePeriodLable = new PVStaticText2();
	private JTextField paidOverduePeriodText = new JTextField();

	private PVStaticText2 remainOverduePeriodLable = new PVStaticText2();
	private JTextField remainOverduePeriodText = new JTextField();

	private PVStaticText2 paidPrincipalLable = new PVStaticText2();
	private JTextField paidPrincipalText = new JTextField();

	private PVStaticText2 remainPrincipalLable = new PVStaticText2();
	private JTextField remainPrincipalText = new JTextField();

	private PVStaticText2 paidInterestLable = new PVStaticText2();
	private JTextField paidInterestText = new JTextField();

	private PVStaticText2 penaltyInterestLable = new PVStaticText2();
	private JTextField penaltyInterestText = new JTextField();

	private PVStaticText2 remainInterestLable = new PVStaticText2();
	private JTextField remainInterestText = new JTextField();

	private PVStaticText2 summaryLable =  new PVStaticText2();
	private PVStaticText2 totalPrincipal =  new PVStaticText2();
	private PVStaticText2 totalInterest =  new PVStaticText2();
	private PVStaticText2 totalAmount =  new PVStaticText2();

	private JRadioButton fullAmountRadioButton = new JRadioButton();
	private JRadioButton advancePaymentRadioButton = new JRadioButton();
	private JRadioButton instalmentRadioButton = new JRadioButton();

	private DataGrid instalmentAccountTable = new DataGrid("InstalmentAccount");
	private DataGrid instalmentDetailTable = new DataGrid("InstalmentDetail");

	private PVButton2 confirmButton = new PVButton2();
	private PVButton2 cancelButton = new PVButton2();
	private boolean isConfirmed = false;

	private Account currentAccount = null;
	private PaymentMode currentPaymentMode = null;
	private InstalmentPolicy currentInstalmentPolicy = null;
	private InstalmentPolicyDetail currentInstalmentPolicyDetail = null;

	public InstalmentForm(Window parent, Order order)
	{
		super(parent);

		this.order = order;
		Account account = order.get_Account();
		Instrument instrument = order.get_Instrument();
		HashMap<Guid, BigDecimal> lots = new HashMap<Guid,BigDecimal>();
		lots.put(account.get_Id(), order.get_Lot());
		HashMap<Guid, InstalmentInfo> instalmentInfoList = new HashMap<Guid, InstalmentInfo> ();
		if(order.get_InstalmentPolicyId() != null)
		{

			PaymentMode paymentMode = order.get_Period().get_Frequence().equals(InstalmentFrequence.TillPayoff)? PaymentMode.AdvancePayment : PaymentMode.Instalment;
			InstalmentInfo instalmentInfo
				= new InstalmentInfo(order.get_InstalmentPolicyId(), order.get_Period(), order.get_DownPayment(),
									 order.get_PhysicalInstalmentType(), order.get_RecalculateRateType(), order.get_InstalmentFee(), paymentMode);
			instalmentInfoList.put(account.get_Id(), instalmentInfo);
		}
		this.isForExecutedOrder = order.get_Phase().equals(Phase.Executed);

		this.init(new Account[]{account}, lots, instrument,
				  account.get_TradingConsole().get_SettingsManager(), null, null, instalmentInfoList, instalmentDetails);

		if(this.isForExecutedOrder)
		{
			this.priceText.setText(order.get_ExecutePriceString());
			int decimals = this.instrument.get_Currency().get_Decimals();
			if(this.order.get_InstalmentFee() != null)
			{
				this.instalmentFeeText.setText(AppToolkit.format(this.order.get_InstalmentFee(), decimals));
			}

			Thread thread = new Thread(new Runnable()
			{
				public void run()
				{
					getOrderInstalmentDetails();
				}
			});
			thread.setDaemon(true);
			thread.start();
		}
	}

	public InstalmentForm(Window parent, Account account, BigDecimal lot,
		Instrument instrument, SettingsManager settingsManager,	Price limitPrice, Price stopPrice,
		InstalmentInfo instalmentInfo, PaymentMode paymentMode, boolean isForModifyOrder)
	{
		super(parent);

		HashMap<Guid, BigDecimal> lots = new HashMap<Guid,BigDecimal>();
		lots.put(account.get_Id(), lot);
		HashMap<Guid, InstalmentInfo> instalmentInfoList = new HashMap<Guid, InstalmentInfo>();

		this.paymentMode = paymentMode;
		if(!isForModifyOrder && instalmentInfo != null)
		{
			if(instalmentInfo.isAdvancePayment())
			{
				instalmentInfo = paymentMode == PaymentMode.AdvancePayment ? instalmentInfo : null;
			}
			else
			{
				instalmentInfo = paymentMode == PaymentMode.Instalment ? instalmentInfo : null;
			}
		}

		if(instalmentInfo != null)
		{
			instalmentInfoList.put(account.get_Id(), instalmentInfo.clone());
		}

		this.init(new Account[]{account}, lots, instrument, settingsManager, limitPrice, stopPrice, instalmentInfoList, null);
	}

	public InstalmentForm(Window parent, MakeOrderAccount[] accounts, HashMap<Guid, BigDecimal> lots,
		Instrument instrument, SettingsManager settingsManager,
		HashMap<Guid, InstalmentInfo> instalmentInfoList)
	{
		super(parent);

		HashMap<Guid, InstalmentInfo> instalmentInfoList2 = new HashMap<Guid, InstalmentInfo>();
		if(instalmentInfoList != null)
		{
			for (Guid id : instalmentInfoList.keySet())
			{
				instalmentInfoList2.put(id, instalmentInfoList.get(id).clone());
			}
		}
		else
		{
			for(MakeOrderAccount account : accounts)
			{
				if(account.get_CanAdvancePayment())
				{
					instalmentInfoList2.put(account.get_Account().get_Id(), InstalmentInfo.createDefaultAdvancePaymentInfo(account));
				}
			}
		}

		Account[] accounts2 = new Account[accounts.length];
		this.makeOrderAccounts = new HashMap<Guid,MakeOrderAccount>(accounts.length);
		for(int index = 0; index < accounts.length; index++)
		{
			MakeOrderAccount account = accounts[index];
			makeOrderAccounts.put(account.get_Account().get_Id(), account);
			accounts2[index] = account.get_Account();
		}
		this.init(accounts2, lots, instrument, settingsManager, null, null, instalmentInfoList2, null);
	}

	private void init(Account[] accounts, HashMap<Guid, BigDecimal> lots,
		Instrument instrument, SettingsManager settingsManager,	Price limitPrice, Price stopPrice,
		HashMap<Guid, InstalmentInfo> instalmentInfoList, ArrayList<InstalmentDetail> instalmentDetails)
	{
		this.setModal(true);

		this.accounts = accounts;
		this.lots = lots;
		this.instrument = instrument;
		this.settingsManager = settingsManager;
		this.limitPrice = limitPrice;
		this.stopPrice = stopPrice;
		this.instalmentInfoList = instalmentInfoList;
		this.instalmentDetails = instalmentDetails;

		this.jbInit();
		this.initData();
		this.updatePrice();
		this.confirmButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				isConfirmed = true;
				dispose();
			}
		});

		this.cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				isConfirmed = false;
				dispose();
			}
		});
	}

	private void showWarning(String info)
	{
		AlertDialogForm.showDialog(this, null, true, info);
	}

	private void getOrderInstalmentDetails()
	{
		DataSet dataSet
			= this.order.get_Transaction().get_Account().get_TradingConsole().get_TradingConsoleServer().getOrderInstalment(this.order.get_Id());
		if(dataSet == null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					showWarning(Language.FailedToGetOrderInstalment);
				}
			});
			return;
		}

		DataTable table = dataSet.get_Tables().get_Item(0);
		DataRowCollection rows = table.get_Rows();
		ArrayList<InstalmentDetail> details = new ArrayList<InstalmentDetail>(rows.get_Count());
		int decimals = this.instrument.get_Currency().get_Decimals();
		for(int index = 0;  index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);
			InstalmentDetail instalmentDetail = new InstalmentDetail(row, decimals);
			instalmentDetail.initStatus(this.settingsManager.get_TradeDay());
			details.add(instalmentDetail);
		}
		this.instalmentDetails = details;

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				fillOrderInstalment();
			}
		});
	}

	private void fillOrderInstalment()
	{
		this.bindInstalmentDetail(this.instalmentDetails);
		this.periodText.setText(Integer.toString(this.instalmentDetails.size()));

		int paidPeriod = 0, unpaidPeriod = 0, paidOverduePeriod = 0, unpaidOverduePeriod = 0;
		BigDecimal paidPrincipal = BigDecimal.ZERO, unpaidPrincipal = BigDecimal.ZERO,
			paidInterest = BigDecimal.ZERO, unpaidInterest = BigDecimal.ZERO, penaltyInterest = BigDecimal.ZERO;

		TradeDay tradeDay
			= this.order.get_Account().get_TradingConsole().get_SettingsManager().get_TradeDay();

		for(InstalmentDetail item : this.instalmentDetails)
		{
			boolean isOverdue = item.isOverdue(tradeDay);

			penaltyInterest = penaltyInterest.add(item.get_PenaltyInterest());
			if(item.isPaid())
			{
				paidPeriod++;
				paidPrincipal = paidPrincipal.add(item.get_Principal());
				paidInterest = paidInterest.add(item.get_Interest());

				if(isOverdue) paidOverduePeriod++;
			}
			else
			{
				unpaidPeriod++;
				unpaidPrincipal = unpaidPrincipal.add(item.get_Principal());
				unpaidInterest = unpaidInterest.add(item.get_Interest());
				if(isOverdue) unpaidOverduePeriod++;
			}
		}

		int decimals = this.instrument.get_Currency().get_Decimals();
		this.paidPeriodText.setText(Integer.toString(paidPeriod));
		this.remainPeriodText.setText(Integer.toString(unpaidPeriod));
		this.paidPrincipalText.setText(AppToolkit.format(paidPrincipal, decimals));
		this.remainPrincipalText.setText(AppToolkit.format(unpaidPrincipal, decimals));
		this.paidInterestText.setText(AppToolkit.format(paidInterest, decimals));
		this.remainInterestText.setText(AppToolkit.format(unpaidInterest, decimals));

		this.paidOverduePeriodText.setText(Integer.toString(paidOverduePeriod));
		this.remainOverduePeriodText.setText(Integer.toString(unpaidOverduePeriod));
		this.penaltyInterestText.setText(AppToolkit.format(penaltyInterest, decimals));
	}

	public boolean get_IsConfirmed()
	{
		return this.isConfirmed;
	}

	public HashMap<Guid, InstalmentInfo> get_InstalmentInfoList()
	{
		return this.instalmentInfoList;
	}

	private void handlePaymentChanged()
	{
		PaymentMode paymentMode = PaymentMode.FullAmount;
		if(this.advancePaymentRadioButton.isSelected())
		{
			paymentMode = PaymentMode.AdvancePayment;
		}
		else if(this.instalmentRadioButton.isSelected())
		{
			paymentMode = PaymentMode.Instalment;
		}
		int row = instalmentAccountTable.getSelectedRow();
		if (row != -1)
		{
			row = TableModelWrapperUtils.getActualRowAt(instalmentAccountTable.getModel(), row);
			InstalmentAccount instalmentAccount = (InstalmentAccount)instalmentAccountTable.get_BindingSource().getObject(row);
			instalmentAccount.set_PaymentMode(paymentMode);
			TradingConsole.bindingManager.update("InstalmentAccount", instalmentAccount);

			this.setCurrentAccount(instalmentAccount.get_Account(), paymentMode);
			if(paymentMode.equals(PaymentMode.FullAmount))
			{
				this.instalmentInfoList.remove(instalmentAccount.get_Account().get_Id());
			}
			else
			{
				this.tryCaculateAndUpdateUI();
			}
		}
	}

	private void jbInit()
	{
		boolean needShowInstalmentDetail = true;
		boolean hasAccountCanPlaceInstalment = false;
		boolean hasAccountCanAdvancePayment = false;

		if(this.makeOrderAccounts != null)
		{
			this.setTitle(InstalmentLanguage.PaymentMode);

			for(MakeOrderAccount makeOrderAccount : this.makeOrderAccounts.values())
			{
				if(makeOrderAccount.get_CanInstalment())
				{
					hasAccountCanPlaceInstalment = true;
				}
				if(makeOrderAccount.get_CanAdvancePayment())
				{
					hasAccountCanAdvancePayment = true;
				}
				if(hasAccountCanPlaceInstalment && hasAccountCanAdvancePayment) break;
			}

			if(hasAccountCanPlaceInstalment)
			{
				this.setSize(520, 480);
			}
			else
			{
				needShowInstalmentDetail = false;
				this.setSize(280, 370);
			}
		}
		else
		{
			if(this.paymentMode != null)
			{
				this.setTitle(this.paymentMode.toLocalString());
				if(this.paymentMode.equals(PaymentMode.Instalment))
				{
					this.setSize(520, 420);
				}
				else
				{
					needShowInstalmentDetail = false;
					this.setSize(300, 320);
				}
			}
			else
			{
				this.setTitle(InstalmentLanguage.Instalment);
				this.setSize(650 , 432);
			}
		}

		this.interestRateText.setEditable(false);
		this.downPaymentAmountText.setEditable(false);
		this.loanAmountText.setEditable(false);
		this.totalAmountText.setEditable(false);

		Font font = new Font("SansSerif", Font.BOLD, 12);
		this.accountCodeLable.setFont(font);
		this.accountCodeLable.setText(Language.Account);
		this.accountText.setFont(font);

		this.lotLable.setFont(font);
		this.lotLable.setText(InstalmentLanguage.Lot);
		this.lotText.setFont(font);

		this.priceLable.setFont(font);
		this.priceLable.setText(TimeAndSaleLanguage.Price);
		this.priceText.setFont(font);

		this.limitPriceCheckbox.setFont(font);
		this.stopPriceCheckbox.setFont(font);
		ButtonGroup checkboxGroup = new ButtonGroup();
		checkboxGroup.add(this.limitPriceCheckbox);
		checkboxGroup.add(this.stopPriceCheckbox);

		this.instalmentTypeLable.setFont(font);
		this.instalmentTypeLable.setText(InstalmentLanguage.InstalmentType);
		this.instalmentTypeChoice.setFont(font);

		this.periodLable.setFont(font);
		this.periodLable.setText(InstalmentLanguage.Period);
		this.periodChoice.setFont(font);
		this.periodText.setFont(font);

		this.downPaymentAmountLable.setFont(font);
		this.downPaymentAmountLable.setText(InstalmentLanguage.DownPayment);
		this.downPaymentAmountText.setFont(font);

		this.interestRateLable.setFont(font);
		this.interestRateLable.setText(InstalmentLanguage.InterestRate);
		this.interestRateText.setFont(font);

		this.totalAmountLable.setFont(font);
		this.totalAmountLable.setText(Language.Amount);
		this.totalAmountText.setFont(font);

		this.downPaymentLable.setFont(font);
		this.downPaymentLable.setText(InstalmentLanguage.DownPayment);
		this.downPaymentField.setFont(font);
		this.downPaymentPercentLable.setFont(font);
		this.downPaymentPercentLable.setText("%");

		this.recalculateRateTypeLable.setFont(font);
		this.recalculateRateTypeLable.setText(InstalmentLanguage.RecalculateRateType);
		this.recalculateRateTypeChoice.setFont(font);

		this.downPaymentAmountLable.setFont(font);
		this.downPaymentAmountLable.setText(InstalmentLanguage.DownPaymentAmount);
		this.downPaymentAmountText.setFont(font);

		this.loanAmountLable.setFont(font);
		this.loanAmountLable.setText(InstalmentLanguage.LoanAmount);
		this.loanAmountText.setFont(font);

		this.instalmentFeeLable.setFont(font);
		this.instalmentFeeLable.setText(InstalmentLanguage.InstalmentFee);
		this.instalmentFeeText.setFont(font);

		this.instalmentWarningLable.setFont(font);
		this.instalmentWarningLable.setForeground(Color.RED);
		this.instalmentWarningLable.setText(InstalmentLanguage.InstalmentWarning);

		this.paidPeriodLable.setFont(font);
		this.paidPeriodLable.setText(InstalmentLanguage.PaidPeriod);
		this.paidPeriodText.setFont(font);

		this.remainPeriodLable.setFont(font);
		this.remainPeriodLable.setText(InstalmentLanguage.RemainPeriod);
		this.remainPeriodText.setFont(font);

		this.paidOverduePeriodLable.setFont(font);
		this.paidOverduePeriodLable.setText(InstalmentLanguage.PaidOverduePeriod);
		this.paidOverduePeriodText.setFont(font);

		this.remainOverduePeriodLable.setFont(font);
		this.remainOverduePeriodLable.setText(InstalmentLanguage.RemainOverduePeriod);
		this.remainOverduePeriodText.setFont(font);

		this.paidPrincipalLable.setFont(font);
		this.paidPrincipalLable.setText(InstalmentLanguage.PaidPrincipal);
		this.paidPrincipalText.setFont(font);

		this.remainPrincipalLable.setFont(font);
		this.remainPrincipalLable.setText(InstalmentLanguage.RemainPrincipal);
		this.remainPrincipalText.setFont(font);

		this.paidInterestLable.setFont(font);
		this.paidInterestLable.setText(InstalmentLanguage.PaidInterest);
		this.paidInterestText.setFont(font);

		this.penaltyInterestLable.setFont(font);
		this.penaltyInterestLable.setText(InstalmentLanguage.PenaltyInterest);
		this.penaltyInterestText.setFont(font);

		this.remainInterestLable.setFont(font);
		this.remainInterestLable.setText(InstalmentLanguage.RemainInterest);
		this.remainInterestText.setFont(font);

		this.fullAmountRadioButton.setFont(font);
		this.fullAmountRadioButton.setText(InstalmentLanguage.FullAmount);

		this.advancePaymentRadioButton.setFont(font);
		this.advancePaymentRadioButton.setText(InstalmentLanguage.AdvancePayment);

		this.instalmentRadioButton.setFont(font);
		this.instalmentRadioButton.setText(InstalmentLanguage.Instalment);

		this.summaryLable.setFont(font);
		this.totalPrincipal.setFont(font);
		this.totalInterest.setFont(font);
		this.totalAmount.setFont(font);
		this.summaryLable.setText(InstalmentLanguage.Summary);

		this.confirmButton.setText(Language.OrderPlacementbtnConfirm);
		this.cancelButton.setText(Language.OrderPlacementbtnCancel);

		this.setLayout(new GridBagLayout());

		int y = 0;
		if(this.isForExecutedOrder)
		{
			this.add(this.periodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 60, 0));
			this.periodLable.setText(InstalmentLanguage.TotalPeriod);
			this.add(this.periodText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 0, 0), 60, 0));
			this.periodText.setEditable(false);
			y++;

			this.add(this.paidPeriodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60, 0));
			this.add(this.paidPeriodText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60, 0));
			this.paidPeriodText.setEditable(false);
			y++;

			this.add(this.remainPeriodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.remainPeriodText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.remainPeriodText.setEditable(false);
			y++;

			this.add(this.paidOverduePeriodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.paidOverduePeriodText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.paidOverduePeriodText.setEditable(false);
			y++;

			this.add(this.remainOverduePeriodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.remainOverduePeriodText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.remainOverduePeriodText.setEditable(false);
			y++;

			this.add(this.paidPrincipalLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.paidPrincipalText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.paidPrincipalText.setEditable(false);
			y++;

			this.add(this.remainPrincipalLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.remainPrincipalText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.remainPrincipalText.setEditable(false);
			y++;

			this.add(this.paidInterestLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.paidInterestText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.paidInterestText.setEditable(false);
			y++;

			this.add(this.penaltyInterestLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60,0));
			this.add(this.penaltyInterestText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60,0));
			this.penaltyInterestText.setEditable(false);
			y++;

			this.add(this.remainInterestLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),60, 0));
			this.add(this.remainInterestText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),60, 0));
			this.remainInterestText.setEditable(false);
			y++;

			/*this.add(this.instalmentFeeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),40, 0));
			this.add(this.instalmentFeeText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0),40, 0));
			this.instalmentFeeText.setEditable(false);
			y++;*/
		    this.instalmentDetailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.add(new JScrollPane(this.instalmentDetailTable), new GridBagConstraints(2, 0, 1, y, 1.0, 1.0
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 5, 0, 5), 380, 0));
		}
		else
		{
			if (this.makeOrderAccounts == null)
			{
				this.add(this.accountCodeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
				this.add(this.accountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 2, 0, 0), 0, 0));
				this.accountText.setText(this.accounts[0].get_Code());
				y++;

				this.add(this.lotLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));
				this.add(this.lotText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 2, 0, 0), 0, 0));
				BigDecimal lot = this.lots.get(this.accounts[0].get_Id());

				String lotStr = AppToolkit.format(lot, this.instrument.get_PhysicalLotDecimal());
				this.lotText.setText(lotStr);
			}
			else
			{
				this.add(new JScrollPane(this.instalmentAccountTable), new GridBagConstraints(0, y, 2, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 0, 0), 0, 50));
				y++;

				JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				panel.add(this.fullAmountRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.5, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				if(hasAccountCanAdvancePayment)
				{
					panel.add(this.advancePaymentRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0
						, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				if(hasAccountCanPlaceInstalment)
				{
					panel.add(this.instalmentRadioButton, new GridBagConstraints(2, 0, 1, 1, 0.1, 0.0
						, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				}
				ButtonGroup group = new ButtonGroup();
				group.add(fullAmountRadioButton);
				group.add(advancePaymentRadioButton);
				group.add(instalmentRadioButton);
				ChangeListener changeListener = new ChangeListener()
				{
					public void stateChanged(ChangeEvent e)
					{
						if(stateChangedEventsEnabled) handlePaymentChanged();
					}
				};
				fullAmountRadioButton.addChangeListener(changeListener);
				advancePaymentRadioButton.addChangeListener(changeListener);
				instalmentRadioButton.addChangeListener(changeListener);

				this.add(panel, new GridBagConstraints(0, y, 2, 1, 1.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 0), 0, 0));
			}
			y++;

			this.add(this.priceLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
			if (this.limitPrice == null || this.stopPrice == null)
			{
				this.add(this.priceText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
			}
			else
			{
				JPanel panel = new JPanel();
				panel.add(this.limitPriceCheckbox);
				panel.add(this.stopPriceCheckbox);
				this.add(panel, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
			}
			y++;

			if(needShowInstalmentDetail)
			{
				this.add(this.instalmentTypeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
				this.add(this.instalmentTypeChoice, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
				y++;

				this.add(this.periodLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
				this.add(this.periodChoice, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
				y++;

				this.add(this.interestRateLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
				this.add(this.interestRateText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
				y++;
			}

			this.add(this.downPaymentLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
			JPanel downPaymentPanel = new JPanel(new GridBagLayout());
			downPaymentPanel.add(this.downPaymentField, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			downPaymentPanel.add(this.downPaymentPercentLable, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 1, 0, 0), 5, 0));
			this.add(downPaymentPanel, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
			y++;

			if(needShowInstalmentDetail)
			{
				this.add(this.recalculateRateTypeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
				this.add(this.recalculateRateTypeChoice, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
				y++;
			}

			this.add(this.totalAmountLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
			this.add(this.totalAmountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
			y++;

			this.add(this.downPaymentAmountLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
			this.add(this.downPaymentAmountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
			y++;

			this.add(this.loanAmountLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 40, 0));
			this.add(this.loanAmountText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));
			y++;

			this.add(this.instalmentFeeLable, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0),40, 0));
			this.add(this.instalmentFeeText, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0),40, 0));
			this.instalmentFeeText.setEditable(false);
			y++;

			if(needShowInstalmentDetail)
			{
				this.add(new JScrollPane(this.instalmentDetailTable), new GridBagConstraints(2, 0, 1, y, 1.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 5, 0, 5), 200, 0));

				JPanel summaryPanel = new JPanel(new GridBagLayout());
				summaryPanel.add(this.summaryLable, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 40, 0));
				summaryPanel.add(this.totalPrincipal, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 2), 30, 0));
				summaryPanel.add(this.totalInterest, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 30, 0));
				summaryPanel.add(this.totalAmount, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
					, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 30, 0));

				this.add(summaryPanel, new GridBagConstraints(2, y, 1, 1, 0.0, 0.0
															  , GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(8, 5, 0, 5), 0, 0));
				y++;
			}
			else
			{
				this.add(new JPanel(), new GridBagConstraints(2, 0, 1, y, 0.0, 0.0
					, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, -4, 0, 0), 0, 0));
			}

			JScrollPane pane = new JScrollPane(this.instalmentWarningLable);
			pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			pane.setBorder(null);
			this.add(pane, new GridBagConstraints(0, y, 3, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5), 0, 10));
			y++;
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());
		if(!this.isForExecutedOrder)
		{
			buttonPanel.add(confirmButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 40, 0));
		}
		buttonPanel.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 40, 0));

		this.add(buttonPanel, new GridBagConstraints(0, y, 3, 1, 0.0, 0.0
				, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 2, 5), 0, 0));
	}

	private void initData()
	{
		if(this.limitPrice == null && this.stopPrice != null)
		{
			this.priceText.setText(Price.toString(this.stopPrice));
		}
		else if(this.limitPrice != null && this.stopPrice == null)
		{
			this.priceText.setText(Price.toString(this.limitPrice));
		}
		else if(this.limitPrice != null && this.stopPrice != null)
		{
			this.limitPriceCheckbox.setText(Price.toString(this.limitPrice));
			this.stopPriceCheckbox.setText(Price.toString(this.stopPrice));
		}

		if(this.makeOrderAccounts != null)
		{
			BindingSource bindingSource = new BindingSource();
			bindingSource.setBackground(Color.WHITE);
			TradingConsole.bindingManager.bind("InstalmentAccount", new Vector(), bindingSource, InstalmentAccount.getPropertyDescriptors());
			this.instalmentAccountTable.setModel(bindingSource);

			for (int index = 0; index < this.accounts.length; index++)
			{
				Account account = accounts[index];
				PaymentMode paymentMode = PaymentMode.FullAmount;
				if(this.instalmentInfoList.containsKey(account.get_Id()))
				{
					InstalmentInfo instalmentInfo = this.instalmentInfoList.get(account.get_Id());
					if(instalmentInfo.isAdvancePayment())
					{
						paymentMode = PaymentMode.AdvancePayment;
					}
					else
					{
						paymentMode = PaymentMode.Instalment;
					}
				}
				BigDecimal lot = this.lots.get(account.get_Id());
				String lotStr = AppToolkit.getFormatLot(lot, account, instrument);

				bindingSource.add(new InstalmentAccount(paymentMode, account, lotStr));
			}
			this.instalmentAccountTable.sortColumn("AccountCode");

			this.instalmentAccountTable.addSelectedRowChangedListener(new ISelectedRowChangedListener()
			{
				public void selectedRowChanged(DataGrid source)
				{
					int row = instalmentAccountTable.getSelectedRow();
					if (row != -1)
					{
						row = TableModelWrapperUtils.getActualRowAt(instalmentAccountTable.getModel(), row);
						InstalmentAccount instalmentAccount = (InstalmentAccount)instalmentAccountTable.get_BindingSource().getObject(row);

						setCurrentAccount(instalmentAccount.get_Account(), instalmentAccount.get_PaymentMode());
					}
				}
			});
			this.instalmentAccountTable.changeSelection(0, 1, false, false);
		}
		else if(!this.isForExecutedOrder)
		{
			Account account = this.accounts[0];
			this.setCurrentAccount(account, this.paymentMode);
		}

		if(this.isForExecutedOrder)
		{
			this.periodChoice.setEnabled(false);
			this.instalmentTypeChoice.setEnabled(false);
			this.downPaymentField.setEnabled(false);
		}
		else
		{
			this.periodChoice.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if(stateChangedEventsEnabled) handlePeriodChanged(true);
				}
			});

			this.instalmentTypeChoice.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if(stateChangedEventsEnabled) handleInstalmentTypeChanged();
				}
			});

			this.downPaymentField.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					if(stateChangedEventsEnabled) handleDownPaymentChanged();
				}
			});

			this.recalculateRateTypeChoice.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					if(stateChangedEventsEnabled) tryCaculateAndUpdateUI();
				}
			});
		}
	}

	public void updatePrice()
	{
		if(this.isForExecutedOrder) return;

		if(this.limitPrice == null && this.stopPrice == null)
		{
			String livePrice = Price.toString(this.instrument.get_Quotation().getBuy());
			this.priceText.setText(livePrice);
		}

		this.tryCaculateAndUpdateUI();
	}

	private void handlePeriodChanged(boolean needRecaculate)
	{
		if(this.periodChoice.getSelectedIndex() < 0) return;

		InstalmentPeriod period = (InstalmentPeriod)this.periodChoice.getSelectedValue();
		this.currentInstalmentPolicyDetail = this.currentInstalmentPolicy.get_InstalmentPolicyDetail(period);
		this.interestRateText.setText(AppToolkit.format(this.currentInstalmentPolicyDetail.get_InterestRate(), 2));

		this.downPaymentField.setEnabled(true);
		double maxDownPayment = this.currentInstalmentPolicyDetail.get_MaxDownPayment().doubleValue();
		double minDownPayment = this.currentInstalmentPolicyDetail.get_MinDownPayment().doubleValue();
		if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
		{
			this.downPaymentField.setModel(new SpinnerNumberModel((double)(minDownPayment * 100), (double)(minDownPayment * 100), maxDownPayment * 100, 0.1));
			this.downPaymentPercentLable.setText("%");
		}
		else
		{
			this.downPaymentField.setModel(new SpinnerNumberModel(minDownPayment, minDownPayment, maxDownPayment, 1.0));
			this.downPaymentPercentLable.setText(InstalmentLanguage.PerLot);
		}
		if(needRecaculate) this.tryCaculateAndUpdateUI();
	}

	private void handleDownPaymentChanged()
	{
		this.tryCaculateAndUpdateUI();
	}

	private void handleInstalmentTypeChanged()
	{
		this.tryCaculateAndUpdateUI();
	}

	public void tryCaculateAndUpdateUI()
	{
		if (this.isForExecutedOrder)
			return;

		if (this.currentAccount == null)
			return;
		if (!this.lots.containsKey(this.currentAccount.get_Id()))
			return;
		BigDecimal lot = this.lots.get(this.currentAccount.get_Id());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
			return;

		if (this.currentInstalmentPolicyDetail == null)
		{
			if(this.currentPaymentMode.equals(PaymentMode.AdvancePayment))
			{
				this.currentInstalmentPolicyDetail = this.currentInstalmentPolicy.get_TillPayoffDetail();
			}
			else
			{
				return;
			}
		}
		boolean isInstalmentPaymentMode = this.currentPaymentMode.equals(PaymentMode.Instalment);
		InstalmentType instalmentType = this.currentPaymentMode.equals(PaymentMode.AdvancePayment) ? InstalmentType.EqualInstallment : null;
		if (isInstalmentPaymentMode)
		{
			instalmentType = this.getCurrentInstalmentType();
			if (instalmentType == null)
				return;
		}
		BigDecimal downPayment = this.getCurrentDownPayment();
		if (downPayment == null)
			return;

		InstalmentPeriod period
			= isInstalmentPaymentMode ? (InstalmentPeriod)this.periodChoice.getSelectedValue() : InstalmentPeriod.TillPayoffInstalmentPeriod;

		Price price = null;
		if (this.limitPrice == null && this.stopPrice == null)
		{
			price = this.instrument.get_LastQuotation().getBuy();
		}
		else if (this.limitPrice != null || this.limitPriceCheckbox.isSelected())
		{
			price = this.limitPrice;
		}
		else if (this.stopPrice != null || this.stopPriceCheckbox.isSelected())
		{
			price = this.stopPrice;
		}

		int decimals = this.instrument.get_Currency().get_Decimals();
		TradePolicyDetail tradePolicyDetail =
			this.settingsManager.getTradePolicyDetail(this.currentAccount.get_TradePolicyId(), this.instrument.get_Id());

		double oddDiscount = tradePolicyDetail.get_DiscountOfOdd().doubleValue();
		short tradePLFormula = this.instrument.get_TradePLFormula();
		BigDecimal marketValue
			= new BigDecimal(Order.caculateMarketValue(price, tradePLFormula, lot, tradePolicyDetail.get_ContractSize().doubleValue(), oddDiscount));
		marketValue = marketValue.setScale(decimals, RoundingMode.HALF_EVEN);

		BigDecimal fee = this.caculateFee(lot, marketValue).setScale(decimals, RoundingMode.HALF_EVEN);
		BigDecimal downPaymentAmount = BigDecimal.ZERO;

		if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
		{
			downPaymentAmount = downPayment.multiply(marketValue).setScale(decimals, RoundingMode.HALF_EVEN);
		}
		else if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.FixedAmountPerLot))
		{
			downPaymentAmount = downPayment.multiply(lot).setScale(decimals, RoundingMode.HALF_EVEN);
		}
		BigDecimal loanAmount = marketValue.subtract(downPaymentAmount);

		RecalculateRateType recalculateRateType = RecalculateRateType.NextMonth;
		if (isInstalmentPaymentMode)
		{
			recalculateRateType = (RecalculateRateType)this.recalculateRateTypeChoice.getSelectedValue();
		}

		if (this.instalmentInfoList.containsKey(this.currentAccount.get_Id()))
		{
			InstalmentInfo instalmentInfo = this.instalmentInfoList.get(this.currentAccount.get_Id());
			instalmentInfo.update(this.currentInstalmentPolicy.get_Id(), period, downPayment, instalmentType, recalculateRateType, fee, this.currentPaymentMode);
		}
		else
		{
			InstalmentInfo instalmentInfo
				= new InstalmentInfo(this.currentInstalmentPolicy.get_Id(), period, downPayment, instalmentType, recalculateRateType, fee, this.currentPaymentMode);
			this.instalmentInfoList.put(this.currentAccount.get_Id(), instalmentInfo);
		}

		if(isInstalmentPaymentMode)
		{
			InstalmentCaculateResult instalmentCaculateResult = null;
			if (instalmentType.value() == InstalmentType.EqualInstallment.value())
			{
				instalmentCaculateResult =
					InstalmentCaculator.CalculateEqualInstalment(loanAmount, period.get_Period(), this.currentInstalmentPolicyDetail.get_InterestRate(), decimals);
			}
			else if (instalmentType.value() == InstalmentType.EqualPrincipal.value())
			{
				instalmentCaculateResult =
					InstalmentCaculator.CalculateEqualPrincipal(loanAmount, period.get_Period(), this.currentInstalmentPolicyDetail.get_InterestRate(), decimals);
			}

			ArrayList<InstalmentDetail> instalmentDetails = instalmentCaculateResult.get_InstalmentDetails();
			this.bindInstalmentDetail(instalmentDetails);

			BigDecimal totalInstrest = BigDecimal.ZERO;
			for (InstalmentDetail instalmentDetail : instalmentDetails)
			{
				totalInstrest = totalInstrest.add(instalmentDetail.get_Interest());
			}
			this.totalPrincipal.setText(AppToolkit.format(loanAmount, decimals));
			this.totalInterest.setText(AppToolkit.format(totalInstrest, decimals));
			this.totalAmount.setText(AppToolkit.format(loanAmount.add(totalInstrest), decimals));
		}
		this.totalAmountText.setText(AppToolkit.format(marketValue, decimals));
		this.downPaymentAmountText.setText(AppToolkit.format(downPaymentAmount, decimals));
		this.loanAmountText.setText(AppToolkit.format(loanAmount, decimals));
		this.instalmentFeeText.setText(AppToolkit.format(fee, decimals));
	}

	private BigDecimal caculateFee(BigDecimal lot, BigDecimal marketValue)
	{
		AdministrationFeeBase administrationFeeBase
			= this.currentInstalmentPolicyDetail.get_AdministrationFeeBase();
		if(administrationFeeBase.equals(AdministrationFeeBase.PerLot))
		{
			return this.currentInstalmentPolicyDetail.get_AdministrationFee().multiply(lot);
		}
		else if(administrationFeeBase.equals(AdministrationFeeBase.PerValue))
		{
			return this.currentInstalmentPolicyDetail.get_AdministrationFee().multiply(marketValue);
		}
		else if(administrationFeeBase.equals(AdministrationFeeBase.LumpSum))
		{
			return this.currentInstalmentPolicyDetail.get_AdministrationFee();
		}
		else
		{
			throw new java.lang.IllegalArgumentException();
		}
	}

	private void bindInstalmentDetail(ArrayList<InstalmentDetail> instalmentDetails)
	{
		PropertyDescriptor[] propertyDescriptors
			= this.isForExecutedOrder ?  InstalmentDetail.getPropertyDescriptorsForExecutedOrder() : InstalmentDetail.getPropertyDescriptors();
		BindingSource bindingSource = new BindingSource();
		bindingSource.setBackground(Color.WHITE);
		TradingConsole.bindingManager.bind("InstalmentDetail", instalmentDetails == null ? new Vector() : instalmentDetails,
										   bindingSource, propertyDescriptors);
		this.instalmentDetailTable.setModel(bindingSource);
	}

	private InstalmentType getCurrentInstalmentType()
	{
		int selectedIndex = this.instalmentTypeChoice.getSelectedIndex();
		if(selectedIndex == -1) return null;
		return (InstalmentType)this.instalmentTypeChoice.getSelectedValue();
	}

	private BigDecimal getCurrentDownPayment()
	{
		double value = (Double)(this.downPaymentField.getValue());
		if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount)) value = value / 100.0;
		return new BigDecimal(value).setScale(3, RoundingMode.HALF_EVEN);
	}

	private boolean stateChangedEventsEnabled = true;
	private void disableStateChangedEvents()
	{
		stateChangedEventsEnabled = false;
	}

	private void enableStateChangedEvents()
	{
		stateChangedEventsEnabled = true;
	}

	private void adjustControlsStatus()
	{
		if(this.isForExecutedOrder) return;

		if(this.currentPaymentMode.equals(PaymentMode.FullAmount))
		{
			this.instalmentTypeChoice.setEnabled(false);
			this.periodChoice.setEnabled(false);
			this.recalculateRateTypeChoice.setEnabled(false);
			this.totalAmountText.setEnabled(false);
			this.downPaymentAmountText.setEnabled(false);
			this.loanAmountText.setEnabled(false);
			this.totalAmount.setEnabled(false);
			this.downPaymentField.setEnabled(false);
			this.instalmentFeeText.setEnabled(false);
			this.instalmentDetailTable.setEnabled(false);
			this.interestRateText.setEnabled(false);
			this.summaryLable.setEnabled(false);
			this.totalAmount.setEnabled(false);
			this.totalInterest.setEnabled(false);
			this.totalPrincipal.setEnabled(false);
		}
		else if(this.currentPaymentMode.equals(PaymentMode.Instalment))
		{
			this.instalmentTypeChoice.setEnabled(true);
			this.periodChoice.setEnabled(true);
			this.recalculateRateTypeChoice.setEnabled(true);
			this.totalAmountText.setEnabled(true);
			this.downPaymentAmountText.setEnabled(true);
			this.loanAmountText.setEnabled(true);
			this.totalAmount.setEnabled(true);
			this.downPaymentField.setEnabled(true);
			this.instalmentFeeText.setEnabled(true);
			this.interestRateText.setEnabled(true);
			this.summaryLable.setEnabled(true);
			this.totalAmount.setEnabled(true);
			this.totalInterest.setEnabled(true);
			this.totalPrincipal.setEnabled(true);


			this.downPaymentAmountLable.setText(InstalmentLanguage.DownPaymentAmount);
			this.loanAmountLable.setText(InstalmentLanguage.LoanAmount);

			if(this.currentInstalmentPolicyDetail != null)
			{
				if (this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.FixedAmountPerLot))
				{
					this.downPaymentPercentLable.setText(InstalmentLanguage.PerLot);
				}
				else if (this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
				{
					this.downPaymentPercentLable.setText("%");
				}
			}

		}
		else if(this.currentPaymentMode.equals(PaymentMode.AdvancePayment))
		{
			this.instalmentTypeChoice.setEnabled(false);
			this.periodChoice.setEnabled(false);
			this.recalculateRateTypeChoice.setEnabled(false);
			this.totalAmountText.setEnabled(true);
			this.downPaymentAmountText.setEnabled(true);
			this.loanAmountText.setEnabled(true);
			this.totalAmount.setEnabled(true);
			this.downPaymentField.setEnabled(true);
			this.instalmentFeeText.setEnabled(true);
			this.interestRateText.setEnabled(false);
			this.summaryLable.setEnabled(false);
			this.totalAmount.setEnabled(false);
			this.totalInterest.setEnabled(false);
			this.totalPrincipal.setEnabled(false);


			this.downPaymentAmountLable.setText(InstalmentLanguage.AdvanceAmount);
			this.loanAmountLable.setText(InstalmentLanguage.RemainAmount);
			if(this.currentInstalmentPolicyDetail != null)
			{
				if (this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.FixedAmountPerLot))
				{
					this.downPaymentPercentLable.setText(InstalmentLanguage.PerLot);
				}
				else if (this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
				{
					this.downPaymentPercentLable.setText("%");
				}
			}
		}
	}

	private void setCurrentAccount(Account account, PaymentMode paymentMode)
	{
		if (this.currentAccount == account && this.currentPaymentMode.equals(paymentMode))	return;

		disableStateChangedEvents();

		this.currentAccount = account;
		this.currentPaymentMode = paymentMode;
		this.currentInstalmentPolicy = null;
		this.currentInstalmentPolicyDetail = null;

		if(this.makeOrderAccounts != null)
		{
			MakeOrderAccount makeOrderAccount = this.makeOrderAccounts.get(account.get_Id());

			this.fullAmountRadioButton.setVisible(makeOrderAccount.get_CanFullPayment());
			this.advancePaymentRadioButton.setVisible(makeOrderAccount.get_CanAdvancePayment());
			this.instalmentRadioButton.setVisible(makeOrderAccount.get_CanInstalment());

			if (paymentMode.equals(PaymentMode.FullAmount))
			{
				this.fullAmountRadioButton.setSelected(true);
			}
			else if (paymentMode.equals(PaymentMode.AdvancePayment))
			{
				this.advancePaymentRadioButton.setSelected(true);
			}
			else if (paymentMode.equals(PaymentMode.Instalment))
			{
				this.instalmentRadioButton.setSelected(true);
			}
		}
		this.adjustControlsStatus();

		if (!this.isForExecutedOrder)
		{
			TradePolicyDetail tadePolicyDetail =
				this.settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this.instrument.get_Id());
			this.currentInstalmentPolicy = this.settingsManager.getInstalmentPolicy(tadePolicyDetail.get_InstalmentPolicyId());
		}

		InstalmentInfo instalmentInfo = null;
		if (this.instalmentInfoList.containsKey(account.get_Id()))
		{
			instalmentInfo = this.instalmentInfoList.get(account.get_Id());
		}
		boolean isInstalmentPaymentMode = this.currentPaymentMode.equals(PaymentMode.Instalment);

		int selectedIndex = 0;
		int index = 0;
		if (!this.isForExecutedOrder)
		{
			if(isInstalmentPaymentMode)
			{
				this.periodChoice.removeAllItems();
				for (InstalmentPeriod period : this.currentInstalmentPolicy.getActivePeriods())
				{
					if (instalmentInfo != null && instalmentInfo.get_Period().equals(period))
					{
						selectedIndex = index;
					}
					this.periodChoice.addItem(period.toString(), period);
					index++;
				}
				this.periodChoice.setSelectedIndex(selectedIndex);
				handlePeriodChanged(false);
			}
		}
		else
		{
			InstalmentPeriod period = instalmentInfo.get_Period();
			this.periodChoice.addItem(period.toString(), period);
			this.periodChoice.setSelectedIndex(0);
		}

		selectedIndex = 0;
		if (!this.isForExecutedOrder)
		{
			if(isInstalmentPaymentMode)
			{
				this.instalmentTypeChoice.removeAllItems();

				InstalmentType allowInstalmentTypes = this.currentInstalmentPolicy.get_AllowedInstalmentTypes();
				if (allowInstalmentTypes.value() == InstalmentType.All.value())
				{
					this.instalmentTypeChoice.addItem(InstalmentType.EqualInstallment.toLocalString(), InstalmentType.EqualInstallment);
					this.instalmentTypeChoice.addItem(InstalmentType.EqualPrincipal.toLocalString(), InstalmentType.EqualPrincipal);

					if (instalmentInfo != null && instalmentInfo.get_InstalmentType().value() == InstalmentType.EqualPrincipal.value())
					{
						selectedIndex = 1;
					}
				}
				else
				{
					this.instalmentTypeChoice.addItem(allowInstalmentTypes.toLocalString(), allowInstalmentTypes);
				}
				this.instalmentTypeChoice.setSelectedIndex(selectedIndex);
			}
		}
		else
		{
			InstalmentType instalmentType = instalmentInfo.get_InstalmentType();
			this.instalmentTypeChoice.addItem(instalmentType.toLocalString(), instalmentType);
			this.instalmentTypeChoice.setSelectedIndex(0);
		}

		selectedIndex = 0;
		if (!this.isForExecutedOrder)
		{
			if(isInstalmentPaymentMode)
			{
				this.recalculateRateTypeChoice.removeAllItems();

				RecalculateRateType allowRecalculateRateType = this.currentInstalmentPolicy.get_AllowedRecalculateRateTypes();
				if (allowRecalculateRateType.value() == RecalculateRateType.All.value())
				{
					this.recalculateRateTypeChoice.addItem(RecalculateRateType.NextMonth.toLocalString(), RecalculateRateType.NextMonth);
					this.recalculateRateTypeChoice.addItem(RecalculateRateType.NextYear.toLocalString(), RecalculateRateType.NextYear);

					if (instalmentInfo != null && instalmentInfo.get_RecalculateRateType().value() == RecalculateRateType.NextYear.value())
					{
						selectedIndex = 1;
					}
				}
				else
				{
					this.recalculateRateTypeChoice.addItem(allowRecalculateRateType.toLocalString(), allowRecalculateRateType);
				}
				this.recalculateRateTypeChoice.setSelectedIndex(selectedIndex);
			}
		}
		else
		{
			RecalculateRateType recalculateRateType = instalmentInfo.get_RecalculateRateType();
			this.recalculateRateTypeChoice.addItem(recalculateRateType.toLocalString(), recalculateRateType);
			this.recalculateRateTypeChoice.setSelectedIndex(0);
		}

		if(this.currentPaymentMode.equals(PaymentMode.AdvancePayment))
		{
			this.currentInstalmentPolicyDetail = this.currentInstalmentPolicy.get_TillPayoffDetail();
			double maxDownPayment = this.currentInstalmentPolicyDetail.get_MaxDownPayment().doubleValue();
			double minDownPayment = this.currentInstalmentPolicyDetail.get_MinDownPayment().doubleValue();
			if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
			{
				this.downPaymentField.setModel(new SpinnerNumberModel((double)(minDownPayment * 100), (double)(minDownPayment * 100), maxDownPayment * 100, 0.1));
				this.downPaymentPercentLable.setText("%");
			}
			else
			{
				this.downPaymentField.setModel(new SpinnerNumberModel(minDownPayment, minDownPayment, maxDownPayment, 1.0));
				this.downPaymentPercentLable.setText(InstalmentLanguage.PerLot);
			}
		}

		if (instalmentInfo != null)
		{
			if(instalmentInfo.isAdvancePayment())
			{
				this.currentInstalmentPolicyDetail = this.currentInstalmentPolicy.get_TillPayoffDetail();
			}
			else
			{
				this.currentInstalmentPolicyDetail = this.currentInstalmentPolicy.get_InstalmentPolicyDetail(instalmentInfo.get_Period());
			}

			double maxDownPayment = this.currentInstalmentPolicyDetail.get_MaxDownPayment().doubleValue();
			double minDownPayment = this.currentInstalmentPolicyDetail.get_MinDownPayment().doubleValue();
			if(this.currentInstalmentPolicyDetail.get_DownPaymentBasis().equals(DownPaymentBasis.PercentageOfAmount))
			{
				this.downPaymentField.setModel(new SpinnerNumberModel((double)(minDownPayment * 100), (double)(minDownPayment * 100), maxDownPayment * 100, 0.1));
				this.downPaymentField.setValue(instalmentInfo.get_DownPayment().doubleValue() * 100);
				this.downPaymentPercentLable.setText("%");
			}
			else
			{
				this.downPaymentField.setModel(new SpinnerNumberModel(minDownPayment, minDownPayment, maxDownPayment, 1.0));
				this.downPaymentField.setValue(instalmentInfo.get_DownPayment().doubleValue());
				this.downPaymentPercentLable.setText(InstalmentLanguage.PerLot);
			}
		}

		if (!this.isForExecutedOrder && instalmentInfo != null) this.tryCaculateAndUpdateUI();

		enableStateChangedEvents();
	}
}

class InstalmentCaculator
{
	private static final BigDecimal Twelve = new BigDecimal(12);

	static InstalmentCaculateResult CalculateEqualInstalment(BigDecimal loanAmount, int period, BigDecimal interestRate, int decimals)
	{
		BigDecimal principal = BigDecimal.ZERO, repaymentAmount = BigDecimal.ZERO;
		BigDecimal totalBaseAmount = BigDecimal.ZERO, totalInterest = BigDecimal.ZERO, totalAmount = BigDecimal.ZERO;

		BigDecimal rate = getMonthRate(interestRate);
		repaymentAmount = getRepaymentAmount(loanAmount, rate, period, decimals);
		ArrayList<InstalmentDetail> instalmentDetails = new ArrayList<InstalmentDetail>();
		for (int i = 1; i <= period; i++)
		{
			BigDecimal interest = (loanAmount.multiply(rate));
			interest = interest.setScale(decimals, RoundingMode.HALF_EVEN);
			principal = repaymentAmount.subtract(interest);
			loanAmount = loanAmount.subtract(principal);
			if (i == period && loanAmount.compareTo(BigDecimal.ZERO) != 0)
			{
				principal = principal.add(loanAmount);
				//interest = interest.subtract(loanAmount);
				loanAmount = BigDecimal.ZERO;
			}
			totalAmount = totalAmount.add(repaymentAmount);
			totalBaseAmount = totalBaseAmount.add(principal);
			totalInterest = totalInterest.add(interest);

			InstalmentDetail instalmentDetail = new InstalmentDetail(i, principal, interest, decimals);
			instalmentDetails.add(instalmentDetail);
		}
		return new InstalmentCaculateResult(totalAmount, totalBaseAmount, totalInterest, instalmentDetails);
	}

	static InstalmentCaculateResult CalculateEqualPrincipal(BigDecimal loanAmount, int period, BigDecimal interestRate, int decimals)
	{
		BigDecimal principal = BigDecimal.ZERO, interest = BigDecimal.ZERO,
			totalAmount = BigDecimal.ZERO, totalBaseAmount = BigDecimal.ZERO, totalInterest = BigDecimal.ZERO;
		BigDecimal repaymentAmount = BigDecimal.ZERO;
		principal = loanAmount.divide(new BigDecimal(period), decimals, RoundingMode.HALF_EVEN);

		BigDecimal rate = getMonthRate(interestRate);
		ArrayList<InstalmentDetail> instalmentDetails = new ArrayList<InstalmentDetail>();
		for (int i = 1; i <= period; i++)
		{
			interest = loanAmount.multiply(rate).setScale(decimals, RoundingMode.HALF_EVEN); //月利息
			loanAmount = loanAmount.subtract(principal).setScale(decimals, RoundingMode.HALF_EVEN); //剩余本金
			if (i == period && loanAmount.compareTo(BigDecimal.ZERO) != 0)
			{
				principal = principal.add(loanAmount).setScale(decimals, RoundingMode.HALF_EVEN);
				interest = interest.subtract(loanAmount).setScale(decimals, RoundingMode.HALF_EVEN);
				loanAmount = BigDecimal.ZERO;
			}

			repaymentAmount = principal.add(interest).setScale(decimals, RoundingMode.HALF_EVEN);

			totalAmount = totalAmount.add(repaymentAmount);
			totalBaseAmount = totalBaseAmount.add(principal);
			totalInterest = totalInterest.add(interest);

			InstalmentDetail instalmentDetail = new InstalmentDetail(i, principal, interest, decimals);
			instalmentDetails.add(instalmentDetail);
		}
		return new InstalmentCaculateResult(totalAmount, totalBaseAmount, totalInterest, instalmentDetails);
	}

	static BigDecimal getMonthRate(BigDecimal interestRate)
	{
		return interestRate.divide(InstalmentCaculator.Twelve, 8, RoundingMode.HALF_EVEN);
	}

	static BigDecimal getRepaymentAmount(BigDecimal balance, BigDecimal rate, int installments, int decimals)
	{
		double repaymentAmount = 0;
		if(rate.compareTo(BigDecimal.ZERO) == 0)
		{
			repaymentAmount = balance.doubleValue() / installments;
		}
		else
		{
			double tmp = Math.pow(1 + rate.doubleValue(), installments);
			repaymentAmount = (balance.doubleValue() * rate.doubleValue() * tmp / (tmp - 1));
		}
		return new BigDecimal(repaymentAmount).setScale(decimals, RoundingMode.HALF_EVEN);
	}
}

class InstalmentCaculateResult
{
	private BigDecimal totalAmount;
	private BigDecimal totalPrincipal;
	private BigDecimal totalInterest;
	private ArrayList<InstalmentDetail> instalmentDetails;

	public InstalmentCaculateResult(BigDecimal totalAmount, BigDecimal totalPrincipal, BigDecimal totalInterest,
									ArrayList<InstalmentDetail> instalmentDetails)
	{
		this.totalAmount = totalAmount;
		this.totalPrincipal = totalPrincipal;
		this.totalInterest = totalInterest;
		this.instalmentDetails = instalmentDetails;
	}

	public BigDecimal get_TotalAmount()
	{
		return this.totalAmount;
	}

	public BigDecimal get_TotalPrincipal()
	{
		return this.totalPrincipal;
	}

	public BigDecimal get_TotalInterest()
	{
		return this.totalInterest;
	}

	public ArrayList<InstalmentDetail> get_InstalmentDetails()
	{
		return this.instalmentDetails;
	}
}
