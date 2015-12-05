import java.util.ArrayList;
import java.util.List;


public class PreflowPush {
    private SimpleGraph sg;
	public PreflowPush(SimpleGraph g) {
    	this.sg = g;
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

    public SimpleGraph create_residual_graph(SimpleGraph orig) {
    	SimpleGraph resid = new SimpleGraph();
    	for(Object e : orig.edgeList) {
    		Edge ed = (Edge)e;
    		double remaining = ed.capacity - ed.flow;
    		Vertex f = this.find_vertex(resid, (String)ed.getFirstEndpoint().getName());
    		if (f == null)
    			f = resid.insertVertex(ed.getFirstEndpoint().getData(), 
    				ed.getFirstEndpoint().getName());
       			
    		Vertex s = this.find_vertex(resid, (String)ed.getSecondEndpoint().getName());
    		if (s == null)
             	s = resid.insertVertex(ed.getSecondEndpoint().getData(), 
       				ed.getSecondEndpoint().getName());
                       		     
    		Edge orig_ed = resid.insertEdge(f, s, remaining, null);
    		orig_ed.artificial = false;
    		Edge resid_new_edge = resid.insertEdge(s, f, ed.capacity, null);
    		resid_new_edge.artificial = true;
    		//System.out.println(orig_ed+ " "+ orig_ed.artificial);
    		//System.out.println(resid_new_edge + " "+ resid_new_edge.artificial);

    	}
    	System.out.println("Num edges in residual graph "+resid.numEdges());
    	return resid;
    }

    public SimpleGraph update_residual(SimpleGraph resid){
    	for(Object o : resid.edgeList) {
    		Edge ed = (Edge)o;
    		double cap = ed.capacity;
    		if(!ed.artificial) {
    			double new_capacity = ed.capacity - ed.flow;
    		    if(new_capacity < 0)
    		    	continue;
    		    ed.capacity = new_capacity;
    			//System.out.println("Setting the cap "+ ed.capacity);
    		}
    		else{
    			ed.capacity = cap;
    		}
    	}
    	return resid;
    }
    void init_height(SimpleGraph g, Vertex source, Vertex sink) {
    	int num_nodes = g.vertexList.size();
    	for (Object v : g.vertexList) {
    		Vertex s = (Vertex)v;
    		if (s.getName().equals(source.getName()))
    			s.height = num_nodes;
    		else 
    			s.height = 0;
    	}
    }
    
    public Edge find_edge(SimpleGraph g, Vertex v, Vertex t) {
    	for(Object e : g.edgeList) {
    		Edge ed = (Edge)e;
    		Vertex fst = ed.getFirstEndpoint();
    		Vertex sec = ed.getSecondEndpoint();
    		if(fst == v && sec == t) {
    			return ed;
    		}
    	}
    	return null;
    }

    public List<Edge> find_all_edges(SimpleGraph g, Vertex v, Vertex t) {
    	List<Edge> edges = new ArrayList<Edge>();
    	for(Object e : g.edgeList) {
    		Edge ed = (Edge)e;
    		Vertex fst = ed.getFirstEndpoint();
    		Vertex sec = ed.getSecondEndpoint();
    		if(fst == v && sec == t) {
    			edges.add(ed);
    		}
    	}
    	return edges;
    }

    public boolean push(SimpleGraph g, Vertex v, Vertex w) {
        double excess_at_v = this.excess(g, v);
    	if(excess_at_v > 0 && w.height < v.height) {
            List<Edge> edges = this.find_all_edges(g, v, w);
            for(Edge ed : edges) {
                //System.out.println(ed.artificial);
              if(!ed.artificial) {
            	  double resid = ed.capacity - ed.flow;
            	  double min = Math.min(excess_at_v, resid);
            	  if(min <= 0)
            		  continue;
            	  ed.flow += min;
            	  System.out.println("Increased the flow "+ ed.flow + " "+min);
                  return true;
              }
              else { // Backward edge
            	double min = Math.min(excess_at_v, ed.flow);
            	if (min <= 0)
            		continue;
                ed.flow = ed.flow - min;
                System.out.println("Decreased the flow "+ ed.flow);
                return true;
              }
              
            }
    	}
        return false;
    }
    
    public boolean check_all_neigh_height(Vertex v){
    	for(Object o : v.incidentEdgeList) {
    		Edge ed = (Edge)o;
    		if(ed.getFirstEndpoint().getName().equals(v.getName())) {
    			Vertex w = ed.getSecondEndpoint();
    			if (w.height >= v.height)
    				return true;
    		}
    	}
    	return false;
    }
    public double relabel(SimpleGraph g, Vertex v){
        if(this.excess(g, v) > 0 && this.check_all_neigh_height(v)) {
        	v.height = v.height + 1;
        }
    	return v.height;
    }
    
    public void init_flow(SimpleGraph resid, Vertex source) {
    	source = this.find_vertex(resid, (String)source.getName());
    	for(Object e : source.incidentEdgeList) {
    		Edge ed = (Edge)e;
    		if(ed.artificial)
    			continue; // Do not conside edges which were introduces due to Residual Graph creation
    		if(ed.getFirstEndpoint().equals(source)) {
    			ed.flow = ed.capacity;
    		}
    	}
    		
    }
    
    public double excess(SimpleGraph g, Vertex v) {
    	double inflow = 0.0;
    	double out = 0.0;
    	for(Object e : v.incidentEdgeList) {
    		Edge ed = (Edge)e;
    		//if(ed.capacity <= 0) {
    		    //System.out.println("EXcess Not : " +ed);
    		//	continue;
    		//}
    		//if(ed.artificial)
    		//	continue; // Do not consider which were created due to Residual Graph
    		if(ed.getFirstEndpoint().equals(v)) {
    			out += ed.flow;
    		}
    		else
    			inflow += ed.flow;
    	}
    	return inflow - out;
    }
    
     List<Vertex> get_neighbors(Vertex v) {
    	List<Vertex> result = new ArrayList<Vertex>();
    	for(Object e : v.incidentEdgeList){
    		Edge ed = (Edge)e;
    		if(ed.getFirstEndpoint().equals(v)) {
    			result.add(ed.getSecondEndpoint());
    		}
    	}
        return result;
     }

    Vertex find_vertex_with_excess(SimpleGraph g) {
    	for(Object o : g.vertexList) {
    		if (this.excess(g, (Vertex)o) > 0) {
    			return (Vertex)o;
    		}
    	}
    	return null;
    }
    
    double max_flow(Vertex source, Vertex sink) {
        double max_flow = 0.0;
        this.init_height(resid, source, sink);
        this.init_flow(resid, source);
        this.update_residual(resid);
        Vertex v_excess = null;
        while((v_excess=this.find_vertex_with_excess(resid)) != null) {
            System.out.println("Excess node "+v_excess );
        	List<Vertex> all_n = this.get_neighbors(v_excess);
            boolean pushed = false;
            for(Vertex n : all_n) {
            	if(this.push(resid, v_excess, n)) {
            		System.out.println("Pushed to "+v_excess + " "+n);
            		pushed = true;
            		this.update_residual(resid);
            		break;
            	}
            }
            if(!pushed) {
            	this.relabel(resid, v_excess);
            	System.out.println("Relabeling " + v_excess + " to "+ v_excess.height + " "+this.excess(resid, v_excess));
            }
        }
        
        return max_flow;
        
    }
}
