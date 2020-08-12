package cn.edu.nju.software.sda.plugin.function.evaluation.impl.metric_v2;

import cn.edu.nju.software.sda.core.domain.dto.InputData;
import cn.edu.nju.software.sda.core.domain.dto.ResultDto;
import cn.edu.nju.software.sda.core.domain.evaluation.Evaluation;
import cn.edu.nju.software.sda.core.domain.evaluation.EvaluationInfo;
import cn.edu.nju.software.sda.core.domain.evaluation.Indicator;
import cn.edu.nju.software.sda.core.domain.info.InfoSet;
import cn.edu.nju.software.sda.core.domain.info.PairRelation;
import cn.edu.nju.software.sda.core.domain.info.PairRelationInfo;
import cn.edu.nju.software.sda.core.domain.info.PartitionInfo;
import cn.edu.nju.software.sda.core.domain.meta.MetaData;
import cn.edu.nju.software.sda.core.domain.meta.MetaInfoDataItem;
import cn.edu.nju.software.sda.core.domain.node.Node;
import cn.edu.nju.software.sda.core.domain.node.NodeSet;
import cn.edu.nju.software.sda.core.domain.partition.Partition;
import cn.edu.nju.software.sda.core.domain.partition.PartitionNode;
import cn.edu.nju.software.sda.core.domain.work.Work;
import cn.edu.nju.software.sda.plugin.function.evaluation.EvaluationAlgorithm;
import cn.edu.nju.software.sda.plugin.function.evaluation.impl.metric.ClassNodeInfo;
import cn.edu.nju.software.sda.plugin.function.evaluation.impl.metric.Community;
import cn.edu.nju.software.sda.plugin.function.evaluation.impl.metric.PartitionResult;
import cn.edu.nju.software.sda.plugin.function.evaluation.impl.util.StatisticalCalculation;

import java.util.*;

/**
 * @Auther: yaya
 * @Date: 2020/3/22 20:04
 * @Description:
 */
public class MetricV2EvaluationAlgorithm extends EvaluationAlgorithm {

