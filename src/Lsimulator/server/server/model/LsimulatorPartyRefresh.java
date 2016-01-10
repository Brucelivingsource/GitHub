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
package Lsimulator.server.server.model;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_Party;

public class LsimulatorPartyRefresh extends TimerTask {
	private static Logger _log = Logger.getLogger(LsimulatorPartyRefresh.class
			.getName());

	private final LsimulatorPcInstance _pc;

	public LsimulatorPartyRefresh(LsimulatorPcInstance pc) {
		_pc = pc;
	}

	/**
	 * 3.3C 更新隊伍封包
	 */
	public void fresh() {
		_pc.sendPackets(new S_Party(110, _pc));
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead() || _pc.getParty() == null) {
				_pc.stopRefreshParty();
				return;
			}
			fresh();
		} catch (Throwable e) {
			_pc.stopRefreshParty();
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}
}