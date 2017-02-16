package de.kimrudolph.routing;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Cache key generator which enriches keys by environment context if available.
 */
public class EnvironmentAwareCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {

        String key = DatabaseContextHolder.getEnvironment().name() + "-" + (
            method == null ? "" : method.getName() + "-") + StringUtils
            .collectionToDelimitedString(Arrays.asList(params), "-");

        return key;
    }

}
