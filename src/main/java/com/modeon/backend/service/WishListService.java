package com.modeon.backend.service;

import com.modeon.backend.entity.Product;
import com.modeon.backend.entity.User;
import com.modeon.backend.entity.WishList;
import com.modeon.backend.exception.BadRequestException;
import com.modeon.backend.repository.ProductRepository;
import com.modeon.backend.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishListService {
    private final AuthenticationService authenticationService;
    private final ProductRepository productRepository;
    private final WishListRepository wishListRepository;

    public boolean toggleWishList(Long ProductId){
        User currentUser = authenticationService.getCurrentUser();

        Product product = productRepository.findById(ProductId).orElseThrow(()-> new BadRequestException("Product is not found"));

        boolean alreadyWishList = wishListRepository.existsByUserAndProduct(currentUser, product);

        if(alreadyWishList){
            wishListRepository.deleteByUserAndProduct(currentUser, product);
            return false;
        } else {
            WishList wishList = WishList.builder()
                    .user(currentUser)
                    .product(product)
                    .build();
            wishListRepository.save(wishList);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public Long getLikeCount(){
        User currentUser = authenticationService.getCurrentUser();
        return wishListRepository.countByUserId(currentUser.getId());
    }
}
