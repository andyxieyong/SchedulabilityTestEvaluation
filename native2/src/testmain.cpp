#include <iostream>

#include "tasks.h"
#include "sharedres.h"
#include "res_io.h"
#include "lp_analysis.h"

#include <jni.h>
#include "javaToC_MIPSolverC.h"

using namespace std;

JNIEXPORT jlong JNICALL Java_javaToC_MIPSolverC_solveMIP(JNIEnv *env, jobject solver, jobject tasks, jobject resources)
{
    jlong blocking = 10;

    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID size = env->GetMethodID(arrayListClass, "size", "()I");
    jmethodID geti = env->GetMethodID(arrayListClass, "get", "(I)Ljava/lang/Object;");

    jint tasks_size = env->CallIntMethod(tasks, size);
    jint resources_size = env->CallIntMethod(resources, size);
    cout << "partitions: " << tasks_size << "  resource size: " << resources_size << endl;

    

    cout << "resources: ";
    for (int i = 0; i < resources_size; i++)
    {
			
		jobject resource = env->CallObjectMethod(resources, geti, 0);
     	jclass resourceClass = env->GetObjectClass(resource);
  		jfieldID csl = env->GetFieldID(resourceClass, "csl", "J");
		jlong csl_value = env->GetLongField(resource, csl);
		cout << " R" << i+1 << ": " << csl_value << "   ";

    }
	cout << endl;

    for (int i = 0; i < tasks_size; i++)
    {
	jobject task_on_partition = env->CallObjectMethod(tasks, geti, i);
	jint tasks_on_partition_size = env->CallIntMethod(task_on_partition, size);
	cout << "tasks_on_partition size: " << tasks_on_partition_size << endl;

	for (int j = 0; j < tasks_on_partition_size; j++)
	{
	    jobject task = env->CallObjectMethod(task_on_partition, geti, j);
	    jclass taskClass = env->GetObjectClass(task);

	    jfieldID priority = env->GetFieldID(taskClass, "priority", "I");
	    jint priority_value = env->GetIntField(task, priority);

	    jfieldID period = env->GetFieldID(taskClass, "period", "J");
	    jlong period_value = env->GetLongField(task, period);

	    jfieldID deadline = env->GetFieldID(taskClass, "deadline", "J");
	    jlong deadline_value = env->GetLongField(task, deadline);

	    jfieldID WCET = env->GetFieldID(taskClass, "WCET", "J");
	    jlong WCET_value = env->GetLongField(task, WCET);

	    jfieldID partition = env->GetFieldID(taskClass, "partition", "I");
	    jint partition_value = env->GetIntField(task, partition);

	    jfieldID id = env->GetFieldID(taskClass, "id", "I");
	    jint id_value = env->GetIntField(task, id);

	    jfieldID pure_resource_execution_time = env->GetFieldID(taskClass, "pure_resource_execution_time", "J");
	    jlong pure_resource_execution_time_value = env->GetLongField(task, pure_resource_execution_time);

	    jfieldID resource_required_index = env->GetFieldID(taskClass, "resource_required_index_cpoy", "[I");
	    jobject resource_required_index_object = env->GetObjectField(task, resource_required_index);
	    jintArray resource_required_index_value = (jintArray)resource_required_index_object;
	    jsize resource_required_length = env->GetArrayLength(resource_required_index_value);

	    jfieldID number_of_access_in_one_release = env->GetFieldID(taskClass, "number_of_access_in_one_release_copy", "[I");
	    jobject number_of_access_in_one_release_object = env->GetObjectField(task, number_of_access_in_one_release);
	    jintArray number_of_access_in_one_release_value = (jintArray)number_of_access_in_one_release_object;
	    jsize number_of_access_in_one_release_length = env->GetArrayLength(number_of_access_in_one_release_value);

	    cout << "id: " << id_value << " priority: " << priority_value << " period: " << period_value << " deadline: " << deadline_value << " WCET: " << WCET_value << " partition: " << partition_value << " pure_resource_execution_time: " << pure_resource_execution_time_value << " resource request size: " << resource_required_length << " number of access size: " << number_of_access_in_one_release_length << endl;

	    cout << "task request resource: ";
	    jint *r_index = env->GetIntArrayElements(resource_required_index_value, 0);
	    for (int k = 0; k < resource_required_length; k++)
	    {
		cout << r_index[k];
	    }
	    cout << endl;

	    cout << "number of access: ";
	    jint *num_access = env->GetIntArrayElements(number_of_access_in_one_release_value, 0);
	    for (int k = 0; k < number_of_access_in_one_release_length; k++)
	    {
		cout << num_access[k];
	    }
	    cout << endl;

	    env->ReleaseIntArrayElements(resource_required_index_value, r_index, 0);
	    env->ReleaseIntArrayElements(number_of_access_in_one_release_value, num_access, 0);
	    env->DeleteLocalRef(task);
	}

	env->DeleteLocalRef(task_on_partition);
    }

    return blocking;
}

