/*
 Licensed to Diennea S.r.l. under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. Diennea S.r.l. licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */
package org.bookkeepervisualmanager.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.BiConsumer;
import javax.servlet.ServletContext;

/**
 * Reads configuration from a Java properties file
 *
 * @author matteo.minardi
 */
public class PropertiesConfigurationStore implements ConfigurationStore {

    private final Properties properties;

    public PropertiesConfigurationStore(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        properties.forEach((k, v) -> {
            consumer.accept(k.toString(), v.toString());
        });
    }

    public static class PropertiesConfigurationFactory {

        public static Properties buildFromSystemProperty(String propName) throws IOException {
            String configPath = System.getProperty(propName);
            return readPropertiesFromFile(configPath);
        }

        public static Properties buildFromEnvironmentVariable(String envName) throws IOException {
            String configPath = System.getenv(envName);
            return readPropertiesFromFile(configPath);
        }

        public static Properties buildFromWebXML(ServletContext ctx, String propName) throws IOException {
            String configPath = ctx.getInitParameter(propName);
            return readPropertiesFromFile(configPath);
        }

        private static Properties readPropertiesFromFile(String path) throws IOException {
            if (path == null) {
                return null;
            }
            File configFile = new File(path);
            if (!configFile.isFile()) {
                return null;
            }
            Properties properties = new Properties();
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return properties;
        }

    }
    
}
