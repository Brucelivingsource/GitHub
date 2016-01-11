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
package Lsimulator.server.server.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.datatables.InnKeyTable;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.storage.CharactersItemStorage;
import Lsimulator.server.server.templates.LsimulatorItem;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Lists;

public class MySqlCharactersItemStorage extends CharactersItemStorage {

	private static final Logger _log = Logger
			.getLogger(MySqlCharactersItemStorage.class.getName());

	@Override
	public List<ItemInstance> loadItems(int objId) throws Exception {
		List<ItemInstance> items = Lists.newList();

		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_items WHERE char_id = ?");
			pstm.setInt(1, objId);

			ItemInstance item;
			rs = pstm.executeQuery();
			while (rs.next()) {
				int itemId = rs.getInt("item_id");
				LsimulatorItem itemTemplate = ItemTable.getInstance().getTemplate(
						itemId);
				if (itemTemplate == null) {
					_log.warning(String.format("item id:%d not found", itemId));
					continue;
				}
				item = new ItemInstance();
				item.setId(rs.getInt("id"));
				item.setItem(itemTemplate);
				item.setCount(rs.getInt("count"));
				item.setEquipped(rs.getInt("is_equipped") != 0 ? true : false);
				item.setEnchantLevel(rs.getInt("enchantlvl"));
				item.setIdentified(rs.getInt("is_id") != 0 ? true : false);
				item.set_durability(rs.getInt("durability"));
				item.setChargeCount(rs.getInt("charge_count"));
				item.setRemainingTime(rs.getInt("remaining_time"));
				item.setLastUsed(rs.getTimestamp("last_used"));
				item.setBless(rs.getInt("bless"));
				item.setAttrEnchantKind(rs.getInt("attr_enchant_kind"));
				item.setAttrEnchantLevel(rs.getInt("attr_enchant_level"));
				item.setFireMr(rs.getInt("firemr"));
				item.setWaterMr(rs.getInt("watermr"));
				item.setEarthMr(rs.getInt("earthmr"));
				item.setWindMr(rs.getInt("windmr"));
				item.setaddSp(rs.getInt("addsp"));
				item.setaddHp(rs.getInt("addhp"));
				item.setaddMp(rs.getInt("addmp"));
				item.setHpr(rs.getInt("hpr"));
				item.setMpr(rs.getInt("mpr"));
				item.setM_Def(rs.getInt("m_def"));
				item.getLastStatus().updateAll();
				// 登入鑰匙紀錄
				if (item.getItem().getItemId() == 40312) {
					InnKeyTable.checkey(item);
				}
				items.add(item);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return items;
	}

	@Override
	public void storeItem(int objId, ItemInstance item) throws Exception {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("INSERT INTO character_items SET id = ?, item_id = ?, char_id = ?, item_name = ?, count = ?, is_equipped = 0, enchantlvl = ?, is_id = ?, durability = ?, charge_count = ?, remaining_time = ?, last_used = ?, bless = ?, attr_enchant_kind = ?, attr_enchant_level = ?,firemr = ?,watermr = ?,earthmr = ?,windmr = ?,addsp = ?,addhp = ?,addmp = ?,hpr = ?,mpr = ?, m_def = ?");
			pstm.setInt(1, item.getId());
			pstm.setInt(2, item.getItem().getItemId());
			pstm.setInt(3, objId);
			pstm.setString(4, item.getItem().getName());
			pstm.setInt(5, item.getCount());
			pstm.setInt(6, item.getEnchantLevel());
			pstm.setInt(7, item.isIdentified() ? 1 : 0);
			pstm.setInt(8, item.get_durability());
			pstm.setInt(9, item.getChargeCount());
			pstm.setInt(10, item.getRemainingTime());
			pstm.setTimestamp(11, item.getLastUsed());
			pstm.setInt(12, item.getBless());
			pstm.setInt(13, item.getAttrEnchantKind());
			pstm.setInt(14, item.getAttrEnchantLevel());
			pstm.setInt(15, item.getFireMr());
			pstm.setInt(16, item.getWaterMr());
			pstm.setInt(17, item.getEarthMr());
			pstm.setInt(18, item.getWindMr());
			pstm.setInt(19, item.getaddSp());
			pstm.setInt(20, item.getaddHp());
			pstm.setInt(21, item.getaddMp());
			pstm.setInt(22, item.getHpr());
			pstm.setInt(23, item.getMpr());
			pstm.setInt(24, item.getM_Def());
			pstm.execute();

		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		item.getLastStatus().updateAll();
	}

	@Override
	public void deleteItem(ItemInstance item) throws Exception {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("DELETE FROM character_items WHERE id = ?");
			pstm.setInt(1, item.getId());
			pstm.execute();
		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	@Override
	public void updateItemId(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET item_id = ? WHERE id = ?",
				item.getItemId());
		item.getLastStatus().updateItemId();
	}

	@Override
	public void updateItemCount(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET count = ? WHERE id = ?",
				item.getCount());
		item.getLastStatus().updateCount();
	}

