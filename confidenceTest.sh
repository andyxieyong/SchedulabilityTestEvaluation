
pathMy="/home/shuai/Desktop/"
pathServer="/usr/userfs/z/zs673/"



cd $pathMy"SchedulabilityTestEvaluation/result"
pwd

rm -rf *.txt
cd $pathMy"SchedulabilityTestEvaluation"
pwd


LD_LIBRARY_PATH=src nohup java -cp $pathMy"SchedulabilityTestEvaluation/bin" test.IdenticalTest &
