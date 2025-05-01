DROP TABLE ascherer.Property;
DROP SEQUENCE  ascherer.PROPERTY_SEQ;

CREATE TABLE ascherer.Property(
    propertyID INT PRIMARY KEY,
    property_type VARCHAR2(25) CHECK(property_type IN ('lodge','gift shop','rental center','visitor center','ski school','free lot','paid lot')), 
    daily_income INT NOT NULL -- free lot will have income as 0
);

create sequence ascherer.PROPERTY_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOMAXVALUE;

GRANT SELECT, INSERT, DELETE ON ascherer.Property TO PUBLIC;
GRANT SELECT ON ascherer.PROPERTY_SEQ TO PUBLIC;

INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'lodge', 100);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'lodge', 2000);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'gift shop', 250);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'gift shop', 750);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'gift shop', 3000);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'rental center', 5000);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'visitor center', 10000);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'ski school', 250);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'free lot', 0);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'free lot', 0);
INSERT INTO ascherer.Property(propertyID, property_type, daily_income) VALUES (ascherer.PROPERTY_SEQ.NEXTVAL, 'paid lot', 1000);