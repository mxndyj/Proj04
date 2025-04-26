DROP TABLE mandyjiang.Entry CASCADE CONSTRAINTS;
DROP TABLE mandyjiang.LiftTrail CASCADE CONSTRAINTS;
DROP TABLE mandyjiang.Lift CASCADE CONSTRAINTS;
DROP TABLE mandyjiang.Trail CASCADE CONSTRAINTS;
DROP TABLE mandyjiang.SkiPass_Archive;
DROP TABLE mandyjiang.SkiPass CASCADE CONSTRAINTS;
DROP TABLE mandyjiang.Member CASCADE CONSTRAINTS;
DROP SEQUENCE mandyjiang.MEMBER_SEQ;
DROP SEQUENCE mandyjiang.SKIPASS_SEQ;
DROP SEQUENCE mandyjiang.SKIPASS_ARCHIVE_SEQ;

CREATE SEQUENCE mandyjiang.MEMBER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE mandyjiang.SKIPASS_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE mandyjiang.SKIPASS_ARCHIVE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE TABLE mandyjiang.Member (
    member_id           INT PRIMARY KEY,
    name                VARCHAR2(100) NOT NULL,
    phone               VARCHAR2(20),
    email               VARCHAR2(100) UNIQUE,
    date_of_birth     DATE,
    emergency_contact VARCHAR2(100)
);

CREATE TABLE mandyjiang.SkiPass (
    pass_id         INT PRIMARY KEY,
    member_id       INT,
    type            VARCHAR2(20) CHECK (type IN ('1-DAY','2-DAY','4-DAY','SEASON')),
    total_uses      INT NOT NULL,
    remaining_uses  INT NOT NULL,
    purchase_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    expiration_date DATE NOT NULL,
    price           NUMBER(8,2) NOT NULL,
    FOREIGN KEY(member_id) REFERENCES mandyjiang.Member(member_id) ON DELETE CASCADE
);

CREATE TABLE mandyjiang.SkiPass_Archive (
    SParchive_id      INT,
    pass_id         INT,
    member_id       INT,
    type            VARCHAR2(20),
    total_uses INT,
    remaining_uses  INT,
    purchase_time   TIMESTAMP,
    expiration_date DATE,
    price           NUMBER(8,2),
    archived_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY(member_id) REFERENCES Member(member_id) ON DELETE CASCADE,
    PRIMARY KEY(SParchive_id)
);

CREATE TABLE mandyjiang.Trail (
    trail_name     VARCHAR2(100) PRIMARY KEY,
    start_location VARCHAR2(100),
    end_location   VARCHAR2(100),
    status         VARCHAR2(10) CHECK (status IN ('OPEN','CLOSED')),
    difficulty     VARCHAR2(20) CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','EXPERT')),
    category       VARCHAR2(50) CHECK (category IN ('GROOMED','PARK','MOGULS','GLADE'))
);

CREATE TABLE mandyjiang.Lift (
    lift_name VARCHAR2(100) PRIMARY KEY,
    ability   VARCHAR2(20) CHECK (ability IN ('BEGINNER','INTERMEDIATE','EXPERT')),
    open_time TIMESTAMP,
    close_time TIMESTAMP,
    status    VARCHAR2(10) CHECK (status IN ('OPEN','CLOSED'))
);

CREATE TABLE mandyjiang.LiftTrail (
    lift_name  VARCHAR2(100),
    trail_name VARCHAR2(100),
    PRIMARY KEY(lift_name,trail_name),
    FOREIGN KEY(lift_name) REFERENCES mandyjiang.Lift(lift_name),
    FOREIGN KEY(trail_name) REFERENCES mandyjiang.Trail(trail_name)
);

CREATE TABLE mandyjiang.Entry (
    lift_name     VARCHAR2(100),
    pass_id       INT,
    entrance_time TIMESTAMP,
    PRIMARY KEY(lift_name,pass_id,entrance_time),
    FOREIGN KEY(lift_name) REFERENCES mandyjiang.Lift(lift_name),
    FOREIGN KEY(pass_id)   REFERENCES mandyjiang.SkiPass(pass_id) ON DELETE CASCADE
);

GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Entry TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.LiftTrail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Lift TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Trail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.SkiPass_Archive TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.SkiPass TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Member TO PUBLIC;

INSERT INTO mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Alice Dude','520-555-0101','alice@demo.com',DATE'1985-03-12','911');
INSERT INTO mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Bob Dude','520-555-0202','bob@demo.com',DATE'1990-07-25','505-505-505');
INSERT INTO mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Apple Evans','520-555-0303','apple@demo.com',DATE'1978-11-02','444-444-444');
INSERT INTO mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Dan Evans','520-555-0404','dan@demo.com',DATE'2000-01-18','444-444-333');
INSERT INTO mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,1,'1-DAY',10,10,SYSTIMESTAMP,DATE'2024-05-01',100);
INSERT INTO mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,2,'2-DAY',20,18,SYSTIMESTAMP,DATE'2025-05-20',200);
INSERT INTO mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,3,'4-DAY',40,1,SYSTIMESTAMP,DATE'2025-04-01',400);
INSERT INTO mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) VALUES ('Rabbit Run','Base','Middle','OPEN','BEGINNER','GROOMED');
INSERT INTO mandyjiang.Lift(lift_name,ability,open_time,close_time,status) VALUES ('SUNRISE EXPRESS','BEGINNER',TIMESTAMP'2025-04-01 08:00:00',TIMESTAMP'2025-04-01 17:00:00','OPEN');
INSERT INTO mandyjiang.LiftTrail(lift_name,trail_name) VALUES ('SUNRISE EXPRESS','Rabbit Run');
INSERT INTO mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',1,SYSTIMESTAMP - INTERVAL '3' HOUR);
INSERT INTO mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',2,SYSTIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',3,SYSTIMESTAMP - INTERVAL '1' HOUR);

commit;
