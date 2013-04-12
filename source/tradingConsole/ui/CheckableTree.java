package tradingConsole.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.*;
import java.util.EventListener;

public class CheckableTree extends JTree
{
	public CheckableTree()
	{
		super((TreeModel)null);

		this.setEditable(true);
		CheckPanelRenderer renderer = new CheckPanelRenderer();
		this.setCellRenderer(renderer);
		this.setCellEditor(new CheckPanelEditor(this, renderer));
		this.addTreeExpansionListener(new ExpansionMonitor());
	}

	@Override
	public void setModel(TreeModel newModel)
	{
		if (newModel != null)
		{
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)newModel.getRoot();
			DefaultMutableTreeNode newRoot = createCheckableTreeNode(null, root);
			super.setModel(new DefaultTreeModel(newRoot));
		}
		else
		{
			super.setModel(newModel);
		}
	}

	/*@Override
	public Rectangle getRowBounds(int row)
	{
		Rectangle bound = super.getRowBounds(row);
		bound.setBounds(bound.x, bound.y, bound.width, bound.height + 6);
		return bound;
	}

	@Override
	public Rectangle getPathBounds(TreePath path)
	{
		Rectangle bound = super.getPathBounds(path);
		bound.setSize(bound.width, bound.height + 6);
		return bound;
	}*/

	private DefaultMutableTreeNode createCheckableTreeNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode treeNode)
	{
		DefaultMutableTreeNode newTreeNode = treeNode;
		if (treeNode.getUserObject() instanceof ICheckable)
		{
			ICheckable userObject = (ICheckable)treeNode.getUserObject();
			newTreeNode = new DefaultMutableTreeNode(userObject);

			if (!treeNode.isLeaf())
			{
				for (int index = 0; index < treeNode.getChildCount(); index++)
				{
					TreeNode childNode = treeNode.getChildAt(index);
					this.createCheckableTreeNode(newTreeNode, (DefaultMutableTreeNode)childNode);
				}
			}
		}
		if (parent != null)
		{
			parent.add(newTreeNode);
		}
		return newTreeNode;
	}
}

class ExpansionMonitor implements TreeExpansionListener
{
	public void treeCollapsed(TreeExpansionEvent e)
	{}

	public void treeExpanded(TreeExpansionEvent e)
	{
		JTree tree = (JTree)e.getSource();
		TreePath path = e.getPath();
		DefaultMutableTreeNode node =
			(DefaultMutableTreeNode)path.getLastPathComponent();
		if (node.getLevel() == 1)
		{
			// Select all children of level one nodes on expansion.
			if (tree.isExpanded(path))
			{
				selectAllChildren(path);
			}
		}
	}

	private void selectAllChildren(TreePath path)
	{
		TreeNode node = (TreeNode)path.getLastPathComponent();
		select(node);
		if (node.getChildCount() > 0)
		{
			java.util.Enumeration e = node.children();
			while (e.hasMoreElements())
			{
				TreeNode n = (TreeNode) (TreeNode)e.nextElement();
				select(n);
				selectAllChildren(path.pathByAddingChild(n));
			}
		}
	}

	private void select(TreeNode node)
	{
		DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
		//ICheckable cs = (ICheckable)n.getUserObject();
		//cs.setChecked(true);
	}
}

interface ICheckable
{
	void setChecked(boolean checked);

	boolean getChecked();

	String getCaption();
}

class CheckPanelEditor extends AbstractCellEditor implements TreeCellEditor, ActionListener
{
	JTree tree;
	CheckPanelRenderer renderer;
	JLabel label;
	EditorPanel panel;
	JCheckBox checkBox;
	ICheckable checkStore;
	protected transient int offset;
	protected int clickCountToStart = 1;

	public CheckPanelEditor(JTree tree, CheckPanelRenderer cpr)
	{
		this.tree = tree;
		renderer = cpr;
		label = new JLabel();
		checkBox = new JCheckBox();
		checkBox.setBorder(null);
		checkBox.setBackground(renderer.getBackgroundNonSelectionColor());
		checkBox.addActionListener(this);
		checkBox.setRequestFocusEnabled(false);
		panel = new EditorPanel();
		panel.setBorder(null);
		panel.setBackground(renderer.getBackgroundNonSelectionColor());
		panel.add(label);
		panel.add(checkBox);
	}

