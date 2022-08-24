package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.PenjaminanKurSpr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PenjaminanKurSprRepository extends JpaRepository<PenjaminanKurSpr, String> {

    Optional<PenjaminanKurSpr> findByNoSertifikatSpr(String noSertifikatSpr);

}