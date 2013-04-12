package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;

import tradingConsole.AppToolkit;
import tradingConsole.settings.SettingsManager;
import tradingConsole.TradingConsole;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.settings.News;
import framework.StringHelper;
import java.awt.TextArea;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JDialog;
import tradingConsole.settings.Message;
import tradingConsole.ui.grid.DataGrid;
import framework.data.DataRow;
import framework.data.DataTable;
import framework.data.DataRowCollection;
import framework.data.DataTableCollection;
import framework.data.DataSet;
import framework.DBNull;
import java.util.ArrayList;

public class NewsContentForm extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private News _currentNews;
	private DataGrid _newsDataGrid;

	private static ArrayList<NewsContentForm> _openNewsForms = new ArrayList<NewsContentForm>();

	public NewsContentForm(TradingConsole tradingConsole, SettingsManager settingsManager,News news)
	{
		this(tradingConsole.get_MainForm());
		NewsContentForm._openNewsForms.add(this);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._currentNews = news;
		this._newsDataGrid = tradingConsole.get_MainForm().get_NewsTable();

		//this.deleteButton.setText(Language.DeleteCaption);
		this.exitButton.setText(Language.ExitCaption);
		this.previousButton.setText(Language.PreviousCaption);
		this.nextButton.setText(Language.NextCaption);

		this.updateNews();
		this.updateButtonStatus();
	}
/*
	private void delete()
	{
		//Are you sure??????????????????
		boolean isDeleted = this._tradingConsole.get_TradingConsoleServer().deleteMessage(this._message.get_Id());
		if (isDeleted)
		{
			this._settingsManager.removeMessage(this._message);
			this._tradingConsole.get_MainForm().refreshMessage();
			this.dispose();
		}
	}
*/
//SourceCode End//////////////////////////////////////////////////////////////////////////////

	public NewsContentForm(JFrame parent)
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

	public static void updateButtonStatusForAll()
	{
		for(NewsContentForm newsContentForm : NewsContentForm._openNewsForms)
		{
			newsContentForm.updateButtonStatus();
		}
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new NewsContentForm_this_windowAdapter(this));

		this.setSize(460, 320);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.newsFormTitle);
		this.setBackground(FormBackColor.messageContentForm);

		//deleteButton.setText("Delete");
		//deleteButton.addActionListener(new NewsContentForm_deleteButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new NewsContentForm_exitButton_actionAdapter(this));
		previousButton.setText("Previous");
		previousButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showPreviousNews();
			}
		});
		nextButton.setText("Next");
		nextButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showNextNews();
			}
		});

		messageContentMultiTextArea.setBackground(Color.white);
		messageContentMultiTextArea.setWrapStyleWord(true);
		titleStaticText.setText("pVStaticText1");
		publishTimeStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		publishTimeStaticText.setText("pVStaticText2");
		this.getContentPane().add(publishTimeStaticText, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 5, 10), 200, 0));

		this.getContentPane().add(previousButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 20, 0));
		this.getContentPane().add(nextButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 10, 10), 20, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 10, 10), 20, 0));
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 10, 10), 0, 0));
		messageContentMultiTextArea.setLineWrap(true);
		messageContentMultiTextArea.setEditable(false);
		this.getContentPane().add(titleStaticText, new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 10, 5, 10), 200, 0));
	}

	private void getNewsContent()
	{
		if (framework.StringHelper.isNullOrEmpty(this._currentNews.get_Content()))
		{
			DataSet dataSet = this._tradingConsole.get_TradingConsoleServer().getNewsContents(this._currentNews.get_Id());
			DataTableCollection tables = dataSet.get_Tables();
			if (tables.get_Count() <= 0)
			{
				return;
			}
			DataTable dataTable = tables.get_Item(0);
			if (dataTable != null)
			{
				DataRowCollection dataRowCollection = dataTable.get_Rows();
				for (int i = 0, count = dataRowCollection.get_Count(); i < count; i++)
				{
					DataRow dataRow = dataRowCollection.get_Item(i);
					Object content = dataRow.get_Item("Contents");
					this._currentNews.set_Content(content == DBNull.value ? "" : (String)content);
				}
			}
		}
	}

	private void showPreviousNews()
	{
		int index = this._newsDataGrid.getRow(this._currentNews);
		if(index > 0)
		{
			index--;
			this._currentNews = (News)this._newsDataGrid.getObject(index);
			this.updateNews();
			this.updateButtonStatus();
		}
	}

	private void showNextNews()
	{
		int index = this._newsDataGrid.getRow(this._currentNews);
		if(index < this._newsDataGrid.getRowCount() - 1)
		{
			index++;
			this._currentNews = (News)this._newsDataGrid.getObject(index);
			this.updateNews();
			this.updateButtonStatus();
		}
	}

	private void updateButtonStatus()
	{
		int index = this._newsDataGrid.getRow(this._currentNews);
		this.previousButton.setEnabled(index > 0);
		this.nextButton.setEnabled(index < this._newsDataGrid.getRowCount() - 1);
	}

	private void updateNews()
	{
		this.getNewsContent();
		this.titleStaticText.setText(this._currentNews.get_Title());
		this.publishTimeStaticText.setText(this._currentNews.get_PublishTimeString());
		this.publishTimeStaticText.setAlignment(2);
		//this.messageContentMultiTextArea.setMessage(this._news.get_Content());
		if(this._currentNews.get_Content() != null)
		{
			String multiLineNews = StringHelper.replace(this._currentNews.get_Content(), "\\n", "\n");
			this.messageContentMultiTextArea.setText(multiLineNews);
			this.messageContentMultiTextArea.setCaretPosition(0);
		}
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	@Override
	public void dispose()
	{
		NewsContentForm._openNewsForms.remove(this);
		super.dispose();
	}

	//PVButton2 deleteButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 previousButton = new PVButton2();
	PVButton2 nextButton = new PVButton2();

	//MultiTextArea messageContentMultiTextArea = new MultiTextArea("",10,1,MultiTextArea.SCROLLBARS_VERTICAL_ONLY);
	JTextArea messageContentMultiTextArea = new JTextArea(10, 1);
	JScrollPane scrollPane = new JScrollPane(messageContentMultiTextArea);
	PVStaticText2 titleStaticText = new PVStaticText2();
	PVStaticText2 publishTimeStaticText = new PVStaticText2();
	GridBagLayout gridBagLayout1 = new GridBagLayout();
	//public void deleteButton_actionPerformed(ActionEvent e)
	//{
	//	this.delete();
	//}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	class NewsContentForm_this_windowAdapter extends WindowAdapter
	{
		private NewsContentForm adaptee;
		NewsContentForm_this_windowAdapter(NewsContentForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class NewsContentForm_exitButton_actionAdapter implements ActionListener
{
	private NewsContentForm adaptee;
	NewsContentForm_exitButton_actionAdapter(NewsContentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
/*
class NewsContentForm_deleteButton_actionAdapter implements ActionListener
{
	private NewsContentForm adaptee;
	NewsContentForm_deleteButton_actionAdapter(NewsContentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.deleteButton_actionPerformed(e);
	}
}
*/
