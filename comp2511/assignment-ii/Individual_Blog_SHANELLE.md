## Week 6

- I met up with Aidan to split up the work we had to do.


## Week 7
**Worked on delegated parts of task 1**
- Found the observer pattern in the code in Switch.java and Bomb.java, and wrote out task 1 (b) into the pair blog.
- Initially for task 1 (c), I thought the code smell was dead code from too many void returns in methods. Aidan helped me to find a better one (refused bequest), which made fixing the code smell easier by using delegation and interfaces. MR: https://nw-syd-gitlab.cseunsw.tech/COMP2511/23T3/teams/T09A_CATERPIE/assignment-ii/-/merge_requests/2
- I found the code smell for (d) quite easily (shotgun surgery), however misinterpreted the question for ages thingking we had to redesign the way collectables were handled. Eventually I realised from the forum that we were to just get rid of the code smell, which was relatively straightforward by adding a class between entity and inventory item. MR: https://nw-syd-gitlab.cseunsw.tech/COMP2511/23T3/teams/T09A_CATERPIE/assignment-ii/-/merge_requests/3
- I implemented the factory method to redesign the goals package, mostly because there was already a factory there and it violated the open-closed principle, so I couldn't say that the design should be left as is. MR: https://nw-syd-gitlab.cseunsw.tech/COMP2511/23T3/teams/T09A_CATERPIE/assignment-ii/-/merge_requests/4

## Week 8
**Started task 2**
- Learnt how the config and dungeon files work, and wrote tests for task 2(a).
- I did task 2 (a), and it wasn't too bad since I was adding a goal and I had just refactored the goals package. Somf of the implementation was similar to the way treasure is implemented. MR: https://nw-syd-gitlab.cseunsw.tech/COMP2511/23T3/teams/T09A_CATERPIE/assignment-ii/-/merge_requests/7

## Week 9
**Logical entities**
- Wrote tests, config files and dungeon files for task 2 (f) - logical entities. I foudn out that the dungeon builder exists and it saved me more time.
- My initial design was checking each tick to see what entities were connected, and then to change their active state. However, I got stuck because the entities would check if they were connected out of order, and I wasn't quite sure what to change, so I restarted TT
- I decided to implement the Observer Pattern, where each logical entity and conductor is watching its neighbours for a change in state. Challenges included:
    - double inheritance - upon implementing the logical bomb, I was stuck because all my logical entities extended an abstract class, but the logical bomb had to extend an inventory item to keep its picking up functionality. So, I had to change the 'logical entities' abstract class into three interfaces, for conductors (), subscribers and logical entities.
    - having to replace lightbulbs when they turn on/off
    - not a challenge but realised I had to change the name of logic bomb
    - co_and functionality - was a pain but I tracked whether switches were activated by holding the tick number.
- MR: https://nw-syd-gitlab.cseunsw.tech/COMP2511/23T3/teams/T09A_CATERPIE/assignment-ii/-/merge_requests/13 
