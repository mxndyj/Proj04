
import java.util.Scanner;

public class SkiResort {
    private final Scanner in =new Scanner(System.in);
    private DBController db;
    private String sysAdPass;

    public static void main(String[] args) {
        if (args.length !=3 ) {
            System.err.println("Usage: java SkiResort <oracle user> <oracle pass> <admin password>");
            return;
        }
        new SkiResort().run(args[0], args[1],args[2]);
    }

    private void run(String user, String pass,String givenAdPass) {
        String sysAdPass = givenAdPass;
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
                5. Gear Rental
                6. New Gear
                0. Quit
                Enter Option : """);
            int choice =readInt();
            switch (choice) {
                case 1 -> memberMenu();
                case 2 -> passMenu();
                case 3 -> liftEntry();
                case 4 -> purchaseLessonMenu();
                case 5 -> rentalMenu();
		case 6 -> equipmentMenu();
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

    private void rentalMenu() {
        System.out.print(
            """
            Equipment Rentals:
            1. Make New Equipment Rental With Ski Pass
            2. Return Rented Equipment
            3. Delete (archive)
            4. Update Rental Time (Admin only)
            0. Back
            Enter Option >\
            """);
        int choice = readInt();
        switch (choice) {
            case 1 -> addEquipmentRental();
            case 2 -> returnEquipment();
            case 3 -> deleteRentalRecord();
            case 4 -> updateRentalTime();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }

    }

    private void equipmentMenu() {
        System.out.print(
            """
            Equipment Rentals:
            1. Input a new piece of equipment
            2. Update Equipment Type (admin only)
            3. Update Equipment Name (admin only)
	    4. Update Equipment Size (admin only)
            3. Delete (archive)
            0. Back
            Enter Option >\
            """);
        int choice = readInt();
        switch (choice) {
            case 1 -> addEquipmentRecord();
            case 2 -> updateEquipmentType();
            case 3 -> updateEquipmentName();
            case 4 -> updateEquipmentSize();
	    case 5 -> deleteEquipmentRecord();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }

    }

    private void addEquipmentRental() {
        System.out.print("First, system needs a valid ski pass id: ");int skiPassId = readInt();
        System.out.print("Next, system needs a valid equipment id: ");int equipmentId = readInt();

        // Now try to run the addRentalRecord function and catch the things that it could potentially throw.
        try {
            int rentalID = db.addRentalRecord(skiPassId,equipmentId);
            if(rentalID>=0) {
                System.out.println("\t\tSuccessfully created a new rental record with rental id "+rentalID+"\n");
            } else { System.out.println("\t\tUpdate of equipment rental failed, check that the given id's were valid\n");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        } 
    }

    private void returnEquipment() {
        System.out.print("\t\tEnter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to update the equipment status to returned.
        try {
            int rentalArchiveID = db.returnEquipment(rentalID);
            if(rentalArchiveID>=0) {System.out.println("\t\t rental equipmemt was successfully returned! Log update id is "+rentalArchiveID+".");}
	    else {System.out.println("\t\tError: Rental record failed to be updated! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }
    }

    private void deleteRentalRecord() {
        System.out.print("\t\t Enter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to delete the rental record.
        try {
            int rentalArchiveID = db.returnEquipment(rentalID);
            if(rentalArchiveID>=0) {System.out.println("\t\t Rental record was successfully deleted! Log update id is "+rentalArchiveID+".");}
            else {System.out.println("\t\tError: Rental record failed to be deleted! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }

    }

    private void updateRentalTime() { //
        System.out.print("\t\t Enter the admin password to modify rental time: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct time update denied!");
            return;
        }
        System.out.print("\t\t Enter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to delete the rental record.
        try {
            int rentalArchiveID = db.updateRentalTime(rentalID);
            if(rentalArchiveID>=0) {System.out.println("\t\t Rental time was succesfully updated! Log update id is "+rentalArchiveID+".");}
            else {System.out.println("\t\tError: Rental record failed to be updates! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }

    }

    private void addEquipmentRecord() {
        // First get the equipment type.
        System.out.print("\t\tEquipment type: ");String type = in.nextLine();

        // Next get the size of the equipment.
        double equipSize = 0.0;
        boolean gotEquipSize = false;

        while(!gotEquipSize) {
              System.out.print("\t\tEquipment size: ");
              if(in.hasNextDouble()) {
                  equipSize = Double.parseDouble(in.nextLine());
		  String equipSzString = Double.toString(equipSize);
                  int decimalInd = equipSzString.indexOf(".");
                  // Check that the size is also a whole number or .5 only if it is a ski boot.
                  if(type.equals("boot")) {
                      if(equipSzString.charAt(decimalInd+1)=='0'||equipSzString.charAt(decimalInd+1)=='5'){gotEquipSize=true;}
                  } else {
                      if(equipSize == (int) equipSize) {gotEquipSize=true;}
                  }
              } else { in.nextLine();}
        }

        // Next get the name of the equipment.
        System.out.print("\t\tEquipment name: ");String name = in.nextLine();

        // Next actually run the add equipment record.
        try {
            int equipmentID = db.addEquipmentRecord(type,equipSize,name);
            if(equipmentID>=0) {System.out.println("\t\tSuccessfully added a new equipment record with equipmentID "+equipmentID+"!");}
            else {System.out.println("The new equipment record was unable to be added check the equipment type is valid");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }
    }

    private void updateEquipmentType() {
        // First get the equipment id from the user of the record they wish to modify. Also verify
        // their admin status.
        System.out.print("\t\t Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct type update denied!");
            return;
        }
        
        System.out.print("\t\t Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        System.out.print("\t\t Enter the type of equipment that change record to: ");String equipType = in.nextLine();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentType(equipmentID,equipType);
            if(equipmentArchiveID>=0) {System.out.println("\t\t Equuipment name was succesfully updated! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("\t\tError: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }
    }

    private void updateEquipmentName() {
        // First get the equipment id from the user of the record they wish to modify. Also verify
        // their admin status.
        System.out.print("\t\t Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct name update denied!");
            return;
        }
        
        System.out.print("\t\t Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        System.out.print("\t\t Enter the new name of the equipmen record to: ");String equipName = in.nextLine();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentName(equipmentID,equipName);
            if(equipmentArchiveID>=0) {System.out.println("\t\t Equipment name was succesfully updated! Log update id is "+equipmentArchiveID+".");}            
            else {System.out.println("\t\tError: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }

    }

    private void updateEquipmentSize() {
        System.out.print("\t\t Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct name update denied!");
            return;
        }
        
        System.out.print("\t\t Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        double equipSize = 0.0;
        boolean gotEquipSize = false;

        
        while(!gotEquipSize) {
              System.out.print("\t\tEquipment size: ");
              if(in.hasNextDouble()) {
                  equipSize = Double.parseDouble(in.nextLine());
              } else { in.nextLine();}
        }

          // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentSize(equipmentID,equipSize);
            if(equipmentArchiveID>=0) {System.out.println("\t\t Equipment size was succesfully updated! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("\t\tError: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }


    }

    private void deleteEquipmentRecord() {
        System.out.print("\t\t Enter the equipment id of the record you wish to delete: ");int equipmentID = readInt();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.deleteEquipmentRecord(equipmentID);
            if(equipmentArchiveID>=0) {System.out.println("\t\t Equuipment entry! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("\t\tError: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
        }

    }

    private void queryTwo() {
        System.out.print("\t\t Enter a valid ski pass id: ");int skiPassID =readInt();
        try{
            db.runQueryTwo(skiPassID);
        } catch(Exception e) {
            System.out.println("\t\tError: " + e.getMessage() + "\n");
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
