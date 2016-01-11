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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AREA_OF_SILENCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SILENCE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_POISON_SILENCE;
import Lsimulator.server.Config;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.GMCommands;
import Lsimulator.server.server.Opcodes;
import Lsimulator.server.server.datatables.ChatLogTable;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ChatPacket;
import Lsimulator.server.server.serverpackets.S_NpcChatPacket;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來的聊天封包
 */
public class C_Chat extends ClientBasePacket {

	private static final String C_CHAT = "[C] C_Chat";

	public C_Chat(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		
		PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}
		
		int chatType = readC();
		String chatText = readS();
		if (pc.hasSkillEffect(SILENCE) || pc.hasSkillEffect(AREA_OF_SILENCE) || pc.hasSkillEffect(STATUS_POISON_SILENCE)) {
			return;
		}
		if (pc.hasSkillEffect(1005)) { // 被魔封
			pc.sendPackets(new S_ServerMessage(242)); // 你從現在被禁止閒談。
			return;
		}

		if (chatType == 0) { // 一般聊天
			if (pc.isGhost() && !(pc.isGm() || pc.isMonitor())) {
				return;
			}
			// GM指令
                                                       // charAt比較快
			if (chatText.charAt(0) == '.'  && (pc.isGm() || pc.isMonitor())) {
				String cmd = chatText.substring(1);
				GMCommands.getInstance().handleCommands(pc, cmd);
				return;
			}

			// 交易頻道
			// 本来はchatType==12になるはずだが、行頭の$が送信されない
			if (chatText.charAt(0) == '$' ) {
				String text = chatText.substring(1);
				chatWorld(pc, text, 12);
				if (!pc.isGm()) {
					pc.checkChatInterval();
				}
				return;
			}

			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,Opcodes.S_OPCODE_NORMALCHAT, 0);
			if (!pc.getExcludingList().contains(pc.getName())) {
				pc.sendPackets(s_chatpacket);
			}
			for (PcInstance listner : LsimulatorWorld.getInstance().getRecognizePlayer(pc)) {
				if (listner.getMapId() < 16384 || listner.getMapId() > 25088 || listner.getInnKeyId() == pc.getInnKeyId()) // 旅館內判斷
					if (!listner.getExcludingList().contains(pc.getName()))
						listner.sendPackets(s_chatpacket);
			}
			// 怪物模仿
			for (LsimulatorObject obj : pc.getKnownObjects()) {
				if (obj instanceof MonsterInstance) {
					MonsterInstance mob = (MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName()) && !mob.isDead()) {
						mob.broadcastPacket(new S_NpcChatPacket(mob, chatText, 0));
					}
				}
			}
		} else if (chatType == 2) { // 喊叫
			if (pc.isGhost()) {
				return;
			}
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,Opcodes.S_OPCODE_NORMALCHAT, 2);
			if (!pc.getExcludingList().contains(pc.getName())) {
				pc.sendPackets(s_chatpacket);
			}
			for (PcInstance listner : LsimulatorWorld.getInstance().getVisiblePlayer(pc, 50)) {
				if (listner.getMapId() < 16384 || listner.getMapId() > 25088
						|| listner.getInnKeyId() == pc.getInnKeyId()) // 旅館內判斷
					if (!listner.getExcludingList().contains(pc.getName()))
						listner.sendPackets(s_chatpacket);
			}

			// 怪物模仿
			for (LsimulatorObject obj : pc.getKnownObjects()) {
				if (obj instanceof MonsterInstance) {
					MonsterInstance mob = (MonsterInstance) obj;
					if (mob.getNpcTemplate().is_doppel() && mob.getName().equals(pc.getName())&& !mob.isDead()) {
						for (PcInstance listner : LsimulatorWorld.getInstance().getVisiblePlayer(mob, 50)) {
							listner.sendPackets(new S_NpcChatPacket(mob, chatText, 2));
						}
					}
				}
			}
		} else if (chatType == 3) { // 全體聊天
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 4) { // 血盟聊天
			if (pc.getClanid() != 0) { // 所屬血盟
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
				if ((clan != null)) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText,chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, 4);
					PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (PcInstance listner : clanMembers) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							if (listner.isShowClanChat() && chatType == 4)// 血盟
								listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 11) { // 組隊聊天
			if (pc.isInParty()) { // 組隊中
				ChatLogTable.getInstance().storeChat(pc, null, chatText,chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,Opcodes.S_OPCODE_GLOBALCHAT, 11);
				PcInstance[] partyMembers = pc.getParty().getMembers();
				for (PcInstance listner : partyMembers) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						if (listner.isShowPartyChat() && chatType == 11)// 組隊
							listner.sendPackets(s_chatpacket);
					}
				}
			}
		} else if (chatType == 12) { // 交易聊天
			chatWorld(pc, chatText, chatType);
		} else if (chatType == 13) { // 聯合血盟
			if (pc.getClanid() != 0) { // 在血盟中
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
				int rank = pc.getClanRank();
				if ((clan != null) && ((rank == LsimulatorClan.CLAN_RANK_GUARDIAN) || (rank == LsimulatorClan.CLAN_RANK_LEAGUE_PRINCE)  || (rank == LsimulatorClan.CLAN_RANK_LEAGUE_VICEPRINCE) ||(rank == LsimulatorClan.CLAN_RANK_LEAGUE_GUARDIAN))) {
					ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
					S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, 13);
					PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (PcInstance listner : clanMembers) {
						int listnerRank = listner.getClanRank();
						if (!listner.getExcludingList().contains(pc.getName()) && ((listnerRank == LsimulatorClan.CLAN_RANK_GUARDIAN) || (listnerRank == LsimulatorClan.CLAN_RANK_LEAGUE_PRINCE) || (rank == LsimulatorClan.CLAN_RANK_LEAGUE_VICEPRINCE)||(rank == LsimulatorClan.CLAN_RANK_LEAGUE_GUARDIAN))) {
							listner.sendPackets(s_chatpacket);
						}
					}
				}
			}
		} else if (chatType == 14) { // 聊天組隊
			if (pc.isInChatParty()) { // 聊天組隊
				ChatLogTable.getInstance().storeChat(pc, null, chatText,chatType);
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText,Opcodes.S_OPCODE_NORMALCHAT, 14);
				PcInstance[] partyMembers = pc.getChatParty().getMembers();
				for (PcInstance listner : partyMembers) {
					if (!listner.getExcludingList().contains(pc.getName())) {
						listner.sendPackets(s_chatpacket);
					}
				}
			}
		} else if(chatType == 17){ // 血盟王族公告頻道
			if(pc.getClanRank() == 10 || pc.getClanRank() == 4){
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
				S_ChatPacket s_chatpacket = new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, 17);
				PcInstance[] clanMembers = clan.getOnlineClanMember();
				for (PcInstance listner : clanMembers) {
					listner.sendPackets(s_chatpacket);
				}
			}
		}
		if (!pc.isGm()) {
			pc.checkChatInterval();
		}
	}

	private void chatWorld(PcInstance pc, String chatText, int chatType) {
		if (pc.isGm()) {
			ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
			LsimulatorWorld.getInstance().broadcastPacketToAll(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT,chatType));
		} else if (pc.getLevel() >= Config.GLOBAL_CHAT_LEVEL) {
			if (LsimulatorWorld.getInstance().isWorldChatElabled()) {
				if (pc.get_food() >= 6) {
					pc.set_food(pc.get_food() - 5);
					ChatLogTable.getInstance().storeChat(pc, null, chatText, chatType);
					pc.sendPackets(new S_PacketBox(S_PacketBox.FOOD, pc.get_food()));
					for (PcInstance listner : LsimulatorWorld.getInstance().getAllPlayers()) {
						if (!listner.getExcludingList().contains(pc.getName())) {
							if (listner.isShowTradeChat() && (chatType == 12)) {
								listner.sendPackets(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, chatType));
							} else if (listner.isShowWorldChat() && (chatType == 3)) {
								listner.sendPackets(new S_ChatPacket(pc, chatText, Opcodes.S_OPCODE_GLOBALCHAT, chatType));
							}
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(462)); // 你太過於饑餓以致於無法談話。
				}
			} else {
				pc.sendPackets(new S_ServerMessage(510)); // 現在ワールドチャットは停止中となっております。しばらくの間ご了承くださいませ。
			}
		} else {
			// 等級 以下的角色無法使用公頻或買賣頻道。
			pc.sendPackets(new S_ServerMessage(195, String.valueOf(Config.GLOBAL_CHAT_LEVEL)));
		}
	}

	@Override
	public String getType() {
		return C_CHAT;
	}
}
