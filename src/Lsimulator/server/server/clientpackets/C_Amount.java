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
package Lsimulator.server.server.clientpackets;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

import Lsimulator.server.Config;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.datatables.AuctionBoardTable;
import Lsimulator.server.server.datatables.HouseTable;
import Lsimulator.server.server.datatables.InnKeyTable;
import Lsimulator.server.server.datatables.InnTable;
import Lsimulator.server.server.datatables.ItemTable;
import Lsimulator.server.server.datatables.NpcActionTable;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.identity.LsimulatorItemId;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;
import Lsimulator.server.server.model.npc.action.LsimulatorNpcAction;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.storage.CharactersItemStorage;
import Lsimulator.server.server.templates.LsimulatorAuctionBoard;
import Lsimulator.server.server.templates.LsimulatorHouse;
import Lsimulator.server.server.templates.LsimulatorInn;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket, C_Amount

/**
 * 處理客戶端傳來拍賣的封包
 */
public class C_Amount extends ClientBasePacket {

	private static final String C_AMOUNT = "[C] C_Amount";

	public C_Amount(byte[] decrypt, ClientThread client) throws Exception {
		super(decrypt);
		
		PcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		int objectId = readD();
		int amount = readD();
		readC();
		String s = readS();

		
		NpcInstance npc = (NpcInstance) LsimulatorWorld.getInstance().findObject(objectId);
		if (npc == null) {
			return;
		}

		String s1 = "";
		String s2 = "";
		try {
			StringTokenizer stringtokenizer = new StringTokenizer(s);
			s1 = stringtokenizer.nextToken();
			s2 = stringtokenizer.nextToken();
		} catch (NoSuchElementException e) {
			s1 = "";
			s2 = "";
		}
		if (s1.equalsIgnoreCase("agapply")) { // 如果你在拍賣競標
			String pcName = pc.getName();
			AuctionBoardTable boardTable = new AuctionBoardTable();
			for (LsimulatorAuctionBoard board : boardTable.getAuctionBoardTableList()) {
				if (pcName.equalsIgnoreCase(board.getBidder())) {
					pc.sendPackets(new S_ServerMessage(523)); // すでに他の家の競売に参加しています。
					return;
				}
			}
			int houseId = Integer.valueOf(s2);
			LsimulatorAuctionBoard board = boardTable.getAuctionBoardTable(houseId);
			if (board != null) {
				int nowPrice = board.getPrice();
				int nowBidderId = board.getBidderId();
				if (pc.getInventory().consumeItem(LsimulatorItemId.ADENA, amount)) {
					// 更新拍賣公告
					board.setPrice(amount);
					board.setBidder(pcName);
					board.setBidderId(pc.getId());
					boardTable.updateAuctionBoard(board);
					if (nowBidderId != 0) {
						// 將金幣退還給投標者
						PcInstance bidPc = (PcInstance) LsimulatorWorld.getInstance().findObject(nowBidderId);
						if (bidPc != null) { // 玩家在線上
							bidPc.getInventory().storeItem(LsimulatorItemId.ADENA,
									nowPrice);
							// あなたが提示された金額よりももっと高い金額を提示した方が現れたため、残念ながら入札に失敗しました。%n
							// あなたが競売に預けた%0アデナをお返しします。%nありがとうございました。%n%n
							bidPc.sendPackets(new S_ServerMessage(525, String
									.valueOf(nowPrice)));
						} else { // 玩家離線中
							ItemInstance item = ItemTable.getInstance().createItem(LsimulatorItemId.ADENA);
							item.setCount(nowPrice);
							CharactersItemStorage storage = CharactersItemStorage.create();
							storage.storeItem(nowBidderId, item);
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(189)); // \f1アデナが不足しています。
				}
			}
		} else if (s1.equalsIgnoreCase("agsell")) { // 出售盟屋
			int houseId = Integer.valueOf(s2);
			AuctionBoardTable boardTable = new AuctionBoardTable();
			LsimulatorAuctionBoard board = new LsimulatorAuctionBoard();
			if (board != null) {
				// 新增拍賣公告到拍賣板
				board.setHouseId(houseId);
				LsimulatorHouse house = HouseTable.getInstance().getHouseTable(houseId);
				board.setHouseName(house.getHouseName());
				board.setHouseArea(house.getHouseArea());
				TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
				Calendar cal = Calendar.getInstance(tz);
				cal.add(Calendar.DATE, 5); // 5天後
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				board.setDeadline(cal);
				board.setPrice(amount);
				board.setLocation(house.getLocation());
				board.setOldOwner(pc.getName());
				board.setOldOwnerId(pc.getId());
				board.setBidder("");
				board.setBidderId(0);
				boardTable.insertAuctionBoard(board);

				house.setOnSale(true); // 設定盟屋為拍賣中
				house.setPurchaseBasement(true); // 地下アジト未購入に設定
				HouseTable.getInstance().updateHouse(house); // 更新到資料庫中
			}
		} else {
			// 旅館NPC
			int npcId = npc.getNpcId();
			if (npcId == 70070 || npcId == 70019 || npcId == 70075 || npcId == 70012
			 || npcId == 70031 || npcId == 70084 || npcId == 70065 || npcId == 70054 || npcId == 70096) {

				if (pc.getInventory().checkItem(LsimulatorItemId.ADENA, (300 * amount))) { // 所需金幣 = 鑰匙價格(300) * 鑰匙數量(amount)
					LsimulatorInn inn = InnTable.getInstance().getTemplate(npcId, pc.getInnRoomNumber());
					if (inn != null) {
						Timestamp dueTime = inn.getDueTime();
						if (dueTime != null) { // 再次判斷房間租用時間
							Calendar cal = Calendar.getInstance();
							if (((cal.getTimeInMillis() - dueTime.getTime()) / 1000) < 0) { // 租用時間未到
								// 此房間被搶走了...
								pc.sendPackets(new S_NPCTalkReturn(npcId, ""));
								return;
							}
						}
						// 租用時間 4小時
						Timestamp ts = new Timestamp(System.currentTimeMillis() + ( (60 * 60 * 1000) << 2)  );
						// 登入旅館資料
						ItemInstance item = ItemTable.getInstance().createItem(40312); // 旅館鑰匙
						if (item != null) {
							item.setKeyId(item.getId()); // 鑰匙編號
							item.setInnNpcId(npcId); // 旅館NPC
							item.setHall(pc.checkRoomOrHall()); // 判斷租房間 or 會議室
							item.setDueTime(ts); // 租用時間
							item.setCount(amount); // 鑰匙數量

							inn.setKeyId(item.getKeyId()); // 旅館鑰匙
							inn.setLodgerId(pc.getId()); // 租用人
							inn.setHall(pc.checkRoomOrHall()); // 判斷租房間 or 會議室
							inn.setDueTime(ts); // 租用時間
							// DB更新
							InnTable.getInstance().updateInn(inn);

							pc.getInventory().consumeItem(LsimulatorItemId.ADENA, (300 * amount)); // 扣除金幣

							// 給予鑰匙並登入鑰匙資料
							LsimulatorInventory inventory;
							if (pc.getInventory().checkAddItem(item, amount) == LsimulatorInventory.OK) {
								inventory = pc.getInventory();
							} else {
								inventory = LsimulatorWorld.getInstance().getInventory(pc.getLocation());
							}
							inventory.storeItem(item);

							if (InnKeyTable.checkey(item)) {
								InnKeyTable.DeleteKey(item);
								InnKeyTable.StoreKey(item);
							} else {
								InnKeyTable.StoreKey(item);
							}

							String itemName = (item.getItem().getName() + item.getInnKeyName());
							if (amount > 1) {
								itemName = (itemName + " (" + amount + ")");
							}
							pc.sendPackets(new S_ServerMessage(143, npc.getName(), itemName)); // \f1%0%s 給你 %1%o 。
							String[] msg = {npc.getName()};
							pc.sendPackets(new S_NPCTalkReturn(npcId, "inn4", msg)); // 要一起使用房間的話，請把鑰匙給其他人，往旁邊的樓梯上去即可。
						}
					}
				}
				else {
					String[] msg = {npc.getName()};
					pc.sendPackets(new S_NPCTalkReturn(npcId, "inn3", msg)); // 對不起，你手中的金幣不夠哦！
				}
			} else {
				LsimulatorNpcAction action = NpcActionTable.getInstance().get(s, pc, npc);
				if (action != null) {
					LsimulatorNpcHtml result = action.executeWithAmount(s, pc, npc, amount);
					if (result != null) {
						pc.sendPackets(new S_NPCTalkReturn(npcId, result));
					}
					return;
				}
			}
		}
	}

	@Override
	public String getType() {
		return C_AMOUNT;
	}
}
