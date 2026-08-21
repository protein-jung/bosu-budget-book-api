package com.bosu.housebook.category;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefaultCategoryTemplateRepository extends JpaRepository<DefaultCategoryTemplate, Long> {

    List<DefaultCategoryTemplate> findAllByOrderBySortOrderAscIdAsc();
}
