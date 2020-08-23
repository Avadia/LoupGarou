package fr.leomelki.loupgarou.arena;

import com.google.gson.JsonObject;
import fr.leomelki.loupgarou.MainLg;
import net.samagames.api.SamaGamesAPI;
import net.samagames.tools.LocationUtils;

/*
 * This file is part of HangoverGames.
 *
 * HangoverGames is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HangoverGames is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HangoverGames.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ArenaManager {
    private final MainLg plugin;

    public ArenaManager(MainLg plugin) {
        this.plugin = plugin;
    }

    public Arena loadArena() {
        JsonObject jsonArena = SamaGamesAPI.get().getGameManager().getGameProperties().getMapProperties();

//        JsonArray jsonCauldrons = jsonArena.get("cauldrons").getAsJsonArray();
//        ArrayList<Location> cauldrons = new ArrayList<>();
//
//        for (int i = 0; i < jsonCauldrons.size(); i++)
//            cauldrons.add(LocationUtils.str2loc(jsonCauldrons.get(i).getAsString()));

        return new Arena(this.plugin, LocationUtils.str2loc(jsonArena.get("spawn").getAsString()));
    }
}
