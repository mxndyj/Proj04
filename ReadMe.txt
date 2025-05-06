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
    5. Update equipment Type & Size (admin only)
    6. Delete (archive)
7. Properties
    1. Add a new property
    2. Update Property Type
    3. Update Property Daily Income
    4. Delete Property
8. Queries
    1. Get Lessons by Member ID
    2. Get Ski Pass Rides and Rentals
    3. Get Open Intermediate Trials
    4. Get Yearly Profit
    5. Display all equipment records
    6. Display all rental records
    7. Display all lessons
    8. Display all employees


--- Workload Distribution ---
Tyler Garfield:
    - Rental, Equipment, Rental_Archive, Equipment_Archive, Equipment (New Gear) menu, Rental (Gear rental) menu.
        - Add, update, and delete for Equipment and Rental.
    - Query two: Given a ski pass ID, find all lift entries and equipment rentals associated with the given ski pass, and for each, print out the lift/equipment name and entrance/rental time.
    - NOTE: For equipment attribute updates, the required admin password is "1234"; the decision to lock equipment updates behind a password was to address this line in the spec: "The system should allow authorized personnel to make such updates".
    
Mandy Jiang:
    - SkiPass, Member, Lift entry operations. [DBController, SKiResort, tables.sql]
    - Design PDF corresponding FDs.
Alex Scherer:
    - Property Table, Add, Update (type and income), and Delete Property
    - Query #4: Finds the total estimated profit for a given (input) amount of years assuming the ski season is a
given (input) number of days out of 365 by totalling the daily property profits * season days and subtracting the 
total salary of employees from that.
Jeffrey Layton:
    - Add, update, or delete a lesson purchase record
    - Query #1: For a given member, list all the ski lessons they have purchased, including the number of remaining
sessions, instructor name, and scheduled time.
    - Query #3: List all open trails suitable for intermediate-level skiers, along with their category and connected lifts
that are currently operational.
    - ReadMe.txt
