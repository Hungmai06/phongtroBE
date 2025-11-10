package com.example.room.controller;

import com.example.room.dto.PageResponse;
import com.example.room.elasticsearch.RoomDocument;
import com.example.room.elasticsearch.RoomSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/search/rooms")
@RequiredArgsConstructor
@Tag(name = " API Search room advance", description = "Tìm kiếm phòng nâng cao")
public class RoomSearchController {

    private final RoomSearchService roomSearchService;

    @GetMapping("/advanced")
    @Operation(summary = "Tìm kiếm phòng nâng cao với các tiêu chí khác nhau")
    public PageResponse<RoomDocument> searchAdvanced(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Float minArea,
            @RequestParam(required = false) Float maxArea,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {
        return roomSearchService.searchAdvanced(keyword, status, minArea, maxArea, minCapacity, page, size);
    }
}