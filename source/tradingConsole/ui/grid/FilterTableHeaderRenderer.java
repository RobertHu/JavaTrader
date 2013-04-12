package tradingConsole.ui.grid;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Color;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;

public class FilterTableHeaderRenderer extends com.jidesoft.grid.AutoFilterTableHeaderRenderer
{
	private int _alignment = -1;
	private int _height = -1;
	private Color _foreground = null;
	private Color _background = null;
	private Font _font = null;

	public FilterTableHeaderRenderer()
	{
		setHorizontalAlignment(SwingConstants.CENTER);
		setOpaque(true);

		// This call is needed because DefaultTableCellRenderer calls setBorder()
		// in its constructor, which is executed after updateUI()
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	public void updateUI()
	{
		super.updateUI();
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	}

	/*public Component getTableCellRendererComponent(JTable table, Object value,
					   boolean selected, boolean focused, int row, int column)
	{
		JTableHeader originHeader = table != null ? table.getTableHeader() : null;

		if (originHeader != null)
		{
			setEnabled(originHeader.isEnabled());
			setComponentOrientation(originHeader.getComponentOrientation());
			setForeground(this._foreground == null ? originHeader.getForeground() : this._foreground);
			setBackground(this._background == null ? originHeader.getBackground() : this._background);
			setFont(this._font == null ? originHeader.getFont() : this._font);
		}
		else
		{
			// Use sensible values instead of random leftover values from the last call
			setEnabled(true);
			setComponentOrientation(ComponentOrientation.UNKNOWN);

			setForeground(this._foreground == null ? UIManager.getColor("TableHeader.foreground") : this._foreground);
			setBackground(this._background == null ? UIManager.getColor("TableHeader.background") : this._background);
			setFont(this._font == null ? UIManager.getFont("TableHeader.font") : this._font);
		}

		if(this._alignment != -1)
		{
			super.setHorizontalAlignment(this._alignment);
		}

		if(this._height != -1)
		{
			super.setPreferredSize(new Dimension( (int)this.getSize().getWidth(), this._height));
		}
		//setValue(value);
		return this;
	}*/

	@Override
	public void setForeground(Color foreground)
	{
		this._foreground = foreground;
		super.setForeground(foreground);
	}

	@Override
	public void setBackground(Color background)
	{
		this._background = background;
		super.setBackground(background);
	}

	@Override
	public void setFont(Font font)
	{
		this._font = font;
		super.setFont(font);
	}

	public void setAlignment(int alignment)
	{
		this._alignment = alignment;
		super.setHorizontalAlignment(alignment);
	}

	public void setHeight(int height)
	{
		this._height = height;
		super.setPreferredSize(new Dimension((int)this.getSize().getWidth(), height));
	}
}
