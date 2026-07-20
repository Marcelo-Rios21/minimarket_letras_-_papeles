package com.minimarket.auth.assembler;

import com.minimarket.auth.controller.UsuarioController;
import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.mapper.UsuarioMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UsuarioModelAssembler implements
        RepresentationModelAssembler<
                Usuario,
                EntityModel<UsuarioResponse>
        > {

    private final UsuarioMapper usuarioMapper;

    public UsuarioModelAssembler(
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public EntityModel<UsuarioResponse> toModel(
            Usuario usuario
    ) {
        EntityModel<UsuarioResponse> model = EntityModel.of(
                usuarioMapper.toResponse(usuario),
                linkTo(
                        methodOn(UsuarioController.class)
                                .buscarPorId(usuario.getId())
                ).withSelfRel(),
                linkTo(
                        methodOn(UsuarioController.class)
                                .listar()
                ).withRel(IanaLinkRelations.COLLECTION)
        );

        if (usuario.isActivo()) {
            model.add(
                    linkTo(
                            methodOn(UsuarioController.class)
                                    .desactivar(usuario.getId())
                    ).withRel("desactivar")
            );
        }

        return model;
    }
}
