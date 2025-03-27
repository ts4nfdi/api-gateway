package org.semantics.apigateway.service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for accessing and setting nested values in complex data structures.
 *
 * <p>This class provides methods to retrieve and set values in nested maps and lists
 * using flexible key path navigation.</p>
 *
 * <h2>Key Path Navigation Features:</h2>
 * <ul>
 *   <li>Support for nested key paths using "->" separator</li>
 *   <li>Multiple key options using "|" separator</li>
 *   <li>Handles Map and List data structures</li>
 *   <li>Null-safe value retrieval</li>
 * </ul>
 *
 * <h2>Key Path Examples:</h2>
 * <pre>
 * // Simple key
 * "name"
 *
 * // Nested key
 * "user->profile->name"
 *
 * // Multiple key options
 * "userId|id|identifier"
 *
 * // Combined nested and multiple options
 * "user->details->name|user->fullName"
 * </pre>
 */
public class MappingTransformer {
    /**
     * Retrieves a value from a nested map or list structure based on a flexible key path.
     *
     * @example Map<String, Object> data = new HashMap<>();
     * data.put("user", Map.of("name", "John Doe"));
     * Object value = itemValueGetter(data, "user->name"); // Returns "John Doe"
     */
    public static Object itemValueGetter(Map<String, Object> item, String key) {
        if (key == null) {
            return null;
        }
        String[] options = key.split("\\|");
        // Use findFirst to return the first non-null value found
        return Arrays.stream(options)
                .map(option -> {
                    if (option.contains("->")) {
                        String[] keys = option.split("->");
                        Object value = item;
                        for (String s : keys) {
                            if (value == null) {
                                break;
                            }
                            if (value instanceof Map) {
                                value = ((Map<?, ?>) value).get(s);
                            } else if (value instanceof List) {
                                value = listItemValueGetter(s, value);
                                if(((List<?>) value).isEmpty())
                                    value = null;
                            }
                        }
                        return value;
                    } else if (item.containsKey(option)) {
                        // This should use 'option' not 'key'
                        return item.get(option);
                    }

                    return null;
                })
                .filter(x -> x != null && !((x instanceof String) && ((String) x).isEmpty()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a value from a list based on an index or key.
     *
     * @param key  The index or key to retrieve
     * @param list The source list
     * @return The value at the specified index or null
     */
    public static List<String> listItemValueGetter(String key, Object list) {
        if (!(list instanceof List)) {
            return null;
        }

        List<?> sourceList = (List<?>) list;
        return sourceList.stream().map(x -> {
            if (x instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) x;
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    return value == null ? null : value.toString();
                }
            } else if (x instanceof String && x.toString().startsWith("{") && x.toString().endsWith("}")) {
                String input = x.toString();
                try {
                    Map<String, String> map = Arrays.stream(
                                    input.replaceAll("[{}]", "").split(", ")
                            )
                            .map(s -> s.split("="))
                            .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
                    return map.get(key);
                } catch (Exception ignored) {
                }
            }

            return null;
        }).filter(Objects::nonNull).toList();
    }

    /**
     * Sets a value in a nested map or list structure based on a flexible key path.
     *
     * @param item  The target map or list to modify
     * @param key   The key path to navigate
     * @param value The value to set
     * @return The modified item
     * @throws IllegalArgumentException if the input is not a Map or List
     * @example Map<String, Object> data = new HashMap<>();
     * itemValueSetter(data, "user->profile->name", "John Doe");
     */
    public static Object itemValueSetter(Object item, String key, Object value) {
        if (key == null || item == null) {
            return item;
        }

        // Handle multiple key options
        String[] options = key.split("\\|");
        String primaryKey = options[0];
        // TODO Handle multiple key options

        // Handle nested keys
        if (primaryKey.contains("->")) {
            String[] keys = primaryKey.split("->");
            Object current = item;

            // Navigate through all keys except the last one
            for (int i = 0; i < keys.length - 1; i++) {
                String currentKey = keys[i];

                if (current instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) current;
                    // Create nested map if not exists
                    if (!map.containsKey(currentKey)) {
                        map.put(currentKey, new HashMap<>());
                    }
                    current = map.get(currentKey);
                } else if (current instanceof List) {
                    // For list handling, you might need more complex logic
                    // This is a simplified version
                    List<Object> list = (List<Object>) current;
                    int index = Integer.parseInt(currentKey);
                    if (index >= list.size()) {
                        // Expand list if needed
                        while (list.size() <= index) {
                            list.add(null);
                        }
                    }
                    current = list.get(index);
                }
            }

            // Set the final value
            String finalKey = keys[keys.length - 1];
            if (current instanceof Map) {
                ((Map<String, Object>) current).put(finalKey, value);
            } else if (current instanceof List) {
                List<Object> list = (List<Object>) current;
                int index = Integer.parseInt(finalKey);
                list.set(index, value);
            }
        } else {
            // Simple key mapping
            if (item instanceof Map) {
                ((Map<String, Object>) item).put(primaryKey, value);
            }
        }

        return item;
    }

    /**
     * Converts a source object to a target map using specified mappings.
     *
     * @param source   The source object containing data
     * @param mappings A map of target keys to source key paths
     * @return A new map with transformed data
     * @example Map<String, Object> source = ...;
     * Map<String, String> mapping = Map.of(
     * "fullName", "user->name",
     * "age", "user->details->age"
     * );
     * Map<String, Object> result = transform(source, mapping);
     */
    public static Map<String, Object> transform(
            Map<String, Object> source,
            Map<String, String> mappings
    ) {
        return mappings.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> itemValueGetter(source, entry.getValue()),
                        (v1, v2) -> v1,  // In case of duplicate keys, keep first value
                        HashMap::new
                ));
    }
}
