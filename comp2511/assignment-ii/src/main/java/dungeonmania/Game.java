package dungeonmania;

import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

import dungeonmania.battles.BattleFacade;
import dungeonmania.entities.Entity;
import dungeonmania.entities.EntityFactory;
import dungeonmania.entities.Interactable;
import dungeonmania.entities.Player;
import dungeonmania.entities.buildables.Buildable;
import dungeonmania.entities.collectables.Bomb;
import dungeonmania.entities.collectables.LogicBomb;
import dungeonmania.entities.collectables.potions.Potion;
import dungeonmania.entities.enemies.Enemy;
import dungeonmania.entities.enemies.SnakeBody;
import dungeonmania.entities.enemies.SnakeHead;
import dungeonmania.entities.enemies.ZombieToastSpawner;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.goals.Goal;
import dungeonmania.map.GameMap;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class Game {
    private String id;
    private String name;
    private Goal goals;
    private GameMap map;
    private Player player;
    private BattleFacade battleFacade;
    private EntityFactory entityFactory;
    private boolean isInTick = false;
    public static final int PLAYER_MOVEMENT = 0;
    public static final int PLAYER_MOVEMENT_CALLBACK = 1;
    public static final int AI_MOVEMENT = 2;
    public static final int AI_MOVEMENT_CALLBACK = 3;
    public static final int ITEM_LONGEVITY_UPDATE = 4;

    private ComparableCallback currentAction = null;

    private int tickCount = 0;
    private PriorityQueue<ComparableCallback> sub = new PriorityQueue<>();
    private PriorityQueue<ComparableCallback> addingSub = new PriorityQueue<>();

    public Game(String dungeonName) {
        this.name = dungeonName;
        this.map = new GameMap();
        this.battleFacade = new BattleFacade();
    }

    public void init() {
        this.id = UUID.randomUUID().toString();
        map.init();
        this.tickCount = 0;
        player = map.getPlayer();
        register(() -> player.onTick(tickCount), PLAYER_MOVEMENT, "potionQueue");
    }

    public Game tick(Direction movementDirection) {
        registerOnce(() -> player.move(this.getMap(), movementDirection), PLAYER_MOVEMENT, "playerMoves");
        tick();
        return this;
    }

    public Game tick(String itemUsedId) throws InvalidActionException {
        Entity item = player.getEntity(itemUsedId);
        if (item == null)
            throw new InvalidActionException(String.format("Item with id %s doesn't exist", itemUsedId));
        if (!(item instanceof Bomb) && !(item instanceof LogicBomb) && !(item instanceof Potion))
            throw new IllegalArgumentException(String.format("%s cannot be used", item.getClass()));

        registerOnce(() -> {
            if (item instanceof Bomb)
                player.use((Bomb) item, map);
            if (item instanceof LogicBomb)
                player.use((LogicBomb) item, map);
            if (item instanceof Potion)
                player.use((Potion) item, tickCount);
        }, PLAYER_MOVEMENT, "playerUsesItem");
        tick();
        return this;
    }

    public void battle(Player player, Enemy enemy) {
        battleFacade.battle(this, player, enemy);
        if (player.getHealth() <= 0) {
            map.destroyEntity(player);
        }
        if (enemy.getHealth() <= 0) {
            if (enemy instanceof SnakeBody) {
                snakeBodyDefeat((SnakeBody) enemy);
            } else if (enemy instanceof SnakeHead) {
                ((SnakeHead) enemy).snakeHeadDefeat(map);
            } else {
                map.destroyEntity(enemy);
            }
        }
    }

    public void snakeBodyDefeat(SnakeBody body) {
        body.getHead().snakeBodyDefeat(map, body);
    }

    public Game build(String buildable) throws InvalidActionException {
        List<String> buildables = player.getBuildables();
        if (!buildables.contains(buildable)) {
            throw new InvalidActionException(String.format("%s cannot be built", buildable));
        }
        registerOnce(() -> player.build(buildable, entityFactory), PLAYER_MOVEMENT, "playerBuildsItem");
        tick();
        return this;
    }

    public Game interact(String entityId) throws IllegalArgumentException, InvalidActionException {
        Entity e = map.getEntity(entityId);
        if (e == null || !(e instanceof Interactable))
            throw new IllegalArgumentException("Entity cannot be interacted");
        if (!((Interactable) e).isInteractable(player)) {
            throw new InvalidActionException("Entity cannot be interacted");
        }
        registerOnce(() -> ((Interactable) e).interact(player, this), PLAYER_MOVEMENT, "playerInteracts");
        tick();
        return this;
    }

    public void register(Runnable r, int priority, String id) {
        if (isInTick)
            addingSub.add(new ComparableCallback(r, priority, id));
        else
            sub.add(new ComparableCallback(r, priority, id));
    }

    public void registerOnce(Runnable r, int priority, String id) {
        if (isInTick)
            addingSub.add(new ComparableCallback(r, priority, id, true));
        else
            sub.add(new ComparableCallback(r, priority, id, true));
    }

    public void unsubscribe(String id) {
        if (this.currentAction != null && id.equals(this.currentAction.getId())) {
            this.currentAction.invalidate();
        }

        invalidateCallback(id, sub);
        invalidateCallback(id, addingSub);
    }

    public void invalidateCallback(String id, PriorityQueue<ComparableCallback> sub) {
        for (ComparableCallback c : sub) {
            if (id.equals(c.getId())) {
                c.invalidate();
            }
        }
    }

    public int tick() {
        PriorityQueue<ComparableCallback> nextTickSub = new PriorityQueue<>();
        isInTick = true;
        while (!sub.isEmpty()) {
            currentAction = sub.poll();
            currentAction.run();
            if (currentAction.isValid()) {
                nextTickSub.add(currentAction);
            }
        }
        map.changeLights();
        isInTick = false;
        nextTickSub.addAll(addingSub);
        addingSub = new PriorityQueue<>();
        sub = nextTickSub;
        tickCount++;
        return tickCount;
    }

    public int getTick() {
        return this.tickCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Goal getGoals() {
        return goals;
    }

    public void setGoals(Goal goals) {
        this.goals = goals;
    }

    public GameMap getMap() {
        return map;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public List<Entity> getEntities() {
        return map.getEntities();
    }

    public List<Entity> getEntities(Position position) {
        return map.getEntities(position);
    }

    public <T extends Entity> List<T> getEntitiesOfType(Class<T> type) {
        return map.getEntities(type);
    }

    public void addEntity(Entity entity) {
        map.addEntity(entity);
    }

    public void destroyEntity(Entity entity) {
        map.destroyEntity(entity);
    }

    public void moveTo(Entity entity, Position position) {
        map.moveTo(entity, position);
    }

    public boolean canMoveTo(Entity entity, Position position) {
        return map.canMoveTo(entity, position);
    }

    public Position dijkstraPathFind(Position src, Position dest, Entity entity) {
        return map.dijkstraPathFind(src, dest, entity);
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory factory) {
        entityFactory = factory;
    }

    public void spawnSpider() {
        entityFactory.spawnSpider(this);
    }

    public void spawnZombie(ZombieToastSpawner spawner) {
        entityFactory.spawnZombie(this, spawner);
    }

    public SnakeHead createNewSnakeHead(GameMap map, SnakeHead oldHead, Position pos) {
        return entityFactory.createNewSnakeHead(map, oldHead, pos);
    }

    public SnakeBody createSnakeBody(GameMap map, SnakeHead head, Position pos) {
        return entityFactory.createSnakeBody(map, head, pos);
    }

    public int getCollectedTreasureCount() {
        return player.getCollectedTreasureCount();
    }

    public int getEnemiesDefeatedCount() {
        return player.getEnemiesDefeatedCount();
    }

    public void incEnemiesDefeatedCount() {
        player.incEnemiesDefeatedCount();
    }

    public boolean allSpawnersDestroyed() {
        if (map.getEntities(ZombieToastSpawner.class).size() == 0) {
            return true;
        }
        return false;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getPlayerPosition() {
        return player.getPosition();
    }

    public Potion getEffectivePotion() {
        return player.getEffectivePotion();
    }

    public <T extends Buildable> void removeBuildableFromPlayer(Buildable b) {
        player.remove(b);
    }

    public BattleFacade getBattleFacade() {
        return battleFacade;
    }
}
