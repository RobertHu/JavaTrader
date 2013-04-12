package tradingConsole.diagnostics;

import framework.FrameworkException;
import javax.swing.JDialog;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import tradingConsole.TradingConsole;
import framework.DateTime;
import tradingConsole.AppToolkit;

public class ExceptionDialog extends JDialog
{
	private static final String info = "An unhandled exception"  + TradingConsole.enterLine + TradingConsole.enterLine
		+ "We are sorry the exception caused any inconvenience to you.  Kindly assist us to click at the Dump button, and copy/email the same to us at omnicare@163.com." + TradingConsole.enterLine
		+ "The program will exit and you may continue trading to start it again. ";

	private Thread _thread;
	private Throwable _throwable;
	private JButton okButton = new JButton();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JButton dumpButton = new JButton();
	private JTextArea infoTextArea = new JTextArea();

	public ExceptionDialog(Thread thread, Throwable throwable) throws Exception
	{
		this._thread = thread;
		this._throwable = throwable;
		jbInit();
		this.infoTextArea.setText(ExceptionDialog.info);
	}

	private void jbInit() throws Exception
	{
		this.getContentPane().setLayout(gridBagLayout1);
		okButton.setMnemonic('0');
		okButton.setText("OK");
		okButton.addActionListener(new ExceptionDialog_okButton_actionAdapter(this));
		dumpButton.setToolTipText("");
		dumpButton.setText("Dump");
		dumpButton.addActionListener(new ExceptionDialog_dumpButton_actionAdapter(this));
		this.setModal(true);
		infoTextArea.setOpaque(false);
		infoTextArea.setEditable(false);
		infoTextArea.setLineWrap(true);
		this.getContentPane().add(okButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 10, 10), 30, 0));
		this.getContentPane().add(dumpButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 10, 10, 0), 30, 0));
		this.getContentPane().add(infoTextArea, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 10, 0, 10), 0, 0));
		this.setSize(350, 240);
	}

	public void dumpButton_actionPerformed(ActionEvent e)
	{
		try
		{
			File file = File.createTempFile("dump", "");
			String fullFilePath = file.getAbsolutePath();
			PrintWriter traceWriter = new PrintWriter(fullFilePath);

			traceWriter.write(DateTime.get_Now().toString(DateTime.fullFormat));
			traceWriter.write(TradingConsole.enterLine);

			traceWriter.write(AppToolkit.getSystemInfo());
			traceWriter.write(TradingConsole.enterLine);

			traceWriter.write(this._thread.toString());
			traceWriter.write(TradingConsole.enterLine);

			traceWriter.write(FrameworkException.getStackTrace(this._throwable));
			traceWriter.write(TradingConsole.enterLine);

			traceWriter.flush();
			traceWriter.close();

			Runtime.getRuntime().exec("Notepad " + fullFilePath);
		}
		catch (Exception ex)
		{
			this.dispose();
			Runtime.getRuntime().exit(-1);
		}
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
		Runtime.getRuntime().exit(-1);
	}
}

class ExceptionDialog_okButton_actionAdapter implements ActionListener
{
	private ExceptionDialog adaptee;
	ExceptionDialog_okButton_actionAdapter(ExceptionDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}

class ExceptionDialog_dumpButton_actionAdapter implements ActionListener
{
	private ExceptionDialog adaptee;
	ExceptionDialog_dumpButton_actionAdapter(ExceptionDialog adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.dumpButton_actionPerformed(e);
	}
}
