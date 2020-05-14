/*
 * Copyright (C) 2020 MCME (Fraspace5)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcme.environment;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcme.environment.commands.EnvironmentCommandExecutor;
import com.mcme.environment.data.PluginData;
import com.mcme.environment.listeners.PlayerListener;
import com.mcme.environment.runnable.RunnablePlayer;
import com.mcme.environment.runnable.SystemRunnable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Fraspace5
 *
 */
public class Environment extends JavaPlugin implements PluginMessageListener {

    static final Logger Logger = Bukkit.getLogger();

    @Getter
    private ConsoleCommandSender clogger = this.getServer().getConsoleSender();

    @Getter
    private static Environment pluginInstance;

    @Getter
    private File envFolder;

    @Getter
    @Setter
    private static String nameserver;

    @Setter
    @Getter
    private ProtocolManager manager;

    @Getter
    private Connection connection;

    String host = this.getConfig().getString("host");
    String port = this.getConfig().getString("port");
    String database = this.getConfig().getString("database");
    String username = this.getConfig().getString("username");
    String password = this.getConfig().getString("password");

    @Getter
    @Setter
    private static boolean engine;

    @Override
    public void onEnable() {
        pluginInstance = this;
        engine = true;
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults();
        try {
            openConnection();
        } catch (SQLException ex) {
            clogger.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "Environment" + ChatColor.DARK_GRAY + "] - " + ChatColor.RED + "Database error!");
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        if (this.isEnabled()) {

            getCommand("environment").setExecutor(new EnvironmentCommandExecutor());
            getCommand("environment").setTabCompleter(new EnvironmentCommandExecutor());
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
            manager = ProtocolLibrary.getProtocolManager();
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
            clogger.sendMessage(ChatColor.GREEN + "---------------------------------------");
            clogger.sendMessage(ChatColor.DARK_GREEN + "Environment Plugin v" + this.getDescription().getVersion() + " enabled!");
            clogger.sendMessage(ChatColor.GREEN + "---------------------------------------");
            SystemRunnable.ConnectionRunnable();
            try {
                onInitiateFile();
            } catch (IOException ex) {
                Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
            }
            Environment.setNameserver("default");

            new BukkitRunnable() {

                @Override
                public void run() {
                    PluginData.loadRegions();
                    SystemRunnable.runnableLocations();
                    RunnablePlayer.runnableLocationsPlayers();
                    RunnablePlayer.runnableRegionsPlayers();
                }

            }.runTaskLater(Environment.getPluginInstance(), 200L);
            new BukkitRunnable() {

                @Override
                public void run() {
                    try {
                        PluginData.onLoad(envFolder);
                    } catch (IOException ex) {
                        Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvalidConfigurationException ex) {
                        Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }.runTaskLater(Environment.getPluginInstance(), 400L);
        }

    }

    @Override
    public void onDisable() {
        clogger.sendMessage(ChatColor.RED + "---------------------------------------");
        clogger.sendMessage(ChatColor.DARK_GREEN + "Environment Plugin v" + this.getDescription().getVersion() + " disabled!");
        clogger.sendMessage(ChatColor.RED + "---------------------------------------");
        for (String str : PluginData.getAllRegions().keySet()) {
            for (List<BukkitTask> s : PluginData.getAllRegions().get(str).getTasks().values()) {
                for (BukkitTask task : s) {
                    task.cancel();
                }
            }

        }

        try {
            PluginData.onSave(envFolder);
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("GetServer")) {
            String servern = in.readUTF();
            Environment.setNameserver(servern);

        }
    }

    /**
     *
     * @throws SQLException
     *
     */
    public void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        if (Environment.getPluginInstance().password.equalsIgnoreCase("default")) {
            clogger.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "Environment" + ChatColor.DARK_GRAY + "] - " + ChatColor.YELLOW + "Plugin INITIALIZED, change database information!");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {

            connection = DriverManager.getConnection("jdbc:mysql://" + Environment.getPluginInstance().host + ":"
                    + Environment.pluginInstance.port + "/"
                    + Environment.getPluginInstance().database + "?useSSL=false&allowPublicKeyRetrieval=true",
                    Environment.getPluginInstance().username,
                    Environment.getPluginInstance().password);
            clogger.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "Environment" + ChatColor.DARK_GRAY + "] - " + ChatColor.GREEN + "Database Found! ");

            new BukkitRunnable() {

                @Override
                public void run() {

                    String stat = "CREATE TABLE IF NOT EXISTS `" + database + "`.`environment_regions_data` (\n"
                            + "  `idregion` VARCHAR(45) NOT NULL,\n"
                            + "  `name` VARCHAR(45) NOT NULL,\n"
                            + "  `type` VARCHAR(45) NOT NULL,\n"
                            + "  `xlist` LONGTEXT NOT NULL,\n"
                            + "  `zlist` LONGTEXT NOT NULL,\n"
                            + "  `ymin` INT NOT NULL,\n"
                            + "  `ymax` INT NOT NULL,\n"
                            + "  `weather` VARCHAR(45),\n"
                            + "  `sound` VARCHAR(45),\n"
                            + "  `info_sound` LONGTEXT,\n"
                            + "  `thunders` BOOLEAN,\n"
                            + "  `location` LONGTEXT NOT NULL,\n"
                            + "  `weight` INT,\n"
                            + "  `time` LONGTEXT,\n"
                            + "  `server` VARCHAR(100) NOT NULL,\n"
                            + "  PRIMARY KEY (`idregion`));";
                    String stat2 = "CREATE TABLE IF NOT EXISTS `" + database + "`.`environment_locations_data` (\n"
                            + "  `name` VARCHAR(45) NOT NULL,\n"
                            + "  `idlocation` VARCHAR(45) NOT NULL,\n"
                            + "  `sound` VARCHAR(45),\n"
                            + "  `location` LONGTEXT,\n"
                            + "  `server` VARCHAR(100) NOT NULL,\n"
                            + "  PRIMARY KEY (`idlocation`));";
                    String stat3 = "CREATE TABLE IF NOT EXISTS `" + database + "`.`environment_players` (\n"
                            + "  `uuid` VARCHAR(45) NOT NULL,\n"
                            + "  `bool` BOOLEAN NOT NULL,\n"
                            + "  PRIMARY KEY (`uuid`));";
                    try {
                        connection.createStatement().execute(stat);

                        connection.createStatement().execute(stat2);

                        connection.createStatement().execute(stat3);
                    } catch (SQLException ex) {
                        Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.runTaskAsynchronously(Environment.getPluginInstance());
        }

    }

    /**
     *
     * @param player The player that sends this message
     */
    public void sendNameServer(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("GetServer");

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    private void onInitiateFile() throws IOException {
        envFolder = new File(Bukkit.getServer().getPluginManager().getPlugin("Environment").getDataFolder(), "locations");

        if (!envFolder.exists()) {

            envFolder.mkdir();

        }

    }

}
