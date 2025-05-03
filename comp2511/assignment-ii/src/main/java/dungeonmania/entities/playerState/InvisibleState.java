package dungeonmania.entities.playerState;

import dungeonmania.battles.BattleStatistics;
import dungeonmania.entities.Player;

public class InvisibleState extends PlayerState {
    public InvisibleState(Player player) {
        super(player, false, true);
    }

    @Override
    public void transitionBase() {
        Player player = getPlayer();
        player.changeState(new BaseState(player));
    }

    @Override
    public void transitionInvincible() {
        Player player = getPlayer();
        player.changeState(new InvincibleState(player));
    }

    @Override
    public void transitionInvisible() {
        // do nothing: you are already invisible!
    }

    @Override
    public BattleStatistics getBattleStatistics(BattleStatistics origin) {
        return BattleStatistics.applyBuff(origin, new BattleStatistics(0, 0, 0, 1, 1, false, false));
    }
}
