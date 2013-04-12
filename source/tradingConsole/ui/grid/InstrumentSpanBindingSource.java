package tradingConsole.ui.grid;

import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.grid.*;
import tradingConsole.*;
import tradingConsole.ui.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.language.*;
import tradingConsole.settings.PublicParametersManager;

public class InstrumentSpanBindingSource extends DefaultStyleTableModel implements HierarchicalTableModel, SpanModel
{
	private CellStyle _descriptionCellStyle;
	private Font _defaultFont = new Font("SansSerif", Font.PLAIN, 14);
	private Font _descriptionFont = new Font("SansSerif", Font.BOLD, 16);
	private Font _bidAskRightFont = new Font("SansSerif", Font.BOLD, 28);

	private ArrayList<Instrument> _instruments;
	private HashMap<Instrument, Color> _instrumentToBackground = new HashMap<Instrument,Color>();
	private HashMap<Instrument, Color> _instrumentToAskBidBackground = new HashMap<Instrument,Color>();
	private int _instrumentPerRow;

	private int _lastRow;
	private int _lastColumn;
	private int _lastColumnOfLastRow;

	private InstrumentComparator _instrumentComparator = new InstrumentComparator();

	public InstrumentSpanBindingSource(Instrument[] instruments, int instrumentPerRow)
	{
		this._instruments = new ArrayList<Instrument>(instruments.length);
		Arrays.sort(instruments, this._instrumentComparator);
		for(Instrument instrument : instruments)
		{
			this._instruments.add(instrument);
		}
		this._instrumentPerRow = instrumentPerRow;

		this._descriptionCellStyle = new CellStyle();
		this._descriptionCellStyle.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, ColorSettings.useBlackAsBackground ? Color.white : Color.BLACK));
		this._descriptionCellStyle.setBackground(GridFixedBackColor.tradingPanel2);
		this._descriptionCellStyle.setForeground(GridFixedForeColor.tradingPanel2);
		this._descriptionCellStyle.setFont(this._descriptionFont);
		this._descriptionCellStyle.setHorizontalAlignment(SwingConstants.CENTER);

		this.initialize();
	}

	private static class InstrumentComparator implements Comparator<Instrument>
	{
		public int compare(Instrument o1, Instrument o2)
		{
			return o1.get_Sequence() - o2.get_Sequence();
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}

	/////SpanTableModel
	public CellSpan getCellSpanAt(int row, int column)
	{
		if(this.isAvailableCell(row, column))
		{
			if (this.isDescriptionCell(row, column))
			{
				return new CellSpan(row, (column / 4) * 4, 1, 4);
			}
			else if(this.isBidOrAskCaptionCell(row, column) || this.isHighOrLowCaptionCell(row, column) || this.isHighOrLowCell(row, column))
			{
				return new CellSpan(row, (column / 2) * 2, 1, 2);
			}
		}
		return null;
	}

	public boolean isCellSpanOn()
	{
		return true;
	}
	//SpanTableModel/////

	@Override
	public CellStyle getCellStyleAt(int row, int column)
	{
		Instrument instrument = this.getInstrument(row, column);
		Color defaultBackground = ColorSettings.useBlackAsBackground ? ColorSettings.TradingPanelGridBackground : Color.WHITE;
		Color background = this._instrumentToBackground.containsKey(instrument) ? this._instrumentToBackground.get(instrument) : defaultBackground;
		if(this.isAvailableCell(row, column))
		{
			if (this.isDescriptionCell(row, column))
			{
				return this._descriptionCellStyle;
			}

			boolean needSetCellStyle = false;
			CellStyle cellStyle = super.getCellStyleAt(row, column);
			if(cellStyle == null)
			{
				cellStyle = new CellStyle();
				needSetCellStyle = true;
			}

			if (this.isAskOrBidRightCell(row, column))
			{
				if(this._instrumentToAskBidBackground.containsKey(instrument))
				{
					Color background2 = this._instrumentToAskBidBackground.get(instrument);
					cellStyle.setBackground(background2);
					cellStyle.setForeground(background2 == null ? Color.black : Color.white);
				}
				else
				{
					cellStyle.setBackground(background);
					cellStyle.setForeground(ColorSettings.useBlackAsBackground ? Color.white : Color.black);
				}
				cellStyle.setFont(this._bidAskRightFont);
				cellStyle.setHorizontalAlignment(SwingConstants.CENTER);
			}
			else if(this.isAskOrBidLeftCell(row, column))
			{
				cellStyle.setBackground(background);
				cellStyle.setFont(this._defaultFont);
				//cellStyle.setVerticalAlignment(SwingConstants.BOTTOM);
				cellStyle.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			else
			{
				cellStyle.setBackground(background);
				cellStyle.setHorizontalAlignment(SwingConstants.CENTER);
				cellStyle.setFont(this._defaultFont);
			}

			int rowMode = row % 5;
			int columnMode = column % 4;
			Color borderColor = ColorSettings.useBlackAsBackground ? Color.WHITE : Color.BLACK;
			if(rowMode == 0)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(1,1,1,1,borderColor));
			}
			else if(rowMode == 1 || rowMode == 3)
			{
				//if(columnMode < 2) cellStyle.setBorder(BorderFactory.createMatteBorder(0,1,0,0,borderColor));
				//if(columnMode >= 2) cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,0,1,borderColor));
				if(columnMode < 2) cellStyle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,1,0,0,borderColor), BorderFactory.createMatteBorder(1,0,1,0, ColorSettings.useBlackAsBackground ? Color.GRAY : Color.LIGHT_GRAY)));
				if(columnMode >= 2) cellStyle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,0,1,borderColor), BorderFactory.createMatteBorder(1,0,1,0,ColorSettings.useBlackAsBackground ? Color.GRAY : Color.LIGHT_GRAY)));
			}
			else if(rowMode == 2)
			{
				if (columnMode == 0)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, borderColor));
				}
				else if(columnMode == 2)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0,1,0,0,ColorSettings.useBlackAsBackground ? Color.GRAY : Color.LIGHT_GRAY));
				}
				else if (columnMode == 3)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));
				}
			}
			else if(rowMode == 4)
			{
				if(columnMode == 0)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0,1,1,0, borderColor));
				}
				else if(columnMode == 1)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,1,0, borderColor));
				}
				else if(columnMode == 2 || columnMode == 3)
				{
					cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,1,1,borderColor));
				}
			}
			if(needSetCellStyle) super.setCellStyle(row, column, cellStyle);
		}
		return super.getCellStyleAt(row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	public void add(Instrument instrument)
	{
		if(!this._instruments.contains(instrument))
		{
			this._instruments.add(instrument);
			Instrument[] instruments = new Instrument[this._instruments.size()];
			instruments = this._instruments.toArray(instruments);
			Arrays.sort(instruments, this._instrumentComparator);
			this._instruments.clear();
			for(Instrument item : instruments)
			{
				this._instruments.add(item);
			}

			this.initialize();
			super.fireTableStructureChanged();
		}
	}

	public boolean remove(Instrument instrument)
	{
		boolean result = this._instruments.remove(instrument);
		if(result)
		{
			this.initialize();
			super.fireTableStructureChanged();
		}
		return result;
	}

	public void setInstrumentPerRow(int instrumentPerRow)
	{
		if(this._instrumentPerRow != instrumentPerRow)
		{
			this._instrumentPerRow = instrumentPerRow;
			this.initialize();
			super.fireTableStructureChanged();
		}
	}

	public void update(Instrument instrument)
	{
		CellSpan instrumentCellSpan = this.getCellSpan(instrument);
		if(instrumentCellSpan == null) return;
		for(ColumnNames columnName : ColumnNames.values())
		{
			CellSpan offset = columnName.getCellSpan();
			for(int rowOffset = 0; rowOffset < offset.getRowSpan(); rowOffset++)
			{
				for(int columnOffset = 0; columnOffset < offset.getColumnSpan(); columnOffset++)
				{
					int row = instrumentCellSpan.getRow() + offset.getRow() + rowOffset;
					int column = instrumentCellSpan.getColumn() + offset.getColumn() + columnOffset;

					if (columnName == ColumnNames.BidLeft || columnName == ColumnNames.BidRight)
					{
						//this.setForeground(row, column, instrument.getBidForeColor());
						Color background = instrument.getBidBackColor();
						if(background == null)
						{
							this._instrumentToAskBidBackground.remove(instrument);
						}
						else
						{
							this._instrumentToAskBidBackground.put(instrument, background);
						}
					}
					if (columnName == ColumnNames.AskLeft || columnName == ColumnNames.AskRight)
					{
						//this.setForeground(row, column, instrument.getAskForeColor());
						Color background = instrument.getAskBackColor();
						if(background == null)
						{
							this._instrumentToAskBidBackground.remove(instrument);
						}
						else
						{
							this._instrumentToAskBidBackground.put(instrument, background);
						}
					}
					super.setValueAt(columnName.getValue(instrument), row, column);
				}
			}
		}
	}

	public void resetForeground(Instrument instrument)
	{
		CellSpan instrumentCellSpan = this.getCellSpan(instrument);
		if(instrumentCellSpan == null) return;
		for(ColumnNames columnName : ColumnNames.values())
		{
			CellSpan offset = columnName.getCellSpan();
			int row = instrumentCellSpan.getRow() + offset.getRow();
			int column = instrumentCellSpan.getColumn() + offset.getColumn();
			this.resetForeground(row, column);
		}
	}

	public void setForeground(Instrument instrument, Color color)
	{
		CellSpan instrumentCellSpan = this.getCellSpan(instrument);
		if(instrumentCellSpan == null) return;
		for(ColumnNames columnName : ColumnNames.values())
		{
			CellSpan offset = columnName.getCellSpan();
			int row = instrumentCellSpan.getRow() + offset.getRow();
			int column = instrumentCellSpan.getColumn() + offset.getColumn();
			this.setForeground(row, column, color);
		}
	}

	public Instrument getInstrument(int row, int column)
	{
		if(!this.isAvailableCell(row, column))
		{
			return null;
		}
		else
		{
			int instrumentIndex = (row / 5) * this._instrumentPerRow + column / 4;
			return this._instruments.get(instrumentIndex);
		}
	}

	public String getColumnName(int row, int column)
	{
		for(ColumnNames columnName : ColumnNames.values())
		{
			if(columnName.getCellSpan().contains(row % 5, column %4))
			{
				return columnName.name();
			}
		}
		return null;
	}

	/////HierarchicalTableModel
	public boolean hasChild(int _int)
	{
		return false;
	}

	public boolean isHierarchical(int _int)
	{
		return false;
	}

	public Object getChildValueAt(int _int)
	{
		return null;
	}

	public boolean isExpandable(int _int)
	{
		return false;
	}
	//HierarchicalTableModel/////

	public void setImage(Instrument instrument, String propertyName, Image image)
	{
		this.setIcon(instrument, propertyName, new ImageIcon(image));
	}

	public void setBackground(Instrument instrument, Color background)
	{
		this._instrumentToBackground.put(instrument, background);
		//super.fireTableDataChanged();
	}

	public void resetBackground(Instrument instrument)
	{
		this._instrumentToBackground.remove(instrument);
		//super.fireTableDataChanged();
	}

	private void initializeCellStyles()
	{
		for(int row = 0; row <= this._lastRow; row++)
		{
			int lastColumn = this._lastRow - row < 5 ? this._lastColumnOfLastRow : this._lastColumn;
			for(int column = 0; column <= lastColumn; column++)
			{
				this.initializeCellStyleAt(row, column);
			}
		}
	}

	private void initializeCellStyleAt(int row, int column)
	{
		boolean needSetCellStyle = false;
		CellStyle cellStyle = super.getCellStyleAt(row, column);
		if(cellStyle == null)
		{
			cellStyle = new CellStyle();
			needSetCellStyle = true;
		}

		int rowMode = row % 5;
		int columnMode = column % 4;
		if(rowMode == 0)
		{
			cellStyle.setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.BLACK));
		}
		else if(rowMode == 1 || rowMode == 3)
		{
			if(columnMode < 2) cellStyle.setBorder(BorderFactory.createMatteBorder(0,1,0,0,Color.BLACK));
			if(columnMode >= 2) cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,0,1,Color.BLACK));
		}
		else if(rowMode == 2)
		{
			if (columnMode == 0)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.BLACK));
			}
			else if (columnMode == 3)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.BLACK));
			}
		}
		else if(rowMode == 4)
		{
			if(columnMode == 0)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(0,1,1,0, Color.BLACK));
			}
			else if(columnMode == 1)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.BLACK));
			}
			else if(columnMode == 2 || columnMode == 3)
			{
				cellStyle.setBorder(BorderFactory.createMatteBorder(0,0,1,1,Color.BLACK));
			}
		}
		if(needSetCellStyle) super.setCellStyle(row, column, cellStyle);
	}

	private void setForeground(int row, int column, Color color)
	{
		CellStyle style = super.getCellStyleAt(row, column);
		if(style == null)
		{
			style = new CellStyle();
			style.setForeground(color);
			this.setCellStyle(row, column, style);
		}
		else
		{
			style.setForeground(color);
		}
	}

	private void resetForeground(int row, int column)
	{
		CellStyle style = super.getCellStyleAt(row, column);
		if(style != null)
		{
			style.setForeground(null);
		}
	}

	private boolean isAvailableCell(int row, int column)
	{
		return (row <= this._lastRow) && ((this._lastRow - row >= 5 && column <= this._lastColumn)
										  || (this._lastRow - row < 5 && column <= this._lastColumnOfLastRow));
	}

	private boolean isDescriptionCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.Description.getCellSpan().contains(rowMode, columnMode);
	}

	private boolean isBidOrAskCaptionCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.BidCaption.getCellSpan().contains(rowMode, columnMode)
			|| ColumnNames.AskCaption.getCellSpan().contains(rowMode, columnMode);
	}

	private boolean isAskOrBidRightCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.BidRight.getCellSpan().contains(rowMode, columnMode)
			|| ColumnNames.AskRight.getCellSpan().contains(rowMode, columnMode);
	}

	private boolean isAskOrBidLeftCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.BidLeft.getCellSpan().contains(rowMode, columnMode)
			|| ColumnNames.AskLeft.getCellSpan().contains(rowMode, columnMode);
	}

	private boolean isHighOrLowCaptionCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.HighCaption.getCellSpan().contains(rowMode, columnMode)
			|| ColumnNames.LowCaption.getCellSpan().contains(rowMode, columnMode);
	}

	private boolean isHighOrLowCell(int row, int column)
	{
		int rowMode = row % 5;
		int columnMode = column % 4;
		return ColumnNames.High.getCellSpan().contains(rowMode, columnMode)
			|| ColumnNames.Low.getCellSpan().contains(rowMode, columnMode);
	}

	private void initialize()
	{
		for(int row = 0; row <= this._lastRow; row++)
		{
			int lastColumn = this._lastRow - row < 5 ? this._lastColumnOfLastRow : this._lastColumn;
			for(int column = 0; column <= lastColumn; column++)
			{
				this.setCellStyle(row, column, null);
			}
		}

		//Every instrument has 5 row and 4 column
		int instrumentRow = (int)(Math.ceil((double)this._instruments.size() / this._instrumentPerRow));
		int rowCount = instrumentRow * 5;
		int columnCount = 4 * this._instrumentPerRow;

		this._lastRow = rowCount - 1;
		this._lastColumn = columnCount - 1;
		int unavailableColumns = (instrumentRow * this._instrumentPerRow - this._instruments.size()) * 4;
		this._lastColumnOfLastRow = this._instrumentPerRow * 4 - unavailableColumns - 1;

		Vector columnIdentifiers = new Vector(columnCount);
		Vector dataVector = new Vector(rowCount);

		for(int index = 0; index < this._instrumentPerRow; index++)
		{
			for(int column = 0; column < 4; column ++)
			{
				columnIdentifiers.add("#" + index + "#" + column);
			}
		}

		for(int row = 0; row < rowCount; row++)
		{
			Vector rowData = new Vector(columnCount);
			for(int column = 0; column < columnCount; column++)
			{
				rowData.add(null);
			}
			dataVector.add(rowData);
		}

		super.setDataVector(dataVector, columnIdentifiers);

		this.initializeCellStyles();
		this.updateInstruments();
	}

	private void updateInstruments()
	{
		for(Instrument instrument : this._instruments)
		{
			this.update(instrument);
		}
	}

	private CellSpan getCellSpan(Instrument instrument)
	{
		int index = this._instruments.indexOf(instrument);
		if(index < 0)
		{
			return null;
		}
		else
		{
			int row = (index / this._instrumentPerRow) * 5;
			int column = (index % this._instrumentPerRow) * 4;
			return new CellSpan(row, column, 5, 4);
		}
	}

	public void setIcon(Instrument instrument, String propertyName, Icon image)
	{
		CellSpan instrumentCellSpan = this.getCellSpan(instrument);
		if(instrumentCellSpan == null) return;

		CellSpan offset = null;
		if(propertyName == InstrumentColKey.Bid)
		{
			offset = ColumnNames.BidLeft.getCellSpan();
		}
		else if(propertyName == InstrumentColKey.Ask)
		{
			offset = ColumnNames.AskLeft.getCellSpan();
		}
		else
		{
			return;
		}

		for(int rowOffset = 0; rowOffset < offset.getRowSpan(); rowOffset++)
		{
			for(int columnOffset = 0; columnOffset < offset.getColumnSpan(); columnOffset++)
			{
				int row = instrumentCellSpan.getRow() + offset.getRow() + rowOffset;
				int column = instrumentCellSpan.getColumn() + offset.getColumn() + columnOffset;
				CellStyle cellStyle = super.getCellStyleAt(row, column);
				if(cellStyle == null)
				{
					cellStyle = new CellStyle();
					cellStyle.setIcon(image);
					super.setCellStyle(row, column, cellStyle);
				}
				else
				{
					cellStyle.setIcon(image);
				}
			}
		}
	}

	public enum ColumnNames
	{
		Description,
		BidCaption,
		AskCaption,
		HighCaption,
		LowCaption,
		BidLeft,
		BidRight,
		AskLeft,
		AskRight,
		High,
		Low;

		CellSpan getCellSpan()
		{
			switch(this)
			{
				case Description:
					return new CellSpan(0, 0, 1, 4);
				case BidCaption:
					return new CellSpan(1, 0, 1, 2);
				case AskCaption:
					return new CellSpan(1, 2, 1, 2);
				case BidLeft:
					return new CellSpan(2, 0, 1, 1);
				case BidRight:
					return new CellSpan(2, 1, 1, 1);
				case AskLeft:
					return new CellSpan(2, 2, 1, 1);
				case AskRight:
					return new CellSpan(2, 3, 1, 1);
				case HighCaption:
					return new CellSpan(3, 0, 1, 2);
				case LowCaption:
					return new CellSpan(3, 2, 1, 2);
				case High:
					return new CellSpan(4, 0, 1, 2);
				case Low:
					return new CellSpan(4, 2, 1, 2);
			}
			return null;
		}

		private Object getValue(Instrument instrument)
		{
			boolean isNormal = instrument.get_IsNormal();
			boolean inChinese = PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT");

			switch(this)
			{
				case Description:
					return instrument.get_Description();
				case BidCaption:
					return isNormal || !inChinese ? InstrumentLanguage.Bid : InstrumentLanguage.Ask;
				case AskCaption:
					return isNormal || !inChinese ? InstrumentLanguage.Ask : InstrumentLanguage.Bid ;
				case BidLeft:
					return instrument.getBidLeft();
				case BidRight:
					return instrument.getBidRight();
				case AskLeft:
					return instrument.getAskLeft();
				case AskRight:
					return instrument.getAskRight();
				case HighCaption:
					return InstrumentLanguage.High;
				case LowCaption:
					return InstrumentLanguage.Low;
				case High:
					return instrument.get_High();
				case Low:
					return instrument.get_Low();
			}
			return null;
		}
	}
}
