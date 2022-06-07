package top.bpd;

import java.util.Collection;
import java.io.*;
import java.util.regex.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import top.bpd.player.MyPlayer;

public class Main extends JavaPlugin {
	/** ����б� */
	public static MyPlayer[][] playerList;
	/** �ӳ��б� */
	public static String[] headerList;
	/** ������� */
	public static String[] anotherName;
	/** ������� */
	public static final int TEAM_NUM = 4;
	/** �ļ� */
	private static final File TEAMS = new File("./plugins/talkde/teams.dat");
	
	@Override
	public void onEnable() {
		command("[TalkDeServer] ------------------------------------");
		command("[TalkDeServer]");
		command("[TalkDeServer]  TalkDeServer������ �汾 1.0 ���԰�");
		command("[TalkDeServer]");
		command("[TalkDeServer] ------------------------------------");
		Bukkit.getPluginManager().registerEvents(new LoginEvent(), this);
		Bukkit.getPluginManager().registerEvents(new QuitEvent(), this);
		Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
		Bukkit.getPluginManager().registerEvents(new DeathEvent(), this);
		Bukkit.getPluginManager().registerEvents(new RespawnEvent(), this);
		Bukkit.getPluginManager().registerEvents(new CommandEvent(), this);
		// �Ķ�
		playerList = new MyPlayer[TEAM_NUM][];
		headerList = new String[TEAM_NUM];
		anotherName = new String[TEAM_NUM];
		try {
			// �����һ��ʹ�ã������ļ��ٶ�ȡ��
			if (!TEAMS.exists()) {
				new File("./plugins/talkde").mkdir();
				TEAMS.createNewFile();
				DataOutputStream config = new DataOutputStream(new FileOutputStream(TEAMS));
				config.writeInt(0);
				config.writeUTF("team1");
				config.writeUTF("None");
				config.writeInt(0);
				config.writeUTF("team2");
				config.writeUTF("None");
				config.writeInt(0);
				config.writeUTF("team3");
				config.writeUTF("None");
				config.writeInt(0);
				config.writeUTF("team4");
				config.writeUTF("None");
				config.close();
			}
			// ��ȡ�ļ�
			DataInputStream config = new DataInputStream(new FileInputStream(TEAMS));
			for (int i = 0; i < TEAM_NUM; ++i) {
				int num = config.readInt();
				playerList[i] = new MyPlayer[num];
				anotherName[i] = config.readUTF();
				headerList[i] = config.readUTF();
				for (int j = 0; j < num; ++j) {
					playerList[i][j] = new MyPlayer();
					playerList[i][j].name = config.readUTF();
					playerList[i][j].isLive = config.readBoolean();
				}
			}
			config.close();
		} catch (IOException e) {
			command("����������");
		}
		command("[TalkDeServer] ����������");
	}
	
	@Override
	public void onDisable() {
		command("[TalkDeServer] ������Ϣ��");
		try {
			// �����ļ�
			DataOutputStream config = new DataOutputStream(new FileOutputStream(TEAMS));
			for (int i = 0; i < TEAM_NUM; ++i) {
				config.writeInt(playerList[i].length);
				config.writeUTF(anotherName[i]);
				config.writeUTF(headerList[i]);
				for (int j = 0; j < playerList[i].length; ++j) {
					config.writeUTF(playerList[i][j].name);
					config.writeBoolean(playerList[i][j].isLive);
				}
			}
			config.close();
		} catch (IOException e) {
			command("����������");
		}
		System.gc();
		command("[TalkDeServer] ��Ϣ������ϣ���������˳���");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return MyCommand.toCommand(sender, cmd, label, args);
	}
	
	public static void command(String str) {
		CommandSender sender = Bukkit.getConsoleSender();
		sender.sendMessage(str);
	}
	
	public static void say(String str) {
		Bukkit.broadcastMessage(str);
	}
	
	public static void say(String str, CommandSender sender) {
		sender.sendMessage(str);
	}
	
	public static void say(String str, Player player) {
		player.sendMessage(str);
	}
	
	/**
	 * ͨ���������ȡ����
	 * @param player �����
	 * @return �����ţ�δ�ҵ�����-1��
	 */
	public static byte getTeam(String player) {
		byte i = 0;
		byte result = -1;
		loop: for (MyPlayer[] team : playerList) {
			for (MyPlayer obj : team) {
				if (obj.name.equalsIgnoreCase(player)) {
					result = i;
					break loop;
				}
			}
			++i;
		}
		return result;
	}
	
