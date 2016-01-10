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
package Lsimulator.server.server.utils.Internationalization;

import java.util.ListResourceBundle;

/**
 * @category 英美-英語<br>
 *           國際化的英文是Internationalization 因為單字中總共有18個字母，簡稱I18N，
 *           目的是讓應用程式可以應地區不同而顯示不同的訊息。
 */

public class messages_en_US extends ListResourceBundle {
	static final Object[][] contents = {
		{ "Lsimulator.server.memoryUse", "Used: " },
		{ "Lsimulator.server.memory", "MB of memory" },
		{ "Lsimulator.server.server.model.onGroundItem", "items on the ground" },
		{ "Lsimulator.server.server.model.seconds","will be delete after 10 seconds" },
		{ "Lsimulator.server.server.model.deleted", "was deleted" }, 
		{ "Lsimulator.server.server.GameServer.ver","" },
		{ "Lsimulator.server.server.GameServer.settingslist","●●●●〈Server Config List〉●●●●"},
		{ "Lsimulator.server.server.GameServer.exp","「exp」"},
		{ "Lsimulator.server.server.GameServer.x","【times】"},
		{ "Lsimulator.server.server.GameServer.level","【LV】"},
		{ "Lsimulator.server.server.GameServer.justice","「justice」"},
		{ "Lsimulator.server.server.GameServer.karma","「karma」"},
		{ "Lsimulator.server.server.GameServer.dropitems","「dropitems」"},
		{ "Lsimulator.server.server.GameServer.dropadena","「dropadena」"},
		{ "Lsimulator.server.server.GameServer.enchantweapon","「enchantweapon」"},
		{ "Lsimulator.server.server.GameServer.enchantarmor","「enchantarmor」"},
		{ "Lsimulator.server.server.GameServer.chatlevel","「chatLevel」"},
		{ "Lsimulator.server.server.GameServer.nonpvp1","「Non-PvP」: Not Work (PvP)"},
		{ "Lsimulator.server.server.GameServer.nonpvp2","「Non-PvP」: Work (Non-PvP)"},
		{ "Lsimulator.server.server.GameServer.maxplayer","Max connection limit "},
		{ "Lsimulator.server.server.GameServer.player"," players"},
		{ "Lsimulator.server.server.GameServer.waitingforuser","Waiting for user's connection..."},
		{ "Lsimulator.server.server.GameServer.from","from "},
		{ "Lsimulator.server.server.GameServer.attempt"," attempt to connect."},
		{ "Lsimulator.server.server.GameServer.setporton","Server is successfully set on port "},
		{ "Lsimulator.server.server.GameServer.initialfinished","Initialize finished.."}};

	@Override
	protected Object[][] getContents() {
		return contents;
	}

}
