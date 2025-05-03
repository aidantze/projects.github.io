package dungeonmania.goals;

import dungeonmania.Game;

public class TreasureGoal extends Goal {
    public TreasureGoal(String type, int target) {
        super(type, target);
    }

    @Override
    public boolean otherAchieved(Game game) {
        return game.getCollectedTreasureCount() >= getTarget();
    }
}
