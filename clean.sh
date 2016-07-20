find org -name "*.class" > sources.txt

while read line
do
    echo delete $line
    rm $line 
done < sources.txt

rm sources.txt
