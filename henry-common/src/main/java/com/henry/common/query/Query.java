package com.henry.common.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Query {
    String value() default "";

    String operator() default "";

    String column() default "";

    boolean ignore() default false;

    final class Condition {
        public final static String EQ = "=";
        public final static String NE = "!=";
        public final static String GT = ">";
        public final static String GTE = ">=";
        public final static String LT = "<";
        public final static String LTE = "<=";
        public final static String LIKE = "like";
        public final static String LIKE_LEFT = "likeLeft";
        public final static String LIKE_RIGHT = "likeRight";
        public final static String NOT_LIKE = "notLike";
        public final static String IS_NULL = "isNull";
        public final static String IS_NOT_NULL = "isNotNull";
        public final static String IN = "in";
        public final static String NOT_IN = "notIn";
        public final static String BETWEEN = "between";
        public final static String NOT_BETWEEN = "notBetween";
    }
}
