package org.bkvm.config;

/**
 *
 * Configuration utiltiy class to retrive parameters from {@link ConfigurationStore}
 *
 * @author matteo.minardi
 */
public class ConfigurationStoreUtils {

    public static int getInt(String key, int defaultValue, ConfigurationStore properties)
            throws ConfigurationNotValidException {
        String property = properties.getProperty(key, defaultValue + "");
        try {
            return Integer.parseInt(properties.getProperty(key, defaultValue + ""));
        } catch (NumberFormatException exx) {
            throw new ConfigurationNotValidException("Invalid integer value '"
                    + property + "' for parameter '"
                    + key + "'"
            );
        }
    }

}
