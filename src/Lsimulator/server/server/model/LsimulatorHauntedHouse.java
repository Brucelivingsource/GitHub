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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.CANCELLATION;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import Lsimulator.server.server.model.Instance.LsimulatorDoorInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.utils.collections.Lists;

public class LsimulatorHauntedHouse {
	public static final int STATUS_NONE = 0;

	public static final int STATUS_READY = 1;

	public static final int STATUS_PLAYING = 2;

	private final List<LsimulatorPcInstance> _members = Lists.newList();

	private int _hauntedHouseStatus = STATUS_NONE;

	private int _winnersCount = 0;

	private int _goalCount = 0;

	private static LsimulatorHauntedHouse _instance;

	public static LsimulatorHauntedHouse getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorHauntedHouse();
		}
		return _instance;
	}

	private void readyHauntedHouse() {
		setHauntedHouseStatus(STATUS_READY);
		LsimulatorHauntedHouseReadyTimer hhrTimer = new LsimulatorHauntedHouseReadyTimer();
		hhrTimer.begin();
	}

	private void startHauntedHouse() {
		setHauntedHouseStatus(STATUS_PLAYING);
		int membersCount = getMembersCount();
		if (membersCount <= 4) {
			setWinnersCount(1);
		}
		else if ((5 >= membersCount) && (membersCount <= 7)) {
			setWinnersCount(2);
		}
		else if ((8 >= membersCount) && (membersCount <= 10)) {
			setWinnersCount(3);
		}
		for (LsimulatorPcInstance pc : getMembersArray()) {
			LsimulatorSkillUse l1skilluse = new LsimulatorSkillUse();
			l1skilluse.handleCommands(pc, CANCELLATION, pc.getId(), pc.getX(), pc.getY(), null, 0, LsimulatorSkillUse.TYPE_LOGIN);
			LsimulatorPolyMorph.doPoly(pc, 6284, 300, LsimulatorPolyMorph.MORPH_BY_NPC);
		}

		for (LsimulatorObject object : LsimulatorWorld.getInstance().getObject()) {
			if (object instanceof LsimulatorDoorInstance) {
				LsimulatorDoorInstance door = (LsimulatorDoorInstance) object;
				if (door.getMapId() == 5140) {
					door.open();
				}
			}
		}
	}

	public void endHauntedHouse() {
		setHauntedHouseStatus(STATUS_NONE);
		setWinnersCount(0);
		setGoalCount(0);
		for (LsimulatorPcInstance pc : getMembersArray()) {
			if (pc.getMapId() == 5140) {
				LsimulatorSkillUse l1skilluse = new LsimulatorSkillUse();
				l1skilluse.handleCommands(pc, CANCELLATION, pc.getId(), pc.getX(), pc.getY(), null, 0, LsimulatorSkillUse.TYPE_LOGIN);
				LsimulatorTeleport.teleport(pc, 32624, 32813, (short) 4, 5, true);
			}
		}
		clearMembers();
		for (LsimulatorObject object : LsimulatorWorld.getInstance().getObject()) {
			if (object instanceof LsimulatorDoorInstance) {
				LsimulatorDoorInstance door = (LsimulatorDoorInstance) object;
				if (door.getMapId() == 5140) {
					door.close();
				}
			}
		}
	}

	public void removeRetiredMembers() {
		LsimulatorPcInstance[] temp = getMembersArray();
		for (LsimulatorPcInstance element : temp) {
			if (element.getMapId() != 5140) {
				removeMember(element);
			}
		}
	}

	public void sendMessage(int type, String msg) {
		for (LsimulatorPcInstance pc : getMembersArray()) {
			pc.sendPackets(new S_ServerMessage(type, msg));
		}
	}

	public void addMember(LsimulatorPcInstance pc) {
		if (!_members.contains(pc)) {
			_members.add(pc);
		}
		if ((getMembersCount() == 1) && (getHauntedHouseStatus() == STATUS_NONE)) {
			readyHauntedHouse();
		}
	}

	public void removeMember(LsimulatorPcInstance pc) {
		_members.remove(pc);
	}

	public void clearMembers() {
		_members.clear();
	}

	public boolean isMember(LsimulatorPcInstance pc) {
		return _members.contains(pc);
	}

	public LsimulatorPcInstance[] getMembersArray() {
		return _members.toArray(new LsimulatorPcInstance[_members.size()]);
	}

	public int getMembersCount() {
		return _members.size();
	}

	private void setHauntedHouseStatus(int i) {
		_hauntedHouseStatus = i;
	}

	public int getHauntedHouseStatus() {
		return _hauntedHouseStatus;
	}

	private void setWinnersCount(int i) {
		_winnersCount = i;
	}

	public int getWinnersCount() {
		return _winnersCount;
	}

	public void setGoalCount(int i) {
		_goalCount = i;
	}

	public int getGoalCount() {
		return _goalCount;
	}

	public class LsimulatorHauntedHouseReadyTimer extends TimerTask {

		public LsimulatorHauntedHouseReadyTimer() {
		}

		@Override
		public void run() {
			startHauntedHouse();
			LsimulatorHauntedHouseTimer hhTimer = new LsimulatorHauntedHouseTimer();
			hhTimer.begin();
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 90000); // 90秒くらい？
		}

	}

	public class LsimulatorHauntedHouseTimer extends TimerTask {

		public LsimulatorHauntedHouseTimer() {
		}

		@Override
		public void run() {
			endHauntedHouse();
			cancel();
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 300000); // 5分
		}

	}

}
