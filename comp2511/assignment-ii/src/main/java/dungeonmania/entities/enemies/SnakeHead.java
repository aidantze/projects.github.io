package dungeonmania.entities.enemies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dungeonmania.Game;
import dungeonmania.entities.Entity;
import dungeonmania.entities.Player;
import dungeonmania.entities.SnakeFood;
import dungeonmania.entities.enemies.moveStrategy.MoveSnake;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public class SnakeHead extends Enemy {
    private List<SnakeBody> bodyParts = new ArrayList<>();

    private double arrowBuff;
    private double treasureBuff;
    private double keyBuff;

    private boolean isHibernating = false; // sleepy boi
    private boolean canPathThroughWalls = false; // sneaky boi
    private boolean isInvincible = false; // unstoppable boi
    private Position prevPos; // previous position of snake, for movement and extension

    public SnakeHead(Position position, double health, double attack, double arrowBuff, double treasureBuff,
            double keyBuff) {
        super(position, health, attack);
        this.arrowBuff = arrowBuff;
        this.treasureBuff = treasureBuff;
        this.keyBuff = keyBuff;
    }

    public List<SnakeBody> getBodyParts() {
        return bodyParts;
    }

    public int length() {
        return 1 + bodyParts.size();
    }

    public boolean hasBodyPart() {
        return length() > 1;
    }

    public void addBodyPart(SnakeBody snakeBody) {
        bodyParts.add(snakeBody);
    }

    public void addBodyPart(GameMap map, Position position) {
        Game game = map.getGame();
        SnakeBody body = game.createSnakeBody(map, this, position);
        addBodyPart(body);
    }

    public void removeBodyPart(SnakeBody snakeBody) {
        bodyParts.remove(snakeBody);
    }

    // function uses List Iterator to remove from list
    public void removeBodyParts(GameMap map, List<SnakeBody> removeBodyParts) {
        Iterator<SnakeBody> iter = removeBodyParts.iterator();
        while (iter.hasNext()) {
            SnakeBody body = iter.next();
            map.destroyEntity(body);
            iter.remove();
        }
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof SnakeFood) {
            addBodyPart(map, getLastPrevPos());
            eatFood((SnakeFood) entity);
            return;
        }
        if (entity instanceof Player && (isHibernating || canPathThroughWalls)) {
            return;
        }
        if (entity instanceof Player && !hasSnakeFoodInMap(map)) {
            setHibernating(true);
            return;
        }
        super.onOverlap(map, entity);
    }

    public boolean hasSnakeFoodInMap(GameMap map) {
        return map.getEntities(SnakeFood.class).stream().count() > 0;
    }

    // snakeHead was destroyed by the player
    public void snakeHeadDefeat(GameMap map) {
        Iterator<SnakeBody> iter = bodyParts.iterator();
        while (iter.hasNext()) {
            SnakeBody body = iter.next();
            map.destroyEntity(body);
            iter.remove();
        }
        map.destroyEntity(this);
    }

    // snakeBody part was destroyed by the player
    public void snakeBodyDefeat(GameMap map, SnakeBody body) {
        List<SnakeBody> newBodyParts = dissectBodyPart(body);
        if (body.isInvincible()) {
            if (newBodyParts.size() == 0) {
                map.destroyEntity(body);
                return;
            }
            formNewSnake(map, newBodyParts);
            map.destroyEntity(body);
            return;
        }
        removeBodyParts(map, newBodyParts);
        map.destroyEntity(body);
    }

    public void formNewSnake(GameMap map, List<SnakeBody> newBodyParts) {
        Game game = map.getGame();
        SnakeBody firstBodyPart = newBodyParts.get(0);
        Position firstBodyPos = firstBodyPart.getPosition();
        SnakeHead newHead = game.createNewSnakeHead(map, this, firstBodyPos);
        newHead.setInvincible(true);
        newHead.setCanPathThroughWalls(canPathThroughWalls);
        newHead.setHibernating(isHibernating);

        // if only one body part, then that forms head of new snake, no new body parts to add
        if (newBodyParts.size() > 1) {
            for (SnakeBody newBody : newBodyParts.subList(1, newBodyParts.size())) {
                newBody.setNewHead(newHead);
                newHead.addBodyPart(newBody);
            }
        }
        map.destroyEntity(firstBodyPart);
        newHead.move(game);
    }

    // removes only following body parts after snakeBody and returns it as a list
    public List<SnakeBody> dissectBodyPart(SnakeBody snakeBody) {
        List<SnakeBody> newSnake = new ArrayList<>();
        for (SnakeBody body : bodyParts) {
            if (body.getId().equals(snakeBody.getId())) {
                int index = bodyParts.indexOf(body);
                if (index == 0 && index == bodyParts.size() - 1) { // only body part in snake
                    this.bodyParts = new ArrayList<SnakeBody>();
                } else if (index == 0) { // first body part in snake
                    newSnake = bodyParts.subList(index + 1, bodyParts.size());
                    this.bodyParts = new ArrayList<SnakeBody>();
                } else if (index == bodyParts.size() - 1) { // last body part in snake
                    this.bodyParts = bodyParts.subList(0, index);
                } else {
                    this.bodyParts = bodyParts.subList(0, index);
                    newSnake = bodyParts.subList(index + 1, bodyParts.size());
                }
                break;
            }
        }
        return newSnake;
    }

    public void eatFood(SnakeFood food) {
        food.applyBuff(this);
    }

    public double getArrowBuff() {
        return arrowBuff;
    }

    public double getTreasureBuff() {
        return treasureBuff;
    }

    public double getKeyBuff() {
        return keyBuff;
    }

    public void addHealth(double health) {
        super.addHealth(health);
    }

    public void multiplyHealth(double factor) {
        super.multiplyHealth(factor);
    }

    public void addAttack(double attack) {
        super.addAttack(attack);
    }

    public void applyInvisiBuff() {
        setCanPathThroughWalls(true);
    }

    public void applyInvinciBuff() {
        setInvincible(true);
    }

    @Override
    public void move(Game game) {
        MoveSnake ms = new MoveSnake();
        ms.move(game, this);
    }

    public boolean isHibernating() {
        return isHibernating;
    }

    public void setHibernating(boolean hibernation) {
        this.isHibernating = hibernation;
    }

    public boolean canPathThroughWalls() {
        return canPathThroughWalls;
    }

    public void setCanPathThroughWalls(boolean canPathThroughWalls) {
        this.canPathThroughWalls = canPathThroughWalls;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean isInvincible) {
        this.isInvincible = isInvincible;
    }

    // get prevPos of head (before movement)
    public Position getPrevPos() {
        return prevPos;
    }

    public void setPrevPos(Position prevPos) {
        this.prevPos = prevPos;
    }

    // get prevPos of last body part, or head if no body parts (before movement)
    public Position getLastPrevPos() {
        if (hasBodyPart()) {
            return bodyParts.get(bodyParts.size() - 1).getPrevPos();
        }
        return prevPos;
    }

}
