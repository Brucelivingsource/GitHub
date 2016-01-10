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

import java.io.UnsupportedEncodingException;
import java.util.List;

import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorPolyMorph;
import Lsimulator.server.server.model.Instance.LsimulatorDollInstance;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.identity.LsimulatorSystemMessageId;
import Lsimulator.server.server.serverpackets.S_ChangeShape;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;
import Lsimulator.server.server.serverpackets.S_DoActionShop;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorPrivateShopBuyList;
import Lsimulator.server.server.templates.LsimulatorPrivateShopSellList;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來商店的封包
 */
public class C_Shop extends ClientBasePacket {

	private static final String C_SHOP = "[C] C_Shop";

	/**
	 * 『來源:客戶端』<位址:38>{長度:36}(時間:-465193548)
        0000: 26 00 01 00 d4 b3 75 00 05 00 00 00 01 00 00 00 &.....u.........
        0010: 00 00 35 35 ff 00 74 72 61 64 65 7a 6f 6e 65 31 ..55..tradezone1
        0020: 00 08 50 57 ..PW
	 */
	public C_Shop(byte abyte0[], ClientThread clientthread) {
		super(abyte0);

		LsimulatorPcInstance pc = clientthread.getActiveChar();
		if ((pc == null) || pc.isGhost()) {
			return;
		}

		int mapId = pc.getMapId();
		if ((mapId != 340) && (mapId != 350) && (mapId != 360) && (mapId != 370) && (mapId != 800) ) {
			pc.sendPackets(new S_ServerMessage(876)); // この場所では個人商店を開けません。
			return;
		}

		List<LsimulatorPrivateShopSellList> sellList = pc.getSellList();
		List<LsimulatorPrivateShopBuyList> buyList = pc.getBuyList();
		LsimulatorItemInstance checkItem;
		boolean tradable = true;

		int type = readC();
		if (type == 0) { // 開始
			int sellTotalCount = readH();
			int sellObjectId;
			int sellPrice;
			int sellCount;
			for (int i = 0; i < sellTotalCount; i++) {
				sellObjectId = readD();
				sellPrice = readD();
				sellCount = readD();
				// 檢查交易項目
				checkItem = pc.getInventory().getItem(sellObjectId);
				if (!checkItem.getItem().isTradable()) {
					tradable = false;
					pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$166, checkItem.getItem().getName(), "這是不可能處理。"));
				}
				for (LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
					if (petNpc instanceof LsimulatorPetInstance) {
						LsimulatorPetInstance pet = (LsimulatorPetInstance) petNpc;
						if (checkItem.getId() == pet.getItemObjId()) {
							tradable = false;
							pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$166, checkItem.getItem().getName(), "這是不可能處理。"));
							break;
						}
					}
				}
				LsimulatorPrivateShopSellList pssl = new LsimulatorPrivateShopSellList();
				pssl.setItemObjectId(sellObjectId);
				pssl.setSellPrice(sellPrice);
				pssl.setSellTotalCount(sellCount);
				sellList.add(pssl);
			}
			int buyTotalCount = readH();
			int buyObjectId;
			int buyPrice;
			int buyCount;
			for (int i = 0; i < buyTotalCount; i++) {
				buyObjectId = readD();
				buyPrice = readD();
				buyCount = readD();
				// 檢查交易項目
				checkItem = pc.getInventory().getItem(buyObjectId);
				if (!checkItem.getItem().isTradable()) {
					tradable = false;
					pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$166, checkItem.getItem().getName(), "這是不可能處理。"));
				}
				if (checkItem.getBless() >= 128) { // 封印的裝備
					pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$210, checkItem.getItem().getName()));
					return;
				}
				// 防止異常堆疊交易
				if ((checkItem.getCount() > 1) && (!checkItem.getItem().isStackable())) {
					pc.sendPackets(new S_SystemMessage("此物品非堆疊，但異常堆疊無法交易。"));
					return;
				}

				// 使用中的寵物項鍊 - 無法販賣
				for (LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
					if (petNpc instanceof LsimulatorPetInstance) {
						LsimulatorPetInstance pet = (LsimulatorPetInstance) petNpc;
						if (checkItem.getId() == pet.getItemObjId()) {
							tradable = false;
							pc.sendPackets(new S_ServerMessage(1187)); // 寵物項鍊正在使用中。
							break;
						}
					}
				}
				// 使用中的魔法娃娃 - 無法販賣
				for (LsimulatorDollInstance doll : pc.getDollList().values()) {
					if (doll.getItemObjId() == checkItem.getId()) {
						tradable = false;
						pc.sendPackets(new S_ServerMessage(1181));
						break;
					}
				}
				LsimulatorPrivateShopBuyList psbl = new LsimulatorPrivateShopBuyList();
				psbl.setItemObjectId(buyObjectId);
				psbl.setBuyPrice(buyPrice);
				psbl.setBuyTotalCount(buyCount);
				buyList.add(psbl);
			}
			if (!tradable) { // 如果項目不包括在交易結束零售商
				sellList.clear();
				buyList.clear();
				pc.setPrivateShop(false);
				pc.sendPackets(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
				pc.broadcastPacket(new S_DoActionGFX(pc.getId(), ActionCodes.ACTION_Idle));
				return;
			}
			byte[] chat = readByte();
			pc.setShopChat(chat);
			pc.setPrivateShop(true);
			pc.sendPackets(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, chat));
			pc.broadcastPacket(new S_DoActionShop(pc.getId(),ActionCodes.ACTION_Shop, chat));
			//// 3.80C 個人商店變身  
			int SelectedPolyNum = 0;
			try {
				SelectedPolyNum = Integer.parseInt(new String(chat, "utf8").split("tradezone")[1].substring(0, 1));
			} catch (Exception e) {
				e.printStackTrace();
			}
			LsimulatorPolyMorph.doPolyPraivateShop(pc, SelectedPolyNum);
		} else if (type == 1) { // 終了
			sellList.clear();
			buyList.clear();
			pc.setPrivateShop(false);
			pc.sendPackets(new S_DoActionGFX(pc.getId(),ActionCodes.ACTION_Idle));
			pc.broadcastPacket(new S_DoActionGFX(pc.getId(),ActionCodes.ACTION_Idle));
			LsimulatorPolyMorph.undoPolyPrivateShop(pc); // 取消變身
		}
	}

	@Override
	public String getType() {
		return C_SHOP;
	}

}
