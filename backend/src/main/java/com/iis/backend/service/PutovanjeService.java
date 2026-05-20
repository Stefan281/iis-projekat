package com.iis.backend.service;

import com.iis.backend.dto.PutovanjeRequest;
import com.iis.backend.dto.PutovanjeResponse;
import com.iis.backend.exception.ResourceNotFoundException;
import com.iis.backend.model.Putovanje;
import com.iis.backend.repository.PutovanjeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PutovanjeService {

    private final PutovanjeRepository putovanjeRepository;

    public PutovanjeService(PutovanjeRepository putovanjeRepository) {
        this.putovanjeRepository = putovanjeRepository;
    }

    @Transactional(readOnly = true)
    public List<PutovanjeResponse> getAllPutovanja() {
        return putovanjeRepository.findAll().stream()
                .map(PutovanjeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PutovanjeResponse getPutovanjeById(Long id) {
        Putovanje putovanje = findPutovanjeOrThrow(id);
        return PutovanjeResponse.from(putovanje);
    }

    @Transactional
    public PutovanjeResponse createPutovanje(PutovanjeRequest request) {
        Putovanje putovanje = new Putovanje();
        applyRequest(putovanje, request);
        Putovanje saved = putovanjeRepository.save(putovanje);
        return PutovanjeResponse.from(saved);
    }

    @Transactional
    public PutovanjeResponse updatePutovanje(Long id, PutovanjeRequest request) {
        Putovanje putovanje = findPutovanjeOrThrow(id);
        applyRequest(putovanje, request);
        Putovanje saved = putovanjeRepository.save(putovanje);
        return PutovanjeResponse.from(saved);
    }

    @Transactional
    public void deletePutovanje(Long id) {
        Putovanje putovanje = findPutovanjeOrThrow(id);
        putovanjeRepository.delete(putovanje);
    }

    private Putovanje findPutovanjeOrThrow(Long id) {
        return putovanjeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Putovanje sa ID " + id + " nije pronađeno"));
    }

    private void applyRequest(Putovanje putovanje, PutovanjeRequest request) {
        putovanje.setNaziv(request.getNaziv());
        putovanje.setLokacija(request.getLokacija());
        putovanje.setPovod(request.getPovod());
        putovanje.setDatumPolaska(request.getDatumPolaska());
        putovanje.setDatumPovratka(request.getDatumPovratka());
        putovanje.setStatus(request.getStatus());
    }
}
