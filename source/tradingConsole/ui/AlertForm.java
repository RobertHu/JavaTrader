package tradingConsole.ui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;

import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.Language;
import tradingConsole.AppToolkit;
import javax.swing.JFrame;
import javax.swing.JDialog;

public class AlertForm extends JDialog
{
	public AlertForm(String title,String message)
	{
		super();
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);

			this.setTitle(title);
			this.messageEdit.setText(message);

			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new AlertForm_this_windowAdapter(this));

		this.setSize(450, 200);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.alertDialogFormTitle);
		this.setBackground(FormBackColor.alertDialogForm);

		okButton.setText("OK");
		okButton.addActionListener(new AlertForm_okButton_actionAdapter(this));
		messageEdit.setColumns(10);
		this.getContentPane().add(messageEdit, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 6, 0, 6), 0, 0));
		this.getContentPane().add(okButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 30, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVButton2 okButton = new PVButton2();
	MultiTextArea messageEdit = new MultiTextArea();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	class AlertForm_this_windowAdapter extends WindowAdapter
	{
		private AlertForm adaptee;
		AlertForm_this_windowAdapter(AlertForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class AlertForm_okButton_actionAdapter implements ActionListener
{
	private AlertForm adaptee;
	AlertForm_okButton_actionAdapter(AlertForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
