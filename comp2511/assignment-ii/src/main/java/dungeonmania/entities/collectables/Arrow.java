package dungeonmania.entities.collectables;

import dungeonmania.entities.Entity;
import dungeonmania.entities.SnakeFood;
import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class Arrow extends SnakeFood {
    public Arrow(Position position) {
        super(position);
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return true;
    }

    @Override
    public void applyBuff(SnakeHead snakeHead) {
        snakeHead.addAttack(snakeHead.getArrowBuff());
    }
}
