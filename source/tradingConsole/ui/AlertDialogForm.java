package tradingConsole.ui;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JDialog;
import javax.swing.JFrame;

import framework.StringHelper;

import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.Language;

public class AlertDialogForm extends JDialog
{
	public static AlertDialogForm showDialog(JFrame parent, String title, boolean modal, String message)
	{
		if (StringHelper.isNullOrEmpty(title))
		{
			title = "Warning";
		}
		AlertDialogForm dialog = new AlertDialogForm(parent, title, true);
		dialog.setMessage(message);
		dialog.show();
		dialog.toFront();

		return dialog;
	}

	public static AlertDialogForm showDialog(JDialog parent, String title, boolean modal, String message)
	{
		if (StringHelper.isNullOrEmpty(title))
		{
			title = "Warning";
		}
		AlertDialogForm dialog = new AlertDialogForm(parent, title, true);
		dialog.setMessage(message);
		dialog.show();
		dialog.toFront();

		return dialog;
	}

	public AlertDialogForm(JFrame parent, String title, boolean modal)
	{
		super(parent, title, modal);
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

	public AlertDialogForm(JDialog parent, String title, boolean modal)
	{
		super(parent, title, modal);
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

	public void setMessage(String message)
	{
		this.messageEdit.setText(message);
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new AlertDialogForm_this_windowAdapter(this));

		this.setSize(450, 200);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		if(StringHelper.isNullOrEmpty(this.getTitle())
			|| this.getTitle().equals("Warning"))
		{
			this.setTitle(Language.alertDialogFormTitle);
		}
		this.setBackground(FormBackColor.alertDialogForm);

		okButton.setText("OK");
		okButton.addActionListener(new AlertDialogForm_okButton_actionAdapter(this));
		//messageEdit.setAutoScroll(true);
		//messageEdit.setBorderStyle(23);
		//messageEdit.setColumns(10);
		this.getContentPane().add(messageEdit, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
		this.getContentPane().add(okButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 10, 0), 40, 0));
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

	class AlertDialogForm_this_windowAdapter extends WindowAdapter
	{
		private AlertDialogForm adaptee;
		AlertDialogForm_this_windowAdapter(AlertDialogForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class AlertDialogForm_okButton_actionAdapter implements ActionListener
{
	private AlertDialogForm adaptee;
	AlertDialogForm_okButton_actionAdapter(AlertDialogForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
