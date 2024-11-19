package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a JSON string into an object of the specified class.
     *
     * @param json the JSON string to convert.
     * @param clazz the class of the object to convert to.
     * @param <T> the type of the object.
     * @return the converted object.
     * @throws JsonProcessingException if the JSON cannot be parsed.
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Converts an object into a JSON string.
     *
     * @param object the object to convert.
     * @return the JSON string representation of the object.
     * @throws JsonProcessingException if the object cannot be serialized.
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}