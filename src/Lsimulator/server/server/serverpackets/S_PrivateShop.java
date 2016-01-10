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
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.templates.LsimulatorPrivateShopBuyList;
import Lsimulator.server.server.templates.LsimulatorPrivateShopSellList;

// Referenced classes of package Lsimulator.server.server.serverpackets:
// ServerBasePacket

public class S_PrivateShop extends ServerBasePacket {

	public S_PrivateShop(LsimulatorPcInstance pc, int objectId, int type) {
		LsimulatorPcInstance shopPc = (LsimulatorPcInstance) LsimulatorWorld.getInstance().findObject(objectId);

		if (shopPc == null) {
			return;
		}

		writeC(Opcodes.S_OPCODE_PRIVATESHOPLIST);
		writeC(type);
		writeD(objectId);

		if (type == 0) {
			List<LsimulatorPrivateShopSellList> list = shopPc.getSellList();
			int size = list.size();
			pc.setPartnersPrivateShopItemCount(size);
			writeH(size);
			for (int i = 0; i < size; i++) {
				LsimulatorPrivateShopSellList pssl = list.get(i);
				int itemObjectId = pssl.getItemObjectId();
				int count = pssl.getSellTotalCount() - pssl.getSellCount();
				int price = pssl.getSellPrice();
				LsimulatorItemInstance item = shopPc.getInventory().getItem(itemObjectId);
				if (item != null) {
					writeC(i);
					writeC(item.getBless());
					writeH(item.getItem().getGfxId());
					writeD(count);
					writeD(price);
					writeS(item.getNumberedViewName(count));
					writeC(0);
				}
			}
		}
		else if (type == 1) {
			List<LsimulatorPrivateShopBuyList> list = shopPc.getBuyList();
			int size = list.size();
			writeH(size);
			for (int i = 0; i < size; i++) {
				LsimulatorPrivateShopBuyList psbl = list.get(i);
				int itemObjectId = psbl.getItemObjectId();
				int count = psbl.getBuyTotalCount();
				int price = psbl.getBuyPrice();
				LsimulatorItemInstance item = shopPc.getInventory().getItem(itemObjectId);
				for (LsimulatorItemInstance pcItem : pc.getInventory().getItems()) {
					if ((item.getItemId() == pcItem.getItemId()) && (item.getEnchantLevel() == pcItem.getEnchantLevel())) {
						writeC(i);
						writeD(pcItem.getId());
						writeD(count);
						writeD(price);
					}
				}
			}
		}
	}

	@Override
	public byte[] getContent() {
		return getBytes();
	}
}
