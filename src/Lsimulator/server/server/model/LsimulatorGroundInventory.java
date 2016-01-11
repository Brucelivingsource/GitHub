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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_DropItem;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.serverpackets.S_RemoveObject;
import Lsimulator.server.server.utils.collections.Maps;

public class LsimulatorGroundInventory extends LsimulatorInventory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Timer _timer = new Timer();

	private Map<Integer, DeletionTimer> _reservedTimers = Maps.newMap();

	private class DeletionTimer extends TimerTask {
		private final ItemInstance _item;

		public DeletionTimer(ItemInstance item) {
			_item = item;
		}

		@Override
		public void run() {
			try {
				synchronized (LsimulatorGroundInventory.this) {
					if (!_items.contains(_item)) {// 拾われたタイミングによってはこの条件を満たし得る
						return; // 既に拾われている
					}
					removeItem(_item);
				}
			}
			catch (Throwable t) {
				_log.log(Level.SEVERE, t.getLocalizedMessage(), t);
			}
		}
	}

	private void setTimer(ItemInstance item) {
		if (!Config.ALT_ITEM_DELETION_TYPE.equalsIgnoreCase("std")) {
			return;
		}
		if (item.getItemId() == 40515) { // 精霊の石
			return;
		}

		_timer.schedule(new DeletionTimer(item), Config.ALT_ITEM_DELETION_TIME * 60 * 1000);
	}

	private void cancelTimer(ItemInstance item) {
		DeletionTimer timer = _reservedTimers.get(item.getId());
		if (timer == null) {
			return;
		}
		timer.cancel();
	}

	public LsimulatorGroundInventory(int objectId, int x, int y, short map) {
		setId(objectId);
		setX(x);
		setY(y);
		setMap(map);
		LsimulatorWorld.getInstance().addVisibleObject(this);
	}

	@Override
	public void onPerceive(PcInstance perceivedFrom) {
		for (ItemInstance item : getItems()) {
			if (!perceivedFrom.knownsObject(item)) {
				perceivedFrom.addKnownObject(item);
				perceivedFrom.sendPackets(new S_DropItem(item)); // プレイヤーへDROPITEM情報を通知
			}
		}
	}

	// 認識範囲内にいるプレイヤーへオブジェクト送信
	@Override
	public void insertItem(ItemInstance item) {
		setTimer(item);

		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_DropItem(item));
			pc.addKnownObject(item);
		}
	}

	// 見える範囲内にいるプレイヤーのオブジェクト更新
	@Override
	public void updateItem(ItemInstance item) {
		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_DropItem(item));
		}
	}

	// 空インベントリ破棄及び見える範囲内にいるプレイヤーのオブジェクト削除
	@Override
	public void deleteItem(ItemInstance item) {
		cancelTimer(item);
		for (PcInstance pc : LsimulatorWorld.getInstance().getRecognizePlayer(item)) {
			pc.sendPackets(new S_RemoveObject(item));
			pc.removeKnownObject(item);
		}

		_items.remove(item);
		if (_items.size() == 0) {
			LsimulatorWorld.getInstance().removeVisibleObject(this);
		}
	}

	private static Logger _log = Logger.getLogger(LsimulatorPcInventory.class.getName());
}
