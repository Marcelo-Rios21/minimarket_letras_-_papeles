# Testing unitario - Semana 4

## Objetivo

Este documento resume la configuración de pruebas unitarias incorporada al proyecto MiniMarket.

El foco de esta etapa fue validar funcionalidades críticas relacionadas con usuarios y ventas, utilizando JUnit 5, Mockito y JaCoCo.

## Herramientas utilizadas

- JUnit 5: ejecución de pruebas unitarias.
- Mockito: simulación de dependencias como repositorios y servicios.
- JaCoCo: medición de cobertura de código.

## Funcionalidades probadas

Las pruebas unitarias se enfocan en:

- Validación de usuarios con datos obligatorios completos.
- Validación de roles permitidos para registrar ventas.
- Registro de ventas asociado a un usuario válido.
- Validación de stock suficiente antes de registrar una venta.
- Cálculo correcto del total de venta.
- Rechazo de ventas inválidas mediante excepciones controladas.

## Archivos principales de prueba

- src/test/java/com/minimarket/service/UsuarioServiceTest.java
- src/test/java/com/minimarket/service/VentaServiceTest.java

## Clases principales cubiertas

- src/main/java/com/minimarket/service/impl/UsuarioServiceImpl.java
- src/main/java/com/minimarket/service/impl/VentaServiceImpl.java

## Ejecución de pruebas

Para ejecutar las pruebas unitarias en Windows:

    .\mvnw.cmd clean test

## Reporte de cobertura

JaCoCo genera el reporte de cobertura en:

    target/site/jacoco/index.html

En la evidencia revisada, las clases principales asociadas a usuarios y ventas superan el 80% de cobertura:

| Clase | Cobertura de instrucciones | Cobertura de ramas |
|---|---:|---:|
| UsuarioServiceImpl | 100% | 83% |
| VentaServiceImpl | 98% | 83% |

## Observación sobre la cobertura global

La cobertura global del proyecto puede ser menor porque JaCoCo considera todos los paquetes del backend, incluyendo controladores, seguridad JWT, DTOs, handlers y otros servicios que no forman parte directa del alcance de esta actividad.

Para esta semana, el foco de las pruebas está en las funcionalidades relacionadas con usuarios y ventas, que corresponden a los requerimientos principales de la pauta.
