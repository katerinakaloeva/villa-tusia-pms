package com.villatusia.pms.property;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping("/{propertyId}")
    public PropertyResponse getById(@PathVariable long propertyId) {
        Property property = propertyService.getById(propertyId);
        return PropertyResponse.from(property);
    }

    @PutMapping("/{propertyId}")
    public PropertyResponse update(
            @PathVariable long propertyId,
            @Valid @RequestBody UpdatePropertyRequest request
    ) {
        Property property = propertyService.update(propertyId, request);
        return PropertyResponse.from(property);
    }
}
