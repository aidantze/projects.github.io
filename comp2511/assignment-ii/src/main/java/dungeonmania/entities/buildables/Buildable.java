package dungeonmania.entities.buildables;

import dungeonmania.Game;
import dungeonmania.entities.BattleItem;
import dungeonmania.entities.inventory.InventoryItem;
import dungeonmania.util.Position;

public abstract class Buildable extends InventoryItem implements BattleItem {
    private int durability;

    public Buildable(Position position, int durability) {
        super(position);
        this.durability = durability;
    }

    public void use(Game game) {
        durability--;
        if (durability <= 0) {
            remove(game);
        }
    }

    public abstract void remove(Game game);

    public int getDurability() {
        return durability;
    }
}
