
create table if not exists NEWS
(
    ID                  bigint  primary key auto_increment,
    TITLE               VARCHAR(100) ,
    SRC                 VARCHAR(1000),
    BODY                text,
    CREATE_DATE datetime,
    MODIFY_DATE timestamp
)default charset=utf8mb4;

create table if not exists LINKS_PROCESSED(
    link varchar(1000)
)default charset=utf8mb4;;
create table if not exists LINKS_TO_BE_PROCESSED(
    link varchar(1000)
)default charset=utf8mb4;;
