package com.clinica.farmacia.repository;

import com.clinica.farmacia.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // Lotes vigentes con stock, ordenados por fechaVencimiento ASC (FEFO).
    @Query("""
            SELECT i FROM Inventario i
            JOIN i.lote l
            WHERE l.medicamento.id = :idMedicamento
              AND l.fechaVencimiento >= :hoy
              AND i.cantidadDisponible > 0
            ORDER BY l.fechaVencimiento ASC
            """)
    List<Inventario> findLotesVigentesFEFO(
            @Param("idMedicamento") Long idMedicamento,
            @Param("hoy") LocalDate hoy);

    // Stock total vigente (lotes no vencidos), incluye lotes con cantidad = 0 en la suma.
    @Query("""
            SELECT COALESCE(SUM(i.cantidadDisponible), 0)
            FROM Inventario i
            JOIN i.lote l
            WHERE l.medicamento.id = :idMedicamento
              AND l.fechaVencimiento >= :hoy
            """)
    Integer sumStockVigente(
            @Param("idMedicamento") Long idMedicamento,
            @Param("hoy") LocalDate hoy);
}
