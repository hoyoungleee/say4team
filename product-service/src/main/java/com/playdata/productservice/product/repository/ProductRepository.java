package com.playdata.productservice.product.repository;

import com.playdata.productservice.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 검색 조건(카테고리, 검색어)에 따른 페이징
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = ?1")
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1%")
    Page<Product> findByNameValue(String keyword,
                                  Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:name% AND c.categoryId = :categoryId")
    Page<Product> findByNameValueAndCategory_CategoryId(@Param("name") String name, @Param("categoryId") Long categoryId, Pageable pageable);

    List<Product> findByProductIdIn(List<Long> ids);

}
