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

import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;

public class CalcInitHpMp {

	private CalcInitHpMp() {
	}

	/**
	 * 各職業的初始HP返回
	 * 
	 * @param pc
	 * @return hp
	 * 
	 */
	public static int calcInitHp(LsimulatorPcInstance pc) {
		int hp = 1;
		if (pc.isCrown()) {
			hp = 14;
		} else if (pc.isKnight()) {
			hp = 16;
		} else if (pc.isElf()) {
			hp = 15;
		} else if (pc.isWizard()) {
			hp = 12;
		} else if (pc.isDarkelf()) {
			hp = 12;
		} else if (pc.isDragonKnight()) { // 3.70C 異動 15->16
			hp = 16;
		} else if (pc.isIllusionist()) {  // 3.70C 異動 15->14
			hp = 14;     
		}
		return hp;
	}

	/**
	 * 各職業的初始MP返回
	 * 
	 * @param pc
	 * @return mp
	 * 
	 */
	public static int calcInitMp(LsimulatorPcInstance pc) {
		int mp = 1;
		if (pc.isCrown()) {
			switch (pc.getWis()) {
			case 11:
				mp = 2;
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 3;
				break;
			case 16:
			case 17:
			case 18:
				mp = 4;
				break;
			default:
				mp = 2;
				break;
			}
		} else if (pc.isKnight()) {
			switch (pc.getWis()) {
			case 9:
			case 10:
			case 11:
				mp = 1;
				break;
			case 12:
			case 13:
				mp = 2;
				break;
			default:
				mp = 1;
				break;
			}
		} else if (pc.isElf()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 4;
				break;
			}
		} else if (pc.isWizard()) {
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 6;
				break;
			case 16:
			case 17:
			case 18:
				mp = 8;
				break;
			default:
				mp = 6;
				break;
			}
		} else if (pc.isDarkelf()) {
			switch (pc.getWis()) {
			case 10:
			case 11:
				mp = 3;
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 4;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 3;
				break;
			}
		} else if (pc.isDragonKnight()) { // 3.70C 異動
			mp = 2;
		} else if (pc.isIllusionist()) {  // 3.70C 異動
			switch (pc.getWis()) {
			case 12:
			case 13:
			case 14:
			case 15:
				mp = 5;
				break;
			case 16:
			case 17:
			case 18:
				mp = 6;
				break;
			default:
				mp = 5;
				break;
			}
		}
		return mp;
	}

}