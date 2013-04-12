package tradingConsole;

import java.util.Iterator;

import framework.Guid;
import framework.data.DataRow;
import framework.xml.XmlNode;
import tradingConsole.settings.SettingsManager;
import framework.xml.XmlAttributeCollection;
import framework.DateTime;
import framework.data.DataSet;
import framework.StringHelper;

public class Customer
{
	private SettingsManager _settingsManager;

	private Guid _userId;
	private String _customerCode;
	private String _customerName;
	private String _email;
	private boolean _isDisplayLedger;
	private int _allowFreeAgent;
	private int _singleAccountOrderType;
	private int _multiAccountsOrderType;
	private int _dQOrderOutTime;
	private int _assignOrderType;
	private boolean _isCalculateFloat;
	private boolean _isSendOrderMail;
	private boolean _disallowTrade;
	//DisplayAlert: 0: none; 1:1; 2:1,2; 3:1,3; 4:1,2,3; 5:2; 6:2,3; 7:3
	private int _displayAlert;
	private boolean _isNoShowAccountStatus;
	private boolean _isEmployee = false;
	private boolean _showLog;
	private DateTime _lastLogTime;
	private Guid _publicQuotePolicyId;
	private Guid _privateQuotePolicyId;
	private Guid _dealingPolicyId;

	public Guid get_UserId()
	{
		return this._userId;
	}

	public Guid get_PrivateQuotePolicyId()
	{
		return this._privateQuotePolicyId;
	}

	public Guid get_PublicQuotePolicyId()
	{
		return this._publicQuotePolicyId;
	}

	public Guid get_DealingPolicyId()
	{
		return this._dealingPolicyId;
	}

	public String get_CustomerCode()
	{
		return this._customerCode;
	}

	public String get_CustomerName()
	{
		return StringHelper.isNullOrEmpty(this._customerName) ? this._customerCode : this._customerName;
	}

	public String get_Email()
	{
		return this._email;
	}

	public boolean get_IsDisplayLedger()
	{
		return this._isDisplayLedger;
	}

	public int get_AllowFreeAgent()
	{
		return this._allowFreeAgent;
	}

	public int get_SingleAccountOrderType()
	{
		return this._singleAccountOrderType;
	}

	public int get_MultiAccountsOrderType()
	{
		return this._multiAccountsOrderType;
	}

	public int get_DQOrderOutTime()
	{
		return this._dQOrderOutTime;
	}

	public int get_AssignOrderType()
	{
		return this._assignOrderType;
	}

	public boolean get_IsCalculateFloat()
	{
		return this._isCalculateFloat;
	}

	public void set_IsCalculateFloat(boolean value)
	{
	  this._isCalculateFloat = value;
	}

	public boolean get_IsSendOrderMail()
	{
		return this._isSendOrderMail;
	}

	public boolean get_DisallowTrade()
	{
		return this._disallowTrade;
	}

	public int get_DisplayAlert()
	{
		return this._displayAlert;
	}

	public boolean get_IsNoShowAccountStatus()
	{
		return this._isNoShowAccountStatus;
	}

	public boolean get_IsEmployee()
	{
		return this._isEmployee;
	}

	public boolean get_ShowLog()
	{
		return this._showLog;
	}

	public DateTime get_LastLogTime()
	{
		return this._lastLogTime;
	}

	public Customer(SettingsManager settingsManager)
	{
		this._settingsManager = settingsManager;
	}

