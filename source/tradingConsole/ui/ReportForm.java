package tradingConsole.ui;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.awt.Dialog;
import java.io.ByteArrayInputStream;

import PdfViewer.PdfViewer;
import framework.xml.XmlConvert;
import framework.DateTime;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.ui.language.Language;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.TradingConsoleServer;
import tradingConsole.settings.Parameter;
import tradingConsole.ui.colorHelper.FormBackColor;
import java.io.IOException;
import java.util.Date;
import framework.Guid;
import tradingConsole.IAsyncCommandListener;
import framework.diagnostics.TraceType;
import javax.swing.Timer;
import javax.swing.JProgressBar;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JFormattedTextField;
import javax.swing.JComboBox;
import java.text.DateFormat;
import com.jidesoft.swing.JideSwingUtilities;
import javax.swing.SwingUtilities;
import com.jidesoft.combobox.DateComboBox;
import tradingConsole.settings.PublicParametersManager;
import java.util.Arrays;
import PdfViewer.ISettingsProvider;
import tradingConsole.Log;
import tradingConsole.enumDefine.LogCode;
import framework.StringHelper;
import javax.swing.JRadioButton;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Calendar;
import framework.TimeSpan;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.net.URL;
import java.net.URI;
import org.apache.log4j.Logger;

public class ReportForm extends JDialog implements IAsyncCommandListener, ActionListener
{
	private Logger logger = Logger.getLogger(ReportForm.class);
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Guid _asyncResultId;
	private Timer _timer;
	private JProgressBar _progressBar;
	private static SettingsProvider settingsProvider = new SettingsProvider();

	private JRadioButton todayRadioButton = new JRadioButton();
	private JRadioButton thisWeekRadioButton = new JRadioButton();
	private JRadioButton thisMonthRadioButton = new JRadioButton();

	public ReportForm(JFrame parent, TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		super(parent, true);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		this.setTitle(Language.Reports);
		this.reportTypeStaticText.setText(Language.ReportType);
		this.accountStaticText.setText(Language.ReportAccountCode);
		this.viewButton.setText(Language.ReportViewCaption);
		this.reportTypeStaticText.setAlignment(2);
		this.accountStaticText.setAlignment(2);
		this.dateFromStaticText.setAlignment(2);
		this.dateToStaticText.setAlignment(2);

		this.reportTypeChoice.setEditable(false);
		this.accountChoice.setEditable(false);

		this.fillReportType();
		this.fillAccount();

		if (this.reportTypeChoice.getItemCount() > 0)
		{
			this.reportTypeChoice.setSelectedIndex(0);
		}
		if (this.accountChoice.getItemCount() > 0)
		{
			this.accountChoice.setSelectedIndex(0);
		}
		this.reportTypeChoice_OnChange();
		this._tradingConsole.addAsyncCommandListener(this);
		this._asyncResultId = Guid.empty;
		this._timer = new Timer(1000, this);
	}

	private void fillReportType()
	{
		this.reportTypeChoice.removeAllItems();
		this.reportTypeChoice.addItem(Language.ReportTypeStatement);
		if (this._settingsManager.get_Customer().get_IsDisplayLedger())
		{
			this.reportTypeChoice.addItem(Language.ReportTypeLedger);
		}
		if(this._settingsManager.get_Customer().get_IsEmployee())
		{
			this.reportTypeChoice.addItem(Language.ReportTypeAccountSummary);
		}
	}

	private void fillAccount()
	{
		this.accountChoice.removeAllItems();
		Account[] accounts = new Account[this._settingsManager.get_Accounts().values().size()];
		accounts = this._settingsManager.get_Accounts().values().toArray(accounts);
		Arrays.sort(accounts, Account.comparatorByCode);

		for (Account account : accounts)
		{
			boolean verifiedCustomerIdentity = account.get_VerifiedCustomerIdentity();
			if (verifiedCustomerIdentity)
			{
				this.accountChoice.addItem(account.get_Code());
			}
		}
	}

