package goblin.app.Category.service;

import goblin.app.Category.model.entity.CategoryRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@NoArgsConstructor
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 카테고리 추가

    // 카테고리 수정

    // 카테고리 삭제 - soft (생성한 사용자만)

    // 카테고리 목록 조회

    // 카테고리 개별 조회

    // 카테고리 그룹별 on off

}
