/*
Database data used for ZORM tests.
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

create database if not exists `zorm`;

USE `zorm`;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

/*Data for the table `item` */

insert  into `item`(`id`,`name`,`rating`,`active`,`author_id`) values (1,'item1',2,1,'john'),(2,'item2',3,0,NULL),(3,'item3',4,0,NULL),(4,'item4',4,0,NULL),(1003,'x',5,1,NULL);

/*Data for the table `user` */

insert  into `user`(`id`,`name`) values ('alice','Alice From Wonderland'),('john','John Doe');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
