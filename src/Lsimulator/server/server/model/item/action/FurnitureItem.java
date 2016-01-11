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
package Lsimulator.server.server.model.item.action;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.datatables.FurnitureSpawnTable;
import Lsimulator.server.server.datatables.NpcTable;
import Lsimulator.server.server.datatables.FurnitureItemTable;
import Lsimulator.server.server.model.LsimulatorHouseLocation;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorPcInventory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.FurnitureInstance;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_AttackPacket;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorFurnitureItem;
import Lsimulator.server.server.templates.LsimulatorNpc;

public class FurnitureItem {
	private static Logger _log = Logger
			.getLogger(FurnitureItem.class.getName());

	public static void useFurnitureItem(PcInstance pc, int itemId,
			int itemObjectId) {

		LsimulatorFurnitureItem furniture_item = FurnitureItemTable.getInstance()
				.getTemplate((itemId));

		boolean isAppear = true;

		FurnitureInstance furniture = null;

		if (furniture_item == null) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1沒有任何事情發生。
			return;
		}

		if (!LsimulatorHouseLocation.isInHouse(pc.getX(), pc.getY(), pc.getMapId())) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1ここでは使えません。
			return;
		}

		for (LsimulatorObject l1object : LsimulatorWorld.getInstance().getObject()) {
			if (l1object instanceof FurnitureInstance) {
				furniture = (FurnitureInstance) l1object;
				if (furniture.getItemObjId() == itemObjectId) { // 既に引き出している家具
					isAppear = false;
					break;
				}
			}
		}

		if (isAppear) {
			if ((pc.getHeading() != 0) && (pc.getHeading() != 2)) {
				return;
			}
			int npcId = furniture_item.getFurnitureNpcId();
			try {
				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(npcId);
				if (l1npc != null) {
					try {
						String s = l1npc.getImpl();
						Constructor<?> constructor = Class.forName(
								"Lsimulator.server.server.model.Instance." + s
										+ "Instance").getConstructors()[0];
						Object aobj[] = { l1npc };
						furniture = (FurnitureInstance) constructor
								.newInstance(aobj);
						furniture.setId(IdFactory.getInstance().nextId());
						furniture.setMap(pc.getMapId());
						if (pc.getHeading() == 0) {
							furniture.setX(pc.getX());
							furniture.setY(pc.getY() - 1);
						} else if (pc.getHeading() == 2) {
							furniture.setX(pc.getX() + 1);
							furniture.setY(pc.getY());
						}
						furniture.setHomeX(furniture.getX());
						furniture.setHomeY(furniture.getY());
						furniture.setHeading(0);
						furniture.setItemObjId(itemObjectId);

						LsimulatorWorld.getInstance().storeObject(furniture);
						LsimulatorWorld.getInstance().addVisibleObject(furniture);
						FurnitureSpawnTable.getInstance().insertFurniture(
								furniture);
					} catch (Exception e) {
						_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			} catch (Exception exception) {
			}
		} else {
			furniture.deleteMe();
			FurnitureSpawnTable.getInstance().deleteFurniture(furniture);
		}
	}

	// 傢俱移除魔杖
	public static void useFurnitureRemovalWand(PcInstance pc, int targetId,
			ItemInstance item) {
		S_AttackPacket s_attackPacket = new S_AttackPacket(pc, 0,
				ActionCodes.ACTION_Wand);
		pc.sendPackets(s_attackPacket);
		pc.broadcastPacket(s_attackPacket);
		int chargeCount = item.getChargeCount();
		if (chargeCount <= 0) {
			return;
		}

		LsimulatorObject target = LsimulatorWorld.getInstance().findObject(targetId);
		if ((target != null) && (target instanceof FurnitureInstance)) {
			FurnitureInstance furniture = (FurnitureInstance) target;
			furniture.deleteMe();
			FurnitureSpawnTable.getInstance().deleteFurniture(furniture);
			item.setChargeCount(item.getChargeCount() - 1);
			pc.getInventory().updateItem(item, LsimulatorPcInventory.COL_CHARGE_COUNT);
		}
	}

}
