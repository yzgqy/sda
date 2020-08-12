package cn.edu.nju.software.sda.core.domain.evaluation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Indicator {
    String name;

    String value;

    private Integer type;//1-粒度，2-耦合，3-内聚

    private String partitionNodeId;

    public Indicator() {

    }

    public Indicator(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Indicator(String name, String value,Integer type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Indicator(String name, String value,Integer type,String partitionNodeId) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.partitionNodeId = partitionNodeId;
    }
}
