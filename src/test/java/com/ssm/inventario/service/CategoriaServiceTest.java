package com.ssm.inventario.service;

import com.ssm.inventario.dto.CategoriaForm;
import com.ssm.inventario.exception.RecursoNoEncontradoException;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.model.Estado;
import com.ssm.inventario.repository.CategoriaRepository;
import com.ssm.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void crearNormalizaNombreYQuedaActiva() {
        CategoriaForm form = new CategoriaForm();
        form.setNombre("   Línea   Blanca  ");
        when(categoriaRepository.existsByNombreIgnoreCase("Línea Blanca")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        Categoria creada = categoriaService.crear(form);

        assertThat(creada.getNombre()).isEqualTo("Línea Blanca");
        assertThat(creada.getEstado()).isEqualTo(Estado.ACTIVO);
    }

    @Test
    void crearRechazaNombreDuplicado() {
        CategoriaForm form = new CategoriaForm();
        form.setNombre("Ropa");
        when(categoriaRepository.existsByNombreIgnoreCase("Ropa")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.crear(form))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Ya existe");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noPermiteInactivarCategoriaConProductosActivos() {
        Categoria categoria = categoria(1L, "Electrónica", Estado.ACTIVO);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaIdAndEstado(1L, Estado.ACTIVO)).thenReturn(3L);

        assertThatThrownBy(() -> categoriaService.inactivar(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("3 producto(s) activo(s)");
        assertThat(categoria.getEstado()).isEqualTo(Estado.ACTIVO);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void inactivaCategoriaSinProductosActivos() {
        Categoria categoria = categoria(2L, "Juguetes", Estado.ACTIVO);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(productoRepository.countByCategoriaIdAndEstado(2L, Estado.ACTIVO)).thenReturn(0L);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.inactivar(2L);

        assertThat(resultado.getEstado()).isEqualTo(Estado.INACTIVO);
    }

    @Test
    void reactivaCategoriaInactiva() {
        Categoria categoria = categoria(3L, "Hogar", Estado.INACTIVO);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.activar(3L);

        assertThat(resultado.getEstado()).isEqualTo(Estado.ACTIVO);
    }

    @Test
    void obtenerCategoriaInexistenteLanzaExcepcion() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Categoria categoria(Long id, String nombre, Estado estado) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setEstado(estado);
        return categoria;
    }
}
