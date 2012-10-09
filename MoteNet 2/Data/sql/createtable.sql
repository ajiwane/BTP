CREATE TABLE users(
userid INT PRIMARY KEY,
username VARCHAR(30) ,
passwd VARCHAR(30),
dbname VARCHAR(30)
);

CREATE TABLE jobs(
jobid INT PRIMARY KEY,
jobname VARCHAR(30),
jobdescription VARCHAR(100)
);

CREATE TABLE jobowners(
userid INT references users(userid),
jobid INT references jobs(jobid),
PRIMARY KEY(userid, jobid)
);

CREATE TABLE classfiles(
jobid INT references jobs(jobid),
classfile VARCHAR(100),
PRIMARY KEY(jobid, classfile)
);

CREATE TABLE mainfiles(
jobid INT references jobs(jobid),
mainfile VARCHAR(100),
PRIMARY KEY(jobid, mainfile)
);

CREATE TABLE slots(
jobid INT references jobs(jobid),
sdate DATE,
stime TIME,
PRIMARY KEY(jobid, sdate,stime)
);

CREATE TABLE motes (
moteid INT PRIMARY KEY,
moteport VARCHAR(30),
condition INT,
ipaddress VARCHAR(30)
);

CREATE TABLE schedules (
moteid INT references motes,
jobid INT,
mainfile VARCHAR(100),
FOREIGN KEY (jobid,mainfile) REFERENCES mainfiles,
PRIMARY KEY(moteid, jobid, mainfile)
);

CREATE TABLE curid (
moteid INT,
jobid INT,
userid INT
);

