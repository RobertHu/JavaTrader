package tradingConsole.ui;

import javax.swing.JPanel;
import tradingConsole.TradingConsole;
import tradingConsole.Instrument;
import tradingConsole.Order;
import framework.DateTime;
import tradingConsole.enumDefine.ExpireType;
import tradingConsole.settings.SettingsManager;
import tradingConsole.settings.MakeOrderAccount;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import java.text.DecimalFormat;
import java.awt.GridBagLayout;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.enumDefine.OperateType;
import javax.swing.JDialog;
import java.math.BigDecimal;
import tradingConsole.enumDefine.OrderType;
import tradingConsole.ui.language.Language;
import tradingConsole.AppToolkit;
import tradingConsole.enumDefine.BuySellType;
import tradingConsole.enumDefine.TradeOption;
import tradingConsole.ui.colorHelper.BuySellColor;
import java.awt.Color;
import tradingConsole.TradePolicyDetail;
import framework.TimeSpan;
import framework.StringHelper;
import tradingConsole.Account;
import java.awt.Rectangle;
import framework.diagnostics.TraceType;
import tradingConsole.ui.grid.IActionListener;
import tradingConsole.BestLimit;
import tradingConsole.ui.columnKey.OutstandingOrderColKey;
import com.jidesoft.grid.TableColumnChooser;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import javax.swing.event.DocumentEvent;
import tradingConsole.ui.colorHelper.FormBackColor;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.ItemEvent;
import tradingConsole.Price;
import tradingConsole.enumDefine.SetPriceError;
import java.util.HashMap;
import tradingConsole.settings.PublicParametersManager;
import framework.Guid;
import java.util.Locale;
import java.util.Iterator;
import javax.swing.SwingConstants;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.grid.BindingSource;
import tradingConsole.BestPending;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;

public class MatchingOrderForm extends JPanel implements IBuySellProvider
{
	private OpenContractForm _openContractForm;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeOrderAccount _makeOrderAccount;
	private Instrument _instrument;
	private Order _order;

	private DateTime _expireTime = null;
	private ExpireType _expireType = null;
	private OpenCloseRelationSite _openCloseRelationSite;

	PVStaticText2 accountStaticText = new PVStaticText2();
	PVStaticText2 isBuyStaticText = new PVStaticText2();
	PVStaticText2 setPriceStaticText = new PVStaticText2();
	PVStaticText2 totalLotStaticText = new PVStaticText2();
	PVStaticText2 closeLotStaticText = new PVStaticText2();
	PVStaticText2 expireTimeStaticText = new PVStaticText2();
	JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	JAdvancedComboBox isBuyChoice = new JAdvancedComboBox();
	//JTextField priceEdit = new JTextField();
	JSpinner priceEdit = new JSpinner();
	JScrollPane scrollPane;
	JFormattedTextField totalLotTextField = new JFormattedTextField(new DecimalFormat());
	JFormattedTextField closeLotTextField = new JFormattedTextField(new DecimalFormat());
	PVStaticText2 totalCloseLot = new PVStaticText2();
	PVStaticText2 totalCloseLotTitle = new PVStaticText2();
	JAdvancedComboBox expireTimeChoice = new JAdvancedComboBox();
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderGrid");

	PVStaticText2 bestBuyTitle = new PVStaticText2();
	DataGrid bestBuyTable;
	private Boolean _closeAllSell = null;

	PVStaticText2 bestSellTitle = new PVStaticText2();
	DataGrid bestSellTable;

	PVStaticText2 timeAndSalesTitle = new PVStaticText2();
	DataGrid timeAndSalesTable;

	PVButton2 resetButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 closeAllButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	ExpireTimeEditor expireTimeDate = new ExpireTimeEditor();

	BidAskButton bidButton = new BidAskButton(true);
	BidAskButton askButton = new BidAskButton(false);

	private JDialog _residedWindow = null;

	private BigDecimal _lastAccountLot = BigDecimal.ZERO;
	private MakeLimitOrder _makeLimitOrder;

	private MatchingOrderForm(Instrument instrument, boolean orderTypeIs2)
	{
		try
		{
			this._instrument = instrument;
			this.bestBuyTable = new DataGrid("BestBuy");
			this.bestSellTable = new DataGrid("BestSell");
			this.timeAndSalesTable = new DataGrid("TimeAndSales");

			BestLimitTableActionListener actionListener = new BestLimitTableActionListener(this);
			this.bestBuyTable.addActionListener(actionListener);
			this.bestSellTable.addActionListener(actionListener);

			jbInit(orderTypeIs2);
			this._openCloseRelationSite = new OpenCloseRelationSite(this);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
			//exception.printStackTrace();
		}
	}

	private void jbInit(boolean orderTypeIs2)
	{
		totalCloseLotTitle.setText("Total close lot:");

		this.setSize(700, 300);
		//this.setResizable(true);
		this.setLayout(gridBagLayout1);
		//this.setTitle(Language.limitOrderFormTitle);
		if (!orderTypeIs2)
		{
			this.setBackground(FormBackColor.limitOrderForm);
		}

		Font font = new Font("SansSerif", Font.BOLD, 13);
		totalLotTextField.setFont(font);
		priceEdit.setFont(font);

		accountStaticText.setText("Account");
		accountStaticText.setFont(font);
		isBuyStaticText.setText("B/S");
		isBuyStaticText.setFont(font);
		setPriceStaticText.setText("Limit");
		setPriceStaticText.setFont(font);
		totalLotStaticText.setText("Lot");
		totalLotStaticText.setFont(font);
		closeLotStaticText.setFont(font);
		expireTimeStaticText.setText("Expire Time");
		expireTimeStaticText.setFont(font);
		totalLotTextField.setText("Numeric1");

		bestSellTitle.setFont(font);
		bestBuyTitle.setFont(font);
		timeAndSalesTitle.setFont(font);

		totalLotTextField.addFocusListener(new MatchingOrderForm_lotNumeric_focusAdapter(this));
		KeyListener keyListener = new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
			}

			public void keyPressed(KeyEvent e)
			{
			}

