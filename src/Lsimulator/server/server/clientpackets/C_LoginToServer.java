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

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_0_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_0_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_6_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_1_6_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_0_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_0_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_6_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_2_6_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_0_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_0_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_6_N;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_3_6_S;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_BEGIN;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_BLOODSTAIN_OF_ANTHARAS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_BLOODSTAIN_OF_FAFURION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EFFECT_END;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_THIRD_SPEED;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.COOKING_WONDER_DRUG;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.MIRROR_IMAGE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SHAPE_CHANGE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_BLUE_POTION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_BRAVE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_BRAVE2;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_CHAT_PROHIBITED;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_ELFBRAVE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_HASTE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.STATUS_RIBRAVE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.UNCANNY_DODGE;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.Config;
import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.Account;
import Lsimulator.server.server.ActionCodes;
import Lsimulator.server.server.ClientThread;
import Lsimulator.server.server.WarTimeController;
import Lsimulator.server.server.datatables.CharacterTable;
import Lsimulator.server.server.datatables.ClanRecommendTable;
import Lsimulator.server.server.datatables.GetBackRestartTable;
import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.model.Getback;
import Lsimulator.server.server.model.LsimulatorCastleLocation;
import Lsimulator.server.server.model.LsimulatorClan;
import Lsimulator.server.server.model.LsimulatorCooking;
import Lsimulator.server.server.model.LsimulatorPolyMorph;
import Lsimulator.server.server.model.LsimulatorWar;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.PcInstance;
import Lsimulator.server.server.model.Instance.SummonInstance;
import Lsimulator.server.server.model.skill.LsimulatorBuffUtil;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_ActiveSpells;
import Lsimulator.server.server.serverpackets.S_AddSkill;
import Lsimulator.server.server.serverpackets.S_Bookmarks;
import Lsimulator.server.server.serverpackets.S_CharTitle;
import Lsimulator.server.server.serverpackets.S_CharacterConfig;
import Lsimulator.server.server.serverpackets.S_ClanAttention;
import Lsimulator.server.server.serverpackets.S_InitialAbilityGrowth;
import Lsimulator.server.server.serverpackets.S_InvList;
import Lsimulator.server.server.serverpackets.S_Karma;
import Lsimulator.server.server.serverpackets.S_Liquor;
import Lsimulator.server.server.serverpackets.S_LoginGame;
import Lsimulator.server.server.serverpackets.S_Mail;
import Lsimulator.server.server.serverpackets.S_MapID;
import Lsimulator.server.server.serverpackets.S_OwnCharPack;
import Lsimulator.server.server.serverpackets.S_OwnCharStatus;
import Lsimulator.server.server.serverpackets.S_OwnCharStatus2;
import Lsimulator.server.server.serverpackets.S_PacketBox;
import Lsimulator.server.server.serverpackets.S_RuneSlot;
import Lsimulator.server.server.serverpackets.S_SPMR;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SkillBrave;
import Lsimulator.server.server.serverpackets.S_SkillHaste;
import Lsimulator.server.server.serverpackets.S_SkillIconGFX;
import Lsimulator.server.server.serverpackets.S_SkillIconThirdSpeed;
import Lsimulator.server.server.serverpackets.S_SummonPack;
import Lsimulator.server.server.serverpackets.S_War;
import Lsimulator.server.server.serverpackets.S_Weather;
import Lsimulator.server.server.serverpackets.S_bonusstats;
import Lsimulator.server.server.templates.LsimulatorGetBackRestart;
import Lsimulator.server.server.templates.LsimulatorSkills;
import Lsimulator.server.server.utils.SQLUtil;

// Referenced classes of package Lsimulator.server.server.clientpackets:
// ClientBasePacket

/**
 * 處理收到由客戶端傳來登入到伺服器的封包
 */
public class C_LoginToServer extends ClientBasePacket {

	private static final String C_LOGIN_TO_SERVER = "[C] C_LoginToServer";

	private static Logger _log = Logger.getLogger(C_LoginToServer.class.getName());

	public C_LoginToServer(byte abyte0[], ClientThread client) throws FileNotFoundException, Exception {
		super(abyte0);

		String login = client.getAccountName();

		String charName = readS();

		if (client.getActiveChar() != null) {
			_log.info("同一個角色重複登入，強制切斷 " + client.getHostname() + ") 的連結");
			client.close();
			return;
		}

		PcInstance pc = PcInstance.load(charName);
		Account account = Account.load(pc.getAccountName());
		if (account.isOnlineStatus()) {
			_log.info("同一個帳號雙重角色登入，強制切斷 " + client.getHostname() + ") 的連結");
			client.close();
			return;
		}
		if ((pc == null) || !login.equals(pc.getAccountName())) {
			_log.info("無效的角色名稱: char=" + charName + " account=" + login + " host=" + client.getHostname());
			client.close();
			return;
		}

		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (pc.getHighLevel() - pc.getLevel() >= Config.LEVEL_DOWN_RANGE) {
				_log.info("登錄請求超出了容忍的等級下降的角色: char=" + charName + " account=" + login + " host=" + client.getHostname());
				client.kick();
				return;
			}
		}

