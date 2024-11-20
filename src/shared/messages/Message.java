package shared.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import shared.enumerations.ServerCommands;

public record Message<T>(ServerCommands type, T data) {

    /**
     * Dynamically converts the data into a specific type.
     *
     * @param clazz the target class type for the data.
     * @param <U>   the type of the target class.
     * @return the deserialized data object.
     */
    public <U> U dataAs(Class<U> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(data, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert data to type: " + clazz.getName(), e);
        }
    }

    public String getType() {
        return type.toString();
    }

    public T getData() {
        return data;
    }
}
