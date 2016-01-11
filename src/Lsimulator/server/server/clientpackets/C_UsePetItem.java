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
import Lsimulator.server.server.datatables.PetItemTable;
import Lsimulator.server.server.datatables.PetTypeTable;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.serverpackets.S_PetEquipment;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorPetItem;
import Lsimulator.server.server.templates.LsimulatorPetType;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來使用寵物道具的封包
 */
public class C_UsePetItem extends ClientBasePacket {

	/**
	 * 【Client】 id:60 size:8 time:1302335819781
	 * 0000	3c 00 04 bd 54 00 00 00                            <...T...
	 * 
	 * 【Server】 id:82 size:16 time:1302335819812
	 * 0000	52 25 00 04 bd 54 00 00 0a 37 80 08 7e ec d0 46    R%...T...7..~..F
	*/

	private static final String C_USE_PET_ITEM = "[C] C_UsePetItem";

	public C_UsePetItem(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);
		
		PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}

		int data = readC();
		int petId = readD();
		int listNo = readC();

		PetInstance pet = (PetInstance) LsimulatorWorld.getInstance().findObject(petId);
		if (pet == null)  {
			return;
		}
		ItemInstance item = pet.getInventory().getItems().get(listNo);
		if (item == null) {
			return;
		}

		if ((item.getItem().getType2() == 0)
				&& (item.getItem().getType() == 11)) { // 寵物道具
			LsimulatorPetType petType = PetTypeTable.getInstance().get(pet.getNpcTemplate().get_npcId());
			// 判斷是否可用寵物裝備
			if (!petType.canUseEquipment()) {
				pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
				return;
			}
			int itemId = item.getItem().getItemId();
			LsimulatorPetItem petItem = PetItemTable.getInstance().getTemplate(itemId);
			if (petItem.getUseType() == 1) { // 牙齒
				pet.usePetWeapon(pet, item);
				pc.sendPackets(new S_PetEquipment(data, pet, listNo)); // 裝備時更新寵物資訊
			} else if (petItem.getUseType() == 0) { // 盔甲
				pet.usePetArmor(pet, item);
				pc.sendPackets(new S_PetEquipment(data, pet, listNo)); // 裝備時更新寵物資訊
			} else {
				pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
			}
		} else {
			pc.sendPackets(new S_ServerMessage(74, item.getLogName()));
		}
	}

	@Override
	public String getType() {
		return C_USE_PET_ITEM;
	}
}
