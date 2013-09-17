package tradingConsole.physical;

import framework.Guid;
import framework.data.DataRow;
import framework.xml.XmlNode;
import framework.DateTime;
import java.math.BigDecimal;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.framework.PropertyDescriptor;
import javax.swing.SwingConstants;
import tradingConsole.ui.language.OpenOrderLanguage;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import tradingConsole.Order;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;
import tradingConsole.ui.columnKey.OpenOrderColKey;
import framework.xml.XmlAttributeCollection;
import tradingConsole.AppToolkit;
import framework.DBNull;

public abstract class PendingInventory
{
	public static final String bindingKey = "pendingInventoryBindingKey";

	protected Guid id;
	protected Guid accountId;
	protected Guid instrumentId;

	protected Account account;
	protected String code;
	protected DateTime submitTime;
	protected BigDecimal weight;
	protected String unit;
	protected Short status;

	public Guid getId()
	{
		return this.id;
	}

	public Account getAccount()
	{
		return this.account;
	}

	public String get_Code()
	{
		return this.code;
	}

	public String get_AccountCode()
	{
		return this.account.getCode();
	}

	public String get_SubmitTime()
	{
		return this.submitTime.toString(Inventory.DateTimeFormat);
	}

	public String get_Weight()
	{
		return AppToolkit.format(this.weight.doubleValue(), this.get_Decimals());
	}

	public String get_Unit()
	{
		return this.unit;
	}

	public Guid get_InstrumentId()
	{
		return this.instrumentId;
	}

	public abstract  String get_Instrument();
	public abstract String get_Status();
	public abstract String get_SettlementTime();
	public abstract int get_Decimals();
	protected abstract void initStatus();

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[8];
		int index = 0;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.Code, true, null, PhysicalInventoryLanguage.Code,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.SubmitTime, true, null, PhysicalInventoryLanguage.SubmitTime,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, OpenOrderColKey.AccountCode, true, null, OpenOrderLanguage.AccountCode,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.Instrument, true, null, PhysicalInventoryLanguage.Instrument,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.Weight, true, null, PhysicalInventoryLanguage.Weight,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.Unit, true, null, PhysicalInventoryLanguage.Unit,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.Status, true, null, PhysicalInventoryLanguage.Status,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(PendingInventory.class, PhysicalInventoryColKey.SettlementTime, true, null, PhysicalInventoryLanguage.SettlementTime,
			80, SwingConstants.CENTER, null, null, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public void initialize(DataRow dataRow, SettingsManager settings)
	{
		this.id = (Guid)dataRow.get_Item("Id");
		this.accountId = (Guid)dataRow.get_Item("AccountId");
		this.code = (String)dataRow.get_Item("Code");
		this.submitTime = (DateTime)dataRow.get_Item("SubmitTime");
		this.status = (Short)dataRow.get_Item("Status");
		this.initStatus();
		this.unit = dataRow.get_Item("Unit") == DBNull.value ? "" : (String)dataRow.get_Item("Unit");

		this.account = settings.getAccount(this.accountId);
	}

	public void initialize(XmlNode node, SettingsManager settings)
	{
		XmlAttributeCollection attributes = node.get_Attributes();

		this.id = new Guid(attributes.get_ItemOf("Id").get_Value());
		this.accountId = new Guid(attributes.get_ItemOf("AccountId").get_Value());
		this.code = attributes.get_ItemOf("Code").get_Value();
		this.submitTime = DateTime.valueOf(attributes.get_ItemOf("SubmitTime").get_Value());
		this.status = Short.parseShort(attributes.get_ItemOf("Status").get_Value());
		this.initStatus();

		this.account = settings.getAccount(this.accountId);
	}
}
