package tradingConsole.enumDefine;

import java.awt.Color;

import framework.lang.Enum;

import tradingConsole.ui.colorHelper.QuotationStatusColor;

public class QuotationStatus extends Enum<QuotationStatus>
{
	public static final QuotationStatus None = new QuotationStatus("None", 0);
	public static final QuotationStatus Up = new QuotationStatus("Up", 1);
	public static final QuotationStatus NotChange = new QuotationStatus("NotChange", 2);
	public static final QuotationStatus Down = new QuotationStatus("Down", 3);

	private QuotationStatus(String name, int value)
	{
		super(name, value);
	}

	public static Color getColor(QuotationStatus quotationStatus)
	{
		if (quotationStatus.equals(QuotationStatus.Up))
		{
			return QuotationStatusColor.up;
		}
		else if (quotationStatus.equals(QuotationStatus.NotChange)
			|| quotationStatus.equals(QuotationStatus.None))
		{
			return QuotationStatusColor.notChange;
		}
		else if (quotationStatus.equals(QuotationStatus.Down))
		{
			return QuotationStatusColor.down;
		}
		return Color.black;
	}

	public static Color getBackColor(QuotationStatus quotationStatus)
	{
		if (quotationStatus.equals(QuotationStatus.Up))
		{
			return QuotationStatusColor.up;
		}
		else if (quotationStatus.equals(QuotationStatus.NotChange))
		{
			//return new Color(244, 147, 0);
			return null;
		}
		else if (quotationStatus.equals(QuotationStatus.Down))
		{
			return QuotationStatusColor.down;
		}
		return null;
	}
}