    @Override
    public String getDesc() {
        return "SYS_Metric_V2";
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
        //获取数据
//        PartitionResult partitionResult = new PartitionResult(inputData);
        Map<String, Integer> partitionCodeSizeMap = new HashMap<>();
        Map<String, Integer> partitionTableSizeMap = new HashMap<>();
        Map<String, Double> partitionCoherenceMap = new HashMap<>();//内聚
        Map<String, Double> partitionCouplingMap = new HashMap<>();//耦合

        Map<String, String> nodePartitionMap = new HashMap<>();
        //社区数据
        PartitionInfo partitionInfo = (PartitionInfo) inputData.getInfoDataObjs().get(Partition.INFO_NAME_PARTITION).get(0);
        Set<PartitionNode> partitionNodeSet = partitionInfo.getPartition().getPartitionNodeSet();
        for (PartitionNode partitionNode : partitionNodeSet) {
            NodeSet nodeSet = partitionNode.getNodeSet();
            int row = 0;
            Set<String> tableSet = new HashSet<>();
            for (Node node : nodeSet) {
                nodePartitionMap.put(node.getName(), partitionNode.getId());
                if (node.getRowCount() != null)
                    row += node.getRowCount();
                if (node.getTables() != null) {
                    String[] tables = node.getTables().split(";");
                    for (int i = 0; i < tables.length; i++) {
                        tableSet.add(tables[i]);
                    }
                }
            }
            partitionCodeSizeMap.put(partitionNode.getName(), row);
            partitionTableSizeMap.put(partitionNode.getName(), tableSet.size());
            partitionCoherenceMap.put(partitionNode.getId(), 0.0);
            partitionCouplingMap.put(partitionNode.getId(), 0.0);
        }

        double couplingCount = 0.0;
        double coherenceCount = 0.0;

        //参考边调用数据
        PairRelationInfo pairRelationInfo1 = (PairRelationInfo) inputData.getInfoDataObjs().get(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT).get(0);
        PairRelationInfo pairRelationInfo2 = (PairRelationInfo) inputData.getInfoDataObjs().get(PairRelation.INFO_NAME_DYNAMIC_CLASS_CALL_COUNT).get(0);
        if (pairRelationInfo1 != null)
            for (PairRelation pairRelation : pairRelationInfo1) {
                Node sourceNode = pairRelation.getSourceNode();
                Node targetNode = pairRelation.getTargetNode();
                String sourcePartitionId = nodePartitionMap.get(sourceNode.getName());
                String targetPartitionId = nodePartitionMap.get(targetNode.getName());
                if (sourcePartitionId != null && targetPartitionId != null) {
                    if (sourcePartitionId.equals(targetPartitionId)) {
                        //内聚
                        Double count = partitionCoherenceMap.get(sourcePartitionId);
                        System.out.println(pairRelation.getValue());
                        count += pairRelation.getValue();
                        partitionCoherenceMap.put(sourcePartitionId, count);
                        coherenceCount += 1;
                    } else {
                        //耦合
                        Double count = partitionCouplingMap.get(sourcePartitionId);
                        count += pairRelation.getValue();
                        partitionCouplingMap.put(sourcePartitionId, count);
                        couplingCount += 1;
                    }
                }
            }
        if (pairRelationInfo2 != null) {
            for (PairRelation pairRelation : pairRelationInfo2) {
                Node sourceNode = pairRelation.getSourceNode();
                Node targetNode = pairRelation.getTargetNode();
                String sourcePartitionId = nodePartitionMap.get(sourceNode.getName());
                String targetPartitionId = nodePartitionMap.get(targetNode.getName());
                if (sourcePartitionId != null && targetPartitionId != null) {
                    if (sourcePartitionId.equals(targetPartitionId)) {
                        //内聚
                        Double count = partitionCoherenceMap.get(sourcePartitionId);
                        count += pairRelation.getValue();
                        partitionCoherenceMap.put(sourcePartitionId, count);
                        coherenceCount += 1;
                    } else {
                        //耦合
                        Double count = partitionCouplingMap.get(sourcePartitionId);
                        count += pairRelation.getValue();
                        partitionCouplingMap.put(sourcePartitionId, count);
                        couplingCount += 1;
                    }
                }
            }
        }

        double coherence = coherenceCount / (coherenceCount + couplingCount);
        double coupling = couplingCount / (coherenceCount + couplingCount);

        //返回结构
        Evaluation evaluation = new Evaluation();
        evaluation.addIndicator(new Indicator("coupling", String.valueOf(coupling), 2));
        evaluation.addIndicator(new Indicator("coherence", String.valueOf(coherence), 3));
        List<Double> listcoherence = new ArrayList<>();
        List<Double> listcoupling = new ArrayList<>();
        for (Map.Entry<String, Double> entry : partitionCoherenceMap.entrySet()) {
            listcoherence.add(entry.getValue());
        }
        for (Map.Entry<String, Double> entry : partitionCouplingMap.entrySet()) {
            listcoupling.add(entry.getValue());
        }
        evaluation.addIndicator(new Indicator("couplingCV",  String.valueOf(StatisticalCalculation.cv(listcoherence)),2));
        evaluation.addIndicator(new Indicator("coherenceCV", String.valueOf(StatisticalCalculation.cv(listcoupling)), 3));

        for (Map.Entry<String, Integer> entry : partitionCodeSizeMap.entrySet()) {
            evaluation.addIndicator(new Indicator("LOC", String.valueOf(entry.getValue()), 1, entry.getKey()));
        }
        for (Map.Entry<String, Integer> entry : partitionTableSizeMap.entrySet()) {
            evaluation.addIndicator(new Indicator("LOT", String.valueOf(entry.getValue()), 1, entry.getKey()));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EvaluationInfo evaluationInfo = new EvaluationInfo(evaluation);
        return new InfoSet(evaluationInfo);
//        return null;
    }

    @Override
    public String getName() {
        return "SYS_Metric_V2";
    }


}