			public void keyReleased(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}
		};
		totalLotTextField.addKeyListener(keyListener);
		resetButton.setText("Reset");
		resetButton.addActionListener(new MatchingOrderForm_resetButton_actionAdapter(this));
		submitButton.setText("Submit");
		submitButton.addActionListener(new MatchingOrderForm_submitButton_actionAdapter(this));
		closeAllButton.addActionListener(new MatchingOrderForm_closeAllButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new MatchingOrderForm_exitButton_actionAdapter(this));
		accountChoice.addItemListener(new MatchingOrderForm_accountChoice_actionAdapter(this));
		isBuyChoice.addItemListener(new MatchingOrderForm_isBuyChoice_actionAdapter(this));
		expireTimeChoice.addItemListener(new MatchingOrderForm_expireTimeChoice_actionAdapter(this));
		priceEdit.addFocusListener(new MatchingOrderForm_setPriceEdit_focusAdapter(this));
		priceEdit.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				updateSubmitButtonStatus();
			}
		});
		/*priceEdit.getDocument().addDocumentListener(new DocumentListener()
		   {
		 public void insertUpdate(DocumentEvent e)
		 {
		  updateSubmitButtonStatus();
		 }

		 public void removeUpdate(DocumentEvent e)
		 {
		  updateSubmitButtonStatus();
		 }

		 public void changedUpdate(DocumentEvent e)
		 {
		  updateSubmitButtonStatus();
		 }
		   });*/
		outstandingOrderTable.addKeyListener(new MatchingOrderForm_outstandingOrderTable_keyAdapter(this));

		this.closeLotTextField.setEnabled(false);
		this.closeLotTextField.setBackground(Color.LIGHT_GRAY);

		/*this.add(bidButton, new GridBagConstraints2(0, 0, 5, 1, 0.0, 0.0,
		 GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 80, 55));
		   this.add(askButton, new GridBagConstraints2(5, 0, 3, 1, 0.0, 0.0,
		 GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 80, 55));*/

		/*this.add(accountStaticText, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 5, 1, 5), 40, 0));
		   this.add(accountChoice, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 2, 1, 0), 0, 5));

		   this.add(isBuyStaticText, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		   this.add(isBuyChoice, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 0), 0, 5));

		   this.add(setPriceStaticText, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 10), 0, 0));
		   this.add(priceEdit, new GridBagConstraints(2, 2, 2, 1, 1.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 30, 0));

		   this.add(totalLotStaticText, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		   this.add(totalLotTextField, new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 0));

		   this.add(closeLotStaticText, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		   this.add(closeLotTextField, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 0));

		   this.add(expireTimeStaticText, new GridBagConstraints(0, 5, 2, 2, 0.0, 0.0
		 , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		   this.add(expireTimeChoice, new GridBagConstraints(2, 5, 2, 1, 0.0, 0.0
		 , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 5));

		   this.add(expireTimeDate, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0
		 , GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 5));

		   scrollPane = new JScrollPane(outstandingOrderTable);
		   outstandingOrderTable.enableRowStripe();
		   this.add(scrollPane, new GridBagConstraints(0, 7, 4, 1, 0.0, 1.0
		 , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 2), 0, 5));

		   this.add(submitButton, new GridBagConstraints(0, 9, 2, 1, 0.0, 0.0
		 , GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 10, 0), 10, 0));
		   this.add(resetButton, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0
		 , GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 10, 0));
		   this.add(exitButton, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0
		 , GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 10, 0));

		   JPanel best5panel = new JPanel();
		   if(!orderTypeIs2) best5panel.setBackground(null);
		   best5panel.setLayout(new GridBagLayout());

		   best5panel.add(this.bestSellTitle, new GridBagConstraints(0, 0, 1, 1, 0, 0
		  , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		   bestSellTable.enableRowStripe();
		   bestSellTable.setShowVerticalLines(false);
		   bestSellTable.setBackground(GridFixedBackColor.relationOrder);
		   bestSellTable.setForeground(GridBackColor.relationOrder);
		   best5panel.add(new JScrollPane(this.bestSellTable), new GridBagConstraints(0, 1, 1, 1, 1.0, 0
		  , GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 95));

		   best5panel.add(this.bestBuyTitle, new GridBagConstraints(1, 0, 1, 1, 0, 0
		  , GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		   bestBuyTable.enableRowStripe();
		   bestBuyTable.setShowVerticalLines(false);
		   bestBuyTable.setBackground(GridFixedBackColor.relationOrder);
		   bestBuyTable.setForeground(GridBackColor.relationOrder);
		   best5panel.add(new JScrollPane(this.bestBuyTable), new GridBagConstraints(1, 1, 1, 1, 1.0, 0
		  , GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 95));

		   best5panel.add(this.timeAndSalesTitle, new GridBagConstraints(0, 2, 2, 1, 0, 0
		  , GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		   timeAndSalesTable.enableRowStripe();
		   timeAndSalesTable.setShowVerticalLines(false);
		   timeAndSalesTable.setBackground(GridFixedBackColor.relationOrder);
		   timeAndSalesTable.setForeground(GridBackColor.relationOrder);
		   best5panel.add(new JScrollPane(this.timeAndSalesTable), new GridBagConstraints(0, 3, 2, 1, 1.0, 1.0
		  , GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 136));

		   this.add(best5panel, new GridBagConstraints(4, 0, 1, 10, 1.0, 1.0
		  , GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 10, 10, 5), 160, 0));*/

		this.add(this.bestSellTitle, new GridBagConstraints(0, 0, 2, 1, 0, 0
			, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		bestSellTable.enableRowStripe();
		bestSellTable.setShowVerticalLines(false);
		bestSellTable.setBackground(GridFixedBackColor.relationOrder);
		bestSellTable.setForeground(GridBackColor.relationOrder);
		this.add(new JScrollPane(this.bestSellTable), new GridBagConstraints(0, 1, 2, 1, 1.0, 0
			, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 80, 95));

		this.add(this.bestBuyTitle, new GridBagConstraints(2, 0, 2, 1, 0, 0
			, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		bestBuyTable.enableRowStripe();
		bestBuyTable.setShowVerticalLines(false);
		bestBuyTable.setBackground(GridFixedBackColor.relationOrder);
		bestBuyTable.setForeground(GridBackColor.relationOrder);
		this.add(new JScrollPane(this.bestBuyTable), new GridBagConstraints(2, 1, 2, 1, 1.0, 0
			, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 80, 95));

		this.add(this.timeAndSalesTitle, new GridBagConstraints(4, 0, 4, 1, 0, 0
			, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 0, 0));
		timeAndSalesTable.enableRowStripe();
		timeAndSalesTable.setShowVerticalLines(false);
		timeAndSalesTable.setBackground(GridFixedBackColor.relationOrder);
		timeAndSalesTable.setForeground(GridBackColor.relationOrder);
		this.add(new JScrollPane(this.timeAndSalesTable), new GridBagConstraints(4, 1, 4, 1, 1.0, 1.0
			, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 95));

		scrollPane = new JScrollPane(outstandingOrderTable);
		outstandingOrderTable.enableRowStripe();
		this.add(scrollPane, new GridBagConstraints(4, 2, 4, 7, 0.0, 1.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 10, 5, 2), 0, 5));

		this.add(accountStaticText, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 5, 1, 5), 40, 0));
		this.add(accountChoice, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 2, 1, 0), 0, 5));

		this.add(isBuyStaticText, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		this.add(isBuyChoice, new GridBagConstraints(2, 3, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 0), 0, 5));

		this.add(setPriceStaticText, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 10), 0, 0));
		this.add(priceEdit, new GridBagConstraints(2, 4, 2, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 30, 0));

		this.add(totalLotStaticText, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		this.add(totalLotTextField, new GridBagConstraints(2, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 0));

		this.add(closeLotStaticText, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		this.add(closeLotTextField, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 0));

		this.add(expireTimeStaticText, new GridBagConstraints(0, 7, 2, 2, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(expireTimeChoice, new GridBagConstraints(2, 7, 2, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 5));

		this.add(expireTimeDate, new GridBagConstraints(2, 8, 2, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 2), 0, 5));

		this.add(submitButton, new GridBagConstraints(0, 9, 4, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 6, 10, 0), 10, 0));

		this.add(resetButton, new GridBagConstraints(4, 9, 2, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 10, 10, 0), 10, 0));
		this.add(closeAllButton, new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 10, 0));
		this.add(exitButton, new GridBagConstraints(7, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 10, 0));
	}

	public MatchingOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
							 OpenContractForm openContractForm, boolean isBuy, MakeLimitOrder makeLimitOrder)
	{
		this(parent, tradingConsole, settingsManager, instrument, order, openContractForm, isBuy, makeLimitOrder, false);
	}

	public MatchingOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
							 OpenContractForm openContractForm, boolean isBuy, MakeLimitOrder makeLimitOrder, boolean orderTypeIs2)
	{
		this(parent, tradingConsole, settingsManager, instrument, order,
							 openContractForm, isBuy, makeLimitOrder, orderTypeIs2, null);
	}

	public MatchingOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
							 OpenContractForm openContractForm, boolean isBuy, MakeLimitOrder makeLimitOrder, boolean orderTypeIs2, Boolean closeAllSell)
	{
		this(instrument, orderTypeIs2);
		this._residedWindow = parent;
		this._makeLimitOrder = makeLimitOrder;

		this._openContractForm = openContractForm;
		this._closeAllSell = closeAllSell;

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._order = order;
		this.outstandingOrderTable.setEditable(this._order == null);

		InstrumentPriceProvider instrumentPriceProvider = new InstrumentPriceProvider(this._instrument);
		this.bidButton.set_PriceProvider(instrumentPriceProvider);
		this.askButton.set_PriceProvider(instrumentPriceProvider);
		this.bidButton.setEnabled(false);
		this.askButton.setEnabled(false);

		this.accountStaticText.setText(Language.OrderLMTlblAccountCodeA);
		this.isBuyStaticText.setText(Language.OrderLMTlblIsBuyA);
		this.setPriceStaticText.setText( (this._order != null) ? Language.OrderLMTlblSetPriceA3 : Language.OrderLMTlblSetPriceA);
		this.totalLotStaticText.setText(Language.OrderLMTlblLot);
		this.closeLotStaticText.setText(Language.CloseLot);
		this.expireTimeStaticText.setText(Language.ExpireOrderPrompt);
		this.resetButton.setText(Language.OrderLMTbtnReset);
		this.submitButton.setText(Language.OrderLMTbtnSubmit);
		this.closeAllButton.setText(Language.CloseAll);
		this.exitButton.setText(Language.OrderLMTbtnExit);

		this.bestBuyTitle.setText(Language.BestBuy);
		this.bestSellTitle.setText(Language.BestSell);
		this.timeAndSalesTitle.setText(Language.TimeAndSales);

		this.accountChoice.setEditable(false);
		this.isBuyChoice.setEditable(false);
		this.expireTimeChoice.setEditable(false);

		//init structure
		//this.imstrumentDesciptionStaticText.setText(this._instrument.get_Description());
		//this.setTitle(Language.limitOrderFormTitle + "-" + this._instrument.get_Description());


		//this.fillExpireTime();
		this.fillAccount();
		this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		this.fillIsBuyChoice();
		this.addModelForSetPriceEdit();
		this.fillDefaultSetPrice();

		boolean isBuy2 = (this._order != null) ? !this._order.get_IsBuy() : isBuy;
		if(this._closeAllSell != null) isBuy2 = this._closeAllSell ? true : false;
		this.changeColor(isBuy2);
		this.isBuyChoice.disableItemEvent();
		this.isBuyChoice.setSelectedIndex( (isBuy2) ? 0 : 1);
		this.isBuyChoice.enableItemEvent();
		//if (Parameter.goodTillMonthType == 1)
		//{
		//	this.expireTimeChoice.setSelectedIndex(0);
		//	//this.expireTimeChoice_OnChange();
		//}

		this.fillExpireTime();
		if (this._order == null)
		{
			if (this._makeOrderAccount != null)
			{
				this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, false, true);
			}
			this.totalLotTextField.setText("");
		}
		this.lotNumericValidation(false);
		this.updateSubmitButtonStatus();
		this.expireTimeDate.initialize(this._settingsManager, this._instrument);
		this.rebind();
		this.fillBestPendings();
		this.fillTimeAndSales();
	}

	public Boolean get_CloseAllSell()
	{
		return this._closeAllSell;
	}

	private void addModelForSetPriceEdit()
	{
		PriceSpinnerModelForMatching model = new PriceSpinnerModelForMatching(this._instrument, this);
		this.priceEdit.setModel(model);
		for (Component component : this.priceEdit.getEditor().getComponents())
		{
			if (component instanceof JFormattedTextField)
			{
				( (JFormattedTextField)component).setEditable(true);
				break;
			}
		}
	}

	public void refreshPrice()
	{
		this.bidButton.updatePrice();
		this.askButton.updatePrice();
	}

	private void fillDefaultSetPrice()
	{
		boolean isBuy = this.getIsBuy();
		Price bid = this._instrument.get_LastQuotation().get_Bid();
		Price ask = this._instrument.get_LastQuotation().get_Ask();
		int acceptLmtVariation = this._instrument.get_NumeratorUnit();
		//Fill Limit Price
		Price price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);

		this.priceEdit.setValue(Price.toString(price));
	}

	private void autoCompleteBy(boolean isBuy, BestPending bestPending)
	{
		if(this._order != null && this._order.get_IsBuy() != isBuy) return;

		if(this._order == null && this.getIsBuy() == isBuy)
		{
			this.reset();
			int selectedIndex = isBuy ? 1 : 0;
			this.isBuyChoice.setSelectedIndex(selectedIndex);
			this.priceEdit.setValue(bestPending.get_Price());
			this.priceEdit.setEnabled(true);

			BigDecimal lot = new BigDecimal(bestPending.get_Quantity());
			Account account = this._makeOrderAccount.get_Account();
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());
			lot = AppToolkit.fixLot(lot, true, tradePolicyDetail, account);
			String qunatity = AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument);
			this.totalLotTextField.setText(qunatity);
		}
		else
		{
			this.priceEdit.setValue(bestPending.get_Price());
		}
		this.submitButton.setEnabled(true);
	}

	private void fillAccount()
	{
		this.accountChoice.disableItemEvent();
		this.reallyFillAccount();
		this.accountChoice.enableItemEvent();
	}

	private MakeOrderAccount getMakeOrderAccount(Guid accountId)
	{
		return this._makeLimitOrder.get_MakeOrderAccount(accountId);
	}

	private HashMap<Guid, MakeOrderAccount> getMakeOrderAccounts()
	{
		return this._makeLimitOrder.get_MakeOrderAccounts();
	}

	private void reallyFillAccount()
	{
		this.accountChoice.removeAllItems();
		if (this._order != null)
		{
			MakeOrderAccount makeOrderAccount = this.getMakeOrderAccount(this._order.get_Transaction().get_Account().get_Id());
			TradePolicyDetail tradePolicyDetail =
					this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(), this._instrument.get_Id());
			if (tradePolicyDetail.get_CanPlaceMatchOrder())
			{
				makeOrderAccount.set_IsPlacMatchOrder(true);
				this.accountChoice.addItem(this._order.get_AccountCode(), makeOrderAccount);
			}
		}
		else
		{
			for (Iterator<MakeOrderAccount> iterator = this.getMakeOrderAccounts().values().iterator(); iterator.hasNext(); )
			{
				MakeOrderAccount makeOrderAccount = iterator.next();
				TradePolicyDetail tradePolicyDetail =
					this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(), this._instrument.get_Id());
				if (tradePolicyDetail.get_CanPlaceMatchOrder())
				{
					makeOrderAccount.set_IsPlacMatchOrder(true);
					this.accountChoice.addItem(makeOrderAccount.get_Account().get_Code(), makeOrderAccount);
				}
			}
		}
		//this.accountChoice.sort(true);
	}

	private void reset()
	{
		this.totalLotTextField.setText(this.getFormatDefaultLot());
		this.closeLotTextField.setText("");
		this.fillDefaultSetPrice();

		this.fillDefaultValueForOutstandingOrder(this._order != null);

		if (this._order != null)
		{
			boolean isBuy = this.getIsBuy();
			this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
				this._instrument));
		}
		int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsBuy);
		if (column != -1)
		{
			TableColumnChooser.hideColumn(this.outstandingOrderTable, column);
		}

		this.updateSubmitButtonStatus();
	}

	private void fillDefaultValueForOutstandingOrder(boolean isSelectedRelationOrder)
	{
		this._makeOrderAccount.reset(isSelectedRelationOrder, false, true);
	}

	private void updateSubmitButtonStatus()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(totalLotTextField.getText());
		Price setPrice = Price.parse( (String)this.priceEdit.getValue(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		submitButton.setEnabled(lot.compareTo(BigDecimal.ZERO) > 0 && setPrice != null);
		if (setPrice == null)
		{
			this.fillDefaultSetPrice();
		}
	}

	public void lotNumeric_focusLost(FocusEvent e)
	{
		if (e.getOppositeComponent() != this.resetButton)
		{
			this.lotNumericValidation(true);
		}
	}

	private void lotNumericValidation(boolean popupAlter)
	{
		boolean isOpen = this._makeOrderAccount.getSumLiqLots(this.getIsBuy()).compareTo(BigDecimal.ZERO) <= 0;

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			lot = this.getDefaultLot();
		}
		else
		{
			BigDecimal maxLot = this._instrument.get_MaxOtherLot();
			if (maxLot.compareTo(BigDecimal.ZERO) != 0 && lot.compareTo(maxLot) > 0)
			{
				if (popupAlter)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true,
											   Language.OrderLMTPagetextLot_OnblurAlert0 + "(" + AppToolkit.getFormatLot(maxLot, true) + ")!");
				}
				lot = this.getDefaultLot();
			}
			else
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
			}
		}
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		BigDecimal lot2 = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, this._makeOrderAccount.get_Account());
		String formattedLot = AppToolkit.getFormatLot(lot2, this._makeOrderAccount.get_Account(), this._instrument);
		if (StringHelper.isNullOrEmpty(formattedLot))
		{
			formattedLot = "0";
		}
		if (lot.compareTo(new BigDecimal(formattedLot)) != 0 && popupAlter)
		{
			String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
											  {lot, formattedLot});
			AlertDialogForm.showDialog(this.getFrame(), null, true, info);
		}
		this.totalLotTextField.setText(formattedLot);

		//input lot < sumLiqLots, clear liqLots....
		boolean isBuy = this.getIsBuy();
		if (lot.compareTo(this._makeOrderAccount.getSumLiqLots(isBuy)) < 0)
		{
			if (this._order != null)
			{
				this._lastAccountLot = lot;
				this.closeLotTextField.setText(this.totalLotTextField.getText());
			}
			else
			{
				if (popupAlter)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
				}
				this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, false, true);
			}
		}
	}

	private BigDecimal getDefaultLot()
	{
		Account account = this._makeOrderAccount.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());
		boolean isOpen = true;
		return AppToolkit.getDefaultLot(this._instrument, isOpen, tradePolicyDetail, account);
	}

	private boolean getIsBuy()
	{
		if(this._closeAllSell != null) return this._closeAllSell ? true : false;

		if (this.isBuyChoice.getSelectedIndex() == -1)
		{
			this.isBuyChoice.setSelectedIndex(0);
		}
		return (Boolean)this.isBuyChoice.getSelectedValue();
	}

	/**
	 * fillExpireTime
	 */
	private void fillExpireTime()
	{
		this.expireTimeChoice.disableItemEvent();
		this.reallyFillExpireTimeChoice();
		this.expireTimeChoice.enableItemEvent();
	}

	private void reallyFillExpireTimeChoice()
	{
		//this.expireTimeDate.setDate(null);
		this._expireTime = null;
		this.expireTimeChoice.removeAllItems();
		if (this._makeOrderAccount == null)
		{
			return;
		}
		if (this._instrument.isFromBursa())
		{
			this.expireTimeChoice.addItem(Language.GoodTillMonthDayOrder, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.GoodTillCancel, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.ImmediateOrCancel, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.GoodTillDate, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.GoodTillMonthSession, DateTime.maxValue);
			return;
		}

		DateTime tradeDay = this._settingsManager.get_TradeDay().get_TradeDay();
		//this._settingsManager.get_TradeDay()
		TimeSpan timePart = this._settingsManager.get_TradeDay().get_BeginTime().get_TimeOfDay();

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		//if (Parameter.isHasGoodTillMonthDayOrder)
		if (tradePolicyDetail.get_GoodTillMonthDayOrder())
		{
			DateTime dayOrderDateTime = this._instrument.get_DayCloseTime();
			if (dayOrderDateTime == null || dayOrderDateTime == DateTime.maxValue)
			{
				dayOrderDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthDayOrder, dayOrderDateTime);
		}
		//if (Parameter.isHasGoodTillMonthSession)
		if (tradePolicyDetail.get_GoodTillMonthSession())
		{
			DateTime sessionDateTime = this._instrument.get_CloseTime();
			if (sessionDateTime == null || sessionDateTime == DateTime.maxValue)
			{
				sessionDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthSession, sessionDateTime);
		}
		//if (Parameter.isHasGoodTillMonthGTM)
		if (tradePolicyDetail.get_GoodTillMonthGTM())
		{
			DateTime addOneMonthTradeDay = tradeDay.addMonths(1);
			DateTime gtmDateTime = addOneMonthTradeDay.addDays( -1).add(timePart);
			this.expireTimeChoice.addItem(Language.GoodTillMonthGTM, gtmDateTime);
		}
		//if (Parameter.isHasGoodTillMonthGTF)
		if (tradePolicyDetail.get_GoodTillMonthGTF())
		{
			this.expireTimeChoice.addItem(Language.GoodTillMonthGTF, this._instrument.get_LastTradeDay());
		}
		if (tradePolicyDetail.get_GoodTillDate())
		{
			this.expireTimeChoice.addItem(Language.GoodTillDate, tradeDay);
		}

		if (this.expireTimeChoice.getItemCount() > 0)
		{
			this.expireTimeChoice.setSelectedIndex(0);

			DateTime date = (DateTime)this.expireTimeChoice.getSelectedValue();
			this._expireTime = date;
		}
	}

	private JDialog getFrame()
	{
		return this._residedWindow;
	}

	private void fillIsBuyChoice()
	{
		this.isBuyChoice.disableItemEvent();
		this.reallyFillIsBuyChoice();
		this.isBuyChoice.enableItemEvent();
	}

	private void changeColor(boolean isBuy)
	{
		Color color = BuySellColor.getColor(isBuy, false);
		this.priceEdit.setForeground(color);
		this.totalLotTextField.setForeground(color);
		//special process,otherwise color will ineffective
		this.totalLotTextField.setText(this.totalLotTextField.getText());
	}

	private void reallyFillIsBuyChoice()
	{
		this.isBuyChoice.removeAllItems();
		this.isBuyChoice.addItem(Language.LongBuy, true);
		this.isBuyChoice.addItem(Language.LongSell, false);
	}

	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		accountLot = AppToolkit.fixLot(accountLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		BigDecimal accountLot2 = accountLot;
		if (currentLot.compareTo(accountLot) >= 0)
		{
			accountLot2 = currentLot;
			if (accountLot.compareTo(this._lastAccountLot) < 0)
			{
				accountLot2 = accountLot2.subtract(this._lastAccountLot.subtract(accountLot));
			}
		}
		this._lastAccountLot = accountLot;

		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}

		if (currentLot.compareTo(accountLot2) != 0 && this.totalLotTextField.isVisible())
		{
			try
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(accountLot2, this._makeOrderAccount.get_Account(), this._instrument));
				//this.totalLotTextField.setEnabled(accountLot.compareTo(BigDecimal.ZERO) <= 0);
			}
			catch (IllegalStateException exception)
			{

			}
		}
		this.updateSubmitButtonStatus();
	}

	public void dispose2()
	{
		if (this._openContractForm != null)
		{
			this._openContractForm.dispose();
		}
		this.getFrame().dispose();
	}

	private void fillTimeAndSales()
	{
		if (this._makeOrderAccount != null)
		{
			BindingSource timeAndSales = this._instrument.get_TimeAndSales().get_TimeAndSales(this._makeOrderAccount.get_Account().get_Id());
			this.timeAndSalesTable.setModel(timeAndSales);
			TradingConsole.bindingManager.setHeader(timeAndSales.get_DataSourceKey(), SwingConstants.CENTER, 25,
				GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);

			this.timeAndSalesTable.setShowVerticalLines(false);
			this.timeAndSalesTable.setShowHorizontalLines(false);
		}
	}

	private void fillBestPendings()
	{
		if (this._makeOrderAccount != null)
		{
			Guid accountId = this._makeOrderAccount.get_Account().get_Id();
			this.bestBuyTable.setModel(this._instrument.get_BestPendings().getBestBuys(accountId));
			TradingConsole.bindingManager.setHeader(this._instrument.get_BestPendings().getBestBuys(accountId).get_DataSourceKey(), SwingConstants.CENTER, 25,
				GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);

			this.bestSellTable.setModel(this._instrument.get_BestPendings().getBestSells(accountId));
			TradingConsole.bindingManager.setHeader(this._instrument.get_BestPendings().getBestSells(accountId).get_DataSourceKey(), SwingConstants.CENTER, 25,
				GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);

			this.bestBuyTable.setShowVerticalLines(false);
			this.bestBuyTable.setShowHorizontalLines(false);

			this.bestSellTable.setShowVerticalLines(false);
			this.bestSellTable.setShowHorizontalLines(false);
		}
	}

	private void rebind()
	{
		this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		//init outstanding Order Grid
		boolean isBuy = this.getIsBuy();
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

		BuySellType buySellType = this._closeAllSell == null ? BuySellType.Both : (this._closeAllSell ? BuySellType.Sell : BuySellType.Buy);
		boolean relationsHasChange = this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, buySellType,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);

		if (relationsHasChange)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
			if (this._order != null)
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
					this._instrument));
			}
			this._lastAccountLot = BigDecimal.ZERO;
		}

		if (this.outstandingOrderTable.getSelectedRow() == -1 && this.outstandingOrderTable.getRowCount() > 0)
		{
			this.outstandingOrderTable.changeSelection(0, 0, false, false);
		}

		this.updateCloseLotVisible(false);
		this.accountChoice.setBackground( (this._order == null) ? Color.white : Color.gray);
		this.isBuyChoice.setBackground( (this._order == null) ? Color.white : Color.gray);
		this.accountChoice.setEnabled(this._order == null);
		this.isBuyChoice.setEnabled(this._order == null);
		this.expireTimeVisible(true);

		//this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		//this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (currentLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
		}
		this.updateSubmitButtonStatus();

		this.setPriceStaticText.setText(Language.OrderLMTlblSetPriceA3);
		if (this._openContractForm != null)
		{
			this._openContractForm.rebind();
		}
	}

	private void updateCloseLotVisible(boolean resetText)
	{
		this.totalLotTextField.setEnabled(true);
		if (resetText)
		{
			this.totalLotTextField.setText("");
		}
		//this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		//this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);
	}

	private String getFormatDefaultLot()
	{
		BigDecimal defaultValue = this.getDefaultLot();
		return AppToolkit.getFormatLot(defaultValue, this._makeOrderAccount.get_Account(), this._instrument);
	}

	private void updateTotalColseLot(BigDecimal totalCloseLot)
	{
		if (totalCloseLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			totalCloseLot = AppToolkit.fixLot(totalCloseLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
		}

		if (totalCloseLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			totalCloseLot = this.getDefaultLot();
		}

		this.totalLotTextField.setText(AppToolkit.getFormatLot(totalCloseLot, this._makeOrderAccount.get_Account(), this._instrument));
	}

	private JTextField getCloseLotEditor()
	{
		if (this.outstandingOrderTable.getRowCount() > 0)
		{
			return this.closeLotTextField;
		}
		else
		{
			return this.totalLotTextField;
		}
	}

	void setPriceEdit_focusLost(FocusEvent e)
	{
		boolean isPrompt = e.getOppositeComponent() != this.resetButton;
		this.setPrice_FocusLost(isPrompt);
	}

	void setPrice_FocusLost(boolean isPrompt)
	{
		this.updateSubmitButtonStatus();
	}

	void setPriceEdit_focusGained(FocusEvent e)
	{
		//String text = priceEdit.getText();
		//priceEdit.select( ( (text.indexOf(".") != -1) ? text.indexOf(".") + 1 : 0), text.length());
	}

	private void expireTimeVisible(boolean isVisible)
	{
		if (!isVisible)
		{
			this.expireTimeStaticText.setVisible(isVisible);
			this.expireTimeChoice.setVisible(isVisible);
			this.expireTimeDate.setVisible(isVisible);
		}
		else
		{
			this.expireTimeStaticText.setVisible(isVisible);

			this.expireTimeDate.setVisible(false);
			this.expireTimeDate.setEnabled(false);
			this.expireTimeChoice.setVisible(true);
			if (this.expireTimeChoice.getSelectedIndex() != -1)
			{
				String caption = this.expireTimeChoice.getSelectedText();
				if (caption.equalsIgnoreCase(Language.GoodTillDate))
				{
					this.expireTimeDate.setVisible(true);
					this.expireTimeDate.setEnabled(true);
				}
			}
		}
		this.doLayout();
	}

	void expireTimeChoice_actionPerformed(ItemEvent e)
	{
		DateTime date = null;
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			date = (DateTime)this.expireTimeChoice.getSelectedValue();
			String caption = this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this.expireTimeDate.setVisible(true);
				//this.expireTimeDate.setValue(date);
			}
			this.expireTimeVisible(true);
		}
		else
		{
			//this.expireTimeDate.setDate(null);
			this.expireTimeDate.setVisible(false);
		}
		this._expireTime = date;

	}

	void isBuyChoice_actionPerformed(ItemEvent e)
	{
		this.isBuyChoice_OnChange();
	}

	private void isBuyChoice_OnChange()
	{
		this._closeAllSell = null;
		boolean isBuy = this.getIsBuy();
		this.changeColor(isBuy);

		boolean oldIsBuy = this._makeOrderAccount.get_IsBuyForCurrent();
		BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;

		this._makeLimitOrder.setDefaultBuySellType(buySellType);

		if (oldIsBuy != isBuy)
		{
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
			this._lastAccountLot = BigDecimal.ZERO;
			this.updateCloseLotVisible(true);
		}
		if (isBuy)
		{
			this._makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
		}
		else
		{
			this._makeOrderAccount.set_SellLot(BigDecimal.ZERO);
		}
		this.totalLotTextField.setText(this.getFormatDefaultLot());
		this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, false, true);
		//isBuy = !isBuy;
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		this.closeLotTextField.setText("");
		this.fillDefaultSetPrice();
	}

	void accountChoice_actionPerformed(ItemEvent e)
	{
		this.fillBestPendings();
		this.fillTimeAndSales();
		MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();
		if (! (this._makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())
			   && this._makeOrderAccount.get_IsBuyForCurrent() == isBuy))
		{
			this._makeOrderAccount = makeOrderAccount;
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
			this._lastAccountLot = BigDecimal.ZERO;
			this.updateCloseLotVisible(true);
			this.isBuyChoice_OnChange();
			this.closeLotTextField.setText("");
		}
		this.fillExpireTime();
	}

	void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	void closeAllButton_actionPerformed(ActionEvent e)
	{
		this.closeAll();
	}

	private String checkPrice()
	{
		boolean isBuy = this.getIsBuy();
		Price setPrice = Price.parse( (String)this.priceEdit.getValue(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		TradePolicyDetail tradePolicyDetail = this._makeOrderAccount.getTradePolicyDetail();
		if (setPrice == null)
		{
			return "[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert1;
		}
		Price marketPrice = this._instrument.get_LastQuotation().getBuySell(isBuy);
		double dblMarketPrice = Price.toDouble(marketPrice);
		if (Math.abs(Price.toDouble(setPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
		{
			return Language.SetPriceTooCloseOrTooFarToMarket;
		}
		return null;
	}

	private boolean isValidOrder(boolean isPrompt)
	{
		boolean isValidOrder = false;

		OrderType orderType = OrderType.Limit;
		if (!MakeOrder.isAllowOrderType(this._instrument, orderType))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return isValidOrder;
		}

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert6);
			}
			return isValidOrder;
		}
		else if (lot.compareTo(this._instrument.get_MaxOtherLot()) > 0)
		{
			if (isPrompt)
			{
				String info = Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 + this._instrument.get_MaxOtherLot() + ")";
				AlertDialogForm.showDialog(this.getFrame(), null, true, info);
			}
			return isValidOrder;
		}

		boolean isBuy = this.getIsBuy();
		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(isBuy);
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			if (lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert7);

					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
				}
				return false;
			}
		}
		else if(!this._makeOrderAccount.get_Account().get_AllowAddNewPosition()
			|| !this._instrument.getAllowAddNewPosition(isBuy))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.NewPositionIsNotAllowed);
			}
			return false;
		}


		BigDecimal[] sumLot = this._makeOrderAccount.getSumLotBSForOpenOrder();
		//if (lot - liqLots > 0)
		{
			//when instrumentCode.substr(0,1)=="#"
			boolean isHasMakeNewOrder = lot.subtract(liqLots).compareTo(BigDecimal.ZERO) > 0;
			if (!this._makeOrderAccount.isAcceptLot(isBuy, lot, isHasMakeNewOrder)) //new BigDecimal( ( (Double) (lot - liqLots)).doubleValue())))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.NewOrderAcceptedHedging);
				}
				return isValidOrder;
			}
		}

		Price setPrice = Price.parse( (String)this.priceEdit.getValue(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		String errorMessage = this.checkPrice();
		if (!StringHelper.isNullOrEmpty(errorMessage))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, errorMessage);
				this.fillDefaultSetPrice();
			}
			return isValidOrder;
		}

		BigDecimal lot2 = lot;
		BigDecimal liqLots2 = liqLots;
		Object[] result = MakeOrder.isAcceptEntrance(this._settingsManager, this._makeOrderAccount.get_Account(), this._instrument, OrderType.Limit, isBuy,
			lot2, liqLots2, setPrice, null, false);
		if (! ( (Boolean)result[0]))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, (String)result[1]);
			}
			return isValidOrder;
		}

		return true;
	}

	private void convertInputGoodTillMonth()
	{
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			DateTime date = (DateTime) (this.expireTimeChoice.getSelectedValue());
			String caption = this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this._expireTime = this.expireTimeDate.getExpireTime();
			}
			else
			{
				this._expireTime = date;
			}

			this._expireType = ExpireType.GTD;

			//if(this._instrument.isFromBursa())
			{
				if (caption.equalsIgnoreCase(Language.GoodTillDate))
				{
					this._expireType = ExpireType.GTD;
				}
				else if (caption.equalsIgnoreCase(Language.GoodTillMonthDayOrder))
				{
					this._expireType = ExpireType.Day;
				}
				if (caption.equalsIgnoreCase(Language.GoodTillCancel))
				{
					this._expireType = ExpireType.GTC;
				}
				if (caption.equalsIgnoreCase(Language.ImmediateOrCancel))
				{
					this._expireType = ExpireType.IOC;
				}
				if (caption.equalsIgnoreCase(Language.GoodTillMonthSession))
				{
					this._expireType = ExpireType.Session;
				}
			}
			//else
			//{

			//}
		}
	}

	private void closeAll()
	{
		BigDecimal lot = this._makeOrderAccount.closeAll();
		BuySellType buySell = this._makeOrderAccount.GetBuySellForCloseAll();
		this.updateAccount(lot, buySell==BuySellType.Buy ? true : false);
	}

	private void submit()
	{
		if (!this.isValidOrder(true))
		{
			return;
		}

		this.convertInputGoodTillMonth();
		if (this._expireTime == null)
		{
			return;
		}

		Price setPrice = Price.parse( (String)this.priceEdit.getValue(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		this.priceEdit.setValue(Price.toString(setPrice));

		boolean isBuy = this.getIsBuy();
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		OperateType operateType = OperateType.Limit;

		this._makeOrderAccount.set_BuyLot(lot);
		this._makeOrderAccount.set_BuySetPrice( (isBuy) ? setPrice : null);
		this._makeOrderAccount.set_SellLot(lot);
		this._makeOrderAccount.set_SellSetPrice( (isBuy) ? null : setPrice);
		this._makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

		TradeOption tradeOption = TradeOption.Better;
		this._makeOrderAccount.set_BuyTradeOption( (isBuy) ? tradeOption : TradeOption.None);
		this._makeOrderAccount.set_SellTradeOption( (isBuy) ? TradeOption.None : tradeOption);

		HashMap<Guid, MakeOrderAccount> makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
		makeOrderAccounts.put(this._makeOrderAccount.get_Account().get_Id(), this._makeOrderAccount);

		//PalceLotNnemonic.set(this._instrument.get_Id(), lot);

		VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this.getFrame(), "Verification Order", true, this._tradingConsole,
			this._settingsManager, this._instrument, makeOrderAccounts /*this.getMakeOrderAccounts()*/, OrderType.Limit,
			operateType, false, null, this._expireTime, this._expireType, false); //date);
		//verificationOrderForm.show();
	}

	void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	void exitButton_actionPerformed(ActionEvent e)
	{
		this.getFrame().dispose();
	}

	void outstandingOrderTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.outstandingOrderTable.requestFocus();
		}
	}

	void lotNumeric_focusGained(FocusEvent e)
	{
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
	}

	public boolean isBuy()
	{
		return this.getIsBuy();
	}

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private MatchingOrderForm _owner;

		public OpenCloseRelationSite(MatchingOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return OperateType.Limit;
		}

		public Instrument getInstrument()
		{
			return this._owner._instrument;
		}

		public MakeOrderAccount getMakeOrderAccount()
		{
			return this._owner._makeOrderAccount;
		}

		public BigDecimal getLot()
		{
			return AppToolkit.convertStringToBigDecimal(this._owner.totalLotTextField.getText());
		}

		public PVStaticText2 getLiqLotValueStaticText()
		{
			return null;
		}

		public BigDecimal getTotalQuantity()
		{
			return BigDecimal.ZERO;
		}

		public DataGrid getRelationDataGrid()
		{
			return this._owner.outstandingOrderTable;
		}

		public JDialog getFrame()
		{
			return this._owner.getFrame();
		}

		public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
		{
			this._owner.updateAccount(accountLot, openOrderIsBuy);
		}

		public OrderType getOrderType()
		{
			return OrderType.Limit;
		}

		public Boolean isMakeLimitOrder()
		{
			return true;
		}

		public void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			//Order type doesn't change
		}

		public void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			//Order type doesn't change
		}

		public JTextField getCloseLotEditor()
		{
			return this._owner.getCloseLotEditor();
		}

		public JTextField getTotalLotEditor()
		{
			return this._owner.totalLotTextField;
		}

		public void updateTotalColseLot(BigDecimal totalCloseLot)
		{
			this._owner.updateTotalColseLot(totalCloseLot);
		}

		public boolean allowChangeCloseLot()
		{
			return false;
		}

		public DataGrid getAccountDataGrid()
		{
			return null;
		}

		public void rebind()
		{
			if (this._owner._order != null
				&& this._owner._order.getAvailableLotBanlance(false, null).compareTo(BigDecimal.ZERO) == 0)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.DisposedForOpenOrderClosed);
				this._owner.dispose2();
			}
			else
			{
				this._owner.rebind();
			}
		}
	}

	private static class BestLimitTableActionListener implements IActionListener
	{
		private MatchingOrderForm _owner;

		public BestLimitTableActionListener(MatchingOrderForm owner)
		{
			this._owner = owner;
		}

		public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
		{
			boolean isBuy = e.get_Source() == this._owner.bestBuyTable ? true : false;
			if (e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
			{
				BestPending bestPending = (BestPending)e.get_Object();
				this._owner.autoCompleteBy(isBuy, bestPending);
			}
		}
	}
}

