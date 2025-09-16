create table USERS
(
    ID          NUMBER(19) generated as identity
        primary key,
    EMAIL       VARCHAR2(255 char) not null,
    NAME        VARCHAR2(255 char) not null,
    PICTURE     VARCHAR2(255 char),
    PROVIDER    VARCHAR2(255 char) not null,
    PROVIDER_ID VARCHAR2(255 char) not null,
    unique (PROVIDER, PROVIDER_ID)
);