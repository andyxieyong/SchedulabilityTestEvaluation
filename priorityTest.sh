cd /home/userfs/z/zs673/SchedulabilityTestEvaluation/result
pwd

rm -rf *.txt
cd /home/userfs/z/zs673/SchedulabilityTestEvaluation
pwd
rm nohup.out

LD_LIBRARY_PATH=src nohup java -cp /home/userfs/z/zs673/SchedulabilityTestEvaluation/bin evaluationSection4.PriorityOrderingTest &
