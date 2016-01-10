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
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorPcInventory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來選擇清單的封包
 */
public class C_SelectList extends ClientBasePacket {

	private static final String C_SELECT_LIST = "[C] C_SelectList";

	public C_SelectList(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		
		LsimulatorPcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}
		
		// アイテム毎にリクエストが来る。
		int itemObjectId = readD();
		int npcObjectId = readD();
		

		if (npcObjectId != 0) { // 武器的修理
			LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(npcObjectId);
			if (obj != null) {
				if (obj instanceof LsimulatorNpcInstance) {
					LsimulatorNpcInstance npc = (LsimulatorNpcInstance) obj;
					int difflocx = Math.abs(pc.getX() - npc.getX());
					int difflocy = Math.abs(pc.getY() - npc.getY());
					// 3格以上的距離視為無效請求
					if ((difflocx > 3) || (difflocy > 3)) {
						return;
					}
				}
			}

			LsimulatorPcInventory pcInventory = pc.getInventory();
			LsimulatorItemInstance item = pcInventory.getItem(itemObjectId);
			int cost = item.get_durability() * 200;
			if (!pc.getInventory().consumeItem(LsimulatorItemId.ADENA, cost)) {
				return;
			}
			item.set_durability(0);
			pcInventory.updateItem(item, LsimulatorPcInventory.COL_DURABILITY);
		}
	}

	@Override
	public String getType() {
		return C_SELECT_LIST;
	}
}
