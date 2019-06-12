package uk.co.aquaq.kdb.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationEntryPoint authEntryPoint;
    @Value("${basic.authentication.user}")
    String user;
    @Value("${basic.authentication.password}")
    String password;

    @Value("${authentication.path}")
    String passwordFile;


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        if(passwordFile!=null && !passwordFile.isEmpty()) {
            NodeList nList = getNodeList();
            addAuthenticationsFromXml(auth, nList);
        }
        auth.inMemoryAuthentication()
                .withUser(user).password(password).authorities("ROLE_USER");
    }

    private void addAuthenticationsFromXml(AuthenticationManagerBuilder auth, NodeList nList) throws Exception {
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String username=eElement.getElementsByTagName("username").item(0)
                        .getTextContent();
                String password=eElement.getElementsByTagName("password").item(0)
                        .getTextContent();

                auth.inMemoryAuthentication()
                        .withUser(username).password(password).authorities("ROLE_USER");
            }
        }
    }

    private NodeList getNodeList() throws ParserConfigurationException, SAXException, IOException {
        File file = new File(passwordFile);
        Map<String, String> authMap=new HashMap<>();
        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        return doc.getElementsByTagName("user");
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .httpBasic().and().cors().configurationSource(request -> new CorsConfiguration().applyPermitDefaultValues());

    }


}