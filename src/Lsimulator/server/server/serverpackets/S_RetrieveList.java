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

import java.io.IOException;

import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;

public class S_RetrieveList extends ServerBasePacket {
	public S_RetrieveList(int objid, PcInstance pc) {
		if (pc.getInventory().getSize() < 180) {
			int size = pc.getDwarfInventory().getSize();
			if (size > 0) {
				writeC(Opcodes.S_OPCODE_SHOWRETRIEVELIST);
				writeD(objid);
				writeH(size);
				writeC(3); // 個人倉庫
				for (Object itemObject : pc.getDwarfInventory().getItems()) {
					ItemInstance item = (ItemInstance) itemObject;
					writeD(item.getId());
					writeC(item.getItem().getUseType()); // 道具:0 武器:1  防具:2...
					writeH(item.get_gfxid());
					writeC(item.getBless());
					writeD(item.getCount());
					writeC(item.isIdentified() ? 1 : 0);
					writeS(item.getViewName());
				}
				writeD(0x0000001e); // 金幣30
			} else {
				pc.sendPackets(new S_ServerMessage(1625));
			}
		} else {
			pc.sendPackets(new S_ServerMessage(263)); // \f1一人のキャラクターが持って歩けるアイテムは最大180個までです。
		}
	}

	@Override
	public byte[] getContent() throws IOException {
		return getBytes();
	}

}
