package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.ProductAcs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAcsRepository extends JpaRepository<ProductAcs, String> {

    Optional<ProductAcs> findById (Integer id);

    Optional<ProductAcs> findByJenisKreditAndJenisKur(String jenisKredit, String jenisKur);

}