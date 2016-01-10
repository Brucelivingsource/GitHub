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
import Lsimulator.server.server.datatables.PetItemTable;
import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.datatables.PetTypeTable;
import Lsimulator.server.server.model.LsimulatorInventory;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorPcInventory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorDollInstance;
import Lsimulator.server.server.model.Instance.LsimulatorItemInstance;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.serverpackets.S_ItemName;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.templates.LsimulatorPet;
import Lsimulator.server.server.templates.LsimulatorPetItem;
import Lsimulator.server.server.templates.LsimulatorPetType;
import Lsimulator.server.server.utils.Random;

/**
 * 處理收到由客戶端傳來給道具的封包
 */
public class C_GiveItem extends ClientBasePacket {
	private static final String C_GIVE_ITEM = "[C] C_GiveItem";

	public C_GiveItem(byte decrypt[], ClientThread client) {
		super(decrypt);
		
		LsimulatorPcInstance pc = client.getActiveChar();
		if ((pc == null) || pc.isGhost()) {
			return;
		}
		
		int targetId = readD();
		readH();
		readH();
		int itemId = readD();
		int count = readD();
		
		LsimulatorObject object = LsimulatorWorld.getInstance().findObject(targetId);
		if ((object == null) || !(object instanceof LsimulatorNpcInstance)) {
			return;
		}
		LsimulatorNpcInstance target = (LsimulatorNpcInstance) object;
		if (!isNpcItemReceivable(target.getNpcTemplate())) {
			return;
		}
		LsimulatorInventory targetInv = target.getInventory();

		LsimulatorInventory inv = pc.getInventory();
		LsimulatorItemInstance item = inv.getItem(itemId);
		if (item == null) {
			return;
		}
		if (item.isEquipped()) {
			pc.sendPackets(new S_ServerMessage(141)); // \f1你不能夠將轉移已經裝備的物品。
			return;
		}
		if (!item.getItem().isTradable()) {
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName())); // \f1%0%d是不可轉移的…
			return;
		}
		if (item.getBless() >= 128) { // 封印的裝備
			// \f1%0%d是不可轉移的…
			pc.sendPackets(new S_ServerMessage(210, item.getItem().getName()));
			return;
		}
		// 使用中的寵物項鍊 - 無法給予
		for (LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
			if (petNpc instanceof LsimulatorPetInstance) {
				LsimulatorPetInstance pet = (LsimulatorPetInstance) petNpc;
				if (item.getId() == pet.getItemObjId()) {
					pc.sendPackets(new S_ServerMessage(1187)); // 寵物項鍊正在使用中。
					return;
				}
			}
		}
		// 使用中的魔法娃娃 - 無法給予
		for (LsimulatorDollInstance doll : pc.getDollList().values()) {
			if (doll.getItemObjId() == item.getId()) {
				pc.sendPackets(new S_ServerMessage(1181)); // 這個魔法娃娃目前正在使用中。
				return;
			}
		}
		if (targetInv.checkAddItem(item, count) != LsimulatorInventory.OK) {
			pc.sendPackets(new S_ServerMessage(942)); // 對方的負重太重，無法再給予。
			return;
		}
		item = inv.tradeItem(item, count, targetInv);
		target.onGetItem(item);
		target.turnOnOffLight();
		pc.turnOnOffLight();

		LsimulatorPetType petType = PetTypeTable.getInstance().get(
				target.getNpcTemplate().get_npcId());
		if ((petType == null) || target.isDead()) {
			return;
		}

		// 捕抓寵物
		if (item.getItemId() == petType.getItemIdForTaming()) {
			tamePet(pc, target);
		}
		// 進化寵物
		else if (item.getItemId() == petType.getEvolvItemId()) {
			evolvePet(pc, target, item.getItemId());
		}

		if (item.getItem().getType2() == 0) { // 道具類
			// 食物類
			if (item.getItem().getType() == 7) {
				eatFood(pc, target, item, count);
			}
			// 寵物裝備類
			else if ((item.getItem().getType() == 11)
					&& (petType.canUseEquipment())) { // 判斷是否可用寵物裝備
				usePetWeaponArmor(target, item);
			}
		}

	}

	private void eatFood(LsimulatorPcInstance pc, LsimulatorNpcInstance target,
			LsimulatorItemInstance item, int count) {
		if (!(target instanceof LsimulatorPetInstance)) {
			return;
		}
		LsimulatorPetInstance pet = (LsimulatorPetInstance) target;
		LsimulatorPet _l1pet = PetTable.getInstance().getTemplate(item.getId());
		int food = 0;
		int foodCount = 0;
		boolean isFull = false;

		if (pet.get_food() == 100) { // 非常飽
			return;
		}
		// 食物營養度判斷
		if (item.getItem().getFoodVolume() != 0) {
			// 吃掉食物的數量判斷
			for (int i = 0; i < count; i++) {
				food = item.getItem().getFoodVolume() / 10;
				food += pet.get_food();
				if (!isFull) {
					if (food >= 100) {
						isFull = true;
						pet.set_food(100);
						foodCount++;
					} else {
						pet.set_food(food);
						foodCount++;
					}
				} else {
					break;
				}
			}
			if (foodCount != 0) {
				pet.getInventory().consumeItem(item.getItemId(), foodCount); // 吃掉食物
				// 紀錄寵物飽食度
				_l1pet.set_food(pet.get_food());
				PetTable.getInstance().storePetFood(_l1pet);
			}
		}
	}

	private void usePetWeaponArmor(LsimulatorNpcInstance target, LsimulatorItemInstance item) {
		if (!(target instanceof LsimulatorPetInstance)) {
			return;
		}
		LsimulatorPetInstance pet = (LsimulatorPetInstance) target;
		LsimulatorPetItem petItem = PetItemTable.getInstance().getTemplate(
				item.getItemId());
		if (petItem.getUseType() == 1) { // 牙齒
			pet.usePetWeapon(pet, item);
		} else if (petItem.getUseType() == 0) { // 盔甲
			pet.usePetArmor(pet, item);
		}
	}

	private final static String receivableImpls[] = new String[] { "LsimulatorNpc", // NPC
			"LsimulatorMonster", // 怪物
			"LsimulatorGuardian", // 妖精森林的守護者
			"LsimulatorTeleporter", // 傳送師
			"LsimulatorGuard" }; // 警衛

	private boolean isNpcItemReceivable(LsimulatorNpc npc) {
		for (String impl : receivableImpls) {
			if (npc.getImpl().equals(impl)) {
				return true;
			}
		}
		return false;
	}

	private void tamePet(LsimulatorPcInstance pc, LsimulatorNpcInstance target) {
		if ((target instanceof LsimulatorPetInstance)
				|| (target instanceof LsimulatorSummonInstance)) {
			return;
		}

		int petcost = 0;
		for (LsimulatorNpcInstance petNpc : pc.getPetList().values()) {
			petcost += petNpc.getPetcost();
		}
		int charisma = pc.getCha();
		if (pc.isCrown()) { // 王族
			charisma += 6;
		} else if (pc.isElf()) { // 妖精
			charisma += 12;
		} else if (pc.isWizard()) { // 法師
			charisma += 6;
		} else if (pc.isDarkelf()) { // 黑暗妖精
			charisma += 6;
		} else if (pc.isDragonKnight()) { // 龍騎士
			charisma += 6;
		} else if (pc.isIllusionist()) { // 幻術師
			charisma += 6;
		}
		charisma -= petcost;

		LsimulatorPcInventory inv = pc.getInventory();
		if ((charisma >= 6) && (inv.getSize() < 180)) {
			if (isTamePet(target)) {
				LsimulatorItemInstance petamu = inv.storeItem(40314, 1); // 漂浮之眼的肉
				if (petamu != null) {
					new LsimulatorPetInstance(target, pc, petamu.getId());
					pc.sendPackets(new S_ItemName(petamu));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(324)); // 馴養失敗。
			}
		}
	}

	private void evolvePet(LsimulatorPcInstance pc, LsimulatorNpcInstance target, int itemId) {
		if (!(target instanceof LsimulatorPetInstance)) {
			return;
		}
		LsimulatorPcInventory inv = pc.getInventory();
		LsimulatorPetInstance pet = (LsimulatorPetInstance) target;
		LsimulatorItemInstance petamu = inv.getItem(pet.getItemObjId());
		if (((pet.getLevel() >= 30) || (itemId == 41310)) && // Lv30以上或是使用勝利果實
				(pc == pet.getMaster()) && // 自分のペット
				(petamu != null)) {
			LsimulatorItemInstance highpetamu = inv.storeItem(40316, 1);
			if (highpetamu != null) {
				pet.evolvePet( // 寵物進化
				highpetamu.getId());
				pc.sendPackets(new S_ItemName(highpetamu));
				inv.removeItem(petamu, 1);
			}
		}
	}

	private boolean isTamePet(LsimulatorNpcInstance npc) {
		boolean isSuccess = false;
		int npcId = npc.getNpcTemplate().get_npcId();
		if (npcId == 45313) { // タイガー
			if ((npc.getMaxHp() / 3 > npc.getCurrentHp() // HPが1/3未満で1/16の確率
					)
					&& (Random.nextInt(16) == 15)) {
				isSuccess = true;
			}
		} else {
			if (npc.getMaxHp() / 3 > npc.getCurrentHp()) {
				isSuccess = true;
			}
		}

		if ((npcId == 45313) || (npcId == 45044) || (npcId == 45711)) { // タイガー、ラクーン、紀州犬の子犬
			if (npc.isResurrect()) { // RES後はテイム不可
				isSuccess = false;
			}
		}

		return isSuccess;
	}

	@Override
	public String getType() {
		return C_GIVE_ITEM;
	}
}
