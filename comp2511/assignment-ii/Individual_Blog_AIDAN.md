## Week 5

I Finished assignment 1, then had a look at the spec. That's it for this week!


## Week 6

This week was the flexi week, and instead of procrastinating like your typical student would... I met up with my partner and we started delegating tasks and setting some due dates. While these were quite informal, we at least hope to abide by them as a general goal, so we can be motivated to do well in this assignment. We can always push them back if things happen.

I was tasked to do part a and f of Task 1 refactoring. I started by looking through the Enemy package to see how I could reduce code repeatability, and then implemented a strategy pattern for movement of Enemies (i.e. the AI) which helps with reducing such repetition. The idea was more about making the Enemy package abide by open-closed principle. 


## Week 7

This week I spent a lot of time outside of doing the assignment with my partner because I had a lab test and a midterm exam for one other subject I'm doing. How unfair is that?!

I finished off the refactoring for part a I started on last week and created a merge request. I also got to review the merge requests sent to me by my partner for parts c through e. I also started on some extra refactoring as part of part f. This involved fixing the hard coding inside of buildables package and the duplicate code inside of inventory class. I fixed these using some additional helper methods, one of which uses generics.

I plan on starting task 2 next week, and coming back to some refactoring later when I have the motivation. 


## Week 8

I started working on snake. Now that my midterm is over, I can fully commit to this. I created the SnakeHead and SnakeBody as two separate enemies that store references to each other. I created the moveSnake class inside of moveStrategy to handle snake movement, which involved figuring out which item the snake should pathfind to, and then ensuring all body parts follow it. I also created a SnakeFood abstract class which determines which of the InventoryItems the snake can consume. Lastly I created the SnakeTest class for testing, and added various config and dungeon files to ensure these tests work.

For now, all the implementations of the Snake are stored in both the head and the body, and once everything is done I will refactor it to add a helper parent class for reducing repeatability.

This week encountered the first real time-consuming challenge: trying to work out how to automatically create a snake body when the snake head consumes an item (which we'll hereon refer to as food). This week, it still wasn't fixed. Hoping to get it fixed during the week 9 labs.

I also fixed a small issue with the generics method from last week's refactoring, that was causing an infinite loop. 

## Week 9

This week was a very hectic week. Computer science assignments always require things to be changed and implemented really close to the deadline: that's the nature of these assignments that I can't seem to avoid. This was no exception.

The issue with the challenge from last week was that the onOverlap method (which overrides its interface) is only called to all entities it overlaps with apart from the object that moves onto it, whether that be the Player or the snake. This meant that when a snake consumes a treasure for example, the onOverlap method in the treasure class should be the one to call the snake create body procedure, otherwise nothing happens. Apparently this was mentioned in the spec, but I did not see anything about this.

This wee saw the completion of snake. I added more tests, fixed issues, and got everything working. The snake will now be defeated in battle whether the Player moves onto a snake head or body, or whether the snake head moves onto the Player. It will also create a new snake when player attacks the body part while invincible, and will path through walls and not engage in battle while invisible. 

Once that was done, I merged in all my changes, without realising how many changes were made for this one merge request in the first place. This was one of my regrets for this assignment, since one should merge small amounts of changes rather than large ones, and one merge request per feature. I assumed that one should not merge unless all tests are passing, but that's not necessarily the case. It should be feature specific, not test results and lint specifc.

Afterwards, I switched branches and refactored snake so as to remove duplicate code between the snake body and its head. The SnakeHead class stores all the info that it's body needs, which hints at some dependency relationships (not inheritance however, since head and body are different enemies). This means no need for a snake helper class as the parent for the snake head and body. Once this was finished and merged, task 2e was finished.

I also refactored a lot of stuff for task 1f. I spent time fixing instances of duplicate code and violations of LOD and LSV (though previous refactoring meant very little instances of violations of LSV to even mention). 

Surprisingly, merge conflicts for the refactoring were a lot more than merge conflicts for snake, despite the number of changes made. Incorrect handling of merge conflicts by both myself and my partner meant that pipeline failed after merge conflicts were resolved. I took on responsibility to fix all of these, and style issues too, since my partner was working on logic (task 2f) still!


### Final Reflections

Throughout the entire project, I think my partner and I worked really well. It wasn't like one of us wasn't doing the work (which would have been raised in week 8 if that was so). We were dedicated to the project, and helping each other out, performing code reviews for each merge request, and helping each other when things got difficult. For example, my partner tried to help me with the snakes challenge I was facing without much success, and in return I helped my partner out with logic stuff, again without much success! But pair programming really helped for this assignment!

I hope to be able to use the group work skills, refactoring strategies design patterns in industry too, depending on where I work.

