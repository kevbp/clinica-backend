package com.clinica.caja.service;

import com.clinica.caja.dto.TarifaConsultaRequestDTO;
import com.clinica.caja.dto.TarifaConsultaResponseDTO;
import com.clinica.caja.model.TarifaConsulta;
import com.clinica.caja.repository.TarifaConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaConsultaService {

    private final TarifaConsultaRepository tarifaRepository;

    @Transactional
    public TarifaConsultaResponseDTO guardar(TarifaConsultaRequestDTO request) {
        TarifaConsulta tarifa = new TarifaConsulta();
        tarifa.setIdEspecialidad(request.getIdEspecialidad());
        tarifa.setMonto(request.getMonto());
        return toResponse(tarifaRepository.save(tarifa));
    }

    @Transactional(readOnly = true)
    public List<TarifaConsultaResponseDTO> listar() {
        return tarifaRepository.findAll().stream().map(this::toResponse).toList();
    }

    private TarifaConsultaResponseDTO toResponse(TarifaConsulta t) {
        TarifaConsultaResponseDTO dto = new TarifaConsultaResponseDTO();
        dto.setIdEspecialidad(t.getIdEspecialidad());
        dto.setMonto(t.getMonto());
        return dto;
    }
}
