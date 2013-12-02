/**
 Split Table ET in database argv[1] to two files
 g++ splitter.cpp -lsqlite4 -lpthread -o splitter

*/

#include <string.h>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <stdlib.h>
using namespace std;
#include <sqlite4.h>
#include <time.h>

const char DataFilesPrefix[100] = "data";
char sql[100];
int selectNum = 0;
int totalNum = 0;

ofstream datafile;
char outFile[100];

static int callback(void *pArg, int nVal, sqlite4_value **apVal, const char **azCol){
    int i;
   /* for(i=0; i<nVal; i++){
        const char *z = sqlite4_value_text(apVal[i],0);
        printf("%s =%s,", azCol[i] , z ? z : "NULL");
    }
    */
    const char *key = sqlite4_value_text(apVal[0],0);
    const char *value = sqlite4_value_text(apVal[1],0);
    datafile << key << " " << value << endl;
    datafile << key;
    //printf("%s %s\n",key,value);
    selectNum ++;
    return 0;
}

static int get_totalNum(void *pArg, int nVal, sqlite4_value **apVal, const char **azCol){
    int i;
    for(i=0; i<nVal; i++){
        totalNum = sqlite4_value_int(apVal[i]);
        
        //const char *z = sqlite4_value_text(apVal[i],0);
        //printf("%s =%s,", azCol[i] , z ? z : "NULL");
    }
    printf("total %d \n",totalNum);
    return 0;
}


int main(int argc,char ** argv)
{
    if(argc != 4){
       printf("Usage:%s dbfile splitNum dataDir\n",argv[0]);
       exit(-1);
    }
    
    clock_t startT = clock();
    int SplitNum = atoi(argv[2]);
    char dataDir[100];
    strcpy(dataDir,argv[3]);
    printf("dataDir %s\n",dataDir);


    time_t cstart,cends;
    cstart = time(NULL);
	char * pErrMsg = 0;
	int ret = 0;
	sqlite4 * db = 0;
    int i;
    sqlite4_env *env = sqlite4_env_default();
	ret = sqlite4_open(env,argv[1], &db);
	if ( ret != SQLITE4_OK)
	{
		printf("Could not open database: %s", sqlite4_errmsg(db));
		exit(1);
	} 

    sprintf(sql,"select count(*) from ET");
    ret = sqlite4_exec(db,sql,get_totalNum,0);

    int eachLoop = totalNum/SplitNum;
    int offset = 0;
    double totalDuration = 0;   
    clock_t preT = clock();
    for(i=0;i<SplitNum;i++) {
       sprintf(outFile,"%s/%s%d",dataDir,DataFilesPrefix,i);
       datafile.open(outFile);
       if (true){
          sprintf(sql,"select * from ET order by key limit %d offset %d;",eachLoop,offset);
          ret = sqlite4_exec(db,sql,callback,0);
          offset = selectNum;
          //datafile << "hello" << endl;
          datafile.close();
       } else {
          printf("Error") ;
       }

       clock_t curT = clock();
       double duration = double(curT-preT) / CLOCKS_PER_SEC;
       double speed = double(selectNum/duration);
       printf("select %d cost %lf,speed %lf,saved in %s\n",selectNum,duration,speed,outFile);
       preT = curT;
       selectNum = 0;
       totalDuration += duration;
    
 //      printf("select %d\n",ret);

    }
       sqlite4_close(db,1);
   	db = 0;
    printf("total select %d\n",selectNum);
    cends = time(NULL);
    cout << "start " << cstart << " end:" << cends << " cost:" << totalDuration << endl;
    
	return 0;
}
