package com.modeon.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.modeon.backend.entity.Category;
import com.modeon.backend.entity.User;
import com.modeon.backend.repository.CategoryRepository;
import com.modeon.backend.service.NaverAuthService;
import com.modeon.backend.service.NaverCategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class CategoryInitializer {


    @Bean
    public CommandLineRunner initCategories(CategoryRepository categoryRepository, NaverCategoryService naverCategoryService, NaverAuthService naverAuthService) {
        return args -> {
            String accessToken = naverAuthService.requestAccessToken();

            boolean exists = categoryRepository.existsByName("패션의류");

            if (exists) {
                System.out.println("ℹ️ 메인 카테고리가 존재합니다. 초기화 생략.");
                return;
            }

            Category main = new Category();
            main.setNaverCategoryId("50000000");
            main.setName("패션의류");
            main.setDepth(0);
            categoryRepository.save(main);

            System.out.println("✅ 메인 카테고리 생성: " + main.getName());

            saveSubCategories(main, 1, accessToken, categoryRepository, naverCategoryService);
        };
    }

    private void saveSubCategories(
            Category parent,
            int depth,
            String accessToken,
            CategoryRepository categoryRepository,
            NaverCategoryService naverCategoryService
    ) throws JsonProcessingException {



        List<Map<String, Object>> children =
                naverCategoryService.getNaverCategory(parent.getNaverCategoryId(), accessToken);

        for (Map<String, Object> item : children) {
            boolean isLast = (boolean) item.getOrDefault("last", false);

            if (depth >= 3 || (depth == 2 && !isLast)) {
                System.out.println("depth " + depth + " 저장 건너뜀 → " + item.get("name"));
                continue;
            }
            Category child = new Category();
            child.setNaverCategoryId((String) item.get("id"));
            child.setName((String) item.get("name"));
            child.setDepth(depth);
            child.setParent(parent);
            categoryRepository.save(child);

            System.out.println("depth " + depth + " 생성 → " + child.getName());


            if (!isLast) {
                try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                saveSubCategories(child, depth + 1,accessToken, categoryRepository, naverCategoryService);
            }
        }
    }
}