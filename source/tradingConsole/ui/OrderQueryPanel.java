package tradingConsole.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;

import tradingConsole.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import com.jidesoft.spinner.PointSpinner;
import framework.Guid;
import framework.data.DataSet;
import framework.data.DataRow;
import framework.DateTime;
import framework.DBNull;
import framework.lang.Enum;
import tradingConsole.enumDefine.TradeOption;
import tradingConsole.enumDefine.OrderType;
import java.math.BigDecimal;
import tradingConsole.enumDefine.Phase;
import tradingConsole.settings.UISetting2;
import tradingConsole.settings.Parameter;
import tradingConsole.settings.UISetting;
import tradingConsole.framework.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import tradingConsole.ui.columnKey.OrderColKey;
import tradingConsole.settings.SettingsManager;
import tradingConsole.ui.columnKey.OpenOrderColKey;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import java.util.Collection;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import framework.data.DataRowCollection;

public class OrderQueryPanel extends JPanel
{
	private JScrollPane notConfirmedOrderContentPane;
	private JScrollPane listOrderContentPane;
	private JPanel queryPane = new JPanel();
	private DataGrid _openOrderTable;

	private PVStaticText2 instrumentStaticText = new PVStaticText2();
	private JAdvancedComboBox instrumentChoice = new JAdvancedComboBox();
	private PVStaticText2 accountStaticText = new PVStaticText2();
	private JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	private PVStaticText2 lastDayStaticText = new PVStaticText2();
	private JSpinner lastDaySpinner = new JSpinner();
	private JRadioButton notConfirmedPendingOrderRadioButton = new JRadioButton();
	private JRadioButton queryOrderRadioButton = new JRadioButton();
	private ButtonGroup radioGroup = new ButtonGroup();

	private PVButton2 queryButton = new PVButton2();
	private DataGrid _queryOrderTable;
	private TradingConsole tradingConsole;

	public OrderQueryPanel(TradingConsole tradingConsole)
	{
		this.tradingConsole = tradingConsole;
		this.jbInit();
	}

	private void jbInit()
	{
		this._openOrderTable = new DataGrid("NotConfirmedPendingOrderTable");
		this._queryOrderTable = new DataGrid("PendingOrderQueryTable");
		this._queryOrderTable.setShowVerticalLines(false);
		this._queryOrderTable.setShowHorizontalLines(false);
		this._queryOrderTable.setBackground(GridFixedBackColor.workingOrderList);
		this._queryOrderTable.setForeground(GridBackColor.workingOrderList);
		this._queryOrderTable.setShowGrid(false);
		this._queryOrderTable.setRowHeight(16);

		this._openOrderTable.setOptimized(true);
		this._queryOrderTable.setOptimized(true);
		this.notConfirmedOrderContentPane = new JScrollPane(this._openOrderTable);
		this.listOrderContentPane = new JScrollPane(this._queryOrderTable);

		if(ColorSettings.useBlackAsBackground)
		{
			this._openOrderTable.enableRowStripe(ColorSettings.GridStripeColors);
			this._queryOrderTable.enableRowStripe(ColorSettings.GridStripeColors);
			this._openOrderTable.setBackground(ColorSettings.AccountStatusGridBackground);
			this._openOrderTable.setGridColor(ColorSettings.AccountStatusGridBackground);
			this._queryOrderTable.setBackground(ColorSettings.AccountStatusGridBackground);
			this._queryOrderTable.setGridColor(ColorSettings.AccountStatusGridBackground);
			this.notConfirmedOrderContentPane.getViewport().setBackground(ColorSettings.TradingPanelListFrameBackground);
			this.listOrderContentPane.getViewport().setBackground(ColorSettings.TradingPanelListFrameBackground);
			this._queryOrderTable.setForeground(Color.WHITE);
			this._openOrderTable.setForeground(Color.WHITE);
		}
		else
		{
			this._openOrderTable.enableRowStripe();
			this._queryOrderTable.enableRowStripe();
		}


		this.radioGroup.add(this.notConfirmedPendingOrderRadioButton);
		this.radioGroup.add(this.queryOrderRadioButton);

		this.notConfirmedPendingOrderRadioButton.setText(Language.NotConfirmedPendingList);
		this.queryOrderRadioButton.setText(Language.Query);

		this.instrumentStaticText.setText(Language.Instrument);
		this.accountStaticText.setText(Language.Account);
		this.lastDayStaticText.setText(Language.LastDay);
		this.queryButton.setText(Language.Submit);

		this.setLayout(new GridBagLayout());
		this.add(notConfirmedPendingOrderRadioButton, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(10, 10, 2, 0), 30, 0));

		this.add(queryOrderRadioButton, new GridBagConstraints2(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(10, 0, 2, 0), 20, 0));

		this.add(queryPane, new GridBagConstraints2(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(10, 10, 2, 2), 0, 0));

