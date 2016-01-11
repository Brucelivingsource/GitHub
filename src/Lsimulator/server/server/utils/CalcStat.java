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

import Lsimulator.server.Config;

public class CalcStat {

	private CalcStat() {

	}

	/**
	 * ACボーナスを返す
	 * 
	 * @param level
	 * @param dex
	 * @return acBonus
	 * 
	 */
	public static int calcAc(int level, int dex) {
		int acBonus = 10;
		if (dex <= 9) {
			acBonus -= ( level >> 3 );
		} else if (dex >= 10 && dex <= 12) {
			acBonus -= level / 7;
		} else if (dex >= 13 && dex <= 15) {
			acBonus -= level / 6;
		} else if (dex >= 16 && dex <= 17) {
			acBonus -= level / 5;
		} else if (dex >= 18) {
			acBonus -= ( level  >> 2 ) ;
		}
		return acBonus;
	}

	/**
	 * <b> 傳回精 wis 對應的抗魔值 </b>
	 * 
	 * @param wis 精神點數
	 *            
	 * @return mrBonus 抗魔值
	 */
	public static int calcStatMr(int wis) {
		int mrBonus = 0;
		if (wis <= 14) {
			mrBonus = 0;
		} else if (wis >= 15 && wis <= 16) {
			mrBonus = 3;
		} else if (wis == 17) {
			mrBonus = 6;
		} else if (wis == 18) {
			mrBonus = 10;
		} else if (wis == 19) {
			mrBonus = 15;
		} else if (wis == 20) {
			mrBonus = 21;
		} else if (wis == 21) {
			mrBonus = 28;
		} else if (wis == 22) {
			mrBonus = 37;
		} else if (wis == 23) {
			mrBonus = 47;
		} else if (wis >= 24 && wis <= 29) {
			mrBonus = 50;
		} else if (wis >= 30 && wis <= 34) {
			mrBonus = 52;
		} else if (wis >= 35 && wis <= 39) {
			mrBonus = 55;
		} else if (wis >= 40 && wis <= 43) {
			mrBonus = 59;
		} else if (wis >= 44 && wis <= 46) {
			mrBonus = 62;
		} else if (wis >= 47 && wis <= 49) {
			mrBonus = 64;
		} else if (wis == 50) {
			mrBonus = 65;
		} else {
			mrBonus = 65;
		}
		return mrBonus;
	}

	public static int calcDiffMr(int wis, int diff) {
		return calcStatMr(wis + diff) - calcStatMr(wis);
	}

	/**
	 * 等級上升HP上升值 ver7.6
	 * 2016 01 10
	 * @param charType
	 * @param baseMaxHp
	 * @param baseCon
	 * @param originalHpup
	 * @return HP上昇値
	 */
	public static short calcStatHp(int charType, int baseMaxHp, byte baseCon,
			int originalHpup) {
		short randomhp = (short) ((short)(Random.nextInt(2)) + baseCon);
                
                                    //按照職業熱門比例排序　妖精　法師　黑妖　騎士　龍騎　幻術　王族
		if (charType == 2) { // 妖精
                                           randomhp -= 2 ;  
                                            if (baseMaxHp + randomhp > Config.ELF_MAX_HP) 
                                                    randomhp = (short) (Config.ELF_MAX_HP - baseMaxHp);
                                     } else if (charType == 3) { // 法師
                                            randomhp  -= 5 ;  
                                            if (baseMaxHp + randomhp > Config.WIZARD_MAX_HP) 
			randomhp = (short) (Config.WIZARD_MAX_HP - baseMaxHp);
                                     }else if (charType == 4) { // 黑妖
                                            randomhp  -= 1 ;  
                                            if (baseMaxHp + randomhp > Config.DARKELF_MAX_HP) 
			randomhp = (short) (Config.DARKELF_MAX_HP - baseMaxHp);
                                     } else if (charType == 1) { // 騎士
                                           randomhp += 5;  
                                            if (baseMaxHp + randomhp > Config.KNIGHT_MAX_HP) 
                                                    randomhp = (short) (Config.KNIGHT_MAX_HP - baseMaxHp);
		} else if (charType == 5) { // 龍騎士
                                            randomhp += 1 ;  
                                            if (baseMaxHp + randomhp > Config.DRAGONKNIGHT_MAX_HP) 
                                                    randomhp = (short) (Config.DRAGONKNIGHT_MAX_HP - baseMaxHp);	
		} else if (charType == 6) { // 幻術師
                                            randomhp  -= 3 ;  
                                            if (baseMaxHp + randomhp > Config.ILLUSIONIST_MAX_HP)
			randomhp = (short) (Config.ILLUSIONIST_MAX_HP - baseMaxHp);
		}else if (charType == 0) { // 王族
                                            if ( baseCon <= 12 ) 
                                                    randomhp = (short) ((short)(Random.nextInt(2)) + 12 );
                                            if (baseMaxHp + randomhp > Config.PRINCE_MAX_HP) 
                                                    randomhp = (short) (Config.PRINCE_MAX_HP - baseMaxHp);
                                            
                                    }

		randomhp += originalHpup;

		if (randomhp < 0) {
			randomhp = 0;
		}
		return randomhp;
	}
                   