		_log.info("角色登入到伺服器中: char=" + charName + " account=" + login+ " host=" + client.getHostname());
		
		int currentHpAtLoad = pc.getCurrentHp();
		int currentMpAtLoad = pc.getCurrentMp();
		pc.clearSkillMastery();
		pc.setOnlineStatus(1);
		CharacterTable.updateOnlineStatus(pc);
		LsimulatorWorld.getInstance().storeObject(pc);

		pc.setNetConnection(client);
		pc.setPacketOutput(client);
		client.setActiveChar(pc);
		
		pc.sendPackets(new S_LoginGame(pc));
		
		Account.OnlineStatus(account, true); // OnlineStatus = 1

		// 如果設定檔中設定自動回村的話
		GetBackRestartTable gbrTable = GetBackRestartTable.getInstance();
		LsimulatorGetBackRestart[] gbrList = gbrTable.getGetBackRestartTableList();
		for (LsimulatorGetBackRestart gbr : gbrList) {
			if (pc.getMapId() == gbr.getArea()) {
				pc.setX(gbr.getLocX());
				pc.setY(gbr.getLocY());
				pc.setMap(gbr.getMapId());
				break;
			}
		}

		// altsettings.properties 中 GetBack 設定為 true 就自動回村
		if (Config.GET_BACK) {
			int[] loc = Getback.GetBack_Location(pc, true);
			pc.setX(loc[0]);
			pc.setY(loc[1]);
			pc.setMap((short) loc[2]);
		}

		// 如果標記是在戰爭期間，如果不是血盟成員回到城堡。
		int castle_id = LsimulatorCastleLocation.getCastleIdByArea(pc);
		if (0 < castle_id) {
			if (WarTimeController.getInstance().isNowWar(castle_id)) {
				LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
				if (clan != null) {
					if (clan.getCastleId() != castle_id) {
						// 沒有城堡
						int[] loc = new int[3];
						loc = LsimulatorCastleLocation.getGetBackLoc(castle_id);
						pc.setX(loc[0]);
						pc.setY(loc[1]);
						pc.setMap((short) loc[2]);
					}
				} else {
					// 有城堡就回到城堡
					int[] loc = new int[3];
					loc = LsimulatorCastleLocation.getGetBackLoc(castle_id);
					pc.setX(loc[0]);
					pc.setY(loc[1]);
					pc.setMap((short) loc[2]);
				}
			}
		}
		LsimulatorWorld.getInstance().addVisibleObject(pc);
		
		if (Config.CHARACTER_CONFIG_IN_SERVER_SIDE) {
			pc.sendPackets(new S_CharacterConfig(pc.getId()));
		}
	
		items(pc);
		
		pc.sendPackets(new S_Mail(pc , 0));
		pc.sendPackets(new S_Mail(pc , 1));
		pc.sendPackets(new S_Mail(pc , 2));
		
