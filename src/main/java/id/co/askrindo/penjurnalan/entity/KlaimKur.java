package id.co.askrindo.penjurnalan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "klaim_kur", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class KlaimKur {
    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "no_rekening")
    private String noRekening;

    @Column(name = "no_sertifikat")
    private String noSertifikat;

    @Column(name = "net_claim_approved")
    private BigDecimal netClaimApproved;

    @Column(name = "nilai_tuntutan")
    private BigDecimal nilaiTuntutan;
}