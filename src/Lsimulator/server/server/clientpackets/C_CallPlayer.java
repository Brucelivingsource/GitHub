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
import Lsimulator.server.server.model.LsimulatorLocation;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 收到由客戶端傳來呼叫玩家的封包
 */
public class C_CallPlayer extends ClientBasePacket {

	private static final String C_CALL = "[C] C_Call";

	public C_CallPlayer(byte[] decrypt, ClientThread client) {
		super(decrypt);
		
		LsimulatorPcInstance pc = client.getActiveChar();
		if ((pc == null) || (!pc.isGm())) {
			return;
		}

		String name = readS();
		if (name.isEmpty()) {
			return;
		}

		LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(name);

		if (target == null) {
			return;
		}

		LsimulatorLocation loc = LsimulatorLocation.randomLocation(target.getLocation(), 1, 2, false);
		LsimulatorTeleport.teleport(pc, loc.getX(), loc.getY(), target.getMapId(), pc.getHeading(), false);
	}

	@Override
	public String getType() {
		return C_CALL;
	}
}
