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

import java.io.Serializable;

import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.map.LsimulatorMap;
import Lsimulator.server.server.model.map.LsimulatorWorldMap;

// Referenced classes of package Lsimulator.server.server.model:
// PcInstance, LsimulatorCharacter

/**
 * 所有對象的基底
 */
public class LsimulatorObject implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 取得對象所存在的地圖ID
	 * 
	 * @return 地圖ID
	 */
	public short getMapId() {
		return (short) _loc.getMap().getId();
	}

	/**
	 * 設定對象所存在的地圖ID
	 * 
	 * @param mapId
	 *            地圖ID
	 */
	public void setMap(short mapId) {
		_loc.setMap(LsimulatorWorldMap.getInstance().getMap(mapId));
	}

	/**
	 * 取得對象所存在的地圖
	 * 
	 */
	public LsimulatorMap getMap() {
		return _loc.getMap();
	}

	/**
	 * 設定對象所存在的地圖
	 * 
	 * @param map
	 *            設定地圖
	 */
	public void setMap(LsimulatorMap map) {
		if (map == null) {
			throw new NullPointerException();
		}
		_loc.setMap(map);
	}

	/**
	 * 取得對象在世界中唯一的ID
	 * 
	 * @return 唯一的ID
	 */
	public int getId() {
		return _id;
	}

	/**
	 * 設定對象在世界中唯一的ID
	 * 
	 * @param id
	 *            唯一的ID
	 */
	public void setId(int id) {
		_id = id;
	}

	/**
	 * 取得對象在地圖上的X軸值
	 * 
	 * @return 座標X軸值
	 */
	public int getX() {
		return _loc.getX();
	}

	/**
	 * 設定對象在地圖上的X軸值
	 * 
	 * @param x
	 *            座標X軸值
	 */
	public void setX(int x) {
		_loc.setX(x);
	}

	/**
	 * 取得對象在地圖上的Y軸值
	 * 
	 * @return 座標Y軸值
	 */
	public int getY() {
		return _loc.getY();
	}

	/**
	 * 設定對象在地圖上的Y軸值
	 * 
	 * @param y
	 *            座標Y軸值
	 */
	public void setY(int y) {
		_loc.setY(y);
	}

	private LsimulatorLocation _loc = new LsimulatorLocation();

	/**
	 * 對象存在在地圖上的LsimulatorLocation
	 * 
	 * @return LsimulatorLocation的座標對應
	 */
	public LsimulatorLocation getLocation() {
		return _loc;
	}

	public void setLocation(LsimulatorLocation loc) {
		_loc.setX(loc.getX());
		_loc.setY(loc.getY());
		_loc.setMap(loc.getMapId());
	}

	public void setLocation(int x, int y, int mapid) {
		_loc.setX(x);
		_loc.setY(y);
		_loc.setMap(mapid);
	}

	/**
	 * 取得與另一個對象間的直線距離。
	 */
	public double getLineDistance(LsimulatorObject obj) {
		return this.getLocation().getLineDistance(obj.getLocation());
	}

	/**
	 * 取得與另一個對象間的距離X軸或Y軸較大的那一個。
	 */
	public int getTileLineDistance(LsimulatorObject obj) {
		return this.getLocation().getTileLineDistance(obj.getLocation());
	}

	/**
	 * 取得與另一個對象間的X軸+Y軸的距離。
	 */
	public int getTileDistance(LsimulatorObject obj) {
		return this.getLocation().getTileDistance(obj.getLocation());
	}

	/**
	 * 對象的螢幕範圍進入玩家
	 * 
	 * @param perceivedFrom
	 *            進入螢幕範圍的玩家
	 */
	public void onPerceive(PcInstance perceivedFrom) {
	}

	/**
	 * 對象對玩家採取的行動
	 * 
	 * @param actionFrom
	 *            要採取行動的玩家目標
	 */
	public void onAction(PcInstance actionFrom) {
	}

	/**
	 * 與對象交談的玩家
	 * 
	 * @param talkFrom
	 *            交談的玩家
	 */
	public void onTalkAction(PcInstance talkFrom) {
	}

	private int _id = 0;

	public void onAction(PcInstance attacker, int skillId) {

	}
}
