package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;

import framework.diagnostics.TraceManager;

import tradingConsole.AppToolkit;
import tradingConsole.settings.SettingsManager;
import tradingConsole.TradingConsole;
import tradingConsole.settings.UISetting;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.settings.PublicParametersManager;
import tradingConsole.settings.ProxyManager;
import tradingConsole.Trace;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.JDialog;
import framework.StringHelper;
import javax.swing.border.EtchedBorder;
import java.awt.Font;
import tradingConsole.LoginInformation;
import java.io.*;

public class OptionForm extends JDialog implements ItemListener //Dialog
{
	private static String _dataSourceKey = "Option";

	private TradingConsole _tradingConsole;
	private boolean _enableDocking;

	public OptionForm(JFrame parent, TradingConsole tradingConsole, SettingsManager settingsManager, int currentIndex)
	{
		this(parent);

		this._tradingConsole = tradingConsole;

		this.proxyInitalize();
		this.traceInitalize();
		this.languageInitalize();

		this.okButton.setText(Language.btnOKCaption);
		this.exitButton.setText(Language.btnCancelCaption);
		//this._enableDocking = PrivateSettings.get_Instnace().get_EnableDocking();
		this.enableDockingCheckBox.setSelected(this._enableDocking);
	}

	private void languageInitalize()
	{
		this.languageChoice.setEditable(false);
		UISetting.fillMultiLanguage(this.languageChoice);
		this.languageStaticText.setText(Language.optionFormLanguageLabel);
		this.languageChoice.setSelectedItem(PublicParametersManager.version);
	}

	private void traceInitalize()
	{
		this.emptyAllLogFilesWhenEnterSystemCheckbox.setText(Language.emptyAllLogFilesWhenEnterSystem);
		this.emptyAllLogFilesWhenEnterSystemCheckbox.setSelected(PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem);

		this.traceCheckbox.setText(Language.optionFormTraceLabel);
		this.specialCheckbox.setText(Language.optionFormTraceSpecial);
		this.minTimeTokenStaticText.setText(Language.optionFormMinTimeToken);
		this.normalCheckbox.setText(Language.optionFormTraceNormal);
		this.emptyAllLogFilesButton.setText(Language.optionFormTraceClear);
		this.openLogFilesButton.setText(Language.optionFormTraceOpenLog);
		this.traceCheckbox.setSelected(PublicParametersManager.isTrace);
		this.normalCheckbox.setEnabled(PublicParametersManager.isTrace);
		this.specialCheckbox.setEnabled(PublicParametersManager.isTrace);
		this.minTimeTokenEdit.setEnabled(PublicParametersManager.isTrace);
		this.emptyAllLogFilesButton.setEnabled(PublicParametersManager.isTrace);
		this.openLogFilesButton.setEnabled(PublicParametersManager.isTrace);

		this.normalCheckbox.setSelected(!PublicParametersManager.isSpecialTrace);
		this.specialCheckbox.setSelected(PublicParametersManager.isSpecialTrace);
		this.minTimeTokenEdit.setText(Integer.toString(PublicParametersManager.minTimeTaken));
	}

	private void saveSettings()
	{
		this.saveProxySettings();
		this.saveTraceSettings();
		this.saveLanguageSettings();
		/*if(PrivateSettings.get_Instnace().get_EnableDocking() != enableDockingCheckBox.isSelected())
		{
			this._tradingConsole.get_MainForm().doLayout(enableDockingCheckBox.isSelected());
			PrivateSettings.get_Instnace().set_EnableDocking(enableDockingCheckBox.isSelected());
			PrivateSettings.get_Instnace().save();
		}*/
	}


	private void saveTraceSettings()
	{
		PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem = this.emptyAllLogFilesWhenEnterSystemCheckbox.isSelected();
		PublicParametersManager.isTrace = this.traceCheckbox.isSelected();
		PublicParametersManager.minTimeTaken = Integer.parseInt(this.minTimeTokenEdit.getText());
		//Trace.start();
		if (this.traceCheckbox.isSelected())
		{
			if (PublicParametersManager.isSpecialTrace != this.specialCheckbox.isSelected())
			{
				if (this._tradingConsole.get_LoginInformation().getIsConnected())
				{
					PublicParametersManager.isSpecialTrace = this.specialCheckbox.isSelected();
					if (PublicParametersManager.isSpecialTrace)
					{
						Trace.GetTracePropertiesForJava(this._tradingConsole.get_TradingConsoleServer());
					}
				}
				Trace.setLogProperties(this._tradingConsole);
				TraceManager.reloadConfig();
				TraceManager.enable();
			}
			else
			{
				TraceManager.reloadConfig();
				TraceManager.enable();
			}
		}
		else
		{
			TraceManager.disable();
		}
		if(!PublicParametersManager.saveVersion())
		{
			this._tradingConsole.messageNotify(Language.FailToSaveSettings, false);
		}
	}

