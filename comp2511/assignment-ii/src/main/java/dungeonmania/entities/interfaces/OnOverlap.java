package dungeonmania.entities.interfaces;

import dungeonmania.entities.Entity;
import dungeonmania.map.GameMap;

public interface OnOverlap {
    public void onOverlap(GameMap map, Entity entity);
}
