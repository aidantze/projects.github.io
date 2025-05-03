package dungeonmania.entities.enemies.moveStrategy;

import dungeonmania.Game;
import dungeonmania.entities.collectables.potions.InvincibilityPotion;
import dungeonmania.entities.collectables.potions.InvisibilityPotion;
import dungeonmania.entities.enemies.Enemy;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public abstract class MoveStrategy {
    public boolean hasInvisibilityPotion(Game game) {
        return game.getEffectivePotion() instanceof InvisibilityPotion;
    }

    public boolean hasInvincibilityPotion(Game game) {
        return game.getEffectivePotion() instanceof InvincibilityPotion;
    }

    public Position moveWithInvincibility(Game game, Enemy enemy) {
        Position enemyPos = enemy.getPosition();
        Position plrDiff = Position.calculatePositionBetween(game.getPlayerPosition(), enemyPos);

        Position moveX = (plrDiff.getX() >= 0) ? Position.translateBy(enemyPos, Direction.RIGHT)
                : Position.translateBy(enemyPos, Direction.LEFT);
        Position moveY = (plrDiff.getY() >= 0) ? Position.translateBy(enemyPos, Direction.UP)
                : Position.translateBy(enemyPos, Direction.DOWN);

        if (game.canMoveTo(enemy, moveX))
            return moveX;
        if (game.canMoveTo(enemy, moveY))
            return moveY;
        return enemyPos;
    }
}
