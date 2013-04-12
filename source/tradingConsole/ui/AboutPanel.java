package tradingConsole.ui;

import java.awt.Rectangle;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Panel;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;

public class AboutPanel extends Container implements ActionListener,ComponentListener
{
	private TradingConsole _tradingConsole;

	public void Initialize(TradingConsole tradingConsole)
	{
		this._tradingConsole = tradingConsole;
		//...

		this.relayoutBackgroundImage();
	}

	public void changeBackgroundImage(Image image)
	{
		if (image != null)
		{
			this.img.setImage(image);
		}
		this.relayoutBackgroundImage();
	}

	private void relayoutBackgroundImage()
	{
		if (this.img == null) return;

		try
		{
			Dimension size = this.getSize();
			int x = (size.width - this.img.getIconWidth()) / 2;
			int y = (size.height - this.img.getIconHeight()) / 2;
			if (x < 0)
			{
				x = 0;
			}
			if (y < 0)
			{
				y = 0;
			}
			this.backgroundLabel.setBounds(x, y, this.img.getIconWidth(), this.img.getIconHeight());
		}
		catch (Exception exception)
		{
		}
	}

	public void componentResized(ComponentEvent e)
	{
		this.relayoutBackgroundImage();
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
	//SourceCode End//////////////////////////////////////////////////////////////////////////////////////////////

	public AboutPanel()
	{
		super();
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());

			this.changeBackgroundImage(AppToolkit.getImage("iExchange.gif"));
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void jbInit() throws Exception
	{
		this.setSize(420, 380);
		this.setLayout(gridBagLayout1);
		this.add(backgroundLabel, new GridBagConstraints2(0, 1, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
	}

	public void actionPerformed(ActionEvent e)
	{
	}

	private ImageIcon img = new ImageIcon();
	//private ImageIcon img = new ImageIcon("d:\\title.gif");
	private JLabel backgroundLabel = new JLabel(img);
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
}
