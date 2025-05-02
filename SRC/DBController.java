import java.sql.*;
import java.util.ArrayList;

public class DBController {
    private static final String URL="jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    private final Connection dbconn;

    public DBController(String user,String pass) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        dbconn=DriverManager.getConnection(URL,user, pass);
    }

    public void close() {
        try { dbconn.close(); } catch (SQLException ignored) {}
    }

    //UNIQUE ID getter using sequences for each tablename
    private int getNextId(String table,String owner) throws SQLException {
        String seq=table.toUpperCase() + "_SEQ";
        String sql="select " + owner + "." + seq + ".NEXTVAL FROM DUAL";
        try (Statement s=dbconn.createStatement();
             ResultSet rs=s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    //  Member

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

    //  Ski Pass

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
	
	System.out.println("Adding old rentals to rental archive!");  
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
                    stmt1.setInt(2,pid);
                    stmt1.executeUpdate();
                }
                // Now that that is done we can delete the rental record from the main Rental relation.
                String deleteRental = "delete from tylergarfield.Rental where rentalID=%d";
                try(PreparedStatement stmt2 = dbconn.prepareStatement(deleteRental)) {
                    stmt2.setInt(1,pid);
                    stmt2.executeUpdate();
                }
              }
            }
           
        }
        
        //System.out.println("Was able to add old rentals to rental archive!"); 

        // Now, Archive / delete 
        String archiveSql="""
            insert into mandyjiang.SkiPass_Archive(
            SPARCHIVE_ID,PASS_ID,MEMBER_ID,TYPE,
            REMAINING_USES,PURCHASE_TIME,EXPIRATION_DATE,ARCHIVED_TIME)
            select mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL,
                pass_id,member_id,type,remaining_uses,
                purchase_time,expiration_date,SYSTIMESTAMP
            from mandyjiang.SkiPass
            where pass_id=?
            """;
                
        String deleteSql=
          "DELETE from mandyjiang.SkiPass where pass_id=?";
    
	System.out.println("Was able to delete the ski pass!"); 
        try (PreparedStatement a=dbconn.prepareStatement(archiveSql);
             PreparedStatement d=dbconn.prepareStatement(deleteSql)) {
            a.setInt(1,pid); 
            a.executeUpdate();
            d.setInt(1,pid);
            return d.executeUpdate() == 1;
        }
        
    }

    //  Lift Entry 
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
            System.err.println("tigkajs;f");
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
                        if (lifts.indexOf(newLift) != -1) {
                            lifts.add(newLift);
                        }
                    }

                    trail = newTrail;
                }            
            }
        }
    }

    // TODO implement these.
    // public int addRentalRecord(int skiPassID, int equipmentID) {
    // }
    //
    // public int deleteRentalRecord(int rentalID) {
    // }
    //
    // public int addEquipmentRecord(String type, int size, String name) {
    // }
    //
    // public int deleteEquipmentRecord(int equipmentID) {
    // }


    // equipment,  equipment rental, lesson purchase, queries (maybe implement in other files?)

    // Lesson + Lesson Purchase
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

    // TODO implement these.
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
    
    // TODO add function here to modify rental time here!!!!!!
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

    // Function returns 1 for a normal error and 2 if the givne rentalID does not exist.
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

    public int addEquipmentRecord(String type, double size, String name) throws SQLException,IllegalStateException{
        Statement myStmt = dbconn.createStatement();

        // Now check if the given size is valid for the given equipment type. Caller needs to verify that the
        // number given is either x.0 or x.5 for boots or x.0 for any other gear type. Rental gear will just have.
        // TODO you were here ACTUALLY FILL IN THESE CHECKS.
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
	if(res.next()){res.getDouble("equip_size");}
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
        // TODO you were here ACTUALLY FILL IN THESE CHECKS.
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

    public void runQueryTwo(int skiPassID) throws SQLException{
        Statement myStmt = dbconn.createStatement();
        // First thing we need to do is determine if the given ski pass id is actually a valid
        // active ski pass the foreign key constraint on equipmentID will take care of that check.
        String checkSkiPassValid = "select 1 from mandyjiang.SkiPass where pass_id=%d";
        checkSkiPassValid = String.format(checkSkiPassValid,skiPassID);
        ResultSet res = myStmt.executeQuery(checkSkiPassValid);

        // Check the result to determine if there is a entry with the ski pass.
        if(!res.next()) {myStmt.close();throw new SQLException("Given pass id was not in ski pass table!");}

        String queryLiftRides = "select lift_name, entrance_time from mandyjiang.Entry where pass_id=%d";
        queryLiftRides = String.format(queryLiftRides,skiPassID);
        res = myStmt.executeQuery(queryLiftRides);

        System.out.println("\t\t\t*********************************************************");

        String colHeadersLift = "\t\t\t%-25s  %-30s";
        colHeadersLift = String.format(colHeadersLift,"Lift name","Entered At Time");
        System.out.println(colHeadersLift);

        //String colDashes = "% %-20s";
        //colDashes = String.format(colDashes,"-","-");

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
    }

}

