package com.moneto.api.dto;

import com.moneto.api.model.Currency;
import com.moneto.api.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private Currency currency;
    private Role role;
}