	public void setValue(DataRow dataRow)
	{
		this._userId = (Guid) dataRow.get_Item("ID");
		this._customerCode = (String) dataRow.get_Item("Code");
		this._customerName = AppToolkit.isDBNull(dataRow.get_Item("Name"))?null:(String) dataRow.get_Item("Name");
		this._email = AppToolkit.isDBNull(dataRow.get_Item("Email"))?null:(String) dataRow.get_Item("Email");
		this._isDisplayLedger = (Boolean) dataRow.get_Item("IsDisplayLedger");
		this._allowFreeAgent = (Integer) dataRow.get_Item("AllowFreeAgent");
		this._singleAccountOrderType = (Integer) dataRow.get_Item("SingleAccountOrderType");
		this._multiAccountsOrderType = (Integer) dataRow.get_Item("MultiAccountsOrderType");
		this._dQOrderOutTime = (Integer) dataRow.get_Item("DQOrderOutTime");
		this._assignOrderType = (Integer) dataRow.get_Item("AssignOrderType");
		this._isCalculateFloat = (Boolean) dataRow.get_Item("IsCalculateFloat");
		this._isSendOrderMail = (Boolean) dataRow.get_Item("IsSendOrderMail");
		this._disallowTrade = (Boolean) dataRow.get_Item("DisallowTrade");
		this._displayAlert = (Integer) dataRow.get_Item("DisplayAlert");
		this._isEmployee = (Boolean)dataRow.get_Item("IsEmployee");
		this._isNoShowAccountStatus = (Boolean) dataRow.get_Item("IsNoShowAccountStatus");
		this._showLog = AppToolkit.isDBNull(dataRow.get_Item("ShowLog"))?false:(Boolean) dataRow.get_Item("ShowLog");
		this._lastLogTime = (DateTime) dataRow.get_Item("LastLogTime");
		if(dataRow.get_Table().get_Columns().contains("PrivateQuotePolicyID"))
		{
			this._privateQuotePolicyId = (Guid)dataRow.get_Item("PrivateQuotePolicyID");
		}
		if(dataRow.get_Table().get_Columns().contains("PublicQuotePolicyID"))
		{
			this._publicQuotePolicyId = (Guid)dataRow.get_Item("PublicQuotePolicyID");
		}
		if(dataRow.get_Table().get_Columns().contains("DealingPolicyID"))
		{
			this._dealingPolicyId = AppToolkit.isDBNull(dataRow.get_Item("DealingPolicyID")) ? null : (Guid)dataRow.get_Item("DealingPolicyID");
		}
	}

