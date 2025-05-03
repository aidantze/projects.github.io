package dungeonmania.entities.enemies.moveStrategy;

import java.util.List;

import dungeonmania.Game;
import dungeonmania.entities.Boulder;
import dungeonmania.entities.Entity;
import dungeonmania.entities.enemies.Spider;
import dungeonmania.util.Position;

public class MoveSpider extends MoveStrategy {
    public void move(Game game, Spider spider) {
        Position nextPos = spider.getNextPositionElement();
        List<Entity> entities = game.getEntities(nextPos);
        if (checkEntitiesList(entities) && entities.stream().anyMatch(e -> e instanceof Boulder)) {
            spider.setForward(!spider.isForward());
            spider.updateNextPosition();
            spider.updateNextPosition();
        }
        nextPos = spider.getNextPositionElement();
        entities = game.getMap().getEntities(nextPos);
        if (!checkEntitiesList(entities) || entities.stream().allMatch(e -> e.canMoveOnto(game.getMap(), spider))) {
            game.moveTo(spider, nextPos);
            spider.updateNextPosition();
        }
    }

    public boolean checkEntitiesList(List<Entity> entities) {
        return entities != null && entities.size() > 0;
    }
}
