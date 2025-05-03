package dungeonmania.entities.enemies;

import dungeonmania.Game;
import dungeonmania.entities.Entity;
import dungeonmania.entities.Player;
import dungeonmania.entities.SnakeFood;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class SnakeBody extends Enemy {
    private SnakeHead head; // reference to head that the body belongs to
    private Position prevPos; // previous position of snake, for movement and extension

    public SnakeBody(Position position, double health, double attack, SnakeHead head) {
        super(position, health, attack);
        this.head = head;
    }

    public SnakeHead getHead() {
        return head;
    }

    public void setNewHead(SnakeHead head) {
        this.head = head;
    }

    @Override
    public void move(Game game) {
        return; // body parts move only when head moves
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof Player && (isHibernating() || canPathThroughWalls())) {
            return;
        }
        if (entity instanceof Player && !hasSnakeFoodInMap(map)) {
            head.setHibernating(true);
            return;
        }
        super.onOverlap(map, entity);
    }

    public boolean hasSnakeFoodInMap(GameMap map) {
        return map.getEntities(SnakeFood.class).stream().count() > 0;
    }

    public boolean isHibernating() {
        return head.isHibernating();
    }

    public void setHibernating(boolean hibernation) {
        head.setHibernating(hibernation);
    }

    public boolean canPathThroughWalls() {
        return head.canPathThroughWalls();
    }

    public void setCanPathThroughWalls(boolean canPathThroughWalls) {
        head.setCanPathThroughWalls(canPathThroughWalls);
    }

    public boolean isInvincible() {
        return head.isInvincible();
    }

    public void setInvincible(boolean isInvincible) {
        head.setInvincible(isInvincible);
    }

    public Position getPrevPos() {
        return prevPos;
    }

    public void setPrevPos(Position prevPos) {
        this.prevPos = prevPos;
    }
}
