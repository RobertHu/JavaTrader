package tradingConsole.ui.grid;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.jidesoft.docking.*;
import tradingConsole.ui.*;

public class DockableTable extends DataGrid
{
	private DockableFrame _dockableFrame;
	private JScrollPane _scrollPane;

	static
	{
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader","TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}

	public DockableTable(String name, String title, Icon icon)
	{
		super(name);

		this._dockableFrame = new DockableFrame(title, icon);
		this._scrollPane = new JScrollPane(this);
		this._scrollPane.addMouseListener(new MouseListener(this));
		this._dockableFrame.getContentPane().add(this._scrollPane);
		this.setOpaque(true);
	}

	public void enalbeHierarchicalTableMainViewport()
	{
		this._scrollPane.getViewport().putClientProperty("HierarchicalTable.mainViewport", Boolean.TRUE);
	}

	public DockableFrame get_DockableFrame()
	{
		return this._dockableFrame;
	}

	public void sort(int column, boolean ascending, boolean caseOn)
	{
		super.sortColumn(column, true, ascending);
	}

	public void reset()
	{
	}

	public void setVertGridLines(boolean b)
	{
		this.setShowVerticalLines(b);
	}

	public void setHorzGridLines(boolean b)
	{
		this.setShowHorizontalLines(b);
	}

	public void setBackColor(Color color)
	{
		this.setBackground(color);
	}

	public void setTableColor(Color color)
	{
		this.setBackground(color);
	}

	public void setBorderStyle(int i)
	{
	}

	public void setRowLabelWidth(int i)
	{
	}

	public void setCurrentCellColor(Color color)
	{
	}

	public void setCurrentCellBorder(int i)
	{
	}

	public void setDataModel(BindingSource bindingSource)
	{
		this.setModel(bindingSource);
	}

	public void setLabel(String string)
	{
	}

	private void clicked(MouseEvent e)
	{
		super.clearSelection();
	}

	private static class MouseListener implements java.awt.event.MouseListener
	{
		private DockableTable _owner;

		public MouseListener(DockableTable owner)
		{
			this._owner = owner;
		}

		public void mouseClicked(MouseEvent e)
		{
			if(e.getButton() == MouseEvent.BUTTON1)
			{
				if(e.getClickCount() == 1)
				{
					this._owner.clicked(e);
				}
			}
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}
	}
}
