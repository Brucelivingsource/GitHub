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

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.Getback;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_CharVisualUpdate;
import Lsimulator.server.server.serverpackets.S_MapID;
import Lsimulator.server.server.serverpackets.S_OtherCharPacks;
import Lsimulator.server.server.serverpackets.S_OwnCharPack;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.serverpackets.S_Weather;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來重新的封包
 */
public class C_Restart extends ClientBasePacket {

	private static final String C_RESTART = "[C] C_Restart";

	public C_Restart(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}

		pc.stopPcDeleteTimer();

		int[] loc;

		if (pc.getHellTime() > 0) {
			loc = new int[3];
			loc[0] = 32701;
			loc[1] = 32777;
			loc[2] = 666;
		}
		else {
			loc = Getback.GetBack_Location(pc, true);
		}

		pc.removeAllKnownObjects();
		pc.broadcastPacket(new S_RemoveObject(pc));

		pc.setCurrentHp(pc.getLevel());
		pc.set_food(40);
		pc.setDead(false);
		pc.setStatus(0);
		LsimulatorWorld.getInstance().moveVisibleObject(pc, loc[2]);
		pc.setX(loc[0]);
		pc.setY(loc[1]);
		pc.setMap((short) loc[2]);
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
		pc.broadcastPacket(new S_OtherCharPacks(pc));
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.sendPackets(new S_CharVisualUpdate(pc));
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.sendPackets(new S_Weather(LsimulatorWorld.getInstance().getWeather()));
		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}
	}

	@Override
	public String getType() {
		return C_RESTART;
	}
}