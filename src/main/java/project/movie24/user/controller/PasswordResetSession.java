package project.movie24.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 비밀번호 재설정 본인확인 상태를 세션에 저장/조회/해제한다.
 * FindAccountApiController(본인확인)와 FindAccountController(재설정 페이지)가 공유하는 세션 상태라
 * 두 컨트롤러가 각자 키를 들고 있는 대신 여기서 한 곳으로 모은다.
 */
final class PasswordResetSession {

    private static final String SESSION_KEY = "passwordResetUserId";

    private PasswordResetSession() {
    }

    static void markVerified(HttpServletRequest request, Long userId) {
        request.getSession().setAttribute(SESSION_KEY, userId);
    }

    static Long verifiedUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Long) session.getAttribute(SESSION_KEY);
    }

    static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_KEY);
        }
    }
}