int aa();
JNIEXPORT void JNICALL Java_javaToC_MIPSolverC_helloFromC(JNIEnv *env, jobject obj)
{
    cout << "111hello from C side testmain! hahahah" << endl;
    aa();
}

int main(int argc, char **argv)
{
    aa();

    return 0;
}

int aa()
{
    ResourceSharingInfo rsi(100);
    unsigned int i;

    rsi.add_task(50000, 50000, 0, 2);
    rsi.add_request(0, 2, 1);

    rsi.add_task(30000, 30000, 0, 1);
    rsi.add_request(0, 4, 3);

    rsi.add_task(20000, 20000, 0, 0);
    rsi.add_request(0, 4, 1);

    rsi.add_task(50000, 50000, 1, 3);
    rsi.add_request(0, 2, 1);

    rsi.add_task(30000, 30000, 1, 2);
    rsi.add_request(0, 3, 3);
    rsi.add_request(1, 100, 100);

    rsi.add_task(20000, 20000, 1, 1);
    rsi.add_request(0, 3, 1);

    rsi.add_task(50000, 50000, 2, 2);
    rsi.add_request(0, 2, 1);

    rsi.add_task(30000, 30000, 2, 1);
    rsi.add_request(0, 5, 3);

    rsi.add_task(20000, 20000, 2, 0);
    rsi.add_request(0, 2, 1);

    rsi.add_task(3000, 3000, 3, 0);
    rsi.add_request(1, 1, 1);

    rsi.add_task(5000, 5000, 1, 0);

    rsi.add_task(100000, 100000, 4, 100);
    rsi.add_request(3, 3, 3);

    cout << rsi << endl;

    BlockingBounds *results;

    results = lp_pfp_msrp_bounds(rsi);

    cout << endl
	 << endl
	 << "MSRP (LP)" << endl;
    for (i = 0; i < results->size(); i++)
	cout << "T" << i
	     << " y=" << rsi.get_tasks()[i].get_priority()
	     << " c=" << rsi.get_tasks()[i].get_cluster()
	     << ": total=" << (*results)[i].total_length
	     << "  remote=" << results->get_remote_blocking(i)
	     << "  local=" << results->get_local_blocking(i)
	     << endl;

    delete results;

    results = lp_pfp_preemptive_fifo_spinlock_bounds(rsi);

    cout << endl
	 << endl
	 << "Preemptive MSRP (LP)" << endl;
    for (i = 0; i < results->size(); i++)
	cout << "T" << i
	     << " y=" << rsi.get_tasks()[i].get_priority()
	     << " c=" << rsi.get_tasks()[i].get_cluster()
	     << ": total=" << (*results)[i].total_length
	     << "  remote=" << results->get_remote_blocking(i)
	     << "  local=" << results->get_local_blocking(i)
	     << endl;

    delete results;
    return 0;
}
