/**
Select Test
based on db created by write.cpp

*/
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
using namespace std;
#include <sqlite4.h>
#include <time.h>

char sql[100];
int selectNum = 0;
static int callback(void *pArg, int nVal, sqlite4_value **apVal, const char **azCol){
    int i;
    for(i=0; i<nVal; i++){
        const char *z = sqlite4_value_text(apVal[i],0);
        printf("%s =%s,", azCol[i] , z ? z : "NULL");
    }
    printf("\n");
    selectNum ++;
    return 0;
}


int main(int argc,char ** argv)
{
    
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
	printf("Successfully connected to database\n");
    sprintf(sql,"select * from ET;");
    ret = sqlite4_exec(db,sql,callback,0);
    printf("select %d\n",ret);
    sqlite4_close(db,1);
	db = 0;
    printf("total select %d\n",selectNum);
    cends = time(NULL);
    cout << "start " << cstart << " end:" << cends << " cost:" << cends-cstart<<endl;
    
	return 0;
}
