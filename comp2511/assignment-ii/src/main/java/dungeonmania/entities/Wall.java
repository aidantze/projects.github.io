package dungeonmania.entities;

import dungeonmania.map.GameMap;
import dungeonmania.entities.enemies.SnakeBody;
import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.entities.enemies.Spider;
import dungeonmania.util.Position;

public class Wall extends Entity {
    public Wall(Position position) {
        super(position.asLayer(Entity.CHARACTER_LAYER));
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return entity instanceof Spider || snakeIsInvisible(entity);
    }

    private boolean snakeIsInvisible(Entity entity) {
        return (entity instanceof SnakeHead && ((SnakeHead) entity).canPathThroughWalls())
                || (entity instanceof SnakeBody && ((SnakeBody) entity).getHead().canPathThroughWalls());
    }
}
