package org.protu.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.service.JWTServiceImpl;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

  private final JWTServiceImpl jwtService;
  private final UserDetailsService userDetailsService;
  private final HandlerExceptionResolver handlerExceptionResolver;

  private boolean isInvalidBearerToken(String authHeader) {
    return authHeader == null || !authHeader.startsWith("Bearer ");
  }

  private String getJWTToken(String authHeader) {
    return authHeader.split(" ")[1];
  }

  private UsernamePasswordAuthenticationToken createAuthenticationToken(
      HttpServletRequest request,
      UserDetails userDetails) {

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );

    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    return authToken;
  }

  private void authenticateUserIfNecessary(HttpServletRequest request, Long userId, String jwt) {
    if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
      if (jwtService.isValidToken(jwt, userId)) {
        UsernamePasswordAuthenticationToken authToken = createAuthenticationToken(request, userDetails);
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt;
      Long userId;
      String authHeader = request.getHeader("Authorization");

      if (isInvalidBearerToken(authHeader)) {
        filterChain.doFilter(request, response);
        return;
      }

      jwt = getJWTToken(authHeader);
      userId = jwtService.getUserIdFromToken(jwt);
      authenticateUserIfNecessary(request, userId, jwt);
      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      handlerExceptionResolver.resolveException(request, response, null, e);
    }
  }

}