	/** �����д��� */
	private class MyCommand {
		/** ������Ϣ */
		public static boolean toCommand(CommandSender sender, Command cmd, String label, String[] args) {
			boolean result = false;
			if (label.equalsIgnoreCase("sendmessage")) {
				result = sendmessage(sender, cmd, args);
			} else if (label.equalsIgnoreCase("teams")) {
				result = teams(sender, cmd, args);
			} else if (label.equalsIgnoreCase("ttt")) {
				result = ttt(sender, cmd, args);
			}
			return result;
		}
		
		/** ��ĳ���鷢����Ϣ */
		private static boolean ttt(CommandSender sender, Command cmd, String[] args) {
			boolean result = false;
			if (args.length >= 2) {
				int num = getTeamNumber(args[0]);
				if (num >= 0) {
					StringBuffer str = new StringBuffer();
					for (int i = 1; i < args.length; ++i) {
						str.append(args[i]);
						str.append(' ');
					}
					if (sender.isOp()) {
						String message = "��3[OP]��6<" + sender.getName() + ">��rto��3[" + anotherName[num] + "] ��r" + str.toString();
						Collection<? extends Player> cl = Bukkit.getOnlinePlayers();
						Object[] objs = cl.toArray();
						Player[] players = new Player[objs.length];
						for (int i = 0; i < objs.length; ++i) {
							players[i] = (Player)objs[i];
						}
						for (Player p : players) {
							if (getTeam(p.getName()) == num || getTeam(p.getName()) < 0 || p.isOp()) {
								say(message, p);
							}
						}
						command(message);
					} else if (getTeam(sender.getName()) >= 0) {
						int team = getTeam(sender.getName());
						boolean isheader = sender.getName().equals(headerList[team]);
						String message = "��3[" + anotherName[team] + (isheader ? ":�ӳ�" : "") + "]��6<" + sender.getName() + ">��rto��3[" + anotherName[num] + "] ��r" + str.toString();
						Collection<? extends Player> cl = Bukkit.getOnlinePlayers();
						Object[] objs = cl.toArray();
						Player[] players = new Player[objs.length];
						for (int i = 0; i < objs.length; ++i) {
							players[i] = (Player)objs[i];
						}
						for (Player p : players) {
							if (getTeam(p.getName()) == num || getTeam(p.getName()) == team || getTeam(p.getName()) < 0 || p.isOp()) {
								say(message, p);
							}
						}
						command(message);
					} else {
						String message = "��6<" + sender.getName() + ">��rto��3[" + anotherName[num] + "] ��r" + str.toString();
						Collection<? extends Player> cl = Bukkit.getOnlinePlayers();
						Object[] objs = cl.toArray();
						Player[] players = new Player[objs.length];
						for (int i = 0; i < objs.length; ++i) {
							players[i] = (Player)objs[i];
						}
						for (Player p : players) {
							if (getTeam(p.getName()) == num || getTeam(p.getName()) < 0 || p.isOp()) {
								say(message, p);
							}
						}
						command(message);
					}
					result = true;
				} else {
					Main.say("��4���ò�������", sender);
				}
			} else {
				Main.say("��4���ò������٣�", sender);
			}
			return result;
		}

		private static boolean sendmessage(CommandSender sender, Command cmd, String[] args) {
			boolean result = false;
			if (args.length >= 1) {
				if (sender.isOp()) {
					Main.say("��7�㷢����һ�����档", sender);
					StringBuffer str = new StringBuffer();
					for (String s : args) {
						str.append(s);
						str.append(' ');
					}
					Main.say("���棺��3" + str.toString());
				} else {
					Main.say("��7��û��ִ�д������Ȩ�ޡ�", sender);
				}
				result = true;
			} else {
				Main.say("��4���ò������٣�", sender);
			}
			return result;
		}
		
