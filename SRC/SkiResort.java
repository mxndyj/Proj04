import java.util.Scanner;

public class SkiResort {
    private final Scanner in =new Scanner(System.in);
    private DBController db;

    public static void main(String[] args) {
        if (args.length !=2 ) {
            System.err.println("Usage: java SkiResort <oracle user> <oracle pass>");
            return;
        }
        new SkiResort().run(args[0], args[1]);
    }

    private void run(String user, String pass) {
        try {db =new DBController(user, pass);
            mainMenu();
        } catch (Exception e) {
            System.out.println("Error initializing DB: "+ e.getMessage());
        } finally {
            if (db !=null) db.close();
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.print("""
                -- Ski Resort System --
                1. Members
                2. Ski Passes
                3. Lift Entry Scan
                4. Purchase Lessons
                0. Quit
                Enter Option : """);
            int choice =readInt();
            switch (choice) {
                case 1 -> memberMenu();
                case 2 -> passMenu();
                case 3 -> liftEntry();
                case 4 -> purchaseLessonMenu();
                case 0 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice.\n");
            }
        }
    }

    private void memberMenu() {
        System.out.print("""
            Member:
            1. Add
            2. Update
            3. Delete
            0. Back
            Choose> """);
        int choice =readInt();
        switch (choice) {
            case 1 -> addMember();
            case 2 -> updateMember();
            case 3 -> deleteMember();
            case 0 -> {}  // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }
    }

    private void addMember() {
        System.out.print("Name: "); String name =in.nextLine();
        System.out.print("Phone: "); String phone =in.nextLine();
        System.out.print("Email: "); String email =in.nextLine();
        System.out.print("DOB (YYYY-MM-DD): "); String dob =in.nextLine();
        System.out.print("Emergency: "); String emergency =in.nextLine();
        try {
            int newid=db.addMember(name, phone, email, dob, emergency);
            System.out.println("Member added.  Member ID: "+ newid + "\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    private void updateMember() {
        System.out.print("Member ID: "); int id =readInt();
        System.out.print("New phone (enter to keep old): "); String phone =in.nextLine();
        System.out.print("New email (enter to keep old): "); String email =in.nextLine();
        System.out.print("New emergency (enter to keep old): "); String emergency =in.nextLine();
        try {
            db.updateMember(id,
                phone.isBlank() ? null : phone,
                email.isBlank() ? null : email,
                emergency.isBlank() ? null : emergency);
            System.out.println("Member updated.\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    private void deleteMember() {
        System.out.print("Member ID: "); int id =readInt();
        try {
            if (db.deleteMember(id)) System.out.println("Member deleted.\n");
            else System.out.println("Member not found.\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    private void passMenu() {
        System.out.print("""
            Ski Pass:
            1. Add
            2. Adjust uses
            3. Delete (archive)
            0. Back
            Enter Option > """);
        int choice =readInt();
        switch (choice) {
            case 1 -> addPass();
            case 2 -> adjustPass();
            case 3 -> deletePass();
            case 0 -> {}  // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }
    }

    private void addPass() {
        System.out.print("Member ID: "); int mid =readInt();
        System.out.print("Type (1-DAY,2-DAY,4-DAY,SEASON): "); String type =in.nextLine().toUpperCase();
        System.out.print("Expiration (YYYY-MM-DD): "); String exp =in.nextLine();
        try {
            int newPassID=db.addPass(mid, type, exp);
            System.out.println("Ski pass added. New Pass ID: "+ newPassID + "\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    private void adjustPass() {
        System.out.print("Pass ID: "); int pid = readInt();
        System.out.print("Remaining uses: "); int r = readInt();
        try {
            db.adjustPassUses(pid, r);
            System.out.println("Remaining uses updated.\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    private void deletePass() {
        System.out.print("Pass ID: "); int pid =readInt();
        try {
            if (db.deletePass(pid)) System.out.println("Pass deleted and archived.\n");
            else System.out.println("Cannot delete pass (active or not expired).\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }

    //lift entry scan
    private void liftEntry() {
        System.out.print("Pass ID: "); int pid =readInt();
        System.out.print("Lift name: "); String ln = in.nextLine();
        try {
            int left =db.recordLiftEntry(pid,ln);
            System.out.println("Entry recorded—"+ left + "uses left.\n");
        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage() + "\n");
        }
    }


    private void purchaseLessonMenu() {
        System.out.print(
            """
            Lessons:
            1. Add Lesson Purchase
            2. Adjust Lesson Purchase
            3. Delete (archive)
            4. Lessons for Member
            0. Back
            Enter Option >\
            """);
        int choice = readInt();
        switch (choice) {
            case 1 -> addLessonPurchase();
            case 2 -> adjustLessonPurchase();
            case 3 -> deleteLessonPurchase();
            case 4 -> getLessonsForMember();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }
    }

    private void addLessonPurchase() {
        System.out.print("Member ID: "); int mid = readInt();
        System.out.print("Lesson ID: "); int lid = readInt();
        System.out.print("Total Sessions: "); int sessions = readInt();
        System.out.print("Remaining Sessions: "); int remaining = readInt();
        try {
            int newOrderID = db.addLessonPurchase(mid, lid, sessions, remaining);
            System.out.println("Lesson Purchase Added. New Order ID: " + newOrderID + "\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private void adjustLessonPurchase() {
        System.out.print("Order ID: "); int oid = readInt();
        System.out.print("Remaining Sessions: "); int remaining = readInt();
        try {
            db.adjustLessonPurchase(oid, remaining);
            System.out.println("Remaining sessions updated.\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private void deleteLessonPurchase() {
        System.out.print("Order ID: "); int oid = readInt();
        try {
            if (db.deleteLessonPurchase(oid)) System.out.println("Lesson Purchase deleted and archived.\n");
            else System.out.println("Cannot delete lesson purchase (unused sessions remain).\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private void getLessonsForMember() {
        System.out.print("Member ID: "); int mid = readInt();
        try {
            db.getLessonsForMember(mid);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    //helper
    private int readInt() {
        while (!in.hasNextInt()) {
            System.out.print("Please enter a number: ");
            in.next();
        }
        int v =in.nextInt(); in.nextLine();
        return v;
    }
}
