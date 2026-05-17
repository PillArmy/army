package io.army.example.stock.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

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
