package dungeonmania.entities;

import java.util.stream.Collectors;

import dungeonmania.entities.interfaces.ConductorInterface;
import dungeonmania.entities.interfaces.LogicalEntityInterface;

public class LogicWrapper {
    private LogicalEntityInterface e;

    public LogicWrapper(LogicalEntityInterface e) {
        this.e = e;
    }

    public int getActiveAdjacent() {
        return e.getConductors().stream().filter(e -> e.isActivated()).collect(Collectors.toList()).size();
    }

    public int getAllAdjacent() {
        return e.getConductors().size();
    }

    public boolean logicRequirements(String logic) { // applies to entities with logic field
        switch (logic) {
        case "or":
            return (getActiveAdjacent() >= 1);
        case "and":
            return (getActiveAdjacent() >= 2 && getActiveAdjacent() == getAllAdjacent());
        case "xor":
            return (getActiveAdjacent() == 1);
        case "co_and":
            return coandValid();
        default:
            return false;
        }
    }

    public boolean coandValid() {
        if (e.getConductors().size() == 0) {
            return false;
        }
        ConductorInterface firstIndex = e.getConductors().stream().filter(c -> c.isActivated())
                .collect(Collectors.toList()).get(0);
        for (ConductorInterface c : e.getConductors()) {
            if (firstIndex.getTickActivated() != c.getTickActivated()) {
                return false;
            }
        }
        return getActiveAdjacent() == getAllAdjacent() && getActiveAdjacent() >= 2;
    }
}
