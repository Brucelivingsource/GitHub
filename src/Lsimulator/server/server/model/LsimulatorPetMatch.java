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

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.templates.LsimulatorPet;

public class LsimulatorPetMatch {
	public static final int STATUS_NONE = 0;

	public static final int STATUS_READY1 = 1;

	public static final int STATUS_READY2 = 2;

	public static final int STATUS_PLAYING = 3;

	public static final int MAX_PET_MATCH = 1;

	private static final short[] PET_MATCH_MAPID =
	{ 5125, 5131, 5132, 5133, 5134 };

	private String[] _pc1Name = new String[MAX_PET_MATCH];

	private String[] _pc2Name = new String[MAX_PET_MATCH];

	private PetInstance[] _pet1 = new PetInstance[MAX_PET_MATCH];

	private PetInstance[] _pet2 = new PetInstance[MAX_PET_MATCH];

	private static LsimulatorPetMatch _instance;

	public static LsimulatorPetMatch getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorPetMatch();
		}
		return _instance;
	}

	public int setPetMatchPc(int petMatchNo, PcInstance pc, PetInstance pet) {
		int status = getPetMatchStatus(petMatchNo);
		if (status == STATUS_NONE) {
			_pc1Name[petMatchNo] = pc.getName();
			_pet1[petMatchNo] = pet;
			return STATUS_READY1;
		}
		else if (status == STATUS_READY1) {
			_pc2Name[petMatchNo] = pc.getName();
			_pet2[petMatchNo] = pet;
			return STATUS_PLAYING;
		}
		else if (status == STATUS_READY2) {
			_pc1Name[petMatchNo] = pc.getName();
			_pet1[petMatchNo] = pet;
			return STATUS_PLAYING;
		}
		return STATUS_NONE;
	}

	private synchronized int getPetMatchStatus(int petMatchNo) {
		PcInstance pc1 = null;
		if (_pc1Name[petMatchNo] != null) {
			pc1 = LsimulatorWorld.getInstance().getPlayer(_pc1Name[petMatchNo]);
		}
		PcInstance pc2 = null;
		if (_pc2Name[petMatchNo] != null) {
			pc2 = LsimulatorWorld.getInstance().getPlayer(_pc2Name[petMatchNo]);
		}

		if ((pc1 == null) && (pc2 == null)) {
			return STATUS_NONE;
		}
		if ((pc1 == null) && (pc2 != null)) {
			if (pc2.getMapId() == PET_MATCH_MAPID[petMatchNo]) {
				return STATUS_READY2;
			}
			else {
				_pc2Name[petMatchNo] = null;
				_pet2[petMatchNo] = null;
				return STATUS_NONE;
			}
		}
		if ((pc1 != null) && (pc2 == null)) {
			if (pc1.getMapId() == PET_MATCH_MAPID[petMatchNo]) {
				return STATUS_READY1;
			}
			else {
				_pc1Name[petMatchNo] = null;
				_pet1[petMatchNo] = null;
				return STATUS_NONE;
			}
		}

		// PCが試合場に2人いる場合
		if ((pc1.getMapId() == PET_MATCH_MAPID[petMatchNo]) && (pc2.getMapId() == PET_MATCH_MAPID[petMatchNo])) {
			return STATUS_PLAYING;
		}

		// PCが試合場に1人いる場合
		if (pc1.getMapId() == PET_MATCH_MAPID[petMatchNo]) {
			_pc2Name[petMatchNo] = null;
			_pet2[petMatchNo] = null;
			return STATUS_READY1;
		}
		if (pc2.getMapId() == PET_MATCH_MAPID[petMatchNo]) {
			_pc1Name[petMatchNo] = null;
			_pet1[petMatchNo] = null;
			return STATUS_READY2;
		}
		return STATUS_NONE;
	}

	private int decidePetMatchNo() {
		// 相手が待機中の試合を探す
		for (int i = 0; i < MAX_PET_MATCH; i++) {
			int status = getPetMatchStatus(i);
			if ((status == STATUS_READY1) || (status == STATUS_READY2)) {
				return i;
			}
		}
		// 待機中の試合がなければ空いている試合を探す
		for (int i = 0; i < MAX_PET_MATCH; i++) {
			int status = getPetMatchStatus(i);
			if (status == STATUS_NONE) {
				return i;
			}
		}
		return -1;
	}

	public synchronized boolean enterPetMatch(PcInstance pc, int amuletId) {
		int petMatchNo = decidePetMatchNo();
		if (petMatchNo == -1) {
			return false;
		}

		PetInstance pet = withdrawPet(pc, amuletId);
		LsimulatorTeleport.teleport(pc, 32799, 32868, PET_MATCH_MAPID[petMatchNo], 0, true);

		LsimulatorPetMatchReadyTimer timer = new LsimulatorPetMatchReadyTimer(petMatchNo, pc, pet);
		timer.begin();
		return true;
	}

	private PetInstance withdrawPet(PcInstance pc, int amuletId) {
		LsimulatorPet l1pet = PetTable.getInstance().getTemplate(amuletId);
		if (l1pet == null) {
			return null;
		}
		LsimulatorNpc npcTemp = NpcTable.getInstance().getTemplate(l1pet.get_npcid());
		PetInstance pet = new PetInstance(npcTemp, pc, l1pet);
		pet.setPetcost(6);
		return pet;
	}

	public void startPetMatch(int petMatchNo) {
		_pet1[petMatchNo].setCurrentPetStatus(1);
		_pet1[petMatchNo].setTarget(_pet2[petMatchNo]);

		_pet2[petMatchNo].setCurrentPetStatus(1);
		_pet2[petMatchNo].setTarget(_pet1[petMatchNo]);

		LsimulatorPetMatchTimer timer = new LsimulatorPetMatchTimer(_pet1[petMatchNo], _pet2[petMatchNo], petMatchNo);
		timer.begin();
	}

	public void endPetMatch(int petMatchNo, int winNo) {
		PcInstance pc1 = LsimulatorWorld.getInstance().getPlayer(_pc1Name[petMatchNo]);
		PcInstance pc2 = LsimulatorWorld.getInstance().getPlayer(_pc2Name[petMatchNo]);
		if (winNo == 1) {
			giveMedal(pc1, petMatchNo, true);
			giveMedal(pc2, petMatchNo, false);
		}
		else if (winNo == 2) {
			giveMedal(pc1, petMatchNo, false);
			giveMedal(pc2, petMatchNo, true);
		}
		else if (winNo == 3) { // 引き分け
			giveMedal(pc1, petMatchNo, false);
			giveMedal(pc2, petMatchNo, false);
		}
		qiutPetMatch(petMatchNo);
	}

	private void giveMedal(PcInstance pc, int petMatchNo, boolean isWin) {
		if (pc == null) {
			return;
		}
		if (pc.getMapId() != PET_MATCH_MAPID[petMatchNo]) {
			return;
		}
		if (isWin) {
			pc.sendPackets(new S_ServerMessage(1166, pc.getName())); // %0%sペットマッチで勝利を収めました。
			ItemInstance item = ItemTable.getInstance().createItem(41309);
			int count = 3;
			if (item != null) {
				if (pc.getInventory().checkAddItem(item, count) == LsimulatorInventory.OK) {
					item.setCount(count);
					pc.getInventory().storeItem(item);
					pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
				}
			}
		}
		else {
			ItemInstance item = ItemTable.getInstance().createItem(41309);
			int count = 1;
			if (item != null) {
				if (pc.getInventory().checkAddItem(item, count) == LsimulatorInventory.OK) {
					item.setCount(count);
					pc.getInventory().storeItem(item);
					pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
				}
			}
		}
	}

	private void qiutPetMatch(int petMatchNo) {
		PcInstance pc1 = LsimulatorWorld.getInstance().getPlayer(_pc1Name[petMatchNo]);
		if ((pc1 != null) && (pc1.getMapId() == PET_MATCH_MAPID[petMatchNo])) {
			for (Object object : pc1.getPetList().values().toArray()) {
				if (object instanceof PetInstance) {
					PetInstance pet = (PetInstance) object;
					pet.dropItem();
					pc1.getPetList().remove(pet.getId());
					pet.deleteMe();
				}
			}
			LsimulatorTeleport.teleport(pc1, 32630, 32744, (short) 4, 4, true);
		}
		_pc1Name[petMatchNo] = null;
		_pet1[petMatchNo] = null;

		PcInstance pc2 = LsimulatorWorld.getInstance().getPlayer(_pc2Name[petMatchNo]);
		if ((pc2 != null) && (pc2.getMapId() == PET_MATCH_MAPID[petMatchNo])) {
			for (Object object : pc2.getPetList().values().toArray()) {
				if (object instanceof PetInstance) {
					PetInstance pet = (PetInstance) object;
					pet.dropItem();
					pc2.getPetList().remove(pet.getId());
					pet.deleteMe();
				}
			}
			LsimulatorTeleport.teleport(pc2, 32630, 32744, (short) 4, 4, true);
		}
		_pc2Name[petMatchNo] = null;
		_pet2[petMatchNo] = null;
	}

	public class LsimulatorPetMatchReadyTimer extends TimerTask {
		private Logger _log = Logger.getLogger(LsimulatorPetMatchReadyTimer.class.getName());

		private final int _petMatchNo;

		private final PcInstance _pc;

		private final PetInstance _pet;

		public LsimulatorPetMatchReadyTimer(int petMatchNo, PcInstance pc, PetInstance pet) {
			_petMatchNo = petMatchNo;
			_pc = pc;
			_pet = pet;
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 3000);
		}

		@Override
		public void run() {
			try {
				for (;;) {
					Thread.sleep(1000);
					if ((_pc == null) || (_pet == null)) {
						cancel();
						return;
					}

					if (_pc.isTeleport()) {
						continue;
					}
					if (LsimulatorPetMatch.getInstance().setPetMatchPc(_petMatchNo, _pc, _pet) == LsimulatorPetMatch.STATUS_PLAYING) {
						LsimulatorPetMatch.getInstance().startPetMatch(_petMatchNo);
					}
					cancel();
					return;
				}
			}
			catch (Throwable e) {
				_log.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}

	}

	public class LsimulatorPetMatchTimer extends TimerTask {
		private Logger _log = Logger.getLogger(LsimulatorPetMatchTimer.class.getName());

		private final PetInstance _pet1;

		private final PetInstance _pet2;

		private final int _petMatchNo;

		private int _counter = 0;

		public LsimulatorPetMatchTimer(PetInstance pet1, PetInstance pet2, int petMatchNo) {
			_pet1 = pet1;
			_pet2 = pet2;
			_petMatchNo = petMatchNo;
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 0);
		}

		@Override
		public void run() {
			try {
				for (;;) {
					Thread.sleep(3000);
					_counter++;
					if ((_pet1 == null) || (_pet2 == null)) {
						cancel();
						return;
					}

					if (_pet1.isDead() || _pet2.isDead()) {
						int winner = 0;
						if (!_pet1.isDead() && _pet2.isDead()) {
							winner = 1;
						}
						else if (_pet1.isDead() && !_pet2.isDead()) {
							winner = 2;
						}
						else {
							winner = 3;
						}
						LsimulatorPetMatch.getInstance().endPetMatch(_petMatchNo, winner);
						cancel();
						return;
					}

					if (_counter == 100) { // 5分経っても終わらない場合は引き分け
						LsimulatorPetMatch.getInstance().endPetMatch(_petMatchNo, 3);
						cancel();
						return;
					}
				}
			}
			catch (Throwable e) {
				_log.log(Level.WARNING, e.getLocalizedMessage(), e);
			}
		}

	}

}
