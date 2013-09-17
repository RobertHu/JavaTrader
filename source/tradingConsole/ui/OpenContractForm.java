package tradingConsole.ui;

import java.math.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.jidesoft.grid.*;
import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import javax.swing.table.TableModel;
import com.jidesoft.swing.JideSwingUtilities;
import tradingConsole.enumDefine.physical.PhysicalTradeSide;

public class OpenContractForm extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Order _order;

	public OpenContractForm(TradingConsole tradingConsole, SettingsManager settingsManager, Order order)
	{
		this(tradingConsole.get_MainForm());

		this._order = order;
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this.setTitle(Language.OpenContractlblOrderContract);
		this.orderCodeStaticText.setText(order.get_Code());
		this.limitStopButton.setText(Language.SetLMTStopCaption);
		this.liquidationButton.setText(Language.OpenContractbtnLiquidation);

		this.deliveryButton.setVisible(false);
		if(this._order.canDelivery())
		{
			TradePolicyDetail tradePolicyDetail
				= this._settingsManager.getTradePolicyDetail(this._order.get_Account().get_TradePolicyId(), this._order.get_Instrument().get_Id());
			boolean canDelivery = tradePolicyDetail.isAllowed(PhysicalTradeSide.Delivery);
			if(canDelivery)
			{
				Guid deliveryChargeId = tradePolicyDetail.get_DeliveryChargeId();
				if(deliveryChargeId != null)
				{
					Instrument instrument = this._order.get_Instrument();
					DeliveryCharge deliveryCharge = tradingConsole.get_SettingsManager().getDeliveryCharge(deliveryChargeId);
					if (deliveryCharge.get_PriceType().equals(MarketValuePriceType.DayOpenPrice))
					{
						if (instrument.get_LastQuotation() == null || instrument.get_LastQuotation().get_Open() == null)
							canDelivery = false;
					}
					else
					{
						if (instrument.get_Quotation() == null || instrument.get_Quotation().get_Timestamp().before(instrument.get_OpenTime()))
							canDelivery = false;
					}
				}
			}


			if(canDelivery)
			{
				this.deliveryButton.setVisible(true);
				this.deliveryButton.setText(Language.deliveryFormTitle);
				this.deliveryButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						delivery();
					}
				});
			}
		}

		if(this._order.get_InstalmentPolicyId() != null)
		{
			this.instalmentButton.setVisible(true);
			this.instalmentButton.setText(InstalmentLanguage.Instalment);
			this.instalmentButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					showInstalment();
				}
			});
		}
		else
		{
			this.instalmentButton.setVisible(false);
		}

		if (this._order.get_Transaction().get_Account().get_Type() == AccountType.Agent
			|| this._order.get_Transaction().get_Account().get_Type() == AccountType.Transit)
		{
			this.limitStopButton.setEnabled(false);
			this.liquidationButton.setEnabled(false);
		}
		else
		{
			Object[] result = MakeOrder.isAllowMakeBroadLimitOrder(this._tradingConsole, this._settingsManager,
				this._order.get_Transaction().get_Instrument(), this._order.get_Transaction().get_Account());
			boolean enableLimitStopButton= (Boolean)result[0];
			if(enableLimitStopButton) enableLimitStopButton = this.shouldEnableLimtStopButton();
			this.limitStopButton.setEnabled(enableLimitStopButton);

			result = MakeLiquidationOrder.isAccept(this._settingsManager, this._order);
			boolean enableLiquidationButton = (Boolean)result[0];
			if(enableLiquidationButton) enableLiquidationButton = this.shouldEnableLiquidationButton();
			this.liquidationButton.setEnabled(enableLiquidationButton);
		}

		this.exitButton.setText(Language.OpenContractbtnExit);
		this.assignButton.setText(Language.OpenContractbtnAssignOrder);
		this.assignButton.setVisible(false);
		if (order.get_Transaction().get_Account().get_Type() == AccountType.Agent)
		{
			Transaction transaction = MakeAssignOrder.getFirstAgentTransaction(this._settingsManager);
			if (transaction != null)
			{
				Object[] result = MakeOrder.isAllowMakeAssignOrder(this._tradingConsole, this._settingsManager, transaction.get_Instrument());
				this.assignButton.setVisible( (Boolean)result[0]);
			}
		}

		Color color = BuySellColor.getColor(order.get_IsBuy(), false);
		this.orderCodeStaticText.setForeground(color);

		this.fillOpenContractTable(this.openContractTable);
		this.limitStopButton.setEnabled(this.shouldEnableLimtStopButton());
	}

	private void fillOpenContractTable(DataGrid grid)
	{
		grid.get_HeaderRenderer().setHeight(0);
		grid.setModel(this.getTableModel());
		grid.enableRowStripe();
		grid.setShowVerticalLines(false);
		grid.setShowHorizontalLines(false);
		grid.setRowSelectionAllowed(false);
		grid.setColumnSelectionAllowed(false);
		grid.setFont(DataGrid.DefaultFont);
		grid.setForeground(Color.BLACK);
		grid.setDisabledForeground(Color.BLACK);
	}

	private TableModel getTableModel()
	{
		String[] columns = new String[]	{"Name", "Value"};

		String[][] data = new String[7][2];
		data[0][0] = Language.OpenContractlblAccountCodeA;
		data[0][1] = this._order.get_AccountCode();

		data[1][0] = Language.Instrument;
		data[1][1] = this._order.get_Transaction().get_Instrument().get_Description();

		data[2][0] = Language.OpenContractlblIsBuyA;
		data[2][1] = this._order.get_IsBuyString();

		data[3][0] = Language.OpenContractlblQuantityA;
		data[3][1] = this._order.get_LotBalanceString();

		data[4][0] = Language.OpenContractlblExecutePriceA;
		data[4][1] = this._order.get_ExecutePriceString();

		data[5][0] = Language.OpenContractlblExecutedTimeA;
		if (this._order.get_Transaction().get_OrderType() == OrderType.Risk)
		{
			data[5][1] = this._order.get_ExecuteTradeDay();
		}
		else
		{
			data[5][1] = Convert.toString(this._order.get_Transaction().get_ExecuteTime().addMinutes(Parameter.timeZoneMinuteSpan),
										  "yyyy-MM-dd HH:mm:ss");
		}

		data[6][0] = Language.OpenContractlblContractSizeA;
		data[6][1] = Convert.toString(this._order.get_Transaction().get_ContractSize());

		DefaultStyleTableModel model = new DefaultStyleTableModel(data, columns);

		CellStyle cellStyle = new CellStyle();
		cellStyle.setForeground(Color.BLACK);
		for(int row = 0; row <= 6; row++)
		{
			model.setCellStyle(row, 1, cellStyle);
		}

		cellStyle = new CellStyle();
		cellStyle.setForeground(BuySellColor.getColor(this._order.get_IsBuy(), false));
		model.setCellStyle(2, 1, cellStyle);

		return model;
	}

	public void dispose()
	{
		this._settingsManager.remove(this._order.get_Transaction().get_Instrument().get_Id());
		super.dispose();
	}

	public void setCanClose(boolean value)
	{
		this.exitButton.setEnabled(value);
		this.setDefaultCloseOperation(value ? WindowConstants.DISPOSE_ON_CLOSE : WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	private void limitStop()
	{
		Object[] result = MakeOrder.isAllowMakeBroadLimitOrder(this._tradingConsole, this._settingsManager, this._order.get_Transaction().get_Instrument(),
			this._order.get_Transaction().get_Account());
		if (! (Boolean)result[0])
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());
		}
		else
		{
			if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
				|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
			{
				this.liquidationButton.setEnabled(false);
				this._tradingConsole.get_MainForm().makeOrder2Instrance(this._order.get_Transaction().get_Instrument(), this._order, null, this,
					OperateSource.LiquidationLMTSTP);
			}
			else
			{
				this._tradingConsole.get_MainForm().limitOrderFormIntrance(this, this._order.get_Transaction().get_Instrument(), this._order, this,
					!this._order.get_IsBuy());
			}
		}
	}

	private boolean shouldEnableLiquidationButton()
	{
		Instrument instrument = this._order.get_Transaction().get_Instrument();
		return !instrument.isFromBursa() && instrument.get_MaxDQLot().compareTo(BigDecimal.ZERO) > 0;
	}

	private boolean shouldEnableLimtStopButton()
	{
		if(this._order.get_Transaction().get_Account().get_Type() == AccountType.Agent
			|| this._order.get_Transaction().get_Account().get_Type() == AccountType.Transit)
		{
			return false;
		}

		Instrument instrument = this._order.get_Transaction().get_Instrument();
		if(instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) <= 0) return false;

		boolean allowPlaceSpotTrade = this._order == null ? true : this._order.getAvailableLotBanlance(true, null).compareTo(BigDecimal.ZERO) > 0;
		boolean allowPlaceNonSpotTrade = this._order == null ? true : this._order.getAvailableLotBanlance(false, true).compareTo(BigDecimal.ZERO) > 0;
		if(!allowPlaceNonSpotTrade)
		{
			allowPlaceNonSpotTrade = this._order.getAvailableLotBanlance(false, false).compareTo(BigDecimal.ZERO) > 0;
		}

		if (allowPlaceNonSpotTrade)
		{
			Object[] result = MakeOrder.isAllowMakeMarketOnOpenOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				return true;
			}

			result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				return true;
			}

			result = MakeOrder.isAllowMakeStopOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				return true;
			}

			result = MakeOrder.isAllowMakeMarketOnCloseOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				return true;
			}

			result = MakeOrder.isAllowMakeOneCancelOtherOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				return true;
			}
		}

		/*if (allowPlaceSpotTrade)
		{
			Object[] result = MakeOrder.isAllowMakeMarketOrder(this._tradingConsole, this._settingsManager, instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());

			if ( (Boolean)result[0])
			{
				return true;
			}
		}*/
		return false;
	}

	private void showInstalment()
	{
		InstalmentForm instalmentForm = new InstalmentForm(this, this._order);
		JideSwingUtilities.centerWindow(instalmentForm);
		instalmentForm.show();
	}

	private void delivery()
	{
		DeliveryDialog dialog
			= new DeliveryDialog(this, this._tradingConsole, this._order.get_Instrument(), this._order.get_Account(), this._order, false);
		JideSwingUtilities.centerWindow(dialog);
		dialog.show();
	}

	private void liquidation()
	{
		Object[] result = MakeOrder.isAllowMakeLiquidationOrder(this._tradingConsole, this._settingsManager, this._order);
		if (! (Boolean)result[0])
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());
		}
		else
		{
			if (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
				|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
			{
				this.limitStopButton.setEnabled(false);
				this._tradingConsole.get_MainForm().makeOrder2Instrance(this._order.get_Transaction().get_Instrument(), this._order, null, this,
					OperateSource.LiquidationSingleSPT);
			}
			else
			{
				this._order.set_Close(true);
				( (MakeLiquidationOrder)result[2]).showUi(this);
			}
		}
	}

	private void assign()
	{
		Transaction transaction = MakeAssignOrder.getFirstAgentTransaction(this._settingsManager);
		if (transaction == null)
		{
			return;
		}
		Object[] result = MakeOrder.isAllowMakeAssignOrder(this._tradingConsole, this._settingsManager, transaction.get_Instrument());
		if (! (Boolean)result[0])
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());
		}
		else
		{
			this.assignButton.setEnabled(false);
			this.setCanClose(false);
			( (MakeAssignOrder)result[2]).showUi(this);
		}
	}

