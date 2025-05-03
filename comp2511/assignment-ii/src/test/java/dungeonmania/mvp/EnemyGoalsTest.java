package dungeonmania.mvp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dungeonmania.DungeonManiaController;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.util.Direction;

public class EnemyGoalsTest {
    @Test
    @Tag("16-1")
    @DisplayName("Test basic enemy goal only")
    public void basic() {
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_enemyGoalsTest_basic", "c_enemyGoalsTest_basic");
        String spawnerId = TestUtils.getEntities(res, "zombie_toast_spawner").get(0).getId();

        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // kill spider
        res = dmc.tick(Direction.RIGHT);
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // collect sword
        res = dmc.tick(Direction.RIGHT);

        // destroy spawner
        res = assertDoesNotThrow(() -> dmc.interact(spawnerId));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // all goals met
        assertEquals("", TestUtils.getGoals(res));
    }

    @Test
    @Tag("16-2")
    @DisplayName("Test enemy goal and treasure goal")
    public void enemyAndTreasure() {
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_enemyGoalsTest_enemyAndTreasure", "c_enemyGoalsTest_enemyAndTreasure");
        String spawnerId = TestUtils.getEntities(res, "zombie_toast_spawner").get(0).getId();

        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // kill spider
        res = dmc.tick(Direction.RIGHT);
        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // collect sword and destroy spawner
        res = dmc.tick(Direction.RIGHT);
        res = assertDoesNotThrow(() -> dmc.interact(spawnerId));
        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // pickup treasure
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);
        assertFalse(TestUtils.getGoals(res).contains(":treasure"));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // all goals met
        assertEquals("", TestUtils.getGoals(res));
    }

    @Test
    @Tag("16-3")
    @DisplayName("Test multiple enemy goal and multiple treasure goal")
    public void multiEnemyAndTreasure() {
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_enemyGoalsTest_multiEnemyAndTreasure",
                "c_enemyGoalsTest_multiEnemyAndTreasure");
        String spawnerId = TestUtils.getEntities(res, "zombie_toast_spawner").get(0).getId();
        String spawnerId2 = TestUtils.getEntities(res, "zombie_toast_spawner").get(1).getId();

        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // destroy two spawners
        res = dmc.tick(Direction.RIGHT);
        res = assertDoesNotThrow(() -> dmc.interact(spawnerId));
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);
        res = assertDoesNotThrow(() -> dmc.interact(spawnerId2));
        res = dmc.tick(Direction.DOWN);
        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // collect two treasure
        res = dmc.tick(Direction.RIGHT);
        assertTrue(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));
        res = dmc.tick(Direction.RIGHT);
        assertFalse(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // kill two spiders
        res = dmc.tick(Direction.RIGHT);
        assertFalse(TestUtils.getGoals(res).contains(":treasure"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));
        res = dmc.tick(Direction.RIGHT);
        assertFalse(TestUtils.getGoals(res).contains(":treasure"));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // all goals met
        assertEquals("", TestUtils.getGoals(res));
    }

    @Test
    @Tag("16-4")
    @DisplayName("Test enemy and exit goal")
    public void enemyAndExit() {
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_enemyGoalsTest_enemyAndExit", "c_enemyGoalsTest_enemyAndExit");
        String spawnerId = TestUtils.getEntities(res, "zombie_toast_spawner").get(0).getId();

        assertTrue(TestUtils.getGoals(res).contains(":exit"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // go on exit, should not pass
        res = dmc.tick(Direction.RIGHT);
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // destroy one enemy and go on exit, should not pass
        res = dmc.tick(Direction.RIGHT);
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // collect sword and destroy spawner
        res = dmc.tick(Direction.UP);
        res = assertDoesNotThrow(() -> dmc.interact(spawnerId));
        assertTrue(TestUtils.getGoals(res).contains(":exit"));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // go on exit and pass
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.LEFT);
        assertFalse(TestUtils.getGoals(res).contains(":exit"));
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // all goals met
        assertEquals("", TestUtils.getGoals(res));
    }

    @Test
    @Tag("16-5")
    @DisplayName("Test enemy OR boulder goal")
    public void enemyOrBoulder() {
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_enemyGoalsTest_enemyOrBoulder", "c_enemyGoalsTest_enemyOrBoulder");
        assertTrue(TestUtils.getGoals(res).contains(":boulders"));
        assertTrue(TestUtils.getGoals(res).contains(":enemies"));

        // kill spider
        res = dmc.tick(Direction.RIGHT);
        assertFalse(TestUtils.getGoals(res).contains(":enemies"));

        // all goals met
        assertEquals("", TestUtils.getGoals(res));
    }
}
