/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.generator;

import io.army.annotation.GeneratorType;
import io.army.dialect._Constant;
import io.army.generator.snowflake.SnowflakeGenerator;
import io.army.util._Assert;
import io.army.util._Collections;

import java.util.Map;

/// example : io.army.spring.ai.chat.memory.SpringAiChatMemory.id=io.army.generator.SnowflakeGeneratorStrategy:{"startTime":1776386333818}
public final class SnowflakeGeneratorStrategy implements GeneratorStrategy {


    public static SnowflakeGeneratorStrategy create(final String argStr) {
        final Map<String, String> map;
        map = parseJsonMap(argStr);
        _Assert.isTrue(SnowflakeGenerator.paramNameSet().containsAll(map.keySet()), "argStr illegal");
        return new SnowflakeGeneratorStrategy(map);
    }

    private static Map<String, String> parseJsonMap(final String argStr) {
        final int length = argStr.length();
        if (length < 5) {
            throw new IllegalArgumentException();
        } else if (argStr.charAt(0) != '{' || argStr.charAt(length - 1) != '}') {
            throw new IllegalArgumentException();
        }
        final String[] itemArray;
        itemArray = argStr.split(",");
        String[] pairArray;
        final Map<String, String> map = _Collections.hashMapForSize(itemArray.length);
        String key, value;
        boolean quoteStart;
        for (int i = 0, len; i < itemArray.length; i++) {
            pairArray = itemArray[i].split(":");
            key = pairArray[0].trim();
            len = key.length();
            if (key.charAt(0) != _Constant.DOUBLE_QUOTE
                    || key.charAt(len - 1) != _Constant.DOUBLE_QUOTE) {
                throw new IllegalArgumentException();
            }
            key = key.substring(1, len - 1);

            value = pairArray[1].trim();
            len = value.length();
            quoteStart = value.charAt(0) == _Constant.DOUBLE_QUOTE;
            if (quoteStart ^ value.charAt(len - 1) == _Constant.DOUBLE_QUOTE) {
                throw new IllegalArgumentException();
            }
            if (quoteStart) {
                value = value.substring(1, len - 1);
            }
            map.put(key, value);
        }
        return map;
    }


    private final Map<String, String> map;

    private SnowflakeGeneratorStrategy(Map<String, String> map) {
        this.map = Map.copyOf(map);
    }

    @Override
    public GeneratorType type() {
        return GeneratorType.PRECEDE;
    }

    @Override
    public Class<?> generatorClass() {
        return SnowflakeGenerator.class;
    }

    @Override
    public Map<String, String> paramMap() {
        return map;
    }
}
