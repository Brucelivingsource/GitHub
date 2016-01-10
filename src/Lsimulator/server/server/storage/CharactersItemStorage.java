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
package Lsimulator.server.server.storage;

import java.util.List;

import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.storage.mysql.MySqlCharactersItemStorage;

public abstract class CharactersItemStorage {
	public abstract List<LsimulatorItemInstance> loadItems(int objId) throws Exception;

	public abstract void storeItem(int objId, LsimulatorItemInstance item)
			throws Exception;

	public abstract void deleteItem(LsimulatorItemInstance item) throws Exception;

	public abstract void updateItemId(LsimulatorItemInstance item) throws Exception;

	public abstract void updateItemCount(LsimulatorItemInstance item) throws Exception;

	public abstract void updateItemIdentified(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemEquipped(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemEnchantLevel(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemDurability(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemChargeCount(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemRemainingTime(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemDelayEffect(LsimulatorItemInstance item)
			throws Exception;

	public abstract int getItemCount(int objId) throws Exception;

	public abstract void updateItemBless(LsimulatorItemInstance item) throws Exception;

	public abstract void updateItemAttrEnchantKind(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateItemAttrEnchantLevel(LsimulatorItemInstance item)
			throws Exception;

	public abstract void updateaddHp(LsimulatorItemInstance item) throws Exception;

	public abstract void updateaddMp(LsimulatorItemInstance item) throws Exception;

	public abstract void updateHpr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateMpr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateFireMr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateWaterMr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateEarthMr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateWindMr(LsimulatorItemInstance item) throws Exception;

	public abstract void updateaddSp(LsimulatorItemInstance item) throws Exception;

	public abstract void updateM_Def(LsimulatorItemInstance item) throws Exception;

	public static CharactersItemStorage create() {
		if (_instance == null) {
			_instance = new MySqlCharactersItemStorage();
		}
		return _instance;
	}

	private static CharactersItemStorage _instance;
}
