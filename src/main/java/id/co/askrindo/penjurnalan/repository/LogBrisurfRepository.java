package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.LogBrisurf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogBrisurfRepository extends JpaRepository<LogBrisurf, String> {

    Optional<LogBrisurf> findById(String id);

}