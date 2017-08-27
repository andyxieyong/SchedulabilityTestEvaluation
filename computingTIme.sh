#cd /home/shuai/Desktop/TestMrsPAnalysis/result
#rm -rf *.txt
#cd /home/shuai/Desktop/TestMrsPAnalysis


cd /usr/userfs/z/zs673/TestMrsPAnalysis/result
rm -rf *.txt
cd /usr/userfs/z/zs673/TestMrsPAnalysis



for smallset in 1 2 3 4 5 6 7 8 9
do
	LD_LIBRARY_PATH=src nohup java -cp /usr/userfs/z/zs673/TestMrsPAnalysis/bin -Djava.library.path="/usr/userfs/z/zs673/TestMrsPAnalysis/src" test.ComputingTimeTest 1 ${smallset} &
done


for smallset in 1 2 3 4 5
do
	LD_LIBRARY_PATH=src nohup java -cp /usr/userfs/z/zs673/TestMrsPAnalysis/bin -Djava.library.path="/usr/userfs/z/zs673/TestMrsPAnalysis/src" test.ComputingTimeTest 2 ${smallset} &
done


for smallset in 1 6 11 16 21 26 31 36 41
do
	LD_LIBRARY_PATH=src nohup java -cp /usr/userfs/z/zs673/TestMrsPAnalysis/bin -Djava.library.path="/usr/userfs/z/zs673/TestMrsPAnalysis/src" test.ComputingTimeTest 3 ${smallset} &
done

for smallset in 4 6 8 10 12 14 16 18 20 22
do
	LD_LIBRARY_PATH=src nohup java -cp /usr/userfs/z/zs673/TestMrsPAnalysis/bin -Djava.library.path="/usr/userfs/z/zs673/TestMrsPAnalysis/src" test.ComputingTimeTest 4 ${smallset} &
done
