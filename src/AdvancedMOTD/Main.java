package AdvancedMOTD;

import ProtocolWrapper.WrapperStatusServerServerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements CommandExecutor
{
    
    public static String       motd;
    public static boolean      playerCount;
    public static String       versionMessage;
    public static List<String> hoverMessages;
    
    private ProtocolManager protocolManager;
    private PacketListener  listener;
    
    @Override
    public void onEnable()
    {
        
        saveDefaultConfig();
        
        loadConfig();
        
        getCommand("motdreload").setExecutor(this);
        
        PacketType packetType;
        if(PacketType.Status.Server.SERVER_INFO == null)
        {
            packetType = PacketType.Status.Server.OUT_SERVER_INFO;
        }
        else
        {
            packetType = PacketType.Status.Server.SERVER_INFO;
        }
        
        // Setup packet listener
        this.listener = (PacketListener)new PacketAdapter((Plugin)this, ListenerPriority.NORMAL, new PacketType[]{packetType}){

            @Override
            public void onPacketSending(PacketEvent event)
            {
                WrapperStatusServerServerInfo info = new WrapperStatusServerServerInfo(event.getPacket());
                WrappedServerPing current = info.getJsonResponse();
                WrappedServerPing res = new WrappedServerPing();

                if(Main.playerCount == true)
                {
                    res.setVersionProtocol(current.getVersionProtocol());
                    res.setVersionName(current.getVersionName());
                    res.setPlayersOnline(current.getPlayersOnline());
                    res.setPlayersMaximum(current.getPlayersMaximum());
                }
                else
                {
                    res.setVersionProtocol(-1);
                    res.setVersionName(doSubstitution(Main.versionMessage,current));
                }
                
                res.setMotD(doSubstitution(Main.motd, current));
                res.setFavicon(current.getFavicon());
                
                List<WrappedGameProfile> HoverMessage = new ArrayList();
                for(String message: Main.hoverMessages)
                {
                    HoverMessage.add(new WrappedGameProfile(UUID.randomUUID(), doSubstitution(message,current)));
                }
                
                res.setPlayersVisible(true);
                res.setPlayers(HoverMessage);
                
                info.setJsonResponse(res);
            }
        };
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener(this.listener);
    }
    
    @Override
    public void onDisable()
    {
        // Literally nothing we can do as reloading ProtocolLib breaks
        // But in case this is fixed unregister listeners
        this.protocolManager.removePacketListeners(this);
    }
    
    public void loadConfig()
    {
        FileConfiguration config = getConfig();
        Main.motd           = config.getString    ("motd"          );
        Main.playerCount    = config.getBoolean   ("playerCount"   );
        Main.versionMessage = config.getString    ("versionMessage");
        Main.hoverMessages  = (List)config.getList("hoverMessages" );
    }
    
    public String doSubstitution(String input, WrappedServerPing current)
    {
        return input.replaceAll("@players@", Integer.toString(current.getPlayersOnline()));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("AdvancedMOTD.reload"))
        {
            loadConfig();
            sender.sendMessage("§c[§6§lAdvancedMOTD§c] §bReloaded MOTD!");
        }
        else
        {
            sender.sendMessage("§c§lError! §fyou do not have the correct permissions to do this");
        }
        return true;
    }
}
