package org.bp.labs.oauthservice.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "registered_user")
@EntityListeners(AuditingEntityListener.class)
@Accessors(chain = true)
@Getter
@Setter
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "user_name")
    private String userName;
    private String password;
    @Column(name = "email")
    private String email;
    @Column(name = "job_title")
    private String jobTitle;
    private String company;
    private String phone;
    private String address;
    private String addressLine2;

    @Column(name = "zip_code")
    private String zipCode;
    private String city;
    @Column(name = "country_id")
    private Integer countryId;
    private String region;
    @CreatedDate
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "last_login_date")
    private Date lastLoginDate;
    @Transient
    private List<String> roles = new ArrayList<>();
}
