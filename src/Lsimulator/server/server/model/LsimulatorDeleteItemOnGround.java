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
package Lsimulator.server.server.model;

import java.util.List;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.LsimulatorMessage;
import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorDeleteItemOnGround

public class LsimulatorDeleteItemOnGround {
	private DeleteTimer _deleteTimer;

	private static final Logger _log = Logger
			.getLogger(LsimulatorDeleteItemOnGround.class.getName());

	public LsimulatorDeleteItemOnGround() {
	}

	private class DeleteTimer implements Runnable {
		public DeleteTimer() {
		}

		@Override
		public void run() {
			LsimulatorMessage.getInstance();// Locale 多國語系
			int time = Config.ALT_ITEM_DELETION_TIME * 60 * 1000 - 10 * 1000;
			for (;;) {
				try {
					Thread.sleep(time);
				} catch (Exception exception) {
					_log.warning("LsimulatorDeleteItemOnGround error: " + exception);
					break;
				}
				LsimulatorWorld.getInstance().broadcastPacketToAll(
						new S_ServerMessage(166, LsimulatorMessage.onGroundItem,
								LsimulatorMessage.secondsDelete + "。"));
				try {
					Thread.sleep(10000);
				} catch (Exception exception) {
					_log.warning("LsimulatorDeleteItemOnGround error: " + exception);
					break;
				}
				deleteItem();
				LsimulatorWorld.getInstance().broadcastPacketToAll(
						new S_ServerMessage(166, LsimulatorMessage.onGroundItem,
								LsimulatorMessage.deleted + "。"));
			}
		}
	}

	public void initialize() {
		if (!Config.ALT_ITEM_DELETION_TYPE.equalsIgnoreCase("auto")) {
			return;
		}

		_deleteTimer = new DeleteTimer();
		GeneralThreadPool.getInstance().execute(_deleteTimer); // タイマー開始
	}

	private void deleteItem() {
		int numOfDeleted = 0;
		for (LsimulatorObject obj : LsimulatorWorld.getInstance().getObject()) {
			if (!(obj instanceof ItemInstance)) {
				continue;
			}

			ItemInstance item = (ItemInstance) obj;
			if (item.getX() == 0 && item.getY() == 0) { // 地面上のアイテムではなく、誰かの所有物
				continue;
			}
			if (item.getItem().getItemId() == 40515) { // 精霊の石
				continue;
			}
			if (LsimulatorHouseLocation.isInHouse(item.getX(), item.getY(),
					item.getMapId())) { // アジト内
				continue;
			}

			List<PcInstance> players = LsimulatorWorld.getInstance()
					.getVisiblePlayer(item, Config.ALT_ITEM_DELETION_RANGE);
			if (players.isEmpty()) { // 指定範囲内にプレイヤーが居なければ削除
				LsimulatorInventory groundInventory = LsimulatorWorld
						.getInstance()
						.getInventory(item.getX(), item.getY(), item.getMapId());
				groundInventory.removeItem(item);
				numOfDeleted++;
			}
		}
		_log.fine("ワールドマップ上のアイテムを自動削除。削除数: " + numOfDeleted);
	}
}
