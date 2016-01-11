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
package Lsimulator.server.server.command.executor;

import java.util.List;

import Lsimulator.server.server.datatables.FurnitureSpawnTable;
import Lsimulator.server.server.datatables.LetterTable;
import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.FurnitureInstance;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;

/**
 * GM指令：刪除地上道具
 */
public class LsimulatorDeleteGroundItem implements LsimulatorCommandExecutor {
	private LsimulatorDeleteGroundItem() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorDeleteGroundItem();
	}

	@Override
	public void execute(PcInstance pc, String cmdName, String arg) {
		for (LsimulatorObject l1object : LsimulatorWorld.getInstance().getObject()) {
			if (l1object instanceof ItemInstance) {
				ItemInstance l1iteminstance = (ItemInstance) l1object;
				if ((l1iteminstance.getX() == 0) && (l1iteminstance.getY() == 0)) { // 地面上のアイテムではなく、誰かの所有物
					continue;
				}

				List<PcInstance> players = LsimulatorWorld.getInstance().getVisiblePlayer(l1iteminstance, 0);
				if (0 == players.size()) {
					LsimulatorInventory groundInventory = LsimulatorWorld.getInstance().getInventory(l1iteminstance.getX(), l1iteminstance.getY(),
							l1iteminstance.getMapId());
					int itemId = l1iteminstance.getItem().getItemId();
					if ((itemId == 40314) || (itemId == 40316)) { // ペットのアミュレット
						PetTable.getInstance().deletePet(l1iteminstance.getId());
					}
					else if ((itemId >= 49016) && (itemId <= 49025)) { // 便箋
						LetterTable lettertable = new LetterTable();
						lettertable.deleteLetter(l1iteminstance.getId());
					}
					else if ((itemId >= 41383) && (itemId <= 41400)) { // 家具
						if (l1object instanceof FurnitureInstance) {
							FurnitureInstance furniture = (FurnitureInstance) l1object;
							if (furniture.getItemObjId() == l1iteminstance.getId()) { // 既に引き出している家具
								FurnitureSpawnTable.getInstance().deleteFurniture(furniture);
							}
						}
					}
					groundInventory.deleteItem(l1iteminstance);
					LsimulatorWorld.getInstance().removeVisibleObject(l1iteminstance);
					LsimulatorWorld.getInstance().removeObject(l1iteminstance);
				}
			}
		}
		LsimulatorWorld.getInstance().broadcastServerMessage("地上的垃圾被GM清除了。");
	}
}
