create table NEWS
(
    ID                  bigint  primary key auto_increment,
    TITLE               VARCHAR(100) ,
    SRC                 VARCHAR(2000),
    BODY                text,
    CREATE_DATE         VARCHAR(45),
    MODIFY_DATE         VARCHAR(45)
);
create table LINKS_PROCESSED(
    link varchar(1000)
);
create table LINKS_TO_BE_PROCESSED(
    link varchar(1000)
);
