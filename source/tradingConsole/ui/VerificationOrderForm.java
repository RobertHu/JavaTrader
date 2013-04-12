package tradingConsole.ui;

import java.math.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import framework.threading.*;
import framework.threading.Scheduler.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.language.*;
import com.jidesoft.dialog.StandardDialog;

public class VerificationOrderForm extends JDialog implements Scheduler.ISchedulerCallback //Frame
{
	//private static Scheduler _scheduler;
	private SchedulerEntry _quoteDelayScheduleEntry;
	private SchedulerEntry _priceValidTimeScheduleEntry;
	private SchedulerEntry _fillVerificationInfoScheduleEntry;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private VerificationOrderManager _verificationOrderManager;
	private Instrument _instrument;
	private OrderType _orderType;
	private OperateType _operateType;
	/*
		public VerificationOrderForm()
		{
		 super();
		try
		 {
		  jbInit();

		  Rectangle rectangle = Functions.getRectangleByDimension(this.getSize());
		  this.setBounds(rectangle);
		  this.setIconImage(TradingConsole.get_TraderImage());
		 }
		 catch (Throwable exception)
		 {
		  exception.printStackTrace();
		 }
		}
	*/
	private VerificationOrderForm(JDialog parent, String title, boolean modal)
	{
		super(parent, title, modal);
		try
		{
			jbInit();
			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public VerificationOrderForm(JDialog parent, String title, boolean modal,
								 TradingConsole tradingConsole, SettingsManager settingsManager,
								 Instrument instrument, HashMap<Guid, MakeOrderAccount> makeOrderAccounts,
		OrderType orderType, OperateType operateType, boolean isHasDeal, Order assigningOrder, DateTime goodTillMonth)
	{
		this(parent, title, modal, tradingConsole, settingsManager,
			 instrument, makeOrderAccounts,	orderType, operateType, isHasDeal, assigningOrder, goodTillMonth, false);

	}

	private HashMap<Guid, MakeOrderAccount> _makeOrderAccounts;
	public VerificationOrderForm(JDialog parent, String title, boolean modal,
								 TradingConsole tradingConsole, SettingsManager settingsManager,
								 Instrument instrument, HashMap<Guid, MakeOrderAccount> makeOrderAccounts,
		OrderType orderType, OperateType operateType, boolean isHasDeal, Order assigningOrder, DateTime goodTillMonth, boolean isMakeOcoOrder)
	{
		this(parent, title, modal, tradingConsole, settingsManager, instrument, makeOrderAccounts,
			 orderType, operateType, isHasDeal, assigningOrder, goodTillMonth, ExpireType.GTD, isMakeOcoOrder);

	}

	public VerificationOrderForm(JDialog parent, String title, boolean modal, TradingConsole tradingConsole, SettingsManager settingsManager,
								 Instrument instrument, HashMap<Guid, MakeOrderAccount> makeOrderAccounts, OrderType orderType,
		OperateType operateType, boolean isHasDeal, Order assigningOrder, DateTime goodTillMonth, ExpireType expireType, boolean isMakeOcoOrder)
	{
		this(parent, title, modal);
		this._makeOrderAccounts = makeOrderAccounts;

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;
		this._orderType = orderType;
		this._operateType = operateType;
		this._verificationOrderManager = VerificationOrderManager.create(tradingConsole, settingsManager, instrument, this._orderType, this._operateType,
			assigningOrder, goodTillMonth, expireType, isMakeOcoOrder, this);

		this.confirmButton.setEnabled(false);
		this.confirmButton.setText(Language.OrderPlacementbtnConfirm);
		this.cancelButton.setText(Language.OrderPlacementbtnCancel);
		this.timeRemainStaticText.setText(Language.TimeRemainPrompt);
		this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());

		this.timeRemainStaticText.setVisible(false);
		this.timeRemainValueStaticText.setVisible(false);
		this.instrumentDescriptionStaticText.setVisible(false);

		this.quoteStaticText.setText(Language.VerifyInstruction);

		//Assembly orders......
		String message = this._verificationOrderManager.setVerificationOrderAccounts(makeOrderAccounts, true);
		if (!StringHelper.isNullOrEmpty(message))
		{
			this.fillMessage(message);
			this.show();
			return;
		}

		/*String warning = this._verificationOrderManager.checkSpotSetPrice();
		if (!StringHelper.isNullOrEmpty(warning))
		{
			AskDialog askDialog = new AskDialog(this, true);
			askDialog.setMessage(warning);
			askDialog.pack();
			askDialog.setLocationRelativeTo(null);
			askDialog.setVisible(true);

			if(askDialog.getDialogResult() == StandardDialog.RESULT_CANCELLED) return;
		}*/
		//this.fillVerificationInfo();

		if (this._operateType.equals(OperateType.SingleSpotTrade)
			|| this._operateType.equals(OperateType.MultiSpotTrade)
			|| this._operateType.equals(OperateType.DirectLiq)) //OperateType.Method2SpotTrade
		{
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				int lifeTime = Math.max(settingsManager.get_SystemParameter().get_PlaceConfirmMinTime(), settingsManager.get_SystemParameter().get_ExceptionEnquiryOutTime());
				this.timeRemainValueStaticText.setText(Integer.toString(lifeTime));
			}
			else
			{
				int lifeTime = Math.max(settingsManager.get_SystemParameter().get_PlaceConfirmMinTime(), this._instrument.get_PriceValidTime());
				this.timeRemainValueStaticText.setText(Integer.toString(lifeTime));
			}

			BSStatus bsStatus = this._verificationOrderManager.getBSStatus();
			BigDecimal quoteLot = this._verificationOrderManager.getQuoteLot();

			//if (bsStatus.equals(BSStatus.HasBuyOnly) || bsStatus.equals(BSStatus.HasSellOnly))
			{
				//record ui????????????
			}
			//isHasDeal????????
			if (!isHasDeal && MakeOrder.isQuote(this._instrument, quoteLot))
			{
				if(this.getParent() instanceof LiquidationOrderForm)
				{
					((LiquidationOrderForm)this.getParent()).StopTimeout();
				}
				/*
				if (this._instrument.get_PriceValidTime() <= 0)
				{
					this.dispose();
					return;
				}
				*/
				this._verificationOrderManager.setSetPriceToNull();
				this.verificationTextArea.setMessage("");

				this.quoteStaticText.setText(Language.GettingQuotesPrompt);
				this._settingsManager.getMakeOrderWindow(instrument).set_QuoteWindow(this);
				this.quote(quoteLot, bsStatus);

				//this.setVisible(true);
			}
			else
			{
				if (this._instrument.get_PriceValidTime() <= 0)
				{
					this.confirm();
					//this.dispose();
					return;
				}
				else
				{
					if(this.getParent() instanceof LiquidationOrderForm)
					{
						( (LiquidationOrderForm)this.getParent()).StopTimeout();
					}

					MakeOrder.setLastQuotationIsUsing(this._instrument, true);
					//this.confirmButton.setEnabled(true);
					this.priceValidTimeSchedulerStart();
					//this.setVisible(true);

					//TradingConsole.traceSource.trace(TraceType.Error, "Parameter.nonQuoteVerificationUiDelay =" + Parameter.nonQuoteVerificationUiDelay);
					if (isHasDeal || Parameter.nonQuoteVerificationUiDelay <= 0)
					{
						this.quoteStaticText.setText(Language.VerifyInstruction);
						this.fillVerificationInfo();
						this.confirmButton.setEnabled(true);
					}
					else
					{
						this.quoteStaticText.setText(Language.GettingQuotesPrompt);
						this.fillVerificationInfoScheculerStart();
						this.confirmButton.setEnabled(false);
					}
				}
			}
		}
		else
		{
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				this.confirm();
				//this.dispose();
				return;
			}
			else
			{
				this.confirmButton.setEnabled(true);
				//this.setVisible(true);

				this.fillVerificationInfo();
			}
		}

