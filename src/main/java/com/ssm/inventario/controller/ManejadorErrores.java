package com.ssm.inventario.controller;

import com.ssm.inventario.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final String VISTA_ERROR = "error";

    @ExceptionHandler({RecursoNoEncontradoException.class, NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noEncontrado(Exception ex, Model model) {
        model.addAttribute("codigo", 404);
        model.addAttribute("titulo", "Recurso no encontrado");
        model.addAttribute("mensaje", ex instanceof RecursoNoEncontradoException
                ? ex.getMessage()
                : "La página o el registro que buscas no existe.");
        return VISTA_ERROR;
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String errorBaseDatos(DataAccessException ex, Model model) {
        log.error("Error de acceso a datos", ex);
        model.addAttribute("codigo", 503);
        model.addAttribute("titulo", "Problema con la base de datos");
        model.addAttribute("mensaje", "No fue posible completar la operación. Verifica la conexión con MySQL e inténtalo de nuevo.");
        return VISTA_ERROR;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String errorGeneral(Exception ex, Model model) {
        log.error("Error inesperado", ex);
        model.addAttribute("codigo", 500);
        model.addAttribute("titulo", "Ocurrió un error inesperado");
        model.addAttribute("mensaje", "Estamos trabajando para solucionarlo. Regresa al inicio e inténtalo de nuevo.");
        return VISTA_ERROR;
    }
}
