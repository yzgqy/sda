package cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.kmeans;

import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: yaya
 * @Date: 2020/3/12 19:44
 * @Description:
 */

//存放最短路径的实体
@Setter
@Getter
public class DijkstraResult {
    private String sourceData;
    private int sourceId;
    private String targetData;
    private int targetId;
    private double weigth;
}
