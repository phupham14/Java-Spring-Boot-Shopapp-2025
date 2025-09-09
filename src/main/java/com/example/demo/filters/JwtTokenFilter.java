package com.example.demo.filters;

import com.example.demo.components.JwtTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String bypassToken;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Request Path: " + request.getServletPath());
        System.out.println("Request Method: " + request.getMethod());

        try {
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println(authHeader);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            final String token = authHeader.substring(7);
            final String phoneNumber = jwtTokenUtil.extractPhoneNumber(token);
            //List<String> roles = jwtTokenUtil.extractRoles(token);

            if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails existingUser = userDetailsService.loadUserByUsername(phoneNumber);
                if (jwtTokenUtil.validateToken(token, existingUser)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            existingUser, null, existingUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                }
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("==> Authenticated User: " + auth.getName());
            System.out.println("==> Authorities: " + auth.getAuthorities());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }

    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/roles", bypassToken), "GET"),
                Pair.of(String.format("%s/products", bypassToken), "GET"),
                Pair.of(String.format("%s/healthcheck/health", bypassToken), "GET"),
                Pair.of(String.format("%s/order_details", bypassToken), "GET"),
                Pair.of(String.format("%s/products/[^/]+$", bypassToken), "GET"),
                Pair.of(String.format("%s/products/images/[^/]+$", bypassToken), "GET"),
                Pair.of(String.format("%s/categories", bypassToken), "GET"),
                Pair.of(String.format("%s/users/register", bypassToken), "POST"),
                Pair.of(String.format("%s/users/login", bypassToken), "POST")
        );

        final String path = request.getServletPath();
        final String method = request.getMethod();

        if (path.startsWith(String.format("/%s/orders", bypassToken))
                && method.equals("GET")) {
            // Check if the requestPath matches the desired pattern
            if (path.matches(String.format("/%s/orders/\\d+", bypassToken))) {
                return true;
            }
            // If the requestPath is just "%s/orders", return true
            if (path.equals(String.format("/%s/orders", bypassToken))) {
                return true;
            }
        }

        for (Pair<String, String> bypass : bypassTokens) {
            if (path.matches(bypass.getLeft()) && method.equals(bypass.getRight())) {
                return true;
            }
        }
        return false;
    }

}
