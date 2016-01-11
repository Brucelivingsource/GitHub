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
package Lsimulator.server.server.model.Instance;

import java.util.List;

import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.serverpackets.S_ChangeHeading;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.CalcExp;
import Lsimulator.server.server.utils.collections.Lists;

public class ScarecrowInstance extends NpcInstance {

	private static final long serialVersionUID = 1L;

	public ScarecrowInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(PcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(PcInstance pc, int skillId) {
		LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
		if (attack.calcHit()) {
			attack.calcDamage();
			attack.calcStaffOfMana();
			attack.addPcPoisonAttack(pc, this);
			attack.addChaserAttack();
		}
		attack.action();
		attack.commit();
	}

	@Override
	public void receiveDamage(LsimulatorCharacter attacker, int damage) {
		if ((getCurrentHp() > 0) && !isDead()) {
			if (damage > 0) {
				if (getHeading() < 7) {
					setHeading(getHeading() + 1);
				}
				else {
					setHeading(0);
				}
				broadcastPacket(new S_ChangeHeading(this));

				if ((attacker instanceof PcInstance)) {
					PcInstance pc = (PcInstance) attacker;
					pc.setPetTarget(this);

					if (pc.getLevel() < 5) {
						List<LsimulatorCharacter> targetList = Lists.newList();
						targetList.add(pc);
						List<Integer> hateList = Lists.newList();
						hateList.add(1);
						CalcExp.calcExp(pc, getId(), targetList, hateList, getExp());
					}
				}
			}
		}
	}

	@Override
	public void onTalkAction(PcInstance l1pcinstance) {

	}

	public void onFinalAction() {

	}

	public void doFinalAction() {
	}
}
