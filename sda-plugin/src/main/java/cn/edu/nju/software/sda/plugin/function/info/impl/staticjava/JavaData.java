package cn.edu.nju.software.sda.plugin.function.info.impl.staticjava;

import cn.edu.nju.software.sda.core.domain.info.*;
import cn.edu.nju.software.sda.core.domain.node.ClassNode;
import cn.edu.nju.software.sda.core.domain.node.MethodNode;
import cn.edu.nju.software.sda.core.domain.node.Node;
import cn.edu.nju.software.sda.core.domain.node.NodeSet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
public class JavaData {
    NodeSet nodeSet = new NodeSet();

    PairRelationInfo classEdges = new PairRelationInfo(PairRelation.INFO_NAME_STATIC_CLASS_CALL_COUNT);

    PairRelationInfo methodEdges = new PairRelationInfo(PairRelation.INFO_NAME_STATIC_METHOD_CALL_COUNT);

    public List<Info> getInfos(){
        clearUp();
        List<Info> infoList = new ArrayList<>();
        NodeInfo nodeInfo = new NodeInfo(nodeSet);
        infoList.add(nodeInfo);
        infoList.add(classEdges);
        infoList.add(methodEdges);
        return infoList;
    }

    /**
     * edges中两端必须都为nodeSet中节点，否则删除
     */
    private void clearUp(){
        // TODO
    }

    public String classNameFormat(String className){
        return className.replace("/",".");
    }

    public String methodNameFormat(String methodName){
        String methodNameTmp = methodName.replace("/",".").replace("(L","(").replace(";L",";");
        int index =methodNameTmp.lastIndexOf(")");
        return methodNameTmp.substring(0,index+1);
    }

    public void formatNodeSet(Map<String,Integer> classRowMap){
        NodeSet nodeSet = new NodeSet();
        NodeSet classNodes = this.nodeSet.getNodeSet(ClassNode.class);
        for(Node node:classNodes){
            String nodeName = node.getName();
            node.setName(classNameFormat(nodeName));
            node.setRowCount(classRowMap.get(node.getName()));
            if(nodeSet.getNodeByName(node.getSupperName())==null)
                node.setSupperName(null);
            String[] myinterfaces = node.getInterfaces().split(",");
            StringBuilder sb = new StringBuilder();
            for(int i=0 ;i<myinterfaces.length;i++){
                if(nodeSet.getNodeByName(myinterfaces[i])!=null){
                    sb.append(myinterfaces[i]);
                    sb.append(",");
                }
            }
            node.setInterfaces(sb.toString());
            nodeSet.addNode(node);
        }
        NodeSet methodNodes = this.nodeSet.getNodeSet(MethodNode.class);
        for(Node node:methodNodes){
            String nodeName = node.getName();
            node.setName(methodNameFormat(nodeName));
            nodeSet.addNode(node);
        }

        this.nodeSet=nodeSet;

        for(Node node: nodeSet){
            Node pnode = node.getParentNode();
            if(pnode!=null) {
                pnode.setName(classNameFormat(pnode.getName()));
                MethodNode methodNode = (MethodNode) node;
                String output = methodNode.getOutputClazz();
                List<String> input = methodNode.getInputClazz();
                StringBuilder sb = new StringBuilder();
                if(nodeSet.getNodeByName(output)!=null) {
                    sb.append(output);
                    sb.append(",");
                }
                for(String str:input){
                    if(nodeSet.getNodeByName(str)!=null) {
                        sb.append(str);
                        sb.append(",");
                    }
                }
                pnode.setParams(sb.toString());
            }
        }

        for(PairRelation r:classEdges){
            Node sourceNode= r.getSourceNode();
            sourceNode.setName(classNameFormat(sourceNode.getName()));
            Node targetNode= r.getTargetNode();
            targetNode.setName(classNameFormat(targetNode.getName()));
        }

        for(PairRelation r:methodEdges){
            Node sourceNode= r.getSourceNode();
            sourceNode.setName(methodNameFormat(sourceNode.getName()));
            Node targetNode= r.getTargetNode();
            targetNode.setName(methodNameFormat(targetNode.getName()));
        }

    }


}
