package com.modeon.backend.config;

import com.modeon.backend.entity.*;
import com.modeon.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadInitialData() {
        return args -> {
            // 이미 데이터가 있으면 스킵
            if (userRepository.count() > 1 || productRepository.count() > 0) {
                log.info("초기 데이터가 이미 존재합니다. 스킵합니다.");
                return;
            }

            log.info("초기 데이터 로딩 시작...");

            // 1. 테스트 유저 생성
            createTestUsers();

            // 2. 카테고리 생성
            List<Category> categories = createCategories();

            // 3. 더미 상품 생성
            createDummyProducts(categories);

            log.info("초기 데이터 로딩 완료!");
        };
    }

    private void createTestUsers() {
        log.info("테스트 유저 생성 중...");

        // 이메일 인증 완료된 일반 유저 1
        User user1 = User.builder()
                .username("testuser1")
                .email("testuser1@example.com")
                .fullName("테스트 유저1")
                .password(passwordEncoder.encode("password123"))
                .role("ROLE_USER")
                .enabled(true) // 이메일 인증 완료
                .point(10000)
                .membership(MembershipLevel.WELCOME)
                .birthMonth(LocalDate.of(1990, 1, 15))
                .address("서울시 강남구 테헤란로 123")
                .build();

        // 이메일 인증 완료된 일반 유저 2
        User user2 = User.builder()
                .username("testuser2")
                .email("testuser2@example.com")
                .fullName("테스트 유저2")
                .password(passwordEncoder.encode("password123"))
                .role("ROLE_USER")
                .enabled(true) // 이메일 인증 완료
                .point(50000)
                .membership(MembershipLevel.SILVER)
                .birthMonth(LocalDate.of(1995, 6, 20))
                .address("서울시 마포구 월드컵로 456")
                .build();

        // 이메일 인증 완료된 일반 유저 3
        User user3 = User.builder()
                .username("testuser3")
                .email("testuser3@example.com")
                .fullName("테스트 유저3")
                .password(passwordEncoder.encode("password123"))
                .role("ROLE_USER")
                .enabled(true) // 이메일 인증 완료
                .point(100000)
                .membership(MembershipLevel.GOLD)
                .birthMonth(LocalDate.of(1988, 12, 5))
                .address("경기도 성남시 분당구 판교역로 789")
                .build();

        userRepository.saveAll(List.of(user1, user2, user3));
        log.info("✅ 테스트 유저 3명 생성 완료 (모두 이메일 인증 완료)");
    }

    private List<Category> createCategories() {
        log.info("카테고리 생성 중...");

        List<Category> allCategories = new ArrayList<>();

        // 최상위 카테고리 (depth 0)
        Category men = Category.builder().name("남성").depth(0).build();
        Category women = Category.builder().name("여성").depth(0).build();
        Category kids = Category.builder().name("키즈").depth(0).build();

        categoryRepository.saveAll(List.of(men, women, kids));

        // 남성 하위 카테고리 (depth 1)
        Category menOuter = Category.builder().name("아우터").depth(1).parent(men).build();
        Category menTop = Category.builder().name("상의").depth(1).parent(men).build();
        Category menBottom = Category.builder().name("하의").depth(1).parent(men).build();
        Category menShoes = Category.builder().name("신발").depth(1).parent(men).build();

        // 여성 하위 카테고리 (depth 1)
        Category womenOuter = Category.builder().name("아우터").depth(1).parent(women).build();
        Category womenTop = Category.builder().name("상의").depth(1).parent(women).build();
        Category womenBottom = Category.builder().name("하의").depth(1).parent(women).build();
        Category womenDress = Category.builder().name("원피스").depth(1).parent(women).build();
        Category womenShoes = Category.builder().name("신발").depth(1).parent(women).build();

        // 키즈 하위 카테고리 (depth 1)
        Category kidsOuter = Category.builder().name("아우터").depth(1).parent(kids).build();
        Category kidsTop = Category.builder().name("상의").depth(1).parent(kids).build();
        Category kidsBottom = Category.builder().name("하의").depth(1).parent(kids).build();

        categoryRepository.saveAll(List.of(
                menOuter, menTop, menBottom, menShoes,
                womenOuter, womenTop, womenBottom, womenDress, womenShoes,
                kidsOuter, kidsTop, kidsBottom
        ));

        allCategories.addAll(List.of(
                men, women, kids,
                menOuter, menTop, menBottom, menShoes,
                womenOuter, womenTop, womenBottom, womenDress, womenShoes,
                kidsOuter, kidsTop, kidsBottom
        ));

        log.info("✅ 카테고리 생성 완료 (총 " + allCategories.size() + "개)");
        return allCategories;
    }

    private void createDummyProducts(List<Category> categories) {
        log.info("더미 상품 생성 중...");

        // 카테고리별로 필터링
        Category menOuter = categories.stream()
                .filter(c -> c.getName().equals("아우터") && c.getParent() != null && c.getParent().getName().equals("남성"))
                .findFirst().orElse(null);

        Category menTop = categories.stream()
                .filter(c -> c.getName().equals("상의") && c.getParent() != null && c.getParent().getName().equals("남성"))
                .findFirst().orElse(null);

        Category womenTop = categories.stream()
                .filter(c -> c.getName().equals("상의") && c.getParent() != null && c.getParent().getName().equals("여성"))
                .findFirst().orElse(null);

        Category womenDress = categories.stream()
                .filter(c -> c.getName().equals("원피스"))
                .findFirst().orElse(null);

        List<Product> products = new ArrayList<>();

        // 남성 아우터 상품
        if (menOuter != null) {
            products.add(createProduct("클래식 블랙 코트", 159000, Gender.MAN, menOuter,
                    "https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?w=800"));
            products.add(createProduct("캐주얼 데님 재킷", 89000, Gender.MAN, menOuter,
                    "https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=800"));
            products.add(createProduct("레더 라이더 재킷", 249000, Gender.MAN, menOuter,
                    "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=800"));
        }

        // 남성 상의 상품
        if (menTop != null) {
            products.add(createProduct("베이직 화이트 티셔츠", 29000, Gender.MAN, menTop,
                    "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=800"));
            products.add(createProduct("스트라이프 셔츠", 59000, Gender.MAN, menTop,
                    "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800"));
            products.add(createProduct("니트 스웨터", 79000, Gender.MAN, menTop,
                    "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=800"));
        }

        // 여성 상의 상품
        if (womenTop != null) {
            products.add(createProduct("플로럴 블라우스", 69000, Gender.WOMAN, womenTop,
                    "https://images.unsplash.com/photo-1485968579580-b6d095142e6e?w=800"));
            products.add(createProduct("베이직 크롭 티셔츠", 35000, Gender.WOMAN, womenTop,
                    "https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=800"));
            products.add(createProduct("실크 캐미솔", 89000, Gender.WOMAN, womenTop,
                    "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=800"));
        }

        // 여성 원피스 상품
        if (womenDress != null) {
            products.add(createProduct("플리츠 미디 원피스", 129000, Gender.WOMAN, womenDress,
                    "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800"));
            products.add(createProduct("랩 드레스", 159000, Gender.WOMAN, womenDress,
                    "https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=800"));
            products.add(createProduct("시폰 롱 원피스", 189000, Gender.WOMAN, womenDress,
                    "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=800"));
        }

        // 상품 저장
        products = productRepository.saveAll(products);

        // 각 상품에 대한 variants 생성
        for (Product product : products) {
            createProductVariants(product);
            createProductImages(product);
        }

        log.info("✅ 더미 상품 생성 완료 (총 " + products.size() + "개)");
    }

    private Product createProduct(String name, int price, Gender gender, Category category, String imageUrl) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .gender(gender)
                .category(category)
                .build();

        // 대표 이미지 추가
        ProductImage mainImage = ProductImage.builder()
                .imageUrl(imageUrl)
                .sortOrder(0)
                .product(product)
                .build();

        product.getDetailImages().add(mainImage);

        return product;
    }

    private void createProductVariants(Product product) {
        List<ProductVariant> variants = new ArrayList<>();

        // 각 상품에 다양한 사이즈와 색상의 variants 추가
        Color[] colors = {Color.BLACK, Color.WHITE, Color.NAVY};
        Size[] sizes = {Size.S, Size.M, Size.L, Size.XL};

        for (Color color : colors) {
            for (Size size : sizes) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .color(color)
                        .size(size)
                        .stock((int) (Math.random() * 50) + 10) // 10~59 사이의 랜덤 재고
                        .build();
                variants.add(variant);
            }
        }

        productVariantRepository.saveAll(variants);
    }

    private void createProductImages(Product product) {
        // 추가 상세 이미지 (이미 대표 이미지는 있으므로 2~4번째 이미지 추가)
        List<ProductImage> additionalImages = List.of(
                ProductImage.builder()
                        .imageUrl("https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=800")
                        .sortOrder(1)
                        .product(product)
                        .build(),
                ProductImage.builder()
                        .imageUrl("https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=800")
                        .sortOrder(2)
                        .product(product)
                        .build()
        );

        product.getDetailImages().addAll(additionalImages);
        productRepository.save(product);
    }
}
