package com.example.room.elasticsearch;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "rooms") // index name trong Elasticsearch
public class RoomDocument {

    @Id
    private String id;

    private String name;
    private String description;
    private String address;
    private BigDecimal price;
    private Float area;
    private Integer capacity;
    private String status;     // AVAILABLE, RENTED, MAINTENANCE...
    private String ownerName;
    private Boolean deleted;

}