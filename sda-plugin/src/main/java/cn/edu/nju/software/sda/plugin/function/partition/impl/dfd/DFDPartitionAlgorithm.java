package cn.edu.nju.software.sda.plugin.function.partition.impl.dfd;

import cn.edu.nju.software.sda.core.domain.dto.InputData;
import cn.edu.nju.software.sda.core.domain.dto.ResultDto;
import cn.edu.nju.software.sda.core.domain.info.*;
import cn.edu.nju.software.sda.core.domain.meta.FormDataType;
import cn.edu.nju.software.sda.core.domain.meta.MetaData;
import cn.edu.nju.software.sda.core.domain.meta.MetaFormDataItem;
import cn.edu.nju.software.sda.core.domain.meta.MetaInfoDataItem;
import cn.edu.nju.software.sda.core.domain.node.ClassNode;
import cn.edu.nju.software.sda.core.domain.node.MethodNode;
import cn.edu.nju.software.sda.core.domain.node.Node;
import cn.edu.nju.software.sda.core.domain.node.NodeSet;
import cn.edu.nju.software.sda.core.domain.partition.Partition;
import cn.edu.nju.software.sda.core.domain.partition.PartitionNode;
import cn.edu.nju.software.sda.core.domain.work.Work;
import cn.edu.nju.software.sda.core.exception.WorkFailedException;
import cn.edu.nju.software.sda.core.utils.FileUtil;
import cn.edu.nju.software.sda.plugin.function.info.impl.gitcommit.GitCommitData;
import cn.edu.nju.software.sda.plugin.function.info.impl.gitcommit.entity.GitCommitFileEdge;
import cn.edu.nju.software.sda.plugin.function.partition.PartitionAlgorithm;
import cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.kmeans.EData;
import cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.kmeans.GraphUtil;
import cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.kmeans.Kmeans;
import cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.mst.MSTCluster_dfd;
import cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.mst.MST_dfd;
import cn.edu.nju.software.sda.plugin.function.partition.impl.mst.util.Component;
import cn.edu.nju.software.sda.plugin.function.partition.impl.mst.util.MST;
import cn.edu.nju.software.sda.plugin.function.partition.impl.mst.util.MSTCluster;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Auther: yaya
 * @Date: 2020/2/20 14:09
 * @Description:
 */
public class DFDPartitionAlgorithm  extends PartitionAlgorithm {

