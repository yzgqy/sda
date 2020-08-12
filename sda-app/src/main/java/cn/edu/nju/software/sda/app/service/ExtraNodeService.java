package cn.edu.nju.software.sda.app.service;

import cn.edu.nju.software.sda.core.domain.node.Node;

import java.util.List;

/**
 * @Auther: yaya
 * @Date: 2020/6/4 21:10
 * @Description:
 */
public interface ExtraNodeService {

    List<Node> findExtraNode(String appId, String partitionId);
}
