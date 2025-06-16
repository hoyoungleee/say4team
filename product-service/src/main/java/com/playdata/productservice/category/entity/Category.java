package com.playdata.productservice.category.entity;

import com.playdata.productservice.category.dto.CategoryResDto;
import com.playdata.productservice.common.entity.BaseTimeEntity;
import com.playdata.productservice.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_categories")
public class Category extends BaseTimeEntity {

    //카테고리 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    //카테고리 이름
    @Column(name = "category_name",nullable = false)
    private String categoryName;

    //카테고리 화면 뒷배경 쓸 이미지 주소
    @Column(length = 1000,name = "category_bg_ImgUrl", nullable = false)
    private String categoryBgImgUrl;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> product;

    public static CategoryResDto fromEntity(Category category) {
        return CategoryResDto.builder()
                .categoryId(category.categoryId)
                .categoryName(category.categoryName)
                .categoryBgImgUrl(category.categoryBgImgUrl)
                .build();
    }
}
