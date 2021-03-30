create table weather
(
    id          bigint generated by default as identity,
    city        varchar(255) not null,
    country     varchar(255) not null,
    description varchar(255) not null,
    updated_on  timestamp    not null,
    primary key (id)
);

create table users
(
    id      bigint generated by default as identity,
    api_key varchar(255)         not null,
    enabled boolean default true not null,
    primary key (id)
);

create table requests
(
    id           bigint generated by default as identity,
    requested_on timestamp not null,
    user_id      bigint    not null,
    primary key (id)
);

alter table users
    add constraint UK_API_KEY unique (api_key);
alter table requests
    add constraint FK_USER_ID_USER foreign key (user_id) references users;