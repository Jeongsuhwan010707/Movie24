package project.movie24.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.movie24.store.domain.StoreCategory;
import project.movie24.store.domain.StoreItem;
import project.movie24.store.dto.StoreItemRequest;
import project.movie24.store.dto.StoreItemResponse;
import project.movie24.store.service.StoreItemService;

import java.util.List;

@RestController
@RequestMapping("/api/store/items")
@RequiredArgsConstructor
public class StoreItemApiController {

    private final StoreItemService storeItemService;

    @PostMapping
    public ResponseEntity<StoreItemResponse> register(@Valid @RequestBody StoreItemRequest request) {
        StoreItem item = storeItemService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StoreItemResponse.from(item));
    }

    @GetMapping
    public ResponseEntity<List<StoreItemResponse>> list(@RequestParam(required = false) StoreCategory category) {
        List<StoreItem> items = category == null ? storeItemService.findAll() : storeItemService.findByCategory(category);
        return ResponseEntity.ok(items.stream().map(StoreItemResponse::from).toList());
    }

    @GetMapping("/{storeItemId}")
    public ResponseEntity<StoreItemResponse> findOne(@PathVariable Long storeItemId) {
        return ResponseEntity.ok(StoreItemResponse.from(storeItemService.findOne(storeItemId)));
    }

    @PutMapping("/{storeItemId}")
    public ResponseEntity<StoreItemResponse> update(@PathVariable Long storeItemId, @Valid @RequestBody StoreItemRequest request) {
        StoreItem item = storeItemService.update(storeItemId, request);
        return ResponseEntity.ok(StoreItemResponse.from(item));
    }

    @DeleteMapping("/{storeItemId}")
    public ResponseEntity<Void> delete(@PathVariable Long storeItemId) {
        storeItemService.delete(storeItemId);
        return ResponseEntity.noContent().build();
    }
}
