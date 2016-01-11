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

import static Lsimulator.server.server.model.Instance.PcInstance.REGENSTATE_MOVE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ABSOLUTE_BARRIER;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.MEDITATION;
import Lsimulator.server.Config;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.model.AcceleratorChecker;
import Lsimulator.server.server.model.Dungeon;
import Lsimulator.server.server.model.DungeonRandom;
import Lsimulator.server.server.model.LsimulatorTrade;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.trap.LsimulatorWorldTraps;
import Lsimulator.server.server.serverpackets.S_MoveCharPacket;
import Lsimulator.server.server.serverpackets.S_SystemMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來移動角色的封包
 */
public class C_MoveChar extends ClientBasePacket {

	private static final byte HEADING_TABLE_X[] =
	{ 0, 1, 1, 1, 0, -1, -1, -1 };

	private static final byte HEADING_TABLE_Y[] =
	{ -1, -1, 0, 1, 1, 1, 0, -1 };

	private static final int CLIENT_LANGUAGE = Config.CLIENT_LANGUAGE;

	// 地圖編號的研究
	@SuppressWarnings("unused")
	private void sendMapTileLog(PcInstance pc) {
		pc.sendPackets(new S_SystemMessage(pc.getMap().toString(pc.getLocation())));
	}

	// 移動
	public C_MoveChar(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);
		
		PcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isTeleport()) { // 傳送中
			return;
		}
		
		int locx = readH();
		int locy = readH();
		int heading = readC();

		// 檢查移動的時間間隔
		if (Config.CHECK_MOVE_INTERVAL) {
			int result;
			result = pc.getAcceleratorChecker().checkInterval(AcceleratorChecker.ACT_TYPE.MOVE);
			if (result == AcceleratorChecker.R_DISPOSED) {
				return;
			}
		}
		
		// 移動中, 取消交易
	    if (pc.getTradeID() != 0) {
	    	LsimulatorTrade trade = new LsimulatorTrade();
	        trade.TradeCancel(pc);
	    }

		if (pc.hasSkillEffect(MEDITATION)) { // 取消冥想效果
			pc.removeSkillEffect(MEDITATION);
		}
		pc.setCallClanId(0); // コールクランを唱えた後に移動すると召喚無効

		if (!pc.hasSkillEffect(ABSOLUTE_BARRIER)) { // 絕對屏障
			pc.setRegenState(REGENSTATE_MOVE);
		}
		pc.getMap().setPassable(pc.getLocation(), true);

		if (CLIENT_LANGUAGE == 3) { // Taiwan Only
			heading ^= 0x49;
			locx = pc.getX();
			locy = pc.getY();
		}

		locx += HEADING_TABLE_X[heading];
		locy += HEADING_TABLE_Y[heading];

		if (Dungeon.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // 傳點
			return;
		}
		if (DungeonRandom.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // 取得隨機傳送地點
			return;
		}

		pc.getLocation().set(locx, locy);
		pc.setHeading(heading);
		if (pc.isGmInvis() || pc.isGhost()) {}
		else if (pc.isInvisble()) {
			pc.broadcastPacketForFindInvis(new S_MoveCharPacket(pc), true);
		}
		else {
			pc.broadcastPacket(new S_MoveCharPacket(pc));
		}

		// sendMapTileLog(pc); //發送信息的目的地瓦（為調查地圖）
		// 寵物競速-判斷圈數
		Lsimulator.server.server.model.game.LsimulatorPolyRace.getInstance().checkLapFinish(pc);
		LsimulatorWorldTraps.getInstance().onPlayerMoved(pc);

		pc.getMap().setPassable(pc.getLocation(), false);
		// user.UpdateObject(); // 可視範囲内の全オブジェクト更新
	}
}