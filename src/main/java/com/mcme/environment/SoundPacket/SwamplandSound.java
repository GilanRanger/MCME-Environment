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
package com.mcme.environment.SoundPacket;

import static com.mcme.environment.SoundPacket.SoundUtil.getRandomLocationNW;
import com.mcme.environment.Util.RandomCollection;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 *
 * @author Fraspace5
 */
public class SwamplandSound {

    public static void SwampLandSound(Player pl, Long time) {
        RandomCollection<Boolean> random = new RandomCollection<>();
        random.add(0.4, true);
        random.add(0.6, false);

        RandomCollection<Boolean> random2 = new RandomCollection<>();
        random2.add(0.4, true);
        random2.add(0.6, false);

        RandomCollection<Boolean> random3 = new RandomCollection<>();
        random3.add(0.2, true);
        random3.add(0.8, false);
        Location l = getRandomLocationNW(pl.getLocation().getBlockX() - 20, pl.getLocation().getBlockX() + 20, pl.getLocation().getBlockZ() - 20, pl.getLocation().getBlockZ() + 20, pl.getWorld(), pl.getLocation().getBlockY());

        Boolean result = random.next();
        Boolean result2 = random2.next();
        Boolean result3 = random3.next();
        Float volume = 1F;
        if (SoundUtil.isOutdoor(pl.getLocation())) {
            volume = 0.4F;

        }

        if (SoundUtil.getTimeString(time).equalsIgnoreCase("day") || SoundUtil.getTimeString(time).equalsIgnoreCase("morning")) {

            if (result) {

                pl.playSound(l, SoundsString.SWAMPLAND_BIRDS_DAY.getPath(),SoundCategory.AMBIENT, volume, 1.0F);

            }
            if (result2) {

                pl.playSound(l, SoundsString.SWAMPLAND_CRICKETS_DAY.getPath(),SoundCategory.AMBIENT,volume, 1.0F);

            }

        } else {
            if (result) {

                pl.playSound(l, SoundsString.SWAMPLAND_FROGS_NIGHT.getPath(),SoundCategory.AMBIENT, volume, 1.0F);

            }
            if (result2) {

                pl.playSound(l, SoundsString.SWAMPLAND_CRICKETS_NIGHT.getPath(),SoundCategory.AMBIENT, volume, 1.0F);

            }
            if (result3) {

                pl.playSound(l, SoundsString.SWAMPLAND_BIRDS_NIGHT.getPath(),SoundCategory.AMBIENT, volume, 1.0F);

            }

        }
    }
}
