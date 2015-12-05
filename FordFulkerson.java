import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
	
    private boolean find_path(Vertex source, Vertex sink, LinkedList<Edge> path, Set<Vertex> seen) {
    	if(source == null || sink == null){
    		return false;
    	}
    	if (source.getName().equals(sink.getName())) {
    		return true;
    	}
    	for(Object e : source.incidentEdgeList) {
    		Edge ed = (Edge)e;
    		Vertex next = ed.getSecondEndpoint();
    		if (seen.contains(next)) {
    			continue;
    		}
    		seen.add(next);
    		boolean found = this.find_path(next, sink, path, seen);
    		if (found) {
    			path.add(ed);
    			return found;
    		}
    	}
    	return false;
    }
    
    private double min_residuals(LinkedList<Edge> edges) {
    	double min = Double.MAX_VALUE;
    	for (Edge e : edges){
    		Double resid = e.capacity;
    		if (resid < min) {
    			min = resid;
    		}
    	}
    	return min;
    }
    
    public void update_flow(SimpleGraph g, LinkedList<Edge> path , double flow) {
    	for(Edge e : path) {
    		Edge ge = g.get_edge(e.getFirstEndpoint(), e.getSecondEndpoint());
    		if(ge != null) {
    		    ge.flow += flow;
    		}
    	}
    }
    
    public SimpleGraph residual_graph(SimpleGraph orig) {
    	SimpleGraph resid = new SimpleGraph();
    	for(Object e : orig.edgeList) {
    		Edge ed = (Edge)e;
    		double remaining = ed.capacity - ed.flow;
    		if (remaining > 0){   			
    			Vertex f = this.find_vertex(resid, (String)ed.getFirstEndpoint().getName());
    			if (f == null)
    				f = resid.insertVertex(ed.getFirstEndpoint().getData(), 
    					ed.getFirstEndpoint().getName());
       			
    			Vertex s = this.find_vertex(resid, (String)ed.getSecondEndpoint().getName());
    			if (s == null)
             		s = resid.insertVertex(ed.getSecondEndpoint().getData(), 
       					ed.getSecondEndpoint().getName());
       		     
    			Edge artificial = resid.insertEdge(f, s, remaining, null);
    			artificial.artificial = true;
    		}
    	}
    	System.out.println("Num edges in residual graph "+resid.numEdges());
    	return resid;
    }
    
    public Vertex find_vertex(SimpleGraph g, String name){
    	Vertex copy = null;
    	for(Object o : g.vertexList) {
    		Vertex c  = (Vertex) o;
    		if (c.getName().equals(name))
    			return c;
    	}
    	return copy;
    }
    
    public double max_flow(Vertex source, Vertex sink) {    	
    	SimpleGraph residual_graph = this.residual_graph(this.sg);
        Vertex source_in_resid = this.find_vertex(residual_graph, (String)source.getName());
        Vertex sink_in_resid = this.find_vertex(residual_graph, (String)sink.getName());
        //System.out.println(residual_graph);
        
    	LinkedList<Edge> path = new LinkedList<Edge>();
    	
        this.find_path(source_in_resid, sink_in_resid, path, new HashSet<Vertex>());
        System.out.println("Init path size "+path.size());
    	
        while (path.size() > 0) {    		
    		double flow = this.min_residuals(path);
    		System.out.println("Min flow "+ flow);
    		this.update_flow(this.sg, path, flow);
    		
    		residual_graph = this.residual_graph(this.sg);
            source_in_resid = this.find_vertex(residual_graph, (String)source.getName());
            sink_in_resid = this.find_vertex(residual_graph,(String)sink.getName());
    		path = new LinkedList<Edge>();
            this.find_path(source_in_resid, sink_in_resid, path, new HashSet<Vertex>());
            System.out.println(path.size());
    	}
    	
    	double max_flow = 0;
    	for(Object e : source.incidentEdgeList){
    		Edge ed = (Edge)e;
    		if (ed.getFirstEndpoint() == source){
    			max_flow += ed.flow;
    		}
    	}
    	return max_flow;
    }
}
