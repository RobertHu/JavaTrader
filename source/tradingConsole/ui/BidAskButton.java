package tradingConsole.ui;

import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import tradingConsole.ui.language.*;
import tradingConsole.Price;
import tradingConsole.settings.PublicParametersManager;
import framework.StringHelper;

public class BidAskButton extends JButton
{
	private static Font _titleFont = new Font("SansSerif", Font.PLAIN, 12);
	private static Font _buySellFont = new Font("SansSerif", Font.BOLD, 14);
	private static Font _priceLeftFont = new Font("SansSerif", Font.BOLD, 16);
	private static Font _priceRightFont = new Font("SansSerif", Font.BOLD, 32);
	private static Color _disableBackGround = new Color(168,168,168);
	private static Color _enableBidBackGround = new Color(255,168,168);
	private static Color _enableAskBackGround = new Color(168,168,255);

	private static String _quotionPrice = "---";
	//private static Color _disableBackGround = new Color(128,128,128);

	private IPriceProvider _priceProvider;
	private boolean _isBid = false;

	private String _title;
	private String _text;
	private BigDecimal _lastPrice = null;
	private String _price = null;
	private String _priceLeft = null;
	private String _priceRight = null;
	private boolean _quoting = false;
	private int _priceChangeFlag = 0;//0: unchanged; 1: up; -1: down
	private int _rollingCount;
	private ArrayList<String> _rollingPrices = null;

	public BidAskButton(boolean isBid)
	{
		this(isBid, 0);
	}

	public BidAskButton(boolean isBid, int rollingCount)
	{
		this._isBid = isBid;
		this._rollingCount = rollingCount;
		if(this._rollingCount > 0)
		{
			this._rollingPrices = new ArrayList<String> (this._rollingCount);
		}

		this._title = this.getTitle();
		this._text = this.getBuySellText();
	}

	public void set_PriceProvider(IPriceProvider priceProvider)
	{
		this._priceProvider = priceProvider;
		if(priceProvider != null)
		{
			this._title = this.getTitle();
			this._text = this.getBuySellText();
			this.updatePrice();
		}
	}

	public void updatePrice()
	{
		this._quoting = false;

		if(this._rollingCount > 0 && this._price != null)
		{
			if(this._rollingPrices.size() == this._rollingCount)
			{
				this._rollingPrices.remove(0);
			}
			this._rollingPrices.add(this._price);
		}

		this._priceChangeFlag = 0;
		String price = this._isBid ? this._priceProvider.get_Bid() : this._priceProvider.get_Ask();
		if(!StringHelper.isNullOrEmpty(price) && (this._price == null || !price.equalsIgnoreCase(this._price)))
		{
			BigDecimal price2 = new BigDecimal(price);
			if (this._lastPrice != null)
			{
				this._priceChangeFlag = price2.compareTo(this._lastPrice);
			}
			this._lastPrice = price2;
			this._price = price;
			this._priceLeft = Price.getPriceLeft(this._price);//this._isBid ? this._priceProvider.getBidLeft() : this._priceProvider.getAskLeft();
			this._priceRight = Price.getPriceRight(this._price);//this._isBid ? this._priceProvider.getBidRight() : this._priceProvider.getAskRight();
		}
		this.repaint();
	}

	public void quotingPrice()
	{
		this._quoting = true;
		this.repaint();
	}

	public boolean isQuoting()
	{
		return this._quoting;
	}