class MatchingOrderForm_setPriceEdit_focusAdapter extends FocusAdapter
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_setPriceEdit_focusAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.setPriceEdit_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.setPriceEdit_focusGained(e);
	}
}

class MatchingOrderForm_expireTimeChoice_actionAdapter implements ItemListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_expireTimeChoice_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.expireTimeChoice_actionPerformed(e);
	}
}

class MatchingOrderForm_isBuyChoice_actionAdapter implements ItemListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_isBuyChoice_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.isBuyChoice_actionPerformed(e);
	}
}

class MatchingOrderForm_accountChoice_actionAdapter implements ItemListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_accountChoice_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.accountChoice_actionPerformed(e);
	}
}

class MatchingOrderForm_closeAllButton_actionAdapter implements ActionListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_closeAllButton_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.closeAllButton_actionPerformed(e);
	}
}

class MatchingOrderForm_submitButton_actionAdapter implements ActionListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_submitButton_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class MatchingOrderForm_resetButton_actionAdapter implements ActionListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_resetButton_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class MatchingOrderForm_exitButton_actionAdapter implements ActionListener
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_exitButton_actionAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class MatchingOrderForm_outstandingOrderTable_keyAdapter extends KeyAdapter
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_outstandingOrderTable_keyAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.outstandingOrderTable_keyPressed(e);
	}
}

class MatchingOrderForm_lotNumeric_focusAdapter extends FocusAdapter
{
	private MatchingOrderForm adaptee;
	MatchingOrderForm_lotNumeric_focusAdapter(MatchingOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.lotNumeric_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.lotNumeric_focusGained(e);
	}
}
