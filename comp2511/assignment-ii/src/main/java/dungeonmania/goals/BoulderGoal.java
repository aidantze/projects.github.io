package dungeonmania.goals;

import dungeonmania.Game;
import dungeonmania.entities.Switch;

public class BoulderGoal extends Goal {
    public BoulderGoal(String type) {
        super(type);
    }

    @Override
    public boolean otherAchieved(Game game) {
        return game.getEntitiesOfType(Switch.class).stream().allMatch(s -> s.isActivated());
    }
}
