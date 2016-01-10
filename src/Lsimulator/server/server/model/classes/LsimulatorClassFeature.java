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
package Lsimulator.server.server.model.classes;

import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;

public abstract class LsimulatorClassFeature {
	public static LsimulatorClassFeature newClassFeature(int classId) {
		if (classId == LsimulatorPcInstance.CLASSID_PRINCE
				|| classId == LsimulatorPcInstance.CLASSID_PRINCESS) {
			return new LsimulatorRoyalClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_ELF_MALE
				|| classId == LsimulatorPcInstance.CLASSID_ELF_FEMALE) {
			return new LsimulatorElfClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_KNIGHT_MALE
				|| classId == LsimulatorPcInstance.CLASSID_KNIGHT_FEMALE) {
			return new LsimulatorKnightClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_WIZARD_MALE
				|| classId == LsimulatorPcInstance.CLASSID_WIZARD_FEMALE) {
			return new LsimulatorWizardClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_DARK_ELF_MALE
				|| classId == LsimulatorPcInstance.CLASSID_DARK_ELF_FEMALE) {
			return new LsimulatorDarkElfClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_DRAGON_KNIGHT_MALE
				|| classId == LsimulatorPcInstance.CLASSID_DRAGON_KNIGHT_FEMALE) {
			return new LsimulatorDragonKnightClassFeature();
		}
		if (classId == LsimulatorPcInstance.CLASSID_ILLUSIONIST_MALE
				|| classId == LsimulatorPcInstance.CLASSID_ILLUSIONIST_FEMALE) {
			return new LsimulatorIllusionistClassFeature();
		}
		throw new IllegalArgumentException();
	}

	public abstract int getAcDefenseMax(int ac);

	public abstract int getMagicLevel(int playerLevel);
}