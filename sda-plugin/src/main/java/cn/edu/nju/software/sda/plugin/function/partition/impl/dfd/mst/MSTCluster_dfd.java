package cn.edu.nju.software.sda.plugin.function.partition.impl.dfd.mst;

import cn.edu.nju.software.sda.plugin.function.partition.impl.mst.util.*;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: yaya
 * @Date: 2020/3/19 11:23
 * @Description:
 */
public class MSTCluster_dfd {
    private final static ComponentComparator componentComparator = new ComponentComparator();
    private final static ClassNodeComparator classNodeComparator = new ClassNodeComparator();
    private final static WeightedEdgeComparator weightedEdgeComparator = new WeightedEdgeComparator();

    private MSTCluster_dfd(){

    }

    public static Set<Component> clusterWithSplit(SpanningTreeAlgorithm.SpanningTree<WeightedEdge> edges, int splitThreshold, int numServices){
//        return  new HashSet<>(ConnectedComponents.connectedComponents(computeClusters(edges, numServices)));
        List<Component> components = ConnectedComponents.connectedComponents(computeClusters(edges, numServices));

        while (components.size()>0){
            components.sort(componentComparator);

            Collections.reverse(components);

            Component largest = components.get(0);

            if(largest.getSize()>splitThreshold){
                components.remove(0);
                List<Component> split = splitByDegree(largest);
                components.addAll(split);
            }else{
                return new HashSet<>(components);
            }
        }
        return new HashSet<>(components);
    }

    private static List<Component> splitByDegree(Component component){
        List<ClassNode> nodes = component.getNodes();
        nodes.sort(classNodeComparator);
        Collections.reverse(nodes);

        ClassNode nodeToRemove = nodes.get(0);
        nodes.remove(0);

        nodes.forEach(node -> {
            node.deleteNeighborWithId(nodeToRemove.getClassName());
        });

        List<Component> connectedComponents = ConnectedComponents.connectedComponentsFromNodes(nodes);
        return connectedComponents.stream().filter(c -> c.getSize() > 1).collect(Collectors.toList());
    }

    public static List<WeightedEdge> computeClusters(SpanningTreeAlgorithm.SpanningTree<WeightedEdge> edges, int numServices){
        Set<WeightedEdge> edgeSet = new HashSet<>();
        Iterator<WeightedEdge> iterator = edges.iterator();
        while (iterator.hasNext()){
            edgeSet.add(iterator.next());
        }
        List<WeightedEdge> edgeList = edgeSet.stream().collect(Collectors.toList());
        List<WeightedEdge> oldList = null;
        Collections.sort(edgeList, weightedEdgeComparator);
        Collections.reverse(edgeList);
        int numConnectedComponents = 1;
        int lastNumConnectedComponents = 1;
        int wantNumComponent = numServices;

        //不该删的边
        List<WeightedEdge> edgeTempList = new ArrayList<>();

        do{
            oldList = new ArrayList<>(edgeList);

            WeightedEdge edgeTemp = edgeList.get(0);

            edgeList.remove(0);
            numConnectedComponents = ConnectedComponents.numberOfComponents(edgeList);
            if(lastNumConnectedComponents >= numConnectedComponents){
//                return oldList;
                edgeTempList.add(edgeTemp);
            }else {
                lastNumConnectedComponents = numConnectedComponents;
            }
        }while ((numConnectedComponents < wantNumComponent)&&(!edgeList.isEmpty()));
        edgeList.addAll(edgeTempList);
        return edgeList;
    }
}
