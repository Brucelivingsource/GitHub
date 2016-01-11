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

import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_Message_YN;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.utils.FaceToFace;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來加入血盟的封包
 */
public class C_JoinClan extends ClientBasePacket {

	private static final String C_JOIN_CLAN = "[C] C_JoinClan";

	public C_JoinClan(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);

		PcInstance pc = clientthread.getActiveChar();
		if ((pc == null) || pc.isGhost()) {
			return;
		}

		PcInstance target = FaceToFace.faceToFace(pc, true);
		if (target != null) {
			JoinClan(pc, target);
		}
	}

	private void JoinClan(PcInstance player, PcInstance target) {
		// 如果面對的對象不是王族或守護騎士
		if (!target.isCrown() && (target.getClanRank() != LsimulatorClan.CLAN_RANK_GUARDIAN)) {
			player.sendPackets(new S_ServerMessage(92, target.getName())); // \f1%0はプリンスやプリンセスではありません。
			return;
		}
		
		if(player.getClanid() == target.getClanid()){
			// 同一血盟
			player.sendPackets(new S_ServerMessage(1199));
			return;
		}

		int clan_id = target.getClanid();
		String clan_name = target.getClanname();
		if (clan_id == 0) { // 面對的對象沒有創立血盟
			player.sendPackets(new S_ServerMessage(90, target.getName())); // \f1%0は血盟を創設していない状態です。
			return;
		}

		LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(clan_name);
		if (clan == null) {
			return;
		}

		if (target.getClanRank() != LsimulatorClan.CLAN_RANK_PRINCE && target.getClanRank() != LsimulatorClan.CLAN_RANK_GUARDIAN 
				&& target.getClanRank() != LsimulatorClan.CLAN_RANK_LEAGUE_GUARDIAN && target.getClanRank() != LsimulatorClan.CLAN_RANK_LEAGUE_PRINCE 
				&& target.getClanRank() != LsimulatorClan.CLAN_RANK_LEAGUE_VICEPRINCE) { 
			// 面對的對象不是盟主
			player.sendPackets(new S_ServerMessage(92, target.getName()));
			return;
		}

		if (player.getClanid() != 0) { // 已經加入血盟
			if (player.isCrown()) { // 自己是盟主
				String player_clan_name = player.getClanname();
				LsimulatorClan player_clan = LsimulatorWorld.getInstance().getClan(player_clan_name);
				if (player_clan == null) {
					return;
				}

				if (player.getId() != player_clan.getLeaderId()) { // 已經加入其他血盟
					player.sendPackets(new S_ServerMessage(89)); // \f1あなたはすでに血盟に加入しています。
					return;
				}

				if ((player_clan.getCastleId() != 0) || // 有城堡或有血盟小屋
						(player_clan.getHouseId() != 0)) {
					player.sendPackets(new S_ServerMessage(665)); // \f1城やアジトを所有した状態で血盟を解散することはできません。
					return;
				}
			}
			else {
				player.sendPackets(new S_ServerMessage(89)); // \f1あなたはすでに血盟に加入しています。
				return;
			}
		}

		target.setTempID(player.getId()); // 暫時保存面對的人的ID
		target.sendPackets(new S_Message_YN(97, player.getName())); // %0が血盟に加入したがっています。承諾しますか？（Y/N）
	}

	@Override
	public String getType() {
		return C_JOIN_CLAN;
	}
}
