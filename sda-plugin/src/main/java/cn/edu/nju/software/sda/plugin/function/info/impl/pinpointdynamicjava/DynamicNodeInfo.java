package cn.edu.nju.software.sda.plugin.function.info.impl.pinpointdynamicjava;

import cn.edu.nju.software.sda.core.domain.node.MethodNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @Auther: yaya
 * @Date: 2020/2/19 14:29
 * @Description:
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class DynamicNodeInfo implements Serializable {
    private String name;

    private String className;

    private Set<String> SQLs = new HashSet<>();

    private Set<String> tables = new HashSet<>();

    private int index;

    public MethodNode toMethodNode(){
        MethodNode methodNode = new MethodNode();
        String nodeName = name.replace(",",";");
        int index = nodeName.lastIndexOf(")");
        char c = nodeName.charAt(index-1);
        if(c != '('){
            String pre = nodeName.substring(0,index);
            nodeName = pre+";"+")";
        }
        methodNode.setMethodName(nodeName);
        methodNode.setName(nodeName);
        StringBuilder sb = new StringBuilder();
        if(tables!=null&&tables.size()>0) {
            for (String table : this.tables) {
                String[] ts = table.split(",");
                for(int i = 0;i<ts.length;i++) {
                    sb.append(ts[i]);
                    sb.append(";");
                }
            }
            methodNode.setTables(sb.toString());
        }
        return methodNode;
    }


}
