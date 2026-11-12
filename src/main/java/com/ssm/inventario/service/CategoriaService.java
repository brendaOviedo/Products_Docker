package com.ssm.inventario.service;

import com.ssm.inventario.dto.CategoriaForm;
import com.ssm.inventario.dto.CategoriaResumen;
import com.ssm.inventario.exception.RecursoNoEncontradoException;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.model.Estado;
import com.ssm.inventario.repository.CategoriaRepository;
import com.ssm.inventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByEstadoOrderByNombreAsc(Estado.ACTIVO);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResumen> listarConResumen() {
        Map<Long, long[]> conteos = new HashMap<>();
        for (Object[] fila : productoRepository.contarPorCategoria()) {
            Long categoriaId = (Long) fila[0];
            long activos = ((Number) fila[1]).longValue();
            long total = ((Number) fila[2]).longValue();
            conteos.put(categoriaId, new long[]{activos, total});
        }
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(c -> {
                    long[] conteo = conteos.getOrDefault(c.getId(), new long[]{0, 0});
                    return new CategoriaResumen(c, conteo[0], conteo[1]);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría solicitada no existe"));
    }

    @Transactional
    public Categoria crear(CategoriaForm form) {
        String nombre = normalizar(form.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con el nombre «" + nombre + "»");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setEstado(Estado.ACTIVO);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria actualizar(Long id, CategoriaForm form) {
        Categoria categoria = obtener(id);
        String nombre = normalizar(form.getNombre());
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otra categoría con el nombre «" + nombre + "»");
        }
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria inactivar(Long id) {
        Categoria categoria = obtener(id);
        if (!categoria.isActiva()) {
            throw new ReglaNegocioException("La categoría «" + categoria.getNombre() + "» ya se encuentra inactiva");
        }
        long productosActivos = productoRepository.countByCategoriaIdAndEstado(id, Estado.ACTIVO);
        if (productosActivos > 0) {
            throw new ReglaNegocioException("No se puede dar de baja la categoría «" + categoria.getNombre()
                    + "» porque tiene " + productosActivos + " producto(s) activo(s) asociado(s). "
                    + "Da de baja o reasigna esos productos primero.");
        }
        categoria.setEstado(Estado.INACTIVO);
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria activar(Long id) {
        Categoria categoria = obtener(id);
        if (categoria.isActiva()) {
            throw new ReglaNegocioException("La categoría «" + categoria.getNombre() + "» ya se encuentra activa");
        }
        categoria.setEstado(Estado.ACTIVO);
        return categoriaRepository.save(categoria);
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }
}
