package cn.edu.nju.software.sda.plugin.function.evaluation.impl.metric_v3;

import cn.edu.nju.software.sda.core.domain.dto.InputData;
import cn.edu.nju.software.sda.core.domain.dto.ResultDto;
import cn.edu.nju.software.sda.core.domain.evaluation.Evaluation;
import cn.edu.nju.software.sda.core.domain.evaluation.EvaluationInfo;
import cn.edu.nju.software.sda.core.domain.evaluation.Indicator;
import cn.edu.nju.software.sda.core.domain.info.*;
import cn.edu.nju.software.sda.core.domain.meta.MetaData;
import cn.edu.nju.software.sda.core.domain.meta.MetaInfoDataItem;
import cn.edu.nju.software.sda.core.domain.node.ClassNode;
import cn.edu.nju.software.sda.core.domain.node.Node;
import cn.edu.nju.software.sda.core.domain.node.NodeSet;
import cn.edu.nju.software.sda.core.domain.node.TableNode;
import cn.edu.nju.software.sda.core.domain.partition.Partition;
import cn.edu.nju.software.sda.core.domain.partition.PartitionNode;
import cn.edu.nju.software.sda.core.domain.work.Work;
import cn.edu.nju.software.sda.plugin.function.evaluation.EvaluationAlgorithm;
import cn.edu.nju.software.sda.plugin.function.evaluation.impl.util.StatisticalCalculation;

import java.util.*;

/**
 * @Auther: yaya
 * @Date: 2020/3/24 21:05
 * @Description:
 */
public class MetricV3EvaluationAlgorithm extends EvaluationAlgorithm {

    @Override
    public String getDesc() {
        return "SYS_Metric_V3";
    }