		pc.beginGameTimeCarrier();
		
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_CLOSE_SLOT, 3)); // 符文關閉欄位數
		pc.sendPackets(new S_RuneSlot(S_RuneSlot.RUNE_OPEN_SLOT, 1)); // 符文開放欄位數
		
		pc.setEquipments();
		
		pc.sendPackets(new S_ActiveSpells(pc));
		
		pc.sendPackets(new S_Bookmarks(pc));
		
		pc.sendPackets(new S_OwnCharStatus(pc));
		
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));
		
		pc.sendPackets(new S_OwnCharPack(pc));

		pc.sendPackets(new S_SPMR(pc));

		S_CharTitle s_charTitle = new S_CharTitle(pc.getId(), pc.getTitle());
		pc.sendPackets(s_charTitle);
		pc.broadcastPacket(s_charTitle);

		pc.sendVisualEffectAtLogin(); // 皇冠，毒，水和其他視覺效果顯示
		
		pc.sendPackets(new S_OwnCharStatus2(pc, 1)); // 角色初始素質 
		
		pc.sendPackets(new S_InitialAbilityGrowth(pc)); // 角色狀態獎勵 

		pc.sendPackets(new S_Weather(LsimulatorWorld.getInstance().getWeather()));

		skills(pc);
		
		buff(client, pc);
		
		pc.turnOnOffLight();

		pc.sendPackets(new S_Karma(pc)); // 友好度
		
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_PLUS, pc.getDodge())); // 閃避率 正
		pc.sendPackets(new S_PacketBox(S_PacketBox.DODGE_RATE_MINUS, pc.getNdodge())); // 閃避率 負
		
		checkPledgeRecommendation(pc);

		if (pc.getCurrentHp() > 0) {
			pc.setDead(false);
			pc.setStatus(0);
		} else {
			pc.setDead(true);
			pc.setStatus(ActionCodes.ACTION_Die);
		}

		if ((pc.getLevel() >= 51) && (pc.getLevel() - 50 > pc.getBonusStats())) {
			if ((pc.getBaseStr() + pc.getBaseDex() + pc.getBaseCon()+ pc.getBaseInt() + pc.getBaseWis() + pc.getBaseCha()) < 210) {
				pc.sendPackets(new S_bonusstats(pc.getId(), 1));
			}
		}

		serchSummon(pc);

		WarTimeController.getInstance().checkCastleWar(pc);

		if (pc.getClanid() != 0) { // 有血盟
			LsimulatorClan clan = LsimulatorWorld.getInstance().getClan(pc.getClanname());
			if (clan != null) {
				if ((pc.getClanid() == clan.getClanId()) && // 血盟解散、又重新用同樣名字創立時的對策
						pc.getClanname().toLowerCase().equals(clan.getClanName().toLowerCase())) {
					PcInstance[] clanMembers = clan.getOnlineClanMember();
					for (PcInstance clanMember : clanMembers) {
						if (clanMember.getId() != pc.getId()) {
							clanMember.sendPackets(new S_ServerMessage(843, pc.getName())); // 只今、血盟員の%0%sがゲームに接続しました。
						}
					}

					// 取得所有的盟戰
					for (LsimulatorWar war : LsimulatorWorld.getInstance().getWarList()) {
						boolean ret = war.CheckClanInWar(pc.getClanname());
						if (ret) { // 盟戰中
							String enemy_clan_name = war.GetEnemyClanName(pc.getClanname());
							if (enemy_clan_name != null) {
								// あなたの血盟が現在_血盟と交戦中です。
								pc.sendPackets(new S_War(8, pc.getClanname(),enemy_clan_name));
							}
							break;
						}
					}
				} else {
					pc.setClanid(0);
					pc.setClanname("");
					pc.setClanRank(0);
					pc.save(); // 儲存玩家的資料到資料庫中
				}
			}
		}

		if (pc.getPartnerId() != 0) { // 結婚中
			PcInstance partner = (PcInstance) LsimulatorWorld.getInstance().findObject(pc.getPartnerId());
			if ((partner != null) && (partner.getPartnerId() != 0)) {
				if ((pc.getPartnerId() == partner.getId())&& (partner.getPartnerId() == pc.getId())) {
					pc.sendPackets(new S_ServerMessage(548)); // あなたのパートナーは今ゲーム中です。
					partner.sendPackets(new S_ServerMessage(549)); // あなたのパートナーはたった今ログインしました。
				}
			}
		}

		if (currentHpAtLoad > pc.getCurrentHp()) {
			pc.setCurrentHp(currentHpAtLoad);
		}
		if (currentMpAtLoad > pc.getCurrentMp()) {
			pc.setCurrentMp(currentMpAtLoad);
		}
		pc.startHpRegeneration();
		pc.startMpRegeneration();
		pc.startObjectAutoUpdate();
		pc.setCryOfSurvivalTime();
		client.CharReStart(false);
		pc.beginExpMonitor();
		pc.save(); // 儲存玩家的資料到資料庫中

		if (pc.getHellTime() > 0) {
			pc.beginHell(false);
		}

		// 處理新手保護系統(遭遇的守護)狀態資料的變動
		pc.checkNoviceType();
	}

	private void items(PcInstance pc) {
		// 從資料庫中讀取角色的道具
		CharacterTable.getInstance().restoreInventory(pc);

		pc.sendPackets(new S_InvList(pc.getInventory().getItems()));
	}

	private void skills(PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_skills WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			int i = 0;
			int lv1 = 0;
			int lv2 = 0;
			int lv3 = 0;
			int lv4 = 0;
			int lv5 = 0;
			int lv6 = 0;
			int lv7 = 0;
			int lv8 = 0;
			int lv9 = 0;
			int lv10 = 0;
			int lv11 = 0;
			int lv12 = 0;
			int lv13 = 0;
			int lv14 = 0;
			int lv15 = 0;
			int lv16 = 0;
			int lv17 = 0;
			int lv18 = 0;
			int lv19 = 0;
			int lv20 = 0;
			int lv21 = 0;
			int lv22 = 0;
			int lv23 = 0;
			int lv24 = 0;
			int lv25 = 0;
			int lv26 = 0;
			int lv27 = 0;
			int lv28 = 0;
			while (rs.next()) {
				int skillId = rs.getInt("skill_id");
				LsimulatorSkills l1skills = SkillsTable.getInstance().getTemplate(
						skillId);
				if (l1skills.getSkillLevel() == 1) {
					lv1 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 2) {
					lv2 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 3) {
					lv3 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 4) {
					lv4 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 5) {
					lv5 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 6) {
					lv6 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 7) {
					lv7 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 8) {
					lv8 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 9) {
					lv9 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 10) {
					lv10 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 11) {
					lv11 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 12) {
					lv12 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 13) {
					lv13 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 14) {
					lv14 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 15) {
					lv15 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 16) {
					lv16 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 17) {
					lv17 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 18) {
					lv18 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 19) {
					lv19 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 20) {
					lv20 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 21) {
					lv21 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 22) {
					lv22 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 23) {
					lv23 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 24) {
					lv24 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 25) {
					lv25 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 26) {
					lv26 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 27) {
					lv27 |= l1skills.getId();
				}
				if (l1skills.getSkillLevel() == 28) {
					lv28 |= l1skills.getId();
				}
				i = lv1 + lv2 + lv3 + lv4 + lv5 + lv6 + lv7 + lv8 + lv9 + lv10
						+ lv11 + lv12 + lv13 + lv14 + lv15 + lv16 + lv17 + lv18
						+ lv19 + lv20 + lv21 + lv22 + lv23 + lv24 + lv25 + lv26
						+ lv27 + lv28;
				pc.setSkillMastery(skillId);
			}
			if (i > 0) {
				pc.sendPackets(new S_AddSkill(lv1, lv2, lv3, lv4, lv5, lv6,
						lv7, lv8, lv9, lv10, lv11, lv12, lv13, lv14, lv15,
						lv16, lv17, lv18, lv19, lv20, lv21, lv22, lv23, lv24,
						lv25, lv26, lv27, lv28));
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	private void serchSummon(PcInstance pc) {
		for (SummonInstance summon : LsimulatorWorld.getInstance().getAllSummons()) {
			if (summon.getMaster().getId() == pc.getId()) {
				summon.setMaster(pc);
				pc.addPet(summon);
				for (PcInstance visiblePc : LsimulatorWorld.getInstance().getVisiblePlayer(summon)) {
					visiblePc.sendPackets(new S_SummonPack(summon, visiblePc));
				}
			}
		}
	}

	private void buff(ClientThread clientthread, PcInstance pc) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM character_buff WHERE char_obj_id=?");
			pstm.setInt(1, pc.getId());
			rs = pstm.executeQuery();
			while (rs.next()) {
				int skillid = rs.getInt("skill_id");
				int remaining_time = rs.getInt("remaining_time");
				int time = 0;
				switch (skillid) {
				case SHAPE_CHANGE: // 變身
					int poly_id = rs.getInt("poly_id");
					LsimulatorPolyMorph.doPoly(pc, poly_id, remaining_time,LsimulatorPolyMorph.MORPH_BY_LOGIN);
					break;
				case STATUS_BRAVE: // 勇敢藥水
					pc.sendPackets(new S_SkillBrave(pc.getId(), 1,remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
					pc.setBraveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_ELFBRAVE: // 精靈餅乾
					pc.sendPackets(new S_SkillBrave(pc.getId(), 3,remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 3, 0));
					pc.setBraveSpeed(3);
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_BRAVE2: // 超級加速
					pc.sendPackets(new S_SkillBrave(pc.getId(), 5,remaining_time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 5, 0));
					pc.setBraveSpeed(5);
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_HASTE: // 加速
					pc.sendPackets(new S_SkillHaste(pc.getId(), 1,remaining_time));
					pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
					pc.setMoveSpeed(1);
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_BLUE_POTION: // 藍色藥水
					pc.sendPackets(new S_SkillIconGFX(34, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_CHAT_PROHIBITED: // 禁言
					pc.sendPackets(new S_SkillIconGFX(36, remaining_time));
					pc.setSkillEffect(skillid, remaining_time * 1000);
					break;
				case STATUS_THIRD_SPEED: // 三段加速
					time = remaining_time >> 2 ;
					pc.sendPackets(new S_Liquor(pc.getId(), 8)); // 人物 *
																	// 1.15
					pc.broadcastPacket(new S_Liquor(pc.getId(), 8)); // 人物 *
																		// 1.15
					pc.sendPackets(new S_SkillIconThirdSpeed(time));
					pc.setSkillEffect(skillid, ( time << 2  ) * 1000);
					break;
				case MIRROR_IMAGE: // 鏡像
				case UNCANNY_DODGE: // 暗影閃避
					time = remaining_time >> 4 ;
					pc.addDodge((byte) 5); // 閃避率 + 50%
					// 更新閃避率顯示
					pc.sendPackets(new S_PacketBox(88, pc.getDodge()));
					pc.sendPackets(new S_PacketBox(21, time));
					pc.setSkillEffect(skillid, ( time  * 1000 ) << 4 );
					break;
				case EFFECT_BLOODSTAIN_OF_ANTHARAS: // 安塔瑞斯的血痕
					remaining_time = remaining_time / 60;
					if (remaining_time != 0) {
						LsimulatorBuffUtil.bloodstain(pc, (byte) 0, remaining_time,
								false);
					}
					break;
				case EFFECT_BLOODSTAIN_OF_FAFURION: // 法利昂的血痕
					remaining_time = remaining_time / 60;
					if (remaining_time != 0) {
						LsimulatorBuffUtil.bloodstain(pc, (byte) 1, remaining_time,
								false);
					}
					break;
				default:
					// 魔法料理
					if (((skillid >= COOKING_1_0_N) && (skillid <= COOKING_1_6_N))
							|| ((skillid >= COOKING_1_0_S) && (skillid <= COOKING_1_6_S))
							|| ((skillid >= COOKING_2_0_N) && (skillid <= COOKING_2_6_N))
							|| ((skillid >= COOKING_2_0_S) && (skillid <= COOKING_2_6_S))
							|| ((skillid >= COOKING_3_0_N) && (skillid <= COOKING_3_6_N))
							|| ((skillid >= COOKING_3_0_S) && (skillid <= COOKING_3_6_S))) {
						LsimulatorCooking.eatCooking(pc, skillid, remaining_time);
					}
					// 生命之樹果實、商城道具
					else if (skillid == STATUS_RIBRAVE
							|| (skillid >= EFFECT_BEGIN && skillid <= EFFECT_END)
							|| skillid == COOKING_WONDER_DRUG) {
						;
					} else {
						LsimulatorSkillUse l1skilluse = new LsimulatorSkillUse();
						l1skilluse.handleCommands(clientthread.getActiveChar(),
								skillid, pc.getId(), pc.getX(), pc.getY(),
								null, remaining_time, LsimulatorSkillUse.TYPE_LOGIN);
					}
					break;
				}
			}
		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}
	
	private void checkPledgeRecommendation(PcInstance pc){
		if(pc.getClanid() > 0){ 
			pc.sendPackets(new S_ClanAttention());
			pc.sendPackets(new S_PacketBox(S_PacketBox.PLEDGE_EMBLEM_STATUS, pc.getClan().getEmblemStatus()));
			if(pc.getClanRank() == LsimulatorClan.CLAN_RANK_PRINCE || pc.getClanRank() == LsimulatorClan.CLAN_RANK_GUARDIAN
			 ||pc.getClanRank() == LsimulatorClan.CLAN_RANK_LEAGUE_GUARDIAN || pc.getClanRank() == LsimulatorClan.CLAN_RANK_LEAGUE_VICEPRINCE
		     ||pc.getClanRank() == LsimulatorClan.CLAN_RANK_LEAGUE_PRINCE){
				// 有登錄，結束
				if(ClanRecommendTable.getInstance().isRecorded(pc.getClanid())){
					// 是否有人申請
					if(ClanRecommendTable.getInstance().isClanApplyByPlayer(pc.getClanid())){
						pc.sendPackets(new S_ServerMessage(3248));
					}
				} else {
					// 吳登錄
					pc.sendPackets(new S_PacketBox(S_PacketBox.PLEDGE_EMBLEM_STATUS, pc.getClan().getEmblemStatus()));
					pc.sendPackets(new S_ClanAttention());
					pc.sendPackets(new S_ServerMessage(3246));
				}
			}
		} else {
			if(pc.isCrown()){
				pc.sendPackets(new S_ServerMessage(3247));
			} else {
				// 如果有登記 不發送
				if(ClanRecommendTable.getInstance().isApplied(pc.getName())){
					
				} else {
					// 沒有登記
					pc.sendPackets(new S_ServerMessage(3245));
				}
			}
		}
		
	}
	

	@Override
	public String getType() {
		return C_LOGIN_TO_SERVER;
	}
}
