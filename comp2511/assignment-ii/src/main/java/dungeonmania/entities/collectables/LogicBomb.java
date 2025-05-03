package dungeonmania.entities.collectables;

import dungeonmania.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.entities.Entity;
import dungeonmania.entities.LogicWrapper;
import dungeonmania.entities.Player;
import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.LogicalEntityInterface;
import dungeonmania.entities.inventory.InventoryItem;
import dungeonmania.map.GameMap;

public class LogicBomb extends InventoryItem implements LogicalEntityInterface {
    private String logic;
    private boolean activated;
    private List<ConductorInterface> conductors;
    private LogicWrapper wrapper;

    public enum State {
        SPAWNED, INVENTORY, PLACED
    }

    public static final int DEFAULT_RADIUS = 1;
    private State state;
    private int radius;

    public LogicBomb(Position position, int radius, String logic) {
        super(position);
        this.radius = radius;
        this.logic = logic;
        this.activated = false;
        this.conductors = new ArrayList<>();
        this.wrapper = new LogicWrapper(this);
        this.state = State.SPAWNED;
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return true;
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (state != State.SPAWNED)
            return;
        if (entity instanceof Player) {
            if (!((Player) entity).pickUp(this))
                return;
            conductors.stream().forEach(c -> c.unsubscribe(this));
            map.destroyEntity(this);
        }
        this.state = State.INVENTORY;
    }

    public void onPutDown(GameMap map, Position p) {
        translate(Position.calculatePositionBetween(getPosition(), p));
        map.addEntity(this);
        this.state = State.PLACED;
        // List<Position> adjPosList = getPosition().getCardinallyAdjacentPositions();
        // adjPosList.stream().forEach(node -> {
        //     List<Entity> entities = map.getEntities(node).stream().filter(e -> (e instanceof Switch))
        //             .collect(Collectors.toList());
        // entities.stream().map(Switch.class::cast).forEach(s -> s.subscribe(this, map));
        // entities.stream().map(Switch.class::cast).forEach(s -> this.subscribe(s));
        // });
        map.registerLogic(this);
    }

    public void explode(GameMap map) {
        int x = getPosition().getX();
        int y = getPosition().getY();
        for (int i = x - radius; i <= x + radius; i++) {
            for (int j = y - radius; j <= y + radius; j++) {
                List<Entity> entities = map.getEntities(new Position(i, j));
                entities = entities.stream().filter(e -> !(e instanceof Player)).collect(Collectors.toList());
                for (Entity e : entities)
                    map.destroyEntity(e);
            }
        }
    }

    @Override
    public List<ConductorInterface> getConductors() {
        return conductors;
    }

    @Override
    public void notify(GameMap map, ConductorInterface c) {
        if (wrapper.logicRequirements(logic)) {
            explode(map);
        }
    }

    @Override
    public void subscribe(ConductorInterface c) {
        conductors.add(c);
    }

    @Override
    public void unsubscribe(ConductorInterface c) {
        conductors.remove(c);
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @Override
    public String getLogic() {
        return logic;
    }

    @Override
    public LogicWrapper getWrapper() {
        return wrapper;
    }

}
