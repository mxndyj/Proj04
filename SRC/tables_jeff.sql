drop table jeffreylayton.Employee cascade constraints;
drop table jeffreylayton.Lesson cascade constraints;
drop table jeffreylayton.LessonPurchase cascade constraints;
drop table jeffreylayton.LessonPurchase_Archive cascade constraints;
drop sequence jeffreylayton.EMPLOYEE_SEQ;
drop sequence jeffreylayton.LESSON_SEQ;
drop sequence jeffreylayton.LESSONPURCHASE_SEQ;
drop sequence jeffreylayton.LESSONPURCHASE_ARCHIVE_SEQ;

create sequence jeffreylayton.EMPLOYEE_SEQ start with 1 increment by 1 nocache nocycle;
create sequence jeffreylayton.LESSON_SEQ start with 1 increment by 1 nocache nocycle;
create sequence jeffreylayton.LESSONPURCHASE_SEQ start with 1 increment by 1 nocache nocycle;
create sequence jeffreylayton.LESSONPURCHASE_ARCHIVE_SEQ start with 1 increment by 1 nocache nocycle;

create table jeffreylayton.Employee (
    employee_id int primary key,
    position varchar2(100),
    start_date date not null,
    name varchar2(100) not null,
    age int not null,
    salary int not null,
    sex varchar2(1) not null,
    ethnicity varchar2(50) not null,
    certification_level Number(1) check (certification_level in (1, 2, 3) or certification_level is null)
);

create table jeffreylayton.Lesson (
    lesson_id int primary key,
    instructor_id int not null,
    private Number(1) not null check (private in (0, 1)),
    time varchar2(5) not null,
    foreign key(instructor_id) references jeffreylayton.Employee(employee_id)
);

create table jeffreylayton.LessonPurchase (
    order_id int primary key,
    member_id int not null,
    lesson_id int not null,
    total_sessions int not null,
    remaining_sessions int not null,
    foreign key(member_id) references mandyjiang.Member(member_id) on delete cascade,
    foreign key(lesson_id) references jeffreylayton.Lesson(lesson_id)
);

create table jeffreylayton.LessonPurchase_Archive (
    order_id int primary key,
    member_id int not null,
    lesson_id int not null,
    total_sessions int not null,
    foreign key(lesson_id) references jeffreylayton.Lesson(lesson_id)
);

grant select, insert, update, delete on jeffreylayton.Employee to public;
grant select, insert, update, delete on jeffreylayton.Lesson to public;
grant select, insert, update, delete on jeffreylayton.LessonPurchase to public;
grant select, insert, update, delete on jeffreylayton.LessonPurchase_Archive to public;
grant select on jeffreylayton.EMPLOYEE_SEQ to public;
grant select on jeffreylayton.LESSON_SEQ to public;
grant select on jeffreylayton.LESSONPURCHASE_SEQ to public;
grant select on jeffreylayton.LESSONPURCHASE_ARCHIVE_SEQ to public;

-- Employees
INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Ski Instructor', TO_DATE('2023-02-10','yyyy-mm-dd'), 'Grace Parker', 28, 57000, 'f', 'hispanic', 2);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Ski Instructor', TO_DATE('2022-05-20','yyyy-mm-dd'), 'Henry Liu', 33, 59000, 'm', 'asian', 3);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Ski Instructor', TO_DATE('2024-12-01','yyyy-mm-dd'), 'Isabella Moore', 24, 52000, 'f', 'black', 1);

INSERT INTO jeffreylayton.employee ( employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Ski Pass Administrator', TO_DATE('2024-12-15','yyyy-mm-dd'), 'James Patel', 39, 63000, 'm', 'south asian', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Parking Attendant', TO_DATE('2024-12-05','yyyy-mm-dd'), 'Kelly Nguyen', 26, 41000, 'f', 'asian', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Parking Attendant', TO_DATE('2024-10-19','yyyy-mm-dd'), 'Liam Carter', 31, 42000, 'm', 'caucasian', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Parking Attendant', TO_DATE('2025-01-08','yyyy-mm-dd'), 'Maya Thompson', 29, 41500, 'f', 'mixed', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Rental Technician', TO_DATE('2023-11-11','yyyy-mm-dd'), 'Nathan Reed', 34, 44000, 'm', 'native', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Lift Operator', TO_DATE('2022-09-30','yyyy-mm-dd'), 'Olivia Garcia', 41, 49000, 'f', 'hispanic', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Cashier', TO_DATE('2021-12-07','yyyy-mm-dd'), 'Paul Kim', 38, 39000, 'm', 'asian', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Cashier', TO_DATE('2021-11-20','yyyy-mm-dd'), 'Quinn Adams', 25, 36000, 'f', 'black', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Cashier', TO_DATE('2022-01-05','yyyy-mm-dd'), 'Ryan Smith', 22, 34000, 'm', 'caucasian', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Security Guard', TO_DATE('2021-10-15','yyyy-mm-dd'), 'Sophia Lopez', 46, 50000, 'f', 'hispanic', null);

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity, certification_level)
VALUES (jeffreylayton.EMPLOYEE_SEQ.NEXTVAL, 'Security Guard', TO_DATE('2022-02-28','yyyy-mm-dd'), 'Trevor Johnson', 52, 52000, 'm', 'black', null);

-- Lessons
INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 1, 0, '08:00');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 2, 1, '12:30');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 3, 0, '14:45');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 1, 1, '09:15');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 2, 0, '10:00');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 3, 1, '10:30');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 1, 0, '11:45');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 2, 1, '13:15');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 3, 0, '15:30');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 1, 1, '16:00');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 2, 0, '17:20');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 3, 1, '18:05');

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, private, time)
VALUES (jeffreylayton.LESSON_SEQ.NEXTVAL, 1, 0, '19:40');

-- LessonPurchases
INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 5, 1, 5, 5);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 5, 2, 4, 3);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 5, 1, 5, 3);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 2, 3, 10, 4);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 1, 3, 8, 8);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 3, 4, 6, 6);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 4, 5, 8, 8);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 1, 6, 10, 10);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 2, 7, 12, 9);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 3, 8, 4, 2);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 4, 9, 6, 6);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 5, 4, 8, 7);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 2, 5, 5, 5);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 1, 7, 7, 5);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (jeffreylayton.LESSONPURCHASE_SEQ.NEXTVAL, 3, 2, 3, 3);

commit;
