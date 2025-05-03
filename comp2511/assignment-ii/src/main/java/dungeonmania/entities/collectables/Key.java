package dungeonmania.entities.collectables;

import dungeonmania.entities.Entity;
import dungeonmania.entities.Player;
import dungeonmania.entities.SnakeFood;
import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class Key extends SnakeFood {
    private int number;

    public Key(Position position, int number) {
        super(position);
        this.number = number;
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return true;
    }

    @Override
    public void applyBuff(SnakeHead snakeHead) {
        snakeHead.multiplyHealth(snakeHead.getKeyBuff());
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (hasKey(player) || !((Player) entity).pickUp(this))
                return;
            map.destroyEntity(this);
        } else if (entity instanceof SnakeHead) {
            SnakeHead head = (SnakeHead) entity;
            head.onOverlap(map, this);
            map.destroyEntity(this);
        }
    }

    private boolean hasKey(Player player) {
        return player.getEntitiesOfType(Key.class).stream().count() == 1;
    }

    public int getnumber() {
        return number;
    }

}
