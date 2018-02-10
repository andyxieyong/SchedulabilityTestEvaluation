package analysisNewIO;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public abstract class RuntimeCostAnalysis {
	
	public abstract long[][] getResponseTimeBySBPO(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);
	
	public abstract long[][] getResponseTimeByOPA(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean isprint);
	
	public abstract long[][] getResponseTimeDM(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean btbHit, boolean useRi,
			boolean printDebug);

}
