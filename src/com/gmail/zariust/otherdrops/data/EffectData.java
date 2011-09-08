package com.gmail.zariust.otherdrops.data;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.gmail.zariust.otherdrops.OtherDrops;

public class EffectData implements Data {
	private int data;
	protected int radius;
	
	public EffectData(int d) {
		data = d;
	}
	
	public EffectData(Material mat) { // BLOCK_BREAK effect
		data = mat.getId();
	}

	public EffectData(BlockFace face) { // SMOKE effect
		switch(face) {
		case EAST: data = 3; break;
		case NORTH: data = 7; break;
		case NORTH_EAST: data = 6; break;
		case NORTH_WEST: data = 8; break;
		case SOUTH: data = 1; break;
		case SOUTH_EAST: data = 0; break;
		case SOUTH_WEST: data = 2; break;
		case UP: data = 4; break;
		case WEST: data = 5; break;
		default: data = 4;
		}
	}

	@Override
	public int getData() {
		return data;
	}
	
	@Override
	public void setData(int d) {
		data = d;
	}

	public int getRadius() {
		return radius;
	}
	
	public void setRadius(int r) {
		radius = r;
	}
	
	@Override
	public boolean matches(Data d) {
		return data == d.getData();
	}
	
	@Override
	public String get(Enum<?> mat) {
		if(mat instanceof Effect) return get((Effect)mat);
		return "";
	}
	
	@SuppressWarnings("incomplete-switch")
	private String get(Effect effect) {
		switch(effect) {
		case STEP_SOUND: // actually BLOCK_BREAK
			return Material.getMaterial(data).toString();
		case SMOKE:
			return getDirection().toString();
		}
		return "";
	}
	
	public BlockFace getDirection() {
		switch(data) {
		case 0: return BlockFace.SOUTH_EAST;
		case 1: return BlockFace.SOUTH;
		case 2: return BlockFace.SOUTH_WEST;
		case 3: return BlockFace.EAST;
		case 4: return BlockFace.UP;
		case 5: return BlockFace.WEST;
		case 6: return BlockFace.NORTH_EAST;
		case 7: return BlockFace.NORTH;
		case 8: return BlockFace.NORTH_WEST;
		}
		return BlockFace.SELF;
	}

	@Override // No effect has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@Override // Effects are not entities, so nothing to do here.
	public void setOn(Entity entity, Player witness) {}

	public static EffectData parse(Effect effect, String state) {
		String[] split = state.split("/");
		String key = split[0];
		int radius = 16; // default radius that noise is heard within
		EffectData data;
		switch(effect) {
		case RECORD_PLAY:
			data = RecordData.parse(key);
			break;
		case SMOKE:
			BlockFace face = null;
			if (!state.isEmpty()) face = BlockFace.valueOf(key);
			if(face == null) {
				data = new EffectData(OtherDrops.rng.nextInt(9)); // default to random if no data specified
				break;
			}
			data = new EffectData(face);
			break;
		case STEP_SOUND: // apparently this is actually BLOCK_BREAK
			Material mat = Material.getMaterial(key);
			if(mat == null) return null;
			data = new EffectData(mat);
			break;
		default:
			data = new EffectData(0);
			break;
		}
		if(split.length > 1) {
			try {
				radius = Integer.parseInt(split[1]);
				data.setRadius(radius);
			} catch(NumberFormatException e) {}
		}
		data.setRadius(radius);
		return data;
	}
}
