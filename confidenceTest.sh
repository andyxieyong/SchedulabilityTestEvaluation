
pathMy="/home/shuai/Desktop/"
pathServer = "/usr/userfs/z/zs673/"



cd $pathServer"SchedulabilityTestEvaluation/result"
pwd

rm -rf *.txt
cd $pathServer"SchedulabilityTestEvaluation"
pwd


LD_LIBRARY_PATH=src nohup java -cp $pathServer"SchedulabilityTestEvaluation/bin" test.ConfidenceTest1000 &
