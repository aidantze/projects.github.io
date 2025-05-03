package dungeonmania.entities.inventory;

import dungeonmania.entities.Entity;
import dungeonmania.entities.Player;
import dungeonmania.entities.interfaces.OnOverlap;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

/**
 * A marker for InventoryItem
 */
public abstract class InventoryItem extends Entity implements OnOverlap {
    public InventoryItem(Position position) {
        super(position);
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof Player) {
            if (!((Player) entity).pickUp(this))
                return;
            map.destroyEntity(this);
        }
    }
}
