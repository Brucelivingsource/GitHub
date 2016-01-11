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

import Lsimulator.server.server.model.Instance.PcInstance;

public abstract class LsimulatorClassFeature {
	public static LsimulatorClassFeature newClassFeature(int classId) {
		if (classId == PcInstance.CLASSID_PRINCE
				|| classId == PcInstance.CLASSID_PRINCESS) {
			return new LsimulatorRoyalClassFeature();
		}
		if (classId == PcInstance.CLASSID_ELF_MALE
				|| classId == PcInstance.CLASSID_ELF_FEMALE) {
			return new LsimulatorElfClassFeature();
		}
		if (classId == PcInstance.CLASSID_KNIGHT_MALE
				|| classId == PcInstance.CLASSID_KNIGHT_FEMALE) {
			return new LsimulatorKnightClassFeature();
		}
		if (classId == PcInstance.CLASSID_WIZARD_MALE
				|| classId == PcInstance.CLASSID_WIZARD_FEMALE) {
			return new LsimulatorWizardClassFeature();
		}
		if (classId == PcInstance.CLASSID_DARK_ELF_MALE
				|| classId == PcInstance.CLASSID_DARK_ELF_FEMALE) {
			return new LsimulatorDarkElfClassFeature();
		}
		if (classId == PcInstance.CLASSID_DRAGON_KNIGHT_MALE
				|| classId == PcInstance.CLASSID_DRAGON_KNIGHT_FEMALE) {
			return new LsimulatorDragonKnightClassFeature();
		}
		if (classId == PcInstance.CLASSID_ILLUSIONIST_MALE
				|| classId == PcInstance.CLASSID_ILLUSIONIST_FEMALE) {
			return new LsimulatorIllusionistClassFeature();
		}
		throw new IllegalArgumentException();
	}

	public abstract int getAcDefenseMax(int ac);

	public abstract int getMagicLevel(int playerLevel);
}