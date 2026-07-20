package com.minimarket.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_username",
                        columnNames = "username"
                )
        }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(
            nullable = false,
            length = 50
    )
    private String username;

    @JsonIgnore
    @Column(
            nullable = false,
            length = 100
    )
    private String password;

    @Column(
            nullable = false,
            length = 80
    )
    private String nombre;

    @Column(
            nullable = false,
            length = 80
    )
    private String apellido;

    @Column(
            nullable = false,
            length = 120
    )
    private String email;

    @Column(
            nullable = false,
            length = 200
    )
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = {
                    @JoinColumn(name = "usuario_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "rol_id")
            },
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_usuario_rol",
                            columnNames = {
                                    "usuario_id",
                                    "rol_id"
                            }
                    )
            }
    )
    private Set<Rol> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles == null
                ? new HashSet<>()
                : new HashSet<>(roles);
    }
}