    @Override
    public MetaData getMetaData() {
        MetaData metaData = new MetaData();
        metaData.addMetaDataItem(new MetaFormDataItem("SplitThreshold", FormDataType.STRING));
        metaData.addMetaDataItem(new MetaFormDataItem("NumServices", FormDataType.STRING));
        metaData.addMetaDataItem(new MetaFormDataItem("Centrepoints", FormDataType.STRING));
        metaData.addMetaDataItem(new MetaInfoDataItem(Node.INFO_NAME_NODE));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_DYNAMIC_METHOD_CALL_COUNT));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_TABLE_SCHEMA_FOREIGN_KEY));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_DYNAMIC_CLASS_CALL_COUNT));
        return metaData;
    }

    @Override
    public ResultDto check(InputData inputData) {
        return ResultDto.ok();
    }

    @Override
    public InfoSet work(InputData inputData, Work work) throws WorkFailedException {
        //获取form信息
        List<Object> splitThresholdObj =inputData.getFormDataObjs(getMetaData()).get("SplitThreshold");
        int splitThreshold = Integer.valueOf((String)splitThresholdObj.get(0));
        List<Object> numServicesObj =inputData.getFormDataObjs(getMetaData()).get("NumServices");
        int numServices = Integer.valueOf((String)numServicesObj.get(0));
        List<Object> centrepoints =inputData.getFormDataObjs(getMetaData()).get("Centrepoints");

        //获取info数据
        NodeSet nodeSet = ((NodeInfo) inputData.getInfoSet().getInfoByName(Node.INFO_NAME_NODE)).getNodeSet();
        NodeSet classNodeSet = nodeSet.getNodeSet(ClassNode.class);
        NodeSet methodNodeSet = nodeSet.getNodeSet(MethodNode.class);
        InfoSet infoSet = inputData.getInfoSet();
        PairRelationInfo tableRelationInfo = (PairRelationInfo) infoSet.getInfoByName(PairRelation.INFO_NAME_TABLE_SCHEMA_FOREIGN_KEY);
        PairRelationInfo dynamicRelationInfo = (PairRelationInfo) infoSet.getInfoByName(PairRelation.INFO_NAME_DYNAMIC_METHOD_CALL_COUNT);
        PairRelationInfo dynamicRelationInfoClass = (PairRelationInfo) infoSet.getInfoByName(PairRelation.INFO_NAME_DYNAMIC_CLASS_CALL_COUNT);
        PairRelationInfo staticRelationInfo = (PairRelationInfo) infoSet.getInfoByName(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT);

        List<String> edges = new ArrayList<>();
        if(dynamicRelationInfo!=null) {
            for (PairRelation pairRelation : dynamicRelationInfo) {
                if (!pairRelation.getTargetNode().getAttrStr().equals("TABLE")) {
                    String line = getClassName(pairRelation.getSourceNode()) + "@@@" + getClassName(pairRelation.getTargetNode())
                            + "@@@" + pairRelation.getValue();
                    System.out.println(line);
                    edges.add(line);
                }
            }
        }

//        String callsPathD="/Users/yaya/Desktop/data/3-包含跟多的类/dynamicClassCall.txt";
//        try {
//            edges = FileUtil.readFile(callsPathD);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        List<String> staticEdge = new ArrayList<>();
        if(staticRelationInfo!=null){
            for(PairRelation pairRelation:staticRelationInfo){
                if(!pairRelation.getTargetNode().getAttrStr().equals("TABLE")) {
                    String line = pairRelation.getSourceNode().getName() + "@@@" + pairRelation.getTargetNode().getName()
                            + "@@@" + pairRelation.getValue();
                    System.out.println(line);
                    staticEdge.add(line);
                }
        }
        }

        //step1 数据库表与相关操作聚类
        List<String>  boClusters = kmeans(methodNodeSet,tableRelationInfo,centrepoints);

        //step2 MST聚类
        Partition partition = mst(edges,staticEdge,boClusters,nodeSet,splitThreshold,numServices);

        Info info = new PartitionInfo(partition);
        String desc = "MST";

        desc += " "+splitThreshold + "-" + numServices;

        if(staticRelationInfo!=null)
            desc += " static";
        if(dynamicRelationInfo!=null){
            desc += " dynamic";
        }
        if(tableRelationInfo!=null){
            desc += " table";
        }
        info.setDesc(desc);
        return new InfoSet(info);
    }

    @Override
    public String getName() {
        return "SYS_DFD_0.0.1";
    }

    @Override
    public String getDesc() {
        return "DFD Algorithm";
    }

    public List<String> kmeans(NodeSet methodNodeSet,PairRelationInfo tableRelationInfo,List<Object> centrepoints){
        List<List<String>> rs = new ArrayList<>();
        Set<String> nodes = new HashSet<>();
        for(Node methodNode :methodNodeSet){
            if(methodNode.getTables()==null)
                continue;
            System.out.println(methodNode.getName());
            List<String> r = new ArrayList<>();
            String name = methodNode.getName().substring(0,methodNode.getName().indexOf("("));
            String className = name.substring(0,name.lastIndexOf("."));
//            String className = methodNode.getName().substring(0,methodNode.getName().indexOf("(")).substring(0,methodNode.getName().lastIndexOf("."));
            r.add(className);
            nodes.add(className);
            String[] table = methodNode.getTables().split(";");
            for(int i=0;i<table.length;i++){
                if(!table[i].isEmpty()){
                    nodes.add(table[i]);
                    r.add(table[i]);
                }
            }
            System.out.println(r);
            rs.add(r);
        }

        if(tableRelationInfo!=null)
            for(PairRelation pairRelation :tableRelationInfo){
                List<String> r = new ArrayList<>();
                r.add(pairRelation.getSourceNode().getName());
                r.add(pairRelation.getTargetNode().getName());
                nodes.add(pairRelation.getSourceNode().getName());
                nodes.add(pairRelation.getTargetNode().getName());
                rs.add(r);
            }
        Map<String,Integer> tbMap =new HashMap<>();
        Map<Integer,String> indexMap =new HashMap<>();
        int index = 0;
        for(String node:nodes){
            tbMap.put(node,index);
            indexMap.put(index,node);
            index++;
        }

        int[][]  matrix = new int[nodes.size()][nodes.size()];
        for(int i=0;i<nodes.size();i++){
            for(int j=0;j<nodes.size();j++){
                matrix[i][j] = 0;
            }
        }


        for(List<String> myr:rs){
            for(int i=0;i<myr.size();i++){
                for(int j=0;j<myr.size();j++){
                    matrix[tbMap.get(myr.get(i))][tbMap.get(myr.get(j))]=matrix[tbMap.get(myr.get(i))][tbMap.get(myr.get(j))]+1;
                }
            }
        }

        for(Map.Entry<String,Integer> entry:tbMap.entrySet()){
            System.out.println(entry.getKey()+"   "+entry.getValue());
        }
        for(int i=0;i<nodes.size();i++){
            for(int j=0;j<nodes.size();j++){
                System.out.print(matrix[i][j]);
                System.out.print(" ");
            }
            System.out.println(" ");
        }

        String[] vexs = new String[nodes.size()];
        index =0;
        for(String t:nodes){
            vexs[index] = t;
            index++;
        }
        List<EData> edgsList =new ArrayList<>();
        for(int i=0; i<nodes.size();i++){
            for(int j=0;j<nodes.size();j++){
                if(i==j)
                    continue;
                if(matrix[i][j]==0){
                    edgsList.add(new EData(indexMap.get(i),indexMap.get(j),Integer.MAX_VALUE));
//                    System.out.println("原weight："+matrix[i][j]+"   倒数："+Integer.MAX_VALUE);
                }else{
                    edgsList.add(new EData(indexMap.get(i),indexMap.get(j),1.0/matrix[i][j]));
//                    System.out.println("原weight："+matrix[i][j]+"   倒数："+1.0/matrix[i][j]);
                }
            }
        }

        EData[] edges = new EData[edgsList.size()];
        for(int i=0;i<edges.length;i++){
            edges[i] = edgsList.get(i);
        }

        GraphUtil pG;

        // 采用已有的"图"
        pG = new GraphUtil(vexs, edges);
//        String[] point = {"owners", "visits","vets"};
        String[] point = {"ACCOUNT", "ORDERS","PRODUCT"};
        Kmeans kmeans = new Kmeans(pG,3,point);
        List<Set<String>> graphs = kmeans.run();
        System.out.println("");
        System.out.println("打印结果：");
        int i = 1;
        List<String> clusters = new ArrayList<>();
        for(Set<String> graphUtil:graphs){
            System.out.println("第"+i+"类：");
            StringBuilder sb = new StringBuilder();
            for(String mypoint : graphUtil) {
                System.out.println(mypoint);
                sb.append(mypoint);
                sb.append(",");
            }
            i++;
            clusters.add(sb.toString());
        }
        return  clusters;
    }

    public Partition mst(List<String> dyedges,List<String> syedges,List<String> boCluster,NodeSet nodeSet, int splitThreshold,int numServices){
        Partition partition = new Partition();
        Set<PartitionNode> partitionNodeSet = new HashSet<>();
        partition.setPartitionNodeSet(partitionNodeSet);

        DataSet dataSet = new DataSet(dyedges,syedges,boCluster);
        dataSet.printInde();

        List<GitCommitFileEdge> edges = dataSet.getCalls();
        for(GitCommitFileEdge edge:edges){
            System.out.println(edge.getSourceName()+"@@@"+edge.getTargetName()+"@@@"+edge.getCount());
        }
        Set<Component> components = new HashSet<>(MSTCluster_dfd.clusterWithSplit(MST_dfd.calcMST(edges), splitThreshold,numServices));
        System.out.println("components.size = " + components.size());
        Integer communityCount = 0;
        for (Component cpt : components){
            System.out.println("*******************************one components "+ cpt.getNodes().size() +"******************************");
            PartitionNode partitionNode = new PartitionNode(communityCount.toString());
            NodeSet partitionNodes = new NodeSet();
            for (cn.edu.nju.software.sda.plugin.function.partition.impl.mst.util.ClassNode node: cpt.getNodes()){
                System.out.println(node.getClassName());
                if(isNumeric(node.getClassName())){
                    String cluster = dataSet.getIndexMap().get(Integer.valueOf(node.getClassName()));
                    String[] items = cluster.split(",");
                    for(String item :items){
                        partitionNodes.addNode(nodeSet.getNodeByName(item));
                    }
                }else{
                    partitionNodes.addNode(nodeSet.getNodeByName(node.getClassName()));

                }
                partitionNode.setNodeSet(partitionNodes);
                partitionNodeSet.add(partitionNode);
                communityCount++;
            }
        }
        return partition;
    }

    private boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    private String getClassName(Node node){
//        if(node.getAttrStr().equals("TABLE"))
//            return node.getName();
        String menthodName = node.getName();
        int index1 = menthodName.indexOf("(");
        String tmpName = menthodName.substring(0,index1);
        int index2 = tmpName.lastIndexOf(".");
        return menthodName.substring(0,index2);
    }

}
