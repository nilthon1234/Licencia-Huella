package com.licencia.config;

import com.licencia.service.implement.CookieService;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CookieFilter implements Filter {
    private final CookieService cookieService;

    @Autowired
    public CookieFilter(CookieService cookieService) {
        this.cookieService = cookieService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        Cookie cookie = cookieService.getCookie(httpRequest,"licenciaToken");
        if (cookie != null){
            System.out.println("Token de licencia encontrada en el token: " +  cookie.getValue());
        }else {
            System.out.println("No se encontro la cookie de licencia");
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
