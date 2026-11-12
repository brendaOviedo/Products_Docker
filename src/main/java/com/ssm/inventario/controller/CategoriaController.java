package com.ssm.inventario.controller;

import com.ssm.inventario.dto.CategoriaForm;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private static final String VISTA_FORMULARIO = "categorias/formulario";
    private static final String REDIRECT_LISTADO = "redirect:/categorias";

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.listarConResumen());
        return "categorias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        prepararFormulario(model, new CategoriaForm(), null);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("categoria") CategoriaForm form,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form, null);
            return VISTA_FORMULARIO;
        }
        try {
            Categoria categoria = categoriaService.crear(form);
            redirect.addFlashAttribute("exito", "Categoría «" + categoria.getNombre() + "» registrada correctamente");
            return REDIRECT_LISTADO;
        } catch (ReglaNegocioException | DataIntegrityViolationException ex) {
            model.addAttribute("error", mensajeDe(ex));
            prepararFormulario(model, form, null);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaService.obtener(id);
        prepararFormulario(model, CategoriaForm.desde(categoria), categoria);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("categoria") CategoriaForm form,
                             BindingResult resultado,
                             Model model,
                             RedirectAttributes redirect) {
        Categoria existente = categoriaService.obtener(id);
        form.setId(id);
        if (resultado.hasErrors()) {
            prepararFormulario(model, form, existente);
            return VISTA_FORMULARIO;
        }
        try {
            Categoria categoria = categoriaService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Categoría «" + categoria.getNombre() + "» actualizada correctamente");
            return REDIRECT_LISTADO;
        } catch (ReglaNegocioException | DataIntegrityViolationException ex) {
            model.addAttribute("error", mensajeDe(ex));
            prepararFormulario(model, form, existente);
            return VISTA_FORMULARIO;
        }
    }

    @PostMapping("/{id}/inactivar")
    public String inactivar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Categoria categoria = categoriaService.inactivar(id);
            redirect.addFlashAttribute("exito", "Categoría «" + categoria.getNombre() + "» dada de baja (inactiva)");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT_LISTADO;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Categoria categoria = categoriaService.activar(id);
            redirect.addFlashAttribute("exito", "Categoría «" + categoria.getNombre() + "» activada nuevamente");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return REDIRECT_LISTADO;
    }

    private void prepararFormulario(Model model, CategoriaForm form, Categoria existente) {
        model.addAttribute("categoria", form);
        model.addAttribute("categoriaExistente", existente);
        model.addAttribute("esEdicion", existente != null);
    }

    private String mensajeDe(RuntimeException ex) {
        if (ex instanceof DataIntegrityViolationException) {
            return "Ya existe una categoría con ese nombre";
        }
        return ex.getMessage();
    }
}
