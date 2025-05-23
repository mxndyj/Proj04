-- Firs call drop on the tables so that if this has to be re-ran than we can reset everything.
DROP TABLE tylergarfield.Rental;
DROP TABLE tylergarfield.Rental_Archive;
DROP TABLE tylergarfield.Equipment;
DROP TABLE tylergarfield.Equipment_Archive;
DROP SEQUENCE  tylergarfield.RENTAL_SEQ;
DROP SEQUENCE  tylergarfield.EQUIPMENT_SEQ;
DROP SEQUENCE  tylergarfield.RENTAL_ARCHIVE_SEQ;
DROP SEQUENCE  tylergarfield.EQUIPMENT_ARCHIVE_SEQ;

-- Next is the equipment relation.
create table tylergarfield.Equipment (
        equipmentID INT primary key,
        equip_type varchar2(25) CHECK(equip_type IN ('boot','pole','snowboard','alpine ski','helmet','goggle','glove')),
        equip_size NUMBER(2,1) NOT NULL, -- Will need to add a check on the use side to only allow records with valid sizes to be
                                -- inserted. I feel like trying to do a constraint with a bunch of ifs in here would
                                -- be way harder.
        name varchar2(255) NOT NULL
);

create table tylergarfield.Equipment_Archive (
        equipArchiveID INT primary key,
        equipmentID INT NOT NULL,
        equip_type varchar2(25) CHECK(equip_type IN ('boot','pole','snowboard','alpine ski','helmet','goggle','glove')),
        equip_size NUMBER(2,1) NOT NULL,
        name varchar2(255) NOT NULL,
        changeState NUMBER(1) NOT NULL CHECK(changeState IN(0,1,2)) -- same state values as rental relation archive.
);


-- Second we will create the needed tables for the rental and gear info.
create table tylergarfield.Rental (
	rentalID INT primary key,
	skiPassID INT NOT NULL,
	equipmentID INT NOT NULL,
	rentalTime TIMESTAMP DEFAULT SYSTIMESTAMP,
	returnStatus NUMBER(1) NOT NULL CHECK(returnStatus IN(1,0)),
<<<<<<< HEAD
=======
	FOREIGN KEY(skiPassID) REFERENCES mandyjiang.SkiPass(pass_id) on delete cascade,
>>>>>>> e14e7de125424b711ea975a368c2f678d13f4216
	FOREIGN KEY(equipmentID) REFERENCES tylergarfield.Equipment(equipmentID)
);

-- Next create the archive relation for Rental
create table tylergarfield.Rental_Archive (
	archRentalID INT primary key,
	rentalID INT NOT NULL,
        skiPassID INT NOT NULL,
        equipmentID INT NOT NULL,
        rentalTime TIMESTAMP DEFAULT SYSTIMESTAMP,
        returnStatus NUMBER(1) NOT NULL CHECK(returnStatus IN(1,0)),
	arhciveTime TIMESTAMP DEFAULT SYSTIMESTAMP,
	changeState NUMBER(1) NOT NULL CHECK(changeState IN(0,1,2)) -- 0 is added, 1 is updated, 2 is deleted.
);

-- Next create a trigger that will throw an exception if a rental is attempted to
-- to be added for which the skiPassID is not in the SkiPass relation. I am doing a trigger
-- here so that the skiPassID can point to an archieved ski pass.
-- Maybe it would be better to just do this check in the application side however. I just need the
-- skiPassID to be allowed to either refrence an active ski pass or an archived pass.

--create trigger check_passid_when_insert
--before insert on tylergarfield.Rental
--for each row
--DECLARE
--    num_matching INT;
--BEGIN
--    select count(*) into num_matching from mandyjiang.SkiPass where pass_id = :NEW.skiPassID;
--    if num_matching != 1 then
--        raise_application_error(-2000,'Must have a valid active ski pass for equipment rental')
--    end if;
--end;
--/




-- Now create the sequences that will be the unique artificial rentalID and equipmentID's
create sequence tylergarfield.RENTAL_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;
create sequence tylergarfield.EQUIPMENT_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;
create sequence tylergarfield.RENTAL_ARCHIVE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;
create sequence tylergarfield.EQUIPMENT_ARCHIVE_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;

-- Finally grant all access to both tables to the public.
GRANT SELECT, INSERT, DELETE ON tylergarfield.Rental TO PUBLIC;
GRANT SELECT, INSERT, DELETE ON tylergarfield.Equipment TO PUBLIC;
GRANT SELECT ON tylergarfield.RENTAL_SEQ TO PUBLIC;
GRANT SELECT ON tylergarfield.EQUIPMENT_SEQ TO PUBLIC;
GRANT SELECT ON tylergarfield.RENTAL_ARCHIVE_SEQ TO PUBLIC;
<<<<<<< HEAD
GRANT SELECT ON tylergarfield.EQUIPMENT_ARCHIVE_SEQ TO PUBLIC;
=======
GRANT SELECT ON tylergarfield.EQUIPMENT_ARCHIVE_SEQ TO PUBLIC;
>>>>>>> e14e7de125424b711ea975a368c2f678d13f4216
