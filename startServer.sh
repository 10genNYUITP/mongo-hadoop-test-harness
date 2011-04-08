DIR0=`dirname $0`
PATH=/home/r_omio/mongodb-linux-i686-1.8.0/bin/:$PATH
 
 mkdir -p logs
 mkdir -p $DIR0/configdb
 mongod --dbpath  $DIR0/configdb --port 20000 > logs/configdb.log &
if [ $? -gt 0 ] ; then
   echo BLAAAAAAAH!!!!!
   exit
fi
 
for I in 1 2 3 ; do
   mkdir -p $DIR0/data$I
   mongod --dbpath `dirname $0`/data$I  --rest --port 3000$I > logs/db-$I.log &
done
sleep 3
mongos --port 30000  --chunkSize 1 --configdb localhost:20000 > logs/mongos.log &
echo Yeppie Kay Yay!
