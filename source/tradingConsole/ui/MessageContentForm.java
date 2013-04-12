package tradingConsole.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import java.util.ArrayList;

public class MessageContentForm extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Message _currentMessage;
	private DataGrid _messageDataGrid;
	private static ArrayList<MessageContentForm> _openMessageForms = new ArrayList<MessageContentForm>();

	public MessageContentForm(TradingConsole tradingConsole, SettingsManager settingsManager,Message message)
	{
		this(tradingConsole.get_MainForm());
		MessageContentForm._openMessageForms.add(this);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._currentMessage = message;
		this._messageDataGrid = tradingConsole.get_MainForm().get_MessageTable();

		this.deleteButton.setText(Language.DeleteCaption);
		this.exitButton.setText(Language.ExitCaption);

		this.previousButton.setText(Language.PreviousCaption);
		this.nextButton.setText(Language.NextCaption);

		this.updateMessage();
		this.updateButtonStatus();
	}

	public static void updateButtonStatusForAll()
	{
		for(MessageContentForm messageContentForm : MessageContentForm._openMessageForms)
		{
			messageContentForm.updateButtonStatus();
		}
	}

	@Override
	public void dispose()
	{
		MessageContentForm._openMessageForms.remove(this);
		super.dispose();
	}

	private void updateMessage()
	{
		this.titleStaticText.setText(this._currentMessage.get_Title());
		this.publishTimeStaticText.setText(this._currentMessage.get_PublishTimeString());
		this.publishTimeStaticText.setAlignment(2);
		this.messageContentMultiTextArea.setMessage(this._currentMessage.get_Content());
	}

	private void delete()
	{
		//Are you sure??????????????????
		boolean isDeleted = this._tradingConsole.get_TradingConsoleServer().deleteMessage(this._currentMessage.get_Id());
		if (isDeleted)
		{
			int index = this._messageDataGrid.getRow(this._currentMessage);
			boolean isLast = index == this._messageDataGrid.getRowCount() - 1;
			this._settingsManager.removeMessage(this._currentMessage);
			this._tradingConsole.rebindMessage();

			if(this._messageDataGrid.getRowCount() == 0)
			{
				this.dispose();
				return;
			}

			if(isLast) index--;
			this._currentMessage = (Message)this._messageDataGrid.getObject(index);
			this.updateMessage();
		}
	}

	private void showPreviousMessage()
	{
		int index = this._messageDataGrid.getRow(this._currentMessage);
		if(index > 0)
		{
			index--;
			this._currentMessage = (Message)this._messageDataGrid.getObject(index);
			this.updateMessage();
			this.updateButtonStatus();
		}
	}

	private void showNextMessage()
	{
		int index = this._messageDataGrid.getRow(this._currentMessage);
		if(index < this._messageDataGrid.getRowCount() - 1)
		{
			index++;
			this._currentMessage = (Message)this._messageDataGrid.getObject(index);
			this.updateMessage();
			this.updateButtonStatus();
		}
	}

	private void updateButtonStatus()
	{
		int index = this._messageDataGrid.getRow(this._currentMessage);
		this.previousButton.setEnabled(index > 0);
		this.nextButton.setEnabled(index < this._messageDataGrid.getRowCount() - 1);
	}

	//SourceCode End//////////////////////////////////////////////////////////////////////////////

	public MessageContentForm(JFrame parent)
	{
		super(parent, true);
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

	private void jbInit() throws Exception
	{
		this.addWindowListener(new MessageContentForm_this_windowAdapter(this));

		this.setSize(460, 320);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.messageContentFormTitle);
		this.setBackground(FormBackColor.messageContentForm);

		deleteButton.setText("Delete");
		deleteButton.addActionListener(new MessageContentForm_deleteButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new MessageContentForm_exitButton_actionAdapter(this));

		previousButton.setText("Previous");
		previousButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showPreviousMessage();
			}
		});
		nextButton.setText("Next");
		nextButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showNextMessage();
			}
		});

		messageContentMultiTextArea.setBackground(Color.white);
		titleStaticText.setText("pVStaticText1");
		publishTimeStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		publishTimeStaticText.setText("pVStaticText2");
		this.getContentPane().add(titleStaticText, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 10, 2, 2), 200, 0));
		this.getContentPane().add(publishTimeStaticText, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 5, 5), 200, 0));
		this.getContentPane().add(messageContentMultiTextArea, new GridBagConstraints(0, 2, 4, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 10, 10, 10), 0, 0));

		this.getContentPane().add(previousButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 2), 20, 0));
		this.getContentPane().add(nextButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 2), 20, 0));

		this.getContentPane().add(deleteButton, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 2), 20, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 10, 10), 20, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVButton2 deleteButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 previousButton = new PVButton2();
	PVButton2 nextButton = new PVButton2();

	MultiTextArea messageContentMultiTextArea = new MultiTextArea();
	PVStaticText2 titleStaticText = new PVStaticText2();
	PVStaticText2 publishTimeStaticText = new PVStaticText2();
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void deleteButton_actionPerformed(ActionEvent e)
	{
		this.delete();
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	class MessageContentForm_this_windowAdapter extends WindowAdapter
	{
		private MessageContentForm adaptee;
		MessageContentForm_this_windowAdapter(MessageContentForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class MessageContentForm_exitButton_actionAdapter implements ActionListener
{
	private MessageContentForm adaptee;
	MessageContentForm_exitButton_actionAdapter(MessageContentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class MessageContentForm_deleteButton_actionAdapter implements ActionListener
{
	private MessageContentForm adaptee;
	MessageContentForm_deleteButton_actionAdapter(MessageContentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.deleteButton_actionPerformed(e);
	}
}
