SET SQL_MODE='ALLOW_INVALID_DATES';
create table NEWS
(
    ID                  bigint  primary key auto_increment,
    TITLE               VARCHAR(100) ,
    SRC                 VARCHAR(1000),
    BODY                text,
    CREATE_DATE timestamp,
    MODIFY_DATE timestamp
)default charset=utf8mb4;

create table LINKS_PROCESSED(
    link varchar(1000)
)default charset=utf8mb4;;
create table LINKS_TO_BE_PROCESSED(
    link varchar(1000)
)default charset=utf8mb4;;
