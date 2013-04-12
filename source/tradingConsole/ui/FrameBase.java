package tradingConsole.ui;

import javax.swing.JFrame;
import java.math.BigDecimal;

import javax.swing.JDialog;

public class FrameBase extends JDialog
{
	public FrameBase(JFrame parent)
	{
		super(parent, false);
	}

	protected void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
	}
}
