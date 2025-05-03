package dungeonmania.entities.enemies;

import dungeonmania.Game;
import dungeonmania.battles.BattleStatistics;
import dungeonmania.battles.Battleable;
import dungeonmania.entities.Entity;
import dungeonmania.entities.Player;
import dungeonmania.entities.interfaces.OnDestroy;
import dungeonmania.entities.interfaces.OnOverlap;
import dungeonmania.map.GameMap;
import dungeonmania.util.Position;

public abstract class Enemy extends Entity implements Battleable, OnOverlap, OnDestroy {
    private BattleStatistics battleStatistics;

    public Enemy(Position position, double health, double attack) {
        super(position.asLayer(Entity.CHARACTER_LAYER));
        battleStatistics = new BattleStatistics(health, attack, 0, BattleStatistics.DEFAULT_DAMAGE_MAGNIFIER,
                BattleStatistics.DEFAULT_ENEMY_DAMAGE_REDUCER);
    }

    @Override
    public boolean canMoveOnto(GameMap map, Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public BattleStatistics getBattleStatistics() {
        return battleStatistics;
    }

    public double getHealth() {
        return battleStatistics.getHealth();
    }

    public double getAttack() {
        return battleStatistics.getAttack();
    }

    public void addAttack(double attack) {
        battleStatistics.setAttack(getAttack() + attack);
    }

    public void addHealth(double health) {
        battleStatistics.setHealth(getHealth() + health);
    }

    public void multiplyHealth(double factor) {
        battleStatistics.setHealth(getHealth() * factor);
    }

    public void setHealth(double health) {
        battleStatistics.setHealth(health);
    }

    @Override
    public void onOverlap(GameMap map, Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            map.battle(player, this);
        }
    }

    @Override
    public void onDestroy(GameMap map) {
        Game g = map.getGame();
        g.unsubscribe(getId());
        g.incEnemiesDefeatedCount();
    }

    public abstract void move(Game game);
}
