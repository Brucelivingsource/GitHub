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

import java.util.List;

import Lsimulator.server.Config;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.serverpackets.S_HPMeter;
import Lsimulator.server.server.serverpackets.S_Party;
import Lsimulator.server.server.serverpackets.S_ServerMessage;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorParty

public class LsimulatorParty {
	private final List<LsimulatorPcInstance> _membersList = Lists.newList();

	private LsimulatorPcInstance _leader = null;

	public void addMember(LsimulatorPcInstance pc) {
		if (pc == null) {
			throw new NullPointerException();
		}
		if (((_membersList.size() == Config.MAX_PT) && !_leader.isGm()) || _membersList.contains(pc)) {
			return;
		}

		if (_membersList.isEmpty()) {
			// 最初のPTメンバーであればリーダーにする
			setLeader(pc);
		} else {
			createMiniHp(pc);
		}

		_membersList.add(pc);
		pc.setParty(this);
		showAddPartyInfo(pc);
		pc.startRefreshParty();
	}

	private void removeMember(LsimulatorPcInstance pc) {
		if (!_membersList.contains(pc)) {
			return;
		}
		pc.stopRefreshParty();
		_membersList.remove(pc);
		pc.setParty(null);
		if (!_membersList.isEmpty()) {
			deleteMiniHp(pc);
		}
	}

	public boolean isVacancy() {
		return _membersList.size() < Config.MAX_PT;
	}

	public int getVacancy() {
		return Config.MAX_PT - _membersList.size();
	}

	public boolean isMember(LsimulatorPcInstance pc) {
		return _membersList.contains(pc);
	}

	private void setLeader(LsimulatorPcInstance pc) {
		_leader = pc;
	}

	public LsimulatorPcInstance getLeader() {
		return _leader;
	}

	public boolean isLeader(LsimulatorPcInstance pc) {
		return pc.getId() == _leader.getId();
	}

	public String getMembersNameList() {
		String _result = new String("");
		for (LsimulatorPcInstance pc : _membersList) {
			_result = _result + pc.getName() + " ";
		}
		return _result;
	}

	private void createMiniHp(LsimulatorPcInstance pc) {
		// パーティー加入時、相互にHPを表示させる
		LsimulatorPcInstance[] members = getMembers();

		for (LsimulatorPcInstance member : members) {
			member.sendPackets(new S_HPMeter(pc.getId(), 100 * pc.getCurrentHp() / pc.getMaxHp()));
			pc.sendPackets(new S_HPMeter(member.getId(), 100 * member.getCurrentHp() / member.getMaxHp()));
		}
	}

	private void deleteMiniHp(LsimulatorPcInstance pc) {
		// 隊員離隊時，頭頂的Hp血條消除。
		LsimulatorPcInstance[] members = getMembers();
		
		for (LsimulatorPcInstance member : members) {
			member.sendPackets(new S_HPMeter(pc.getId(), 0xff));
			pc.sendPackets(new S_HPMeter(member.getId(), 0xff));
		}
		pc.sendPackets(new S_HPMeter(pc.getId(), 0xff));
	}

	public void updateMiniHP(LsimulatorPcInstance pc) {
		LsimulatorPcInstance[] members = getMembers();

		for (LsimulatorPcInstance member : members) { // パーティーメンバー分更新
			member.sendPackets(new S_HPMeter(pc.getId(), 100 * pc.getCurrentHp() / pc.getMaxHp()));
		}
	}

	private void breakup() {
		LsimulatorPcInstance[] members = getMembers();

		for (LsimulatorPcInstance member : members) {
			if (!isLeader(member)) {
				sendLeftMessage(getLeader(), member);
				removeMember(member);
				member.sendPackets(new S_ServerMessage(418)); // 您解散您的隊伍了!!
			} else {
				member.sendPackets(new S_ServerMessage(418)); // 您解散您的隊伍了!!
				removeMember(member);
			}
		}
	}

	public void passLeader(LsimulatorPcInstance pc) {
		pc.getParty().setLeader(pc);
		for (LsimulatorPcInstance member : getMembers()) {
			member.sendPackets(new S_Party(0x6A, pc));
		}
	}

	public void leaveMember(LsimulatorPcInstance pc) {
		if (isLeader(pc) || (getNumOfMembers() == 2)) {
			// パーティーリーダーの場合
			breakup();
		} else {
			removeMember(pc);
			for (LsimulatorPcInstance member : getMembers()) {
				sendLeftMessage(member, pc);
			}
			sendLeftMessage(pc, pc);
			// パーティーリーダーでない場合
			/*
			 * if (getNumOfMembers() == 2) { // パーティーメンバーが自分とリーダーのみ
			 * removeMember(pc); LsimulatorPcInstance leader = getLeader();
			 * removeMember(leader); sendLeftMessage(pc, pc);
			 * sendLeftMessage(leader, pc); } else { // 残りのパーティーメンバーが２人以上いる
			 * removeMember(pc); for (LsimulatorPcInstance member : members) {
			 * sendLeftMessage(member, pc); } sendLeftMessage(pc, pc); }
			 */
		}
	}

	public void kickMember(LsimulatorPcInstance pc) {
		if (getNumOfMembers() == 2) {
			// パーティーメンバーが自分とリーダーのみ
			breakup();
		} else {
			removeMember(pc);
			for (LsimulatorPcInstance member : getMembers()) {
				sendLeftMessage(member, pc);
			}
			sendKickMessage(pc);
		}
	}

	private void showAddPartyInfo(LsimulatorPcInstance pc) {
		for (LsimulatorPcInstance member : getMembers()) {
			if ((pc.getId() == getLeader().getId()) && (getNumOfMembers() == 1)) {
				continue;
			}
			// 發送給隊長的封包
			if (pc.getId() == member.getId()) {
				pc.sendPackets(new S_Party(0x68, pc));
			} else {// 其他成員封包
				member.sendPackets(new S_Party(0x69, pc));
			}
			member.sendPackets(new S_Party(0x6e, member));
			createMiniHp(member);
		}
	}

	public LsimulatorPcInstance[] getMembers() {
		return _membersList.toArray(new LsimulatorPcInstance[_membersList.size()]);
	}

	public int getNumOfMembers() {
		return _membersList.size();
	}

	private void sendKickMessage(LsimulatorPcInstance kickpc) {
		kickpc.sendPackets(new S_ServerMessage(419));
	}

	private void sendLeftMessage(LsimulatorPcInstance sendTo, LsimulatorPcInstance left) {
		// %0がパーティーから去りました。
		sendTo.sendPackets(new S_ServerMessage(420, left.getName()));
	}

}
