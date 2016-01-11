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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SHAPE_CHANGE;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_DoActionGFX;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來額外動作指令的封包
 */
public class C_ExtraCommand extends ClientBasePacket {
	private static final String C_EXTRA_COMMAND = "[C] C_ExtraCommand";

	public C_ExtraCommand(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		
		PcInstance pc = client.getActiveChar();
		if (pc == null) {
			return;
		}
		
		int actionId = readC();
		if (pc.isGhost()) {
			return;
		}
		if (pc.isInvisble()) { // 隱形中
			return;
		}
		if (pc.isTeleport()) { // 傳送中
			return;
		}
		if (pc.hasSkillEffect(SHAPE_CHANGE)) { // 變深中
			int gfxId = pc.getTempCharGfx();
			if ((gfxId != 6080) && (gfxId != 6094)) { // 騎馬用的變身例外
				return;
			}
		}
		S_DoActionGFX gfx = new S_DoActionGFX(pc.getId(), actionId);
		pc.broadcastPacket(gfx); // 將動作送給附近的玩家
	}

	@Override
	public String getType() {
		return C_EXTRA_COMMAND;
	}
}
