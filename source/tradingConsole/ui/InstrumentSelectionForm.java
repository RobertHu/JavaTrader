package tradingConsole.ui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.jidesoft.grid.*;
import framework.*;
import framework.data.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import framework.diagnostics.TraceType;
import java.math.BigDecimal;
import tradingConsole.physical.PendingInventoryManager;
import tradingConsole.physical.InventoryManager;

public class InstrumentSelectionForm extends JDialog//Frame
{
	private static String _dataSourceKey = "InstrumentSelection";
	private static String _unselectedTreeRootName = "Available instruments";

	private tradingConsole.ui.grid.BindingSource _bindingSource;
	private HashMap<Guid, InstrumentSelection> _instumentSelections;
	private HashMap<String, InstrumentTreeNode> _groupNodes;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	public InstrumentSelectionForm(JFrame parent)
	{
		super(parent, true);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public InstrumentSelectionForm(JFrame parent, TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this(parent);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this.headerStaticText.setText(Language.InstrumentSelectlblSelectInstrument);
		this.moveUpButton.setText(Language.InstrumentSelectbtnUp);
		this.moveDownButton.setText(Language.InstrumentSelectbtnDown);
		this.synchronizeButton.setText(Language.InstrumentSelectbtnSynchronize);
		this.okButton.setText(Language.InstrumentSelectbtnOk);
		this.exitButton.setText(Language.InstrumentSelectbtnExit);

		this.synchronize();
	}

	public void dispose()
	{
		InstrumentSelection.unbind(this._dataSourceKey, this._bindingSource);
		super.dispose();
	}

	private boolean _exitOnConfirm = false;
	public void exitOnConfirm()
	{
		this._exitOnConfirm = true;
	}

	private void initialize(boolean ascend, boolean caseOn)
	{
		InstrumentSelection.unbind(this._dataSourceKey, this._bindingSource);
		this.fillUnselectedTree();

		this._bindingSource = new tradingConsole.ui.grid.BindingSource()
		{
			private CellStyle defaultCellStyle = null;

			@Override
			public CellStyle getCellStyleAt(int row, int column)
			{
				CellStyle cellStyle = super.getCellStyleAt(row, column);
				if (cellStyle != null)
				{
					if (cellStyle.getForeground() == null)
						cellStyle.setForeground(Color.BLACK);
					if (cellStyle.getSelectionForeground() == null)
						cellStyle.setSelectionForeground(Color.BLACK);
				}
				if(this.defaultCellStyle == null)
				{
					this.defaultCellStyle = new CellStyle();
					this.defaultCellStyle.setForeground(Color.BLACK);
					this.defaultCellStyle.setForeground(Color.BLACK);
				}
				return cellStyle == null ? this.defaultCellStyle : cellStyle;
			}
		};

		//grid.setRedraw(false);
		ArrayList<InstrumentSelection> selectedInstruments = new ArrayList<InstrumentSelection>();
		for (Iterator<InstrumentSelection> iterator = this._instumentSelections.values().iterator(); iterator.hasNext(); )
		{
			InstrumentSelection instrumentSelection = iterator.next();
			if(instrumentSelection.get_IsSelected()
			   && !instrumentSelection.get_GroupName().equals(InstrumentSelectionForm._unselectedTreeRootName))
			{
				selectedInstruments.add(instrumentSelection);
			}
		}

		InstrumentSelection.initialize(this.instrumentSelectionTable, this._dataSourceKey, selectedInstruments, this._bindingSource);

		for (Iterator<InstrumentSelection> iterator = this._instumentSelections.values().iterator(); iterator.hasNext(); )
		{
			InstrumentSelection instrumentSelection = iterator.next();
			instrumentSelection.update(this._dataSourceKey);
		}
		this.instrumentSelectionTable.setSortingEnabled(false);
		//this.setSequence();
		this.sort(ascend, caseOn);
		this.instrumentSelectionTable.setHeader(0,0,null,null,null);

		//grid.setRedraw(true);
		this.hideSequenceColumn();
	}

	private static InstrumentTreeNode sortTree(InstrumentTreeNode node)
	{
		ArrayList<InstrumentTreeNode> children = new ArrayList<InstrumentTreeNode>(node.getChildCount());
		for (int i = 0; i < node.getChildCount(); i++)
		{
			InstrumentTreeNode childNode = (InstrumentTreeNode)node.getChildAt(i);
			children.add(childNode);
		}
		Collections.sort(children);
		node.removeAllChildren();
		for(InstrumentTreeNode child : children)
		{
			node.add(InstrumentSelectionForm.sortTree(child));
		}
		return node;
	}

	private void hideSequenceColumn()
	{
		int column = this._bindingSource.getColumnByName(InstrumentSelectColKey.Sequence);
		com.jidesoft.grid.TableColumnChooser.hideColumn(this.instrumentSelectionTable, column);
	}

	private void fillUnselectedTree()
	{
		InstrumentTreeNode root = new InstrumentTreeNode(InstrumentSelectionForm._unselectedTreeRootName);
		this._groupNodes = new HashMap<String, InstrumentTreeNode>();
		this._groupNodes.put(InstrumentSelectionForm._unselectedTreeRootName, root);

		for (Iterator<InstrumentSelection> iterator = this._instumentSelections.values().iterator(); iterator.hasNext();)
		{
			InstrumentSelection instrumentSelection = iterator.next();
			if(!instrumentSelection.get_IsSelected())
			{
				this.addToUnselectedTree(instrumentSelection);
			}
		}
		root = InstrumentSelectionForm.sortTree(root);
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		this.unselectedInstrumentsTree.setModel(treeModel);
		if(root.getChildCount() > 0)
		{
			TreeNode[] path = ((InstrumentTreeNode)root.getFirstChild()).getPath();
			this.unselectedInstrumentsTree.expandPath(new TreePath(path));
		}
	}

	private void addToUnselectedTree(InstrumentSelection instrumentSelection)
	{
		String groupName = instrumentSelection.get_GroupName();

		if(!StringHelper.isNullOrEmpty(groupName)
			&& !groupName.equals(InstrumentSelectionForm._unselectedTreeRootName))
		{
			if (!this._groupNodes.containsKey(groupName))
			{
				InstrumentTreeNode group = new InstrumentTreeNode(groupName);
				DefaultMutableTreeNode root = this._groupNodes.get(InstrumentSelectionForm._unselectedTreeRootName);
				root.add(group);
				this._groupNodes.put(groupName, group);
			}
			InstrumentTreeNode treeNode = new InstrumentTreeNode(instrumentSelection);
			this._groupNodes.get(groupName).add(treeNode);
		}
	}

	private void moveToSelectedInstruments(boolean onlySelected)
	{
		DefaultTreeModel treeModel = (DefaultTreeModel)this.unselectedInstrumentsTree.getModel();
		if(onlySelected)
		{
			TreePath[] selectedPathes = this.unselectedInstrumentsTree.getSelectionPaths();
			if(selectedPathes != null)
			{
				if((selectedPathes.length + this.instrumentSelectionTable.getRowCount()) > Parameter.selectInstrumentsRange)
				{
					String info = StringHelper.format(Language.ExceedMaxInstrumentCount, new Object[]{Parameter.selectInstrumentsRange});
					AlertDialogForm.showDialog(this, null, true, info);
					return;
				}
				for (TreePath selectedPath : selectedPathes)
				{
					InstrumentTreeNode treeNode = (InstrumentTreeNode)selectedPath.getLastPathComponent();
					this.moveToSelectedInstruments(treeModel, treeNode);
				}
			}
		}
		else
		{
			InstrumentTreeNode root = this._groupNodes.get(InstrumentSelectionForm._unselectedTreeRootName);
			if((this.getInstrumentCount(root) + this.instrumentSelectionTable.getRowCount()) > Parameter.selectInstrumentsRange)
			{
				String info = StringHelper.format(Language.ExceedMaxInstrumentCount, new Object[]{Parameter.selectInstrumentsRange});
				AlertDialogForm.showDialog(this, null, true, info);
				return;
			}

			this.moveToSelectedInstruments(treeModel, root);
		}
		this.updateSelectButtonStatus();
	}

	private int getInstrumentCount(InstrumentTreeNode node)
	{
		int count = 0;
		if(node.get_InstrumentSelection() != null)
		{
			count++;
		}
		Enumeration children = node.children();
		while(children.hasMoreElements())
		{
			InstrumentTreeNode child = (InstrumentTreeNode)children.nextElement();
			count += this.getInstrumentCount(child);
		}
		return count;
	}

	private void moveToSelectedInstruments(DefaultTreeModel treeModel, InstrumentTreeNode treeNode)
	{
		ArrayList<InstrumentTreeNode> childNodes = new ArrayList<InstrumentTreeNode>();
		Enumeration children = treeNode.children();
		while(children.hasMoreElements())
		{
			InstrumentTreeNode child = (InstrumentTreeNode)children.nextElement();
			childNodes.add(child);
		}
		for(InstrumentTreeNode child : childNodes)
		{
			this.moveToSelectedInstruments(treeModel, child);
		}

		if(!this.isSelectOverRange())
		{
			InstrumentSelection instrumentSelection = treeNode.get_InstrumentSelection();
			if (instrumentSelection != null)
			{
				instrumentSelection.set_Sequence(this.instrumentSelectionTable.getRowCount() + 1);
				this._bindingSource.add(instrumentSelection);
				instrumentSelection.setBackground(this._bindingSource.get_DataSourceKey(),GridBackgroundColor.instrumentSelection);
				treeModel.removeNodeFromParent(treeNode);
			}
		}
	}

	private void moveToUnselectedInstruments(boolean onlySelected)
	{
		ArrayList<InstrumentSelection> shouldRemoveInstruments = new ArrayList<InstrumentSelection>();
		boolean hasCannotDeselectInsturment = false;

		if(onlySelected)
		{
			for (int row : this.instrumentSelectionTable.getSelectedRows())
			{
				InstrumentSelection instrumentSelection = (InstrumentSelection)this.instrumentSelectionTable.getObject(row);
				if (this.canDeselect(instrumentSelection))
				{
					shouldRemoveInstruments.add(instrumentSelection);
				}
				else
				{
					hasCannotDeselectInsturment = true;
				}
			}
		}
		else
		{
			for (int row = 0; row < this.instrumentSelectionTable.getRowCount(); row++)
			{
				InstrumentSelection instrumentSelection = (InstrumentSelection)this.instrumentSelectionTable.getObject(row);
				if (this.canDeselect(instrumentSelection))
				{
					shouldRemoveInstruments.add(instrumentSelection);
				}
				else
				{
					hasCannotDeselectInsturment = true;
				}
			}
		}

		if(hasCannotDeselectInsturment)
		{
			AlertDialogForm.showDialog(this, null, true, Language.SomeInstrumentCannotDeselectedForHasOrder);
		}

		if(shouldRemoveInstruments.size() > 0)
		{
			for (InstrumentSelection instrumentSelection : shouldRemoveInstruments)
			{
				this._bindingSource.remove(instrumentSelection);
				this.addToUnselectedTree(instrumentSelection);
			}
			InstrumentTreeNode root = this._groupNodes.get(InstrumentSelectionForm._unselectedTreeRootName);
			root = InstrumentSelectionForm.sortTree(root);
			DefaultTreeModel treeModel = new DefaultTreeModel(root);
			this.unselectedInstrumentsTree.setModel(treeModel);
			for (InstrumentSelection instrumentSelection : shouldRemoveInstruments)
			{
				String groupName = instrumentSelection.get_GroupName();
				if (!StringHelper.isNullOrEmpty(groupName))
				{
					DefaultMutableTreeNode group = this._groupNodes.get(groupName);
					TreePath path = new TreePath(group.getPath());
					this.unselectedInstrumentsTree.expandPath(path);
				}
			}

			if (shouldRemoveInstruments.size() > 0 && this.instrumentSelectionTable.getRowCount() > 0)
			{
				this.sort(true, false);
				int sequence = 1;
				for (int row = 0; row < this.instrumentSelectionTable.getRowCount(); row++)
				{
					InstrumentSelection instrumentSelection = (InstrumentSelection)this.instrumentSelectionTable.getObject(row);
					instrumentSelection.set_Sequence(sequence);
					sequence++;
				}
				this.sort(true, false);
			}
		}
	}

	private void sort(boolean ascend, boolean caseOn)
	{
		int column = this._bindingSource.getColumnByName(InstrumentSelectColKey.Sequence);
		this.instrumentSelectionTable.sortColumn(column, true, ascend);
	}

	private int getLocalInstrumentSelections()
	{
		this._instumentSelections = new HashMap<Guid, InstrumentSelection> ();
		int maxSequence = 0;
		for (Iterator<Instrument> iterator = this._settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			Instrument instrument = iterator.next();
			InstrumentSelection instrumentSelection = InstrumentSelection.create();
			instrumentSelection.setValue(instrument);
			this._instumentSelections.put(instrument.get_Id(), instrumentSelection);
			maxSequence = maxSequence > instrumentSelection.get_Sequence() ? maxSequence : instrumentSelection.get_Sequence();
		}
		return maxSequence;
	}

	private void move(MoveDirection moveDirection)
	{
		int row = this.instrumentSelectionTable.getSelectedRow();
		if (row == -1)
		{
			return;
		}

		int actualRow = TableModelWrapperUtils.getActualRowAt(this.instrumentSelectionTable.getModel(), row);
		InstrumentSelection instrumentSelection = (InstrumentSelection)this._bindingSource.getObject(actualRow);
		int sequence = instrumentSelection.get_Sequence();
		for (int row2 = 0; row2 < this._bindingSource.getRowCount(); row2++)
		{
			InstrumentSelection instrumentSelection2 = (InstrumentSelection)this._bindingSource.getObject(row2);
			if(instrumentSelection2 != instrumentSelection)
			{
				int sequence2 = instrumentSelection2.get_Sequence();
				if (sequence2 == (sequence + ( (moveDirection == MoveDirection.Up) ? -1 : 1)))
				{
					instrumentSelection2.set_Sequence(sequence);
					instrumentSelection.set_Sequence(sequence2);
					instrumentSelection2.update(this._dataSourceKey);
					instrumentSelection.update(this._dataSourceKey);

					break;
				}
			}
		}
		this.sort(true, true);

		actualRow = this._bindingSource.getRow(instrumentSelection);
		row = this.instrumentSelectionTable.getSortedRowAt(actualRow);
		this.instrumentSelectionTable.changeSelection(row, 0, false, false);
		this.hideSequenceColumn();
	}

	private void moveDown()
	{
		this.move(MoveDirection.Down);
	}

	private void moveUp()
	{
		this.move(MoveDirection.Up);
	}

	//<InstrumentSetting>
	//	<Instrument ID="" Sequence=""></Instrument>
	//	<Instrument ID="" Sequence=""></Instrument>
	//</InstrumentSetting>
	private void submit()
	{
		boolean oldHasMarginInstrument = this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin);
		boolean oldHasPhysicalInstrument =this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical);