	public Component getTreeCellEditorComponent(JTree tree,
												Object value,
												boolean isSelected,
												boolean expanded,
												boolean leaf,
												int row)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		checkStore = (ICheckable)node.getUserObject();
		label.setText(checkStore.getCaption());
		checkBox.setSelected(checkStore.getChecked());
		if (leaf)
		{
			label.setIcon(renderer.getLeafIcon());
		}
		else if (expanded)
		{
			label.setIcon(renderer.getOpenIcon());
		}
		else
		{
			label.setIcon(renderer.getClosedIcon());
		}
		return panel;
	}

	public Object getCellEditorValue()
	{
		checkStore.setChecked(checkBox.isSelected());
		return checkStore;
	}

	public boolean isCellEditable(EventObject anEvent)
	{
		if (anEvent != null && anEvent instanceof MouseEvent &&
			( (MouseEvent)anEvent).getClickCount() >= clickCountToStart)
		{
			TreePath path = tree.getPathForLocation(
				( (MouseEvent)anEvent).getX(),
				( (MouseEvent)anEvent).getY());
			int row = tree.getRowForPath(path);
			Object value = path.getLastPathComponent();
			boolean isSelected = tree.isRowSelected(row);
			boolean expanded = tree.isExpanded(path);
			boolean leaf = tree.getModel().isLeaf(value);
			determineOffset(tree, value, isSelected, expanded, leaf, row);
			Rectangle pb = tree.getPathBounds(path);
			Rectangle target = new Rectangle(offset, pb.y,
											 pb.x + pb.width - offset,
											 pb.height);
			Point p = ( (MouseEvent)anEvent).getPoint();
			if (target.contains(p))
			{
				tree.startEditingAtPath(path);
				return true;
			}
		}
		return false;
	}

	public void actionPerformed(ActionEvent e)
	{
		super.stopCellEditing();
	}

	protected void determineOffset(JTree tree, Object value,
								   boolean isSelected, boolean expanded,
								   boolean leaf, int row)
	{
		int x0 = tree.getPathBounds(tree.getPathForRow(row)).x;
		int hgap = renderer.getHgap();
		offset = x0 + hgap;
		Icon editingIcon = null;
		if (leaf)
		{
			editingIcon = renderer.getLeafIcon();
		}
		else if (expanded)
		{
			editingIcon = renderer.getOpenIcon();
		}
		else
		{
			editingIcon = renderer.getClosedIcon();
		}
		if (editingIcon != null)
		{
			offset += editingIcon.getIconWidth() +
				renderer.getIconTextGap();
		}
		String stringValue = tree.convertValueToText(value, isSelected,
			expanded, leaf, row, false);
		Font font = tree.getFont();
		FontRenderContext frc = new FontRenderContext(null, false, false);
		int width = (int)font.getStringBounds(stringValue, frc).getWidth();
		offset += width + hgap;
	}

	private class EditorPanel extends JPanel
	{
		Color treeBGColor;
		Color focusBGColor;

		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Color bColor = renderer.getBackgroundSelectionColor();

			int imageOffset = -1;
			if (bColor != null)
			{
				Icon currentI = label.getIcon();

				imageOffset = renderer.getLabelStart();
				g.setColor(bColor);
				g.fillRect(imageOffset, 0, getWidth() - imageOffset,
						   getHeight());
				label.setForeground(new Color(~bColor.getRGB()));
			}

			if (renderer.drawsFocusBorderAroundIcon)
			{
				imageOffset = 0;
			}
			else if (imageOffset == -1)
			{
				imageOffset = renderer.getLabelStart();
			}
			paintFocus(g, imageOffset, 0, getWidth() - imageOffset,
					   getHeight(), bColor);
		}

		private void paintFocus(Graphics g, int x, int y,
								int w, int h, Color notColor)
		{
			Color bsColor = renderer.getBorderSelectionColor();

			if (bsColor != null)
			{
				g.setColor(bsColor);
				g.drawRect(x, y, w - 1, h - 1);
			}
			if (renderer.drawDashedFocusIndicator && notColor != null)
			{
				if (treeBGColor != notColor)
				{
					treeBGColor = notColor;
					focusBGColor = new Color(~notColor.getRGB());
				}
				g.setColor(focusBGColor);
				BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
			}
		}
	}
}

class CheckPanelRenderer extends JPanel implements TreeCellRenderer
{
	JLabel label;
	JCheckBox checkBox;
	protected boolean selected;
	protected boolean hasFocus;
	public boolean drawsFocusBorderAroundIcon;
	public boolean drawDashedFocusIndicator;
	private Color treeBGColor;
	private Color focusBGColor;
	transient protected Icon closedIcon;
	transient protected Icon leafIcon;
	transient protected Icon openIcon;
	protected Color backgroundSelectionColor;
	protected Color backgroundNonSelectionColor;
	protected Color borderSelectionColor;

	public CheckPanelRenderer()
	{
		loadDefaults();
		label = new JLabel();
		checkBox = new JCheckBox();
		checkBox.setBorder(null);
		checkBox.setBackground(getBackgroundNonSelectionColor());
		setBorder(null);
		setBackground(getBackgroundNonSelectionColor());
		add(label);
		add(checkBox);
	}

