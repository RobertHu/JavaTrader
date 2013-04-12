package tradingConsole.ui;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.ActionListener;

import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.ui.language.ActivateAccount;
import tradingConsole.service.UpdatePasswordResult;
import framework.diagnostics.TraceType;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import java.util.Enumeration;
import javax.swing.table.TableColumn;
import javax.swing.JScrollPane;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.language.Language;

public class RecoverPasswordQuotationSettingForm extends JDialog//Frame
{
	private ActivateAccountForm _activateAccountForm;
	private TradingConsole _tradingConsole;
	private String _currentPassword;
	private String _newPassword;

/*
	public RecoverPasswordQuotationSettingForm()
	   {
	  super();
		try
		{
		 jbInit();

		}
		catch (Throwable exception)
		{
		 exception.printStackTrace();
		}

	   }
*/

	  public RecoverPasswordQuotationSettingForm(JFrame parent)
	  {
		  super(parent, true);
		  try
		  {
			  jbInit();

			  Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			  this.setBounds(rectangle);
		  }
		  catch (Throwable exception)
		  {
			  exception.printStackTrace();
		  }
	  }

	  public RecoverPasswordQuotationSettingForm(JFrame parent, ActivateAccountForm activateAccountForm, TradingConsole tradingConsole,
												 String currentPassword, String newPassword)
	  {
		  this(parent);

		  this._activateAccountForm = activateAccountForm;
		  this._tradingConsole = tradingConsole;
		  this._currentPassword = currentPassword;
		  this._newPassword = newPassword;

		  this.notifyStaticText.setText(ActivateAccount.RecoverPasswordQuotationSettingNotify);
		  this.okButton.setText(ActivateAccount.Ok);
		  this.exitButton.setText(Language.ExitCaption);
		  this.messageStaticText.setText("");

		  this._tradingConsole.get_RecoverPasswordManager().setAnswerToEmpty();
		  this._tradingConsole.get_RecoverPasswordManager().initialize(this.answerTable, true, false);
		  //this.answerTable.doLayout();
	  }

	  private void submit()
	  {
		  this.answerTable.updateEditingValue();
		  Enumeration<TableColumn> tableColumns =  this.answerTable.getColumnModel().getColumns();
		  while(tableColumns.hasMoreElements())
		  {
			  TableColumn tableColumn = tableColumns.nextElement();
			  String info = tableColumn.getIdentifier().toString() + "; " + tableColumn.getHeaderValue().toString();
			  System.out.println(info);
		  }

		  this.messageStaticText.setText("");

		  String[][] recoverPasswordDatas = this._tradingConsole.get_RecoverPasswordManager().getRecoverPasswordDatas();
		  if (recoverPasswordDatas.length <=0)
		  {
			  this.messageStaticText.setText(ActivateAccount.RecoverPasswordDataNotAvailable);
			  return;
		  }

		  try
		  {
			  UpdatePasswordResult updatePasswordResult = this._tradingConsole.get_TradingConsoleServer().updatePassword2(this._tradingConsole.
				  get_LoginInformation().get_LoginName(), this._currentPassword, this._newPassword, recoverPasswordDatas);
			  boolean isSucceed = updatePasswordResult.get_IsSucceed();
			  if (isSucceed)
			  {
				  this._tradingConsole.enterMainForm();

				  ActivateAccountForm.Set_IsSubmit(true);

				  this._activateAccountForm.dispose();
				  this.dispose();
			  }
			  else
			  {
				  this.messageStaticText.setText(ActivateAccount.Failed);
			  }
		  }
		  catch(Throwable exception)
		  {
			  this.messageStaticText.setText(ActivateAccount.Failed);
			  TradingConsole.traceSource.trace(TraceType.Error, exception);
		  }
	  }

	  //SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new RecoverPasswordQuotationSettingForm_this_windowAdapter(this));

		this.setSize(620, 400);
		this.setResizable(false);
		this.setLayout(gridBagLayout1);
		this.setBackground(FormBackColor.activateAccountForm);

		notifyStaticText.setText("title");
		//answerTable.setLabel("pVTable1");
		okButton.setText("OK");
		exitButton.setText("Exit");
		okButton.addActionListener(new RecoverPasswordQuotationSettingForm_okButton_actionAdapter(this));
		exitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		messageStaticText.setText("message");
		messageStaticText.setAlignment(1);
		this.add(messageStaticText, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 5, 2), 60, 20));
		this.add(notifyStaticText, new GridBagConstraints2(0, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(15, 20, 2, 2), 0, 0));
		this.add(okButton, new GridBagConstraints2(0, 2, 1, 1, 0.0, 0.0,
												   GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 2, 2, 2), 80, 2));

		/*this.add(exitButton, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0,
												   GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 2, 2), 80, 2));*/
		//answerTable.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JScrollPane scrollPane = new JScrollPane(answerTable);
		this.add(scrollPane, new GridBagConstraints2(0, 1, 1, 1, 1.0, 1.0,
													  GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 5, 2, 5), 0, 0));
	}

	private PVStaticText2 notifyStaticText = new PVStaticText2();
	private DataGrid answerTable = new DataGrid("RecoverPasswordAnswerGrid");
	private PVButton2 okButton = new PVButton2();
	private PVButton2 exitButton = new PVButton2();
	private PVStaticText2 messageStaticText = new PVStaticText2();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	class RecoverPasswordQuotationSettingForm_this_windowAdapter extends WindowAdapter
	{
		private RecoverPasswordQuotationSettingForm adaptee;
		RecoverPasswordQuotationSettingForm_this_windowAdapter(RecoverPasswordQuotationSettingForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class RecoverPasswordQuotationSettingForm_okButton_actionAdapter implements ActionListener
{
	private RecoverPasswordQuotationSettingForm adaptee;
	RecoverPasswordQuotationSettingForm_okButton_actionAdapter(RecoverPasswordQuotationSettingForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
