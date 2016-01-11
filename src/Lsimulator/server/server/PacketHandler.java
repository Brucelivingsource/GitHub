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
package Lsimulator.server.server;

import static Lsimulator.server.server.Opcodes.C_OPCODE_ADDBUDDY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_AMOUNT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_ARROWATTACK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_ATTACK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_ATTR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BANCLAN;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BANPARTY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOARD;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOARDDELETE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOARDREAD;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOARDWRITE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOOKMARK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BOOKMARKDELETE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BUDDYLIST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_BEANFUNLOGINPACKET;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CAHTPARTY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CALL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHANGECHAR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHANGEHEADING;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHANGEWARTIME;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHARACTERCONFIG;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHARRESET;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHAT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHATGLOBAL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHATWHISPER;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CHECKPK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CLIENTVERSION;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CREATECLAN;
import static Lsimulator.server.server.Opcodes.C_OPCODE_CREATEPARTY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DRAWAL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DELBUDDY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DEPOSIT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DELETECHAR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DELETEINVENTORYITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DOOR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_DROPITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_EMBLEMUPLOAD;
import static Lsimulator.server.server.Opcodes.C_OPCODE_EMBLEMDOWNLOAD;
import static Lsimulator.server.server.Opcodes.C_OPCODE_ENTERPORTAL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_EXCLUDE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_EXIT_GHOST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_EXTCOMMAND;
import static Lsimulator.server.server.Opcodes.C_OPCODE_FIGHT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_FISHCLICK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_FIX_WEAPON_LIST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_GIVEITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_HIRESOLDIER;
import static Lsimulator.server.server.Opcodes.C_OPCODE_JOINCLAN;
import static Lsimulator.server.server.Opcodes.C_OPCODE_KEEPALIVE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_LEAVECLANE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_LEAVEPARTY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_LOGINPACKET;
import static Lsimulator.server.server.Opcodes.C_OPCODE_LOGINTOSERVER;
import static Lsimulator.server.server.Opcodes.C_OPCODE_LOGINTOSERVEROK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_MAIL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_MOVECHAR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_NEWCHAR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_NPCACTION;
import static Lsimulator.server.server.Opcodes.C_OPCODE_NPCTALK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PARTYLIST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PETMENU;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PICKUPITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PLEDGE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PRIVATESHOPLIST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PLEDGECONTENT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PLEDGE_RECOMMENDATION;
import static Lsimulator.server.server.Opcodes.C_OPCODE_PROPOSE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_QUITGAME;
import static Lsimulator.server.server.Opcodes.C_OPCODE_RESTART;
import static Lsimulator.server.server.Opcodes.C_OPCODE_RESULT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SELECTLIST;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SELECTTARGET;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SENDLOCATION;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SHIP;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SHOP;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SKILLBUY;
import static Lsimulator.server.server.Opcodes.C_OPCODE_SKILLBUYOK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TELEPORT;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TITLE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TRADE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TAXRATE;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TRADEADDCANCEL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TRADEADDITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_TRADEADDOK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_USEITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_USEPETITEM;
import static Lsimulator.server.server.Opcodes.C_OPCODE_USESKILL;
import static Lsimulator.server.server.Opcodes.C_OPCODE_WAR;
import static Lsimulator.server.server.Opcodes.C_OPCODE_WAREHOUSELOCK;
import static Lsimulator.server.server.Opcodes.C_OPCODE_WHO;
import static Lsimulator.server.server.Opcodes.C_OPCODE_RESTARTMENU;
import Lsimulator.server.server.clientpackets.C_AddBookmark;
import Lsimulator.server.server.clientpackets.C_AddBuddy;
import Lsimulator.server.server.clientpackets.C_Amount;
import Lsimulator.server.server.clientpackets.C_Attack;
import Lsimulator.server.server.clientpackets.C_Attr;
import Lsimulator.server.server.clientpackets.C_AuthLogin;
import Lsimulator.server.server.clientpackets.C_BanClan;
import Lsimulator.server.server.clientpackets.C_BanParty;
import Lsimulator.server.server.clientpackets.C_Board;
import Lsimulator.server.server.clientpackets.C_BoardDelete;
import Lsimulator.server.server.clientpackets.C_BoardRead;
import Lsimulator.server.server.clientpackets.C_BoardWrite;
import Lsimulator.server.server.clientpackets.C_Buddy;
import Lsimulator.server.server.clientpackets.C_CallPlayer;
import Lsimulator.server.server.clientpackets.C_ChangeHeading;
import Lsimulator.server.server.clientpackets.C_ChangeWarTime;
import Lsimulator.server.server.clientpackets.C_CharReset;
import Lsimulator.server.server.clientpackets.C_CharcterConfig;
import Lsimulator.server.server.clientpackets.C_Chat;
import Lsimulator.server.server.clientpackets.C_ChatParty;
import Lsimulator.server.server.clientpackets.C_ChatWhisper;
import Lsimulator.server.server.clientpackets.C_CheckPK;
import Lsimulator.server.server.clientpackets.C_CreateChar;
import Lsimulator.server.server.clientpackets.C_CreateClan;
import Lsimulator.server.server.clientpackets.C_CreateParty;
import Lsimulator.server.server.clientpackets.C_DelBuddy;
import Lsimulator.server.server.clientpackets.C_DeleteBookmark;
import Lsimulator.server.server.clientpackets.C_DeleteChar;
import Lsimulator.server.server.clientpackets.C_DeleteInventoryItem;
import Lsimulator.server.server.clientpackets.C_Deposit;
import Lsimulator.server.server.clientpackets.C_Door;
import Lsimulator.server.server.clientpackets.C_Drawal;
import Lsimulator.server.server.clientpackets.C_DropItem;
import Lsimulator.server.server.clientpackets.C_EmblemUpload;
import Lsimulator.server.server.clientpackets.C_EmblemDownload;
import Lsimulator.server.server.clientpackets.C_EnterPortal;
import Lsimulator.server.server.clientpackets.C_Exclude;
import Lsimulator.server.server.clientpackets.C_ExitGhost;
import Lsimulator.server.server.clientpackets.C_ExtraCommand;
import Lsimulator.server.server.clientpackets.C_Fight;
import Lsimulator.server.server.clientpackets.C_FishClick;
import Lsimulator.server.server.clientpackets.C_FixWeaponList;
import Lsimulator.server.server.clientpackets.C_GiveItem;
import Lsimulator.server.server.clientpackets.C_HireSoldier;
import Lsimulator.server.server.clientpackets.C_ItemUSe;
import Lsimulator.server.server.clientpackets.C_JoinClan;
import Lsimulator.server.server.clientpackets.C_KeepALIVE;
import Lsimulator.server.server.clientpackets.C_LeaveClan;
import Lsimulator.server.server.clientpackets.C_LeaveParty;
import Lsimulator.server.server.clientpackets.C_LoginToServer;
import Lsimulator.server.server.clientpackets.C_LoginToServerOK;
import Lsimulator.server.server.clientpackets.C_Mail;
import Lsimulator.server.server.clientpackets.C_MoveChar;
import Lsimulator.server.server.clientpackets.C_NPCAction;
import Lsimulator.server.server.clientpackets.C_NPCTalk;
import Lsimulator.server.server.clientpackets.C_NewCharSelect;
import Lsimulator.server.server.clientpackets.C_Party;
import Lsimulator.server.server.clientpackets.C_PetMenu;
import Lsimulator.server.server.clientpackets.C_PickUpItem;
import Lsimulator.server.server.clientpackets.C_Pledge;
import Lsimulator.server.server.clientpackets.C_PledgeContent;
import Lsimulator.server.server.clientpackets.C_PledgeRecommendation;
import Lsimulator.server.server.clientpackets.C_Propose;
import Lsimulator.server.server.clientpackets.C_Restart;
import Lsimulator.server.server.clientpackets.C_RestartMenu;
import Lsimulator.server.server.clientpackets.C_Result;
import Lsimulator.server.server.clientpackets.C_SelectList;
import Lsimulator.server.server.clientpackets.C_SelectTarget;
import Lsimulator.server.server.clientpackets.C_SendLocation;
import Lsimulator.server.server.clientpackets.C_ServerVersion;
import Lsimulator.server.server.clientpackets.C_Ship;
import Lsimulator.server.server.clientpackets.C_Shop;
import Lsimulator.server.server.clientpackets.C_ShopList;
import Lsimulator.server.server.clientpackets.C_SkillBuy;
import Lsimulator.server.server.clientpackets.C_SkillBuyOK;
import Lsimulator.server.server.clientpackets.C_TaxRate;
import Lsimulator.server.server.clientpackets.C_Teleport;
import Lsimulator.server.server.clientpackets.C_Title;
import Lsimulator.server.server.clientpackets.C_Trade;
import Lsimulator.server.server.clientpackets.C_TradeAddItem;
import Lsimulator.server.server.clientpackets.C_TradeCancel;
import Lsimulator.server.server.clientpackets.C_TradeOK;
import Lsimulator.server.server.clientpackets.C_UsePetItem;
import Lsimulator.server.server.clientpackets.C_UseSkill;
import Lsimulator.server.server.clientpackets.C_War;
import Lsimulator.server.server.clientpackets.C_WarePassword;
import Lsimulator.server.server.clientpackets.C_Who;
import Lsimulator.server.server.model.Instance.PcInstance;

