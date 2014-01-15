package tradingConsole.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tradingConsole.*;
import framework.diagnostics.TraceType;
import framework.xml.XmlNode;
import framework.data.DataSet;
import framework.data.DataRowCollection;
import framework.data.DataRow;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import framework.Guid;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

public class AccountSelectionForm extends JDialog implements IChangedListener
{
	private TradingConsole _tradingConsole;
	private boolean _isForInitailization = false;
	private CheckableTree _tree = new CheckableTree();
	private JScrollPane _treePane;
	private PVButton2 synchronizeButton = new PVButton2();
	private PVButton2 okButton = new PVButton2();
	private PVButton2 exitButton = new PVButton2();
	private PVStaticText2 synchronizeStaticText = new PVStaticText2();
	private boolean selectionChanged = false;
	private boolean refreshed = false;

	private AccountGroup root = null;

	public AccountSelectionForm(JFrame parent, TradingConsole tradingConsole, boolean isForInitailization)
	{
		super(parent, true);

		try
		{
			this._tradingConsole = tradingConsole;
			this._isForInitailization = isForInitailization;
			this._treePane = new JScrollPane(this._tree);
			jbInit();
			this.addWindowListener(new WindowAdapter()
				{
					public void windowClosed(WindowEvent e)
					{
						if(!_isForInitailization && selectionChanged && !refreshed)
						{
							_tradingConsole.get_MainForm().refreshSystem();
							refreshed = true;
						}
					}
				});

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);

			this.addComponentListener(new ComponentAdapter()
			{
				public void componentShown(ComponentEvent e)
				{
					synchronize();
				}
			});
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public void handleChanged(boolean changed)
	{
		int selectedAccountCount = 0;
		for (AccountGroup accountGroup : this.root.getGroups())
		{
			for (AccountSelection account : accountGroup.getChildren())
			{
				if (account.getChecked())
				{
					selectedAccountCount++;
				}
			}
		}

		this.okButton.setEnabled(selectedAccountCount > 0 && changed);
		this._tree.repaint();
	}

	private void submit()
	{
		int selectedAccountCount = 0;
		ArrayList<Guid> deselectedAccounts = new ArrayList<Guid> ();
		for (AccountGroup accountGroup : this.root.getGroups())
		{
			for (AccountSelection account : accountGroup.getChildren())
			{
				if (!account.getChecked())
				{
					deselectedAccounts.add(account.getId());
				}
				else
				{
					selectedAccountCount++;
				}
			}
		}
		if(selectedAccountCount == 0)
		{
		 AlertDialogForm.showDialog(this, Language.accountSelectionFormTitle, true, Language.faileToSaveAccountForSelection);
		 return;
		}

		Guid[] deselectedAccounts2 = new Guid[deselectedAccounts.size()];
		if (this._tradingConsole.get_TradingConsoleServer().updateAccountsSetting(deselectedAccounts.toArray(deselectedAccounts2)))
		{
			selectionChanged = true;
			refreshed = false;
			this.okButton.setEnabled(false);
			if (this._isForInitailization)
			{
				this._tradingConsole.resetSystem(true, false);
				this.dispose();
			}
		}
		else
		{
			AlertDialogForm.showDialog(this, Language.accountSelectionFormTitle, true, Language.faileToSaveAccountForSelection);
		}
	}

	private DataSet dataSet = null;
	private void synchronize()
	{
		this.okButton.setEnabled(this._isForInitailization);
		this.synchronizeButton.setEnabled(false);
		dataSet = null;

		this._treePane.setVisible(false);
		this.synchronizeStaticText.setForeground(Color.BLACK);
		this.synchronizeStaticText.setVisible(true);
		this.synchronizeStaticText.setText(Language.getAccountForSelection);

		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				dataSet = _tradingConsole.get_TradingConsoleServer().getAccountsForSetting();
				updateData();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void updateData()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				innerUpdateData();
			}
		});
	}

	private void innerUpdateData()
	{
		this.synchronizeButton.setEnabled(true);
		if(dataSet == null)
		{
			this._tree.setModel(null);
			this._treePane.setVisible(false);
			this.synchronizeStaticText.setVisible(true);
			this.synchronizeStaticText.setForeground(Color.RED);
			this.synchronizeStaticText.setText(Language.faileToGetAccountForSelection);
		}
		else
		{
			this._treePane.setVisible(true);
			this.synchronizeStaticText.setVisible(false);

			DataRowCollection rows = dataSet.get_Tables().get_Item(0).get_Rows();
			HashMap<String, AccountGroup> accountGroups = new HashMap<String, AccountGroup> ();
			root = new AccountGroup(null, Language.candidateAccounts, false, this);
			for (int index = 0; index < rows.get_Count(); index++)
			{
				DataRow row = rows.get_Item(index);
				String code = (String)row.get_Item("Code");
				boolean selected = (Boolean)row.get_Item("IsSelected");
				String groupName = (String)row.get_Item("GroupName");
				Guid id = (Guid)row.get_Item("ID");

				AccountGroup accountGroup = null;
				if (!accountGroups.containsKey(groupName))
				{
					accountGroup = new AccountGroup(this.root, groupName, false);
					accountGroups.put(groupName, accountGroup);
				}
				else
				{
					accountGroup = accountGroups.get(groupName);
				}
				AccountSelection account = new AccountSelection(accountGroup, id, code, selected);
				accountGroup.add(account);
				accountGroup.setChecked(accountGroup.getChecked() & account.getChecked());
			}

			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(root, true);
			for (AccountGroup accountGroup : accountGroups.values())
			{
				root.add(accountGroup);

				DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(accountGroup, true);
				root.setChecked(root.getChecked() & accountGroup.getChecked());
				rootNode.add(groupNode);

				for (AccountSelection account : accountGroup.getChildren())
				{
					DefaultMutableTreeNode accountNode = new DefaultMutableTreeNode(account, false);
					groupNode.add(accountNode);
				}
			}

			this._tree.setModel(new DefaultTreeModel(rootNode));
		}
	}

	private void jbInit()
	{
		this.setDefaultCloseOperation(this._isForInitailization ? JFrame.DO_NOTHING_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
		this.setSize(300, 420);

		this.setResizable(true);
		this.setLayout(new GridBagLayout());
		this.setTitle(Language.accountSelectionFormTitle);
		this.setBackground(FormBackColor.instrumentSelectionForm);

		this.synchronizeButton.setText(Language.InstrumentSelectbtnSynchronize);
		this.okButton.setText(Language.InstrumentSelectbtnOk);
		this.exitButton.setText(Language.InstrumentSelectbtnExit);
		this.exitButton.setEnabled(!this._isForInitailization);

		Font font = synchronizeStaticText.getFont();
		font = new Font(font.getName(), Font.BOLD, font.getSize());
		synchronizeStaticText.setFont(font);
		synchronizeStaticText.setHorizontalAlignment(PVStaticText2.CENTER);
		this.add(this.synchronizeStaticText, new GridBagConstraints2(0, 0, 4, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 5, 2, 5), 0, 0));
		this.synchronizeStaticText.setVisible(false);

		this._tree.setRowHeight(this._tree.getRowHeight() + 4);
		this.add(this._treePane, new GridBagConstraints2(0, 0, 4, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 5, 2, 5), 0, 0));

		this.add(this.synchronizeButton, new GridBagConstraints2(0, 1, 1, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 1), 20, 0));
		this.add(this.okButton, new GridBagConstraints2(2, 1, 1, 1, 0, 0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 0, 5, 0), 20, 0));
		this.add(this.exitButton, new GridBagConstraints2(3, 1, 1, 1, 0, 0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 5, 5), 20, 0));

		synchronizeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				synchronize();
			}
		});

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				submit();
			}
		});

		exitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}
}

