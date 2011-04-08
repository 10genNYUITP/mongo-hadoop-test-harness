/home/r_omio/mongodb-linux-i686-1.8.0/bin/mongo 127.0.0.1:30000/admin setup-shards.js
 
if ! [ -f big.txt ] ; then
#NOTE wgt should have an e after the w and before the gt (wiki filtering problem apparently)
#following two lines is to warn you if you run the script w/o fixing it
#  echo fix the wgt in the script remove this line and rerun!
#  exit 1
 
  wget http://norvig.com/big.txt
fi
 
echo loading...
perl -e 'while (<>) {  s/\"/\\"/g ; print }' < big.txt | sed 's/.*/  db.lines.insert({"line": "&"});/' > big.js
echo loaded
 
#load data into database
/home/r_omio/mongodb-linux-i686-1.8.0/bin/mongo 127.0.0.1:30000 big.js
