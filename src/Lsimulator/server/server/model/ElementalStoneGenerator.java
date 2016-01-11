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

import java.util.ArrayList;
import Lsimulator.server.server.utils.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;
import Lsimulator.server.server.types.Point;

public class ElementalStoneGenerator implements Runnable {

	private static Logger _log = Logger.getLogger(ElementalStoneGenerator.class
			.getName());

	private static final int ELVEN_FOREST_MAPID = 4;
	private static final int MAX_COUNT = Config.ELEMENTAL_STONE_AMOUNT; // 設置個数
	private static final int INTERVAL = 3; // 設置間隔 秒
	private static final int SLEEP_TIME = 300; // 設置終了後、再設置までのスリープ時間 秒
	private static final int FIRST_X = 32911;
	private static final int FIRST_Y = 32210;
	private static final int LAST_X = 33141;
	private static final int LAST_Y = 32500;
	private static final int ELEMENTAL_STONE_ID = 40515; // 精霊の石

	private ArrayList<LsimulatorGroundInventory> _itemList = new ArrayList<LsimulatorGroundInventory>(
			MAX_COUNT);

	private static ElementalStoneGenerator _instance = null;

	private ElementalStoneGenerator() {
	}

	public static ElementalStoneGenerator getInstance() {
		if (_instance == null) {
			_instance = new ElementalStoneGenerator();
		}
		return _instance;
	}

	private final LsimulatorObject _dummy = new LsimulatorObject();

	/**
	 * 指定された位置に石を置けるかを返す。
	 */
	private boolean canPut(LsimulatorLocation loc) {
		_dummy.setMap(loc.getMap());
		_dummy.setX(loc.getX());
		_dummy.setY(loc.getY());

		// 可視範囲のプレイヤーチェック
		if (LsimulatorWorld.getInstance().getVisiblePlayer(_dummy).size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 次の設置ポイントを決める。
	 */
	private Point nextPoint() {
		int newX = Random.nextInt(LAST_X - FIRST_X) + FIRST_X;
		int newY = Random.nextInt(LAST_Y - FIRST_Y) + FIRST_Y;

		return new Point(newX, newY);
	}

	/**
	 * 拾われた石をリストから削除する。
	 */
	private void removeItemsPickedUp() {
		for (int i = 0; i < _itemList.size(); i++) {
			LsimulatorGroundInventory gInventory = _itemList.get(i);
			if (!gInventory.checkItem(ELEMENTAL_STONE_ID)) {
				_itemList.remove(i);
				i--;
			}
		}
	}

	/**
	 * 指定された位置へ石を置く。
	 */
	private void putElementalStone(LsimulatorLocation loc) {
		LsimulatorGroundInventory gInventory = LsimulatorWorld.getInstance().getInventory(loc);

		ItemInstance item = ItemTable.getInstance().createItem(
				ELEMENTAL_STONE_ID);
		item.setEnchantLevel(0);
		item.setCount(1);
		gInventory.storeItem(item);
		_itemList.add(gInventory);
	}

	@Override
	public void run() {
		try {
			LsimulatorMap map = LsimulatorWorldMap.getInstance().getMap(
					(short) ELVEN_FOREST_MAPID);
			while (true) {
				removeItemsPickedUp();

				while (_itemList.size() < MAX_COUNT) { // 減っている場合セット
					LsimulatorLocation loc = new LsimulatorLocation(nextPoint(), map);

					if (!canPut(loc)) {
						// XXX 設置範囲内全てにPCが居た場合無限ループになるが…
						continue;
					}

					putElementalStone(loc);

					Thread.sleep(INTERVAL * 1000); // 一定時間毎に設置
				}
				Thread.sleep(SLEEP_TIME * 1000); // maxまで設置終了後一定時間は再設置しない
			}
		} catch (Throwable e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
