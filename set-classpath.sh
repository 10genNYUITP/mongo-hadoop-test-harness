CLASSPATH=.
for I in `ls /home/r_omio/hadoop/**/*.jar ` ; do
   # echo $I
    CLASSPATH=$CLASSPATH:$I
done 
for J in `ls /home/r_omio/hadoop/*.jar`; do
    echo $J
    CLASSPATH=$CLASSPATH:$J
done
 echo $CLASSPATH
 export CLASSPATH
