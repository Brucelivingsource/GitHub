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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_ANTHARAS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_FAFURION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AWAKEN_VALAKAS;

import java.util.List;
import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.ArmorSetTable;
import Lsimulator.server.server.model.Instance.ItemInstance;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorArmorSets;
import Lsimulator.server.server.utils.collections.Lists;

public abstract class LsimulatorArmorSet {
	public abstract void giveEffect(PcInstance pc);

	public abstract void cancelEffect(PcInstance pc);

	public abstract boolean isValid(PcInstance pc);

	public abstract boolean isPartOfSet(int id);

	public abstract boolean isEquippedRingOfArmorSet(PcInstance pc);

	public static List<LsimulatorArmorSet> getAllSet() {
		return _allSet;
	}

	private static List<LsimulatorArmorSet> _allSet = Lists.newList();

	/*
	 * ここで初期化してしまうのはいかがなものか・・・美しくない気がする
	 */
	static {
		LsimulatorArmorSetImpl impl;

		for (LsimulatorArmorSets armorSets : ArmorSetTable.getInstance().getAllList()) {
			try {

				impl = new LsimulatorArmorSetImpl(getArray(armorSets.getSets(), ","));
				if (armorSets.getPolyId() != -1) {
					impl.addEffect(new PolymorphEffect(armorSets.getPolyId()));
				}
				impl.addEffect(new AcHpMpBonusEffect(armorSets.getAc(),
						armorSets.getHp(), armorSets.getMp(), armorSets
								.getHpr(), armorSets.getMpr(), armorSets
								.getMr()));
				impl.addEffect(new StatBonusEffect(armorSets.getStr(),
						armorSets.getDex(), armorSets.getCon(), armorSets
								.getWis(), armorSets.getCha(), armorSets
								.getIntl()));
				impl.addEffect(new DefenseBonusEffect(armorSets
						.getDefenseWater(), armorSets.getDefenseWind(),
						armorSets.getDefenseFire(), armorSets.getDefenseWind()));
				impl.addEffect(new HitDmgModifierEffect(armorSets.getHitModifier(), armorSets.getDmgModifier()
						, armorSets.getBowHitModifier(), armorSets.getBowDmgModifier(), armorSets.getSp()));
				_allSet.add(impl);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private static int[] getArray(String s, String sToken) {
		StringTokenizer st = new StringTokenizer(s, sToken);
		int size = st.countTokens();
		String temp = null;
		int[] array = new int[size];
		for (int i = 0; i < size; i++) {
			temp = st.nextToken();
			array[i] = Integer.parseInt(temp);
		}
		return array;
	}
}

interface LsimulatorArmorSetEffect {
	public void giveEffect(PcInstance pc);

	public void cancelEffect(PcInstance pc);
}

class LsimulatorArmorSetImpl extends LsimulatorArmorSet {
	private final int _ids[];

	private final List<LsimulatorArmorSetEffect> _effects;

	protected LsimulatorArmorSetImpl(int ids[]) {
		_ids = ids;
		_effects = Lists.newList();
	}

	public void addEffect(LsimulatorArmorSetEffect effect) {
		_effects.add(effect);
	}

	public void removeEffect(LsimulatorArmorSetEffect effect) {
		_effects.remove(effect);
	}

	@Override
	public void cancelEffect(PcInstance pc) {
		for (LsimulatorArmorSetEffect effect : _effects) {
			effect.cancelEffect(pc);
		}
	}

	@Override
	public void giveEffect(PcInstance pc) {
		for (LsimulatorArmorSetEffect effect : _effects) {
			effect.giveEffect(pc);
		}
	}

	@Override
	public final boolean isValid(PcInstance pc) {
		return pc.getInventory().checkEquipped(_ids);
	}

	@Override
	public boolean isPartOfSet(int id) {
		for (int i : _ids) {
			if (id == i) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEquippedRingOfArmorSet(PcInstance pc) {
		LsimulatorPcInventory pcInventory = pc.getInventory();
		ItemInstance armor = null;
		boolean isSetContainRing = false;

		// セット装備にリングが含まれているか調べる
		for (int id : _ids) {
			armor = pcInventory.findItemId(id);
			if ((armor.getItem().getType2() == 2)
					&& (armor.getItem().getType() == 9)) { // ring
				isSetContainRing = true;
				break;
			}
		}

		// リングを2つ装備していて、それが両方セット装備か調べる
		if ((armor != null) && isSetContainRing) {
			int itemId = armor.getItem().getItemId();
			if (pcInventory.getTypeEquipped(2, 9) == 2) {
				ItemInstance ring[] = new ItemInstance[2];
				ring = pcInventory.getRingEquipped();
				if ((ring[0].getItem().getItemId() == itemId)
						&& (ring[1].getItem().getItemId() == itemId)) {
					return true;
				}
			}
		}
		return false;
	}

}

class AcHpMpBonusEffect implements LsimulatorArmorSetEffect {
	private final int _ac;

	private final int _addHp;

	private final int _addMp;

	private final int _regenHp;

	private final int _regenMp;

	private final int _addMr;

	public AcHpMpBonusEffect(int ac, int addHp, int addMp, int regenHp,
			int regenMp, int addMr) {
		_ac = ac;
		_addHp = addHp;
		_addMp = addMp;
		_regenHp = regenHp;
		_regenMp = regenMp;
		_addMr = addMr;
	}

	@Override
	public void giveEffect(PcInstance pc) {
		pc.addAc(_ac);
		pc.addMaxHp(_addHp);
		pc.addMaxMp(_addMp);
		pc.addHpr(_regenHp);
		pc.addMpr(_regenMp);
		pc.addMr(_addMr);
	}

	@Override
	public void cancelEffect(PcInstance pc) {
		pc.addAc(-_ac);
		pc.addMaxHp(-_addHp);
		pc.addMaxMp(-_addMp);
		pc.addHpr(-_regenHp);
		pc.addMpr(-_regenMp);
		pc.addMr(-_addMr);
	}
}

class StatBonusEffect implements LsimulatorArmorSetEffect {
	private final int _str;

	private final int _dex;

	private final int _con;

	private final int _wis;

	private final int _cha;

	private final int _intl;

	public StatBonusEffect(int str, int dex, int con, int wis, int cha, int intl) {
		_str = str;
		_dex = dex;
		_con = con;
		_wis = wis;
		_cha = cha;
		_intl = intl;
	}

	@Override
	public void giveEffect(PcInstance pc) {
		pc.addStr((byte) _str);
		pc.addDex((byte) _dex);
		pc.addCon((byte) _con);
		pc.addWis((byte) _wis);
		pc.addCha((byte) _cha);
		pc.addInt((byte) _intl);
	}

	@Override
	public void cancelEffect(PcInstance pc) {
		pc.addStr((byte) -_str);
		pc.addDex((byte) -_dex);
		pc.addCon((byte) -_con);
		pc.addWis((byte) -_wis);
		pc.addCha((byte) -_cha);
		pc.addInt((byte) -_intl);
	}
}

// 水、風、火、地屬性
class DefenseBonusEffect implements LsimulatorArmorSetEffect {
	private final int _defenseWater;

	private final int _defenseWind;

	private final int _defenseFire;

	private final int _defenseEarth;

	public DefenseBonusEffect(int defenseWater, int defenseWind,
			int defenseFire, int defenseEarth) {
		_defenseWater = defenseWater;
		_defenseWind = defenseWind;
		_defenseFire = defenseFire;
		_defenseEarth = defenseEarth;
	}

	// @Override
	@Override
	public void giveEffect(PcInstance pc) {
		pc.addWater(_defenseWater);
		pc.addWind(_defenseWind);
		pc.addFire(_defenseFire);
		pc.addEarth(_defenseEarth);
	}

	// @Override
	@Override
	public void cancelEffect(PcInstance pc) {
		pc.addWater(-_defenseWater);
		pc.addWind(-_defenseWind);
		pc.addFire(-_defenseFire);
		pc.addEarth(-_defenseEarth);
	}
}

// 命中率、額外攻擊力、魔攻
class HitDmgModifierEffect implements LsimulatorArmorSetEffect {
	private final int _hitModifier;

	private final int _dmgModifier;

	private final int _bowHitModifier;

	private final int _bowDmgModifier;

	private final int _sp;

	public HitDmgModifierEffect(int hitModifier, int dmgModifier,
			int bowHitModifier, int bowDmgModifier, int sp) {
		_hitModifier = hitModifier;
		_dmgModifier = dmgModifier;
		_bowHitModifier = bowHitModifier;
		_bowDmgModifier = bowDmgModifier;
		_sp = sp;
	}

	// @Override
	@Override
	public void giveEffect(PcInstance pc) {
		pc.addHitModifierByArmor(_hitModifier);
		pc.addDmgModifierByArmor(_dmgModifier);
		pc.addBowHitModifierByArmor(_bowHitModifier);
		pc.addBowDmgModifierByArmor(_bowDmgModifier);
		pc.addSp(_sp);
	}

	// @Override
	@Override
	public void cancelEffect(PcInstance pc) {
		pc.addHitModifierByArmor(-_hitModifier);
		pc.addDmgModifierByArmor(-_dmgModifier);
		pc.addBowHitModifierByArmor(-_bowHitModifier);
		pc.addBowDmgModifierByArmor(-_bowDmgModifier);
		pc.addSp(-_sp);
	}
}

class PolymorphEffect implements LsimulatorArmorSetEffect {
	private int _gfxId;

	public PolymorphEffect(int gfxId) {
		_gfxId = gfxId;
	}

	@Override
	public void giveEffect(PcInstance pc) {
		int awakeSkillId = pc.getAwakeSkillId();
		if ((awakeSkillId == AWAKEN_ANTHARAS)
				|| (awakeSkillId == AWAKEN_FAFURION)
				|| (awakeSkillId == AWAKEN_VALAKAS)) {
			pc.sendPackets(new S_ServerMessage(1384)); // 現在の状態では変身できません。
			return;
		}
		if ((_gfxId == 6080) || (_gfxId == 6094)) {
			if (pc.get_sex() == 0) {
				_gfxId = 6094;
			} else {
				_gfxId = 6080;
			}
			if (!isRemainderOfCharge(pc)) { // 残チャージ数なし
				return;
			}
		}
		LsimulatorPolyMorph.doPoly(pc, _gfxId, 0, LsimulatorPolyMorph.MORPH_BY_ITEMMAGIC);
	}

	@Override
	public void cancelEffect(PcInstance pc) {
		int awakeSkillId = pc.getAwakeSkillId();
		if ((awakeSkillId == AWAKEN_ANTHARAS)
				|| (awakeSkillId == AWAKEN_FAFURION)
				|| (awakeSkillId == AWAKEN_VALAKAS)) {
			pc.sendPackets(new S_ServerMessage(1384)); // 現在の状態では変身できません。
			return;
		}
		if (_gfxId == 6080) {
			if (pc.get_sex() == 0) {
				_gfxId = 6094;
			}
		}
		if (pc.getTempCharGfx() != _gfxId) {
			return;
		}
		LsimulatorPolyMorph.undoPoly(pc);
	}

	private boolean isRemainderOfCharge(PcInstance pc) {
		boolean isRemainderOfCharge = false;
		if (pc.getInventory().checkItem(20383, 1)) {
			ItemInstance item = pc.getInventory().findItemId(20383);
			if (item != null) {
				if (item.getChargeCount() != 0) {
					isRemainderOfCharge = true;
				}
			}
		}
		return isRemainderOfCharge;
	}

}
