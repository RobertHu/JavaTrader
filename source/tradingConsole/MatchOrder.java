package tradingConsole;

import java.math.BigDecimal;
import java.awt.Color;
import java.util.Collection;

import framework.Guid;
import framework.data.DataRow;

import tradingConsole.settings.SettingsManager;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.language.MatchOrderLanguage;
import tradingConsole.ui.columnKey.MatchOrderColKey;
import tradingConsole.ui.colorHelper.CurrentCellColor;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import tradingConsole.ui.borderStyleHelper.BorderStyle;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.columnFixedHelper.RowLabelWidth;
import tradingConsole.ui.colorHelper.SelectionBackground;
import tradingConsole.ui.borderStyleHelper.CurrentCellBorder;
import tradingConsole.ui.colorHelper.GridBackgroundColor;
import tradingConsole.ui.columnKey.OutstandingOrderColKey;
import tradingConsole.ui.colorHelper.BuySellColor;
import tradingConsole.enumDefine.InstrumentCategory;
import framework.xml.XmlAttributeCollection;
import framework.lang.Enum;
import javax.swing.SwingConstants;
import tradingConsole.ui.grid.DataGrid;
import java.awt.Dimension;

public class MatchOrder
{
	public static String matchOrderPanelKey;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private BigDecimal _bidLot;
	private Price _bidPrice;
	private BigDecimal _askLot;
	private Price _askPrice;

	private Instrument _instrument;

	static
	{
		//MatchOrder.matchOrderPanelKey = Guid.newGuid();
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_BidLotString()
	{
		return AppToolkit.getFormatLot(this._bidLot, true);
	}

	public BigDecimal get_BidLot()
	{
		return this._bidLot;
	}

	public String get_BidPriceString()
	{
		String priceString = Price.toString(this._bidPrice);
		return priceString;
	}

	public String get_AskLotString()
	{
		return AppToolkit.getFormatLot(this._askLot, true);
	}

	public BigDecimal get_AskLot()
	{
		return this._askLot;
	}

	public String get_AskPriceString()
	{
		String priceString = Price.toString(this._askPrice);
		return priceString;
	}

	public MatchOrder(TradingConsole tradingConsole, SettingsManager settingsManager,Instrument instrument)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;
	}

	public MatchOrder(TradingConsole tradingConsole, SettingsManager settingsManager,Instrument instrument, DataRow dataRow)
	{
		this(tradingConsole, settingsManager,instrument);
		this._id = new Guid(dataRow.get_Item("ID").toString());
		this.setValue(dataRow);
	}

	public MatchOrder(TradingConsole tradingConsole, SettingsManager settingsManager,Instrument instrument,XmlAttributeCollection instrumentCollection)
	{
		this(tradingConsole, settingsManager,instrument);
		this.setValue(instrumentCollection);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._bidLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("BidLot"), 0.0);
		this._bidPrice = (AppToolkit.isDBNull(dataRow.get_Item("BidPrice"))) ? null :
			Price.parse(dataRow.get_Item("BidPrice").toString(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		this._askLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("AskLot"), 0.0);
		this._askPrice = (AppToolkit.isDBNull(dataRow.get_Item("AskPrice"))) ? null :
			Price.parse(dataRow.get_Item("AskPrice").toString(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
	}

	public void setValue(XmlAttributeCollection collection)
	{
		for (int i = 0; i < collection.get_Count(); i++)
		{
			String nodeName = collection.get_ItemOf(i).get_LocalName();
			String nodeValue = collection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("BidLot"))
			{
				this._bidLot = AppToolkit.convertStringToBigDecimal(nodeValue);
			}
			else if (nodeName.equals("BidPrice"))
			{
				this._bidPrice = Price.parse(nodeValue, this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			}
			else if (nodeName.equals("AskLot"))
			{
				this._askLot =AppToolkit.convertStringToBigDecimal(nodeValue);
			}
			else if (nodeName.equals("AskPrice"))
			{
				this._askPrice = Price.parse(nodeValue, this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			}
		}
	}

	public static PropertyDescriptor[] getPropertyDescriptors(SettingsManager settingsManager)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[5];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(MatchOrder.class, MatchOrderColKey.Id, true, null, MatchOrderLanguage.Id,
			0, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(MatchOrder.class, MatchOrderColKey.BidLotString, true, null, MatchOrderLanguage.BidLotString,
			100, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(MatchOrder.class, MatchOrderColKey.BidPriceString, true, null, MatchOrderLanguage.BidPriceString,
			100, SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(MatchOrder.class, MatchOrderColKey.AskPriceString, true, null, MatchOrderLanguage.AskPriceString,
			100, SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(MatchOrder.class, MatchOrderColKey.AskLotString, true, null, MatchOrderLanguage.AskLotString,
			100, SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(SettingsManager settingsManager, DataGrid grid, String dataSourceKey, Collection dataSource,
												 tradingConsole.ui.grid.BindingSource bindingSource)
	{
		//grid.setRedraw(false);
		//grid.reset();
		grid.setIntercellSpacing(new Dimension(0, 0));
		grid.setBackground(GridFixedBackColor.matchOrderPanel);
		grid.setForeground(GridBackColor.matchOrderPanel);
		//grid.setBorderStyle(BorderStyle.matchOrderPanel);
		//grid.setRowLabelWidth(RowLabelWidth.matchOrderPanel);
		//grid.setSelectionBackground(SelectionBackground.matchOrderPanel);
		//grid.setCurrentCellColor(CurrentCellColor.matchOrderPanel);
		//grid.setCurrentCellBorder(CurrentCellBorder.matchOrderPanel);
		//grid.setRowLabelWidth(0);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, MatchOrder.getPropertyDescriptors(settingsManager));
		grid.setModel(bindingSource);

		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.matchOrderPanel, Color.white, HeaderFont.matchOrderPanel);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.white, Color.blue, true, true,
											  GridFont.matchOrderPanel, false, true, true);
		//grid.setRedraw(true);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void add(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setStyle(dataSourceKey);
		this.setBackground(dataSourceKey,GridBackgroundColor.matchOrder);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setStyle(dataSourceKey);
		this.setBackground(dataSourceKey,GridBackgroundColor.matchOrder);
	}

	public void remove(String dataSourceKey)
	{
		TradingConsole.bindingManager.remove(dataSourceKey, this);
	}

	public static void removeAll(String dataSourceKey)
	{
		TradingConsole.bindingManager.removeAll(dataSourceKey);
	}

	private void setStyle(String dataSourceKey)
	{
		this.setForeground(dataSourceKey, MatchOrderColKey.BidPriceString, Color.red);
		this.setForeground(dataSourceKey, MatchOrderColKey.AskPriceString, Color.blue);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		//should change to array......
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}


}
