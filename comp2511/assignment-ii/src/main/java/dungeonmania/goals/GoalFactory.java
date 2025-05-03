package dungeonmania.goals;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoalFactory {
    public static Goal createGoal(JSONObject jsonGoal, JSONObject config) {
        JSONArray subgoals;
        // return new Goal(jsonGoal.getString("goal"));
        switch (jsonGoal.getString("goal")) {
        case "AND":
            subgoals = jsonGoal.getJSONArray("subgoals");
            return new AndGoal("AND", createGoal(subgoals.getJSONObject(0), config),
                    createGoal(subgoals.getJSONObject(1), config));
        case "OR":
            subgoals = jsonGoal.getJSONArray("subgoals");
            return new OrGoal("OR", createGoal(subgoals.getJSONObject(0), config),
                    createGoal(subgoals.getJSONObject(1), config));
        case "exit":
            return new ExitGoal("exit");
        case "boulders":
            return new BoulderGoal("boulders");
        case "treasure":
            int treasureGoal = config.optInt("treasure_goal", 1);
            return new TreasureGoal("treasure", treasureGoal);
        case "enemies":
            int enemyGoal = config.optInt("enemy_goal", 1);
            return new EnemyGoal("enemies", enemyGoal);
        default:
            return null;
        }
    }
}
