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
package Lsimulator.server.server.clientpackets;

import java.util.Timer;
import java.util.TimerTask;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.datatables.HouseTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.DoorInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.templates.LsimulatorHouse;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_Door

/**
 * 處理收到由客戶端傳來開關門的封包
 */
public class C_Door extends ClientBasePacket {

	private static final String C_DOOR = "[C] C_Door";

	public C_Door(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		
		PcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		readH();
		readH();
		int objectId = readD();
		
		DoorInstance door = (DoorInstance) LsimulatorWorld.getInstance().findObject(objectId);
		if (door == null) {
			return;
		}

		if (((door.getDoorId() >= 5001) && (door.getDoorId() <= 5010))) { // 水晶洞窟
			return;
		}
		else if (door.getDoorId() == 6006) { // 冒險洞穴二樓
			if (door.getOpenStatus() == ActionCodes.ACTION_Open) {
				return;
			}
			if (pc.getInventory().consumeItem(40163, 1)) { // 黃金鑰匙
				door.open();
				CloseTimer closetimer = new CloseTimer(door);
				closetimer.begin();
			}
		}
		else if (door.getDoorId() == 6007) { // 冒險洞穴二樓
			if (door.getOpenStatus() == ActionCodes.ACTION_Open) {
				return;
			}
			if (pc.getInventory().consumeItem(40313, 1)) { // 銀鑰匙
				door.open();
				CloseTimer closetimer = new CloseTimer(door);
				closetimer.begin();
			}
		}
		else if (!isExistKeeper(pc, door.getKeeperId())) {
			if (door.getOpenStatus() == ActionCodes.ACTION_Open) {
				door.close();
			}
			else if (door.getOpenStatus() == ActionCodes.ACTION_Close) {
				door.open();
			}
		}
	}

	private boolean isExistKeeper(PcInstance pc, int keeperId) {
		if (keeperId == 0) {
			return false;
		}

		LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
		if (clan != null) {
			int houseId = clan.getHouseId();
			if (houseId != 0) {
				LsimulatorHouse house = HouseTable.getInstance().getHouseTable(houseId);
				if (keeperId == house.getKeeperId()) {
					return false;
				}
			}
		}
		return true;
	}

	public class CloseTimer extends TimerTask {

		private DoorInstance _door;

		public CloseTimer(DoorInstance door) {
			_door = door;
		}

		@Override
		public void run() {
			if (_door.getOpenStatus() == ActionCodes.ACTION_Open) {
				_door.close();
			}
		}

		public void begin() {
			Timer timer = new Timer();
			timer.schedule(this, 5 * 1000);
		}
	}

	@Override
	public String getType() {
		return C_DOOR;
	}
}
