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

package io.army.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

public abstract class _ResourceUtils {

    private _ResourceUtils() {
    }


    public static Properties loadArmyProperties(final String fileName) {
        try {
            final String location;
            location = String.format("META-INF/army/%s.properties", fileName);
            final Enumeration<URL> enumeration;
            enumeration = Thread.currentThread().getContextClassLoader().getResources(location);
            URL url;
            final Properties properties = new Properties();
            while (enumeration.hasMoreElements()) {
                url = enumeration.nextElement();
                try (Reader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    properties.load(reader);
                }
            } // while loop
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
