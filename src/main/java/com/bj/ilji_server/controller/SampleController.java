// src/main/java/com/bj/ilji_server/controller/SampleController.java
package com.bj.ilji_server.controller;

import com.bj.ilji_server.entity.SampleEntity;
import com.bj.ilji_server.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SampleController {

    private final SampleRepository repo;

    @PostMapping
    public ResponseEntity<SampleEntity> create(@RequestBody SampleEntity req) {
        SampleEntity saved = repo.save(req);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleEntity> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<SampleEntity> list() {
        return repo.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SampleEntity> update(@PathVariable Long id, @RequestBody SampleEntity req) {
        return repo.findById(id)
                .map(entity -> {
                    entity.setName(req.getName());
                    return ResponseEntity.ok(repo.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}