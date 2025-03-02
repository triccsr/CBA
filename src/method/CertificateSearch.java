package method;

import HBLL.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import static java.lang.Math.abs;

public class CertificateSearch {
    int nodeNum;
    public int kwnum;
    public double min_maxWeight;
    //kwnum of ansQueue

    int[][] kwHop;
    boolean[][] kwHopStore;
    static final double INF = 0x3f3f3f3f;

    public CertificateSearch(WeightedGraph graph) {
        nodeNum = graph.nodeNum;
        kwnum = 0;
        min_maxWeight = INF;
    }

    public int getHop(int u, int kwId, HopLimitHL hl, KwdHL[] kwhl) {
        if (kwHopStore[kwId][u]) return kwHop[kwId][u];
        kwHopStore[kwId][u] = true;
        kwHop[kwId][u] = hl.getHop(u, kwhl[kwId]);//O(HLsize), written as O(n) in article but in fact it is small
        return kwHop[kwId][u];
    }

    public CertificateQueue BFSearch(WeightedGraph graph, HopLimitHL hl, KwdHL[] kwdhl,DynamicWD[] dwdArray,ArrayList<ArrayList<Integer>> queryList, int diambound)//queryList is the query of vertices of each keyword
    {

        long starttime=System.currentTimeMillis();
        boolean[] visit=new boolean[nodeNum];
        LinkedList<PriItem> pq=new LinkedList<PriItem>();
        kwHopStore=new boolean[queryList.size()][nodeNum];
        kwHop=new int[queryList.size()][nodeNum];

        for(int i=0;i<queryList.size();i++)
        {
            for(int j=0;j<queryList.get(i).size();j++)
            {
                int u=queryList.get(i).get(j);
                if(!visit[u])
                {

                    pq.add(new PriItem(u,u,0,0,i,0));
                    visit[u]=true;
                }
            }
        }


        CertificateQueue min_maxWeightQueue=null;

        while(!pq.isEmpty())
        {
            if(System.currentTimeMillis()-starttime>1000*1000)
            {
                return null;
            }
            PriItem pitem = pq.poll();
            CertificateQueue q;

            q=GenerateQueueEven(graph,hl,kwdhl,dwdArray,queryList,diambound,pitem.v,pitem.startKwd);//MaxM

            if(q.kwnum>kwnum)
            {
                kwnum=q.kwnum;
                min_maxWeight=q.weightMax;
                min_maxWeightQueue=q;
            }
            else if(q.kwnum==kwnum&&q.weightMin<min_maxWeight)
            {
                if(q.weightMax<min_maxWeight)
                {
                    min_maxWeight=q.weightMax;
                    min_maxWeightQueue=q;
                }
            }
            if(pitem.hop<diambound/2)
            {
                for(Edge e:graph.graph.get(pitem.v))
                {
                    int v_=e.v;
                    if(!visit[v_])
                    {
                        visit[v_]=true;

                        pq.add(new PriItem(pitem.start,v_,0,pitem.hop+1,pitem.startKwd,0));
                    }
                }
            }
        }


        return min_maxWeightQueue;
    }


