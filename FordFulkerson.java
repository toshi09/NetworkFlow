/**
 * 
 */

/**
 * @author ashwanir
 *
 */
public class FordFulkerson {
    private SimpleGraph sg;
	public FordFulkerson(SimpleGraph g) {
    	this.sg = g;
    }
	
	private double excess(SimpleGraph G, Vertex v) {
		double excess = 0.0;
	    for (Object e : v.incidentEdgeList) {
	    	Edge ed = (Edge)e;
	        if (v == ed.from()) 
	        	excess -= ed.flow();
	        else
	        	excess += ed.flow();
	     }
	    return excess;
	}
}
