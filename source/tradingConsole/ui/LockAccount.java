package tradingConsole.ui;

import java.util.Collection;
import java.awt.Color;

import tradingConsole.TradingConsole;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.columnKey.AccountLockColKey;
import tradingConsole.ui.language.AccountLockLanguage;
import tradingConsole.Account;
import tradingConsole.enumDefine.LockedStatus;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import tradingConsole.ui.colorHelper.GridBackgroundColor;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.colorHelper.SelectionBackground;
import javax.swing.SwingConstants;
import tradingConsole.ui.grid.DataGrid;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;

public class LockAccount
{
	private LockedStatus _prevLockedStatus;
	private boolean _isLocked;
	private String _code;

	private Account _account;

	public Account get_Account()
	{
		return this._account;
	}

	public Color getLockedStatusColor()
	{
		return LockedStatus.getColor(this._prevLockedStatus);
	}

	public LockedStatus get_PrevLockedStatus()
	{
		return this._prevLockedStatus;
	}

	public boolean get_IsLocked()
	{
		return this._isLocked;
	}

	public void set_IsLocked(boolean value)
	{
		this._isLocked = value;
	}

	public String get_Code()
	{
		return this._code;
	}

	public void setValue(Account account)
	{
		this._account = account;
		this._prevLockedStatus = this._account.getAccountLockedStatus();
		this._isLocked = (this._prevLockedStatus == LockedStatus.BySelf);
		this._code = account.get_Code();
	}

	private LockAccount()
	{
	}

	public static LockAccount create()
	{
		return new LockAccount();
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(LockAccount.class, AccountLockColKey.IsLocked, false, null,
			AccountLockLanguage.IsLocked, 60, SwingConstants.CENTER, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(LockAccount.class, AccountLockColKey.Code, true, null, AccountLockLanguage.Code, 100,
			SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		//grid.setRedraw(false);
		//grid.reset();
		grid.setShowVerticalLines(false);
		grid.setShowHorizontalLines(false);
		grid.setBackground(GridFixedBackColor.lockAccount);
		grid.setForeground(GridBackColor.lockAccount);
		//grid.setBorderStyle(BorderStyle.lockAccount);
		//grid.setRowLabelWidth(RowLabelWidth.lockAccount);
		//grid.setSelectionBackground(SelectionBackground.lockAccount);
		//grid.setCurrentCellColor(CurrentCellColor.lockAccount);
		//grid.setCurrentCellBorder(CurrentCellBorder.lockAccount);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, LockAccount.getPropertyDescriptors());
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.lockAccount, Color.white, HeaderFont.lockAccount);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, true, true, GridFont.lockAccount, false, true, true);

		//grid.setRedraw(true);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setBackground(dataSourceKey,GridBackgroundColor.lockAccount);
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public void setBackground(String dataSourceKey, String propertyName, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, propertyName, background);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		if (bindingSource != null)
		{
			TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
		}
	}
}
