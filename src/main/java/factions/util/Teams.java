package factions.util;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;

/**
 * Wrapper class for Minecraft teams.
 * @author Alec
 */
public class Teams {
	
	/** The world scoreboard. */
	private static final Scoreboard scoreboard =
			DimensionManager.getWorld(0).getScoreboard();
	
	/** The score objective. */
	private static final ScoreObjective score = getObjective("Score");
	
	static {
		//Display the score in the sidebar.
		scoreboard.setObjectiveInDisplaySlot(1, score);
	}
	
	/**
	 * Give/remove points to/from a specified team.
	 * @param team the team to give points to.
	 * @param points the number of points to add/take.
	 */
	public static void givePoints(Team team, int points) {
		String name = team.getColor() + team.getName() + TextFormatting.RESET;
		ScoreObjective score = scoreboard.getObjectiveInDisplaySlot(1);
		scoreboard.getOrCreateScore(name, score).increaseScore(points);
	}
	
	/**
	 * Switch to a temporary scoreboard with reset scores.
	 * Call popScore() to return to the main scoreboard after.
	 * @param name the name of the temporary scoreboard.
	 */
	public static void pushScore(String name) {
		scoreboard.setObjectiveInDisplaySlot(1, getObjective(name));
	}
	
	/**
	 * Return to the original scoreboard, adding the temporary scores to the main scoreboard.
	 * Negative values are discarded.
	 * This should be called after a previous call to pushScore().
	 */
	public static void popScore() {
		ScoreObjective oldScore = scoreboard.getObjectiveInDisplaySlot(1);
		//Add the temporary scores to the main scoreboard.
		for(Score s : scoreboard.getScores()) {
			if(s.getObjective() == oldScore) {
				scoreboard.getOrCreateScore(s.getPlayerName(), score)
						.increaseScore(Math.max(s.getScorePoints(), 0));
			}
		}
		//Switch back to the main scoreboard, removing the temporary one.
		scoreboard.removeObjective(oldScore);
		scoreboard.setObjectiveInDisplaySlot(1, score);
	}
	
	/**
	 * Finds the team of the given colour.
	 * @param colour the colour of team to find.
	 * @return the matching team.
	 */
	public static Team getTeam(Colour colour) {
		for(Team team : getTeams()) {
			if(team.getColor().equals(colour.FORMATTER)) {
				return team;
			}
		}
		return null;
	}
	
	/**
	 * @return a list of all the current teams.
	 */
	public static Collection<ScorePlayerTeam> getTeams() {
		return scoreboard.getTeams();
	}
	
	/**
	 * Determines whether a player is on the team of a particular colour.
	 * @param player the player to check.
	 * @param colour the colour of the team.
	 * @return whether the player is on the team.
	 */
	public static boolean isPlayerOnTeam(EntityPlayer player, Colour colour) {
		return player.getTeam().getColor().equals(colour.FORMATTER);
	}
	
	/**
	 * Get the scoreboard objective of the given name.
	 * Will create a new objective if it doesn't yet exist.
	 * @param name the name of the objective to find/create.
	 * @return the objective instance.
	 */
	private static ScoreObjective getObjective(String name) {
		ScoreObjective objective = scoreboard.getObjective(name);
		return objective != null ? objective :
				scoreboard.addScoreObjective(name, IScoreCriteria.DUMMY);
	}
}