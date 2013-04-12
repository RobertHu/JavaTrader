package tradingConsole.bursa;

import tradingConsole.Instrument;
import tradingConsole.enumDefine.bursa.InstrumentTimeState;

public interface IInstrumentStateListener
{
	void instrumentTimeStateChanged(Instrument instrument, InstrumentTimeState newState);
}
