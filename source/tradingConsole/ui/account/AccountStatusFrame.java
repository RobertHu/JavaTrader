package tradingConsole.ui.account;

import javax.swing.*;
import java.awt.Component;


public class AccountStatusFrame extends JFrame
{
	public AccountStatusFrame(Component o)
	{
		JScrollPane scrollPanel = new JScrollPane(o);
		super.add(scrollPanel);
	}
}
