drop table mandyjiang.Entry CASCADE CONSTRAINTS;
drop table mandyjiang.LiftTrail CASCADE CONSTRAINTS;
drop table mandyjiang.Lift CASCADE CONSTRAINTS;
drop table mandyjiang.Trail CASCADE CONSTRAINTS;
drop table mandyjiang.SkiPass_Archive;
drop table mandyjiang.SkiPass CASCADE CONSTRAINTS;
drop table mandyjiang.PassType;
drop table mandyjiang.Member CASCADE CONSTRAINTS;
drop sequence mandyjiang.MEMBER_SEQ;
drop sequence mandyjiang.SKIPASS_SEQ;
drop sequence mandyjiang.SKIPASS_ARCHIVE_SEQ;

create sequence mandyjiang.MEMBER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
create sequence mandyjiang.SKIPASS_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
create sequence mandyjiang.SKIPASS_ARCHIVE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

create table mandyjiang.Member (
    member_id       INT PRIMARY KEY,
    name        varchar2(100) NOT NULL,
    phone       varchar2(20),
    email       varchar2(100) UNIQUE,
    date_of_birth   DATE,
    emergency_contact varchar2(100)
);


CREATE TABLE mandyjiang.PassType (
  type       VARCHAR2(20) PRIMARY KEY CHECK (type IN ('1-DAY','2-DAY','4-DAY','SEASON')),
  total_uses INT NOT NULL,
  price      NUMBER(8,2) NOT NULL
);

create table mandyjiang.SkiPass (
    pass_id         INT PRIMARY KEY,
    member_id       INT,
    type            VARCHAR2(20) NOT NULL references mandyjiang.PassType(type),
    remaining_uses  INT NOT NULL,
    purchase_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    expiration_date DATE NOT NULL,
    FOREIGN KEY(member_id) references mandyjiang.Member(member_id) ON DELETE CASCADE
);

create table mandyjiang.SkiPass_Archive (
    SParchive_id    INT,
    pass_id         INT NOT NULL,
    member_id       INT,
    type            VARCHAR2(20) NOT NULL references mandyjiang.PassType(type),
    remaining_uses  INT,
    purchase_time   TIMESTAMP,
    expiration_date DATE,
    archived_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY(member_id) references Member(member_id) ON DELETE CASCADE,
    PRIMARY KEY(SParchive_id)
);

create table mandyjiang.Trail (
    trail_name     varchar2(100) PRIMARY KEY,
    start_location varchar2(100),
    end_location   varchar2(100),
    status      varchar2(10) CHECK (status IN ('OPEN','CLOSED')),
    difficulty  varchar2(20) CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','EXPERT')),
    category    varchar2(50) CHECK (category IN ('GROOMED','PARK','MOGULS','GLADE'))
);

create table mandyjiang.Lift (
    lift_name varchar2(100) PRIMARY KEY,
    ability   varchar2(20) CHECK (ability IN ('BEGINNER','INTERMEDIATE','EXPERT')),
    open_time TIMESTAMP,
    close_time TIMESTAMP,
    status    varchar2(10) CHECK (status IN ('OPEN','CLOSED'))
);

create table mandyjiang.LiftTrail (
    lift_name  varchar2(100),
    trail_name varchar2(100),
    PRIMARY KEY(lift_name,trail_name),
    FOREIGN KEY(lift_name) references mandyjiang.Lift(lift_name),
    FOREIGN KEY(trail_name) references mandyjiang.Trail(trail_name)
);

create table mandyjiang.Entry (
    lift_name   varchar2(100),
    pass_id     INT,
    entrance_time TIMESTAMP,
    PRIMARY KEY(lift_name,pass_id,entrance_time),
    FOREIGN KEY(lift_name) references mandyjiang.Lift(lift_name),
    FOREIGN KEY(pass_id)   references mandyjiang.SkiPass(pass_id) ON DELETE CASCADE
);

GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.Entry TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.LiftTrail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.Lift TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.Trail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.SkiPass_Archive TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.SkiPass TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE,REFERENCES ON mandyjiang.Member TO PUBLIC;


