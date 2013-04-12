package tradingConsole.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.*;

import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.language.Login;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AgreementForm extends JDialog
{
	public static AgreementForm showDialog(JFrame parent, String title, boolean modal, String message)
	{
		AgreementForm dialog = new AgreementForm(parent, title, modal);
		dialog.messageTextArea.setMessage(message);
		dialog.messageTextArea.scrollToFirstLine();

		dialog.show();

		return dialog;
	}

	private AgreementForm(JFrame parent, String title, boolean modal)
	{
		super(parent, title, modal);
		try
		{
			jbInit();

			this.agreeCheckbox.setText(Login.agreeCheckBoxCaption);
			this.disagreeCheckbox.setText(Login.disagreeCheckBoxCaption);
			this.okButton.setText(Language.InstrumentSelectbtnOk);
			this.messageTextArea.setColumns(30);
			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.setAlwaysOnTop(true);
		this.setBackground(FormBackColor.alertDialogForm);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		this.setSize(800, 600);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		okButton.setBackground(Color.lightGray);
		okButton.setText("Ok");
		okButton.addActionListener(new AgreementForm_okButton_actionAdapter(this));
		agreeCheckbox.setText("I Agree");
		this.getContentPane().add(okButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(6, 279, 6, 5), 40, 0));

		buttonGroup1.add(agreeCheckbox);
		buttonGroup1.add(disagreeCheckbox);

		this.getContentPane().add(disagreeCheckbox, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 6, 0, 16), 0, 0));
		this.getContentPane().add(messageTextArea, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 6, 6, 6), 0, 0));
		this.getContentPane().add(agreeCheckbox, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 6, 0, 16), 0, 0));

		if(Login.showRiskStatementInRed())
		{
			this.messageTextArea.setTextColor(Color.RED);
			( (JPanel)this.getContentPane()).setBorder(BorderFactory.createLineBorder(Color.RED));
		}
	}

	private JButton okButton = new JButton();
	private MultiTextArea messageTextArea = new MultiTextArea();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JCheckBox agreeCheckbox = new JCheckBox("I Agree", false);
	private JCheckBox disagreeCheckbox = new JCheckBox("I Disagree", true);
	private ButtonGroup buttonGroup1 = new ButtonGroup();

	public void acceptButton_actionPerformed(ActionEvent e)
	{
		if (this.agreeCheckbox.isSelected())
		{
			this.dispose();
		}
		else
		{
			System.exit(0);
		}
	}
}

class AgreementForm_okButton_actionAdapter implements ActionListener
{
	private AgreementForm adaptee;
	AgreementForm_okButton_actionAdapter(AgreementForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.acceptButton_actionPerformed(e);
	}
}
