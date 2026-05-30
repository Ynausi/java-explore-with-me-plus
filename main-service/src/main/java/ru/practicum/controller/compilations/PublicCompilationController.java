package ru.practicum.controller.compilations;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationResponse;
import ru.practicum.dto.compilation.GetCompilationListDto;
import ru.practicum.service.compilation.CompilationService;

import java.util.Collection;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationResponse> getById(@PathVariable("compId") Long compId) {
        return ResponseEntity.ok().body(compilationService.findById(compId));
    }

    @GetMapping
    public ResponseEntity<Collection<CompilationResponse>> getNeededCompilation(
            @Valid @RequestBody GetCompilationListDto getCompilationListDto) {
        return ResponseEntity.ok().body(compilationService.findNeededCompilation(getCompilationListDto));
    }
}
