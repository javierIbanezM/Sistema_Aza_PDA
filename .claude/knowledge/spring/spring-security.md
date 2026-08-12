# Spring Security

> Ver [`../../standards/api.md`](../../standards/api.md) para el estándar de códigos HTTP (401/403) que este documento aplica en Spring, y `CLAUDE.md` sección 12 (OWASP) para el marco general de seguridad.

## Autenticación: JWT sobre sesiones para APIs stateless

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // aceptable en API stateless con JWT, ver CLAUDE.md 4.3
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // factor de coste explícito, ≥10 (CLAUDE.md sección 4.3)
    }
}
```

## Autorización granular con `@PreAuthorize`

```java
@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @PreAuthorize("hasRole('CLIENTE') and #clienteId == authentication.principal.clienteId")
    @GetMapping("/{clienteId}/pedidos")
    public List<PedidoResponse> listarPedidos(@PathVariable String clienteId) { /* ... */ }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) { /* ... */ }
}
```

Principio de menor privilegio: cada endpoint declara exactamente qué rol/condición requiere, en lugar de un `authenticated()` genérico que permite a cualquier usuario autenticado acceder a datos de otro cliente.

## Generación y validación de JWT

```java
@Component
public class JwtService {

    private final SecretKey key; // cargada desde configuración externa, nunca hardcodeada
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000; // 15 minutos

    public String generarAccessToken(String usuarioId, List<String> roles) {
        return Jwts.builder()
            .subject(usuarioId)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
            .signWith(key)
            .compact();
    }

    public Claims validarYExtraerClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload(); // lanza excepción si el token es inválido/expirado
    }
}
```

Access tokens de vida corta (15 minutos) + refresh tokens de vida más larga con rotación y revocación explícita, según `CLAUDE.md` sección 5.5.

## Filtro de autenticación JWT

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.validarYExtraerClaims(header.substring(7));
                var auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, extraerAuthorities(claims));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                log.warn("Token JWT inválido: {}", e.getMessage()); // no se expone el detalle al cliente
            }
        }
        chain.doFilter(request, response);
    }
}
```

## CORS por entorno, nunca `*` en producción

```java
@Bean
public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> origenes) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(origenes); // desde configuración externa, distinto por entorno
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

## Rate limiting en login (mitiga fuerza bruta)

Spring Security no incluye rate limiting nativo — se implementa con un interceptor propio o una librería (`bucket4j`), contando intentos fallidos por IP/usuario y aplicando backoff o bloqueo temporal, según `CLAUDE.md` sección 12.1 (Identification and Authentication Failures).

## Checklist

- [ ] ¿Las contraseñas se codifican con `BCryptPasswordEncoder` con factor de coste ≥10?
- [ ] ¿Cada endpoint declara explícitamente su requisito de rol/permiso, sin depender de un `authenticated()` genérico donde se necesita más granularidad?
- [ ] ¿Los access tokens tienen expiración corta, con refresh tokens rotables por separado?
- [ ] ¿CORS está configurado por entorno, sin `*` en producción?
- [ ] ¿Existe rate limiting o bloqueo temporal ante intentos de login fallidos repetidos?
- [ ] ¿Los mensajes de error de autenticación no revelan si el fallo fue por usuario inexistente o contraseña incorrecta (evitar enumeración de usuarios)?

Ver también: `CLAUDE.md` secciones 4.3 y 12, [`../../standards/api.md`](../../standards/api.md), [`spring.md`](./spring-core.md).
