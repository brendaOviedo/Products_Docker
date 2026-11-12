package com.ssm.inventario.service;

import com.ssm.inventario.dto.ProductoForm;
import com.ssm.inventario.exception.ReglaNegocioException;
import com.ssm.inventario.model.Categoria;
import com.ssm.inventario.model.Estado;
import com.ssm.inventario.model.Producto;
import com.ssm.inventario.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProductoEnCategoriaActiva() {
        Categoria categoria = categoria(1L, Estado.ACTIVO);
        when(categoriaService.obtener(1L)).thenReturn(categoria);
        when(productoRepository.save(any(Producto.class))).thenAnswer(i -> i.getArgument(0));

        Producto producto = productoService.crear(form(1L, "  Teclado   mecánico ", "350.00", 10));

        assertThat(producto.getNombre()).isEqualTo("Teclado mecánico");
        assertThat(producto.getEstado()).isEqualTo(Estado.ACTIVO);
        assertThat(producto.getCategoria()).isSameAs(categoria);
    }

    @Test
    void noPermiteCrearProductoEnCategoriaInactiva() {
        when(categoriaService.obtener(5L)).thenReturn(categoria(5L, Estado.INACTIVO));

        assertThatThrownBy(() -> productoService.crear(form(5L, "Pelota", "25.00", 3)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactiva");
        verify(productoRepository, never()).save(any());
    }

    @Test
    void inactivaProductoActivo() {
        Producto producto = producto(10L, categoria(1L, Estado.ACTIVO), Estado.ACTIVO, 8);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        assertThat(productoService.inactivar(10L).getEstado()).isEqualTo(Estado.INACTIVO);
    }

    @Test
    void reactivaProductoSiSuCategoriaEstaActiva() {
        Producto producto = producto(11L, categoria(1L, Estado.ACTIVO), Estado.INACTIVO, 8);
        when(productoRepository.findById(11L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        assertThat(productoService.activar(11L).getEstado()).isEqualTo(Estado.ACTIVO);
    }

    @Test
    void noPermiteReactivarProductoConCategoriaInactiva() {
        Producto producto = producto(12L, categoria(5L, Estado.INACTIVO), Estado.INACTIVO, 8);
        when(productoRepository.findById(12L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> productoService.activar(12L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("categoría");
        assertThat(producto.getEstado()).isEqualTo(Estado.INACTIVO);
    }

    @Test
    void marcaBajoStockCuandoElStockEsMenorOIgualACinco() {
        Categoria categoria = categoria(1L, Estado.ACTIVO);

        assertThat(producto(1L, categoria, Estado.ACTIVO, 0).isBajoStock()).isTrue();
        assertThat(producto(2L, categoria, Estado.ACTIVO, 5).isBajoStock()).isTrue();
        assertThat(producto(3L, categoria, Estado.ACTIVO, 6).isBajoStock()).isFalse();
    }

    private ProductoForm form(Long categoriaId, String nombre, String precio, int stock) {
        ProductoForm form = new ProductoForm();
        form.setCategoriaId(categoriaId);
        form.setNombre(nombre);
        form.setPrecio(new BigDecimal(precio));
        form.setStock(stock);
        return form;
    }

    private Categoria categoria(Long id, Estado estado) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre("Categoría " + id);
        categoria.setEstado(estado);
        return categoria;
    }

    private Producto producto(Long id, Categoria categoria, Estado estado, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto " + id);
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal("10.00"));
        producto.setStock(stock);
        producto.setEstado(estado);
        return producto;
    }
}