GRANT SELECT ON mandyjiang.MEMBER_SEQ TO PUBLIC;
GRANT SELECT ON mandyjiang.SKIPASS_SEQ TO PUBLIC;
GRANT SELECT ON mandyjiang.SKIPASS_ARCHIVE_SEQ TO PUBLIC;
--skipass and members
insert into mandyjiang.PassType(type, total_uses, price) VALUES ('1-DAY',  10, 75.00);
insert into mandyjiang.PassType(type, total_uses, price) VALUES ('2-DAY',  20, 180.00);
insert into mandyjiang.PassType(type, total_uses, price)VALUES ('4-DAY',  40, 350.00);
insert into mandyjiang.PassType(type, total_uses, price) VALUES ('SEASON',100, 500.00);

insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Alice Dude','520-555-0101','alice@demo.com',DATE'1985-03-12','911');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Bob Dude','520-555-0202','bob@demo.com',DATE'1990-07-25','505-505-505');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Apple Evans','520-555-0303','apple@demo.com',DATE'1978-11-02','444-444-444');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Dan Evans','520-555-0404','dan@demo.com',DATE'2000-01-18','444-444-333');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Charlie Brown','520-555-0505','cbrowny@demo.com',DATE'1992-04-22','520-555-9999');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'James Charlese','520-555-0606','james.char@demo.com',DATE'1987-09-10','520-555-8888');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Anne Boelynn','520-555-0707','anne.boel@demo.com',DATE'1995-12-05','520-555-7777');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Tofu Brown','520-555-0808','tfu.brown@demo.com',DATE'1983-02-17','520-555-6666');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Goofy Doose','520-555-1010','goof.doof@demo.com',DATE'1978-11-18','520-555-0001');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Donald Buck','520-555-1111','donald.buck@demo.com',DATE'1975-06-09','520-555-0002');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Okla Homa','520-555-1212','Okla.Homa@demo.com',DATE'1980-04-01','520-555-0003');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Daffy Drip','520-555-1313','daffy.drip@demo.com',DATE'1983-12-14','520-555-0004');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Scooby Dew','520-555-1414','scooby.dew@demo.com',DATE'1992-09-13','520-555-0005');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Elmer Red','520-555-1515','elmer.red@demo.com',DATE'1972-03-31','520-555-0006');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Mickey Mooser','520-555-2701','mickey.mooser@demo.com',DATE'1978-10-18','520-555-9001');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Minnie Mooser','520-555-2702','minnie.mooser@demo.com',DATE'1980-12-04','520-555-9002');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Homer simpleton','520-555-2703','homer.ski@demo.com',DATE'1956-05-12','520-555-9003');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Marge Simpleton','520-555-2704','marge.simpleton@demo.com',DATE'1957-07-01','520-555-9004');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Bart simpleton','520-555-2705','bart.sim@demo.com',DATE'2005-02-23','520-555-9005');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Lisa Carver','520-555-2706','lisa.carver@demo.com',DATE'2008-05-09','520-555-9006');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Tom Toboggan','520-555-2707','tom.toboggan@demo.com',DATE'1990-02-10','520-555-9007');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Jerry Mouse','520-555-2708','jerry.mouse@demo.com',DATE'1992-06-15','520-555-9008');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'SpongeBob SquareSlope','520-555-2709','spongebob.squareslope@demo.com',DATE'1999-07-14','520-555-9009');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) values (mandyjiang.MEMBER_SEQ.NEXTVAL,'Patrick Starhead','520-555-2710','patrick.starhead@demo.com',DATE'1998-08-17','520-555-9010');


insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,1,'1-DAY',0,SYSTIMESTAMP,DATE'2024-05-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,2,'2-DAY',0,SYSTIMESTAMP,DATE'2024-05-20');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,3,'4-DAY',0,SYSTIMESTAMP,DATE'2024-04-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,5,'SEASON',0,SYSTIMESTAMP,DATE'2024-04-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,6,'1-DAY',0,SYSTIMESTAMP,DATE'2024-06-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,7,'2-DAY',0,SYSTIMESTAMP,DATE'2024-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,8,'4-DAY',0,SYSTIMESTAMP,DATE'2024-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,9,'1-DAY',0,SYSTIMESTAMP,DATE'2024-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,10,'1-DAY',0,SYSTIMESTAMP,DATE'2024-05-10');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,11,'2-DAY',20,SYSTIMESTAMP,DATE'2025-06-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,12,'4-DAY',40,SYSTIMESTAMP,DATE'2025-06-10');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,13,'SEASON',100,SYSTIMESTAMP,DATE'2026-04-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,14,'1-DAY',10,SYSTIMESTAMP,DATE'2025-05-11');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,15,'2-DAY',20,SYSTIMESTAMP,DATE'2025-06-02');


