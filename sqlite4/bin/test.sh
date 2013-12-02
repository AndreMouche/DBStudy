#!/bin/bash
#build source file
srcDir="../src"
insertSrc=$srcDir"/write.cpp"
selectSrc=$srcDir"/selectAndSplit.cpp"
g++ $insertSrc -lsqlite4 -lpthread -o write
g++ $selectSrc -lsqlite4 -lpthread -o splitter

cur=$(date +%Y%m%d-%T)
echo $cur
start=10
mu=10
rootDir="test"`date +%s`
dbDir="$rootDir/db"
dataDir="$rootDir/data"
logDir="$rootDir/log"
mkdir -p $dbDir | mkdir -p $dataDir | mkdir -p $logDir | echo "create dirs ok"

for i in {1..2}
do
   echo $i"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" 
   ((start=$start*$mu))  
   echo "*******************************************start $start*****************************************************" 
   startT=$(date +%s.%N)
  # echo "start ",$(date +%s.%N)
   dbFile=$dbDir"/student"$i
   logFile=$logDir"/write$i.log"
   ./write $dbFile $start > $logFile
  # echo "end insert",$(date +%s.%N)
   midT=$(date +%s.%N)
   curDataDir=$dataDir"/db$i"
   ./splitter $dbFile 1 $curDataDir$> $logFile
   #echo "finished $i,$start"
   #echo $(date +%Y%m%d-%T), $(date +%s.%N)
   endT=$(date +%s.%N)
  # echo $startT, $midT,$endT
   echo "insert:$start $midT-$startT"
   echo "select:$start $endT-$midT"

   echo "*******************************************end $start*****************************************************" 
done
echo "exit"