		this.add(this.notConfirmedOrderContentPane, new GridBagConstraints2(0, 1, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.add(this.listOrderContentPane, new GridBagConstraints2(0, 1, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

		this.queryPane.setLayout(new GridBagLayout());
		this.queryPane.add(this.accountStaticText, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
		this.queryPane.add(this.accountChoice, new GridBagConstraints2(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 5), 0, 0));
		this.queryPane.add(this.instrumentStaticText, new GridBagConstraints2(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
		this.queryPane.add(this.instrumentChoice, new GridBagConstraints2(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 5), 0, 0));
		this.queryPane.add(this.lastDayStaticText, new GridBagConstraints2(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
		this.queryPane.add(this.lastDaySpinner, new GridBagConstraints2(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 5), 0, 0));
		this.queryPane.add(this.queryButton, new GridBagConstraints2(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

		this.fillInstruments();
		this.fillAccounts();
		this.fillLastDays();

		RadioButtonHandler handler = new RadioButtonHandler();
		this.notConfirmedPendingOrderRadioButton.addItemListener(handler);
		this.queryOrderRadioButton.addItemListener(handler);
		this.notConfirmedPendingOrderRadioButton.setSelected(true);

		this.queryButton.addActionListener(new ButtonHandler());
	}

	public void initailize()
	{
		this.fillInstruments();
		this.fillAccounts();
	}

	private void fillInstruments()
	{
		this.instrumentChoice.removeAllItems();

		this.instrumentChoice.addItem("*", null);
		for(Instrument instrument : this.tradingConsole.get_SettingsManager().getInstruments().values())
		{
			this.instrumentChoice.addItem(instrument.get_Description(), instrument);
		}
	}

	private void fillAccounts()
	{
		this.accountChoice.removeAllItems();

		this.accountChoice.addItem("*", null);
		for(Account account : this.tradingConsole.get_SettingsManager().getAccounts().values())
		{
			this.accountChoice.addItem(account.get_Code(), account);
		}
	}

	private void fillLastDays()
	{
		this.lastDaySpinner.setModel(new LastDaySpinnerModel());
	}

	private void switchOperation()
	{
		if(this.notConfirmedPendingOrderRadioButton.isSelected())
		{
			this.queryPane.setVisible(false);
			this.listOrderContentPane.setVisible(false);
			this.notConfirmedOrderContentPane.setVisible(true);
			//this.tradingConsole.initializeQueryOrderList();
		}
		else
		{
			this.queryPane.setVisible(true);
			this.listOrderContentPane.setVisible(true);
			this.notConfirmedOrderContentPane.setVisible(false);
		}
	}

	private void query()
	{
		Guid customerId = this.tradingConsole.get_SettingsManager().get_Customer().get_UserId();
		Object value = this.accountChoice.getSelectedValue();
		String accountId = value == null ? null : ((Account)value).get_Id().toString();
		value = this.instrumentChoice.getSelectedValue();
		String instrumentId = value == null ? null : ((Instrument)value).get_Id().toString();
		int lastDays = ((Integer)this.lastDaySpinner.getValue()).intValue();
		DataSet queryResult =
			this.tradingConsole.get_TradingConsoleServer().queryOrder(customerId, accountId, instrumentId, lastDays);
		if(queryResult == null)
		{
			AlertDialogForm.showDialog(this.tradingConsole.get_MainForm(), Language.Query, true, Language.QueryOrederFailedPleaseRetry);
			return;
		}

		DataRowCollection rows = queryResult.get_Tables().get_Item(0).get_Rows();
		ArrayList<HistoryOrder> orders = new ArrayList<HistoryOrder>(rows.get_Count());
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow dataRow = rows.get_Item(index);
			orders.add(HistoryOrder.from(dataRow, this.tradingConsole.get_SettingsManager()));
		}
		HistoryOrder.initializeList(this.tradingConsole.get_SettingsManager(), this._queryOrderTable, orders);
		for(HistoryOrder order : orders)
		{
			order.setForeground();
		}
	}

	public tradingConsole.ui.grid.DataGrid get_NotConfirmedPendingOrderTable()
	{
		return this._openOrderTable;
	}

	private class LastDaySpinnerModel extends AbstractSpinnerModel
	{
		private int _lastDays = 1;

		public Object getValue()
		{
			return this._lastDays;
		}

		public void setValue(Object value)
		{
			if(value instanceof Integer)
			{
				int lastDays = ((Integer)value).intValue();
				if(lastDays >0 && lastDays < 8)
				{
					this._lastDays = lastDays;
					fireStateChanged();
				}
			}
		}

		public Object getNextValue()
		{
			return this._lastDays >= 7 ? this._lastDays : ++this._lastDays;
		}

		public Object getPreviousValue()
		{
			return this._lastDays <= 1 ? this._lastDays : --this._lastDays;
		}
	}

	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			query();
		}
	}

	private class RadioButtonHandler implements ItemListener
	{
		public void itemStateChanged( ItemEvent e )
		{
			switchOperation();
		}
	}
}
