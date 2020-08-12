package cn.edu.nju.software.sda.plugin.function.info.impl.tableschema;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

/**
 * @Auther: yaya
 * @Date: 2020/2/19 22:47
 * @Description:
 */
public class MySQLUtil {
    private Map<String, String> tableMap = new HashMap<>();//别名->表名

    public static void main(String[] args) {
        MySQLUtil mySQLUtil = new MySQLUtil();
        Map<String, List> data = mySQLUtil.getForeignKeyFromDDLFile(new File("/Users/yaya/Desktop/bs-project/jpetstore-6/spring-jpetstore/src/main/resources/database/H2-schema.sql"));
        List<List<String>> fkys = data.get("fkeys");
        for (List<String> list:fkys){
                System.out.println(list.get(0)+"->"+list.get(1));
        }
        List<String> tableNames = data.get("tables");
        for(String t:tableNames){
            System.out.println(t);
        }


    }

    //解析DDL语句，获取表名和外键关系
    public Map<String, List> getForeignKeyFromDDLFile(File ddlFile){
        Map<String, List> data = new HashMap<>();
        List<List<String>> fks = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        String dDLs = null;
        try {
            dDLs = FileUtils.readFileToString(ddlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Statements statements = null;
        try {
            statements = CCJSqlParserUtil.parseStatements(dDLs);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        statements.getStatements()
                .stream().filter(statement -> (statement instanceof CreateTable))
                .map(statement -> (CreateTable) statement).forEach(ct -> {
            String tableName = ct.getTable().getName();
            tableNames.add(tableName.toUpperCase());
            List<Index> indexes = ct.getIndexes();
            if(indexes.size()>0)
                for(Index index:indexes){
                    if(index instanceof ForeignKeyIndex){
                        ForeignKeyIndex foreignKeyIndex = (ForeignKeyIndex)index;
                        String fkTableName = foreignKeyIndex.getTable().getName();
                        List<String> fk = new ArrayList<>();
                        fk.add(tableName.toUpperCase());
                        fk.add(fkTableName.toUpperCase());
                        fks.add(fk);
                    }
                }
        });
        data.put("tables",tableNames);
        data.put("fkeys",fks);
        return data;
    }

    //解析sql语句，提取表和关联关系
    public Map<String, List<String>> getTable(List<String> SQLs){
        Map<String, List<String>> data = new HashMap<>();
        List<TableAssociation> tableAssociations = new ArrayList<>();

        Set<String> tableNameLine = new HashSet<>();

        for(String sql:SQLs) {
            try {
                getTableRelation(sql, tableAssociations);
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<String> tables = getTableName(sql);
            StringBuilder sb = new StringBuilder();
            for(String x:tables){
                sb.append(x);
                sb.append(",");
            }
            tableNameLine.add(sb.toString());
        }
        for(TableAssociation tableAssociation:tableAssociations) {
            System.out.println(tableAssociation.toString());
        }
        List<String> fkeyLine = new ArrayList<>();

        for(TableAssociation tableAssociation:tableAssociations){
            String line = tableAssociation.getLeftTable()+"."+tableAssociation.getLeftColumn()
                    +"="+tableAssociation.getRightTable()+"."+ tableAssociation.getRightColumn()+"@@@";
            fkeyLine.add(line);
        }

        data.put("tables",new ArrayList<>(tableNameLine));
        data.put("fkey",fkeyLine);
        return data;
    }
    private List<String> getTableName(String sql) {
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
//        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);
        return tableList;
    }

    private void getTableRelation(String sql,List<TableAssociation> tableAssociations) throws Exception {
        Statement stmt = CCJSqlParserUtil.parse(sql);
        if (stmt instanceof Select) {
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            alise(selectBody,tableAssociations);
        }
    }

    private void alise(SelectBody selectBody,List<TableAssociation> tableAssociations) {
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            FromItem fromItem = plainSelect.getFromItem();
            List<Join> joins = plainSelect.getJoins();

            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                if(table.getAlias()!=null)
                    tableMap.put(table.getAlias().getName(), table.getName());
                else
                    tableMap.put(table.getName(), table.getName());
            }else if(fromItem instanceof SubSelect) {
                SubSelect subSelect = (SubSelect) fromItem;
                SelectBody subSelectBody = subSelect.getSelectBody();
                alise(subSelectBody,tableAssociations);
            }

            if(joins != null) {
                for (Join join : joins) {
                    Expression onExpression = join.getOnExpression();
                    FromItem rightItem = join.getRightItem();
                    if (rightItem instanceof Table) {
                        Table table = (Table) rightItem;
                        if(table.getAlias()!=null)
                            tableMap.put(table.getAlias().getName(), table.getName());
                        else
                            tableMap.put(table.getName(), table.getName());
                    }
                    doExpression(onExpression,tableAssociations);
                }
            }
            doExpression(plainSelect.getWhere(),tableAssociations);
        }else if(selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selects = setOperationList.getSelects();
            for (SelectBody selectBody3 : selects) {
                alise(selectBody3,tableAssociations);
            }
        }
    }

    private void doExpression(Expression expression,List<TableAssociation> tableAssociations) {
        if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            Expression rightExpression = equalsTo.getRightExpression();
            Expression leftExpression = equalsTo.getLeftExpression();
            if (rightExpression instanceof Column && leftExpression instanceof Column) {
                Column rightColumn = (Column) rightExpression;
                Column leftColumn = (Column) leftExpression;
                tableAssociations.add(new TableAssociation(
                        tableMap.get(rightColumn.getTable().toString()),
                        rightColumn.getColumnName(),
                        tableMap.get(leftColumn.getTable().toString()),
                        leftColumn.getColumnName()));
                System.out.println(tableMap.get(rightColumn.getTable().toString()) + "表的" + rightColumn.getColumnName() + "字段 -> "
                        + tableMap.get(leftColumn.getTable().toString()) + "表的" + leftColumn.getColumnName() + "字段");
            }
        }else if(expression instanceof AndExpression){
            AndExpression andExpression = (AndExpression) expression;
            Expression leftExpression = andExpression.getLeftExpression();
            doExpression(leftExpression,tableAssociations);
            Expression rightExpression = andExpression.getRightExpression();
            doExpression(rightExpression,tableAssociations);
        }
    }
}
