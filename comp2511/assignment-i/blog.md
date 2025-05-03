# Assignment 1 Blogging

## Week 2

This is the first week of blogging, since the assignment was released monday of week 2.

I spent a lot of time reading through the spec before making a start on Tuesday.

I established a hierarchy of classes and put each class in a separate file as follows (indent = child class):

Blackout Controller
- Device
- Satellite

This was the overall class design of my system, until I changed a lot of this in week 4. It made creating a UML diagram easy, before I realised more changes were to be made.

I had also added a File class, but then realised the FileInfoResponse class can be used for this very purpose, and to increase coverage and prevent unused code in code smells, I deleted the File class entirely. 

The Device and Satellite classes was created with subclasses such as RelaySatellite and HandheldDevice in mind. Each satellite and device store similar information, but the device class was created separately since a file can be added to a device, but cannot be added directly to a satellite, and satellites contain more information such as linear speed and devices supported that devices need not store. The Device and Satellite classes (for now) store a unique Id, the position in radians, a height in kilometres (which is RADIUS_OF_JUPITER for all devices) and the type.

Once I had established these classes, implementing the methods in BlackoutController made a lot more sense. 

On Saturday I started creating a UML diagram which explores these relationships graphically. This helps me to know the relationships between each of the entities in the app and how one can affect the other. When I add more classes, I can just update the UML diagram as I go along, and after next week's tutorial on UML diagrams, I will certainly be updating it!

On Sunday I added extra parameters to the Device class which will help with range calculations in the next task. I plan on continuing this next week for the Satellite class, which is more complicated.


## Week 3

This week was the week that I finished and polished task 1 of the assignment, and focused a lot on refactoring and making design changes to my current implementation in order to make task 2 easier. 

I added extra parameters to the Satellite class, as I said last week, and added switch statements to add information to different devices and satellites based on their type. For example, the maxRange of each device and satellite is different. 

Tuesday night was when I started testing task 1, and got everything all passing. I can safely say, with reference to my exhaustive testing, that task 1 is complete!

After Wednesday's lecture and Tuesday's tutorial, it was clear my UML diagram was in the wrong format, so I spent time updating it.

On Friday, I made considerable progress on the entities in range method for task 2, and even going as far as ensuring entities can be in range via a relay satellite, though I have not started testing this behaviour yet. From the tests I have done so far, they weren't passing on Friday but were passing on Saturday. 

On Saturday, I pushed to the gitLab repo, and updated the UML diagram. 


## Week 4

This week, not much has happened in terms of progress with the assignment due to a range of other commitments. However, the send file method was fixed, and changes to the design were made, as well as some more refactoring.

I created separate subclasses of satellites, namely, StandardSatellite, TeleportingSatellite and RelaySatellite. The hope was to use these to implement specific movement behaviours for simulate later on. In the end, this turned out to not be so useful, and am contemplating whether to keep these or not, if I have time to refactor the entire system and create room for Liskov Substitution Principle.

On top of this, I created an Entity Parent class, because a lot of methods in Device and Satellite were common. Making Device and Satellite child classes of entity improved design and made inheritance relationships in my UML diagram a lot easier to see.

Lastly, I added extra fields to existing classes. For example, Device now has a canMove parameter which allows for the moving device extension. Satellites store bandwidth limits and max file/byte limits, as well as compatible devices. I added a temporary field hasTeleported, which is only for teleporting satellites, to determine if the satellite has teleported or not, but I plan on doing refactoring later since I'm short on time to put this into the TeleportingSatellite class. The Entity class stores the direction of movement as an int which can only be 1 or -1 (anticlockwise or clockwise).

On Sunday, there was quite a considerable change to the design of the software system. I added a File class, because there was a need, in order to implement simulate and send file, to store and manipulate files. The File class stores almost the same information as FileInfoResponse, with the exception that it stores both the current and original (full) contents of the file (in the event that a transfer gets interrupted). FileInfoResponse was only ever designed to be for displaying output rather than storage and maintenance of files, hence why I needed another class.

List of classes as of week 4 (Sunday week 4):

Blackout Controller
- Entity
    - Device
    - Satellite
        - StandardSatellite
        - TeleportingSatellite
        - RelaySatellite
- File

Additional tests were also created to test edge case behaviour with entities in range via a relay satellite, which was finally fixed by the way. The recursive function required storing a separate list that marks off visited relays. This follows a depth-first search graph algorithm, with considerations regarding satellite range. I'm glad I can apply my comp2521 knowledge here!

