package tradingConsole.ui.grid;

import com.jidesoft.grid.CellStyle;
import tradingConsole.framework.PropertyDescriptor;
import javax.swing.SwingConstants;
import java.awt.Color;

public final class CellStyleHelper
{
	public static CellStyle createCellStyle(PropertyDescriptor property)
	{
		CellStyle cellStyle = new CellStyle();
		cellStyle.setFont(property.get_Font());
		cellStyle.setHorizontalAlignment(property.get_Alignment());
		cellStyle.setBackground(property.get_Background());
		return cellStyle;
	}

	public static CellStyle merge(CellStyle cellStyle1, CellStyle cellStyle2)
	{
		if(cellStyle1 == null && cellStyle2 == null)
		{
			return null;
		}
		else if(cellStyle1 == null)
		{
			return cellStyle2;
		}
		else if(cellStyle2 == null)
		{
			return cellStyle1;
		}
		else
		{
			if(cellStyle1.getPriority() <= cellStyle2.getPriority())
			{
				return CellStyleHelper.mergeFILO(cellStyle1, cellStyle2);
			}
			else
			{
				return CellStyleHelper.mergeFILO(cellStyle2, cellStyle1);
			}
		}
	}

	private static CellStyle mergeFILO(CellStyle cellStyle1, CellStyle cellStyle2)
	{
		CellStyle cellStyle = CellStyleHelper.clone(cellStyle1);

		if(cellStyle2.getBackground() != null) cellStyle.setBackground(cellStyle2.getBackground());
		if(cellStyle2.getBorder() != null) cellStyle.setBorder(cellStyle2.getBorder());
		if(cellStyle2.getFont() != null) cellStyle.setFont(cellStyle2.getFont());
		if(cellStyle2.getFontStyle() != cellStyle2.getFontStyle()) cellStyle.setFontStyle(cellStyle2.getFontStyle());
		if(cellStyle2.getForeground() != null) cellStyle.setForeground(cellStyle2.getForeground());
		if(cellStyle2.getHorizontalAlignment() != cellStyle2.getHorizontalAlignment()) cellStyle.setHorizontalAlignment(cellStyle2.getHorizontalAlignment());
		if(cellStyle2.getIcon() != null) cellStyle.setIcon(cellStyle2.getIcon());
		if(cellStyle2.getSelectionBackground() != null) cellStyle.setSelectionBackground(cellStyle2.getSelectionBackground());
		if(cellStyle2.getSelectionForeground() != null) cellStyle.setSelectionForeground(cellStyle2.getSelectionForeground());
		if(cellStyle2.getText() != null) cellStyle.setText(cellStyle2.getText());
		if(cellStyle2.getVerticalAlignment() != cellStyle2.getVerticalAlignment()) cellStyle.setVerticalAlignment(cellStyle2.getVerticalAlignment());
		cellStyle.setPriority(cellStyle2.getPriority());

		return cellStyle;
	}

	private static CellStyle clone(CellStyle originCellStyle)
	{
		CellStyle cellStyle = new CellStyle();

		cellStyle.setBackground(originCellStyle.getBackground());
		cellStyle.setBorder(originCellStyle.getBorder());
		cellStyle.setFont(originCellStyle.getFont());
		cellStyle.setFontStyle(originCellStyle.getFontStyle());
		cellStyle.setForeground(originCellStyle.getForeground());
		cellStyle.setHorizontalAlignment(originCellStyle.getHorizontalAlignment());
		cellStyle.setIcon(originCellStyle.getIcon());
		cellStyle.setSelectionBackground(originCellStyle.getSelectionBackground());
		cellStyle.setSelectionForeground(originCellStyle.getSelectionForeground());
		cellStyle.setText(originCellStyle.getText());
		cellStyle.setVerticalAlignment(originCellStyle.getVerticalAlignment());
		cellStyle.setPriority(originCellStyle.getPriority());

		return cellStyle;
	}
}
