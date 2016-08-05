package com.web.config;

import com.web.butler.util.entity.User;
import com.web.butler.util.json.JsonObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("user/registration", "/registration").anonymous()
                .antMatchers("/", "/topic", "/hello", "/topic/greetings", "/init", "/logout").authenticated()
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/login").defaultSuccessUrl("/init", true).permitAll()
                .and().logout().logoutSuccessUrl("/login")
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            String reply = getAuth(username);
            return new org.springframework.security
                    .core.userdetails.User(username, reply, AuthorityUtils.createAuthorityList("USER"));
        }).passwordEncoder(passwordEncoder);
    }

    private String getAuth(String username) {
        String reply = "";
        try {
            SocketConfig instance = SocketConfig.getInstance();
            User user = new User(username);
            String command = "getUserByLogin";
            String json = JsonObjectFactory.getJsonString(command, user);

            instance.send(json);
            reply = instance.receive();

            user = Optional.ofNullable(JsonObjectFactory.getObjectFromJson(reply, User.class)).orElseGet(User::new);
            reply = user.getPassword() + "";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return reply;
    }

    @Autowired
    private ShaPasswordEncoder passwordEncoder;

    @Bean
    public ShaPasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }


}