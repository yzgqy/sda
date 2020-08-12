package cn.edu.nju.software.sda.app.service.impl;

import cn.edu.nju.software.sda.app.entity.NodeEntity;
import cn.edu.nju.software.sda.app.entity.PartitionNodeEntity;
import cn.edu.nju.software.sda.app.mock.dto.GraphDto;
import cn.edu.nju.software.sda.app.service.ExtraNodeService;
import cn.edu.nju.software.sda.app.service.NodeService;
import cn.edu.nju.software.sda.app.service.PartitionDetailService;
import cn.edu.nju.software.sda.app.service.PartitionNodeService;
import cn.edu.nju.software.sda.core.domain.PageQueryDto;
import cn.edu.nju.software.sda.core.domain.node.Node;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Auther: yaya
 * @Date: 2020/6/4 21:10
 * @Description:
 */

@Service
public class ExtraNodeServiceImpl implements ExtraNodeService {
    @Autowired
    private PartitionNodeService partitionNodeService;
    @Autowired
    private PartitionDetailService partitionDetailService;
    @Autowired
    private NodeService nodeService;

    @Override
    public List<Node> findExtraNode(String appId, String partitionId) {
        List<PartitionNodeEntity> partitionNodeEntities = partitionNodeService.queryPartitionResult(partitionId);

        Set<Node> partitionNodes = new HashSet<>();
        for(PartitionNodeEntity partitionNodeEntity:partitionNodeEntities){
            PageQueryDto<NodeEntity> dto = partitionDetailService.queryPartitionDetailByResultIdPaged(PageQueryDto.create(1, 1000), partitionNodeEntity.getId());
            for(NodeEntity nodeEntity:dto.getResult()){
                partitionNodes.add(nodeEntity.toNode());
            }
        }

        List<NodeEntity> nodeEntities = nodeService.findClassNodeByAppid(appId);
        Set<Node> appNodes = new HashSet<>();
        for(NodeEntity nodeEntity:nodeEntities){
            appNodes.add(nodeEntity.toNode());
        }

        List<Node> extraNodes = new ArrayList<>();
        for(Node node :appNodes){
            if(partitionNodes.contains(node))
                continue;
            else
                extraNodes.add(node);
        }

        return extraNodes;
    }
}
