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
package Lsimulator.server.server.serverpackets;

import java.util.List;

import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.datatables.ShopTable;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.shop.LsimulatorAssessedItem;
import Lsimulator.server.server.model.shop.LsimulatorShop;

// Referenced classes of package Lsimulator.server.server.serverpackets:
// ServerBasePacket, S_SystemMessage

public class S_ShopBuyList extends ServerBasePacket {

	private static final String S_SHOP_BUY_LIST = "[S] S_ShopBuyList";

	public S_ShopBuyList(int objid, PcInstance pc) {
		LsimulatorObject object = LsimulatorWorld.getInstance().findObject(objid);
		if (!(object instanceof NpcInstance)) {
			return;
		}
		NpcInstance npc = (NpcInstance) object;
		int npcId = npc.getNpcTemplate().get_npcId();
		LsimulatorShop shop = ShopTable.getInstance().get(npcId);
		if (shop == null) {
			pc.sendPackets(new S_NoSell(npc));
			return;
		}

		List<LsimulatorAssessedItem> assessedItems = shop.assessItems(pc.getInventory());
		if (assessedItems.isEmpty()) {
			pc.sendPackets(new S_NoSell(npc));
			return;
		}

		writeC(Opcodes.S_OPCODE_SHOWSHOPSELLLIST);
		writeD(objid);
		writeH(assessedItems.size());

		for (LsimulatorAssessedItem item : assessedItems) {
			writeD(item.getTargetId());
			writeD(item.getAssessedPrice());
		}
		writeH(0x0007); // 7 = 金幣為單位 顯示總金額
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}

	@Override
	public String getType() {
		return S_SHOP_BUY_LIST;
	}
}