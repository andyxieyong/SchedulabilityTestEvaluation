package analysisNewIO;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public abstract class RuntimeCostAnalysis {

	int extendCal = 5;

	public abstract long[][] getResponseTimeSBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeRPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);

	public abstract long[][] getResponseTimeDM(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit, boolean useRi,
			boolean printDebug);

}
