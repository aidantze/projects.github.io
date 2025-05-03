package dungeonmania.mvp;

import dungeonmania.DungeonManiaController;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SnakesTest {
    @Test
    @Tag("18-1")
    @DisplayName("Test a wild sleepy boi in its natural habitat (snake hibernating)")
    public void hibernate() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_noTreasure", "c_snakeTest");

        // dungeon layout matrix
        //     0   1   2   3   4
        // 0
        // 1       P   S
        // 2
        // 3
        Position startingPos = getSnakeHeadPos(res);
        res = dmc.tick(Direction.RIGHT);
        assertEquals(startingPos, getSnakeHeadPos(res));
    }

    @Test
    @Tag("18-2")
    @DisplayName("Test snake navigates to a treasure")
    public void seekTreasure() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_oneTreasure", "c_snakeTest");

        // dungeon layout matrix
        //     1   2   3   4   5   6   7
        // 1
        // 2   S       T       P
        // 3
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.RIGHT);
        assertEquals(new Position(2, 2), getSnakeHeadPos(res));
    }

    @Test
    @Tag("18-3")
    @DisplayName("Test snake navigates to nearest treasure if there are multiple")
    public void seekNearestTreasure() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_moreTreasure", "c_snakeTest");

        //     1   2   3   4   5   6   7
        // 2   SH      T       P       T
        // 3
        // 4
        // 5
        // 6   T                       T
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(2, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(3, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.UP);
        assertEquals(new Position(4, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(5, 2), getSnakeHeadPos(res));
    }

    @Test
    @Tag("18-4")
    @DisplayName("Test snake hibernates when player picks up treasure")
    public void moveThenHibernate() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_playerTreasure", "c_snakeTest");

        //     1   2   3   4   5   6   7
        // 1
        // 2   S           T   P
        // 3
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
    }

    @Test
    @Tag("18-5")
    @DisplayName("Test snake eats an item and grows body")
    public void eatFood() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_eatFood", "c_snakeTest");

        //     1   2   3   4   5
        // 1   P
        // 2
        // 3
        // 4           A   S
        assertEquals(new Position(4, 4), getSnakeHeadPos(res));
        res = dmc.tick(Direction.RIGHT);
        assertEquals(new Position(3, 4), getSnakeHeadPos(res));
        assertEquals(1, countType(res, "snake_body"));
        assertEquals(new Position(4, 4), getSnakeBodyPos(res));
    }

    @Test
    @Tag("18-6")
    @DisplayName("Test snake body part follows head")
    public void snakeBodyFollowsHead() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_moreTreasure", "c_snakeTest");

        //     1   2   3   4   5   6   7
        // 2   SH      T       P       T
        // 3
        // 4
        // 5
        // 6   T                       T
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(2, 2), getSnakeHeadPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(3, 2), getSnakeHeadPos(res));
        assertEquals(new Position(2, 2), getSnakeBodyPos(res));
        res = dmc.tick(Direction.UP);
        assertEquals(new Position(4, 2), getSnakeHeadPos(res));
        assertEquals(new Position(3, 2), getSnakeBodyPos(res));
    }

    @Test
    @Tag("18-7")
    @DisplayName("Test snake head attacked by player kills the snake")
    public void playerAttackSnakeHead() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_attackSnake", "c_snakeTest");

        // config data ensures player wins battle
        //     1   2   3   4   5   6   7
        // 1
        // 2   S       P       T
        // 3
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getPlayerPos(res));
        assertEquals(0, countType(res, "snake_head"));
    }

    @Test
    @Tag("18-8")
    @DisplayName("Test snake body attacked by player destroys following body parts")
    public void playerAttackSnakeBody() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_moreTreasure", "c_snakeTest");

        // initial dungeon layout matrix
        //     1   2   3   4   5   6   7
        // 2   SH      T       P       T
        // 3
        // 4
        // 5
        // 6   T                       T
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        assertEquals(new Position(5, 2), getPlayerPos(res));
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.UP);

        // layout matrix should now be:
        //     1   2   3   4   5   6   7
        // 2                           SB
        // 3                       P   SB
        // 4                           SH
        // 5
        // 6   T                       T
        // player moving right should attack snake body as head heads to treasure
        assertEquals(new Position(6, 3), getPlayerPos(res));
        assertEquals(new Position(7, 4), getSnakeHeadPos(res));
        assertEquals(2, countType(res, "snake_body"));

        List<Position> expList = new ArrayList<Position>();
        expList.add(new Position(7, 2));
        expList.add(new Position(7, 3));
        assertEquals(expList, getAllSnakeBodyPos(res));

        res = dmc.tick(Direction.RIGHT);
        assertEquals(new Position(7, 3), getPlayerPos(res));
        assertEquals(new Position(7, 5), getSnakeHeadPos(res));
        assertEquals(0, countType(res, "snake_body"));
    }

    @Test
    @Tag("18-9")
    @DisplayName("Test player defeated by snake head")
    public void playerDefeatsSnakeHead() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_attackSnake", "c_snakeTest_playerDefeat");

        // config data ensures player loses battle
        //     1   2   3   4   5   6   7
        // 1
        // 2   S       P       T
        // 3
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(0, countType(res, "player"));
    }

    @Test
    @Tag("18-10")
    @DisplayName("Test player cannot attack hibernating snake")
    public void playerAttackSnakeHibernating() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_noTreasure", "c_snakeTest");

        //     0   1   2   3   4
        // 1       P   S
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getPlayerPos(res));
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(1, 2), getSnakeHeadPos(res));
        assertEquals(new Position(1, 2), getPlayerPos(res));
    }

    @Test
    @Tag("18-11")
    @DisplayName("Test snake receives arrow buff")
    public void arrowBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_arrowBuff", "c_snakeTest_buffedSnake");

        //     1   2   3   4   5
        // 1   S   A       P   T
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(4, 1), getPlayerPos(res));

        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getSnakeBodyPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));

        // snake is super buffed. Player will lose battle
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(3, 1), getSnakeHeadPos(res));
        assertEquals(0, countType(res, "player"));
    }

    @Test
    @Tag("18-12")
    @DisplayName("Test snake receives treasure buff")
    public void treasureBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_treasureBuff", "c_snakeTest_buffedSnake");

        //     1   2   3   4   5
        // 1   S   T       P   T
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(4, 1), getPlayerPos(res));

        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getSnakeBodyPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));

        // snake is super buffed. Player will lose battle
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(3, 1), getSnakeHeadPos(res));
        assertEquals(0, countType(res, "player"));
    }

    @Test
    @Tag("18-13")
    @DisplayName("Test snake receives key buff")
    public void keyBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_keyBuff", "c_snakeTest_buffedSnake");

        //     1   2   3   4   5
        // 1   S   K       P   T
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(4, 1), getPlayerPos(res));

        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getSnakeBodyPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));

        // snake is super buffed. Player will lose battle
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(3, 1), getSnakeHeadPos(res));
        assertEquals(0, countType(res, "player"));
    }

    @Test
    @Tag("18-14")
    @DisplayName("Test invisible snake can path through a wall")
    public void invisiBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_invisiSnake", "c_snakeTest");

        //     1   2   3   4   5   6
        // 1   S   IVS W       P   T
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(5, 1), getPlayerPos(res));

        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getSnakeBodyPos(res));
        assertEquals(new Position(4, 1), getPlayerPos(res));

        // snake is invisible, head should go through the wall
        res = dmc.tick(Direction.RIGHT);
        assertEquals(new Position(3, 1), getSnakeHeadPos(res));
        assertEquals(new Position(2, 1), getSnakeBodyPos(res));
        assertEquals(new Position(5, 1), getPlayerPos(res));

        // snake is invisible, body should go through wall
        res = dmc.tick(Direction.DOWN);
        assertEquals(new Position(4, 1), getSnakeHeadPos(res));
        assertEquals(new Position(3, 1), getSnakeBodyPos(res));
        assertEquals(new Position(5, 2), getPlayerPos(res));
    }

    @Test
    @Tag("18-15")
    @DisplayName("Test player cannot attack invisible snake")
    public void playerAttackSnakeInvisible() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_invisiBuff", "c_snakeTest");

        //     1   2   3   4   5
        // 1   S   IVS     P   T
        assertEquals(new Position(1, 1), getSnakeHeadPos(res));
        assertEquals(new Position(4, 1), getPlayerPos(res));

        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(2, 1), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getSnakeBodyPos(res));
        assertEquals(new Position(3, 1), getPlayerPos(res));

        // snake is invisible. Player cannot attack snake
        res = dmc.tick(Direction.LEFT);
        assertEquals(new Position(3, 1), getSnakeHeadPos(res));
        assertEquals(new Position(2, 1), getPlayerPos(res));
    }

    @Test
    @Tag("18-16")
    @DisplayName("Test snake forms new snake after invincibility potion buff when player attacks body")
    public void playerAttackSnakeBodyAfterInvisiBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_invinciBuff", "c_snakeTest");

        //     0   1   2   3   4   5   6   7
        // 0
        // 1       P           T
        // 2
        // 3
        // 4   T           IVC IVC IVC SH
        assertEquals(new Position(6, 4), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getPlayerPos(res));

        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);

        // layout matrix should now be:
        //     0   1   2   3   4   5   6   7
        // 0
        // 1                   T
        // 2
        // 3               P
        // 4   T       SH  SB  SB  SB
        // player moving down should attack snake body
        // old snake should path find to treasure at (0, 4)
        // new snake should path find to treasure at (4, 1)
        assertEquals(new Position(3, 3), getPlayerPos(res));
        assertEquals(new Position(2, 4), getSnakeHeadPos(res));

        List<Position> expList1 = new ArrayList<Position>();
        expList1.add(new Position(5, 4));
        expList1.add(new Position(4, 4));
        expList1.add(new Position(3, 4));
        assertEquals(expList1, getAllSnakeBodyPos(res));

        res = dmc.tick(Direction.DOWN);

        // layout matrix should now be:
        //     0   1   2   3   4   5   6   7
        // 0
        // 1                   T
        // 2
        // 3                   SH
        // 4   T   SH      P   SB
        // there are now two snakeHeads
        assertEquals(new Position(3, 4), getPlayerPos(res));
        assertEquals(2, countType(res, "snake_head"));
        assertEquals(1, countType(res, "snake_body"));

        List<Position> expList2 = new ArrayList<Position>();
        expList2.add(new Position(4, 3));
        expList2.add(new Position(1, 4));
        assertEquals(expList2, getAllSnakeHeadPos(res));

        assertEquals(new Position(4, 4), getSnakeBodyPos(res));
    }

    @Test
    @Tag("18-17")
    @DisplayName("Test player attacks last body part after invisibility buff")
    public void playerAttackLastSnakeBodyAfterInvisiBuff() {
        DungeonManiaController dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_snakeTest_invinciBuff", "c_snakeTest");

        //     0   1   2   3   4   5   6   7
        // 0
        // 1       P           T
        // 2
        // 3
        // 4   T           IVC IVC IVC SH
        assertEquals(new Position(6, 4), getSnakeHeadPos(res));
        assertEquals(new Position(1, 1), getPlayerPos(res));

        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);

        // layout matrix should now be:
        //     0   1   2   3   4   5   6   7
        // 0
        // 1                   T
        // 2
        // 3                   P
        // 4   T   SH  SB  SB  SB
        // player moving down should attack snake body, no new snake created
        assertEquals(new Position(4, 3), getPlayerPos(res));
        assertEquals(new Position(1, 4), getSnakeHeadPos(res));

        List<Position> expList1 = new ArrayList<Position>();
        expList1.add(new Position(4, 4));
        expList1.add(new Position(3, 4));
        expList1.add(new Position(2, 4));
        assertEquals(expList1, getAllSnakeBodyPos(res));

        res = dmc.tick(Direction.DOWN);

        assertEquals(new Position(4, 4), getPlayerPos(res));
        assertEquals(new Position(0, 4), getSnakeHeadPos(res));

        // body part should still extend since snake eats a treasure
        assertEquals(3, countType(res, "snake_body"));

        List<Position> expList2 = new ArrayList<Position>();
        expList2.add(new Position(3, 4));
        expList2.add(new Position(2, 4));
        expList2.add(new Position(1, 4));
        assertEquals(expList2, getAllSnakeBodyPos(res));
    }

    private Position getPlayerPos(DungeonResponse res) {
        return TestUtils.getEntities(res, "player").get(0).getPosition();
    }

    private List<Position> getAllSnakeHeadPos(DungeonResponse res) {
        return TestUtils.getEntities(res, "snake_head").stream().map(e -> e.getPosition()).collect(Collectors.toList());
    }

    private Position getSnakeHeadPos(DungeonResponse res) {
        return TestUtils.getEntities(res, "snake_head").get(0).getPosition();
    }

    private List<Position> getAllSnakeBodyPos(DungeonResponse res) {
        return TestUtils.getEntities(res, "snake_body").stream().map(e -> e.getPosition()).collect(Collectors.toList());
    }

    private Position getSnakeBodyPos(DungeonResponse res) {
        return TestUtils.getEntities(res, "snake_body").get(0).getPosition();
    }

    private long countType(DungeonResponse res, String string) {
        return TestUtils.countType(res, string);
    }
}
