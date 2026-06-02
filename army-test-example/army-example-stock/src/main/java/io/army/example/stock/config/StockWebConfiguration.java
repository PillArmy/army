package io.army.example.stock.config;

import io.army.example.stock.web.CookieInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/// Spring MVC configuration for the stock web application.
///
/// <p>Configures:</p>
/// - **Static resource handling** with 365-day cache for `/static/**` paths
/// - **Cookie interceptor** registration for all request paths
@Configuration
public class StockWebConfiguration implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/public/", "classpath:/static/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieInterceptor())
                .addPathPatterns("/**");
    }


    @Bean
    public CookieInterceptor cookieInterceptor() {
        return new CookieInterceptor();
    }


}
