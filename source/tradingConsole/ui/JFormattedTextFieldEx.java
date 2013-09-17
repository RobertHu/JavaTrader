package tradingConsole.ui;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class JFormattedTextFieldEx extends JFormattedTextField
{
	public JFormattedTextFieldEx(java.text.Format format, boolean enableLostFocusByEnterOrTabKey)
	{
		super(format);
		if(enableLostFocusByEnterOrTabKey) this.lostFocusByEnterOrTabKey();
	}

	public void lostFocusByEnterOrTabKey()
	{
		this.addKeyListener(new KeyListener()
			{
			public void keyTyped(KeyEvent e)
			{
			}

			public void keyPressed(KeyEvent e)
			{
			}

			public void keyReleased(KeyEvent e)
			{
				if(e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB)
				{
					transferFocus();
				}
			}
		});
	}

	public String getPlainText()
	{
		return super.getText().replaceAll(",", "");
	}
}