    public CertificateQueue BestFirstSearchPri(WeightedGraph graph, HopLimitHL hl, KwdHL[] kwdhl, DynamicWD[] dwdArray,ArrayList<ArrayList<Integer>> queryList, int diambound)//queryList is the query of vertices of each keyword
    {
        long starttime=System.currentTimeMillis();
        boolean[] checked=new boolean[nodeNum];
        boolean[] visit=new boolean[nodeNum];
        int[] h=new int[nodeNum];
        kwHopStore=new boolean[queryList.size()][nodeNum];
        kwHop=new int[queryList.size()][nodeNum];
        PriorityQueue<PriItem> pq=new PriorityQueue<PriItem>(PriItem.priItemComparator);
        KwdHL kwdHL_full=new KwdHL(kwdhl,diambound/2);


        for(int i=0;i<queryList.size();i++)
        {
            for(int j=0;j<queryList.get(i).size();j++)
            {
                int u=queryList.get(i).get(j);


                double priority = 1;
                for (int k = 0; k < queryList.size(); k++)
                {
                    if(k==i)
                    {
                        continue;
                    }
                    int hop=getHop(u,k,hl,kwdhl);
                    if(hop<=diambound)
                    {
                        priority++;
                    }
                }

                pq.add(new PriItem(u,u,priority,0,i,0));
                visit[u]=true;
            }
        }



        CertificateQueue min_maxWeightQueue=null;

        while(!pq.isEmpty())
        {
            if(System.currentTimeMillis()-starttime>=1000*1000) return null;
            PriItem pitem=pq.poll();
            if(pitem.pri<kwnum)
            {
                break;
            }
            else if(pitem.pri==kwnum)
            {
                double d_most=hl.getDist(pitem.v,diambound/2,kwdHL_full).dis;
                if(d_most*kwnum>=min_maxWeight) continue;

            }
            if(!checked[pitem.v])
            {
                CertificateQueue q;

                q=GenerateQueueEven(graph,hl,kwdhl,dwdArray,queryList,diambound,pitem.v,pitem.startKwd);
                checked[pitem.v]=true;
                if(q.kwnum>kwnum)
                {
                    kwnum=q.kwnum;
                    min_maxWeight=q.weightMax;
                    min_maxWeightQueue=q;
                }
                else if(q.kwnum==kwnum&&q.weightMin<min_maxWeight)
                {
                    if(q.weightMax<min_maxWeight)
                    {
                        min_maxWeight=q.weightMax;
                        min_maxWeightQueue=q;
                    }
                }
            }

            if(pitem.hop<Math.floor(((double)diambound)/2))
            {
                for(Edge e:graph.graph.get(pitem.v))
                {
                    int v_=e.v;
                    int hop_=pitem.hop+1;
                    if(!visit[v_])
                    {
                        double priority=1;
                        for(int i=0;i<queryList.size();i++)
                        {
                            if(i==pitem.startKwd) continue;
                            if(getHop(v_,i,hl,kwdhl)+hop_<=diambound)
                            {
                                priority++;
                            }
                        }

                        if(priority>=kwnum)
                        {
                            pq.add(new PriItem(pitem.start,v_,priority,hop_,pitem.startKwd,0));
                        }
                        visit[v_]=true;
                        h[v_]=hop_;
                    }
                    else if(hop_<h[v_])
                    {
                        double priority=1;
                        for(int i=0;i<queryList.size();i++)
                        {
                            if(i==pitem.startKwd) continue;
                            if(getHop(v_,i,hl,kwdhl)+hop_<=diambound)
                            {
                                priority++;
                            }
                        }

                        if(priority>=kwnum)
                        {
                            pq.add(new PriItem(pitem.start,v_,priority,hop_,pitem.startKwd,0));
                        }
                        h[v_]=hop_;
                    }
                }
            }
        }


        return min_maxWeightQueue;
    }

    CertificateQueue GenerateQueueEven(WeightedGraph graph,HopLimitHL hl,KwdHL[] kwdhl,DynamicWD[] dwdArray,ArrayList<ArrayList<Integer>> queryList,int diambound,int v,int startkwd)
    {
        //MaxM of article
        CertificateQueue q=new CertificateQueue(v);
        for(int i=0;i<queryList.size();i++)//foreach K
        {
            if(i==startkwd||getHop(v,i,hl,kwdhl)<=diambound/2)// if GetWD is not INF
            {
                DistOriginPair pair=hl.getDist(v,diambound/2,kwdhl[i]);//getWD(v,K[i],d/2)
                double weight=pair.dis;
                int loc=pair.origin;
                DistOriginPair dwdPair=dwdArray[i].get_wd(v,diambound/2);// my getWD
                assert abs(pair.dis-dwdPair.dis)<1e-5;
                q.addKwVertex(loc,weight);
            }
//            if(i==startkwd||getHop(v,i,hl,kwdhl)<=diambound/2){
//                DistOriginPair dwdPair=dwdArray[i].get_wd(v,diambound/2);
//                if(dwdPair!=null) {
//                    q.addKwVertex(dwdPair.origin, dwdPair.dis);
//                }
//            }
        }
        q.calculateWeightMin();
        return q;
    }



}