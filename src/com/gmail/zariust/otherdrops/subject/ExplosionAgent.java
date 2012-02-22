// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.subject;

import static com.gmail.zariust.common.Verbosity.EXTREME;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.CreatureData;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.ToolDamage;

public class ExplosionAgent implements Agent {
	private Object explosive;
	//private Explosive bomb; // Creeper doesn't implement Explosive yet...
	private Entity bomb;
	
	public ExplosionAgent() { // Wildcard
		this(null, (Material)null);
	}
	
	public ExplosionAgent(CreatureType boom) { // Creature explosion
		this(new CreatureSubject(boom), null);
	}
	
	public ExplosionAgent(CreatureType boom, int data) {
		this(new CreatureSubject(boom, data), null);
	}
	
	public ExplosionAgent(CreatureType boom, Data data) {
		this(new CreatureSubject(boom, data), null);
	}
	
	public ExplosionAgent(Material boom) { // Non-creature explosion
		this(null, boom);
	}
	
	// TODO: Entity -> Explosive (if the API changes so Creeper implements Explosive)
	public ExplosionAgent(Entity boom) { // Actual explosion
		this(new CreatureSubject(CommonEntity.getCreatureType(boom)), CommonEntity.getExplosiveType(boom));
		bomb = boom;
	}
	
	private ExplosionAgent(CreatureSubject agent, Material mat) { // Rome
		explosive = mat;
	}

	public boolean isCreature() {
		return bomb instanceof LivingEntity;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof ExplosionAgent)) return false;
		if(explosive == null) return true;
		ExplosionAgent tool = (ExplosionAgent) other;
		return explosive == tool.explosive;
	}
	
	@Override
	public int hashCode() {
		return new HashCode(this).get(explosive);
	}
	
	@Override
	public boolean matches(Subject other) {
		if(!(other instanceof ExplosionAgent)) return false;
		ExplosionAgent tool = (ExplosionAgent)other;
		
		if(explosive == null) return true; // wildcard
		
		return explosive.equals(tool.explosive);
	}
	
	@Override
	public ItemCategory getType() {
		return ItemCategory.EXPLOSION;
	}

	public static Agent parse(String name, String data) {
		if (name.equalsIgnoreCase("EXPLOSION") || name.equalsIgnoreCase("EXPLOSION_ANY")) return new ExplosionAgent();
		name = name.toUpperCase().replace("EXPLOSION_", "");
		if(name.equals("TNT")) return new ExplosionAgent(Material.TNT);
		else if(name.equals("FIRE") || name.equals("FIREBALL"))
			return new ExplosionAgent(Material.FIRE);
		OtherDrops.logInfo("Parsing explosion for: "+name, Verbosity.HIGH);
		CreatureType creature = CommonEntity.getCreatureType(name);
		Data cdata = CreatureData.parse(creature, data);
		if(cdata != null) return new ExplosionAgent(creature, cdata);
		return new ExplosionAgent(creature);
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(ToolDamage amount, Random rng) {}

	@Override
	public Location getLocation() {
		if(bomb != null) return bomb.getLocation();
		return null;
	}

	@Override
	public String toString() {
		if(explosive == null) return "EXPLOSION_ANY";
		if (explosive instanceof CreatureSubject) return "EXPLOSION_"+((CreatureSubject)explosive).toString().replace("CREATURE_", "");
		return "EXPLOSION_" + explosive.toString();
	}

	@Override
	public Data getData() {
		CreatureSubject creature = null;
		if (explosive instanceof CreatureSubject) 
			creature = (CreatureSubject)explosive;
		return creature == null ? null : creature.getData();
	}
	
	@Override
	public String getReadableName() {
		return toString();
	}

}
