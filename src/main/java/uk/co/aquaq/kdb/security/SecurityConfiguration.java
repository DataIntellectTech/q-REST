
package uk.co.aquaq.kdb.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Value("${security.ldap.url}")
    private String url;
    @Value("${managerDn}")
    private String managerDn;
    @Value("${managerPassword}")
    private String managerPassword;
    @Value("${groupSearchFilter}")
    private String groupSearchFilter;
    @Value("${userDnPatterns}")
    private String userDnPatterns;
    @Value("${userSearchBase}")
    private String userSearchBase;
    @Value("${userSearchFilter}")
    private String userSearchFilter;
    @Value("${basic.authentication.user}")
    private String basicAuthUsername;
    @Value("${basic.authentication.password}")
    private String basicAuthPassword;
    @Value("${authorisation.type}")
    private String authType;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin()
                .and()
                .httpBasic().and().cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        switch(authType.trim().toUpperCase()) {
            case "LDAP":
                configureLdapAuth(auth);
                break;
            default:
                configureBasicAuth(auth);
                break;
        }
    }

    private void configureBasicAuth(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(basicAuthUsername).password(basicAuthPassword).authorities("ROLE_USER");
    }

    private void configureLdapAuth(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
                .userDnPatterns(userDnPatterns)
                .userSearchFilter(userSearchFilter)
                .userSearchBase("")
                .groupSearchFilter(groupSearchFilter)
                .contextSource()
                .url(url)
                .managerDn(managerDn)
                .managerPassword(managerPassword);
    }
}