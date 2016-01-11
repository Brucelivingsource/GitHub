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

import java.util.TimerTask;

import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.datatables.PetTypeTable;
import Lsimulator.server.server.model.Instance.PetInstance;
import Lsimulator.server.server.serverpackets.S_NpcChatPacket;
import Lsimulator.server.server.templates.LsimulatorPet;
import Lsimulator.server.server.templates.LsimulatorPetType;

public class LsimulatorPetFood extends TimerTask {
	/** 寵物飽食度計時器 */
	public LsimulatorPetFood(PetInstance pet, int itemObj) {
		_pet = pet;
		_l1pet = PetTable.getInstance().getTemplate(itemObj);
	}

	@Override
	public void run() {
		if (_pet != null && !_pet.isDead()) {
			_food = _pet.get_food() - 2;
			if (_food <= 0) {
				_pet.set_food(0);
				_pet.setCurrentPetStatus(3);

				// 非常餓時提醒主人
				LsimulatorPetType type = PetTypeTable.getInstance().get(
						_pet.getNpcTemplate().get_npcId());
				int id = type.getDefyMessageId();
				if (id != 0) {
					_pet.broadcastPacket(new S_NpcChatPacket(_pet, "$" + id, 0));
				}
			} else {
				_pet.set_food(_food);
			}
			if (_l1pet != null) {
				// 紀錄寵物飽食度
				_l1pet.set_food(_pet.get_food());
				PetTable.getInstance().storePetFood(_l1pet);
			}
		} else {
			cancel();
		}
	}

	private final PetInstance _pet;
	private int _food = 0;
	private LsimulatorPet _l1pet;
}
