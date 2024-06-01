# ... login to vxdb2, source env, run your server as usual ...
# ... if you already had such a database
dropdb ass1
# ... create a new empty atabase
createdb ass1
# ... load the database, saving the output in a file called log
psql ass1 -f ass1.dump > log 2>&1
# ... check for error messages in the log; should be none
grep ERR log
# ... examine the database contents ...
psql ass1