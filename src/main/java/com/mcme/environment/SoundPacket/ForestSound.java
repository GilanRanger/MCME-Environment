/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcme.environment.SoundPacket;

import static com.mcme.environment.SoundPacket.SoundUtil.getRandomLocationNW;
import com.mcme.environment.Util.RandomCollection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Fraspace5
 */
public class ForestSound {

    public static void ForestSound(Player pl, Long time) {
        RandomCollection<Boolean> random = new RandomCollection<>();
        random.add(0.4, true);
        random.add(0.6, false);

        RandomCollection<Boolean> random2 = new RandomCollection<>();
        random2.add(0.4, true);
        random2.add(0.6, false);
        Location l = getRandomLocationNW(pl.getLocation().getBlockX() - 20, pl.getLocation().getBlockX() + 20, pl.getLocation().getBlockZ() - 20, pl.getLocation().getBlockZ() + 20, pl.getWorld(), pl.getLocation().getBlockY());

        Boolean result = random.next();
        Boolean result2 = random2.next();

        Float volume = 1F;

        if (SoundUtil.isOutdoor(pl.getLocation())) {
            volume = 0.4F;

        }

        if (SoundUtil.getTimeString(time).equalsIgnoreCase("day")) {

            if (result) {

                pl.playSound(l, SoundsString.FOREST_BIRD_DAY.getPath(),volume, 1.0F);

            }
            if (result2) {

                pl.playSound(l, SoundsString.FOREST_INSECT_DAY.getPath(), volume, 1.0F);

            }

        } else if (SoundUtil.getTimeString(time).equalsIgnoreCase("morning")) {
            if (result) {

                pl.playSound(l, SoundsString.FOREST_BIRD_MORNING.getPath(), volume, 1.0F);

            }
            if (result2) {

                pl.playSound(l, SoundsString.FOREST_INSECT_MORNING.getPath(), volume, 1.0F);

            }

        } else {
            if (result) {

                pl.playSound(l, SoundsString.FOREST_BIRD_NIGHT.getPath(), volume, 1.0F);

            }
            if (result2) {

                pl.playSound(l, SoundsString.FOREST_INSECT_NIGHT.getPath(), volume, 1.0F);

            }

        }

    }
}
