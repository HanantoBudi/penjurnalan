package id.co.askrindo.penjurnalan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "t_ijp_projected", schema = "brisurf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TIjpProjected {

    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "no_rekening_pinjaman", length = 50)
    private String noRekeningPinjaman;

    @Column(name = "urutan_pengajuan")
    private Integer urutanPengajuan;

    @Column(name = "tahun_ke")
    private Integer tahunKe;

    @Column(name = "nominal_ijp")
    private BigDecimal nominalIjp;

    @Column(name = "kompensasi")
    private BigDecimal kompensasi;

    @Column(name = "outstanding_teoritis")
    private BigDecimal outstandingTeoritis;

    @Column(name = "outstanding_real")
    private BigDecimal outstandingReal;

    @Column(name = "tanggal_produksi")
    private Date tanggalProduksi;

    @Column(name = "tanggal_awal")
    private Date tanggalAwal;

    @Column(name = "tanggal_akhir")
    private Date tanggalAkhir;

    @Column(name = "suku_bunga")
    private BigDecimal sukuBunga;

    @Column(name = "loan_type", length = 50)
    private String loanType;

    @Column(name = "f_id_program")
    private Integer fIdProgram;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "id_covering_validation", columnDefinition="uniqueidentifier")
    private String idCoveringValidation;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Date modifiedDate;

    @Column(name = "created_by", length = 50)
    @CreatedBy
    private String createdBy;

    @Column(name = "modified_by")
    @LastModifiedBy
    private String modifiedBy;
}