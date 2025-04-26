import java.sql.*;

public class DBController {
    private static final String URL = "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";
    private final Connection dbconn;

    public DBController(String user, String pass) throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        dbconn = DriverManager.getConnection(URL, user, pass);
    }

    public void close() {
        try { dbconn.close(); } catch (SQLException ignored) {}
    }

    //UNIQUE ID getter using sequences for each tablename
    private int getNextId(String table) throws SQLException {
        String seq = table.toUpperCase() + "_SEQ";
        String sql = "SELECT mandyjiang." + seq + ".NEXTVAL FROM DUAL";
        try (Statement s = dbconn.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    //  Member
    public int addMember(String name, String phone, String email, String dob, String emergency) throws SQLException {
        int id = getNextId("Member");
        String sql = "INSERT INTO mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, id);
            p.setString(2, name);
            p.setString(3, phone);
            p.setString(4, email);
            p.setDate(5, dob == null || dob.isBlank() ? null : Date.valueOf(dob));
            p.setString(6, emergency);
            p.executeUpdate();
            
        }
        return id;
    }

    public void updateMember(int id, String phone, String email, String emergency) throws SQLException {
        String sql = "UPDATE mandyjiang.Member SET phone=COALESCE(?,phone), email=COALESCE(?,email), emergency_contact=COALESCE(?,emergency_contact) WHERE member_id=?";
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setString(1, phone);
            p.setString(2, email);
            p.setString(3, emergency);
            p.setInt(4, id);
            int updated = p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No member with ID " + id);
            }
        
        }
    }

    public boolean deleteMember(int id) throws SQLException {
        // 1. Check for active ski passes
        String checkPass = 
          "SELECT 1 FROM mandyjiang.SkiPass " +
          "WHERE member_id=? AND (remaining_uses>0 OR expiration_date > SYSTIMESTAMP)";
        try (PreparedStatement cp = dbconn.prepareStatement(checkPass)) {
            cp.setInt(1, id);
            if (cp.executeQuery().next()) {
                throw new IllegalStateException("Cannot delete member: active ski passes exist.");
            }
        }
    
        // // 2. Check for open equipment rentals
        // String checkRental = "SELECT 1 FROM mandyjiang.Rental WHERE member_id=? AND return_status='OUT'";
        // try (PreparedStatement cr = dbconn.prepareStatement(checkRental)) {
        //     cr.setInt(1, id);
        //     if (cr.executeQuery().next()) {
        //         throw new IllegalStateException("Cannot delete member: open equipment rentals exist.");
        //     }
        // }
    
        // // 3. Check for unused lesson sessions
        // String checkLesson = "SELECT 1 FROM mandyjiang.LessonPurchase WHERE member_id=? AND remaining_sessions>0";
        // try (PreparedStatement cl = dbconn.prepareStatement(checkLesson)) {
        //     cl.setInt(1, id);
        //     if (cl.executeQuery().next()) {
        //         throw new IllegalStateException("Cannot delete member: unused lesson sessions exist.");
        //     }
        // }
    
    

        //  Delete member
        try (PreparedStatement dm = dbconn.prepareStatement(
                 "DELETE FROM mandyjiang.Member WHERE member_id=?"
             )) {
            dm.setInt(1, id);
            return dm.executeUpdate() == 1;
        }
    }

    //  Ski Pass
    public int addPass(int mid, String type, int total, String exp, double price) throws SQLException {
        int id = getNextId("SkiPass");
        String sql = "INSERT INTO mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES(?,?,?,?,?,SYSTIMESTAMP,?,?)";
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, id);
            p.setInt(2, mid);
            p.setString(3, type);
            p.setInt(4, total);
            p.setInt(5, total);
            p.setDate(6, Date.valueOf(exp));
            p.setDouble(7, price);
            p.executeUpdate();
        
        }
        return id;

    }


    public void adjustPassUses(int pid, int uses) throws SQLException {
        String sql = "UPDATE mandyjiang.SkiPass SET remaining_uses=? WHERE pass_id=?";
        try (PreparedStatement p = dbconn.prepareStatement(sql)) {
            p.setInt(1, uses);
            p.setInt(2, pid);
            int updated = p.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No ski pass with ID " + pid);
            }
        
        }
    }

    public boolean deletePass(int pid) throws SQLException {
        // 1. Check that the pass exists and is expired/unused
        try (PreparedStatement chk = dbconn.prepareStatement(
                 "SELECT remaining_uses, expiration_date "
               + "FROM mandyjiang.SkiPass WHERE pass_id = ?")) {
            chk.setInt(1, pid);
            try (ResultSet rs = chk.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Ski pass not found: " + pid);
                if (rs.getInt(1) > 0
                    || rs.getDate(2)
                         .after(new java.sql.Date(System.currentTimeMillis())))
                    throw new IllegalStateException(
                        "Cannot delete pass: still active or not expired."
                    );
            }
        }
    
        // 2. Archive & delete 
        String archiveSql = 
          "INSERT INTO mandyjiang.SkiPass_Archive(SPARCHIVE_ID,PASS_ID,MEMBER_ID,TYPE,TOTAL_USES,REMAINING_USES,"
        + "PURCHASE_TIME,EXPIRATION_DATE,PRICE,ARCHIVED_TIME) "
        + "SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL,"
        + "pass_id,member_id,type,total_uses,remaining_uses,"
        + "purchase_time,expiration_date,price,SYSTIMESTAMP "
        + "FROM mandyjiang.SkiPass WHERE pass_id = ?";
    
        String deleteSql = 
          "DELETE FROM mandyjiang.SkiPass WHERE pass_id = ?";
    
        try (PreparedStatement a = dbconn.prepareStatement(archiveSql);
             PreparedStatement d = dbconn.prepareStatement(deleteSql)) {
            a.setInt(1, pid); 
            a.executeUpdate();
            d.setInt(1, pid);
            return d.executeUpdate() == 1;
        }
    }

    //  Lift Entry 
    public int recordLiftEntry(int pid, String liftName) throws SQLException {
        liftName = liftName.toUpperCase();
        //  lift exists
        try (PreparedStatement lv = dbconn.prepareStatement(
                "SELECT 1 FROM mandyjiang.Lift WHERE lift_name=?")) {
            lv.setString(1, liftName);
            if (!lv.executeQuery().next()) throw new IllegalArgumentException("Lift does not exist.");
        }
        // deduct use
        try (PreparedStatement u = dbconn.prepareStatement(
                "UPDATE mandyjiang.SkiPass SET remaining_uses = remaining_uses - 1 " +
                "WHERE pass_id = ? AND remaining_uses > 0")) {
            u.setInt(1, pid);
            if (u.executeUpdate() == 0) throw new IllegalStateException("No uses left on pass.");
        }
        // insert into entry log
        try (PreparedStatement l = dbconn.prepareStatement(
                "INSERT INTO mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES(?,?,SYSTIMESTAMP)")) {
            l.setString(1, liftName); l.setInt(2, pid); l.executeUpdate();
        }
        // fetch remaining uses
        int left;
        try (PreparedStatement q = dbconn.prepareStatement(
                "SELECT remaining_uses FROM mandyjiang.SkiPass WHERE pass_id=?")) {
            q.setInt(1, pid);
            try (ResultSet rs = q.executeQuery()) { rs.next(); left = rs.getInt(1); }
        }
    
        return left;
    }


    // equipment,  equipment rental, lesson purchase, queries (maybe implement in other files?)
}
