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
package Lsimulator.server.server.datatables;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.model.LsimulatorWorld;
import Lsimulator.server.server.model.Instance.FieldObjectInstance;
import Lsimulator.server.server.templates.LsimulatorNpc;
import Lsimulator.server.server.utils.SQLUtil;

public class LightSpawnTable {
	private static Logger _log = Logger.getLogger(LightSpawnTable.class.getName());

	private static LightSpawnTable _instance;

	public static LightSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new LightSpawnTable();
		}
		return _instance;
	}

	private LightSpawnTable() {
		FillLightSpawnTable();
	}

	private void FillLightSpawnTable() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_light");
			rs = pstm.executeQuery();
			do {
				if (!rs.next()) {
					break;
				}

				LsimulatorNpc l1npc = NpcTable.getInstance().getTemplate(rs.getInt(2));
				if (l1npc != null) {
					String s = l1npc.getImpl();
					Constructor<?> constructor = Class.forName("Lsimulator.server.server.model.Instance." + s + "Instance").getConstructors()[0];
					Object parameters[] =
					{ l1npc };
					FieldObjectInstance field = (FieldObjectInstance) constructor.newInstance(parameters);
					field = (FieldObjectInstance) constructor.newInstance(parameters);
					field.setId(IdFactory.getInstance().nextId());
					field.setX(rs.getInt("locx"));
					field.setY(rs.getInt("locy"));
					field.setMap((short) rs.getInt("mapid"));
					field.setHomeX(field.getX());
					field.setHomeY(field.getY());
					field.setHeading(0);
					field.setLightSize(l1npc.getLightSize());

					LsimulatorWorld.getInstance().storeObject(field);
					LsimulatorWorld.getInstance().addVisibleObject(field);
				}
			}
			while (true);
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (SecurityException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (ClassNotFoundException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (IllegalArgumentException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (InstantiationException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (IllegalAccessException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		catch (InvocationTargetException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

}
