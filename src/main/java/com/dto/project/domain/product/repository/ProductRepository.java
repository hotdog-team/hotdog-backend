package com.dto.project.domain.product.repository;

import com.dto.project.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    List<Product> findByStatus(String status);

    @Query(value = """
            SELECT product_id
            FROM (
                SELECT p.id AS product_id,
                       ROW_NUMBER() OVER (ORDER BY p.weight_score DESC) AS rn,
                       COUNT(*) OVER () AS cnt
                FROM products p
                JOIN (
                    SELECT product_id, MAX(event_timestamp) AS last_log_at
                    FROM product_weight_logs
                    GROUP BY product_id
                ) l ON l.product_id = p.id
                WHERE p.status = 'ON_SALE'
                  AND l.last_log_at >= :activeSince
            )
            WHERE rn <= CEIL(cnt * :percent / 100.0)
            """, nativeQuery = true)
    List<Long> findPopularProductIds(
            @Param("activeSince") LocalDateTime activeSince,
            @Param("percent") int percent);

    @Query(value = """
            SELECT product_id
            FROM (
                SELECT product_id,
                       ROW_NUMBER() OVER (ORDER BY trend_score DESC) AS rn,
                       COUNT(*) OVER () AS cnt
                FROM (
                    SELECT p.id AS product_id,
                           SUM(l.applied_weight) AS trend_score
                    FROM products p
                    JOIN product_weight_logs l ON l.product_id = p.id
                    WHERE p.status = 'ON_SALE'
                      AND l.event_timestamp >= :trendingSince
                    GROUP BY p.id
                )
            )
            WHERE rn <= CEIL(cnt * :percent / 100.0)
            """, nativeQuery = true)
    List<Long> findTrendingProductIds(
            @Param("trendingSince") LocalDateTime trendingSince,
            @Param("percent") int percent);
}
