#!/bin/bash

hosts=("192.168.204.89" "192.168.204.91" "192.168.204.101")
port="3306"
user="root"
pass=""
dbNum=10

for hostname in ${hosts[*]}
do
id=0
while [ "$id" != "$dbNum" ]
do
dbname="ngmdb"$id
create_db_sql="create database if not exists $dbname"
mysql -h$hostname -P$port  -u$user  -e "$create_db_sql"                             
id=$(($id+1));
if [ $id -gt $dbNum ];then
  break
fi
done
echo "Finished create db on $hostname"
done
