package com.clinica.caja.service;

import com.clinica.caja.dto.NotaCreditoRequestDTO;
import com.clinica.caja.dto.NotaCreditoResponseDTO;
import com.clinica.caja.model.EstadoNotaCredito;
import com.clinica.caja.model.EstadoPagoConsulta;
import com.clinica.caja.model.NotaCredito;
import com.clinica.caja.model.PagoConsulta;
import com.clinica.caja.repository.NotaCreditoRepository;
import com.clinica.caja.repository.PagoConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotaCreditoService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final PagoConsultaRepository pagoRepository;

    @Transactional
    public NotaCreditoResponseDTO emitir(NotaCreditoRequestDTO request) {
        PagoConsulta pago = pagoRepository.findByIdCita(request.getIdCita())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe pago de consulta para la cita " + request.getIdCita()));

        if (pago.getEstado() != EstadoPagoConsulta.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo se puede emitir NotaCredito sobre un pago en estado PAGADO.");
        }

        NotaCredito nc = new NotaCredito();
        nc.setIdPaciente(pago.getIdPaciente());
        nc.setMonto(pago.getMonto());
        nc.setIdPagoConsultaOrigen(pago.getId());
        nc.setMotivo(request.getMotivo());
        nc.setEstado(EstadoNotaCredito.DISPONIBLE);

        NotaCredito guardada = notaCreditoRepository.save(nc);

        NotaCreditoResponseDTO dto = new NotaCreditoResponseDTO();
        dto.setId(guardada.getId()); dto.setIdPaciente(guardada.getIdPaciente());
        dto.setMonto(guardada.getMonto()); dto.setIdPagoConsultaOrigen(guardada.getIdPagoConsultaOrigen());
        dto.setMotivo(guardada.getMotivo()); dto.setEstado(guardada.getEstado());
        return dto;
    }
}
