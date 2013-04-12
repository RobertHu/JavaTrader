package tradingConsole.ui.grid;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import com.jidesoft.docking.*;
import com.jidesoft.grid.*;
import tradingConsole.*;
import tradingConsole.ui.*;
import java.awt.Dimension;
import java.awt.Color;

public class InstrumentSpanGrid extends CellSpanTable
{
	private InstrumentSpanBindingSource _bindingSource;
	private DockableFrame _dockableFrame;
	private JScrollPane _scrollPane;
	private ArrayList<IActionListener> _actionListeners = new ArrayList<IActionListener>();

	public InstrumentSpanGrid(String title, Icon icon)
	{
		HeaderRenderer headerRenderer = new HeaderRenderer();
		headerRenderer.setPreferredSize(new Dimension(10, 0));
		headerRenderer.setMaximumSize(new Dimension(10, 0));
		this.getTableHeader().setDefaultRenderer(headerRenderer);

		super.addMouseListener(new MouseListener(new AdvancedMouseListener(this)));

		this._dockableFrame = new DockableFrame(title, icon);
		this._scrollPane = new JScrollPane(this);
		this._dockableFrame.getContentPane().add(this._scrollPane);
		this.setShowHorizontalLines(false);
		this.setShowVerticalLines(false);
		this.setShowGrid(false);

		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setCellSelectionEnabled(false);
		if(ColorSettings.useBlackAsBackground)
		{
			this.setBackground(ColorSettings.TradingPanelGridFrameBackground);
			this.setForeground(Color.WHITE);
		}
		else
		{
			this.setBackground(null);
		}
		this.setOpaque(true);

		this.setIntercellSpacing(new Dimension(0,0));
		this._dockableFrame.addComponentListener(new ComponentListener()
			{
				public void componentResized(ComponentEvent e)
				{
					int count = (int)(Math.floor((double)e.getComponent().getWidth() / 200));
					if(count < 1) count = 1;
					_bindingSource.setInstrumentPerRow(count);
					adjustRowHight();
					adjustColumnWidth();
				}

				public void componentMoved(ComponentEvent e)
				{
				}

				public void componentShown(ComponentEvent e)
				{
				}

				public void componentHidden(ComponentEvent e)
				{
				}
		});
	}

	@Override
	public void setModel(TableModel model)
	{
		super.setModel(model);
		if (model instanceof InstrumentSpanBindingSource)
		{
			this._bindingSource = (InstrumentSpanBindingSource)model;
			this.adjustRowHight();
			this.adjustColumnWidth();
		}
	}

	private void adjustRowHight()
	{
		this.setRowResizable(true);
		for(int row = 0; row < this._bindingSource.getRowCount(); row++)
		{
			int rowMode = row % 5;
			if(rowMode == 0)
			{
				this.setRowHeight(row, 24);
			}
			else if(rowMode == 2)
			{
				this.setRowHeight(row, 36);
			}
		}
		this.setRowResizable(false);
	}

	private void adjustColumnWidth()
	{
		this.setColumnResizable(true);
		Enumeration<TableColumn> tableColumns = this.getColumnModel().getColumns();
		while(tableColumns.hasMoreElements())
		{
			TableColumn tableColumn = (TableColumn)tableColumns.nextElement();
			tableColumn.setPreferredWidth(50);
		}
		this.setColumnResizable(false);
	}

	public DockableFrame get_DockableFrame()
	{
		return this._dockableFrame;
	}

	public void addActionListener(IActionListener actionListener)
	{
		this._actionListeners.add(actionListener);
	}

	public boolean removeActionListener(IActionListener actionListener)
	{
		return this._actionListeners.remove(actionListener);
	}

	private void clicked(MouseEvent e)
	{
		this.fireMouseAction(e, Action.Clicked);
	}

	private void doubleClicked(MouseEvent e)
	{
		this.fireMouseAction(e, Action.DoubleClicked);
	}

	private void fireMouseAction(MouseEvent e, Action action)
	{
		int row = super.rowAtPoint(e.getPoint());
		int column = super.columnAtPoint(e.getPoint());
		if(row != -1 && column != -1)
		{
			Instrument instrument = this._bindingSource.getInstrument(row, column);
			String columnName = this._bindingSource.getColumnName(row, column);
			if(instrument != null && columnName != null)
			{
				for (IActionListener actionListener : this._actionListeners)
				{
					actionListener.actionPerformed(new ActionEvent(this, action, instrument, columnName, row, column));
				}
			}
		}
	}

	private static class AdvancedMouseListener implements IAdvancedMouseListener
	{
		private InstrumentSpanGrid _instrumentSpanGrid;
		AdvancedMouseListener(InstrumentSpanGrid instrumentSpanGrid)
		{
			this._instrumentSpanGrid = instrumentSpanGrid;
		}

		public void clicked(MouseEvent e)
		{
			this._instrumentSpanGrid.clicked(e);
		}

		public void doubleClicked(MouseEvent e)
		{
			this._instrumentSpanGrid.doubleClicked(e);
		}
	}
}
