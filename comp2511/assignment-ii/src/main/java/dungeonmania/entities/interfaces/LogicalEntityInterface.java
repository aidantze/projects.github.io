package dungeonmania.entities.interfaces;

import dungeonmania.entities.LogicWrapper;
import dungeonmania.util.Position;

public interface LogicalEntityInterface extends SubscriberInterface {
    public boolean isActivated();

    public String getLogic();

    public Position getPosition();

    public LogicWrapper getWrapper();

}
