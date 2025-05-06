package edu.kdkce.openelectivefcfs.src.dto;

public record LoginResponse(
        boolean success,
        String token,
        Object user,
        String error
) {
    public static LoginResponse success(String token, Object user) {
        return new LoginResponse(true, token, user, null);
    }
    public static LoginResponse error(String error) {
        return new LoginResponse(false, null, null, error);
    }
}
