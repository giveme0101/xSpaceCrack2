package com.xu88.x.fileexployyrer.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Vector;

public class FileUtil {

    public static String mkdirs(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getPath();
    }

    public static File createFile(String path, String name){
        try {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                File file = new File(folder, name);
                if (!file.exists()) {
                    file.createNewFile();
                }
                return file;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }


    public static Vector<String> getFileList(File basepath, String path) {
        Vector<String> vecFile = new Vector<>();
        File file = new File(basepath, path);
        File[] subFile = file.listFiles();

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                vecFile.add("    " + filename + "\n");
            } else {
                String filename = subFile[iFileLength].getName();
                vecFile.add(filename + "\n");
            }
        }
        return vecFile;
    }

    public static void appendFile(File basepath, String path, String name, String buffer) {
        writeFile(basepath.getPath() + path, name, buffer, true);
    }

    public static File writeFile(String path, String name, String buffer, Boolean append){

        PrintWriter pw = null;
        try {
            File file = null;
            if (null != path){
                File fpath = new File(path);
                if (!fpath.exists()) {
                    fpath.mkdirs();
                }
                file = new File(fpath, name);
            } else {
                file = new File(name);
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            pw = new PrintWriter(new FileOutputStream(file, append));
            pw.println(buffer);

            return file;
        } catch (Exception ex){
            ex.printStackTrace();
        } finally {
            if (null != pw)
                pw.close();
        }

        return null;
    }

    /**
     * 获得文件的大小
     *
     * @param f
     * @return
     */
    public static String getFileSize(File f) {
        long size = 0;
        try {
            if (f.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(f);
                size = fis.available();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formatFileSize(size);
    }

    /**
     * 转换文件的大小以B,KB,M,G等计算
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.000");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static void copy(File sourceFile, File distPath) throws Exception{
        if (sourceFile.isFile()){
            FileUtil.copyFile(new File(sourceFile.getParent() + File.separator), sourceFile.getName(), new File(distPath.getPath() + File.separator), sourceFile.getName());
        } else {
            FileUtil.copyFolder(new File(sourceFile.getParent() + File.separator), sourceFile.getName(), new File(distPath.getPath() + File.separator), sourceFile.getName());
        }
    }

    public static void copyFile(File sourcePath, String sourceName, File distPath, String distName) throws Exception{
        int bytesum = 0;
        int byteread = 0;
        File oldfile = new File(sourcePath, sourceName);
        if (!distPath.exists()){
            distPath.mkdirs();
        }
        File newFile = new File(distPath, distName);
        if (oldfile.exists()) {
            InputStream inStream = new FileInputStream(oldfile);
            FileOutputStream fs = new FileOutputStream(newFile);
            byte[] buffer = new byte[1444];
            int length;
            while ( (byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
        }
    }

    public static void copyFolder(File sourcePath, String sourceName, File distPath, String distName) throws Exception{
        File oldfile = new File(sourcePath, sourceName);
        File newFile = new File(distPath, distName);

        if (!distPath.exists()){
            distPath.mkdirs();
        }

        File[] files = oldfile.listFiles();
        for (File file : files){
            if (file.isFile()){
                copyFile(new File(file.getParent() + File.separator), file.getName(), new File(newFile.getPath() + File.separator), file.getName());
            } else {
                copyFolder(new File(file.getParent() + File.separator), file.getName(), new File(newFile.getPath() + File.separator), file.getName());
            }
        }
    }

    /**
     * 删除已存储的文件
     */
    public static void deletefile(File path, String fileName) {
        try {
            File file = new File(path, fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Boolean deleteFolder(File file){

        if (null == file)
            return Boolean.TRUE;

        if (file.isFile()) {
            file.delete();
            return Boolean.TRUE;
        }

        if (file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files){
                deleteFolder(f);
            }
            file.delete();
        }

        return Boolean.TRUE;
    }

    /**
     * 读取文件里面的内容
     *
     * @return
     */
    public static String getFile(File path, String fileName) {
        BufferedReader reader = null;
        try {
            File file = null;
            if (null != path){
                file = new File(path,fileName);
            } else {
                file = new File(fileName);
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String buffer = null;
            while (null != (buffer = reader.readLine())){
                sb.append(buffer)
                    .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != reader){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
