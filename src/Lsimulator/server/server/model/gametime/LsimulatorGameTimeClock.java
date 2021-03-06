/**
 *                            License
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF THIS  
 * CREATIVE COMMONS PUBLIC LICENSE ("CCPL" OR "LICENSE"). 
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.  
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR  
 * COPYRIGHT LAW IS PROHIBITED.
 * 
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND  
 * AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE. TO THE EXTENT THIS LICENSE  
 * MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU THE RIGHTS CONTAINED 
 * HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 * 
 */
package Lsimulator.server.server.model.gametime;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorGameTimeClock {
	private static Logger _log = Logger.getLogger(LsimulatorGameTimeClock.class.getName());

	private static LsimulatorGameTimeClock _instance;

	private volatile LsimulatorGameTime _currentTime = LsimulatorGameTime.fromSystemCurrentTime();

	private LsimulatorGameTime _previousTime = null;

	private List<LsimulatorGameTimeListener> _listeners = Lists.newConcurrentList();

	private class TimeUpdater implements Runnable {
		@Override
		public void run() {
			while (true) {
				_previousTime = _currentTime;
				_currentTime = LsimulatorGameTime.fromSystemCurrentTime();
				notifyChanged();

				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private boolean isFieldChanged(int field) {
		return _previousTime.get(field) != _currentTime.get(field);
	}

	private void notifyChanged() {
		if (isFieldChanged(Calendar.MONTH)) {
			for (LsimulatorGameTimeListener listener : _listeners) {
				listener.onMonthChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.DAY_OF_MONTH)) {
			for (LsimulatorGameTimeListener listener : _listeners) {
				listener.onDayChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.HOUR_OF_DAY)) {
			for (LsimulatorGameTimeListener listener : _listeners) {
				listener.onHourChanged(_currentTime);
			}
		}
		if (isFieldChanged(Calendar.MINUTE)) {
			for (LsimulatorGameTimeListener listener : _listeners) {
				listener.onMinuteChanged(_currentTime);
			}
		}
	}

	private LsimulatorGameTimeClock() {
		GeneralThreadPool.getInstance().execute(new TimeUpdater());
	}

	public static void init() {
		_instance = new LsimulatorGameTimeClock();
	}

	public static LsimulatorGameTimeClock getInstance() {
		return _instance;
	}

	public LsimulatorGameTime currentTime() {
		return _currentTime;
	}

	public void addListener(LsimulatorGameTimeListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(LsimulatorGameTimeListener listener) {
		_listeners.remove(listener);
	}
}
