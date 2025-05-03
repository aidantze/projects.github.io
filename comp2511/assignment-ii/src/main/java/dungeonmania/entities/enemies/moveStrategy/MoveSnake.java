package dungeonmania.entities.enemies.moveStrategy;

import java.util.List;

import dungeonmania.Game;
import dungeonmania.entities.Entity;
import dungeonmania.entities.SnakeFood;
import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.entities.enemies.SnakeBody;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class MoveSnake extends MoveStrategy {
    public void move(Game game, SnakeHead head) {
        if (head.isHibernating()) {
            return;
        }
        if (!hasFoodInMap(game)) {
            head.setHibernating(true);
            return;
        }

        Position nextPos;
        GameMap map = game.getMap();

        SnakeFood nearestFood = findNearestFood(map, head);

        Position prevPos = head.getPosition();
        head.setPrevPos(prevPos);
        nextPos = dijkstraPathFind(game, nearestFood, head);
        map.moveTo(head, nextPos);

        for (SnakeBody body : head.getBodyParts()) {
            prevPos = moveBody(game, body, prevPos);
        }
    }

    // move current body part to prevPos of either head or previous body part
    // returns prevPos of current body part for the next body part
    public Position moveBody(Game game, SnakeBody body, Position nextPos) {
        GameMap map = game.getMap();
        Position prevPos = body.getPosition();
        map.moveTo(body, nextPos);
        body.setPrevPos(prevPos);
        return prevPos;
    }

    public boolean hasFoodInMap(Game game) {
        return game.getEntitiesOfType(SnakeFood.class).stream().count() > 0;
    }

    public int magnitude(SnakeHead head, Entity entity) {
        return Position.calculatePositionBetween(head.getPosition(), entity.getPosition()).magnitude();
    }

    public SnakeFood findNearestFood(GameMap map, SnakeHead head) {
        List<SnakeFood> foods = map.getEntities(SnakeFood.class);

        // find nearest food using magnitude and calculatePositionBetween
        SnakeFood nearestFood = foods.get(0);
        int minMagnitude = magnitude(head, nearestFood);
        for (SnakeFood food : foods.subList(1, foods.size())) {
            int mag = magnitude(head, food);
            if (mag < minMagnitude) {
                minMagnitude = mag;
                nearestFood = food;
            }
        }
        return nearestFood;
    }

    public Position dijkstraPathFind(Game game, Entity entity, SnakeHead head) {
        return game.dijkstraPathFind(head.getPosition(), entity.getPosition(), head);
    }
}