// Referenced classes of package Lsimulator.server.server:
// Opcodes, LoginController, ClientThread, Logins

public class PacketHandler {

	public PacketHandler(ClientThread clientthread) {
		_client = clientthread;
	}

	public void handlePacket(byte abyte0[], PcInstance object) throws Exception {
		int i = abyte0[0] & 0xff;

		switch (i) {
			case C_OPCODE_EXCLUDE:
				new C_Exclude(abyte0, _client);
				break;

			case C_OPCODE_CHARACTERCONFIG:
				new C_CharcterConfig(abyte0, _client);
				break;

			case C_OPCODE_DOOR:
				new C_Door(abyte0, _client);
				break;

			case C_OPCODE_TITLE:
				new C_Title(abyte0, _client);
				break;

			case C_OPCODE_BOARDDELETE:
				new C_BoardDelete(abyte0, _client);
				break;

			case C_OPCODE_PLEDGE:
				new C_Pledge(abyte0, _client);
				break;

			case C_OPCODE_CHANGEHEADING:
				new C_ChangeHeading(abyte0, _client);
				break;

			case C_OPCODE_NPCACTION:
				new C_NPCAction(abyte0, _client);
				break;

			case C_OPCODE_USESKILL:
				new C_UseSkill(abyte0, _client);
				break;

			case C_OPCODE_EMBLEMUPLOAD:
				new C_EmblemUpload(abyte0, _client);
				break;
				
			case C_OPCODE_TAXRATE:
				new C_TaxRate(abyte0, _client);
				break;
				
			case C_OPCODE_EMBLEMDOWNLOAD:
				new C_EmblemDownload(abyte0, _client);
				break;

			case C_OPCODE_TRADEADDCANCEL:
				new C_TradeCancel(abyte0, _client);
				break;

			case C_OPCODE_CHANGEWARTIME:
				new C_ChangeWarTime(abyte0, _client);
				break;

			case C_OPCODE_BOOKMARK:
				new C_AddBookmark(abyte0, _client);
				break;

			case C_OPCODE_CREATECLAN:
				new C_CreateClan(abyte0, _client);
				break;

			case C_OPCODE_CLIENTVERSION:
				new C_ServerVersion(abyte0, _client);
				break;
			
			case C_OPCODE_DRAWAL:
				new C_Drawal(abyte0, _client);
				break;
				
			case C_OPCODE_DEPOSIT:
				new C_Deposit(abyte0, _client);
				break;

			case C_OPCODE_PROPOSE:
				new C_Propose(abyte0, _client);
				break;

			case C_OPCODE_SKILLBUY:
				new C_SkillBuy(abyte0, _client);
				break;

			case C_OPCODE_SHOP:
				new C_Shop(abyte0, _client);
				break;

			case C_OPCODE_BOARDREAD:
				new C_BoardRead(abyte0, _client);
				break;

			case C_OPCODE_TRADE:
				new C_Trade(abyte0, _client);
				break;

			case C_OPCODE_DELETECHAR:
				new C_DeleteChar(abyte0, _client);
				break;

			case C_OPCODE_KEEPALIVE:
				new C_KeepALIVE(abyte0, _client);
				break;

			case C_OPCODE_ATTR:
				new C_Attr(abyte0, _client);
				break;

			case C_OPCODE_LOGINPACKET:
				new C_AuthLogin(abyte0, _client);
				break;
				
			case C_OPCODE_BEANFUNLOGINPACKET:
				new C_AuthLogin(abyte0, _client);
				break;

			case C_OPCODE_RESULT:
				new C_Result(abyte0, _client);
				break;

			case C_OPCODE_LOGINTOSERVEROK:
				new C_LoginToServerOK(abyte0, _client);
				break;

		    case C_OPCODE_SKILLBUYOK:
				new C_SkillBuyOK(abyte0, _client);
				break;

			case C_OPCODE_TRADEADDITEM:
				new C_TradeAddItem(abyte0, _client);
				break;
				
			case C_OPCODE_ADDBUDDY:
				new C_AddBuddy(abyte0, _client);
				break;

			case C_OPCODE_CHAT:
				new C_Chat(abyte0, _client);
				break;

			case C_OPCODE_TRADEADDOK:
				new C_TradeOK(abyte0, _client);
				break;

			case C_OPCODE_CHECKPK:
				new C_CheckPK(abyte0, _client);
				break;

			case C_OPCODE_CHANGECHAR:
				new C_NewCharSelect(abyte0, _client);
				break;

			case C_OPCODE_BUDDYLIST:
				new C_Buddy(abyte0, _client);
				break;

			case C_OPCODE_DROPITEM:
				new C_DropItem(abyte0, _client);
				break;

			case C_OPCODE_LEAVEPARTY:
				new C_LeaveParty(abyte0, _client);
				break;
				
			case C_OPCODE_PLEDGECONTENT:
				new C_PledgeContent(abyte0, _client);
				break;
				
			case C_OPCODE_PLEDGE_RECOMMENDATION:
				new C_PledgeRecommendation(abyte0, _client);
				break;
				
			case C_OPCODE_ATTACK:
			case C_OPCODE_ARROWATTACK:
				new C_Attack(abyte0, _client);
				break;

			// TODO 翻譯
			// キャラクターのショートカットやインベントリの状態がプレイ中に変動した場合に
			// ショートカットやインベントリの状態を付加してクライアントから送信されてくる
			// 送られてくるタイミングはクライアント終了時
			case C_OPCODE_QUITGAME:
				break;

			case C_OPCODE_BANCLAN:
				new C_BanClan(abyte0, _client);
				break;

			case C_OPCODE_BOARD:
				new C_Board(abyte0, _client);
				break;

			case C_OPCODE_DELETEINVENTORYITEM:
				new C_DeleteInventoryItem(abyte0, _client);
				break;

			case C_OPCODE_CHATWHISPER:
				new C_ChatWhisper(abyte0, _client);
				break;

			case C_OPCODE_PARTYLIST:
				new C_Party(abyte0, _client);
				break;

			case C_OPCODE_PICKUPITEM:
				new C_PickUpItem(abyte0, _client);
				break;

			case C_OPCODE_WHO:
				new C_Who(abyte0, _client);
				break;

			case C_OPCODE_GIVEITEM:
				new C_GiveItem(abyte0, _client);
				break;

			case C_OPCODE_MOVECHAR:
				new C_MoveChar(abyte0, _client);
				break;

			case C_OPCODE_BOOKMARKDELETE:
				new C_DeleteBookmark(abyte0, _client);
				break;

			case C_OPCODE_RESTART:
				new C_Restart(abyte0, _client);
				break;

			case C_OPCODE_LEAVECLANE:
				new C_LeaveClan(abyte0, _client);
				break;

			case C_OPCODE_NPCTALK:
				new C_NPCTalk(abyte0, _client);
				break;

			case C_OPCODE_BANPARTY:
				new C_BanParty(abyte0, _client);
				break;

			case C_OPCODE_DELBUDDY:
				new C_DelBuddy(abyte0, _client);
				break;

			case C_OPCODE_WAR:
				new C_War(abyte0, _client);
				break;

			case C_OPCODE_LOGINTOSERVER:
				new C_LoginToServer(abyte0, _client);
				break;

			case C_OPCODE_PRIVATESHOPLIST:
				new C_ShopList(abyte0, _client);
				break;

			case C_OPCODE_CHATGLOBAL:
				new C_Chat(abyte0, _client);
				break;

			case C_OPCODE_JOINCLAN:
				new C_JoinClan(abyte0, _client);
				break;

			case C_OPCODE_NEWCHAR:
				new C_CreateChar(abyte0, _client);
				break;

			case C_OPCODE_EXTCOMMAND:
				new C_ExtraCommand(abyte0, _client);
				break;

			case C_OPCODE_BOARDWRITE:
				new C_BoardWrite(abyte0, _client);
				break;

			case C_OPCODE_USEITEM:
				new C_ItemUSe(abyte0, _client);
				break;

			case C_OPCODE_CREATEPARTY:
				new C_CreateParty(abyte0, _client);
				break;

			case C_OPCODE_ENTERPORTAL:
				new C_EnterPortal(abyte0, _client);
				break;

			case C_OPCODE_AMOUNT:
				new C_Amount(abyte0, _client);
				break;

			case C_OPCODE_FIX_WEAPON_LIST:
				new C_FixWeaponList(abyte0, _client);
				break;
		    
			case C_OPCODE_SELECTLIST:
				new C_SelectList(abyte0, _client);
				break;

			case C_OPCODE_EXIT_GHOST:
				new C_ExitGhost(abyte0, _client);
				break;

			case C_OPCODE_CALL:
				new C_CallPlayer(abyte0, _client);
				break;

			case C_OPCODE_HIRESOLDIER:
				new C_HireSoldier(abyte0, _client);
				break;

			case C_OPCODE_FISHCLICK:
				new C_FishClick(abyte0, _client);
				break;

			case C_OPCODE_SELECTTARGET:
				new C_SelectTarget(abyte0, _client);
				break;

			case C_OPCODE_PETMENU:
				new C_PetMenu(abyte0, _client);
				break;

			case C_OPCODE_USEPETITEM:
				new C_UsePetItem(abyte0, _client);
				break;

			case C_OPCODE_TELEPORT:
				new C_Teleport(abyte0, _client);
				break;

			case C_OPCODE_CAHTPARTY:
				new C_ChatParty(abyte0, _client);
				break;

			case C_OPCODE_FIGHT:
				new C_Fight(abyte0, _client);
				break;

			case C_OPCODE_SHIP:
				new C_Ship(abyte0, _client);
				break;

			case C_OPCODE_MAIL:
				new C_Mail(abyte0, _client);
				break;

			case C_OPCODE_CHARRESET:
				new C_CharReset(abyte0, _client);
				break;

			case C_OPCODE_SENDLOCATION:
				new C_SendLocation(abyte0, _client);
				break;

			case C_OPCODE_WAREHOUSELOCK:
				new C_WarePassword(abyte0, _client);
				break;
				
			case C_OPCODE_RESTARTMENU:
				new C_RestartMenu(abyte0, _client);
				break;

			default:
				// String s = Integer.toHexString(abyte0[0] & 0xff);
				// _log.warning("用途不明オペコード:データ内容");
				// _log.warning((new StringBuilder()).append("オペコード ").append(s)
				// .toString());
				// _log.warning(new ByteArrayUtil(abyte0).dumpToString());
				break;
		}
		// _log.warning((new StringBuilder()).append("オペコード
		// ").append(i).toString());
	}

	private final ClientThread _client;
}