//SourceCode End/////////////////////////////////////////////////////////////////////////////////////////

	public OpenContractForm(JFrame parent)
	{
		super(parent, false);
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
		this.setSize(390, 240);
		this.setResizable(false);
		this.setLayout(gridBagLayout1);
		this.setTitle("Open Contract Information");
		this.setBackground(FormBackColor.openContractForm);

		Font font = HeaderFont.defaultFont;
		orderCodeStaticText.setText("20060504DQ0000001");
		orderCodeStaticText.setFont(font);
		orderCodeStaticText.setOdometer(4);
		orderCodeStaticText.setAlignment(1);
		limitStopButton.setText("Lmt/Stop");
		limitStopButton.setTriangle(4);
		limitStopButton.addActionListener(new OpenContractUi_limitStopButton_actionAdapter(this));
		liquidationButton.setTriangle(4);
		liquidationButton.addActionListener(new OpenContractUi_liquidationButton_actionAdapter(this));
		liquidationButton.setText("Liquidation");
		assignButton.setTriangle(4);
		assignButton.addActionListener(new OpenContractUi_assignButton_actionAdapter(this));
		assignButton.setText("Assign");
		exitButton.setText("Exit");
		exitButton.addActionListener(new OpenContractUi_exitButton_actionAdapter(this));
		openContractTable.setEditable(false);
		this.getContentPane().add(orderCodeStaticText, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(15, 1, 5, 1), 0, 0));
		this.getContentPane().add(limitStopButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 10, 0));
		JScrollPane scrollPane = new JScrollPane(openContractTable);
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 1, 5, 1, 1.0, 1.0
			, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));
		this.getContentPane().add(assignButton, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 0), 15, 0));
		this.getContentPane().add(liquidationButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 0), 0, 0));
		this.getContentPane().add(deliveryButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 0), 0, 0));
		this.getContentPane().add(instalmentButton, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 0), 0, 0));

		this.getContentPane().add(exitButton, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 20, 0));

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	public Order get_Order()
	{
		return this._order;
	}

	PVStaticText2 orderCodeStaticText = new PVStaticText2();
	DataGrid openContractTable = new DataGrid("OpenContractTable");
	PVButton2 limitStopButton = new PVButton2();
	PVButton2 liquidationButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 assignButton = new PVButton2();
	PVButton2 deliveryButton = new PVButton2();
	PVButton2 instalmentButton = new PVButton2();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void limitStopButton_actionPerformed(ActionEvent e)
	{
		this.limitStopButton.setEnabled(false);
		this.limitStop();
	}

	public void liquidationButton_actionPerformed(ActionEvent e)
	{
		this.liquidationButton.setEnabled(false);
		this.liquidation();
	}

	public void assignButton_actionPerformed(ActionEvent e)
	{
		this.assign();
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void rebind()
	{
		this.fillOpenContractTable(this.openContractTable);
	}

	public void enableLimitStopButton()
	{
		if(this.shouldEnableLimtStopButton()) this.limitStopButton.setEnabled(true);
	}

	public void enableLiquidationButton()
	{
		if(this.shouldEnableLiquidationButton()) this.liquidationButton.setEnabled(true);
	}

	public void enableAssignButton()
	{
		this.assignButton.setEnabled(true);
	}
}

class OpenContractUi_exitButton_actionAdapter implements ActionListener
{
	private OpenContractForm adaptee;
	OpenContractUi_exitButton_actionAdapter(OpenContractForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class OpenContractUi_assignButton_actionAdapter implements ActionListener
{
	private OpenContractForm adaptee;
	OpenContractUi_assignButton_actionAdapter(OpenContractForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.assignButton_actionPerformed(e);
	}
}

class OpenContractUi_liquidationButton_actionAdapter implements ActionListener
{
	private OpenContractForm adaptee;
	OpenContractUi_liquidationButton_actionAdapter(OpenContractForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.liquidationButton_actionPerformed(e);
	}
}

class OpenContractUi_limitStopButton_actionAdapter implements ActionListener
{
	private OpenContractForm adaptee;
	OpenContractUi_limitStopButton_actionAdapter(OpenContractForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.limitStopButton_actionPerformed(e);
	}
}
