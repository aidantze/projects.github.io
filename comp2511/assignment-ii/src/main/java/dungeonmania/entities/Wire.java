package dungeonmania.entities;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.LogicalEntityInterface;
import dungeonmania.entities.interfaces.SubscriberInterface;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class Wire extends Entity implements ConductorInterface, SubscriberInterface {
    private boolean activated;
    private List<SubscriberInterface> subs;
    private List<ConductorInterface> conductors;
    private int tickActivated;

    public Wire(Position position) {
        super(position);
        this.activated = false;
        this.subs = new ArrayList<>();
        this.conductors = new ArrayList<>();
        this.tickActivated = -1;
    }

    @Override
    public int getTickActivated() {
        return tickActivated;
    }

    @Override
    public void setTickActivated(int tickActivated) {
        this.tickActivated = tickActivated;
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return true;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @Override
    public List<SubscriberInterface> getSubscribers() {
        return subs;
    }

    @Override
    public List<ConductorInterface> getConductors() {
        return conductors;
    }

    @Override
    public void subscribe(SubscriberInterface e) {
        subs.add(e);
    }

    @Override
    public void unsubscribe(SubscriberInterface e) {
        subs.remove(e);
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
    public void notify(GameMap map, ConductorInterface c) {
        if (c.isActivated()) {
            setActivated(map, true);
            subs.stream().forEach(n -> {
                if (!n.isActivated()
                        || (n instanceof LogicalEntityInterface
                                && ((LogicalEntityInterface) n).getLogic().equals("xor"))
                        || (n instanceof LogicalEntityInterface
                                && ((LogicalEntityInterface) n).getLogic().equals("co_and"))) {
                    n.notify(map, this);
                }
            });
        } else {
            setActivated(map, false);
            subs.stream().forEach(n -> {
                if (n.isActivated()
                        || (n instanceof LogicalEntityInterface
                                && ((LogicalEntityInterface) n).getLogic().equals("xor"))
                        || (n instanceof LogicalEntityInterface
                                && ((LogicalEntityInterface) n).getLogic().equals("co_and"))) {
                    n.notify(map, this);
                }
            });

        }
    }

    @Override
    public void setActivated(GameMap map, boolean state) {
        this.activated = state;
        if (state) {
            tickActivated = map.getTick();
        } else {
            tickActivated = -1;
        }
    }

}