	/**2016 01 10
	 * 等級上升MP上升值 ver7.6 後面可能不準
	 * 
	 * @param charType
	 * @param baseMaxMp
	 * @param baseWis
	 * @param originalMpup
	 * @return MP上昇値
	 */
	public static short calcStatMp(int charType, int baseMaxMp, byte baseWis,
			int originalMpup) {
		int randommp = 0;
		 if (charType == 2) { // 妖精
                                     // 整數除以浮點數就變浮點數
                                            randommp = (int) (Random.nextInt( (int)(baseWis/3.0*1.5) - (int)(baseWis/5.0*1.5) )  + (int)(baseWis/5.0*1.5));
                                            if (baseMaxMp + randommp > Config.ELF_MAX_MP) 
			randommp = Config.ELF_MAX_MP - baseMaxMp;
		} else if (charType == 3) { // 法師
                                            randommp = Random.nextInt( ( baseWis<<1 ) /3 +2 - ( ( baseWis/5 +1) <<1) )  + ( baseWis<<1 )/3 +2  ;
                                            if (baseMaxMp + randommp > Config.WIZARD_MAX_MP) 
			randommp = Config.WIZARD_MAX_MP - baseMaxMp;
			
		} else if (charType == 4) { //黑妖
                                            randommp = (int) (Random.nextInt( (int)(baseWis/3.0*1.5) - (int)(baseWis/5.0*1.5) )  + (int)(baseWis/5.0*1.5));
                                            if (baseMaxMp + randommp > Config.DARKELF_MAX_MP) 
			randommp = Config.DARKELF_MAX_MP - baseMaxMp;
		
		}else if (charType == 1) { // 騎
                                            randommp = Random.nextInt( 2 )  + baseWis/10;
                                            if (baseMaxMp + randommp > Config.KNIGHT_MAX_MP)
                                                    randommp = Config.KNIGHT_MAX_MP - baseMaxMp;
                                     	
		} else if (charType == 5) { //龍騎
                                            randommp =  Random.nextInt(  baseWis/3-1  - baseWis/5  )  + baseWis/5 ;
                                            if (baseMaxMp + randommp > Config.DRAGONKNIGHT_MAX_MP)
			randommp = Config.DRAGONKNIGHT_MAX_MP - baseMaxMp;
			
		} else if (charType == 6) { // 幻術　有點誤差
                                            randommp =  Random.nextInt(  (baseWis<<1 )/3 - 1  - ( (baseWis/5) << 1 )  )  + ( ( baseWis/5) <<1 ) ;
                                            if (baseMaxMp + randommp > Config.ILLUSIONIST_MAX_MP)
			randommp = Config.ILLUSIONIST_MAX_MP - baseMaxMp;
		
                                    }else if (charType == 0) { // 王
                                            randommp = Random.nextInt( baseWis/5 -  baseWis/3 )  + baseWis/5+1;
                                            if (baseMaxMp + randommp > Config.PRINCE_MAX_MP)
                                                    randommp = Config.PRINCE_MAX_MP - baseMaxMp;
		
		} 

		randommp += originalMpup;

		if (randommp < 0) {
			randommp = 0;
		}
		return (short) randommp;
	}
}
