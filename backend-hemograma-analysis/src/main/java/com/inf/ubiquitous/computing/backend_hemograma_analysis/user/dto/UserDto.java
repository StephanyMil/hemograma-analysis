package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private String name;
    private String passwordHash;
    private String email;
}
