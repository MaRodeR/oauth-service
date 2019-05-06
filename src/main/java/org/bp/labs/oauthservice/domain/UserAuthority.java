package org.bp.labs.oauthservice.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

@Entity(name = "authorities")
@IdClass(UserAuthority.class)
@Accessors(chain = true)
@Getter
@Setter
public class UserAuthority implements Serializable{

    @Id
    @Column(name = "username")
    private String userName;
    @Id
    @Column(name = "authority")
    private String role;
}
