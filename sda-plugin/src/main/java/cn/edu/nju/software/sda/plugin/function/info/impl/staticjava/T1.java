package cn.edu.nju.software.sda.plugin.function.info.impl.staticjava;

import cn.edu.nju.software.sda.core.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: yaya
 * @Date: 2020/4/23 15:33
 * @Description:
 */
public class T1 {
    public static void main(String[] args) throws Exception{
        ArrayList<String> myfiles = new ArrayList<String>();
        String path = "/Users/yaya/Desktop/bs-project/jpetstore-6/spring-jpetstore/target/classes";
        traverseFolder(path, myfiles);
        List<String> lines  = new ArrayList<>();
        for (String classfile : myfiles) {
            if (classfile.endsWith(".class")) {
                String classLine = classfile.substring(75,classfile.lastIndexOf(".")).replace("/",".");
                lines.add(classLine);
                FileUtil.writeFile(lines,"/Users/yaya/Desktop/jpetstore.txt");
                System.out.println(classLine);
            }
        }
    }

    public static void traverseFolder(String path, ArrayList<String> myfiles) {

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

                        if (file2.getName().endsWith(".class")) {
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
