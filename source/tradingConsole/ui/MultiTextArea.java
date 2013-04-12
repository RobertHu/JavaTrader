package tradingConsole.ui;

import java.awt.*;
import javax.swing.*;

import framework.*;
import java.awt.event.KeyAdapter;

public class MultiTextArea extends JScrollPane
{
	private JTextArea _textArea;

	public MultiTextArea()
	{
		this._textArea = new JTextArea();
		this._textArea.setBackground(Color.white);
		this._textArea.setEditable(false);
		this._textArea.setLineWrap(true);
		this.setViewportView(this._textArea);
	}

	public void setMessage(String str)
	{
		if (str==null)str = "";
		this._textArea.setText(StringHelper.replace(str, "\\n", "\n"));
	}

	public void append(String str)
	{
		if (str==null)str = "";
		this._textArea.append(StringHelper.replace(str, "\\n", "\n"));
	}

	public void scrollToFirstLine()
	{
		this._textArea.setCaretPosition(0);
	}

	public void setTextColor(Color color)
	{
		this._textArea.setForeground(color);
	}

	public void setText(String string)
	{
		this._textArea.setText(string);
	}

	public void setColumns(int i)
	{
		this._textArea.setColumns(i);
	}

	public void setEditable(boolean b)
	{
		this._textArea.setEditable(b);
	}

	public String getText()
	{
		return this._textArea.getText();
	}

	public void addKeyAdapter(KeyAdapter keyAdapter)
	{
		this._textArea.addKeyListener(keyAdapter);
	}
}
