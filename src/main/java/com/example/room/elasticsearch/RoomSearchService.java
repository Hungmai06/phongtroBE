package com.example.room.elasticsearch;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.json.JsonData;
import com.example.room.dto.PageResponse;
import com.example.room.model.Room;
import com.example.room.utils.Enums.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class RoomSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "rooms";

    public void indexRoom(Room room) {
        try {
            RoomDocument doc = RoomMapper.toDocument(room);
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(doc.getId())
                    .document(doc)
                    // ensure doc is visible for search right away; can be changed to WaitFor or True depending on needs
                    .refresh(Refresh.WaitFor)
            );
            if (response.result() != null) {
                log.debug("Indexed room id={} result={}", doc.getId(), response.result().jsonValue());
            } else {
                log.debug("Indexed room id={} (no result returned)", doc.getId());
            }
        } catch (IOException e) {
            log.error("Không thể index Room id={} vào Elasticsearch", room != null && room.getId() != null ? room.getId() : null, e);
            throw new RuntimeException("Không thể index Room vào Elasticsearch", e);
        }
    }

    public void deleteById(String id) {
        try {
            DeleteResponse response = elasticsearchClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(id)
                    .refresh(Refresh.WaitFor)
            );
            log.debug("Deleted room id={} result={}", id, response.result() != null ? response.result().jsonValue() : null);
        } catch (IOException e) {
            log.error("Không thể xóa Room id={} khỏi Elasticsearch", id, e);
            throw new RuntimeException("Không thể xóa Room khỏi Elasticsearch", e);
        }
    }

    public PageResponse<RoomDocument> searchAdvanced(
             String keyword,
             String status,
             String type,
             Float minArea,
             Float maxArea,
             Integer minCapacity,
             int page,
             int size
     ) throws IOException {

        BoolQuery.Builder bool = new BoolQuery.Builder();

        if (keyword != null && !keyword.isEmpty()) {
            bool.must(MultiMatchQuery.of(m -> m
                    .fields("name", "description", "address")
                    .query(keyword)
            )._toQuery());
        }

        if (status != null && !status.isEmpty()) {
            bool.must(MatchQuery.of(m -> m
                    .field("status")
                    .query(status.toUpperCase())
            )._toQuery());
        }
        if (type != null && !type.isEmpty()) {
            bool.must(MatchQuery.of(m -> m
                    .field("type")
                    .query(type.toUpperCase())
            )._toQuery());
        }

        if (minArea != null || maxArea != null) {
            bool.filter(RangeQuery.of(r -> r
                    .field("area")
                    .gte(JsonData.of(minArea != null ? minArea : 0))
                    .lte(JsonData.of(maxArea != null ? maxArea : Float.MAX_VALUE))
            )._toQuery());
        }

        if (minCapacity != null) {
            bool.filter(RangeQuery.of(r -> r
                    .field("capacity")
                    .gte(JsonData.of(minCapacity))
            )._toQuery());
        }

        bool.filter(TermQuery.of(t -> t.field("deleted").value(false))._toQuery());

        int from = page * size;

        SearchRequest request = SearchRequest.of(s -> s
                .index(INDEX_NAME)
                .from(from)
                .size(size)
                .query(q -> q.bool(bool.build()))
        );

        SearchResponse<RoomDocument> response =
                elasticsearchClient.search(request, RoomDocument.class);

        List<RoomDocument> results = response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());

        long totalElements = response.hits().total() != null
                ? response.hits().total().value()
                : results.size();

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<RoomDocument>builder()
                .code(200)
                .message("Success")
                .data(results)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}