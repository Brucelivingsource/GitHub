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

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.serverpackets.S_AttackPacket;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

/**
 * 處理收到由客戶端傳來撿起道具的封包
 */
public class C_PickUpItem extends ClientBasePacket {

	private static final String C_PICK_UP_ITEM = "[C] C_PickUpItem";

	public C_PickUpItem(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		
		PcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isDead() || pc.isGhost()) {
			return;
		}
		
		int x = readH();
		int y = readH();
		int objectId = readD();
		int pickupCount = readD();
		
		if (objectId == pc.getId()) {
			return;
		}

		if (pc.isInvisble()) { // 隱身狀態
			return;
		}
		if (pc.isInvisDelay()) { // 還在解除隱身的延遲
			return;
		}

		LsimulatorInventory groundInventory = LsimulatorWorld.getInstance().getInventory(x, y, pc.getMapId());
		LsimulatorObject object = groundInventory.getItem(objectId);

		if ((object != null) && !pc.isDead()) {
			ItemInstance item = (ItemInstance) object;
			if ((item.getItemOwnerId() != 0) && (pc.getId() != item.getItemOwnerId())) {
				pc.sendPackets(new S_ServerMessage(623)); // アイテムが拾えませんでした。
				return;
			}
			if (pc.getLocation().getTileLineDistance(item.getLocation()) > 3) {
				return;
			}

			if (item.getItem().getItemId() == LsimulatorItemId.ADENA) {
				ItemInstance inventoryItem = pc.getInventory().findItemId(LsimulatorItemId.ADENA);
				int inventoryItemCount = 0;
				if (inventoryItem != null) {
					inventoryItemCount = inventoryItem.getCount();
				}
				// 超過20億
				if ((long) inventoryItemCount + (long) pickupCount > 2000000000L) {
					pc.sendPackets(new S_ServerMessage(166, // \f1%0が%4%1%3%2
							"你身上的金幣已經超過", "2,000,000,000了，所以不能撿取金幣。"));
					return;
				}
			}

			if (pc.getInventory().checkAddItem( // 檢查容量與重量
					item, pickupCount) == LsimulatorInventory.OK) {
				if ((item.getX() != 0) && (item.getY() != 0)) { // ワールドマップ上のアイテム
					groundInventory.tradeItem(item, pickupCount, pc.getInventory());
					pc.turnOnOffLight();

					S_AttackPacket s_attackPacket = new S_AttackPacket(pc, objectId, ActionCodes.ACTION_Pickup);
					pc.sendPackets(s_attackPacket);
					if (!pc.isGmInvis()) {
						pc.broadcastPacket(s_attackPacket);
					}
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_PICK_UP_ITEM;
	}
}
