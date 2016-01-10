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

import Lsimulator.server.Config;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.datatables.CharacterConfigTable;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_RequestDoors

/**
 * 收到由客戶端傳來角色設定的封包
 */
public class C_CharcterConfig extends ClientBasePacket {

	private static final String C_CHARCTER_CONFIG = "[C] C_CharcterConfig";

	public C_CharcterConfig(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			LsimulatorPcInstance pc = client.getActiveChar();
			if (pc == null) {
				return;
			}
			int length = readD() - 3;
			byte data[] = readByte();
			int count = CharacterConfigTable.getInstance().countCharacterConfig(pc.getId());
			if (count == 0) {
				CharacterConfigTable.getInstance().storeCharacterConfig(pc.getId(), length, data);
			}
			else {
				CharacterConfigTable.getInstance().updateCharacterConfig(pc.getId(), length, data);
			}
		}
	}

	@Override
	public String getType() {
		return C_CHARCTER_CONFIG;
	}
}
