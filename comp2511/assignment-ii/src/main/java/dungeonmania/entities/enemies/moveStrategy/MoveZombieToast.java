package dungeonmania.entities.enemies.moveStrategy;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Game;
import dungeonmania.entities.enemies.ZombieToast;
import dungeonmania.util.Position;

public class MoveZombieToast extends MoveStrategy {
    public void move(Game game, ZombieToast zombieToast) {
        Position nextPos;
        if (hasInvincibilityPotion(game)) {
            nextPos = super.moveWithInvincibility(game, zombieToast);
        } else {
            Position enemyPos = zombieToast.getPosition();
            List<Position> pos = enemyPos.getCardinallyAdjacentPositions();
            pos = pos.stream().filter(p -> game.canMoveTo(zombieToast, p)).collect(Collectors.toList());
            nextPos = pos.size() == 0 ? enemyPos : pos.get(zombieToast.getNextRandom(pos.size()));
        }
        game.getMap().moveTo(zombieToast, nextPos);

    }
}