	private void loadDefaults()
	{
		setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
		setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
		setOpenIcon(UIManager.getIcon("Tree.openIcon"));
		setBackgroundSelectionColor(UIManager.getColor(
			"Tree.selectionBackground"));
		setBackgroundNonSelectionColor(UIManager.getColor(
			"Tree.textBackground"));
		setBorderSelectionColor(UIManager.getColor(
			"Tree.selectionBorderColor"));
		Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
		drawsFocusBorderAroundIcon =
			(value != null && ( (Boolean)value).booleanValue());
		value = UIManager.get("Tree.drawDashedFocusIndicator");
		drawDashedFocusIndicator =
			(value != null && ( (Boolean)value).booleanValue());
	}

	public void setClosedIcon(Icon newIcon)
	{
		closedIcon = newIcon;
	}

	public Icon getClosedIcon()
	{
		return closedIcon;
	}

	public void setLeafIcon(Icon newIcon)
	{
		leafIcon = newIcon;
	}

	public Icon getLeafIcon()
	{
		return leafIcon;
	}

	public void setOpenIcon(Icon newIcon)
	{
		openIcon = newIcon;
	}

	public Icon getOpenIcon()
	{
		return openIcon;
	}

	public void setBackgroundSelectionColor(Color newColor)
	{
		backgroundSelectionColor = newColor;
	}

	public Color getBackgroundSelectionColor()
	{
		return backgroundSelectionColor;
	}

	public void setBackgroundNonSelectionColor(Color newColor)
	{
		backgroundNonSelectionColor = newColor;
		treeBGColor = newColor;
	}

	public Color getBackgroundNonSelectionColor()
	{
		return backgroundNonSelectionColor;
	}

	public void setBorderSelectionColor(Color newColor)
	{
		borderSelectionColor = newColor;
	}

	public Color getBorderSelectionColor()
	{
		return borderSelectionColor;
	}

	public Component getTreeCellRendererComponent(JTree tree,
												  Object value,
												  boolean selected,
												  boolean expanded,
												  boolean leaf,
												  int row,
												  boolean hasFocus)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

		if(node.getUserObject() instanceof ICheckable)
		{
			ICheckable cs = (ICheckable)node.getUserObject();
			label.setText(cs.getCaption());
			checkBox.setSelected(cs.getChecked());
		}

		this.selected = selected;
		this.hasFocus = hasFocus;
		if (leaf)
		{
			label.setIcon(getLeafIcon());
		}
		else if (expanded)
		{
			label.setIcon(getOpenIcon());
		}
		else
		{
			label.setIcon(getClosedIcon());
		}

		return this;
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Color bColor;
		if (selected)
		{
			bColor = getBackgroundSelectionColor();
		}
		else
		{
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null)
			{
				bColor = getBackground();
			}
		}

		int imageOffset = -1;
		if (bColor != null)
		{
			Icon currentI = label.getIcon();

			imageOffset = getLabelStart();
			g.setColor(bColor);
			g.fillRect(imageOffset, 0, getWidth() - imageOffset,
					   getHeight());
			this.label.setForeground(new Color(~bColor.getRGB()));
		}

		if (hasFocus)
		{
			if (drawsFocusBorderAroundIcon)
			{
				imageOffset = 0;
			}
			else if (imageOffset == -1)
			{
				imageOffset = getLabelStart();
			}
			paintFocus(g, imageOffset, 0, getWidth() - imageOffset,
					   getHeight(), bColor);
		}
	}

	private void paintFocus(Graphics g, int x, int y,
							int w, int h, Color notColor)
	{
		Color bsColor = getBorderSelectionColor();

		if (bsColor != null && (selected || !drawDashedFocusIndicator))
		{
			g.setColor(bsColor);
			g.drawRect(x, y, w - 1, h - 1);
		}
		if (drawDashedFocusIndicator && notColor != null)
		{
			if (treeBGColor != notColor)
			{
				treeBGColor = notColor;
				focusBGColor = new Color(~notColor.getRGB());
			}
			g.setColor(focusBGColor);
			BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
		}
	}

	public int getLabelStart()
	{
		Icon currentI = label.getIcon();
		if (currentI != null && label.getText() != null)
		{
			return currentI.getIconWidth() +
				Math.max(0, label.getIconTextGap() - 1) + getHgap();
		}
		return 0;
	}

	public int getIconTextGap()
	{
		return label.getIconTextGap();
	}

	public int getHgap()
	{
		return ( (FlowLayout)getLayout()).getHgap();
	}
}
