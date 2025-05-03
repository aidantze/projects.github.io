package dungeonmania.goals;

import dungeonmania.Game;

public class OrGoal extends Goal {
    public OrGoal(String type, Goal goal1, Goal goal2) {
        super(type, goal1, goal2);
    }

    @Override
    public boolean otherAchieved(Game game) {
        return getGoal1().achieved(game) || getGoal2().achieved(game);
    }

    @Override
    public String toString(Game game) {
        return "(" + getGoal1().toString(game) + " OR " + getGoal2().toString(game) + ")";
    }

}
