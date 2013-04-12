package tradingConsole.ui.account;

import java.awt.Component;
import com.jidesoft.grid.HierarchicalTable;
import java.awt.Rectangle;
import javax.swing.table.TableModel;
import com.jidesoft.grid.TreeLikeHierarchicalPanel;
import javax.swing.JComponent;
import java.awt.Container;
import javax.swing.JViewport;
import javax.swing.JScrollPane;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelListener;
import java.awt.Dimension;
import javax.swing.JTable;
import tradingConsole.ui.grid.HeaderRenderer;
import tradingConsole.ui.grid.BindingSource;
import java.awt.event.MouseEvent;
import tradingConsole.ui.account.Account.ToolTipProvider;
import framework.diagnostics.TraceType;
import tradingConsole.TradingConsole;
import tradingConsole.ui.ColorSettings;
import com.jidesoft.grid.RowStripeCellStyleProvider;
import tradingConsole.ui.grid.DataGrid;
import java.awt.Color;
import java.awt.Graphics;

public class HierarchicalTableComponentFactory implements com.jidesoft.grid.HierarchicalTableComponentFactory
{
	public HierarchicalTableComponentFactory()
	{
	}

	public Component createChildComponent(HierarchicalTable hierarchicalTable, Object object, int row)
	{
		BindingSource model = ((BindingSource) object);
		ToolTipableHierarchicalTable childTable = new ToolTipableHierarchicalTable(model)
		{
			@Override
			public void scrollRectToVisible(Rectangle aRect)
			{
				HierarchicalTableComponentFactory.scrollRectToVisible(this, aRect);
			}
		};

		Account.ToolTipProvider toolTipProvider = new Account.ToolTipProvider(childTable);
		childTable.set_ToolTipProvider(toolTipProvider);
		childTable.setHierarchicalColumn(0);
		HeaderRenderer headerRenderer = new HeaderRenderer();
		headerRenderer.setHeight(0);
		childTable.getTableHeader().setDefaultRenderer(headerRenderer);
		childTable.setIntercellSpacing(new Dimension(0, 0));
		childTable.setRowSelectionAllowed(false);
		childTable.setColumnSelectionAllowed(false);
		childTable.setComponentFactory(new HierarchicalTableComponentFactory());
		if(ColorSettings.useBlackAsBackground)
		{
			childTable.setBackground(ColorSettings.AccountStatusGridBackground);
			childTable.setGridColor(ColorSettings.OpenOrderListGridBackground);
			model.useBlackAsBackground();
			childTable.setOpaque(true);
		}
		else
		{
			childTable.setOpaque(false);
			childTable.setBackground(Account.LightGray);
		}
		childTable.setFont(DataGrid.DefaultFont);

		TreeLikeHierarchicalPanel treeLikeHierarchicalPanel = new TreeLikeHierarchicalPanel(new FitScrollPane(childTable));
		treeLikeHierarchicalPanel.setBorder(BorderFactory.createEmptyBorder());

		this.setBackground(treeLikeHierarchicalPanel, ColorSettings.useBlackAsBackground ? ColorSettings.AccountStatusGridBackground : Account.LightGray);

		TradingConsole.traceSource.trace(TraceType.Information, "HierarchicalTableComponentFactory.createChildComponent create new child grid!");
		return treeLikeHierarchicalPanel;
	}

	private void setBackground(Container container, Color backgroundColor)
	{
		container.setBackground(backgroundColor);
		if(container instanceof JScrollPane)
		{
			((JScrollPane)container).getViewport().setBackground(backgroundColor);
		}

		for(Component component : container.getComponents())
		{
			if(component instanceof Container)
			{
				this.setBackground((Container)component, backgroundColor);
			}
		}
	}

	public void destroyChildComponent(HierarchicalTable hierarchicalTable, Component component, int row)
	{
	}

	static class FitScrollPane extends JScrollPane implements ComponentListener
	{
		public FitScrollPane(Component view)
		{
			super(view);
			this.initScrollPane();
		}

		private void initScrollPane()
		{
			this.setBorder(BorderFactory.createEmptyBorder());
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			this.getViewport().getView().addComponentListener(this);
			//this.getViewport().setBackground(ColorSettings.AccountStatusGridBackground);
			this.removeMouseWheelListeners();
		}

		// remove MouseWheelListener as there is no need for it in FitScrollPane.
		private void removeMouseWheelListeners()
		{
			MouseWheelListener[] listeners = getMouseWheelListeners();
			for (MouseWheelListener listener : listeners)
			{
				this.removeMouseWheelListener(listener);
			}
		}

		@Override
		public void updateUI()
		{
			super.updateUI();
			this.removeMouseWheelListeners();
		}

		public void componentResized(ComponentEvent e)
		{
			this.setSize(getSize().width, getPreferredSize().height);
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

		@Override
		public Dimension getPreferredSize()
		{
			this.getViewport().setPreferredSize(getViewport().getView().getPreferredSize());
			Dimension preferredSize = super.getPreferredSize();
			return preferredSize;
		}
	}

	public static void scrollRectToVisible(Component component, Rectangle aRect)
	{
		Container parent;
		int dx = component.getX(), dy = component.getY();

		for (parent = component.getParent();
			 parent != null && (! (parent instanceof JViewport)/* || ( ( (JViewport)parent).getClientProperty("HierarchicalTable.mainViewport") == null)*/);
			 parent = parent.getParent())
		{
			Rectangle bounds = parent.getBounds();

			dx += bounds.x;
			dy += bounds.y;
		}

		if (parent != null)
		{
			aRect.x += dx;
			aRect.y += dy;

			( (JComponent)parent).scrollRectToVisible(aRect);
			aRect.x -= dx;
			aRect.y -= dy;
		}
	}
}

class ToolTipableHierarchicalTable extends HierarchicalTable
{
	private IToolTipProvider _toolTipProvider;

	public ToolTipableHierarchicalTable(TableModel model)
	{
		super(model);
	}

	public void set_ToolTipProvider(ToolTipProvider toolTipProvider)
	{
		this._toolTipProvider = toolTipProvider;
	}

	@Override
	public String getToolTipText(MouseEvent event)
	{
		if(this._toolTipProvider != null)
		{
			java.awt.Point p = event.getPoint();
			int row = rowAtPoint(p);
			int column = columnAtPoint(p);
			if(row != -1 && column != -1)
			{
				if (this._toolTipProvider.showToolTip(row, column))
				{
					return this.getValueAt(row, column).toString();
				}
			}
		}
		return super.getToolTipText(event);
	}
}
