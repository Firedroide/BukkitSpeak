package net.but2002.minecraft.BukkitSpeak.teamspeakEvent;

import com.dthielke.Herochat;
import java.util.HashMap;

import org.bukkit.entity.Player;

//import com.dthielke.herochat.Herochat;
//import com.dthielke.channel.ChannelManager;

import net.but2002.minecraft.BukkitSpeak.BukkitSpeak;
import net.but2002.minecraft.BukkitSpeak.Configuration.Configuration;
import net.but2002.minecraft.BukkitSpeak.Configuration.Messages;
import net.but2002.minecraft.BukkitSpeak.util.MessageUtil;
import net.but2002.minecraft.BukkitSpeak.util.Replacer;

public abstract class TeamspeakEvent {

	private HashMap<String, String> user;

	public HashMap<String, String> getUser() {
		return user;
	}

	public String getClientName() {
		return user.get("client_nickname");
	}

	public Integer getClientId() {
		return Integer.valueOf(user.get("clid"));
	}

	public Integer getClientType() {
		return Integer.valueOf(user.get("client_type"));
	}

	protected void setUser(Integer clid) {
		user = BukkitSpeak.getClientList().get(clid);
	}

	protected void sendMessage(Messages message, String permission) {
		sendMessage(message, permission, null);
	}

	protected void sendMessage(Messages message, String permission, String msg) {
		String m = message.get();
		if (m.isEmpty()) return;
		Replacer r = new Replacer().addClient(getUser());
		if (msg != null && !msg.isEmpty()) {
			r.addMessage(msg);
		}
		m = MessageUtil.toMinecraft(r.replace(m), true, true);

		if (BukkitSpeak.useHerochat() && Configuration.PLUGINS_HEROCHAT_RELAY_EVENTS.getBoolean()) {
			// Send to Herochat channel
			String c = Configuration.PLUGINS_HEROCHAT_CHANNEL.getString();
			Herochat.getChannelManager().getChannel(c).announce(m);
		} else {
			// Directly send to players with permissions
			for (Player pl : BukkitSpeak.getInstance().getServer().getOnlinePlayers()) {
				if (!BukkitSpeak.getMuted(pl) && checkPermissions(pl, permission)) {
					pl.sendMessage(m);
				}
			}
		}

		// Finally log in console if enabled
		if (Configuration.TS_LOGGING.getBoolean()) {
			m = MessageUtil.toMinecraft(m, false, true);
			BukkitSpeak.log().info(m);
		}
	}

	protected boolean checkPermissions(Player player, String perm) {
		return player.hasPermission("bukkitspeak.messages." + perm);
	}

	protected abstract void performAction();
}
