package io.army.example.coder.web.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/// Spring MVC interceptor that ensures every request has an **`AuthToken` cookie**.
///
/// <p>If the cookie is not present, it automatically sets a default `AuthToken` cookie
/// with a hard-coded user ID for development/demo purposes.
/// The cookie is configured as HTTP-only with maximum age for persistence.</p>
public class CookieInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (hasNotUserIdCookie(request.getCookies())) {
            sendCookie(response);
        }
        return true;
    }


    private boolean hasNotUserIdCookie(Cookie[] cookies) {
        if (cookies == null) {
            return true;
        }
        boolean none = true;
        for (Cookie cookie : cookies) {
            if ("AuthToken".equals(cookie.getName())) {
                none = false;
                break;
            }
        }
        return none;
    }

    private void sendCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("AuthToken", "47540383744");
        cookie.setPath("/");
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }


}
