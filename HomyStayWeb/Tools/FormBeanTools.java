package com.HomyStayWeb.Tools;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class FormBeanTools {
    public <T> T formToClass(HttpServletRequest request, Class<T> target) {
        try {
            // 创建目标类实例
            T result = target.getDeclaredConstructor().newInstance();

            // 获取所有字段(包括私有字段)
            Field[] fields = target.getDeclaredFields();

            // 获取请求参数Map
            Map<String, String[]> parameterMap = request.getParameterMap();

            for (Field field : fields) {
                String fieldName = field.getName();
                if (parameterMap.containsKey(fieldName)) {
                    String[] values = parameterMap.get(fieldName);
                    try {
                        field.setAccessible(true);
                        Object convertedValue = convertValue(field.getType(), values);
                        field.set(result, convertedValue);
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("Failed to set field %s with value %s", fieldName, Arrays.toString(values)), e);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert form to class " + target.getName(), e);
        }
    }

    /**
     * 将字符串数组转换为目标类型的值
     */
    private Object convertValue(Class<?> targetType, String[] values) throws Exception {
        if (values == null || values.length == 0) {
            return null;
        }

        // 处理数组类型
        if (targetType.isArray()) {
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, values.length);
            for (int i = 0; i < values.length; i++) {
                Array.set(array, i, convertSingleValue(componentType, values[i]));
            }
            return array;
        }
        // 处理非数组类型(取第一个值)
        else {
            return convertSingleValue(targetType, values[0]);
        }
    }

    // 处理单个值的转换
    private static Object convertSingleValue(Class<?> targetType, String value) throws Exception {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == Date.class) {
            return parseDate(value);
        } else if (targetType == LocalDate.class) {
            return LocalDate.parse(value);
        } else if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        } else if (targetType.isEnum()) {
            @SuppressWarnings("unchecked") Enum<?> enumValue = Enum.valueOf((Class<Enum>) targetType, value);
            return enumValue;
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + targetType);
        }
    }

    // 日期解析方法(同上)
    private static Date parseDate(String dateStr) throws ParseException {
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy", "yyyy-MM-dd HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern).parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        throw new ParseException("Unparseable date: " + dateStr, 0);
    }
}
