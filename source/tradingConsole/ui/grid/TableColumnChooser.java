package tradingConsole.ui.grid;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import com.jidesoft.combobox.FontChooserPanel;
import tradingConsole.AppToolkit;
import java.awt.Rectangle;
import java.awt.Font;
import tradingConsole.ui.language.Language;
import tradingConsole.TradingConsole;
import java.util.ArrayList;
import java.awt.Component;

public class TableColumnChooser extends com.jidesoft.grid.TableColumnChooser
{
	public static JFrame MainFrame;
	public static final String UnchoosableColumns = "UnseletedColumns";
	private FontChooseFrameOKActionListener _fontChooseFrameOKActionListener = new FontChooseFrameOKActionListener();
	private FontChooseFrame _fontChooseFrame;
	private Font _selectedFont;

	public TableColumnChooser()
	{
		this._fontChooseFrameOKActionListener.set_Owner(this._fontChooseFrame);
	}

	@Override
	protected void createColumnChooserMenuItems(JPopupMenu popup, JTableHeader header, int[]
												fixedColumns, int clickingColumn)
	{
		super.createColumnChooserMenuItems(popup, header, fixedColumns, clickingColumn);
		this.updateMenuItems(popup, header);
		popup.addSeparator();
		JMenuItem chooseFontItem = new JMenuItem(Language.optionFormFontLabel);

		BindingSource bindingSource = ((DataGrid)header.getTable()).get_BindingSource();
		this._selectedFont = (bindingSource.get_Font() == null ? header.getTable().getFont() :  bindingSource.get_Font());
		this._fontChooseFrameOKActionListener.set_BindingSource(bindingSource);

		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_fontChooseFrame = new FontChooseFrame(_fontChooseFrameOKActionListener);
				_fontChooseFrameOKActionListener.set_Owner(_fontChooseFrame);
				_fontChooseFrame.setSelectedFont(_selectedFont);
				_fontChooseFrame.show();
				_fontChooseFrame.toFront();
			}
		};
		chooseFontItem.addActionListener(actionListener);
		popup.add(chooseFontItem);
	}

	private void updateMenuItems(JPopupMenu popup, JTableHeader header)
	{
		DataGrid dataGrid = (DataGrid)header.getTable();
		BindingSource bindingSource = dataGrid.get_BindingSource();
		ArrayList<String> unchooseableColumns = dataGrid.getClientProperty(TableColumnChooser.UnchoosableColumns) == null ? null : (ArrayList<String>)(dataGrid.getClientProperty(TableColumnChooser.UnchoosableColumns));

		for(MenuElement item : popup.getSubElements())
		{
			if(item instanceof JMenuItem)
			{
				JMenuItem menuItem = ((JMenuItem)item);
				String name = menuItem.getText();
				if(unchooseableColumns != null && unchooseableColumns.contains(name))
				{
					menuItem.setVisible(false);
					menuItem.setEnabled(false);
				}
				else
				{
					int column = bindingSource.getColumnByName(name);
					String caption = bindingSource.getPropertyDescriptors()[column].get_Caption();
					menuItem.setText(caption);
				}
			}
		}
	}
}

class FontChooseFrameOKActionListener implements ActionListener
{
	private FontChooseFrame _owner;
	private BindingSource _bindingSource;

	public void set_BindingSource(BindingSource bindingSource)
	{
		this._bindingSource = bindingSource;
	}

	public void set_Owner(FontChooseFrame owner)
	{
		this._owner = owner;
	}

	public void actionPerformed(ActionEvent e)
	{
		this._bindingSource.setFont(this._owner.getSelectedFont());
	}
}

class MyFontChooserPanel extends FontChooserPanel
{
	public MyFontChooserPanel(javax.swing.Action okAction, javax.swing.Action cancelAction)
	{
		super(okAction, cancelAction);
	}

	public void setMaxFontSize(int maxSize)
	{
		SpinnerModel spinnerModel = this._sizeSpinner.getModel();
		this._sizeSpinner.setModel(new SpinnerNumberModel(0, 0, maxSize, 1));
	}
}

class FontChooseFrame extends JDialog
{
	private ActionListener _okActionListener;
	private MyFontChooserPanel _fontChooserPanel;

	public FontChooseFrame(ActionListener okActionListener)
	{
		super(TableColumnChooser.MainFrame, false);
		//this.setIconImage(TradingConsole.get_TraderImage());
		this.setTitle(Language.optionFormFontLabel);

		this._okActionListener = okActionListener;

		this._fontChooserPanel = new MyFontChooserPanel(new OKAction(this), new CancelAction(this));
		this._fontChooserPanel.setMaxFontSize(150);
		this.add(this._fontChooserPanel);

		this.setSize(400, 300);
		Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
		this.setBounds(rectangle);
	}

	public void setSelectedFont(Font font)
	{
		this._fontChooserPanel.setSelectedFont(font);
	}

	public Font getSelectedFont()
	{
		return this._fontChooserPanel.getSelectedFont();
	}

	static class OKAction extends AbstractAction
	{
		private FontChooseFrame _owner;
		public OKAction(FontChooseFrame owner)
		{
			this._owner = owner;
		}

		public void actionPerformed(ActionEvent e)
		{
			this._owner._okActionListener.actionPerformed(e);
			this._owner.dispose();
		}
	}

	static class CancelAction extends AbstractAction
	{
		private FontChooseFrame _owner;
		public CancelAction(FontChooseFrame owner)
		{
			this._owner = owner;
		}

		public void actionPerformed(ActionEvent e)
		{
			this._owner.dispose();
		}
	}
}
