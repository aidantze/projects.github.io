package dungeonmania.entities.enemies;

import java.util.Random;

import dungeonmania.Game;
import dungeonmania.entities.enemies.moveStrategy.MoveZombieToast;
import dungeonmania.util.Position;

public class ZombieToast extends Enemy {
    public static final double DEFAULT_HEALTH = 5.0;
    public static final double DEFAULT_ATTACK = 6.0;
    private Random randGen = new Random();

    public ZombieToast(Position position, double health, double attack) {
        super(position, health, attack);
    }

    public Random getRandom() {
        return randGen;
    }

    public int getNextRandom(int bound) {
        return randGen.nextInt(bound);
    }

    @Override
    public void move(Game game) {
        MoveZombieToast mzt = new MoveZombieToast();
        mzt.move(game, this);
    }

}
