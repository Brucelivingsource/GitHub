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

import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_SkillSound; //TODO HPR効果
import Lsimulator.server.server.templates.LsimulatorMagicDoll;

public class HpRegenerationByDoll extends TimerTask {
	private static Logger _log = Logger.getLogger(HpRegenerationByDoll.class
			.getName());

	private final PcInstance _pc;

	public HpRegenerationByDoll(PcInstance pc) {
		_pc = pc;
	}

	@Override
	public void run() {
		try {
			if (_pc.isDead()) {
				return;
			}
			regenHp();
		} catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void regenHp() {
		int newHp = _pc.getCurrentHp() + LsimulatorMagicDoll.getHpByDoll(_pc);
		if (newHp < 0) {
			newHp = 0;
		}
		_pc.sendPackets(new S_SkillSound(_pc.getId(), 744));
		_pc.broadcastPacket(new S_SkillSound(_pc.getId(), 744));
		_pc.setCurrentHp(newHp);
	}
}