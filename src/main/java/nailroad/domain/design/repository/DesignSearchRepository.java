package nailroad.domain.design.repository;

import lombok.RequiredArgsConstructor;
import nailroad.domain.design.enums.DesignCategoryCode;
import nailroad.domain.design.enums.DesignColorCode;
import nailroad.domain.design.enums.DesignShapeCode;
import nailroad.domain.design.enums.TagName;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class DesignSearchRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<Long> findMatchingIds(long cursor, int limit, String keyword,
                                      Set<DesignCategoryCode> categories, Set<DesignShapeCode> shapes,
                                      Set<DesignColorCode> colors, Set<TagName> tags) {
        StringBuilder sql = new StringBuilder("SELECT d.design_id FROM design d WHERE d.design_id < ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(cursor);
        appendFilters(sql, parameters, keyword, categories, shapes, colors, tags);
        sql.append(" ORDER BY d.design_id DESC LIMIT ?");
        parameters.add(limit);

        return jdbcTemplate.query(connection -> prepare(connection, sql.toString(), parameters),
                (result, row) -> result.getLong(1));
    }

    public long countMatching(String keyword, Set<DesignCategoryCode> categories,
                              Set<DesignShapeCode> shapes, Set<DesignColorCode> colors,
                              Set<TagName> tags) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM design d WHERE TRUE");
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, keyword, categories, shapes, colors, tags);

        return jdbcTemplate.query(connection -> prepare(connection, sql.toString(), parameters), result -> {
            result.next();
            return result.getLong(1);
        });
    }

    private void appendFilters(StringBuilder sql, List<Object> parameters, String keyword,
                               Set<DesignCategoryCode> categories, Set<DesignShapeCode> shapes,
                               Set<DesignColorCode> colors, Set<TagName> tags) {
        if (!categories.isEmpty()) {
            sql.append(" AND d.category_code && ?");
            parameters.add(enumNames(categories));
        }
        if (!shapes.isEmpty()) {
            sql.append(" AND d.shape_code && ?");
            parameters.add(enumNames(shapes));
        }
        if (!colors.isEmpty()) {
            sql.append(" AND d.color_code && ?");
            parameters.add(enumNames(colors));
        }
        if (!tags.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM tag t WHERE t.design_id = d.design_id AND t.tag_name = ANY(?))");
            parameters.add(enumNames(tags));
        }
        if (!keyword.isEmpty()) {
            sql.append(" AND (POSITION(? IN UPPER(d.name)) > 0 OR EXISTS (")
                    .append("SELECT 1 FROM tag t WHERE t.design_id = d.design_id ")
                    .append("AND POSITION(? IN t.tag_name) > 0))");
            parameters.add(keyword);
            parameters.add(keyword);
        }
    }

    private PreparedStatement prepare(java.sql.Connection connection, String sql,
                                      List<Object> parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.get(index);
            if (parameter instanceof String[] names) {
                statement.setArray(index + 1, connection.createArrayOf("varchar", names));
            } else {
                statement.setObject(index + 1, parameter);
            }
        }
        return statement;
    }

    private String[] enumNames(Set<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).toArray(String[]::new);
    }
}
