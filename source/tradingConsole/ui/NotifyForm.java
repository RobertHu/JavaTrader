package tradingConsole.ui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.Language;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Timer;

public class NotifyForm extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private NotifyForm(JFrame parent, boolean isModal)
	{
		super(parent, isModal);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setAlwaysOnTop(true);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
		this.setAlwaysOnTop(true);
	}

	public NotifyForm(TradingConsole tradingConsole, SettingsManager settingsManager,String title,int width,int height, boolean isModal)
	{
		this(tradingConsole.get_MainForm(), isModal);

		this.setSize(width,height);
		this.setTitle(title);
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this.okButton.setText(Language.ExitCaption);
	}

	public void setMessage(String message, boolean isClear)
	{
		if (isClear)
		{
			this.messageEdit.setMessage(message);
		}
		else
		{
			this.messageEdit.append("\n");
			this.messageEdit.append(message);
		}
		//String message2 = (isClear) ? message : this.messageEdit.getText() + "\n" + message;
		//this.messageEdit.setMessag(message2);
	}

	public void dispose()
	{
		this.setMessage("", true);
		if(this._settingsManager != null) this._settingsManager.set_IsHasNotifiedAssignOrder(false);
		super.dispose();
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new NotifyForm_this_windowAdapter(this));

		this.setSize(450, 200);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.notifyFormTitle);
		this.setBackground(FormBackColor.notifyForm);

		okButton.setText("OK");
		okButton.addActionListener(new NotifyForm_okButton_actionAdapter(this));
		messageEdit.setBackground(Color.white);
		messageEdit.setColumns(10);
		messageEdit.setEditable(false);
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

	class NotifyForm_this_windowAdapter extends WindowAdapter
	{
		private NotifyForm adaptee;
		NotifyForm_this_windowAdapter(NotifyForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class NotifyForm_okButton_actionAdapter implements ActionListener
{
	private NotifyForm adaptee;
	NotifyForm_okButton_actionAdapter(NotifyForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}
