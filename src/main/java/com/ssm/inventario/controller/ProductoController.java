package com.ssm.inventario.controller;

import com.ssm.inventario.dto.ProductoForm;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.model.Estado;
import com.ssm.inventario.model.Producto;
import com.ssm.inventario.service.CategoriaService;
import com.ssm.inventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private static final String FILTRO_TODOS = "TODOS";
    private static final String VISTA_FORMULARIO = "productos/formulario";
    private static final String REDIRECT_LISTADO = "redirect:/productos";

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String nombre,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false, defaultValue = FILTRO_TODOS) String estado,
                         Model model) {
        Estado filtroEstado = convertirEstado(estado);
        model.addAttribute("productos", productoService.buscar(nombre, categoriaId, filtroEstado));
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("resumen", productoService.resumen());
        model.addAttribute("umbralBajoStock", Producto.UMBRAL_BAJO_STOCK);
        model.addAttribute("nombre", nombre);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("estado", filtroEstado == null ? FILTRO_TODOS : filtroEstado.name());
        model.addAttribute("hayFiltros", StringUtils.hasText(nombre) || categoriaId != null || filtroEstado != null);
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        prepararFormulario(model, new ProductoForm(), null);
        return VISTA_FORMULARIO;
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("producto") ProductoForm form,
                        BindingResult resultado,
                        Model model,
                        RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, form, null);
            return VISTA_FORMULARIO;
        }
        try {
            Producto producto = productoService.crear(form);
            redirect.addFlashAttribute("exito", "Producto «" + producto.getNombre() + "» registrado correctamente");
            return REDIRECT_LISTADO;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormulario(model, form, null);
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtener(id);
        prepararFormulario(model, ProductoForm.desde(producto), producto);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("producto") ProductoForm form,
                             BindingResult resultado,
                             Model model,
                             RedirectAttributes redirect) {
        Producto existente = productoService.obtener(id);
        form.setId(id);
        if (resultado.hasErrors()) {
            prepararFormulario(model, form, existente);
            return VISTA_FORMULARIO;
        }
        try {
            Producto producto = productoService.actualizar(id, form);
            redirect.addFlashAttribute("exito", "Producto «" + producto.getNombre() + "» actualizado correctamente");
            return REDIRECT_LISTADO;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            prepararFormulario(model, form, existente);
            return VISTA_FORMULARIO;
        }
    }

    @PostMapping("/{id}/inactivar")
    public String inactivar(@PathVariable Long id,
                            @RequestParam(required = false) String nombre,
                            @RequestParam(required = false) Long categoriaId,
                            @RequestParam(required = false) String estado,
                            RedirectAttributes redirect) {
        try {
            Producto producto = productoService.inactivar(id);
            redirect.addFlashAttribute("exito", "Producto «" + producto.getNombre() + "» dado de baja (inactivo)");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        conservarFiltros(redirect, nombre, categoriaId, estado);
        return REDIRECT_LISTADO;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          @RequestParam(required = false) String nombre,
                          @RequestParam(required = false) Long categoriaId,
                          @RequestParam(required = false) String estado,
                          RedirectAttributes redirect) {
        try {
            Producto producto = productoService.activar(id);
            redirect.addFlashAttribute("exito", "Producto «" + producto.getNombre() + "» activado nuevamente");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        conservarFiltros(redirect, nombre, categoriaId, estado);
        return REDIRECT_LISTADO;
    }

    private void prepararFormulario(Model model, ProductoForm form, Producto existente) {
        List<Categoria> categorias = new ArrayList<>(categoriaService.listarActivas());
        if (existente != null && !existente.getCategoria().isActiva()) {
            categorias.add(existente.getCategoria());
        }
        model.addAttribute("producto", form);
        model.addAttribute("productoExistente", existente);
        model.addAttribute("categorias", categorias);
        model.addAttribute("esEdicion", existente != null);
    }

    private void conservarFiltros(RedirectAttributes redirect, String nombre, Long categoriaId, String estado) {
        if (StringUtils.hasText(nombre)) {
            redirect.addAttribute("nombre", nombre.trim());
        }
        if (categoriaId != null) {
            redirect.addAttribute("categoriaId", categoriaId);
        }
        Estado filtroEstado = convertirEstado(estado);
        if (filtroEstado != null) {
            redirect.addAttribute("estado", filtroEstado.name());
        }
    }

    private Estado convertirEstado(String estado) {
        if (!StringUtils.hasText(estado) || FILTRO_TODOS.equalsIgnoreCase(estado)) {
            return null;
        }
        try {
            return Estado.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
