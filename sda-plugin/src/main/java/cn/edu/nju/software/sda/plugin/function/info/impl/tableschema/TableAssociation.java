package cn.edu.nju.software.sda.plugin.function.info.impl.tableschema;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @Auther: yaya
 * @Date: 2020/2/19 22:50
 * @Description:
 */
public class TableAssociation {
    private String rightTable;
    private String leftTable;
    private String rightColumn;
    private String leftColumn;

    @Override
    public String toString() {
        return "TableAssociation{" +
                "rightTable='" + rightTable + '\'' +
                ", leftTable='" + leftTable + '\'' +
                ", rightColumn='" + rightColumn + '\'' +
                ", leftColumn='" + leftColumn + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(rightTable)
                .append(rightColumn)
                .append(leftTable)
                .append(leftColumn)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null) return false;

        TableAssociation tableAssociation = (TableAssociation) obj;

        return new EqualsBuilder()
                .append(rightTable, tableAssociation.rightTable)
                .append(rightColumn, tableAssociation.rightColumn)
                .append(leftTable,tableAssociation.leftTable)
                .append(leftColumn,tableAssociation.leftColumn)
                .isEquals();
    }

    public TableAssociation(String rightTable, String rightColumn, String leftTable, String leftColumn) {
        this.rightTable = rightTable;
        this.leftTable = leftTable;
        this.rightColumn = rightColumn;
        this.leftColumn = leftColumn;
    }

    public void setRightTable(String rightTable) {
        this.rightTable = rightTable;
    }

    public void setLeftTable(String leftTable) {
        this.leftTable = leftTable;
    }

    public void setRightColumn(String rightColumn) {
        this.rightColumn = rightColumn;
    }

    public void setLeftColumn(String leftColumn) {
        this.leftColumn = leftColumn;
    }

    public String getRightTable() {
        return rightTable;
    }

    public String getLeftTable() {
        return leftTable;
    }

    public String getRightColumn() {
        return rightColumn;
    }

    public String getLeftColumn() {
        return leftColumn;
    }
}