interface IChangedListener
{
	void handleChanged(boolean changed);
}

class AccountGroup extends DefaultCheckable
{
	private ArrayList<AccountGroup> groups = new ArrayList<AccountGroup>();
	private ArrayList<AccountSelection> children = new ArrayList<AccountSelection>();
	private AccountGroup parent;
	private boolean changed = false;
	private IChangedListener changedListener = null;

	AccountGroup(AccountGroup parent, String name, boolean checked)
	{
		this(parent, name, checked, null);
	}

	AccountGroup(AccountGroup parent, String name, boolean checked, IChangedListener changedListener)
	{
		super(name, checked);
		this.parent = parent;
		this.changedListener = changedListener;
	}

	@Override
	public void setChecked(boolean checked)
	{
		if(checked != this.getChecked())
		{
			super.setChecked(checked);

			for (AccountGroup accountGroup : this.groups)
			{
				accountGroup.setChecked(checked);
			}

			for (AccountSelection account : this.children)
			{
				account.setChecked(checked);
			}
		}
	}

	public void add(AccountSelection account)
	{
		this.children.add(account);
	}

	public void add(AccountGroup accountGroup)
	{
		this.groups.add(accountGroup);
	}

	public Collection<AccountGroup> getGroups()
	{
		return this.groups;
	}

	public Collection<AccountSelection> getChildren()
	{
		return this.children;
	}

	public void setChanged(boolean value)
	{
		this.changed = value;
		if(this.parent != null)
		{
			this.parent.setChanged(value);
		}
		if(changedListener != null) changedListener.handleChanged(value);
	}
}

class AccountSelection extends DefaultCheckable
{
	private Guid id;
	private AccountGroup parent;

	AccountSelection(AccountGroup parent, Guid id, String name, boolean checked)
	{
		super(name, checked);
		this.parent = parent;
		this.id = id;
	}

	Guid getId()
	{
		return this.id;
	}

	@Override
	public void setChecked(boolean checked)
	{
		if(checked != this.getChecked())
		{
			super.setChecked(checked);
			this.parent.setChanged(true);
		}
	}
}

class DefaultCheckable implements ICheckable
{
	private String name;
	private boolean checked;

	DefaultCheckable(String name, boolean checked)
	{
		this.name = name;
		this.checked = checked;
	}

	public void setChecked(boolean checked)
	{
		if(this.checked != checked)
		{
			this.checked = checked;
		}
	}

	public boolean getChecked()
	{
		return this.checked;
	}

	public String getCaption()
	{
		return this.name;
	}

	@Override
	public String toString()
	{
		return this.getCaption();
	}
}
