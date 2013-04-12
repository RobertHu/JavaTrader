package tradingConsole.ui;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Font;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Instrument;
import java.awt.GridBagLayout;
import java.awt.*;
import tradingConsole.ui.language.Language;
import tradingConsole.settings.MakeOrderWindow;
import tradingConsole.ui.grid.DataGrid;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import javax.swing.JDialog;

public class MatchOrderForm extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Instrument _instrument;

	public MatchOrderForm(JFrame parent)
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

	public MatchOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		this(tradingConsole.get_MainForm());
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;

		this.init();

		this.setMakeOrderWindow();
		this.setNotifyIsAcceptWindow();
	}

	public void setMakeOrderWindow()
	{
		MakeOrderWindow makeOrderWindow = new MakeOrderWindow(this._instrument.get_Id(), this, false);
		this._settingsManager.setMakeOrderWindow(this._instrument, makeOrderWindow);
	}

	private void removeMakeOrderWindow()
	{
		this._settingsManager.removeMakeOrderWindow(this._instrument, this);
	}

	private void setNotifyIsAcceptWindow()
	{
		this._settingsManager.setNotifyIsAcceptWindow(this._instrument, this);
	}

	public void dispose()
	{ //??????
		//this._makeLiquidationOrder.unbindLiquidation();
		this.removeMakeOrderWindow();

		super.dispose();
	}

	public void notifyMakeOrderUiByPrice()
	{
		//????????
		this.setPriceBidStaticText.setText( (!this._instrument.get_IsSinglePrice()) ? this._instrument.get_Bid() : this._instrument.get_Ask());
		this.setPriceAskStaticText.setText(this._instrument.get_Ask());
	}

	private void init()
	{
		this.setTitle(Language.matchOrderFormTitle);

		this.buyButton.setText(Language.LongBuy);
		this.sellButton.setText(Language.LongSell);

		this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());
		this.setPriceBidStaticText.setText(this._instrument.get_Bid());
		this.setPriceBidStaticText.setForeground(Color.red);
		this.setPriceAskStaticText.setText(this._instrument.get_Ask());
		this.setPriceAskStaticText.setForeground(Color.blue);

		this._instrument.initializeMatchOrder(this.enableMatchOrderTable, true, false);

	}

	private void buy()
	{
		this._tradingConsole.get_MainForm().broadLimitOrderProcess(this._instrument,true);
	}

	private void sell()
	{
		this._tradingConsole.get_MainForm().broadLimitOrderProcess(this._instrument,false);
	}

	/*public void paint(Graphics g)
	{
		super.paint(g);
		try
		{
			int x = this.setPriceBidStaticText.getLocation().x - 1;
			int y = this.setPriceBidStaticText.getLocation().y - 1;
			int width = this.setPriceBidStaticText.getSize().width + this.setPriceAskStaticText.getSize().width + this.separatorStaticText.getSize().width;
			int height = this.setPriceBidStaticText.getSize().height + 2;
			g.drawRect(x, y, width, height);
		}
		catch (Throwable exception)
		{}
	}*/

	//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new MatchOrderForm_this_windowAdapter(this));

		this.setSize(450, 250);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		//this.setTitle(Language.MatchDialogFormTitle);
		//this.setBackground(FormBackColor.MatchDialogForm);

		buyButton.setText("Buy");
		buyButton.addActionListener(new MatchOrderForm_buyButton_actionAdapter(this));
		sellButton.setText("Sell");
		sellButton.addActionListener(new MatchOrderForm_sellButton_actionAdapter(this));
		setPriceBidStaticText.setForeground(Color.red);
		setPriceBidStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		setPriceBidStaticText.setText("1.2250");
		setPriceBidStaticText.setAlignment(2);
		setPriceAskStaticText.setForeground(Color.blue);
		setPriceAskStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		setPriceAskStaticText.setText("1.2550");
		instrumentDescriptionStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		instrumentDescriptionStaticText.setText("GBP");
		separatorStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		separatorStaticText.setText("/");
		JScrollPane scrollPane = new JScrollPane(enableMatchOrderTable);
		this.add(buyButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 2), 20, 0));
		this.add(sellButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 2, 5, 2), 20, 0));
		this.add(setPriceBidStaticText, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15, 10, 5, 2), 0, 0));
		this.add(separatorStaticText, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 2, 5, 2), 0, 0));
		this.add(scrollPane, new GridBagConstraints(0, 2, 5, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 10, 10, 10), 0, 0));
		this.add(setPriceAskStaticText, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 2, 5, 2), 0, 0));
		this.add(instrumentDescriptionStaticText, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 10, 5, 2), 78, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVButton2 buyButton = new PVButton2();
	private PVButton2 sellButton = new PVButton2();
	private DataGrid enableMatchOrderTable = new DataGrid("EnableMatchOrderTable");
	private PVStaticText2 setPriceBidStaticText = new PVStaticText2();
	private PVStaticText2 setPriceAskStaticText = new PVStaticText2();
	private PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	private PVStaticText2 separatorStaticText = new PVStaticText2();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void buyButton_actionPerformed(ActionEvent e)
	{
		this.buy();
	}

	public void sellButton_actionPerformed(ActionEvent e)
	{
		this.sell();
	}

	class MatchOrderForm_this_windowAdapter extends WindowAdapter
	{
		private MatchOrderForm adaptee;
		MatchOrderForm_this_windowAdapter(MatchOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class MatchOrderForm_buyButton_actionAdapter implements ActionListener
{
	private MatchOrderForm adaptee;
	MatchOrderForm_buyButton_actionAdapter(MatchOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.buyButton_actionPerformed(e);
	}
}

class MatchOrderForm_sellButton_actionAdapter implements ActionListener
{
	private MatchOrderForm adaptee;
	MatchOrderForm_sellButton_actionAdapter(MatchOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.sellButton_actionPerformed(e);
	}
}
