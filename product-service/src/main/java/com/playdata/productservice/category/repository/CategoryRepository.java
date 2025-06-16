package com.playdata.productservice.category.repository;

import com.playdata.productservice.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryId(Long categoryId);

    @Query(value ="select * from tbl_categories where category_id > 8 order by category_id ASC", nativeQuery = true)
    Optional<Category> findByExtraCategory();

}
