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
	/** 玩家列表 */
	public static MyPlayer[][] playerList;
	/** 队长列表 */
	public static String[] headerList;
	/** 队伍别名 */
	public static String[] anotherName;
	/** 队伍个数 */
	public static final int TEAM_NUM = 4;
	/** 文件 */
	private static final File TEAMS = new File("./plugins/talkde/teams.dat");
	
	@Override
	public void onEnable() {
		command("[TalkDeServer] ------------------------------------");
		command("[TalkDeServer]");
		command("[TalkDeServer]  TalkDeServer加载中 版本 1.0 测试版");
		command("[TalkDeServer]");
		command("[TalkDeServer] ------------------------------------");
		Bukkit.getPluginManager().registerEvents(new LoginEvent(), this);
		Bukkit.getPluginManager().registerEvents(new QuitEvent(), this);
		Bukkit.getPluginManager().registerEvents(new ChatEvent(), this);
		Bukkit.getPluginManager().registerEvents(new DeathEvent(), this);
		Bukkit.getPluginManager().registerEvents(new RespawnEvent(), this);
		Bukkit.getPluginManager().registerEvents(new CommandEvent(), this);
		// 四队
		playerList = new MyPlayer[TEAM_NUM][];
		headerList = new String[TEAM_NUM];
		anotherName = new String[TEAM_NUM];
		try {
			// 如果第一次使用，生成文件再读取。
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
			// 读取文件
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
			command("出问题啦！");
		}
		command("[TalkDeServer] 插件加载完毕");
	}
	
	@Override
	public void onDisable() {
		command("[TalkDeServer] 保存信息中");
		try {
			// 保存文件
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
			command("出问题啦！");
		}
		System.gc();
		command("[TalkDeServer] 信息保存完毕，插件正常退出。");
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
	 * 通过玩家名获取队伍
	 * @param player 玩家名
	 * @return 队伍编号（未找到返回-1）
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
	
	/** 命令行处理 */
	private class MyCommand {
		/** 接受消息 */
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
		
		/** 向某队伍发送信息 */
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
						String message = "§3[OP]§6<" + sender.getName() + ">§rto§3[" + anotherName[num] + "] §r" + str.toString();
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
						String message = "§3[" + anotherName[team] + (isheader ? ":队长" : "") + "]§6<" + sender.getName() + ">§rto§3[" + anotherName[num] + "] §r" + str.toString();
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
						String message = "§6<" + sender.getName() + ">§rto§3[" + anotherName[num] + "] §r" + str.toString();
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
					Main.say("§4调用参数错误！", sender);
				}
			} else {
				Main.say("§4调用参数过少！", sender);
			}
			return result;
		}

		private static boolean sendmessage(CommandSender sender, Command cmd, String[] args) {
			boolean result = false;
			if (args.length >= 1) {
				if (sender.isOp()) {
					Main.say("§7你发布了一个公告。", sender);
					StringBuffer str = new StringBuffer();
					for (String s : args) {
						str.append(s);
						str.append(' ');
					}
					Main.say("公告：§3" + str.toString());
				} else {
					Main.say("§7你没有执行此命令的权限。", sender);
				}
				result = true;
			} else {
				Main.say("§4调用参数过少！", sender);
			}
			return result;
		}
		
		private static boolean teams(CommandSender sender, Command cmd, String[] args) {
			boolean result = false;
			if (args.length < 1) {
				say("§4参数过少。", sender);
				return result;
			}
			// 参数分析
			if (args[0].equalsIgnoreCase("help")) { // 帮助
				say("该命令用于操作队伍信息。", sender);
				say("显示列表：", sender);
				say("/teams list §6<队伍名>", sender);
				say("队伍名：代表队伍的名称，包括编号：team1/team2/team3/team4", sender);
				say("增加人数：", sender);
				say("/teams add §6<队伍名> <玩家名>", sender);
				say("设置队长：", sender);
				say("/teams header §6<队伍名> <玩家名>", sender);
				say("注：应将队员设为队长。", sender);
				say("设置别名：", sender);
				say("/teams another §6<队伍名> <新名称>", sender);
				say("队伍列表：", sender);
				say("/teams teamlist", sender);
			} else if (args[0].equalsIgnoreCase("list") && args.length >= 2) { // 显示列表
				int num = getTeamNumber(args[1]);
				if (num == -1) {
					say("§4参数错误！", sender);
				} else {
					say(args[1] + "的成员信息：", sender);
					say(anotherName[num], sender);
					say("队长：§6" + Main.headerList[num], sender);
					say("成员：", sender);
					int count = 0;
					for (MyPlayer player : Main.playerList[num]) {
						if (player.isLive) {
							Main.say(player.name, sender);
							++count;
						}
					}
					say("共" + count + "人。", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("add") && args.length >= 3) { // 新增
				boolean flag = true;
				byte num = getTeamNumber(args[1]);
				Player p = Bukkit.getPlayerExact(args[2]);
				if (p == null) {
					flag = false;
					say("§4此玩家不存在！", sender);
				}
				if (p.isOp()) {
					flag = false;
					say("§4你不能将管理员设为队成员！", sender);
				}
				if (num == -1) {
					flag = false;
					say("§4参数错误！", sender);
				}
				byte t = getTeam(p.getName());
				if (t != -1) {
					flag = false;
					say("§4你不能将已有队伍的成员设为队成员，此玩家已在" + anotherName[t] + "中存在！", sender);
				}
				if (!sender.isOp()) {
					flag = false;
					say("§4你没有权限修改。", sender);
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
					say("添加成功。", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("header") && args.length >= 3) { // 设置队长
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				int t = getTeam(args[2]);
				if (num == -1) {
					say("§4参数错误！", sender);
					flag = false;
				}
				if (!sender.isOp()) {
					flag = false;
					say("§4你没有权限修改。", sender);
				}
				if (t == num) {
					flag = false;
					say("§4只能将队内成员设置为队长。", sender);
				}
				if (flag) {
					headerList[num] = args[2];
					say("队长设置成功。", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("clear") && args.length >= 2) {
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				if (num == -1) {
					say("§4参数错误！", sender);
					flag = false;
				}
				if (!sender.isOp()) {
					flag = false;
					say("§4你没有权限修改。", sender);
				}
				if (flag) {
					playerList[num] = new MyPlayer[0];
					headerList[num] = "None";
					say("已经清空" + args[1] + "的成员。", sender);
					result = true;
				}
			} else if (args[0].equalsIgnoreCase("another") && args.length >= 3) { // 修改别名
				boolean flag = true;
				int num = getTeamNumber(args[1]);
				int an = getTeamNumber(args[2]);
				if (num != -1 && num == an && sender.isOp()) {
					anotherName[num] = args[2];
					say("修改别名成功。", sender);
					result = true;
				} else if (num == -1) {
					say("§4找不到相应的队伍！", sender);
				} else if (!sender.isOp()) {
					say("§4你没有权限修改。", sender);
				} else {
					say("§4无效的队伍名。", sender);
				}
				
				if (!sender.isOp()) {
					flag = false;
				}
				if (flag) {
					
				}
			} else if (args[0].equalsIgnoreCase("teamlist")) {
				say("队伍列表：", sender);
				for (int i = 0; i < TEAM_NUM; ++i) {
					say("team" + (i + 1) + ' ' + anotherName[i], sender);
				}
				result = true;
			} else {
				say("§4参数错误！", sender);
			}
			return result;
		}
		
		/** 获取队伍编号 */
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
	
	/** 玩家进入事件 */
	private class LoginEvent implements Listener {
		@EventHandler
		public void login(PlayerJoinEvent event) {
			String name = event.getPlayer().getName();
			event.setJoinMessage("§6" + name + " §r进入了服务器。");
		}
	}
	
	/** 玩家退出事件 */
	private class QuitEvent implements Listener {
		@EventHandler
		public void quit(PlayerQuitEvent event) {
			String name = event.getPlayer().getName();
			event.setQuitMessage("§6" + name + " §r退出了服务器。");
		}
	}
	
	/** 玩家聊天事件 */
	private class ChatEvent implements Listener {
		@EventHandler
		public void chat(AsyncPlayerChatEvent event) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			if (player.isOp()) {
				say("§3[OP]§6<" + player.getName() + "> §r" + event.getMessage());
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
						say("§3[" + anotherName[team] + (isheader ? ":队长" : "") + "]§6<" + player.getName() + "> §r" + event.getMessage(), p);
					}
				}
				command("§3[" + anotherName[team] + (isheader ? ":队长" : "") + "]§6<" + player.getName() + "> §r" + event.getMessage());
			} else {
				say("§6<" + player.getName() + "> §r" + event.getMessage());
			}
		}
	}
	
	/** 玩家死亡事件 */
	private class DeathEvent implements Listener {
		@EventHandler
		public void die(PlayerDeathEvent event) {
			Player player = event.getEntity();
			if (player.isOp()) {
				event.setDeathMessage("§3[OP]§6" + player.getName() + " §r跳进了虚空？");
			} else if (getTeam(player.getName()) >= 0) {
				int team = getTeam(player.getName());
				boolean isheader = player.getName().equals(headerList[team]);
				event.setDeathMessage("§3[" + anotherName[team] + (isheader ? ":队长" : "") + "]§6" + player.getName() + " §r阵亡了");
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
					say(anotherName[team] + "已全体阵亡");
				}
			} else {
				event.setDeathMessage("§6" + player.getName() + " §r阵亡了");
			}
		}
	}
	
	/** 玩家重生事件 */
	private class RespawnEvent implements Listener {
		@EventHandler
		public void die(PlayerRespawnEvent event) {
			if (!event.getPlayer().isOp() && getTeam(event.getPlayer().getName()) != -1) {
				event.getPlayer().setGameMode(GameMode.SPECTATOR);
			}
		}
	}
	
	/** 拦截玩家命令 */
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
					say("§3[OP]§6<" + player.getName() + "> §r" + m.group(1));
				} else if (getTeam(player.getName()) == -1) {
					say("§6<" + player.getName() + "> §r" + m.group(1));
				} else {
					int team = getTeam(player.getName());
					boolean isheader = player.getName().equals(headerList[team]);
					say("§3[" + anotherName[team] + (isheader ? ":队长" : "") + "]§6<" + player.getName() + "> §r" + m.group(1));
				}
				event.setCancelled(true);
			}
		}
	}
}
