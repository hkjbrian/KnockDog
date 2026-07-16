package com.knockdog.global.common.dto;

/** 성공·실패 API 응답을 같은 형태로 전달하는 공통 DTO. */
public record ApiResponse<T>(String code, String message, T data) {

    private static final String SUCCESS_CODE = "SUCCESS";
    private static final String SUCCESS_MESSAGE = "요청이 성공했습니다.";

    /** 기본 성공 메시지와 데이터를 담은 응답을 만든다. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /** 호출 맥락에 맞는 성공 메시지와 데이터를 담은 응답을 만든다. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS_CODE, message, data);
    }

    /** 오류 코드·메시지와 빈 데이터를 담은 응답을 만든다. */
    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
