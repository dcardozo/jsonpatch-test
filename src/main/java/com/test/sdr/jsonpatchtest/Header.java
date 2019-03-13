package com.test.sdr.jsonpatchtest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Header {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}
