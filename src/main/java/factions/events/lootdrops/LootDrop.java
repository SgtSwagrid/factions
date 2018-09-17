package factions.events.lootdrops;

import factions.util.Colour;
import static factions.util.Colour.*;

public enum LootDrop {
	
	COMMON("Common", WHITE, 1, 0.25F, 300, 600, 0, 2, 3, 5, 0, new float[] {0.2F, 0.4F, 0.4F}),
	RARE("Rare", BLUE, 1, 0.2F, 400, 800, 2, 2, 4, 10, 0, new float[] {0.4F, 0.4F, 0.2F}),
	EPIC("Epic", PINK, 2, 0.15F, 500, 1000, 6, 2, 5, 10, 0, new float[] {0.6F, 0.3F, 0.1F}),
	LEGENDARY("Legendary", ORANGE, 3, 0.1F, 1000, 2000, 12, 3, 6, 15, 0, new float[] {0.8F, 0.2F}),
	INFERNO("Inferno", RED, 4, 0.06F, 500, 1000, 15, 3, 7, 15, -1, new float[] {1.0F}),
	DRACONIC("Draconic", MAGENTA, 4, 0.04F, 0, 100, 18, 3, 8, 20, 1, new float[] {1.0F}),
	SCATTER("Scatter", GREEN, 1, 0.2F, 300, 1000, 2, 3, 6, 10, 0,
			new float[] {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2F, 0.2F, 0.2F, 0.2F, 0.2F});
	
	public final String NAME;
	
	public final Colour COLOUR;
	
	public final int POINTS;
	
	public final float WEIGHT;
	
	public final int MIN_DIST;
	
	public final int MAX_DIST;
	
	public final int WARM_UP;
	
	public final int MIN_TEAMS;
	
	public final int MIN_PLAYERS;
	
	public final int COUNTDOWN;
	
	public final int DIMENSION;
	
	public final float[] SCATTERING;
	
	LootDrop(String name, Colour colour, int points, float weight, int minDist, int maxDist, int warmUp,
			int minTeams, int minPlayers, int countdown, int dimension, float[] scattering) {
		NAME = name;
		COLOUR = colour;
		POINTS = points;
		WEIGHT = weight;
		MIN_DIST = minDist;
		MAX_DIST = maxDist;
		WARM_UP = warmUp;
		MIN_TEAMS = minTeams;
		MIN_PLAYERS = minPlayers;
		COUNTDOWN = countdown;
		DIMENSION = dimension;
		SCATTERING = scattering;
	}
}