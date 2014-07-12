package com.tenjava.slipcor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Redstone;
import org.bukkit.material.RedstoneWire;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TenJava extends JavaPlugin {
    List<Chargeable> chargeableList = new ArrayList<Chargeable>();
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new ChargeListener(this), this);
        getCommand("battery").setExecutor(new CmdBattery(this));
    }

    public void addChargeable(Chargeable chargeable) {
        if (chargeableList.contains(chargeable)) {
            return;
        }
        chargeableList.add(chargeable);
    }

    public List<Chargeable> getChargeables () {
        return chargeableList;
    }

    public void removeChargeable(Chargeable chargeable) {
        chargeableList.remove(chargeable);
    }
}
