package dungeonmania.entities;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.entities.enemies.Spider;
import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.LogicalEntityInterface;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class SwitchDoor extends Entity implements LogicalEntityInterface {
    private boolean activated;
    private List<ConductorInterface> conductors;
    private String logic;
    private LogicWrapper wrapper;

    public SwitchDoor(Position position, String logic) {
        super(position);
        this.activated = false;
        this.conductors = new ArrayList<>();
        this.logic = logic;
        this.wrapper = new LogicWrapper(this);
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        if (isActivated() || entity instanceof Spider) {
            return true;
        }
        return (entity instanceof Player && isActivated());
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
    public List<ConductorInterface> getConductors() {
        return conductors;
    }

    @Override
    public void notify(GameMap map, ConductorInterface c) {
        if (!wrapper.logicRequirements(getLogic())) {
            this.activated = false;
        } else {
            this.activated = true;
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
    public LogicWrapper getWrapper() {
        return wrapper;
    }
}
