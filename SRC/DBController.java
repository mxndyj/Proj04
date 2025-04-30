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
        try (PreparedStatement p=dbconn.prepareStatement(sql)) {
            p.setInt(1,id);
            p.setString(2,name);
            p.setString(3,phone);
            p.setString(4,email);
            p.setDate(5,dob == null || dob.isBlank() ? null : Date.valueOf(dob));
            p.setString(6,emergency);
            p.executeUpdate();
            
        }
        return id;
    }

    public void updateMember(int id,String phone,String email,String emergency) throws SQLException {
        String sql="update mandyjiang.Member SET phone=COALESCE(?,phone),email=COALESCE(?,email),emergency_contact=COALESCE(?,emergency_contact) where member_id=?";
        try (PreparedStatement p=dbconn.prepareStatement(sql)) {
            p.setString(1,phone);
            p.setString(2,email);
            p.setString(3,emergency);
            p.setInt(4,id);
            int updated=p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No member with ID " + id);
            }
        
        }
    }

    public boolean deleteMember(int id) throws SQLException {
        // 1. Check for active ski passes
        String checkPass=
          "select 1 from mandyjiang.SkiPass " +
          "where member_id=? AND (remaining_uses>0 OR expiration_date > SYSTIMESTAMP)";
        try (PreparedStatement cp=dbconn.prepareStatement(checkPass)) {
            cp.setInt(1,id);
            if (cp.executeQuery().next()) {
                throw new IllegalStateException("Cannot delete member: active ski passes exist.");
            }
        }
    
        // // 2. Check for open equipment rentals update tables names later
        // String checkRental="select * from Rental where member_id=? AND return_status='OUT'";
        // try (PreparedStatement cr=dbconn.prepareStatement(checkRental)) {
        //     cr.setInt(1,id);
        //     if (cr.executeQuery().next()) {
        //         throw new IllegalStateException("Cannot delete member: open equipment rentals exist.");
        //     }
        // }
    
        // // 3. Check for unused lesson sessions
        // String checkLesson="select * from LessonPurchase where member_id=? AND remaining_sessions>0";
        // try (PreparedStatement cl=dbconn.prepareStatement(checkLesson)) {
        //     cl.setInt(1,id);
        //     if (cl.executeQuery().next()) {
        //         throw new IllegalStateException("Cannot delete member: unused lesson sessions exist.");
        //     }
        // }
    
    

        //  Delete member
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
        String lookupSql=""" 
        SELECT total_uses,price from mandyjiang.PassType where type=?
        """;
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

        String insertsql="""
        Insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) 
        VALUES(?,?,?,?,SYSTIMESTAMP,?)
        """;
        try (PreparedStatement p=dbconn.prepareStatement(insertsql)) {
            p.setInt(1,id);
            p.setInt(2,mid);
            p.setString(3,type);
            p.setInt(4,defaultUses);
            p.setDate(5,Date.valueOf(exp));
            p.executeUpdate();
        }

        return id;
    }


    public void adjustPassUses(int pid,int uses) throws SQLException {
        String sql="update mandyjiang.SkiPass SET remaining_uses=? where pass_id=?";
        try (PreparedStatement p =dbconn.prepareStatement(sql)) {
            p.setInt(1,uses);
            p.setInt(2,pid);
            int updated=p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No ski pass exist with ID " +pid);
            }
        
        }
    }

    public boolean deletePass(int pid) throws SQLException {
        // 1. Check that the pass exists and is expired/used up
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
    
        // 2. Archive / delete 
        String archiveSql="""
            INSERT INTO mandyjiang.SkiPass_Archive(
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
    public int addRentalRecord(int skiPassID, int equipmentID) throw SQLException{
	// First thing we need to do is determine if the given ski pass id is actually a valid
	// active ski pass the foreign key constraint on equipmentID will take care of that check.
	checkSkiPassValid = "select count(*) as pass_id_cnt from mandyjiang.SkiPass where pass_id=%d";
	checkSkiPassValid = String.format(checkSkiPassValid,skiPassID);
	ResultSet res = myStmt.executeQuery(checkSkiPassValid);

        // Get the pass_id_cnt value.
	int isSkiPassActive = 0;
        if(res!=null) {
        	if(res.next()) {isSkiPassActive=res.getInt("pass_id_cnt");}
		else { return 1; } 
        } else {
		return 1; // If there is no result then the pass id is not valid return a return code of an error.
        }
 
        // Now actually check that there was a entrie with the proposed ski pass id.
	if(isSkiPassActive != 1) {return 1;}

        // Now that we have verified that the skiPassId is valid we can actually attempt to insert the new record.
    }

    public int deleteRentalRecord(int rentalID) {
    }

    public int addEquipmentRecord(String type, int size, String name) {
    }

    public int deleteEquipmentRecord(int equipmentID) {
    }

    // equipment,  equipment rental, lesson purchase, queries (maybe implement in other files?)

    // Lesson + Lesson Purchase
    public int addLessonPurchase(int mid, int lid, int totalSessions, int remaining) throws SQLException {
        int id = getNextId("LessonPurchase", "jeffreylayton");
        String sql = "insert into jeffreylayton.LessonPurchase(order_id, member_id, lesson_id, total_sessions, remaining_sessions) values (?, ?, ?, ?, ?)"; 
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, id);
            p.setInt(2, mid);
            p.setInt(3, lid);
            p.setInt(4, totalSessions);
            p.setInt(5, remaining);
            p.executeUpdate();
        }

        return id;
    }

    public void adjustLessonPurchase(int oid, int remaining) throws SQLException {
        String sql = "update jeffreylayton.LessonPurchase set remaining_sessions=? where order_id=?";
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, remaining);
            p.setInt(2, oid);
            int updated = p.executeUpdate();
            if (updated == 0){
                throw new SQLException("No order with ID " + oid);
            }
        }
    }
}
