package tradingConsole.bursa;

import framework.xml.XmlNode;
import tradingConsole.settings.SettingsManager;
import framework.xml.XmlAttributeCollection;
import framework.Guid;
import tradingConsole.enumDefine.bursa.InstrumentGroupState;
import java.util.HashMap;
import tradingConsole.enumDefine.bursa.InstrumentState;
import tradingConsole.Instrument;
import tradingConsole.enumDefine.bursa.ActionCode;
import framework.DateTime;
import tradingConsole.enumDefine.bursa.InstrumentTimeState;
import framework.time.AppTime;
import tradingConsole.enumDefine.OrderType;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import tradingConsole.TradingConsoleServer;
import framework.time.ITimeSource;

public class InstrumentStateManager
{
	private IInstrumentStateListener _instrumentStateListener;
	private SettingsManager _settingsManager;
	private ITimeSource _timeSource;

	private HashMap<Guid, InstrumentState> _instrumentStates = new HashMap<Guid, InstrumentState>();
	private HashMap<Guid, InstrumentGroupState> _instrumentGroupStates = new HashMap<Guid, InstrumentGroupState>();
	private HashMap<Guid, DateTime> _instrumentDeferrOpenTime = new HashMap<Guid, DateTime>();
	private HashMap<Guid, TimeTable> _instrumentTimeTables = new HashMap<Guid, TimeTable>();
	private TimeTable _defaultTimeTable;
	private Timer _timer;
	private HashMap<Guid, InstrumentTimeState> _instrumentLastTimeStates = new HashMap<Guid, InstrumentTimeState>();

	public InstrumentStateManager(IInstrumentStateListener instrumentStateListener, SettingsManager settingsManager,
								  TimeTable defaultTimeTable, ITimeSource timeSource)
	{
		this._instrumentStateListener = instrumentStateListener;
		this._settingsManager = settingsManager;
		this._defaultTimeTable = defaultTimeTable;
		this._timeSource = timeSource;
	}

