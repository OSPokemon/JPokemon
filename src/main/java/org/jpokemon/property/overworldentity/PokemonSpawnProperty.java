package org.jpokemon.property.overworldentity;

import java.util.HashMap;
import java.util.Map;

import org.jpokemon.api.JPokemonException;
import org.jpokemon.api.PropertyProvider;
import org.jpokemon.util.Options;

/**
 * Use this class to decorate overworld entities that should have properties
 * like what species they spawn, etc.
 * 
 * Does not do anything.
 * 
 * @author zach
 *
 */
public class PokemonSpawnProperty {

	protected int maxLevel;

	protected int minLevel;

	protected Map<String, Integer> species;

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public Map<String, Integer> getSpecies() {
		if (species == null) {
			species = new HashMap<String, Integer>();
		}

		return species;
	}

	public void setSpecies(Map<String, Integer> species) {
		this.species = species;
	}

	public static class Provider extends PropertyProvider<PokemonSpawnProperty> {
		@Override
		public String getName() {
			return PokemonSpawnProperty.class.getName();
		}

		@Override
		public PokemonSpawnProperty build(String o) throws JPokemonException {
			Map<String, String> options = Options.parseMap(o);
			PokemonSpawnProperty property = new PokemonSpawnProperty();

			for (Map.Entry<String, String> optionEntry : options.entrySet()) {
				String key = optionEntry.getKey();
				Integer value = Integer.parseInt(optionEntry.getValue());

				if ("max-level".equals(key)) {
					property.setMaxLevel(value);
				}
				else if ("min-level".equals(key)) {
					property.setMinLevel(value);
				}
				else {
					property.getSpecies().put(key, value);
				}
			}

			return null;
		}

		@Override
		public String serialize(Object object) throws JPokemonException {
			PokemonSpawnProperty property = (PokemonSpawnProperty) object;

			Map<String, String> map = new HashMap<String, String>();
			for (Map.Entry<String, Integer> speciesEntry : property.getSpecies().entrySet()) {
				map.put(speciesEntry.getKey(), speciesEntry.getValue() + "");
			}
			map.put("max-level", property.getMaxLevel() + "");
			map.put("min-level", property.getMaxLevel() + "");

			return Options.serializeMap(map);
		}
	}
}
