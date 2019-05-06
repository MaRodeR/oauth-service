package org.bp.labs.oauthservice.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;


@Entity(name = "country")
@Accessors(chain = true)
@Getter
@Setter
public class Country {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "english_name")
    private String englishName;
}
