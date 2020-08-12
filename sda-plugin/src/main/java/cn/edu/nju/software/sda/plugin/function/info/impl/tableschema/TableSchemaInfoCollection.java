package cn.edu.nju.software.sda.plugin.function.info.impl.tableschema;

import cn.edu.nju.software.sda.core.domain.dto.InputData;
import cn.edu.nju.software.sda.core.domain.dto.ResultDto;
import cn.edu.nju.software.sda.core.domain.info.InfoSet;
import cn.edu.nju.software.sda.core.domain.info.NodeInfo;
import cn.edu.nju.software.sda.core.domain.info.PairRelation;
import cn.edu.nju.software.sda.core.domain.info.PairRelationInfo;
import cn.edu.nju.software.sda.core.domain.meta.FormDataType;
import cn.edu.nju.software.sda.core.domain.meta.MetaData;
import cn.edu.nju.software.sda.core.domain.meta.MetaFormDataItem;
import cn.edu.nju.software.sda.core.domain.node.NodeSet;
import cn.edu.nju.software.sda.core.domain.node.TableNode;
import cn.edu.nju.software.sda.core.domain.work.Work;
import cn.edu.nju.software.sda.core.exception.WorkFailedException;
import cn.edu.nju.software.sda.plugin.function.info.InfoCollection;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @Auther: yaya
 * @Date: 2020/2/19 22:43
 * @Description:
 */
public class TableSchemaInfoCollection extends InfoCollection {

    public static final String tableFormName = "Table Schema";

    @Override
    public MetaData getMetaData() {
        MetaData metaData = new MetaData();
        metaData.addMetaDataItem(new MetaFormDataItem(tableFormName, FormDataType.FILE));
        return metaData;
    }

    @Override
    public ResultDto check(InputData inputData) {
        return ResultDto.ok();
    }

    @Override
    public InfoSet work(InputData inputData, Work work) throws WorkFailedException {
        File file = (File) inputData.getFormDataObjs(getMetaData()).get(tableFormName).get(0);
        MySQLUtil mySQLUtil = new MySQLUtil();
        Map<String, List> tableData = mySQLUtil.getForeignKeyFromDDLFile(file);

        NodeSet nodeSet = new NodeSet();
        PairRelationInfo tableRelationInfo = new PairRelationInfo(PairRelation.INFO_NAME_TABLE_SCHEMA_FOREIGN_KEY);

        List<String> tableNames = tableData.get("tables");
        for(String t:tableNames){
            TableNode tableNode = new TableNode();
            tableNode.setName(t);
            nodeSet.addNode(tableNode);
        }
        List<List<String>> fkys = tableData.get("fkeys");
        for (List<String> list:fkys){
            PairRelation pairRelation = new PairRelation(1.0,
                    nodeSet.getNodeByName(list.get(0)), nodeSet.getNodeByName(list.get(1)));
            tableRelationInfo.addRelationByAddValue(pairRelation);
        }


        InfoSet info = new InfoSet();
        info.addInfo(new NodeInfo(nodeSet));
        info.addInfo(tableRelationInfo);
        return info;
    }

    @Override
    public String getName() {
        return "sys_TableSchemaInfoCollection";
    }

    @Override
    public String getDesc() {
        return "collect table data.";
    }
}
