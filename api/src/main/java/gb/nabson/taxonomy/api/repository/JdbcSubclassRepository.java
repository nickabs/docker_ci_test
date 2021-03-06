package gb.nabson.taxonomy.api.repository;

import gb.nabson.taxonomy.api.model.Subclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Repository

@Primary // use @Qualifier("subclassRepositoryBlah") in constructor to override
public class JdbcSubclassRepository implements SubclassRepository {

    private final JdbcTemplate jdbcTemplate ;
    private final DivisionRepository divisionRepository;

    @Autowired
    public JdbcSubclassRepository(JdbcTemplate jdbcTemplate, JdbcDivisionRepository divisionRepository)
    {
        this.jdbcTemplate=jdbcTemplate;
        this.divisionRepository = divisionRepository;
    };


    @Override
    public List<Subclass> findAll(String divisionId) {

        String sql = "SELECT subclass.id, division.id division_id, division.name division_name, division.description division_description," +
                " subclass.name, subclass.description" +
                " FROM subclass , division" +
                " WHERE subclass.division_id = division.id "+
                " AND division.id = ?";


//        RowMapper<Subclass> rowMapper = new BeanPropertyRowMapper<Subclass>(Subclass.class);

        return this.jdbcTemplate.query(sql, new SubclassRowMapper(new DivisionRowMapper()), divisionId);
    }

    @Override
    public Subclass findById(String id) {

        String sql = "SELECT id, name, division_id, description FROM subclass where id = ? ";

        RowMapper<Subclass> rowMapper = new BeanPropertyRowMapper<Subclass>(Subclass.class);
        return this.jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    // add or replace subclass
    @Override
    public void save(Subclass subclass) {
        String sql = "INSERT INTO subclass (id, division_id, name, description) VALUES (?,?,?,?) " +
                "ON CONFLICT (id,division_id) DO UPDATE " +
                "SET name = EXCLUDED.name, description = EXCLUDED.description, division_id = EXCLUDED.division_id" ;
        this.jdbcTemplate.update(sql, subclass.getId(), subclass.getDivision().getId(), subclass.getName(), subclass.getDescription());
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM subclass WHERE id = ?";
        this.jdbcTemplate.update(sql, id);
    }

    @Override
    public long count(String divisionId) {
        String sql = "SELECT COUNT(*) FROM subclass where division_id = ?";
        RowMapper<Long> rowMapper = new BeanPropertyRowMapper<>(Long.class);
        return jdbcTemplate.queryForObject(sql,rowMapper, divisionId);
    }
}
