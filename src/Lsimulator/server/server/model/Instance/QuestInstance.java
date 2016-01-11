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

import java.util.Timer;
import java.util.TimerTask;

import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorQuest;
import Lsimulator.server.server.serverpackets.S_ChangeHeading;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class QuestInstance extends NpcInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public QuestInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onNpcAI() {
		int npcId = getNpcTemplate().get_npcId();
		if (isAiRunning()) {
			return;
		}
		if ((npcId == 71075) || (npcId == 70957) || (npcId == 81209)) {
			return;
		}
		else {
			setActived(false);
			startAI();
		}
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
	public void onTalkAction(PcInstance pc) {
		int pcX = pc.getX();
		int pcY = pc.getY();
		int npcX = getX();
		int npcY = getY();

		if ((pcX == npcX) && (pcY < npcY)) {
			setHeading(0);
		}
		else if ((pcX > npcX) && (pcY < npcY)) {
			setHeading(1);
		}
		else if ((pcX > npcX) && (pcY == npcY)) {
			setHeading(2);
		}
		else if ((pcX > npcX) && (pcY > npcY)) {
			setHeading(3);
		}
		else if ((pcX == npcX) && (pcY > npcY)) {
			setHeading(4);
		}
		else if ((pcX < npcX) && (pcY > npcY)) {
			setHeading(5);
		}
		else if ((pcX < npcX) && (pcY == npcY)) {
			setHeading(6);
		}
		else if ((pcX < npcX) && (pcY < npcY)) {
			setHeading(7);
		}
		broadcastPacket(new S_ChangeHeading(this));

		int npcId = getNpcTemplate().get_npcId();
		if ((npcId == 71092) || (npcId == 71093)) { // 調査員
			if (pc.isKnight() && (pc.getQuest().get_step(3) == 4)) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "searcherk1"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "searcherk4"));
			}
		}
		else if (npcId == 71094) { // 安迪亞
			if (pc.isDarkelf() && (pc.getQuest().get_step(4) == 2)) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "endiaq1"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "endiaq4"));
			}
		}
		else if (npcId == 71062) { // 卡米特
			if (pc.getQuest().get_step(LsimulatorQuest.QUEST_CADMUS) == 2) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "kamit1b"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "kamit1"));
			}
		}
		else if (npcId == 71075) { // 疲憊的蜥蜴人戰士
			if (pc.getQuest().get_step(LsimulatorQuest.QUEST_LIZARD) == 1) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "llizard1b"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "llizard1a"));
			}
		}
		else if ((npcId == 70957) || (npcId == 81209)) { // ロイ
			if (pc.getQuest().get_step(LsimulatorQuest.QUEST_ROI) != 1) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "roi1"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "roi2"));
			}
		}
		else if (npcId == 81350) { // 迪嘉勒廷的女間諜
			if (pc.isElf() && (pc.getQuest().get_step(4) == 3)) {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "dspy2"));
			}
			else {
				pc.sendPackets(new S_NPCTalkReturn(getId(), "dspy1"));
			}
		}

		synchronized (this) {
			if (_monitor != null) {
				_monitor.cancel();
			}
			setRest(true);
			_monitor = new RestMonitor();
			_restTimer.schedule(_monitor, REST_MILLISEC);
		}
	}

	@Override
	public void onFinalAction(PcInstance pc, String action) {
		if (action.equalsIgnoreCase("start")) {
			int npcId = getNpcTemplate().get_npcId();
			if (((npcId == 71092) || (npcId == 71093)) && pc.isKnight() && (pc.getQuest().get_step(3) == 4)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(71093);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}
			else if ((npcId == 71094) && pc.isDarkelf() && (pc.getQuest().get_step(4) == 2)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(71094);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}
			else if ((npcId == 71062) && (pc.getQuest().get_step(LsimulatorQuest.QUEST_CADMUS) == 2)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(71062);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}
			else if ((npcId == 71075) && (pc.getQuest().get_step(LsimulatorQuest.QUEST_LIZARD) == 1)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(71075);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}
			else if ((npcId == 70957) || (npcId == 81209)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(70957);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}
			else if ((npcId == 81350) && (pc.getQuest().get_step(4) == 3)) {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(81350);
				new FollowerInstance(l1npc, this, pc);
				pc.sendPackets(new S_NPCTalkReturn(getId(), ""));
			}

		}
	}

	private static final long REST_MILLISEC = 10000;

	private static final Timer _restTimer = new Timer(true);

	private RestMonitor _monitor;

	public class RestMonitor extends TimerTask {
		@Override
		public void run() {
			setRest(false);
		}
	}

}
