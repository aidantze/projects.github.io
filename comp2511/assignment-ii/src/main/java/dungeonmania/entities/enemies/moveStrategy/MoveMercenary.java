package dungeonmania.entities.enemies.moveStrategy;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import dungeonmania.Game;
import dungeonmania.entities.Player;
import dungeonmania.entities.enemies.Mercenary;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class MoveMercenary extends MoveStrategy {
    public void move(Game game, Mercenary mercenary) {
        Position nextPos;
        GameMap map = game.getMap();
        if (mercenary.isAllied()) {
            nextPos = moveAlliedMercenary(game, mercenary);
        } else if (hasInvisibilityPotion(game)) {
            nextPos = moveWithInvisibility(game, mercenary);
        } else if (hasInvincibilityPotion(game)) {
            nextPos = super.moveWithInvincibility(game, mercenary);
        } else {
            // Follow hostile
            nextPos = dijkstraPathFind(game, mercenary);
        }
        map.moveTo(mercenary, nextPos);
    }

    public Position dijkstraPathFind(Game game, Mercenary mercenary) {
        return game.dijkstraPathFind(mercenary.getPosition(), game.getPlayerPosition(), mercenary);
    }

    public Position moveWithInvisibility(Game game, Mercenary mercenary) {
        // Move random
        Position nextPos;
        Random randGen = new Random();
        GameMap map = game.getMap();
        Position enemyPos = mercenary.getPosition();
        List<Position> pos = enemyPos.getCardinallyAdjacentPositions();
        pos = pos.stream().filter(p -> map.canMoveTo(mercenary, p)).collect(Collectors.toList());
        nextPos = pos.size() == 0 ? enemyPos : pos.get(randGen.nextInt(pos.size()));
        map.moveTo(mercenary, nextPos);
        return nextPos;
    }

    public Position moveAlliedMercenary(Game game, Mercenary mercenary) {
        Position nextPos;
        Player player = game.getPlayer();
        boolean isAdjacent = mercenary.isAdjacentToPlayer();
        nextPos = isAdjacent ? player.getPreviousDistinctPosition() : dijkstraPathFind(game, mercenary);
        if (!isAdjacent && Position.isAdjacent(player.getPosition(), nextPos))
            mercenary.setAdjacentToPlayer(true);
        return nextPos;
    }
}
