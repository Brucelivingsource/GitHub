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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import Lsimulator.server.LsimulatorDatabaseFactory;
import Lsimulator.server.server.IdFactory;
import Lsimulator.server.server.model.Instance.LsimulatorPcInstance;
import Lsimulator.server.server.templates.LsimulatorMail;
import Lsimulator.server.server.utils.SQLUtil;
import Lsimulator.server.server.utils.collections.Lists;

// Referenced classes of package Lsimulator.server.server:
// IdFactory

public class MailTable {
	private static Logger _log = Logger.getLogger(MailTable.class.getName());

	private static MailTable _instance;

	private static List<LsimulatorMail> _allMail = Lists.newList();

	public static MailTable getInstance() {
		if (_instance == null) {
			_instance = new MailTable();
		}
		return _instance;
	}

	private MailTable() {
		loadMail();
	}

	private void loadMail() {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM mail");
			rs = pstm.executeQuery();
			while (rs.next()) {
				LsimulatorMail mail = new LsimulatorMail();
				mail.setId(rs.getInt("id"));
				mail.setType(rs.getInt("type"));
				mail.setSenderName(rs.getString("sender"));
				mail.setReceiverName(rs.getString("receiver"));
				mail.setDate(rs.getTimestamp("date"));
				mail.setReadStatus(rs.getInt("read_status"));
				mail.setSubject(rs.getBytes("subject"));
				mail.setContent(rs.getBytes("content"));
				mail.setInBoxId(rs.getInt("inbox_id"));

				_allMail.add(mail);
			}
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, "error while creating mail table", e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public void setReadStatus(int mailId) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			rs = con.createStatement().executeQuery("SELECT * FROM mail WHERE id=" + mailId);
			if ((rs != null) && rs.next()) {
				pstm = con.prepareStatement("UPDATE mail SET read_status=? WHERE id=" + mailId);
				pstm.setInt(1, 1);
				pstm.execute();

				changeMailStatus(mailId);
			}
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public void setMailType(int mailId, int type) {
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			rs = con.createStatement().executeQuery("SELECT * FROM mail WHERE id=" + mailId);
			if ((rs != null) && rs.next()) {
				pstm = con.prepareStatement("UPDATE mail SET type=? WHERE id=" + mailId);
				pstm.setInt(1, type);
				pstm.execute();

				changeMailType(mailId, type);
			}
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public void deleteMail(int mailId) {
		Connection con = null;
		PreparedStatement pstm = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("DELETE FROM mail WHERE id=?");
			pstm.setInt(1, mailId);
			pstm.execute();

			delMail(mailId);
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}

	}

	public int writeMail(int type, String receiver, LsimulatorPcInstance writer, byte[] text, int inboxId) {
		Timestamp date = new Timestamp(System.currentTimeMillis());
		int readStatus = 0;
		int id = 0;

		// subjectとcontentの区切り(0x00 0x00)位置を見つける
		int spacePosition1 = 0;
		int spacePosition2 = 0;
		for (int i = 0; i < text.length; i += 2) {
			if ((text[i] == 0) && (text[i + 1] == 0)) {
				if (spacePosition1 == 0) {
					spacePosition1 = i;
				}
				else if ((spacePosition1 != 0) && (spacePosition2 == 0)) {
					spacePosition2 = i;
					break;
				}
			}
		}

		// mailテーブルに書き込む
		int subjectLength = spacePosition1 + 2;
		int contentLength = spacePosition2 - spacePosition1;
		if (contentLength <= 0) {
			contentLength = 1;
		}
		byte[] subject = new byte[subjectLength];
		byte[] content = new byte[contentLength];
		System.arraycopy(text, 0, subject, 0, subjectLength);
		System.arraycopy(text, subjectLength, content, 0, contentLength);

		Connection con = null;
		PreparedStatement pstm2 = null;
		try {
			con = LsimulatorDatabaseFactory.getInstance().getConnection();
			pstm2 = con.prepareStatement("INSERT INTO mail SET " + "id=?, type=?, sender=?, receiver=?,"
					+ " date=?, read_status=?, subject=?, content=?, inbox_id=?");
			id = IdFactory.getInstance().nextId();
			pstm2.setInt(1, id);
			pstm2.setInt(2, type);
			pstm2.setString(3, writer.getName());
			pstm2.setString(4, receiver);
			pstm2.setTimestamp(5, date);
			pstm2.setInt(6, readStatus);
			pstm2.setBytes(7, subject);
			pstm2.setBytes(8, content);
			pstm2.setInt(9, inboxId);
			pstm2.execute();

			LsimulatorMail mail = new LsimulatorMail();
			mail.setId(id);
			mail.setType(type);
			mail.setSenderName(writer.getName());
			mail.setReceiverName(receiver);
			mail.setDate(date);
			mail.setSubject(subject);
			mail.setContent(content);
			mail.setReadStatus(readStatus);
			mail.setInBoxId(inboxId);

			_allMail.add(mail);
		}
		catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		finally {
			SQLUtil.close(pstm2);
			SQLUtil.close(con);
		}
		return id;
	}

	public static List<LsimulatorMail> getAllMail() {
		return _allMail;
	}

	public static LsimulatorMail getMail(int mailId) {
		for (LsimulatorMail mail : _allMail) {
			if (mail.getId() == mailId) {
				return mail;
			}
		}
		return null;
	}

	private void changeMailStatus(int mailId) {
		for (LsimulatorMail mail : _allMail) {
			if (mail.getId() == mailId) {
				LsimulatorMail newMail = mail;
				newMail.setReadStatus(1);

				_allMail.remove(mail);
				_allMail.add(newMail);
				break;
			}
		}
	}

	private void changeMailType(int mailId, int type) {
		for (LsimulatorMail mail : _allMail) {
			if (mail.getId() == mailId) {
				LsimulatorMail newMail = mail;
				newMail.setType(type);

				_allMail.remove(mail);
				_allMail.add(newMail);
				break;
			}
		}
	}

	private void delMail(int mailId) {
		for (LsimulatorMail mail : _allMail) {
			if (mail.getId() == mailId) {
				_allMail.remove(mail);
				break;
			}
		}
	}

}