		this.setNotifyIsAcceptWindow(this);

		this.show();
	}

	private void fillMessage(String message)
	{
		this.verificationTextArea.setMessage(message);
		this.verificationTextArea.setFont(GridFont.verification);
	}

	private void fillVerificationInfo()
	{
		if(this._verificationOrderManager.isSpot())
		{
			this._instrument.freezeRefreshQuotation();
		}
		this._verificationOrderManager.freezePriceForPlacing();
		this.fillMessage(this._verificationOrderManager.getVerificationInfo());
	}

	private void setQuoteWindow(Window window)
	{
		this._settingsManager.setQuoteWindow(this._instrument, window);
	}

	private void setNotifyIsAcceptWindow(Window window)
	{
		this._settingsManager.setNotifyIsAcceptWindow(this._instrument, window);
	}

	public void dispose()
	{
		try
		{
			this.quoteDelayTimeSchedulerStop();
			this.priceValidTimeSchedulerStop();
			this.fillVerificationInfoScheculerStop();
			//this._verificationOrderManager.unbind();

			this.setQuoteWindow(null);
			this.setNotifyIsAcceptWindow(null);
			MakeOrder.setLastQuotationIsUsing(this._instrument, false);
		}
		catch(Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
		finally
		{
			this._instrument.unfreezeRefreshQuotation();
			if(this.getParent() instanceof MakeOrderForm) ((MakeOrderForm)this.getParent()).refreshPrice();
			super.dispose();
		}
	}

	private void setIsQuoting(boolean isQuoting)
	{
		this._settingsManager.getMakeOrderWindow(this._instrument).set_IsQuoting(isQuoting);
	}

	//Scheduler.SchedulerCallback.ActionsetDealStatus
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		SwingUtilities.invokeLater(new SwingSafelyAction(this, schedulerEntry));
	}

	private static class SwingSafelyAction implements Runnable
	{
		private VerificationOrderForm _owner;
		private Scheduler.SchedulerEntry _schedulerEntry;

		public SwingSafelyAction(VerificationOrderForm owner, Scheduler.SchedulerEntry schedulerEntry)
		{
			this._owner = owner;
			this._schedulerEntry = schedulerEntry;
		}

		public void run()
		{
			if (this._schedulerEntry.get_IsRemoved())
			{
				return;
			}
			if (this._schedulerEntry.get_ActionArgs().equals("PriceValidTime"))
			{
				int timeRemain = Integer.parseInt(this._owner.timeRemainValueStaticText.getText());
				timeRemain -= 1;
				this._owner.timeRemainValueStaticText.setText(Integer.toString(timeRemain));
				if (timeRemain <= 0)
				{
					this._owner._isCanceled = true;
					this._owner.resetParentData(false);
					this._owner.dispose();
				}
			}
			else if (this._schedulerEntry.get_ActionArgs().equals("QuoteDelay"))
			{
				this._owner.setQuoteWindow(null);
				MakeOrder.setLastQuotationIsUsing(this._owner._instrument, false);

				if (!this._owner._settingsManager.getMakeOrderWindow(this._owner._instrument).get_IsQuoting())
				{
					return;
				}
				this._owner.setIsQuoting(false);
				this._owner.quoteStaticText.setText(Language.QuoteDelayTimeout);
				this._owner.cancelButton.setText(Language.btnCancelCaption);
			}
			else if (this._schedulerEntry.get_ActionArgs().equals("FillVerificationInfo"))
			{
				this._owner.fillVerificationInfoScheculerStop();
				this._owner.quoteStaticText.setText(Language.QuotePriceArrivedPrompt);
				this._owner.confirmButton.setEnabled(true);
				this._owner.getLastPrice();
				this._owner._verificationOrderManager.setSetPrice();
				this._owner.fillVerificationInfo();
				//MakeOrder.setLastQuotationIsUsing(this._instrument, false);
			}
			else if (this._schedulerEntry.get_ActionArgs().equals("Close"))
			{
				this._owner._isCanceled = true;
				this._owner.resetParentData(false);
				this._owner.dispose();
			}
		}
	}

	private void getLastPrice()
	{
		Container parent = this.getParent();
		if (parent instanceof SpotTradeOrderForm)
		{
			( (SpotTradeOrderForm)parent).getLastPrice();
		}
		else if (parent instanceof MultiDQOrderForm)
		{
			( (MultiDQOrderForm)parent).getLastPrice();
		}
		else if (parent instanceof LiquidationOrderForm)
		{
			( (LiquidationOrderForm)parent).getLastPrice();
		}
	}
	private void quoteDelaySchedulerStart()
	{
		//TimeSpan timeSpan = TimeSpan.fromSeconds(Parameter.quoteDelay);
		TimeSpan timeSpan = TimeSpan.fromSeconds(this._settingsManager.get_SystemParameter().get_EnquiryOutTime());
		Object actionArgs = "QuoteDelay";
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			this._quoteDelayScheduleEntry = TradingConsoleServer.scheduler.add(this, actionArgs, beginTime, timeSpan, ThreadPriority.normal);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private void fillVerificationInfoScheculerStart()
	{
		TimeSpan timeSpan = TimeSpan.fromSeconds(Parameter.nonQuoteVerificationUiDelay);
		Object actionArgs = "FillVerificationInfo";
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			this._fillVerificationInfoScheduleEntry = TradingConsoleServer.scheduler.add(this, actionArgs, beginTime, DateTime.maxValue, timeSpan, true);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private synchronized void fillVerificationInfoScheculerStop()
	{
		if (this._fillVerificationInfoScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._fillVerificationInfoScheduleEntry);
			this._fillVerificationInfoScheduleEntry = null;
		}
	}

	private void priceValidTimeSchedulerStart()
	{
		TimeSpan timeSpan = TimeSpan.fromSeconds(1);
		Object actionArgs = "PriceValidTime";
		DateTime beginTime = TradingConsoleServer.appTime();
		try
		{
			this._priceValidTimeScheduleEntry = TradingConsoleServer.scheduler.add(this, actionArgs, beginTime, DateTime.maxValue, timeSpan, true);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private synchronized void quoteDelayTimeSchedulerStop()
	{
		if (this._quoteDelayScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._quoteDelayScheduleEntry);
			this._quoteDelayScheduleEntry = null;
		}
	}

	private synchronized void priceValidTimeSchedulerStop()
	{
		if (this._priceValidTimeScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._priceValidTimeScheduleEntry);
			this._priceValidTimeScheduleEntry = null;
		}
	}

	//call service
	private void quote(BigDecimal quoteLot, BSStatus bsStatus)
	{
		this.setQuoteWindow(this);
		MakeOrder.setLastQuotationIsUsing(this._instrument, true);
		this.setIsQuoting(true);

		this._tradingConsole.get_TradingConsoleServer().quote(this._instrument.get_Id(), quoteLot, bsStatus.value());
		this._instrument.set_InQuoting(true);
		this.quoteDelaySchedulerStart();
	}

	public void quoteArrived()
	{
		this.setQuoteWindow(null);

		if (!this._settingsManager.getMakeOrderWindow(this._instrument).get_IsQuoting())
		{
			return;
		}
		this.quoteDelayTimeSchedulerStop();
		this.setIsQuoting(false);
		this.quoteStaticText.setText(Language.QuotePriceArrivedPrompt);
		this.fillSetPriceByQuote();
		this.confirmButton.setEnabled(true);

		this.priceValidTimeSchedulerStart();
	}

	private void fillSetPriceByQuote()
	{
		this._verificationOrderManager.setSetPrice();
		//this._verificationOrderManager.updateSetPrice();
		BigDecimal quoteLot = this._verificationOrderManager.getQuoteLot();
		BigDecimal answerLot = this._instrument.get_LastQuotation().get_AnswerLot();
		try
		{
			if (answerLot != null && quoteLot.compareTo(answerLot) != 0)
			{
				this._verificationOrderManager.answer(answerLot);
				this._verificationOrderManager.clear();

				this._verificationOrderManager.setVerificationOrderAccounts(this._makeOrderAccounts, true);
			}
		}
		finally
		{
			this._instrument.get_LastQuotation().clearAnswerLot();
		}
		this.fillVerificationInfo();
		this.verificationTextArea.doLayout();
	}

	private void confirm()
	{
		try
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Confirm_Begin---------------------------------------------------------------------------");
			DateTime dt = DateTime.get_Now();
			TimeSpan timeSpan;
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: priceValidTimeSchedulerStop()_Begin: " + dt.toString("HH:mm:ss.fff"));
			this.priceValidTimeSchedulerStop();
			timeSpan = DateTime.substract(DateTime.get_Now(), dt);
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: priceValidTimeSchedulerStop()_End: " + timeSpan.toString());
			dt = DateTime.get_Now();
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: fillVerificationInfoScheculerStop()_Begin: " + dt.toString("HH:mm:ss.fff"));
			this.fillVerificationInfoScheculerStop();
			timeSpan = DateTime.substract(DateTime.get_Now(), dt);
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: fillVerificationInfoScheculerStop()_End: " + timeSpan.toString());
			dt = DateTime.get_Now();
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: confirm()_Begin: " + dt.toString("HH:mm:ss.fff"));
			Object[] result = this._verificationOrderManager.confirm();
			this._verificationOrderManager.unfreezePriceForPlacing();
			timeSpan = DateTime.substract(DateTime.get_Now(), dt);
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: confirm()_End: " + timeSpan.toString());

			if (! (Boolean)result[0])
			{
				//???????????????
				//AlertDialogForm.showDialog((Frame)this.getParent(),null,true,((Boolean)result[0]).booleanValue(),result[1].toString());
				this.quoteStaticText.setText(result[1].toString());
			}
		}
		finally
		{
			DateTime dt = DateTime.get_Now();
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: CloseWindow()_Begin: " + dt.toString("HH:mm:ss.fff"));
			this.resetParentData(true);
			TimeSpan timeSpan = DateTime.substract(DateTime.get_Now(), dt);
			TradingConsole.traceSource.trace(TraceType.Information, "confirm: CloseWindow()_End: " + timeSpan.toString());

			TradingConsole.traceSource.trace(TraceType.Information, "Confirm_End---------------------------------------------------------------------------");
		}
	}

	private boolean _isCanceled = false;
	private void cancel()
	{
		this._isCanceled = true;
		this.resetParentData(false);
		this.dispose();
	}

	private void resetParentData(boolean isCloseParent)
	{
		Container parent = this.getParent();
		if (parent instanceof SpotTradeOrderForm)
		{
			if (isCloseParent)
			{
				( (SpotTradeOrderForm)parent).dispose2();
			}
			else
			{
				( (SpotTradeOrderForm)parent).resetData();
			}
		}
		else if (parent instanceof LiquidationOrderForm)
		{
			if (isCloseParent)
			{
				( (LiquidationOrderForm)parent).dispose2();
			}
			else
			{
				( (LiquidationOrderForm)parent).resetData();
			}
		}
		else if (parent instanceof MultiDQOrderForm)
		{
			if (isCloseParent)
			{
				( (MultiDQOrderForm)parent).dispose2();
			}
			else
			{
				( (MultiDQOrderForm)parent).resetData();
			}
		}
		else if (parent instanceof AssignOrderForm)
		{
			if (isCloseParent)
			{
				( (AssignOrderForm)parent).dispose2();
			}
			else
			{
				( (AssignOrderForm)parent).resetData();
			}
		}
		else if (parent instanceof LimitOrderFormDialog)
		{
			if (isCloseParent)
			{
				( (LimitOrderFormDialog)parent).dispose2();
			}
			else
			{
				( (LimitOrderFormDialog)parent).resetData();
			}
		}
		else if (parent instanceof MakeOrderForm)
		{
			if (isCloseParent)
			{
				( (MakeOrderForm)parent).dispose2();
			}
			else
			{
				( (MakeOrderForm)parent).resetData();
			}
		}
		//other........................
	}

	//when confirm DQ Order, check if accept price again..............
	private DateTime initTime = DateTime.get_Now();
	private SchedulerEntry closeEntry = null;
	public void notifyIsAcceptMakeSpotTradeOrderByPrice()
	{
		BSStatus bsStatus = this._verificationOrderManager.getBSStatus();
		boolean isBuy = (bsStatus == BSStatus.HasBuyOnly) ? true : false;
		boolean isAcceptMakeSpotTradeOrderByPrice = this._instrument.isAcceptMakeSpotTradeOrderByPrice(isBuy);
		if (!isAcceptMakeSpotTradeOrderByPrice)
		{
			int placeConfirMinTime
				= this._tradingConsole.get_SettingsManager().get_SystemParameter().get_PlaceConfirmMinTime();
			if(placeConfirMinTime > 0)
			{
				int elapsedTime = DateTime.get_Now().substract(initTime).get_Seconds();
				int remainTime = placeConfirMinTime - elapsedTime;
				if(this.closeEntry != null && remainTime > 0)
				{
					TimeSpan timeSpan = TimeSpan.fromSeconds(remainTime);
					DateTime beginTime = TradingConsoleServer.appTime();
					try
					{
						this.closeEntry = TradingConsoleServer.scheduler.add(this, "Close", beginTime, DateTime.maxValue, timeSpan, true);
					}
					catch (Throwable exception)
					{
						TradingConsole.traceSource.trace(TraceType.Error, exception);
					}

					return;
				}
			}

			this.resetParentData(false);
			if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_ShowPriceChangedBeforeCloseConfirmWindow())
			{
				AlertDialogForm.showDialog(this, Language.notify, true, Language.PriceChangedSincePlace);
			}
			this.dispose();
		}
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new VerificationOrderUi_this_windowAdapter(this));

		this.setSize(320, 300);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.verificationOrderFormTitle);
		this.setBackground(FormBackColor.verificationOrderForm);
		this.setVisible(false);

		instrumentDescriptionStaticText.setText("BGP");
		timeRemainStaticText.setAlignment(2);
		timeRemainStaticText.setText("Time Remain");
		timeRemainValueStaticText.setAlignment(2);
		timeRemainValueStaticText.setForeground(Color.red);
		timeRemainValueStaticText.setText("10");
		confirmButton.setText("Confirm");
		cancelButton.
			setText("Cancel");
		cancelButton.addActionListener(new VerificationOrderForm_cancelButton_actionAdapter(this));
		quoteStaticText.setText("");
		confirmButton.addActionListener(new VerificationOrderForm_confirmButton_actionAdapter(this));
		this.getContentPane().add(cancelButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 10, 10), 20, 0));
		this.getContentPane().add(confirmButton, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 2), 20, 0));
		this.getContentPane().add(verificationTextArea, new GridBagConstraints(0, 3, 5, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 10, 10, 10), 0, 0));
		this.getContentPane().add(instrumentDescriptionStaticText, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 5, 2), 40, 0));
		this.getContentPane().add(timeRemainStaticText, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 2, 5, 0), 40, 0));
		this.getContentPane().add(timeRemainValueStaticText, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 10), 20, 0));
		this.getContentPane().add(quoteStaticText, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 5, 10), 0, 20));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this._isCanceled = true;
		this.dispose();
	}

	PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	PVStaticText2 timeRemainStaticText = new PVStaticText2();
	PVStaticText2 timeRemainValueStaticText = new PVStaticText2();
	PVButton2 confirmButton = new PVButton2();
	PVButton2 cancelButton = new PVButton2();
	PVStaticText2 quoteStaticText = new PVStaticText2();
	MultiTextArea verificationTextArea = new MultiTextArea();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public void cancelButton_actionPerformed(ActionEvent e)
	{
		this.cancel();
	}

	public void confirmButton_actionPerformed(ActionEvent e)
	{
		this.confirm();
	}

	public boolean isCanceled()
	{
		return this._isCanceled;
	}

	class VerificationOrderUi_this_windowAdapter extends WindowAdapter
	{
		private VerificationOrderForm adaptee;
		VerificationOrderUi_this_windowAdapter(VerificationOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class VerificationOrderForm_confirmButton_actionAdapter implements ActionListener
{
	private VerificationOrderForm adaptee;
	VerificationOrderForm_confirmButton_actionAdapter(VerificationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.confirmButton_actionPerformed(e);
	}
}

class VerificationOrderForm_cancelButton_actionAdapter implements ActionListener
{
	private VerificationOrderForm adaptee;
	VerificationOrderForm_cancelButton_actionAdapter(VerificationOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.cancelButton_actionPerformed(e);
	}
}
