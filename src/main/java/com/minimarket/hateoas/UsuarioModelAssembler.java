package com.minimarket.hateoas;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.entity.Usuario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler
        implements RepresentationModelAssembler<
                Usuario,
                EntityModel<Usuario>
        > {

    @Override
    public EntityModel<Usuario> toModel(Usuario usuario) {
        EntityModel<Usuario> modelo = EntityModel.of(usuario);

        if (usuario.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(UsuarioController.class)
                                    .obtenerUsuarioPorId(usuario.getId())
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(UsuarioController.class)
                                .listarUsuarios()
                ).withRel("usuarios")
        );

        modelo.add(
                linkTo(
                        methodOn(CarritoController.class)
                                .listarCarrito()
                ).withRel("carrito")
        );

        return modelo;
    }
}