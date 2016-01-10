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
package Lsimulator.server.server.model.map;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.server.utils.PerformanceTimer;

public class LsimulatorWorldMap {
	private static Logger _log = Logger.getLogger(LsimulatorWorldMap.class.getName());

	private static LsimulatorWorldMap _instance;
	private Map<Integer, LsimulatorMap> _maps;

	public static LsimulatorWorldMap getInstance() {
		if (_instance == null) {
			_instance = new LsimulatorWorldMap();
		}
		return _instance;
	}

	private LsimulatorWorldMap() {
		PerformanceTimer timer = new PerformanceTimer();
		System.out.print("loading map...");

		try {
			_maps = MapReader.getDefaultReader().read();
			if (_maps == null) {
				throw new RuntimeException("地圖檔案讀取失敗...");
			}
		} catch (FileNotFoundException e) {  
			System.out.println("提示: 地圖檔案缺失，請檢查330_maps.zip是否尚未解壓縮。"); 
			System.exit(0);
		} catch (Exception e) {
			// 復帰不能
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			System.exit(0);
		}

		System.out.println("OK! " + timer.get() + "ms");
	}

	/**
	 * 指定されたマップの情報を保持するLsimulatorMapを返す。
	 * 
	 * @param mapId
	 *            マップID
	 * @return マップ情報を保持する、LsimulatorMapオブジェクト。
	 */
	public LsimulatorMap getMap(short mapId) {
		LsimulatorMap map = _maps.get((int) mapId);
		if (map == null) { // マップ情報が無い
			map = LsimulatorMap.newNull(); // 何もしないMapを返す。
		}
		return map;
	}
}
