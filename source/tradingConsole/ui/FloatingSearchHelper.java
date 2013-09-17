package tradingConsole.ui;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.awt.Point;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowEvent;
import javax.swing.JWindow;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.KeyStroke;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import framework.StringHelper;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import tradingConsole.ui.language.Language;
import java.awt.event.InputEvent;

public class FloatingSearchHelper
{
	private static ArrayList<JComponent> residers = new ArrayList<JComponent>();
	public static interface SearchHandler
	{
		public void doSearch(String searchStr);
	}

	public static void register(JComponent resider, JFrame parent, SearchHandler searchHandler)
	{
		if(!FloatingSearchHelper.residers.contains(resider))
		{
			FloatingSearchHelper.residers.add(resider);
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "ShowSearchWindow");
			//resider.addKeyListener(new KeyListener(resider, parent));
			/*resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_6, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_7, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_8, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_9, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0), "ShowSearchWindow");
			resider.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "ShowSearchWindow");*/

			resider.getActionMap().put("ShowSearchWindow", new ActionListener(resider, parent, searchHandler));
			/*resider.registerKeyboardAction(new ActionListener(resider, parent),
										   KeyStroke.getKeyStroke(KeyEvent.KEY_PRESSED, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);*/
		}
	}

	private static class FloatingSearchWindow extends JDialog
	{
		private JLabel label = new JLabel();
		private JTextField textField = new JTextField();
		private SearchHandler searchHandler;

		FloatingSearchWindow(JFrame parent, String initStr, SearchHandler searchHandler)
		{
			super(parent);
			this.searchHandler = searchHandler;

			this.jbInit();

			this.addFocusListener(new FocusListener()
				{
				public void focusGained(FocusEvent e)
				{
					textField.requestFocus();
				}

				public void focusLost(FocusEvent e)
				{
				}
			});

			this.textField.addFocusListener(new FocusListener()
				{
				public void focusGained(FocusEvent e)
				{
				}

				public void focusLost(FocusEvent e)
				{
					dispose();
				}
			});


			this.textField.getDocument().addDocumentListener(new DocumentListener()
				{
				public void insertUpdate(DocumentEvent e)
				{
					doSearch();
				}

				public void removeUpdate(DocumentEvent e)
				{
					doSearch();
				}

				public void changedUpdate(DocumentEvent e)
				{
					doSearch();
				}
			});

			if(!StringHelper.isNullOrEmpty(initStr))
			{
				this.textField.setText(initStr);
			}

			java.awt.event.ActionListener escListener = new java.awt.event.ActionListener()
			{
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			};
			this.getRootPane().registerKeyboardAction(escListener,
												 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
												 JComponent.WHEN_IN_FOCUSED_WINDOW);
		}

		private void doSearch()
		{
			this.searchHandler.doSearch(textField.getText());
		}

		private void jbInit()
		{
			this.setSize(180, 20);
			JPanel panel = new JPanel();
			panel.setBorder(new LineBorder(Color.BLACK, 1, false));
			panel.setBackground(new Color(255,255,218));
			panel.setLayout(new GridBagLayout());

			this.setResizable(false);
			this.setModal(false);
			this.setUndecorated(true);

			this.label.setText(Language.searchFor);
			this.label.setForeground(Color.GRAY);

			this.label.setBorder(new EmptyBorder(0, 0, 0, 0));
			this.textField.setBorder(new EmptyBorder(0, 0, 0, 0));
			this.textField.setBackground(null);

			panel.add(this.label, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 2, 0, 0), 0, 0));
			panel.add(this.textField, new GridBagConstraints2(1, 0, 1, 1, 1.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 80, 0));
			this.textField.requestFocus();

			this.add(panel);
		}
	}

	private static class ActionListener extends AbstractAction//implements java.awt.event.ActionListener
	{
		private JComponent resider;
		private JFrame parent;
		private SearchHandler searchHandler;
		ActionListener(JComponent resider, JFrame parent, SearchHandler searchHandler)
		{
			this.resider = resider;
			this.parent = parent;
			this.searchHandler = searchHandler;
		}

		public void actionPerformed(ActionEvent e)
		{
			Point point = this.resider.getLocationOnScreen();
			JPanel panel = new JPanel();
			panel.setBorder(new LineBorder(Color.BLACK, 1, false));

			//FloatingSearchWindow floatingSearchWindow = new FloatingSearchWindow(this.parent, e.getActionCommand(), this.searchHandler);
			FloatingSearchWindow floatingSearchWindow = new FloatingSearchWindow(this.parent, null, this.searchHandler);
			point.x += 30;
			point.y += 0;
			floatingSearchWindow.setLocation(point);
			floatingSearchWindow.show();
			floatingSearchWindow.toFront();
			floatingSearchWindow.requestFocus();
		}
	}
}