	//this method will not need if modify accountChoise item???????
	private Account getAccount(String accountCode)
	{
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Code().equals(accountCode))
			{
				return account;
			}
		}
		return null;
	}

	private void viewReport(String url)
	{
		int i = this.reportTypeChoice.getSelectedIndex();
		if (i < 0)
		{
			return;
		}
		String reportType = this.reportTypeChoice.getItemAt(i).toString();
		i = this.accountChoice.getSelectedIndex();
		if (i < 0)
		{
			return;
		}

		Date from = this.dateFromDate.getDate();
		Date to = this.dateToDate.getDate();
		if (from == null || to == null)
		{
			return;
		}

		DateTime dateFrom = DateTime.fromDate(from);
		String dateFromStr = dateFrom.toString("yyyy-MM-dd");
		DateTime dateTo = DateTime.fromDate(to);
		String dateToStr = dateTo.toString("yyyy-MM-dd");

		String accountCode = this.accountChoice.getItemAt(i).toString();
		Account account = this.getAccount(accountCode);
		if (account == null)
		{
			return;
		}
		String accountIdString = account.get_Id().toString();

		String companyCode = this._tradingConsole.get_LoginInformation().get_CompanyName();
		String userId = this._tradingConsole.get_LoginInformation().get_CustomerId().toString();

		String language = PublicParametersManager.version.toLowerCase();
		if (reportType.equals(Language.ReportTypeStatement))
		{
			url = url +"?companyName="+companyCode+"&reporttype=statement&accountId="+accountIdString
				+"&tradeDayBegin="+dateFromStr+"&tradeDayEnd="+dateToStr+"&language="+language+"&userID="+userId+"&runModel=1";
			String info = StringHelper.format("{0}: Account={1}, DatTime={2}", new Object[]
											  {Language.ReportTypeStatement, accountCode, XmlConvert.toString(dateFrom, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.Statement, info, null, account.get_Id());

		}
		else if (reportType.equals(Language.ReportTypeLedger))
		{
			url = url +"?companyName="+companyCode+"&reporttype=ledger&accountId="+accountIdString+"&tradeDayBegin="+dateFromStr+
				"&tradeDayEnd="+dateToStr+"&language="+language+"&userID="+userId+"&runModel=1";

			String info = StringHelper.format("{0}: Account={1}, From={2}, To={3}",
											  new Object[]
											  {Language.ReportTypeLedger, accountCode, XmlConvert.toString(dateFrom, "yyyy/MM/dd"),
											  XmlConvert.toString(dateTo, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.Ledger, info, null, account.get_Id());

		}
		else if (reportType.equals(Language.ReportTypeAccountSummary))
		{
			url = url +"?companyName="+companyCode+"&reporttype=accountsummary&accountId="+accountIdString+"&tradeDay="
				+dateFromStr+"&language="+language+"&userID="+userId+"&runModel=1";

			String info = StringHelper.format("{0}: Account={1}, TradeDay={2}",
											  new Object[]
											  {Language.ReportTypeAccountSummary, accountIdString, XmlConvert.toString(dateFrom, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.AccountSummary, info, null, account.get_Id());
		}

		try
		{
			//Runtime.getRuntime().exec("C:\\Program Files\\Internet Explorer\\iexplore.exe -k " + url);
			String command = "C:\\Program Files\\Internet Explorer\\iexplore.exe " + url;
			Runtime.getRuntime().exec("Elevate.exe " + command);
		}
		catch(Exception ex)
		{
			try
			{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				desktop.browse(new URI(url));
			}
			catch(Exception ex2)
			{
				this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
			}
		}
		//showUrl(flashReportUrl);
	}

	private boolean view()
	{
		int i = this.reportTypeChoice.getSelectedIndex();
		if (i < 0)
		{
			return false;
		}
		String reportType = this.reportTypeChoice.getItemAt(i).toString();
		i = this.accountChoice.getSelectedIndex();
		if (i < 0)
		{
			return false;
		}

		Date from = this.dateFromDate.getDate();
		Date to = this.dateToDate.getDate();
		if (from == null || to == null)
		{
			return false;
		}

		DateTime dateFrom = DateTime.fromDate(from);
		DateTime dateTo = DateTime.fromDate(to);

		String accountCode = this.accountChoice.getItemAt(i).toString();
		Account account = this.getAccount(accountCode);
		if (account == null)
		{
			return false;
		}
		String accountIdString = account.get_Id().toString();

		String companyCode = this._tradingConsole.get_LoginInformation().get_CompanyName();
		String reportxml = companyCode + "/";

		byte[] reportData = null;
		Guid asyncResultId = Guid.empty;

		if (reportType.equals(Language.ReportTypeStatement))
		{
			if (dateFrom == null || dateTo == null)
			{
				return false;
			}

			if (account.get_IsMultiCurrency())
			{
				reportxml += Parameter.statementMCXML;
			}
			else
			{
				switch (Parameter.statementReportType)
				{
					case 0:
						reportxml += Parameter.statementXML;
						break;
					case 1:
						reportxml += Parameter.statement2XML;
						break;
					case 2:
						reportxml += Parameter.statement3XML;
						break;
					case 3:
						reportxml += Parameter.statement5XML;
						break;
					default:
						reportxml += Parameter.statementXML;
				}
			}
			//this._tradingConsole.get_TradingConsoleServer().statementForJava(Parameter.statementReportType,
			//XmlConvert.toString(dateFrom, "yyyy/MM/dd"), "{" + accountIdString + "}", reportxml);
			reportxml = reportxml.substring(0, reportxml.length() - 4) + ".rdlc";
			asyncResultId = this._tradingConsole.get_TradingConsoleServer().statementForJava2(Parameter.statementReportType,
				XmlConvert.toString(dateFrom, "yyyy/MM/dd"), XmlConvert.toString(dateTo, "yyyy/MM/dd"), "{" + accountIdString + "}", reportxml);

			String info = StringHelper.format("{0}: Account={1}, DatTime={2}", new Object[]
											  {Language.ReportTypeStatement, accountCode, XmlConvert.toString(dateFrom, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.Statement, info, null, account.get_Id());
		}
		else if (reportType.equals(Language.ReportTypeLedger))
		{
			if (dateFrom == null || dateTo == null)
			{
				return false;
			}
			reportxml += Parameter.ledgerXML;
			//this._tradingConsole.get_TradingConsoleServer().ledgerForJava(XmlConvert.toString(dateFrom, "yyyy/MM/dd"),
			//	XmlConvert.toString(dateTo, "yyyy/MM/dd"), "{" + accountIdString + "}", reportxml);
			reportxml = reportxml.substring(0, reportxml.length() - 4) + ".rdlc";
			asyncResultId = this._tradingConsole.get_TradingConsoleServer().ledgerForJava2(XmlConvert.toString(dateFrom, "yyyy/MM/dd"),
				XmlConvert.toString(dateTo, "yyyy/MM/dd"), "{" + accountIdString + "}", reportxml);

			String info = StringHelper.format("{0}: Account={1}, From={2}, To={3}",
											  new Object[]
											  {Language.ReportTypeLedger, accountCode, XmlConvert.toString(dateFrom, "yyyy/MM/dd"),
											  XmlConvert.toString(dateTo, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.Ledger, info, null, account.get_Id());
		}
		else if (reportType.equals(Language.ReportTypeAccountSummary))
		{
			if (dateFrom == null)
			{
				return false;
			}
			reportxml += Parameter.accountSummaryRDLC;
			StringBuilder sb = new StringBuilder();
			for(Account item : this._settingsManager.get_Accounts().values())
			{
				if(item.get_Select())
				{
					if(sb.length() > 0) sb.append(",");
					sb.append(item.get_Id().toString());
				}
			}
			accountIdString = sb.toString();
			asyncResultId = this._tradingConsole.get_TradingConsoleServer().accountSummaryForJava2(XmlConvert.toString(dateFrom, "yyyy/MM/dd"),
				accountIdString, reportxml);

			String info = StringHelper.format("{0}: TradeDay={1}",
											  new Object[]
											  {Language.ReportTypeAccountSummary, XmlConvert.toString(dateFrom, "yyyy/MM/dd")});
			this._tradingConsole.saveLog(LogCode.AccountSummary, info, null);
		}

		if (asyncResultId == Guid.empty)
		{
			this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
			return false;
		}
		else
		{
			this._asyncResultId = asyncResultId;
		}

		return true;
	}

	public void showPDF(byte[] bytes)
	{
		ByteArrayInputStream stream = null;
		try
		{
			TradingConsole.traceSource.trace(TraceType.Information, "------------------------------------------------------------------");
			TradingConsole.traceSource.trace(TraceType.Information, "Length of pdf report = " + bytes.length);
			StringBuilder sb = new StringBuilder();
			for (int index = 0; index < bytes.length; index++)
			{
				sb.append(bytes[index] + " ");
			}
			TradingConsole.traceSource.trace(TraceType.Information, sb.toString());
			TradingConsole.traceSource.trace(TraceType.Information, "------------------------------------------------------------------");
			stream = new ByteArrayInputStream(bytes);
			PdfViewer pdfViewer = new PdfViewer(ReportForm.settingsProvider);
			pdfViewer.Open(stream);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(AppToolkit.get_ScreenSize());
			String title = this.reportTypeChoice.getSelectedItem().toString() + "-" + this.accountChoice.getSelectedItem().toString();
			JDialog dialog = new JDialog(this, title);
			int width = (int) (rectangle.getWidth() * 0.8);
			width = pdfViewer.GetPageWidth() > width ? width : pdfViewer.GetPageWidth();
			int height = (int) (rectangle.getHeight() * 0.8);
			height = pdfViewer.GetPageHeight() > height ? height : pdfViewer.GetPageHeight();

			dialog.setSize(width, height);
			JideSwingUtilities.centerWindow(dialog);
			dialog.add(pdfViewer);
			dialog.setVisible(true);
			dialog.show();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
		}
		finally
		{
			try
			{
				if (stream != null)
				{
					stream.close();
				}
			}
			catch (IOException exception)
			{
				exception.printStackTrace();
				this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
			}
		}
	}

	private void showUrl(String url)
	{
		try
		{
			JTextPane tp = new JTextPane();
			JScrollPane js = new JScrollPane();
			js.getViewport().add(tp);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(AppToolkit.get_ScreenSize());
			String title = this.reportTypeChoice.getSelectedItem().toString() + "-" + this.accountChoice.getSelectedItem().toString();
			JDialog dialog = new JDialog(this, title);
			int width = (int) (rectangle.getWidth() * 0.8);
			int height = (int) (rectangle.getHeight() * 0.8);

			dialog.setSize(width, height);
			JideSwingUtilities.centerWindow(dialog);
			dialog.add(js);
			dialog.setVisible(true);
			dialog.show();
			URL url2 = new URL(url);
			tp.setPage(url2);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
		}
	}


	/*
	 private void displayReport()
	 {
	  String url = StringHelper.replace(this._tradingConsole.get_ServiceManager().get_ServiceUrl(), "Service.asmx", "");
	  url = AppToolkit.changeUrlProtocol(url,"http");
	  url += "Inner/ReportForJava.aspx"
	   + "?parameter=ReportForJava?userId=" + this._settingsManager.get_Customer().get_UserId().toString();
	  int mapPort = this._tradingConsole.get_ServiceManager().getMapPort();
	  url = AppToolkit.changeToMapPort(url,mapPort);
	  BrowserControl.displayURL(url,true);
	 }
	 */

	private void reportTypeChoice_OnChange()
	{
		boolean isForEVGREEN = false;

		int i = this.reportTypeChoice.getSelectedIndex();
		if (i < 0)
		{
			return;
		}
		//DateTime appTime = TradingConsoleServer.appTime();
		DateTime tradeDay = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay();
		this.dateFromDate.setDate(tradeDay.toDate());
		this.dateToDate.setDate(tradeDay.toDate());
		String reportType = this.reportTypeChoice.getItemAt(i).toString();
		if (reportType.equals(Language.ReportTypeStatement))
		{
			if(isForEVGREEN)
			{
				this.dateToStaticText.setVisible(false);
				this.dateToDate.setVisible(false);
				this.todayRadioButton.setVisible(false);
				this.thisWeekRadioButton.setVisible(false);
				this.thisMonthRadioButton.setVisible(false);
			}
			this.dateFromStaticText.setText(Language.ReportDate1InnerTextForStatement);
			this.dateToStaticText.setText(Language.ReportDate2InnerTextForLedger);
			this.dateToDate.setEnabled(true);
			this.accountChoice.setEnabled(true);
			this.todayRadioButton.setEnabled(true);
			this.thisWeekRadioButton.setEnabled(true);
			this.thisMonthRadioButton.setEnabled(true);
		}
		else if (reportType.equals(Language.ReportTypeLedger))
		{
			if(isForEVGREEN)
			{
				this.dateToStaticText.setVisible(true);
				this.dateToDate.setVisible(true);
				this.todayRadioButton.setVisible(true);
				this.thisWeekRadioButton.setVisible(true);
				this.thisMonthRadioButton.setVisible(true);
			}

			this.dateFromStaticText.setText(Language.ReportDate1InnerTextForLedger);
			this.dateToStaticText.setText(Language.ReportDate2InnerTextForLedger);
			this.dateToDate.setEnabled(true);
			this.accountChoice.setEnabled(true);
			this.todayRadioButton.setEnabled(true);
			this.thisWeekRadioButton.setEnabled(true);
			this.thisMonthRadioButton.setEnabled(true);
		}
		else if (reportType.equals(Language.ReportTypeAccountSummary))
		{
			this.dateFromStaticText.setText(Language.ReportDate1InnerTextForStatement);
			this.dateToDate.setEnabled(false);
			this.accountChoice.setEnabled(false);
			this.todayRadioButton.setEnabled(false);
			this.thisWeekRadioButton.setEnabled(false);
			this.thisMonthRadioButton.setEnabled(false);
			this.todayRadioButton.setSelected(true);
		}

		this.doLayout();
	}

	private void accountChoice_OnChange()
	{
	}

	//SourceCode End////////////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new ReportForm_this_windowAdapter(this));

		this.setSize(340, 240);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle("Report");
		this.setBackground(FormBackColor.reportForm);

		reportTypeStaticText.setText("Report Type");
		accountStaticText.setText("Account");
		dateFromStaticText.setText("From");
		this._progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		this._progressBar.setVisible(false);
		viewButton.addActionListener(new ReportForm_viewButton_actionAdapter(this));
		reportTypeChoice.addActionListener(new ReportForm_reportTypeChoice_actionAdapter(this));
		accountChoice.addActionListener(new ReportForm_accountChoice_actionAdapter(this));
		dateToStaticText.setText("To");
		this.dateFromDate.setLocale(PublicParametersManager.getLocal());
		this.dateFromDate.setShowTodayButton(false);
		this.dateFromDate.setShowNoneButton(false);

		this.dateToDate.setLocale(PublicParametersManager.getLocal());
		this.dateToDate.setShowTodayButton(false);
		this.dateToDate.setShowNoneButton(false);

		todayRadioButton.setText(Language.today);
		thisWeekRadioButton.setText(Language.thisWeek);
		thisMonthRadioButton.setText(Language.thisMonth);

		this.add(reportTypeStaticText, new GridBagConstraints2(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 1, 5), 0, 0));
		this.add(accountStaticText, new GridBagConstraints2(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 10, 1, 5), 0, 0));
		this.add(dateFromStaticText, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 10, 1, 5), 0, 0));
		this.add(dateToStaticText, new GridBagConstraints2(0, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 10, 1, 5), 0, 0));

		this.getContentPane().add(reportTypeChoice, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 1, 10), 24, 0));
		this.getContentPane().add(accountChoice, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 10), 0, 0));
		this.getContentPane().add(dateFromDate, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 10), 26, 0));
		this.getContentPane().add(dateToDate, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 10), 26, 0));
		dateFromDate.setDate(null);

		dateFromDate.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				Date tradeDay = _tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay().get_Date();
				if(dateFromDate.getDate().after(tradeDay))
				{
					dateFromDate.setDate(tradeDay);
				}
				fillDate();
			}
		});
		dateToDate.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (dateToDate.getDate().after(_maxAllowDate))
				{
					dateToDate.setDate(_maxAllowDate);
				}
				else if(dateToDate.getDate().before(dateFromDate.getDate()))
				{
					dateToDate.setDate(dateFromDate.getDate());
				}
			}
		});
		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(this.todayRadioButton);
		radioGroup.add(this.thisWeekRadioButton);
		radioGroup.add(this.thisMonthRadioButton);
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.add(this.todayRadioButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 3, 10), 20, 0));
		panel.add(this.thisWeekRadioButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 3, 10), 20, 0));
		panel.add(this.thisMonthRadioButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 3, 10), 20, 0));

		RadioButtonHandler handler = new RadioButtonHandler();
		this.todayRadioButton.addItemListener(handler);
		this.thisWeekRadioButton.addItemListener(handler);
		this.thisMonthRadioButton.addItemListener(handler);
		this.todayRadioButton.setSelected(true);

		this.getContentPane().add(panel, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 3, 10), 20, 0));

		this.getContentPane().add(viewButton, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 100, 3, 100), 20, 0));

		this.add(this._progressBar, new GridBagConstraints2(0, 7, 2, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 0, 10), 0, 0));
	}

	private Date _maxAllowDate = null;
	private void fillDate()
	{
		if (this.todayRadioButton.isSelected())
		{
			if (this.dateFromDate.getDate() == null)
			{
				DateTime tradeDay = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay();
				this._maxAllowDate = tradeDay.toDate();
				this.dateFromDate.setDate(this._maxAllowDate);
				this.dateToDate.setDate(this._maxAllowDate);
			}
			else
			{
				this._maxAllowDate = this.dateFromDate.getDate();
				this.dateToDate.setDate(this._maxAllowDate);
			}
			this.dateToDate.setEnabled(false);
		}
		else if (this.thisWeekRadioButton.isSelected())
		{
			TimeSpan oneDay = TimeSpan.fromDays(1);
			TimeSpan twoDay = TimeSpan.fromDays(2);
			Date now = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay().get_Date();
			if (this.dateFromDate.getDate() != null) now = this.dateFromDate.getDate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(now);
			int week = calendar.get(Calendar.WEEK_OF_YEAR);
			DateTime end = DateTime.fromDate(now);
			if(calendar.getFirstDayOfWeek() != Calendar.SUNDAY || calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			{
				while (true)
				{
					end = end.add(oneDay);
					calendar.setTime(end);
					if (week != calendar.get(Calendar.WEEK_OF_YEAR))
					{
						break;
					}
				}
				end = calendar.getFirstDayOfWeek() == Calendar.SUNDAY ? end : end.substract(oneDay);
				if(end.after(this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay())) end = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay();
			}

			if (this.dateFromDate.getDate() == null) this.dateFromDate.setDate(now);
			this._maxAllowDate = end;
			this.dateToDate.setDate(end);
			this.dateToDate.setEnabled(true);
		}
		else if (this.thisMonthRadioButton.isSelected())
		{
			DateTime dateTime = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay();
			if (this.dateFromDate.getDate() != null) dateTime = DateTime.fromDate(this.dateFromDate.getDate());
			DateTime end = new DateTime(dateTime.getYear() + 1900, dateTime.getMonth() + 1, 1);
			end = end.addMonths(1);
			end = end.addDays(-1);
			if(end.after(this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay())) end = this._tradingConsole.get_SettingsManager().get_TradeDay().get_TradeDay();
			if (this.dateFromDate.getDate() == null) this.dateFromDate.setDate(dateTime.get_Date());
			this._maxAllowDate = end.toDate();
			this.dateToDate.setDate(end.toDate());
			this.dateToDate.setEnabled(true);
		}
	}

	public void this_windowClosing(WindowEvent e)
	{
		this._tradingConsole.removeAsyncCommandListener(this);
		this.dispose();
	}

	private PVStaticText2 reportTypeStaticText = new PVStaticText2();
	private PVStaticText2 accountStaticText = new PVStaticText2();
	private PVStaticText2 dateFromStaticText = new PVStaticText2();
	private JComboBox reportTypeChoice = new JComboBox();
	private JComboBox accountChoice = new JComboBox();
	private DateComboBox dateFromDate = new DateComboBox();
	private PVButton2 viewButton = new PVButton2();
	private PVStaticText2 dateToStaticText = new PVStaticText2();
	private DateComboBox dateToDate = new DateComboBox();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private String fileToExecute;

	public void viewButton_actionPerformed(ActionEvent e)
	{
		if (this.viewButton.getText().compareToIgnoreCase(Language.ReportViewCaption) == 0)
		{
			if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_useFlashReportInJava())
			{
				String flashReportUrl =
					this._tradingConsole.get_ServiceManager().get_ServiceUrl().toLowerCase().replaceFirst("tradingconsole/service.asmx",
					"TradingConsoleSLReport/ReportViewer.aspx");
				this.viewReport(flashReportUrl);
			}
			else
			{
				if (this.view())
				{
					this.viewButton.setText(Language.OrderPlacementbtnCancel);
					this._progressBar.setVisible(true);
					this._timer.start();
				}
				else
				{
					this._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
				}
			}
		}
		else
		{
			this.viewButton.setText(Language.ReportViewCaption);
			this._asyncResultId = Guid.empty;
			this._progressBar.setVisible(false);
			this._progressBar.setValue(0);
			this._timer.stop();
		}
		this.doLayout();
	}

	public void reportTypeChoice_actionPerformed(ActionEvent e)
	{
		this.reportTypeChoice_OnChange();
	}

	public void accountChoice_actionPerformed(ActionEvent e)
	{
		this.accountChoice_OnChange();
	}

	public void asyncCommandCompleted(Guid asyncResultId, String methodName, boolean failed, String errorMessage)
	{
		SwingUtilities.invokeLater(new AwtSafePdfViewer(this, asyncResultId, methodName, failed, errorMessage));
	}

	public Guid getAsyncResultId()
	{
		return this._asyncResultId;
	}

	public void actionPerformed(ActionEvent e)
	{
		int oldValue = this._progressBar.getValue();
		int value = oldValue == 100 ? 0 : oldValue + 10;
		this._progressBar.setValue(value);
	}

	class ReportForm_this_windowAdapter extends WindowAdapter
	{
		private ReportForm adaptee;
		ReportForm_this_windowAdapter(ReportForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	private static class SettingsProvider implements ISettingsProvider
	{
		public String getConfigPath()
		{
			return AppToolkit.get_SettingDirectory();
		}
	}

	private class RadioButtonHandler implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			fillDate();
		}
	}

	static class AwtSafePdfViewer implements Runnable
	{
		private Guid _asyncResultId;
		private ReportForm _reportForm;
		private String _methodName;
		private boolean _failed;
		private String _exception;

		AwtSafePdfViewer(ReportForm reportForm, Guid asyncResultId, String methodName, boolean failed, String exception)
		{
			this._reportForm = reportForm;
			this._asyncResultId = asyncResultId;
			this._methodName = methodName;
			this._failed = failed;
			this._exception = exception;
		}

		public void run()
		{
			if (this._failed)
			{
				this._reportForm._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
				TradingConsole.traceSource.trace(TraceType.Error, this._exception);
			}
			else
			{
				byte[] reportContent = this._reportForm._tradingConsole.get_TradingConsoleServer().getReportContent(this._asyncResultId);
				if (reportContent == null)
				{
					this._reportForm._tradingConsole.messageNotify(Language.FailedToLoadReport, false);
				}
				else if (this._reportForm._asyncResultId.equals(this._asyncResultId))
				{
					this._reportForm.showPDF(reportContent);
				}
			}
			this._reportForm._timer.stop();
			this._reportForm._progressBar.setVisible(false);
			this._reportForm._progressBar.setValue(0);
			this._reportForm.viewButton.setText(Language.ReportViewCaption);
			this._reportForm.doLayout();
		}
	}
}

class ReportForm_accountChoice_actionAdapter implements ActionListener
{
	private ReportForm adaptee;
	ReportForm_accountChoice_actionAdapter(ReportForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.accountChoice_actionPerformed(e);
	}
}

class ReportForm_reportTypeChoice_actionAdapter implements ActionListener
{
	private ReportForm adaptee;
	ReportForm_reportTypeChoice_actionAdapter(ReportForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.reportTypeChoice_actionPerformed(e);
	}
}

class ReportForm_viewButton_actionAdapter implements ActionListener
{
	private ReportForm adaptee;
	ReportForm_viewButton_actionAdapter(ReportForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.viewButton_actionPerformed(e);
	}
}
