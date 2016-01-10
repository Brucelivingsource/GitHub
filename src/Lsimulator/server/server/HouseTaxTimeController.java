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
package Lsimulator.server.server;

import java.util.Calendar;
import java.util.TimeZone;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.AuctionBoardTable;
import Lsimulator.server.server.datatables.ClanTable;
import Lsimulator.server.server.datatables.HouseTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.templates.LsimulatorAuctionBoard;
import Lsimulator.server.server.templates.LsimulatorHouse;

public class HouseTaxTimeController implements Runnable {
	private static HouseTaxTimeController _instance;

	public static HouseTaxTimeController getInstance() {
		if (_instance == null) {
			_instance = new HouseTaxTimeController();
		}
		return _instance;
	}

	@Override
	public void run() {
		try {
			while (true) {
				checkTaxDeadline();
				Thread.sleep(600000);
			}
		} catch (Exception e1) {
		}
	}

	public Calendar getRealTime() {
		TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		Calendar cal = Calendar.getInstance(tz);
		return cal;
	}

	private void checkTaxDeadline() {
		for (LsimulatorHouse house : HouseTable.getInstance().getHouseTableList()) {
			if (!house.isOnSale()) { // 不檢查再拍賣的血盟小屋
				if (house.getTaxDeadline().before(getRealTime())) {
					sellHouse(house);
				}
			}
		}
	}

	private void sellHouse(LsimulatorHouse house) {
		AuctionBoardTable boardTable = new AuctionBoardTable();
		LsimulatorAuctionBoard board = new LsimulatorAuctionBoard();
		if (board != null) {
			// 在拍賣板張貼新公告
			int houseId = house.getHouseId();
			board.setHouseId(houseId);
			board.setHouseName(house.getHouseName());
			board.setHouseArea(house.getHouseArea());
			TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
			Calendar cal = Calendar.getInstance(tz);
			cal.add(Calendar.DATE, 5); // 5天以後
			cal.set(Calendar.MINUTE, 0); //
			cal.set(Calendar.SECOND, 0);
			board.setDeadline(cal);
			board.setPrice(100000);
			board.setLocation(house.getLocation());
			board.setOldOwner("");
			board.setOldOwnerId(0);
			board.setBidder("");
			board.setBidderId(0);
			boardTable.insertAuctionBoard(board);
			house.setOnSale(true); // 設定為拍賣中
			house.setPurchaseBasement(true); // TODO: 翻譯 地下アジト未購入に設定
			cal.add(Calendar.DATE, Config.HOUSE_TAX_INTERVAL);
			house.setTaxDeadline(cal);
			HouseTable.getInstance().updateHouse(house); // 儲存到資料庫中
			// 取消之前的擁有者
			for (LsimulatorClan clan : LsimulatorWorld.getInstance().getAllClans()) {
				if (clan.getHouseId() == houseId) {
					clan.setHouseId(0);
					ClanTable.getInstance().updateClan(clan);
				}
			}
		}
	}

}