		TreeSet<InstrumentSelection> arrayList = new TreeSet<InstrumentSelection> ();
		int sequence = 1;
		int size = this._bindingSource.getRowCount();
		boolean hasChange = size != this._settingsManager.getInstruments().size();

		for (int row = 0; row < size; row++)
		{
			int sortedSequence = this.instrumentSelectionTable.getSortedRowAt(row) + 1;
			InstrumentSelection instrumentSelection = (InstrumentSelection)this._bindingSource.getObject(row);
			instrumentSelection.set_Sequence(sortedSequence);
			Guid instrumentId = instrumentSelection.get_Id();
			Instrument instrument = this._settingsManager.getInstrument(instrumentId);
			//if (instrumentSelection.get_IsSelected())
			{
				arrayList.add(instrumentSelection);
				if (instrument != null)
				{
					if(instrument.get_Sequence() != instrumentSelection.get_Sequence())
					{
						hasChange = true;
						instrument.set_Sequence(instrumentSelection.get_Sequence());
						//instrument.update(Instrument.tradingPanelKey);
						instrument.updateForSelection(Instrument.tradingPanelKey);
						instrument.updateSummaryPanel(Instrument.summaryPanelKey);
					}
				}
				else
				{
					hasChange = true;
				}
				sequence++;
			}
		}