--ski pass archive and more new skipasses
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 1;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 2;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 3;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 4;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 5;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 6;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 7;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 8;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 9;
insert into mandyjiang.SkiPass_Archive (SParchive_id, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date) SELECT mandyjiang.SKIPASS_ARCHIVE_SEQ.NEXTVAL, pass_id, member_id, type, remaining_uses, purchase_time, expiration_date FROM mandyjiang.SkiPass WHERE pass_id = 10;
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,1,'1-DAY',10,SYSTIMESTAMP,DATE'2025-05-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,2,'2-DAY',18,SYSTIMESTAMP,DATE'2024-05-20');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,3,'4-DAY',20,SYSTIMESTAMP,DATE'2024-04-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,5,'SEASON',10,SYSTIMESTAMP,DATE'2025-04-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,6,'1-DAY',2,SYSTIMESTAMP,DATE'2025-06-01');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,7,'2-DAY',2,SYSTIMESTAMP,DATE'2024-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,8,'4-DAY',23,SYSTIMESTAMP,DATE'2025-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,9,'1-DAY',10,SYSTIMESTAMP,DATE'2025-05-03');
insert into mandyjiang.SkiPass(pass_id,member_id,type,remaining_uses,purchase_time,expiration_date) values (mandyjiang.SKIPASS_SEQ.NEXTVAL,10,'1-DAY',10,SYSTIMESTAMP,DATE'2025-05-10');


