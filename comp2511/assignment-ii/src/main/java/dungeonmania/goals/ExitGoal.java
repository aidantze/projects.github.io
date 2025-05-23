package dungeonmania.goals;

import java.util.List;

import dungeonmania.Game;
import dungeonmania.entities.Entity;
import dungeonmania.entities.Exit;
import dungeonmania.entities.Player;
import dungeonmania.util.Position;

public class ExitGoal extends Goal {
    public ExitGoal(String type) {
        super(type);
    }

    @Override
    public boolean otherAchieved(Game game) {
        Player character = game.getPlayer();
        Position pos = character.getPosition();
        List<Exit> es = game.getEntitiesOfType(Exit.class);
        if (es == null || es.size() == 0)
            return false;
        return es.stream().map(Entity::getPosition).anyMatch(pos::equals);
    }
}
