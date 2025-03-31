-- Add all your SQL setup statements here. 

-- When we test your submission, you can assume that the following base
-- tables have been created and loaded with data.  However, before testing
-- your own code, you will need to create and populate them on your
-- Postgres instance
--
-- Do not alter the following tables' contents or schema in your code.


-- FLIGHTS(fid int primary key, 
--         month_id int REFERENCES MONTHS,        -- 1-12
--         day_of_month int,    -- 1-31 
--         day_of_week_id int REFERENCES WEEKDAYS,  -- 1-7, 1 = Monday, 2 = Tuesday, etc
--         carrier_id varchar(7) REFERENCES CARRIERS, 
--         flight_num int,
--         origin_city varchar(34), 
--         origin_state varchar(47), 
--         dest_city varchar(34), 
--         dest_state varchar(46), 
--         departure_delay int, -- in mins
--         taxi_out int,        -- in mins
--         arrival_delay int,   -- in mins
--         canceled int,        -- 1 means canceled
--         actual_time int,     -- in mins
--         distance int,        -- in miles
--         capacity int, 
--         price int            -- in $             
--         )

-- CARRIERS(cid varchar(7) primary key,
--          name varchar(83))

-- MONTHS(mid int primary key,
--        month varchar(9));	

-- WEEKDAYS(did int primary key,
--          day_of_week varchar(9));

-- CREATE TABLE CARRIERS (
--     cid VARCHAR(7) PRIMARY KEY,
--     name VARCHAR(83)
-- );

-- CREATE TABLE MONTHS (
--     mid INT PRIMARY KEY,
--     month VARCHAR(9)
-- );

-- CREATE TABLE WEEKDAYS (
--     did INT PRIMARY KEY,
--     day_of_week VARCHAR(9)
-- );

-- CREATE TABLE FLIGHTS (
--     fid INT PRIMARY KEY,
--     month_id INT REFERENCES MONTHS(mid),        -- 1-12
--     day_of_month INT,                           -- 1-31 
--     day_of_week_id INT REFERENCES WEEKDAYS(did),-- 1-7, 1 = Monday, 2 = Tuesday, etc
--     carrier_id VARCHAR(7) REFERENCES CARRIERS(cid), 
--     flight_num INT,
--     origin_city VARCHAR(34), 
--     origin_state VARCHAR(47), 
--     dest_city VARCHAR(34), 
--     dest_state VARCHAR(46), 
--     departure_delay INT,  -- in mins
--     taxi_out INT,         -- in mins
--     arrival_delay INT,    -- in mins
--     canceled INT,         -- 1 means canceled
--     actual_time INT,      -- in mins
--     distance INT,         -- in miles
--     capacity INT, 
--     price INT             -- in $
-- );

CREATE TABLE Users_yfeng33 (
    username TEXT PRIMARY KEY,
    password BYTEA,
    balance INT
);

CREATE TABLE Reservations_yfeng33 (
    res_id TEXT PRIMARY KEY NOT NULL,
    username TEXT NOT NULL REFERENCES Users_yfeng33(username),
    fl_id1 INT NOT NULL REFERENCES Flights(fid),
    fl_id2 INT NULL REFERENCES Flights(fid),
    paid INT NOT NULL, -- 0 for unpaid and 1 for paid
    day_of_month INT
);
