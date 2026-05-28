package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationRequest;
import ru.practicum.dto.compilation.CompilationResponse;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

public interface CompilationService {

    CompilationResponse save(CompilationRequest compilationRequest);

    CompilationResponse update(UpdateCompilationRequest compilationRequest, Long compId);

    void delete(Long compId);

}
