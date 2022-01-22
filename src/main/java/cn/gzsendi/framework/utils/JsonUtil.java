package cn.gzsendi.framework.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

//https://www.cnblogs.com/christopherchan/p/11071098.html
public class JsonUtil {

    private final static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    //日期格式化
    private static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static ObjectMapper objectMapper;

    static{

        /**
         * ObjectobjectMapper是JSON操作的核心，Jackson的所有JSON操作都是在ObjectobjectMapper中实现。
         * ObjectobjectMapper有多个JSON序列化的方法，可以把JSON字符串保存File、OutputStream等不同的介质中。
         * writeValue(File arg0, Object arg1)把arg1转成json序列，并保存到arg0文件中。
         * writeValue(OutputStream arg0, Object arg1)把arg1转成json序列，并保存到arg0输出流中。
         * writeValueAsBytes(Object arg0)把arg0转成json序列，并把结果输出成字节数组。
         * writeValueAsString(Object arg0)把arg0转成json序列，并把结果输出成字符串。
         */
        objectMapper = new ObjectMapper();

        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(STANDARD_FORMAT));

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);

        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);


    }

    /**
     * 对象转Json格式字符串
     * @param obj 对象
     * @return Json格式字符串
     */
    public static String toJSONString(Object o) {

        if (o == null) {
            return null;
        }

        if (o instanceof String)
            return (String) o;

        String jsonValue = null;
        try {
            jsonValue = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("Parse Object to String error",e);
        }

        return jsonValue;

    }

    @SuppressWarnings("unchecked")
    public static Map<String,Object> castToObject(String str){
        if(str == null || "".equals(str) ){
            return null;
        }

        try {
            return objectMapper.readValue(str, Map.class);
        } catch (Exception e) {
            logger.error("Parse String to Object error:", e);
            return null;
        }

    }

    /**
     * 字符串转换为自定义对象
     * @param str 要转换的字符串
     * @param clazz 自定义对象的class对象
     * @return 自定义对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T castToObject(String str, Class<T> clazz){
        if(str == null || "".equals(str) || clazz == null){
            return null;
        }

        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            logger.error("Parse String to Object error:", e);
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> T castToObject(String str, TypeReference<T> typeReference) {
        if (str == null || "".equals(str) || typeReference == null) {
            return null;
        }
        try {
            return (T) (typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            logger.error("Parse String to Object error:", e);
            return null;
        }
    }

    public static <T> T castToObject(String str, Class<?> collectionClazz, Class<?>... elementClazzes) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementClazzes);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            logger.error("Parse String to Object error : ", e.getMessage());
            return null;
        }
    }

    public static String getString(Map<String,Object> jsonObject, String fieldName){
        return jsonObject.get(fieldName)==null ? null : (String)jsonObject.get(fieldName);
    }

    public static Integer getInteger(Map<String,Object> jsonObject, String fieldName){
        return jsonObject.get(fieldName)==null ? null : new Double(jsonObject.get(fieldName).toString()).intValue();
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(Map<String,Object> jsonObject, String fieldName,Class<T> clazz){
        return jsonObject.get(fieldName)==null ? null : (List<T>)jsonObject.get(fieldName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] getArray(Map<String,Object> jsonObject, String fieldName,Class<T> clazz){
        return jsonObject.get(fieldName)==null ? null : (T[])jsonObject.get(fieldName);
    }

}