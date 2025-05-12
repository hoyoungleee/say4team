package com.playdata.productservice.product.service;

import com.playdata.productservice.common.configs.AwsS3Config;
import com.playdata.productservice.product.dto.ProductResDto;
import com.playdata.productservice.product.dto.ProductSaveReqDto;
import com.playdata.productservice.product.dto.ProductSearchDto;
import com.playdata.productservice.product.entity.Category;
import com.playdata.productservice.product.entity.Product;
import com.playdata.productservice.product.entity.ProductImages;
import com.playdata.productservice.product.repository.CategoryRepository;
import com.playdata.productservice.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AwsS3Config s3Config;

    public Product productCreate(ProductSaveReqDto dto) throws IOException {

        MultipartFile mainImage = dto.getMainImage();
        MultipartFile thumbnailImage = dto.getThumbnailImage();


        String uniqueMainImageName
                = UUID.randomUUID() + "_" + mainImage.getOriginalFilename();
        String uniqueThumbnailImageName
                = UUID.randomUUID() + "_" + thumbnailImage.getOriginalFilename();

        String mainImageUrl
                = s3Config.uploadToS3Bucket(mainImage.getBytes(), uniqueMainImageName);
        String thumbnailImageUrl
                = s3Config.uploadToS3Bucket(thumbnailImage.getBytes(), uniqueThumbnailImageName);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        dto.setMainImagePath(mainImageUrl);
        dto.setThumbnailPath(thumbnailImageUrl);
        Product product = dto.toEntity(category);


        List<ProductImages> images = new ArrayList<>();


        for (int i =0; i<dto.getImages().size(); i++) {
            MultipartFile image = dto.getImages().get(i);
            ProductImages productImages = new ProductImages();
            String uniqueImageName
                    = UUID.randomUUID() + "_" + image.getOriginalFilename();
            String imageUrl
                    = s3Config.uploadToS3Bucket(image.getBytes(), uniqueImageName);
            productImages.setImgUrl(imageUrl);
            productImages.setImgOrder(i);
            productImages.setProduct(product);
            images.add(productImages);
        }

        product.setProductImages(images);


        return productRepository.save(product);

    }

    public List<ProductResDto> productList(ProductSearchDto dto, Pageable pageable) {
        Page<Product> products;
        if (dto.getSearchType() == null) {
            products = productRepository.findAll(pageable);
        } else if (dto.getSearchType().equals("name")) {
            products = productRepository.findByNameValue(dto.getSearchName(), pageable);
        } else {
            products = productRepository.findByCategoryId(dto.getCategoryId(), pageable);
        }

        List<Product> productList = products.getContent();

        return productList.stream()
                .map(Product::fromEntity)
                .collect(Collectors.toList());
    }

    public void productDelete(Long id) throws Exception {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Product with id: " + id + " not found")
        );

        for (ProductImages image : product.getProductImages()) {
            s3Config.deleteFromS3Bucket(image.getImgUrl());
        }


        String thumbnailPath = product.getThumbnailPath();
        String mainImagePath = product.getMainImagePath();
        s3Config.deleteFromS3Bucket(thumbnailPath);
        s3Config.deleteFromS3Bucket(mainImagePath);

        productRepository.deleteById(id);
    }

    public ProductResDto getProductInfo(Long prodId) {
        Product product = productRepository.findById(prodId).orElseThrow(
                () -> new EntityNotFoundException("Product with id: " + prodId + " not found")
        );

        //연관 테이블 데이터 명시적으로 불러와서 영속성주기
        product.getProductImages();

        return product.fromEntity();
    }

    public void updateStockQuantity(Long prodId, int stockQuantity) {
        Product foundProduct = productRepository.findById(prodId).orElseThrow(
                () -> new EntityNotFoundException("Product with id: " + prodId + " not found")
        );
        foundProduct.setStockQuantity(stockQuantity);
        productRepository.save(foundProduct);
    }

    public List<ProductResDto> getProductsName(List<Long> productIds) {
        List<Product> products = productRepository.findByProductIdIn(productIds);

        return products.stream()
                .map(Product::fromEntity)
                .collect(Collectors.toList());
    }

    public void cancelProduct(Map<Long, Integer> map) {
        for (Long key : map.keySet()) {
            Product foundProd = productRepository.findById(key).orElseThrow(
                    () -> new EntityNotFoundException("Product with id: " + key + " not found")
            );
            int quantity = foundProd.getStockQuantity();
            foundProd.setStockQuantity(quantity + map.get(key));
            productRepository.save(foundProd);
        }
    }
}









