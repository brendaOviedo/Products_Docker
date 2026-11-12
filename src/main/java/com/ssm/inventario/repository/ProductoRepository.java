package com.ssm.inventario.repository;

import com.ssm.inventario.model.Estado;
import com.ssm.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            SELECT p FROM Producto p JOIN FETCH p.categoria c
            WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:categoriaId IS NULL OR c.id = :categoriaId)
              AND (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscar(@Param("nombre") String nombre,
                          @Param("categoriaId") Long categoriaId,
                          @Param("estado") Estado estado);

    @Query("""
            SELECT p.categoria.id,
                   SUM(CASE WHEN p.estado = com.ssm.inventario.model.Estado.ACTIVO THEN 1 ELSE 0 END),
                   COUNT(p)
            FROM Producto p
            GROUP BY p.categoria.id
            """)
    List<Object[]> contarPorCategoria();

    long countByCategoriaIdAndEstado(Long categoriaId, Estado estado);

    long countByEstado(Estado estado);

    long countByEstadoAndStockLessThanEqual(Estado estado, Integer stock);
}
