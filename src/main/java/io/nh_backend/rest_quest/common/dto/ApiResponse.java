package io.nh_backend.rest_quest.common.dto;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(T data, String successMessage) {

        return new ApiResponse<>(
                true,
                successMessage,
                data
        );
    }

    public static <T> ApiResponse<T> created(T data, String successMessage) {
        return ok(
                data,
                successMessage
        );
    }

    public static ApiResponse<Void> ok() {

        return ok(null,"");
    }


    public static<T> ApiResponse<T> fail(String errorMessage) {
        return new ApiResponse<>(
                false,
                errorMessage,
                null
        );
    }


}
