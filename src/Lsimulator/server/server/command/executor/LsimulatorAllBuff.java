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
package Lsimulator.server.server.command.executor;

import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ADDITIONAL_FIRE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ADVANCE_SPIRIT;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.AQUA_PROTECTER;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BERSERKERS;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BLESS_WEAPON;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BOUNCE_ATTACK;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BRAVE_AURA;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BURNING_SPIRIT;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.BURNING_WEAPON;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.CLEAR_MIND;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.DECREASE_WEIGHT;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.DOUBLE_BRAKE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.DRESS_EVASION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ELEMENTAL_FIRE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ELEMENTAL_PROTECTION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.ENCHANT_VENOM;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.EXOTIC_VITALIZE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.GLOWING_AURA;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.IMMUNE_TO_HARM;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.IRON_SKIN;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.LIGHT;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.MEDITATION;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.PHYSICAL_ENCHANT_DEX;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.PHYSICAL_ENCHANT_STR;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.REDUCTION_ARMOR;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.RESIST_MAGIC;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SOLID_CARRIAGE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.SOUL_OF_FLAME;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.UNCANNY_DODGE;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.VENOM_RESIST;
import static Lsimulator.server.server.model.skill.LsimulatorSkillId.WATER_LIFE;

import java.util.StringTokenizer;

import Lsimulator.server.server.datatables.SkillsTable;
import Lsimulator.server.server.model.LsimulatorPolyMorph;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.model.skill.LsimulatorBuffUtil;
import Lsimulator.server.server.model.skill.LsimulatorSkillUse;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.serverpackets.S_SystemMessage;
import Lsimulator.server.server.templates.LsimulatorSkills;

/**
 * GM指令：給對象所有魔法
 */
public class LsimulatorAllBuff implements LsimulatorCommandExecutor {
	private LsimulatorAllBuff() {
	}

	public static LsimulatorCommandExecutor getInstance() {
		return new LsimulatorAllBuff();
	}

	@Override
	public void execute(LsimulatorPcInstance pc, String cmdName, String arg) {
		int[] allBuffSkill =
		{ LIGHT, DECREASE_WEIGHT, PHYSICAL_ENCHANT_DEX, MEDITATION, PHYSICAL_ENCHANT_STR, BLESS_WEAPON, BERSERKERS, IMMUNE_TO_HARM, ADVANCE_SPIRIT,
				REDUCTION_ARMOR, BOUNCE_ATTACK, SOLID_CARRIAGE, ENCHANT_VENOM, BURNING_SPIRIT, VENOM_RESIST, DOUBLE_BRAKE, UNCANNY_DODGE,
				DRESS_EVASION, GLOWING_AURA, BRAVE_AURA, RESIST_MAGIC, CLEAR_MIND, ELEMENTAL_PROTECTION, AQUA_PROTECTER, BURNING_WEAPON, IRON_SKIN,
				EXOTIC_VITALIZE, WATER_LIFE, ELEMENTAL_FIRE, SOUL_OF_FLAME, ADDITIONAL_FIRE };
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String name = st.nextToken();
			LsimulatorPcInstance target = LsimulatorWorld.getInstance().getPlayer(name);
			if (target == null) {
				pc.sendPackets(new S_ServerMessage(73, name)); // \f1%0はゲームをしていません。
				return;
			}

			LsimulatorBuffUtil.haste(target, 3600 * 1000);
			LsimulatorBuffUtil.brave(target, 3600 * 1000);
			LsimulatorPolyMorph.doPoly(target, 5641, 7200, LsimulatorPolyMorph.MORPH_BY_GM);
			for (int element : allBuffSkill) {
				LsimulatorSkills skill = SkillsTable.getInstance().getTemplate(element);
				new LsimulatorSkillUse().handleCommands(target, element, target.getId(), target.getX(), target.getY(), null, skill.getBuffDuration() * 1000,
						LsimulatorSkillUse.TYPE_GMBUFF);
			}
		}
		catch (Exception e) {
			pc.sendPackets(new S_SystemMessage("請輸入 .allBuff 玩家名稱。"));
		}
	}
}
