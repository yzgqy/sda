package cn.edu.nju.software.sda.plugin.function.evaluation.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: yaya
 * @Date: 2020/3/24 21:09
 * @Description:
 */
public class StatisticalCalculation {
    //标准差σ=sqrt(s^2)
    public static double StandardDiviation(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        //reture Math.sqrt(dVar/(m-1));
        return Math.sqrt(dVar / m);
    }

    //变异系数
    public static double cvDouble(List<Double> x){
//        int[] array = {15,96,85,88,18,58,68,16,6,99,88,11,8,36,82,44,55,66};
        double sum = 0;
        for(int i=0;i<x.size();i++){
            sum += x.get(i);      //求出数组的总和
        }
        System.out.println(sum);  //939
        double average = sum/x.size();  //求出数组的平均数
        System.out.println(average);   //52.0
        int total=0;
        for(int i=0;i<x.size();i++){
            total += (x.get(i)-average)*(x.get(i)-average);   //求出方差，如果要计算方差的话这一步就可以了
        }
        double standardDeviation = Math.sqrt(total/x.size());   //求出标准差
        System.out.println(standardDeviation);    //32.55764119219941

        return standardDeviation/average;

    }
    //变异系数
    public static double cv(List<Double> x){
//        int[] array = {15,96,85,88,18,58,68,16,6,99,88,11,8,36,82,44,55,66};
        double sum = 0;
        for(int i=0;i<x.size();i++){
            sum += x.get(i)*100;      //求出数组的总和
        }
        double average = sum/x.size();  //求出数组的平均数
        System.out.println("均值："+average);   //52.0
        int total=0;
        for(int i=0;i<x.size();i++){
            total += (x.get(i)*100-average)*(x.get(i)*100-average);   //求出方差，如果要计算方差的话这一步就可以了
        }
        double standardDeviation = Math.sqrt(total/x.size());   //求出标准差
        System.out.println("标准差："+standardDeviation);    //32.55764119219941

        double cv = standardDeviation/average;
        System.out.println("变化系数："+cv);
        return cv;

    }

    public static void main(String[] args) {
        List<Double> x = new ArrayList<>();
        x.add(5.0);
        x.add(5.0);
        x.add(0.0);
        cv(x);
    }

    public static double cvInteger(List<Integer> x){
        double sum = 0;
        for(int i=0;i<x.size();i++){
            sum += x.get(i);      //求出数组的总和
        }
        System.out.println(sum);  //939
        double average = sum/x.size();  //求出数组的平均数
        System.out.println(average);   //52.0
        int total=0;
        for(int i=0;i<x.size();i++){
            total += (x.get(i)-average)*(x.get(i)-average);   //求出方差，如果要计算方差的话这一步就可以了
        }
        double standardDeviation = Math.sqrt(total/x.size());   //求出标准差
        System.out.println(standardDeviation);    //32.55764119219941

        return standardDeviation/average;

    }
}
