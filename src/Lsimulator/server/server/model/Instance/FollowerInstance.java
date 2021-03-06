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

import java.lang.reflect.Constructor;

import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorQuest;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.serverpackets.S_FollowerPack;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class FollowerInstance extends NpcInstance {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean noTarget() {
		for (LsimulatorObject object : LsimulatorWorld.getInstance().getVisibleObjects(this)) {
			if (object instanceof NpcInstance) {
				NpcInstance npc = (NpcInstance) object;
				if ((npc.getNpcTemplate().get_npcId() == 70740 // ディカルデンソルジャー
						)
						&& (getNpcTemplate().get_npcId() == 71093)) { // 調査員
					setParalyzed(true);
					PcInstance pc = (PcInstance) _master;
					if (!pc.getInventory().checkItem(40593)) {
						createNewItem(pc, 40593, 1);
					}
					deleteMe();
					return true;
				}
				else if ((npc.getNpcTemplate().get_npcId() == 70811 // 萊拉
						)
						&& (getNpcTemplate().get_npcId() == 71094)) { // 安迪亞
					setParalyzed(true);
					PcInstance pc = (PcInstance) _master;
					if (!pc.getInventory().checkItem(40582)
							&& !pc.getInventory().checkItem(40583)) { // 身上無安迪亞之袋、安迪亞之信
						createNewItem(pc, 40582, 1);
					}
					deleteMe();
					return true;
				}
				else if ((npc.getNpcTemplate().get_npcId() == 71061 // カドモス
						)
						&& (getNpcTemplate().get_npcId() == 71062)) { // カミット
					if (getLocation().getTileLineDistance(_master.getLocation()) < 3) {
						PcInstance pc = (PcInstance) _master;
						if (((pc.getX() >= 32448) && (pc.getX() <= 32452)) // カドモス周辺座標
								&& ((pc.getY() >= 33048) && (pc.getY() <= 33052)) && (pc.getMapId() == 440)) {
							setParalyzed(true);
							if (!pc.getInventory().checkItem(40711)) {
								createNewItem(pc, 40711, 1);
								pc.getQuest().set_step(LsimulatorQuest.QUEST_CADMUS, 3);
							}
							deleteMe();
							return true;
						}
					}
				}
				else if ((npc.getNpcTemplate().get_npcId() == 71074 // リザードマンの長老
						)
						&& (getNpcTemplate().get_npcId() == 71075)) {
					// 疲れ果てたリザードマンファイター
					if (getLocation().getTileLineDistance(_master.getLocation()) < 3) {
						PcInstance pc = (PcInstance) _master;
						if (((pc.getX() >= 32731) && (pc.getX() <= 32735)) // リザードマン長老周辺座標
								&& ((pc.getY() >= 32854) && (pc.getY() <= 32858)) && (pc.getMapId() == 480)) {
							setParalyzed(true);
							if (!pc.getInventory().checkItem(40633)) {
								createNewItem(pc, 40633, 1);
								pc.getQuest().set_step(LsimulatorQuest.QUEST_LIZARD, 2);
							}
							deleteMe();
							return true;
						}
					}
				}
				else if ((npc.getNpcTemplate().get_npcId() == 70964 // バッシュ
						)
						&& (getNpcTemplate().get_npcId() == 70957)) { // ロイ
					if (getLocation().getTileLineDistance(_master.getLocation()) < 3) {
						PcInstance pc = (PcInstance) _master;
						if (((pc.getX() >= 32917) && (pc.getX() <= 32921)) // バッシュ周辺座標
								&& ((pc.getY() >= 32974) && (pc.getY() <= 32978)) && (pc.getMapId() == 410)) {
							setParalyzed(true);
							createNewItem(pc, 41003, 1);
							pc.getQuest().set_step(LsimulatorQuest.QUEST_ROI, 0);
							deleteMe();
							return true;
						}
					}
				}
				else if ((npc.getNpcTemplate().get_npcId() == 71114)
					&& (getNpcTemplate().get_npcId() == 81350)) { // 迪嘉勒廷的女間諜
					if (getLocation().getTileLineDistance(_master.getLocation()) < 15) {
						PcInstance pc = (PcInstance) _master;
						if (((pc.getX() >= 32542) && (pc.getX() <= 32585))
						&& ((pc.getY() >= 32656) && (pc.getY() <= 32698)) && (pc.getMapId() == 400)) {
							setParalyzed(true);
							createNewItem(pc, 49163, 1);
							pc.getQuest().set_step(4, 4);
							deleteMe();
							return true;
						}
					}
				}
			}
		}

		if (_master.isDead() || (getLocation().getTileLineDistance(_master.getLocation()) > 10)) {
			setParalyzed(true);
			spawn(getNpcTemplate().get_npcId(), getX(), getY(), getHeading(), getMapId());
			deleteMe();
			return true;
		}
		else if ((_master != null) && (_master.getMapId() == getMapId())) {
			if (getLocation().getTileLineDistance(_master.getLocation()) > 2) {
				setDirectionMove(moveDirection(_master.getX(), _master.getY()));
				setSleepTime(calcSleepTime(getPassispeed(), MOVE_SPEED));
			}
		}
		return false;
	}

	public FollowerInstance(LsimulatorNpc template, NpcInstance target, LsimulatorCharacter master) {
		super(template);

		_master = master;
		setId(IdFactory.getInstance().nextId());

		setMaster(master);
		setX(target.getX());
		setY(target.getY());
		setMap(target.getMapId());
		setHeading(target.getHeading());
		setLightSize(target.getLightSize());

		target.setParalyzed(true);
		target.setDead(true);
		target.deleteMe();

		LsimulatorWorld.getInstance().storeObject(this);
		LsimulatorWorld.getInstance().addVisibleObject(this);
		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(this)) {
			onPerceive(pc);
		}

		startAI();
		master.addFollower(this);
	}

	@Override
	public synchronized void deleteMe() {
		_master.getFollowerList().remove(getId());
		getMap().setPassable(getLocation(), true);
		super.deleteMe();
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
	public void onTalkAction(PcInstance player) {
		if (isDead()) {
			return;
		}
		if (getNpcTemplate().get_npcId() == 71093) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "searcherk2"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "searcherk4"));
			}
		}
		else if (getNpcTemplate().get_npcId() == 71094) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "endiaq2"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "endiaq4"));
			}
		}
		else if (getNpcTemplate().get_npcId() == 71062) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "kamit2"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "kamit1"));
			}
		}
		else if (getNpcTemplate().get_npcId() == 71075) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "llizard2"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "llizard1a"));
			}
		}
		else if (getNpcTemplate().get_npcId() == 70957) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "roi2"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "roi2"));
			}
		}
		else if (getNpcTemplate().get_npcId() == 81350) {
			if (_master.equals(player)) {
				player.sendPackets(new S_NPCTalkReturn(getId(), "dspy3"));
			}
			else {
				player.sendPackets(new S_NPCTalkReturn(getId(), "dspy3"));
			}
		}

	}

	@Override
	public void onPerceive(PcInstance perceivedFrom) {
		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_FollowerPack(this, perceivedFrom));
	}

	private void createNewItem(PcInstance pc, int item_id, int count) {
		ItemInstance item = ItemTable.getInstance().createItem(item_id);
		item.setCount(count);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == LsimulatorInventory.OK) {
				pc.getInventory().storeItem(item);
			}
			else {
				LsimulatorWorld.getInstance().getInventory(pc.getX(), pc.getY(), pc.getMapId()).storeItem(item);
			}
			pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
		}
	}

	public void spawn(int npcId, int X, int Y, int H, short Map) {
		LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(npcId);
		if (l1npc != null) {
			NpcInstance mob = null;
			try {
				String implementationName = l1npc.getImpl();
				Constructor<?> _constructor = Class.forName(
						(new StringBuilder()).append("Lsimulator.server.server.model.Instance.").append(implementationName).append("Instance").toString())
						.getConstructors()[0];
				mob = (NpcInstance) _constructor.newInstance(new Object[]
				{ l1npc });
				mob.setId(IdFactory.getInstance().nextId());
				mob.setX(X);
				mob.setY(Y);
				mob.setHomeX(X);
				mob.setHomeY(Y);
				mob.setMap(Map);
				mob.setHeading(H);
				LsimulatorWorld.getInstance().storeObject(mob);
				LsimulatorWorld.getInstance().addVisibleObject(mob);
				LsimulatorObject object = LsimulatorWorld.getInstance().findObject(mob.getId());
				QuestInstance newnpc = (QuestInstance) object;
				newnpc.onNpcAI();
				newnpc.turnOnOffLight();
				newnpc.startChat(NpcInstance.CHAT_TIMING_APPEARANCE); // チャット開始
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
