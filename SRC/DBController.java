/*
 * DBController.java
 * Assignment: CSC460 Project 4
 * Authors: Tyler Garfield, Mandy Jiang, Jeffrey Layton, Alex Scherer
 * Date: 05/05/2025
 *
 * Description:
 * The DBController class manages all database operations for the Ski Resort Management System.
 * It coordinates the core logic for adding, updating, deleting, and querying relational data
 * related to members, ski passes, equipment rentals, lessons, lift entries, and property income.
 *
 * Usage:
 *     java SkiResort yourUser yourOraclePass
 */


import java.sql.*;
import java.util.ArrayList;


/*----------------------------------------------------------------------
 ||
 ||  Class DBController
 ||
 ||         Author:  Tyler Garfield, Mandy Jiang, Jeffrey Layton, Alex Scherer
 ||
 ||        Purpose:  This class performs all interactions with our database
 ||                  that means being the class that updates our relations
 ||                  such as Rental, SkiPass, etc. This class also performs
 ||                  the required queries for this program, and prints out the
 ||                  results. One such query is getting all entries and rentals
 ||                  associated with a given ski pass entry. Essentially any
 ||                  Interaction with our Ski Resort database takes place here.
 ||
 ||  Inherits From:  None.
 ||
 ||     Interfaces:  None.
 ||
 |+-----------------------------------------------------------------------
 ||
 ||      Constants:  String URL - The URL that indicates the location of
 ||			our Oracle database system.
 ||                  Connection dbconn - The connection to the database we
 ||			are interacting with.
 ||
 |+-----------------------------------------------------------------------
 ||
 ||   Constructors:  DBController(String user,String pass) throws Exception -
 ||			The constructor attempts to initiate a connection to the
 ||			database with the given username and password.
 ||
 ||  Class Methods:  None.
 ||
 ||  Inst. Methods:  Member relation manipulation:
 ||			addMember(String name,String phone,String email,Stirng dob,String emergency)
 ||							throws SQLException
 ||			updateMember(int id,Stirng phone,String email,String emergency)
 ||							throws SQLException
 ||			deleteMember(int id) throws SQLException
 ||		      SkiPass relation manipulation:
 ||			addPass(int mid,String type,String exp) throws SQLException
 ||			adjustPassUses(int pid, int uses) throws SQLException
 ||			deletePass(int pid) throws SQLException
 ||		      Lift relation manipulation methods:
 ||			recordLifEntry(int pid,String liftName) throws SQLException
 ||		      LessonPurchase relation manipulation methods:
 ||			addLessonPurchase(int mid,int lid, int totalSessions, int remaining)
 ||							throws SQLException
 ||			adjustLessonPurchase(int oid,int remaining) throws SQLException
 ||			deleteLessonPurchase(int oid) throws SQLException
 ||		      Rental relation manipulation methods:
 ||			addRentalRecord(int skiPassID,int equipmentID) throws SQLException, IllegalStateException
 ||			updateRentalTime(int rentalID) throws SQLException
 ||			returnEquipment(int rentalID) throws throws SQLException, IllegalStateException
 ||			deleteRentalRecord(int rentalID) throws throws SQLException, IllegalStateException
 ||		      Equipment relation manipulation methods:
 ||			addEquipmentRecord(String type, double size, String name) throws SQLException, IllegalStateException
 ||			deleteEquipmentRecord(int equipmentID) throws SQLException
 ||			updateEquipmentType(int equipmentID,String newType) throws SQLException
 ||			updateEquipmentTypeSz(int equipmentID,String newType,double newSz) throws SQLException, IllegalStateException
 ||			updateEquipmentName(int equipmentID,String equipName) throws SQLException 
 ||			updateEquipmentSize(int equipmentID,double newSize) throws SQLException, IllegalStateException
 ||		      Property relation manipulation methods:
 ||			addProperty(String type, int income) throws SQLException, IllegalArgumentException
 ||			updatePropertyIncome(int propertyID, int newIncome) throws SQLException
 ||			updatePropertyType(int propertyID, String newType) throws SQLException
 ||			deleteProperty(int propertyID) throws SQLException
 ||		      DB querying methods:
 ||			getIntermediateTrails() throws SQLException
 ||			getLessons() throws SQLException
 ||			getEmployees() throws SQLException
 ||			getLessonsForMember(int mid) throws SQLException
 ||			getYearlyProfit(int season, int years) throws SQLException
 ||			runQueryTwo(int skiPassID) throws SQLException
 ||			printOutRentals() throws SQLException
 ||			printOutEquipment() throws SQLException
 ||
 ++-----------------------------------------------------------------------*/

public class DBController {
    private static final String URL="jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    private final Connection dbconn;

