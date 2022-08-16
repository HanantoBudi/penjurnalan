package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.TIjpProjected;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TIjpProjectedRepository extends JpaRepository<TIjpProjected, String> {

}