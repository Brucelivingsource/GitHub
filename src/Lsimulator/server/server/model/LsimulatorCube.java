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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ABSOLUTE_BARRIER;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EARTH_BIND;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FREEZING_BLIZZARD;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.FREEZING_BREATH;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ICE_LANCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_BALANCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_IGNITION_TO_ENEMY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_QUAKE_TO_ENEMY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CUBE_SHOCK_TO_ENEMY;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_FREEZE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_MR_REDUCTION_BY_CUBE_SHOCK;

import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.Instance.LsimulatorMonsterInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_Paralysis;

public class LsimulatorCube extends TimerTask {
	private static Logger _log = Logger.getLogger(LsimulatorCube.class.getName());

	private ScheduledFuture<?> _future = null;

	private int _timeCounter = 0;

	private final LsimulatorCharacter _effect;

	private final LsimulatorCharacter _cha;

	private final int _skillId;

	public LsimulatorCube(LsimulatorCharacter effect, LsimulatorCharacter cha, int skillId) {
		_effect = effect;
		_cha = cha;
		_skillId = skillId;
	}

	@Override
	public void run() {
		try {
			if (_cha.isDead()) {
				stop();
				return;
			}
			if (!_cha.hasSkillEffect(_skillId)) {
				stop();
				return;
			}
			_timeCounter++;
			giveEffect();
		}
		catch (Throwable e) {
			_log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void begin() {
		// 効果時間が8秒のため、4秒毎のスキルの場合処理時間を考慮すると実際には1回しか効果が現れない
		// よって開始時間を0.9秒後に設定しておく
		_future = GeneralThreadPool.getInstance().scheduleAtFixedRate(this, 900, 1000);
	}

	public void stop() {
		if (_future != null) {
			_future.cancel(false);
		}
	}

	public void giveEffect() {
		if (_skillId == STATUS_CUBE_IGNITION_TO_ENEMY) {
			if (_timeCounter % 4 != 0) {
				return;
			}
			if (_cha.hasSkillEffect(STATUS_FREEZE)) {
				return;
			}
			if (_cha.hasSkillEffect(ABSOLUTE_BARRIER)) {
				return;
			}
			if (_cha.hasSkillEffect(ICE_LANCE)) {
				return;
			}
			if (_cha.hasSkillEffect(FREEZING_BLIZZARD)) {
				return;
			}
			if (_cha.hasSkillEffect(FREEZING_BREATH)) {
				return;
			}
			if (_cha.hasSkillEffect(EARTH_BIND)) {
				return;
			}

			if (_cha instanceof LsimulatorPcInstance) {
				LsimulatorPcInstance pc = (LsimulatorPcInstance) _cha;
				pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
				pc.broadcastPacket(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Damage));
				pc.receiveDamage(_effect, 10, false);
			}
			else if (_cha instanceof LsimulatorMonsterInstance) {
				LsimulatorMonsterInstance mob = (LsimulatorMonsterInstance) _cha;
				mob.broadcastPacket(new S_DoActionGFX(mob.getId(), ActionCodes.ACTION_Damage));
				mob.receiveDamage(_effect, 10);
			}
		}
		else if (_skillId == STATUS_CUBE_QUAKE_TO_ENEMY) {
			if (_timeCounter % 4 != 0) {
				return;
			}
			if (_cha.hasSkillEffect(STATUS_FREEZE)) {
				return;
			}
			if (_cha.hasSkillEffect(ABSOLUTE_BARRIER)) {
				return;
			}
			if (_cha.hasSkillEffect(ICE_LANCE)) {
				return;
			}
			if (_cha.hasSkillEffect(FREEZING_BLIZZARD)) {
				return;
			}
			if (_cha.hasSkillEffect(FREEZING_BREATH)) {
				return;
			}
			if (_cha.hasSkillEffect(EARTH_BIND)) {
				return;
			}

			if (_cha instanceof LsimulatorPcInstance) {
				LsimulatorPcInstance pc = (LsimulatorPcInstance) _cha;
				pc.setSkillEffect(STATUS_FREEZE, 1000);
				pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_BIND, true));
			}
			else if (_cha instanceof LsimulatorMonsterInstance) {
				LsimulatorMonsterInstance mob = (LsimulatorMonsterInstance) _cha;
				mob.setSkillEffect(STATUS_FREEZE, 1000);
				mob.setParalyzed(true);
			}
		}
		else if (_skillId == STATUS_CUBE_SHOCK_TO_ENEMY) {
			// if (_timeCounter % 5 != 0) {
			// return;
			// }
			// _cha.addMr(-10);
			// if (_cha instanceof LsimulatorPcInstance) {
			// LsimulatorPcInstance pc = (LsimulatorPcInstance) _cha;
			// pc.sendPackets(new S_SPMR(pc));
			// }
			_cha.setSkillEffect(STATUS_MR_REDUCTION_BY_CUBE_SHOCK, 4000);
		}
		else if (_skillId == STATUS_CUBE_BALANCE) {
			if (_timeCounter % 4 == 0) {
				int newMp = _cha.getCurrentMp() + 5;
				if (newMp < 0) {
					newMp = 0;
				}
				_cha.setCurrentMp(newMp);
			}
			if (_timeCounter % 5 == 0) {
				if (_cha instanceof LsimulatorPcInstance) {
					LsimulatorPcInstance pc = (LsimulatorPcInstance) _cha;
					pc.receiveDamage(_effect, 25, false);
				}
				else if (_cha instanceof LsimulatorMonsterInstance) {
					LsimulatorMonsterInstance mob = (LsimulatorMonsterInstance) _cha;
					mob.receiveDamage(_effect, 25);
				}
			}
		}
	}

}
