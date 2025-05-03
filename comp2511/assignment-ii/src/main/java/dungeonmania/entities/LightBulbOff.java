package dungeonmania.entities;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.LogicalEntityInterface;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class LightBulbOff extends Entity implements LogicalEntityInterface {
    private boolean activated;
    private List<ConductorInterface> conductors;
    private String logic;
    private LogicWrapper wrapper;

    public LightBulbOff(Position position, String logic) {
        super(position);
        this.activated = false;
        this.conductors = new ArrayList<>();
        this.logic = logic;
        this.wrapper = new LogicWrapper(this);
    }

    @Override
    public List<ConductorInterface> getConductors() {
        return conductors;
    }

    @Override
    public void notify(GameMap map, ConductorInterface c) {
        if (wrapper.logicRequirements(logic)) {
            LightBulbOn onLight = new LightBulbOn(getPosition(), getLogic());
            map.addEntity(onLight);
            map.addLightBulb(onLight);
            map.destroyEntity(this);
        }
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
