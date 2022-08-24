package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.KlaimKur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KlaimKurRepository extends JpaRepository<KlaimKur, String> {

    Optional<KlaimKur> findById (Integer id);

}