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
    ethnicity varchar2(50) not null
);

create table jeffreylayton.Lesson (
    lesson_id int primary key,
    instructor_id int not null,
    certification_level Number(1),
    private Number(1) not null,
    time date not null,
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
    remaining_sessions int not null,
    foreign key(lesson_id) references jeffreylayton.Lesson(lesson_id)
);

grant select, insert, update, delete on jeffreylayton.Employee to public;
grant select, insert, update, delete on jeffreylayton.Lesson to public;
grant select, insert, update, delete on jeffreylayton.LessonPurchase to public;
grant select, insert, update, delete on jeffreylayton.LessonPurchase_Archive to public;

-- Employees
INSERT INTO jeffreylayton.employee (
  employee_id, position, start_date, name, age, salary, sex, ethnicity
) VALUES (1, 'Instructor', TO_DATE('2023-01-15','yyyy-mm-dd'), 'Alice Person', 29, 55000, 'f', 'asian');

INSERT INTO jeffreylayton.employee (employee_id, position, start_date, name, age, salary, sex, ethnicity)
VALUES (2, 'Instructor', TO_DATE('2022-06-01','yyyy-mm-dd'), 'Bob Person', 35, 60000, 'm', 'caucasian');

INSERT INTO jeffreylayton.employee ( employee_id, position, start_date, name, age, salary, sex, ethnicity)
VALUES (3, 'Person', TO_DATE('2024-03-12','yyyy-mm-dd'), 'Carla Person', 42, 65000, 'f', 'hispanic');

-- Lessons
INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, certification_level, private, time)
VALUES (10, 1, 2, 0, TO_TIMESTAMP('2025-05-05 09:00:00','yyyy-mm-dd hh24:mi:ss'));

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, certification_level, private, time)
VALUES (11, 2, 3, 1, TO_TIMESTAMP('2025-05-06 11:30:00','yyyy-mm-dd hh24:mi:ss'));

INSERT INTO jeffreylayton.Lesson (lesson_id, instructor_id, certification_level, private, time)
VALUES (12, 3, 1, 0, TO_TIMESTAMP('2025-05-07 14:00:00','yyyy-mm-dd hh24:mi:ss'));

-- LessonPurchases
INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (1001, 15, 10, 5, 5);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (1004, 15, 11, 4, 3);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (1005, 15, 12, 5, 3);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (1002, 14, 11, 10, 4);

INSERT INTO jeffreylayton.LessonPurchase (order_id, member_id, lesson_id, total_sessions, remaining_sessions)
VALUES (1003, 12, 12, 8, 8);

commit;