	private void setValue(TradingConsole tradingConsole,XmlAttributeCollection customerCollection)
	{
		Guid userId = new Guid(customerCollection.get_ItemOf("ID").get_Value());
		if (!userId.equals(this._userId)) return;

		for (int i = 0; i < customerCollection.get_Count(); i++)
		{
			String nodeName = customerCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = customerCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Code"))
			{
				this._customerCode = nodeValue;
			}
			if (nodeName.equals("PrivateQuotePolicyID")
				|| nodeName.equals("EmployeeQuotePolicyID"))
			{
				this._privateQuotePolicyId = new Guid(nodeValue);
			}
			if (nodeName.equals("PublicQuotePolicyID"))
			{
				this._publicQuotePolicyId = new Guid(nodeValue);
			}
			if (nodeName.equals("DealingPolicyID")
				|| nodeName.equals("EmployeeDealingPolicyID"))
			{
				if(!StringHelper.isNullOrEmpty(nodeValue))
				{
					this._dealingPolicyId = new Guid(nodeValue);
				}
				else
				{
					this._dealingPolicyId = null;
				}
			}
			else if (nodeName.equals("Name"))
			{
				this._customerName = nodeValue;
			}
			else if (nodeName.equals("Email"))
			{
				this._email = nodeValue;
				for (Iterator<Account> iterator = this._settingsManager.getAccounts().values().iterator(); iterator.hasNext(); )
				{
					Account account = iterator.next();
					if (account.get_CustomerId().equals(userId))
					{
						account.set_CustomerEmail(nodeValue);
					}
					else if (account.get_AgentId() != null && account.get_AgentId().equals(userId))
					{
						account.set_AgentEmail(nodeValue);
					}
				}
			}
			else if (nodeName.equals("Pager"))
			{
				this._isDisplayLedger = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowFreeAgent")
				|| nodeName.equals("EmployeeAllowFreeAgent"))
			{
				this._allowFreeAgent = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("SingleAccountOrderType")
				|| nodeName.equals("EmployeeSingleAccountOrderType"))
			{
				this._singleAccountOrderType = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("MultiAccountsOrderType")
				|| nodeName.equals("EmployeeMultiAccountsOrderType"))
			{
				this._multiAccountsOrderType = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DQOrderOutTime")
				|| nodeName.equals("EmployeeDQOrderOutTime"))
			{
				this._dQOrderOutTime = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("AssignOrderType")
				|| nodeName.equals("EmployeeAssignOrderType"))
			{
				this._assignOrderType = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsCalculateFloat")
				|| nodeName.equals("EmployeeIsCalculateFloat"))
			{
				this._isCalculateFloat = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("DisplayAlert")
				|| nodeName.equals("EmployeeDisplayAlert"))
			{
				this._displayAlert = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DisallowTrade"))
			{
				this._disallowTrade = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsTradingAllowed"))
			{
				this._disallowTrade = !Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsNoShowAccountStatus")
				|| nodeName.equals("EmployeeIsNoShowAccountStatus"))
			{
				boolean newValue = Boolean.valueOf(nodeValue);
				if(this._isNoShowAccountStatus != newValue)
				{
					this._isNoShowAccountStatus = newValue;

					tradingConsole.get_AccountBindingManager().clear();
					this._settingsManager.showAccountStatus();
					tradingConsole.get_MainForm().setMenuVisible();
				}
			}
			else if (nodeName.equals("ShowLog")
				|| nodeName.equals("EmployeeShowLog"))
			{
				this._showLog = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsSendOrderMail"))
			{
				this._isSendOrderMail = Boolean.valueOf(nodeValue);
			}
		}
	}

	public static void updateCustomers(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode customersNode, String updateType)
	{
		if (customersNode == null)
		{
			return;
		}

		Customer customer = settingsManager.get_Customer();
		for (int i = 0; i < customersNode.get_ChildNodes().get_Count(); i++)
		{
			XmlNode customerNode = customersNode.get_ChildNodes().itemOf(i);

			XmlAttributeCollection customerCollection = customerNode.get_Attributes();
			Guid userId = new Guid(customerCollection.get_ItemOf("ID").get_Value());
			if (userId.equals(customer.get_UserId()))
			{
				updateCustomer(tradingConsole, settingsManager, customerNode, updateType);
				break;
			}
		}
	}

	public static void updateCustomer(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode customerNode, String updateType)
	{
		if (customerNode == null)
		{
			return;
		}
		XmlAttributeCollection customerCollection = customerNode.get_Attributes();
		if (updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			Customer customer = settingsManager.get_Customer();
			Guid oldPrivateQuotePolicyId= customer.get_PrivateQuotePolicyId();
			Guid oldDealingPolicyId = customer.get_DealingPolicyId();
			customer.setValue(tradingConsole,customerCollection);
			Guid userId = new Guid(customerCollection.get_ItemOf("ID").get_Value());

			if (userId.equals(customer.get_UserId())
				&& !GuidHelper.equals(oldPrivateQuotePolicyId, customer.get_PrivateQuotePolicyId()))
			{
				DataSet dataSet = tradingConsole.get_TradingConsoleServer().getQuotePolicyDetailsAndRefreshInstrumentsState(userId);
				if(dataSet != null && dataSet.get_Tables().get_Item("QuotePolicyDetail") != null)
				{
					settingsManager.updateQuotePolicyDetails(dataSet.get_Tables().get_Item("QuotePolicyDetail"));
				}
			}

			if(userId.equals(customer.get_UserId()) && customer.get_DealingPolicyId() != null
			   && !GuidHelper.equals(oldDealingPolicyId, customer.get_DealingPolicyId()))
			{
				settingsManager.getClearDealingPolicyDetails();
				DataSet dataSet = tradingConsole.get_TradingConsoleServer().getDealingPolicyDetails();
				if(dataSet != null && dataSet.get_Tables().get_Count() > 0)
				{
					settingsManager.replaceDealingPolicyDetails(dataSet.get_Tables().get_Item(0));
				}
			}
		}
	}

	private static class GuidHelper
	{
		public static boolean equals(Guid guid1, Guid guid2)
		{
			if(guid1 == null && guid2 == null)
			{
				return true;
			}
			else if((guid1 == null && guid2 != null))
			{
				return false;
			}
			else
			{
				return guid1.equals(guid2);
			}
		}
	}
}
