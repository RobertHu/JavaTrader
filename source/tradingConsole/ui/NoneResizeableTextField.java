package tradingConsole.ui;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;

public class NoneResizeableTextField extends JScrollPane
{
	private PVStaticText2 _staticText;

	public NoneResizeableTextField()
	{
		this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER );
		this.setBorder(null);
		this.setBackground(null);
		this.setOpaque(false);

		this._staticText = new PVStaticText2();
		this._staticText.setBackground(null);
		this.setViewportView(this._staticText);
		this.getViewport().setOpaque(false);
	}

	public void setText(String value)
	{
		this._staticText.setText(value);
	}

	public void setFont(Font font)
	{
		super.setFont(font);
		if(this._staticText != null)
		{
			this._staticText.setFont(font);
		}
	}
}
