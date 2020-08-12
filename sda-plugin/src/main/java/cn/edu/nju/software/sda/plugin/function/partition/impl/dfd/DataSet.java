package cn.edu.nju.software.sda.plugin.function.partition.impl.dfd;

import cn.edu.nju.software.sda.core.domain.info.PairRelation;
import cn.edu.nju.software.sda.core.domain.info.PairRelationInfo;
import cn.edu.nju.software.sda.core.utils.FileUtil;
import cn.edu.nju.software.sda.plugin.function.info.impl.gitcommit.entity.GitCommitFileEdge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: yaya
 * @Date: 2020/3/12 21:10
 * @Description:
 */
public class DataSet {
    private List<GitCommitFileEdge> calls = new ArrayList<>();
    private Map<String, Integer> clusterResults = new HashMap<>();
    private Map<Integer, String> indexMap = new HashMap<>();
    private Map<String,Integer> edgeMap = new HashMap<>();

    public DataSet(List<String> dynamicRelationInfo, List<String> boCluster){
        setClusterResults(boCluster);
        setCalls(dynamicRelationInfo);
    }

    public DataSet(List<String> dynamicRelationInfo,List<String> staticRelationInfo, List<String> boCluster){
        setClusterResults(boCluster);
        setCalls(dynamicRelationInfo);
        setCalls(staticRelationInfo);
    }

    private void setClusterResults(List<String> cluster){
        int index = 0;
        for(String item:cluster){
            String[] infos = item.split(",");
            for(int i=0;i<infos.length;i++){
                this.clusterResults.put(infos[i],index);
            }
            indexMap.put(index,item);
            index++;
        }
    }

    public Map<Integer, String> getIndexMap() {
        return indexMap;
    }

    private void setCalls(List<String> dynamicRelationInfo){

        for(String call:dynamicRelationInfo){
            String[] infos = call.split("@@@");
            String callee = infos[0].replace("/",".");
            String caller = infos[1].replace("/",".");
            String sourceName;
            String targetName;
            if(clusterResults.containsKey(callee)&&clusterResults.containsKey(caller))
                continue;

            if(clusterResults.containsKey(callee))
                sourceName = clusterResults.get(callee).toString();
            else
                sourceName = callee;
            if(clusterResults.containsKey(caller))
                targetName=clusterResults.get(caller).toString();
            else
                targetName=caller;

//            System.out.println("sourceName:  "+sourceName);
//            System.out.println("targetName:  "+targetName);
//            System.out.println("count:  "+infos[2]);

            if(sourceName.equals(targetName)) {
//                System.out.println("N");
                continue;
            }
//            System.out.println("Y");

            String key = sourceName+"@@@"+targetName;
            int newCount = Double.valueOf(infos[2]).intValue();
            if(edgeMap.containsKey(key)) {
//                System.out.println("+");
                int oldCount = edgeMap.get(key)+newCount;
                edgeMap.put(key,oldCount);
            }else {
                edgeMap.put(key,newCount);
            }
        }


    }


    public void printInde(){
        for(Map.Entry<Integer,String> entry:indexMap.entrySet()){
            System.out.println(entry.getKey()+":  "+entry.getValue());
        }
    }

    public List<GitCommitFileEdge> getCalls() {
        for(Map.Entry<String,Integer> entry:edgeMap.entrySet()){
            String key = entry.getKey();
            String[] keyStrs = key.split("@@@");
            GitCommitFileEdge edge = new GitCommitFileEdge();
            edge.setSourceName(keyStrs[0]);
            edge.setTargetName(keyStrs[1]);
            edge.setCount(entry.getValue());
//            System.out.println(edge.getSourceName()+"@@@"+edge.getTargetName()+"@@@"+edge.getCount());
            calls.add(edge);
        }
        return calls;
    }

    private String getClassName(String menthodName){
        int index1 = menthodName.indexOf("(");
        String tmpName = menthodName.substring(0,index1);
        int index2 = tmpName.lastIndexOf(".");
        return menthodName.substring(0,index2);
    }

}