	@Override
	public void updateItemDurability(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET durability = ? WHERE id = ?",
				item.get_durability());
		item.getLastStatus().updateDuraility();
	}

	@Override
	public void updateItemChargeCount(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET charge_count = ? WHERE id = ?",
				item.getChargeCount());
		item.getLastStatus().updateChargeCount();
	}

	@Override
	public void updateItemRemainingTime(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET remaining_time = ? WHERE id = ?",
				item.getRemainingTime());
		item.getLastStatus().updateRemainingTime();
	}

	@Override
	public void updateItemEnchantLevel(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET enchantlvl = ? WHERE id = ?",
				item.getEnchantLevel());
		item.getLastStatus().updateEnchantLevel();
	}

	@Override
	public void updateItemEquipped(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET is_equipped = ? WHERE id = ?",
				(item.isEquipped() ? 1 : 0));
		item.getLastStatus().updateEquipped();
	}

	@Override
	public void updateItemIdentified(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET is_id = ? WHERE id = ?",
				(item.isIdentified() ? 1 : 0));
		item.getLastStatus().updateIdentified();
	}

	@Override
	public void updateItemDelayEffect(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET last_used = ? WHERE id = ?",
				item.getLastUsed());
		item.getLastStatus().updateLastUsed();
	}

	@Override
	public void updateItemBless(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET bless = ? WHERE id = ?",
				item.getBless());
		item.getLastStatus().updateBless();
	}

	@Override
	public void updateItemAttrEnchantKind(ItemInstance item) throws Exception {
		executeUpdate(
				item.getId(),
				"UPDATE character_items SET attr_enchant_kind = ? WHERE id = ?",
				item.getAttrEnchantKind());
		item.getLastStatus().updateAttrEnchantKind();
	}

	@Override
	public void updateItemAttrEnchantLevel(ItemInstance item)
			throws Exception {
		executeUpdate(
				item.getId(),
				"UPDATE character_items SET attr_enchant_level = ? WHERE id = ?",
				item.getAttrEnchantLevel());
		item.getLastStatus().updateAttrEnchantLevel();
	}

	/** 飾品強化卷軸 */
	@Override
	public void updateFireMr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET firemr = ? WHERE id = ?",
				item.getFireMr());
		item.getLastStatus().updateFireMr();
	}

	@Override
	public void updateWaterMr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET watermr = ? WHERE id = ?",
				item.getWaterMr());
		item.getLastStatus().updateWaterMr();
	}

	@Override
	public void updateEarthMr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET earthmr = ? WHERE id = ?",
				item.getEarthMr());
		item.getLastStatus().updateEarthMr();
	}

	@Override
	public void updateWindMr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET windmr = ? WHERE id = ?",
				item.getWindMr());
		item.getLastStatus().updateWindMr();
	}

	@Override
	public void updateaddSp(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET addsp = ? WHERE id = ?",
				item.getaddSp());
		item.getLastStatus().updateSp();
	}

	@Override
	public void updateaddHp(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET addhp = ? WHERE id = ?",
				item.getaddHp());
		item.getLastStatus().updateaddHp();
	}

	@Override
	public void updateaddMp(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET addmp = ? WHERE id = ?",
				item.getaddMp());
		item.getLastStatus().updateaddMp();
	}

	@Override
	public void updateHpr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET hpr = ? WHERE id = ?",
				item.getHpr());
		item.getLastStatus().updateHpr();
	}

	@Override
	public void updateMpr(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET mpr = ? WHERE id = ?",
				item.getMpr());
		item.getLastStatus().updateMpr();
	}

	@Override
	public void updateM_Def(ItemInstance item) throws Exception {
		executeUpdate(item.getId(),
				"UPDATE character_items SET m_def = ? WHERE id = ?",
				item.getM_Def());
		item.getLastStatus().updateM_Def();
	}

	@Override
	public int getItemCount(int objId) throws Exception {
		int count = 0;
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con
					.prepareStatement("SELECT * FROM character_items WHERE char_id = ?");
			pstm.setInt(1, objId);
			rs = pstm.executeQuery();
			while (rs.next()) {
				count++;
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		return count;
	}

	private void executeUpdate(int objId, String sql, int updateNum)
			throws SQLException {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement(sql.toString());
			pstm.setInt(1, updateNum);
			pstm.setInt(2, objId);
			pstm.execute();
		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void executeUpdate(int objId, String sql, Timestamp ts)
			throws SQLException {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement(sql.toString());
			pstm.setTimestamp(1, ts);
			pstm.setInt(2, objId);
			pstm.execute();
		} catch (SQLException e) {
			throw e;
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
}
