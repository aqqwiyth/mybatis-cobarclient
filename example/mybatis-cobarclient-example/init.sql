CREATE DATABASE if not EXISTS `mybatis`;
CREATE DATABASE if not EXISTS `mybatis_2`;
CREATE DATABASE if not EXISTS `mybatis_3`;

use mybatis;
CREATE TABLE if not EXISTS `user` (
  `user_nick` varchar(32) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`user_nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
use mybatis_2;
CREATE TABLE if not `trade` (
  `tid` bigint(32) NOT NULL,
  `title` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

use mybatis_3;
CREATE TABLE if not `trade` (
  `tid` bigint(32) NOT NULL,
  `title` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`tid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8