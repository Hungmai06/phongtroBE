package com.example.room.elasticsearch;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "rooms") // index name trong Elasticsearch
public class RoomDocument {

    @Id
    private String id;
    private List<String> images;
    private List<String> facilities;
    private String name;
    private String description;
    private String address;
    private BigDecimal price;
    private Float area;
    private Integer capacity;
    private String status;// AVAILABLE, RENTED, MAINTENANCE...
    private String type; // APARTMENT, HOUSE, STUDIO...
    private String ownerName;
    private Long ownerId;
    private Boolean deleted;

}