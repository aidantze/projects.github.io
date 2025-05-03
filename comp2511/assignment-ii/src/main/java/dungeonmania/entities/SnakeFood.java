package dungeonmania.entities;

import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.entities.inventory.InventoryItem;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public abstract class SnakeFood extends InventoryItem {
    public SnakeFood(Position position) {
        super(position);
    }

    public abstract void applyBuff(SnakeHead snakeHead);

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof SnakeHead) {
            SnakeHead head = (SnakeHead) entity;
            head.onOverlap(map, this);
            map.destroyEntity(this);
        }
        super.onOverlap(map, entity);
    }
}
