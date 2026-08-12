// snippets/security/security-headers.java
// Cabeceras de seguridad HTTP con Spring Security — ver knowledge/security/defense-in-depth.md
// (capa de protección adicional, nunca sustituto de validación en la aplicación).

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'"))
            .frameOptions(frame -> frame.deny()) // previene clickjacking
            .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)) // fuerza HTTPS durante 1 año tras la primera visita
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
        );
        return http.build();
    }
}
