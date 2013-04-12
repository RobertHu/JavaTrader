package tradingConsole.settings;

import framework.Guid;
import java.awt.Window;

public class MakeOrderWindow
{
	private Guid _instrumentId;
	private Window _mainWindow;
	private Window _quoteWindow;
	private Window _notifyIsAcceptWindow;

	private boolean _isQuoting;

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public Window get_MainWindow()
	{
		return this._mainWindow;
	}

	public Window get_QuoteWindow()
	{
		return this._quoteWindow;
	}

	public void set_QuoteWindow(Window value)
	{
		this._quoteWindow = value;
	}

	public Window get_NotifyIsAcceptWindow()
	{
		return this._notifyIsAcceptWindow;
	}

	public void set_NotifyIsAcceptWindow(Window value)
	{
		this._notifyIsAcceptWindow = value;
	}

	public boolean get_IsQuoting()
	{
		return this._isQuoting;
	}

	public void set_IsQuoting(boolean value)
	{
		this._isQuoting = value;
	}

	public MakeOrderWindow(Guid instrumentId,Window mainWindow,boolean isQuoting)
	{
		this._instrumentId=instrumentId;
		this._mainWindow = mainWindow;
		this._isQuoting = isQuoting;
	}

	public void closeAllWindow()
	{
		if (this._quoteWindow != null)
		{
			try
			{
				this._quoteWindow.dispose();
			}
			catch (Throwable exception)
			{
			}
		}
		if (this._mainWindow != null)
		{
			try
			{
				this._mainWindow.dispose();
			}
			catch (Throwable exception2)
			{
			}
		}
	}
}
