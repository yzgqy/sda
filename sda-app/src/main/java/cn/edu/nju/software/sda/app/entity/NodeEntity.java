package cn.edu.nju.software.sda.app.entity;

import cn.edu.nju.software.sda.core.NodeManager;
import cn.edu.nju.software.sda.core.domain.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@Table(name = "node")
public class NodeEntity implements Serializable {

    @Id
    private String id;

    @Column(name = "`name`")
    private String name;

    private String appId;

    private String attrs;

    private String clazz;

    private String parentNode;

    @Column(name = "`desc`")
    private String desc;

    private Integer flag;

    private Date createdAt;

    private Date updatedAt;

    private String tables;

    private Integer rowCount;

    private String supperName;

    private String interfaces;

    private String params;

    public static NodeEntity createNewEntity(String appId, Node node){
        if(node == null){
            throw new RuntimeException("node is null");
        }
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setId(node.getId());
        nodeEntity.setAppId(appId);
        nodeEntity.setName(node.getName());
        nodeEntity.setAttrs(node.getAttrStr());
        nodeEntity.setClazz(NodeManager.getNode(node.getClass()));
        if(node.getParentNode() != null)
            nodeEntity.setParentNode(node.getParentNode().getId());
        nodeEntity.setFlag(1);
        nodeEntity.setCreatedAt(new Date());
        nodeEntity.setUpdatedAt(new Date());
        nodeEntity.setRowCount(node.getRowCount());
        nodeEntity.setSupperName(node.getSupperName());
        nodeEntity.setInterfaces(node.getInterfaces());
        nodeEntity.setParams(node.getParams());
        return nodeEntity;
    }

    public static List<NodeEntity> createNewEntities(String appId, List<Node> nodeList){
        List<NodeEntity> nodeEntities = new ArrayList<>();
        for (Node node :
                nodeList) {
            nodeEntities.add(createNewEntity(appId, node));
        }
        return nodeEntities;
    }

    public NodeEntity createNodeEntityForUpdate(Node node){
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setId(id);

        // TODO 判断node是否需要更新，如父节点改变，attr改变等
        //如果nodeInfo不包含tables就不更新
        if(node.getTables()==null||node.getTables().isEmpty()||node.getTables().trim().equals("")){
            return null;
        }else {
            //如果该nodeEntity的tables不是空且等于nodeInfo的tables不更新
            if(this.tables!=null&&this.tables.equals(node.getTables()))
                return null;
            nodeEntity.setTables(node.getTables());
            return nodeEntity;
        }
    }

    public static Map<String, NodeEntity> toNameNodeMap(List<NodeEntity> nodeEntities){
        Map<String, NodeEntity> nodeEntityMap = new HashMap<>();
        for (NodeEntity nodeEntity:
             nodeEntities) {
            nodeEntityMap.put(nodeEntity.getName(), nodeEntity);
        }
        return nodeEntityMap;
    }

    public Node toNode(){
        Node node = null;
        try {
            node = (Node) NodeManager.getClass(getClazz()).newInstance();
            node.setAttrStr(getAttrs());
            node.setId(getId());
            node.setName(getName());
            node.setTables(getTables());
            node.setRowCount(getRowCount());
            node.setSupperName(getSupperName());
            node.setInterfaces(getInterfaces());
            node.setParams(getParams());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return node;
    }
}