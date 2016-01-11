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
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.MonsterInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.model.Instance.SummonInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來選擇目標的封包
 */
public class C_SelectTarget extends ClientBasePacket {

	private static final String C_SELECT_TARGET = "[C] C_SelectTarget";

	public C_SelectTarget(byte abyte0[], ClientThread clientthread) throws Exception {
		super(abyte0);

		int petId = readD();
		readC();
		int targetId = readD();

		PetInstance pet = (PetInstance) LsimulatorWorld.getInstance().findObject(petId);
		LsimulatorCharacter target = (LsimulatorCharacter) LsimulatorWorld.getInstance().findObject(targetId);

		if ((pet != null) && (target != null)) {
			// 目標為玩家
			if (target instanceof PcInstance) {
				PcInstance pc = (PcInstance) target;
				// 目標在安區、攻擊者在安區、NOPVP
				if ((pc.getZoneType() == 1) || (pet.getZoneType() == 1) || (pc.checkNonPvP(pc, pet))) {
					// 寵物主人
					if (pet.getMaster() instanceof PcInstance) {
						PcInstance petMaster = (PcInstance) pet.getMaster();
						petMaster.sendPackets(new S_ServerMessage(328)); // 請選擇正確的對象。
					}
					return;
				}
			}
			// 目標為寵物
			else if (target instanceof PetInstance) {
				PetInstance targetPet = (PetInstance) target;
				// 目標在安區、攻擊者在安區
				if ((targetPet.getZoneType() == 1) || (pet.getZoneType() == 1)) {
					// 寵物主人
					if (pet.getMaster() instanceof PcInstance) {
						PcInstance petMaster = (PcInstance) pet.getMaster();
						petMaster.sendPackets(new S_ServerMessage(328)); // 請選擇正確的對象。
					}
					return;
				}
			}
			// 目標為召喚怪
			else if (target instanceof SummonInstance) {
				SummonInstance targetSummon = (SummonInstance) target;
				// 目標在安區、攻擊者在安區
				if ((targetSummon.getZoneType() == 1) || (pet.getZoneType() == 1)) {
					// 寵物主人
					if (pet.getMaster() instanceof PcInstance) {
						PcInstance petMaster = (PcInstance) pet.getMaster();
						petMaster.sendPackets(new S_ServerMessage(328)); // 請選擇正確的對象。
					}
					return;
				}
			}
			// 目標為怪物
			else if (target instanceof MonsterInstance) {
				MonsterInstance mob = (MonsterInstance) target;
				// 特定狀態下才可攻擊
				if (pet.getMaster().isAttackMiss(pet.getMaster(), mob.getNpcId())) {
					if (pet.getMaster() instanceof PcInstance) {
						PcInstance petMaster = (PcInstance) pet.getMaster();
						petMaster.sendPackets(new S_ServerMessage(328)); // 請選擇正確的對象。
					}
					return;
				}
			}
			pet.setMasterTarget(target);
		}
	}

	@Override
	public String getType() {
		return C_SELECT_TARGET;
	}
}
