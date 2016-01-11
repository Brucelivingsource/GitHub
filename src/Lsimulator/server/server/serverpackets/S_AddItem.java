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

import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.model.Instance.ItemInstance;

// Referenced classes of package Lsimulator.server.server.serverpackets:
// ServerBasePacket

public class S_AddItem extends ServerBasePacket {

	private static final String S_ADD_ITEM = "[S] S_AddItem";

	/**
	 * 增加物品到背包處理封包。
	 */
	public S_AddItem(ItemInstance item) {
		writeC(Opcodes.S_OPCODE_ADDITEM);
		writeD(item.getId());
		writeH(item.getItem().getMagicCatalystType() > 0 ? item.getItem().getMagicCatalystType() :
			   item.getItem().getItemDescId() > 0 ? item.getItem().getItemDescId() : item.getItem().getGroundGfxId());
		writeC(item.getItem().getUseType());
		writeC(item.getChargeCount());
		writeH(item.get_gfxid());
		writeC(item.getBless());
		writeD(item.getCount());
		writeC(item.getItemStatusX());                // 3.80C 物品驗證機制
		writeS(item.getViewName());
		if (!item.isIdentified()) {                   // 未鑑定
			writeC(0);
		} else {
			byte[] status = item.getStatusBytes();
			writeC(status.length);
			for (byte b : status) {
				writeC(b);
			}
		}
		writeC(0x17);
		writeC(0);
		writeH(0);
		writeH(0);
		if(item.getItem().getType() == 10){     // 如果是法書，傳出法術編號
			writeC(0);
		} else {
			writeC(item.getEnchantLevel());     // 物品武捲等級
		}
		writeD(item.getId());                              // 3.80 物品世界流水編號
		writeD(0);
		writeD(0);
		writeD(item.getBless() >= 128 ? 3 : item.getItem().isTradable() ? 7 : 2); // 7:可刪除, 2: 不可刪除, 3: 封印狀態
		writeC(0);
		
		/*writeC(0x17);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeH(0);
		writeC(0);*/
	}

	@Override
	public byte[] getContent() {
		return _bao.toByteArray();
	}

	@Override
	public String getType() {
		return S_ADD_ITEM;
	}
}
