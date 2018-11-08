package com.smarthome.sso.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * User Class
 *
 * Last Modify: Frank at 181107
 * */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class User {

    /** Id, unique */
    @Id
    private String userId;

    /** username, can be duplicated */
    private String username;

    /** pwd */
    private String password;

    public User(String username, String password){
        this.username = username;
        this.password=password;
    }

}
