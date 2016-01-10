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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.utils.SQLUtil;

/**
 * 負責物品狀態檢查是否作弊
 */
public class LsimulatorItemCheck {
	private int itemId;
	private boolean isStackable = false;

	public boolean ItemCheck(LsimulatorItemInstance item, LsimulatorPcInstance pc) {
		itemId = item.getItem().getItemId();
		int itemCount = item.getCount();
		boolean isCheat = false;

		if ((findWeapon() || findArmor()) && itemCount != 1) {
			isCheat = true;
		} else if (findEtcItem()) {
			// 不可堆疊的道具卻堆疊，就視為作弊
			if (!isStackable && itemCount != 1) {
				isCheat = true;
				// 金幣大於20億以及金幣負值則為作弊
			} else if (itemId == LsimulatorItemId.ADENA
					&& (itemCount > 2000000000 || itemCount < 0)) {
				isCheat = true;
				// 可堆疊道具(金幣除外)堆疊超過十萬個以及堆疊負值設定為作弊
			} else if (isStackable && itemId != LsimulatorItemId.ADENA
					&& (itemCount > 100000 || itemCount < 0)) {
				isCheat = true;
			}
		}
		if (isCheat) {
			// 作弊直接刪除物品
			pc.getInventory().removeItem(item, itemCount);
		}
		return isCheat;
	}

	private boolean findWeapon() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		boolean inWeapon = false;

		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM weapon WHERE item_id = ?");
			pstm.setInt(1, itemId);
			rs = pstm.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					inWeapon = true;
				}
			}
		} catch (Exception e) {
		} finally {
			SQLUtil.close(rs, pstm, con);
		}
		return inWeapon;
	}

	private boolean findArmor() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		boolean inArmor = false;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM armor WHERE item_id = ?");
			pstm.setInt(1, itemId);
			rs = pstm.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					inArmor = true;
				}
			}
		} catch (Exception e) {
		} finally {
			SQLUtil.close(rs, pstm, con);
		}
		return inArmor;
	}

	private boolean findEtcItem() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		boolean inEtcitem = false;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM etcitem WHERE item_id = ?");
			pstm.setInt(1, itemId);
			rs = pstm.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					inEtcitem = true;
					isStackable = rs.getInt("stackable") == 1 ? true : false;
				}
			}
		} catch (Exception e) {
		} finally {
			SQLUtil.close(rs, pstm, con);
		}
		return inEtcitem;
	}
}