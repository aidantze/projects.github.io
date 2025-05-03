package dungeonmania.entities.interfaces;

import java.util.List;

import dungeonmania.map.GameMap;

public interface SubscriberInterface {
    // public int getTickActivated();

    // public void setTickActivated(int a);

    // public void setActivated(GameMap map, boolean state);
    public boolean isActivated();

    public List<ConductorInterface> getConductors();

    public void notify(GameMap map, ConductorInterface c);

    public void subscribe(ConductorInterface c);

    public void unsubscribe(ConductorInterface c);
}
