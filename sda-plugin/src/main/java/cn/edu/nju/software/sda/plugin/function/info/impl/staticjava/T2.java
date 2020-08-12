package cn.edu.nju.software.sda.plugin.function.info.impl.staticjava;

import cn.edu.nju.software.sda.core.utils.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: yaya
 * @Date: 2020/6/5 08:29
 * @Description:
 */
public class T2 {
    public static void main(String[] args) throws Exception {
        T2 t2 = new T2();
        Map<String, Integer> javaCount = t2.javaCount();
        for (Map.Entry<String, Integer> entry : javaCount.entrySet()) {
            System.out.println(entry.getKey() + "   " + entry.getValue());
        }
    }

    public Map<String, Integer> javaCount() {
        ArrayList<String> myfiles = new ArrayList<String>();
        String path = "/Users/yaya/Desktop/bs-project/jpetstore-6/spring-jpetstore/src/main/java";
        traverseFolder(path, myfiles);
        List<String> lines = new ArrayList<>();

        Map<String, Integer> javaCount = new HashMap<>();
        int sun = 0;
        for (String classfile : myfiles) {
            if (classfile.endsWith(".java")) {
                int row = 0;
                FileReader fr = null;//创建文件输入流
                try {
                    fr = new FileReader(new File(classfile));

                    BufferedReader in = new BufferedReader(fr);//包装文件输入流，可整行读取
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        row++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sun += row;

                String className = toClassName(classfile);
                javaCount.put(className, row);
//                System.out.println(classfile);
//                System.out.println(className);
//                System.out.println(row);

            }
        }
//        System.out.println(sun);
        return javaCount;
    }

    public String toClassName(String path) {
//        path = "/Users/yaya/Desktop/bs-project/jpetstore-6/spring-jpetstore/src/main/java/ik/am/jpetstore/domain/model/Item.java";
        int index = path.lastIndexOf(".");

        int index2 = path.indexOf("ik/am/jpetstore");
//        System.out.println(index+"  "+index2);
        String p = path.substring(index2, index).replace("/", ".");
//        System.out.println(p);

        return p;
    }

    public void traverseFolder(String path, ArrayList<String> myfiles) {

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("文件夹是空的!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        traverseFolder(file2.getAbsolutePath(), myfiles);
                    } else {

                        if (file2.getName().endsWith(".java")) {
                            myfiles.add(file2.getAbsolutePath());
//                            System.out.println("文件:" + file2.getAbsolutePath());
                        }

                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
}
