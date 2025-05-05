--- Compilation & Usage ---

Enter the SRC directory
cd SRC/

Compile the application
javac SkiResort.java

Run the application
java SkiResort <oracle username> <oracle password>

Follow the menus to execute desired functionality by entering the number of the menu/function you with to execute. Follow the prompts to input any necessary data.
The four required queries are on the Main Menu as "8. Queries".

--- Menu Hierarchy ---
1. Members
    1. Add
    2. Update
    3. Delete
2. Lift Entry Scan
3. Purchase Lessons
    1. Purchase Lesson
    2. Update Sessions
    3. Delete (archive)
4. Gear Rental
    1. Make New Equipment Rental With Ski Pass
    2. Return Rented Equipment
    3. Delete (archive)
    4. Update Rental Time (Admin only)
6. New Gear
    1. Input a new piece of equipment
    2. Update Equipment Type (admin only)
    3. Update Equipment Name (admin only)
    4. Update Equipment Size (admin only)
    5. Delete (archive)
7. Properties
8. Queries
    1. Get Lessons by Member ID
    2. Get Ski Pass Rides and Rentals
    3. Get Open Intermediate Trials
    4. Unknown


--- Workload Distribution ---
Tyler Garfield:
Mandy Jiang:
Alex Scherer:
Jeffrey Layton:
    - Add, update, or delete a lesson purchase record
    - Query #1: For a given member, list all the ski lessons they have purchased, including the number of remaining
sessions, instructor name, and scheduled time.
    - Query #3: List all open trails suitable for intermediate-level skiers, along with their category and connected lifts
that are currently operational.
    - ReadMe.txt