	private void proxyInitalize()
	{
		this.proxyNotifyStaticText.setText(Language.optionFormProxyNotify);
		if (PublicParametersManager.version.equalsIgnoreCase("JPN"))
		{
			this.proxyNotifyStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 9));
		}
		this.proxyHostStaticText.setText(Language.optionFormProxyHost);
		this.proxyPortStaticText.setText(Language.optionFormProxyPort);
		this.userIdStaticText.setText(Language.optionFormUserId);
		this.passwordStaticText.setText(Language.optionFormPassword);
		/*
		 0 - text string is not modified.
		 1 - text string is converted to the upper case.
		 2 - text string is converted to the lower case.
		 Default value is 0.

		  BUT DEFAULT is 2!!!
		 */

		//this.passwordPassword.setCase(0);

		ProxyManager proxy = ProxyManager.create();
		this.proxyHostEdit.setText(proxy.get_ProxyHost());
		this.proxyPortNumeric.setText(proxy.get_ProxyPort());
		this.userIdEdit.setText(proxy.get_UserId());
		this.passwordPassword.setText(proxy.get_Password());
	}

	private void saveProxySettings()
	{
		ProxyManager proxy = ProxyManager.create();
		proxy.set_ProxyHost(this.proxyHostEdit.getText());
		proxy.set_ProxyPort(this.proxyPortNumeric.getText());
		proxy.set_UserId(this.userIdEdit.getText());
		proxy.set_Password(new String(this.passwordPassword.getPassword()));
		proxy.saveProxy();
	}

	/*private void saveFontSettings()
	{
		//String filePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),this._tradingConsole.get_LoginInformation().get_CustomerId().toString() + ".xml");
		String filePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "UiSetting.xml");
		UISetting uiSetting = this._settingsManager.getUISetting(UISetting.tradingPanelUiSetting);
		UISettingsManager.setSetting(uiSetting);
		uiSetting = this._settingsManager.getUISetting(UISetting.workingOrderListUiSetting);
		UISettingsManager.setSetting(uiSetting);
		uiSetting = this._settingsManager.getUISetting(UISetting.openOrderListUiSetting);
		UISettingsManager.setSetting(uiSetting);
		UISettingsManager.saveSettings(filePath);

		this._tradingConsole.rebindInstrument();
		this._tradingConsole.rebindWorkingOrderList();
		this._tradingConsole.rebindOpenOrderList();
	}*/

	private void doCheck(Object src)
	{
		if (src == this.traceCheckbox)
		{
			boolean state = traceCheckbox.isSelected();
			this.normalCheckbox.setEnabled(state);
			this.specialCheckbox.setEnabled(state);
			this.minTimeTokenEdit.setEnabled(state);
			this.emptyAllLogFilesButton.setEnabled(state);
			this.openLogFilesButton.setEnabled(state);
		}
	}

	//SourceCode End///////////////////////////////////////////////////////////////////////////////////

	public OptionForm(JFrame parent)
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
		String proxyTtitle = Language.optionFormProxyLabel;
		border2 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), proxyTtitle);
		String traceTitle = Language.optionFormTraceLabel;
		border1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)), traceTitle);
		this.notifyText.setText(Language.optionFormuseUnitGridPromptLabel);

		this.addWindowListener(new OptionUi_this_windowAdapter(this));
		proxyNotifyStaticText.setToolTipText("Proxy");
		tracePanel.setBorder(border1);
		tracePanel.setToolTipText("");
		proxyPanel.setBorder(border2);

		languagePanel.setLayout(gridBagLayout3);
		languageStaticText.setToolTipText("");
		languageStaticText.setText("Language");
		languagePanel.setBorder(border5);
		notifyText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		notifyText.setForeground(Color.red);
		enableDockingCheckBox.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		enableDockingCheckBox.setText("Enable docking");
		this.traceCheckboxGroup.add(this.normalCheckbox);
		this.traceCheckboxGroup.add(this.specialCheckbox);

		this.setSize(390, 450);
		this.setResizable(true);
		this.setLayout(gridBagLayout4);
		this.setTitle(Language.optionFormTitle);
		this.setBackground(FormBackColor.optionForm);
		okButton.setText("OK");
		okButton.setPreferredSize(new Dimension(80, 23));
		okButton.addActionListener(new OptionForm_okButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.setPreferredSize(new Dimension(80, 23));
		exitButton.addActionListener(new OptionUi_exitButton_actionAdapter(this));
		proxyHostStaticText.setText("Host");
		footPanel.setLayout(gridBagLayout1);
		proxyPortStaticText.setText(":");
		proxyPortStaticText.setAlignment(1);
		proxyPortNumeric.setText("pVNumeric1");
		userIdStaticText.setText("Username:");
		passwordStaticText.setText("Password:");
		proxyNotifyStaticText.setText("proxy");
		proxyPanel.setLayout(gridBagLayout2);
		this.enableDockingCheckBox.setText(Language.optionFormEnableDocking);

		tracePanel.setLayout(gridBagLayout5);
		passwordPassword.setEchoChar('*');
		traceCheckbox.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		traceCheckbox.setText("Enable trace");
		emptyAllLogFilesButton.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		openLogFilesButton.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		this.minTimeTokenEdit.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		this.minTimeTokenStaticText.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		emptyAllLogFilesButton.setText("Empty log");
		emptyAllLogFilesButton.setShadowWidth(7);
		//emptyAllLogFilesButton.setPreferredSize(new Dimension(80, 23));
		emptyAllLogFilesButton.addActionListener(new OptionForm_emptyAllLogFilesButton_actionAdapter(this)); //gridsPerUnitRowStaticText.setText("Grids/Row");
		specialCheckbox.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		normalCheckbox.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		emptyAllLogFilesWhenEnterSystemCheckbox.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		emptyAllLogFilesWhenEnterSystemCheckbox.setText("Empty all log when restarting");
		traceCheckbox.addItemListener(this);
		normalCheckbox.addItemListener(this);
		specialCheckbox.addItemListener(this);
		proxyPanel.add(proxyHostStaticText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 10, 2, 2), 0, 0));
		proxyPanel.add(userIdStaticText, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 2), 0, 0));
		proxyPanel.add(userIdEdit, new GridBagConstraints(1, 2, 1, 1, 0.3, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 80, 0));
		tracePanel.add(emptyAllLogFilesWhenEnterSystemCheckbox, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
		proxyPanel.add(passwordStaticText, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 2), 40, 0));
		proxyPanel.add(proxyHostEdit, new GridBagConstraints(1, 1, 2, 1, 0.6, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		proxyPanel.add(passwordPassword, new GridBagConstraints(1, 3, 1, 1, 0.3, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 80, -4));
		proxyPanel.add(proxyNotifyStaticText, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		proxyPanel.add(proxyPortNumeric, new GridBagConstraints(3, 1, 1, 1, 0.4, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, -2));

		languagePanel.add(this.notifyText, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 0), 20, 0));
		languagePanel.add(this.languageStaticText, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 10, 0), 20, 0));
		languagePanel.add(this.languageChoice, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 10, 5), 40, 0));

		footPanel.add(okButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 10, 0), 0, 0));
		footPanel.add(exitButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 10, 2), 0, 0));

		this.add(proxyPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.40
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(15, 5, 0, 5), 0, 0));
		this.add(tracePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.40
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 5, 0, 5), 10, 0));
		this.add(footPanel, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.1
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));
		this.getContentPane().add(languagePanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.1
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 5, 0, 5), 0, 0));
		proxyPanel.add(proxyPortStaticText, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		languagePanel.add(languageChoice, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 50, 0));
		languagePanel.add(notifyText, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.1
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(10, 12, 0, 0), 0, 0));
		/*languagePanel.add(enableDockingCheckBox, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 0), 0, 0));*/
		languagePanel.add(languageStaticText, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 5, 0), 30, 0));
		tracePanel.add(normalCheckbox, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 20, 2, 10), 0, 0));
		tracePanel.add(traceCheckbox, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 2, 10), 0, 0));
		tracePanel.add(specialCheckbox, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 10, 2, 10), 0, 0));

		tracePanel.add(this.minTimeTokenStaticText, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 10, 2, 1), 0, 0));
		tracePanel.add(this.minTimeTokenEdit, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 1, 2, 10), 40, 0));

		tracePanel.add(emptyAllLogFilesButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 2, 5), 0, 0));
		tracePanel.add(openLogFilesButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 2, 5), 0, 0));

		openLogFilesButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String logDirectory = AppToolkit.get_LogDirectory();
				LoginInformation loginInformation = _tradingConsole.get_LoginInformation();
				if (loginInformation != null && loginInformation.getIsConnected())
				{
					logDirectory += loginInformation.get_LoginName();
				}

				try
				{
					Runtime.getRuntime().exec("Explorer " + logDirectory);
				}
				catch (IOException ex)
				{
				}
			}
		});
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	JPanel proxyPanel = new JPanel();
	PVStaticText2 proxyHostStaticText = new PVStaticText2();

	PVButton2 okButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();

	JPanel tracePanel = new JPanel();

	private JPanel footPanel = new JPanel();
	private JPanel languagePanel = new JPanel();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private JTextField proxyHostEdit = new JTextField();
	private PVStaticText2 proxyPortStaticText = new PVStaticText2();
	private JFormattedTextField proxyPortNumeric = new JFormattedTextField(new DecimalFormat());
	private PVStaticText2 userIdStaticText = new PVStaticText2();
	private JTextField userIdEdit = new JTextField();
	private PVStaticText2 passwordStaticText = new PVStaticText2();
	private JPasswordField passwordPassword = new JPasswordField();
	private PVStaticText2 proxyNotifyStaticText = new PVStaticText2();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout5 = new GridBagLayout();
	private PVButton2 emptyAllLogFilesButton = new PVButton2();
	private PVButton2 openLogFilesButton = new PVButton2();
	private JCheckBox traceCheckbox = new JCheckBox();
	private ButtonGroup traceCheckboxGroup = new ButtonGroup();
	private JCheckBox normalCheckbox = new JCheckBox("Normal", true);
	private JCheckBox specialCheckbox = new JCheckBox("Special", false);
	private PVStaticText2 minTimeTokenStaticText = new PVStaticText2();
	private JTextField minTimeTokenEdit = new JTextField();
	private JCheckBox emptyAllLogFilesWhenEnterSystemCheckbox = new JCheckBox();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private Border border1 = BorderFactory.createLineBorder(SystemColor.controlText, 2);
	private Border border2 = BorderFactory.createLineBorder(Color.black, 2);
	private JAdvancedComboBox languageChoice = new JAdvancedComboBox();
	private PVStaticText2 notifyText = new PVStaticText2();
	private PVStaticText2 languageStaticText = new PVStaticText2();
	private Border border3 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(148, 145, 140));
	private Border border4 = new TitledBorder(border3);
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private Border border5 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private JCheckBox enableDockingCheckBox = new JCheckBox();

	public void itemStateChanged(ItemEvent e)
	{
		this.doCheck(e.getSource());
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.saveSettings();
		//boolean showInstrumentSpanGrid = PublicParametersManager.showInstrumentSpanGrid;
		//this._tradingConsole.get_MainForm().setVisibleOfInstrumentSpanGrid(showInstrumentSpanGrid);
		//this.toFront();
	}

	public void emptyAllLogFilesButton_actionPerformed(ActionEvent e)
	{
		TraceManager.emptyAllLogFiles(false);
	}

	class OptionUi_this_windowAdapter extends WindowAdapter
	{
		private OptionForm adaptee;
		OptionUi_this_windowAdapter(OptionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	private void saveLanguageSettings()
	{
		if (StringHelper.isNullOrEmpty(this.languageChoice.getSelectedItem().toString()))
		{
			return;
		}
		PublicParametersManager.version = this.languageChoice.getSelectedItem().toString();
		if(!PublicParametersManager.saveVersion())
		{
			this._tradingConsole.messageNotify(Language.FailToSaveSettings, false);
		}
	}
}

class OptionForm_emptyAllLogFilesButton_actionAdapter implements ActionListener
{
	private OptionForm adaptee;
	OptionForm_emptyAllLogFilesButton_actionAdapter(OptionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.emptyAllLogFilesButton_actionPerformed(e);
	}
}

class OptionForm_exitButton_actionAdapter implements ActionListener
{
	private OptionForm adaptee;
	OptionForm_exitButton_actionAdapter(OptionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class OptionForm_okButton_actionAdapter implements ActionListener
{
	private OptionForm adaptee;
	OptionForm_okButton_actionAdapter(OptionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}

class OptionUi_exitButton_actionAdapter implements ActionListener
{
	private OptionForm adaptee;
	OptionUi_exitButton_actionAdapter(OptionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
