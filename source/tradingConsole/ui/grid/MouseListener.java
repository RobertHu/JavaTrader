package tradingConsole.ui.grid;

import java.awt.event.MouseEvent;

public class MouseListener implements java.awt.event.MouseListener
{
	private IAdvancedMouseListener _advancedMouseListener;

	public MouseListener(IAdvancedMouseListener advancedMouseListener)
	{
		this._advancedMouseListener = advancedMouseListener;
	}

	public void mouseClicked(MouseEvent e)
	{
		if(e.getButton() == MouseEvent.BUTTON1)
		{
			if(e.getClickCount() == 1)
			{
				this._advancedMouseListener.clicked(e);
			}
			else if(e.getClickCount() == 2)
			{
				this._advancedMouseListener.doubleClicked(e);
			}
		}
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}
}
