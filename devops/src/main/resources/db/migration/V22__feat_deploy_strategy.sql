CREATE TABLE `devops_application_deploy_settings`
(
    `id`    varchar(50) NOT NULL,
    `name`  varchar(30) DEFAULT NULL,
    `date`  date        DEFAULT NULL,
    `start` time        DEFAULT NULL,
    `end`   time        DEFAULT NULL,
    `order_num` int(10)     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

insert into `devops_application_deploy_settings` ( `id`, `name`,`date`,`start`, `end`,`order_num`) values ('1efb0baf-c415-446a-91ce-42636f9c1fd7','weekday.MONDAY',null,TIME('00:00:00'),TIME('00:00:00'),1);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('83134e52-0c04-4b25-98a3-ca822789a6b4','weekday.TUESDAY',null,TIME('00:00:00'),TIME('00:00:00'),2);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('79fe887a-3b41-4309-a18b-4317fdb624e7','weekday.WEDNESDAY',null,TIME('00:00:00'),TIME('00:00:00'),3);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('2e1f4667-5edb-4695-b425-b83e42e797bf','weekday.THURSDAY',null,TIME('00:00:00'),TIME('00:00:00'),4);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('526b0f5f-3e63-4613-8e1d-e2c596cf0a9d','weekday.FRIDAY',null,TIME('00:00:00'),TIME('00:00:00'),5);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('1b857a06c-edca-47ef-b78d-2ae02b041905','weekday.SATURDAY',null,TIME('00:00:00'),TIME('00:00:00'),6);
insert into `devops_application_deploy_settings` ( `id`, `name`,`date`, `start`, `end`,`order_num`) values ('632147d2-ca9e-412c-aca7-2c87427369a9','weekday.SUNDAY',null,TIME('00:00:00'),TIME('00:00:00'),7);
