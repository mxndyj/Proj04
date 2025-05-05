
import java.util.Scanner;

public class SkiResort {
    private final Scanner in =new Scanner(System.in);
    private DBController db;
    private final String sysAdPass="1234";

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
            System.out.print(
                "\t-- Ski Resort System --\n"+
                "\t1. Members\n"+
                "\t2. Ski Passes\n"+
                "\t3. Lift Entry Scan\n"+
                "\t4. Purchase Lessons\n"+
                "\t5. Gear Rental\n"+
	        "\t6. New Gear\n"+
                "\t7. Properties\n"+
                "\t8. Queries\n"+
                "\t0. Quit\n"+
                "\tEnter Option : ");
            int choice =readInt();
            switch (choice) {
                case 1 -> memberMenu();
                case 2 -> passMenu();
                case 3 -> liftEntry();
                case 4 -> purchaseLessonMenu();
                case 5 -> rentalMenu();
		case 6 -> equipmentMenu();
                case 7 -> propertyMenu();
                case 8 -> queriesMenu();
                case 0 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid choice.\n");
            }
        }
    }

    private void memberMenu() {
        System.out.print(
            "\t\tMember:\n"+
            "\t\t1. Add:\n"+
            "\t\t2. Update\n"+
            "\t\t3. Delete\n"+
            "\t\t0. Back\n"+
            "\t\tChoose> ");
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
        System.out.print(
            "\t\tSki Pass\n"+
            "\t\t1. Add\n"+
            "\t\t2. Adjust uses\n"+
            "\t\t3. Delete (archive)\n"+
            "\t\t0. Back\n"+
            "\t\tEnter Option > ");
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
            "\t\tLessons:\n"+
            "\t\t1. Add Lesson Purchase\n"+
            "\t\t2. Adjust Lesson Purchase\n"+
            "\t\t3. Delete (archive)\n"+
            "\t\t0. Back\n"+
            "\t\tEnter Option > ");
        int choice = readInt();
        switch (choice) {
            case 1 -> addLessonPurchase();
            case 2 -> adjustLessonPurchase();
            case 3 -> deleteLessonPurchase();
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

    private void getLessons() {
        try {
            db.getLessons();     
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n"); 
        }
    }

    private void getEmployees() {
        try {
            db.getEmployees();     
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

    /*-------------------------------------------------------------------
    | Method: void rentalMenu()
    |
    | Purpose: Function serves as rental sub menu deciding what method to
    |          call next depending upon what number the user selects.
    |
    | Pre-condition: in is not null and points to valid class intance.
    |
    | Post-condition: The aproprite method has been called in response to the
    |		      user's choice.
    |
    | Parameters: None. Gets input for decision from user.
    |
    | Returns: None. Calls apropriete function.
    *-------------------------------------------------------------------*/ 
    private void rentalMenu() {
        String rentalEquipMen =  "\t\tEquipment Rentals:\n"+
            "\t\t1. Make New Equipment Rental With Ski Pass\n"+
            "\t\t2. Return Rented Equipment\n"+
            "\t\t3. Delete (archive)\n"+
            "\t\t4. Update Rental Time (Admin only)\n"+
            "\t\t0. Back\n"+
            "\t\tEnter Option >";
        System.out.print(rentalEquipMen);
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

    /*-------------------------------------------------------------------
    | Method: void equipmentMenu()
    |
    | Purpose: This function is the equipment submenu and takes in an int
    |	       from the user and then calls the apropriet function based
    |          upon what the user input.
    |
    | Pre-condition: in is not null and points to valid class intance.
    |
    | Post-condition: The apropriet function has been called in response to
    |		      the users input.
    |
    | Parameters: None. Gets all input from user.
    |
    | Returns: None. Calls desired function.
    *-------------------------------------------------------------------*/ 
    private void equipmentMenu() {
        String equipMen =  "\t\tEquipment Rentals:\n"+
            "\t\t1. Input a new piece of equipment\n"+
            "\t\t2. Update Equipment Type (admin only)\n"+
            "\t\t3. Update Equipment Name (admin only)\n"+
            "\t\t4. Update Equipment Size (admin only)\n"+
            "\t\t5. Update Equipment Type & Size (admin only)\n"+
            "\t\t6. Delete (archive)\n"+
            "\t\t0. Back\n"+
            "\t\tEnter Option >";
        System.out.print(equipMen);
        int choice = readInt();
        switch (choice) {
            case 1 -> addEquipmentRecord();
            case 2 -> updateEquipmentType();
            case 3 -> updateEquipmentName();
            case 4 -> updateEquipmentSize();
            case 5 -> updateEquipTypeSz();
	    case 6 -> deleteEquipmentRecord();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void addEquipmentRental()
    |
    | Purpose: Function first gets user input on what ski pass and equipment
    |	       they would like this new rental record to be asociated with
    |	       and then calls on the DBController class instance to actually
    |	       add the new rental record to the db.
    |
    | Pre-condition: db and instance variables are not null and points to a valid
    |		     class instance.
    |
    | Post-condition: The new rental record has been added and the new id has
    |		      been printed to the user or a helpful error message has
    |                 been printed.
    |
    | Parameters: None. Gets all input from user.
    |
    | Returns: None. Prints out results of attempted record creation.
    *-------------------------------------------------------------------*/ 
    private void addEquipmentRental() {
        System.out.print("First, system needs a valid ski pass id: ");int skiPassId = readInt();
        System.out.print("Next, system needs a valid equipment id: ");int equipmentId = readInt();

        // Now try to run the addRentalRecord function and catch the things that it could potentially throw.
        try {
            int rentalID = db.addRentalRecord(skiPassId,equipmentId);
            if(rentalID>=0) {
                System.out.println("Successfully created a new rental record with rental id "+rentalID+"\n");
            } else { System.out.println("Update of equipment rental failed, check that the given id's were valid\n");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        } 
    }

    /*-------------------------------------------------------------------
    | Method: void returnEquipment()
    |
    | Purpose: This function gets input from the user on what rental record
    |	       they desire to be updated with a returned equipment status and
    |	       then prints the resulting archive id to the user or a helpful
    |          error message.
    |
    | Pre-condition: db and in are not null and points to a valid class instance.
    |
    | Post-condition: The rental record has been updated and the resulting
    |		      log id has been printed to the user or a helpful error
    |                 message has been printed.
    |
    | Parameters: None. Gets all input paramaters from the user via stdin.
    |
    | Returns: None. Prints results of record update to the user.
    *-------------------------------------------------------------------*/ 
    private void returnEquipment() {
        System.out.print("Enter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to update the equipment status to returned.
        try {
            int rentalArchiveID = db.returnEquipment(rentalID);
            if(rentalArchiveID>=0) {System.out.println("Rental equipmemt was successfully returned! Log update id is "+rentalArchiveID+".");}
	    else {System.out.println("Error: Rental record failed to be updated! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    /*-------------------------------------------------------------------
    | Method: void deleteRentalRecord()
    |
    | Purpose: This function gets input from the user via stdin on what
    |          renetal record they wish to be deleted and prints the results
    |          of the attempted deletion to stdout.
    |
    | Pre-condition: db and in are not null and points to a valid class instance.
    |
    | Post-condition: The rental record has been updated and the new log
    |		      id has been printed to the user or a helpful error
    |                 message has been printed out.
    |
    | Parameters: None. Gets all input from user via stdin.
    |
    | Returns: None. Prints results of the record deletion to stdout.
    *-------------------------------------------------------------------*/ 
    private void deleteRentalRecord() {
        System.out.print("Enter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to delete the rental record.
        try {
            int rentalArchiveID = db.deleteRentalRecord(rentalID);
            if(rentalArchiveID>=0) {System.out.println("\t\t Rental record was successfully deleted! Log update id is "+rentalArchiveID+".");}
            else {System.out.println("Error: Rental record failed to be deleted! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void updateRentalTime()
    |
    | Purpose: Function takes from the user the set admin password and
    |          the id of the rental record to be updated and then calls
    |          db to attempt to update rental time to now.
    |		*** ADMIN PW IS SET AT VERY TOP OF THIS CLASS ***
    |
    | Pre-condition: db and in are not null and points to a valid class instance.
    |
    | Post-condition: The results of the attempted update have been printed
    |                 to the user via stdout. If update succeded user has
    |                 associated log id.
    |
    | Parameters: None. Gets all input from user via stdin.
    |
    | Returns: None. Prints update resutls to user via stdout.
    *-------------------------------------------------------------------*/ 
    private void updateRentalTime() {
        System.out.print("Enter the admin password to modify rental time: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct time update denied!");
            return;
        }
        System.out.print("Enter a valid rentalID: ");int rentalID = readInt();

        // Next actually try to delete the rental record.
        try {
            int rentalArchiveID = db.updateRentalTime(rentalID);
            if(rentalArchiveID>=0) {System.out.println("\t\t Rental time was succesfully updated! Log update id is "+rentalArchiveID+".");}
            else {System.out.println("Error: Rental record failed to be updates! Check given rentalID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void addEquipmentRecord()
    |
    | Purpose: This function attempts to add a new equipment record to the
    |          to the equipment relation. Function gets user input for each
    |          record attribute and partially validates the given input. The
    |          remaining validation occures within the DBController class.
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The results of the update have been relayed to the user
    |                 via stdout and if the update has succeeded the id of the
    |                 new record has been printed to the user.
    |
    | Parameters: None. Gets all input from the user via stdin.
    |
    | Returns: None. Prints results of update to stdout.
    *-------------------------------------------------------------------*/ 
    private void addEquipmentRecord() {
        // First get the equipment type.
        System.out.print("Equipment type: ");String type = in.nextLine();

        // Next get the size of the equipment.
        double equipSize = 0.0;
        boolean gotEquipSize = false;

        while(!gotEquipSize) {
              System.out.print("Equipment size: ");
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
        System.out.print("Equipment name: ");String name = in.nextLine();

        // Next actually run the add equipment record.
        try {
            int equipmentID = db.addEquipmentRecord(type,equipSize,name);
            if(equipmentID>=0) {System.out.println("Successfully added a new equipment record with equipmentID "+equipmentID+"!");}
            else {System.out.println("The new equipment record was unable to be added check the equipment type is valid");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    /*-------------------------------------------------------------------
    | Method: void updateEquipmentType()
    |
    | Purpose: This function attempts to update the desired equipment record's
    |          type to the desired new type. If the update is successfull then
    |	       the id of the newly generated archive entry is printed. Otherwise
    |          a helpful error message is printed.
    |           *** ADMIN PW AT THE VERY TOP OF THIS CLASS ***
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The given equipment entry has been updated and the log
    |		      entry id relayed to the user or a helpful error message
    |                 has been printed.
    |
    | Parameters: None. Gets all input from the user via stdin.
    |
    | Returns: None. Prints results of update to user via stdout.
    *-------------------------------------------------------------------*/ 
    private void updateEquipmentType() {
        // First get the equipment id from the user of the record they wish to modify. Also verify
        // their admin status.
        System.out.print("Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        //System.out.println(givenPw);
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct type update denied!");
            return;
        }
        
        System.out.print("Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        System.out.print("Enter the type of equipment that change record to: ");String equipType = in.nextLine();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentType(equipmentID,equipType);
            if(equipmentArchiveID>=0) {System.out.println("\t\t Equipment type was succesfully updated! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("Error: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
            System.out.println("\t**Error info: if error is a key constraint then check you entered a valid equip type**\n");
        }
    }

    /*-------------------------------------------------------------------
    | Method: void updateEquipmentName()
    |
    | Purpose: This function attempts to update the given equipment record's
    |	       name to the given desired name. The only preq for a succesfull
    |          update here is a valid equipment id as there are no stipulations
    |	       on a piec of equipment's name.
    |           *** ADMIN PW AT THE VERY TOP OF THIS CLASS ***
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The desired equipment record's name has been updated
    |                 and the resulting log id has been relayed to the user
    |                 or a helpful error message has been printed.
    |
    | Parameters: None. Gets all input from user via stdin.
    |
    | Returns: None. Prints results of update to user via stdout.
    *-------------------------------------------------------------------*/ 
    private void updateEquipmentName() {
        // First get the equipment id from the user of the record they wish to modify. Also verify
        // their admin status.
        System.out.print("Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct name update denied!");
            return;
        }
        
        System.out.print("Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        System.out.print("Enter the new name of the equipmen record to change: ");String equipName = in.nextLine();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentName(equipmentID,equipName);
            if(equipmentArchiveID>=0) {System.out.println("Equipment name was succesfully updated! Log update id is "+equipmentArchiveID+".");}            
            else {System.out.println("Error: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void updateEquipmentSize()
    |
    | Purpose: This function attempts to update the given equipment record to
    |          the desired new size. This update will only succeeed if the new
    |          size is compatible with the valid record's current type.
    |           *** ADMIN PW AT THE VERY TOP OF THIS CLASS ***
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The desired equipment record's size has been updated
    |                 and the associated log id is relayed to the user or
    |                 an informative error message has been printed.
    |
    | Parameters: None. Gets all input from user via stdin.
    |
    | Returns: None. Prints results of the update to the user via stdout.
    *-------------------------------------------------------------------*/ 
    private void updateEquipmentSize() {
        // First get the admin password and the id of the equipment record to be updated and the new desired size.
        System.out.print("Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct name update denied!");
            return;
        }
        
        System.out.print("Enter the equipment id of the record you wish to change: ");int equipmentID = readInt();

        double equipSize = 0.0;
        boolean gotEquipSize = false;

         while(!gotEquipSize) {
              System.out.print("Equipment size: ");
              if(in.hasNextDouble()) {
                  equipSize = in.nextDouble();
                  gotEquipSize=true;
              } else { in.nextLine();}
        }
        in.nextLine();

          // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.updateEquipmentSize(equipmentID,equipSize);
            if(equipmentArchiveID>=0) {System.out.println("Equipment size was succesfully updated! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("Error: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }


    }

    /*-------------------------------------------------------------------
    | Method: void deleteEquipmentRecord()
    |
    | Purpose: This function attempts to delete the given equipmet record. If
    |          the deletion is successful the associated log id is printed
    |          otherwise a helpful error message has been printed.
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The equipment record has been updated and the log id
    |                 relayed or a helpful error message has been printed.
    |
    | Parameters: None. Get input from stdin.
    |
    | Returns: None. Prints all output to stdout.
    *-------------------------------------------------------------------*/ 
    private void deleteEquipmentRecord() {
        System.out.print("Enter the equipment id of the record you wish to delete: ");int equipmentID = readInt();

        // Now actually call the method that updates the equipment type.
        try {
            int equipmentArchiveID = db.deleteEquipmentRecord(equipmentID);
            if(equipmentArchiveID>=0) {System.out.println("Equuipment entry! Log update id is "+equipmentArchiveID+".");}
            else {System.out.println("Error: Equipment record failed to be updated! Check given equipmentID!");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void updateEquipTypeSz()
    |
    | Purpose: This function attempts to jointly update the given equipmet
    |          record's type and size jointly. This adresses cases where
    |          the record can only remain valid if both attributes are changed.
    |          Function relayes log id if updated or helpful error message.
    |           *** ADMIN PW AT THE VERY TOP OF THIS CLASS ***
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The desired equipment record has been updated and the
    |		      update log id relayed or a helpful error message printed.
    |
    | Parameters: None. Gets input from stdin.
    |
    | Returns: None. Outputs results to stdout.
    *-------------------------------------------------------------------*/ 
    private void updateEquipTypeSz() {
        // First get the admin password and the id of the equipment record to be updated and the new desired size.
        System.out.print("Enter the admin password to modify equipment attributes: ");String givenPw = in.nextLine();
        if(!givenPw.equals(sysAdPass)) {
            System.out.println("Given admin password was not correct name update denied!");
            return;
        }

    
        // Next get the equipment id of the record that should be updated.
        System.out.print("Enter a valid equipment id: ");int equipmentID = readInt();

        //Next get the new desired equipment type from the user.
        System.out.print("Enter the new equipment type: ");String type = in.nextLine();

         // Next get the size of the equipment.
        double equipSize = 0.0;
        boolean gotEquipSize = false;

        while(!gotEquipSize) {
              System.out.print("Equipment size: ");
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

        // Now that we got the new equipment type and size the next thing to do is to actually pass that to the dbcontroller.
        try {
            int equipmentArchiveID = db.updateEquipTypeSz(equipmentID,type,equipSize);
            if(equipmentArchiveID>=0) {System.out.println("Equipment record was succesfully updated. Log entry id is " +equipmentArchiveID+"\n");}
            else { System.out.println("Equipment record failed to be updated, verify equipment id is valid.");}
        } catch(Exception e) {
            System.out.println("Error: " +  e.getMessage() + "\n");
            System.out.println("\t**Error info: if error is a key constraint then check you entered a valid equip type**\n");
        }

    }

    /*-------------------------------------------------------------------
    | Method: void getEquipmentTable()
    |
    | Purpose: Display the current contents of the equipment relation.
    |
    | Pre-condition: db has been initiated to valid class instances.
    |
    | Post-condition: The contents of the equipment relation have been
    |		      neatly printed to stdout.
    |
    | Parameters: None.
    |
    | Returns: None. Prints to stdout.
    *-------------------------------------------------------------------*/ 
    private void getEquipmentTable(){
        // Just call the db controllers method to print out the equipment table.
        try {
            db.printOutEquipment();
        } catch(Exception e) {
            System.out.println("Error: " +  e.getMessage() + "\n");
        }
    }

    /*-------------------------------------------------------------------
    | Method: void getRentalTable()
    |
    | Purpose: Print current contents of rental relation to stdout.
    |
    | Pre-condition: db has been initiated to valid class instances.
    |
    | Post-condition: Contents of rental relation have been nearlt printed
    |                 to stdout.
    |
    | Parameters: None.
    |
    | Returns: None. Prints to stdout.
    *-------------------------------------------------------------------*/ 
    private void getRentalTable(){
        // Just call the db controllers method to print out the rental table.
        try {
            db.printOutRentals();
        } catch(Exception e) {
            System.out.println("Error: " +  e.getMessage() + "\n");
        }
    }

    /*-------------------------------------------------------------------
    | Method: void queryTwo()
    |
    | Purpose: Function gets input needed for query two to get entries and
    |          rentals associated with a given skipass and then relays that
    |          info to DBController's runQuerTwo to actually compute and display
    |          the query results.
    |
    | Pre-condition: db and in have been initiated to valid class instances.
    |
    | Post-condition: The results of query two have been printed to stdout.
    |
    | Parameters: None. Gets desired ski pass id from stdin.
    |
    | Returns: None. Prints results in a neat tabular format to stdout.
    *-------------------------------------------------------------------*/ 
    private void queryTwo() {
        System.out.print("Enter a valid ski pass id: ");int skiPassID =readInt();
        try{
            db.runQueryTwo(skiPassID);
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }


    private void queriesMenu() {
        System.out.print(
            "\t\tQueries:\n"+
            "\t\t1. Get Lessons by Member ID\n"+
            "\t\t2. Get Ski Pass Rides and Rentals\n"+
            "\t\t3. Get Intermediate Trails\n"+
            "\t\t4. Get Yearly Profit\n"+
            "\t\t5. Display all equipment records\n"+
            "\t\t6. Display all rental records\n"+
            "\t\t7. Display all lessons\n" +
            "\t\t8. Display all employees\n" +
            "\t\t0. Back\n"+
            "\t\tEnter Option > ");
        int choice = readInt();
        switch (choice) {
            case 1 -> getLessonsForMember();
            case 2 -> queryTwo();
            case 3 -> getIntermediateTrails();
            case 4 -> getYearlyProfit();
            case 5 -> getEquipmentTable();
            case 6 -> getRentalTable();
            case 7 -> getLessons();
            case 8 -> getEmployees();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }
    }

    private void getYearlyProfit(){
        try {
            System.out.print("How many days of the year (max 365) does the ski season last: ");
            int season = readInt();
            System.out.print("Enter how many years to check profits for: ");
            int years = readInt();
            int profit = db.getYearlyProfit(season,years); 
            System.out.printf("The estimated profit after %d year(s) is $%d%n", years, profit);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void getIntermediateTrails() {
        try {
           db.getIntermediateTrails(); 
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void propertyMenu(){
        System.out.print(
            "\t\tProperties:\n"+
            "\t\t1. Add a new property\n"+
            "\t\t2. Update Property Type\n"+
            "\t\t3. Update Property Daily Income\n"+
            "\t\t4. Delete Property\n"+
            "\t\t0. Back\n"+
            "Enter Option > ");
            
        int choice = readInt();
        switch (choice) {
            case 1 -> addProperty();
            case 2 -> updatePropertyType();
            case 3 -> updatePropertyIncome();
            case 4 -> deleteProperty();
            case 0 -> {} // back to main menu
            default -> System.out.println("Invalid choice.\n");
        }
    }
    private void addProperty(){
        System.out.print("Choose from one of the following property types: lodge, gift shop, rental center, visitor center, ski school, free lot, paid lot \n");
        System.out.print("Propety type: ");String type = in.nextLine();
        System.out.print("Daily Income: ");int income = readInt();

        try {
            int propertyID = db.addProperty(type,income);
            if(propertyID>=0) {System.out.println("Successfully added a new property with propertyID "+propertyID+"!");}
            else {System.out.println("The new property was unable to be added, check the property type is valid");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    private void updatePropertyType(){
        System.out.print("Choose from one of the following property types: lodge, gift shop, rental center, visitor center, ski school, free lot, paid lot \n");
        System.out.print("New Propety type: ");String type = in.nextLine();
        System.out.print("What propertyID is being updated?: ");int id = readInt();

        try {
            int propertyID = db.updatePropertyType(id,type);
            if(propertyID>=0) {System.out.println("Successfully changed the type of the property with propertyID "+propertyID+"!");}
            else {System.out.println("The property was unable to have the type updated");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    private void updatePropertyIncome(){
        System.out.print("What propertyID is being updated?: ");int id = readInt();
        System.out.print("New Daily Income: ");int income = readInt();

        try {
            int propertyID = db.updatePropetyIncome(id,income);
            if(propertyID>=0) {System.out.println("Successfully changed the type of the property with propertyID "+propertyID+"!");}
            else {System.out.println("The property was unable to have the type updated");}
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }
    private void deleteProperty(){
        System.out.print("What propertyID that you wish to delete?: ");int id = readInt();

        try {
            int propertyID = db.deleteProperty(id);
            if(propertyID>=0) {System.out.println("Successfully deleted the property with propertyID "+propertyID+"!");}
            else {System.out.println("The property at the chosen ID was unable to be deleted");}
        } catch(Exception e) {
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
