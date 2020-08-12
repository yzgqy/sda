package cn.edu.nju.software.sda.plugin.function.info.impl.staticjava;

import cn.edu.nju.software.sda.core.domain.dto.InputData;
import cn.edu.nju.software.sda.core.domain.dto.ResultDto;
import cn.edu.nju.software.sda.core.domain.info.Info;
import cn.edu.nju.software.sda.core.domain.info.InfoSet;
import cn.edu.nju.software.sda.core.domain.meta.FormDataType;
import cn.edu.nju.software.sda.core.domain.meta.MetaData;
import cn.edu.nju.software.sda.core.domain.meta.MetaFormDataItem;
import cn.edu.nju.software.sda.core.domain.work.Work;
import cn.edu.nju.software.sda.core.utils.FileCompress;
import cn.edu.nju.software.sda.plugin.function.info.InfoCollection;
import net.sf.jsqlparser.statement.create.table.Index;
import org.springframework.asm.ClassReader;

import java.io.*;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticJavaInfoCollection extends InfoCollection {

    public static final String dataFormName = "Jar/War";
    public static final String packageFormName = "Package";

    @Override
    public MetaData getMetaData() {
        MetaData metaData = new MetaData();
        metaData.addMetaDataItem(new MetaFormDataItem(packageFormName, FormDataType.STRING));
        metaData.addMetaDataItem(new MetaFormDataItem(dataFormName, FormDataType.FILE));
        return metaData;
    }

    @Override
    public ResultDto check(InputData inputData) {
        return ResultDto.ok();
    }

    @Override
    public InfoSet work(InputData inputData, Work work) {

        File file = (File) inputData.getFormDataObjs(getMetaData()).get(dataFormName).get(0);

        String packageName = (String) inputData.getFormDataObjs(getMetaData()).get(packageFormName).get(0);

        ArrayList<String> myfiles = new ArrayList<String>();
        String path;
        String outPath = work.getWorkspace().getAbsolutePath();
        System.out.println("解压路径：" + outPath);
        FileCompress.unCompress(file.getAbsolutePath(), outPath);
        if (file.getName().trim().endsWith(".war"))
            path = outPath + "/WEB-INF/classes";
        else
            path = outPath;

        traverseFolder(path, myfiles);
        System.out.println("class文件数：" + myfiles.size());
        JavaData data = new JavaData();
        try {
            for (String classfile : myfiles) {
                if (classfile.endsWith(".class")) {
                    //统计代码行数
                    Integer row=0;

                    InputStream inputstream = new FileInputStream(new File(classfile));
                    ClassReader cr = new ClassReader(inputstream);
                    ClassAdapter ca = new ClassAdapter(data, packageName);
                    cr.accept(ca, ClassReader.EXPAND_FRAMES);

//                    FileReader fr=new FileReader(new File(classfile));//创建文件输入流
//                    BufferedReader in=new BufferedReader(fr);//包装文件输入流，可整行读取
//                    String line="";
//                    while((line=in.readLine()) != null) {
//                        row++;
//                    }
//                    classRowMap.put(toClassName(classfile),row);
//                    classRowMap.put(toClassName(classfile),1);
                }
            }
            T2 t2 = new T2();
            Map<String, Integer> classRowMap = t2.javaCount();
            data.formatNodeSet(classRowMap);
            return new InfoSet(data.getInfos());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    @Override
    public String getName() {
        return "sys_StaticJavaInfoCollection";
    }

    @Override
    public String getDesc() {
        return "collect java program static data.";
    }

    public static void main(String[] args) throws Exception{
        String outPath = "/Users/yaya/Desktop/a/";
        System.out.println("解压路径：" + outPath);
        String filename  = "/Users/yaya/Desktop/bs-project/jpetstore-6/spring-jpetstore/target/spring-jpetstore.war";
        String path = "";
        FileCompress.unCompress(filename, outPath);
        if (filename.trim().endsWith(".war"))
            path = outPath + "/WEB-INF/classes";
        else
            path = outPath;
        ArrayList<String> myfiles = new ArrayList<String>();
        traverseFolder(path, myfiles);
        System.out.println("class文件数：" + myfiles.size());
        Map<String,Integer> classRowMap = new HashMap<>();

        for (String classfile : myfiles) {
            if (classfile.endsWith(".class")) {
                //统计代码行数
                Integer row=0;
                FileReader fr=new FileReader(new File(classfile));//创建文件输入流
                BufferedReader in=new BufferedReader(fr);//包装文件输入流，可整行读取
                String line="";
                while((line=in.readLine()) != null) {
                    row++;
                }
                System.out.println(row+"   "+toClassName(classfile));
                classRowMap.put(classfile,row);
            }
        }
    }

    private static  String toClassName(String path){
//        int index = path.lastIndexOf('.');
//        String name = path.substring(0,index).replace("/",".");
//        int size = name.length();
//        String text = "ik.am.jpetstore";
//        int index2 = name.indexOf(text);
//        String className = name.substring(index2,size);
//        System.out.println(className);
//        return className;
        return path;
    }
}
