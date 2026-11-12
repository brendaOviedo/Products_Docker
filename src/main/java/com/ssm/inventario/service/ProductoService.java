package com.ssm.inventario.service;

import com.ssm.inventario.dto.InventarioResumen;
import com.ssm.inventario.dto.ProductoForm;
import com.ssm.inventario.exception.RecursoNoEncontradoException;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.model.Estado;
import com.ssm.inventario.model.Producto;
import com.ssm.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
    }

    @Transactional(readOnly = true)
    public List<Producto> buscar(String nombre, Long categoriaId, Estado estado) {
        String filtroNombre = StringUtils.hasText(nombre) ? nombre.trim() : null;
        return productoRepository.buscar(filtroNombre, categoriaId, estado);
    }

    @Transactional(readOnly = true)
    public InventarioResumen resumen() {
        long activos = productoRepository.countByEstado(Estado.ACTIVO);
        long inactivos = productoRepository.countByEstado(Estado.INACTIVO);
        long bajoStock = productoRepository.countByEstadoAndStockLessThanEqual(Estado.ACTIVO, Producto.UMBRAL_BAJO_STOCK);
        return new InventarioResumen(activos, inactivos, bajoStock);
    }

    @Transactional(readOnly = true)
    public Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto solicitado no existe"));
    }

    @Transactional
    public Producto crear(ProductoForm form) {
        Categoria categoria = categoriaService.obtener(form.getCategoriaId());
        if (!categoria.isActiva()) {
            throw new ReglaNegocioException("No se puede registrar un producto en la categoría inactiva «"
                    + categoria.getNombre() + "»");
        }
        Producto producto = new Producto();
        aplicarCambios(producto, form, categoria);
        producto.setEstado(Estado.ACTIVO);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, ProductoForm form) {
        Producto producto = obtener(id);
        Categoria categoria = categoriaService.obtener(form.getCategoriaId());
        boolean cambiaCategoria = !categoria.getId().equals(producto.getCategoria().getId());
        if (!categoria.isActiva() && (producto.isActivo() || cambiaCategoria)) {
            throw new ReglaNegocioException("La categoría «" + categoria.getNombre()
                    + "» está inactiva; selecciona una categoría activa");
        }
        aplicarCambios(producto, form, categoria);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto inactivar(Long id) {
        Producto producto = obtener(id);
        if (!producto.isActivo()) {
            throw new ReglaNegocioException("El producto «" + producto.getNombre() + "» ya se encuentra inactivo");
        }
        producto.setEstado(Estado.INACTIVO);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto activar(Long id) {
        Producto producto = obtener(id);
        if (producto.isActivo()) {
            throw new ReglaNegocioException("El producto «" + producto.getNombre() + "» ya se encuentra activo");
        }
        if (!producto.getCategoria().isActiva()) {
            throw new ReglaNegocioException("No se puede activar «" + producto.getNombre()
                    + "» porque su categoría «" + producto.getCategoria().getNombre()
                    + "» está inactiva. Activa la categoría o edita el producto para asignarle otra.");
        }
        producto.setEstado(Estado.ACTIVO);
        return productoRepository.save(producto);
    }

    private void aplicarCambios(Producto producto, ProductoForm form, Categoria categoria) {
        producto.setNombre(form.getNombre().trim().replaceAll("\\s+", " "));
        producto.setPrecio(form.getPrecio());
        producto.setStock(form.getStock());
        producto.setCategoria(categoria);
    }
}
