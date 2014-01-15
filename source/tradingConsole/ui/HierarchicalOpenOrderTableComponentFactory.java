package tradingConsole.ui;

import tradingConsole.ui.grid.BindingSource;
import javax.swing.JViewport;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Container;
import java.awt.event.MouseWheelListener;
import java.awt.Component;
import com.jidesoft.grid.HierarchicalTable;
import java.awt.Dimension;
import javax.swing.JComponent;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import com.jidesoft.grid.TreeLikeHierarchicalPanel;

import tradingConsole.ui.grid.DataGrid;
import java.util.HashMap;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.BorderFactory;
import tradingConsole.ui.grid.IActionListener;
import javax.swing.JTable;
import tradingConsole.ui.grid.TableColumnChooser;

public class HierarchicalOpenOrderTableComponentFactory implements com.jidesoft.grid.HierarchicalTableComponentFactory
{
	private IActionListener _actionListener;
	private IColumnVisibilityProvider _columnVisibilityProvider;
	private HashMap<BindingSource, TreeLikeHierarchicalPanel> _cache = new HashMap<BindingSource, TreeLikeHierarchicalPanel>();

	public HierarchicalOpenOrderTableComponentFactory()
	{
		this(null);
	}

	public HierarchicalOpenOrderTableComponentFactory(IActionListener actionListener)
	{
		this(actionListener, null);
	}

	public HierarchicalOpenOrderTableComponentFactory(IActionListener actionListener, IColumnVisibilityProvider columnVisibilityProvider)
	{
		this._actionListener = actionListener;
		this._columnVisibilityProvider = columnVisibilityProvider;
		if(this._columnVisibilityProvider != null)
		{
			this._columnVisibilityProvider.addColumnVisibilityChangedHandler(new IColumnVisibilityProvider.IColumnVisibilityChangedHandler()
			{
				public void handleColumnVisibilityChanged(BindingSource bindingSource, String columnName, boolean isVisible)
				{
					doHandleColumnVisibilityChanged(bindingSource, columnName, isVisible);
				}
			});
		}
	}

	private void doHandleColumnVisibilityChanged(BindingSource model, String columnName, boolean isVisible)
	{
		if(this._cache.containsKey(model))
		{
			DataGrid childTable = (DataGrid)this._cache.get(model).getClientProperty("childTable");
			int modelIndex = model.getColumnByName(columnName);
			if (isVisible)
			{
				if(!TableColumnChooser.isVisibleColumn(childTable.getColumnModel(), modelIndex))
				{
					TableColumnChooser.showColumn(childTable, modelIndex, -1);
				}
			}
			else
			{
				if(TableColumnChooser.isVisibleColumn(childTable.getColumnModel(), modelIndex))
				{
					TableColumnChooser.hideColumn(childTable, modelIndex);
				}
			}
		}
	}

	public Component createChildComponent(HierarchicalTable hierarchicalTable, Object object, int row)
	{
		if (object == null)	return new JPanel();

	    BindingSource model = ((BindingSource) object);
		if(!this._cache.containsKey(model))
		{
			TradingConsole.traceSource.trace(TraceType.Information, "HierarchicalOpenOrderTableComponentFactory.createChildComponent  create new child grid");

			//HierarchicalTable childTable = new HierarchicalTable(model)//DataGrid("ChildTable")
			DataGrid childTable = new DataGrid("ChildTable")
			{
				@Override
				public void scrollRectToVisible(Rectangle aRect)
				{
					HierarchicalOpenOrderTableComponentFactory.scrollRectToVisible(this, aRect);
				}
			};

			childTable.setEditable(false);
			childTable.setShowGrid(false);
			if(ColorSettings.useBlackAsBackground)
			{
				childTable.enableRowStripe(ColorSettings.GridStripeColors);
			}
			else
			{
				childTable.enableRowStripe();
			}
			childTable.setRowSelectionAllowed(false);
			childTable.setColumnSelectionAllowed(false);
			childTable.setModel(model);
			if(this._columnVisibilityProvider != null)
			{
				for(int index = 0; index < childTable.getColumnCount(); index++)
				{
					String columnName = childTable.getColumnName(index);
					if(!this._columnVisibilityProvider.isVisible(model, columnName))
					{
						int modelIndex = model.getColumnByName(columnName);
						TableColumnChooser.hideColumn(childTable, modelIndex);
					}
				}
			}
			childTable.setOptimized(true);
			childTable.getTableHeader().setReorderingAllowed(false);
			childTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			if(this._actionListener != null) childTable.addActionListener(this._actionListener);

			if(ColorSettings.useBlackAsBackground)
			{
				childTable.setBackground(ColorSettings.OpenOrderListGridBackground);
				childTable.setGridColor(ColorSettings.OpenOrderListGridBackground);
				model.useBlackAsBackground();
			}
			childTable.setOpaque(true);
			childTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

			TreeLikeHierarchicalPanel treeLikeHierarchicalPanel = new TreeLikeHierarchicalPanel(new FitScrollPane(childTable));
			treeLikeHierarchicalPanel.setBackground(childTable.getMarginBackground());
			treeLikeHierarchicalPanel.putClientProperty("childTable", childTable);
			this._cache.put(model, treeLikeHierarchicalPanel);
		}
		else
		{
			DataGrid childTable = (DataGrid)this._cache.get(model).getClientProperty("childTable");
			if(ColorSettings.useBlackAsBackground) childTable.setBackground(ColorSettings.OpenOrderListGridBackground);
			childTable.setOpaque(true);
			childTable.updateProperties(model.getPropertyDescriptors());
			TradingConsole.traceSource.trace(TraceType.Information, "HierarchicalOpenOrderTableComponentFactory.createChildComponent use cached child grid");
		}

		return this._cache.get(model);
	}

	public void destroyChildComponent(HierarchicalTable hierarchicalTable, Component component, int row)
	{
	}

	public static void scrollRectToVisible(Component component, Rectangle aRect)
	{
		Container parent;
		int dx = component.getX(), dy = component.getY();

		for (parent = component.getParent();
			 parent != null && (! (parent instanceof JViewport) || ( ( (JViewport)parent).getClientProperty("HierarchicalTable.mainViewport") == null));
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

	static class FitScrollPane extends JScrollPane implements ComponentListener
	{
		public FitScrollPane(Component view)
		{
			super(view);
			this.initScrollPane();
		}

		private void initScrollPane()
		{
			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			this.getViewport().getView().addComponentListener(this);
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
}
