package rom41k.vasilek.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final T data;

    public static ApiResponse<Map<String, String>> message(String message) {
        String responseMessage = (message != null) ? message : "Сообщение не доступно";
        return new ApiResponse<>(Map.of("message", responseMessage));
    }
}
