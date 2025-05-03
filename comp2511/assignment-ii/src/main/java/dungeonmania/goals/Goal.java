package dungeonmania.goals;

import dungeonmania.Game;

public abstract class Goal {
    private String type;
    private int target;
    private Goal goal1;
    private Goal goal2;

    public Goal(String type) {
        this.type = type;
    }

    public Goal(String type, int target) {
        this.type = type;
        this.target = target;
    }

    public Goal(String type, Goal goal1, Goal goal2) {
        this.type = type;
        this.goal1 = goal1;
        this.goal2 = goal2;
    }

    /**
     * @return true if the goal has been achieved, false otherwise
     */
    public boolean achieved(Game game) {
        if (game.getPlayer() == null)
            return false;
        return otherAchieved(game);
    }

    public String toString(Game game) {
        if (this.achieved(game))
            return "";
        return otherToString(game);
    }

    public boolean otherAchieved(Game game) {
        return false;
    }

    public String otherToString(Game game) {
        return ":" + getType();
    }

    public String getType() {
        return type;
    }

    public int getTarget() {
        return target;
    }

    public Goal getGoal1() {
        return goal1;
    }

    public Goal getGoal2() {
        return goal2;
    }
}
