drop table mandyjiang.Entry CASCADE CONSTRAINTS;
drop table mandyjiang.LiftTrail CASCADE CONSTRAINTS;
drop table mandyjiang.Lift CASCADE CONSTRAINTS;
drop table mandyjiang.Trail CASCADE CONSTRAINTS;
drop table mandyjiang.SkiPass_Archive;
drop table mandyjiang.SkiPass CASCADE CONSTRAINTS;
drop table mandyjiang.Member CASCADE CONSTRAINTS;
drop sequence mandyjiang.MEMBER_SEQ;
drop sequence mandyjiang.SKIPASS_SEQ;
drop sequence mandyjiang.SKIPASS_ARCHIVE_SEQ;

create sequence mandyjiang.MEMBER_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
create sequence mandyjiang.SKIPASS_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
create sequence mandyjiang.SKIPASS_ARCHIVE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

create table mandyjiang.Member (
    member_id           INT PRIMARY KEY,
    name                varchar2(100) NOT NULL,
    phone               varchar2(20),
    email               varchar2(100) UNIQUE,
    date_of_birth     DATE,
    emergency_contact varchar2(100)
);

create table mandyjiang.SkiPass (
    pass_id         INT PRIMARY KEY,
    member_id       INT,
    type            varchar2(20) CHECK (type IN ('1-DAY','2-DAY','4-DAY','SEASON')),
    total_uses      INT NOT NULL,
    remaining_uses  INT NOT NULL,
    purchase_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    expiration_date DATE NOT NULL,
    price           NUMBER(8,2) NOT NULL,
    FOREIGN KEY(member_id) references mandyjiang.Member(member_id) ON DELETE CASCADE
);

create table mandyjiang.SkiPass_Archive (
    SParchive_id      INT,
    pass_id         INT,
    member_id       INT,
    type            varchar2(20),
    total_uses INT,
    remaining_uses  INT,
    purchase_time   TIMESTAMP,
    expiration_date DATE,
    price           NUMBER(8,2),
    archived_time   TIMESTAMP DEFAULT SYSTIMESTAMP,
    FOREIGN KEY(member_id) references Member(member_id) ON DELETE CASCADE,
    PRIMARY KEY(SParchive_id)
);

create table mandyjiang.Trail (
    trail_name     varchar2(100) PRIMARY KEY,
    start_location varchar2(100),
    end_location   varchar2(100),
    status         varchar2(10) CHECK (status IN ('OPEN','CLOSED')),
    difficulty     varchar2(20) CHECK (difficulty IN ('BEGINNER','INTERMEDIATE','EXPERT')),
    category       varchar2(50) CHECK (category IN ('GROOMED','PARK','MOGULS','GLADE'))
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
    lift_name     varchar2(100),
    pass_id       INT,
    entrance_time TIMESTAMP,
    PRIMARY KEY(lift_name,pass_id,entrance_time),
    FOREIGN KEY(lift_name) references mandyjiang.Lift(lift_name),
    FOREIGN KEY(pass_id)   references mandyjiang.SkiPass(pass_id) ON DELETE CASCADE
);

GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Entry TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.LiftTrail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Lift TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Trail TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.SkiPass_Archive TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.SkiPass TO PUBLIC;
GRANT SELECT, INSERT, UPDATE, DELETE ON mandyjiang.Member TO PUBLIC;

insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Alice Dude','520-555-0101','alice@demo.com',DATE'1985-03-12','911');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Bob Dude','520-555-0202','bob@demo.com',DATE'1990-07-25','505-505-505');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Apple Evans','520-555-0303','apple@demo.com',DATE'1978-11-02','444-444-444');
insert into mandyjiang.Member(member_id,name,phone,email,date_of_birth,emergency_contact) VALUES (mandyjiang.MEMBER_SEQ.NEXTVAL,'Dan Evans','520-555-0404','dan@demo.com',DATE'2000-01-18','444-444-333');
insert into mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,1,'1-DAY',10,10,SYSTIMESTAMP,DATE'2024-05-01',100);
insert into mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,2,'2-DAY',20,18,SYSTIMESTAMP,DATE'2025-05-20',200);
insert into mandyjiang.SkiPass(pass_id,member_id,type,total_uses,remaining_uses,purchase_time,expiration_date,price) VALUES (mandyjiang.SKIPASS_SEQ.NEXTVAL,3,'4-DAY',40,1,SYSTIMESTAMP,DATE'2025-04-01',400);
insert into mandyjiang.Trail(trail_name,start_location,end_location,status,difficulty,category) VALUES ('Rabbit Run','Base','Middle','OPEN','BEGINNER','GROOMED');
insert into mandyjiang.Lift(lift_name,ability,open_time,close_time,status) VALUES ('SUNRISE EXPRESS','BEGINNER',TIMESTAMP'2025-04-01 08:00:00',TIMESTAMP'2025-04-01 17:00:00','OPEN');
insert into mandyjiang.LiftTrail(lift_name,trail_name) VALUES ('SUNRISE EXPRESS','Rabbit Run');
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',1,SYSTIMESTAMP - INTERVAL '3' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',2,SYSTIMESTAMP - INTERVAL '2' HOUR);
insert into mandyjiang.Entry(lift_name,pass_id,entrance_time) VALUES ('SUNRISE EXPRESS',3,SYSTIMESTAMP - INTERVAL '1' HOUR);

commit;
