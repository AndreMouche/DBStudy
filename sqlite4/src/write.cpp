/**
Write test

g++ write.cpp -lsqlite4 -lpthread -o write
*/
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
using namespace std;
#include <sqlite4.h>
const int keyLength = 1024;
const int vLength = 1024;
const int sqlLength = 100+keyLength+vLength;
char key[keyLength+1]="key";
char value[vLength+1]="len";
char sql[sqlLength];


static int callback(void *pArg, int nVal, sqlite4_value **apVal, const char **azCol){
    int i;
    for(i=0; i<nVal; i++){
        const char *z = sqlite4_value_text(apVal[i],0);
        printf("%s = %s\n", azCol[i] , z ? z : "NULL");
    }
    printf("\n");
    return 0;
}

void rand_str(char *str,const int len) {
   int i;
   for (i=0;i<len;i++) {
     str[i] = 'A'+rand()%26;
   }
   str[i]='\0';
   //return str;
}

int main(int argc,char ** argv)
{ 
    if (argc != 3) {
       printf("Usage %s dbFile testNum\n",argv[0]);
       exit(-1);
    }

    time_t  cstart,cends;
    cstart = time(NULL);
    clock_t startT = clock();
	char * pErrMsg = 0;
	int ret = 0;
	sqlite4 * db = 0;
    int i;
    sqlite4_env *env = sqlite4_env_default();
    sprintf(sql,"CREATE TABLE ET( key varchar(1024) primary key,va varchar(1024));");
	ret = sqlite4_open(env,argv[1], &db);
    int testNum = atoi(argv[2]);
    printf("test num %d\n",testNum);
	if ( ret != SQLITE4_OK)
	{
		printf("Could not open database: %s", sqlite4_errmsg(db));
		exit(1);
	} 
    
    sqlite4_exec(db,sql,0,0);
    
	printf("Successfully connected to database\n");
    
    for(i=0;i<testNum;i++){
      int kl = rand()%keyLength;
      int vl = rand()%vLength;
      rand_str(key,kl);
      rand_str(value,vl);
      //printf("%s",key);
      sprintf(sql,"REPLACE INTO ET VALUES('%s','%s');",key,value);
      ret = sqlite4_exec(db,sql,0,0);
      if (ret !=0) {
         printf("insert error %d\n",ret);
      }
    }

 //   sprintf(sql,"select * from Students;");
 //   ret = sqlite4_exec(db,sql,callback,0);
 //   printf("select %d\n",ret);
    sqlite4_close(db,1);
	db = 0;
    cends = time(NULL);
    cout << "start at:" << cstart;
    cout << " end at:" << cends;
    clock_t endT = clock();
    double duration =double(endT - startT)/CLOCKS_PER_SEC;
    cout << " total cost:" << duration << " speed:" << testNum/duration <<endl;
	return 0;
}
