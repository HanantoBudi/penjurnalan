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
@Table(name = "penjaminan_kur_spr", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PenjaminanKurSpr {
    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "id_penjaminan", columnDefinition="uniqueidentifier")
    private String idPenjaminan;

    @Column(name = "line_no")
    private Integer lineNo;

    @Column(name = "no_rekening_spr")
    private String noRekeningSpr;

    @Column(name = "no_rekening_awal")
    private String noRekeningAwal;

    @Column(name = "no_sertifikat_awal")
    private String noSertifikatAwal;

    @Column(name = "no_pk_spr")
    private String noPkSpr;

    @Column(name = "tanggak_pk_spr")
    private Date tanggalPkSpr;

    @Column(name = "tanggak_awal_spr")
    private Date tanggalAwalSpr;

    @Column(name = "tanggak_akhir_spr")
    private Date tanggalAkhirSpr;

    @Column(name = "jangka_waktu_spr")
    private Integer jangkaWaktuSpr;

    @Column(name = "baki_debet")
    private BigDecimal bakiDebet;

    @Column(name = "plafon_spr")
    private BigDecimal plafonSpr;

    @Column(name = "status_data")
    private String statusData;

    @Column(name = "status_rekening")
    private String statusRekening;

    @Column(name = "jenis_penundaan")
    private String jenisPenundaan;

    @Column(name = "tanggal_awal_penundaan")
    private Date tanggalAwalPenundaam;

    @Column(name = "tanggal_akhir_penundaan")
    private Date tanggalAkhirPenundaan;

    @Column(name = "flag_covid")
    private String flagCovid;

    @Column(name = "flag_terbit_acs")
    private String flagTerbitAcs;

    @Column(name = "no_sertifikat_spr")
    private String noSertifikatSpr;

    @Column(name = "tanggal_sertifikat_spr")
    private Date tanggalSertifikatSpr;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Date modifiedDate;

    @Column(name = "created_by", length = 50)
    @CreatedBy
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    @LastModifiedBy
    private String modifiedBy;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "keterangan_tolak")
    private String keteranganTolak;

    @Column(name = "ket_audit_trail")
    private String ketAuditTrail;

    @Column(name = "tanggal_rekam")
    private Date tanggal_rekam;

    @Column(name = "counter_rehit")
    private Integer counterRehit;

    @Column(name = "no_sertifikat_lama")
    private String noSertifikatLama;

    @Column(name = "flag_update_acs")
    private String flagUpdateAcs;

    @Column(name = "flag_sikp")
    private String flagSikp;

    @Column(name = "flag_pembatalan")
    private String flagPembatalan;

    @Column(name = "status_ijp")
    private String statusIjp;
}