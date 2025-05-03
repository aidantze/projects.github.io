package dungeonmania.goals;

import dungeonmania.Game;

public class EnemyGoal extends Goal {
    public EnemyGoal(String type, int target) {
        super(type, target);
    }

    @Override
    public boolean otherAchieved(Game game) {
        return (game.getEnemiesDefeatedCount() >= getTarget()) && game.allSpawnersDestroyed();
    }
}
