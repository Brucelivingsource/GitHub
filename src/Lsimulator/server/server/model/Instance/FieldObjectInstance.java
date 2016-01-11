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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.CANCELLATION;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.LsimulatorHauntedHouse;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class FieldObjectInstance extends NpcInstance {

	private static final long serialVersionUID = 1L;

	public FieldObjectInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(PcInstance pc) {
		if (getNpcTemplate().get_npcId() == 81171) { // おばけ屋敷のゴールの炎
			if (LsimulatorHauntedHouse.getInstance().getHauntedHouseStatus() == LsimulatorHauntedHouse.STATUS_PLAYING) {
				int winnersCount = LsimulatorHauntedHouse.getInstance().getWinnersCount();
				int goalCount = LsimulatorHauntedHouse.getInstance().getGoalCount();
				if (winnersCount == goalCount + 1) {
					ItemInstance item = ItemTable.getInstance().createItem(49280); // 勇者のパンプキン袋(銅)
					int count = 1;
					if (item != null) {
						if (pc.getInventory().checkAddItem(item, count) == LsimulatorInventory.OK) {
							item.setCount(count);
							pc.getInventory().storeItem(item);
							pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
						}
					}
					LsimulatorHauntedHouse.getInstance().endHauntedHouse();
				}
				else if (winnersCount > goalCount + 1) {
					LsimulatorHauntedHouse.getInstance().setGoalCount(goalCount + 1);
					LsimulatorHauntedHouse.getInstance().removeMember(pc);
					ItemInstance item = null;
					if (winnersCount == 3) {
						if (goalCount == 1) {
							item = ItemTable.getInstance().createItem(49278); // 勇者のパンプキン袋(金)
						}
						else if (goalCount == 2) {
							item = ItemTable.getInstance().createItem(49279); // 勇者のパンプキン袋(銀)
						}
					}
					else if (winnersCount == 2) {
						item = ItemTable.getInstance().createItem(49279); // 勇者のパンプキン袋(銀)
					}
					int count = 1;
					if (item != null) {
						if (pc.getInventory().checkAddItem(item, count) == LsimulatorInventory.OK) {
							item.setCount(count);
							pc.getInventory().storeItem(item);
							pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
						}
					}
					LsimulatorSkillUse l1skilluse = new LsimulatorSkillUse();
					l1skilluse.handleCommands(pc, CANCELLATION, pc.getId(), pc.getX(), pc.getY(), null, 0, LsimulatorSkillUse.TYPE_LOGIN);
					LsimulatorTeleport.teleport(pc, 32624, 32813, (short) 4, 5, true);
				}
			}
		}
	}

	@Override
	public void deleteMe() {
		_destroyed = true;
		if (getInventory() != null) {
			getInventory().clearItems();
		}
		LsimulatorWorld.getInstance().removeVisibleObject(this);
		LsimulatorWorld.getInstance().removeObject(this);
		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			pc.removeKnownObject(this);
			pc.sendPackets(new S_RemoveObject(this));
		}
		removeAllKnownObjects();
	}
}
