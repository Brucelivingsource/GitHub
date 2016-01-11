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

import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_PinkName;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorPinkName

public class LsimulatorPinkName {
	private LsimulatorPinkName() {
	}

	static class PinkNameTimer implements Runnable {
		private PcInstance _attacker = null;

		public PinkNameTimer(PcInstance attacker) {
			_attacker = attacker;
		}

		@Override
		public void run() {
			for (int i = 0; i < 180; i++) {
				try {
					Thread.sleep(1000);
				}
				catch (Exception exception) {
					break;
				}
				// 死亡、または、相手を倒して赤ネームになったら終了
				if (_attacker.isDead()) {
					// setPinkName(false);はLsimulatorPcInstance#death()で行う
					break;
				}
				if (_attacker.getLawful() < 0) {
					_attacker.setPinkName(false);
					break;
				}
			}
			stopPinkName(_attacker);
		}

		private void stopPinkName(PcInstance attacker) {
			attacker.sendPackets(new S_PinkName(attacker.getId(), 0));
			attacker.broadcastPacket(new S_PinkName(attacker.getId(), 0));
			attacker.setPinkName(false);
		}
	}

	public static void onAction(PcInstance pc, LsimulatorCharacter cha) {
		if ((pc == null) || (cha == null)) {
			return;
		}

		if (!(cha instanceof PcInstance)) {
			return;
		}
		PcInstance attacker = (PcInstance) cha;
		if (pc.getId() == attacker.getId()) {
			return;
		}
		if (attacker.getFightId() == pc.getId()) {
			return;
		}

		boolean isNowWar = false;
		int castleId = LsimulatorCastleLocation.getCastleIdByArea(pc);
		if (castleId != 0) { // 旗内に居る
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}

		if ((pc.getLawful() >= 0) && // pc, attacker共に青ネーム
				!pc.isPinkName() && (attacker.getLawful() >= 0) && !attacker.isPinkName()) {
			if ((pc.getZoneType() == 0) && // 共にノーマルゾーンで、戦争時間内で旗内でない
					(attacker.getZoneType() == 0) && (  !isNowWar )) {
				attacker.setPinkName(true);
				attacker.sendPackets(new S_PinkName(attacker.getId(), 180));
				if (!attacker.isGmInvis()) {
					attacker.broadcastPacket(new S_PinkName(attacker.getId(), 180));
				}
				PinkNameTimer pink = new PinkNameTimer(attacker);
				GeneralThreadPool.getInstance().execute(pink);
			}
		}
	}
}
