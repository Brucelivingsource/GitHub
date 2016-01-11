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

import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_CharTitle;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來變更稱號的封包
 */
public class C_Title extends ClientBasePacket {

	private static final String C_TITLE = "[C] C_Title";
	private static Logger _log = Logger.getLogger(C_Title.class.getName());

	public C_Title(byte abyte0[], ClientThread clientthread) {
		super(abyte0);
		
		PcInstance pc = clientthread.getActiveChar();
		if (pc == null) {
			return;
		}
		
		String charName = readS();
		String title = readS();

		if (charName.isEmpty() || title.isEmpty()) {
			// \f1次のように入力してください：「/title \f0キャラクター名 呼称\f1」
			pc.sendPackets(new S_ServerMessage(196));
			return;
		}
		PcInstance target = LsimulatorWorld.getInstance().getPlayer(charName);
		if (target == null) {
			return;
		}

		if (pc.isGm()) {
			changeTitle(target, title);
			return;
		}

		if (isClanLeader(pc)) { // 血盟主
			if (pc.getId() == target.getId()) { // 自己
				if (pc.getLevel() < 10) {
					// \f1血盟員の場合、呼称を持つにはレベル10以上でなければなりません。
					pc.sendPackets(new S_ServerMessage(197));
					return;
				}
				changeTitle(pc, title);
			} else { // 他人
				if (pc.getClanid() != target.getClanid()) {
					// \f1血盟員でなければ他人に呼称を与えることはできません。
					pc.sendPackets(new S_ServerMessage(199));
					return;
				}
				if (target.getLevel() < 10) {
					// \f1%0のレベルが10未満なので呼称を与えることはできません。
					pc.sendPackets(new S_ServerMessage(202, charName));
					return;
				}
				changeTitle(target, title);
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					for (PcInstance clanPc : clan.getOnlineClanMember()) {
						// \f1%0が%1に「%2」という呼称を与えました。
						clanPc.sendPackets(new S_ServerMessage(203, pc
								.getName(), charName, title));
					}
				}
			}
		} else {
			if (pc.getId() == target.getId()) { // 自分
				if (pc.getClanid() != 0 && !Config.CHANGE_TITLE_BY_ONESELF) {
					// \f1血盟員に呼称を与えられるのはプリンスとプリンセスだけです。
					pc.sendPackets(new S_ServerMessage(198));
					return;
				}
				if (target.getLevel() < 40) {
					// \f1血盟員ではないのに呼称を持つには、レベル40以上でなければなりません。
					pc.sendPackets(new S_ServerMessage(200));
					return;
				}
				changeTitle(pc, title);
			} else { // 他人
				if (pc.isCrown()) { // 連合に所属した君主
					if (pc.getClanid() == target.getClanid()) {
						// \f1%0はあなたの血盟ではありません。
						pc.sendPackets(new S_ServerMessage(201, pc
								.getClanname()));
						return;
					}
				}
			}
		}
	}

	private void changeTitle(PcInstance pc, String title) {
		int objectId = pc.getId();
		pc.setTitle(title);
		pc.sendPackets(new S_CharTitle(objectId, title));
		pc.broadcastPacket(new S_CharTitle(objectId, title));
		try {
			pc.save(); // 儲存玩家的資料到資料庫中
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	private boolean isClanLeader(PcInstance pc) {
		boolean isClanLeader = false;
		if (pc.getClanid() != 0) { // 有血盟
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if (pc.isCrown() && pc.getId() == clan.getLeaderId()) { // 君主、かつ、血盟主
					isClanLeader = true;
				}
			}
		}
		return isClanLeader;
	}

	@Override
	public String getType() {
		return C_TITLE;
	}

}
