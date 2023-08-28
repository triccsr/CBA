package method;

import HBLL.DistOriginPair;
import HBLL.Edge;
import HBLL.WeightedGraph;

import java.util.ArrayList;

public class DynamicWD {
    DistOriginPair[][] wd;
    boolean[] inqueue;
    final double eps=1e-6;
    public DynamicWD(WeightedGraph ww, ArrayList<Integer> query, int maxHop){
        long initStart=System.currentTimeMillis();
        wd=new DistOriginPair[ww.nodeNum][maxHop+1];
        inqueue=new boolean[ww.nodeNum];
        long initEnd=System.currentTimeMillis();
        //System.out.println("dwd init="+(initEnd-initStart));
        ArrayList<Integer> queue = new ArrayList<>();
        for(Integer u:query){
            wd[u][0]=new DistOriginPair(0.0,u);
            inqueue[u]=true;
            queue.add(u);
        }
        for(int h=1;h<=maxHop;++h){
            ArrayList<Integer> nextQ=new ArrayList<>();
            for(Integer u:queue) {
                if(wd[u][h]==null||wd[u][h-1].dis+eps<wd[u][h].dis){
                    wd[u][h]=wd[u][h-1];
                }
                for(Edge e:ww.graph.get(u)){
                    if(wd[e.v][h]==null||wd[u][h-1].dis+e.weight+eps<wd[e.v][h].dis){
                        wd[e.v][h]=new DistOriginPair(wd[u][h-1].dis+e.weight,wd[u][h-1].origin);
                        if(!inqueue[e.v]){
                            inqueue[e.v]=true;
                            nextQ.add(e.v);
                        }
                    }
                }
            }
            queue.addAll(nextQ);
        }
    }

    public DistOriginPair get_wd(int v,int hop){
        return wd[v][hop];
    }
}
