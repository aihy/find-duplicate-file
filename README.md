# find-duplicate-file

## usage

```bash
CORE_NUM=3 ALL_FILE_PATH=/media/data0/snapshot.txt java -jar md5-0.0.1-SNAPSHOT.jar
```

## DDL

```sql
-- auto-generated definition
create table file_md5
(
    id           serial
        constraint file_md5_pk
            primary key,
    gmt_create   timestamp(0) default CURRENT_TIMESTAMP not null,
    gmt_modified timestamp(0) default CURRENT_TIMESTAMP not null,
    path         text,
    md5          text,
    size         bigint
);

alter table file_md5
    owner to postgres;

create unique index file_md5_id_uindex
    on file_md5 (id);

create index idx_md5
    on file_md5 (md5);

create index idx_path
    on file_md5 (path);

```
