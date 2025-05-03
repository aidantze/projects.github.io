package dungeonmania.entities;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.entities.collectables.Bomb;
import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.OnMovedAway;
import dungeonmania.entities.interfaces.OnOverlap;
import dungeonmania.entities.interfaces.SubscriberInterface;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class Switch extends Entity implements OnOverlap, OnMovedAway, ConductorInterface {
    private boolean activated;
    private List<Bomb> bombs = new ArrayList<>();
    private List<SubscriberInterface> subs = new ArrayList<>();
    private int tickActivated;

    public Switch(Position position) {
        super(position.asLayer(Entity.ITEM_LAYER));
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

    public void subscribe(Bomb b) {
        bombs.add(b);
    }

    public void subscribe(Bomb bomb, GameMap map) {
        bombs.add(bomb);
        if (activated) {
            bombs.stream().forEach(b -> b.notify(map));
        }
    }

    public void unsubscribe(Bomb b) {
        bombs.remove(b);
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return true;
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof Boulder) {
            setActivated(map, true);
            bombs.stream().forEach(b -> b.notify(map));
            subs.stream().forEach(e -> e.notify(map, this));
            map.changeLights();
        }
    }

    @Override
    public void onMovedAway(GameMap map, Entity entity) {
        if (entity instanceof Boulder) {
            setActivated(map, false);
            subs.stream().forEach(e -> e.notify(map, this));
            map.changeLights();
        }
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
    public void subscribe(SubscriberInterface e) {
        subs.add(e);
    }

    @Override
    public void unsubscribe(SubscriberInterface e) {
        subs.remove(e);
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
