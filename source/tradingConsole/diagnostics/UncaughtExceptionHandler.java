package tradingConsole.diagnostics;

import com.jidesoft.swing.JideSwingUtilities;
import framework.FrameworkException;

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
	public void uncaughtException(Thread thread, Throwable throwable)
	{
		try
		{
			if(throwable instanceof NullPointerException)
			{
				String stack = FrameworkException.getStackTrace(throwable);
				if(stack.indexOf("com.jidesoft.grid.EditableTableHeader.isCellEditable") > 0
					|| stack.indexOf("com.jidesoft.docking.k.mousePressed") > 0
					 || stack.indexOf("com.jidesoft.plaf.basic.BasicDockableFrameUI") > 0
					  || stack.indexOf("com.jidesoft.docking.DefaultDockingManager") > 0)
				{
					return;
				}
			}
			else if(throwable instanceof ArrayIndexOutOfBoundsException)
			{
				String stack = FrameworkException.getStackTrace(throwable);
				if(stack.indexOf("sun.font.FontDesignMetrics.charsWidth") > 0)
				{
					return;
				}

			}
			javax.swing.SwingUtilities.invokeLater(new MyRunnable(thread, throwable));
		}
		catch(Exception ex)
		{
			Runtime.getRuntime().exit( -1);
		}
	}

	private static class MyRunnable implements Runnable
	{
		private Thread _thread;
		private Throwable _throwable;

		public MyRunnable(Thread thread, Throwable throwable)
		{
			this._thread = thread;
			this._throwable = throwable;
		}

		public void run()
		{
			try
			{
				ExceptionDialog exceptionDialog = new ExceptionDialog(this._thread, this._throwable);
				exceptionDialog.setAlwaysOnTop(true);
				exceptionDialog.setModal(true);
				JideSwingUtilities.centerWindow(exceptionDialog);
				exceptionDialog.show();
			}
			catch(Exception ex)
			{
				Runtime.getRuntime().exit(-1);
			}
		}
	}
}
