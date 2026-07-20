package com.minimarket.auth.service;

import com.minimarket.auth.dto.AuthResponse;
import com.minimarket.auth.dto.LoginRequest;
import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioResponse;

public interface AuthService {

    UsuarioResponse registrar(RegisterRequest request);

    AuthResponse autenticar(LoginRequest request);
}
