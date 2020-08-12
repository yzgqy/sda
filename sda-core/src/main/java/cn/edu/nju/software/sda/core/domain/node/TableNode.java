package cn.edu.nju.software.sda.core.domain.node;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Auther: yaya
 * @Date: 2020/2/20 13:19
 * @Description:
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TableNode  extends Node{
    private String type = "TABLE";

    public TableNode(String type) {
        this.type = type;
    }

    @Override
    public String getAttrStr() {
        return type;
    }

    @Override
    public void setAttrStr(String attrStr) {
        this.setType(attrStr);
    }
}