	public void start()
	{
		this._timer = new Timer(1000, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				checkTimeStateAndNotifyIfChanged();
			}
		});
		this._timer.setRepeats(true);
		this._timer.start();
	}

	public void stop()
	{
		if(this._timer != null) this._timer.stop();
	}

	private void checkTimeStateAndNotifyIfChanged()
	{
		for(Instrument instrument : this._settingsManager.getInstruments().values())
		{
			if(instrument.isFromBursa())
			{
				InstrumentTimeState lastTimeState = null;
				if (this._instrumentLastTimeStates.containsKey(instrument.get_Id()))
				{
					lastTimeState = this._instrumentLastTimeStates.get(instrument.get_Id());
				}

				InstrumentTimeState newTimeState = this.getInstrumentTimeState(instrument.get_Id());
				this._instrumentLastTimeStates.put(instrument.get_Id(), newTimeState);
				if (newTimeState != lastTimeState && this._instrumentStateListener != null)
				{
					this._instrumentStateListener.instrumentTimeStateChanged(instrument, newTimeState);
				}
			}
		}
	}

	public void addInstrumentGroupStateChangeCommand(XmlNode instrumentGroupStateChangeCommand)
	{
		XmlAttributeCollection attributes = instrumentGroupStateChangeCommand.get_Attributes();
		Guid groupId = new Guid(attributes.get_ItemOf("GroupId").get_Value());
		InstrumentGroupState state = framework.lang.Enum.valueOf(InstrumentGroupState.class, Integer.parseInt(attributes.get_ItemOf("State").get_Value()));
		this._instrumentGroupStates.put(groupId, state);
	}

	public void addInstrumentStateChangeCommand(XmlNode instrumentStateChangeCommand)
	{
		XmlAttributeCollection attributes = instrumentStateChangeCommand.get_Attributes();
		Guid instrumentId = new Guid(attributes.get_ItemOf("InstrumentId").get_Value());
		InstrumentState instrumentState = InstrumentState.None;

		ActionCode actionCode = framework.lang.Enum.valueOf(ActionCode.class, Integer.parseInt(attributes.get_ItemOf("ActionCode").get_Value()));
		if(actionCode == ActionCode.DeferrOpen)
		{
			DateTime deferrOpenTime = DateTime.valueOf(attributes.get_ItemOf("DeferrOpenTime").get_Value());
			this._instrumentDeferrOpenTime.put(instrumentId, deferrOpenTime);
		}
		else if(actionCode == ActionCode.CancelDeferrOpen)
		{
			this._instrumentDeferrOpenTime.remove(instrumentId);
		}
		else if(actionCode == ActionCode.Activate)
		{
			instrumentState = InstrumentState.Authorised;
		}
		else if(actionCode == ActionCode.AuthorizeOrderEntry)
		{
			instrumentState = InstrumentState.AuthorizeOrderEntry;
		}
		else if(actionCode == ActionCode.Expiry)
		{
			instrumentState = InstrumentState.Forbid;
		}
		else if(actionCode == ActionCode.ForbidOrderEntry)
		{
			instrumentState = InstrumentState.ForbidOrderEntry;
		}
		else if(actionCode == ActionCode.Freeze)
		{
			instrumentState = InstrumentState.Forbid;
		}
		else if(actionCode == ActionCode.Open)
		{
			instrumentState = InstrumentState.Open;
		}
		else if(actionCode == ActionCode.Reserve)
		{
			instrumentState = InstrumentState.Reserved;
		}
		else if(actionCode == ActionCode.Suspend)
		{
			instrumentState = InstrumentState.Suspended;
		}
		else if(actionCode == ActionCode.Thaw)
		{
			instrumentState = InstrumentState.Authorised;
		}
		else if(actionCode == ActionCode.Trading)
		{
			instrumentState = InstrumentState.Open;
		}

		this._instrumentStates.put(instrumentId, instrumentState);
	}

	public InstrumentTimeState getInstrumentTimeState(Guid instrumentId)
	{
		TimeTable timeTable = this._defaultTimeTable;
		if(this._instrumentTimeTables.containsKey(instrumentId)) timeTable = this._instrumentTimeTables.get(instrumentId);

		DateTime deferrOpenTime = null;
		if(this._instrumentDeferrOpenTime.containsKey(instrumentId)) deferrOpenTime = this._instrumentDeferrOpenTime.get(instrumentId);

		DateTime now = TradingConsoleServer.appTime();
		if(deferrOpenTime != null && now.before(deferrOpenTime)) return InstrumentTimeState.Closed;

		InstrumentTimeState instrumentTimeState = timeTable.get_ForenoonSession().getTimeState(now);
		if(instrumentTimeState == InstrumentTimeState.End) instrumentTimeState = timeTable.get_AfternoonSession().getTimeState(now);
		return instrumentTimeState;
	}

	public boolean canPlaceOrder(Guid instrumentId, OrderType orderType)
	{
		InstrumentState instrumentState = this.getInstrumentState(instrumentId);
		InstrumentTimeState instrumentTimeState = this.getInstrumentTimeState(instrumentId);
		InstrumentGroupState instrumentGroupState = this.getInstrumentGroupState(instrumentId);
		return instrumentTimeState.get_CanPlaceOrder(orderType) && instrumentState.get_CanPlaceOrder() && instrumentGroupState.get_CanPlaceOrder(orderType);
	}

	public boolean canModifyOrder(Guid instrumentId)
	{
		InstrumentState instrumentState = this.getInstrumentState(instrumentId);
		InstrumentTimeState instrumentTimeState = this.getInstrumentTimeState(instrumentId);
		InstrumentGroupState instrumentGroupState = this.getInstrumentGroupState(instrumentId);
		return instrumentTimeState.get_CanModifyOrder() && instrumentState.get_CanModifyOrder() && instrumentGroupState.get_CanModifyOrder();
	}

	public boolean canCancelOrder(Guid instrumentId)
	{
		InstrumentState instrumentState = this.getInstrumentState(instrumentId);
		InstrumentTimeState instrumentTimeState = this.getInstrumentTimeState(instrumentId);
		InstrumentGroupState instrumentGroupState = this.getInstrumentGroupState(instrumentId);
		return instrumentTimeState.get_CanCancelOrder() && instrumentState.get_CanCancelOrder() && instrumentGroupState.get_CanCancelOrder();
	}

	private InstrumentState getInstrumentState(Guid instrumentId)
	{
		InstrumentState instrumentState = InstrumentState.None;
		if (this._instrumentStates.containsKey(instrumentId))
		{
			instrumentState = this._instrumentStates.get(instrumentId);
		}
		return instrumentState;
	}

	private InstrumentGroupState getInstrumentGroupState(Guid instrumentId)
	{
		InstrumentGroupState instrumentGroupState = InstrumentGroupState.ContinuousTrading;
		Guid groupId = this._settingsManager.getInstrument(instrumentId).get_GroupId();
		if (this._instrumentGroupStates.containsKey(groupId))
		{
			instrumentGroupState = this._instrumentGroupStates.get(groupId);
		}
		return instrumentGroupState;
	}
}