I decided to leave out maintenance of my UML diagram until next week, since I'm making so many changes to the system's classes and methods. 


## Week 5

This week I made considerable progress on the assignment, more than any other week! I got to finishing up the methods in task 2 such as entities in range, send file, and then making a start on (and finishing) simulate. 

The approach to the assignment I thought was working quite well. Implementing entities in range first made simulate much easier, since I didn't have to create separate helper methods for determining if two entities were in range with each other when transferring files. Implementing movement of devices was easy since I had already implemented movement for a standard satellite, and movement was exactly the same. 

On Monday, another significant change to the design was made. I created a FileTransfer class, as a helper class for Files. The FileTransfer class helps keep track of information about a file transfer happening within the system between any two entities. It stores the ids of the two entities, and the name of the file being transferred. These objects are created when send file is called, and removed when the transfer is complete or interrupted (e.g. entity out of range, teleporting satellite teleports out of range, etc.). 

After this change was made, simulate and send file were finished, and I pushed to the github repo. However, testing and refactoring are not finished. There's always more I can do to improve the design of the system. I'm not done yet!

On Wednesday, I started testing simulate and send file methods, and movement and file transfer behaviours. The tests went well apart from some mishaps and bug fixes. I used the in-built VScode debugging tool to help with running and finding errors in tests. I also ran the coverage check to analyse which parts of the code were not covered in testing. While the coverage was quite high for Blackout Controller, it was not a requirement for a successful design. The testing continued and finished on Thursday, one day before the due date. 

On Thursday, I finished testing, and fixed up style issues and removed unnecessary comments. I also updated my blog. I also created a Slope class for storing and maintaining slopes for task 3, with the intent that if I have extra time on my hands I could implement task 3 (since moving satellites had already been created). Unfortunately this was not the case on the day before the due date! Nevertheless, I pushed to the github repo, and submitted via give.

On Friday, I spent the morning refactoring the entire system to prevent violation of design principles such as Law of Demeter and Liskov Substitution Principle. 

The biggest change to the system in the refactoring process was to change Device and Satellite to an abstract class. This is because movement methods were different and dependent on the type of satellite, hence requiring an override association. 

I had previously created classes for each specific type of satellite, but had been left unused since week 4. Now with the abstract class, these subclasses were now being used, especially for create Satellite method. I created subclasses for a HandheldDevice, LaptopDevice and DesktopDevice, and even though they don't seem to do anything special, they allow for easy maintainability and expandability, which is really important for software design. They allow creation of devices, since Device is now an abstract class. 

List of classes (friday week 5):

Blackout Controller
- Entity
    - *Device*
        - HandheldDevice
        - LaptopDevice
        - DesktopDevice
    - *Satellite*
        - StandardSatellite
        - TeleportingSatellite
        - RelaySatellite
- File
- FileTransfer
- Slope

Finally, I completed and uploaded my UML diagram to the repo, and completed all my blogs.


## Final Reflections

This assignment was full of highs and lows, and was very challenging. I started right as the assignment was released to us, and yet still felt like I was stuggling to finish everything. The refactoring I did wasn't what I had hoped to achieve (e.g. I hoped to use the strategy pattern by creating interfaces for movement and file transfer behaviours) due to immense time constraints. 

At the start, my implementations were a lot more uncertain. Performing task 1 would have been easy, but the way I did it made task 2 harder, because I don't think I had the right system design in place. Initially I just had a Device and Satellite class, which made task 1 easier, but task 2 was nearly unimplementable without the need to establish an entity class for inheritance and a file class for managing files. I wished I had made these changes at the start rather than discovering them along the way.

The biggest regret I had with this assignment was not refactoring everything as I went along. I saved it, and the UML diagram, to the very end, intentionally because I knew I would keep making changes along the way to the system. But if I had refactored everything as I went along, I would have saved so much time refactoring later, and been able to identify bugs a lot more efficiently and test more of my program extensively (e.g. I wasn't able to test the behaviour of removing a device/satellite in the middle of a file transfer, and I wasn't able to test the behaviour of sending multiple files to the same teleporting satellite as it teleports away). Law of Demeter was out the window when I submitted everything on Thursday, knowing I needed to spend my Friday fixing this. 

I also had a look at how my implementation works on the frontend. It's pretty cool being able to visualise what I have achieved thus far.

If this assignment has taught me one thing, it would have to be the difference between clockwise and anticlockwise... 
