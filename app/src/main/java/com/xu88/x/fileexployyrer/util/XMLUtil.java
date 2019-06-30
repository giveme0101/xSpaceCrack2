package com.xu88.x.fileexployyrer.util;

import android.util.Log;
import android.util.Xml;

import com.xu88.x.fileexployyrer.bean.SqlConnectionRecord;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class XMLUtil {

    /**
     * 解析XML转换成对象
     *
     * @param is 输入流
     * @param clazz 对象Class
     * @param fields 字段集合一一对应节点集合
     * @param elements  节点集合一一对应字段集合
     * @param itemElement 每一项的节点标签
     * @return
     */
    public static <T> List<T> parse(InputStream is, Class<T> clazz, List<String> fields, List<String> elements, String itemElement) {
        Log.v("rss", "开始解析XML.");
        List<T> list = new ArrayList<>();
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(is, "UTF-8");
            int event = xmlPullParser.getEventType();
            T obj = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (itemElement.equals(xmlPullParser.getName())) {
                            obj = clazz.newInstance();
                            setFieldValue(obj, "name", xmlPullParser.getAttributeValue(0));
                        }
                        if (obj != null && elements.contains(xmlPullParser.getName())) {
                            setFieldValue(obj, fields.get(elements.indexOf(xmlPullParser.getName())), xmlPullParser.nextText());
                        }
                    break;
                    case XmlPullParser.END_TAG:
                        if (itemElement.equals(xmlPullParser.getName())) {
                            list.add(obj);
                            obj = null;
                        }
                    break;
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e("rss", "解析XML异常：" + e.getMessage());
            throw new RuntimeException("解析XML异常：" + e.getMessage());
        }
        return list;
    }

    public static void writeToXml(List<SqlConnectionRecord> records, String rootName, String recordName, String path){

        PrintWriter pw = null;
        try {
            String buffer = writeToString(records, rootName, recordName);
            pw = new PrintWriter(new FileOutputStream(path));
            pw.append(buffer);
            pw.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (null != pw)
                pw.close();
        }
    }

    public static String  writeToString(List<SqlConnectionRecord> records, String rootName, String recordName){

        XmlSerializer serializer = Xml.newSerializer();
        Writer writer = new StringWriter();
        try{
            serializer.setOutput(writer);
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, rootName);

            for(SqlConnectionRecord record : records){
                serializer.startTag(null, recordName);
                serializer.attribute(null,"name", record.getName());

                addTag(record, serializer);

                serializer.endTag(null, recordName);
            }
            serializer.endTag(null, rootName);
            serializer.endDocument();

        }catch (Exception e) {
            Log.i("", e.getMessage());
        }
        return writer.toString();
    }

    private static void addTag(SqlConnectionRecord record, XmlSerializer serializer){
        Field[] fields = record.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                field.setAccessible(true);
                String name = field.getName();
                String value = (String) field.get(record);
                serializer.startTag(null, name);
                serializer.text(value);
                serializer.endTag(null, name);
            } catch (Exception ex){
            }
        }
    }

    /**
     * 设置字段值
     *
     * @param propertyName 字段名
     * @param obj 实例对象
     * @param value 新的字段值
     * @return
     */
    public static void setFieldValue(Object obj, String propertyName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}
