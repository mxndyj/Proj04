-- Firs call drop on the tables so that if this has to be re-ran than we can reset everything.
DROP TABLE tylergarfield.Rental;
DROP TABLE tylergarfield.Equipment;

-- Second we will create the needed tables for the rental and gear info.
create table tylergarfield.Rental (
	rentalID INT primary key,
	skiPassID INT NOT NULL,
	equipmentID INT NOT NULL,
	rentalTime TIMESTAMP DEFAULT SYSTIMESTAMP,
	returnStatus NUMBER(1,0) NOT NULL,
	FOREIGN KEY(equipmentID) REFERENCES tylergarfield.Equipment(equipmentID)
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


-- Next is the equipment relation.
create table tylergarfield.Equipment (
	equipmentID INT primary key,
	equip_type INT varchar2(25) CHECK(equip_type IN ('boot','pole','snowboard','alpine ski','protective gear')),
	size INT NOT NULL, -- Will need to add a check on the use side to only allow records with valid sizes to be
				-- inserted. I feel like trying to do a constraint with a bunch of ifs in here would
				-- be way harder.
	name varchar2(255) NOT NULL
);

-- Now create the sequences that will be the unique artificial rentalID and equipmentID's
create sequence tylergarfield.RENTAL_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;
create sequence tylergarfield.EQUIPMENT_SEQ START WITH 1 INCREMENT BY 1 NOCHACE NOMAXVALUE;

-- Finally grant all access to both tables to the public.
GRANT SELECT, INSERT, DELETE ON tylergarfield.Rental TO PUBLIC;
GRANT SELECT, INSERT, DELETE ON tylergarfield.Equipment TO PUBLIC;
