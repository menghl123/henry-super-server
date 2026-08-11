package com.henry.common.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.base.CaseFormat;
import com.henry.common.query.PageQuery;
import com.henry.common.query.Query;
import com.henry.common.query.Sort;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 将查询入参对象转换为mybatis-plus的查询条件
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryConverter {

    public static <T> QueryWrapper<T> convert(Object queryParam) {
        final QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        return convert(queryWrapper, queryParam);
    }

    @SuppressWarnings("unchecked")
    public static <T> QueryWrapper<T> convert(QueryWrapper<T> queryWrapper, Object queryParam) {
        final Field[] fields = queryParam.getClass().getDeclaredFields();
        for (Field field : fields) {
            final Object value = parseValue(field, queryParam);
            if (Objects.isNull(value) || (value instanceof String && StringUtils.isBlank((CharSequence) value))) {
                continue;
            }

            final Query annotation = field.getDeclaredAnnotation(Query.class);
            if (Objects.nonNull(annotation) && !annotation.ignore()) {
                final String operator = parseOperator(field, annotation);
                final String column = parseColumn(field, annotation);
                addCondition(queryWrapper, operator, column, value);
            }

            if (isSortList(field)) {
                addSorts(queryWrapper, (List<Sort>) value);
            }
        }

        if (PageQuery.class.isAssignableFrom(queryParam.getClass())) {
            addSorts(queryWrapper, ((PageQuery) queryParam).getSorts());
        }
        return queryWrapper;
    }

    /**
     * 解析查询操作符：字符串默认使用like查询，集合和数组默认使用in查询，其他默认使用eq查询，
     */
    private static String parseOperator(Field field, Query annotation) {
        String operator = annotation.operator();
        if(StringUtils.isBlank(operator)) {
            operator = annotation.value();
        }

        if (StringUtils.isBlank(operator)) {
            if (String.class.isAssignableFrom(field.getType())) {
                operator = "like";
            } else if (Collection.class.isAssignableFrom(field.getType()) || field.getClass().isArray()) {
                operator = "in";
            } else {
                operator = "eq";
            }
        }
        return operator;
    }

    /**
     * 如果没有专门指定数据库字段名，就直接使用注解标记的字段名转为下划线格式
     */
    private static String parseColumn(Field field, Query annotation) {
        String column = annotation.column();
        if (StringUtils.isBlank(column)) {
            column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
        }
        return column;
    }

    /**
     * 使用字段对应的get方法来获取字段值
     */
    private static Object parseValue(Field field, Object queryParam) {
        try {
            final PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(), queryParam.getClass());
            return descriptor.getReadMethod().invoke(queryParam);
        } catch (Exception e) {
            throw new IllegalArgumentException("获取对象值失败", e);
        }
    }

    private static <T> void addCondition(QueryWrapper<T> wrapper, String operator, String column, Object value) {
        switch (operator) {
            case "=":
            case "eq":
                wrapper.eq(column, value);
                break;
            case "!=":
            case "<>":
            case "ne":
                wrapper.ne(column, value);
                break;
            case ">":
            case "gt":
                wrapper.gt(column, value);
                break;
            case ">=":
            case "ge":
                wrapper.ge(column, value);
                break;
            case "<":
            case "lt":
                wrapper.lt(column, value);
                break;
            case "<=":
            case "le":
                wrapper.le(column, value);
                break;
            case "like":
                wrapper.like(column, value);
                break;
            case "likeLeft":
                wrapper.likeLeft(column, value);
                break;
            case "likeRight":
                wrapper.likeRight(column, value);
                break;
            case "notLike":
                wrapper.notLike(column, value);
                break;
            case "isNull":
                wrapper.isNull(column);
                break;
            case "isNotNull":
                wrapper.isNotNull(column);
                break;
            case "in":
                wrapper.in(column, value);
                break;
            case "notIn":
                wrapper.notIn(column, value);
                break;
            case "between":
                final List pair1 = (List) value;
                wrapper.between(column, pair1.get(0), pair1.get(1));
                break;
            case "notBetween":
                final List pair2 = (List) value;
                wrapper.notBetween(column, pair2.get(0), pair2.get(1));
                break;
            default:
                throw new IllegalArgumentException("不支持操作符" + operator);
        }
    }

    private static boolean isSortList(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            return pt.getRawType().equals(List.class) && pt.getActualTypeArguments()[0].equals(Sort.class);
        } else {
            return false;
        }
    }

    private static <T> void addSorts(QueryWrapper<T> queryWrapper, List<Sort> sorts) {
        if (!CollectionUtils.isEmpty(sorts)) {
            sorts.stream()
                    .filter(sort -> StringUtils.isNotBlank(sort.getName()))
                    .forEach(sort -> {
                        String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sort.getName());
                        if (sort.getType().equalsIgnoreCase("asc")) {
                            queryWrapper.orderByAsc(column);
                        } else {
                            queryWrapper.orderByDesc(column);
                        }
                    });
        }
    }
}