    /*
     * Constructs the DBController and connects to the Oracle database.
     *
     * @param user Oracle DB username
     * @param pass Oracle DB password
     * @throws exception if the driver fails to load or connection fails
     */
    public DBController(String user,String pass) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        dbconn=DriverManager.getConnection(URL,user, pass);
    }


    /*
     * Closes the database connection.
     */
    public void close() {
        try { dbconn.close(); } catch (SQLException ignored) {}
    }

    /*
     * gets the next unique ID from a sequence for the given table.
     *
     * @param table Table name
     * @param owner Schema owner name 
     * @return The next integer of the sequence
     * @throws SQLException if the query fails
     */
    private int getNextId(String table,String owner) throws SQLException {
        String seq=table.toUpperCase() + "_SEQ";
        String sql="select " + owner + "." + seq + ".NEXTVAL FROM DUAL";
        try (Statement s=dbconn.createStatement();
             ResultSet rs=s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }


	
    /*
     * adds a new member to the Member table with the given attributes.
     *
     * @param name Member's full name
     * @param phone Member's phone number
     * @param email Member's email address
     * @param dob Date of birth in "YYYY-MM-DD" format, or null/empty for unknown
     * @param emergency Emergency contact info
     * @return The newly generated member ID
     * @throws SQLException if the insert fails
     */

    public int addMember(String name,String phone,String email,String dob,String emergency) throws SQLException {
        int id=getNextId("Member","mandyjiang");
        String sql="insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement stmt=dbconn.prepareStatement(sql)) {
            stmt.setInt(1,id);
            stmt.setString(2,name);
            stmt.setString(3,phone);
            stmt.setString(4,email);
            Date dobDate;
                if (dob ==null ||dob.trim().isEmpty()) {
                    dobDate =null;
                } else {
                    dobDate = Date.valueOf(dob);
                }

            stmt.setDate(5, dobDate);
            stmt.setString(6,emergency);
            stmt.executeUpdate();
            
        }
        return id;
    }

	
    /*
     * updates an existing member's phone, email, and emergency contact.
     * If any field is null, that field remains unchanged.
     *
     * @param id Member ID to update
     * @param phone New phone number (nullable)
     * @param email New email address (nullable)
     * @param emergency New emergency contact (nullable)
     * @throws SQLException if update fails or member does not exist
     */
    public void updateMember(int id,String phone,String email,String emergency) throws SQLException {
        String sql="update mandyjiang.Member SET phone=COALESCE(?,phone),email=COALESCE(?,email),emergency_contact=COALESCE(?,emergency_contact) where member_id=?";
        try (PreparedStatement stmt=dbconn.prepareStatement(sql)) {
            stmt.setString(1,phone);
            stmt.setString(2,email);
            stmt.setString(3,emergency);
            stmt.setInt(4,id);
            int updated=stmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No member with ID " + id);
            }
        
        }
    }

	
    /*
     * Deletes a member from the Member table after verifying that:
     * - First, there is no active ski passes for them,
     * - Then, they have no open equipment rentals,
     * - Also, they have no unused lesson sessions.
     * Also removes archived rental history associated with the member's ski passes.
     *
     * @param id Member ID to delete
     * @return true if deletion succeeded, false otherwise
     * @throws SQLException if a query fails
     * @throws IllegalStateException if deletion rules are violated
     */
    public boolean deleteMember(int id) throws SQLException {
         // refuse deletion if remaining_uses > 0 or expiration_date > now
        String checkPass= "select * from mandyjiang.SkiPass " +
          "where member_id=? and  (remaining_uses>0 OR expiration_date > SYSTIMESTAMP)";
            try (PreparedStatement cp_stmt=dbconn.prepareStatement(checkPass)) {
                cp_stmt.setInt(1,id);
                if (cp_stmt.executeQuery().next()) {
                    throw new IllegalStateException("Member can't be deleted: active ski passes exist.");
                }
            }
         // refuse if any unreturned rental is still out
        String checkRental ="select * from tylergarfield.Rental " + "where skiPassID in (select pass_id from mandyjiang.SkiPass where member_id = ?) " +
        "and returnStatus = 1" ;
            try (PreparedStatement ch_rental = dbconn.prepareStatement(checkRental)) {
                ch_rental.setInt(1, id);
                if (ch_rental.executeQuery().next()) {
                    throw new IllegalStateException("Member can't be deleted: open equipment rentals exist.");
                }
            }
        // check for unused lessons
        String checkLesson =
        "select * from jeffreylayton.LessonPurchase " + "where member_id = ? and  remaining_sessions > 0";
            try (PreparedStatement cl_lesson = dbconn.prepareStatement(checkLesson)) {
                cl_lesson.setInt(1, id);
                if (cl_lesson.executeQuery().next()) {
                    throw new IllegalStateException("Member can't be deleted: unused lesson sessions exist.");
                }
            }
        //delete the rentals history
        String delArchRentals =
        "delete from tylergarfield.Rental_Archive where skiPassID in ( " +
        "  select pass_id FROM mandyjiang.SkiPass WHERE member_id = ?)";
            try (PreparedStatement ps = dbconn.prepareStatement(delArchRentals)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

        //  dekete  member
            try (PreparedStatement dm=dbconn.prepareStatement(
                    "DELETE from mandyjiang.Member where member_id=?"
                )) {
                dm.setInt(1,id);
                return dm.executeUpdate() == 1;
            }
    }

	
    /*
     * Adds a new ski pass for a member by looking up type values from the PassType table.
     * Sets remaining uses and price automatically based on pass type.
     *
     * @param mid Member ID the pass belongs to
     * @param type Type of the ski pass 
     * @param exp Expiration date in "YYYY-MM-DD" format
     * @return The newly generated ski pass ID
     * @throws SQLException if the insert fails or the pass type is unknown
     */

    public int addPass(int mid,String type,String exp) throws SQLException {
        int id=getNextId("SkiPass","mandyjiang");

        int defaultUses;
        double defaultPrice;
        String lookupSql="select  total_uses,price from mandyjiang.PassType where type=?";
        try (PreparedStatement lookup=dbconn.prepareStatement(lookupSql)) {
            lookup.setString(1,type);
            try (ResultSet rs=lookup.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Unknown pass type: " + type);
                }
                defaultUses =rs.getInt("total_uses");
                defaultPrice=rs.getDouble("price");
            }
        }
        System.out.printf("FYI: That %s pass costs $%.2f and grants %d uses.%n",type,defaultPrice,defaultUses);

        String insertsql="Insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES(?,?,?,?,SYSTIMESTAMP,?)";
        try (PreparedStatement stmt=dbconn.prepareStatement(insertsql)) {
            stmt.setInt(1,id);
            stmt.setInt(2,mid);
            stmt.setString(3,type);
            stmt.setInt(4,defaultUses);
            stmt.setDate(5,Date.valueOf(exp));
            stmt.executeUpdate();
        }

        return id;
    }

	
    /*
     *  updates the remaining uses on a ski pass.
     *
     * @param pid Ski pass ID
     * @param uses New remaining uses value
     * @throws SQLException if the ski pass does not exist or update fails
     */
    public void adjustPassUses(int pid,int uses) throws SQLException {
        String sql="update mandyjiang.SkiPass SET remaining_uses=? where pass_id=?";
        try (PreparedStatement stmt=dbconn.prepareStatement(sql)) {
            stmt.setInt(1,uses);
            stmt.setInt(2,pid);
            int updated=stmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No ski pass exist with ID " +pid);
            }
        
        }
    }

	
    /*
     * archive and deletes a ski pass if it has no remaining uses and is expired.
     * Also moves associated rental records to the Rental_Archive table and deletes them from active rentals.
     *
     * @param pid Ski pass ID to archive/delete
     * @return true if deletion succeeded, false if not
     * @throws SQLException if the pass is still active, rentals are unreturned, or any SQL operation fails
     * @throws IllegalStateException if pass still has uses, is not expired, or rentals are not returned
     */
    public boolean deletePass(int pid) throws SQLException {
        // check that the pass exists and is expired/used up
        try (PreparedStatement chk=dbconn.prepareStatement("select remaining_uses,expiration_date from mandyjiang.SkiPass where pass_id=?")) {
            chk.setInt(1,pid);
            try (ResultSet rs=chk.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Ski pass does not exist: " +pid);
                if (rs.getInt(1) > 0 || rs.getDate(2).after(new java.sql.Date(System.currentTimeMillis())))
                    throw new IllegalStateException(
                        "Cannot delete pass: still active or not expired."
                    );
            }
        }
        // check if any live rental is still out
        String checkRental =
            "select *   from tylergarfield.Rental " +
            " where skiPassID = ? and returnStatus = 0";
            try (PreparedStatement stmt = dbconn.prepareStatement(checkRental)) {
                stmt.setInt(1, pid);
                if (stmt.executeQuery().next()) {
                    throw new IllegalStateException(
                        "Cannot delete pass: there are unreturned equipment rentals."
                    );
            }
        }
	
        // Next we are going to delete the returned equipment rentals and put them in the rental log.
        //PreparedStatement getRIDs= null;
        try(PreparedStatement stmt = dbconn.prepareStatement("select rentalID from tylergarfield.Rental where skiPassID= ?")) {
            stmt.setInt(1,pid);
            try(ResultSet res=stmt.executeQuery()) {
	    
              while(res.next()) {
                int rentalID = res.getInt(1);

                // Now for this rental id add it to the log and delete it.
                int rentalArchiveID = getNextId("Rental_Archive","tylergarfield");

                String addRentalToArchive = "insert into tylergarfield.Rental_Archive " +
                                    "select ?,rentalID,skiPassID,equipmentID,rentalTime,returnStatus,SYSTIMESTAMP,2 " +
                                    "from tylergarfield.Rental " +
                                    "where rentalID= ?";
                try(PreparedStatement stmt1 = dbconn.prepareStatement(addRentalToArchive)) {
                    stmt1.setInt(1,rentalArchiveID);
                    stmt1.setInt(2,rentalID);
                    stmt1.executeUpdate();
                }
                // Now that that is done we can delete the rental record from the main Rental relation.
                String deleteRental = "delete from tylergarfield.Rental where rentalID=?";
                try(PreparedStatement stmt2 = dbconn.prepareStatement(deleteRental)) {
                    stmt2.setInt(1,rentalID);
                    stmt2.executeUpdate();
                }

              }
            }
           
        }
        


        // Now, Archive / delete 
        String archiveSql="""
            insert into mandyjiang.SkiPass_Archive(
            SPARCHIVE_ID,PASS_ID,MEMBER_ID,TYPE,
            REMAINING_USES,PURCHASE_TIME,EXPIRATION_DATE,ARCHIVED_TIME)
            select tylergarfield.SKIPASS_ARCHIVE_SEQ.NEXTVAL,
                pass_id,member_id,type,remaining_uses,
                purchase_time,expiration_date,SYSTIMESTAMP
            from mandyjiang.SkiPass
            where pass_id=?
            """;
                
        String deleteSql=
          "DELETE from mandyjiang.SkiPass where pass_id=?";
    
        try (PreparedStatement a=dbconn.prepareStatement(archiveSql);
             PreparedStatement d=dbconn.prepareStatement(deleteSql)) {
            a.setInt(1,pid); 
            a.executeUpdate();
            d.setInt(1,pid);
            return d.executeUpdate() == 1;
        }
        
    }
	

    /**
     * Records a lift entry for a ski pass by:
     * - First, verifying the lift exists
     * - Then, deducting one use from the pass
     * - And finally, inserts a lift entry into the Entry table
     *
     * @param pid Ski pass ID
     * @param liftName Lift name (case-insensitive)
     * @return Remaining uses after entry
     * @throws SQLException if the lift does not exist, the pass has no uses left, or the insert fails
     * @throws IllegalArgumentException if the lift name is invalid
     * @throws IllegalStateException if no uses are left on the ski pass
     */
    public int recordLiftEntry(int pid,String liftName) throws SQLException {
        liftName=liftName.toUpperCase();
        //  lift exists
        try (PreparedStatement le=dbconn.prepareStatement(
                "select * from mandyjiang.Lift where lift_name=?")) {
            le.setString(1,liftName);
            if (!le.executeQuery().next()) throw new IllegalArgumentException("Lift does not exist.");
        }
        // deduct use
        try (PreparedStatement u=dbconn.prepareStatement(
                "update mandyjiang.SkiPass SET remaining_uses=remaining_uses - 1 " +
                "where pass_id=? AND remaining_uses > 0")) {
            u.setInt(1,pid);
            if (u.executeUpdate() == 0) throw new IllegalStateException("No uses left on pass.");
        }
        // insert into entry log
        try (PreparedStatement log=dbconn.prepareStatement(
                "insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES(?,?,SYSTIMESTAMP)")) {
            log.setString(1,liftName); log.setInt(2,pid); log.executeUpdate();
        }
        // fetch remaining uses
        int left;
        try (PreparedStatement q=dbconn.prepareStatement(
                "select remaining_uses from mandyjiang.SkiPass where pass_id=?")) {
            q.setInt(1,pid);
            try (ResultSet rs=q.executeQuery()) { rs.next(); left=rs.getInt(1); }
        }
    
        return left;
    }

    /*-------------------------------------------------------------------
    | Method: getIntermediateTrails()
    |
    | Purpose: This method displays a list of all open BEGINNER and
    |          INTERMEDIATE trails from the Trail relation and all
    |          open lifts connected to each trail using the LiftTrail
    |          and Lift relations.
    |
    | Pre-condition:  The Trail, Lift, and LiftTrail relations are exist
    |                 and are accessible.
    |
    | Post-condition: The open trails and their lfits are displayed.
    |
    | Parameters: None.
    |
    | Returns: void or error - void unless an error occurs.
    *-------------------------------------------------------------------*/
    public void getIntermediateTrails() throws SQLException {
        String sql = """
        select t.trail_name, t.difficulty, t.category, l.lift_name
        from mandyjiang.Trail t
        join mandyjiang.LiftTrail lt 
          on lt.trail_name = t.trail_name
        join mandyjiang.Lift l
          on l.lift_name = lt.lift_name
        where (t.difficulty = 'INTERMEDIATE' or t.difficulty = 'BEGINNER') 
          and t.status = 'OPEN' 
          and l.status = 'OPEN'
        order by t.trail_name
        """;

        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            try (ResultSet rs = p.executeQuery()) {
                String trail = "";
                String category = "";
                String difficulty = "";
                ArrayList<String> lifts = new ArrayList<String>();

                while (rs.next()) {
                    String newTrail = rs.getString("trail_name");
                    // Only print trail data once all lifts have been given
                    if (trail.length() != 0 && trail.compareTo(newTrail) != 0) {
                        System.out.println("Trail: " + trail);
                        System.out.println("  Difficulty:\t" + difficulty);
                        System.out.println("  Category:\t" + category);
                        System.out.println("  Open Lifts:");
                        if (lifts.size() > 0) {
                            for (int i = 0; i < lifts.size(); i++) {
                                System.out.println("\t" + lifts.get(i));
                            }
                        } else {
                            System.out.println("\tNone.");
                        }

                        lifts = new ArrayList<String>();
                    } else {
                        category = rs.getString("category");
                        difficulty = rs.getString("difficulty");
                        String newLift = rs.getString("lift_name");
                        if (lifts.indexOf(newLift) == -1) {
                            lifts.add(newLift);
                        }
                    }

                    trail = newTrail;
                }            
            }
        }
    }

    /*-------------------------------------------------------------------
    | Method: getLessons()
    |
    | Purpose: This method displays a list of all lessons in the
    |          Lesson relation and the instructor information from
    |          the Employee relation.
    |
    | Pre-condition:  The Lesson and Employee relations exist and are accessible. 
    |
    | Post-condition: All Lessons are displayed.
    |
    | Parameters: None.
    |
    | Returns: void or error - void unless an error occurs.
    *-------------------------------------------------------------------*/
    public void getLessons() throws SQLException {
        String sql = """
        select l.lesson_id, l.private, l.time, e.employee_id, e.name, e.certification_level
        from jeffreylayton.Lesson l
        join jeffreylayton.Employee e on e.employee_id = l.instructor_id
        """;

        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int lesson_id = rs.getInt("lesson_id");
                    int isPrivate = rs.getInt("private");
                    String time = rs.getString("time");
                    int eid = rs.getInt("employee_id");
                    String eName = rs.getString("name");
                    int eCert = rs.getInt("certification_level");

                    String cert = "";
                    switch (eCert) {
                        case 1 -> { cert="I"; }
                        case 2 -> { cert = "II"; }
                        case 3 -> { cert = "III"; }
                        default -> {}
                    }

                    System.out.println("\tLesson: " + String.valueOf(lesson_id));
                    System.out.println("\t\tPrivate: " + (isPrivate == 1 ? "yes" : "no"));
                    System.out.println("\t\tTime: " + time);
                    System.out.println("\t\tInstructor: " + String.valueOf(eid));
                    System.out.println("\t\t\tName: " + eName);
                    System.out.println("\t\t\tCertification: " + cert);
                }
            }
        }
    }

    /*-------------------------------------------------------------------
    | Method: getEmployees()
    |
    | Purpose: This method displays a list of all employees in the
    |          Employee relation.
    |
    | Pre-condition:  The Employee relation exists and is accessible. 
    |
    | Post-condition: All Employees are displayed.
    |
    | Parameters: None.
    |
    | Returns: void or error - void unless an error occurs.
    *-------------------------------------------------------------------*/
    public void getEmployees() throws SQLException {
        String sql = """
        select employee_id, position, start_date, name, age, salary, sex, ethnicity
        from jeffreylayton.Employee
        """;

        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int eid = rs.getInt("employee_id");
                    String position = rs.getString("position");
                    Date startDate = rs.getDate("start_date");
                    String name = rs.getString("name");
                    int age = rs.getInt("age");
                    int salary = rs.getInt("salary");
                    String sex = rs.getString("sex");
                    String ethnicity = rs.getString("ethnicity");

                    System.out.println("\tEmployee: " + String.valueOf(eid));
                    System.out.println("\t\tName: " + name);
                    System.out.println("\t\tPosition: " + position);
                    System.out.println("\t\tSalary: " + String.valueOf(salary));
                    System.out.println("\t\tStart Date: " + startDate.toString());
                    System.out.println("\t\tAge: " + String.valueOf(age));
                    System.out.println("\t\tSex: " + sex);
                    System.out.println("\t\tEthnicity: " + ethnicity);
                }
            }
        }
    }

    /*-------------------------------------------------------------------
    | Method: addLessonPurchase(int mid, int lid, int totalSessions, 
    |                           int remaining)
    |
    | Purpose: This method adds a new lesson purchase order to the 
    |          LessonPurchase relation containing all records of lesson 
    |          purchases within the SkiResort. The order id of the created 
    |          record is returned.
    |
    | Pre-condition:  The LessonPurchase relation exists and is accessible. 
    |
    | Post-condition: A new record has been added to the LessonPuchase relation 
    |                 denoting a lesson has been purchased by a member.
    |
    | Parameters: int mid               - The id of the member making 
    |                                     a purchase.
    |             int lid               - The id of the lesson to be
    |                                     purchased.
    |             int totalSessions     - The number of sessions being 
    |                                     purchased.
    |             int remainingSessions - The remaining number of sessions
    |                                     that can be used.
    |
    | Returns: int or error - the id of a created record unless an error
    |                         occurs.
    *-------------------------------------------------------------------*/
    public int addLessonPurchase(int mid, int lid, int totalSessions, int remaining) throws SQLException {
        int id = getNextId("LessonPurchase", "jeffreylayton");
        String sql = "insert into jeffreylayton.LessonPurchase(order_id, member_id, lesson_id, total_sessions, remaining_sessions) values (?, ?, ?, ?, ?)"; 
        try (PreparedStatement stmt= dbconn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, mid);
            stmt.setInt(3, lid);
            stmt.setInt(4, totalSessions);
            stmt.setInt(5, remaining);
            stmt.executeUpdate();
        }

        return id;
    }

    /*-------------------------------------------------------------------
    | Method: adjustLessonPurchase(int oid, int remaining)
    |
    | Purpose: This method adjusts the remaining sessions of an existing 
    |          lesson purchase order in the LessonPurchase relation.
    |
    | Pre-condition:  The LessonPurchase relation exists and is accessible. 
    |
    | Post-condition: The remaining sessions on the lesson have 
    |                 been changed to the desired amount.
    |
    | Parameters: int oid       - The id of the order to adjust.
    |             int remaining - The remaining number of sessions
    |                             that can be used.
    |
    | Returns: void or error - void unless an error occurs.
    *-------------------------------------------------------------------*/
    public void adjustLessonPurchase(int oid, int remaining) throws SQLException {
        String sql = "update jeffreylayton.LessonPurchase set remaining_sessions=? where order_id=?";
        try (PreparedStatement stmt = dbconn.prepareStatement(sql)) {
            stmt.setInt(1, remaining);
            stmt.setInt(2, oid);
            int updated = stmt.executeUpdate();
            if (updated == 0){
                throw new SQLException("No order with ID " + oid);
            }
        }
    }

    /*-------------------------------------------------------------------
    | Method: deleteLessonPurchase(int oid)
    |
    | Purpose: This method displays all the Lessons, including the
    |          purchase information, and the instructor information.
    |
    | Pre-condition:  The LessonPurchase and Employee relations exist 
    |                 and are accessible. 
    |
    | Post-condition: The member's purchased Lessons are displayed.
    |
    | Parameters: int oid - The id of the order to adjust.
    |
    | Returns: boolean or error - a boolean if the record is deleted or 
    |                             an error if one occurs.
    *-------------------------------------------------------------------*/
    public boolean deleteLessonPurchase(int oid) throws SQLException {
        String checkSql = "select remaining_sessions from jeffreyLayton.LessonPurchase where order_id = ?";
        try (PreparedStatement c = dbconn.prepareStatement(checkSql)) {
            c.setInt(1, oid);
            try (ResultSet rs =  c.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Lesson Purchase does not exist");
                } else {
                    int sessions = rs.getInt("remaining_sessions");
                    if (sessions > 0) {
                    throw new IllegalStateException("Lesson Purchase contains remaining sessions");
                    }
                }
            }
        }


        String archiveSql = """
        insert into jeffreylayton.LessonPurchase_Archive (
            order_id, member_id, lesson_id, total_sessions, remaining_sessions
        )
        select order_id, member_id, lesson_id, total_sessions
        from jeffreylayton.LessonPurchase
        where order_id = ?
        """;

        try (PreparedStatement a = dbconn.prepareStatement(archiveSql)) {
            a.setInt(1, oid);
            a.executeUpdate();
        }

        String deleteSql = "delete from jeffreylayton.LessonPurchase where order_id=?";

        try (PreparedStatement d = dbconn.prepareStatement(deleteSql)) {
            d.setInt(1, oid);
            return d.executeUpdate() == 1;
        }

    }

    /*-------------------------------------------------------------------
    | Method: getLessonsForMember(int mid)
    |
    | Purpose: This method deletes the an existing lesson purchase order 
    |          from the LessonPurchase relation. First, the lesson
    |          purchase must exist and have a value for remaining sessions
    |          set to 0. If the condition is met, the order is removed
    |          from the LessonPurchase relation and a log is added to
    |          the LessonPurchase_Archive relation.
    |
    | Pre-condition:  The LessonPurchase relation exists and is accessible. 
    |
    | Post-condition: All Lessons that a user has purchased are displayed.
    |
    | Parameters: int mid - The id of the member
    |
    | Returns: void or error - void unless an error occurs.
    *-------------------------------------------------------------------*/
    public void getLessonsForMember(int mid) throws SQLException {
        String sql = """
        select l.lesson_id, e.name as "instructor_name", l.time, sum(lp.total_sessions) as "total_sessions", sum(lp.remaining_sessions) as "remaining_sessions"
        from jeffreylayton.LessonPurchase lp
        join jeffreylayton.Lesson l on l.lesson_id = lp.lesson_id
        join jeffreylayton.Employee e on e.employee_id = l.instructor_id
        where lp.member_id=?
        group by l.lesson_id, e.name, l.time
        """;
        
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, mid);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    String instructorName = rs.getString("instructor_name");
                    Date lessonTime = rs.getDate("time");
                    int totalSessions = rs.getInt("total_sessions");
                    int remainingSessions = rs.getInt("remaining_sessions");

                    System.out.println("Lesson: ");
                    System.out.println("  Instructor:\t\t"       + instructorName);
                    System.out.println("  Time:\t\t\t"           + lessonTime);
                    System.out.println("  Purchased Sessions:\t" + String.valueOf(totalSessions));
                    System.out.println("  Remaining Sessions:\t" + String.valueOf(remainingSessions));
                    System.out.println();
                }
            }
        }
    }

    /*-------------------------------------------------------------------
    | Method: addRentalRecord(int skiPassID, int equipmentID)
    |
    | Purpose: This method adds a new rental to the Rental relation containing
    |	       all records of equipment rentals within the SkiResort. Function
    |          first verifies that the given skipass and equipment id's actully
    |          to valid entries in their respective relations and then the new 
    |          record is added. Rental entry is also added to rental archive with
    |          a change state of 0 indicating a new renal record creation.
    |
    | Pre-condition:  The SkiPass, and Equipment relations exist. 
    |		      Public able to acccess rental archive seq.
    |
    | Post-condition: A new record has been added to the Rental relation denoting
    |                 a rental of the given equipment record by the given skipass
    |                 record.
    |
    | Parameters: int skiPassID - The id denoting which ski pass record this
    |			rental is being made from.
    |             int equipmentID - The id pointing to the equipment record
    |			that is being rented by the given ski pass.
    |
    | Returns: int, Attempts to add a new record to the rental relation if
    |		this is not possible then a error is thrown. Otherwise log id
    |           is returned.
    *-------------------------------------------------------------------*/
    public int addRentalRecord(int skiPassID, int equipmentID) throws SQLException,IllegalStateException{
	Statement myStmt = dbconn.createStatement();
	// First thing we need to do is determine if the given ski pass id is actually a valid
	// active ski pass the foreign key constraint on equipmentID will take care of that check.
	String checkSkiPassValid = "select 1 from mandyjiang.SkiPass where pass_id=%d";
	checkSkiPassValid = String.format(checkSkiPassValid,skiPassID);
	ResultSet res = myStmt.executeQuery(checkSkiPassValid);

        // Check the result to determine if there is a entry with the ski pass.
        if(!res.next()) {myStmt.close();throw new SQLException("Given pass id was not in ski pass table!");}
 
	// Next verify that there are no unreturned rental currently with the same equipmentID.
        String check_active_rental = "select 1 from tylergarfield.Rental where equipmentID=%d and returnStatus=0";
	check_active_rental = String.format(check_active_rental,equipmentID);
	ResultSet res2 = myStmt.executeQuery(check_active_rental);
	if(res2.next()) { myStmt.close(); throw new IllegalStateException("Tried to rent equipment that has not yet been returned!");}


        // Now that we have verified that the skiPassId is valid and the equipment is not already being rented.
 	//  we can actually attempt to insert the new record.
        int rentalID = getNextId("Rental","tylergarfield");
        String insert_query = "insert into tylergarfield.Rental values(%d,%d,%d,SYSTIMESTAMP,0)";
	insert_query = String.format(insert_query,rentalID,skiPassID,equipmentID);
        int numRowsAffected = myStmt.executeUpdate(insert_query);



        if(numRowsAffected > 0) {
            // Next create a new entrie in the log table with the previous state.
           // Now actually add a new archive entry for logging changes.
            int rentalArchiveID = getNextId("Rental_Archive","tylergarfield");
            String insertIntoLog = "insert into tylergarfield.Rental_Archive " +
                               " select %d,rentalID,skiPassID,equipmentID,rentalTime,returnStatus,SYSTIMESTAMP,0 " +
                               " from tylergarfield.Rental where rentalID=%d";
            insertIntoLog = String.format(insertIntoLog,rentalArchiveID,rentalID);
            myStmt.executeUpdate(insertIntoLog);
        } else { rentalID = -1;}

        myStmt.close();
        return rentalID;
    }
    
    /*-------------------------------------------------------------------
    | Method: updateRentalTime(int rentalID)
    |
    | Purpose: This function, givne a rentalID pointing to the rental record
    |          attempt to update the corresponding record's rental time. Function
    |          throws an error if the given rental id does not point to a valid
    |          rental record. The rental record is also added to the rental archive
    |          with a change state of 1 indicating a record update.
    |
    | Pre-condition: the Rental relation exists in my db and can be acessed
    |                by the public. Public able to acccess rental archive seq.
    |
    | Post-condition: If the given rentalID points to a valid rental record
    |                 then then that record's rental time has been updated to
    |                 the current time if the rentalID was valid.
    |
    | Parameters: int rentalID - The id for the rental relation to update
    |			the corresponding records time.
    |
    | Returns: int, return the id of the newly generated log record denoting
    |		the updated rental time.
    *-------------------------------------------------------------------*/ 
    public int updateRentalTime(int rentalID) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        // First verify that the given rentalID is in the Rental relation.
        String check_rental_id = "select 1 from tylergarfield.Rental where rentalID=%d";
        check_rental_id = String.format(check_rental_id, rentalID);
        ResultSet res = myStmt.executeQuery(check_rental_id);
        if(!res.next()) {myStmt.close(); throw new SQLException("The given rentalID does not exist!");}

        // Next update the rental time.
        String updateRentTime = "update tylergarfield.Rental set rentalTime=SYSTIMESTAMP where rentalID=%d";
        updateRentTime = String.format(updateRentTime,rentalID);
        int numRowsUpdated = myStmt.executeUpdate(updateRentTime);

        int rentalArchiveID = -1;

        if(numRowsUpdated > 0) {
         // Next create a new entrie in the log table with the previous state.
        // Now actually add a new archive entry for logging changes.
            rentalArchiveID = getNextId("Rental_Archive","tylergarfield");
            String insertIntoLog = "insert into tylergarfield.Rental_Archive " +
                               " select %d,rentalID,skiPassID,equipmentID,rentalTime,returnStatus,SYSTIMESTAMP,1 " +
                               " from tylergarfield.Rental where rentalID=%d";
            insertIntoLog = String.format(insertIntoLog,rentalArchiveID,rentalID);
            myStmt.executeUpdate(insertIntoLog);
        }

        myStmt.close();
        return rentalArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: returnEquipment(int rentalID)
    |
    | Purpose: Function attempts to update the rental record pointed to by
    |          the given rental id to a returned status thereby returning
    |          the desired piece of equipment. Function throws an error if
    |          an invalid rental is given or if equipmene has already been
    |          returned. The rental entry update is also added to the rental
    |          log with the changeState set to 1 indicating a record update.
    |
    | Pre-condition: The Rental and Rental_Archive relations are active and
    |                acessable to the public. Public able to acccess rental
    |		     archive seq.
    |
    | Post-condition: The Rental relation has been updated with the record
    |                 pointed to by rentalID now having a returned status.
    |                 Or an informative error has been thrown.
    |
    | Parameters: int rentalID - The id of the rental record to be updated.
    |
    | Returns: Function attempts to update the desired rental record and returns
    |		the log id of the logging the changed record.
    *-------------------------------------------------------------------*/ 
    public int returnEquipment(int rentalID) throws SQLException,IllegalStateException{
        Statement myStmt = dbconn.createStatement();
        // First verify that the given rentalID is in the Rental relation.
        String check_rental_id = "select 1 from tylergarfield.Rental where rentalID=%d";
        check_rental_id = String.format(check_rental_id, rentalID);
        ResultSet res = myStmt.executeQuery(check_rental_id);
        if(!res.next()) {myStmt.close(); throw new SQLException("The given rentalID does not exist!");}

        // Check that the rental had not already been returned.
        String checkRentalRet = "select returnStatus from tylergarfield.Rental where rentalID=%d";
        checkRentalRet = String.format(checkRentalRet,rentalID);
        res = myStmt.executeQuery(checkRentalRet);
        int retStat = 0;
        if(res.next()){retStat = res.getInt("returnStatus");}
        if(retStat == 1){myStmt.close();throw new IllegalStateException("Attempted to return equipment that was already returned!");}

        // Now that we have verified that the entry exists, the next thing to do is to actually update the
        // equipments returnStatus.
        String updateRent = "update tylergarfield.Rental set returnStatus=1 where rentalID=%d";
        updateRent = String.format(updateRent,rentalID);
        int numRowsUpdated = myStmt.executeUpdate(updateRent);

        int rentalArchiveID = -1;

        if(numRowsUpdated > 0) {
         // Next create a new entrie in the log table with the previous state.
        // Now actually add a new archive entry for logging changes.
            rentalArchiveID = getNextId("Rental_Archive","tylergarfield");
            String insertIntoLog = "insert into tylergarfield.Rental_Archive " +
                               " select %d,rentalID,skiPassID,equipmentID,rentalTime,returnStatus,SYSTIMESTAMP,1 " +
                               " from tylergarfield.Rental where rentalID=%d";
            insertIntoLog = String.format(insertIntoLog,rentalArchiveID,rentalID);
            myStmt.executeUpdate(insertIntoLog);
        }

	myStmt.close();
        return rentalArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: deleteRentalRecord(int rentalID)
    |
    | Purpose: This function, given the id of the rental record to be deleted,
    |          deletes the desired rental entry and adds it to the rental archive
    |          the new archive entry has a change state of 2 indicating the
    |          event being logged is a record deletion.
    |
    | Pre-condition: The rental archive and rental relations exist in the 
    |                tylergarfield db and are acessable to the public.
    |		     Public able to acccess rental archive seq.
    |
    | Post-condition: The given rental record has been deleted and archived
    |                 or an informative error message has been thrown indicating
    |                 why the desired rental record could not be deleted.
    |
    | Parameters: int rentalID - The unique id of the rental record to delete.
    |
    | Returns: int, the unique id of the rental archive record that has
    |		been created or -1 if an error occured.
    *-------------------------------------------------------------------*/ 
    public int deleteRentalRecord(int rentalID) throws SQLException,IllegalStateException{
        Statement myStmt = dbconn.createStatement();
        // First verify that the given rentalID is in the Rental relation.
        String checkRentalId = "select 1 from tylergarfield.Rental where rentalID=%d";
        checkRentalId = String.format(checkRentalId, rentalID);
        ResultSet res = myStmt.executeQuery(checkRentalId);
        if(!res.next()) {myStmt.close();throw new SQLException("Given rentalID was not present in the rental records!");}

        // We will check if the equipment " the record was created and the equipment has been used " by checking if the only
        // logged even is the record being created.
        String checkBeenUsed = "select changeState from tylergarfield.Rental_Archive where rentalID=%d";
        checkBeenUsed = String.format(checkBeenUsed,rentalID);
        res = myStmt.executeQuery(checkBeenUsed);
        int onlyAdded = 1;
        if(res!=null) {
            while(res.next()) {
                if(res.getInt("changeState")!=0){onlyAdded=0;}
            }
        }


        if(onlyAdded == 0) {
            // Before deleting or archiving the rental record also verify that the renetal has a return status of 1.
            String checkRentalReturned = "select returnStatus from tylergarfield.Rental where rentalID=%d";
            checkRentalReturned = String.format(checkRentalReturned,rentalID);
            res = myStmt.executeQuery(checkRentalReturned);
            int rentRetStat = 0;
            if(res.next()) {rentRetStat=res.getInt("returnStatus");}

            if(rentRetStat == 0) {myStmt.close();throw new IllegalStateException("Attempted to delete a active rental, return your equipment first!");}
        }
        // Now that we have verified that the rented equipment is no longer in use the next thing to do
        // is to actually archive the record and delete it.
        int rentalArchiveID = getNextId("Rental_Archive","tylergarfield");
        String addRentalToArchive = "insert into tylergarfield.Rental_Archive " +
                                    "select %d,rentalID,skiPassID,equipmentID,rentalTime,returnStatus,SYSTIMESTAMP,2 " +
                                    "from tylergarfield.Rental " +
                                    "where rentalID=%d";
        addRentalToArchive = String.format(addRentalToArchive,rentalArchiveID,rentalID);
        myStmt.executeUpdate(addRentalToArchive);

        // Now that that is done we can delete the rental record from the main Rental relation.
        String deleteRental = "delete from tylergarfield.Rental where rentalID=%d";
        deleteRental = String.format(deleteRental,rentalID);
        int numRowsDeleted = myStmt.executeUpdate(deleteRental);

        myStmt.close();

        return rentalArchiveID;


    }

    /*-------------------------------------------------------------------
    | Method: addEquipmentRecord(String type, double size, String name)
    |
    | Purpose: Function attempts to add a new record to the equipment relation
    |          with the desired atributes. Function first checks that all
    |          aspects of the proposed new record are in fact valid. If a
    |          new equipment record is able to be created then function also
    |          adds the new record to the equipment archive with a changeState
    |          of 0 indicating record creation.
    |
    | Pre-condition:  Equipment and Equipment_Archive relations exist in
    |                 the tylergarfield db and are acesable to the public.
    |                 Public able to acccess equipment archive seq.
    |
    | Post-condition: A new equipment record with the desired atributes has
    |                 been added to the equipment relation, or if this has
    |                 failed then a helpful error message has been thrown.
    |                 Also new entry has been added to the archive with a
    |                 changeState of 0 if equipment record was added.
    |
    | Parameters: String type - The desired equipment type for the new entry.
    |             double size - The desired size of the new equipment record.
    |             Strig name - The desired name of the new equipment record.
    |
    | Returns: int, the id of the equipment record that has been created
    |		or -1 if an error occured.
    *-------------------------------------------------------------------*/ 
    public int addEquipmentRecord(String type, double size, String name) throws SQLException,IllegalStateException{
        Statement myStmt = dbconn.createStatement();

        // Now check if the given size is valid for the given equipment type. Caller needs to verify that the
        // number given is either x.0 or x.5 for boots or x.0 for any other gear type. Rental gear will just have.
        String equipSzString = Double.toString(size);
        int decimalInd = equipSzString.indexOf(".");
        if(type.equals("boot")) {
            if(equipSzString.charAt(decimalInd+1)!='0'&&equipSzString.charAt(decimalInd+1)!='5'){
                myStmt.close();
                throw new IllegalStateException("Given boot for equipment but size was not .5 or .0!");
            }
        } else {
            if(size != (int) size) {
                myStmt.close();
                throw new IllegalStateException("Anything other than a boot must be an integer size!");
            }
        }

        if(type.equals("boot") && (size < 4.0 || size > 14.0)) {
            myStmt.close();
            throw new IllegalStateException("Given boot for equipment but size was not within valid range!");
        } else if(type.equals("pole") && (size < 100.0 || size > 140.0)){
            myStmt.close();
            throw new IllegalStateException("Given pole for equipment update but size was not within valid range!");
        } else if(type.equals("alpine ski") && (size < 115.0 || size > 200.0)){
            myStmt.close();
            throw new IllegalStateException("Given alpine ski for equipment update but size was not within valid range!");
        } else if(type.equals("snowboard") && (size < 90.0 || size > 178.0)){
             myStmt.close();
             throw new IllegalStateException("Given snowboard ski for equipment update but size was not within valid range!");
        } else if(type.equals("helmet") || type.equals("goggle") || type.equals("glove")){
            if(size < 1.0 || size > 3.0) {
                myStmt.close();
                throw new IllegalStateException("Given "+type +" for equipment update but size was not within valid range!");
            }
        }


        // Next we will get the next equipment id.
        int equipmentID = getNextId("Equipment","tylergarfield");
        // Now actually add the new entry to the relaton.
        String addToTable = "insert into tylergarfield.Equipment values(%d,'%s',%.1f,'%s')";
        addToTable = String.format(addToTable,equipmentID,type,size,name);
        //System.out.println("Formated equipment insert '"+addToTable+"' ");
        // Now execute the query.
        int numRowsAffected = myStmt.executeUpdate(addToTable);

        if(numRowsAffected > 0) {
        // Next log the equipment adition in the archive table.
            int equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
            String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
					"select %d,equipmentID,equip_type,equip_size,name,0 " +
					"from tylergarfield.Equipment where equipmentID=%d";
            addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
            myStmt.executeQuery(addEquipmentToArchive);
        } else {equipmentID=-1;}
        myStmt.close();
        return equipmentID;
    }

    /*-------------------------------------------------------------------
    | Method: deleteEquipmentRecord(int equipmentID)
    |
    | Purpose: This function attempts to delete the givne equipment record
    |          pointed to by the given id. Before deletion function checks
    |          for things like does a record with the given id exist and
    |          is the given record activly being rented at all. If equipment
    |          record can not be deleted then a helpful error message has been
    |          thrown.
    |
    | Pre-condition: The equipment, rental, and rental archive relations all
    |                exist in the tylergarfield db and are asccesable to the
    |                public. Public able to acccess equipment archive seq.
    |
    | Post-condition: The desired equipment record has been deleted and logged
    |                 in the equipment archive with a changState of 2, or if
    |                 the equipment record could not be deleted a helpful
    |                 error message has been thrown.
    |
    | Parameters: int equipmentID - The id of the equipment record to be
    |			potentially deleted.
    |
    | Returns: int, The id of the equipment archive record created or -1
    |		if an error has occured.
    *-------------------------------------------------------------------*/ 
    public int deleteEquipmentRecord(int equipmentID) throws SQLException{
        Statement myStmt = dbconn.createStatement();

        // // First check that the given equipmentID actually exists in the Equipment table.
        String checkEQID = "select 1 from tylergarfield.Equipment where equipmentID=%d";
        checkEQID = String.format(checkEQID,equipmentID);
        ResultSet res = myStmt.executeQuery(checkEQID);
        if(!res.next()){myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}

        // Next check if the given piece of equipment is currently being rented at all all.
        String checkRentedOut = "select 1 from tylergarfield.Rental where equipmentID=%d";
        checkRentedOut = String.format(checkRentedOut,equipmentID);
        res = myStmt.executeQuery(checkRentedOut);
        if(res.next()){myStmt.close();throw new SQLException("Equipment is currently rented!");}

        // Now put the equipment delition in the log table.
        int equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
        String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
                                        "select %d,equipmentID,equip_type,equip_size,name,2 " +
                                        "from tylergarfield.Equipment where equipmentID=%d";
        addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
        myStmt.executeUpdate(addEquipmentToArchive);

        // Finally remove the equipment from the equipment table.
        String removeQuery = "delete from tylergarfield.Equipment where equipmentID=%d";
        removeQuery = String.format(removeQuery,equipmentID);
        int numRowsAffected = myStmt.executeUpdate(removeQuery);
        myStmt.close();
        return equipmentArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: updateEquipmentType(int equipmentID,String newType)
    |
    | Purpose: Function attempts to update the equipment type of the given
    |          equipment record to the desired new type. Function first checks
    |	       for things such as if a equipment record with the given id actually
    |          exists and if the proposed new equipment type maintains the
    |          proposed records validity. Should the update fail a helpful
    |          error message is thrown.
    |
    | Pre-condition:  The equipment and equipment archive records exist in
    |                 the tylergarfield database and are accesable to the
    |                 public. Public able to acccess equipment archive seq.
    |
    | Post-condition: The given record has an updated type or if the type
    |                 could not be updated a helpful error message has been
    |                 thrown. Also if the record was updated a new entry
    |                 has been added to the equipment archive with a changeState
    |                 of 1 indicating a update.
    |
    | Parameters: int equipmentID - The id of the equipment record to be updated.
    |             String newType - The proposed new type of the equipment record.
    |
    | Returns: int, the id of the equipment archive entry that has recorded the update
    |		or -1 if the update has failed.
    *-------------------------------------------------------------------*/ 
    public int updateEquipmentType(int equipmentID,String newType) throws SQLException{
        Statement myStmt = dbconn.createStatement();

        // First verify that the equipment that is attempting to be added updated actually exists.
        String checkEQID = "select 1 from tylergarfield.Equipment where equipmentID=%d";
        checkEQID = String.format(checkEQID,equipmentID);
        ResultSet res = myStmt.executeQuery(checkEQID);
        if(!res.next()){myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}

        // Now get the current size of the equipment record and check if the proposed new type is compatable with the current
        // record.
        String getCurrSz = "select equip_size from tylergarfield.Equipment where equipmentID=%d";
        getCurrSz = String.format(getCurrSz,equipmentID);
        res = myStmt.executeQuery(getCurrSz);
        double currSz = 0.0;
	if(res.next()){currSz=res.getDouble("equip_size");}
        else{myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}

        if(newType.equals("boot") && (currSz < 4.0 || currSz > 14.0)) {
            myStmt.close();
            throw new IllegalStateException("Given boot is for equipment update new current was not within valid range!");
        } else if(newType.equals("pole") && (currSz < 100.0 || currSz > 140.0)){
            myStmt.close();
            throw new IllegalStateException("Given pole for equipment update but current size was not within valid range!");
        } else if(newType.equals("alpine ski") && (currSz < 115.0 || currSz > 200.0)){
            myStmt.close();
            throw new IllegalStateException("Given alpine ski for equipment update but current size was not within valid range!");
        } else if(newType.equals("snowboard") && (currSz < 90.0 || currSz > 178.0)){
             myStmt.close();
             throw new IllegalStateException("Given snowboard ski for equipment update but current size was not within valid range!");
        } else if(newType.equals("helmet") || newType.equals("goggle") || newType.equals("glove")){
            if(currSz < 1.0 || currSz > 3.0) {
                myStmt.close();
                throw new IllegalStateException("Given "+newType +" for equipment update but current size was not within valid range!");
            }
        }


        // Now actually update the equipment type and record the change in the log.
        String updateType = "update tylergarfield.Equipment set equip_type='%s' where equipmentID=%d";
        updateType = String.format(updateType,newType,equipmentID);
        int numRowsAffected = myStmt.executeUpdate(updateType);

         // If the entry was successfully updated add the update to the log.
        int equipmentArchiveID = -1;
        if(numRowsAffected > 0 ) {
            equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
            String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
                                        "select %d,equipmentID,equip_type,equip_size,name,1 " +
                                        "from tylergarfield.Equipment where equipmentID=%d";
            addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
            myStmt.executeQuery(addEquipmentToArchive);
        }

        myStmt.close();
        return equipmentArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: updateEquipTypeSz(int equipmentID,String newType, double newSz)
    |
    | Purpose: This function attemps to update the given equipment record's
    |	       type and size in conjunction with eachother. I added this
    |          function for cases where there was no other valid equipment
    |          type for the current size but one desires to update the
    |          equipment type of the given record. As in the above functions
    |          the equipment record is updated and the change is logged with
    |          a change state of 1 indicating a record update.
    |
    | Pre-condition: The equipment and equipment_archive relations exist
    |                in the tylergarfield database and are assecable to 
    |		     to the public.  The public is also able to acess 
    |	             the equipment archive id sequence.
    |
    |
    | Post-condition: The givne equipment record has been updated and the
    |                 update has been logged in the equipment_archive or
    |                 a helpful error message has been thrown.
    |
    | Parameters: int equipmentID - The id of the equipment record to be updated.
    |             String newType - The desired new type for this equipment record.
    |             double newSz - The desired new size of this equipment record.
    |
    | Returns: int,the id of the equipmenet archive entry recording the equipment
    |		record or -1 if an error has occured.
    *-------------------------------------------------------------------*/ 
    public int updateEquipTypeSz(int equipmentID,String newType, double newSz) throws SQLException,IllegalStateException{
        Statement myStmt = dbconn.createStatement();

        // First verify that the equipment that is attempting to be added actually exists.
        String checkEQID = "select 1 from tylergarfield.Equipment where equipmentID=%d";
        checkEQID = String.format(checkEQID,equipmentID);
        ResultSet res = myStmt.executeQuery(checkEQID);
        if(!res.next()){myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}

        // Now validate that the new equipment type and size are compatable with eachother.
        if(newType.equals("boot") && (newSz < 4.0 || newSz > 14.0)) {
            myStmt.close();
            throw new IllegalStateException("Given boot is for equipment update new current was not within valid range!");
        } else if(newType.equals("pole") && (newSz < 100.0 || newSz > 140.0)){
            myStmt.close();
            throw new IllegalStateException("Given pole for equipment update but current size was not within valid range!");
        } else if(newType.equals("alpine ski") && (newSz < 115.0 || newSz > 200.0)){
            myStmt.close();
            throw new IllegalStateException("Given alpine ski for equipment update but current size was not within valid range!");
        } else if(newType.equals("snowboard") && (newSz < 90.0 || newSz > 178.0)){
             myStmt.close();
             throw new IllegalStateException("Given snowboard ski for equipment update but current size was not within valid range!");
        } else if(newType.equals("helmet") || newType.equals("goggle") || newType.equals("glove")){
            if(newSz < 1.0 || newSz > 3.0) {
                myStmt.close();
                throw new IllegalStateException("Given "+newType +" for equipment update but current size was not within valid range!");
            }
        }

        // Now actaully execute the update after we have validated that the update is valid.
        String updateTypeSz = "update tylergarfield.Equipment set equip_type='%s',equip_size=%f where equipmentID=%d";
        updateTypeSz = String.format(updateTypeSz,newType,newSz,equipmentID);
        int numRowsAffected = myStmt.executeUpdate(updateTypeSz);

         // If the entry was successfully updated add the update to the log.
        int equipmentArchiveID = -1;
        if(numRowsAffected > 0 ) {
            equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
            String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
                                        "select %d,equipmentID,equip_type,equip_size,name,1 " +
                                        "from tylergarfield.Equipment where equipmentID=%d";
            addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
            myStmt.executeQuery(addEquipmentToArchive);
        }

        myStmt.close();
        return equipmentArchiveID;

    }

    /*-------------------------------------------------------------------
    | Method: updateEquipmentName(int equipmentID,String equipName)
    |
    | Purpose: Function attempts to update the desired equipment record's
    |	       name. Since there are no stipulations on the equipment's name
    |          so long as the given equipment id points to a valid equipment
    |          record this function should successfully update the given record.
    |
    | Pre-condition:  The equipment, equipment archive relations exist in the
    |                 tylergarield db and are assecable to the public. The
    |                 public is also able to acess the equipment archive id
    |		      sequence.
    |
    | Post-condition: The given equipment record has an updated name and the
    |                 equipment archive has a new entry with a changState of
    |                 1 to reflect this change or a helpful error message has
    |                 been thrown.
    |
    | Parameters: int equipmentID - The id of the equipment record to be updated.
    |		  String equipName - The desired new name for the given equipment
    |			record.
    |
    | Returns: int, returns the id of the new equipment archive entry denoting
    |		the update or -1 if an error has occured.
    *-------------------------------------------------------------------*/ 
    public int updateEquipmentName(int equipmentID,String equipName) throws SQLException{
         Statement myStmt = dbconn.createStatement();

        // First verify that the equipment that is attempting to be added actually exists.
        String checkEQID = "select 1 from tylergarfield.Equipment where equipmentID=%d";
        checkEQID = String.format(checkEQID,equipmentID);
        ResultSet res = myStmt.executeQuery(checkEQID);
        if(!res.next()){myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}

        // Now actually update the equipment type and record the change in the log.
        String updateName = "update tylergarfield.Equipment set name='%s' where equipmentID=%d";
        updateName = String.format(updateName,equipName,equipmentID);
        int numRowsAffected = myStmt.executeUpdate(updateName);

         // If the entry was successfully updated add the update to the log.
        int equipmentArchiveID = -1;
        if(numRowsAffected > 0 ) {
            equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
            String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
                                        "select %d,equipmentID,equip_type,equip_size,name,1 " +
                                        "from tylergarfield.Equipment where equipmentID=%d";
            addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
            myStmt.executeQuery(addEquipmentToArchive);
        }

        myStmt.close();
        return equipmentArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: updateEquipmentSize(int equipmentID, double newSize)
    |
    | Purpose: This function attempts to update the size of the given
    |          equipment record to the new size. Function first checks
    |          if the given equipment id is valid and if the proposed
    |          new size is compatable with the current equipment type.
    |          If the update is valid then the record is updated and a
    |          new archive entry is created.
    |
    | Pre-condition: The equipment, equipment_archive relations exist in
    |                the tylergarfield db and are acessable to the public.
    |		     Same goes for the equipment_archive sequence.
    |
    | Post-condition: The given record has been updated and a new archive
    |                 entry with a changeState of 1 has been created or
    |                 if this failed a helpful error message has been thrown.
    |
    | Parameters: int equipmentID - The id of the desired equipment record to
    |			be updated.
    |             double newSize - The desired new size of the equipment record.
    |
    | Returns: int, the id of the equipment_archive entry denoting the update
    |		or -1 if an error has occured.
    *-------------------------------------------------------------------*/ 
    public int updateEquipmentSize(int equipmentID, double newSize) throws SQLException,IllegalArgumentException{
        Statement myStmt = dbconn.createStatement();

        // First verify that the equipment that is attempting to be added actually exists.
        String checkEQID = "select equip_type from tylergarfield.Equipment where equipmentID=%d";
        checkEQID = String.format(checkEQID,equipmentID);
        ResultSet res = myStmt.executeQuery(checkEQID);
	String equipType = "";
        if(!res.next()){myStmt.close();throw new SQLException("A record with the given equipmentID could not be found!");}
        else{equipType = res.getString("equip_type");}

        // Now check if the given size is valid for the given equipment type. Caller needs to verify that the
        // number given is either x.0 or x.5 for boots or x.0 for any other gear type. Rental gear will just have.
        String equipSzString = Double.toString(newSize);
        int decimalInd = equipSzString.indexOf(".");
        char charAfterDecimal = equipSzString.charAt(decimalInd+1);
        if(equipType.equals("boot") && ((newSize < 4.0 || newSize > 14.0) || (charAfterDecimal!='0'&&charAfterDecimal!='5'))) {
            myStmt.close();
            throw new IllegalStateException("Given equipment records is for a boot but size was not within valid range!");
        } else if(equipType.equals("pole") && ((newSize < 100.0 || newSize > 140.0) || newSize!=(int)newSize)){
            myStmt.close();
            throw new IllegalStateException("Given equipment is for a pole update but size was not within valid range!");
        } else if(equipType.equals("alpine ski") && ((newSize < 115.0 || newSize > 200.0)||newSize!=(int)newSize)){
            myStmt.close();
            throw new IllegalStateException("Given equipment is for ski's update but size was not within valid range!");
        } else if(equipType.equals("snowboard") && ((newSize < 90.0 || newSize > 178.0)||newSize!=(int)newSize)){
             myStmt.close();
             throw new IllegalStateException("Given equipment record is for a snowboard update but size was not within valid range!");
        } else if(equipType.equals("helmet") || equipType.equals("goggle") || equipType.equals("glove")) {
            if((newSize < 1.0 || newSize > 3.0)||newSize!=(int)newSize) {
                myStmt.close();
                throw new IllegalStateException("Given record was for a "+equipType +" but size was not within valid range!");
            }
        }

        int numRowsAffected = 0;

        // Now actually update the equipment type and record the change in the log.
        String updateSize = "update tylergarfield.Equipment set equip_size=%f where equipmentID=%d";
        updateSize = String.format(updateSize,newSize,equipmentID);
        numRowsAffected = myStmt.executeUpdate(updateSize);
         // If the entry was successfully updated add the update to the log.
        int equipmentArchiveID = -1;
        if(numRowsAffected > 0 ) {
            equipmentArchiveID = getNextId("Equipment_Archive","tylergarfield");
            String addEquipmentToArchive = "insert into tylergarfield.Equipment_Archive " +
                                        "select %d,equipmentID,equip_type,equip_size,name,1 " +
                                        "from tylergarfield.Equipment where equipmentID=%d";
            addEquipmentToArchive = String.format(addEquipmentToArchive,equipmentArchiveID,equipmentID);
            myStmt.executeQuery(addEquipmentToArchive);
        }

        myStmt.close();
        return equipmentArchiveID;
    }

    /*-------------------------------------------------------------------
    | Method: int addProperty(String type, int income)
    |
    | Purpose: Generates the SQL statement to add a new property to the 
    |          Property table with property_type type and daily_income income.
    |          Throws an error if a free lot is attmepted to be made with
    |          an income that is not 0.
    |
    | Pre-condition: type is a valid propety_type and the Property table is
    |                public.
    |
    | Post-condition: A new property with property_type type and 
    |                 daily_income income is added to Property with a 
    |                 unique propertyID
    |
    | Parameters: String type - String value that will be set to the new
    |             property_type
    |             int income - the value for the property daily_income
    |
    | Returns: int propetyID -> either the unique propertyID of the added
    |          property, or -1 if the addition failed
    *-------------------------------------------------------------------*/
    public int addProperty(String type, int income)throws SQLException,IllegalArgumentException {
        Statement myStmt = dbconn.createStatement();
        if (type == "free lot" && income != 0){
            myStmt.close();
            throw new IllegalStateException("A free parking lot cannot have any daily income");
        }
        int propertyID = getNextId("Property","ascherer");
        
        String addToTable = "insert into ascherer.Property values(%d,'%s',%d)";
        addToTable = String.format(addToTable,propertyID,type,income);
        int rowsAffected = myStmt.executeUpdate(addToTable);
        if (rowsAffected <= 0){
            propertyID = -1;
        }
        return propertyID;
    }

    /*-------------------------------------------------------------------
    | Method: int updatePropertyIncome(int propertyID, int newIncome)
    |
    | Purpose: Generates the SQL statement to change the property daily_income
    |          of the property at propertyID in Property to newIncome. Throws
    |          an error if the property_type is free lot and the income is
    |          trying to be changed to something that is not 0
    |
    | Pre-condition: propertyID exists in the pubic Property table
    |
    | Post-condition: The property at propetyID now has a daily_income
    |                 of newIncome
    |
    | Parameters: int propertyID - the ID for the property in Property
    |             that is set to be updated.
    |             int newIncome - the new value for the property
    |                             daily_income
    |
    | Returns: propetyID -> either the same as the parameter if update was
    |                       successful, or -1 if the update failed
    *-------------------------------------------------------------------*/
    public int updatePropetyIncome(int propertyID, int newIncome) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        String checkPID = "select property_type from ascherer.Property where propertyID=%d";
        checkPID = String.format(checkPID,propertyID);
        ResultSet res = myStmt.executeQuery(checkPID);

        String propertyType = "";
        if(!res.next()){myStmt.close();throw new SQLException("A property with that ID was not found");}
        else{
            propertyType = res.getString("property_type");
        }

        if (propertyType == "free lot" && newIncome != 0){
            myStmt.close();
            throw new IllegalStateException("A free parking lot cannot have any daily income");
        }

        String updateIncome = "update ascherer.Property set daily_income=%d where propertyID=%d";
        updateIncome = String.format(updateIncome,newIncome,propertyID);
        int rowsAffected = myStmt.executeUpdate(updateIncome);
        if (rowsAffected <= 0){
            propertyID = -1;
        }
        return propertyID;
    }

    /*-------------------------------------------------------------------
    | Method: int updatePropertyType(int propertyID, String newType)
    |
    | Purpose: Generates the SQL statement to change the property type
    |          of the property at propertyID in Property to newType.
    |          If newType == free lot, update income to 0
    |
    | Pre-condition: propertyID exists in the pubic Property table and
    |                newType is a valid property_type
    |
    | Post-condition: The property at propetyID now has a property_type
    |                 of newType
    |
    | Parameters: int propertyID - the ID for the property in Property
    |             that is set to be updated.
    |             String newType - String value for the property_type
    |
    | Returns: propetyID -> either the same as the parameter if update was
    |                       successful, or -1 if the update failed
    *-------------------------------------------------------------------*/ 
    public int updatePropertyType(int propertyID, String newType) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        String checkPID = "select property_type from ascherer.Property where propertyID=%d";
        checkPID = String.format(checkPID,propertyID);
        ResultSet res = myStmt.executeQuery(checkPID);

        if(!res.next()){myStmt.close();throw new SQLException("A property with that ID was not found");}

        if (newType == "free lot"){
            updatePropetyIncome(propertyID, 0);
        }

        String updateType = "update ascherer.Property set property_type='%s' where propertyID=%d";
        updateType = String.format(updateType,newType,propertyID);
        int rowsAffected = myStmt.executeUpdate(updateType);
        if (rowsAffected <= 0){
            propertyID = -1;
        }
        return propertyID;
    }

    /*-------------------------------------------------------------------
    | Method: int deleteProperty(int propertyID)
    |
    | Purpose: Generates the SQL statement to delete the property at 
    |          propertyID from the Property table and runs it.
    |
    | Pre-condition: propertyID exists in the Property table and that 
    |                table is set to public
    |
    | Post-condition: propertyID no longer exists in Property
    |
    | Parameters: int propertyID - the ID for the property in Property
    |             that is set to be deleted.
    |
    | Returns: numRowsAffected -> 1 if deletion was successful, 0 if 
    |          the deletion failed
    *-------------------------------------------------------------------*/ 
    public int deleteProperty(int propertyID) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        String checkPID = "select property_type from ascherer.Property where propertyID=%d";
        checkPID = String.format(checkPID,propertyID);
        ResultSet res = myStmt.executeQuery(checkPID);

        if(!res.next()){myStmt.close();throw new SQLException("A property with that ID was not found");}

        String deleteQuery = "delete from ascherer.Property where propertyID=%d";
        deleteQuery = String.format(deleteQuery,propertyID);
        int numRowsAffected = myStmt.executeUpdate(deleteQuery);
        myStmt.close();
        return numRowsAffected;
    }

    /*-------------------------------------------------------------------
    | Method: int getYearlyProfit(int season, int years)
    |
    | Purpose: Generates and returns the value of Query 4 to get the 
    |          estimated yearly profits for a given number of years. 
    |          Does so by multiplying the daily_income of all properties 
    |          in the Property table by season and subtracting the total 
    |          salaries from the Employee table, then multiplying that by years.
    |
    | Pre-condition: Both the Property and Employee table have the necessay
    |                relations and are set to public for selecting
    |
    | Post-condition: The profit has been calculated and will be printed by
    |                 getYearlyProfit() in SkiResort
    |
    | Parameters: int season - how many days out of 365 the ski season lasts
    |             to determine how long the daily_income should be totaled.
    |             int years - how many years of profit to look at
    |
    | Returns: int profit
    *-------------------------------------------------------------------*/ 
    public int getYearlyProfit(int season, int years) throws SQLException{
        int profit = 0;

        String sql = """
                SELECT ((p.total_income - e.total_salary) * %d) AS yearly_profit
                FROM (SELECT SUM(daily_income) * %d AS total_income FROM ascherer.Property) p,
                (SELECT SUM(salary) AS total_salary FROM jeffreylayton.Employee) e
                """;
        sql = String.format(sql, years, season);

        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            try (ResultSet rs = p.executeQuery()) {
                while(rs.next()){
                    profit = rs.getInt("yearly_profit");
                }
            }
        }
        return profit;
    }

    /*-------------------------------------------------------------------
    | Method: runQueryTwo(int skiPassID) 
    |
    | Purpose: Function prints out all lift entrys and equipment renetals
    |          associated with the givne ski pass id. Specically for entries
    |          the name of the lift and the entry time's are printed and for
    |	       rentals the equipmenet name and rental time's are printed.
    |
    | Pre-condition: The SkiPass, Entry, Rental, and Equipment relations
    |                all exist in their respective db's and are acessable
    |                to the public.
    |
    | Post-condition: A nicley formated table has been printed to the user
    |		      displaying the results of this query.
    |
    | Parameters: int skiPassID - The id of the ski pass entry to get the
    |			asocciated information for.
    |
    | Returns: None. Prints query result to stdout or throws an exception
    |		should a error occure.
    *-------------------------------------------------------------------*/ 
    public void runQueryTwo(int skiPassID) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        // First thing we need to do is determine if the given ski pass id is actually a valid
        // active ski pass the foreign key constraint on equipmentID will take care of that check.
        String checkSkiPassValid = "select 1 from mandyjiang.SkiPass where pass_id=%d";
        checkSkiPassValid = String.format(checkSkiPassValid,skiPassID);
        ResultSet res = myStmt.executeQuery(checkSkiPassValid);

        // Check the result to determine if there is a entry with the ski pass.
        if(!res.next()) {myStmt.close();throw new SQLException("Given pass id was not in ski pass table!");}

        // Next perform the query for the first part of this which is getting the lift entries associated
        // with the given ski pass.
        String queryLiftRides = "select lift_name, entrance_time from mandyjiang.Entry where pass_id=%d";
        queryLiftRides = String.format(queryLiftRides,skiPassID);
        res = myStmt.executeQuery(queryLiftRides);

        // Print out the result table of the above query.
        System.out.println("\t\t\t*********************************************************");

        String colHeadersLift = "\t\t\t%-25s  %-30s";
        colHeadersLift = String.format(colHeadersLift,"Lift name","Entered At Time");
        System.out.println(colHeadersLift);

        int numDashes = 25;
        int numDashes2 = 30;
        System.out.print("\t\t\t");
        for(int i=0; i<numDashes; i++) {System.out.print("-");}
        System.out.print("  ");
        for(int i=0; i<numDashes2; i++) {System.out.print("-");}
        System.out.println();

        if(res!=null) {
            while(res.next()) {
                String nextTup = "\t\t\t%-25s  %-30s";
                nextTup = String.format(nextTup,res.getString("lift_name"),res.getString("entrance_time"));
                System.out.println(nextTup);
	    }
        }

        System.out.println();

        String colHeadersRent = "\t\t\t%-25s  %-30s";
        colHeadersRent = String.format(colHeadersRent,"Gear Name","Rented At Time");
        System.out.println(colHeadersRent);
        

        System.out.print("\t\t\t");
        for(int i=0; i<numDashes; i++) {System.out.print("-");}
        System.out.print("  ");
        for(int i=0; i<numDashes2; i++) {System.out.print("-");}
        System.out.println();

        // Now perform the second part of this query which is getting all the equipment name's and rentalTime's
        // of all rentals associated with the given ski pass.
        String queryRentalRecords = "select name,rentalTime from tylergarfield.Rental join " +
				    "tylergarfield.Equipment on tylergarfield.Rental.equipmentID = " +
				    "tylergarfield.Equipment.equipmentID where skiPassID=%d";
        queryRentalRecords = String.format(queryRentalRecords,skiPassID);
        res = myStmt.executeQuery(queryRentalRecords);

        if(res!=null) {
            while(res.next()) {
                String nextTup = "\t\t\t%-25s  %-30s";
                nextTup = String.format(nextTup,res.getString("name"),res.getString("rentalTime"));
                System.out.println(nextTup);
            }
        }

        System.out.println("\t\t\t*********************************************************");
        myStmt.close();
    }

    /*-------------------------------------------------------------------
    | Method: void printOutRentals()
    |
    | Purpose: Print out the contents of the Rental relation to the user.
    |
    | Pre-condition:  Rental relation exists in the tylergarfield db and
    |                 is able to be used by the public.
    |
    | Post-condition: The current contents of the Rental relation has
    |		      been printed to stdout in a neat manner or a exception
    |                 has been thrown.
    |
    | Parameters: None.
    |
    | Returns: None. Prints to stdout.
    *-------------------------------------------------------------------*/ 
    public void printOutRentals() throws SQLException{
        Statement myStmt = dbconn.createStatement();

        // First actually execute the query to get all columns of each tuple.
        String getRentalTable = "select * from tylergarfield.Rental";
        ResultSet res = myStmt.executeQuery(getRentalTable);

        // Next print out the column headers for each field.
        String colHeaders = "\t\t\t%-6s %-6s %-6s %-35s %-8s\n";
        colHeaders = String.format(colHeaders,"rid","skipid","eid","rental time","ret stat");
        System.out.print(colHeaders);
        System.out.println("\t\t\t-----------------------------------------------------------------");

        // Now make sure there are results and if there are any then print out each tuple.
        String nextTupFmat = "\t\t\t%-6d %-6d %-6d %-35s %-8d\n";
        if(res!=null) {
            while(res.next()) {
                String nextTup = String.format(nextTupFmat,res.getInt("rentalID"),res.getInt("skiPassID"),res.getInt("equipmentID"),
						res.getString("rentalTime"),res.getInt("returnStatus"));
                System.out.print(nextTup);
            }
        }
        myStmt.close();

    }

    /*-------------------------------------------------------------------
    | Method: void printOutEquipment()
    |
    | Purpose: Method prints out the contents of the equipment relation to
    |	       the user.
    |
    | Pre-condition: The Equipment relation exists in the tylergarfield db
    |		     and is able to be used by the public.
    |
    | Post-condition: The contents of the equipment relation have been printed
    |                 to stdout or a exception has been thrown.
    |
    | Parameters: None.
    |
    | Returns: None. Prints to stdout or throws an exception.
    *-------------------------------------------------------------------*/ 
    public void printOutEquipment() throws SQLException {
        Statement myStmt = dbconn.createStatement();

        // First actually execute the query to get all columns of each tuple.
        String getEquipTable = "select * from tylergarfield.Equipment";
        ResultSet res = myStmt.executeQuery(getEquipTable);

        // Next print out the column headers for each field.
        String colHeaders = "\t\t\t%-6s %-10s %-8s %-30s\n";
        colHeaders = String.format(colHeaders,"eid","equip type","equip sz","equip name");
        System.out.print(colHeaders);
        System.out.println("\t\t\t--------------------------------------------------------");

        String nextTupFmat = "\t\t\t%-6d %-10s %-8.1f %-30s\n";
        // Now actually verify that there are results and for each tuple print that tuples info out.
        if(res!=null) {
            while(res.next()) {
                String nextTup = String.format(nextTupFmat,res.getInt("equipmentID"),res.getString("equip_type"),
					res.getDouble("equip_size"),res.getString("name"));
                System.out.print(nextTup);
            }
        }
        myStmt.close();
    }

}
