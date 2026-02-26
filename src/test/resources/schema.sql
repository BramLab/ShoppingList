

CREATE TABLE IF NOT EXISTS storage (
                                       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       name       VARCHAR(255),
                                       remarks    VARCHAR(255)
);

create table IF NOT EXISTS food
(
    created_at datetime(6)  null,
    deleted_at datetime(6)  null comment 'Soft-delete indicator',
    id         bigint auto_increment primary key,
    updated_at datetime(6)  null,
    dtype      varchar(31)  not null,
    name       varchar(255) null,
    remarks    varchar(255) null,
    check (dtype in ('food_original'))
);

create table IF NOT EXISTS food_original
(
    best_before_end date   null,
    original_ml_g   double not null,
    remaining_ml_g  double not null,
    use_by          date   null,
    food_id         bigint not null primary key,
    constraint FK4a1tyhabf5gokqhlqgxtaomb8
        foreign key (food_id) references food (id)
);