--trails 
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) VALUES ('Rabbit Road','Base','Middle','OPEN','BEGINNER','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Town Road','Middle','Base','OPEN','EXPERT','MOGULS');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('First Road','Village','Park Area','CLOSED','INTERMEDIATE','PARK');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Happy Path','Summit','Glade Area','OPEN','INTERMEDIATE','GLADE');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Teen Zone','Mid','Base','CLOSED','INTERMEDIATE','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Beginners Hill','Base','Lower Base','OPEN','BEGINNER','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Advanced Hill','Summit','Base','OPEN','EXPERT','MOGULS');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Toodledo Hill','Summit','Base','OPEN','INTERMEDIATE','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Campus Crossing','Base','Lower Base','OPEN','BEGINNER','GLADE');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Marshmellow Delight','Summit','Village','OPEN','INTERMEDIATE','PARK');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Club House','Middle','Base','CLOSED','EXPERT','MOGULS');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Total Wipeout','Summit','Base','OPEN','INTERMEDIATE','PARK');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Banana Peel','Base','Mid','OPEN','BEGINNER','MOGULS');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Penguin Shuffle','Village','Park Area','OPEN','BEGINNER','PARK');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Squirrel Run','Base','Lower Base','OPEN','INTERMEDIATE','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Loopy Loop','Village','Base','CLOSED','INTERMEDIATE','GROOMED');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Fun Blvd','Summit','Base','OPEN','EXPERT','MOGULS');
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) values ('Chili Pill','Mid','Lower Base','OPEN','BEGINNER','GROOMED');
--Lifts
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) VALUES ('SUNRISE EXPRESS','BEGINNER',TIMESTAMP'2025-04-01 08:00:00',TIMESTAMP'2025-04-01 17:00:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('SUNSET EXPRESS','BEGINNER',TIMESTAMP'2025-05-01 09:00:00',TIMESTAMP'2025-05-01 17:00:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('BUNNY LIFT','BEGINNER',TIMESTAMP'2025-05-01 08:00:00',TIMESTAMP'2025-05-01 17:00:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('SUMMIT LIFT','INTERMEDIATE',TIMESTAMP'2025-05-01 07:30:00',TIMESTAMP'2025-05-01 17:00:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('TOODELDO TOW','INTERMEDIATE',TIMESTAMP'2025-05-01 08:15:00',TIMESTAMP'2025-05-01 17:15:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('SKYHIGH LIFT','EXPERT',TIMESTAMP'2025-05-01 08:45:00',TIMESTAMP'2025-05-01 17:15:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('TOWING COMPANY','EXPERT',TIMESTAMP'2025-05-01 09:00:00',TIMESTAMP'2025-05-01 16:30:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('SKYSCREAM LIFT','EXPERT',TIMESTAMP'2025-05-01 09:00:00',TIMESTAMP'2025-05-01 16:30:00','CLOSED');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('JOES LIFT','BEGINNER',TIMESTAMP'2025-05-01 08:10:00',TIMESTAMP'2025-05-01 17:00:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('REINDEER MOVER','BEGINNER',TIMESTAMP'2025-05-01 08:20:00',TIMESTAMP'2025-05-01 17:05:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('PENGUIN TRANSPORT','INTERMEDIATE',TIMESTAMP'2025-05-01 08:40:00',TIMESTAMP'2025-05-01 17:15:00','OPEN');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) values ('YETI BOWLS','EXPERT',TIMESTAMP'2025-05-01 09:00:00',TIMESTAMP'2025-05-01 17:25:00','OPEN');


--Connecting lifts and trails
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SUNRISE EXPRESS','Marshmellow Delight');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SUNRISE EXPRESS','Rabbit Road');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SUNSET EXPRESS','Beginners Hill');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('BUNNY LIFT','Campus Crossing');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SUMMIT LIFT','Happy Path');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('TOODELDO TOW','Toodledo Hill');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SKYHIGH LIFT','Town Road');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('TOWING COMPANY','Advanced Hill');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('SKYSCREAM LIFT','Club House');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('JOES LIFT','Chili Pill');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('REINDEER MOVER','Squirrel Run');
insert into mandyjiang.LiftTrail(lift_name,trail_name) values ('PENGUIN TRANSPORT','Penguin Shuffle');



--entries
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',1,SYSTIMESTAMP - INTERVAL '3' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',2,SYSTIMESTAMP - INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',3,SYSTIMESTAMP - INTERVAL '1' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SUNSET EXPRESS',4,SYSTIMESTAMP - INTERVAL '5' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('BUNNY LIFT',5,SYSTIMESTAMP -  INTERVAL '3' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SUMMIT LIFT',6,SYSTIMESTAMP - INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('TOODELDO TOW',7,SYSTIMESTAMP - INTERVAL '1' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SKYHIGH LIFT',8,SYSTIMESTAMP - INTERVAL '6' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('TOWING COMPANY',9,SYSTIMESTAMP - INTERVAL '4' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SKYSCREAM LIFT',10,SYSTIMESTAMP -INTERVAL '3' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('JOES LIFT',11,SYSTIMESTAMP - INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('REINDEER MOVER',12,SYSTIMESTAMP - INTERVAL '1' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('PENGUIN TRANSPORT',13,SYSTIMESTAMP - INTERVAL '30' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('YETI BOWLS',14,SYSTIMESTAMP -INTERVAL '45' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SUNRISE EXPRESS',1,SYSTIMESTAMP- INTERVAL '4' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SUNSET EXPRESS',2,SYSTIMESTAMP- INTERVAL '5' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('BUNNY LIFT',3,SYSTIMESTAMP - INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SUMMIT LIFT',4,SYSTIMESTAMP - INTERVAL '3' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('TOODELDO TOW',5,SYSTIMESTAMP - INTERVAL '90' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SKYHIGH LIFT',6,SYSTIMESTAMP -INTERVAL '30' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('TOWING COMPANY',7,SYSTIMESTAMP - INTERVAL '4' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('SKYSCREAM LIFT',8,SYSTIMESTAMP - INTERVAL '6' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('JOES LIFT',9,SYSTIMESTAMP -INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('REINDEER MOVER',10,SYSTIMESTAMP -INTERVAL '45' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('PENGUIN TRANSPORT',11,SYSTIMESTAMP - INTERVAL '20' MINUTE);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) values ('YETI BOWLS',12,SYSTIMESTAMP - INTERVAL '3' HOUR);




commit;

