package ru.practicum.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.category.CategoryFilter;
import ru.practicum.model.Category;
import ru.practicum.model.QCategory;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class CategoryCustomRepositoryImpl implements CategoryCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Collection<Category> search(CategoryFilter categoryFilter) {

        QCategory category = QCategory.category;

        return queryFactory
                .selectFrom(category)
                .orderBy(category.id.asc())
                .offset(categoryFilter.getFrom())
                .limit(categoryFilter.getSize())
                .fetch();
    }
}
