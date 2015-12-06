import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    public boolean push(SimpleGraph g, Vertex v, Vertex w) {
        double excess_at_v = this.excess(g, v);
    	if(excess_at_v > 0 && w.height < v.height) {
    		Edge forward = this.find_edge(g, v, w);
    		if(forward != null) {
    			double resid = forward.capacity - forward.flow;
    			double min = Math.min(excess_at_v, resid);
    			if(min <= 0)
    				return false;
    			
    			forward.flow += min;
    			//System.out.println("Pushed the flow "+ min + " on edge "+ forward + " total "+forward.flow);
                return true;   
    		}
    		else {
    			Edge back = this.find_edge(g, w, v);
    			double min = Math.min(excess_at_v, back.flow);
    			if(min <= 0)
    				return false;
    			back.flow = back.flow - min;
    			//System.out.println("Reduce the flow "+ min + " on edge "+ back + " total "+ back.flow);       
    		    return true;
    		}              
    	}
        return false;
    }
    
    
    public boolean check_all_neigh_height(SimpleGraph g, Vertex v){
    	Set<Vertex> n_list = this.get_all_neighbors(v);
        boolean ht = true;
    	for(Vertex w : n_list) {
    		Edge ed = this.find_edge(g, v, w);
    		if(ed != null) {
    		    if (ed.capacity - ed.flow > 0) {
    	            if(w.height < v.height) {
    	    	       ht = false;
    	            }
    		    }
    		}
    	}
    	return ht;
    }
    public double relabel(SimpleGraph g, Vertex v){
        if(this.excess(g, v) > 0 && this.check_all_neigh_height(g, v)) {
        	v.height = v.height + 1;
        }
    	return v.height;
    }
    
    public void init_flow(SimpleGraph resid, Vertex source) {
    	source = this.find_vertex(resid, (String)source.getName());
    	for(Object e : source.incidentEdgeList) {
    		Edge ed = (Edge)e;
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

     Set<Vertex> get_all_neighbors(Vertex v) {
     	Set<Vertex> result = new HashSet<Vertex>();
     	for(Object e : v.incidentEdgeList){
     		Edge ed = (Edge)e;
            Vertex f = ed.getSecondEndpoint();
            if (!f.equals(v)) {
            	result.add(f);
            }
            Vertex s = ed.getFirstEndpoint();
            if (!s.equals(v)) {
            	result.add(s);
            }
            
     	}
         return result;
      }

    Vertex find_vertex_with_excess(SimpleGraph g, Vertex source, Vertex sink) {
    	for(Object o : g.vertexList) {
    		Vertex v = (Vertex)o;
    		if (v.equals(source) || v.equals(sink))
    			continue;
    		if (this.excess(g, (Vertex)o) > 0) {
    			return (Vertex)o;
    		}
    	}
    	return null;
    }
    
    double max_flow(Vertex source, Vertex sink) {
        double max_flow = 0.0;
        this.init_height(this.sg, source, sink);
        this.init_flow(this.sg, source);
        
        Vertex v_excess = null;
        int i = 0;
        while((v_excess=this.find_vertex_with_excess(this.sg, source, sink)) != null) {
            //System.out.println("Excess node "+v_excess );
        	Set<Vertex> all_n = this.get_all_neighbors(v_excess);
            
        	boolean pushed = false;
            for(Vertex n : all_n) {
            	if(this.push(this.sg, v_excess, n)) {
            		//System.out.println("Pushed to "+v_excess + " "+n);
            		pushed = true;
            		break;
            	}
            }
           
            if(!pushed) {
            	this.relabel(this.sg, v_excess);
            	//System.out.println("Relabeling " + v_excess + " to "+ v_excess.height + " "+this.excess(this.sg, v_excess));
            }
           
        }
        
        for(Object o : source.incidentEdgeList){
        	Edge ed = (Edge)o;
        	if(ed.getFirstEndpoint().equals(source)) {
        		max_flow += ed.flow;
        	}
        }
        return max_flow;
        
    }
}
