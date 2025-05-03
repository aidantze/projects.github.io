package dungeonmania.entities.interfaces;

import java.util.List;

import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public interface ConductorInterface {
    public int getTickActivated();

    public void setTickActivated(int a);

    public void setActivated(GameMap map, boolean state);

    public boolean isActivated();

    public List<SubscriberInterface> getSubscribers();

    public void subscribe(SubscriberInterface s);

    public void unsubscribe(SubscriberInterface s);

    public Position getPosition();

}