		Iterator<InstrumentTreeNode> groups = this._groupNodes.values().iterator();
		while(groups.hasNext())
		{
			InstrumentTreeNode node = groups.next();
			if(this.removeInstrument(node) > 0)
			{
				hasChange = true;
			}
		}

		if(!hasChange) return;

		Iterator<InstrumentSelection> instrumentSelectionIterator = arrayList.iterator();
		String[] instrumentIds = new String[arrayList.size()];
		for(int i = 0; instrumentSelectionIterator.hasNext();i++)
		{
			InstrumentSelection instrumentSelection = instrumentSelectionIterator.next();
			instrumentIds[i] = instrumentSelection.get_Id().toString();
		}

		DataSet result = this._tradingConsole.get_TradingConsoleServer().updateInstrumentSetting(instrumentIds);
		if (result == null)
		{
			this._tradingConsole.sortForTradingPanel();
			this._tradingConsole.sortForSummaryPanel();
			this._tradingConsole.get_MainForm().changeChartInstrumentList();

			boolean hasMarginInstrument = this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin);
			boolean hasPhysicalInstrument =this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical);

			if(oldHasMarginInstrument != hasMarginInstrument || oldHasPhysicalInstrument != hasPhysicalInstrument)
			{
				this._tradingConsole.resetLayout(true);
				this._tradingConsole.reConnect(false);
			}
		}
		else
		{
			this._tradingConsole.updateData2(result);
			boolean hasMarginInstrument = this._settingsManager.hasInstrumentOf(InstrumentCategory.Margin);
			boolean hasPhysicalInstrument =this._settingsManager.hasInstrumentOf(InstrumentCategory.Physical);

			if(oldHasMarginInstrument != hasMarginInstrument || oldHasPhysicalInstrument != hasPhysicalInstrument)
			{
				this._tradingConsole.resetLayout(true);
				this._tradingConsole.reConnect(false);
			}

			int size2 = this._bindingSource.getRowCount();
			for (int row = 0; row < size2; row++)
			{
				InstrumentSelection instrumentSelection = (InstrumentSelection)this._bindingSource.getObject(row);
				Instrument instrument = this._settingsManager.getInstrument(instrumentSelection.get_Id());
				if(instrument != null)
				{
					instrument.set_Sequence(instrumentSelection.get_Sequence());
					TradingConsole.bindingManager.update(Instrument.tradingPanelKey, instrument);
				}
			}
			this._tradingConsole.get_MainForm().changeChartInstrumentList();
		}
		this._tradingConsole.fillInstrumentSpanGrid();
		this._tradingConsole.get_MainForm().get_QueryPanel().initailize();
	}

	private int removeInstrument(InstrumentTreeNode node)
	{
		int removedCount = 0;
		InstrumentSelection instrumentSelection = node.get_InstrumentSelection();
		if(instrumentSelection != null)
		{
			Guid instrumentId = instrumentSelection.get_Id();
			Instrument instrument = this._settingsManager.getInstrument(instrumentId);
			if (instrument != null)
			{
				removedCount++;
				this._settingsManager.removeInstrument(instrument);
				//Update Instrument UI
				instrument.remove(Instrument.tradingPanelKey);
				instrument.remove(Instrument.summaryPanelKey);
			}
		}

		for(int index = 0; index < node.getChildCount(); index++)
		{
			InstrumentTreeNode child = (InstrumentTreeNode)node.getChildAt(index);
			removedCount += this.removeInstrument(child);
		}

		return removedCount;
	}

	private void synchronize()
	{
		XmlNode xmlNode = this._tradingConsole.get_TradingConsoleServer().getInstrumentForSetting();
		TradingConsole.traceSource.trace(TraceType.Information, xmlNode == null ? "" : xmlNode.get_OuterXml());
		this.handleSynchronizeResult(xmlNode);
	}

	private void handleSynchronizeResult(XmlNode xmlNode)
	{
		ArrayList<String> groupNames = new ArrayList<String>();
		int sequence = this.getLocalInstrumentSelections();

		if (xmlNode != null)
		{
			XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
			for (int i = 0, count = xmlNodeList.get_Count(); i < count; i++)
			{
				XmlNode instrumentNode = xmlNodeList.item(i);
				Guid instrumentId = new Guid(instrumentNode.get_Attributes().get_ItemOf("ID").get_Value());
				String description = (String)instrumentNode.get_Attributes().get_ItemOf("Description").get_Value();
				String groupName = InstrumentSelectionForm._unselectedTreeRootName;
				XmlAttribute groupNameAttribute = instrumentNode.get_Attributes().get_ItemOf("GroupName");
				if(groupNameAttribute != null)
				{
					groupName = (String)groupNameAttribute.get_Value();
					if(!groupNames.contains(groupName)) groupNames.add(groupName);
				}

				if (!this._instumentSelections.containsKey(instrumentId))
				{
					InstrumentSelection instrumentSelection = InstrumentSelection.create();
					instrumentSelection.setValue(instrumentId, false, description, ++sequence, groupName);
					this._instumentSelections.put(instrumentId, instrumentSelection);
				}
				else
				{
					this._instumentSelections.get(instrumentId).set_GroupName(groupName);
				}
				TradingConsole.traceSource.trace(TraceType.Information, "Group of " + description + " is " + groupName);
			}
		}
		this.initialize(true, false);
	}

	private boolean isSelectOverRange()
	{
		return this._bindingSource.getRowCount() > Parameter.selectInstrumentsRange;
	}

	private boolean canDeselect(InstrumentSelection instrumentSelection)
	{
		Instrument instrument = this._settingsManager.getInstrument(instrumentSelection.get_Id());
		if (instrument != null)
		{
			if(instrument.get_Category().equals(InstrumentCategory.Margin))
			{
				if(instrument.get_Transactions() == null || instrument.get_Transactions().size() == 0) return true;

				int valuedTransactionCount = 0;
				int executedTransactionCount = 0;
				for (Transaction transaction : instrument.get_Transactions().values())
				{
					if (transaction.get_Phase() != Phase.Cancelled && transaction.get_Phase() != Phase.Deleted)
					{
						valuedTransactionCount++;
						if (transaction.get_Phase() == Phase.Executed || transaction.get_Phase() == Phase.Completed)
							executedTransactionCount++;
					}
				}
				if (executedTransactionCount == valuedTransactionCount)
				{
					return instrument.get_BuyLots().compareTo(BigDecimal.ZERO) == 0
						&& instrument.get_SellLots().compareTo(BigDecimal.ZERO) == 0;
				}
				else
				{
					return valuedTransactionCount <= 0;
				}
			}
			else if(instrument.get_Category().equals(InstrumentCategory.Physical))
			{
				if(PendingInventoryManager.instance.hasInventoryOf(instrument)
				   || InventoryManager.instance.hasInventoryOf(instrument))
				{
					return false;
				}
				else
				{
					return true;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}

	private void updateSelectButtonStatus()
	{
		boolean enable = false;
		if (unselectedInstrumentsTree.getSelectionPaths() != null
			&& unselectedInstrumentsTree.getSelectionPaths().length > 0)
		{
			for (TreePath path : unselectedInstrumentsTree.getSelectionPaths())
			{
				InstrumentTreeNode treeNode = (InstrumentTreeNode)path.getLastPathComponent();
				if (treeNode.canBeSelected())
				{
					enable = true;
					break;
				}
			}
		}
		selectButton.setEnabled(enable);
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new InstrumentSelectionUi_this_windowAdapter(this));

		this.setSize(450, 320);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.instrumentSelectionFormTitle);
		this.setBackground(FormBackColor.instrumentSelectionForm);

		instrumentSelectionTable.setIntercellSpacing(new Dimension(0, 0));
		instrumentSelectionTable.setSortingEnabled(false);
		instrumentSelectionTable.setSelectionForeground(Color.BLUE);
		headerStaticText.setText("Please select items to display:");
		moveUpButton.setText("Move Up");
		moveUpButton.addActionListener(new InstrumentSelectionForm_moveUpButton_actionAdapter(this));
		moveDownButton.setText("Move Down");
		moveDownButton.addActionListener(new InstrumentSelectionForm_moveDownButton_actionAdapter(this));
		synchronizeButton.setText("Synchronize");
		synchronizeButton.addActionListener(new InstrumentSelectionForm_synchronizeButton_actionAdapter(this));
		okButton.setText("OK");
		okButton.addActionListener(new InstrumentSelectionForm_okButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new InstrumentSelectionForm_exitButton_actionAdapter(this));

		selectButton.setText(">");
		selectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				moveToSelectedInstruments(true);
			}
		});

		selectAllButton.setText(">>");
		selectAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				moveToSelectedInstruments(false);
			}
		});

		deselectButton.setText("<");
		deselectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				moveToUnselectedInstruments(true);
			}
		});

		deselectAllButton.setText("<<");
		deselectAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				moveToUnselectedInstruments(false);
			}
		});

		selectButton.setEnabled(false);
		unselectedInstrumentsTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent e)
			{
				updateSelectButtonStatus();
			}
		});
		deselectButton.setEnabled(false);
		instrumentSelectionTable.addSelectedRowChangedListener(new ISelectedRowChangedListener()
		{
			public void selectedRowChanged(DataGrid source)
			{
				int row = instrumentSelectionTable.getSelectedRow();
				boolean enable = false;
				if(row >= 0	&& row < instrumentSelectionTable.getRowCount())
				{
					InstrumentSelection instrumentSelection = (InstrumentSelection)instrumentSelectionTable.getObject(row);
					enable = canDeselect(instrumentSelection);
				}
				deselectButton.setEnabled(enable);
			}
		});

		this.add(headerStaticText, new GridBagConstraints2(0, 0, 2, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 10, 2, 20), 0, 0));

		//unselectedInstrumentsTree.setBorder(BorderFactory.createEtchedBorder());
		unselectedInstrumentsTree.setFont(new Font("SansSerif", Font.BOLD, 12));

		JScrollPane scrollPane = new JScrollPane(unselectedInstrumentsTree);
		this.add(scrollPane, new GridBagConstraints2(0, 1, 1, 5, 0.5, 1.0
			, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(10, 10, 20, 0), 0, 0));

		this.add(selectButton, new GridBagConstraints2(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(15, 5, 10, 5), 0, 0));
		this.add(selectAllButton, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 10, 5), 0, 0));
		this.add(deselectButton, new GridBagConstraints2(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 10, 5), 0, 0));
		this.add(deselectAllButton, new GridBagConstraints2(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 10, 5), 0, 0));

		JScrollPane scrollPane2 = new JScrollPane(instrumentSelectionTable);
		instrumentSelectionTable.setRowResizable(false);
		instrumentSelectionTable.setColumnResizable(false);
		instrumentSelectionTable.setRowAutoResizes(false);
		this.add(scrollPane2, new GridBagConstraints2(2, 1, 1, 5, 0.5, 1.0
			, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(10, 0, 20, 5), 60, 0));

		this.getContentPane().add(exitButton, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 20, 10), 0, 0));
		this.getContentPane().add(okButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(9, 2, 2, 10), 0, 0));
		this.getContentPane().add(synchronizeButton, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(11, 2, 2, 10), 0, 0));
		this.getContentPane().add(moveDownButton, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(7, 2, 2, 10), 0, 0));
		this.getContentPane().add(moveUpButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 2, 10), 0, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVButton2 moveUpButton = new PVButton2();
	PVButton2 moveDownButton = new PVButton2();
	PVButton2 synchronizeButton = new PVButton2();
	PVButton2 okButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 selectButton = new PVButton2();
	PVButton2 selectAllButton = new PVButton2();
	PVButton2 deselectButton = new PVButton2();
	PVButton2 deselectAllButton = new PVButton2();

	JTree unselectedInstrumentsTree = new JTree();
	PVStaticText2 headerStaticText = new PVStaticText2();
	DataGrid instrumentSelectionTable = new DataGrid("InstrumentSelectionTable");
	GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void moveDownButton_actionPerformed(ActionEvent e)
	{
		this.moveDown();
	}

	public void moveUpButton_actionPerformed(ActionEvent e)
	{
		this.moveUp();
	}

	public void synchronizeButton_actionPerformed(ActionEvent e)
	{
		this.synchronize();
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.submit();
		if(this._exitOnConfirm) this.dispose();
	}

	class InstrumentSelectionUi_this_windowAdapter extends WindowAdapter
	{
		private InstrumentSelectionForm adaptee;
		InstrumentSelectionUi_this_windowAdapter(InstrumentSelectionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class InstrumentTreeNode extends DefaultMutableTreeNode implements Comparable
{
	private InstrumentSelection _instrumentSelection;

	public InstrumentTreeNode(Object userObject)
	{
		super(userObject, true);
	}

	public InstrumentTreeNode(InstrumentSelection instrumentSelection)
	{
		super(instrumentSelection.get_Description(), true);
		this._instrumentSelection = instrumentSelection;
	}

	public InstrumentSelection get_InstrumentSelection()
	{
		return this._instrumentSelection;
	}

	public boolean canBeSelected()
	{
		if(this._instrumentSelection != null)
		{
			return true;
		}
		else
		{
			for(int index = 0; index < this.getChildCount(); index++)
			{
				InstrumentTreeNode child = (InstrumentTreeNode)this.getChildAt(index);
				if(child.canBeSelected()) return true;
			}
		}
		return false;
	}

	public int compareTo(Object o)
	{
		if(o == null || !(o instanceof InstrumentTreeNode))
		{
			return 1;
		}
		this.userObject.toString();
		String description = this.toString();
		String description2 = ((InstrumentTreeNode)o).toString();
		return description.compareToIgnoreCase(description2);
	}

	@Override
	public String toString()
	{
		if(this.userObject instanceof InstrumentSelection)
		{
			return ((InstrumentSelection)this.userObject).get_Description();
		}
		else
		{
			return this.userObject.toString();
		}
	}
}

class InstrumentSelectionForm_okButton_actionAdapter implements ActionListener
{
	private InstrumentSelectionForm adaptee;
	InstrumentSelectionForm_okButton_actionAdapter(InstrumentSelectionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}

class InstrumentSelectionForm_synchronizeButton_actionAdapter implements ActionListener
{
	private InstrumentSelectionForm adaptee;
	InstrumentSelectionForm_synchronizeButton_actionAdapter(InstrumentSelectionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.synchronizeButton_actionPerformed(e);
	}
}

class InstrumentSelectionForm_moveUpButton_actionAdapter implements ActionListener
{
	private InstrumentSelectionForm adaptee;
	InstrumentSelectionForm_moveUpButton_actionAdapter(InstrumentSelectionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.moveUpButton_actionPerformed(e);
	}
}

class InstrumentSelectionForm_moveDownButton_actionAdapter implements ActionListener
{
	private InstrumentSelectionForm adaptee;
	InstrumentSelectionForm_moveDownButton_actionAdapter(InstrumentSelectionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.moveDownButton_actionPerformed(e);
	}
}

class InstrumentSelectionForm_exitButton_actionAdapter implements ActionListener
{
	private InstrumentSelectionForm adaptee;
	InstrumentSelectionForm_exitButton_actionAdapter(InstrumentSelectionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
