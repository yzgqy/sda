package cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.kmeans;

import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: yaya
 * @Date: 2020/3/12 19:44
 * @Description:
 */

// 邻接表中表的顶点
@Setter
@Getter
public class VNode {
    int ivex;               //顶点位置
    String data;          // 顶点信息
    ENode firstEdge;    // 指向第一条依附该顶点的弧
    int degree;
    double sumWeight;

}
