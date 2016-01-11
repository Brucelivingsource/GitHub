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

import Lsimulator.server.server.Account;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.identity.LsimulatorSystemMessageId;
import Lsimulator.server.server.serverpackets.S_RetrieveElfList;
import Lsimulator.server.server.serverpackets.S_RetrieveList;
import Lsimulator.server.server.serverpackets.S_RetrievePledgeList;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

public class C_WarePassword extends ClientBasePacket {
	public C_WarePassword(byte abyte0[], ClientThread client) {
		super(abyte0);
		
		PcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		// 類型(0: 密碼變更, 1: 一般倉庫, 2: 血盟倉庫)
		int type = readC();

		// 取得第一組數值(舊密碼, 或待驗證的密碼)
		int pass1 = readD();

		// 取得第二組數值(新密碼, 或倉庫 NPC 的 objId)
		int pass2 = readD();

		// 不明的2個位元組
		readH();

		// 取得角色物件
		Account account = client.getAccount();

		// 變更密碼
		if (type == 0) {
			// 兩次皆直接跳過密碼輸入
			if ((pass1 < 0) && (pass2 < 0)) {
				pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$79));
			}

			// 進行新密碼的設定
			else if ((pass1 < 0) && (account.getWarePassword() == 0)) {
				// 進行密碼變更
				account.changeWarePassword(pass2);
				pc.sendPackets(new S_SystemMessage("倉庫密碼設定完成，請牢記您的新密碼。"));
			}

			// 進行密碼變更
			else if ((pass1 > 0) && (pass1 == account.getWarePassword())) {
				// 進行密碼變更
				if (pass1 == pass2) {
					// [342::你不能使用舊的密碼當作新的密碼。請再次輸入密碼。]
					pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$342));
					return;
				} else if (pass2 > 0) {
					account.changeWarePassword(pass2);
					pc.sendPackets(new S_SystemMessage("倉庫密碼變更完成，請牢記您的新密碼。"));
				} else {
					account.changeWarePassword(0);
					pc.sendPackets(new S_SystemMessage("倉庫密碼取消完成。"));
				}
			} else {
				// 送出密碼錯誤的提示訊息[835:密碼錯誤。]
				pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$835));
			}
		}

		// 密碼驗證
		else {
			if (account.getWarePassword() == pass1) {
				int objid = pass2;
				LsimulatorObject obj = LsimulatorWorld.getInstance().findObject(objid);
				if (pc.getLevel() >= 5) {// 判斷玩家等級
					if (type == 1) {
						if (obj != null) {
							if (obj instanceof NpcInstance) {
								NpcInstance npc = (NpcInstance) obj;
								// 判斷npc所屬倉庫類別
								switch (npc.getNpcId()) {
								case 60028:// 倉庫-艾爾(妖森)
									// 密碼吻合 輸出倉庫視窗
									if (pc.isElf())// 判斷是否為妖精
										pc.sendPackets(new S_RetrieveElfList(
												objid, pc));
									break;
								default:
									// 密碼吻合 輸出倉庫視窗
									pc.sendPackets(new S_RetrieveList(objid, pc));
									break;
								}
							}
						}
					} else if (type == 2) {
						if (pc.getClanid() == 0) {
							// \f1若想使用血盟倉庫，必須加入血盟。
							pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$208));
							return;
						}
						int rank = pc.getClanRank();
						if (rank == LsimulatorClan.CLAN_RANK_PUBLIC) {
							// 只有收到稱謂的人才能使用血盟倉庫。
							pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$728));
							return;
						}
						if ((rank != LsimulatorClan.CLAN_RANK_PROBATION) && (rank != LsimulatorClan.CLAN_RANK_GUARDIAN)
						  &&(rank != LsimulatorClan.CLAN_RANK_LEAGUE_PROBATION) && (rank != LsimulatorClan.CLAN_RANK_PRINCE) 
						  &&(rank != LsimulatorClan.CLAN_RANK_LEAGUE_VICEPRINCE) && (rank != LsimulatorClan.CLAN_RANK_LEAGUE_GUARDIAN) 
						  &&(rank != LsimulatorClan.CLAN_RANK_LEAGUE_PRINCE)) {
							// 只有收到稱謂的人才能使用血盟倉庫。
							pc.sendPackets(new S_ServerMessage(LsimulatorSystemMessageId.$728));
							return;
						}
						pc.sendPackets(new S_RetrievePledgeList(objid, pc));

					}
				}
			} else {
				// 送出密碼錯誤的提示訊息
				pc.sendPackets(new S_ServerMessage(835));
			}
		}
	}
}
