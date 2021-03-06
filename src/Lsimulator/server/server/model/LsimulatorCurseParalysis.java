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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CURSE_PARALYZED;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CURSE_PARALYZING;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_Paralysis;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

/*
 * LsimulatorParalysisPoisonと被るコードが多い。特にタイマー。何とか共通化したいが難しい。
 */
public class LsimulatorCurseParalysis extends LsimulatorParalysis {
	private final LsimulatorCharacter _target;

	private final int _delay;

	private final int _time;

	private Thread _timer;

	private class ParalysisDelayTimer extends Thread {
		@Override
		public void run() {
			_target.setSkillEffect(STATUS_CURSE_PARALYZING, 0);

			try {
				Thread.sleep(_delay); // 麻痺するまでの猶予時間を待つ。
			}
			catch (InterruptedException e) {
				_target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
				return;
			}

			if (_target instanceof PcInstance) {
				PcInstance player = (PcInstance) _target;
				if (!player.isDead()) {
					player.sendPackets(new S_Paralysis(1, true)); // 麻痺状態にする
				}
			}
			_target.setParalyzed(true);
			_timer = new ParalysisTimer();
			GeneralThreadPool.getInstance().execute(_timer); // 麻痺タイマー開始
			if (isInterrupted()) {
				_timer.interrupt();
			}
		}
	}

	private class ParalysisTimer extends Thread {
		@Override
		public void run() {
			_target.killSkillEffectTimer(STATUS_CURSE_PARALYZING);
			_target.setSkillEffect(STATUS_CURSE_PARALYZED, 0);
			try {
				Thread.sleep(_time);
			}
			catch (InterruptedException e) {}

			_target.killSkillEffectTimer(STATUS_CURSE_PARALYZED);
			if (_target instanceof PcInstance) {
				PcInstance player = (PcInstance) _target;
				if (!player.isDead()) {
					player.sendPackets(new S_Paralysis(1, false)); // 麻痺状態を解除する
				}
			}
			_target.setParalyzed(false);
			cure(); // 解呪処理
		}
	}

	private LsimulatorCurseParalysis(LsimulatorCharacter cha, int delay, int time) {
		_target = cha;
		_delay = delay;
		_time = time;

		curse();
	}

	private void curse() {
		if (_target instanceof PcInstance) {
			PcInstance player = (PcInstance) _target;
			player.sendPackets(new S_ServerMessage(212));
		}

		_target.setPoisonEffect(2);

		_timer = new ParalysisDelayTimer();
		GeneralThreadPool.getInstance().execute(_timer);
	}

	public static boolean curse(LsimulatorCharacter cha, int delay, int time) {
		if (!((cha instanceof PcInstance) || (cha instanceof MonsterInstance))) {
			return false;
		}
		if (cha.hasSkillEffect(STATUS_CURSE_PARALYZING) || cha.hasSkillEffect(STATUS_CURSE_PARALYZED)) {
			return false; // 既に麻痺している
		}

		cha.setParalaysis(new LsimulatorCurseParalysis(cha, delay, time));
		return true;
	}

	@Override
	public int getEffectId() {
		return 2;
	}

	@Override
	public void cure() {
		if (_timer != null) {
			_timer.interrupt(); // 麻痺タイマー解除
		}

		_target.setPoisonEffect(0);
		_target.setParalaysis(null);
	}
}
