package dungeonmania.mvp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import dungeonmania.DungeonManiaController;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class LogicSwitchesTest {
    @Test
    @Tag("17-1")
    @DisplayName("Test OR lightbulb switches on and off according to condition")
    public void orLightBulb() {
        // MAP:
        // P B S
        //     W W W L
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_orLightBulb", "c_LogicSwitchesTest_orLightBulb");

        assertEquals(1, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());

        // push boulder onto switch, lightbulb turns on
        res = dmc.tick(Direction.RIGHT);
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_on").size());

        // push boulder off switch, lightbulb turns off
        res = dmc.tick(Direction.RIGHT);
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());
    }

    @Test
    @Tag("17-2")
    @DisplayName("Test AND lightbulb")
    public void andLightbulb() {
        // MAP:
        //        S  W  L
        //        B2    W
        //   S B3 P  B1 S W L
        // W W    B4
        // W L W  S
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_andLightbulb", "c_LogicSwitchesTest_andLightbulb");

        assertEquals(3, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());

        // push first boulder onto first switch, lightbulbs with one switch and two switches fail
        res = dmc.tick(Direction.RIGHT);
        assertEquals(3, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());

        // push second boulder onto second switch, lightbulb with two switches on
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.UP);
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_on").size());

        // push third boulder onto third switch, lightbulb with three switches fails
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.LEFT);
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_on").size());

        // push fourth boulder onto fourth switch, lightbulb with three switches on
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_on").size());
    }

    @Test
    @Tag("17-3")
    @DisplayName("Test OR switch door opens and closes according to condition")
    public void orSwitchDoor() {
        // MAP:
        // P B S
        //   W W
        // D W
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_orSwitchDoor", "c_LogicSwitchesTest_orSwitchDoor");

        // check door is closed
        res = dmc.tick(Direction.DOWN);
        Position pos = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertEquals(pos, TestUtils.getEntities(res, "player").get(0).getPosition());

        // push boulder onto switch
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);

        // check door is opened
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.DOWN);
        Position pos1 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertNotEquals(pos1, TestUtils.getEntities(res, "player").get(0).getPosition());

        // push boulder off switch
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.RIGHT);

        // check door is closed again
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.DOWN);
        Position pos2 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertEquals(pos2, TestUtils.getEntities(res, "player").get(0).getPosition());
    }

    @Test
    @Tag("17-4")
    @DisplayName("Test XOR switch door")
    public void xorSwitchDoor() {
        // MAP:
        //       D1 W  W
        // S  B2 P  B1 S
        // W     D2 W  W
        // W     W
        // W  W  W
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_xorSwitchDoor", "c_LogicSwitchesTest_xorSwitchDoor");
        // check both doors locked
        Position pos = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.UP);
        assertEquals(pos, TestUtils.getEntities(res, "player").get(0).getPosition());
        Position pos1 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertEquals(pos1, TestUtils.getEntities(res, "player").get(0).getPosition());

        // push first boulder, both doors unlock
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.LEFT);

        Position pos2 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.UP);
        assertNotEquals(pos2, TestUtils.getEntities(res, "player").get(0).getPosition());

        res = dmc.tick(Direction.DOWN);

        Position pos3 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertNotEquals(pos3, TestUtils.getEntities(res, "player").get(0).getPosition());

        res = dmc.tick(Direction.UP);

        // push second boulder, second door locks
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.RIGHT);
        Position pos4 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.DOWN);
        assertEquals(pos4, TestUtils.getEntities(res, "player").get(0).getPosition());
    }

    @Test
    @Tag("17-5")
    @DisplayName("Test CO_AND lightbulb")
    public void coandLightbulb() {
        // MAP:
        //         S  W  L2
        //         B2    W
        //    S B3 P  B1 S  W  L1
        // W  W
        // W  L3   B4 B5 S  W  W  L5 W
        //         S           W  W
        //      W  W  W
        //      W  L4 W
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_coandLightbulb", "c_LogicSwitchesTest_coandLightbulb");

        assertEquals(5, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());

        // push first boulder onto first switch, L1 still off (one switch)
        res = dmc.tick(Direction.RIGHT);
        assertEquals(5, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());
        res = dmc.tick(Direction.LEFT);

        // push second boulder onto second switch, L2 still off (two switches, diff tick)
        res = dmc.tick(Direction.UP);
        assertEquals(5, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());
        res = dmc.tick(Direction.DOWN);

        // push third boulder onto third switch, L3 turns on (two switches, same tick)
        res = dmc.tick(Direction.LEFT);
        assertEquals(4, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(1, TestUtils.getEntities(res, "light_bulb_on").size());
        res = dmc.tick(Direction.RIGHT);

        // push fourth boulder onto fourth switch, L4 turns on (three switches, same tick)
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.DOWN);
        assertEquals(3, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_on").size());

        // push fifth boulder onto fifth switch, L5 fails (not all switches on)
        res = dmc.tick(Direction.RIGHT);
        assertEquals(3, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_on").size());
    }

    @Test
    @Tag("17-6")
    @DisplayName("Test OR bomb")
    public void orBomb() throws InvalidActionException {
        // MAP: [] is where to place bombs
        //    P
        // WA LB B  S
        // WA [] W  W
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_orBomb", "c_LogicSwitchesTest_orBomb");

        assertEquals(1, TestUtils.getEntities(res, "bomb").size());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(2, TestUtils.getEntities(res, "wall").size());

        // pick up logic bomb
        res = dmc.tick(Direction.DOWN);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());

        // place logic bomb and check not exploded
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(2, TestUtils.getEntities(res, "wall").size());

        // activate switch and check exploded
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);
        assertEquals(0, TestUtils.getEntities(res, "wall").size());
    }

    @Test
    @Tag("17-7")
    @DisplayName("Test AND bomb")
    public void andBomb() throws InvalidActionException {
        // MAP: [] is where to place bombs
        //    P
        // B2 LB B1 S
        // S  [] W  W
        // WA WA
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_andBomb", "c_LogicSwitchesTest_andBomb");

        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(2, TestUtils.getEntities(res, "wall").size());

        // pick up logic bomb
        res = dmc.tick(Direction.DOWN);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());

        // place logic bomb and check not exploded
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(2, TestUtils.getEntities(res, "wall").size());

        // push first boulder and check not exploded
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);
        assertEquals(2, TestUtils.getEntities(res, "wall").size());

        // push second boulder and check exploded
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.DOWN);
        assertEquals(0, TestUtils.getEntities(res, "wall").size());

    }

    @Test
    @Tag("17-8")
    @DisplayName("Test XOR bomb")
    public void xorBomb() throws InvalidActionException {
        // MAP: [] is where to place bombs
        //       WA WA
        //       [] W  W
        //       L1 B1 S
        //       P
        // S  B2 L2
        // W  W  []
        //    W  W  WA
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_xorBomb", "c_LogicSwitchesTest_xorBomb");

        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(3, TestUtils.getEntities(res, "wall").size());

        // pick up logic bomb 1
        res = dmc.tick(Direction.UP);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());

        // place logic bomb 1 and check not exploded
        res = dmc.tick(Direction.UP);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(3, TestUtils.getEntities(res, "wall").size());

        // push first boulder, explode
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);
        assertEquals(1, TestUtils.getEntities(res, "wall").size());

        // pick up logic bomb 2 and place
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.DOWN);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());

        // push second boulder
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.LEFT);

        // check not exploded
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(1, TestUtils.getEntities(res, "bomb").size());
        assertEquals(1, TestUtils.getEntities(res, "wall").size());
    }

    @Test
    @Tag("17-9")
    @DisplayName("Test CO_AND bomb")
    public void coandBomb() throws InvalidActionException {
        // MAP: [] is where to place bombs
        //    WA WA
        // W  W  []
        // S  B1 L1    WA
        // W     P  L3 [] W
        // W     L2 B2 S  W
        // W  W  [] W  W
        //    WA WA WA
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_coandBomb", "c_LogicSwitchesTest_coandBomb");

        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(6, TestUtils.getEntities(res, "wall").size());

        // pick up logic bomb 1 and place down
        res = dmc.tick(Direction.UP);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());
        res = dmc.tick(Direction.UP);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(6, TestUtils.getEntities(res, "wall").size());
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.DOWN);

        // pick up logic bomb 2 and place down
        res = dmc.tick(Direction.DOWN);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(6, TestUtils.getEntities(res, "wall").size());
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.UP);

        // pick up logic bomb 3 and place down
        res = dmc.tick(Direction.RIGHT);
        assertEquals(1, TestUtils.getInventory(res, "bomb").size());
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(TestUtils.getInventory(res, "bomb").get(0).getId());
        assertEquals(0, TestUtils.getInventory(res, "bomb").size());
        assertEquals(6, TestUtils.getEntities(res, "wall").size());
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.LEFT);

        // push first boulder, L1 and L2 not exploded
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.LEFT);
        assertEquals(6, TestUtils.getEntities(res, "wall").size());
        res = dmc.tick(Direction.RIGHT);
        res = dmc.tick(Direction.DOWN);

        // push second boulder, L2 not exploded, L3 exploded
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);
        assertEquals(5, TestUtils.getEntities(res, "wall").size());
    }

    @Test
    @Tag("17-10")
    @DisplayName("Test circuit")
    public void circuit() throws InvalidActionException {
        // MAP:
        // P B S W W W
        //     W   W L
        //     D   L
        DungeonManiaController dmc;
        dmc = new DungeonManiaController();
        DungeonResponse res = dmc.newGame("d_LogicSwitchesTest_circuit", "c_LogicSwitchesTest_circuit");

        assertEquals(2, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_on").size());

        // check door is locked
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.RIGHT);
        Position pos = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.RIGHT);
        assertEquals(pos, TestUtils.getEntities(res, "player").get(0).getPosition());

        // activate switch
        res = dmc.tick(Direction.LEFT);
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.UP);
        res = dmc.tick(Direction.RIGHT);

        // check lightbulbs on and door is unlocked
        assertEquals(0, TestUtils.getEntities(res, "light_bulb_off").size());
        assertEquals(2, TestUtils.getEntities(res, "light_bulb_on").size());
        res = dmc.tick(Direction.DOWN);
        res = dmc.tick(Direction.DOWN);
        Position pos1 = TestUtils.getEntities(res, "player").get(0).getPosition();
        res = dmc.tick(Direction.RIGHT);
        assertNotEquals(pos1, TestUtils.getEntities(res, "player").get(0).getPosition());
    }
}
