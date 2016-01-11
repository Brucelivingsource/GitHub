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
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server.serverpackets:
// ServerBasePacket

public class S_PetList extends ServerBasePacket {

	private static final String S_PETLIST = "[S] S_PetList";

	private byte[] _byte = null;

	public S_PetList(int npcObjId, PcInstance pc) {
		buildPacket(npcObjId, pc);
	}

	private void buildPacket(int npcObjId, PcInstance pc) {
		List<ItemInstance> amuletList = Lists.newList();
		// 判斷身上是否有寵物項圈！
		for (ItemInstance item : pc.getInventory().getItems()) {
			if ((item.getItem().getItemId() == 40314) || (item.getItem().getItemId() == 40316)) {
				if (!isWithdraw(pc, item)) {
					amuletList.add(item);
				}
			}
		}

		if (amuletList.size() != 0) {
			writeC(Opcodes.S_OPCODE_SHOWRETRIEVELIST);
			writeD(npcObjId);
			writeH(amuletList.size());
			writeC(0x0c);
			for (ItemInstance item : amuletList) {
				writeD(item.getId());
				writeC(0x00);
				writeH(item.get_gfxid());
				writeC(item.getBless());
				writeD(item.getCount());
				writeC(item.isIdentified() ? 1 : 0);
				writeS(item.getViewName());
			}
		} else {
			return;
		}
		writeD(0x00000073); // Price
	}

	private boolean isWithdraw(PcInstance pc, ItemInstance item) {
		for (NpcInstance petNpc : pc.getPetList().values()) {
			if (petNpc instanceof PetInstance) {
				PetInstance pet = (PetInstance) petNpc;
				if (item.getId() == pet.getItemObjId()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public byte[] getContent() {
		if (_byte == null) {
			_byte = getBytes();
		}
		return _byte;
	}

	@Override
	public String getType() {
		return S_PETLIST;
	}
}
