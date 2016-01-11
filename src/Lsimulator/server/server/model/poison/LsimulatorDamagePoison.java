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
package Lsimulator.server.server.model.poison;

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_POISON;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.PcInstance;

public class LsimulatorDamagePoison extends LsimulatorPoison {
	private Thread _timer;

	private final LsimulatorCharacter _attacker;

	private final LsimulatorCharacter _target;

	private final int _damageSpan;

	private final int _damage;

	private LsimulatorDamagePoison(LsimulatorCharacter attacker, LsimulatorCharacter cha, int damageSpan, int damage) {
		_attacker = attacker;
		_target = cha;
		_damageSpan = damageSpan;
		_damage = damage;

		doInfection();
	}

	private class NormalPoisonTimer extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(_damageSpan);
				}
				catch (InterruptedException e) {
					break;
				}

				if (!_target.hasSkillEffect(STATUS_POISON)) {
					break;
				}
				if (_target instanceof PcInstance) {
					PcInstance player = (PcInstance) _target;
					player.receiveDamage(_attacker, _damage, false);
					if (player.isDead()) { // 死亡したら解毒処理
						break;
					}
				}
				else if (_target instanceof MonsterInstance) {
					MonsterInstance mob = (MonsterInstance) _target;
					mob.receiveDamage(_attacker, _damage);
					if (mob.isDead()) { // 死亡しても解毒しない
						return;
					}
				}
			}
			cure(); // 解毒処理
		}
	}

	boolean isDamageTarget(LsimulatorCharacter cha) {
		return (cha instanceof PcInstance) || (cha instanceof MonsterInstance);
	}

	private void doInfection() {
		_target.setSkillEffect(STATUS_POISON, 30000);
		_target.setPoisonEffect(1);

		if (isDamageTarget(_target)) {
			_timer = new NormalPoisonTimer();
			GeneralThreadPool.getInstance().execute(_timer); // 通常毒タイマー開始
		}
	}

	public static boolean doInfection(LsimulatorCharacter attacker, LsimulatorCharacter cha, int damageSpan, int damage) {
		if (!isValidTarget(cha)) {
			return false;
		}

		cha.setPoison(new LsimulatorDamagePoison(attacker, cha, damageSpan, damage));
		return true;
	}

	@Override
	public int getEffectId() {
		return 1;
	}

	@Override
	public void cure() {
		if (_timer != null) {
			_timer.interrupt(); // 毒タイマー解除
		}

		_target.setPoisonEffect(0);
		_target.killSkillEffectTimer(STATUS_POISON);
		_target.setPoison(null);
	}
}
