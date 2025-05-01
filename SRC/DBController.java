import java.sql.*;

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
        String archiveSql = """
        insert into jeffreylayton.LessonPurchase_Archive (
            order_id, member_id, lesson_id, total_sessions, remaining_sessions
        )
        select order_id, member_id, lesson_id, total_sessions, remaining_sessions
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
        select e.name as "instructor_name", l.time, lp.total_sessions, lp.remaining_sessions
        from jeffreylayton.LessonPurchase lp
        join jeffreylayton.Lesson l on l.lesson_id = lp.lesson_id
        join jeffreylayton.Employee e on e.employee_id = l.instructor_id
        where lp.member_id=?
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
}
