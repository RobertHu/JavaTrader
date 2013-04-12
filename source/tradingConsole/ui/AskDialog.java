package tradingConsole.ui;

import com.jidesoft.dialog.StandardDialog;
import javax.swing.JComponent;
import com.jidesoft.dialog.ButtonPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import com.jidesoft.swing.MultilineLabel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.util.Locale;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.dialog.ButtonResources;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import java.awt.Frame;
import java.awt.Dialog;
import tradingConsole.ui.language.Language;

public class AskDialog extends StandardDialog
{
	private String info;

	public AskDialog(Frame parent)
	{
		super(parent, Language.alertDialogFormTitle);
	}

	public AskDialog(Dialog parent, boolean modal)
	{
		super(parent, Language.alertDialogFormTitle, modal);
	}

	public JComponent createBannerPanel()
	{
		return null;
	}

	public JComponent createContentPanel()
	{
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		JTextArea textArea = new MultilineLabel();
		textArea.setColumns(50);
		textArea.setRows(20);
		textArea.setText(this.info);
		panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
		return panel;

	}

	public ButtonPanel createButtonPanel()
	{
		ButtonPanel buttonPanel = new ButtonPanel();
		JButton okButton = new JButton();
		JButton cancelButton = new JButton();
		okButton.setName(OK);
		cancelButton.setName(CANCEL);
		buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
		buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);

		okButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.okButtonText")) {
			public void actionPerformed(ActionEvent e) {
				setDialogResult(RESULT_AFFIRMED);
				setVisible(false);
				dispose();
			}
		});
		cancelButton.setAction(new AbstractAction(UIDefaultsLookup.getString("OptionPane.cancelButtonText")) {
			public void actionPerformed(ActionEvent e) {
				setDialogResult(RESULT_CANCELLED);
				setVisible(false);
				dispose();
			}
		});

		setDefaultCancelAction(cancelButton.getAction());
		setDefaultAction(okButton.getAction());
		getRootPane().setDefaultButton(okButton);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return buttonPanel;
	}

	public void setMessage(String info)
	{
		this.info = info;
	}
}
