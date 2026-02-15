package com.resale.homeflycommunication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    // Secrets per issuer
    private static final Map<String, String> ISSUER_SECRETS = Map.of(
            "vso-auth", "MySuperSecureLongSecretKeyThatIsAtLeast32Bytes!",
            "user-ms", "AnotherVerySecureSecretKeyThatIs32PlusChars!"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Map<String, AuthenticationManager> authManagers = new HashMap<>();

        ISSUER_SECRETS.forEach((issuer, secret) -> {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");

            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).build();
            jwtDecoder.setJwtValidator(JwtValidators.createDefault());

            JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
            provider.setJwtAuthenticationConverter(jwtAuthenticationConverter());

            authManagers.put(issuer, new ProviderManager(provider));
        });

        JwtIssuerAuthenticationManagerResolver resolver = new JwtIssuerAuthenticationManagerResolver(authManagers::get);

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/zoom/auth/**").permitAll()
//                        .requestMatchers("/zoom/**").authenticated()
                                .requestMatchers("/zoom/**").permitAll()
                                .requestMatchers("/user/**").permitAll()
                                .requestMatchers("/mail/user/sendOtpMail").permitAll()
                                .requestMatchers("/mail/user/sendMail").permitAll()
                                .requestMatchers("/mail/**").authenticated()
                                .requestMatchers("/sms/**").authenticated()
                                .requestMatchers("/admin/**").authenticated()
//                        .requestMatchers("/notification/**").authenticated()
                                .requestMatchers("/notification/**").permitAll()
                                .requestMatchers("/user/**").authenticated()
                                .requestMatchers("/sms/singleSMS").permitAll()

                                .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieBearerTokenResolver(
                                "ADMIN_AUTH_TOKEN",
                                "SALES_AUTH_TOKEN",
                                "CUSTOMER_AUTH_TOKEN"
                        ))
                        .authenticationManagerResolver(resolver)
                );

        return http.build();
    }

    // Convert "roles" claim in JWT to Spring authorities
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public CookieBearerTokenResolver cookieBearerTokenResolver() {
        return new CookieBearerTokenResolver(
                "ADMIN_AUTH_TOKEN",
                "SALES_AUTH_TOKEN",
                "CUSTOMER_AUTH_TOKEN"
        );
    }
}


