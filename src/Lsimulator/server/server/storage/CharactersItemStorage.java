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

import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.storage.mysql.MySqlCharactersItemStorage;

public abstract class CharactersItemStorage {
	public abstract List<ItemInstance> loadItems(int objId) throws Exception;

	public abstract void storeItem(int objId, ItemInstance item)
			throws Exception;

	public abstract void deleteItem(ItemInstance item) throws Exception;

	public abstract void updateItemId(ItemInstance item) throws Exception;

	public abstract void updateItemCount(ItemInstance item) throws Exception;

	public abstract void updateItemIdentified(ItemInstance item)
			throws Exception;

	public abstract void updateItemEquipped(ItemInstance item)
			throws Exception;

	public abstract void updateItemEnchantLevel(ItemInstance item)
			throws Exception;

	public abstract void updateItemDurability(ItemInstance item)
			throws Exception;

	public abstract void updateItemChargeCount(ItemInstance item)
			throws Exception;

	public abstract void updateItemRemainingTime(ItemInstance item)
			throws Exception;

	public abstract void updateItemDelayEffect(ItemInstance item)
			throws Exception;

	public abstract int getItemCount(int objId) throws Exception;

	public abstract void updateItemBless(ItemInstance item) throws Exception;

	public abstract void updateItemAttrEnchantKind(ItemInstance item)
			throws Exception;

	public abstract void updateItemAttrEnchantLevel(ItemInstance item)
			throws Exception;

	public abstract void updateaddHp(ItemInstance item) throws Exception;

	public abstract void updateaddMp(ItemInstance item) throws Exception;

	public abstract void updateHpr(ItemInstance item) throws Exception;

	public abstract void updateMpr(ItemInstance item) throws Exception;

	public abstract void updateFireMr(ItemInstance item) throws Exception;

	public abstract void updateWaterMr(ItemInstance item) throws Exception;

	public abstract void updateEarthMr(ItemInstance item) throws Exception;

	public abstract void updateWindMr(ItemInstance item) throws Exception;

	public abstract void updateaddSp(ItemInstance item) throws Exception;

	public abstract void updateM_Def(ItemInstance item) throws Exception;

	public static CharactersItemStorage create() {
		if (_instance == null) {
			_instance = new MySqlCharactersItemStorage();
		}
		return _instance;
	}

	private static CharactersItemStorage _instance;
}
