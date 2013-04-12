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

public class DebugForm extends JFrame
{
	public DebugForm()
	{
		super();
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
	}

	public void clearMessage()
	{
		this.messageEdit.setText("");
		this.messageEdit.doLayout();
	}

	public void append(String message)
	{
		this.messageEdit.append(message);
		this.messageEdit.doLayout();
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new DebugForm_this_windowAdapter(this));

		this.setSize(450, 200);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.alertDialogFormTitle);
		this.setBackground(FormBackColor.alertDialogForm);

		okButton.setText("OK");
		okButton.addActionListener(new DebugForm_okButton_actionAdapter(this));
		messageEdit.setColumns(10);
		messageEdit.setEditable(true);
		clearButton.setText("Clear");
		clearButton.addActionListener(new DebugForm_clearButton_actionAdapter(this));
		this.getContentPane().add(messageEdit, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
			, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(10, 6, 0, 6), 0, 81));
		this.getContentPane().add(okButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15, 1, 9, 6), 30, 0));
		this.getContentPane().add(clearButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(15, 6, 9, 0), 30, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVButton2 okButton = new PVButton2();
	MultiTextArea messageEdit = new MultiTextArea();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private PVButton2 clearButton = new PVButton2();
	public void okButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void clearButton_actionPerformed(ActionEvent e)
	{
		this.clearMessage();
	}

	class DebugForm_this_windowAdapter extends WindowAdapter
	{
		private DebugForm adaptee;
		DebugForm_this_windowAdapter(DebugForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class DebugForm_clearButton_actionAdapter implements ActionListener
{
	private DebugForm adaptee;
	DebugForm_clearButton_actionAdapter(DebugForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.clearButton_actionPerformed(e);
	}
}

class DebugForm_okButton_actionAdapter implements ActionListener
{
	private DebugForm adaptee;
	DebugForm_okButton_actionAdapter(DebugForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