    @Override
    public MetaData evaluationMetaData(MetaData metaData) {
        metaData.addMetaDataItem(new MetaInfoDataItem(Node.INFO_NAME_NODE));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT));
        metaData.addMetaDataItem(new MetaInfoDataItem(PairRelation.INFO_NAME_DYNAMIC_CLASS_CALL_COUNT));
        return metaData;
    }

    @Override
    public ResultDto check(InputData inputData) {
        return ResultDto.ok();
    }

    @Override
    public InfoSet work(InputData inputData, Work work) {
        Map<String, Integer> partitionCodeSizeMap = new HashMap<>();//分区名称->代码数
        Map<String, Integer> partitionTableSizeMap = new HashMap<>();//分区名称->数据库表数
        Map<String, Integer> partitionClassNumMap = new HashMap<>();//分区名称->类数

        Map<String, Integer> partitionCeMap = new HashMap<>(); //分区名称->对其他服务依赖类个数
        Map<String, Integer> partitionCaMap = new HashMap<>();//分区名称->被其他服务依赖类的个数

        Map<String, Integer> partitionInternalCountMap = new HashMap<>();//内部调用个数，接口、继承和参数
        Map<String, Set<String>> partitionInternalRMap = new HashMap<>();//内部调用边
//        Map<String, Integer> partitionexternalValueMap = new HashMap<>();//外部调用权重

        Map<String, String> nodePartitionMap = new HashMap<>();//结点名称->分区name

//        Map<String, List<PairRelation>> partitionInternalMap = new HashMap<>();//内部调用情况
        Map<String, List<PairRelation>> partitionCePMap = new HashMap<>();//对其他服务依赖类调用
        Map<String, List<PairRelation>> partitionCaPMap = new HashMap<>();//被其他服务依赖类调用

        Map<String, Set<Node>> partitionCeCMap = new HashMap<>();//对其他服务依赖类
        Map<String, Set<Node>> partitionCaCMap = new HashMap<>();//被其他服务依赖类


        //社区数据
        PartitionInfo partitionInfo = (PartitionInfo) inputData.getInfoDataObjs().get(Partition.INFO_NAME_PARTITION).get(0);
        Set<PartitionNode> partitionNodeSet = partitionInfo.getPartition().getPartitionNodeSet();
        for (PartitionNode partitionNode : partitionNodeSet) {
            NodeSet nodeSet = partitionNode.getNodeSet();
            int row = 0;
            int classNum = 0;
            int tableNum = 0;
//            Set<String> tableSet = new HashSet<>();
            for (Node node : nodeSet) {
                if (node.getClass().equals(ClassNode.class)) {
                    classNum++;
                }
                nodePartitionMap.put(node.getName(), partitionNode.getName());
                if (node.getRowCount() != null)
                    row += node.getRowCount();

                if (node.getClass().equals(TableNode.class)) {
                    tableNum++;
                }
//                if (node.getTables() != null) {
//                    String[] tables = node.getTables().split(";");
//                    for (int i = 0; i < tables.length; i++) {
//                        tableSet.add(tables[i]);
//                    }
//                }
            }
//            System.out.println(tableSet.toArray());
            partitionCodeSizeMap.put(partitionNode.getName(), row);
            partitionTableSizeMap.put(partitionNode.getName(),tableNum);
            partitionClassNumMap.put(partitionNode.getName(), classNum);
            partitionCeMap.put(partitionNode.getName(), 0);
            partitionCaMap.put(partitionNode.getName(), 0);
            partitionCeCMap.put(partitionNode.getName(), new HashSet<>());
            partitionCaCMap.put(partitionNode.getName(), new HashSet<>());
            partitionInternalCountMap.put(partitionNode.getName(), 0);
            partitionInternalRMap.put(partitionNode.getName(), new HashSet<>());
//            partitionexternalValueMap.put(partitionNode.getName(), 0);
        }

        //每个服务中的内部继承、接口、传参关系
        for (PartitionNode partitionNode : partitionNodeSet) {
            NodeSet nodeSet = partitionNode.getNodeSet();
            int relationNum = 0;
            String partitionName = partitionNode.getName();
            for (Node node : nodeSet) {
                if (node.getClass().equals(ClassNode.class)) {
                    String supper = node.getSupperName();
                    String[] interfaces = node.getInterfaces().split(",");
                    String[] params = node.getParams().split(",");
                    String supperPartitionName = nodePartitionMap.get(supper);
                    if (partitionName.equals(supperPartitionName))
                        relationNum++;
                    for (int i = 0; i < interfaces.length; i++) {
                        String interfacePartitionName = nodePartitionMap.get(interfaces[i]);
                        if (partitionName.equals(interfacePartitionName))
                            relationNum++;
                    }

                    for (int i = 0; i < params.length; i++) {
                        String paramsPartitionName = nodePartitionMap.get(params[i]);
                        if (partitionName.equals(paramsPartitionName))
                            relationNum++;
                    }
                }

            }

            partitionInternalCountMap.put(partitionNode.getName(), relationNum);
        }


        int irn = 0;//所有跨服务调用权重之和
        int totalValue = 0;//所有调用权重之和

        //参考边调用数据
        List<Info> staticInfo =inputData.getInfoDataObjs().get(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT);
        List<Info> dynamicInfo =inputData.getInfoDataObjs().get(PairRelation.INFO_NAME_DYNAMIC_CLASS_CALL_COUNT);
        if (staticInfo != null) {
            Info info = staticInfo.get(0);
            PairRelationInfo pairRelationInfo1 = (PairRelationInfo)info;
            for (PairRelation pairRelation : pairRelationInfo1) {
                totalValue += pairRelation.getValue();
                Node sourceNode = pairRelation.getSourceNode();
                Node targetNode = pairRelation.getTargetNode();
                String sourcePartitionName = nodePartitionMap.get(sourceNode.getName());
                String targetPartitionName = nodePartitionMap.get(targetNode.getName());
                if (sourcePartitionName != null && targetPartitionName != null) {
                    if (sourcePartitionName.equals(targetPartitionName)) {
                        //内部调用
                        Set<String> internalR = partitionInternalRMap.get(sourcePartitionName);
                        String rKey = sourceNode.getName() + "@@@" + targetNode.getName();
                        internalR.add(rKey);
                        partitionInternalRMap.put(sourcePartitionName, internalR);

                    } else {
                        //外部调用
                        irn += pairRelation.getValue();

                        int ceCount = partitionCeMap.get(sourcePartitionName);
                        ceCount++;
                        partitionCeMap.put(sourcePartitionName, ceCount);
                        int caCount = partitionCaMap.get(targetPartitionName);
                        caCount++;
                        partitionCaMap.put(targetPartitionName, caCount);


                        List<PairRelation> cep = partitionCePMap.get(sourcePartitionName);
                        if (cep != null) {
                            cep.add(pairRelation);
                        } else {
                            cep = new ArrayList<>();
                            cep.add(pairRelation);
                        }
                        partitionCePMap.put(sourcePartitionName, cep);

                        List<PairRelation> cap = partitionCaPMap.get(targetPartitionName);
                        if (cap != null) {
                            cap.add(pairRelation);
                        } else {
                            cap = new ArrayList<>();
                            cap.add(pairRelation);
                        }
                        partitionCaPMap.put(targetPartitionName, cap);

                        Set<Node> ceN = partitionCeCMap.get(sourcePartitionName);
                        if (ceN != null) {
                            ceN.add(pairRelation.getTargetNode());
                        } else {
                            ceN = new HashSet<>();
                            ceN.add(pairRelation.getTargetNode());
                        }
                        partitionCeCMap.put(sourcePartitionName, ceN);

                        Set<Node> caN = partitionCaCMap.get(targetPartitionName);
                        if (caN != null) {
                            caN.add(pairRelation.getSourceNode());
                        } else {
                            caN = new HashSet<>();
                            caN.add(pairRelation.getSourceNode());
                        }
                        partitionCaCMap.put(targetPartitionName, caN);
                    }
                }
            }
            }

        System.out.println("CE");
        for (Map.Entry<String, List<PairRelation>> entry : partitionCePMap.entrySet()) {
            System.out.println(entry.getKey());
            List<PairRelation> cep = entry.getValue();
            for (PairRelation p : cep) {
                System.out.println(p.getSourceNode().getName() + "->" + p.getTargetNode().getName());
            }
        }

        System.out.println("CA");
        for (Map.Entry<String, List<PairRelation>> entry : partitionCaPMap.entrySet()) {
            System.out.println(entry.getKey());
            List<PairRelation> cap = entry.getValue();
            for (PairRelation p : cap) {
                System.out.println(p.getSourceNode().getName() + "->" + p.getTargetNode().getName());
            }
        }

        System.out.println("CE");
        for (Map.Entry<String, Set<Node>> entry : partitionCeCMap.entrySet()) {
            System.out.println(entry.getKey());
            Set<Node> cep = entry.getValue();
            for (Node p : cep) {
                System.out.println(p.getName());
            }
        }

        System.out.println("CA");
        for (Map.Entry<String, Set<Node>> entry : partitionCaCMap.entrySet()) {
            System.out.println(entry.getKey());
            Set<Node> cep = entry.getValue();
            for (Node p : cep) {
                System.out.println(p.getName());
            }
        }


        if (dynamicInfo != null){
            Info info = dynamicInfo.get(0);
            PairRelationInfo pairRelationInfo2 = (PairRelationInfo)info;
            for (PairRelation pairRelation : pairRelationInfo2) {
                totalValue += pairRelation.getValue();
                Node sourceNode = pairRelation.getSourceNode();
                Node targetNode = pairRelation.getTargetNode();
                String sourcePartitionName = nodePartitionMap.get(sourceNode.getName());
                String targetPartitionName = nodePartitionMap.get(targetNode.getName());
                if (sourcePartitionName != null && targetPartitionName != null) {
                    if (sourcePartitionName.equals(targetPartitionName)) {
                        //内部调用
                        Set<String> internalR = partitionInternalRMap.get(sourcePartitionName);
                        String rKey = sourceNode.getName() + "@@@" + targetNode.getName();
                        internalR.add(rKey);
                        partitionInternalRMap.put(sourcePartitionName, internalR);

                    } else {
                        //外部调用
                        irn += pairRelation.getValue();

                        int ceCount = partitionCeMap.get(sourcePartitionName);
                        ceCount++;
                        partitionCeMap.put(sourcePartitionName, ceCount);
                        int caCount = partitionCaMap.get(targetPartitionName);
                        caCount++;
                        partitionCaMap.put(targetPartitionName, caCount);


                        List<PairRelation> cep = partitionCePMap.get(sourcePartitionName);
                        if (cep != null) {
                            cep.add(pairRelation);
                        } else {
                            cep = new ArrayList<>();
                            cep.add(pairRelation);
                        }
                        partitionCePMap.put(sourcePartitionName, cep);

                        List<PairRelation> cap = partitionCaPMap.get(targetPartitionName);
                        if (cap != null) {
                            cap.add(pairRelation);
                        } else {
                            cap = new ArrayList<>();
                            cap.add(pairRelation);
                        }
                        partitionCaPMap.put(targetPartitionName, cap);

                        Set<Node> ceN = partitionCeCMap.get(sourcePartitionName);
                        if (ceN != null) {
                            ceN.add(pairRelation.getTargetNode());
                        } else {
                            ceN = new HashSet<>();
                            ceN.add(pairRelation.getTargetNode());
                        }
                        partitionCeCMap.put(sourcePartitionName, ceN);

                        Set<Node> caN = partitionCaCMap.get(targetPartitionName);
                        if (caN != null) {
                            caN.add(pairRelation.getSourceNode());
                        } else {
                            caN = new HashSet<>();
                            caN.add(pairRelation.getSourceNode());
                        }
                        partitionCaCMap.put(targetPartitionName, caN);
                    }

                }
            }
            }

//        Map<String, Set<String>> partitionInternalRMap
        for (Map.Entry<String, Set<String>> entry : partitionInternalRMap.entrySet()) {
            String key = entry.getKey();
            int count = entry.getValue().size();
            int internalCount = partitionInternalCountMap.get(key);
            internalCount +=count;
            partitionInternalCountMap.put(key, internalCount);
        }

        System.out.println("CE");
        for (Map.Entry<String, List<PairRelation>> entry : partitionCePMap.entrySet()) {
            System.out.println(entry.getKey());
            List<PairRelation> cep = entry.getValue();
            for (PairRelation p : cep) {
                System.out.println(p.getSourceNode().getName() + "->" + p.getTargetNode().getName());
            }
        }

        System.out.println("CA");
        for (Map.Entry<String, List<PairRelation>> entry : partitionCaPMap.entrySet()) {
            System.out.println(entry.getKey());
            List<PairRelation> cap = entry.getValue();
            for (PairRelation p : cap) {
                System.out.println(p.getSourceNode().getName() + "->" + p.getTargetNode().getName());
            }
        }

        System.out.println("CE");
        for (Map.Entry<String, Set<Node>> entry : partitionCeCMap.entrySet()) {
            System.out.println(entry.getKey());
            Set<Node> cep = entry.getValue();
            for (Node p : cep) {
                System.out.println(p.getName());
            }
        }

        System.out.println("CA");
        for (Map.Entry<String, Set<Node>> entry : partitionCaCMap.entrySet()) {
            System.out.println(entry.getKey());
            Set<Node> cep = entry.getValue();
            for (Node p : cep) {
                System.out.println(p.getName());
            }
        }

        //返回结构
        Evaluation evaluation = new Evaluation();

        List<Double> locList = new ArrayList<>();
        List<Double> notList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : partitionCodeSizeMap.entrySet()) {
            locList.add(Double.valueOf(entry.getValue()));
            evaluation.addIndicator(new Indicator("LOC", String.valueOf(entry.getValue()), 1, entry.getKey()));
        }
        for (Map.Entry<String, Integer> entry : partitionTableSizeMap.entrySet()) {
            notList.add(Double.valueOf(entry.getValue()));
            evaluation.addIndicator(new Indicator("NOT", String.valueOf(entry.getValue()), 1, entry.getKey()));
        }

        double is = 0;


        List<Double> imList = new ArrayList<>();
        List<Double> rcList = new ArrayList<>();
        double rcSum = 0;
        for (Map.Entry<String, Set<Node>> entry : partitionCeCMap.entrySet()) {
            double ce = entry.getValue().size();
            double ca = partitionCaCMap.get(entry.getKey()).size();
            double im = 0;
            if (ce != 0 || ca != 0)
                im = ce / (ce + ca);
            imList.add(im);
            is += im;

            System.out.println(entry.getKey());
            System.out.println("Ce " + ce);
            System.out.println("Ca " + ca);
            System.out.println("I " + im);
            evaluation.addIndicator(new Indicator("Ce", String.valueOf(ce), 1, entry.getKey()));
            evaluation.addIndicator(new Indicator("Ca", String.valueOf(ca), 1, entry.getKey()));
            evaluation.addIndicator(new Indicator("I", String.valueOf(im), 1, entry.getKey()));

            int internalCount = partitionInternalCountMap.get(entry.getKey());
            int classCount = partitionClassNumMap.get(entry.getKey());

            double rc = Double.valueOf(internalCount) / Double.valueOf(classCount);
            rcSum += rc;
            System.out.println("RC " + internalCount + "/" + classCount + "=" + rc);
            evaluation.addIndicator(new Indicator("RC", String.valueOf(rc), 1, entry.getKey()));
            rcList.add(rc);
        }


        evaluation.addIndicator(new Indicator("LOC_Cv", String.valueOf(StatisticalCalculation.cv(locList)), 2));
        evaluation.addIndicator(new Indicator("NOT_Cv", String.valueOf(StatisticalCalculation.cv(notList)), 2));
        System.out.println("稳定性：" + imList);
        evaluation.addIndicator(new Indicator("I_Cv", String.valueOf(StatisticalCalculation.cv(imList)), 2));
        evaluation.addIndicator(new Indicator("RC_Cv", String.valueOf(StatisticalCalculation.cv(rcList)), 2));

        double is_avg = is / partitionNodeSet.size();
        double rc_avg = rcSum / partitionNodeSet.size();
        evaluation.addIndicator(new Indicator("I_Avg", String.valueOf(is_avg), 2));
        evaluation.addIndicator(new Indicator("RC_Avg", String.valueOf(rc_avg), 2));
        evaluation.addIndicator(new Indicator("IRN", String.valueOf(irn), 2));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EvaluationInfo evaluationInfo = new EvaluationInfo(evaluation);
        return new InfoSet(evaluationInfo);
    }

    @Override
    public String getName() {
        return "SYS_Metric_V3";
    }


}
