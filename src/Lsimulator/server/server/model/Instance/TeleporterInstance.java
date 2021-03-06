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
package Lsimulator.server.server.model.Instance;

import Lsimulator.server.server.utils.Random;
import java.util.logging.Logger;

import Lsimulator.server.server.GeneralThreadPool;
import Lsimulator.server.server.datatables.NPCTalkDataTable;
import Lsimulator.server.server.model.LsimulatorAttack;
import Lsimulator.server.server.model.LsimulatorNpcTalkData;
import Lsimulator.server.server.model.LsimulatorQuest;
import Lsimulator.server.server.model.LsimulatorTeleport;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.npc.LsimulatorNpcHtml;
import Lsimulator.server.server.serverpackets.S_NPCTalkReturn;
import Lsimulator.server.server.templates.LsimulatorNpc;

// Referenced classes of package Lsimulator.server.server.model:
// NpcInstance, LsimulatorTeleport, LsimulatorNpcTalkData, PcInstance,
// LsimulatorTeleporterPrices, LsimulatorTeleportLocations

public class TeleporterInstance extends NpcInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TeleporterInstance(LsimulatorNpc template) {
		super(template);
	}

	@Override
	public void onAction(PcInstance pc) {
		onAction(pc, 0);
	}

	@Override
	public void onAction(PcInstance pc, int skillId) {
		LsimulatorAttack attack = new LsimulatorAttack(pc, this, skillId);
		attack.calcHit();
		attack.action();
		attack.addChaserAttack();
		attack.calcDamage();
		attack.calcStaffOfMana();
		attack.addPcPoisonAttack(pc, this);
		attack.commit();
	}

	@Override
	public void onTalkAction(PcInstance player) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		int npcid = getNpcTemplate().get_npcId();
		LsimulatorQuest quest = player.getQuest();
		String htmlid = null;

		if (talking != null) {
			if (npcid == 50014) { // ディロン
				if (player.isWizard()) { // ウィザード
					if (quest.get_step(LsimulatorQuest.QUEST_LEVEL30) == 1
							&& !player.getInventory().checkItem(40579)) { // アンデッドの骨
						htmlid = "dilong1";
					} else {
						htmlid = "dilong3";
					}
				}
			} else if (npcid == 70779) { // ゲートアント
				if (player.getTempCharGfx() == 2437) { // ジャイアントアント変身
					htmlid = "ants3";
				} else if (player.getTempCharGfx() == 2438) {// ジャイアントアントソルジャー変身
					if (player.isCrown()) { // 君主
						if (quest.get_step(LsimulatorQuest.QUEST_LEVEL30) == 1) {
							if (player.getInventory().checkItem(40547)) { // 住民たちの遺品
								htmlid = "antsn";
							} else {
								htmlid = "ants1";
							}
						} else { // Step1以外
							htmlid = "antsn";
						}
					} else { // 君主以外
						htmlid = "antsn";
					}
				}
			} else if (npcid == 70853) { // フェアリープリンセス
				if (player.isElf()) { // エルフ
					if (quest.get_step(LsimulatorQuest.QUEST_LEVEL30) == 1) {
						if (!player.getInventory().checkItem(40592)) { // 呪われた精霊書
							if (Random.nextInt(100) < 50) { // 50%でダークマールダンジョン
								htmlid = "fairyp2";
							} else { // ダークエルフダンジョン
								htmlid = "fairyp1";
							}
						}
					}
				}
			} else if (npcid == 50031) { // セピア
				if (player.isElf()) { // エルフ
					if (quest.get_step(LsimulatorQuest.QUEST_LEVEL45) == 2) {
						if (!player.getInventory().checkItem(40602)) { // ブルーフルート
							htmlid = "sepia1";
						}
					}
				}
			} else if (npcid == 50043) { // ラムダ
				if (quest.get_step(LsimulatorQuest.QUEST_LEVEL50) == LsimulatorQuest.QUEST_END) {
					htmlid = "ramuda2";
				} else if (quest.get_step(LsimulatorQuest.QUEST_LEVEL50) == 1) { // ディガルディン同意済み
					if (player.isCrown()) { // 君主
						if (_isNowDely) { // テレポートディレイ中
							htmlid = "ramuda4";
						} else {
							htmlid = "ramudap1";
						}
					} else { // 君主以外
						htmlid = "ramuda1";
					}
				} else {
					htmlid = "ramuda3";
				}
			}
			// 歌う島のテレポーター
			else if (npcid == 50082) {
				if (player.getLevel() < 13) {
					htmlid = "en0221";
				} else {
					if (player.isElf()) {
						htmlid = "en0222e";
					} else if (player.isDarkelf()) {
						htmlid = "en0222d";
					} else {
						htmlid = "en0222";
					}
				}
			}
			// バルニア
			else if (npcid == 50001) {
				if (player.isElf()) {
					htmlid = "barnia3";
				} else if (player.isKnight() || player.isCrown()) {
					htmlid = "barnia2";
				} else if (player.isWizard() || player.isDarkelf()) {
					htmlid = "barnia1";
				}
			} else if (npcid == 81258) {// 幻術士 艾希雅
				if (player.isIllusionist()) {
					htmlid = "asha1";
				} else {
					htmlid = "asha2";
				}
			} else if (npcid == 81259) {// 龍騎士 費艾娜
				if (player.isDragonKnight()) {
					htmlid = "feaena1";
				} else {
					htmlid = "feaena2";
				}
			} else if (npcid == 71013) { // 卡連
				if (player.isDarkelf()) {
					if (player.getLevel() < 14) {
						htmlid = "karen1";
					} else {
						htmlid = "karen4";
					}
				} else {
					htmlid = "karen2";
				}
			} else if (npcid == 71095) { // 墮落的靈魂
				if (player.isDarkelf()) { // 黑暗妖精
					if (player.getLevel() >= 50) {
						int lv50_step = quest
						.get_step(LsimulatorQuest.QUEST_LEVEL50);
						if (lv50_step == LsimulatorQuest.QUEST_END) {
							htmlid = "csoulq3";
						} else if (lv50_step >= 3) {
							boolean find = false;
							for (Object objs : LsimulatorWorld.getInstance().getVisibleObjects(306).values()) {
								if (objs instanceof PcInstance) {
									PcInstance _pc = (PcInstance) objs;
									if (_pc != null) {
										find = true;
										htmlid = "csoulqn"; // 你的邪念還不夠！
										break;
									}
								}
							}
							if (!find) {
								htmlid = "csoulq1";
							} else {
								htmlid = "csoulqn";
							}
						}
					}
				}
			}

			// html表示
			if (htmlid != null) { // htmlidが指定されている場合
				player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
			} else {
				if (player.getLawful() < -1000) { // プレイヤーがカオティック
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 2));
				} else {
					player.sendPackets(new S_NPCTalkReturn(talking, objid, 1));
				}
			}
		} else {
			_log.finest((new StringBuilder())
					.append("No actions for npc id : ").append(objid)
					.toString());
		}
	}

	@Override
	public void onFinalAction(PcInstance player, String action) {
		int objid = getId();
		LsimulatorNpcTalkData talking = NPCTalkDataTable.getInstance().getTemplate(
				getNpcTemplate().get_npcId());
		if (action.equalsIgnoreCase("teleportURL")) {
			LsimulatorNpcHtml html = new LsimulatorNpcHtml(talking.getTeleportURL());
			player.sendPackets(new S_NPCTalkReturn(objid, html));
		} else if (action.equalsIgnoreCase("teleportURLA")) {
			LsimulatorNpcHtml html = new LsimulatorNpcHtml(talking.getTeleportURLA());
			player.sendPackets(new S_NPCTalkReturn(objid, html));
		}
		if (action.charAt(0) == 't'
                                        &&action.charAt(1) == 'e'
                                        &&action.charAt(2) == 'l'
                                        &&action.charAt(3) == 'e'
                                        &&action.charAt(4) == 'p'
                                        &&action.charAt(5) == 'o'
                                        &&action.charAt(6) == 'r'
                                        &&action.charAt(7) == 't' ) {
			_log.finest((new StringBuilder()).append("Setting action to : ")
					.append(action).toString());
			doFinalAction(player, action);
		}
	}

	private void doFinalAction(PcInstance player, String action) {
		int objid = getId();

		int npcid = getNpcTemplate().get_npcId();
		String htmlid = null;
		boolean isTeleport = true;

		if (npcid == 50014) { // ディロン
			if (!player.getInventory().checkItem(40581)) { // アンデッドのキー
				isTeleport = false;
				htmlid = "dilongn";
			}
		} else if (npcid == 50043) { // ラムダ
			if (_isNowDely) { // テレポートディレイ中
				isTeleport = false;
			}
		} else if (npcid == 50625) { // 古代人（Lv50クエスト古代の空間2F）
			if (_isNowDely) { // テレポートディレイ中
				isTeleport = false;
			}
		}

		if (isTeleport) { // テレポート実行
			try {
				// ミュータントアントダンジョン(君主Lv30クエスト)
				if (action.equalsIgnoreCase("teleport mutant-dungen")) {
					// 3マス以内のPc
					for (PcInstance otherPc : LsimulatorWorld.getInstance()
							.getVisiblePlayer(player, 3)) {
						if (otherPc.getClanid() == player.getClanid()
								&& otherPc.getId() != player.getId()) {
							LsimulatorTeleport.teleport(otherPc, 32740, 32800,
									(short) 217, 5, true);
						}
					}
					LsimulatorTeleport.teleport(player, 32740, 32800, (short) 217, 5,
							true);
				}
				// 試練のダンジョン（ウィザードLv30クエスト）
				else if (action.equalsIgnoreCase("teleport mage-quest-dungen")) {
					LsimulatorTeleport.teleport(player, 32791, 32788, (short) 201, 5,
							true);
				} else if (action.equalsIgnoreCase("teleport 29")) { // ラムダ
					PcInstance kni = null;
					PcInstance elf = null;
					PcInstance wiz = null;
					// 3マス以内のPc
					for (PcInstance otherPc : LsimulatorWorld.getInstance()
							.getVisiblePlayer(player, 3)) {
						LsimulatorQuest quest = otherPc.getQuest();
						if (otherPc.isKnight() // ナイト
								&& quest.get_step(LsimulatorQuest.QUEST_LEVEL50) == 1) { // ディガルディン同意済み
							if (kni == null) {
								kni = otherPc;
							}
						} else if (otherPc.isElf() // エルフ
								&& quest.get_step(LsimulatorQuest.QUEST_LEVEL50) == 1) { // ディガルディン同意済み
							if (elf == null) {
								elf = otherPc;
							}
						} else if (otherPc.isWizard() // ウィザード
								&& quest.get_step(LsimulatorQuest.QUEST_LEVEL50) == 1) { // ディガルディン同意済み
							if (wiz == null) {
								wiz = otherPc;
							}
						}
					}
					if (kni != null && elf != null && wiz != null) { // 全クラス揃っている
						LsimulatorTeleport.teleport(player, 32723, 32850, (short) 2000,
								2, true);
						LsimulatorTeleport.teleport(kni, 32750, 32851, (short) 2000, 6,
								true);
						LsimulatorTeleport.teleport(elf, 32878, 32980, (short) 2000, 6,
								true);
						LsimulatorTeleport.teleport(wiz, 32876, 33003, (short) 2000, 0,
								true);
						TeleportDelyTimer timer = new TeleportDelyTimer();
						GeneralThreadPool.getInstance().execute(timer);
					}
				} else if (action.equalsIgnoreCase("teleport barlog")) { // 古代人（Lv50クエスト古代の空間2F）
					LsimulatorTeleport.teleport(player, 32755, 32844, (short) 2002, 5,
							true);
					TeleportDelyTimer timer = new TeleportDelyTimer();
					GeneralThreadPool.getInstance().execute(timer);
				}
			} catch (Exception e) {
			}
		}
		if (htmlid != null) { // 表示するhtmlがある場合
			player.sendPackets(new S_NPCTalkReturn(objid, htmlid));
		}
	}

	class TeleportDelyTimer implements Runnable {

		public TeleportDelyTimer() {
		}

		public void run() {
			try {
				_isNowDely = true;
				Thread.sleep(900000); // 15分
			} catch (Exception e) {
				_isNowDely = false;
			}
			_isNowDely = false;
		}
	}

	private boolean _isNowDely = false;
	private static Logger _log = Logger
			.getLogger(Lsimulator.server.server.model.Instance.TeleporterInstance.class
					.getName());

}