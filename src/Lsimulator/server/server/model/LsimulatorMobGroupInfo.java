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

import Lsimulator.server.server.model.Instance.NpcInstance;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server.model:
// LsimulatorMobGroupInfo

public class LsimulatorMobGroupInfo {
	private final List<NpcInstance> _membersList = Lists.newList();

	private NpcInstance _leader;

	public LsimulatorMobGroupInfo() {
	}

	public void setLeader(NpcInstance npc) {
		_leader = npc;
	}

	public NpcInstance getLeader() {
		return _leader;
	}

	public boolean isLeader(NpcInstance npc) {
		return npc.getId() == _leader.getId();
	}

	private LsimulatorSpawn _spawn;

	public void setSpawn(LsimulatorSpawn spawn) {
		_spawn = spawn;
	}

	public LsimulatorSpawn getSpawn() {
		return _spawn;
	}

	public void addMember(NpcInstance npc) {
		if (npc == null) {
			throw new NullPointerException();
		}

		// 最初のメンバーであればリーダーにする
		if (_membersList.isEmpty()) {
			setLeader(npc);
			// リーダーの再ポップ情報を保存する
			if (npc.isReSpawn()) {
				setSpawn(npc.getSpawn());
			}
		}

		if (!_membersList.contains(npc)) {
			_membersList.add(npc);
		}
		npc.setMobGroupInfo(this);
		npc.setMobGroupId(_leader.getId());
	}

	public synchronized int removeMember(NpcInstance npc) {
		if (npc == null) {
			throw new NullPointerException();
		}

		if (_membersList.contains(npc)) {
			_membersList.remove(npc);
		}
		npc.setMobGroupInfo(null);

		// リーダーで他のメンバーがいる場合は、新リーダーにする
		if (isLeader(npc)) {
			if (isRemoveGroup() && (_membersList.size() != 0)) { // リーダーが死亡したらグループ解除する場合
				for (NpcInstance minion : _membersList) {
					minion.setMobGroupInfo(null);
					minion.setSpawn(null);
					minion.setreSpawn(false);
				}
				return 0;
			}
			if (_membersList.size() != 0) {
				setLeader(_membersList.get(0));
			}
		}

		// 残りのメンバー数を返す
		return _membersList.size();
	}

	public int getNumOfMembers() {
		return _membersList.size();
	}

	private boolean _isRemoveGroup;

	public boolean isRemoveGroup() {
		return _isRemoveGroup;
	}

	public void setRemoveGroup(boolean flag) {
		_isRemoveGroup = flag;
	}

}
