package utils;

public class GeneatorUtils {

	/* define how long the critical section can be */
	public static enum CS_LENGTH_RANGE {
		VERY_SHORT_CS_LEN, /* 1 - 15 us */
		SHORT_CS_LEN, /* 16 - 50 us */
		MEDIUM_CS_LEN, /* 51 - 100 us */
		LONG_CSLEN, /* 101 - 200 us */
		VERY_LONG_CSLEN, /* 201 - 300 us */
		RANDOM, /* 1 - 300 us */
	};

	/* define how many resources in the system */
	public static enum RESOURCES_RANGE {
		HALF_PARITIONS, /* partitions us */
		PARTITIONS, /* partitions * 2 us */
		DOUBLE_PARTITIONS, /* partitions / 2 us */
	};
}
