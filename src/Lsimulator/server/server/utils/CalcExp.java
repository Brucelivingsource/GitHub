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
package Lsimulator.server.server.utils;

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_7_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_7_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_7_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_7_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_7_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_7_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_EXP_150;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_EXP_175;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_EXP_200;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_EXP_225;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_EXP_250;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_POTION_OF_BATTLE;

import java.util.List;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.server.datatables.ExpTable;
import Lsimulator.server.server.datatables.PetTable;
import Lsimulator.server.server.model.LsimulatorCharacter;
import Lsimulator.server.server.model.LsimulatorObject;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorNpcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.Instance.LsimulatorPetInstance;
import Lsimulator.server.server.model.Instance.LsimulatorSummonInstance;
import Lsimulator.server.server.serverpackets.S_PetPack;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.templates.LsimulatorPet;

// Referenced classes of package Lsimulator.server.server.utils:
// CalcStat

public class CalcExp {

	private static final long serialVersionUID = 1L;

	private static Logger _log = Logger.getLogger(CalcExp.class.getName());

	public static final int MAX_EXP = ExpTable.getExpByLevel(100) - 1;

	private CalcExp() {
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static void calcExp(LsimulatorPcInstance l1pcinstance, int targetid, List<LsimulatorCharacter> acquisitorList, List<Integer> hateList, int exp) {

		int i = 0;
		double party_level = 0;
		double dist = 0;
		int member_exp = 0;
		int member_lawful = 0;
		LsimulatorObject l1object = LsimulatorWorld.getInstance().findObject(targetid);
		LsimulatorNpcInstance npc = (LsimulatorNpcInstance) l1object;

		// ヘイトの合計を取得
		LsimulatorCharacter acquisitor;
		int hate = 0;
		int acquire_exp = 0;
		int acquire_lawful = 0;
		int party_exp = 0;
		int party_lawful = 0;
		int totalHateExp = 0;
		int totalHateLawful = 0;
		int partyHateExp = 0;
		int partyHateLawful = 0;
		int ownHateExp = 0;

		if (acquisitorList.size() != hateList.size()) {
			return;
		}
		for (i = hateList.size() - 1; i >= 0; i--) {
			acquisitor = acquisitorList.get(i);
			hate = hateList.get(i);
			if ((acquisitor != null) && !acquisitor.isDead()) {
				totalHateExp += hate;
				if (acquisitor instanceof LsimulatorPcInstance) {
					totalHateLawful += hate;
				}
			}
			else { // nullだったり死んでいたら排除
				acquisitorList.remove(i);
				hateList.remove(i);
			}
		}
		if (totalHateExp == 0) { // 取得者がいない場合
			return;
		}

		if ((l1object != null) && !(npc instanceof LsimulatorPetInstance) && !(npc instanceof LsimulatorSummonInstance)) {
			// int exp = npc.get_exp();
			/*if (!LsimulatorWorld.getInstance().isProcessingContributionTotal() && (l1pcinstance.getHomeTownId() > 0)) {
				int contribution = npc.getLevel() / 10;
				l1pcinstance.addContribution(contribution);
			}*/ // 取消由打怪獲得村莊貢獻度，改由製作村莊福利品獲得貢獻度 for 3.3C
			int lawful = npc.getLawful();

			if (l1pcinstance.isInParty()) { // パーティー中
				// パーティーのヘイトの合計を算出
				// パーティーメンバー以外にはそのまま配分
				partyHateExp = 0;
				partyHateLawful = 0;
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = acquisitorList.get(i);
					hate = hateList.get(i);
					if (acquisitor instanceof LsimulatorPcInstance) {
						LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
						if (pc == l1pcinstance) {
							partyHateExp += hate;
							partyHateLawful += hate;
						}
						else if (l1pcinstance.getParty().isMember(pc)) {
							partyHateExp += hate;
							partyHateLawful += hate;
						}
						else {
							if (totalHateExp > 0) {
								acquire_exp = (exp * hate / totalHateExp);
							}
							if (totalHateLawful > 0) {
								acquire_lawful = (lawful * hate / totalHateLawful);
							}
							AddExp(pc, acquire_exp, acquire_lawful);
						}
					}
					else if (acquisitor instanceof LsimulatorPetInstance) {
						LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
						LsimulatorPcInstance master = (LsimulatorPcInstance) pet.getMaster();
						if (master == l1pcinstance) {
							partyHateExp += hate;
						}
						else if (l1pcinstance.getParty().isMember(master)) {
							partyHateExp += hate;
						}
						else {
							if (totalHateExp > 0) {
								acquire_exp = (exp * hate / totalHateExp);
							}
							AddExpPet(pet, acquire_exp);
						}
					}
					else if (acquisitor instanceof LsimulatorSummonInstance) {
						LsimulatorSummonInstance summon = (LsimulatorSummonInstance) acquisitor;
						LsimulatorPcInstance master = (LsimulatorPcInstance) summon.getMaster();
						if (master == l1pcinstance) {
							partyHateExp += hate;
						}
						else if (l1pcinstance.getParty().isMember(master)) {
							partyHateExp += hate;
						}
						else {}
					}
				}
				if (totalHateExp > 0) {
					party_exp = (exp * partyHateExp / totalHateExp);
				}
				if (totalHateLawful > 0) {
					party_lawful = (lawful * partyHateLawful / totalHateLawful);
				}

				// EXP、ロウフル配分

				// プリボーナス
				double pri_bonus = 0;
				LsimulatorPcInstance leader = l1pcinstance.getParty().getLeader();
				if (leader.isCrown() && (l1pcinstance.knownsObject(leader) || l1pcinstance.equals(leader))) {
					pri_bonus = 0.059;
				}

				// PT経験値の計算
				LsimulatorPcInstance[] ptMembers = l1pcinstance.getParty().getMembers();
				double pt_bonus = 0;
				for (LsimulatorPcInstance each : ptMembers) {
					if (l1pcinstance.knownsObject(each) || l1pcinstance.equals(each)) {
						party_level += each.getLevel() * each.getLevel();
					}
					if (l1pcinstance.knownsObject(each)) {
						pt_bonus += 0.04;
					}
				}

				party_exp = (int) (party_exp * (1 + pt_bonus + pri_bonus));

				// 自キャラクターとそのペット・サモンのヘイトの合計を算出
				if (party_level > 0) {
					dist = ((l1pcinstance.getLevel() * l1pcinstance.getLevel()) / party_level);
				}
				member_exp = (int) (party_exp * dist);
				member_lawful = (int) (party_lawful * dist);

				ownHateExp = 0;
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = acquisitorList.get(i);
					hate = hateList.get(i);
					if (acquisitor instanceof LsimulatorPcInstance) {
						LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
						if (pc == l1pcinstance) {
							ownHateExp += hate;
						}
					}
					else if (acquisitor instanceof LsimulatorPetInstance) {
						LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
						LsimulatorPcInstance master = (LsimulatorPcInstance) pet.getMaster();
						if (master == l1pcinstance) {
							ownHateExp += hate;
						}
					}
					else if (acquisitor instanceof LsimulatorSummonInstance) {
						LsimulatorSummonInstance summon = (LsimulatorSummonInstance) acquisitor;
						LsimulatorPcInstance master = (LsimulatorPcInstance) summon.getMaster();
						if (master == l1pcinstance) {
							ownHateExp += hate;
						}
					}
				}
				// 自キャラクターとそのペット・サモンに分配
				if (ownHateExp != 0) { // 攻撃に参加していた
					for (i = hateList.size() - 1; i >= 0; i--) {
						acquisitor = acquisitorList.get(i);
						hate = hateList.get(i);
						if (acquisitor instanceof LsimulatorPcInstance) {
							LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
							if (pc == l1pcinstance) {
								if (ownHateExp > 0) {
									acquire_exp = (member_exp * hate / ownHateExp);
								}
								AddExp(pc, acquire_exp, member_lawful);
							}
						}
						else if (acquisitor instanceof LsimulatorPetInstance) {
							LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
							LsimulatorPcInstance master = (LsimulatorPcInstance) pet.getMaster();
							if (master == l1pcinstance) {
								if (ownHateExp > 0) {
									acquire_exp = (member_exp * hate / ownHateExp);
								}
								AddExpPet(pet, acquire_exp);
							}
						}
						else if (acquisitor instanceof LsimulatorSummonInstance) {}
					}
				}
				else { // 攻撃に参加していなかった
						// 自キャラクターのみに分配
					AddExp(l1pcinstance, member_exp, member_lawful);
				}

				// パーティーメンバーとそのペット・サモンのヘイトの合計を算出
				for (LsimulatorPcInstance ptMember : ptMembers) {
					if (l1pcinstance.knownsObject(ptMember)) {
						if (party_level > 0) {
							dist = ((ptMember.getLevel() * ptMember.getLevel()) / party_level);
						}
						member_exp = (int) (party_exp * dist);
						member_lawful = (int) (party_lawful * dist);

						ownHateExp = 0;
						for (i = hateList.size() - 1; i >= 0; i--) {
							acquisitor = acquisitorList.get(i);
							hate = hateList.get(i);
							if (acquisitor instanceof LsimulatorPcInstance) {
								LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
								if (pc == ptMember) {
									ownHateExp += hate;
								}
							}
							else if (acquisitor instanceof LsimulatorPetInstance) {
								LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
								LsimulatorPcInstance master = (LsimulatorPcInstance) pet.getMaster();
								if (master == ptMember) {
									ownHateExp += hate;
								}
							}
							else if (acquisitor instanceof LsimulatorSummonInstance) {
								LsimulatorSummonInstance summon = (LsimulatorSummonInstance) acquisitor;
								LsimulatorPcInstance master = (LsimulatorPcInstance) summon.getMaster();
								if (master == ptMember) {
									ownHateExp += hate;
								}
							}
						}
						// パーティーメンバーとそのペット・サモンに分配
						if (ownHateExp != 0) { // 攻撃に参加していた
							for (i = hateList.size() - 1; i >= 0; i--) {
								acquisitor = acquisitorList.get(i);
								hate = hateList.get(i);
								if (acquisitor instanceof LsimulatorPcInstance) {
									LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
									if (pc == ptMember) {
										if (ownHateExp > 0) {
											acquire_exp = (member_exp * hate / ownHateExp);
										}
										AddExp(pc, acquire_exp, member_lawful);
									}
								}
								else if (acquisitor instanceof LsimulatorPetInstance) {
									LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
									LsimulatorPcInstance master = (LsimulatorPcInstance) pet.getMaster();
									if (master == ptMember) {
										if (ownHateExp > 0) {
											acquire_exp = (member_exp * hate / ownHateExp);
										}
										AddExpPet(pet, acquire_exp);
									}
								}
								else if (acquisitor instanceof LsimulatorSummonInstance) {}
							}
						}
						else { // 攻撃に参加していなかった
								// パーティーメンバーのみに分配
							AddExp(ptMember, member_exp, member_lawful);
						}
					}
				}
			}
			else { // パーティーを組んでいない
					// EXP、ロウフルの分配
				for (i = hateList.size() - 1; i >= 0; i--) {
					acquisitor = acquisitorList.get(i);
					hate = hateList.get(i);
					acquire_exp = (exp * hate / totalHateExp);
					if (acquisitor instanceof LsimulatorPcInstance) {
						if (totalHateLawful > 0) {
							acquire_lawful = (lawful * hate / totalHateLawful);
						}
					}

					if (acquisitor instanceof LsimulatorPcInstance) {
						LsimulatorPcInstance pc = (LsimulatorPcInstance) acquisitor;
						AddExp(pc, acquire_exp, acquire_lawful);
					}
					else if (acquisitor instanceof LsimulatorPetInstance) {
						LsimulatorPetInstance pet = (LsimulatorPetInstance) acquisitor;
						AddExpPet(pet, acquire_exp);
					}
					else if (acquisitor instanceof LsimulatorSummonInstance) {}
				}
			}
		}
	}

	private static void AddExp(LsimulatorPcInstance pc, int exp, int lawful) {

		int add_lawful = (int) (lawful * Config.RATE_LA) * -1;
		pc.addLawful(add_lawful);

		double exppenalty = ExpTable.getPenaltyRate(pc.getLevel());
		double foodBonus = 1.0;
		double expBonus = 1.0;
		// 魔法料理經驗加成
		if (pc.hasSkillEffect(COOKING_1_7_N) || pc.hasSkillEffect(COOKING_1_7_S)) {
			foodBonus = 1.01;
		}
		if (pc.hasSkillEffect(COOKING_2_7_N) || pc.hasSkillEffect(COOKING_2_7_S)) {
			foodBonus = 1.02;
		}
		if (pc.hasSkillEffect(COOKING_3_7_N) || pc.hasSkillEffect(COOKING_3_7_S)) {
			foodBonus = 1.03;
		}
		// 戰鬥藥水、神力藥水經驗加成
		if (pc.hasSkillEffect(EFFECT_POTION_OF_BATTLE)) {
			expBonus = 1.2;
		} else if (pc.hasSkillEffect(EFFECT_POTION_OF_EXP_150)) {
			expBonus = 2.5;
		} else if (pc.hasSkillEffect(EFFECT_POTION_OF_EXP_175)) {
			expBonus = 2.75;
		} else if (pc.hasSkillEffect(EFFECT_POTION_OF_EXP_200)) {
			expBonus = 3.0;
		} else if (pc.hasSkillEffect(EFFECT_POTION_OF_EXP_225)) {
			expBonus = 3.25;
		} else if (pc.hasSkillEffect(EFFECT_POTION_OF_EXP_250)) {
			expBonus = 3.5;
		}

		int add_exp = (int) (exp * exppenalty * Config.RATE_XP * foodBonus * expBonus);
		pc.addExp(add_exp);
		pc.addMonsKill();
	}

	private static void AddExpPet(LsimulatorPetInstance pet, int exp) {
		LsimulatorPcInstance pc = (LsimulatorPcInstance) pet.getMaster();

		int petItemObjId = pet.getItemObjId();

		int levelBefore = pet.getLevel();
		int totalExp = (int) (exp * Config.RATE_XP + pet.getExp());
		if (totalExp >= ExpTable.getExpByLevel(51)) {
			totalExp = ExpTable.getExpByLevel(51) - 1;
		}
		pet.setExp(totalExp);

		pet.setLevel(ExpTable.getLevelByExp(totalExp));

		int expPercentage = ExpTable.getExpPercentage(pet.getLevel(), totalExp);

		int gap = pet.getLevel() - levelBefore;
		for (int i = 1; i <= gap; i++) {
			IntRange hpUpRange = pet.getPetType().getHpUpRange();
			IntRange mpUpRange = pet.getPetType().getMpUpRange();
			pet.addMaxHp(hpUpRange.randomValue());
			pet.addMaxMp(mpUpRange.randomValue());
		}

		pet.setExpPercent(expPercentage);
		pc.sendPackets(new S_PetPack(pet, pc));

		if (gap != 0) { // レベルアップしたらDBに書き込む
			LsimulatorPet petTemplate = PetTable.getInstance().getTemplate(petItemObjId);
			if (petTemplate == null) { // PetTableにない
				_log.warning("LsimulatorPet == null");
				return;
			}
			petTemplate.set_exp(pet.getExp());
			petTemplate.set_level(pet.getLevel());
			petTemplate.set_hp(pet.getMaxHp());
			petTemplate.set_mp(pet.getMaxMp());
			PetTable.getInstance().storePet(petTemplate); // DBに書き込み
			pc.sendPackets(new S_ServerMessage(320, pet.getName())); // \f1%0のレベルが上がりました。
		}
	}
}