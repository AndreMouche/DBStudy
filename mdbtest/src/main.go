/**
 * Test mdb
 * User: fun
 * Date: 13-11-30
 * Time: 下午3:32
 * To change this template use File | Settings | File Templates.
 */
package main

import (
	"fmt"
	gomdb "github.com/benbjohnson/gomdb"
	//	"io/ioutil"
	"math/rand"
	"os"
	"time"
)

const KeyLength = 512 - 10
const ValueLength = 1024 - 10

//const TestDir = "/tmp/mdb"
const TestDir = "/tmp/mdb/FIXEDMAP_C"

func generateString(modNum int) string {
	str := fmt.Sprintf("%v", time.Now().UnixNano())
	l := rand.Int()%modNum + 10
	if l <= len(str) {
		return str
	}
	need := l - len(str)

	for i := 0; i < need; i++ {
		str = str + "a"
	}
	return str
}

func insert(num int, env *gomdb.Env) {

	defer CostTime(fmt.Sprintf("insert %d item", num), time.Now())
	var dbi gomdb.DBI
	var err error
	var txn *gomdb.Txn
	start_time := time.Now()
	thx_entries := 1
	totalLoops := num / thx_entries
	for loop := 1; loop <= totalLoops; loop++ {
		txn, err = env.BeginTxn(nil, 0)
		if err != nil {
			fmt.Printf("Cannot begin transaction: %s", err)
		}

		dbi, err = txn.DBIOpen(nil, 0)
		defer env.DBIClose(dbi)
		if err != nil {
			fmt.Printf("Cannot create DBI %s", err)
		}
		var key string
		var val string
		for i := 0; i < thx_entries; i++ {
			key = generateString(KeyLength)
			val = generateString(ValueLength)
			err = txn.Put(dbi, []byte(key), []byte(val), gomdb.NOOVERWRITE)
			if err != nil {
				fmt.Printf("Error during put: %s", err)
				if txn != nil {
					txn.Abort()
				}
				fmt.Printf("Error during put: %s", err)
				return
			}
		}
		err = txn.Commit()
		if err != nil {
			txn.Abort()
			fmt.Printf("Cannot commit %s", err)
		}
	}

	cur := time.Now()
	cost := float64(cur.Sub(start_time).Nanoseconds()) / float64(time.Second)
	//fmt.Println(cost)
	speed := float64(num) / (cost)
	fmt.Printf("Insert %d:[%v,%v] cost %v,speed %v\n", num, start_time.Unix(), cur.Unix(), cost, speed)

}

func GetAndWriteToFile(env *gomdb.Env, filePath string) {
	msg := fmt.Sprintf("write to filePath %s", filePath)
	defer CostTime(msg, time.Now())
	var txn *gomdb.Txn
	var dbi gomdb.DBI

	f, err := os.OpenFile(filePath, os.O_RDWR|os.O_CREATE|os.O_APPEND, os.ModePerm)
	if err != nil {
		panic(err)
	}
	defer f.Close()

	txn, err = env.BeginTxn(nil, 0)
	if err != nil {
		fmt.Printf("Cannot begin transaction: %s", err)
		return
	}
	dbi, err = txn.DBIOpen(nil, 0)
	defer env.DBIClose(dbi)
	var cursor *gomdb.Cursor
	cursor, err = txn.CursorOpen(dbi)
	if err != nil {
		cursor.Close()
		txn.Abort()
		fmt.Printf("Error during cursor open %s", err)
	}

	var bkey, bval []byte
	var rc error
	for {
		bkey, bval, rc = cursor.Get(nil, gomdb.NEXT)
		if rc != nil {
			break
		}
		skey := string(bkey)
		sval := string(bval)
		line := fmt.Sprintf("%s,%s\n", skey, sval)
		f.WriteString(line)
		//t.Logf("Val: %s", sval)
		//t.Logf("Key: %s", skey)
	}
	cursor.Close()
}

func CostTime(mes string, start time.Time) {
	current := time.Now()
	cost := current.Sub(start)
	fmt.Printf("mes time [%v,%v] cost %v\n", start.Unix(), current.Unix(), cost)
}

func TestInsertAndWrite(testNum int) {
	fmt.Println("**********************************************************")
	fmt.Println("**********************************************************")
	defer CostTime(fmt.Sprintf("test Num %d", testNum), time.Now())

	dbPath := fmt.Sprintf("%s/db_%d", TestDir, testNum)
	dbData := fmt.Sprintf("%s/data_%d", TestDir, testNum)
	env, err := gomdb.NewEnv()
	if err != nil {
		fmt.Printf("Cannot create environment: %s", err)
	}

	err = env.SetMapSize(200 * 1024 * 1024 * 1024)

	if err != nil {
		fmt.Printf("Cannot set mapsize: %s", err)
	}
	//path, err := //ioutil.TempDir("/tmp", "mdb_batch_test")
	//	if err != nil {
	//		fmt.Printf("Cannot create temporary directory")
	//		return
	//	}

	err = os.MkdirAll(dbPath, 0770)
	//	defer os.RemoveAll(path)
	if err != nil {
		fmt.Printf("Cannot create directory: %s", dbPath)
		return
	}
	err = env.Open(dbPath, gomdb.FIXEDMAP, 0664)
	//err = env.Open(dbPath,gomdb.NOSUBDIR,0644)
	//err = env.Open(dbPath,gomdb.WRITEMAP,0664)
	defer env.Close()
	if err != nil {
		fmt.Printf("Cannot open environment: %s", err)
		return
	}

	insert(testNum,env)

	stat, err := env.Stat()
	if err != nil {
		fmt.Printf("Cannot get stat %s", err)
	}
	fmt.Printf("stat %+v", stat)

	startTime := time.Now()
	realNum := stat.Entries
	GetAndWriteToFile(env, dbData)
	endTime := time.Now()
	cost := float64(endTime.Sub(startTime).Nanoseconds()) / float64(time.Second)
	//fmt.Println(cost)
	speed := float64(realNum) / (cost)
	fmt.Printf("Select %d:[%v,%v] cost %v,speed %v\n", realNum, startTime.Unix(), endTime.Unix(), cost, speed)
	fmt.Println("**********************************************************")
	fmt.Println("**********************************************************")
}
func main() {
	testNum := 10
	for i := 0; i < 6; i++ {
		TestInsertAndWrite(testNum)
		testNum *= 10
	}
}