		private static boolean teams(CommandSender sender, Command cmd, String[] args) {
			boolean result = false;
			if (args.length < 1) {
				say("��4�������١�", sender);
				return result;
			}
			// ��������
			if (args[0].equalsIgnoreCase("help")) { // ����
				say("���������ڲ���������Ϣ��", sender);
				say("��ʾ�б�", sender);
				say("/teams list ��6<������>", sender);
				say("�������������������ƣ�������ţ�team1/team2/team3/team4", sender);
				say("����������", sender);
				say("/teams add ��6<������> <�����>", sender);
				say("���öӳ���", sender);
				say("/teams header ��6<������> <�����>", sender);
				say("ע��Ӧ����Ա��Ϊ�ӳ���", sender);
				say("���ñ�����", sender);
				say("/teams another ��6<������> <������>", sender);
				say("�����б�", sender);
				say("/teams teamlist", sender);
			} else if (args[0].equalsIgnoreCase("list") && args.length >= 2) { // ��ʾ�б�
				int num = getTeamNumber(args[1]);
				if (num == -1) {
					say("��4��������", sender);
				} else {
					say(args[1] + "�ĳ�Ա��Ϣ��", sender);
					say(anotherName[num], sender);
					say("�ӳ�����6" + Main.headerList[num], sender);
					say("��Ա��", sender);
					int count = 0;
					for (MyPlayer player : Main.playerList[num]) {
						if (player.isLive) {
							Main.say(player.name, sender);
							++count;
						}
					}
					say("��" + count + "�ˡ�", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("add") && args.length >= 3) { // ����
				boolean flag = true;
				byte num = getTeamNumber(args[1]);
				Player p = Bukkit.getPlayerExact(args[2]);
				if (p == null) {
					flag = false;
					say("��4����Ҳ����ڣ�", sender);
				}
				if (p.isOp()) {
					flag = false;
					say("��4�㲻�ܽ�����Ա��Ϊ�ӳ�Ա��", sender);
				}
				if (num == -1) {
					flag = false;
					say("��4��������", sender);
				}
				byte t = getTeam(p.getName());
				if (t != -1) {
					flag = false;
					say("��4�㲻�ܽ����ж���ĳ�Ա��Ϊ�ӳ�Ա�����������" + anotherName[t] + "�д��ڣ�", sender);
				}
				if (!sender.isOp()) {
					flag = false;
					say("��4��û��Ȩ���޸ġ�", sender);
				}
				if (flag) {
					MyPlayer[] newArray = new MyPlayer[playerList[num].length+1];
					for (int i = 0; i < playerList[num].length; ++i) {
						newArray[i] = playerList[num][i];
					}
					newArray[newArray.length-1] = new MyPlayer();
					newArray[newArray.length-1].name = args[2];
					newArray[newArray.length-1].isLive = true;
					playerList[num] = newArray;
					say("��ӳɹ���", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("header") && args.length >= 3) { // ���öӳ�
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				int t = getTeam(args[2]);
				if (num == -1) {
					say("��4��������", sender);
					flag = false;
				}
				if (!sender.isOp()) {
					flag = false;
					say("��4��û��Ȩ���޸ġ�", sender);
				}
				if (t == num) {
					flag = false;
					say("��4ֻ�ܽ����ڳ�Ա����Ϊ�ӳ���", sender);
				}
				if (flag) {
					headerList[num] = args[2];
					say("�ӳ����óɹ���", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("clear") && args.length >= 2) {
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				if (num == -1) {
					say("��4��������", sender);
					flag = false;
				}
				if (!sender.isOp()) {
					flag = false;
					say("��4��û��Ȩ���޸ġ�", sender);
				}
				if (flag) {
					playerList[num] = new MyPlayer[0];
					headerList[num] = "None";
					say("�Ѿ����" + args[1] + "�ĳ�Ա��", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("another") && args.length >= 3) { // �޸ı���
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				int an = getTeamNumber(args[2]);
				if (num != -1 && num == an && sender.isOp()) {
					anotherName[num] = args[2];
					say("�޸ı����ɹ���", sender);
					result = true;
				} else if (num == -1) {
					say("��4�Ҳ�����Ӧ�Ķ��飡", sender);
				} else if (!sender.isOp()) {
					say("��4��û��Ȩ���޸ġ�", sender);
				} else {
					say("��4��Ч�Ķ�������", sender);
				}
				
				if (!sender.isOp()) {
					flag = false;
				}
				if (flag) {
					
				}
			} else if (args[0].equalsIgnoreCase("teamlist")) {
				say("�����б�", sender);
				for (int i = 0; i < TEAM_NUM; ++i) {
					say("team" + (i + 1) + ' ' + anotherName[i], sender);
				}
				result = true;
			} else {
				say("��4��������", sender);
			}
			return result;
		}
		
		/** ��ȡ������ */
		private static byte getTeamNumber(String teamname) {
			byte num;
			if (teamname.equalsIgnoreCase("team1") || teamname.equalsIgnoreCase(anotherName[0])) {
				num = 0;
			} else if (teamname.equalsIgnoreCase("team2") || teamname.equalsIgnoreCase(anotherName[1])) {
				num = 1;
			} else if (teamname.equalsIgnoreCase("team3") || teamname.equalsIgnoreCase(anotherName[2])) {
				num = 2;
			} else if (teamname.equalsIgnoreCase("team4") || teamname.equalsIgnoreCase(anotherName[3])) {
				num = 3;
			} else {
				num = -1;
			}
			return num;
		}
	}
	
	/** ��ҽ����¼� */
	private class LoginEvent implements Listener {
		@EventHandler
		public void login(PlayerJoinEvent event) {
			String name = event.getPlayer().getName();
			event.setJoinMessage("��6" + name + " ��r�����˷�������");
		}
	}
	
	/** ����˳��¼� */
	private class QuitEvent implements Listener {
		@EventHandler
		public void quit(PlayerQuitEvent event) {
			String name = event.getPlayer().getName();
			event.setQuitMessage("��6" + name + " ��r�˳��˷�������");
		}
	}
	
	/** ��������¼� */
	private class ChatEvent implements Listener {
		@EventHandler
		public void chat(AsyncPlayerChatEvent event) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			if (player.isOp()) {
				say("��3[OP]��6<" + player.getName() + "> ��r" + event.getMessage());
			} else if (getTeam(player.getName()) >= 0) {
				Collection<? extends Player> cl = Bukkit.getOnlinePlayers();
				Object[] objs = cl.toArray();
				Player[] players = new Player[objs.length];
				for (int i = 0; i < objs.length; ++i) {
					players[i] = (Player)objs[i];
				}
				int team = getTeam(player.getName());
				boolean isheader = player.getName().equals(headerList[team]);
				for (Player p : players) {
					if (getTeam(p.getName()) == team || getTeam(p.getName()) < 0 || p.isOp()) {
						say("��3[" + anotherName[team] + (isheader ? ":�ӳ�" : "") + "]��6<" + player.getName() + "> ��r" + event.getMessage(), p);
					}
				}
				command("��3[" + anotherName[team] + (isheader ? ":�ӳ�" : "") + "]��6<" + player.getName() + "> ��r" + event.getMessage());
			} else {
				say("��6<" + player.getName() + "> ��r" + event.getMessage());
			}
		}
	}
	
	/** ��������¼� */
	private class DeathEvent implements Listener {
		@EventHandler
		public void die(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (player.isOp()) {
				event.setDeathMessage("��3[OP]��6" + player.getName() + " ��r��������գ�");
			} else if (getTeam(player.getName()) >= 0) {
				int team = getTeam(player.getName());
				boolean isheader = player.getName().equals(headerList[team]);
				event.setDeathMessage("��3[" + anotherName[team] + (isheader ? ":�ӳ�" : "") + "]��6" + player.getName() + " ��r������");
				for (MyPlayer obj : playerList[team]) {
					if (obj.name.equalsIgnoreCase(player.getName())) {
						obj.isLive = false;
						break;
					}
				}
				boolean flag = false;
				for (MyPlayer obj : playerList[team]) {
					if (obj.isLive == true) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					say(anotherName[team] + "��ȫ������");
				}
			} else {
				event.setDeathMessage("��6" + player.getName() + " ��r������");
			}
		}
	}
	
	/** ��������¼� */
	private class RespawnEvent implements Listener {
		@EventHandler
		public void die(PlayerRespawnEvent event) {
			if (!event.getPlayer().isOp() && getTeam(event.getPlayer().getName()) != -1) {
				event.getPlayer().setGameMode(GameMode.SPECTATOR);
			}
		}
	}
	
	/** ����������� */
	private class CommandEvent implements Listener {
		@EventHandler
		public void com(PlayerCommandPreprocessEvent event) {
			String comm = event.getMessage();
			Player player = event.getPlayer();
			String pattern;
			Pattern p;
			Matcher m;
			
			pattern = "^/say (.+)$";
			p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			m = p.matcher(comm);
			
			if (m.find()) {
				if (player.isOp()) {
					say("��3[OP]��6<" + player.getName() + "> ��r" + m.group(1));
				} else if (getTeam(player.getName()) == -1) {
					say("��6<" + player.getName() + "> ��r" + m.group(1));
				} else {
					int team = getTeam(player.getName());
					boolean isheader = player.getName().equals(headerList[team]);
					say("��3[" + anotherName[team] + (isheader ? ":�ӳ�" : "") + "]��6<" + player.getName() + "> ��r" + m.group(1));
				}
				event.setCancelled(true);
			}
		}
	}
}
