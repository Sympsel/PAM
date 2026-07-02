## 创建数据库

```mysql
create database if not exists pam_db character set utf8mb4 collate utf8mb4_unicode_ci;

use pam_db;
```

## 创建表

* 用户表 - users

```mysql
create table if not exists users
(
    id            varchar(36) primary key comment '用户UUID',
    username      varchar(50) unique not null comment '用户名',
    password_hash varchar(100)       not null comment '加密密码',
    permission    enum ('ADMIN', 'NORMAL') default 'NORMAL' comment '权限类型',
    real_name     varchar(50) comment '真实姓名',
    phone         varchar(11) comment '手机号',
    address       varchar(200)             default '未知' comment '地址',
    is_online     boolean                  default false comment '是否在线',
    create_time   bigint             not null comment '创建时间戳',
    index idx_username (username),
    index idx_permission (permission)
) engine = InnoDB
  default charset = utf8mb4 comment ='用户表';

describe users;
```

* 宠物表 - pets

```mysql
create table if not exists pets
(
    id              varchar(36) primary key comment '宠物UUID',
    name            varchar(50) unique not null comment '名字',
    specie          enum ('DOG', 'CAT', 'OTHER')             default 'OTHER' comment '物种',
    age             smallint                                 default 0 comment '年龄',
    description     varchar(200)                             default '无' comment '描述信息',
    adoption_status enum ('AVAILABLE', 'PENDING', 'ADOPTED') default 'AVAILABLE' comment '领养状态',
    create_time     bigint             not null comment '创建时间戳'
) engine = InnoDB
  default charset = utf8mb4 comment ='宠物表';

describe pets;
```

* 公告表 - announcements

```mysql
create table if not exists announcements
(
    id          varchar(36) primary key comment '公告uuid',
    sender_id    varchar(36)    not null comment '发布者uuid',
    title       varchar(100)   not null comment '标题',
    content     varchar(10000) not null comment '内容',
    create_time bigint         not null comment '发布时间戳',
    constraint fk_announcement_sender foreign key (sender_id) references users (id)
        on delete cascade
        on update cascade
) engine = innodb
  default charset = utf8mb4 comment = '公告表';
describe announcements;
```

* 领养申请表

```mysql
create table if not exists adoption_applications
(
    id            varchar(36) primary key comment '申请uuid',
    applicator_id varchar(36) not null comment '申请人uuid',
    pet_id        varchar(36) not null comment '宠物uuid',
    status        enum ('pending', 'approved', 'rejected') default 'pending' comment '申请状态',
    admin_id      varchar(36) comment '审核人uuid',
    comment       varchar(1000)                            default '无' comment '审核短评',
    review_time   bigint                                   default 0 comment '审核时间戳',
    constraint fk_application_applicator foreign key (applicator_id) references users (id)
        on delete cascade
        on update cascade,
    constraint fk_application_pet foreign key (pet_id) references pets (id)
        on delete cascade
        on update cascade,
    constraint fk_application_admin foreign key (admin_id) references users (id)
        on delete set null
        on update cascade
) engine = innodb
  default charset = utf8mb4 comment = '领养申请表';
describe adoption_applications;
```

## 添加管理员

```mysql
-- 需要使用管理员密码生成工具，得到密文密码，否则得到的账户无法登录
insert into users (id, username, password_hash, permission, real_name, phone, address, is_online, create_time)
values (uuid(), 'sympsel', '+I9dyZg11uquOSdd8q3QTTsTkKDwE0WWphUw3OTfU1E=', 'admin', '赵轩', '13500047303',
        '河南财经政法大学', FALSE, unix_timestamp() * 1000);

```

## 添加测试数据 - AI生成

* doc/pets.sql
* doc/announcements.sql