	@Override
	protected void paintChildren(Graphics g)
	{
		//Rectangle clientSize = new Rectangle();
		//clientSize = SwingUtilities.calculateInnerArea(this, clientSize);

		Dimension clientSize = this.getSize();
		Color enableBackGround = this._isBid ? this._enableBidBackGround : this._enableAskBackGround;
		if(!this._priceProvider.isNormal()) enableBackGround = this._isBid ? this._enableAskBackGround : this._enableBidBackGround;
		Color backGround = this.isEnabled() ? enableBackGround : this._disableBackGround;
		((Graphics2D)g).setPaint(new GradientPaint(clientSize.width/2, 2, backGround,
												   clientSize.width/2, clientSize.height/2, Color.WHITE, true));
		g.fillRect(2, 2, clientSize.width - 4, clientSize.height - 4);

		g.setColor(Color.BLACK);

		g.setFont(BidAskButton._titleFont);
		FontMetrics fontMetrics = this.getFontMetrics(BidAskButton._titleFont);
		int stringHeight = fontMetrics.getHeight() - 4;
		g.drawString(this._title, 4, stringHeight + 2);

		g.setFont(BidAskButton._buySellFont);
		if(!this.isEnabled())
		{
			g.setColor(Color.GRAY);
		}
		else
		{
			if(this._text.equalsIgnoreCase(Language.OrderSingleDQbtnSell))
			{
				g.setColor(Color.RED);
			}
			else
			{
				g.setColor(PublicParametersManager.useGreenAsRiseColor ? new Color(0, 192, 0) : Color.BLUE);
			}
		}
		g.drawString(this._text, 4, clientSize.height - 6);
		/*if(!StringHelper.isNullOrEmpty(this._lotString))
		{
			int stringWidth = SwingUtilities.computeStringWidth(fontMetrics, this._text);
			g.drawString(this._lotString, stringWidth + 8, clientSize.height - 6);
		}*/
		g.setColor(Color.BLACK);

		if(this._quoting)
		{
			g.setFont(BidAskButton._priceLeftFont);
			fontMetrics = this.getFontMetrics(BidAskButton._priceLeftFont);
			int stringHeight2 = fontMetrics.getHeight();
			int stringWidth = SwingUtilities.computeStringWidth(fontMetrics, BidAskButton._quotionPrice);
			g.drawString(BidAskButton._quotionPrice, (clientSize.width - stringWidth) / 2 ,
						 (clientSize.height + stringHeight2) / 2 - 4);
		}
		else
		{
			if (this._priceLeft != null)
			{
				g.setFont(BidAskButton._priceLeftFont);
				fontMetrics = this.getFontMetrics(BidAskButton._priceLeftFont);
				int stringHeight2 = fontMetrics.getHeight() - 4;
				g.drawString(this._priceLeft, 4, stringHeight2 + stringHeight);
			}

			if(this._priceRight != null)
			{
				g.setFont(BidAskButton._priceRightFont);
				fontMetrics = this.getFontMetrics(BidAskButton._priceRightFont);
				stringHeight = fontMetrics.getHeight() - 4;
				int stringWidth = SwingUtilities.computeStringWidth(fontMetrics, this._priceRight);
				g.drawString(this._priceRight, (clientSize.width - stringWidth) / 2 + 4,
							 (clientSize.height + stringHeight) / 2 - 4);
			}

			if (this._priceChangeFlag > 0)
			{
				//g.drawImage(this.getPriceUpImage(), clientSize.width - 22, 6, 16, 16, this);
				int[] xs = new int[]
					{clientSize.width - 10, clientSize.width - 4, clientSize.width - 16};
				int[] ys = new int[]
					{8, 16, 16};

				//g.setColor(new Color(0, 64, 0));
				g.drawPolygon(xs, ys, 3);
				g.setColor(Color.GREEN);
				g.fillPolygon(xs, ys, 3);
			}
			else if (this._priceChangeFlag < 0)
			{
				//g.drawImage(this.getPriceDownImage(), clientSize.width - 22, 6, 16, 16, this);

				int[] xs = new int[]
					{clientSize.width - 10, clientSize.width - 4, clientSize.width - 16};
				int[] ys = new int[]{16, 8, 8};

				//g.setColor(new Color(64, 0, 0));
				g.drawPolygon(xs, ys, 3);
				g.setColor(Color.RED);
				g.fillPolygon(xs, ys, 3);
			}

			if(this._rollingCount > 0 && this._rollingPrices.size() > 0)
			{
				g.setFont(BidAskButton._titleFont);
				g.setColor(Color.BLACK);
				int x = clientSize.width - 50;
				int y = clientSize.height - this._rollingCount * 14;
				for (int index = this._rollingPrices.size() - 1; index >= 0; index--)
				{
					String price = this._rollingPrices.get(index);
					g.drawString(price, x, y);
					y += 14;
				}
			}
		}
	}

	private String getTitle()
	{
		return this._isBid ? "Bid" : "Ask";
	}

	private String getBuySellText()
	{
		if(this._priceProvider == null)
		{
			return this._isBid ? Language.OrderSingleDQbtnSell : Language.OrderSingleDQbtnBuy;
		}
		else
		{
			if(this._priceProvider.isNormal())
			{
				return this._isBid ? Language.OrderSingleDQbtnSell : Language.OrderSingleDQbtnBuy;
			}
			else
			{
				return this._isBid ? Language.OrderSingleDQbtnBuy : Language.OrderSingleDQbtnSell;
			}
		}
	}

	public String get_price()
	{
		return this._price;
	}

	/*private String _lotString = null;
	public void updateLot(String lotString, boolean isBuyLot)
	{
		if(isBuyLot && this._text.equals(Language.OrderSingleDQbtnBuy))
		{
			this._lotString = lotString;
		}
		else if(!isBuyLot && this._text.equals(Language.OrderSingleDQbtnSell))
		{
			this._lotString = lotString;
		}
		this.repaint();
	}*/
}
